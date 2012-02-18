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
package com.edgenius.core.util;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.Global;

/**
 * @author Dapeng.Ni
 */
public class WebUtil {
	public static final Logger log = LoggerFactory.getLogger(WebUtil.class);
    public static final String USER_AGENET_BROWSER = "com.edgenius.core.user-agent-browser";
    public static final List<Pattern> userAgentPatternList = new ArrayList<Pattern>(); 
    public static boolean agentListInit = false;
	
    public static final String[] AJAX_URL_SUFFIX = new String[]{
    	".rpcs",
    	"pages/upload",
    	"user/portrait.do",
    	"space/logo.do",
    };
    
	public static boolean isAjaxRequest(HttpServletRequest request) {
		
		String uri = request.getRequestURI();
		for(String suffix:AJAX_URL_SUFFIX){
			if(uri.endsWith(suffix))
				return true;
		}
    	return false;
    }
	/**
	 * 
	 * @return web context, which contain leading and end "/", such as "/geniuswiki/"
	 */
	public static String getWebConext(){
		String context = "/";
		if(!StringUtils.isBlank(Global.SysContextPath)){
			if(Global.SysContextPath.startsWith("/"))
				context = Global.SysContextPath;
			else
				context +=  Global.SysContextPath;
			if(!context.endsWith("/"))
				context += "/";
		}
		return context;
	}

	/**
	 * @return A URL with context, such as http://geniuswiki.com/geniuswiki/
	 */
	public static String getHostAppURL(){
		String protocol = Global.SysHostProtocol.trim();
		if(!protocol.endsWith("://"))
			protocol += "://";
		String host = Global.SysHostProtocol + Global.SysHostAddress.trim();
		//can not handle with double //
		if(host.endsWith("/"))
			host = host.substring(0,host.length()-1);
		
		return host + getWebConext();
	}
	/**
	 * @return 
	 */
	public static ServletContext getServletContext() {
		ServletContext context = ServletUtils.getServletContext();
		if(context == null){
			try {
				context = ServletActionContext.getServletContext();
			} catch (Exception e) {
				//STRUTS 2.1.2 throw null point exception
			}
		}
		return context;
	}
	
	public static HttpServletRequest getRequest() {
		//TODO: future, remove STRUTS2 then remove this line as well
		HttpServletRequest request = ServletUtils.getRequest();
		if(request == null){
			try {
				request = ServletActionContext.getRequest();
			} catch (Exception e) {
				//STRUTS2.1.2 throw null point exception
			}
		}
		return request;
	}
	/**
	 * @return
	 */
	public static HttpServletResponse getResponse() {
		//TODO: future, remove STRUTS2 then remove this line as well
		HttpServletResponse response = ServletUtils.getResponse(); 
		if(response == null){
			try {
				response = ServletActionContext.getResponse();
			} catch (Exception e) {
				//STRUTS2.1.2 throw null point exception
			}
		}
		return response;
	}

	/**
	 * This method will use by Core API, so put here rather than WikiUtil
	 * Return url with context and host, such as http://geniuswiki.com/geniuswiki/download?xxxx
	 * 
	 * @param hostUrl - this URL must with web context, such as http://geniuswiki.com/geniuswiki/
	 * @param spaceUname
	 * @param spaceUname
	 * @param fileName
	 * @return
	 */
	public static String getPageRepoFileUrl(String hostUrl, String spaceUname, String fileName, String fileNodeUuid, boolean download) {
		
		try {
			spaceUname = URLEncoder.encode(spaceUname,Constants.UTF8);
			fileName = URLEncoder.encode(fileName,Constants.UTF8);
			fileNodeUuid = URLEncoder.encode(fileNodeUuid,Constants.UTF8);
		} catch (UnsupportedEncodingException e) {
			log.error("Failed on convert download space name.",e);
		}
		hostUrl = StringUtils.trimToEmpty(hostUrl);
		if(!hostUrl.endsWith("/")){
			//add last "/"
			hostUrl += "/";
		}
		
		return new StringBuffer(hostUrl).append("download?space=").append(spaceUname)
			.append("&uuid=").append(fileNodeUuid)
			.append("&file=").append(fileName)
			.append("&download=").append(download).toString();	
	}
	/**
	 * @param agent 
	 * @return
	 */
	public static boolean isPublicSearchEngineRobot(String agent) {
		if(!agentListInit){
			agentListInit = true;
	    	try{
		    	ResourceBundle ua = ResourceBundle.getBundle(USER_AGENET_BROWSER);
		    	Enumeration<String> em = ua.getKeys();
		    	while(em.hasMoreElements()){
		    		String regex = ua.getString(em.nextElement());
		    		try{
		    			userAgentPatternList.add(Pattern.compile(regex));
		    		}catch(Exception e){
		    			log.error("Unable compile user agent pattern: " + regex);
		    		}
		    	}
	    	}catch (Throwable e) {
	    		log.error("Unable load user agent properties, use default instead");
			}
		}
		if(userAgentPatternList.size() > 0){
			// use User-Agent to detect if current request is from browser, search engine robot, web crawler etc.
			for(Pattern pattern : userAgentPatternList){
				//so far, browser is small amount than robot, so for performance reason, use browser agent list
				//See our issue http://bug.edgenius.com/issues/34
				//and SUN Java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337993
				try {
					if(pattern.matcher(agent).matches())
						return false;
				} catch (StackOverflowError e) {
					AuditLogger.error("StackOverflow Error in WebUtil.isPublicSearchEngineRobot. Input[" + agent+"] Pattern [" 
							+ pattern.pattern()+ "]");
				} catch (Throwable e) {
					AuditLogger.error("Unexpected error in WebUtil.isPublicSearchEngineRobot. Input[" + agent+"] Pattern [" 
							+ pattern.pattern()+ "]",e);
				}
			}
		}else{
			//default very rough check
			String user = agent.toLowerCase();
			if(user.indexOf("crawl") != -1 
				|| user.indexOf("spider") != -1
				|| user.indexOf("check") != -1
				|| user.indexOf("heritrix") != -1
				|| user.indexOf("bot") != -1){
				return true;
			}else if(user.indexOf("mozilla") != -1
//					||user.indexOf("") != -1
					||user.indexOf("opera") != -1){
				return false;
			}
		}
		return true;
	}

}
