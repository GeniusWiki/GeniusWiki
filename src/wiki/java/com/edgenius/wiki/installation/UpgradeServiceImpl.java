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
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.edgenius.core.Constants;
import com.edgenius.core.DataRoot;
import com.edgenius.core.Global;
import com.edgenius.core.GlobalSetting;
import com.edgenius.core.Server;
import com.edgenius.core.Version;
import com.edgenius.core.util.CompareToComparator;
import com.edgenius.core.util.FileUtil;
import com.edgenius.wiki.PageTheme;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.gwt.client.model.BlogCategory;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.thoughtworks.xstream.XStream;

/**
 * Please be careful on using this service bean, as it only runs in Installation application context, very limited 
 * service bean available in it.
 * 
 * @author Dapeng.Ni
 */
public class UpgradeServiceImpl implements UpgradeService {
	private static final Logger log = LoggerFactory.getLogger(UpgradeServiceImpl.class);

	private static final String FILTER_METHOD_MIDDLE = "To";

	// at moment only upgrade, down-grade is not support
	private static final String UPGRADE_FILTER_METHOD_PREFIX = "up";
	// this is for backup export package import back check method prefix
	private static final String BACKUP_EXPORT_BINDER_FILTER_METHOD_PREFIX = "exportBinder";
	private static final String BACKUP_EXPORT_DATAFILES_FILTER_METHOD_PREFIX = "exportDataFiles";

	public static boolean isSmoothUpgrade(String oldVerStr, String newVerStr){
		Map<Integer, Method> methodNeeds = getUpgradeFilterMethods(oldVerStr, newVerStr, UPGRADE_FILTER_METHOD_PREFIX);
		
		return !(methodNeeds != null && methodNeeds.size() > 0);
	}
	/**
	 * Execute any upgrade action between 2 version.
	 * 
	 * @return true, then means upgrade successfully.
	 */
	public void doBackupPackageUpgardeForBinder(String oldVerStr, File binderFile) throws Exception {
		Map<Integer, Method> methodNeeds = getUpgradeFilterMethods(oldVerStr, Version.VERSION, BACKUP_EXPORT_BINDER_FILTER_METHOD_PREFIX);
		executeFilters(methodNeeds, binderFile);
	}
	public void doBackupPackageUpgardeForDataFiles(String oldVerStr) throws Exception {
		Map<Integer, Method> methodNeeds = getUpgradeFilterMethods(oldVerStr, Version.VERSION, BACKUP_EXPORT_DATAFILES_FILTER_METHOD_PREFIX);
		executeFilters(methodNeeds);
		
	}
	
	public void doUpgarde(String oldVerStr, String newVerStr) throws Exception {
		Map<Integer, Method> methodNeeds = getUpgradeFilterMethods(oldVerStr, newVerStr, UPGRADE_FILTER_METHOD_PREFIX);
		executeFilters(methodNeeds);
	}
	/**
	 * @param methodNeeds
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void executeFilters(Map<Integer, Method> methodNeeds, Object... params) throws IllegalAccessException,
			InvocationTargetException {
		if(methodNeeds != null){
			Object filter = this;
			Collection<Method> calls = methodNeeds.values();
			for (Method method : calls) {
				method.invoke(filter, params);
				log.debug("Version filter class method " + method.getName() + " is executed.");
			}
		}
	}
	/**
	 * @param oldVerStr
	 * @param newVerStr
	 * @return 
	 */
	private static Map<Integer, Method> getUpgradeFilterMethods(String oldVerStr, String newVerStr, String prefix) {
		// basic assumption: version MUST be float format. version the precision
		// must less or equal 3
		// For example, valid version is 0.879 or 1.0 or 2.12
		int oldVer = (int) (NumberUtils.toFloat(oldVerStr, 1f) * 1000);
		int newVer = (int) (NumberUtils.toFloat(newVerStr, 1f) * 1000);

		if (oldVer < newVer) {
			log.debug("Version filter class will filter from version " + oldVer + " to " + newVer);

			
			Method[] methods = UpgradeServiceImpl.class.getDeclaredMethods();
			
			Map<Integer, Method> methodNeeds = new TreeMap<Integer, Method>(new CompareToComparator());
			for (Method method : methods) {
				String name = method.getName();
				if (name.startsWith(prefix)) {
					String[] ver = name.split(prefix + "|" + FILTER_METHOD_MIDDLE);
					int mfrom = 0;
					int mto = 0;
					for (int idx = 0; idx < ver.length; idx++) {
						if (StringUtils.isBlank(ver[idx])) {
							continue;
						}
						mfrom = NumberUtils.toInt(ver[idx]);
						if (ver.length > idx) {
							mto = NumberUtils.toInt(ver[++idx]);
						}
						break;
					}
					if (mfrom >= oldVer && mto <= newVer) {
						methodNeeds.put(mfrom, method);
					}
				}
			}
			return methodNeeds;
		}
		
		return null;
	}
	
	/**
	 * Upgrade from version 1.0 to 1.01
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	private void up1000To1010() throws Exception{
		log.info("Version 1.0 to 1.01 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// dateabase
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			String migrateSQL = type+"-1000-1010.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Global.xml : add new DefaultNotifyMail
		String gfile = DataRoot.getDataRoot()+Global.FILE;
		GlobalSetting gs = GlobalSetting.loadGlobalSetting(FileUtil.getFileInputStream(gfile));
		gs.setDefaultNotifyMail(Global.DefaultNotifyMail==null?"notify@geniuswiki.com":Global.DefaultNotifyMail);
		gs.saveTo(gfile);
		Global.syncTo(gs);
		
		Server server = new Server();
		Properties prop = FileUtil.loadProperties(root+Server.FILE);
		server.syncFrom(prop);
		server.setDbJNDI("");
		server.setMqServerEmbedded("true");
		//save
		server.syncTo(prop);
		prop.store(FileUtil.getFileOutputStream(root+Server.FILE), "save by server");

	}
	/**
	 * Upgrade from version 1.01 to 1.1
	 */
	@SuppressWarnings("unused")
	private void up1010To1020() throws Exception{
		log.info("Version 1.02 to 1.02 is upgarding");
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			String migrateSQL = type+"-1010-1020.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
	}
	
	
	/**
	 * Upgrade from version 1.1 to 1.2
	 */
	@SuppressWarnings("unused")
	private void up1100To1200() throws Exception{
		log.info("Version 1.1 to 1.2 is upgarding");
		
		String root = DataRoot.getDataRoot();
		Server server = new Server();
		Properties prop = FileUtil.loadProperties(root+Server.FILE);
		server.syncFrom(prop);
		server.setDbType("mysql");
		server.setDbConnectType("jndi");
		server.setQuartzJobstoreDriver("org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
		
		//save
		server.syncTo(prop);
		prop.store(FileUtil.getFileOutputStream(root+Server.FILE), "save by server");
	}
	
	@SuppressWarnings("unused")
	private void up1543To1544() throws Exception{
		log.info("Version 1.543 to 1.544 is upgarding");
		
		String root = DataRoot.getDataRoot();
		//upgrade database
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			String migrateSQL = type+"-1543-1544.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
	}
	@SuppressWarnings("unused")
	private void up1544To1550() throws Exception{
		log.info("Version 1.544 to 1.55 is upgarding");
		
		//update global.xml - fix a word quote->quota
		String root = DataRoot.getDataRoot();
		String gfile = DataRoot.getDataRoot()+Global.FILE;
		
		String gstr = FileUtils.readFileToString(FileUtil.getFile(gfile), Constants.UTF8);
		gstr = gstr.replace("<spaceQuote>", "<spaceQuota>");
		gstr = gstr.replace("</spaceQuote>", "</spaceQuota>");
		
		FileUtils.writeStringToFile(FileUtil.getFile(gfile), gstr);
		
		//upgrade database
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			String migrateSQL = type+"-1544-1550.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(),server.getDbSchema(),server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
	}

	@SuppressWarnings("unused")
	private void up1550To1551() throws Exception{
		log.info("Version 1.55 to 1.551 is upgarding");
		
		//add server.db.schema 
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			server.setDbSchema("");
			server.syncTo(prop);
			prop.store(FileUtil.getFileOutputStream(root+Server.FILE), "save by server");
			
		}
	}
	@SuppressWarnings("unused")
	private void up1562To1563() throws Exception{
		log.info("Version 1.562 to 1.563 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			String migrateSQL = type+"-1562-1563.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
	}
	@SuppressWarnings("unused")
	private void up1620To1630() throws Exception{
		log.info("Version 1.62 to 1.63 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			String migrateSQL = type+"-1620-1630.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
	}
	
	@SuppressWarnings("unused")
	private void up1660To1660() throws Exception{
		log.info("Version 1.66 to 1.67 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			String migrateSQL = type+"-1660-1670.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
	}
	
	
	@SuppressWarnings("unused")
	private void up1710To1720() throws Exception{
		log.info("Version 1.71 to 1.72 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			String migrateSQL = type+"-1710-1720.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
	}
	@SuppressWarnings("unused")
	private void up1800To1810() throws Exception{
		log.info("Version 1.8 to 1.81 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			
//			//merge title and body of NOTIFICATION
//			PreparedStatement stat1 = con.prepareStatement("select PUID, TITLE, NOTIFY_MESSAGE from EDG_NOTIFICATION");
//			ResultSet rs = stat1.executeQuery();
//			Map<Integer, String> map = new HashMap<Integer, String>();
//			while(rs.next()){
//				int ID = rs.getInt(1);
//				String title = StringUtils.trimToEmpty(rs.getString(2));
//				String merge = title + (title.endsWith(".")?" ":". ")+ StringUtils.trimToEmpty(rs.getString(3));
//				map.put(ID, merge);
//			}
//			rs.close();
//			stat1.close();
//			stat1 = con.prepareStatement("update EDG_NOTIFICATION set NOTIFY_MESSAGE=? where PUID=?");
//			for (Entry<Integer, String> entry : map.entrySet()) {
//				stat1.setString(1, entry.getValue());
//				stat1.setInt(2, entry.getKey());
//				stat1.executeUpdate();
//			}
//			stat1.close();
//			
//			//merge title and body of NOTIFICATION_RESPONSE
//			stat1 = con.prepareStatement("select PUID, TITLE, REPONSE_MESSAGE from EDG_NOTIFICATION_RESPONSE");
//			rs = stat1.executeQuery();
//			map = new HashMap<Integer, String>();
//			while(rs.next()){
//				int ID = rs.getInt(1);
//				String title = StringUtils.trimToEmpty(rs.getString(2));
//				String merge = title + (title.endsWith(".")?" ":". ")+ StringUtils.trimToEmpty(rs.getString(3));
//				map.put(ID, merge);
//			}
//			rs.close();
//			stat1.close();
//			stat1 = con.prepareStatement("update EDG_NOTIFICATION_RESPONSE set REPONSE_MESSAGE=? where PUID=?");
//			for (Entry<Integer, String> entry : map.entrySet()) {
//				stat1.setString(1, entry.getValue());
//				stat1.setInt(2, entry.getKey());
//				stat1.executeUpdate();
//			}
//			stat1.close();
			
			//remove field of TITLE
			String migrateSQL = type+"-1800-1810.sql";
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
	}
	
	@SuppressWarnings("unused")
	private void up1921To1930() throws Exception{
		log.info("Version 1.921 to 1.93 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			
			//merge title and body of NOTIFICATION
			PreparedStatement stat1 = con.prepareStatement("select PUID, CONTENT from EDG_WIDGETS where WIDGET_TYPE='com.edgenius.wiki.widget.PageLinkerWidgetTemplate'");
			ResultSet rs = stat1.executeQuery();
			Map<Integer, String> map = new HashMap<Integer, String>();
			while(rs.next()){
				int ID = rs.getInt(1);
				String content = StringUtils.trimToEmpty(rs.getString(2));
				String[] parts = content.split("(\\$SP)");
				if(parts.length != 2){
					log.error("Unable to found separator $SP in page link widget");
					continue;
				}
					
				String merge = "{include:src="+EscapeUtil.escapeMacroParam(parts[0])+ "@"+EscapeUtil.escapeMacroParam(parts[1])+"}"; 
				map.put(ID, merge);
			}
			rs.close();
			stat1.close();
			
			stat1 = con.prepareStatement("update EDG_WIDGETS set CONTENT=? where PUID=?");
			for (Entry<Integer, String> entry : map.entrySet()) {
				stat1.setString(1, entry.getValue());
				stat1.setInt(2, entry.getKey());
				stat1.executeUpdate();
			}
			stat1.close();
			
			con.close();
		}
	}
	
	@SuppressWarnings("unused")
	private void up1930To1940() throws Exception{
		log.info("Version 1.93 to 1.94 is upgarding");
		
		//update global.xml - fix a word quote->quota
		String root = DataRoot.getDataRoot();
		String gfile = DataRoot.getDataRoot()+Global.FILE;
		
		String gstr = FileUtils.readFileToString(FileUtil.getFile(gfile), Constants.UTF8);
		gstr = gstr.replace("<openRegister>0</openRegister>", "<registerMethod>signup</registerMethod>");
		
		FileUtils.writeStringToFile(FileUtil.getFile(gfile), gstr);
		
	}
	@SuppressWarnings("unused")
	private void up1940To1950() throws Exception{
		log.info("Version 1.94 to 1.95 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			String migrateSQL = type+"-1940-1950.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
		
	}
	@SuppressWarnings("unused")
	private void up1950To1960() throws Exception{
		log.info("Version 1.95 to 1.96 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			String migrateSQL = type+"-1950-1960.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
		
	}
	@SuppressWarnings("unused")
	private void up1960To1970() throws Exception{
		log.info("Version 1.96 to 1.97 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			
			//update space attribute
			PreparedStatement stat1 = con.prepareStatement("select f.PUID, f.SETTING_VALUE,s.PUID  " +
					"from EDG_CONF as f, EDG_SPACES as s where f.SETTING_TYPE='com.edgenius.wiki.SpaceSetting' and s.CONFIGURATION_PUID=f.PUID");
			ResultSet rs = stat1.executeQuery();
			Map<Integer, String> map = new HashMap<Integer, String>();
			Map<Integer, String> keyMap = new HashMap<Integer, String>();
			while(rs.next()){
				int ID = rs.getInt(1);
				String content = StringUtils.trimToEmpty(rs.getString(2));
				int sID = rs.getInt(3);
				if(content.indexOf("<linkedMeta>") != -1){
					String blogKey =UUID.randomUUID().toString();
					keyMap.put(sID,blogKey);
					content = StringUtils.replace(content, "<linkedMeta>", "<linkedMetas><blogMeta><key>"+blogKey+"</key>");
					content = StringUtils.replace(content, "</linkedMeta>", " </blogMeta></linkedMetas>");
					content = StringUtils.replace(content, "<com.edgenius.wiki.gwt.client.model.BlogCategory>", " <blogCategory>");
					content = StringUtils.replace(content, "</com.edgenius.wiki.gwt.client.model.BlogCategory>", " </blogCategory>");
					map.put(ID, content);
				}
			}
			rs.close();
			stat1.close();
			
			if(map.size() > 0){
				stat1 = con.prepareStatement("update EDG_CONF set SETTING_VALUE=? where PUID=?");
				for (Entry<Integer, String> entry : map.entrySet()) {
					stat1.setString(1, entry.getValue());
					stat1.setInt(2, entry.getKey());
					stat1.executeUpdate();
				}
				stat1.close();
			}
			
			
			//update page attribute
			stat1 = con.prepareStatement("select g.PUID, g.LINK_EXTRA_INFO,p.SPACE_PUID " +
					"from EDG_PAGES_PROGRESS as g, EDG_PAGES as p where LINK_EXTRA_INFO is not null and g.PUID=p.PROGRESS_PUID");
			
			rs = stat1.executeQuery();
			map = new HashMap<Integer, String>();
			while(rs.next()){
				int ID = rs.getInt(1);
				String content = StringUtils.trimToEmpty(rs.getString(2));
				int sID = rs.getInt(3);
				if(content.indexOf("<BlogPostMeta>") != -1){
					content = StringUtils.replace(content, "<BlogPostMeta>", "<list><blogPostMeta><blogKey>"+keyMap.get(sID)+"</blogKey>");
					content = StringUtils.replace(content, "</BlogPostMeta>", " </blogPostMeta></list>");
					map.put(ID, content);
				}
			}
			rs.close();
			stat1.close();
			
			if(map.size() > 0){
				stat1 = con.prepareStatement("update EDG_PAGES_PROGRESS set LINK_EXTRA_INFO=? where PUID=?");
				for (Entry<Integer, String> entry : map.entrySet()) {
					stat1.setString(1, entry.getValue());
					stat1.setInt(2, entry.getKey());
					stat1.executeUpdate();
				}
				stat1.close();
			}
			con.close();
		}
		
	}
	
	@SuppressWarnings("unused")
	private void up1990To1991() throws Exception{
		log.info("Version 1.99 to 1.991 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			String migrateSQL = type+"-1990-1991.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
		
	}
	@SuppressWarnings("unused")
	private void up1991To1992() throws Exception{
		log.info("Version 1.991 to 1.992 is upgarding");
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			
			Statement stat = con.createStatement();
			ResultSet rs0 = stat.executeQuery("select PUID from EDG_ROLES where NAME='ROLE_SYS_USERS'");
			if(rs0.next()){
				int roleID = rs0.getInt(1);
				rs0.close();
				
				PreparedStatement pre0 = con.prepareStatement("select distinct USER_PUID from EDG_USER_ROLE where USER_PUID in (select PUID from EDG_USERS where PUID not in "
						+" (select u.PUID from EDG_USERS as u, EDG_USER_ROLE as r where u.PUID=r.USER_PUID and r.ROLE_PUID=?))");
				pre0.setInt(1, roleID);
				ResultSet rs1 = pre0.executeQuery();
				
				PreparedStatement pre = con.prepareStatement("insert into EDG_USER_ROLE(USER_PUID,ROLE_PUID) values(?,?)");
				while(rs1.next()){
					pre.setInt(1, rs1.getInt(1));
					pre.setInt(2, roleID);
					pre.addBatch();
				}
				pre.executeBatch();
				
				rs1.close();
				pre0.close();
				pre.close();
			}
			stat.close();
			con.close();
		}
	}
	
	@SuppressWarnings("unused")
	private void up1992To1993() throws Exception{
		log.info("Version 1.992 to 1.993 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			if(!"postgresql".equalsIgnoreCase(type))
				return;
			String migrateSQL = type+"-1992-1993.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
		
	}
	@SuppressWarnings("unused")
	private void up2000To2010() throws Exception{
		log.info("Version 2.0 to 2.01 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			String migrateSQL = type+"-2000-2010.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//update global.xml - fix a word indexOptimizeCron->maintainJobCron
		String gfile = DataRoot.getDataRoot()+Global.FILE;
		
		String gstr = FileUtils.readFileToString(FileUtil.getFile(gfile), Constants.UTF8);
		gstr = gstr.replace("<indexOptimizeCron>", "<maintainJobCron>");
		gstr = gstr.replace("</indexOptimizeCron>", "</maintainJobCron>");
		gstr = gstr.replace("</com.edgenius.core.GlobalSetting>","<purgeDaysOldActivityLog>365</purgeDaysOldActivityLog></com.edgenius.core.GlobalSetting>");
		
		FileUtils.writeStringToFile(FileUtil.getFile(gfile), gstr);
		
	}
	@SuppressWarnings("unused")
	private void up2130To2140() throws Exception{
		log.info("Version 2.13 to 2.14 is upgarding");
		
		//detect server.properties, add 2 new attributes, if mail user is not empty, also enable auth attribute
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			prop.setProperty("mail.smtp.jndi", "");
			prop.setProperty("mail.smtp.properties", "");
			
			Server server = new Server();
			server.syncFrom(prop);
			if(!StringUtils.isEmpty(server.getMailUsername())){ //?? should also check password?
				server.setMailProperties("mail.smtp.auth:true");
			}
			
			server.syncTo(prop);
			prop.store(FileUtil.getFileOutputStream(root+Server.FILE), "save by system program");
		}
	}
	@SuppressWarnings("unused")
	private void up2170To2180() throws Exception{
		log.info("Version 2.17 to 2.18 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			String migrateSQL = type+"-2170-2180.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(type, migrateSQL,con);
			con.close();
		}
		
	}
	@SuppressWarnings("unused")
	private void up2180To2190() throws Exception{
		log.info("Version 2.18 to 2.19 is upgarding");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//update global.xml - fix a word indexOptimizeCron->maintainJobCron
		String gfile = DataRoot.getDataRoot()+Global.FILE;
		
		String gstr = FileUtils.readFileToString(FileUtil.getFile(gfile), Constants.UTF8);
		gstr = gstr.replace("<defaultSkin>", "<skin>");
		gstr = gstr.replace("</defaultSkin>", "</skin>");
		gstr = gstr.replaceAll("<defaultTheme>[^<]*</defaultTheme>", "");
		gstr = gstr.replace("</com.edgenius.core.GlobalSetting>"," <textnut>disabled</textnut>\n</com.edgenius.core.GlobalSetting>");
		
		FileUtils.writeStringToFile(FileUtil.getFile(gfile), gstr);
		
		String root = DataRoot.getDataRoot();
		mergeCustomizedThemesOnVersion2180(root);
	}
	/**
	 * @param root
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void mergeCustomizedThemesOnVersion2180(String root) throws ParserConfigurationException, SAXException,
			IOException {
		
		File rootFile = FileUtil.getFile(root);
		File dir = new File(rootFile,"data/themes/customized");
		if(dir.exists() && dir.isDirectory()){
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setCoalescing(true);
			factory.setIgnoringComments(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Map<Integer,List<PageTheme>> map = new HashMap<Integer, List<PageTheme>>();
			File[] files = dir.listFiles((FilenameFilter)FileFilterUtils.suffixFileFilter(".xml"));
			for (File file : files) {
				//get space setting - file.getName();
				//parse customized theme, and get back the 
				List<PageTheme> list = new ArrayList<PageTheme>();
				Document dom = builder.parse(file);
				NodeList elements = dom.getElementsByTagName("com.edgenius.wiki.PageTheme");
				for(int idx=0;idx<elements.getLength();idx++){
					PageTheme pTheme = new PageTheme();
					Node element = elements.item(idx);
					NodeList children = element.getChildNodes();
					
					for (int idj = 0;idj < children.getLength(); idj++) {
						String value = children.item(idj).getTextContent();
						if("bodyMarkup".equals(children.item(idj).getNodeName())){
							pTheme.setBodyMarkup(value);
						}else if("sidebarMarkup".equals(children.item(idj).getNodeName())){
							pTheme.setSidebarMarkup(value);
						}else if("welcome".equals(children.item(idj).getNodeName())){
							pTheme.setWelcome(value);
						}else if("type".equals(children.item(idj).getNodeName())){
							String scope;
							if("0".equals(value)){
								scope = PageTheme.SCOPE_DEFAULT;
							}else if("1".equals(value)){
								scope = PageTheme.SCOPE_HOME;
							}else{
								scope = value;
							}
							pTheme.setScope(scope);
						}
					}
					list.add(pTheme);
				}
				if(!file.delete()){
					file.deleteOnExit();
				}
				if(list.size() > 0){
					int uid = NumberUtils.toInt(file.getName().substring(0,file.getName().length()-4),-1);
					if(uid != -1){
						map.put(uid, list);
					}
				}
				
			}
			//Convert  <com.edgenius.wiki.PageTheme> to <PageTheme>
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String type = server.getDbType();
			
			DBLoader loader = new DBLoader();
			ConnectionProxy conn = null;
			
			XStream xs = new XStream();
			xs.processAnnotations(PageTheme.class);
			xs.processAnnotations(BlogMeta.class);
			xs.processAnnotations(BlogCategory.class);
			
			try {
				conn = loader.getConnection(type,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
				for (Entry<Integer,List<PageTheme>> entry: map.entrySet()) {
					PreparedStatement stat1 = null, stat2 = null;
					ResultSet rs = null;
					try {
						stat1 = conn.prepareStatement("select f.PUID, f.SETTING_VALUE,s.PUID  " +
						"from EDG_CONF as f, EDG_SPACES as s where f.SETTING_TYPE='com.edgenius.wiki.SpaceSetting' " +
						"and s.CONFIGURATION_PUID=f.PUID and s.PUID=?");
						stat2 = conn.prepareStatement("update EDG_CONF set SETTING_VALUE=? where PUID=?");
						stat1.setInt(1, entry.getKey());
						rs = stat1.executeQuery();
						if(rs.next()){
							int ID = rs.getInt(1);
							SpaceSetting setting = (SpaceSetting) xs.fromXML(StringUtils.trimToEmpty(rs.getString(2)));
							setting.setPageThemes(entry.getValue());
							String content = xs.toXML(setting);
							//update
							stat2.setString(1, content);
							stat2.setInt(2, ID);
							stat2.executeUpdate();
							log.info("Update space setting  {} for page theme ", ID);
						}
					} catch (Exception e) {
						log.error("Update space setting failed " + entry.getKey(),e);
					}finally{
						if(rs != null) rs.close();
						if(stat1 != null) stat1.close();
						if(stat2 != null) stat2.close();
					}
				}
			} catch (Exception e) {
				log.error("update space setting failed with PageTheme",e);
			} finally{
				if(conn != null) conn.close();
			}
			
			//delete unnecessary files
			File f1 = new File(rootFile,"data/themes/defaultwiki.xml");
			f1.delete();
			File f2 = new File(rootFile,"data/themes/defaultblog.xml");
			f2.delete();
			File f3 = new File(rootFile,"data/themes/customized");
			if(f3.isDirectory() && f3.list().length == 0){
				f3.delete();
			}
			
		}else{
			log.error("Unable to parse out theme directory {}", (root+"data/themes/customized"));
		}
	}


	@SuppressWarnings("unused")
	private void up3000To3100() throws Exception{
		log.info("Version 3.0 to 3.1 is upgarding");
		
		String root = DataRoot.getDataRoot();
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			if(server.getMqServerEmbedded() == null || BooleanUtils.toBoolean(server.getMqServerEmbedded())){
			    //embedded
				server.setMqServerUrl("tcp://" + server.getMqServerUrl() + "?wireFormat.maxInactivityDuration=0");
				server.syncTo(prop);
				prop.store(FileUtil.getFileOutputStream(root+Server.FILE), "save by system program");
			}
		}
		
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// database - remove all quartz tables - we don't backup Exportable job(backup and remove space) - it is not perfect but not big issue.
		if(FileUtil.exist(root+Server.FILE)){
			Server server = new Server();
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
			String dbType = server.getDbType();
		
			String migrateSQL = dbType+"-3000-3100.sql";
			DBLoader loader = new DBLoader();
			ConnectionProxy con = loader.getConnection(dbType,server.getDbUrl(), server.getDbSchema(), server.getDbUsername(),server.getDbPassword());
			loader.runSQLFile(dbType, migrateSQL,con);
			
			//reload quartz table
			log.info("Initialize quartz tables for system...");
			Statement stat = con.createStatement();
			Statement dropStat = con.createStatement();
			List<String> lines = loader.loadSQLFile(dbType,  dbType+"-quartz.sql");
			for (String sql : lines) {
				sql = sql.replaceAll("\n", " ").trim();
				if(sql.toLowerCase().startsWith("drop ")){
					try {
						dropStat.execute(sql);
					} catch (Exception e) {
						log.error("Drop operation failed...." + sql);
					}
					continue;
				}
				stat.addBatch(sql);
			}
			stat.executeBatch();
			
			dropStat.close();
			stat.close();
			con.close();
		}
		
	}

	
	//********************************************************************
	//               Method for backup export upgrade
	//********************************************************************
	@SuppressWarnings("unused")
	private void exportBinder1544To1550(File binderFile) throws Exception{
		log.info("Version 1.544 to 1.55 is upgarding backup export package");
		String binder = FileUtils.readFileToString(binderFile, Constants.UTF8);
		binder = binder.replaceAll("<quote>", "<quota>");
		binder = binder.replaceAll("</quote>", "</quota>");
		
		FileUtils.writeStringToFile(binderFile, binder, Constants.UTF8);
		
	}
	@SuppressWarnings("unused")
	private void exportDataFiles2180To2190() throws Exception{
		log.info("Version 2.18 to 2.19 is upgarding on data root - themes");
		
		String root = DataRoot.getDataRoot();
		mergeCustomizedThemesOnVersion2180(root);
	}
}
