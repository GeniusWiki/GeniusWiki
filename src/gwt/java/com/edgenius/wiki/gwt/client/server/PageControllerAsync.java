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
package com.edgenius.wiki.gwt.client.server;

import com.edgenius.wiki.gwt.client.model.CaptchaCodeModel;
import com.edgenius.wiki.gwt.client.model.DiffListModel;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.model.TreeItemListModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Dapeng.Ni
 */
public interface PageControllerAsync extends RemoteServiceAsync {

	void savePage(PageModel page, boolean forceSave, AsyncCallback<PageModel> callback);
	void saveAutoDraft(PageModel page, AsyncCallback<PageModel> callback);
	void saveManualDraft(PageModel draft, boolean exitToView, AsyncCallback<PageModel> callback);
	
	void getEmptyPageDefaultContent(String pageUuid, AsyncCallback<PageModel> callback);
	
	void viewPage(String spaceUname,String  pageTitle, String commentUid, AsyncCallback<PageModel> callback);
	void editPage(String spaceUname,String  pageUuid, AsyncCallback<PageModel> callback);
	void editPageSidebar(String spaceUname, String uuid, AsyncCallback<PageModel> callback);
	
	//return histories less than this startVer  
	void getPageHistory(String spaceUname, String pageUuid,int startVer, int returnCount, AsyncCallback<PageItemListModel> callback);
	
	void getAttachments(String spaceUname, String pageUuid, int draft, AsyncCallback<String> callback);
	void removeAttachment(String spaceUname,String pageUuid, String nodeUuid, String nodeVersion, AsyncCallback<PageModel> callback);
	void removeDraft(String spaceUname,String pageUuid, int type, AsyncCallback<PageModel> callback);
	void getPageTree(String spaceUname, AsyncCallback<TreeItemListModel> callback);
	
	/**
	 * Called when user choose a history page from list.
	 * @param model
	 * @param callback
	 */
	void getHistoryByUid(Integer historyUid,boolean refreshAttachment, AsyncCallback<PageModel> callback);
	/**
	 * @param draftUid
	 * @param draftType, auto or manual draft
	 * @param loadDraftAsync
	 */
	void editDraft(Integer draftUid, int draftType, boolean refreshAttachment, AsyncCallback<PageModel> callback);

	void diff(Integer uid1,Integer uid2, AsyncCallback<DiffListModel> callback);
	void diff(String spaceUname, String currPageTitle, Integer historyVersion, AsyncCallback<DiffListModel> callback);
	void diffConflict(PageModel draft, AsyncCallback<DiffListModel> callback);
	
	void move(String fromSpaceUname, String fromPageUuid, String toSpaceUname, String toParentUuid, boolean withChildren, AsyncCallback<PageModel> callback);
	void copy(String fromSpaceUname, String fromPageUuid, String toSpaceUname, String toParentUuid, boolean withChildren, AsyncCallback<PageModel> callback);

	void markPage(String pageUuid, int type, boolean add, AsyncCallback<Boolean> callback);

	

	void removePage(CaptchaCodeModel captcha, String spaceUname, String pageUuid, boolean permenant, AsyncCallback<PageModel> callback);

	/**
	 * If spaceUname is blank, return this users updated/created pages in all spaces order by Page.createdDate.
	 * @param spaceUname
	 * @param username
	 * @param returnNum How many pages of this user return. To avoid return too much pages once
	 * @param pagesAsync
	 */
	void getUserPagesInSpace(String spaceUname, String username, int returnNum, AsyncCallback<PageItemListModel> callback);
	/**
	 * @param params  String value, may contains multiple params, separator defined by customized.
	 * @param cid
	 * @param viewPanel
	 */
	void getCustomizedPage(String customizedPageID, String[] params, AsyncCallback<PageModel> callback);
	/**
	 */
	void updateAttachmentMeta(String spaceUname, String pageUuid, String nodeUuid, String name, String desc, AsyncCallback<String> callback);
	/**
	 * @param pageUuid
	 * @param preRestoreAsync
	 */
	void restoreCheck(String pageUuid, AsyncCallback<String> callback);
	/**
	 * @param spaceUname
	 * @param pageUuid
	 * @param withHisotry
	 * @param restoreAsync
	 */
	void restorePage(String spaceUname, String pageUuid, boolean homepage, boolean withHisotry, AsyncCallback<PageModel> callback);
	/**
	 * @param model : coming, spaceUname, page title, parentPageUuid, and newPageType(New page or New Homepage)
	 * @param editPanel
	 */
	void createPage(String spaceUname, String newPageTitle, String parentPageUuid, int newPageType ,  AsyncCallback<PageModel> callback);
	/**
	 * @param newEditorIsRich
	 * @param text
	 */
	void switchEditor(String spaceUname, String pageUuid, boolean newEditorIsRich, String text,String[] visibleAttsNodeUuid, AsyncCallback<PageModel> callback);
	/**
	 * 
	 * @param captchaResponse null then it need not captcha validation, otherwise, do validate.
	 * @param spaceUname
	 * @param currPageTitle
	 * @param version
	 * @param restoryHistoryCallback
	 */
	void restoreHistory(CaptchaCodeModel captchaResponse, String spaceUname, String currPageTitle, int version, AsyncCallback<PageModel> callback);
	/**
	 * @param parentPageUuid
	 * @param parentPageUuid 
	 * @param cancelAsync
	 */
	void cancelEdit(String pageUuid, String parentPageUuid, AsyncCallback<String> callback);
	/**
	 * @param text
	 * @param previewAsync
	 */
	void previewSidebar(String spaceUname, String pageUuid, String text, boolean isRichContent, AsyncCallback<PageModel> callback);
	/**
	 * @param text
	 * @param viewPanel
	 */
	void saveSidebar(String spaceUname,String pageUuid, String type, String text,boolean isRichContent, AsyncCallback<PageModel> callback);
	/**
	 * @param spaceUname
	 * @param pageUuid
	 * @param childrenPanel
	 */
	void getPageChildren(String spaceUname, String pageUuid,  AsyncCallback<PageItemListModel> callback);
	/**
	 * @param spaceUname
	 * @param title
	 * @param validPageAsync
	 */
	void exist(String spaceUname, String pageTitle, AsyncCallback<Boolean> callback);
	/**
	 * @param spaceUname
	 * @param pageUuid: if export single page, it has value. Otherwise, it is null.
	 * @param type - PDF or HTML
	 * @param exportDialog
	 */
	void export(String spaceUname, String pageUuid, int type, AsyncCallback<String> callback);
	/**
	 * Render whole page(if pieceName is blank) or page phase
	 * @param spaceUname
	 * @param pageTitle
	 * @param pieceName
	 * @param callback
	 */
	void renderPagePiecePhase(String spaceUname, String pageTitle, String pieceName, AsyncCallback<PageModel> callback);
	
	/**
	 * Rich editor insert a Macro which need the mighty of server... 
	 * @param macro
	 * @param callback
	 */
	void requestMacroRenderInEditor(String spaceUname, String pageUuid, String macro, String currentContent, String[] visibleAttsNodeUuid, AsyncCallback<TextModel> callback);
	/**
	 * @param spaceUname
	 * @param pageUuid
	 * @param allowVisibleOnComment
	 * @param allowVisibleOnChildren
	 * @param counterAsync
	 */
	void getPageTabCount(String spaceUname, String pageUuid, boolean allowVisibleOnComment,
			boolean allowVisibleOnChildren, AsyncCallback<Integer[]> callback);

	
}
