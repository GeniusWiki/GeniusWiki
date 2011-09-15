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
package com.edgenius.wiki.gwt.client.offline.controller;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.html.RenderPieceParser;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.model.CaptchaCodeModel;
import com.edgenius.wiki.gwt.client.model.DiffListModel;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.PageThemeModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.model.TreeItemListModel;
import com.edgenius.wiki.gwt.client.model.TreeItemModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.offline.GearsDB;
import com.edgenius.wiki.gwt.client.offline.GearsServer;
import com.edgenius.wiki.gwt.client.offline.OfflineUtil;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.CascadeComparator;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.PageAttribute;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.google.gwt.gears.client.GearsException;
import com.google.gwt.gears.client.database.DatabaseException;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Dapeng.Ni
 */
public class PageOfflineControllerImpl extends AbstractOfflineControllerImpl implements PageControllerAsync{

	private Map<String, List<PageModel>> pageTree = new HashMap<String,  List<PageModel>>();

	public void viewPage(String spaceUname,String  pageTitle, String commentUid, AsyncCallback<PageModel> callback) {
		PageModel model = new PageModel();
		try {
			UserModel user = OfflineUtil.getUser();
			Log.info("viewing page " + pageTitle + " on space " + spaceUname);
			
			if(pageTitle == null || pageTitle.trim().length() == 0){
				model = GearsDB.getUserDB(user.getUid()).getHomepage(spaceUname);
			}else{
				model = GearsDB.getUserDB(user.getUid()).getPageByTitle(spaceUname,pageTitle);
			}
			
			if(model == null){
				model = new PageModel();
				model.title = Msg.consts.page_not_found();
				model.renderContent = new ArrayList<RenderPiece>();
				model.renderContent.add(new TextModel(Msg.consts.page_not_found()));
				
				//set nav bar info
				PageModel nav = new PageModel();
				nav.title = Msg.consts.page_not_found();
				nav.navToken = GwtUtils.getSpacePageToken(spaceUname, pageTitle);
				model.ancenstorList.add(nav);
				
				model.attribute = PageAttribute.NO_CREATOR|PageAttribute.NO_MODIFIER|PageAttribute.NO_ATTACHMENT|PageAttribute.NO_HISTORY;
			}else{
				//render page RichHTMLTag content to RenderPiece 
				model.renderContent = RenderPieceParser.parse(model.content,false);
				buildAncestors(model);
				
			}
			
			buildSidebar(GearsDB.getUserDB(user.getUid()),model);
			
			//Retrieve page attachments
			refreshPageAttachments(model, user);

			//reset page permission according to offline requirement
			//these features will keep original value
//			model.permissions[ClientConstants.COMMENT_READ] = 0;
//			model.permissions[ClientConstants.READ]
//			model.permissions[ClientConstants.WRITE] = 0;

			//screen following feature in offline model: different with History-> here does not block "NO_TAG"
			model.attribute = model.attribute | PageAttribute.OFFLINE_PAGE;
			model.permissions[ClientConstants.REMOVE] = 0;
			//comment perm will final set in CommentOfflineControllerImpl.
			model.permissions[ClientConstants.COMMENT_WRITE] = 0;
			model.permissions[ClientConstants.OFFLINE] = 0;
			model.permissions[ClientConstants.RESTRICT] = 0;
			model.permissions[SharedConstants.PERM_SPACE_BASE+ClientConstants.EXPORT] = 0;
			model.permissions[SharedConstants.PERM_SPACE_BASE+ClientConstants.ADMIN] = 0;
			model.permissions[SharedConstants.PERM_INSTNACE_MGM] = 0;
		} catch (GearsException e) {
			Log.error("Unable to get page " + spaceUname + " : "  + pageTitle,e);
			model.errorCode = ErrorCode.PAGE_GET_ERROR;
		}
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);
	}

	public void getEmptyPageDefaultContent(String pageUuid, AsyncCallback<PageModel> callback) {
		// TODO Auto-generated method stub
		
	}
	
	public void getPageHistory(String spaceUname, String pageUuid, int startVer, int returnCount, AsyncCallback<PageItemListModel> callback) {
		PageItemListModel model = new PageItemListModel();
		try {
			UserModel user = OfflineUtil.getUser();
			ArrayList<PageItemModel> list = GearsDB.getUserDB(user.getUid()).getPageHistory(pageUuid);
			model.itemList = list;
			
			//NOTE: offline model doesn't use pagination
			
			//we also need put current page into history list as first element
			PageModel page = GearsDB.getUserDB(user.getUid()).getCurrentPageByUuid(pageUuid);
			list.add(0, OfflineUtil.extractToItem(page));
		} catch (GearsException e) {
			Log.error("Unable to get history list for page " + spaceUname + " : " + pageUuid, e);
			model.errorCode = ErrorCode.HISTORY_GET_ERROR;
		}
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);
	}


	
	public void getHistoryByUid(Integer uid, boolean refreshAttachment, AsyncCallback<PageModel> callback) {
		PageModel history = new PageModel();
		try {
			UserModel user = OfflineUtil.getUser();
			GearsDB userDB = GearsDB.getUserDB(user.getUid());
			history = userDB.getHistoryByUid(uid);
			
			if (history == null){
				Log.error("Null return for history by uid:" + uid);
				history = new PageModel();
				history.errorCode = ErrorCode.HISTORY_GET_ERROR;
			}else{
				PageModel page = userDB.getCurrentPageByUuid(history.pageUuid);
				if(page == null){
					Log.error("Null return for page by uuid:" + history.pageUuid + " for history uid:" +uid);
					history.errorCode = ErrorCode.HISTORY_GET_ERROR;
				}else{
					//render page RichHTMLTag content to RenderPiece 
					history.renderContent = RenderPieceParser.parse(history.content,false);
					buildAncestors(history);
					
					buildSidebar(userDB,history);
					
					history.attribute = PageAttribute.OFFLINE_HISTORY;
					history.currentTitle = page.title;

					history.isHistory = true;
					
					//screen following feature in offline model
					history.permissions[ClientConstants.REMOVE] = 0;
					history.permissions[ClientConstants.COMMENT_WRITE] = 0;
					history.permissions[ClientConstants.OFFLINE] = 0;
					history.permissions[ClientConstants.RESTRICT] = 0;
					history.permissions[SharedConstants.PERM_SPACE_BASE+ClientConstants.EXPORT] = 0;
					history.permissions[SharedConstants.PERM_SPACE_BASE+ClientConstants.ADMIN] = 0;
					history.permissions[SharedConstants.PERM_INSTNACE_MGM] = 0;
				}
			}
		}catch (GearsException e) {
			Log.error("Get History with error:" , e);
			history.errorCode = ErrorCode.HISTORY_GET_ERROR;
		}
		OfflineUtil.setLoginInfo(history);
		callback.onSuccess(history);
	}
	public void getUserPagesInSpace(String spaceUname, String username, int returnNum,AsyncCallback<PageItemListModel> callback) {
		
		PageItemListModel model = new PageItemListModel();
		
		try {
			UserModel viewer = OfflineUtil.getUser();
			GearsDB database = GearsDB.getUserDB(viewer.getUid());
			model.itemList = database.getAuthoredPagesInSpace(spaceUname, username, returnNum);
		} catch (GearsException e) {
			model.errorCode = ErrorCode.PAGE_GET_ERROR;
		}
		
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);
	}

	public void createPage(String spaceUname, String newPageTitle, String parentPageUuid, int type , AsyncCallback<PageModel> callback) {
		//this method does not check if page is exist. It is non-sense: only user saving page, 
		//it is useful to check page exist or not
		PageModel model = new PageModel();
		try {
			
			UserModel user = OfflineUtil.getUser();
			List<PageItemModel> drafts = GearsDB.getUserDB(user.getUid()).hasDraft(model.spaceUname,model.title,user);
			OfflineUtil.copyDraftStatus(drafts,model);
		} catch (Exception e) {
			Log.error("Get Page with error in create page pre check:" , e);
			model.errorCode = ErrorCode.PAGE_GET_ERROR;
		}
		
		//always is readModel in offline status
		model.isRichContent = true;
		
		//!!! bring back value
		model.title = newPageTitle;
		model.parentPageUuid = parentPageUuid;
		model.spaceUname = spaceUname;
		model.type = type;
		
		//temporarily using model.pageUuid for parentPageUuid as buildAncestors() required.
		String existPageUuid = model.pageUuid;
		model.pageUuid = parentPageUuid;
		buildAncestors(model);
		//restore
		model.pageUuid = existPageUuid;
		
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);
		
	}


	public void editPage(String spaceUname,String  pageUuid, AsyncCallback<PageModel> callback) {
		PageModel model = new PageModel();
		try {
			UserModel user = OfflineUtil.getUser();
			// don't use page uid as well: if user click refresh button during
			// edit, pageUid will be empty.
			model = GearsDB.getUserDB(user.getUid()).getCurrentPageByUuid(pageUuid);
			if(model == null){
				model = new PageModel();
				model.errorCode = ErrorCode.PAGE_GET_ERROR;
			}else{
				//render page RichHTMLTag content to RenderPiece 
				model.renderContent = RenderPieceParser.parse(model.content,true);		
				buildAncestors(model);
				
				//Retrieve page attachments
				refreshPageAttachments(model, user);
				
				//always is readModel in offline status
				//current is RichEditor request, then replace PageModel.content by HTML text rather than wiki markup

				List<PageItemModel> drafts = GearsDB.getUserDB(user.getUid()).hasDraft(spaceUname,model.title,user);
				OfflineUtil.copyDraftStatus(drafts,model);
			}
			//only hide right bar - page info. User still can turn on TreePanel left sidebar or keep it visible.
			model.attribute = model.attribute| PageAttribute.NO_SIDE_BAR;
		} catch (Exception e) {
			Log.error("Get Page with error:" , e);
			model.errorCode = ErrorCode.PAGE_GET_ERROR;
		}

		
		model.isRichContent = true;
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);
	}

	public void savePage(PageModel model, boolean forceSave, AsyncCallback<PageModel> callback) {

		PageModel pPage = new PageModel();
		try {
			UserModel user = OfflineUtil.getUser();
			GearsDB userDB = GearsDB.getUserDB(user.getUid());
			//do similar check with PageService.save():
			
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			//if page is new page, the need check its title conflict
			
			PageModel existPage = null;
			if(model.pageUuid != null)
				existPage = userDB.getCurrentPageByUuid(model.pageUuid);
			
			if(existPage != null){
				PageModel sameTitlePage = userDB.getPageByTitle(model.spaceUname, model.title);
				if(sameTitlePage != null && !sameTitlePage.pageUuid.equals(existPage.pageUuid)){
					Log.warn("Page has duplicate title page exist in same space " + existPage.title);
					model.errorCode = ErrorCode.PAGE_DUPLICATED_TITLE;
					throw new Exception(model.errorCode);
				}
				
				pPage = existPage;
				//this will update modified date
				setTouchedInfo(pPage, existPage,user);
				updateFromInput(pPage, model);
				// don't create history while offline edit page. it is complex to handle while upload, especial has version conflict
//				pPage.pageVersion++;
				//please note: this new page uses same PageUid with old one.
				
				//update exist page to history
//				userDB.updateToHistory(existPage);
			}else{
				pPage = model;
				setTouchedInfo(pPage, null ,user);
				//this will convert content, to RenderPiece, so it still need call even pPage=model
				updateFromInput(pPage, model);
				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				//get parent info
				if(!StringUtil.isBlank(model.parentPageUuid)){
					PageModel parentPage = userDB.getCurrentPageByUuid(model.parentPageUuid);
					if(parentPage != null){
						pPage.level = parentPage.level + 1;
					}else{
						Log.warn("page parent page does not exist. Page title is " 
								+ model.title + ". Parent page uuid is "+ model.parentPageUuid);
					}
				}else{
					//root page, such as home page
					pPage.level = 0;
				}
				
				//update version
				pPage.pageVersion = 1;
				pPage.pageUuid = OfflineUtil.createPageUuid(user);
				
				//according to space permission to decide page default permission. Only need: READ and WRITE,PAGE_COMMENT_READ
				SpaceModel space = userDB.getSpace(pPage.spaceUname);
				pPage.permissions = new int[SharedConstants.PAGE_PERM_SIZE];
				if(space != null){
					pPage.permissions[ClientConstants.READ] = space.permissions[ClientConstants.READ];
					pPage.permissions[ClientConstants.WRITE] = space.permissions[ClientConstants.WRITE];
					pPage.permissions[ClientConstants.COMMENT_READ] = space.permissions[ClientConstants.COMMENT_READ];
				}
			}
	
			
			userDB.saveOrUpdatePage(pPage,true);
			buildSidebar(userDB,pPage);
			
			//clean pageTree cache
			pageTree.remove(model.spaceUname);
			//must after clean cache - then reset page tree from database again
			//TODO: performance improve - put saved page into pageTree cache rather than read whole database
			buildAncestors(model);
			
			//refresh page attachment: savePage() method does not refresh it
			refreshPageAttachments(pPage, user);

			//delete auto and manual draft
			removeDraft(pPage.pageUuid,0);
		} catch (GearsException e) {
			Log.error("Page saved with error:" , e);
			model.errorCode = ErrorCode.PAGE_SAVE_ERROR;
		} catch (Exception e) {
			Log.error(e.getMessage());
			//offline does not handle following error
//			model.errorCode = ErrorCode.PAGE_VERSION_CONFLICT;
//			model.errorCode = ErrorCode.PAGE_SAVE_TIME_OUT;
		}
		
		OfflineUtil.setLoginInfo(pPage);
		callback.onSuccess(pPage);
	}


	public void saveAutoDraft(PageModel draft, AsyncCallback<PageModel> callback) {

		
		try {
			UserModel user = OfflineUtil.getUser();
			
			//anonymous does not allow save draft! This call must be from "Preview" action, so just return rendered content back
			//offline model, do nothing
			if(!GwtUtils.isAnonymous(user)){
				draft.type = SharedConstants.AUTO_DRAFT;
				saveDraft(draft, user);
			}
		} catch (Exception e) {
			Log.error("Page saved with error:" , e);
			draft.errorCode = ErrorCode.PAGE_SAVE_DRAFT_ERROR;
		}

	    //only hide right bar - page info. User still can turn on TreePanel left sidebar or keep it visible.
		draft.attribute = draft.attribute| PageAttribute.NO_SIDE_BAR; 

		OfflineUtil.setLoginInfo(draft);
		callback.onSuccess(draft);
		
	}


	
	public void saveManualDraft(PageModel draft, boolean exitToView,AsyncCallback<PageModel> callback) {


		try {
			UserModel user = OfflineUtil.getUser();
			//failure tolerance check, anonymous does not allow save draft. 
			if(!GwtUtils.isAnonymous(user)){
				draft.type = SharedConstants.MANUAL_DRAFT;
				//this return draft will have pageUuid
				draft = saveDraft(draft, user);
				
				//delete auto draft
				removeDraft(draft.pageUuid,SharedConstants.AUTO_DRAFT);

				//only hide right bar - page info. User still can turn on TreePanel left sidebar or keep it visible.
				draft.attribute = draft.attribute| PageAttribute.NO_SIDE_BAR; 

			}
			
			
			if(exitToView){
				// get page title from original one. For existed page, it simple return itself. For new creating,
				//it need return its original page.
				String title = getReturnPageTitle(draft.pageUuid,draft.parentPageUuid);
				viewPage(draft.spaceUname,title,null, callback);
				return;
			}
		} catch (Exception e) {
			Log.error("Page saved with error:" , e);
			draft.errorCode = ErrorCode.PAGE_SAVE_DRAFT_ERROR;
		}
		
		OfflineUtil.setLoginInfo(draft);
		callback.onSuccess(draft);
		
	}
	public void removeDraft(String spaceUname, String pageUuid, int type, AsyncCallback<PageModel> callback) {
		PageModel model = new PageModel();
		
		try {
			removeDraft(pageUuid, type);
			//so far only copy draft UUID and type, need modify if later need more value from page
			model.pageUuid = pageUuid;
			model.type = type;
		} catch (GearsException e) {
			Log.error("Unable delete draft " + pageUuid + " with type " + type);
		}
		
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);
	}
	


	public void editDraft(Integer draftUid, int draftType, boolean refreshAttachment,
			AsyncCallback<PageModel> callback) {
		
		PageModel model = new PageModel();
		try {
			UserModel user = OfflineUtil.getUser();
			GearsDB userDB = GearsDB.getUserDB(user.getUid());
			model = userDB.getDraft(draftUid,draftType);
			if (model == null){
				throw new Exception("Error in finding draft by uid:" + draftUid);
			}
			if (refreshAttachment) {
				// copy this user's draft attachment: call by resume editing
				// history page
				refreshPageAttachments(model, user);
			} else {
				// clean attachment info to keep original attachment in current
				// page
				model.attachmentJson = "";
			}

			//only hide right bar - page info. User still can turn on TreePanel left sidebar or keep it visible.
			model.attribute = model.attribute| PageAttribute.NO_SIDE_BAR;
		} catch (Exception e) {
			Log.error("Get Draft with error:" , e);
			model.errorCode = ErrorCode.PAGE_GET_ERROR;
		}
		
		model.isRichContent = true;
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);
	}

	
	public void cancelEdit(String pageUuid, String parentPageUuid, AsyncCallback<String> callback) {
		//return home page
		String title = getReturnPageTitle(pageUuid, parentPageUuid);
		
		callback.onSuccess(title);
	}


	public void removeAttachment(String spaceUname, String pageUuid, String nodeUuid, String nodeVersion,
			AsyncCallback<PageModel> callback) {
		
		try {
			UserModel user = OfflineUtil.getUser();
			List<AttachmentModel> deleted = GearsDB.getUserDB(user.getUid()).removeAttachment(pageUuid, nodeUuid,nodeVersion,false);
			
			SpaceModel space = GearsDB.getUserDB(user.getUid()).getSpace(spaceUname);
			if(space == null){
				Log.error("Fail to uncapture local files: unable to get space by " + spaceUname);
			}else{
				//remove from store
				for (AttachmentModel del : deleted) {
					GearsServer server = new GearsServer();
					int maxVer = Integer.parseInt(del.version);
					server.uncaptureFile(space.uid, spaceUname,del.filename, nodeUuid, maxVer);
				}
			}
		} catch (Exception e) {
			Log.error("remove attachment for page " + pageUuid + " on node " + nodeUuid + " with error.",e);
		}
		
	}


	public void getPageChildren(String spaceUname, String pageUuid, AsyncCallback<PageItemListModel> callback) {
		UserModel user = OfflineUtil.getUser();
		
		PageItemListModel model = new PageItemListModel();
		try {
			model.itemList = GearsDB.getUserDB(user.getUid()).getPageChildren(pageUuid);
		} catch (Exception e) {
			Log.error("Failed get page children",e);
		}
		
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);
	}

	public void renderPagePiecePhase(String spaceUname, String pageTitle, String pieceName, AsyncCallback<PageModel> callback) {
		//render page piece
		//security check ignored in offline model
		PageModel model = null;
		
		UserModel user = OfflineUtil.getUser();
		try {
			model = GearsDB.getUserDB(user.getUid()).getPageByTitle(spaceUname,pageTitle);
		} catch (DatabaseException e) {
			Log.error("Page exception for render page phase",e);
		} catch (GearsException e) {
			Log.error("Page exception for render page phase",e);
		}
		
		if(model == null){
			model = new PageModel();
			model.errorCode = ErrorCode.PAGE_PHASE_RENDER_NO_PAGE;
			OfflineUtil.setLoginInfo(model);
			//yes, return success() which will display an error message
			callback.onSuccess(model);
			return;
		}
		
		if(StringUtil.isBlank(pieceName)){
			//whole page
			model.renderContent = RenderPieceParser.parse(model.content,false);
		}else{
			model.renderContent = RenderPieceParser.parsePiece(model.content ,pieceName, false);
			if(model.renderContent == null){
				model.errorCode = ErrorCode.PAGE_PHASE_RENDER_NO_PHASE;
			}
		}
		
		OfflineUtil.setLoginInfo(model);
		//yes, return success() which will display an error message
		callback.onSuccess(model);
		
	}


	public void getPageTree(String spaceUname, AsyncCallback<TreeItemListModel> callback) {

		List<PageModel> tree = getPageTree(spaceUname);
		
		TreeItemListModel model = new TreeItemListModel();
		model.spaceUname = spaceUname;
		if(tree != null){
			//transfer PageModel to TreeItemModel
			ArrayList<TreeItemModel> modelList = new ArrayList<TreeItemModel>();
			//try to get from cached pageTree
			for (PageModel page : tree) {
				TreeItemModel item = new TreeItemModel();
				item.pageUuid = page.pageUuid;
				item.title = page.title;
				item.level = page.level;
				if (page.parentPageUuid != null) {
					String pUuid = page.parentPageUuid;
					int size = modelList.size() - 1;
					TreeItemModel parent = null;
					// reverse loop, parent must before child, this is dependent on
					// pageService.getPageTree() return a sorted tree set.
					for (int idx = size; idx > -1; idx--) {
						parent = modelList.get(idx);
						if (StringUtil.equals(parent.pageUuid,pUuid))
							break;
					}
					item.parent = parent;
				}
				modelList.add(item);
			}
			
			model.list = modelList;
		}
		
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);
		
	}


	public void requestMacroRenderInEditor(String spaceUname, String pageUuid, String macro, String currentContent, String[] visibleAttsNode, AsyncCallback<TextModel> callback){
		//it is impossible to render {foo-macro} into HTML in offline model, so render to an instruction text
		//to tell this macro only can be renderred while goes online.
		TextModel model = new TextModel();
		//TODO...
		
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);
	}

	public void getPageTabCount(String spaceUname, String pageUuid, boolean allowVisibleOnComment,
			boolean allowVisibleOnChildren, AsyncCallback<Integer[]> callback) {
		Integer[] counts= {-1,-1};
		try {
			UserModel user = OfflineUtil.getUser();
			
			PageModel page = GearsDB.getUserDB(user.getUid()).getCurrentPageByUuid(pageUuid);
			if(allowVisibleOnComment){
				//comment count
				counts[0] = GearsDB.getUserDB(user.getUid()).getCommentCount(page.pageUuid);
			}
			if(allowVisibleOnChildren){
				//children count
				counts[1] = GearsDB.getUserDB(user.getUid()).getPageChildrenCount(page.pageUuid);
			}
		} catch (GearsException e) {
			Log.error("unable to get offline comment/children count",e);
		}
		callback.onSuccess(counts);
	}
	//********************************************************************
	//               function method
	//********************************************************************

	/**
	 * @param spaceUname
	 * @return
	 */
	private List<PageModel> getPageTree(String spaceUname) {
		List<PageModel> tree =  pageTree.get(spaceUname);
		if(tree == null){
			//retrieve database to build page tree
			UserModel user = OfflineUtil.getUser();
			try {
				tree = GearsDB.getUserDB(user.getUid()).getPageTree(spaceUname);
				tree = sortPageTree(tree);
				
				if(tree != null){
					//cache it
					pageTree.put(spaceUname,tree);
				}
			} catch (GearsException e) {
				//current no error code for this
				Log.error("Unable to get page tree for space " + spaceUname);
			}
		}
		return tree;
	}
	
	/**
	 * @param tree
	 * @return
	 */
	private List<PageModel> sortPageTree(List<PageModel> list) {
		//retrieve all comment object to manually set comment's parent
		if(list != null){
			for (PageModel page: list) {
				if(page.parentPageUuid != null){
					page.setParent(findParent(list, page.parentPageUuid));
				}
			}
			//sort comment
			Set<PageModel> sort = new TreeSet<PageModel>(new CascadeComparator());
			sort.addAll(list);
			
			return new ArrayList<PageModel>(sort); 
		}else{
			return null;
		}
		
	}

	/**
	 * @param list
	 * @param parentPageUuid
	 * @return
	 */
	private PageModel findParent(List<PageModel> list, String parentPageUuid) {
		for (PageModel page: list) {
			if(parentPageUuid.equals(page.pageUuid))
				return page;
		}
		return null;
	}

	/*
	 * Set pageModel.ancenstor and pageModel.spaceTitle
	 */
	private void buildAncestors(PageModel model){
		model.ancenstorList = new ArrayList<PageModel>();
		
		//here just for failure tolerance, normally, spaceTitle will replace by space.name
		String spaceTitle = model.spaceUname;
		try {
			UserModel user = OfflineUtil.getUser();
			SpaceModel space = GearsDB.getUserDB(user.getUid()).getSpace(model.spaceUname);
			spaceTitle = space.name;
			
			List<PageModel> ancestors =  findPageAncestors(model.spaceUname, model.pageUuid);
			if(ancestors != null){
				for (PageModel parent : ancestors ) {
					//only copy necessary fields to PageModel, need adjust according to navbar requirement
					PageModel parentModel = new PageModel();
					parentModel.title = parent.title;
					parentModel.spaceTitle = spaceTitle;
					model.ancenstorList.add(parentModel);
				}
			}
		} catch (Exception e) {
			Log.error("build page anscetors list failed",e);
		}
		
		//reverse - so that sort from deepest to root
		//sort from root parent
		Collections.reverse(model.ancenstorList);
		model.spaceTitle = spaceTitle;
		
	}
	/**
	 * @param spaceUname
	 * @param pageUuid
	 * @return
	 */
	private List<PageModel> findPageAncestors(String spaceUname, String pageUuid) {
		ArrayList<PageModel> ancestors = new ArrayList<PageModel>();
		if(pageUuid == null)
			return  ancestors;
		
		//get whole space tree rather than retrieve parent to parent
		PageModel tailPage = null;
		List<PageModel> tree = getPageTree(spaceUname);
		for (PageModel page : tree) {
			if(pageUuid.equals(page.pageUuid)){
				tailPage = page;
				break;
			}
		}
		
		if(tailPage != null){
			ancestors.add(tailPage);
			while(tailPage.getParent() != null){
				ancestors.add(tailPage.getParent());
				tailPage = tailPage.getParent();
			}
		}
		return ancestors;
	}

	private String buildSidebar(GearsDB userDB, PageModel page) {
		String sidebarMarkup = null;
			
		if(!GwtUtils.contains(page.attribute, PageAttribute.NO_SIDE_BAR)){
			try {
				//retrieve page side bar, then render theme->right side bar
				PageModel home = userDB.getHomepage(page.spaceUname);
				boolean isHome = home != null && home.pageUuid.equals(page.pageUuid);
				String pageType = isHome?SharedConstants.THEME_PAGE_SCOPE_HOME:SharedConstants.THEME_PAGE_SCOPE_DEFAULT;
				PageThemeModel pTheme = userDB.getPageTheme(page.spaceUname, pageType, page.pageUuid);
				
				if(pTheme != null){
					sidebarMarkup = pTheme.sidebarMarkup;
					page.sidebarRenderContent = RenderPieceParser.parse(sidebarMarkup,false);
				}
			} catch (Exception e) {
				Log.error("Unable set side bar content",e);
			}
		}
		return sidebarMarkup;
	}

	private void removeDraft(String pageUuid, int type) throws GearsException {
		UserModel user = OfflineUtil.getUser();
		GearsDB userDB = GearsDB.getUserDB(user.getUid());
		
		if(type == 0){
			//remove all draft
			userDB.removeDraft(pageUuid,SharedConstants.MANUAL_DRAFT);
			userDB.removeDraft(pageUuid,SharedConstants.AUTO_DRAFT);
		}else{
			userDB.removeDraft(pageUuid,type);
		}
	}


	/**
	 * @param draft
	 * @param user
	 * @param userDB
	 * @throws GearsException 
	 */
	private PageModel saveDraft(PageModel draft, UserModel user) throws GearsException {
		GearsDB userDB = GearsDB.getUserDB(user.getUid());
		
		//check if this user has auto draft, if has, delete first
		PageModel existDraft = userDB.getUserDraft(draft.spaceUname,draft.title,draft.type,user);
		if(existDraft != null){
			userDB.removeDraft(existDraft.pageUuid,draft.type);
		}
		
		PageModel existPage = null;
		if(draft.pageUuid != null)
			existPage = userDB.getCurrentPageByUuid(draft.pageUuid);
		
		PageModel pDraft = new PageModel();
		if(existPage != null){
			pDraft = existPage;
			
			setTouchedInfo(pDraft, existPage, user);
			updateFromInput(pDraft, draft);
			
		}else{
			pDraft = draft;
			setTouchedInfo(pDraft, null, user);
			//this will convert content, to RenderPiece, so it still need call even pDraft = draft;
			updateFromInput(pDraft, draft);

			//get parent info
			if(!StringUtil.isBlank(draft.parentPageUuid)){
				PageModel parentPage = userDB.getCurrentPageByUuid(draft.parentPageUuid);
				if(parentPage != null){
					pDraft.level = parentPage.level + 1;
				}else{
					Log.warn("draft parent page does not exist. draft title is " 
							+ draft.title + ". Parent page uuid is "+ draft.parentPageUuid);
				}
			}else{
				//root page, such as home page
				pDraft.level = 0;
			}
			
			//update version
			pDraft.pageVersion = 1;
			pDraft.pageUuid = OfflineUtil.createPageUuid(user);
			
		}
		
		//does not update model.uid by return value: saveDraft return draftUid, so keep original one: 
		userDB.saveOrUpdatePage(pDraft,true);
		
		return pDraft;
	}



	/**
	 * @param pPage
	 * @param inputPage
	 */
	private void updateFromInput(PageModel pPage, PageModel inputPage) {
		
		//convert richHTML tag to RenderPiece list
		pPage.renderContent = RenderPieceParser.parse(inputPage.content,false);
		
		pPage.title = inputPage.title;
		pPage.type = inputPage.type;
	}

	private void refreshPageAttachments(PageModel model, UserModel user) throws DatabaseException, GearsException {
		List<AttachmentModel> atts = GearsDB.getUserDB(user.getUid()).getPageAttachments(model.pageUuid,model.type);
		model.attachmentJson = OfflineUtil.toAttachmentsJsonObject(atts);
	
	}	

	/**
	 * @param model
	 * @param existPage
	 * @param user
	 */
	private void setTouchedInfo(PageModel model, PageModel existPage, UserModel user) {
		long now =  new Date().getTime();
		if(existPage != null){
			//just copy creator info
			model.creator = existPage.creator;
			model.creatorPortrait = existPage.creatorPortrait;
			model.creatorUsername = existPage.creatorUsername;
			model.createDate = existPage.createDate;
		}else{
			model.creator = user.getFullname();
			model.creatorPortrait = user.getPortrait();
			model.creatorUsername = user.getLoginname();
			model.createDate = now;
		}
		model.modifier = user.getFullname();
		model.modifierPortrait = user.getPortrait();
		model.modifierUsername = user.getLoginname();
		model.modifiedDate = now;
	}
	/**
	 * @param pageUuid
	 * @param parentPageUuid
	 * @return
	 */
	private String getReturnPageTitle(String pageUuid, String parentPageUuid) {
		String title = "";
		try {
			UserModel user = OfflineUtil.getUser();
			PageModel page = null;
			if(!StringUtil.isBlank(pageUuid)){
				page = GearsDB.getUserDB(user.getUid()).getCurrentPageByUuid(pageUuid);
			}
			if(page == null && !StringUtil.isBlank(parentPageUuid)){
				//current page not exist, then go to parent
				page = GearsDB.getUserDB(user.getUid()).getCurrentPageByUuid(parentPageUuid);
			}
			if(page != null){
				title = page.title;
			}
		} catch (Exception e) {
			Log.error("Return to parent page with error." , e);
		}
		return title;
	}
	
	//********************************************************************
	//               Unsupported method in offline mode
	//********************************************************************
	public void switchEditor(String spaceUname, String pageUuid, boolean newEditorIsRich, String text,String[] visibleAttsNodeUuid,
			AsyncCallback<PageModel> callback) {
		// TODO Auto-generated method stub
		
	}



	public void diff(Integer uid1, Integer uid2, AsyncCallback<DiffListModel> callback) {
		// TODO Auto-generated method stub
		
	}

	
	public void diff(String spaceUname, String currPageTitle, Integer historyVersion,
			AsyncCallback<DiffListModel> callback) {
		// TODO Auto-generated method stub
		
	}

	
	public void diffConflict(PageModel draft, AsyncCallback<DiffListModel> callback) {
		// TODO Auto-generated method stub
		
	}

	
	public void getCustomizedPage(String customizedPageID, String[] params,
			AsyncCallback<PageModel> callback) {
		// TODO Auto-generated method stub
		
	}

	
	public void removePage(CaptchaCodeModel captcha, String spaceUname, String pageUuid, boolean permenant, AsyncCallback<PageModel> callback) {
		// TODO Auto-generated method stub
		
	}

	
	public void restoreCheck(String pageUuid, AsyncCallback<String> callback) {
		// TODO Auto-generated method stub
		
	}

	
	public void restoreHistory(CaptchaCodeModel captchaResponse, String spaceUname, String currPageTitle, int version, AsyncCallback<PageModel> callback) {
		// TODO Auto-generated method stub
		
	}

	
	public void restorePage(String spaceUname, String pageUuid, boolean homepage, boolean withHisotry,
			AsyncCallback<PageModel> callback) {
		// TODO Auto-generated method stub
		
	}	

	public void markPage(String pageUuid, int type, boolean add, AsyncCallback<Boolean> callback) {
		// TODO Auto-generated method stub
		
	}

	public void copy(String fromSpaceUname, String fromPageUuid, String toSpaceUname, String toParentUuid,
			boolean withChildren, AsyncCallback<PageModel> callback) {
		// TODO Auto-generated method stub
		
	}

	public void move(String fromSpaceUname, String fromPageUuid, String toSpaceUname, String toParentUuid,
			boolean withChildren, AsyncCallback<PageModel> callback) {
		// TODO Auto-generated method stub
		
	}
	
	public void updateAttachmentMeta(String spaceUname, String pageUuid, String nodeUuid, String name, String desc,
			AsyncCallback<String> callback) {
		// TODO Auto-generated method stub
		
	}

	public void editPageSidebar(String spaceUname, String uuid, AsyncCallback<PageModel> callback) {
		
	}

	public void saveSidebar(String spaceUname, String pageUuid,String type, String text, boolean isRichContent, AsyncCallback<PageModel> callback) {
		// TODO Auto-generated method stub
		
	}

	public void previewSidebar(String spaceUname, String pageUuid, String text, boolean isRichContent,
			AsyncCallback<PageModel> callback) {
		// TODO Auto-generated method stub
		
	}

	public void exist(String spaceUname, String pageTitle, AsyncCallback<Boolean> callback) {
		// TODO Auto-generated method stub
		
	}

	public void export(String spaceUname, String pageUuid, int type, AsyncCallback<String> callback) {
		// TODO Auto-generated method stub
		
	}

	public void previewTemplate(String spaceUname, String title, String desc, String content, boolean richEnabled,
			boolean shared, AsyncCallback<PageModel> callback) {
		// TODO Auto-generated method stub
		
	}

	public void saveTemplate(String spaceUname, String title, String desc, String content, boolean richEnabled,
			boolean shared, AsyncCallback<PageModel> callback) {
		// TODO Auto-generated method stub
		
	}

	
}
