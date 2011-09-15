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
package com.edgenius.core;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.edgenius.core.util.FileUtil;

/**
 * Server configuration, these setting could be change on Instance Admin page. 
 * The different with Global setting is their modification only can be implements after restart server
 * So far these properties read from classpath:server.properties file.  
 * @author Dapeng.Ni
 */
public class Server implements Serializable{
	private static final long serialVersionUID = 3005815916852302092L;
	
	public static final String FILE = "server.properties";

	public static final String FILE_DEFAULT = "classpath:geniuswiki/server.default.properties";
	public static final String CONN_TYPE_JDBC = "jdbc";
	public static final String CONN_TYPE_DS = "datasource";

	public static final String DBTYPE_DB2= "db2";
	public static final String DBTYPE_ORACLE9I= "oracle9i";
	public static final String DBTYPE_POSTGRESQL= "postgresql";
	public static final String DBTYPE_MYSQL= "mysql";
	public static final String DBTYPE_HSQLDB = "hsqldb";
	public static final String DBTYPE_SQLSERVER = "sqlserver";
	
	public static final String JNDI_PREFIX = "java:comp/env";

	
	//db2, mysql, postgresql or oracle
	private String dbType;
	//JDBC or datasource
	private String dbConnectType;
	private String dbDriverClass;
	private String dbUrl;
	private String dbSchema;
	private String dbUsername;
	private String dbPassword;

	private String dbJNDI;

	private String mailHost;
	private String mailHostPort;
	private String mailUsername;
	private String mailPassword;
	private String mailJndi;
	private String mailProperties;
	
	private String mqServerEmbedded;
	private String mqServerUrl;
	
	private String quartzJobstoreDriver;

	public static Properties load() throws IOException{
		String root = DataRoot.getDataRoot();
		if(!FileUtil.exist(root+Server.FILE)){
			//copy from defaultExternalResource
			FileOutputStream dest = FileUtil.getFileOutputStream(root+Server.FILE);
			IOUtils.copy(FileUtil.getFileInputStream(FILE_DEFAULT), dest);
			IOUtils.closeQuietly(dest);
		}
		
		return FileUtil.loadProperties(root+Server.FILE);
	}

	/**
	 * Save to data root server.xml
	 * @throws IOException 
	 */
	public void saveTo() throws IOException {
		OutputStream os = null;
		try {
			String root = DataRoot.getDataRoot();
			//save server.properties into file
			os = FileUtil.getFileOutputStream(root+Server.FILE);
			
			Properties prop = new Properties();
			syncTo(prop);
			prop.store(os, "Save from system program");
		}finally{
			try {
				if(os != null)
					os.close();
			} catch (Exception e) {
				// nothing;
			}
		}
		
	}
	
	public void syncTo(Properties prop) {
		prop.setProperty("server.db.type", StringUtils.trimToEmpty(dbType));
		prop.setProperty("server.db.connect.type", StringUtils.trimToEmpty(dbConnectType));
		prop.setProperty("server.db.driver.class.name", StringUtils.trimToEmpty(dbDriverClass));
		prop.setProperty("server.db.url", StringUtils.trimToEmpty(dbUrl));
		prop.setProperty("server.db.schema", StringUtils.trimToEmpty(dbSchema));
		prop.setProperty("server.db.username", StringUtils.trimToEmpty(dbUsername));
		prop.setProperty("server.db.password", StringUtils.trimToEmpty(dbPassword));
		prop.setProperty("server.db.jndi", StringUtils.trimToEmpty(dbJNDI));
		
		prop.setProperty("mail.smtp.host", StringUtils.trimToEmpty(mailHost));
		prop.setProperty("mail.smtp.port", StringUtils.trimToEmpty(mailHostPort));
		prop.setProperty("mail.smtp.username", StringUtils.trimToEmpty(mailUsername));
		prop.setProperty("mail.smtp.password", StringUtils.trimToEmpty(mailPassword));
		prop.setProperty("mail.smtp.properties", StringUtils.trimToEmpty(mailProperties));
		prop.setProperty("mail.smtp.jndi", StringUtils.trimToEmpty(mailJndi));
		prop.setProperty("mq.server.embedded", StringUtils.trimToEmpty(mqServerEmbedded));
		prop.setProperty("mq.server.url", StringUtils.trimToEmpty(mqServerUrl));
		prop.setProperty("quartz.jobstore.driver", StringUtils.trimToEmpty(quartzJobstoreDriver));
		
		
	}
	/**
	 * @param prop
	 */
	public void syncFrom(Properties prop) {
		dbType  = StringUtils.trimToEmpty(prop.getProperty("server.db.type"));
		dbConnectType  = StringUtils.trimToEmpty(prop.getProperty("server.db.connect.type"));
		dbDriverClass = StringUtils.trimToEmpty(prop.getProperty("server.db.driver.class.name"));
		dbUrl = StringUtils.trimToEmpty(prop.getProperty("server.db.url"));
		dbSchema = StringUtils.trimToEmpty(prop.getProperty("server.db.schema"));
		dbUsername = StringUtils.trimToEmpty(prop.getProperty("server.db.username"));
		dbPassword = StringUtils.trimToEmpty(prop.getProperty("server.db.password"));
		dbJNDI = StringUtils.trimToEmpty(prop.getProperty("server.db.jndi"));
		
		mailHost = StringUtils.trimToEmpty(prop.getProperty("mail.smtp.host"));
		mailHostPort = StringUtils.trimToEmpty(prop.getProperty("mail.smtp.port"));
		mailUsername = StringUtils.trimToEmpty(prop.getProperty("mail.smtp.username"));
		mailPassword = StringUtils.trimToEmpty(prop.getProperty("mail.smtp.password"));
		mailProperties = StringUtils.trimToEmpty(prop.getProperty("mail.smtp.properties"));
		mailJndi = StringUtils.trimToEmpty(prop.getProperty("mail.smtp.jndi"));
		
		mqServerEmbedded = StringUtils.trimToEmpty(prop.getProperty("mq.server.embedded"));
		mqServerUrl = StringUtils.trimToEmpty(prop.getProperty("mq.server.url"));
		quartzJobstoreDriver = StringUtils.trimToEmpty(prop.getProperty("quartz.jobstore.driver"));
		
		//set some default values
		if("".equals(dbConnectType)){
			dbConnectType = "jdbc";
		}
		if("".equals(mqServerEmbedded)){
			mqServerEmbedded = "true";
		}
		if("".equals(quartzJobstoreDriver)){
			quartzJobstoreDriver = "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
		}
	}

	//********************************************************************
	//               Set /get methods
	//********************************************************************
	public String getDbDriverClass() {
		return dbDriverClass;
	}
	public void setDbDriverClass(String dbDriverClass) {
		this.dbDriverClass = dbDriverClass;
	}
	public String getDbUrl() {
		return dbUrl;
	}
	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}
	public String getDbUsername() {
		return dbUsername;
	}
	public void setDbUsername(String dbUsername) {
		this.dbUsername = dbUsername;
	}
	public String getDbPassword() {
		return dbPassword;
	}
	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}
	public String getMailHost() {
		return mailHost;
	}
	public void setMailHost(String mailHost) {
		this.mailHost = mailHost;
	}
	public String getMailUsername() {
		return mailUsername;
	}
	public void setMailUsername(String mailUsername) {
		this.mailUsername = mailUsername;
	}
	public String getMailPassword() {
		return mailPassword;
	}
	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}
	public String getMailHostPort() {
		return mailHostPort;
	}
	public void setMailHostPort(String mailHostPort) {
		this.mailHostPort = mailHostPort;
	}
	public String getMqServerUrl() {
		return mqServerUrl;
	}
	public void setMqServerUrl(String mqServerUrl) {
		this.mqServerUrl = mqServerUrl;
	}
	public String getMqServerEmbedded() {
		return mqServerEmbedded;
	}
	public String getMailJndi() {
		return mailJndi;
	}

	public void setMailJndi(String mailJndi) {
		this.mailJndi = mailJndi;
	}

	public String getMailProperties() {
		return mailProperties;
	}

	public void setMailProperties(String mailProperties) {
		this.mailProperties = mailProperties;
	}

	public void setMqServerEmbedded(String mqServerEmbedded) {
		this.mqServerEmbedded = mqServerEmbedded;
	}
	
	public String getDbJNDI() {
		return dbJNDI;
	}
	public void setDbJNDI(String dbJNDI) {
		this.dbJNDI = dbJNDI;
	}
	public String getDbConnectType() {
		return dbConnectType;
	}
	public void setDbConnectType(String connectType) {
		this.dbConnectType = connectType;
	}
	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	public String getQuartzJobstoreDriver() {
		return quartzJobstoreDriver;
	}
	public void setQuartzJobstoreDriver(String quartzJobstoreDriver) {
		this.quartzJobstoreDriver = quartzJobstoreDriver;
	}
	public String getDbSchema() {
		return dbSchema;
	}
	public void setDbSchema(String dbSchema) {
		this.dbSchema = dbSchema;
	}

}
