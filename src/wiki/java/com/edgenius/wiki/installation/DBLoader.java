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

import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.Global;
import com.edgenius.core.Server;
import com.edgenius.core.util.CodecUtil;
import com.edgenius.core.util.FileUtil;

/**
 * @author Dapeng.Ni
 */
public class DBLoader {
	//those 2 tokens also use in JSP ,so it can not be ANT standard token model
	private static final String TOKEN_DBNAME = "@DBNAME@";
	private static final String TOKEN_HOST = "@HOST@";
	private static final String TOKEN_TABLE_PREFIX = "@TOKEN.TABLE.PREFIX@";
	
	private static final Logger log = LoggerFactory.getLogger(DBLoader.class);
	private static Properties prototype = new Properties();
	static{
		try {
			prototype = FileUtil.loadProperties("classpath:geniuswiki/setup.properties");
		} catch (IOException e) {
			log.error("unable load setup.properties file.",e);
		}
	}
	
	// This is not good about DBLoader, but need read from setup.properties, so just put it here
	public static String detectQuartzJobstoreDriver(String dbType){
		return prototype.getProperty(dbType+".quartz.jobstore.driver");
	}
	/**
	 * Return host(IP) and port
	 * @param type
	 * @param url
	 * @return
	 */
	public static String detectHost(String type, String url) {
		if(Server.DBTYPE_MYSQL.equalsIgnoreCase(type)){
			//sample URL-- jdbc:mysql://localhost/steveneo_edgenius?autoReconnect=true&useUnicode=true&characterEncoding=utf-8
			Pattern p = Pattern.compile("[^/]+//([^/]+)/.*");
			Matcher m = p.matcher(url);
			if(m.find()){
				return m.group(1);
			}
			
		}else if(Server.DBTYPE_POSTGRESQL.equalsIgnoreCase(type)){
			//sample URL:  jdbc:postgresql://localhost/geniuswiki?useUnicode=true&characterEncoding=utf-8
			Pattern p = Pattern.compile("[^/]+//([^/]+)/.*");
			Matcher m = p.matcher(url);
			if(m.find()){
				return m.group(1);
			}
		}else if(Server.DBTYPE_ORACLE9I.equalsIgnoreCase(type)){
			//sample URL:  jdbc:oracle:thin:@localhost:1521:geniuswiki
			Pattern p = Pattern.compile(".*@(.+\\:\\d+):.*");
			Matcher m = p.matcher(url);
			if(m.find()){
				return m.group(1);
			}
		}else if(Server.DBTYPE_DB2.equalsIgnoreCase(type)){
			//sample URL:  jdbc:db2://localhost:50000/geniuswiki
			Pattern p = Pattern.compile("[^/]+//([^/]+)/.*");
			Matcher m = p.matcher(url);
			if(m.find()){
				return m.group(1);
			}
		}else if(Server.DBTYPE_HSQLDB.equalsIgnoreCase(type)){
			//sample URL:  jdbc:hsqldb:hsql://localhost:9001/geniuswiki
			Pattern p = Pattern.compile("[^/]+//([^/]+)/.*");
			Matcher m = p.matcher(url);
			if(m.find()){
				return m.group(1);
			}
		}else if(Server.DBTYPE_SQLSERVER.equalsIgnoreCase(type)){
			//sample URL:  jdbc:microsoft:sqlserver://localhost:1433;databaseName=geniuswiki
			Pattern p = Pattern.compile("[^/]+//([^/]+);.*");
			Matcher m = p.matcher(url);
			if(m.find()){
				return m.group(1);
			}
		}
		return prototype.getProperty(type+".default.host");
	}
	
	public static String detectDBName(String type, String url) {
		if("mysql".equalsIgnoreCase(type)){
			Pattern p = Pattern.compile("[^/]+//[^/]+/([^?]*)(?:\\?|$)");
			Matcher m = p.matcher(url);
			if(m.find()){
				return m.group(1);
			}
		}
		return "geniuswiki";
	}
	
	public static void main(String[] args) {
		System.out.println(detectHost(Server.DBTYPE_MYSQL, "jdbc:mysql://localhost:3306/steveneo_edgenius?autoReconnect=true&useUnicode=true&characterEncoding=utf-8"));
		System.out.println(detectHost(Server.DBTYPE_POSTGRESQL, "jdbc:postgresql://localhost:3306/geniuswiki?useUnicode=true&characterEncoding=utf-8"));
		System.out.println(detectHost(Server.DBTYPE_ORACLE9I, "jdbc:oracle:thin:@localhost:1521:geniuswiki"));
		System.out.println(detectHost(Server.DBTYPE_DB2, "jdbc:db2://localhost:50000/geniuswiki"));
		System.out.println(detectHost(Server.DBTYPE_HSQLDB, "jdbc:hsqldb:hsql://localhost:9001/geniuswiki"));
		System.out.println(detectHost(Server.DBTYPE_SQLSERVER, "jdbc:jtds:sqlserver://localhost:1433;databaseName=geniuswiki"));
	}

	public String getURL(String dbType, String driverType, String host, String dbname, boolean admin) {
		log.info("get url by " + dbType + " for driver " + driverType + " on host " + host + " for admin " + admin);
		driverType = StringUtils.isBlank(driverType)?"":("."+StringUtils.trim(driverType));
		String url = prototype.getProperty(dbType+driverType+(admin?".admin.url":".url"));
		url = url.replaceAll(TOKEN_HOST, host);
		url = url.replaceAll(TOKEN_DBNAME, dbname);
		return url;
	}

	public Map<String, String> getAdminURLPatterns() {
		Map<String, String> map = new HashMap<String, String>();
		String type = Server.DBTYPE_MYSQL;
		map.put(type, prototype.getProperty(type+".admin.url"));
		
		type = Server.DBTYPE_POSTGRESQL;
		map.put(type, prototype.getProperty(type+".admin.url"));
		
		type = Server.DBTYPE_ORACLE9I;
		map.put(type, prototype.getProperty(type+".admin.url"));
		
		type = Server.DBTYPE_DB2;
		map.put(type, prototype.getProperty(type+".admin.url"));
		
		type = Server.DBTYPE_HSQLDB;
		map.put(type, prototype.getProperty(type+".admin.url"));
		
		type = Server.DBTYPE_SQLSERVER;
		map.put(type, prototype.getProperty(type+".admin.url"));
		
		type = Server.DBTYPE_SQLSERVER+".ms";
		map.put(type, prototype.getProperty(type+".admin.url"));
		
		return map;
	}


	public Map<String, String> getUserURLPatterns() {
		Map<String, String> map = new HashMap<String, String>();
		String type = Server.DBTYPE_MYSQL;
		map.put(type, prototype.getProperty(type+".url"));
		
		type = Server.DBTYPE_POSTGRESQL;
		map.put(type, prototype.getProperty(type+".url"));
		
		type = Server.DBTYPE_ORACLE9I;
		map.put(type, prototype.getProperty(type+".url"));
		
		type = Server.DBTYPE_DB2;
		map.put(type, prototype.getProperty(type+".url"));
		
		type = Server.DBTYPE_HSQLDB;
		map.put(type, prototype.getProperty(type+".url"));
		
		type = Server.DBTYPE_SQLSERVER;
		map.put(type, prototype.getProperty(type+".url"));
		
		type = Server.DBTYPE_SQLSERVER+".ms";
		map.put(type, prototype.getProperty(type+".url"));
		
		return map;
	}
	
	public String getDriver(String dbType, String driverType){
		driverType = StringUtils.isBlank(driverType)?"":("."+StringUtils.trim(driverType));
		return StringUtils.trimToEmpty(prototype.getProperty(dbType+driverType+".driver"));
	}
	/**
	 * If url is empty, try to use prototype, by host and dbname. Otherwise, compare prototype(combile host/dbname) with URL, if diff
	 */
	public ConnectionProxy getConnection(String dbType, String url, String dbSchema, String username, String password) throws Exception {
		
		String driver = getDriver(dbType,getDriverType(url));
		log.info("Loading database driver {} on type {}.", driver, dbType);
		if(StringUtils.isBlank(driver) || StringUtils.isBlank(url) || StringUtils.isBlank(username)){
			throw new DBLoaderException("driver or url or username is blank:" + dbType +"|"+url+"|"+username +"|");
		}
		try {
			Class.forName(driver);
		} catch (Exception e) {
			throw new DriverNotFoundException(e);
		}
		return new ConnectionProxy(dbType, DriverManager.getConnection(url.trim(), username.trim(), StringUtils.trim(password)), StringUtils.trim(dbSchema));

	}
	
	/**
	 * @param url
	 * @return
	 */
	private String getDriverType(String url) {
		if(url.startsWith("jdbc:microsoft:sqlserver"))
			return "ms";
		
		return null;
	}
	public ConnectionProxy getConnection(String dbType, Server server) throws Exception {
		if (server.getDbConnectType() != null && server.getDbConnectType().equalsIgnoreCase(Server.CONN_TYPE_DS)) {
			// Datasource
			Context ctx = new InitialContext();
			if (ctx == null)
				throw new Exception("Boom - No Context");
			
			//this just confirm java:comp/env exist, then go next step
			Context envCtx = (Context) ctx.lookup(Server.JNDI_PREFIX);
			if (envCtx == null)
				throw new Exception("Boom - No java:comp/env Context");

			//jndi should be full URL
			DataSource ds = (DataSource) ctx.lookup(server.getDbJNDI());
			if (ds != null) {
				//TODO: does JNDI datasource need schmea?
				return new ConnectionProxy(dbType, ds.getConnection(), null);
			}
		} else {
			// JDBC:
			return getConnection(dbType, server.getDbUrl(), server.getDbSchema(), server.getDbUsername(), server.getDbPassword());
		}

		return null;
	}

	/**
	 * Limitation: Special for MySQL database.
	 */
	public boolean isDBExist(String type, String host, String dbname, String schema, String root, String password) throws SQLException {
		ConnectionProxy conn = null;
		try {
			//as this method only for MQSQL database, so driverType set to Null
			String url = getURL(type,null, host, dbname, false);
			conn = getConnection(type,url, schema, root, password);
			return true;
		} catch (Exception e) {
			return false;
		} finally{
			if(conn != null)conn.close();
		}
		
	}

	public void runSQLFile(String type, String filename,  ConnectionProxy con) throws SQLException, IOException{
		Statement stat = null;
		try {
			stat = con.createStatement();
			List<String> lines = loadSQLFile(type, filename);
			for (String sql : lines) {
				stat.addBatch(sql);
			}
			stat.executeBatch();
		} finally{
			if(stat != null)
				stat.close();
		}
	}
	public void resetDB(String type, ConnectionProxy con, String dbname,  String username, String password) throws SQLException, IOException{
		Statement stat = null;
		try {
			log.info("Reset database " + dbname + " starting...");
			dbname = StringUtils.trimToEmpty(dbname);
			username = StringUtils.trimToEmpty(username);
			password = StringUtils.trimToEmpty(password);
			stat = con.createStatement();
			
			List<String> lines = loadSQLFile(type, type+"-create-db.sql");
			for (String sql : lines) {
				sql = sql.replaceAll("@TOKEN.DATABASE.NAME@", dbname);
				sql = sql.replaceAll("@TOKEN.DATABASE.USERNAME@", username);
				sql = sql.replaceAll("@TOKEN.DATABASE.PASSWORD@", password);
				stat.addBatch(sql);
			}
			stat.executeBatch();
			
			log.info("Database " + dbname + " reset success");
		} finally{
			if(stat != null)
				stat.close();
		}
		
	}
	public boolean isTableExist(ConnectionProxy con){
		ResultSet tables = null;
		Statement stat = null;
		try {
			stat = con.createStatement();
			//role must have some initial values
			tables = stat.executeQuery("select * from " + Constants.TABLE_PREFIX+"ROLES");
			return tables.next();
//			DatabaseMetaData meta = con.getMetaData();
//			tables = meta.getTables(catalog, schema, , new String[]{"TABLE"});
//			return tables.next();
		} catch (SQLException e) {
			log.warn("Detect table get exception, means table not exist...");
		} finally{
			if(tables != null){
				try {
					tables.close();
				} catch (SQLException e) {
				}
			}
		}
		return false;
	}
	
	public void resetTable(String dbType, ConnectionProxy con) throws SQLException, IOException{
		Statement dropStat = null;
		Statement stat = null;
		try {
			log.info("Creating tables...");
			dropStat = con.createStatement();
			stat = con.createStatement();
			List<String> lines = loadSQLFile(dbType, dbType+".ddl");
			for (String sql : lines) {
				//need know if table already exist, if exist need run alter table ... drop index..., otherwise, skip that
				sql = sql.replaceAll("\n", " ").trim();
				// Here is really a special patch for MYSQL 4 as I don't want to waste much time on this special issue
				// key size is over 1024 issue on MySQL 4 http://bugs.mysql.com/bug.php?id=4541
				if(sql.toLowerCase().startsWith("create index page_link_index on "+Constants.TABLE_PREFIX.toLowerCase()+"page_links")){
					try {
						//only mysql4 may throw exception, ignore it.
						dropStat.execute(sql);
					} catch (Exception e) {
						log.error("Create page link index  operation failed....");
					}
					continue;

				}
				//1. don't detect table exist because it only check if role table has data, if partial create, isTableExist() won't work
				//2. put drop independent and try..catch b/c some DBs will report error if table not exist while drop
				if(sql.toLowerCase().startsWith("alter table ") && sql.toLowerCase().indexOf(" drop ") != -1
					|| sql.toLowerCase().startsWith("drop ") ){
					try {
						dropStat.execute(sql);
					} catch (Exception e) {
						log.error("Drop operation failed. It is OK for initial installation.");
					}
					continue;
				}
				
				stat.addBatch(sql);
			}
			stat.executeBatch();

			log.info("Initialize data for system...");
			lines = loadSQLFile(dbType, dbType+"-init-tables.sql");
			for (String sql : lines) {
				sql = sql.replaceAll("\n", " ").trim();
				
				stat.addBatch(sql);
			}
			stat.executeBatch();
			
			log.info("Initialize quartz tables for system...");
			lines = loadSQLFile(dbType,  dbType+"-quartz.sql");
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
			log.info("System all tables and initial data are ready");
		} finally{
			if(stat != null)
				stat.close();
			if(dropStat != null)
				dropStat.close();

		}
		
	}
	/**
	 * @throws SQLException 
	 */
	public void createUser(ConnectionProxy con, String type,String fullname, String username, String password,String email) throws SQLException {
		
		PreparedStatement stat = null;
		PreparedStatement stat2 = null;
		PreparedStatement stat3 = null;
		Statement stat1 = null;
		ResultSet rs = null;
		try {
			if(Global.EncryptPassword){
	            String algorithm = Global.PasswordEncodingAlgorithm;
	    
	            if (algorithm == null) { 
	                algorithm = "MD5";
	            }
	            password = CodecUtil.encodePassword(password, algorithm);
			}
			
			String sql = prototype.getProperty(type+".user.create");
			sql = sql.replaceAll(TOKEN_TABLE_PREFIX, Constants.TABLE_PREFIX);
			
			stat = con.prepareStatement(sql);
			int idx = 1;
			stat.setString(idx++, fullname);
			stat.setString(idx++, username);
			stat.setString(idx++, password);
			stat.setString(idx++, email);
//			if( Server.DBTYPE_POSTGRESQL.equalsIgnoreCase(type)){
				//TODO: oracle need get from key table and update key table
				//stat.setInt(idx++,userKey);
//			}
			
			if(Server.DBTYPE_MYSQL.equalsIgnoreCase(type)){
				try {
					//mysql 5 and 4 has different value for bit/tinyint, so try 5 first, if fail try mysql 4 again
					stat.executeUpdate();
				} catch (Exception e) {
					String sql4 = prototype.getProperty(type+"4.user.create");
					sql4 = sql4.replaceAll(TOKEN_TABLE_PREFIX, Constants.TABLE_PREFIX);
					
					stat2= con.prepareStatement(sql4);
					idx = 1;
					stat2.setString(idx++, fullname);
					stat2.setString(idx++, username);
					stat2.setString(idx++, password);
					stat2.setString(idx++, email);
					stat2.executeUpdate();
				}
			}else{
				stat.executeUpdate();
			}
			
			int id =-1;
			stat3 = con.prepareStatement("select puid from " + Constants.TABLE_PREFIX + "USERS where user_name=?" );
			stat3.setString(1, username);
			rs = stat3.executeQuery();
			if(rs == null || rs.next()){
					id = rs.getInt(1);
			}
			if(id >=0){
				//give user all roles
				stat1 = con.createStatement();
				String role1 = "INSERT INTO "+Constants.TABLE_PREFIX+"USER_ROLE (user_puid,role_puid) values("+id+",1)";
				String role2 = "INSERT INTO "+Constants.TABLE_PREFIX+"USER_ROLE (user_puid,role_puid) values("+id+",2)";
				String role3 = "INSERT INTO "+Constants.TABLE_PREFIX+"USER_ROLE (user_puid,role_puid) values("+id+",3)";
			
				stat1.addBatch(role1);
				stat1.addBatch(role2);
				stat1.addBatch(role3);
				
				stat1.executeBatch();
			}else{
				log.error("unable initialize role for user " + username + ". roleback");
				stat.execute("delete from "+Constants.TABLE_PREFIX+"USER where user_name="+username);
				throw new SQLException("unable initialize role for user " + username + ". roleback");
			}
		} finally{
			try {
				if(rs != null) rs.close();
				if(stat != null) stat.close();
				if(stat1 != null) stat1.close();
				if(stat2 != null) stat2.close();
				if(stat3 != null) stat3.close();
			} catch (Exception e) {
			}
		}

	}

	//********************************************************************
	//               private methods
	//********************************************************************
	
	@SuppressWarnings("unchecked")
	private List<String> loadSQLFile(String type, String filename) throws IOException{
		InputStream is = FileUtil.getFileInputStream("classpath:META-INF/meta/"+type+"/"+filename);
		List<String> plines = IOUtils.readLines(is);
		IOUtils.closeQuietly(is);
		
		List<String> sqlLines = new ArrayList<String>();
		StringBuffer line = new StringBuffer();
		for (String pline : plines) {
			pline  = StringUtils.trimToEmpty(pline);
			if(pline.startsWith("--") || pline.startsWith("#"))
				continue;
			
			if(pline.endsWith(";")){
				//trim last ";" as it is invalid in Oracle
				line.append(pline,0,pline.length()-1);
				sqlLines.add(line.toString());
				line = new StringBuffer();
			}else{
				line.append(pline).append("\n");
			}
		}
		
		return sqlLines;
	}

}
