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
package com.edgenius.wiki.webapp.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.edgenius.core.Constants;
import com.edgenius.core.DataRoot;
import com.edgenius.core.Global;
import com.edgenius.core.GlobalSetting;
import com.edgenius.core.Installation;
import com.edgenius.core.Server;
import com.edgenius.core.Version;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.FileUtil;
import com.edgenius.core.util.WebUtil;
import com.edgenius.core.webapp.BaseServlet;
import com.edgenius.license.LicenseManager;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.installation.ConnectionProxy;
import com.edgenius.wiki.installation.DBLoader;
import com.edgenius.wiki.installation.DriverNotFoundException;
import com.edgenius.wiki.installation.UpgradeService;
import com.edgenius.wiki.webapp.context.ApplicationContextUtil;

/**
 * @author Dapeng.Ni
 */
public class InstallServlet extends BaseServlet {
	private static final long serialVersionUID = -2291585339038974441L;
	protected transient final Logger log = LoggerFactory.getLogger(InstallServlet.class);

//	StringBuffer buf = new StringBuffer("{visible:on=login}Welcome to GeniusWiki! Enjoy all new web2.0 Wiki and Blog.")
//	.append("\n* Customized dashboard")
//	.append("\n* Unlimited spaces for wiki or blog")
//	.append("\n* Markup or WYSIWYG editor")
//	.append("\n* Not only sharing but also control access")
//	.append("\n* No Internet no worries,offline support!")
//	.append("\n Sign up for free!")
//	.append("\n{visible}")
//	.append("\n{visible:on=logout}Thanks your registration. You can use \"create space\" function to create your owned space.")
//	.append("\n Some tips:")
//	.append("\n* Press F1 to invoke help dialog.")
//	.append("\n* Use \"space friends\" to control access on the space.")
//	.append("\n* Upload your portrait ~click your name on right upper corner~.")
//	.append("\n* Don't forget to invite your friend!")
//	.append("\n* Send us your feedback. We do appreciate it!")
//	.append("\n Enjoy!\n")
//	.append("{visible}");

	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
    	if(checkDowngrade(request, response)){
    		return;
    	}
    	
		String step = request.getParameter("step");
		if(Version.LICENSE_STATUS > 0){
			if("ulicense".equalsIgnoreCase(step)){
				//this is from license-expired.jsp - user try to input another license
				updateLicense(request,response);
			}else{
				request.getRequestDispatcher("/WEB-INF/pages/install/license-expired.jsp").forward(request, response);
			}
			return;
		}

		if("chglang".equalsIgnoreCase(step)){
			String langs[] = request.getParameter("lang").split("_");
			Global.DefaultLanguage = langs[0];
			Global.DefaultCountry = langs[1];
			response.sendRedirect(WebUtil.getHostAppURL());
			return;
		}
		if(Installation.DONE){
			//system already installed, just skip all process
			request.getRequestDispatcher("/").forward(request, response);
			return;
		}

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//download DB script request 
		String script = request.getParameter("script");
		if(!StringUtils.isBlank(script)){
			String type = request.getParameter("type");
			if("db".equalsIgnoreCase(script)){
				//download DB create script
				InputStream is = FileUtil.getFileInputStream("classpath:META-INF/meta/"+type+"/"+type+"-create-db.sql");
				String buf = IOUtils.toString(is);
				IOUtils.closeQuietly(is);
				response.getOutputStream().write(buf.getBytes(Constants.UTF8));
			}else if("tables".equalsIgnoreCase(script)){
				//download table create script
				StringBuffer buf = new StringBuffer(); 
				buf.append("\n\n-- ============= create tables =================\n");
				
				InputStream is = FileUtil.getFileInputStream("classpath:META-INF/meta/"+type+"/"+type+".ddl");
				buf.append(IOUtils.toString(is));
				IOUtils.closeQuietly(is);
				
				buf.append("\n\n-- ============= initial data =================\n");
				is = FileUtil.getFileInputStream("classpath:META-INF/meta/"+type+"/"+type+"-init-tables.sql");
				buf.append(IOUtils.toString(is));
				IOUtils.closeQuietly(is);
				
				buf.append("\n\n-- ============= quartz tables =================\n");
				is = FileUtil.getFileInputStream("classpath:META-INF/meta/"+type+"/"+type+"-quartz.sql");
				buf.append(IOUtils.toString(is));
				IOUtils.closeQuietly(is);
				response.getOutputStream().write(buf.toString().getBytes(Constants.UTF8));
			}
			return;
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Normal installation process
		Installation install = Installation.refreshInstallation();
		loadInitData(request);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Data root
		if("dataroot".equalsIgnoreCase(step)){
			//show create data root
			resetContextPath(request);
			String sysRoot =  System.getProperty(DataRoot.rootKey);
			if(sysRoot != null && sysRoot.trim().length() > 0){
				request.setAttribute("sysPropRoot", sysRoot);
				request.setAttribute("message","GeniusWiki found "  + sysRoot + 
						" is set as your 'geniuswiki.data.root' in the system property. " +
						"Your data root is able to use this value. Please reset it and restart web server if it is incorrect.");
			}
			request.getRequestDispatcher("/WEB-INF/pages/install/createroot.jsp").forward(request, response);
		}else if("cdataroot".equalsIgnoreCase(step)){
			//create data root
			String root =  System.getProperty(DataRoot.rootKey);
			if(root == null || root.trim().length() == 0){
				root = request.getParameter("root");
			}
			createDataRoot(root, request,response);
			
			//this method must after createDataRoot() as it need DataRoot.getRoot() which save in above method
			//save contextPath, host, port, and server timezone into global.xml
			updateGlobal(request.getRequestURL().toString(),request.getServletPath());
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Data root			
		}else if("license".equalsIgnoreCase(step)){
			//show create license
			updateInstallationStatus(install, Installation.STATUS_DATAROOT);
			request.getRequestDispatcher("/WEB-INF/pages/install/createlicense.jsp").forward(request, response);
		}else if("clicense".equalsIgnoreCase(step)){
			//create data root
			createLicense(request,response);
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// MQ
		}else if("mq".equalsIgnoreCase(step)){
			updateInstallationStatus(install, Installation.STATUS_LICENSE);
			request.getRequestDispatcher("/WEB-INF/pages/install/createmq.jsp").forward(request, response);
		}else if("cmq".equalsIgnoreCase(step)){
			//create data root
			createMQServer(request,response);
			
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// DB
		}else if("db".equalsIgnoreCase(step)){
			updateInstallationStatus(install, Installation.STATUS_MQ);
			
			viewDBCreate(request, response);
			
		}else if("cdb".equalsIgnoreCase(step)){
			//create data root
			createDatabase(request,response);
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Table
		}else if("tables".equalsIgnoreCase(step)){
			updateInstallationStatus(install, Installation.STATUS_DB);
			
			viewTableCreate(request, response);
			
		}else if("ctables".equalsIgnoreCase(step)){
			//create data root
			createTables(request,response);
			
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// create admin user
		}else if("admin".equalsIgnoreCase(step)){
			updateInstallationStatus(install, Installation.STATUS_TABLES);
			
			request.getRequestDispatcher("/WEB-INF/pages/install/createadmin.jsp").forward(request, response);
		}else if("cadmin".equalsIgnoreCase(step)){
			//create data root
			createAdmin(request,response);
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Done
		}else if("done".equalsIgnoreCase(step)){
			installDone(request,response);
			response.sendRedirect(WebUtil.getHostAppURL());
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Done
		}else if("reset".equalsIgnoreCase(step)){
			Installation.DONE = true;
			// possible 3 cases: 
			// * system install not complete 
			// * context is already loaded, 
			// * context reload successed
			// all possibility just go to home url
			request.getRequestDispatcher("/").forward(request, response);

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Upgrade
		}else if("upgrade".equalsIgnoreCase(step)){
    		//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    		//!!! There is similar piece code in StartupListener.java, if any bugs found here, please update correspondingly.
			if(upgrade(request, install)){
				installDone(request,response);
				response.sendRedirect(WebUtil.getHostAppURL());
			}else{
				//there are some error in upgrade checking
				viewUpgrade(request,response);
			}			
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Welcome
		}else{
			if(Installation.STATUS_DATAROOT.equalsIgnoreCase(install.getStatus())){
				request.getRequestDispatcher("/WEB-INF/pages/install/createmq.jsp").forward(request, response);
			}else if(Installation.STATUS_LICENSE.equalsIgnoreCase(install.getStatus())){
				request.getRequestDispatcher("/WEB-INF/pages/install/createlicense.jsp").forward(request, response);
			}else if(Installation.STATUS_MQ.equalsIgnoreCase(install.getStatus())){
				viewDBCreate(request, response);
			}else if(Installation.STATUS_DB.equalsIgnoreCase(install.getStatus())){
				viewTableCreate(request, response);
			}else if(Installation.STATUS_TABLES.equalsIgnoreCase(install.getStatus())){
				request.getRequestDispatcher("/WEB-INF/pages/install/createadmin.jsp").forward(request, response);
			}else if(Installation.STATUS_UPGRADE.equalsIgnoreCase(install.getStatus())){
				viewUpgrade(request,response);
			}else{
				resetContextPath(request);
				request.getRequestDispatcher("/WEB-INF/pages/install/welcome.jsp").forward(request, response);
			}
		}

	}

	private boolean checkDowngrade(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		Installation install = Installation.refreshInstallation();
    	int installVer = (int) (NumberUtils.toFloat(install.getVersion(), 1f) * 1000);
    	int appVer = (int) (NumberUtils.toFloat(Version.VERSION, 1f) * 1000);
    	if(installVer > appVer){
    		Installation.DONE = false;
    		//application version is lower than existed data version, stop further test
    		request.setAttribute("appVer", Version.VERSION);
    		request.setAttribute("installVer", install.getVersion());
    		request.getRequestDispatcher("/WEB-INF/pages/install/lower-appversion.jsp").forward(request, response);
    		return true;
    	}
    	
    	return false;
	}



	/**
	 * @param request 
	 * @param install 
	 * @return
	 */
	private boolean upgrade(HttpServletRequest request, Installation install) {
		Server server = (Server) request.getSession().getAttribute("server");
		String dbType = server.getDbType();
		
		DBLoader loader = new DBLoader();
		ConnectionProxy con = null;
		try {
			con = loader.getConnection(dbType, server);
		} catch (Exception e) {
			log.error("Detect upgrade: table does not exist",e);
		}
		if(con != null){
			boolean tableExist = false;
			try{
				tableExist = loader.isTableExist(con);
			} catch (Exception e) {
				log.error("Detect upgrade: table does not exist",e);
			}finally{
				if(con !=null)
					con.close();
			}
			if(!tableExist){
				request.setAttribute("error", "Tables do not exist." );
				return false;
			}
		}else{
			request.setAttribute("error", "Failed connect to database." );
			return false;
		}
		
		try{
			getUpgradeService().doUpgarde(install.getVersion(), Version.VERSION);
			return true;
		}catch(Exception e){
			log.error("Version upgrade is failed from " + install.getVersion() + " to " + Version.VERSION,e);
			request.setAttribute("error", "Version upgrade is failed from " + install.getVersion() + " to " + Version.VERSION);
			return false;
		}
	}

	/**
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws ServletException 
	 */
	private void createDataRoot(String root, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean error = false;
		
		//put original input as echo back while error
		request.setAttribute("root", root);
		
		root = DataRoot.formatRoot(root);

		try {
			File file = FileUtil.getFile(root);
			if(!FileUtil.exist(root)){
				if(!file.mkdirs()){
					error = true;
					request.setAttribute("error", "Unable to create directory, please create manually.");
				}
			}else if(!file.isDirectory()){
				error = true;
				request.setAttribute("error", "The value is not directory.");
			}
		} catch (Exception e) {
			log.error("Unable to locate or create data root directory:"+root ,e);
			error = true;
			request.setAttribute("error", "Unexpected exception " +e.toString()+". Please manually create root directory.");
		}
		if(error){
			resetContextPath(request);
			request.getRequestDispatcher("/WEB-INF/pages/install/createroot.jsp").forward(request, response);
		}else{
			DataRoot.saveDataRoot(root);
			//detect if the global.xml, installation.properties and server.properties exist? if so, the goto upgrade page
			Installation install = Installation.refreshInstallation();
			if(FileUtil.exist(root+Global.FILE) && FileUtil.exist(root+Server.FILE) && FileUtil.exist(root+Installation.FILE)){
				if(install.getVersion() == null){ //this means file not success load
					 install = Installation.refreshInstallation();
					 if(install.getInstanceID() == null){
						install.setInstanceID(RandomStringUtils.randomAlphanumeric(WikiConstants.UUID_KEY_SIZE).toLowerCase());
						install.setInstanceType(Installation.INSTANCE_STANDARD);
						Installation.saveInstallation(install);
					}
				}
				if(Installation.STATUS_COMPLETED.equalsIgnoreCase(install.getStatus())){
					if(checkDowngrade(request, response)){
						return;
					}else{
						install.setStatus(Installation.STATUS_UPGRADE);
						Installation.saveInstallation(install);
						Installation.DONE = false;
						
						//echo back some info
						viewUpgrade(request, response);
						return;
					}
				}
			}else{
				//fresh new installation - try to give an instance ID to installation.xml
				if(install.getInstanceID() == null){
					install.setInstanceID(UUID.randomUUID().toString());
					install.setInstanceType(Installation.INSTANCE_STANDARD);
					Installation.saveInstallation(install);
				}
			}
			//new installation
			request.getRequestDispatcher("/WEB-INF/pages/install/createlicense.jsp").forward(request, response);
		}

	}
	/**
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws ServletException 
	 */
	private void createLicense(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String type = request.getParameter("licenseType");
		if(!"free".equals(type)){
			String license = StringUtils.trimToEmpty(request.getParameter("license"));
			if(license != ""){
				//validate license
				if(LicenseManager.verfiyLicense(license, 0) != 0){
					request.setAttribute("message","Invalid license, please verify your input.");
					//echo back message
					request.setAttribute("license", license);
					request.getRequestDispatcher("/WEB-INF/pages/install/createlicense.jsp").forward(request, response);
					return;
				}
				
				//save valid license
				Installation install = Installation.refreshInstallation();
				install.setLicense(license);
				Installation.saveInstallation(install);
			}
		}
		//next step
		request.getRequestDispatcher("/WEB-INF/pages/install/createmq.jsp").forward(request, response);
		
	}
	private void updateLicense(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String license = StringUtils.trimToEmpty(request.getParameter("license"));
		if(license != ""){
			//validate license
			if(LicenseManager.verfiyLicense(license, Version.exist_user_amout) != 0){
				request.setAttribute("error","Invalid license, please verify your input.");
				//echo back message
				request.setAttribute("license", license);
				request.getRequestDispatcher("/WEB-INF/pages/install/license-expired.jsp").forward(request, response);
				return;
			}
			
			//save valid license
			Installation install = Installation.refreshInstallation();
			install.setLicense(license);
			Installation.saveInstallation(install);
			
			//here will reload context and refresh Version class static varaibles
			installDone(request,response);
			response.sendRedirect(WebUtil.getHostAppURL());
		}else{
			request.getRequestDispatcher("/WEB-INF/pages/install/license-expired.jsp").forward(request, response);
		}
		
	}
	private void createMQServer(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = request.getParameter("url");
		String embed = request.getParameter("embed");
		if(embed == null){
			embed = "false";
		}
		
		Server server = (Server) request.getSession().getAttribute("server");
		if(!StringUtils.isBlank(url)){
			server.setMqServerUrl(url);
			server.setMqServerEmbedded(embed);
			server.saveTo();
		}
	
		
		response.sendRedirect("install?step=db");
	}
	
	private void createDatabase(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean confirmed = BooleanUtils.toBoolean(request.getParameter("confirmed"));
		String dbType = request.getParameter("dbType");
		String rootUser = request.getParameter("rootUser");
		String rootpass = request.getParameter("rootPassword");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String dbname = request.getParameter("dbname");
		String dbSchema = request.getParameter("dbschema");
		String host = request.getParameter("host");
		String adminDBUrl = request.getParameter("adminDBUrl");
		boolean urlEdited =  BooleanUtils.toBoolean(request.getParameter("urlEdited"));
		
		//echo back value
		request.setAttribute("dbType", dbType);
		request.setAttribute("rootUser", rootUser);
		request.setAttribute("rootPassword", rootpass);
		request.setAttribute("host", host);
		request.setAttribute("dbname", dbname);
		request.setAttribute("dbschema", dbSchema);
		request.setAttribute("adminDBUrl", adminDBUrl);
		request.setAttribute("username", username);
		request.setAttribute("password", password);
		request.setAttribute("urlEdited", urlEdited);

		if(Server.DBTYPE_MYSQL.equalsIgnoreCase(dbType)){
			DBLoader loader = new DBLoader();
			ConnectionProxy con = null;
			try {
				log.info("Creating DB {} for user {}", dbname, username);
				if(confirmed || !loader.isDBExist(dbType, host, dbname, dbSchema, rootUser, rootpass)){
					if(!urlEdited){
						adminDBUrl = loader.getURL(dbType,null, host,dbname, true); 
					}
					con = loader.getConnection(dbType,adminDBUrl, dbSchema, rootUser, rootpass);
					log.info("DB {} is going to create or reset", dbname);
					//at moment, only MySQL allows to create DB, and its schema is null.
					loader.resetDB(dbType, con, dbname,username,password);
					
					//into session and file if current server is not jndi...
					Server server = (Server) request.getSession().getAttribute("server");
					if(StringUtils.isBlank(server.getDbJNDI())){
						updateServerPropertiesJDBC(request, dbType, null, loader.getDriver(dbType,null), username, password, 
								loader.getURL(dbType, null, host, dbname, false));
					} else{
						updateServerPropertiesJNDI(request, dbType, server.getDbJNDI());
					}
					//success init DB
					response.sendRedirect("install?step=tables");
				}else{
					log.info("DB {} is existed", dbname);
					//return message, need user confirm
					request.setAttribute("existed", true);
					request.setAttribute("message","Database already exist, do you want to reset it?");
					request.getRequestDispatcher("/WEB-INF/pages/install/createdb.jsp").forward(request, response);
				}
			} catch (DriverNotFoundException e) {
				log.error("DB driver not found", e);
				request.setAttribute("error", "No suitable database driver found. Please copy corresponding driver to your web server library directory.");
				request.getRequestDispatcher("/WEB-INF/pages/install/createdb.jsp").forward(request, response);
			} catch (SQLException e) {
				log.error("SQL error", e.getNextException());
				request.setAttribute("error", "Exception:"+ e.toString());
				request.getRequestDispatcher("/WEB-INF/pages/install/createdb.jsp").forward(request, response);
			} catch (Exception e) {
				log.error("Unable complete database initialize task",e);
				request.setAttribute("error", "Unable create database, please create manually.");
				request.getRequestDispatcher("/WEB-INF/pages/install/createdb.jsp").forward(request, response);
			}finally{
				if(con != null)
					con.close();
			}
		}else{
			//into session and file if current server is not jndi...
			Server server = (Server) request.getSession().getAttribute("server");
			if(!dbType.equalsIgnoreCase(server.getDbType())){
				//this case is, server.xml is there,  but user change database type, keep DB type, connect Type to JDBC, and clear all other DB attributes
				updateServerPropertiesJDBC(request, dbType, null,null,null,null,null);	
			}
			//for Oracle and DB2, only DBA create DB and table...
			response.sendRedirect("install?step=tables");
		}

	}
	
	private void createTables(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean confirmed = BooleanUtils.toBoolean(request.getParameter("confirmed"));
		String dbType = request.getParameter("dbType");
		String connectType = request.getParameter("connectType");
		String jndi = request.getParameter("jndi");
		String dbname = request.getParameter("dbname");
		String dbSchema = request.getParameter("dbschema");
		String host = request.getParameter("host");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String userDBUrl = request.getParameter("userDBUrl");
		String driverType = request.getParameter("driverType");
		boolean connonly =  "true".equals(request.getParameter("connonly"))?true:false;
		boolean urlEdited =  BooleanUtils.toBoolean(request.getParameter("urlEdited"));
		
		//echo back value
		request.setAttribute("dbType", dbType);
		request.setAttribute("connectType", connectType);
		request.setAttribute("jndi", jndi);
		request.setAttribute("host", host);
		request.setAttribute("dbname", dbname);
		request.setAttribute("dbschema", dbSchema);
		request.setAttribute("userDBUrl", userDBUrl);
		request.setAttribute("username", username);
		request.setAttribute("password", password);
		request.setAttribute("urlEdited", urlEdited);
		request.setAttribute("driverType", driverType);

		DBLoader loader = new DBLoader();
		ConnectionProxy con = null;
		try {
			//put all connection information into a Server object, and let loader to decide what kind Connect
			Server connServer = new Server();
			connServer.setDbType(dbType);
			connServer.setDbConnectType(connectType);
			
			if(connectType.equalsIgnoreCase(Server.CONN_TYPE_JDBC)){
				if(!urlEdited){
					userDBUrl = loader.getURL(dbType,driverType, host,dbname, false); 
				}
				connServer.setDbUrl(userDBUrl);
				connServer.setDbSchema(dbSchema);
			}else{
				//JDNI won't be blank so far
				if(!jndi.startsWith(Server.JNDI_PREFIX)){
					if(jndi.startsWith("/"))
						jndi = Server.JNDI_PREFIX+jndi;
					else
						jndi = Server.JNDI_PREFIX+"/"+jndi;
				}
				connServer.setDbJNDI(jndi);
			}
			connServer.setDbUsername(username);
			connServer.setDbPassword(password);
			con = loader.getConnection(dbType, connServer);
			if(con == null){
				request.setAttribute("error", "Unable to connect database.");
				request.getRequestDispatcher("/WEB-INF/pages/install/createtables.jsp").forward(request, response);
				return;
			}
			if(connonly){
				//success init DB
				if(StringUtils.equalsIgnoreCase(Server.CONN_TYPE_DS, connectType)){
					//Datasource
					updateServerPropertiesJNDI(request, dbType, jndi);
				}else{
					//JDBC
					updateServerPropertiesJDBC(request, dbType, dbSchema, loader.getDriver(dbType,driverType), username, password, userDBUrl);
				}
				response.sendRedirect("install?step=admin");
				return;
			}
			
			//pre-check if tables exist or not
			if(confirmed || !loader.isTableExist(con)){
				loader.resetTable(dbType, con);
				//success init DB
				if(StringUtils.equalsIgnoreCase(Server.CONN_TYPE_DS, connectType)){
					//Datasource
					updateServerPropertiesJNDI(request, dbType, jndi);
				}else{
					//JDBC
					updateServerPropertiesJDBC(request, dbType, dbSchema, loader.getDriver(dbType,driverType), username, password, userDBUrl);
				}
				response.sendRedirect("install?step=admin");
			}else{
				//return message, need user confirm
				request.setAttribute("existed", true);
				request.setAttribute("message","Tables already exist, do you want to reset them?");
				request.getRequestDispatcher("/WEB-INF/pages/install/createtables.jsp").forward(request, response);
			}
		} catch (DriverNotFoundException e) {
			log.error("Driver not found ", e);
			request.setAttribute("error", "No suitable database driver found. Please copy corresponding driver to your web server library directory.");
			request.getRequestDispatcher("/WEB-INF/pages/install/createtables.jsp").forward(request, response);
		} catch (SQLException e) {
			log.error("SQL error {}" , e.getNextException(),e);
			request.setAttribute("error", "Exception:" + e.toString());
			request.getRequestDispatcher("/WEB-INF/pages/install/createtables.jsp").forward(request, response);
		} catch (Exception e) {
			log.error("Unable complete table initialize task",e);
			request.setAttribute("error", "Unable create tables, please retry or create them manually.");
			request.getRequestDispatcher("/WEB-INF/pages/install/createtables.jsp").forward(request, response);
		}finally{
			if(con !=null)
				con.close();
		}

	}


	private void createAdmin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String fullname = request.getParameter("fullname");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String email = request.getParameter("email");
		
		//echo back
		request.setAttribute("fullname", fullname);
		request.setAttribute("username", username);
		request.setAttribute("password", password);
		request.setAttribute("email", email);

		String root = DataRoot.getDataRoot();
		GlobalSetting setting = GlobalSetting.loadGlobalSetting(FileUtil.getFileInputStream(root+Global.FILE));
		setting.setDefaultNotifyMail(email);
		setting.setDefaultReceiverMailAddress(email);
		try {
			setting.saveTo(root+Global.FILE);
		} catch (IOException e) {
			log.error("Unable save GlobalSetting with notify email: " +email,e);
		}
		Global.DefaultNotifyMail = email;
		Global.DefaultReceiverMail = email;
		
		DBLoader loader = new DBLoader();
		ConnectionProxy con = null;
		try {
			HttpSession s = request.getSession();
			Server server = (Server)s.getAttribute("server");
			
			con = loader.getConnection(server.getDbType(),server);
			
			loader.createUser(con,server.getDbType(), fullname,username,password,email);
			//success init DB
			response.sendRedirect("install?step=done");
		} catch (SQLException e) {
			log.error("Unable complete admin user initialize task",e);
			request.setAttribute("error", "Exception:" + e.toString());
			request.getRequestDispatcher("/WEB-INF/pages/install/createadmin.jsp").forward(request, response);
		} catch (Exception e) {
			log.error("Unable complete admin user initialize task",e);
			request.setAttribute("error", "Create administrator failed, please try again.");
			request.getRequestDispatcher("/WEB-INF/pages/install/createadmin.jsp").forward(request, response);
		}finally{
			if(con != null)
				con.close();
		}
	}

	/**
	 * @return 
	 * @throws Exception 
	 *  
	 */
	private boolean installDone(HttpServletRequest request,HttpServletResponse response){
		//clean all cache from session
		request.getSession().invalidate();

		try {
			//update install status - it is better update installation status earliy than refresh installation
			//so that system is able to work if refresh failed but restart.
			Installation install = Installation.refreshInstallation();
			updateInstallationStatus(install, Installation.STATUS_COMPLETED);
			Installation.DONE = true;
			
			//refresh to startup ApplicationContext
			ApplicationContextUtil.refreshApplcationContext(getServletContext(), ApplicationContextUtil.TARGET_STARTUP);
			
			return true;
		} catch (Exception e) {
			AuditLogger.error("Unable to reload install context",e);
			log.error("Unable to reload install context",e);
			return false;
		}
	}

	//********************************************************************
	//               some method to facility
	//********************************************************************
	private void updateInstallationStatus(Installation install, String status){
		install.setStatus(status);
		install.setVersion(Version.VERSION);
		Installation.saveInstallation(install);
	}
	
	
	private void updateServerPropertiesJNDI(HttpServletRequest request, String dbType, String jndi) 
		throws IOException {
		//update server.properties
		Server server = (Server) request.getSession().getAttribute("server");
		
		server.setDbType(dbType);
		server.setDbConnectType(Server.CONN_TYPE_DS);
		server.setDbJNDI(jndi);
		server.setDbSchema("");
		server.setDbUsername("");
		server.setDbPassword("");
		server.setDbDriverClass("");
		server.setDbUrl("");
		server.saveTo();
		
	}

	private void updateServerPropertiesJDBC(HttpServletRequest request, String dbType, String schema, String driver,String username, String password, String url) 
			throws IOException {
		//update server.properties
		Server server = (Server) request.getSession().getAttribute("server");
		
		server.setQuartzJobstoreDriver(DBLoader.detectQuartzJobstoreDriver(dbType));
		server.setDbType(dbType);
		server.setDbConnectType(Server.CONN_TYPE_JDBC);
		server.setDbDriverClass(driver);
		server.setDbSchema(schema);
		server.setDbPassword(password);
		server.setDbUsername(username);
		server.setDbUrl(url);
		server.setDbJNDI("");
		server.saveTo();
		
	}
	/**
	 * As install process may start from some steps, load init data from default place to avoid panic
	 * @throws IOException 
	 */
	private void loadInitData(HttpServletRequest request) throws IOException {
		//load root data
		String root = DataRoot.getDataRoot();
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// global
		//check if it is existed data root: does global.xml and server.properties exist?
		if(FileUtil.exist(root+Global.FILE)){
			try {
				GlobalSetting setting = GlobalSetting.loadGlobalSetting(FileUtil.getFileInputStream(root+Global.FILE));
				Global.syncFrom(setting);
			} catch (Exception e) {
				log.error("Loading global.xml failed. Are you upgrading this file?",e);
			}
		}

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// server`
		Server server = new Server();
		if(FileUtil.exist(root+Server.FILE)){
			Properties prop = FileUtil.loadProperties(root+Server.FILE);
			server.syncFrom(prop);
		}else{
			//load default value
			Properties prop = FileUtil.loadProperties(Server.FILE_DEFAULT);
			server.syncFrom(prop);
		}
		//put it to session
		request.getSession().setAttribute("server", server);
		
		//DB url pattern
		DBLoader ld = new DBLoader();
		request.getSession().setAttribute("adminUrlP", ld.getAdminURLPatterns().entrySet());
		request.getSession().setAttribute("userUrlP", ld.getUserURLPatterns().entrySet());
		
	}
	/**
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws ServletException 
	 */
	private void viewUpgrade(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String root = DataRoot.getDataRoot();
		Installation install = Installation.refreshInstallation();
		
		request.setAttribute("existVer", install.getVersion());
		request.setAttribute("newVer", Version.VERSION);
		request.setAttribute("root", root);
		request.getRequestDispatcher("/WEB-INF/pages/install/upgrade.jsp").forward(request, response);
	}



	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void viewTableCreate(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		
		//echo back value
		Server server = (Server) request.getSession().getAttribute("server");
		String dbType = server.getDbType();
		request.setAttribute("dbType", dbType);
		request.setAttribute("connectType", server.getDbConnectType());
		request.setAttribute("host", DBLoader.detectHost(dbType,server.getDbUrl()));
		request.setAttribute("dbname", DBLoader.detectDBName(dbType,server.getDbUrl()));
		request.setAttribute("userDBUrl", server.getDbUrl());
		request.setAttribute("username", server.getDbUsername());
		request.setAttribute("password", server.getDbPassword());
		request.setAttribute("jndi", server.getDbJNDI());
		request.getRequestDispatcher("/WEB-INF/pages/install/createtables.jsp").forward(request, response);
	}


	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void viewDBCreate(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		//echo back value
		Server server = (Server) request.getSession().getAttribute("server");
		String dbType =  server.getDbType();
		request.setAttribute("dbType", dbType);
		request.setAttribute("rootUser", "");
		request.setAttribute("rootPassword", "");
		request.setAttribute("host", DBLoader.detectHost(dbType,server.getDbUrl()));
		request.setAttribute("dbname", DBLoader.detectDBName(dbType,server.getDbUrl()));
		
		request.setAttribute("adminDBUrl", server.getDbUrl());
		request.setAttribute("username", server.getDbUsername());
		request.setAttribute("password", server.getDbPassword());

		request.getRequestDispatcher("/WEB-INF/pages/install/createdb.jsp").forward(request, response);
	}
	
	private void updateGlobal(String url, String servletPath) {
		//this url looks like http://localhost:8080/mycontext/install?step...; here remove the servletPath "/install"
		url = url.substring(0,url.length()-servletPath.length());
		
		Global.resetURLInfo(url);
		
		//use system default timezone
		Global.DefaultTimeZone = TimeZone.getDefault().getID();
		
		//save
		GlobalSetting gs = new GlobalSetting();
		Global.syncTo(gs);
		try {
			gs.saveTo(DataRoot.getDataRoot()+Global.FILE);
		} catch (IOException e) {
			log.error("Unable save GlobalSetting with URL :" + url,e);
		}
		
		
	}


	/**
	 * 15/09/2008: this is not true in below comments, as I remove skinTag dependent (move style into static/styles rather than skin/default/styles)/
	 * But I still keep this code as it is not harmful.
	 * <br> 
	 * JSP page rely on SkinTag, which is rely on the Global.contextPath. Some pages display before context path is decided, which is saved 
	 * into global.xml only saveURLInfo() after data root is created.  So use this method to temporarily put contenxtPath to Global static.
	 */
	private void resetContextPath(HttpServletRequest request) {
		String url = request.getRequestURL().toString();
		String servletPath = request.getServletPath();
		url = url.substring(0,url.length()-servletPath.length());
		Global.resetURLInfo(url);
	}

	private UpgradeService getUpgradeService(){
		
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
		return (UpgradeService) ctx.getBean(UpgradeService.SERVICE_NAME);
	}


}
