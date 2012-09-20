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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.DateUtil;
import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.gwt.client.model.BlogPostMeta;
import com.edgenius.wiki.gwt.client.model.CommentModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.server.constant.PageType;
import com.edgenius.wiki.gwt.client.server.utils.PageAttribute;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.Draft;
import com.edgenius.wiki.model.DraftContent;
import com.edgenius.wiki.model.History;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.PageContent;
import com.edgenius.wiki.model.PageProgress;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.util.WikiUtil;
import com.google.gson.Gson;

/**
 * @author Dapeng.Ni
 */
public class PageUtil {

	/**
	 * 
	 * @param page
	 * @param value
	 * @param userReadingService
	 * @param coypAttachment if null, NOT_COPY_ATTACHMENT, if PageType.NONE_DRAFT, COPY_ATTACHMENT_WITHOUT_DRAFT
	 */
	public static void copyPageToModel(AbstractPage page,PageModel value, UserReadingService userReadingService, PageType coypAttachment) {
		User currentUser = WikiUtil.getUser();

		//Show sidebar or not  - this value only take effect when PageAttribute.NO_SIDEBAR is turned off.
		//set default value anyway
		value.pinPanel = SharedConstants.TAB_TYPE_RIGHT_SIDEBAR;
		if(!currentUser.isAnonymous()){
			int pin = currentUser.getSetting().getFixedPanel();
			//system design: -1 as default value of fixedPanel, i.e., SharedConstants.TAB_TYPE_RIGHT_SIDEBAR
			if(pin >= 0 ){ 
				value.pinPanel = pin;
			}
		}
		
// 		parentPageTitle		
//		requireNotified
		if(page.getParent() != null){
			value.parentPageUuid = page.getParent().getPageUuid();
		}
		value.pageUuid = page.getPageUuid();
		value.title = page.getTitle();
		value.uid = page.getUid();
		
		//so far, getPageHistory() will return null for space field
		Space space = page.getSpace();
		if(space != null){
			value.spaceUname = space.getUnixName();
			value.spaceUid = String.valueOf(space.getUid());
			value.spaceTitle = space.getName();
			
			//sync with blog
			if(space.containExtLinkType(Space.EXT_LINK_BLOG)){
				value.linkedBlogs = space.getSetting().getLinkedMetas();
				if(value.linkedBlogs != null && value.linkedBlogs.size() > 0){
					if(page instanceof Draft || page instanceof Page){
						PageProgress progress; 
						if(page instanceof Draft)
							progress =((Draft)page).getPageProgress();
						else
							progress =((Page)page).getPageProgress();
						
						//fill in post ext information
						if(progress != null && progress.getLinkExtInfoObject() != null){
							for(BlogPostMeta postValue:progress.getLinkExtInfoObject()){
								BlogMeta blog = space.getSetting().getBlogMeta(postValue.getBlogKey());
								if(blog != null)
									blog.setPostValue(postValue);
							}
						}
					}
				}
			}
		}
		if(page.getCreator()!= null){
			value.creator = page.getCreator().getFullname();	
			value.creatorUsername = page.getCreator().getUsername();
			value.creatorPortrait = UserUtil.getPortraitUrl(page.getCreator().getPortrait());
		}else{
			//Anonymous
			User anony = WikiUtil.getAnonymous(userReadingService);
			value.creator =  anony.getFullname();
			value.creatorUsername =  anony.getUsername();
			value.creatorPortrait = UserUtil.getPortraitUrl(null);
		}
		if(page.getModifier()!=null){
			value.modifier  = page.getModifier().getFullname();
			value.modifierUsername  = page.getModifier().getUsername();
			value.modifierPortrait = UserUtil.getPortraitUrl( page.getModifier().getPortrait());
		}else{
			//Anonymous
			User anony = WikiUtil.getAnonymous(userReadingService);
			value.modifier =  anony.getFullname();
			value.modifierUsername =  anony.getUsername();
			value.modifierPortrait = UserUtil.getPortraitUrl(null);
		}
		//covert:page modifiedDate is possible from database directly read out, which is is java.sql.TimeStamp type
		value.modifiedDate = DateUtil.getLocalDate(currentUser, page.getModifiedDate());
		value.createDate = DateUtil.getLocalDate(currentUser, page.getCreatedDate());
		value.pageVersion = page.getVersion();
		//only page have tag info, comparing draft
		if(page instanceof Page){
			value.tagString = ((Page)page).getTagString();
			value.content= ((Page)page).getContent() == null? "":((Page)page).getContent().getContent();
		}else if(page instanceof Draft){
			value.content= ((Draft)page).getContent() == null? "":((Draft)page).getContent().getContent();
		}else if(page instanceof History){
			value.content= ((History)page).getContent() == null? "":((History)page).getContent().getContent();
		}
		
		value.type = page.getType();
		value.level = page.getLevel();
		value.attribute = page.getAttribute();
		if(currentUser.isAnonymous()){
			//anonymous: no favorite, no watch, does not allow save draft.
			value.attribute = value.attribute | PageAttribute.NO_FAVORITE 
							| PageAttribute.NO_WATCHED | PageAttribute.NO_CREATE_DRAFT;
		}
		
		//create page pieces: text and links
		value.renderContent = page.getRenderPieces();
		value.sidebarRenderContent = page.getSidebarRenderPieces();
		value.spaceMenuContent= page.getSpaceMenuPieces();
		
		value.ancenstorList = new ArrayList<PageModel>();
		if(page.getAncestorList() != null){
			for (AbstractPage parent : page.getAncestorList() ) {
				//only duplicate necessary fields, need adjust according to navbar requirement
				PageModel parentModel = new PageModel();
				parentModel.title = parent.getTitle();
				value.ancenstorList.add(parentModel);
			}
		}
		
		if(coypAttachment != null){
		    value.attachmentJson = copyAttachmentsJson(page.getAttachments(), currentUser.getUsername(), coypAttachment);
		}
		//size of all OPERATIONS, some operation is not available for page, just left it as zero value
		//plus space admin permission onto position 11
		value.permissions = new int[SharedConstants.PAGE_PERM_SIZE];
		List<WikiOPERATIONS> perms = page.getWikiOperations();
		if(perms != null){
			for (WikiOPERATIONS perm : perms) {
				if(OPERATIONS.ADMIN.equals(perm.operation)
					|| OPERATIONS.EXPORT.equals(perm.operation)){
					//13,16, for space 
					value.permissions[SharedConstants.PERM_SPACE_BASE+perm.operation.ordinal()] = 1;
				}else{
					value.permissions[perm.operation.ordinal()] = 1;
				}
					
			}
		}
		//check if this user have system admin permission
		List<OPERATIONS> userPerms = currentUser.getWikiPermissions();
		if(perms != null){
			for (OPERATIONS perm : userPerms) {
				if(OPERATIONS.ADMIN.equals(perm)){
					//index 19
					value.permissions[SharedConstants.PERM_INSTNACE_MGM] = 1;
					break;
				}
			}
		}
	}


	public static void copyModelToPage(PageModel value,AbstractPage page,RenderService renderService) {
//		requireNotified
//		public String creator;
//		public String modifier;
//		public Date modifiedDate;
		page.setPageUuid(value.pageUuid);
		
		Space space = new Space();
		space.setUnixName(value.spaceUname);
		page.setSpace(space);
		page.setTitle(StringUtils.trimToEmpty(value.title));
//		page.setUnixName(WikiUtil.getPageUnixname(value.title));
		
		page.setVersion(value.pageVersion);
		
		if(value.linkedBlogs != null && value.linkedBlogs.size() > 0 && (page instanceof Draft || page instanceof Page)){
			PageProgress progress;
			if(page instanceof Draft){
				progress = ((Draft)page).getPageProgress();
				if(progress == null){
					progress = new PageProgress();
					((Draft)page).setPageProgress(progress);
				}
			}else{
				progress = ((Page)page).getPageProgress();
				if(progress == null){
					progress = new PageProgress();
					((Page)page).setPageProgress(progress);
				}
			}
			List<BlogPostMeta> postList = progress.getLinkExtInfoObject();
			if(postList == null){
				postList = new ArrayList<BlogPostMeta>();
			}
			for (BlogMeta blog : value.linkedBlogs) {
				if(blog.getPostValue() != null)
					postList.add(blog.getPostValue());
			}
			//must reset as it not simple set method - it will convert object to XML stream
			progress.setLinkExtInfoObject(postList);
		}
		//so far, only create/createHome will bring back ParentPageTitle. Otherwise it is null.
		if(value.parentPageUuid != null){
			Page parent = new Page();
			parent.setPageUuid(value.parentPageUuid);
			parent.setSpace(space);
			page.setParent(parent);
		}
//		page.setType(value.type);
		page.setAttribute(value.attribute);
		page.setVisibleAttachmentNodeList(value.visibleAttachments);
		
		if(page instanceof Page){
			PageContent pageContent = new PageContent();
			if(value.isRichContent){
				pageContent.setContent(renderService.renderHTMLtoMarkup(value.spaceUname,value.content));
			}else{
				pageContent.setContent(value.content);
			}
			((Page)page).setContent(pageContent);
			((Page)page).setTagString(value.tagString);
			((Page)page).setNewPageType(value.newPageType);
		}else{
			DraftContent pageContent = new DraftContent();
			if(value.isRichContent){
				pageContent.setContent(renderService.renderHTMLtoMarkup(value.spaceUname,value.content));
			}else{
				pageContent.setContent(value.content);
			}
			((Draft)page).setContent(pageContent);
			//does not set tag for draft
		}
		//page attachment
		if(value.attachmentList != null){
			List<FileNode> attachments = new ArrayList<FileNode>();
			for (Iterator<String> iter= value.attachmentList.iterator();iter.hasNext();) {
				String nodeUuid = iter.next();
				FileNode node = new FileNode();
				node.setNodeUuid(nodeUuid);
				attachments.add(node);
			}
			page.setAttachments(attachments);
		}
	}
	
	public static PageItemModel copyToPageItem(AbstractPage page) {
		User currentUser = WikiUtil.getUser();
		PageItemModel model = new PageItemModel();
		model.uid = page.getUid();
		model.uuid = page.getPageUuid();
		//pageHistory does not return space object
		if(page.getSpace() != null)
			model.spaceUname= page.getSpace().getUnixName();
		model.title= page.getTitle();
		
		if(page.getModifier()!=null){
			model.modifier  = page.getModifier().getFullname();
			model.modifierUsername = page.getModifier().getUsername();
			model.modifierPortrait = UserUtil.getPortraitUrl(page.getModifier().getPortrait());
		}else{
			//Anonymous
			User anony = WikiUtil.getAnonymous();
			model.modifier =  anony.getFullname();
			model.modifierUsername = anony.getUsername();
			model.modifierPortrait = UserUtil.getPortraitUrl(null);
		}
		
		model.modifiedDate =  DateUtil.getLocalDate(currentUser, page.getModifiedDate());
		model.type = page.getType();
		model.isCurrent = (page instanceof Page)?true:false;
		model.version = page.getVersion();

		return model;
	}

	public static String copyAttachmentsJson(List<FileNode> attList, String currentUserName, PageType coypAttachment){
        List<FileNode> userAttList = new ArrayList<FileNode>();
        if(attList != null && attList.size() > 0){
            for (FileNode node: attList) {
                //this node is manual draft or auto draft
                if(node.getStatus()  > 0){
                    if(!StringUtils.equalsIgnoreCase(node.getCreateor(), currentUserName)
                        || coypAttachment == PageType.NONE_DRAFT){
                        //if user is not current user, it means it is other person's draft, then remove 
                        continue;
                    }
                    //I comment this filter(2009/06/17) because I think this can maximum ensure user upload works.
                    //If do below filter, it causes confuse sometimes. For example, user upload foo.png and save manual 
                    //draft then exit, if go back editing, upload image bar.png, if reload manual draft, the bar.png is gone as 
                    //it is auto-draft status yet. But if at beginning, user only keep auto draft, then reload will load
                    //both images as they both status are auto-draft... confused here - if I upload an image bar.png, 
                    // this image kept if I restore auto draft, but it is gone if I restore manual draft? So anyway, 
                    // I always load all status attachment whatever the request...
                    
                    //could be COPY_ATTACHMENT_WITH_DRAFT,COPY_ATTACHMENT_WITH_AUTOSAVE, 
                    //Auto-save also need load manual draft attachment, but manual does not load auto's
//                  if(coypAttachment < node.getStatus() ){
//                      continue;
//                  }
                }
                userAttList.add(node);
            }
            Gson gson = new Gson();
            return  gson.toJson(userAttList);
        }
        //empty json
        return "{}";
	}
	//********************************************************************
	//                       Private methods
	//********************************************************************

	/**
	 * Copy drafts (may contain auto and manual) to PageModel draftUid,draftDate(manual) and autoSavedUid,autoSavedDate
	 * @param drafts
	 * @param model
	 * @param user
	 */
	public static void copyDraftStatus(List<Draft> drafts, PageModel model, User user) {
		if(drafts !=null){
			for (Draft draft : drafts) {
				if(draft.getType() == PageType.MANUAL_DRAFT){
					model.draftUid = draft.getUid();
					model.draftDate = DateUtil.getLocalDate(user, draft.getModifiedDate());
				}else if(draft.getType() == PageType.AUTO_DRAFT){
					model.autoSaveUid = draft.getUid();
					model.autoSaveDate = DateUtil.getLocalDate(user, draft.getModifiedDate());
				}
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<PageItemModel> copyPageItem(List pages, int copySize) {
		ArrayList<PageItemModel> models = new ArrayList<PageItemModel>();
		//chose minimum one
		int size = (pages.size() < copySize || copySize <= 0 )?pages.size():copySize;
		for(int idx=0;idx<size;idx++) {
			AbstractPage page = (AbstractPage) pages.get(idx);
			//only copy necessary fields, so don't use PageUtil.copy()
			PageItemModel model = copyToPageItem(page);
			models.add(model);
		}
		return models;
	}

	/**
	 * @param node
	 * @param attModel
	 * @param user 
	 */
	public static void copyAttachmentToModel(FileNode node, AttachmentModel attModel, User user) {
//		attModel.index=node.;
		attModel.nodeUuid=node.getNodeUuid();
		attModel.version = node.getVersion();
		//display element
		attModel.filename=node.getFilename();
		attModel.creator=node.getCreateor();
		attModel.date= DateUtil.getLocalDate(user, new Date(node.getDate()));
		//bytes
		attModel.size=node.getSize();
		attModel.desc=node.getComment();
		attModel.draftStatus = node.getStatus();
	}

	//********************************************************************
	//               comments methods
	//********************************************************************
	public static CommentModel copyCommentToModel(PageComment comment, User currentUser) {
		CommentModel model = new CommentModel();
		if(comment.getCreator() != null){
			model.author = comment.getCreator().getFullname();
			model.authorUsername = comment.getCreator().getUsername();
			model.authorPortrait = UserUtil.getPortraitUrl(comment.getCreator().getPortrait());
		}else{
			User user = WikiUtil.getAnonymous();
			model.authorUsername = user.getUsername();
			model.author = user.getFullname();
			model.authorPortrait = UserUtil.getPortraitUrl(null);
		}
		
		model.body = comment.getBody();
		model.uid = comment.getUid();
		if( comment.getParent() != null)
			model.parentUid = comment.getParent().getUid();
		if(comment.getRoot() != null)
			model.rootUid = comment.getRoot().getUid();
		model.level = comment.getLevel();
		model.hide = comment.isHide();
		
		model.modifiedDate = DateUtil.getLocalDate(currentUser, comment.getCreatedDate()); 
			
		return model;
	}

	/**
	 * @return
	 */
	public static PageComment copyModelToComment(CommentModel msg) {
		PageComment comment = new PageComment();
		comment.setBody(msg.body);
		if(msg.parentUid != null){
			PageComment parent = new PageComment();
			parent.setUid(msg.parentUid);
			comment.setParent(parent);
		}
		if(msg.rootUid != null){
			PageComment root = new PageComment();
			root.setUid(msg.rootUid);
			comment.setParent(root);
		}
		
		//hide?
		comment.setHide(msg.hide);
		
		return comment;
	}


}
