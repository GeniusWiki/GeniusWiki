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
package com.edgenius.wiki.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Global;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.ITicket;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.service.MessageService;
import com.edgenius.wiki.Shell;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.blogsync.BlogSyncException;
import com.edgenius.wiki.blogsync.BlogSyncService;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.gwt.client.model.BlogMetaList;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.SpaceListModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.server.SpaceController;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.server.handler.GWTSpringController;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.ActivityLogService;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.SpaceException;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.ThemeService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class SpaceControllerImpl extends GWTSpringController implements SpaceController{
	private static final Logger log = LoggerFactory.getLogger(SpaceControllerImpl.class);
	private SpaceService spaceService;
	private SecurityService securityService;
	private SettingService settingService;
	private RepositoryService repositoryService;
	private ThemeService themeService;
	private BlogSyncService blogSyncService;  
	private MessageService messageService;  
	private ActivityLogService activityLog;
	
	public SpaceListModel getSpacesInfo(String filterText, int returnCount, String sortBy) {
		
		User viewer = WikiUtil.getUser();
		//default sort by score then create date
		List<Space> spaces = spaceService.getSpaces(viewer, 0, returnCount, null, filterText, true);
		
		SpaceListModel model = new SpaceListModel();
		model.spaceList = new ArrayList<SpaceModel>();
		if(spaces != null){
			for (Space space : spaces) {
				SpaceModel sModel = new SpaceModel();
				sModel.name=space.getName();
				sModel.unixName=space.getUnixName();
				sModel.description=space.getDescription();
				model.spaceList.add(sModel);
	
			}
		}
		
		return model;
	}
	public PageItemListModel getRemovedPages(String spaceUname) {
		List<Page> removed = spaceService.getRemovedCurrentPages(spaceUname);
		
		PageItemListModel model = new PageItemListModel();
		for (Page page : removed) {
			PageItemModel history = PageUtil.copyToPageItem(page);
			model.itemList.add(history);
		}

		return model;
	}
	
	public SpaceModel removeSpace(String spaceUname){
		SpaceModel model = new SpaceModel();
		try {
			Space space = spaceService.removeSpaceInDelay(spaceUname, Global.DelayRemoveSpaceHours);
			model.unixName = spaceUname;
			model.isRemoved = true;
			if(Global.DelayRemoveSpaceHours <= 0){
				model.delayRemoveHours = "";
			}else{
				model.delayRemoveHours = Integer.valueOf(Global.DelayRemoveSpaceHours).toString();
			}
			
			activityLog.logSpaceRemoved(space, WikiUtil.getUser(), Global.DelayRemoveSpaceHours <= 0);
		} catch (SpaceException e) {
			model.errorCode = ErrorCode.REMOVE_SPACE_ERR; 
		}
		
		return model;
	}
	public SpaceModel restoreSpace(String spaceUname){
		
		try {
			spaceService.undoRemoveSpace(spaceUname);
			SpaceModel spaceModel = getSpace(spaceUname);
			
			//log activity
			if(spaceModel.errorCode != null)
				activityLog.logSpaceRestored(spaceUname, spaceModel.name, WikiUtil.getUser());
			
			return spaceModel;
		} catch (SpaceException e) {
			SpaceModel model = new SpaceModel();
			model.unixName = spaceUname;
			model.errorCode = ErrorCode.REMOVE_SPACE_ERR; 
			return model;
		}
		
	}
	public SpaceModel updateSpace(SpaceModel space){
		space.unixName = StringUtil.trim(space.unixName);
		space.name = StringUtil.trim(space.name);

		Space pSpace = spaceService.getSpaceByUname(space.unixName);
		if(pSpace == null){
			space.errorCode = ErrorCode.SPACE_NOT_EXIST_ERR;
			return space;
		}
		
		Space spaceByName = spaceService.getSpaceByTitle(space.name);
		if( spaceByName != null && !StringUtils.equals(space.unixName, spaceByName.getUnixName())){
			space.errorCode = ErrorCode.DUPLICATE_SPACE_TITLE_ERR;
			return space;
		}

		//only update name and description currently
		pSpace.setName(space.name);
		pSpace.setTagString(space.tags);
		pSpace.setDescription(space.description);
		pSpace.setType((short) space.type);
		
		try {
			spaceService.updateSpace(pSpace, true);
			securityService.resetSpacePrivateCache(space.unixName);
			securityService.fillSpaceWikiOperations(WikiUtil.getUser(),pSpace);
			//need copy back some info
			SpaceUtil.copySpaceToModel(pSpace, space,WikiUtil.getUser(),themeService);
			space.linkBlogMetas = SpaceUtil.getSpaceLinkMetaToModel(pSpace);
			
			try {
				ITicket ticket = repositoryService.login(space.unixName, space.unixName, space.unixName);
				space.quota = repositoryService.getSpaceQuoteUsage(ticket, space.unixName);
			} catch (RepositoryException e) {
				log.error("Unable to get space quota. " + space.unixName, e);
			}
		} catch (Exception e) {
			log.error("Update space failed with errors :" ,e);
			space.errorCode = ErrorCode.SAVEUPDATE_ERR;
		}
		return space;

	}
	
	//JDK1.6 @Override
	public Boolean updateCommentNotifyType(String spaceUname, int type) {
		
		try {
			Space space = spaceService.getSpaceByUname(spaceUname);
			SpaceSetting setting = space.getSetting();
			setting.setCommentNotifyType(type);
			settingService.saveOrUpdateSpaceSetting(space, setting);
			return Boolean.TRUE;
		} catch (Exception e) {
			log.error("Unable to update comment notify type",e);
		}
		return Boolean.FALSE;
	}

	/**
	 * This method is disable as change theme is difficult to decide:
	 * 1. if user has customized theme, how can keep user customized sidebar, body markup etc?
	 * 2. if theme switch from/to wiki to/from blog, how can setup home page, default page? How to handle customzied values?
	 * 
	 */
	public SpaceModel updateTheme(String spaceUname, String themeName){
		
		SpaceModel model = new SpaceModel();
		Space space = spaceService.getSpaceByUname(spaceUname);
		if(space == null){
			model.errorCode = ErrorCode.SPACE_NOT_EXIST_ERR;
			return model;
		}
		//only update name and description currently
		SpaceSetting set = space.getSetting();
		set.setTheme(themeName);
		if(set.isCustomizedTheme()){
			//user already create cutomized theme, change name is not enough, need update type etc....
		}
		//MUST put it back, because getSetting() may return a new SpaceSetting instance which is not reference to space.
		space.setSetting(set);
		
		try {
			settingService.saveOrUpdateSpaceSetting(space, space.getSetting());
			//need copy back some info
			model.unixName = spaceUname;
		} catch (Exception e) {
			log.error("Update space failed with errors :" ,e);
			model.errorCode = ErrorCode.SAVEUPDATE_ERR;
		}
		return model;
		
	}
	public boolean isDuplicatedSpace(String nameOrUname, boolean isName){
		if(isName){
			if(spaceService.getSpaceByTitle(nameOrUname) != null){
				return true;
			}
		}else {
			if(spaceService.getSpaceByUname(nameOrUname) != null){
				return true;
			}
		}
		
		return false;
	}
	/**
	 * !!! This method has similar logic in NutServlet.createSpace() and WsSpaceServiceImpl.createSpace(), 
	 * if any bug fix on this method, please also fix there!!!
	 * 
	 * Create a space. <br>
	 * All input fields (title, space uuid and description cannot be blank). <Br>
	 * The uuid must be uniqued
	 * @return
	 */
	public SpaceModel createSpace(SpaceModel space){
		space.unixName = StringUtil.trim(space.unixName);
		space.name = StringUtil.trim(space.name);
		
		if(spaceService.getSpaceByTitle(space.name) != null){
			space.errorCode = ErrorCode.DUPLICATE_SPACE_TITLE_ERR;
			return space;
		}
		if(spaceService.getSpaceByUname(space.unixName) != null){
			space.errorCode = ErrorCode.DUPLICATE_SPACE_KEY_ERR;
			return space;
		}
		
		Space pSpace = new Space();
		SpaceUtil.copyModelToSpace(space,pSpace);
		//clean Uid to avoid staleException
		pSpace.setUid(null);
	
		WikiUtil.setTouchedInfo(userReadingService, pSpace);
		try {
			if(space.linkBlogMetas != null && space.linkBlogMetas.size() > 0){
				boolean hasBlogError = false;
				for (BlogMeta blog : space.linkBlogMetas) {
					if(!blogSyncService.verifyBlog(blog)){
						blog.setError(messageService.getMessage("err.invalid.blog.info"));
						hasBlogError = true;
					}
					
					//stop create space!
					if(hasBlogError){
						space.errorMsg = messageService.getMessage("err.invalid.blog.info");
						return space;
					}
				}
			}					
			Page homepage = spaceService.createSpace(pSpace);
			if(homepage != null){
				//link ext blog if it has
				if(space.linkBlogMetas != null && space.linkBlogMetas.size() > 0){
					for (BlogMeta blog : space.linkBlogMetas) {
						//link blog must before SpaceService.saveHomepage(); as saveHomePage() will dispatch JMS message to invoke blogSync Service.
						//update canonical URL
						blogSyncService.linkToSpace(blog,space.unixName);
					}
				}
				
				spaceService.saveHomepage(pSpace,homepage);
				settingService.saveOrUpdateSpaceSetting(pSpace, pSpace.getSetting());
			}
			//copy back space unixName
			space.unixName = pSpace.getUnixName();
			
			space.isShellEnabled = Shell.enabled && !pSpace.isPrivate() && !pSpace.containExtLinkType(Space.EXT_LINK_SHELL_DISABLED);
			if(space.isShellEnabled){
				space.isShellAutoEnabled = Shell.autoEnabled;
				space.shellThemeBaseURL = Shell.getThemeBaseURL();
			}
			
			activityLog.logSpaceCreated(pSpace);
		} catch (Exception e) {
			log.error("Create space failed with errors during repository worksapce creating :" ,e);
			space.errorCode = ErrorCode.SAVEUPDATE_ERR;
		} 
		return space;
	}
	
	public SpaceModel getSpace(String spaceUname) {
		Space space = spaceService.getSpaceByUname(spaceUname);
		SpaceModel model = new SpaceModel();
		if(space == null){
			model.errorCode = ErrorCode.SPACE_NOT_EXIST_ERR;
			return model;
		}
		User viewer = WikiUtil.getUser();
		securityService.fillSpaceWikiOperations(viewer,space);
		SpaceUtil.copySpaceToModel(space, model,viewer,themeService);
		model.linkBlogMetas = SpaceUtil.getSpaceLinkMetaToModel(space);
		
		try {
			ITicket ticket = repositoryService.login(spaceUname, spaceUname, spaceUname);
			model.quota = repositoryService.getSpaceQuoteUsage(ticket, spaceUname);
		} catch (RepositoryException e) {
			log.error("Unable to get space qoota. " + spaceUname, e);
		}
		//put viewer information: it is useful for offline button(SyncButton)
		model.viewer = UserUtil.copyUserToModel(viewer,viewer);
		
		if(space.isRemoved())
			model.delayRemoveHours = Integer.valueOf(spaceService.getRemovedSpaceLeftHours(spaceUname)).toString();
		
		return model;
	}
	
	public String updateShellLink(String spaceUname, boolean link) {
		Space space = spaceService.getSpaceByUname(spaceUname);
		if(space == null){
			//error
			return null;
		}
		
		String url = null;
		if(link){
			//link shell - please note , at this time, space link is default enabled and already updated Shell in createSpace() method.
			//However, here is able to choose shell theme or disconnect from shell.
			if(!space.isPrivate()){
				String name = Shell.requestSpaceThemeName(spaceUname);
				if(name != null){
					//send request to get space theme 
					//TODO: now, only one space mapping to one shell, use spaceUname as key, but future may one space mapping to multiple shells.
					space.getSetting().setShellTheme(spaceUname, name);
					settingService.saveOrUpdateSpaceSetting(space, space.getSetting());
					
					if(space.containExtLinkType(Space.EXT_LINK_SHELL_DISABLED)){
						//send request to shell and get back theme name, then save space setting. Return space shell URL
						space.removeExtLinkType(Space.EXT_LINK_SHELL_DISABLED);
						spaceService.updateSpace(space, false);
					}
					
					url = Shell.getPageShellURL(spaceUname, null);
				}
			}
		}else{
			//disable and delete
			if(!space.containExtLinkType(Space.EXT_LINK_SHELL_DISABLED)){
				space.addExtLinkType(Space.EXT_LINK_SHELL_DISABLED);
				spaceService.updateSpace(space, false);
				
				Shell.notifySpaceRemove(spaceUname);
			}
			
		}
		
		return url;
	}
	
	public BlogMetaList getBlogs(int type, String url, String user, String pwd) {
		BlogMetaList blogList = new BlogMetaList(); 
		try {
			 blogList.blogList = (ArrayList<BlogMeta>) blogSyncService.getUsersBlogs(type, url, user, pwd);
		} catch (BlogSyncException e) {
			blogList.errorMsg = e.getMessage();
			log.error("Get blogs failed " + url, e);
		}
		return blogList;
	}

	public BlogMetaList updateLinkedBlog(String spaceUname, ArrayList<BlogMeta> blogs) {
		BlogMetaList list = new BlogMetaList();
		
		Space space = spaceService.getSpaceByUname(spaceUname);
		List<BlogMeta> exists = space.getSetting().getLinkedMetas();
		if(exists != null){
			List<BlogMeta> needRemove = new ArrayList<BlogMeta>();
			//check if anyone removed or updated
			for (BlogMeta exist : exists) {
				if(blogs != null){
					if(blogs.contains(exist)){
						//already exist, then don't do anything -- ???
						blogs.remove(exist);
					}else{
						//it is not inside list, so remove existing one.
						needRemove.add(exist);
					}
				}else{
					//remove all existing
					needRemove.add(exist);
				}
				
			}
			//the reason that don't call disconnectFromSpace() within above looping, as it will cause ConcurrenceModification exception
			//as disconnectFromSpace() also update list from space.getSetting().getLinkedMetas()
			if(needRemove.size() > 0){
				for (BlogMeta blogMeta : needRemove) {
					blogSyncService.disconnectFromSpace(spaceUname, blogMeta.getKey());
				}
			}
			
		}
		if(blogs != null){
			for(BlogMeta blog:blogs){
				if(!blogSyncService.verifyBlog(blog)){
					blog.setError(messageService.getMessage("err.invalid.blog.info"));
					list.errorMsg = messageService.getMessage("err.invalid.blog.info");
				}else{
					//stop create space!
					try {
						blogSyncService.linkToSpace(blog, spaceUname);
					} catch (BlogSyncException e) {
						log.error("Update blog failed " + blog, e);
						blog.setError(messageService.getMessage("err.invalid.blog.info"));
						list.errorMsg = messageService.getMessage("err.invalid.blog.info");
					}
				}
			}
		}		
		
		//get from space, rather than from input parameter as some blog may not success saved.
		Collection<BlogMeta> blogMetas = SpaceUtil.getSpaceLinkMetaToModel(space);
		if(blogMetas != null)
			list.blogList = new ArrayList<BlogMeta>(blogMetas);
		return list;
		
	}
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setThemeService(ThemeService themeService) {
		this.themeService = themeService;
	}

	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}
	public void setBlogSyncService(BlogSyncService blogSyncService) {
		this.blogSyncService = blogSyncService;
	}
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	public void setActivityLog(ActivityLogService activityLog) {
		this.activityLog = activityLog;
	}

	
}
