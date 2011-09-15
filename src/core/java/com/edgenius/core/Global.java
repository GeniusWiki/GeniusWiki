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

import java.io.Serializable;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.Constants.SUPPRESS;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class Global implements Serializable{
	public static final String FILE = "global.xml";
	public static final String DEFAULT_GLOBAL_XML = "classpath:geniuswiki/global.default.xml";
	
	//this variable is not from global.xml, but when server starting, it gets from StartupListener.contextInitialized(ServletContextEvent)
	public static String ServerInstallRealPath = null;
	
	//feedback marco
	public static String DefaultReceiverMail = "feedback@geniuswiki.com";
	//if above feedback message will cc to all system administrators
	public static boolean ccToSystemAdmin = true;
	
	public static String SystemTitle= "Diversify your website |GeniusWiki";
	//user for user signup, page change, email "From"
	public static String DefaultNotifyMail = "notify@geniuswiki.com";

	//System session timeout. Unit is Second
	//this direction setting is useless now
	public static String DefaultDirection = Constants.DIRECT_LEFT;
	public static String DefaultLanguage = "en";
	public static String DefaultCountry = "AU";
	//Just use GMT as default time zone
	public static String DefaultTimeZone = "GMT";
	//allow user login from username or email
	public static String DefaultLoginKey = "username";
	
	//allow google/yahoo etc search engine robot index wiki content
	public static boolean PublicSearchEngineAllow =true;
	
	public static String SysContextPath="";
	public static String SysHostAddress="localhost:8080";
	public static String SysHostProtocol="http://";

	
	//byte of quota space attachment 200M
	public static long SpaceQuota = 1024*1024*200;
	
	//password encrypt 
	//This field does not easy works, as these definition of applicationContext-security.xml need change as well
	//<bean id="passwordEncoder" class="org.springframework.security.providers.encoding.Md5PasswordEncoder"/>
	public static boolean EncryptPassword = true;
	public static String PasswordEncodingAlgorithm = "MD5";
	
	//hours
	public static int DelayRemoveSpaceHours = 48;

	//Note, when geniuswiki is enterprise usage, this function is better enable to make user clear view
	//but for public site, it is better make things simple, then hide admin permission.
	public static boolean EnableAdminPermControl = false;

	//how often does offline sync(download) will happen.
	public static int DelayOfflineSyncMinutes = 30;
	
	//signup: allow user free signup, approval: require system admin approval, maybe there are more options, ...no implementation yet
	public static String registerMethod = "signup";
	
	//refer to http://www.quartz-scheduler.org/docs/tutorials/crontrigger.html
	//every day 3am
	public static String MaintainJobCron = "0 0 3 * * ?";
	
	//11pm, send out page comments summary
	public static String CommentsNotifierCron = "0 0 23 * * ?";
	//how many maximum email notify sent out per day 
	public static int MaxCommentsNotifyPerDay = 2;
	
	//don't fix link at moment as it is dangerous
	//bad package reference- WikiConstants.AUTO_FIX_COPY_LINK|WikiConstants.AUTO_FIX_TITLE_CHANGE_LINK;
	public static int AutoFixLinks = 3;
	
	public static boolean webServiceEnabled = false;
	public static String webServiceAuth= "basic";
	
	public static boolean restServiceEnabled = false;
	public static String restServiceAuth= "basic";
	
	//suppress function: signup, logout...
	//@see WikiConstants.SUPRESS_*
	public static int suppress;
	public static boolean DetectLocaleFromRequest = false;
	public static boolean VersionCheck = true;
	//every day 2am
	public static String VersionCheckCron = "0 0 2 * * ?";

	//how old activity log need to be purged, by days. 
	//if it is equal or less zero, log never be purged.
	public static int PurgeDaysOldActivityLog = 365;
	
	public static boolean ADSENSE = false;
	public static boolean TEXTNUT = false;
	public static String Skin = "default";
	
	//this is holder of current http request(thread) suppress value - it mixes Global setting and "suppress" parameter in URL.
	private transient final static ThreadLocal<Integer> currentSuppress = new ThreadLocal<Integer>();

	
	public static String TwitterOauthConsumerKey;
	public static String TwitterOauthConsumerSecret;
	
	public static void syncTo(GlobalSetting setting){
		
        setting.setDefaultDirection(Global.DefaultDirection);
        setting.setDefaultLanguage(Global.DefaultLanguage);
        setting.setDefaultCountry(Global.DefaultCountry);
        setting.setDefaultTimeZone(Global.DefaultTimeZone);
        setting.setDefaultLoginKey(Global.DefaultLoginKey);
        setting.setHostProtocol(Global.SysHostProtocol);
        if(StringUtils.indexOf(Global.SysHostAddress,':') != -1){
        	String[] addr = StringUtils.split(Global.SysHostAddress,':');
        	setting.setHostName(addr[0]);
        	setting.setHostPort(Integer.parseInt(addr[1]));
        }else{
        	setting.setHostName(Global.SysHostAddress);
        	if(Global.SysHostProtocol != null && Global.SysHostProtocol.startsWith("https:"))
        		setting.setHostPort(443);
        	else
        		setting.setHostPort(80);
        }
        
        setting.setPublicSearchEngineAllow(Global.PublicSearchEngineAllow);
        setting.setContextPath(StringUtils.trim(Global.SysContextPath));
        setting.setEncryptAlgorithm(Global.PasswordEncodingAlgorithm);
        setting.setEncryptPassword(Global.EncryptPassword);
        setting.setSpaceQuota(Global.SpaceQuota);
        setting.setDelayRemoveSpaceHours(Global.DelayRemoveSpaceHours);
        setting.setEnableAdminPermControl(Global.EnableAdminPermControl);
        setting.setDelayOfflineSyncMinutes(Global.DelayOfflineSyncMinutes);
        setting.setRegisterMethod(Global.registerMethod);
        setting.setMaintainJobCron(Global.MaintainJobCron);
        setting.setCommentsNotifierCron(Global.CommentsNotifierCron);
        setting.setMaxCommentsNotifyPerDay(Global.MaxCommentsNotifyPerDay);
        setting.setAutoFixLinks(Global.AutoFixLinks);
        setting.setDefaultNotifyMail(Global.DefaultNotifyMail);
        setting.setCcToSystemAdmin(Global.ccToSystemAdmin);
        setting.setDefaultReceiverMailAddress(Global.DefaultReceiverMail);
        setting.setSystemTitle(Global.SystemTitle);
        
        setting.setAdsense(enable(Global.ADSENSE));
        setting.setTextnut(enable(Global.TEXTNUT));
        setting.setSkin(Global.Skin);
        
        setting.setWebservice(enable(Global.webServiceEnabled));
        setting.setWebserviceAuthenticaton(StringUtils.isBlank(Global.webServiceAuth)?"basic":Global.webServiceAuth);
        
        setting.setRestservice(enable(Global.restServiceEnabled));
        setting.setRestserviceAuthenticaton(StringUtils.isBlank(Global.restServiceAuth)?"basic":Global.restServiceAuth);
        
        setting.setDetectLocaleFromRequest(Global.DetectLocaleFromRequest);
        
        setting.setVersionCheck(Global.VersionCheck);
        setting.setVersionCheckCron(Global.VersionCheckCron);
        setting.setPurgeDaysOldActivityLog(Global.PurgeDaysOldActivityLog);
        
        //convert suppress value to names 
        StringBuffer supStr = new StringBuffer();
        if(Global.suppress > 0){
        	for(SUPPRESS sup: SUPPRESS.values()){
        		if((sup.getValue() & Global.suppress) > 0){
        			supStr.append(sup.name()).append(",");
        		}
        	}
        	if(supStr.length() > 0){
        		supStr.deleteCharAt(supStr.length()-1);
        	}
        }
        setting.setSuppress(supStr.toString());
        
        setting.setTwitterOauthConsumerKey(Global.TwitterOauthConsumerKey);
        setting.setTwitterOauthConsumerSecret(Global.TwitterOauthConsumerSecret);
	}

	/**
	 * @param setting
	 */
	public static void syncFrom(GlobalSetting setting) {
		if(setting == null)
			return;
		
		Global.DefaultDirection = setting.getDefaultDirection();
		Global.DefaultLanguage = setting.getDefaultLanguage();
		Global.DefaultCountry = setting.getDefaultCountry();
		Global.DefaultTimeZone = setting.getDefaultTimeZone();
		Global.DefaultLoginKey = setting.getDefaultLoginKey();
		Global.PublicSearchEngineAllow = setting.isPublicSearchEngineAllow();
		Global.SysContextPath = setting.getContextPath();
		Global.SysHostProtocol = setting.getHostProtocol();
		if((setting.getHostPort() == 80 &&  setting.getHostProtocol().startsWith("http:"))
			|| (setting.getHostPort() == 443  &&  setting.getHostProtocol().startsWith("https:"))
			|| setting.getHostPort() == 0)
			Global.SysHostAddress = setting.getHostName();
		else
			Global.SysHostAddress = setting.getHostName() + ":" + setting.getHostPort();
		
		Global.PasswordEncodingAlgorithm = setting.getEncryptAlgorithm();
		Global.EncryptPassword = setting.isEncryptPassword();
		Global.SpaceQuota = setting.getSpaceQuota();
		Global.DelayRemoveSpaceHours = setting.getDelayRemoveSpaceHours();
		Global.EnableAdminPermControl = setting.isEnableAdminPermControl();
		Global.DelayOfflineSyncMinutes = setting.getDelayOfflineSyncMinutes();
		Global.registerMethod = setting.getRegisterMethod();
		Global.MaintainJobCron = setting.getMaintainJobCron();
		Global.CommentsNotifierCron = setting.getCommentsNotifierCron();
		Global.MaxCommentsNotifyPerDay = setting.getMaxCommentsNotifyPerDay();
		Global.AutoFixLinks = setting.getAutoFixLinks();
		
		Global.DefaultNotifyMail = setting.getDefaultNotifyMail();
		Global.DefaultReceiverMail = setting.getDefaultReceiverMailAddress();
		Global.ccToSystemAdmin =  setting.isCcToSystemAdmin();
		Global.SystemTitle = setting.getSystemTitle();
		
		Global.webServiceEnabled = isEnabled(setting.getWebservice());
		Global.webServiceAuth =  StringUtils.isBlank(setting.getWebserviceAuthenticaton())?"basic":StringUtils.trim(setting.getWebserviceAuthenticaton());
		
		Global.restServiceEnabled = isEnabled(setting.getRestservice());
		Global.restServiceAuth =  StringUtils.isBlank(setting.getRestserviceAuthenticaton())?"basic":StringUtils.trim(setting.getRestserviceAuthenticaton());
		
		Global.ADSENSE = isEnabled(setting.getAdsense());
		Global.TEXTNUT = isEnabled(setting.getTextnut());
		Global.Skin = setting.getSkin();
		
		//convert suppress name to values
		String supStr = setting.getSuppress();
		Global.suppress = 0; 
		if(!StringUtils.isBlank(supStr)){
			String[] supStrs = supStr.split(",");
			for (String supName : supStrs) {
				try {
					SUPPRESS sup = SUPPRESS.valueOf(supName.toUpperCase());
					Global.suppress |= sup.getValue(); 
				} catch (Exception e) {
					//skip invalid suppress
				}
			}
		}

		Global.DetectLocaleFromRequest = setting.isDetectLocaleFromRequest();
		Global.VersionCheck = setting.isVersionCheck();
		if(Global.VersionCheck && !StringUtils.isBlank(setting.getVersionCheckCron())){
			//if version check is true, but cron is blank then keep its default value.
			Global.VersionCheckCron = setting.getVersionCheckCron();
		}
		Global.PurgeDaysOldActivityLog = setting.getPurgeDaysOldActivityLog();
		
        Global.TwitterOauthConsumerKey = setting.getTwitterOauthConsumerKey();
        Global.TwitterOauthConsumerSecret = setting.getTwitterOauthConsumerSecret();
	}

	public static String enable(boolean val) {
		return val?"enabled":"disabled";
	}
	public static boolean isEnabled(String value) {
		return "enabled".equalsIgnoreCase(value) || "enable".equalsIgnoreCase(value);
	}

	public static Locale getDefaultLocale(){
		return new Locale(Global.DefaultLanguage!=null?Global.DefaultLanguage:"en",
				Global.DefaultCountry!=null?Global.DefaultCountry:"AU");
	}
	public String getDefaultCountry() {
		return DefaultCountry;
	}

	/**
	 * @param url
	 */
	public static void resetURLInfo(String url) {
		String host = url;
		if(url.startsWith("http://")){
			SysHostProtocol = "http://";
			host = url.substring("http://".length());
		}else if(url.startsWith("https://")){
			SysHostProtocol = "https://";
			host = url.substring("https://".length());
		}
		
		int paraS = host.indexOf("?");
		if(paraS != -1)
			host = host.substring(0,paraS);
		paraS = host.indexOf("#");
		if(paraS != -1)
			host = host.substring(0,paraS);
		
		int contextS = host.indexOf("/");
		if(contextS != -1){
			SysHostAddress = host.substring(0,contextS);
			SysContextPath = host.substring(contextS+1);  
		}else{
			SysHostAddress = host;
			SysContextPath = "";
		}
	}

	
	public static void setCurrentSuppress(Integer suppr) {
		Global.currentSuppress.set(suppr);
	}
	public static int getCurrentSuppress() {
		return currentSuppress.get() ==null?Global.suppress:currentSuppress.get();
	}


}
