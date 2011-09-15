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
package com.edgenius.wiki;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.DataRoot;
import com.edgenius.core.Global;
import com.edgenius.core.Installation;
import com.edgenius.core.util.CodecUtil;
import com.edgenius.core.util.FileUtil;
import com.edgenius.core.util.WebUtil;

/**
 * Properties for Shell service:
 * shell.enabled=true
 * shell.url = http://someurl.com
 * connection.timeout=20000 //20s
 * # value from Shell host - see requestInstanceConnection() 
 * shell.key=xxxx  
 * 
 * @author Dapeng.Ni
 */
public class Shell {
	private static final String FILE_DEFAULT = "classpath:geniuswiki/shell.default.properties";
	private static final String FILE = "shell.properties";
	private static final int DEFAULT_TIMEOUT = 20000; //default 20s
	private static final String HEAD_SHELL_KEY = "shellKey";
	
	private static final Logger log = LoggerFactory.getLogger(Shell.class);

	public static boolean enabled;
	//if popup Shell dialog when creating a space to confirm link. 
	public static boolean autoEnabled;
	public static String key;
	public static int timeout = DEFAULT_TIMEOUT;
	
	//This is value in properties, the shell hosting root URL.
	public static String rootUrl;
	
	//This URL doesn't read out from property file, it has a suffix from shell.key against rootURL
	public static String url;
	
	private static final ReadWriteLock filelock = new ReentrantReadWriteLock();
	
	//Process - 
	//1. GW send request for instance shell key, instanceID and GW host address are parameters
	//2. Shell create shell key and return a MD5 validation string - at the moment, the GW thread is still block.
	//3. Shell requests by GW host address with shell key - hope it is correct one.
	//4. GW receives the shell key, valid with MD5, if success save it and break GW waiting thread.
	// If GW doesn't wait any request from shell or request without any valid shell key. Then, it also break waiting thread in 20s. But return false; 
	private static String keyValidator;
	private static final Lock lock = new ReentrantLock();
	private static final Condition keyValidCondition  = lock.newCondition();
	static{
		load();
	}
	
	/**
	 * @return
	 * @throws IOException
	 */
	public static void load(){
		Shell.enabled = false;
		
		log.info("Loading shell.properties ...");
		String root = DataRoot.getDataRoot();
		if(!FileUtil.exist(root+Shell.FILE)){
			try {
				//copy from defaultExternalResource
				FileOutputStream dest = FileUtil.getFileOutputStream(root+Shell.FILE);
				IOUtils.copy(FileUtil.getFileInputStream(FILE_DEFAULT), dest);
				IOUtils.closeQuietly(dest);
			} catch (IOException e) {
				log.error("Copy default shell.properties file failed",e);
			}
		}
		try {
			filelock.readLock().lock();
			Properties props = FileUtil.loadProperties(root+Shell.FILE);
			Shell.enabled = BooleanUtils.toBoolean(props.getProperty("shell.enabled"));
			Shell.autoEnabled = BooleanUtils.toBoolean(props.getProperty("shell.auto.enable.at.space.creating"));
			Shell.rootUrl = StringUtils.trimToEmpty(props.getProperty("shell.url"));
			Shell.timeout = NumberUtils.toInt(props.getProperty("connection.timeout"), DEFAULT_TIMEOUT); //default 20s
			Shell.key = StringUtils.trimToEmpty(props.getProperty("shell.key"));
			//fix to add end slash
			if(!Shell.rootUrl.endsWith("/"))
				Shell.rootUrl +="/";
		
			updateUrl();
		} catch (IOException e) {
			log.error("Load shell.properties failed", e);
		} finally{
			filelock.readLock().unlock();
		}
		
	}
	public static void save() {
		try{
			filelock.writeLock().lock();
			//write this key to shell.properties
			String root = DataRoot.getDataRoot();
			Properties props = FileUtil.loadProperties(root+Shell.FILE);
			props.setProperty("shell.key", StringUtils.trimToEmpty(Shell.key));
			props.setProperty("shell.enabled", Boolean.toString(Shell.enabled));
			
			//fix to add end slash
			if(!Shell.rootUrl.endsWith("/"))
				Shell.rootUrl +="/";
			props.setProperty("shell.url", StringUtils.trimToEmpty(Shell.rootUrl));
			props.setProperty("connection.timeout", String.valueOf(Shell.timeout));
			
			updateUrl();
			
			FileOutputStream os = FileUtil.getFileOutputStream(root+Shell.FILE);
			props.store(os, "Shell is update by system.");
			
		}catch(Exception e){
			log.error("Unable to save shell.properties", e);
		}finally{
			filelock.writeLock().unlock();
		}

	}

	/**
	 * This method is try to get a MD5 valid string from keyValidator and valid key, if success, save it 
	 * @param key
	 * @return
	 */
	public static boolean updateShellKey(String key){
		try {
			if(!StringUtils.isEmpty(key) && keyValidator != null && keyValidator.equals(CodecUtil.encodePassword(key, "MD5"))){
				log.info("Instance shell key accquire successed");
				Shell.key = key;

				updateUrl();
				
				save();
				
				//break requestInstanceShellKey() waiting.
				lock.lock();
				try {
					keyValidator = null;
					keyValidCondition.signalAll();
				} finally {
					lock.unlock();
				}
				return true;
			}
			log.error("Valid shell key failed by validtor {}", keyValidator);
		} catch (Exception e) {
			log.error("Unable to save key", e);
		}
		
		return false;
	}
	public static String getThemeBaseURL() {
		
		//root URL with out shellKey!
		return new StringBuilder(Shell.rootUrl).append("theme?instance=").append(Shell.key).toString();
	}

	public static String getPageShellURL(String spaceUnamme, String pageTitle){
		StringBuilder sb = new StringBuilder(Shell.url);
		try {
			sb.append("page/").append(URLEncoder.encode(spaceUnamme, Constants.UTF8));
			if(pageTitle != null){
				sb.append("/").append(URLEncoder.encode(pageTitle, Constants.UTF8));
			}
		} catch (Exception e) {
			log.error("Failed to get shell URL ", e);
		}
		
		return sb.toString();
	}
	
	public static String requestSpaceThemeName(String spaceUname) {
		try {
			log.info("Request shell theme for space {}", spaceUname);
			
			//reset last keyValidator value  - will use new one.
			HttpURLConnection conn = (HttpURLConnection) new URL(getThemeRequestURL(spaceUname)).openConnection();
			conn.setConnectTimeout(timeout);
			InputStream is = conn.getInputStream();
			ByteArrayOutputStream writer = new ByteArrayOutputStream();
			int len;
			byte[] bytes = new byte[200];
			while((len = is.read(bytes)) != -1){
				writer.write(bytes,0, len);
			}
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
				return new String(writer.toByteArray());
			}
		} catch (IOException e) {
			log.error("Unable to connect shell for theme name request", e);
		} catch(Throwable e){
			log.error("Notify shell failure", e);
		}
		
		return null;
	}
	
	/**
	 * Please note, this method doesn't reset current Shell.key. If this request failed, the original Shell.key value is still remained.
	 * @return
	 */
	public static boolean requestInstanceShellKey(){
		try {
			log.info("Request shell key for current instance");
			
			//reset last keyValidator value  - will use new one.
			keyValidator = null;
			HttpURLConnection conn = (HttpURLConnection) new URL(getShellURL(Installation.INSTANCE_ID, null,null,false,null,false)).openConnection();
			//as instance key request is important, so we extends its timeout as original 2 times.
			conn.setConnectTimeout(timeout*2);
			InputStream is = conn.getInputStream();
			ByteArrayOutputStream writer = new ByteArrayOutputStream();
			int len;
			byte[] bytes = new byte[200];
			while((len = is.read(bytes)) != -1){
				writer.write(bytes,0, len);
			}
			String response = new String(writer.toByteArray());
			
			//see GShell project, ShellServlet response for instance
			//Here return is MD5 of key value....
			if(response.startsWith("KEY-VALIDATOR:")){
			     lock.lock();
			     try {
			    	 keyValidator = response.substring(14).trim();
			         if(!keyValidCondition.await(timeout*2, TimeUnit.MILLISECONDS)){
			        	 log.error("Unable to get valid Shell request for key. Request Instance shell key failed.");
			        	 keyValidator = null;
			        	 return false;
			         }
			         
			         //here, suppose shell key is already fill in.
			         return !StringUtils.isEmpty(Shell.key);
			     } finally {
			       lock.unlock();
			     }
			}
			//suspect current thread to wait until key valid
		} catch (IOException e) {
			log.error("Unable to connect shell for notification", e);
		} catch(Throwable e){
			log.error("Notify shell failure", e);
		}
		
		return false;
	}
	public static boolean notifySpaceCreate(String spaceUname){
		if(!enabled || !Global.restServiceEnabled) return false;
		
		return notifyShell(getShellURL(null,spaceUname,null,false,null,false));
		
	}
	
	public static boolean notifySpaceRemove(String spaceUname) {
		if(!enabled || !Global.restServiceEnabled) return false;
		
		return notifyShell(getShellURL(null, spaceUname,null,false,null, true));
	}
	
	public static boolean notifyPageCreate(String spaceUname, String pageUuid, boolean withAttachments){
		if(!enabled || !Global.restServiceEnabled) return false;
		
		return notifyShell(getShellURL(null, spaceUname,pageUuid, withAttachments, null, false));
	}
	
	public static boolean notifyPageRemoved(String spaceUname, String pageUuid) {
		if(!enabled || !Global.restServiceEnabled) return false;
		
		return notifyShell(getShellURL(null, spaceUname, pageUuid,false, null, true));		
	}

	public static boolean notifyPageAttachments(String spaceUname, String pageUuid) {
		if(!enabled || !Global.restServiceEnabled) return false;
		
		return notifyShell(getShellURL(null, spaceUname, pageUuid,false,"attachments", false));		
	}
	public static boolean notifyPageTags(String spaceUname, String pageUuid) {
		if(!enabled || !Global.restServiceEnabled) return false;
		
		return notifyShell(getShellURL(null, spaceUname,pageUuid,false,"tags", false));		
	}

	//********************************************************************
	//               Private methods
	//********************************************************************

	private static void updateUrl() {
		//Shell URL is shell host plus shell key - which is integer value that identify current instance in Shell side.
		//we use rootUrl rather than URL as this method may be call multiple times, to avoid append multiple Shell.key to original URL;
		
		if(!StringUtils.isEmpty(Shell.key)){
			Shell.url = Shell.rootUrl + Shell.key + "/";
		}else{
			Shell.url = null;
		}
		
	}

	/**
	 * @param url
	 * @return
	 */
	private static boolean notifyShell(String url) {
		if(StringUtils.isEmpty(Shell.key)) {
			//I suppose all notify methods will execute in backend thread, i.e.,  MQ consumer. This won't use new thread to avoid thread block.  
			if(!requestInstanceShellKey()){
				log.error("Unable to locate Shell key value from shell hosting {}", url);
				return false;
			}
		}
		
		try {
			log.info("Notify shell {}", url);
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestProperty(HEAD_SHELL_KEY, Shell.key);
			
			conn.setConnectTimeout(timeout);
			return (conn.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (IOException e) {
			log.error("Unable to connect shell for notification", e);
		} catch(Throwable e){
			log.error("Notify shell failure", e);
		}
		
		return false;
	}
	/**
	 * If spaceUname is not null, then return space notification URL.
	 * If pageUuid is not null,  then return page notification URL.
	 * @param spaceUname
	 * @param pageUuid
	 * @return
	 */

	private static String getShellURL(String instanceID, String spaceUname, String pageUuid, boolean withAttachments, String update, boolean removed) {
		try {
			StringBuilder sb = new StringBuilder(Shell.rootUrl);
			
			if(pageUuid != null){
				sb.append("shell?puuid=").append(URLEncoder.encode(pageUuid, Constants.UTF8));
				//if pageUuid is not null, spaceUname also must not null
				sb.append("&uname=").append(URLEncoder.encode(spaceUname, Constants.UTF8));
			}else if(spaceUname != null)
				sb.append("shell?uname=").append(URLEncoder.encode(spaceUname, Constants.UTF8));
			else if(instanceID != null){
				sb.append("shell?instance=").append(URLEncoder.encode(instanceID, Constants.UTF8))
					.append("&addr=").append(URLEncoder.encode(WebUtil.getHostAppURL(), Constants.UTF8));
			}
			
			if(removed)
				sb.append("&remove=true");
			
			if(withAttachments)
				sb.append("&withattachments=true");
			
			if(update != null)
				sb.append("&update=").append(update);
			
			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			log.error("Unable to get user URL " + spaceUname, e);
		} catch(Throwable e){
			log.error("Get shell URL failure", e);
		}
		return null;
	}

	private static String getThemeRequestURL(String spaceUname) {
		if(StringUtils.isEmpty(Shell.key)) {
			//I suppose all notify methods will execute in backend thread, i.e.,  MQ consumer. This won't use new thread to avoid thread block.  
			if(!requestInstanceShellKey()){
				log.error("Unable to locate Shell key value from shell hosting {}", url);
				return null;
			}
		}
		
		try {
			StringBuilder sb = new StringBuilder(Shell.rootUrl);
			sb.append("theme?action=query&instance=").append(Shell.key).append("&space="+URLEncoder.encode(spaceUname, Constants.UTF8));

			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			log.error("Unable to get user URL " + spaceUname, e);
		} catch(Throwable e){
			log.error("Get shell URL failure", e);
		}
		return null;
	}
	

}
