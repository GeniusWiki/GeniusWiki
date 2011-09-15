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

/**
 * @author Dapeng.Ni
 */
public interface PageController extends RemoteService{
	String MODULE_ACTION_URI = "page.rpcs";

	//********************************************************************
	//               FunctionWidget methods
	//********************************************************************
	public PageModel savePage(PageModel page,boolean forceSave);
	public PageModel saveAutoDraft(PageModel page);
	public PageModel saveManualDraft(PageModel draft, boolean exitToView);
	
	public PageModel getEmptyPageDefaultContent(String pageUuid);
	public PageModel viewPage(String spaceUname,String pageTitle, String commentUid);
	public PageModel editPage(String spaceUname,String pageUuid);
	public PageModel editDraft(Integer draftUid, int draftType, boolean refreshAttachment);
	public PageModel editPageSidebar(String spaceUname, String pageUuid);
	
	public PageModel previewSidebar(String spaceUname, String pageUuid, String text, boolean isRichContent);

	public PageModel saveSidebar(String spaceUname, String pageUuid,String type, String text,boolean isRichContent);

	/**
	 * Return parent page title
	 */
	public String cancelEdit(String pageUuid, String parentPageUuid);
	/**
	 * This method simply load page parent's nav bar and return. It does not check if page exist since it is useless
	 * until page saved.
	 */
	public PageModel createPage(String spaceUname, String newPageTitle, String parentPageUuid, int newPageType);

	public PageItemListModel getPageHistory(String spaceUname, String pageUuid, int startVer, int returnCount);
	
	public PageModel removeAttachment(String spaceUname,String pageUuid, String nodeUuid, String nodeVersion);
	//return removed page uid
	public PageModel removePage(CaptchaCodeModel captcha, String spaceUname, String pageUuid, boolean permenant);
	
	public PageModel removeDraft(String spaceUname, String pageUuid, int type);
	public TreeItemListModel getPageTree(String spaceUname);
	
	/**
	 * Mark page as favorite or watch page
	 * @param type
	 * @return
	 */
	boolean markPage(String pageUuid, int type, boolean add);
	
	public PageModel getHistoryByUid(Integer historyUid,boolean refreshAttachment);
	
	public PageModel move(String fromSpaceUname,String fromPageUuid, String toSpaceUname, String toParentUuid, boolean withChildren);
	public PageModel copy(String fromSpaceUname,String fromPageUuid, String toSpaceUname, String toParentUuid, boolean withChildren);
	
	/*
	 * Return flat html format DiffModel
	 */
	public DiffListModel diff(Integer uid1,Integer uid2);
	public DiffListModel diff(String spaceUname, String currPageTitle, Integer historyVersion);
	public DiffListModel diffConflict(PageModel draft);
	
	public PageItemListModel getUserPagesInSpace(String spaceUname, String username, int returnNum);
	
	public PageModel getCustomizedPage(String customizedPageID, String[] params);
	
	//return error code
	public String updateAttachmentMeta(String spaceUname,  String pageUuid, String nodeUuid, String name, String desc);
	/**
	 * Check if this restored page has a duplicated title page exist in space. This is useful to ask user choose if go ahead to restore.  
	 * @param pageUuid
	 * @return the potential page title, which dependent on if same title page exist in this space.
	 */
	public String restoreCheck(String pageUuid);
	
	public PageModel restorePage(String spaceUname, String pageUuid, boolean homepage, boolean withHisotry);
	public PageModel switchEditor(String spaceUname,String pageUuid, boolean  newEditorIsRich, String text,String[] visibleAttsNodeUuid);
	
	public PageModel restoreHistory(CaptchaCodeModel captchaResponse,String spaceUname, String currPageTitle, int version);
	
	public PageItemListModel getPageChildren(String spaceUname, String pageUuid);
	
	public Boolean exist(String spaceUname, String pageTitle);
	
	/**
	 * 
	 * @param pageUuid: if export single page, it has value. Otherwise, it is null.
	 * @param type - PDF or HTML
	 * @param type
	 * @return
	 */
	public String export(String spaceUname, String pageUuid, int type);
	
	public PageModel renderPagePiecePhase(String spaceUname, String pageTitle, String phaseName);
	
	public TextModel requestMacroRenderInEditor(String spaceUname, String pageUuid, String macro, String currentContent, String[] visibleAttsNode);

	public Integer[] getPageTabCount(String spaceUname, String pageUuid, boolean allowVisibleOnComment,boolean allowVisibleOnChildren);
}
