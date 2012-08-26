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
package com.edgenius.wiki.gwt.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;

/**
 * @author Dapeng.Ni
 */
public class PageModel extends CaptchaCodeModel  implements CascadeObject<PageModel>{
	private static final long serialVersionUID = -8875837170061514031L;

	// please note, it is not recommend use it to read page  except you know what exactly type
	// as History,Draft,Page, Offline pages may use same uid.
	//-- Offline model use this field for bring uid to PageItemModel as it need for getting history, draft by UID as it is uid of table record. 
	//-- Now, PageUtil.copyPageToModel() also fill this field - in view page history, this field is used to for comparison link as history Uid
	public int uid;

	//********************************************************************
	//               Variables
	//********************************************************************
	//input/output parameters
	//create: spaceUname+parentPageUid+pageTitle(when click undefined link): return page Uid
	//create: spaceUname+parentPageUid (just click create button): return pageUid
	//update: pageUid : return same pageUid
	//view: spaceUname+pageTitle: return pageUid, spaceUname, parentPageUid
	public String spaceUname;
	public String spaceUid;
	public String spaceTitle;

	public String title;
	//use for 2nd parameter on navigator, aka, new HyperLink(title,navToken): if navToken is null, use title and spaceUname to build token instead.
	public String navToken;
	//page uuid: unique for page whatever history change
	public String pageUuid;
	public int pageVersion;
	public String parentPageUuid;

	//Identify if this model is history page
	public boolean isHistory = false;
	//if page is history page, it must bring back latest current page Title. 
	//The reasons are, page title may change many times, 
	//if we don't change PageMain.setCurrentPageTitle(), browser refresh, that value will gone.
	//if we change PageMain.setCurrentPageTitle() to history page title, user click in many different title history, then 
	//program can not get correct current page title (PageMain.getCurrentPageTitle()). 
	public String currentTitle;
	
	//input value or return value: It may be rich html tag or plain wiki markup in different scenario.
	public String content;
	//identify if current content is rich(HTML) text. For request, wiki 
	public boolean isRichContent = false;
	
	/**
	 * for render, text and link : maybe contain string and LinkModel
	 */
	public List<RenderPiece> renderContent;

	public List<RenderPiece> spaceMenuContent;

	//side bar render piece, normally include {$author}{$modifier}
	public List<RenderPiece> sidebarRenderContent;

	//tells which Div is target render part. if blank, then render to entire RenderPanel
	public String targetDiv;
	
	public String creator;
	public String creatorUsername;
	public String creatorPortrait;
	public long createDate;
	
	public String modifier;
	public String modifierUsername;
	public String modifierPortrait;
	public long modifiedDate;
	
	
	public int requireNotified;
	//0: is current page or history page,  draft page:Manual(1) or Auto draft(2).
	public int type;
	public int level;
	
	//identify this page is "page not found"(disable create/edit button etc), 
	//or "pure drafe page"(only draft pages without any saved page)
	public int attribute;
	
	public String tagString;
	
	//SEND TO SERVER: this fields will bring back current page attachment NodeUuid fields. It is for merge attachments with current user's
	//auto/manual draft attachment list.
	public ArrayList<String> attachmentList;
	
	//Offline sync page: bring back page attachments URL, also includes version attachment history 
	public ArrayList<AttachmentModel> attachmentNodes;
	//SEND TO CLIENT: attachment JSON string
	public String attachmentJson;
	
	//0:disable, 1 enable, -1 not allow
	public int favorite;
	public int watched;
	public int pintop;

	public Integer draftUid;
	public long draftDate;
	public Integer autoSaveUid;
	public long autoSaveDate;
	
	//for nav bar, includes all ancenstor page model(limited fields filled). 
	public ArrayList<PageModel> ancenstorList;
	
	//page permission: use  WikiPrivilegeStrategy.WikiOPERATIONS as index, value should be 0 or 1 means forbid or allow
	public int[] permissions = new int[SharedConstants.PAGE_PERM_SIZE];
	
	//only use on client side to transfer PageAttribute.NEW_PAGE or PageAttribute.NEW_HOMEPAGE
	//actually it transfer to service side PageControllerImpl.createPage() method and transfer back without any change.
	public int newPageType;

	//offline sync: sync comments of this page
	public ArrayList<CommentModel> commentList;
	public String pageThemeType;

	//see what tab display under page - Comment, History, Children etc. Value is from PageTabPanel.TAB_TYPE_xxx
	public int tabIndex = -1;
	//if scroll page to tab part?
	public boolean tabFocus = false;
	
	//tabs(comment, history, children) and side bars visible status - so far only applied to right sidebar.
	//value is from SharedConstant.TAB_TYPE_*
	public int pinPanel;
	
	//@see RenderContext.getVisibleAttachments() usage
	public String[] visibleAttachments;
	
	//only for client side comparable
	private transient PageModel parent;
	
	public String editingUsername;
	public String editingUserFullname;
	public int editingTime;
	public String editingUserPortrait;

	//link ext blog data
	public Collection<BlogMeta> linkedBlogs;

	//When display history, these two are previous and next history around current - if it has
	public PageItemModel nextHistoryItem;
	public PageItemModel prevHistoryItem;


	//********************************************************************
	//               methods
	//********************************************************************
	public String toString(){
		return title + ":" + pageUuid+":Ver"+pageVersion;
	}

	
	/**
	 * @param parent the parent to set
	 */
	public void setParent(PageModel parent) {
		this.parent = parent;
	}

	//********************************************************************
	//               CascadeObject methods
	//********************************************************************
	public boolean before(PageModel obj) {
		if(this.title != null && obj != null && obj.title != null){
			return this.title.compareTo(obj.title)>0;
		}else{
			return false;
		}
	}


	public int getLevel() {
		return level;
	}

	public PageModel getParent() {
		return parent;
	}
}
