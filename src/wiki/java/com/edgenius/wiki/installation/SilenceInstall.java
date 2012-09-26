/* 
 * =============================================================
 * Copyright (C) 2007-2011 Edgenius (http://www.edgenius.com)
 * =============================================================
 * License Information: http://www.edgenius.com/licensing/edgenius/2.0/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 *  
 * ****************************************************************
 */
package com.edgenius.wiki.installation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import com.edgenius.core.DataRoot;
import com.edgenius.core.Global;
import com.edgenius.core.GlobalSetting;
import com.edgenius.core.Installation;
import com.edgenius.core.Server;
import com.edgenius.core.util.FileUtil;

/**
 * Silence install uses a property file or system properties to 
 * 1) Create global.xml, server.properties,installation.xml files.
 * 2) Create database(optional, MySQL only) and tables.
 * 3) Add administrator account to table.
 * 
 * If data root already has installation.xml files, the program does nothing.
 * 
 * Now, this program is used for hosting service and standalone installation.
 * 
 * silence-install.properites must include following properties:
 * log.dir
 * data.root.in.system.property (identify if put data root value into System.setProperty())
 * data.root
 * database.root.username
 * database.root.password
 * admin.fullname
 * admin.username
 * admin.password
 * admin.email
 * 
 * the others properties will replace the values from Server.properties, Global.xml and Installation.properties, 
 * if there are same name value existed, for example:
 * "license" will replace the one in  Installation.xml
 * "mqServerUrl" will replace  the one in Server.properties -- PLEASE NOTE: the property name is field name in class, rather than server.properties, i.e, "mq.server.embedded"
 * "hostName" will replace the one in Global.xml 
 * 
 * silence-install.properties value can be override by System.getPropety()
 * @author Dapeng.Ni
 */
public class SilenceInstall {
	
	static Logger log = LoggerFactory.getLogger(SilenceInstall.class);
	//all keys in this list won't do mapping to configuration files (i.e., Installation.xml, Server.properties and Global.xml)
	private static final List<String> DEFAULT_KEYS = Arrays.asList("log.dir", "data.root","database.root.username","database.root.password"
		,"admin.fullname","admin.username","admin.password","admin.email", "create.database");
	
	private static Set<String> serverFields = new HashSet<String>();
	private static Set<String> globalFields = new HashSet<String>();
	private static Set<String> installFields = new HashSet<String>();

	
	public static void main(String[] args) throws FileNotFoundException, IOException, NumberFormatException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		if(args.length != 1){
			System.out.println("Usage: SilenceInstall silence-install.properites");
			System.exit(1);
			return;
		}
		if (!(new File(args[0]).exists())){
			System.out.println("Given silence-install.properites not found: [" + args[0] + "]");
			System.exit(1);
			return;
		}
		SilenceInstall silence = new SilenceInstall();
		Properties prop = new Properties();
		prop.load(new FileInputStream(args[0]));
		
		log.info("Silence installation starting... on properties: {}", args[0]);
		
		if(Boolean.parseBoolean(getProperty(prop,"data.root.in.system.property"))){
			System.setProperty(DataRoot.rootKey, getProperty(prop,"data.root"));
			log.info("Date root is set to System Properties {}", getProperty(prop,"data.root"));
		}
		try {
			Field[] flds = Class.forName(Server.class.getName()).getDeclaredFields();
			for (Field field : flds) {
				serverFields.add(field.getName());
			}
			flds = Class.forName(GlobalSetting.class.getName()).getDeclaredFields();
			for (Field field : flds) {
				globalFields.add(field.getName());
			}
			flds = Class.forName(Installation.class.getName()).getDeclaredFields();
			for (Field field : flds) {
				installFields.add(field.getName());
			}
		} catch (Exception e) {
			log.error("Load fields name failed",e);
			System.exit(1);
		}
		
	

		boolean succ = silence.createDataRoot(getProperty(prop,"data.root"));
		if(!succ){
			log.error("Unable to complete create data root");
			return;
		}
		//detect if Install.xml exist and if it is already installed.
		File installFile = FileUtil.getFile(DataRoot.getDataRoot()+Installation.FILE);
		if(installFile.exists()){
			Installation install = Installation.refreshInstallation();
			if(Installation.STATUS_COMPLETED.equals(install.getStatus())){
				log.info("GeniusWiki is already installed, exit this installation.");
				System.exit(0);
			}
		}
		
		//load Server.properties, Global.xml and Installation.properties
		Server server = new Server();
		Properties serverProp = FileUtil.loadProperties(Server.FILE_DEFAULT);
		server.syncFrom(serverProp);
		GlobalSetting global = GlobalSetting.loadGlobalSetting(FileUtil.getFileInputStream(Global.DEFAULT_GLOBAL_XML));
		Installation  install = Installation.loadDefault();
		
		//sync values from silence-install.properites
		silence.sync(prop, server, global, install);
		
		//install....
		succ = silence.setupDataRoot(server, global, install);
		if(!succ){
			log.error("Unable to complete save configuration files to data root");
			return;
		}
		
		if(Boolean.parseBoolean(getProperty(prop,"create.database"))){
			succ = silence.createDatabase(server,getProperty(prop,"database.root.username"),getProperty(prop,"database.root.password"));
			if(!succ){
				log.error("Unable to complete create database");
				return;
			}
		}
		
		succ = silence.createTable(server);
		if(!succ){
			log.error("Unable to complete create tables");
			return;
		}
		succ = silence.createAdministrator(server,getProperty(prop,"admin.fullname"), getProperty(prop,"admin.username")
				,getProperty(prop,"admin.password"), getProperty(prop,"admin.email"));
		if(!succ){
			log.error("Unable to complete create administrator");
			return;
		}
		
		log.info("Silence installation completed successfully.");
	}
	

	/**
	 * @param silence
	 * @param server
	 * @param global
	 * @param install
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NumberFormatException 
	 * @throws InvocationTargetException 
	 */
	private void sync(Properties prop, Server server, GlobalSetting global, Installation install) throws NumberFormatException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		//AUTO detect country and timezone if properties file not set.
		if(StringUtils.isBlank(prop.getProperty("defaultCountry"))){
			prop.setProperty("defaultCountry", Locale.getDefault().getCountry());
		}
		if(StringUtils.isBlank(prop.getProperty("defaultTimeZone"))){
			prop.setProperty("defaultTimeZone", TimeZone.getDefault().getID());
		}
		for(Iterator<Object> iter = prop.keySet().iterator();iter.hasNext();){
			String key = (String) iter.next();
			if( DEFAULT_KEYS.contains(key))
				continue;

			Object obj = null;
			if(serverFields.contains(key)){
				obj = server;
			}else if(globalFields.contains(key)){
				obj = global;
			}else if(installFields.contains(key)){
				obj = install;
			}
			if(obj == null){
				log.warn("Invalid field name {} cann't find in any configuration files - could only configuration option for silence installation", key);
				continue;
			}
			BeanUtils.setProperty(obj, key, getProperty(prop,key));
		}
		
	}
	
	/**
	 * First try to get property value from system properties, if not exist, then get from Properties file. 
	 */
	private static String getProperty(Properties prop, String key) {
		String value = System.getProperty(key);
		if(StringUtils.isBlank(value))
			return prop.getProperty(key);
		return value;
	}


	/**
	 * @param prop
	 * @throws IOException
	 */
	private boolean createDataRoot(String root) throws IOException {
		root = DataRoot.saveDataRoot(root);
		File file = FileUtil.getFile(root);
		if(!FileUtil.exist(root)){
			if(!file.mkdirs()){
				log.error("Unable to create directory, please create manually.");
				return false;
			}
		}else if(!file.isDirectory()){
			log.error("The value is not directory.");
			return false;
		}
		return true;
	}
	
	private boolean setupDataRoot(Server server, GlobalSetting global, Installation install) throws IOException{
		Installation.saveInstallation(install);
		global.saveTo(DataRoot.getDataRoot() + Global.FILE);
		server.saveTo();
		return true;
	}
	private boolean createDatabase(Server server, String rootUser,String rootpass){
		DBLoader loader = new DBLoader();
		ConnectionProxy con = null;
		try {
			String host = DBLoader.detectHost(server.getDbType(),server.getDbUrl());
			String dbname  = DBLoader.detectDBName(server.getDbType(),server.getDbUrl());
			log.info("Creating DB {} for user {}", dbname, server.getDbUsername());
			if(!loader.isDBExist(server.getDbType(),host, dbname, server.getDbSchema(), rootUser, rootpass)){
				//driverType use null - aka, default driver.
				String adminDBUrl = loader.getURL(server.getDbType(),null, host,dbname, true); 
				con = loader.getConnection(server.getDbType(), adminDBUrl, server.getDbSchema(), rootUser, rootpass);
				log.info("DB {} is going to create or reset", dbname);
				//at moment, only MySQL allows to create DB, and its schema is null.
				loader.resetDB(server.getDbType(), con, dbname,server.getDbUsername(),server.getDbPassword());
				//success init DB
				return true;
			}else{
				log.info("DB {} is existed", dbname);
				return false;
			}
		} catch (DriverNotFoundException e) {
			log.error("DB driver not found", e);
			return false;
		} catch (SQLException e) {
			log.error("SQL error", e.getNextException());
			return false;
		} catch (Exception e) {
			log.error("Unable complete database initialize task",e);
			return false;
		}finally{
			if(con != null)
				con.close();
		}
	
	}
	private boolean createTable(Server server){
		DBLoader loader = new DBLoader();
		ConnectionProxy con = null;
		try {
			log.info("Get connection for {} ", server.getDbType());
			con = loader.getConnection(server.getDbType(), server);
			if(con == null){
				return false;
			}
			
			//pre-check if tables exist or not
			if(!loader.isTableExist(con)){
				log.info("Reset table for database type {} " + server.getDbType());
				loader.resetTable(server.getDbType(), con);
				return true;
			}else{
				return false;
			}
		} catch (DriverNotFoundException e) {
			log.error("Driver not found ", e);
			return false;
		} catch (SQLException e) {
			log.error("SQL error {}" , e.toString(), e);
			return false;
		} catch (Exception e) {
			log.error("Unable complete table initialize task",e);
			return false;
		}finally{
			if(con !=null)
				con.close();
		}
	}
	private boolean createAdministrator(Server server, String fullname,String username,String password,String email){
		DBLoader loader = new DBLoader();
		ConnectionProxy con = null;
		try {
			con = loader.getConnection(server.getDbType(),server);
			loader.createUser(con,server.getDbType(), fullname,username,password,email);
			//success init DB
			return true;
		} catch (SQLException e) {
			log.error("Unable complete admin user initialize task",e);
			return false;
		} catch (Exception e) {
			log.error("Unable complete admin user initialize task",e);
			return false;
		}finally{
			if(con != null)
				con.close();
		}
	}
	
}
