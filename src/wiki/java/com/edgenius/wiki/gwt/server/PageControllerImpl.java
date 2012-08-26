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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.captcha.CaptchaServiceProxy;

import com.edgenius.core.Global;
import com.edgenius.core.UserSetting;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.PageTheme;
import com.edgenius.wiki.Theme;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.CaptchaCodeModel;
import com.edgenius.wiki.gwt.client.model.DiffListModel;
import com.edgenius.wiki.gwt.client.model.DiffModel;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.model.TreeItemListModel;
import com.edgenius.wiki.gwt.client.model.TreeItemModel;
import com.edgenius.wiki.gwt.client.server.PageController;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.PageAttribute;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.handler.GWTSpringController;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.Draft;
import com.edgenius.wiki.model.History;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageContent;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.UserPageMark;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.ActivityLogService;
import com.edgenius.wiki.service.CommentService;
import com.edgenius.wiki.service.DeltaObject;
import com.edgenius.wiki.service.DiffException;
import com.edgenius.wiki.service.DiffService;
import com.edgenius.wiki.service.DuplicatedPageException;
import com.edgenius.wiki.service.ExportException;
import com.edgenius.wiki.service.ExportService;
import com.edgenius.wiki.service.HomePageRemoveException;
import com.edgenius.wiki.service.PageException;
import com.edgenius.wiki.service.PageSaveTiemoutExcetpion;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.SecurityDummy;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.SpaceNotFoundException;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.ThemeSaveException;
import com.edgenius.wiki.service.ThemeService;
import com.edgenius.wiki.service.VersionConflictException;
import com.edgenius.wiki.util.WikiUtil;

import static com.edgenius.wiki.gwt.server.PageUtil.COPY_ATTACHMENT_WITHOUT_DRAFT;
import static com.edgenius.wiki.gwt.server.PageUtil.NOT_COPY_ATTACHMENT;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class PageControllerImpl extends GWTSpringController implements PageController {
	private static final Logger log = LoggerFactory.getLogger(PageControllerImpl.class);

	
	private SecurityDummy securityDummy;
	private SecurityService securityService;
	private PageService pageService;
	private RenderService renderService;
	private SpaceService spaceService;
	private SettingService settingService;
	private DiffService diffService;
	private ExportService exportService;
	private MessageService messageService;
	private CaptchaServiceProxy captchaService;
	private CommentService commentService;
	private ThemeService themeService;
	private ActivityLogService activityLog;
	
	public PageModel savePage(PageModel model, boolean forceSave) {
		if(!WikiUtil.captchaValid(captchaService, model))
			return null;

		Page page = new Page();
		PageUtil.copyModelToPage(model, page, renderService);

		try {
			page = pageService.savePage(page, model.requireNotified, forceSave);
			buildSidebar(page);
			buildSpaceMenu(page);
			User user = WikiUtil.getUser(userReadingService);
			
			//refresh page attachment: savePage() method does not refresh it, because PageIndexInterceptor does not suggest contain attachment. 
			try {
				page.setAttachments(pageService.getPageAttachment(model.spaceUname, page.getPageUuid(), true, true, user));
			} catch (RepositoryException e) {
				log.error("Refresh page attachment error after saving page:" , e);
			}
			
			// refresh all parameters
			setUserMarkOnModel(model, user, page);
			PageUtil.copyPageToModel(page, model, userReadingService, COPY_ATTACHMENT_WITHOUT_DRAFT);
			model.tabIndex = SharedConstants.TAB_TYPE_DEFAULT_VISIBLE;
			
			pageService.stopEditing(page.getPageUuid(), user);
			
			activityLog.logPageSaved(page);
		} catch (PageException e) {
			log.error("Page saved with error:" , e);
			model.errorCode = ErrorCode.PAGE_SAVE_ERROR;
		} catch (VersionConflictException e) {
			model.errorCode = ErrorCode.PAGE_VERSION_CONFLICT;
		} catch (DuplicatedPageException e) {
			model.errorCode = ErrorCode.PAGE_DUPLICATED_TITLE;
		} catch (PageSaveTiemoutExcetpion e) {
			model.errorCode = ErrorCode.PAGE_SAVE_TIME_OUT;
		} 
		
		
		return model;
	}

	public PageModel saveAutoDraft(PageModel model) {

		Draft draft = new Draft();
		PageUtil.copyModelToPage(model, draft, renderService);
		log.info("Auto draft is going to save for " + model.title + " on space " + model.spaceUname);
		
		try {
			if(WikiUtil.getUser(userReadingService).isAnonymous()){
				//anonymous does not allow save draft! This call must be from "Preview" action, so just return rendered content back
				List<RenderPiece> renderPieces  = renderService.renderHTML(draft);
				draft.setRenderPieces(renderPieces);
			}else{
				draft = pageService.saveDraft(WikiUtil.getUser(userReadingService), draft, Draft.AUTO_DRAFT);
			}
			// refresh all parameters
			PageUtil.copyPageToModel(draft, model, userReadingService, NOT_COPY_ATTACHMENT);

			//only hide right bar - page info. User still can turn on TreePanel left sidebar or keep it visible.
			model.attribute = model.attribute| PageAttribute.NO_SIDE_BAR  | PageAttribute.NO_SPACE_MENU; 

		} catch (PageException e) {
			log.error("Page saved with error:" , e);
			model.errorCode = ErrorCode.PAGE_SAVE_DRAFT_ERROR;
		}

		return model;
	}

	public PageModel saveManualDraft(PageModel model, boolean exitToView) {
		Draft draft = new Draft();
		PageUtil.copyModelToPage(model, draft, renderService);

		try {
			if(!WikiUtil.getUser(userReadingService).isAnonymous()){
				//failure tolerance check, anonymous does not allow save draft. 
				draft = pageService.saveDraft(WikiUtil.getUser(userReadingService), draft, Draft.MANUAL_DRAFT);
			}
			if(exitToView){
				// get page title from original one. For existed page, it simple return itself. For new creating,
				//it need return its parent page.
				String title = getReturnPageTitle(model.pageUuid,model.parentPageUuid);
				
				//this draft may has not persist page, but this method won't harmful
				pageService.stopEditing(draft.getPageUuid(), WikiUtil.getUser(userReadingService));
				
				return viewPage(model.spaceUname,title, null);
			}else{
				//manually save draft by keyboard, the editing page still hold. return this draft render, for preview.
				PageUtil.copyPageToModel(draft, model, userReadingService, NOT_COPY_ATTACHMENT);
				
			    //only hide right bar - page info. User still can turn on TreePanel left sidebar or keep it visible.
				model.attribute = model.attribute| PageAttribute.NO_SIDE_BAR  | PageAttribute.NO_SPACE_MENU; 
			}
			
			
		} catch (PageException e) {
			log.error("Page saved with error:" , e);
			model.errorCode = ErrorCode.PAGE_SAVE_DRAFT_ERROR;
		}

		return model;
	}

	public PageModel getEmptyPageDefaultContent(String pageUuid){
		//!!! client side must ensure only current page will call this method!
		
		PageModel model = new PageModel();
		Page page = pageService.getCurrentPageByUuid(pageUuid);
		if(page != null){
			//return necessary fields value
			model.pageUuid = page.getPageUuid();
			model.pageVersion = page.getVersion();
			model.spaceUname = page.getSpace().getUnixName();
			
			String pageType = WikiUtil.isHomepage(page)?PageTheme.SCOPE_HOME:PageTheme.SCOPE_DEFAULT;
			if(pageType == PageTheme.SCOPE_HOME){
				Theme theme = themeService.getPageTheme(page,pageType);
				//display welcome message if page is home page, blank, and is current (not history or draft)
				model.renderContent = renderService.renderHTML(theme.getCurrentPageTheme().getWelcome());
			}
		}
		
		return model;

	
	}
	/**
	 * Load initial edit page with page content.
	 */
	public PageModel editPage(String spaceUname,String pageUuid) {
		long s=0;
		if(log.isDebugEnabled()){
			s = System.currentTimeMillis();
		}
		
		PageModel model = new PageModel();
		try {
			//don't use title directly, the reason is title may changed, but uuid never change....
			Page page = pageService.getCurrentPageByUuid(pageUuid);
			if (page == null){
				log.error("Error in finding current page by space Uname:" + spaceUname + " and page Uuid:" + pageUuid);
				throw new PageException("Page not found by pageUuid: " + pageUuid);
			}
			//because getCurrentPageByUuid() does not render, refresh attach and navbar, so do these by call getByTitle()
			page = pageService.getCurrentPageByTitle(spaceUname, page.getTitle());
			try {
				page.setAttachments(pageService.getPageAttachment(spaceUname, page.getPageUuid(),true,false, null));
			} catch (RepositoryException e) {
				log.error("Failed get page node in repository " + page.getPageUuid(),e);
			}
			
			PageUtil.copyPageToModel(page, model, userReadingService, COPY_ATTACHMENT_WITHOUT_DRAFT);
			
			//current is RichEditor request, then replace PageModel.content by HTML text rather than wiki markup
			User user = WikiUtil.getUser(userReadingService);
			model.isRichContent = WikiUtil.isUsingRichEditor(user);
			if(model.isRichContent){
				renderService.renderHTML(RenderContext.RENDER_TARGET_RICH_EDITOR, page);
				model.content = renderService.renderRichHTML(spaceUname, pageUuid, page.getRenderPieces());
			}
			// set draftUid, then edit page can display message if resuming edit draft
			List<Draft> drafts = pageService.hasDraft(model.spaceUname,model.title, user);
			PageUtil.copyDraftStatus(drafts,model,user);
			
			
			Map<String, Long> editorMap = pageService.isEditing(page.getPageUuid());
			if(editorMap != null){
				//find latest editing user
				long max = 0;
				String recentEditorUsername = null; 
				for (Entry<String, Long> entry : editorMap.entrySet()) {
					if(StringUtils.equalsIgnoreCase(user.getUsername(),entry.getKey()))
						//ignore current login user - you don't need to tell login user himself is editing the page
						continue;
					if(entry.getValue() > max){
						max = entry.getValue();
						recentEditorUsername = entry.getKey();
					}
				}
				if(recentEditorUsername != null){
					User recentEditor = userReadingService.getUserByName(recentEditorUsername);
					if(recentEditor != null){
						model.editingUsername = recentEditor.getUsername();
						model.editingUserFullname = recentEditor.getUsername();
						model.editingUserPortrait = UserUtil.getPortraitUrl(recentEditor.getPortrait());
						//change to minutes
						model.editingTime = (int) (System.currentTimeMillis() - max)/60000;
					}
				}
			}
		    //only hide right bar - page info. User still can turn on TreePanel left sidebar or keep it visible.
			model.attribute = model.attribute| PageAttribute.NO_SIDE_BAR | PageAttribute.NO_SPACE_MENU; 
			pageService.startEditing(page.getPageUuid(), user);
		} catch (Exception e) {
			log.error("Get Page with error:" , e);
			model.errorCode = ErrorCode.PAGE_GET_ERROR;
		}
		if(log.isDebugEnabled()){
			log.debug("Edit page takes " + (System.currentTimeMillis() -s) + "ms");
		}
		return model;
	}


	public PageModel createPage(String spaceUname, String newPageTitle, String parentPageUuid, int newPageType) {
		//this method is called while user push "Create" button
		//this method does not check if the page(user want to create) is exist or not. It is non-sense: only user saving page,
		PageModel model = new PageModel();
		String spaceTitle = null;
		try {
			Space space = spaceService.getSpaceByUname(spaceUname);
			spaceTitle = space.getName();
			
			List<AbstractPage> ancestors = pageService.getPageAncestors(spaceUname, parentPageUuid);
			model.ancenstorList = new ArrayList<PageModel>();
			if(ancestors != null){
				for (AbstractPage parent : ancestors) {
					//only copy necessary fields to PageModel, need adjust according to navbar requirement
					PageModel parentModel = new PageModel();
					parentModel.title = parent.getTitle();
					parentModel.spaceTitle = spaceTitle;
					model.ancenstorList.add(parentModel);
				}
			}
			
			// set draftUid, then edit page can display message if resuming edit draft
			User user = WikiUtil.getUser(userReadingService);
			model.isRichContent = WikiUtil.isUsingRichEditor(user);

			List<Draft> drafts = pageService.hasDraft(spaceUname,newPageTitle, user);
			PageUtil.copyDraftStatus(drafts,model,user);
			
			if(space.getSetting().getLinkedMetas() != null)
				model.linkedBlogs = space.getSetting().getLinkedMetas();
			
		} catch (Exception e) {
			log.error("Get Page with error in create page pre check:" , e);
			model.errorCode = ErrorCode.PAGE_GET_ERROR;
		}
		
		//!!! bring back value
		model.title = newPageTitle;
		model.parentPageUuid = parentPageUuid;
		model.spaceUname = spaceUname;
		model.spaceTitle = spaceTitle; 
		model.newPageType = newPageType;
		//only hide right bar - page info. User still can turn on TreePanel left sidebar or keep it visible.
		model.attribute = model.attribute| PageAttribute.NO_SIDE_BAR | PageAttribute.NO_SPACE_MENU;
		
		return model;
	}

	//JDK1.6 @Override
	public String cancelEdit(String pageUuid, String parentPageUuid) {
		pageService.stopEditing(pageUuid, WikiUtil.getUser(userReadingService));
		return getReturnPageTitle(pageUuid, parentPageUuid);
	}


	public PageModel getHistoryByUid(Integer historyUid, boolean refreshAttachment) {
		PageModel model = new PageModel();

		try {
			History history = pageService.getHistory(historyUid);
			if (history == null){
				throw new PageException("Null return for history by uid:" + historyUid);
			}
			buildSidebar(history);
			buildSpaceMenu(history);
			
			if (refreshAttachment) {
				//!! so far, no functions use refreshAttachment is true yet!
				try {
					history.setAttachments(pageService.getPageAttachment(history.getSpace().getUnixName(), history.getPageUuid(),true,false,null));
				} catch (RepositoryException e) {
					log.error("Refresh history attachment error:" + history.getPageUuid() + ";version:" +history.getVersion() , e);
				}
				PageUtil.copyPageToModel(history, model, userReadingService, COPY_ATTACHMENT_WITHOUT_DRAFT);
			} else {
				PageUtil.copyPageToModel(history, model, userReadingService, NOT_COPY_ATTACHMENT);
				// clean attachment info to keep original attachment in current page
				model.attachmentJson = "";
			}
			
			Page page = pageService.getCurrentPageByUuid(history.getPageUuid());
			if (page == null){
				throw new PageException("Null return for page by uuid:" + history.getPageUuid() + " for history uid:" +historyUid);
			}
			
			if(page.getVersion() != (history.getVersion()+1)){
				History nextHistory = pageService.getHistoryByVersion(history.getPageUuid(), history.getVersion() + 1);
				if(nextHistory != null){
					model.nextHistoryItem = PageUtil.copyToPageItem(nextHistory);
				}
			}else{
				//next is latest version
				model.nextHistoryItem = PageUtil.copyToPageItem(page);
				//a convention to tell current is latest version.
				model.nextHistoryItem.version = -1;
			}
			if(history.getVersion() > 1){
				History prevHistory = pageService.getHistoryByVersion(history.getPageUuid(), history.getVersion() - 1);
				if(prevHistory != null){
					model.prevHistoryItem = PageUtil.copyToPageItem(prevHistory);
				}
			}
			
			//don't allow user update "favorite, watch, attachment and tag" becuase these info does not bring current values back
			//if allow edit, it has conflict. For example, history show tag always empty, but current page may contain some. If edit on
			//history page, current ones will be replaced. 
			model.attribute = PageAttribute.NO_FAVORITE|PageAttribute.NO_WATCHED|PageAttribute.NO_ATTACHMENT|PageAttribute.NO_TAG;
			model.currentTitle = page.getTitle();
			model.isHistory = true;
		} catch (PageException e) {
			log.error("Get History with error:" , e);
			model.errorCode = ErrorCode.HISTORY_GET_ERROR;
		}
		return model;
	}

	public PageModel editDraft(Integer draftUid,int draftType, boolean refreshAttachment) {
		PageModel model = new PageModel();
		
		try {
			User user = WikiUtil.getUser(userReadingService);
			Draft draft = pageService.getDraft(user, draftUid);
			if (draft == null){
				throw new PageException("Error in finding draft by uid:" + draftUid);
			}
			
			if (refreshAttachment) {
				// copy this user's draft attachment: call by resume editing history page
				try {
					draft.setAttachments(pageService.getPageAttachment(draft.getSpace().getUnixName(), draft.getPageUuid(),true,true, user));
				} catch (RepositoryException e) {
					log.error("Refresh draft attachment error:" + draft.getPageUuid() + ";draft type:" +draftType, e);
				}
				
				//set all attachment as visible - keep consistent with PageUtil.copyPageToModel()
				String[] visibleAttachmentNodeList = new String[draft.getAttachments().size()];
				int idx = 0;
				for (FileNode node: draft.getAttachments()) {
					visibleAttachmentNodeList[idx] = node.getNodeUuid();
					idx++;
				}
				draft.setVisibleAttachmentNodeList(visibleAttachmentNodeList);
				if(model.isRichContent){
					renderService.renderHTML(RenderContext.RENDER_TARGET_RICH_EDITOR, draft);
				}else{
					renderService.renderHTML(draft);
				}
				PageUtil.copyPageToModel(draft, model, userReadingService, draftType);
			} else {
				if(model.isRichContent){
					renderService.renderHTML(RenderContext.RENDER_TARGET_RICH_EDITOR, draft);
				}else{
					renderService.renderHTML(draft);
				}
				PageUtil.copyPageToModel(draft, model, userReadingService, NOT_COPY_ATTACHMENT);
				// clean attachment info to keep original attachment in current page
				model.attachmentJson = "";
			}
			//current is RichEditor request, then replace PageModel.content by HTML text rather than wiki markup
			
			model.isRichContent = WikiUtil.isUsingRichEditor(user);
			if(model.isRichContent){
				model.content = renderService.renderRichHTML(draft.getSpace().getUnixName(), draft.getPageUuid(), draft.getRenderPieces());
			}
			//only hide right bar - page info. User still can turn on TreePanel left sidebar or keep it visible.
			model.attribute = model.attribute| PageAttribute.NO_SIDE_BAR | PageAttribute.NO_SPACE_MENU;
		} catch (PageException e) {
			log.error("Get Draft with error:" , e);
			model.errorCode = ErrorCode.PAGE_GET_ERROR;
		}
		return model;
	}
	
	/**
	 * Input: pageTitle and spaceUname
	 */
	public PageModel viewPage(String spaceUname,String  pageTitle, String commentUid){
		long s = 0;
		
		if(log.isDebugEnabled()){
			s = System.currentTimeMillis();
		}
		PageModel model = new PageModel();
		model.spaceUname = spaceUname;
		model.title = pageTitle;
		try {
			Page page = null;
			try{
				// show home page
				if (StringUtils.isBlank(model.title)) {
					page = pageService.getHomepage(model.spaceUname);
				} else{
					page = pageService.getCurrentPageByTitle(model.spaceUname, model.title);
				}
				if(page != null)
					page.setAttachments(pageService.getPageAttachment(spaceUname, page.getPageUuid(),true,false, null));
				//do nothing, waiting for creating "not found" page
			} catch (RepositoryException e) {
				log.error("Failed get page node in repository " + page.getPageUuid(),e);
			}
			
			boolean found = true;
			//save original title, because it may changed in createPageNotFound()
			String origTitle = model.title;
			if (page == null) {
				page = createPageNotFound(model);
				found = false;
			}else{
				buildSidebar(page);
				buildSpaceMenu(page);
			}
			User user = WikiUtil.getUser(userReadingService);
			setUserMarkOnModel(model, user, page);
			PageUtil.copyPageToModel(page, model, userReadingService, COPY_ATTACHMENT_WITHOUT_DRAFT);
			if(!found){
				//set nav bar info
				PageModel nav = new PageModel();
				nav.title = messageService.getMessage("not.found");
				nav.navToken = GwtUtils.getSpacePageToken(spaceUname,origTitle);
				model.ancenstorList.add(nav);
			}else{
				//found page - then try to see if it needs display comments etc.
				if(commentUid != null){
					model.tabIndex = SharedConstants.TAB_TYPE_COMMENT;
					model.tabFocus = true;
				}
			}
			if(Global.ADSENSE){
				//insert adsense in the front of page
				model.renderContent.add(0, new TextModel(WikiUtil.getAdsenseHTML(spaceUname, page)));
			}
			
			if(model.tabIndex <= 0){
				//load comment immediately after page loading. It looks most user prefer to this???
				model.tabIndex = SharedConstants.TAB_TYPE_DEFAULT_VISIBLE;
			}

		} catch (AccessDeniedException e){
			log.info("Access denied for page '" + model.title + "' on space '" + model.spaceUname + "' for user: " + WikiUtil.getUserName());
		} catch (Exception e) {
			log.error("View Page with error:" , e);
			model.errorCode = ErrorCode.PAGE_GET_ERROR;
		}
		
		if(log.isDebugEnabled()){
			log.debug("View page takes " + (System.currentTimeMillis() -s) + "ms");
		}
		return model;
	}


	public PageModel removeAttachment(String spaceUname,String pageUuid, String nodeUuid, String nodeVersion) {

		PageModel model = new PageModel();
		try {
			FileNode node = pageService.removeAttachment(spaceUname, pageUuid, nodeUuid, nodeVersion);
			
			try {
				activityLog.logAttachmentRemoved(spaceUname, pageService.getCurrentPageByUuid(pageUuid).getTitle(), WikiUtil.getUser(), node);
			} catch (Exception e) {
				log.warn("Activity log save error for attachment remove",e);
			}
		} catch (Exception e) {
			log.error("Failed remove attachment " , e);
			model.errorCode = ErrorCode.ATT_REMOVE_FAILED;
		}
		// have no idea what return yet(May 03,2007)
		return model;
	}

	public TreeItemListModel getPageTree(String spaceUname) {
		ArrayList<TreeItemModel> modelList = new ArrayList<TreeItemModel>();

		List<Page> list = pageService.getPageTree(spaceUname);
	
		for (Page page : list) {
			TreeItemModel item = new TreeItemModel();
			item.pageUuid = page.getPageUuid();
			item.title = page.getTitle();
			item.level = page.getLevel();
			if (page.getParent() != null) {
				String pUuid = page.getParent().getPageUuid();
				int size = modelList.size() - 1;
				TreeItemModel parent = null;
				// reverse loop, parent must before child, this is dependent on
				// pageService.getPageTree() return a sorted tree set.
				for (int idx = size; idx > -1; idx--) {
					parent = modelList.get(idx);
					if (StringUtils.equals(parent.pageUuid,pUuid))
						break;
				}
				item.parent = parent;
			}
			modelList.add(item);
		}
		
		TreeItemListModel model = new TreeItemListModel();
		model.list = modelList;
		model.spaceUname = spaceUname;
		return model;
	}

	public PageItemListModel getPageHistory(String spaceUname, String pageUuid, int startVer, int returnCount) {
		
		//this page history: null-> get all history
		List<History> list = pageService.getHistoryPages(spaceUname,pageUuid,startVer,returnCount, null);
		PageItemListModel model = new PageItemListModel();
		model.itemList = copyHistoryItem(list);
		
		if(startVer == 0){
			//current page model 
			Page currPage = pageService.getCurrentPageByUuid(pageUuid);
			PageItemModel currModel = PageUtil.copyToPageItem(currPage);
			model.itemList.add(0,currModel);
		}
		
		return model;
	}



	public PageModel copy(String fromSpaceUname, String fromPageUuid, String toSpaceUname, String toParentUuid, boolean withChildren) {
		PageModel model = new PageModel();
		try {
			Page page = pageService.copy(fromSpaceUname, fromPageUuid, toSpaceUname, toParentUuid, withChildren);
			buildSidebar(page);
			buildSpaceMenu(page);
			 
			PageUtil.copyPageToModel(page, model, userReadingService, COPY_ATTACHMENT_WITHOUT_DRAFT);
			
			model.tabIndex = SharedConstants.TAB_TYPE_DEFAULT_VISIBLE;
			
			activityLog.logPageCopied(pageService.getCurrentPageByUuid(fromPageUuid, true), page);
		} catch (Exception e) {
			model.errorCode = ErrorCode.PAGE_COPY_ERROR;
		}
		return model;
	}

	public PageModel move(String fromSpaceUname, String fromPageUuid, String toSpaceUname, String toParentUuid, boolean withChildren) {
		log.info("Move page " + fromPageUuid + " from space " + fromSpaceUname 
				+ " to " + toSpaceUname + " to parent " + toParentUuid + " with children " + withChildren);
		
		PageModel model = new PageModel();
		try {
			//for back
			Page src = (Page) pageService.getCurrentPageByUuid(fromPageUuid, true).clone();

			Page page = pageService.move(fromSpaceUname, fromPageUuid, toSpaceUname, toParentUuid, withChildren);
			buildSidebar(page);
			buildSpaceMenu(page);
			
			PageUtil.copyPageToModel(page, model, userReadingService, COPY_ATTACHMENT_WITHOUT_DRAFT);
			
			model.tabIndex = SharedConstants.TAB_TYPE_DEFAULT_VISIBLE;
			
			activityLog.logPageMoved(src, page);
		} catch (Exception e) {
			log.error("Page move failed " + e.toString(), e);
			model.errorCode = ErrorCode.PAGE_MOVE_ERROR;
		}
		return model;
	}

	public PageModel removeDraft(String spaceUname, String pageUuid, int type) {

		PageModel model = new PageModel();
		try {
			if(WikiUtil.getUser(userReadingService).isAnonymous()){
				//don't continue, otherwise, this will invoke PageMethodBeforeAdvice.before(), then goto login page
				return model;
			}
			//only remove auto saved draft when user cancel page
			Draft draft = pageService.removeDraft(WikiUtil.getUser(userReadingService), spaceUname, pageUuid, type);
			//cancel page also call this method, in this case, draft may not exist.
			if(draft != null){
				//so far only copy draft UUID and type, need modify if later need more value from page
				model.pageUuid = draft.getPageUuid();
				model.type = draft.getType();
			}
		} catch (PageException e) {
			log.error("Cancel page try to remove draft failed" , e);
			// don't send error to client side?
			// model.errorMsg = "Remove draft failed during cancelling page.";
		}
		return model;
	}

	public boolean markPage(String pageUuid, int type, boolean add) {
		User user = WikiUtil.getUser(userReadingService);
		return pageService.markPageFlag(type, pageUuid, user.getUsername(), add);
	}

	public PageModel removePage(CaptchaCodeModel captcha, String spaceUname, String pageUuid, boolean permenant) {
		if(!WikiUtil.captchaValid(captchaService, captcha))
			return null;
		
		PageModel model = new PageModel();
		try {
			Page page = pageService.removePage(spaceUname, pageUuid, false, permenant);
			if (page == null) {
				model.errorCode = ErrorCode.PAGE_REMOVE_NOT_FOUND;
			}
			
			model.tabIndex = SharedConstants.TAB_TYPE_DEFAULT_VISIBLE;
			
			activityLog.logPageRemoved(page, permenant, WikiUtil.getUser());
		} catch (HomePageRemoveException e) {
			model.errorCode = ErrorCode.PAGE_HOME_CANNOT_REMOVE;
		} catch (PageException e) {
			model.errorCode = ErrorCode.PAGE_REMOVE_FAILED;
		}

		return model;
	}
	
	public DiffListModel diff(Integer uid1, Integer uid2) {
		DiffListModel model = new DiffListModel();
		model.type = DiffListModel.FLAT_TYPE;
		ArrayList<DiffModel> diffList = new ArrayList<DiffModel>();
		try {
			log.info("Version comparing(pageUid:pageUid, -1 is current): " + uid1 + " vs " + uid2);
			String diffText = diffService.diffToHtml(uid1, uid2, true);
			DiffModel dm = new DiffModel(DiffModel.FLAT_HTML, diffText);
			diffList.add(dm);
			
		} catch (DiffException e) {
			log.error("Conflict diff exception:" , e);
			model.errorCode = ErrorCode.DIFF_FAILED;
		}
		if(uid1 != SharedConstants.CURRENT){
			History p1 = pageService.getHistoryObject(uid1);
			if(p1 != null){
				model.ver1 = uid1 == SharedConstants.CURRENT?0:(p1.getVersion());
			}else{
				//don't put model.errorCode, as it is not serious issue, just keep version number as -1, and let client side handle it
				log.error("Conflict failed get version number on page UID {}", uid1);
			}
		}else{
			//current version
			model.ver1 = 0;
		}
		if(uid2 != SharedConstants.CURRENT){
			History p2 = pageService.getHistoryObject(uid2);
			if(p2 != null){
				model.ver2 = uid2 == SharedConstants.CURRENT?0:(p2.getVersion());
			}else{
				//don't put model.errorCode, as it is not serious issue, just keep version number as -1, and let client side handle it
				log.error("Conflict failed get version numberon page UID {}", uid1);
			}
		}else{
			//current version
			model.ver2 = 0;
		}
		model.revs = diffList;
		return model;
	}
	
	public DiffListModel diff(String spaceUname, String currPageTitle, Integer historyVersion) {
		DiffListModel model = new DiffListModel();
		model.type = DiffListModel.FLAT_TYPE;
		ArrayList<DiffModel> diffList = new ArrayList<DiffModel>();
		try {
			Page page = pageService.getCurrentPageByTitle(spaceUname, currPageTitle);
			History history = pageService.getHistoryByVersion(page.getPageUuid(), historyVersion);
			if(history == null)
				throw new DiffException("Unable get history for page title " + currPageTitle + " by version " + historyVersion);
			String diffText = diffService.diffToHtml(null, history.getUid(), true);
			DiffModel dm = new DiffModel(DiffModel.FLAT_HTML, diffText);
			diffList.add(dm);
			
			//setup compare version number
			model.ver1 = 0;
			model.ver2 = history.getVersion();
			
		} catch (Exception e) {
			log.error("Diff exception:" , e);
			model.errorCode = ErrorCode.DIFF_FAILED;
		}
		model.revs = diffList;
		return model;
	}

	public DiffListModel diffConflict(PageModel draft) {
		DiffListModel model = new DiffListModel();
		model.type = DiffListModel.MERGE_TYPE;
		try {
			Page page;
			if (draft.pageUuid != null)
				//page Uuid may be null, if page is created new one, but other user already saved same title.
				page = pageService.getCurrentPageByUuid(draft.pageUuid);
			else
				page = pageService.getCurrentPageByTitle(draft.spaceUname, draft.title);
			if (page == null) {
				log.warn("Conflict diff can not get original page:{}", draft);
				model.errorCode = ErrorCode.DIFF_ORIG_PAGE_NOT_FOUND;
				return model;
			}
			List<DeltaObject> list = diffService.diffToObjectList(page.getContent().getContent(), draft.content, true);
			ArrayList<DiffModel> diffList = new ArrayList<DiffModel>();
			if(list != null){
				for (DeltaObject delta : list) {
					DiffModel diff = new DiffModel(delta.type,delta.content);
					diffList.add(diff);
				}
			}
			model.revs = diffList;
			
		} catch (DiffException e) {
			log.error("Conflict diff exception:" , e);
			model.errorCode = ErrorCode.DIFF_FAILED;
		} catch (SpaceNotFoundException e) {
			log.error("Conflict diff can not get original page:" , e);
			model.errorCode = ErrorCode.DIFF_ORIG_PAGE_NOT_FOUND;
		}
		
		return model;
	}

	public PageItemListModel getUserPagesInSpace(String spaceUname, String username, int returnNum) {

		PageItemListModel model = new PageItemListModel();
		User viewer = WikiUtil.getUser(userReadingService);
		List<Page> pages;
		if(StringUtils.isBlank(spaceUname)){
			pages = pageService.getUserAllContributedPages(username, returnNum, viewer);
		}else{
			pages = pageService.getUserUpdatedPagesInSpace(spaceUname, username, returnNum, viewer);
		}
		model.itemList  = copyPageItem(pages);
		User user = userReadingService.getUserByName(username);
		if(user != null){
			model.userStatus = StringUtils.abbreviate(user.getSetting().getStatus(),50);
			model.userPortrait = UserUtil.getPortraitUrl(user.getPortrait());
			if(user.isAnonymous() || user.equals(WikiUtil.getUser()) || WikiUtil.getUser().isAnonymous()){
				model.isFollowing = -1;
			}else{
				model.isFollowing = userReadingService.isFollowing(WikiUtil.getUser(), user)?1:0;
			}
		}
		
		return model;
	}

	public PageModel getCustomizedPage(String customizedPageID, String[] params) {
		Page page = null;
		PageModel model = new PageModel();
		List<PageModel> navList = new ArrayList<PageModel>();
		PageModel nav = new PageModel();
		
		if(StringUtils.equals(customizedPageID, SharedConstants.CPAGE_TAG_CLOUD)){
			String spaceUname;
			if(params == null || params.length  == 0){
				//this normally happens when user is on space tag cloud page, and refresh page...
				spaceUname = SharedConstants.SYSTEM_SPACEUNAME;
			}else{
				spaceUname = params[0];
			}
			String spaceTitle = getSpaceTitle(spaceUname);
			page = createTagCloudPage(spaceUname);
			//for navbar display correct space title
			page.getSpace().setName(spaceTitle);

			nav.title = messageService.getMessage("tag.cloud");
			nav.spaceTitle = spaceTitle;
			if(!SharedConstants.SYSTEM_SPACEUNAME.equals(spaceUname)){
				nav.navToken = GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_TAG_CLOUD), spaceUname);
			}else{
				nav.navToken = GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_TAG_CLOUD));
			}
			navList.add(nav);
		}else if(StringUtils.equals(customizedPageID, SharedConstants.CPAGE_USER_PROFILE)){
			//params: user name - it is identical value of user
			String spaceUname = spaceService.getSystemSpace().getUnixName();
			String username = (params != null && params.length > 0)?params[0]:null;
			page = createUserProfilePage(spaceUname,username);
			nav.title = messageService.getMessage("user.profile");
			//navToken must do GwtUtils.escapeUrl() in server side!!! the reason is navToken may contain valid TOKEN such as $CPAGE etc.
			nav.navToken = GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_USER_PROFILE), params);
			navList.add(nav);
		}else if(StringUtils.equals(customizedPageID, SharedConstants.CPAGE_SEARCH_RESULT)){
			//params: search keyword
			String spaceUname = spaceService.getSystemSpace().getUnixName();
			String keyword = (params != null && params.length > 0)?params[0]:null;
			//TODO: error handle with no username
			page = createSearchResultPage(spaceUname, keyword);
			nav.title = messageService.getMessage("search.result");
			//navToken must do GwtUtils.escapeUrl() in server side!!! the reason is navToken may contain valid TOKEN such as $CPAGE etc.
			nav.navToken = GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_SEARCH_RESULT), params);
			navList.add(nav);
		}else if(StringUtils.equals(customizedPageID, SharedConstants.CPAGE_SPACEADMIN)){
			//need verify this user has spaceAdmin permission
			String spaceUname = (params != null && params.length > 0)?params[0]:null;
			securityDummy.checkSpaceAdmin(spaceUname);
			String spaceTitle = getSpaceTitle(spaceUname);
			page = createSpaceAdminPage(spaceUname);
			//for navbar display correct space title
			page.getSpace().setName(spaceTitle);
			
			nav.title = messageService.getMessage("space.admin");
			nav.spaceTitle = spaceTitle;
			nav.navToken = GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_SPACEADMIN), spaceUname);
			navList.add(nav);
		}else if(StringUtils.equals(customizedPageID, SharedConstants.CPAGE_SYSADMIN_NOTIFY)){
			String spaceUname = spaceService.getSystemSpace().getUnixName();
			page = createNotifySysAdminPage(spaceUname);
			nav.title = messageService.getMessage("send.msg.to.admin");
			nav.navToken = GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_SYSADMIN_NOTIFY));
			navList.add(nav);
		}else if(StringUtils.equals(customizedPageID, SharedConstants.CPAGE_TEMPLATE_LIST)){
			String spaceUname = (params != null && params.length > 0)?params[0]:null; 
			String spaceTitle = getSpaceTitle(spaceUname);
			page = createTemplateListPage(spaceUname);
			page.getSpace().setName(spaceTitle);
			nav.title = messageService.getMessage("template.list");
			nav.spaceTitle = spaceTitle;
			nav.navToken = GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_TEMPLATE_LIST),spaceUname);
			navList.add(nav);
		}else{
//			TODO
			//failure tolerance
//			model.spaceUname = spaceUname;
//			model.title = messageService.getMessage("undefined")+"-" + spaceUname;
//			page = createPageNotFound(model);
//			//make it read only
//			AbstractContent content = page.getContent();
//			content.setContent(messageService.getMessage(WikiConstants.I18N_PAGE_NOT_FOUND_CONTENT_READONLY));
//			List<RenderPiece> pieces = page.getRenderPieces();
//			pieces.clear();
//			pieces.add(new TextModel(content.getContent()));
		}
		
//		setUserMarkOnModel(model, user, page);
		PageUtil.copyPageToModel(page, model, userReadingService, COPY_ATTACHMENT_WITHOUT_DRAFT);
		
		//reset nav
		if(navList.size() > 0)
			model.ancenstorList.addAll(navList);
		return model;
	}


	public String updateAttachmentMeta(String spaceUname,  String pageUuid, String nodeUuid, String name, String desc) {
		try {
			pageService.updateAttachmentMetaData(spaceUname, pageUuid, nodeUuid, name, desc);
		} catch (RepositoryException e) {
			log.error("update attachment " + nodeUuid + " failed." , e);
			return ErrorCode.ATT_METADATA_UPDATE_FAILED;
		}
		return null;
	}

	
	public String restoreCheck(String pageUuid) {
		return pageService.restorePageCheck(pageUuid);
	}

	
	public PageModel restorePage(String spaceUname, String pageUuid, boolean homepage, boolean withHistory) {
		PageModel model = new PageModel();
		try {
			Page page = pageService.restorePage(spaceUname, pageUuid,homepage, withHistory);
			model.title = page.getTitle();
			
			activityLog.logPageRestored(page);
		} catch (PageException e) {
			log.error("Restore page failed for " + pageUuid + " with history " + withHistory + " on space " + spaceUname,e);
			model.errorCode = ErrorCode.PAGE_RESTORE_ERROR;
		}
		return model;
	}

	//JDK1.6 @Override
	public PageModel restoreHistory(CaptchaCodeModel captcha, String spaceUname, String currPageTitle, int version) {
		if(!WikiUtil.captchaValid(captchaService, captcha))
			return null;
		
		PageModel model = new PageModel();
		try {
			Page originalCurrPage = pageService.getCurrentPageByTitle(spaceUname, currPageTitle);
			if(originalCurrPage == null)
				throw new PageException("Page by  title " + currPageTitle + " on space " + spaceUname + " does not exist.");
			
			//use pageUuid as input parameter rather than pageTitle is for security check reason.
			Page page = pageService.restoreHistory(spaceUname, originalCurrPage.getPageUuid(),version);
			try {
				page.setAttachments(pageService.getPageAttachment(spaceUname, page.getPageUuid(),true,false, null));
			} catch (RepositoryException e) {
				log.error("Failed get page node in repository " + page.getPageUuid(),e);
			}
			buildSidebar(page);
			buildSpaceMenu(page);
			
			User user = WikiUtil.getUser(userReadingService);
			setUserMarkOnModel(model, user, page);
			PageUtil.copyPageToModel(page, model, userReadingService, COPY_ATTACHMENT_WITHOUT_DRAFT);
			
			//show history tab
			model.tabIndex = SharedConstants.TAB_TYPE_HISTORY;
			
			activityLog.logPageReverted(page,version);
		} catch (PageException e) {
			log.error("Restore history version failed for " + currPageTitle + " from version " + version + " on space " + spaceUname,e);
			model.errorCode = ErrorCode.HISTORY_RESTORE_ERROR;
		} catch (DuplicatedPageException e) {
			//so far, this error will never happen, as history title is replace by current page title, which won't be duplicated
			model.errorCode = ErrorCode.HISTORY_RESTORE_DUPLICATE_TITLE_ERROR;
		} catch (PageSaveTiemoutExcetpion e) {
			model.errorCode = ErrorCode.HISTORY_RESTORE_SAVE_TIME_OUT_ERROR;
		} catch (SpaceNotFoundException e) {
			log.error("Restore history version failed for " + currPageTitle + " from version " + version + " on space " + spaceUname,e);
			model.errorCode = ErrorCode.HISTORY_RESTORE_ERROR;
		}
		return model;
	}



	public PageModel switchEditor( String spaceUname, String pageUuid, boolean newEditorIsRich, String text, String[] visibleAttsNodeUuid) {
		//update this user default editor setting
		User user = WikiUtil.getUser(userReadingService);
		UserSetting setting = user.getSetting();
		setting.setUsingRichEditor(newEditorIsRich);
		settingService.saveOrUpdateUserSetting(user, setting);
		
		//switch text from wiki markup(html) to html(wiki markup)
		PageModel model = new PageModel();
		if(newEditorIsRich){
			//original editor is plain, then switch text to HTML
			List<RenderPiece> pieces = renderService.renderHTML(RenderContext.RENDER_TARGET_RICH_EDITOR, spaceUname,pageUuid,text, visibleAttsNodeUuid);
			model.content = renderService.renderRichHTML(spaceUname, pageUuid, pieces); 
			model.isRichContent = true;
		}else{
			model.content = renderService.renderHTMLtoMarkup(spaceUname,text);
			model.isRichContent = false;
		}
			
		return model;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// side bar 
	//JDK1.6 @Override
	public PageModel editPageSidebar(String spaceUname, String pageUuid) {
		securityDummy.checkSpaceAdmin(spaceUname);
		
		PageModel model = new PageModel();
		try {
			Page page = pageService.getCurrentPageByUuid(pageUuid);
			if (page == null){
				log.error("Error in finding current page by space Uname:" + spaceUname + " and page Uuid:" + pageUuid);
				throw new PageException("Page not found by pageUuid: " + pageUuid);
			}
			//!!!these 3 can not be disorder!!!
			String sidemarkup = buildSidebar(RenderContext.RENDER_TARGET_RICH_EDITOR, page);
			PageUtil.copyPageToModel(page, model, userReadingService, NOT_COPY_ATTACHMENT);
			//always show sidebar whatever user hide or show sidebar - actually, use cannot edit side if sidebar is hidden. 
			//So here is just double confirm.
			model.pinPanel =  model.pinPanel |SharedConstants.TAB_TYPE_RIGHT_SIDEBAR;
			model.content = sidemarkup;
			
			//current is RichEditor request, then replace PageModel.content by HTML text rather than wiki markup
			User user = WikiUtil.getUser(userReadingService);
			model.isRichContent = WikiUtil.isUsingRichEditor(user);
			if(model.isRichContent){
				model.content = renderService.renderRichHTML(spaceUname, pageUuid, page.getSidebarRenderPieces());
			}
			
			model.pageThemeType = SharedConstants.SIDEBAR_TYPE_DEFAULT;
			Theme theme = themeService.getPageTheme(page, WikiUtil.isHomepage(page)?PageTheme.SCOPE_HOME:PageTheme.SCOPE_DEFAULT);
			if(theme != null && theme.getPageThemeByScope(pageUuid) != null){
				model.pageThemeType = SharedConstants.SIDEBAR_TYPE_CURRENT;
			}else if(WikiUtil.isHomepage(page)){
				model.pageThemeType = SharedConstants.SIDEBAR_TYPE_HOME;
			}
		} catch (Exception e) {
			log.error("Get Page with error:" , e);
			model.errorCode = ErrorCode.PAGE_GET_ERROR;
		}
		return model;
	}


	//JDK1.6 @Override
	public PageModel previewSidebar(String spaceUname, String pageUuid, String text, boolean isRichContent) {
		securityDummy.checkSpaceAdmin(spaceUname);
		
		if(isRichContent){
			text = renderService.renderHTMLtoMarkup(spaceUname,text);
		}
		PageModel model = new PageModel();
		try {
			Page page = pageService.getCurrentPageByUuid(pageUuid);
			if (page == null){
				log.error("Error in finding current page by page Uuid:" + pageUuid);
				throw new PageException("Page not found by pageUuid: " + pageUuid);
			}
			page.setSidebarRenderPieces(renderService.renderHTML(text));
			//as text may contain PageInfo macro, which need page author, modifier information etc. so 
			//I have to read out page and copy it to PageModel
			PageUtil.copyPageToModel(page, model, userReadingService, NOT_COPY_ATTACHMENT);
			
		} catch (Exception e) {
			log.error("Get Page with error:" , e);
			model.errorCode = ErrorCode.PAGE_GET_ERROR;
		}
		return model;
	}

	
	//JDK1.6 @Override
	public PageModel saveSidebar(String spaceUname, String pageUuid, String type, String text, boolean isRichContent) {
		securityDummy.checkSpaceWrite(spaceUname);
		
		if(isRichContent){
			text = renderService.renderHTMLtoMarkup(spaceUname,text);
		}
		
		Space space = spaceService.getSpaceByUname(spaceUname);
		Theme theme = themeService.getSpaceTheme(space);
		PageTheme pTheme = null;
		
		if(SharedConstants.SIDEBAR_TYPE_DEFAULT.equals(type)){
			//only update theme object default sidebar
			pTheme = theme.getPageThemeByScope(PageTheme.SCOPE_DEFAULT);
			pTheme.setSidebarMarkup(text);
			
		}else if(SharedConstants.SIDEBAR_TYPE_HOME.equals(type)){
			//only update home page theme
			pTheme = theme.getPageThemeByScope(PageTheme.SCOPE_HOME);
			if(pTheme == null){
				pTheme = new PageTheme();
				pTheme.setScope(PageTheme.SCOPE_HOME);
			}
			pTheme.setSidebarMarkup(text);
		}else if(SharedConstants.SIDEBAR_TYPE_CURRENT.equals(type)){
			pTheme = theme.getPageThemeByScope(pageUuid);
			if(pTheme == null){
				//also need keep its other original value, like welcome message etc. 
				if(StringUtils.equals(pageUuid,space.getHomepage() != null?space.getHomepage().getPageUuid():null)){
					pTheme = theme.getPageThemeByScope(PageTheme.SCOPE_HOME);
				}else{
					pTheme = theme.getPageThemeByScope(PageTheme.SCOPE_DEFAULT);
				}
			}
			if(pTheme == null){
				pTheme = new PageTheme();
			}
			pTheme.setScope(pageUuid);
			pTheme.setSidebarMarkup(text);
		}
		
		//update all - not implemented
		//if(SharedConstants.SIDEBAR_TYPE_ALL.equals(type)){
		
		if(pTheme != null){
			try {
				themeService.saveOrUpdatePageTheme(space,pTheme);
			} catch (ThemeSaveException e) {
				log.error("Unable save theme object for space " + spaceUname,e);
			}
		}
		//view page after saving side bar
		Page page = pageService.getCurrentPageByUuid(pageUuid);
		return viewPage(spaceUname,page.getTitle(),null);
	}


	//JDK1.6 @Override
	public PageItemListModel getPageChildren(String spaceUname, String pageUuid) {
		
		PageItemListModel model = new PageItemListModel();
		List<Page> pages = pageService.getPageChildren(pageUuid);
		model.itemList  = copyPageItem(pages);
		return model;
	}


	//JDK1.6 @Override
	public Boolean exist(String spaceUname, String pageTitle) {
		return pageService.existCurrentPageByTitle(spaceUname, pageTitle);
	}

	//JDK1.6 @Override
	public String export(String spaceUname, String pageUuid, int type) {
		String pageTitle = null;
		if(!StringUtils.isBlank(pageUuid)){
			Page page = pageService.getCurrentPageByUuid(pageUuid);
			if(page != null)
				pageTitle = page.getTitle();
		}
		try {
			File export;
			if(type == SharedConstants.EXPORT_TYPE_PDF){
				export = exportService.exportPDF(spaceUname, pageTitle);
			}else{
				export = exportService.exportHTML(spaceUname, pageTitle);
			}
			return WebUtil.getWebConext()+"download?export="+export.getName();
		} catch (ExportException e) {
			log.error("Export failed",e);
			return null;
		}
	}
	
	public PageModel renderPagePiecePhase(String spaceUname, String pageTitle, String pieceName) {
		//!!!IMPORTATN: the following code has similar in INcludeMacro.getTemplValues() - I am not quite sure worth to merge them...
		
		//security check first - here does not use method default security is,  it is not necessary to 
		//redirect user to login page if only included page has not reading permission
		PageModel model = new PageModel();
		Page page = null;
		try {
			page = pageService.getCurrentPageByTitleWithoutSecurity(spaceUname, pageTitle, false);
		} catch (SpaceNotFoundException e) {
			log.error("Space not found for render page phase",e);
		}
		
		if(page == null){
			model.errorCode = ErrorCode.PAGE_PHASE_RENDER_NO_PAGE;
			return model;
		}
			
		if(!securityService.isAllowPageReading(spaceUname, page.getPageUuid(), WikiUtil.getUser(userReadingService))){
			model.errorCode = ErrorCode.PAGE_PHASE_RENDER_NO_ALLOW;
			return model;
		}
		
		String content = null;
		if(StringUtils.isBlank(pieceName)){
			content = page.getContent().getContent();
		}else{
			content = RenderUtil.getPiece(page.getContent().getContent(),pieceName);
			page.setPhaseContent(content);
			if(content == null){
				model.errorCode = ErrorCode.PAGE_PHASE_RENDER_NO_PHASE;
			}
		}
		if(!StringUtils.isBlank(model.errorCode) || content == null){
			//content == null?? failure tolerance
			//has some error 
			return model;
		}
		
		//TODO: how to process whole page content but with PageAttribute macro? or similar macro??? 
		page.setRenderPieces(renderService.renderHTML(page));
		PageUtil.copyPageToModel(page, model, userReadingService, NOT_COPY_ATTACHMENT);
		
		return model;
	}
	
	public TextModel requestMacroRenderInEditor(String spaceUname, String pageUuid, String macro,  String currentContent, String[] visibleAttsNode){
		TextModel model = new TextModel();
		List<RenderPiece> pieces;
		if(!StringUtils.isBlank(currentContent)){
			currentContent = renderService.renderHTMLtoMarkup(spaceUname,currentContent);
			String sep = "\n"+ WikiUtil.findUniqueKey(currentContent)+ "\n";
			StringBuffer buf = new StringBuffer(macro).append(sep).append(currentContent);
			pieces = renderService.renderHTML(RenderContext.RENDER_TARGET_RICH_EDITOR, spaceUname, pageUuid, buf.toString(), visibleAttsNode);
			String content = renderService.renderRichHTML(spaceUname, pageUuid, pieces);
			
			int sepIdx = content.indexOf(sep);
			if( sepIdx != -1){
				model.setText(content.substring(0,sepIdx));
				return model;
			}else{
				AuditLogger.error("The macro " + macro +" render in  richeditor is failed:" + currentContent);
				//continue and only render macro itself without input content
			}
		}
		
		//render the macro
		pieces = renderService.renderHTML(RenderContext.RENDER_TARGET_RICH_EDITOR, spaceUname, pageUuid, macro, visibleAttsNode);
		model.setText(renderService.renderRichHTML(spaceUname, pageUuid, pieces));
		return model;
	}
	public Integer[] getPageTabCount(String spaceUname, String pageUuid, boolean allowVisibleOnComment,
			boolean allowVisibleOnChildren) {
		Integer[] counts = {-1,-1};
		if(allowVisibleOnComment){
			try {
				counts[0] = commentService.getPageCommentCount(spaceUname,pageUuid);
			} catch (Exception e) {
				log.error("Unable get page comment count. sapce " + spaceUname + " uuid " + pageUuid,e);
				
			}
		}
		if(allowVisibleOnChildren){
			try {
				counts[1] = pageService.getPageChildrenCount(pageUuid);
		} catch (Exception e) {
			log.error("Unable get page children count. sapce " + spaceUname + " uuid " + pageUuid,e);
			
		}
		}
		return counts;
	}


	// ********************************************************************
	// private method
	// ********************************************************************
	/**
	 * Try to return current page, or its parent, then to home
	 * @param pageUuid
	 * @param parentPageUuid
	 * @return
	 */
	private String getReturnPageTitle(String pageUuid, String parentPageUuid) {
		Page page = null; 
		if(!StringUtils.isBlank(pageUuid)){
			//try to return itself
			page = pageService.getCurrentPageByUuid(pageUuid);
		}
		if(page == null && !StringUtils.isBlank(parentPageUuid)){
			//current page not exist, then try to return its parent
			page = pageService.getCurrentPageByUuid(parentPageUuid);
		}
		if(page != null)
			return page.getTitle();
		
		//return home page
		return "";
	}
	private ArrayList<PageItemModel> copyPageItem(List<Page> list) {
		ArrayList<PageItemModel> pageList = new ArrayList<PageItemModel>();
		if(list != null){
			for (Page page : list) {
				PageItemModel history = PageUtil.copyToPageItem(page);
				pageList.add(history);
			}
		}
		return pageList;
	}
	private ArrayList<PageItemModel> copyHistoryItem(List<History> list) {
		ArrayList<PageItemModel> historyList = new ArrayList<PageItemModel>();
		if(list != null){
			for (History page : list) {
				PageItemModel history = PageUtil.copyToPageItem(page);
				historyList.add(history);
			}
		}
		return historyList;
	}
	/**
	 * Set PageModel favourite/watch mark according to current login user and given page
	 */
	private User setUserMarkOnModel(PageModel model, User user, Page page) {
		
		// initial:not allow mark
		model.favorite = -1;
		model.watched = -1;
		model.pintop = -1;
		if (!user.isAnonymous() && page.getUid() != null) {
			// user is login user, allow mark, and initial value is not
			// marked
			model.favorite = 0;
			model.watched = 0;
			model.pintop = 0;
			List<UserPageMark> userMarks = pageService.getPageMarks(user, page);
			if (userMarks != null) {
				for (UserPageMark mark : userMarks) {
					if (mark.getType() == SharedConstants.USER_PAGE_TYPE_FAVORITE) {
						model.favorite = 1;
					} else if (mark.getType() == SharedConstants.USER_PAGE_TYPE_WATCH) {
						model.watched = 1;
					} else if (mark.getType() == SharedConstants.USER_PAGE_TYPE_PINTOP) {
						model.pintop = 1;
					}
				}
			}
		}
		return user;
	}

	private Page createTagCloudPage(String spaceUname) {
		Page page = new Page();
		Space space  = new Space();
		space.setUnixName(spaceUname);
		page.setSpace(space);
		
		page.setUid(SharedConstants.CPAGE_TAG_CLOUD_UID);
		
		
		if(SharedConstants.SYSTEM_SPACEUNAME.equals(spaceUname)){
			page.setTitle(messageService.getMessage(WikiConstants.I18N_SPACE_TAGCLOUD_TITLE));
		}else{
			page.setTitle(messageService.getMessage(WikiConstants.I18N_TAGCLOUD_TITLE,new String[] {spaceUname}));
		}
		PageContent content = new PageContent();
		content.setContent("{tagcloud}");
		page.setContent(content);
		// this page does not allow edit!
		page.setAttribute(PageAttribute.FUNCTION_PAGE);
		// just put pageUuid as null, which is useful when page has image,
		// current page won't have any image, so it is safe.
		renderService.renderHTML(page);
		return page;
	}
	private Page createTemplateListPage(String spaceUname) {
		Page page = new Page();
		Space space  = new Space();
		space.setUnixName(spaceUname);
		page.setSpace(space);
		
		page.setUid(SharedConstants.CPAGE_TEMPLATE_LIST_UID);
		
		page.setTitle(messageService.getMessage("template.list"));
		PageContent content = new PageContent();
		
		//system admin to view all template if spaceUname is blank
		String param = ""; //so far, spaceUname is decide by client side; StringUtils.isBlank(spaceUname)?"":(":space=" + EscapeUtil.escapeMacroParam(spaceUname)); 
		content.setContent("{templatelist"+ param + "}");
		
		page.setContent(content);
		// this page does not allow edit!
		page.setAttribute(PageAttribute.FUNCTION_PAGE);
		// just put pageUuid as null, which is useful when page has image,
		// current page won't have any image, so it is safe.
		renderService.renderHTML(page);
		return page;
	}
	/**
	 * @param username
	 * @param spaceUname 
	 * @return
	 */
	private Page createUserProfilePage(String spaceUname, String username) {
		Page page = new Page();
		//set system spaceUname, this make navBar display correctly
		Space space  = new Space();
		space.setUnixName(spaceUname);
		page.setSpace(space);
		
		page.setUid(SharedConstants.CPAGE_USER_PROFILE_UID);
		page.setTitle(messageService.getMessage(WikiConstants.I18N_USERPROFILE_TITLE,new String[] {username}));
		PageContent content = new PageContent();
		content.setContent("{userprofile:"+NameConstants.USERNAME + "="+EscapeUtil.escapeMacroParam(username)+"}");
		page.setContent(content);
		// this page does not allow edit!
		page.setAttribute(PageAttribute.FUNCTION_PAGE);
		// just put pageUuid as null, which is useful when page has image,
		// current page won't have any image, so it is safe.
		renderService.renderHTML(page);
		return page;
	}
	private Page createSearchResultPage(String spaceUname, String keyword) {
		Page page = new Page();
		//set system spaceUname, this make navBar display correctly
		Space space  = new Space();
		space.setUnixName(spaceUname);
		page.setSpace(space);
		
		page.setUid(SharedConstants.CPAGE_SEARCH_RESULT_UID);
		page.setTitle(messageService.getMessage(WikiConstants.I18N_SEARCH_RESULT_TITLE,new String[] {keyword}));
		PageContent content = new PageContent();
		content.setContent("{search:"+NameConstants.KEYWORD+"="+EscapeUtil.escapeMacroParam(keyword)+MacroParameter.SEP+"return="+SharedConstants.PAGE_SIZE+"}");
		page.setContent(content);
		// this page does not allow edit!
		page.setAttribute(PageAttribute.FUNCTION_PAGE);
		// just put pageUuid as null, which is useful when page has image,
		// current page won't have any image, so it is safe.
		renderService.renderHTML(page);
		return page;
	}

	/**
	 * @param spaceUname
	 * @param params
	 * @return
	 */
	private Page createSpaceAdminPage(String spaceUname) {
		Page page = new Page();
		//does it really need space information?
		Space space  = new Space();
		space.setUnixName(spaceUname);
		page.setSpace(space);
		
		page.setUid(SharedConstants.CPAGE_SPACEADMIN_UID);
		page.setTitle(messageService.getMessage(WikiConstants.I18N_SPACE_ADMIN_PAGE_TITLE,new String[] {spaceUname}));
		PageContent content = new PageContent();
		page.setContent(content);
		content.setContent("{macro:name="+SharedConstants.MACRO_SPACE_ADMIN+"}");
		// this page does not allow edit!
		page.setAttribute(PageAttribute.FUNCTION_PAGE);
		// just put pageUuid as null, which is useful when page has image,
		// current page won't have any image, so it is safe.
		renderService.renderHTML(page);
		return page;
	}
	
	private Page createNotifySysAdminPage(String spaceUname) {
		Page page = new Page();
		Space space = new Space();
		//set system spaceUname, this make navBar display correctly
		space.setUnixName(spaceUname);
		page.setSpace(space);
		
		page.setUid(SharedConstants.CPAGE_SYSADMIN_NOTIFY_UID);
		page.setTitle(messageService.getMessage(WikiConstants.I18N_NOTIFY_SYSADMIN));
		PageContent content = new PageContent();
		content.setContent("{macro:name="+SharedConstants.MACRO_NOTIFY_SYSADMIN+"}");
		page.setContent(content);
		// this page does not allow edit!
		page.setAttribute(PageAttribute.FUNCTION_PAGE);
		// just put pageUuid as null, which is useful when page has image,
		// current page won't have any image, so it is safe.
		renderService.renderHTML(page);
		return page;
	}
	

	private Page createPageNotFound(PageModel model) {

		Page page = new Page();
		//always create new page
		String homeCreate = LinkModel.LINK_TO_CREATE_FLAG+":";
		if (StringUtils.isBlank(model.title)) {
			// it would be home page if there is no title
			homeCreate = LinkModel.LINK_TO_CREATE_HOME_FLAG + ":";
			model.title = messageService.getMessage(WikiConstants.I18N_HOMEPAGE_NOT_FOUND_TITLE);
		}
			
		page.setTitle(model.title);

		//for rendering
		Space space = new Space();
		space.setUnixName(model.spaceUname);
		//this is for navbar display
		space.setName(getSpaceTitle(model.spaceUname));
		page.setSpace(space);

		PageContent content = new PageContent();
		content.setContent(messageService.getMessage(WikiConstants.I18N_PAGE_NOT_FOUND_CONTENT,
				new String[] { homeCreate + page.getTitle()}));
		page.setContent(content);
		// this page does not allow edit!
		page.setAttribute(PageAttribute.FUNCTION_PAGE);
		// just put pageUuid as null, which is useful when page has image,
		// current page won't have any image, so it is safe.
		renderService.renderHTML(page);

		// check if user has write permission on this space
		boolean readonly = true;
		List<WikiOPERATIONS> opers = page.getWikiOperations();
		for (WikiOPERATIONS wikiOPERATIONS : opers) {
			if (WikiOPERATIONS.PAGE_WRITE.equals(wikiOPERATIONS)) {
				readonly = false;
				break;
			}
		}
		if (readonly) {
			content.setContent(messageService.getMessage(WikiConstants.I18N_PAGE_NOT_FOUND_CONTENT_READONLY));
			List<RenderPiece> pieces = page.getRenderPieces();
			pieces.clear();
			pieces.add(new TextModel(content.getContent()));
		}
		return page;
	}
	private void buildSpaceMenu(AbstractPage page) {
		if(page == null)
			return;
		if(!GwtUtils.contains(page.getAttribute(), PageAttribute.NO_SPACE_MENU)){
			Theme theme = themeService.getPageTheme(page, WikiUtil.isHomepage(page)?PageTheme.SCOPE_HOME:PageTheme.SCOPE_DEFAULT);
			if(theme != null){
				String spaceMenuMarkup = theme.getCurrentPageTheme().getSpaceMenuMarkup();
				if(!StringUtils.isEmpty(spaceMenuMarkup)){
					try {
						//Keep necessary information: spaceUname, pageUuid, creator and modifier information etc
						 //Clone: to avoid any PO and page content render impact...
						AbstractPage renderPage = (AbstractPage) page.clone();
						renderPage.setPhaseContent(spaceMenuMarkup);
						page.setSpaceMenuPieces(renderService.renderHTML(renderPage));
					} catch (CloneNotSupportedException e) {
						log.error("Side bar render failed",e);
					}
				}
			}
		}
	}
	private String buildSidebar(AbstractPage page) {
		return buildSidebar(RenderContext.RENDER_TARGET_PAGE, page);
	}
	private String buildSidebar(String renderTarget, AbstractPage page) {
		String sidebarMarkup = "";
		if(page == null)
			return sidebarMarkup;
		
		if(!GwtUtils.contains(page.getAttribute(), PageAttribute.NO_SIDE_BAR)){
			//retrieve page side bar, then render theme->right side bar
			Theme theme = themeService.getPageTheme(page, WikiUtil.isHomepage(page)?PageTheme.SCOPE_HOME:PageTheme.SCOPE_DEFAULT);
			if(theme != null){
				sidebarMarkup = theme.getCurrentPageTheme().getSidebarMarkup();
				if(!StringUtils.isEmpty(sidebarMarkup)){
					try {
						//Keep necessary information: spaceUname, pageUuid, creator and modifier information etc
						 //Clone: to avoid any PO and page content render impact...
						AbstractPage renderPage = (AbstractPage) page.clone();
						renderPage.setPhaseContent(sidebarMarkup);
						page.setSidebarRenderPieces(renderService.renderHTML(renderTarget, renderPage));
					} catch (CloneNotSupportedException e) {
						log.error("Side bar render failed",e);
					}
				}
			}
		}
		return sidebarMarkup;
	}

	/**
	 * @param spaceUname
	 * @return
	 */
	private String getSpaceTitle(String spaceUname) {
		Space space = spaceService.getSpaceByUname(spaceUname);
		if(space != null)
			return space.getName();
		
		//???if space not exist, return spaceUname???
		return spaceUname;
	}
	// ********************************************************************
	// Set / Get
	// ********************************************************************
	public void setPageService(PageService pageService) {
		this.pageService = pageService;
	}

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}


	public void setThemeService(ThemeService themeService) {
		this.themeService = themeService;
	}

	public void setDiffService(DiffService diffService) {
		this.diffService = diffService;
	}

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public void setCaptchaService(CaptchaServiceProxy captchaService) {
		this.captchaService = captchaService;
	}

	public void setRenderService(RenderService renderService) {
		this.renderService = renderService;
	}

	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}

	public void setExportService(ExportService exportService) {
		this.exportService = exportService;
	}

	/**
	 * @param securityService the securityService to set
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	/**
	 * @param securityDummy the securityDummy to set
	 */
	public void setSecurityDummy(SecurityDummy securityDummy) {
		this.securityDummy = securityDummy;
	}

	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	public void setActivityLog(ActivityLogService activityLog) {
		this.activityLog = activityLog;
	}

}
