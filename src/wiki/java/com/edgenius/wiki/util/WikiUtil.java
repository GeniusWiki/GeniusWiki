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
package com.edgenius.wiki.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.captcha.CaptchaServiceProxy;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.edgenius.core.Constants;
import com.edgenius.core.UserSetting;
import com.edgenius.core.model.SensitiveTouchedInfo;
import com.edgenius.core.model.TouchedInfo;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.ITicket;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.repository.RepositoryTiemoutExcetpion;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.PageTheme;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.CaptchaCodeModel;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.LinkUtil;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.UserUtil;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageTag;
import com.edgenius.wiki.model.SpaceTag;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.render.filter.UserFilter;
import com.edgenius.wiki.service.PageException;
import com.edgenius.wiki.service.ThemeService;

/**
 * @author Dapeng.Ni
 */
public class WikiUtil {
	private static final Logger log = LoggerFactory.getLogger(WikiUtil.class);

	//Page or Space tag string split by comma.
	private static final Pattern tagPattern = Pattern.compile("[\\s,]*([^\\s,]+)[\\s,]*");


	//********************************************************************
	//                methods
	//********************************************************************
	
	/**
	 * @param content
	 * @return
	 */
	public static boolean hasBlogRender(Page page, ThemeService themeService) {
		
		if(RenderUtil.hasBlogMacro(page.getContent().getContent()))
			return true;
		
		String body = themeService.getPageTheme(page,isHomepage(page)?PageTheme.SCOPE_HOME:PageTheme.SCOPE_DEFAULT).getCurrentPageTheme().getBodyMarkup();
		body = body == null?"":body;
		
		return RenderUtil.hasBlogMacro(body);
	}

	public static String getUserName() {
		HttpServletRequest request = WebUtil.getRequest();
		if(request != null)
			return request.getRemoteUser();
		else{
			User user = ProxyLoginUtil.getRequester();
			if(user != null){
				return user.getUsername();
			}
			return null;
		}
		
	}
	
	/**
	 * @param model
	 * @return 
	 */
	public static boolean captchaValid(CaptchaServiceProxy captchaService, CaptchaCodeModel model) {
		if(model != null && model.reqireCaptcha){
			HttpServletRequest request = WebUtil.getRequest();
			HttpServletResponse response = WebUtil.getResponse();
			String id = request.getSession().getId();
			boolean valid =captchaService.validateReponseForId(id, model.captchaCode);
			if(!valid){
				try {
					//here does not put webcontext - it is fine as GwtSpringController only do endWith() check.
					response.sendRedirect(WikiConstants.URL_CAPTCHA_VERIFIED_ERROR );
				} catch (IOException e) {
					log.error("Redir failed:" + WikiConstants.URL_CAPTCHA_VERIFIED_ERROR,e);
				}
				return false;
			}
		}
		return true;
	}
	/**
	 * @return
	 */
	public static User getUser() {
		ServletContext servletCtx = WebUtil.getServletContext();
		User user = null;
		if(servletCtx != null){
			ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletCtx);
			UserReadingService userReadingService = (UserReadingService) ctx.getBean(UserReadingService.SERVICE_NAME);
			user = userReadingService.getUserByName(getUserName());
		}
		if(user == null){
			user = ProxyLoginUtil.getRequester();
		}
		return user;
	}

	/**
	 * Some service is called is not inside HttpRequest, such as Indexing, RSS service. They need use this method to get User information.
	 * @param userReadingService
	 * @return
	 */
	public static User getUser(UserReadingService userReadingService) {
		
		return userReadingService.getUserByName(getUserName());
	}
	
	
	/**
	 * @param userService
	 * @return
	 */
	public static User getAnonymous() {

		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(WebUtil.getServletContext());
		UserReadingService userReadingService = (UserReadingService) ctx.getBean(UserReadingService.SERVICE_NAME);
		return userReadingService.getUserByName(null);
	}


	/**
	 * @param userService
	 * @return
	 */
	public static User getAnonymous(UserReadingService userReadingService) {
		return userReadingService.getUserByName(null);
	}
	/**
	 * @param touchedObject
	 */
	public static void setTouchedInfo(UserReadingService userReadingService, TouchedInfo touchedObject) {
		User author = userReadingService.getUserByName(getUserName());
		if(author == null || author.isAnonymous())
			author = null;
		
		Date now = new Date();
		if(touchedObject.getCreatedDate() == null){
			touchedObject.setCreatedDate(now);
			touchedObject.setCreator(author);
		}
		touchedObject.setModifiedDate(now);
		touchedObject.setModifier(author);
		
		if(touchedObject instanceof SensitiveTouchedInfo){
			((SensitiveTouchedInfo)touchedObject).setTouchedDate(now);
		}
	}

	/**
	 * @param pageTitle, concat by "+" string
	 * @return, replace  "+" with space
	 */
	public static String getPageTitle(String pageUname) {
		if(pageUname == null)
			return "";
		return pageUname.replaceAll("\\+", " ");
	}
	/**
	 * Return an unique UUID and initial it in repository. 
	 * @param spacename
	 * @param username
	 * @param password
	 * @param repositoryService
	 * @return
	 * @throws PageException
	 */
	public static String createPageUuid(String spacename, String username, String password, RepositoryService repositoryService) throws PageException{
		//DON'T user repository created UUID for future export/import function:
		//always keep page UUID unchanged whatever import to any new database
		//UUID.randomUUID().toString(); - use smaller string to replace 32 length UUID, I test possible duplicated case
		//if it is case sensitive, it is almost impossible duplicated even 50 million. But I have to make to lowerCase 
		// 1 million no duplicate, but 50 million will have 9 duplicate -- this make need make duplciate try-catch check?
		String uuid = RandomStringUtils.randomAlphanumeric(WikiConstants.UUID_KEY_SIZE).toLowerCase();
		try {
			ITicket ticket = repositoryService.login(spacename,username, password);
			repositoryService.createIdentifier(ticket,RepositoryService.TYPE_ATTACHMENT, uuid);
		} catch (RepositoryException e) {
			log.error("Create page UUID failed request from repository :" , e);
			throw new PageException(e);
		} catch (RepositoryTiemoutExcetpion e) {
			log.error("Create page UUID failed request from repository :" , e);
			throw new PageException(e);
		}
		
		return uuid;
	}


	/**
	 * @param tagString split by , or space
	 * @return 
	 */
	public static List<PageTag> parsePageTagString(String tagString) {
		List<PageTag> tags = new ArrayList<PageTag>();
		if(tagString == null)
			return tags;
		
//		String[] tagStrs = tagString.split("[\\s,]+");
//		for (String tagStr : tagStrs) {
//			Tag tag = new Tag();
//			tag.setName(tagStr);
//			tags.add(tag);
//		}

		//See our issue http://bug.edgenius.com/issues/34
		//and SUN Java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337993
		try {
			Matcher c = tagPattern.matcher(tagString);
			while(c.find()){
				//remove duplicated tag
				String name = c.group(1);
				boolean dup = false;
				for (PageTag pageT : tags) {
					if(pageT.getName().equalsIgnoreCase(name)){
						dup = true;
						break;
					}
				}
				if(dup) continue;
				
				//new tag
				PageTag tag = new PageTag();
				tag.setName(name);
				tags.add(tag);
			}		
		} catch (StackOverflowError e) {
			AuditLogger.error("StackOverflow Error in WikiUtil.parsePageTagString. Input[" 
					+ tagString+"]  Pattern [" + tagPattern.pattern()+ "]");
		} catch (Throwable e) {
			AuditLogger.error("Unexpected error in WikiUtil.parsePageTagString. Input[" 
					+ tagString+"]  Pattern [" + tagPattern.pattern()+ "]",e);
		}		
		return tags;
	}
	public static List<SpaceTag> parseSpaceTagString(String tagString) {
		List<SpaceTag> tags = new ArrayList<SpaceTag>();
		if(tagString == null)
			return tags;
		
		try {
			Matcher c = tagPattern.matcher(tagString);
			while(c.find()){
				//remove duplicated tag
				String name = c.group(1);
				boolean dup = false;
				for (SpaceTag pageT : tags) {
					if(pageT.getName().equalsIgnoreCase(name)){
						dup = true;
						break;
					}
				}
				if(dup) continue;
	
				SpaceTag tag = new SpaceTag();
				tag.setName(name);
				tags.add(tag);
			}		
		} catch (StackOverflowError e) {
			AuditLogger.error("StackOverflow Error in WikiUtil.parseSpaceTagString. Input[" 
					+ tagString+"]  Pattern [" + tagPattern.pattern()+ "]");
		} catch (Throwable e) {
			AuditLogger.error("Unexpected error in WikiUtil.parseSpaceTagString. Input[" 
					+ tagString+"]  Pattern [" + tagPattern.pattern()+ "]",e);
		}	
		return tags;
	}
	//********************************************************************
	//               URL methods
	//********************************************************************
	/**
	 * 
	 * Return &lt;a&gt; href link HTML tag, i.e.,
	 * <pre> 
	 * <a href="/context/page#token">pageTitle</a>
	 * </pre> 
	 */
	public static String getPageRelativeTokenLink(String spaceUname, String pageTitle, String anchor) {
		if(StringUtils.isBlank(pageTitle)){
			return getSpaceRelativeTokenLink(spaceUname, null);
		}else{
			return new StringBuffer("<a href=\"").append(getPageRelativeTokenURL(spaceUname, pageTitle, anchor))
			.append("\" title=\"").append(pageTitle).append("\">").append(pageTitle).append("</a>").toString();
		}
	}
	public static String getSpaceRelativeTokenLink(String spaceUname, String name) {
		//space link
		return new StringBuffer("<a href=\"").append(getPageRelativeTokenURL(spaceUname, null, null))
				.append("\" title=\"").append(spaceUname)
				.append("\">").append(StringUtils.isEmpty(name)?spaceUname:name).append("</a>").toString();
	}
	
	/**
	 * return format "/context/page#token"
	 * This URL can used in server side code to redirect to page. 
	 * @return
	 */
	public static String getPageRelativeTokenURL(String spaceUname, String pageTitle, String anchor) {
		StringBuffer redir = new StringBuffer(WebUtil.getWebConext());
		if(!StringUtils.isBlank(spaceUname)){
			//if URL is page.do?s=spaceUname&p=pageTitle, convert this URL
			//to ajax token URL /page#/spaceUname/pageTitle#anchor
			redir.append(SharedConstants.URL_PAGE).append("#");
			try {
				//page#/xxx - xxx must do URLEncode otherwise it will broken URL even its value in anchor part
				redir.append(URLEncoder.encode(GwtUtils.getSpacePageToken(spaceUname,pageTitle),Constants.UTF8));
				if(!StringUtils.isBlank(anchor)){
					redir.append(URLEncoder.encode("#"+EscapeUtil.escapeToken(anchor),Constants.UTF8));
				}
			} catch (UnsupportedEncodingException e1) {
				log.error("URL encode error", e1);
			}
		}
		return redir.toString();
	}
	/**
	 * !!! This method is used in JSP page as function method, please be carefully if any refactoring !!!
	 * @param readonly
	 * @return
	 */
	public static String getRootURL(boolean readonly) {
		String hostURL = WebUtil.getHostAppURL();
		return hostURL+(readonly?SharedConstants.URL_VIEW:SharedConstants.URL_PAGE);
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// All below non-readonly URL will convert to /page/#spaceUname/pageTitle format in PageAction.class
	/**
	 * !!! This method is used in JSP page as function method, please be carefully if any refactoring !!! 
	 *  <li>/page(view)/spaceUname - if not invalid character in spaceUname</li>
	 *  <li>/s(vs)/spaceUid - if invalid character in spaceUname and spaceUid is not blank</li>
	 * @param page
	 * @return
	 */
	public static String getSpaceRedirFullURL(boolean readonly, Integer spaceUid, String spaceUname) {
		String hostURL = WebUtil.getHostAppURL();
		if(StringUtils.isBlank(spaceUname))
			//TODO: this is problem for read-only model
			return hostURL;
		
		if(GwtUtils.isSupportInURL(spaceUname) || spaceUid == null){
			try {
				return  hostURL+(readonly?SharedConstants.URL_VIEW:SharedConstants.URL_PAGE)+"/" +URLEncoder.encode(spaceUname, Constants.UTF8);
			} catch (UnsupportedEncodingException e) {
				log.error("Failed to encode URL:"+spaceUname,e);
			}
			return hostURL;
		}else{
			return hostURL+(readonly?SharedConstants.URL_TINY_READONLY_SPACE:SharedConstants.URL_TINY_SPACE)+"/" +spaceUid;
		}
	}
	/**
	 * !!! This method is used in JSP page as function method, please be carefully if any refactoring !!! 
	 * 
	 * @return
	 */
	public static String getRedirFullURL(boolean readonly, String spaceUname, String pageTitle, String pageUuid) {
		if(readonly)
			return getFullURL(WebUtil.getHostAppURL(), SharedConstants.URL_VIEW, spaceUname, pageTitle, pageUuid, null);
		else
			return getFullURL(WebUtil.getHostAppURL(), SharedConstants.URL_PAGE, spaceUname, pageTitle, pageUuid, null);
	}
	public static String getPageRedirFullURL(String hostURL, String spaceUname, String pageTitle, String pageUuid, String anchor) {
		return getFullURL(hostURL, SharedConstants.URL_PAGE, spaceUname, pageTitle, pageUuid, anchor);
	}
	public static String getPageRedirFullURL(String spaceUname, String pageTitle, String pageUuid) {
		return getFullURL(WebUtil.getHostAppURL(), SharedConstants.URL_PAGE, spaceUname, pageTitle, pageUuid, null);
	}
	/**
	 * This page return URL according to given parameters and could produce one of below format URL:
	 *  <li>/context/page(view)/spaceUname/pageTitle/anchor  - This is normal case if all spaceUname, pageTitle and anchor don't have invalid characters</li>
	 *  <li>/context/p(v)/pageUuid/anchor  - if any invalid characters and pageUuid is not blank</li>
	 * @return
	 */
	private static String getFullURL(String hostURL, String urlStartWith, String spaceUname, String pageTitle, String pageUuid, String anchor) {
		
		if(StringUtils.isBlank(spaceUname))
			return hostURL;
		

		try{
			if((GwtUtils.isSupportInURL(pageTitle) && GwtUtils.isSupportInURL(spaceUname))
					//if page not exist, pageUuid is null, it becomes impossible to give tinyURL format. Then always give meaning URL 
				||StringUtils.isBlank(pageUuid)){
				if(StringUtils.isBlank(pageTitle)){ 
					//home page
					return  hostURL+urlStartWith+"/" +URLEncoder.encode(spaceUname,Constants.UTF8);
				}else{
					//normal url p/spaceUname/pageTitle
					return  hostURL+urlStartWith+"/"+URLEncoder.encode(spaceUname,Constants.UTF8)
							+"/"+ URLEncoder.encode(pageTitle,Constants.UTF8) 
							+ (StringUtils.isBlank(anchor)?"":("/"+anchor));
				}
			}else{
				// tinyURL: /v/pageUuid or p/pageUuid 
				return  hostURL+(urlStartWith.equals(SharedConstants.URL_VIEW)?SharedConstants.URL_TINY_VIEW:SharedConstants.URL_TINY_PAGE)
						+"/"+ pageUuid + (StringUtils.isBlank(anchor)?"":("/"+anchor));
			}
		}catch(UnsupportedEncodingException e){
			log.error("Failed to encode URL",e);
		}
		
		return hostURL;
	}

	/**
	 * @param user
	 * @return
	 */
	public static boolean isUsingRichEditor(User user) {
		UserSetting setting = user.getSetting();
		
		return setting.isUsingRichEditor();
	}

	/**
	 * GWT use anchor to do ajax call. such as #token.
	 * Basically, this is same with javascript location.href=token. location.href will do encoding to token. 
	 * But this encoding has some different with URL encoding. This method will encode string to location.href encoding  
	 * 
	 * Normally,Different list<br>
	 * Space: URL=+ ; Anchor=%20
	 * 
	 * But system use special entity replace to replace some keywords, include +.
	 * 
	 * @param token
	 * @return
	 */
	public static String encodeURL(String token) {
		try {
			//replace token keyword #,@,>,+ etc to entity
			token = EscapeUtil.escapeToken(token);
			token = URLEncoder.encode(token, Constants.UTF8);
		} catch (UnsupportedEncodingException e) {
			WebUtil.log.error("Encode token failed.",e);
		}
		return token;
	}

	/**
	 * Get unique key which no newline inside.
	 * @param htmlText
	 * @return
	 */
	public static String findUniqueKey(String htmlText) {
		return findUniqueKey(htmlText,false, false);
	}
	/**
	 * Get an unique string from given html. It means indexOf(htmlText) will always return -1. 
	 * 
	 * @param htmlText
	 * @param multipleLines -- put newline(\n) into returned key.
	 * @param newlineAtEnd -- only multipleLines is true, this flag is useful. If true, it puts newline end of key, 
	 * 						otherwise, put newline as first character of key
	 * @return
	 */
	public static String findUniqueKey(String htmlText, boolean multipleLines, boolean newlineAtEnd) {
		//please note, if multipleLines flag is true, the unique part should be the part which does not include newline.
		//this scenario: some multipleLines key may be trimmed during markup render process. this means, so we may try to 
		//use unique part rather than whole key part as parameter in String.index(). See MarkupRenderEngineImpl.processObjects().
		String key, uniquePart;
		do  {
			key = RandomStringUtils.randomAlphanumeric(WikiConstants.UUID_KEY_SIZE);
			uniquePart = key;
			if(multipleLines){
				char[] bk = key.toCharArray();
				if(newlineAtEnd){
					//replace last character, this is requirement of NewlineFilter, which will detect \n is belong to unique key or not
					bk[bk.length-1] = '\n';
					uniquePart = new String(bk,0,bk.length-1);
				}else{
					bk[0] = '\n';
					uniquePart = new String(bk,1,bk.length-1);
				}
				key = new String(bk);
			}
		}while(htmlText != null && htmlText.indexOf(uniquePart) != -1);

		return key;
	}
	

	/**
	 * @param page
	 * @return
	 */
	public static boolean isHomepage(AbstractPage page) {
		return page.equals(page.getSpace().getHomepage());
	}

	/**
	 * @return System administrator notification web page URL
	 */
	public static String getSystemAdminTokenLink() {
		return new StringBuffer(WebUtil.getHostAppURL()).append(SharedConstants.URL_PAGE).append("#")
		.append(GwtUtils.getCPageToken(SharedConstants.CPAGE_SYSADMIN_NOTIFY)).toString();
	}

	/**
	 * @param page
	 * @return
	 */
	public static String getAdsenseHTML(String spaceUname, Page page) {
		String viewURL = getFullURL(WebUtil.getHostAppURL(), SharedConstants.URL_VIEW, spaceUname, page.getTitle(), page.getPageUuid(), null);
		return new StringBuffer("<iframe id=\"googlead\" name=\"googlead\" src=\"")
			.append(viewURL)
			.append("\"  width=\"100%\" height=\"92\" scrolling=\"no\" frameborder=\"0\" marginwidth=\"0\" marginheight=\"0\"></iframe>").toString();
	}

	public static LinkModel createUserLinkModel(User user) {
		LinkModel userPop = new LinkModel();
		userPop.setType(LinkModel.LINK_TO_CLIENT_CLICK_LINK);
		userPop.setAid(UserFilter.class.getName());
		userPop.setLink(LinkUtil.createCLinkToken(SharedConstants.JS_USER_POP, user.getUsername(),user.getFullname(),UserUtil.getPortraitUrl(user.getPortrait())));
		userPop.setAnchor(GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_USER_PROFILE), user.getUsername()));
		userPop.setView(EscapeUtil.escapeHTML(user.getFullname()));
		//In nativer render, such as RSS feed, only display user full name , rather than a link.
		userPop.setLinkTagStr(EscapeUtil.escapeHTML(user.getFullname()));
		return userPop;
	}

	
	
}
