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
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.edgenius.core.util.FileUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 * Global setting class. It will initial value from global configuration file then copying value to Global class, which only contain 
 * static variable for system real use.
 * @author Dapeng.Ni
 */
public class GlobalSetting implements Serializable{
	private static final long serialVersionUID = 9111324747968710226L;
	
	private String defaultNotifyMail;
	private String defaultReceiverMailAddress;
	private boolean ccToSystemAdmin;
	private String systemTitle;
	private String defaultDirection;
	private String defaultLanguage;
	private String defaultCountry;
	private String defaultTimeZone;
	
	//allow user login from username or email
	private String defaultLoginKey;
	
	private boolean publicSearchEngineAllow;
	private String hostProtocol;
	private String hostName;
	private String contextPath;
	private int hostPort;
	private long spaceQuota;
	
	private boolean encryptPassword;
	private String encryptAlgorithm;
	
	private int delayRemoveSpaceHours;
	private boolean enableAdminPermControl;
	private int  delayOfflineSyncMinutes;
	private String registerMethod;
	private String maintainJobCron;
	private String commentsNotifierCron;
	private int maxCommentsNotifyPerDay;
	private int autoFixLinks;

	private String adsense;
	
	private String textnut;
	private String skin;
	
	//enable or disable
	private String webservice;
	//basic, ws-security, etc.
	private String webserviceAuthenticaton;
	
	//enable or disable
	private String restservice;
	//basic, ws-security, etc.
	private String restserviceAuthenticaton;
	
	private String suppress;
	private boolean detectLocaleFromRequest;
	private boolean versionCheck;
	private String versionCheckCron;
	private int purgeDaysOldActivityLog;
	
	public String twitterOauthConsumerKey;
	public String twitterOauthConsumerSecret;
	
	//********************************************************************
	//               function 
	//********************************************************************
	/**
	 * Please note, this method will try to close InputStream after loading - it means the following 
	 * code can not use that stream again after this method.
	 */
	public static GlobalSetting loadGlobalSetting(InputStream in){
		try {
			XStream xstream = new XStream();
			xstream.registerConverter(new IntConvert());
			xstream.registerConverter(new LongConvert());
			return (GlobalSetting) xstream.fromXML(in);
			
		} finally{
			try {
				in.close();
			} catch (Exception e2) {
				// nothing;
			}
		}
	}
	/**
	 * Only used in upgrade/install services. For other purpose, use SettingServiceImpl.saveOrUpdateGlobalSetting() instead.
	 */
	public void saveTo(String file) throws IOException {
		FileOutputStream os = null;
		try {
			os = FileUtil.getFileOutputStream(file);
			XStream xstream = new XStream();
			xstream.toXML(this,os);
			os.flush();
		} finally{
			if(os != null)
				try {os.close();} catch (IOException e) {}
		}

	}
	/**
	 * Add suppress to current suppress string, use comma to separator,and do nothing if it already existed.
	 * @param supress
	 */
	public void addSuppress(String newSuppress) {
		if(suppress != null && suppress.toUpperCase().indexOf(newSuppress.toUpperCase()) != -1)
			return;
		
		suppress += (!StringUtils.isBlank(suppress)?",":"") + newSuppress.toUpperCase();
		
	}
	/**
	 * Remove suppress from current suppress string, and do nothing if it doesn't exist.
	 * @param supress
	 */
	public void removeSuppress(String newSuppress) {
		if(StringUtils.isBlank(suppress) 
			|| suppress.toUpperCase().indexOf(newSuppress.toUpperCase()) == -1)
			return;
		
		suppress = StringUtils.remove(suppress.toUpperCase(), newSuppress.toUpperCase());
		if(suppress.length() > 0){
			if(suppress.startsWith(",")){
				//first suppress removed, and has more
				suppress = suppress.substring(1);
			}else if(suppress.endsWith(",")){
				//last suppress removed, and has more
				suppress = suppress.substring(0, suppress.length()-1);
			}else{
				//middle suppress removed
				suppress = suppress.replace(",,", ",");
			}
		}
	}	
	/**
	 * @param supressSignup
	 * @return
	 */
	public boolean hasSuppress(String hasSupress) {
		return (suppress != null && suppress.toUpperCase().indexOf(hasSupress.toUpperCase()) != -1);
	}
	/**
	 * Replacement of XStream default int convert so that can support empty string rather than throw NumberFormatException
	 * @author Dapeng.Ni
	 */
	private static class IntConvert extends AbstractSingleValueConverter{

		public boolean canConvert(Class clz) {
			return clz.equals(int.class) || clz.equals(Integer.class);
		}

		public Object fromString(String str) {
			return NumberUtils.toInt(str, 0);
		}
		
	}
	private static class LongConvert extends AbstractSingleValueConverter{
		public boolean canConvert(Class clz) {
			return clz.equals(long.class) || clz.equals(Long.class);
		}
		
		public Object fromString(String str) {
			return NumberUtils.toLong(str, 0);
		}
		
	}

	//********************************************************************
	//               set / get
	//********************************************************************
	public String getMaintainJobCron() {
		return maintainJobCron;
	}
	public void setMaintainJobCron(String maintainJobCron) {
		this.maintainJobCron = maintainJobCron;
	}
	public String getRegisterMethod() {
		return registerMethod;
	}
	public void setRegisterMethod(String registerMethod) {
		this.registerMethod = registerMethod;
	}
	public int getDelayOfflineSyncMinutes() {
		return delayOfflineSyncMinutes;
	}
	public void setDelayOfflineSyncMinutes(int delayOfflineSyncMinutes) {
		this.delayOfflineSyncMinutes = delayOfflineSyncMinutes;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostAddress) {
		this.hostName = hostAddress;
	}
	public String getContextPath() {
		return contextPath;
	}
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	public String getDefaultDirection() {
		return defaultDirection;
	}
	public void setDefaultDirection(String defaultDirection) {
		this.defaultDirection = defaultDirection;
	}

	public String getTextnut() {
		return textnut;
	}
	public String getSkin() {
		//Skin.default - in wiki package :(
		return StringUtils.isBlank(skin)?"default":skin;
	}
	public void setTextnut(String textnut) {
		this.textnut = textnut;
	}
	public void setSkin(String skin) {
		this.skin = skin;
	}
	public String getDefaultTimeZone() {
		return defaultTimeZone;
	}
	public void setDefaultTimeZone(String defaultTimeZone) {
		this.defaultTimeZone = defaultTimeZone;
	}
	public String getAdsense() {
		return adsense;
	}
	public void setAdsense(String adsense) {
		this.adsense = adsense;
	}
	public int getHostPort() {
		return hostPort;
	}
	public void setHostPort(int hostPort) {
		this.hostPort = hostPort;
	}
	public String getDefaultCountry() {
		return defaultCountry;
	}
	public String getTwitterOauthConsumerKey() {
		return twitterOauthConsumerKey;
	}
	public void setTwitterOauthConsumerKey(String twitterOauthConsumerKey) {
		this.twitterOauthConsumerKey = twitterOauthConsumerKey;
	}
	public String getTwitterOauthConsumerSecret() {
		return twitterOauthConsumerSecret;
	}
	public void setTwitterOauthConsumerSecret(String twitterOauthConsumerSecret) {
		this.twitterOauthConsumerSecret = twitterOauthConsumerSecret;
	}
	public void setDefaultCountry(String defaultCountry) {
		this.defaultCountry = defaultCountry;
	}
	public String getDefaultLanguage() {
		return defaultLanguage;
	}
	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}
	public int getPurgeDaysOldActivityLog() {
		return purgeDaysOldActivityLog;
	}
	public void setPurgeDaysOldActivityLog(int purgeDaysOldActivityLog) {
		this.purgeDaysOldActivityLog = purgeDaysOldActivityLog;
	}
	public String getVersionCheckCron() {
		return versionCheckCron;
	}
	public void setVersionCheckCron(String versionCheckCron) {
		this.versionCheckCron = versionCheckCron;
	}
	public boolean isVersionCheck() {
		return versionCheck;
	}
	public void setVersionCheck(boolean versionCheck) {
		this.versionCheck = versionCheck;
	}
	public boolean isDetectLocaleFromRequest() {
		return detectLocaleFromRequest;
	}
	public void setDetectLocaleFromRequest(boolean detectLocaleFromRequest) {
		this.detectLocaleFromRequest = detectLocaleFromRequest;
	}
	public String getDefaultLoginKey() {
		return defaultLoginKey;
	}
	public void setDefaultLoginKey(String defaultLoginKey) {
		this.defaultLoginKey = defaultLoginKey;
	}
	public String getEncryptAlgorithm() {
		return encryptAlgorithm;
	}
	public void setEncryptAlgorithm(String encryptAlgorithm) {
		this.encryptAlgorithm = encryptAlgorithm;
	}
	public boolean isEncryptPassword() {
		return encryptPassword;
	}
	public void setEncryptPassword(boolean encryptPassword) {
		this.encryptPassword = encryptPassword;
	}


	public long getSpaceQuota() {
		return spaceQuota;
	}
	public void setSpaceQuota(long spaceQuota) {
		this.spaceQuota = spaceQuota;
	}
	public String getHostProtocol() {
		return hostProtocol;
	}
	public void setHostProtocol(String hostProtocol) {
		this.hostProtocol = hostProtocol;
	}
	public int getDelayRemoveSpaceHours() {
		return delayRemoveSpaceHours;
	}
	public void setDelayRemoveSpaceHours(int delayRemoveSpaceHours) {
		this.delayRemoveSpaceHours = delayRemoveSpaceHours;
	}
	public boolean isPublicSearchEngineAllow() {
		return publicSearchEngineAllow;
	}
	public void setPublicSearchEngineAllow(boolean publicSearchEngineAllow) {
		this.publicSearchEngineAllow = publicSearchEngineAllow;
	}
	public boolean isEnableAdminPermControl() {
		return enableAdminPermControl;
	}
	public void setEnableAdminPermControl(boolean enableAdminPermControl) {
		this.enableAdminPermControl = enableAdminPermControl;
	}

	public String getCommentsNotifierCron() {
		return commentsNotifierCron;
	}
	public void setCommentsNotifierCron(String commentsNotifierCron) {
		this.commentsNotifierCron = commentsNotifierCron;
	}
	public int getMaxCommentsNotifyPerDay() {
		return maxCommentsNotifyPerDay;
	}
	public void setMaxCommentsNotifyPerDay(int maxCommentsNotifyPerDay) {
		this.maxCommentsNotifyPerDay = maxCommentsNotifyPerDay;
	}
	public int getAutoFixLinks() {
		return autoFixLinks;
	}
	public void setAutoFixLinks(int autoFixLink) {
		this.autoFixLinks = autoFixLink;
	}
	public String getDefaultNotifyMail() {
		return defaultNotifyMail;
	}
	public void setDefaultNotifyMail(String defaultNotifyMailList) {
		defaultNotifyMail = defaultNotifyMailList;
	}

	public String getDefaultReceiverMailAddress() {
		return defaultReceiverMailAddress;
	}

	public void setDefaultReceiverMailAddress(String defaultReceiverMailAddress) {
		this.defaultReceiverMailAddress = defaultReceiverMailAddress;
	}

	public boolean isCcToSystemAdmin() {
		return ccToSystemAdmin;
	}

	public void setCcToSystemAdmin(boolean ccToSystemAdmin) {
		this.ccToSystemAdmin = ccToSystemAdmin;
	}

	public String getSystemTitle() {
		return systemTitle;
	}

	public void setSystemTitle(String systemTitle) {
		this.systemTitle = systemTitle;
	}


	public String getWebservice() {
		return webservice;
	}
	public void setWebservice(String webservice) {
		this.webservice = webservice;
	}
	public String getWebserviceAuthenticaton() {
		return webserviceAuthenticaton;
	}
	public void setWebserviceAuthenticaton(String webserviceAuthenticaton) {
		this.webserviceAuthenticaton = webserviceAuthenticaton;
	}
	public String getSuppress() {
		return suppress;
	}
	public void setSuppress(String suppress) {
		this.suppress = suppress;
	}
	public String getRestservice() {
		return restservice;
	}
	public void setRestservice(String restservice) {
		this.restservice = restservice;
	}
	public String getRestserviceAuthenticaton() {
		return restserviceAuthenticaton;
	}
	public void setRestserviceAuthenticaton(String restserviceAuthenticaton) {
		this.restserviceAuthenticaton = restserviceAuthenticaton;
	}


}

