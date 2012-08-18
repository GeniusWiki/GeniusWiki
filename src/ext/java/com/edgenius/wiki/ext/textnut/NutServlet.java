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
package com.edgenius.wiki.ext.textnut;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.edgenius.core.Constants;
import com.edgenius.core.Global;
import com.edgenius.core.UserSetting;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.repository.RepositoryQuotaException;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.repository.RepositoryTiemoutExcetpion;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.CodecUtil;
import com.edgenius.core.webapp.BaseServlet;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Draft;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageContent;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.ActivityLogService;
import com.edgenius.wiki.service.DuplicatedPageException;
import com.edgenius.wiki.service.PageException;
import com.edgenius.wiki.service.PageSaveTiemoutExcetpion;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.VersionConflictException;
import com.edgenius.wiki.util.WikiUtil;
import com.edgenius.wiki.widget.SpaceWidget;

/**
 * Cooperate with TextNut - Just use Servlet to avoid blunt system.
 * 
 * @author Dapeng.Ni
 */
public class NutServlet extends BaseServlet{
	private static final long serialVersionUID = 2706976031168522858L;
	private static final Logger log = LoggerFactory.getLogger(NutServlet.class);
	
	private static final String ACTION_DETECT_USER = "du";
	private static final String ACTION_DETECT_SPACE = "ds";
	private static final String ACTION_CREATE_SPACE = "cs";
	private static final String ACTION_SAVE_OR_UPDATE_PAGE = "cp"; //default action
	private static final String ACTION_REMOVE_PAGE = "rp";
	
	private static NutParser nutParser = new NutParser();
	private UserReadingService  userReadingService;
	private SecurityService securityService;
	private SpaceService spaceService;
	private SettingService settingService;
	private ActivityLogService activityLog;
	private RenderService renderService;
	private PageService pageService;
	
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
	
		String statusCode = String.valueOf(NutCode.UNKNOWN_ERROR);

		//detect user; detect space; create space; upload page; delete page;
		String action = request.getParameter("action");
		log.info("Nut get request for action {}", action);
		
		if(StringUtils.isEmpty(action) || ACTION_SAVE_OR_UPDATE_PAGE.equalsIgnoreCase(action)){
			//put it as first "if" as most requests should be saveUpdatePage - tiny performance consideration.
			statusCode = saveOrUpdatePage(request, response);
		}else if(ACTION_DETECT_USER.equalsIgnoreCase(action)){
			statusCode =  String.valueOf(detectUser(request, response));
		}else if(ACTION_DETECT_SPACE.equalsIgnoreCase(action)){
			statusCode =  String.valueOf(detectSpace(request, response));
		}else if(ACTION_CREATE_SPACE.equalsIgnoreCase(action)){
			statusCode =  String.valueOf(createSpace(request, response));
		}else if(ACTION_REMOVE_PAGE.equalsIgnoreCase(action)){
			statusCode =  String.valueOf(removePage(request, response));
		}else{
			statusCode = String.valueOf(NutCode.UNKNOWN_ACTION);
		}
		
		response.getOutputStream().write(statusCode.getBytes(Constants.UTF8));

	}
	//********************************************************************
	//               Action methods
	//********************************************************************
	private int detectUser(HttpServletRequest request, HttpServletResponse response) {
		if(!doBasicAuthentication(request))
			return NutCode.AUTHENTICATION_ERROR;
		
		return NutCode.AUTHENTICATION_SUCCESS;
	}
	private int detectSpace(HttpServletRequest request, HttpServletResponse response) {
		if(!doBasicAuthentication(request))
			return NutCode.AUTHENTICATION_ERROR;
		
		String spaceUname = request.getParameter("s");
		if(getSpaceService().getSpaceByUname(spaceUname) == null)
			return NutCode.SPACE_NOT_FOUND;
		
		//for nut user, he must have write permission
		boolean noPerm = true;
		
		Space space = getSpaceService().getSpaceByUname(spaceUname);
		getSecurityService().fillSpaceWikiOperations(WikiUtil.getUser(), space);
		for (WikiOPERATIONS oper : space.getWikiOperations()) {
			if(WikiOPERATIONS.SPACE_PAGE_WRITE.equals(oper)){
				noPerm = false;
				break;
			}
		}
		
		if(noPerm)
			return NutCode.SPACE_WRITE_PERMISSION_DENIED;
		
		return NutCode.SPACE_FOUND;
	}

	private int createSpace(HttpServletRequest request, HttpServletResponse response) {
		//!!! Some code duplicated with SpaceController.createSpace() and WsDashboardServiceImpl.attachSpaceToUsers()
		//and  WsSpaceServiceImpl.createSpace(), if any bugs fix here, please fix there also!!!
		
		if(!doBasicAuthentication(request))
			return NutCode.AUTHENTICATION_ERROR;
		
		String spaceUname = request.getParameter("s");
		if(StringUtils.isBlank(spaceUname)){
			return NutCode.SPACE_CREATED_FAILED;
		}
		
		if(getSpaceService().getSpaceByUname(spaceUname) != null){
			return NutCode.SPACE_DUPLICATED_ERROR;
		}
		//as we only have spaceKey input in TextNut and it will be used as both title and unixName.
		//if this spaceUname has duplicated title, we just create a new title for it
		String title = spaceUname;
		int idx = 1;
		while(getSpaceService().getSpaceByTitle(title) != null){
			title = spaceUname + " "+ idx; //append some number to avoid duplicated title
			idx++;
		}
		
		Space space = new Space();
		space.setUnixName(spaceUname);
		space.setName(title);
		space.setDescription(spaceUname);
		WikiUtil.setTouchedInfo(getUserReadingService(), space);
		try {
			Page homepage = getSpaceService().createSpace(space);
			if(homepage != null){
				getSpaceService().saveHomepage(space,homepage);
				getSettingService().saveOrUpdateSpaceSetting(space, space.getSetting());
			}
			log.info("Space {} created.", spaceUname);
			getActivityLog().logSpaceCreated(space);
		} catch (Exception e) {
			log.error("Create space failed with errors during repository worksapce creating :" ,e);
			return NutCode.SPACE_CREATED_FAILED;
		} 
		
		//append this space to Dashboard
		try{
			User user = WikiUtil.getUser();
			UserSetting setting = user.getSetting();
			setting.addWidgetToHomelayout(SpaceWidget.class.getName(), spaceUname, getSettingService().getInstanceSetting().getHomeLayout());
			
			getSettingService().saveOrUpdateUserSetting(user, setting);
			log.info("User [{}] dashboard add space {}.", user.getUsername(), spaceUname);
		}catch (Exception e) {
			//this is not critical error, so just catch to log
			log.error("Unable to add new space to user Dashboard", e);
		}
		return NutCode.SPACE_CREATED;
		
	}

	private String saveOrUpdatePage(HttpServletRequest request, HttpServletResponse response) {
		if(!doBasicAuthentication(request))
			return NutCode.AUTHENTICATION_ERROR+"";
		
		String spaceUname = null, title = null, pageUuid = null;
		InputStream content = null;
		int version = 0;
	
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			@SuppressWarnings("unchecked")
			List<FileItem> items = upload.parseRequest(request);
			for (FileItem item : items) {
				String name = item.getFieldName();
				if (StringUtils.equals(name, "space")) {
					spaceUname = item.getString(Constants.UTF8);
				}else if (StringUtils.equals(name, "title")) {
					title = item.getString(Constants.UTF8);
				}else if (StringUtils.equals(name, "puuid")) {
					pageUuid = item.getString(Constants.UTF8);
				}else if (StringUtils.equals(name, "version")) {
					version = NumberUtils.toInt(item.getString(Constants.UTF8));
				}else if (StringUtils.equals(name, "content")) {
					content = item.getInputStream();
				}
			}
			
			
			log.warn("Nut service for page {} (UUID:{}) on space {}.",new String[]{ title, pageUuid, spaceUname});
			if(content != null && spaceUname != null && title != null){
				//parse BPlist
				Map<String, File> files = nutParser.parseBPlist(content);
				Space space = getSpaceService().getSpaceByUname(spaceUname);
				if(files != null && space != null){
					File htmlFile = files.remove(NutParser.MAIN_RESOURCE_URL);
					if(htmlFile != null){
						String htmlText = nutParser.convertNutHTMLToPageHTML(FileUtils.readFileToString(htmlFile));
						
						//save Page
						Page page = new Page();
						PageContent pageContent = new PageContent();
						page.setContent(pageContent);
						pageContent.setContent(getRenderService().renderHTMLtoMarkup(spaceUname, htmlText));
						page.setPageUuid(pageUuid);
						page.setTitle(title);
						page.setSpace(space);
						page.setVersion(version);
						
						//upload attachments
						if(files.size() > 0){
							if(pageUuid == null){
								//must get pageUUID first for upload attachment, so save page to draft first
								Draft draft = getPageService().saveDraft(WikiUtil.getUser(), page.cloneToDraft(),Draft.AUTO_DRAFT);
								
								pageUuid = draft.getPageUuid();
								page.setPageUuid(pageUuid);
								
								log.info("Nut save draft with new page uuid {}", pageUuid);
							}
							List<FileNode> attachments = new ArrayList<FileNode>();
							for (File attach : files.values()) {
								FileNode node = new FileNode();
								node.setFilename(attach.getName());
								node.setFile(new FileInputStream(attach));
								node.setBulkZip(false);
								node.setShared(false);
								node.setIdentifier(pageUuid);
								node.setCreateor(WikiUtil.getUserName());
								node.setType(RepositoryService.TYPE_ATTACHMENT);
								node.setStatus(SharedConstants.NONE_DRAFT);
								node.setComment("TextNut uploaded attached file");
								//???node.setContentType(contentType);
								
								attachments.add(node);
								
								log.info("Uploading attachment {}", node.getFilename());
							}
							attachments = getPageService().uploadAttachments(spaceUname, pageUuid, attachments, true);
							page.setAttachments(attachments);
							
							log.info("Nut uploaded attachments successfully.");
						}
						
						
						getPageService().savePage(page, WikiConstants.NOTIFY_ALL, true);
						
						log.info("Nut save page {} by version {} successfully.", title, version);
						
						getActivityLog().logPageSaved(page);
						//return version:pageUUID combination. Version number must greater than 0
						return page.getVersion()+":" + page.getPageUuid();
					}
				}
			}
			
			log.warn("Nut save or update page {} (UUID:{}) failed on space {}.",new String[]{ title, pageUuid, spaceUname});
			if(pageUuid == null){
				return String.valueOf(NutCode.PAGE_CREATED_FAILED);
			}else{
				return String.valueOf(NutCode.PAGE_UPDATE_FAILED);
			}
		} catch (FileUploadException e) {
			log.error("Upload Nut file failed", e);
		} catch (UnsupportedEncodingException e) {
			log.error("Upload Nut file failed", e);
		} catch (IOException e) {
			log.error("Upload Nut file failed", e);
		} catch (PageException e) {
			log.error("Upload Nut file failed", e);
		} catch (VersionConflictException e) {
			log.error("Upload Nut file failed", e);
		} catch (PageSaveTiemoutExcetpion e) {
			log.error("Upload Nut file failed", e);
		} catch (DuplicatedPageException e) {
			log.error("Duplicate name for nut file.", e);
			return String.valueOf(NutCode.PAGE_DUPLICATED_TITLE);
		} catch (RepositoryException e) {
			log.error("Upload Nut file failed", e);
		} catch (RepositoryTiemoutExcetpion e) {
			log.error("Upload Nut file failed", e);
		} catch (RepositoryQuotaException e) {
			log.error("Upload Nut file failed", e);
		}
		
		return String.valueOf(NutCode.PAGE_UPDATED);
	}
	private int removePage(HttpServletRequest request, HttpServletResponse response) {
		if(!doBasicAuthentication(request))
			return NutCode.AUTHENTICATION_ERROR;
		
		//TODO:
		return NutCode.PAGE_DELETED;
	}
	//********************************************************************
	//               Function methods
	//********************************************************************
	/**
	 * Do basic authentication
	 * @param request
	 * @return
	 */
	private boolean doBasicAuthentication(HttpServletRequest request){
		  String authHeader = request.getHeader("Authorization");
	      if (authHeader != null) {
	         String[] tokens = authHeader.split("\\s+");
	         if (tokens.length == 2) {
	            if (tokens[0].equalsIgnoreCase("Basic")) {
	               String userPass = new String(Base64.decodeBase64(tokens[1].getBytes()));
	               int p = userPass.indexOf(":");
					if (p != -1) {
						String userID = userPass.substring(0, p);
						String password = userPass.substring(p + 1);
						return checkUserPassword(userID, password);
	               }
	            }
	         }
	      }
	      return false;
	}

	/**
	 * @param userID
	 * @param password
	 * @return
	 */
	private boolean checkUserPassword(String userID, String password) {
		// Verify the password
		User user = getUserReadingService().getUserByName(userID);
		// !!! Must do password validation here as SecurityServiceproxyLogin() always give authentication
		String passwd = CodecUtil.encodePassword(password, Global.PasswordEncodingAlgorithm);
		if (user == null || !StringUtils.equals(user.getPassword(), passwd)) {
			log.warn("Invalid username or password for user: " +userID);
			return false;
		}
		getSecurityService().proxyLogin(user.getUsername());
		return true;
	}

	//********************************************************************
	//               Set / Get
	//********************************************************************
	/**
	 * @return
	 */
	private UserReadingService getUserReadingService() {
		if(userReadingService == null){
			ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
			userReadingService = (UserReadingService) ctx.getBean(UserReadingService.SERVICE_NAME);
		}
		return userReadingService;
	}
	private SpaceService getSpaceService() {
		if(spaceService == null){
			ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
			spaceService = (SpaceService) ctx.getBean(SpaceService.SERVICE_NAME);
		}
		return spaceService;
	}

	/**
	 * @return
	 */
	private ActivityLogService getActivityLog() {
		if(activityLog == null){
			ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
			activityLog = (ActivityLogService) ctx.getBean(ActivityLogService.SERVICE_NAME);
		}
		return activityLog;
	}
	/**
	 * @return
	 */
	private SettingService getSettingService() {
		if(settingService == null){
			ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
			settingService = (SettingService) ctx.getBean(SettingService.SERVICE_NAME);
		}
		return settingService;
	}
	private SecurityService getSecurityService() {
		if(securityService == null){
			ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
			securityService = (SecurityService) ctx.getBean(SecurityService.SERVICE_NAME);
		}
		return securityService;
	}
	private RenderService getRenderService() {
		if(renderService == null){
			ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
			renderService  = (RenderService) ctx.getBean(RenderService.SERVICE_NAME);
		}
		return renderService ;
	}
	private PageService getPageService() {
		if(pageService == null){
			ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
			pageService  = (PageService) ctx.getBean(PageService.SERVICE_NAME);
		}
		return pageService ;
	}
}
