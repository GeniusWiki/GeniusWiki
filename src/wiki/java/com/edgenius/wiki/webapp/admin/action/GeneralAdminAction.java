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
package com.edgenius.wiki.webapp.admin.action;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.edgenius.core.Global;
import com.edgenius.core.GlobalSetting;
import com.edgenius.core.Server;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.service.impl.JavaMailSenderImpl;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.Shell;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Draft;
import com.edgenius.wiki.quartz.QuartzException;
import com.edgenius.wiki.quartz.VersionCheckJobInvoker;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.SettingServiceException;
import com.edgenius.wiki.service.ThemeService;
import com.edgenius.wiki.service.WidgetService;
import com.edgenius.wiki.util.WikiUtil;
import com.edgenius.wiki.webapp.action.BaseAction;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class GeneralAdminAction extends BaseAction{
	private static final long MB = 1024*1024;
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// parameters
//	private String host;
	private String systemTitle;
	private String baseURL;
	private String notifyEmail;
	private String receiverEmail;
	private boolean ccSysAdmins = false;
	private int spaceQuota;
	private boolean allSpacesQuota;
	private int removeDelay;
	private int syncFeq;
	private boolean allowPublic = false;
	private boolean detectLocale = false;
	private String sysLang;
	private boolean allowSE = false;
	
	//Mail Server(0), JNDI (1)
	private int mailType;
	
	private String smtpHost;
	private String smtpPort;
	private boolean smtpAuth = false;
	private String mailUsername;
	private String mailPassword;
	//none(0) TLS(1) SSL(2)
	private int smtpConnectType;
	//JNDI name
	private String smtpJNDI;
	
	private boolean versionCheck = false;
	
	private File file;
    private String fileContentType;
    private String fileFileName;
    
    private boolean soap;
    private boolean rest;
    private boolean shell;
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// service
	private SettingService settingService;
	private ThemeService themeService;
	private RepositoryService repositoryService;
	private VersionCheckJobInvoker versionCheckJobInvoker;
	private JavaMailSenderImpl mailSender;
	private WidgetService widgetService;
	
	//********************************************************************
	//               function methods
	//********************************************************************
	public String  execute(){
		//load from Global, Server setting
		GlobalSetting global = settingService.getGlobalSetting();
		baseURL = WebUtil.getHostAppURL();
		spaceQuota = (int) (global.getSpaceQuota()/MB);
		removeDelay = global.getDelayRemoveSpaceHours();
		syncFeq = global.getDelayOfflineSyncMinutes();
		allowPublic = !global.hasSuppress(SharedConstants.SUPPRESS.SIGNUP.name());
		detectLocale = global.isDetectLocaleFromRequest();
		sysLang = (global.getDefaultLanguage()+"_" + global.getDefaultCountry()).toLowerCase();
		allowSE = global.isPublicSearchEngineAllow();
		notifyEmail = global.getDefaultNotifyMail();
		receiverEmail = global.getDefaultReceiverMailAddress();
		ccSysAdmins = global.isCcToSystemAdmin();
		systemTitle = global.getSystemTitle();
		versionCheck = global.isVersionCheck();
		soap = Global.isEnabled(global.getWebservice());
		rest = Global.isEnabled(global.getRestservice());
		
		shell = Shell.enabled;
		Server server = settingService.getServerSetting();
		//default values 
		smtpHost = "";
		smtpPort = "";
		mailUsername = "";
		mailPassword = "";
		smtpAuth = false;
		smtpConnectType = 0;
		smtpJNDI = "";
		if(StringUtils.isBlank(server.getMailHost()) && !StringUtils.isBlank(server.getMailJndi())){
			//JNDI mode
			smtpJNDI = server.getMailJndi();
			mailType = 1;
		}else{
			mailType = 0;
			//SMTP host mode - could also if user doesn't setup any email options
			smtpHost = server.getMailHost();
			if(StringUtils.isBlank(smtpHost)){
				mailType = -1;
			}else{
				smtpPort = server.getMailHostPort();
				if(StringUtils.isBlank(server.getMailUsername())){
					smtpAuth = false;
				}else{
					smtpAuth = true;
					mailUsername = server.getMailUsername();
					mailPassword = server.getMailPassword();
					String prop = server.getMailProperties();
					if(prop.indexOf("mail.smtp.socketFactory.class:javax.net.ssl.SSLSocketFactory") > 0)
						smtpConnectType = 2; //SSL
					else if(prop.indexOf("mail.smtp.starttls.enable:true") > 0)
						smtpConnectType = 1; //TSL
					else
						smtpConnectType = 0; //None
				}
			}			
		}
		

		return SUCCESS;
	}

	public String update(){
		try {
			
			GlobalSetting global = settingService.getGlobalSetting();
			//this just for confirm Global and GlobalSetting has consistent value
			Global.syncFrom(global);
			Global.resetURLInfo(baseURL);
			//copy back base URL information to global setting.
			Global.syncTo(global);
			
			//other global setting 
			global.setDefaultNotifyMail(notifyEmail);
			global.setDefaultReceiverMailAddress(receiverEmail);
			global.setCcToSystemAdmin(ccSysAdmins);
			
			global.setDelayRemoveSpaceHours(removeDelay);
			global.setSpaceQuota(spaceQuota*MB);
			if(allowPublic)
				global.removeSuppress(SharedConstants.SUPPRESS.SIGNUP.name());
			else
				global.addSuppress(SharedConstants.SUPPRESS.SIGNUP.name());
			global.setDelayOfflineSyncMinutes(syncFeq);
			global.setPublicSearchEngineAllow(allowSE);
			global.setDetectLocaleFromRequest(detectLocale);
			String[] cl = sysLang.split("_");
			if(cl.length == 2){
				global.setDefaultLanguage(cl[0]);
				global.setDefaultCountry(cl[1].toUpperCase());
			}
			//for display - only accept lower case
			sysLang = sysLang.toLowerCase();
			//Widget load title and desc from i18n file, here reset widget, then it will reload i18n again before next render.
			widgetService.resetWidgets();
			
			global.setSystemTitle(systemTitle);
			boolean oldVc = global.isVersionCheck();
			global.setVersionCheck(versionCheck);
			
			global.setWebservice(Global.enable(soap));
			global.setRestservice(Global.enable(rest));
			settingService.saveOrUpdateGlobalSetting(global);
			
			if(oldVc != versionCheck){
				try {
					if(versionCheck)
						versionCheckJobInvoker.invokeJob();
					else
						versionCheckJobInvoker.cancelJob();
				} catch (QuartzException e) {
					getRequest().setAttribute("error","Unable to reset version check job status.");
					log.error("Unable to reset version check job status",e);
				}
			}
			
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// Shell
			Shell.enabled = shell;
			if(!Shell.enabled){
				Shell.key = null;
			}else{
				//acquire shell key now
				if(!Shell.requestInstanceShellKey()){
					Shell.enabled = false;
					shell = false;
					getRequest().setAttribute("error","Failed to connect Shell website " + Shell.rootUrl + ". Please ensure Shell website is running and try again.");
				}
			}
			Shell.save();
			
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// Mail SMTP
			Server server = settingService.getServerSetting();
			//reset SMTP host values
			server.setMailHost("");
			server.setMailHostPort("-1");
			server.setMailUsername("");
			server.setMailPassword("");
			server.setMailProperties("");
			server.setMailJndi("");
			if(mailType == 0){
				//Smtp host mode
				smtpHost = StringUtils.trimToEmpty(smtpHost);
				//parse smtpHost - remove protocol prefix and slip port and host
				if(smtpHost.indexOf("://") > 0){  
					//remove stmp://
					smtpHost = smtpHost.substring(smtpHost.indexOf("://")+3);
				}
				server.setMailHost(smtpHost);
				server.setMailHostPort(smtpPort);
				if(smtpAuth){
					server.setMailUsername(mailUsername);
					server.setMailPassword(mailPassword);
					if(smtpConnectType == 2){ //SSL
						server.setMailProperties("mail.smtp.auth:true;;mail.smtp.socketFactory.class:javax.net.ssl.SSLSocketFactory");
					}else if(smtpConnectType == 1){ //TLS
						server.setMailProperties("mail.smtp.auth:true;;mail.smtp.starttls.enable:true");
					}else{
						server.setMailProperties("mail.smtp.auth:true");
					}
				}
				
				//reload mail session
				try {
					mailSender.setHost(smtpHost);
					mailSender.setPort(NumberUtils.toInt(smtpPort));
					mailSender.setUsername(mailUsername);
					mailSender.setPassword(mailPassword);
					mailSender.setMailProperties(server.getMailProperties());
					mailSender.resetMailSessionByProperties();
				} catch (Exception e) {
					log.error("Reset mail by properties with error",e);
					getRequest().setAttribute("error","Reset mail by properties with error: "+ e.getMessage());
				}
			}else if (mailType == 1){
				if(!StringUtils.equals(server.getMailJndi(),smtpJNDI)){
					//reload mail session bean
					try {
						mailSender.resetMailSessionByJNDI(smtpJNDI);
					} catch (Exception e) {
						log.error("Reset mail JNDI with error",e);
						getRequest().setAttribute("error","Reset mail JNDI with error: " + e.getMessage());
					}
				}
				
				//JNDI mode
				server.setMailJndi(smtpJNDI);
			} //not set
			settingService.saveOrUpdateServerSetting(server);
	
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// Space Quota
			if(allSpacesQuota){
				int update = repositoryService.updateExistWorkspacesQuota(spaceQuota*MB);
				log.info(update + " workspaces update quota to " + spaceQuota + "MB");
			}
		} catch (SettingServiceException e) {
			log.error("Update server.properties failed",e);
		}
		return SUCCESS;
	}
	
	public String upload(){
		try {
			User user = WikiUtil.getUser();
			FileNode att = new FileNode();
			att.setShared(false);
			att.setFile(new FileInputStream(file));
			att.setFilename(fileFileName);
			att.setContentType(fileContentType);
			att.setType(RepositoryService.TYPE_INSTNACE);
			att.setIdentifier(user.getUsername());
			att.setCreateor(user.getFullname());
			att.setStatus(Draft.NONE_DRAFT);
			att.setSize(0);
			themeService.uploadSystemLogo(att);
		} catch (Exception e) {
			log.error("Failed upload logo",e);
		}
		
		return "uploadDone";
	}
	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	//********************************************************************
	//               set /get
	//********************************************************************
	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public void setSmtpAuth(boolean smtpAuth) {
		this.smtpAuth = smtpAuth;
	}

	public void setMailUsername(String mailUsername) {
		this.mailUsername = mailUsername;
	}

	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}

	public void setSpaceQuota(int attachSize) {
		this.spaceQuota = attachSize;
	}

	public void setRemoveDelay(int removeDelay) {
		this.removeDelay = removeDelay;
	}

	public void setSyncFeq(int syncFeq) {
		this.syncFeq = syncFeq;
	}

	public void setAllowPublic(boolean allowPublic) {
		this.allowPublic = allowPublic;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public boolean isSmtpAuth() {
		return smtpAuth;
	}

	public String getMailUsername() {
		return mailUsername;
	}

	public String getMailPassword() {
		return mailPassword;
	}

	public int getSpaceQuota() {
		return spaceQuota;
	}

	public int getRemoveDelay() {
		return removeDelay;
	}

	public int getSyncFeq() {
		return syncFeq;
	}

	public boolean isAllowPublic() {
		return allowPublic;
	}

	public boolean isAllowSE() {
		return allowSE;
	}

	public int getMailType() {
		return mailType;
	}

	public void setMailType(int mailType) {
		this.mailType = mailType;
	}

	public int getSmtpConnectType() {
		return smtpConnectType;
	}

	public void setSmtpConnectType(int smtpConnectType) {
		this.smtpConnectType = smtpConnectType;
	}

	public String getSmtpJNDI() {
		return smtpJNDI;
	}

	public void setSmtpJNDI(String smtpJNDI) {
		this.smtpJNDI = smtpJNDI;
	}

	public void setMailSender(JavaMailSenderImpl mailSender) {
		this.mailSender = mailSender;
	}

	public void setAllowSE(boolean allowSE) {
		this.allowSE = allowSE;
	}

	public boolean isVersionCheck() {
		return versionCheck;
	}

	public void setVersionCheck(boolean versionCheck) {
		this.versionCheck = versionCheck;
	}

	public String getNotifyEmail() {
		return notifyEmail;
	}

	public void setNotifyEmail(String notifyEmail) {
		this.notifyEmail = notifyEmail;
	}

	public String getReceiverEmail() {
		return receiverEmail;
	}

	public void setReceiverEmail(String receiverEmail) {
		this.receiverEmail = receiverEmail;
	}

	public String getSystemTitle() {
		return systemTitle;
	}

	public void setSystemTitle(String systemTitle) {
		this.systemTitle = systemTitle;
	}

	public String getFileContentType() {
		return fileContentType;
	}

	public void setVersionCheckJobInvoker(VersionCheckJobInvoker versionCheckJobInvoker) {
		this.versionCheckJobInvoker = versionCheckJobInvoker;
	}

	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
	}

	public String getFileFileName() {
		return fileFileName;
	}

	public void setFileFileName(String fileFileName) {
		this.fileFileName = fileFileName;
	}

	public void setThemeService(ThemeService themeService) {
		this.themeService = themeService;
	}

	/**
	 * @param repositoryService the repositoryService to set
	 */
	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public boolean isCcSysAdmins() {
		return ccSysAdmins;
	}

	public void setCcSysAdmins(boolean ccToSysAdmins) {
		this.ccSysAdmins = ccToSysAdmins;
	}

	public boolean isAllSpacesQuota() {
		return allSpacesQuota;
	}

	public void setAllSpacesQuota(boolean allSpacesQuota) {
		this.allSpacesQuota = allSpacesQuota;
	}

	public boolean isDetectLocale() {
		return detectLocale;
	}

	public void setDetectLocale(boolean detectLocale) {
		this.detectLocale = detectLocale;
	}

	public String getSysLang() {
		return sysLang;
	}

	public void setSysLang(String sysLang) {
		this.sysLang = sysLang;
	}

	public void setWidgetService(WidgetService widgetService) {
		this.widgetService = widgetService;
	}

	public boolean isSoap() {
		return soap;
	}

	public void setSoap(boolean soap) {
		this.soap = soap;
	}

	public boolean isRest() {
		return rest;
	}

	public void setRest(boolean rest) {
		this.rest = rest;
	}

	public boolean isShell() {
		return shell;
	}

	public void setShell(boolean shell) {
		this.shell = shell;
	}
}
