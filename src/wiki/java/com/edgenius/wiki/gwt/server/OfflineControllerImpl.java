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
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.ITicket;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.repository.RepositoryTiemoutExcetpion;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.Theme;
import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.model.CommentModel;
import com.edgenius.wiki.gwt.client.model.OfflineModel;
import com.edgenius.wiki.gwt.client.model.PageListModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.ThemeModel;
import com.edgenius.wiki.gwt.client.model.UploadModel;
import com.edgenius.wiki.gwt.client.server.OfflineController;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.handler.GWTSpringController;
import com.edgenius.wiki.model.Draft;
import com.edgenius.wiki.model.History;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.ActivityLogService;
import com.edgenius.wiki.service.CommentException;
import com.edgenius.wiki.service.CommentService;
import com.edgenius.wiki.service.DuplicatedPageException;
import com.edgenius.wiki.service.PageException;
import com.edgenius.wiki.service.PageSaveTiemoutExcetpion;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.SecurityDummy;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.ThemeService;
import com.edgenius.wiki.service.VersionConflictException;
import com.edgenius.wiki.util.WikiUtil;
	
/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class OfflineControllerImpl extends GWTSpringController implements OfflineController {
	private static final Logger log = LoggerFactory.getLogger(OfflineControllerImpl.class);
	
	private SpaceService spaceService;
	private RepositoryService repositoryService; 
	private CommentService commentService;
	private RenderService renderService;
	private PageService pageService;
	private SecurityService securityService;
	private ThemeService themeService;
	private SecurityDummy securityDummy;
	private ActivityLogService activityLog;
	
	//JDK1.6 @Override
	public ArrayList<OfflineModel> downloadSpaces(ArrayList<OfflineModel> offlineSpaces) {
		
		ArrayList<OfflineModel> retList = new ArrayList<OfflineModel>();
		if(offlineSpaces == null || offlineSpaces.size() == 0){
			log.info("No pages of spaces request to download to offline");
			return retList;
		}
		
		for (OfflineModel offline : offlineSpaces) {
			//get this space for this user's offline options
			try {
				OfflineModel retModel = new OfflineModel();
				retModel.spaceUname = offline.spaceUname;
				retList.add(retModel);
				
				try {
					securityDummy.checkSpaceOffline(offline.spaceUname);
				} catch (Exception e) {
					log.error("Secuirty check for space " + offline.spaceUname 
							+ " offline permission failed.Offline is disabled for it.");
					//put null, delete this space on client side
					retModel.space = null;
					continue;
				}
				log.info("User " + WikiUtil.getUserName() + " is downloading offline space " + offline.spaceUname + " with options " + offline.options);
				
				SpaceModel spaceM = this.getSpace(offline.spaceUname);
				if(spaceM == null){
					//space not exist or not allow read,put null to Return offlineModel(will invoke entire space deleted in client side)
					//and don't continue to get pages
					retModel.space = null;
					continue;
				}
				//record this sync date time. 
				spaceM.syncDate = System.currentTimeMillis();
				
				retModel.space = spaceM;
				retModel.pageList = this.getSpacePages(offline.spaceUname,offline.options, offline.syncDate);
				retModel.existPages = this.getSpaceAvailablePagesUuid(offline.spaceUname);
				
				//get space theme
				Space space = spaceService.getSpaceByUname(offline.spaceUname);
				Theme theme = themeService.getSpaceTheme(space);
				if(theme.getUpdateDate() == null || offline.syncDate == null || theme.getUpdateDate().getTime() > offline.syncDate){
					retModel.theme = new ThemeModel();
					ThemeUtil.copyContentToModel(theme, retModel.theme,renderService);
				}
				
				
			} catch (Exception e) {
				log.error("Failed to get sync pages from space " + offline.spaceUname);
			}
		}
		
		return retList;
	}

	//JDK1.6 @Override
	public ArrayList<UploadModel> uploadPages(ArrayList<PageModel> pages, HashMap<String,Long> lastSyncTime) {
		//upload offline edited pages to server and return success updated pages, whatever it save into draft or replace page
		ArrayList<UploadModel> uuidList = new ArrayList<UploadModel>();
		List<PageModel> uploadDrafts = new ArrayList<PageModel>();
		for (PageModel model : pages) {
			try {
				String oldUuid = model.pageUuid;
				if(model.pageUuid != null && model.pageUuid.startsWith("0.")){
					//client side uuid, reset it to null.
					model.pageUuid = null; 
				}
				if(model.type > 0){
					//uploaded is drafts
					UploadModel upload = new UploadModel();
					upload.oldPageUuid = oldUuid;
					upload.newPageUuid = saveToDraft(model,model.type);
					upload.pageType = model.type;
					uuidList.add(upload);
					//save all uploaded draft into list, to compare with current, then decide delete
					uploadDrafts.add(model);
				}else{
					try {
						Page page = new Page();
						//certainly, offline edited content is rich content;
						model.isRichContent = true;
						PageUtil.copyModelToPage(model, page, renderService);
						page = pageService.savePage(page, model.requireNotified, false);
						UploadModel upload = new UploadModel();
						upload.oldPageUuid = oldUuid;
						upload.newPageUuid = page.getPageUuid();
						uuidList.add(upload);
						
						activityLog.logPageSaved(page);
					} catch (VersionConflictException e) {
						if(!WikiUtil.getUser().isAnonymous()){
							UploadModel upload = new UploadModel();
							upload.oldPageUuid = oldUuid;
							upload.newPageUuid = saveToDraft(model,Draft.OFFLINE_CONFLICT_DRAFT);
							uuidList.add(upload);
						}else{
							AuditLogger.warn("Anonymous user has version conflict, no conflict draft saved!");
						}
					} catch (DuplicatedPageException e) {
						if(!WikiUtil.getUser().isAnonymous()){
							UploadModel upload = new UploadModel();
							upload.oldPageUuid = oldUuid;
							upload.newPageUuid = saveToDraft(model,Draft.OFFLINE_CONFLICT_DRAFT);
							uuidList.add(upload);
						}else{
							AuditLogger.warn("Anonymous user has duplicated page conflict, no conflict draft saved!");
						}
					}
					
				}
			} catch (PageException e) {
				//handle it as error
				log.error("Save offline edit page with error:" +model.title,e);
			} catch (PageSaveTiemoutExcetpion e) {
				//handle it as error
				log.error("Save offline edit page with error:" +model.title,e);
			} catch (Exception e){
				//handle it as error
				log.error("Save offline edit page with error:" +model.title,e);
			}
		}
		User user = WikiUtil.getUser();
		//some draft may deleted on client side. here compare uploaded and current,then decide delete draft from server side
		if(uploadDrafts.size() > 0 && !user.isAnonymous()){
	
			List<Draft> existDrafts = pageService.getDraftPages(user, 0);
			List<Draft> deleteDrafts = new ArrayList<Draft>();
			for (Draft draft : existDrafts) {
				Long dt = lastSyncTime.get(draft.getSpace().getUnixName());
				if(dt == null){
					log.info("Draft on space " + draft.getSpace().getUnixName() + " is not in sync list, skip it.");
					continue;
				}
				Date lastDt = new Date(dt);
				
				if(draft.getModifiedDate().after(lastDt)){
					log.info("Draft won't be delete as it is newer than last sync:" + draft.getPageUuid() + "; type:" + draft.getType());
					continue;
				}
				boolean found = false;
				for (PageModel up : uploadDrafts) {
					if(StringUtils.equals(draft.getSpace().getUnixName(),up.spaceUname)
						&& StringUtils.equals(draft.getPageUuid(),up.pageUuid) 
						&& draft.getType() == up.type){
						found = true;
						break;
					}
				}
				if(!found){
					deleteDrafts.add(draft);
				}
			}
		
			for (Draft draft : deleteDrafts) {
				try {
					pageService.removeDraft(user, draft.getSpace().getUnixName(), draft.getPageUuid() , draft.getType());
					log.info("Offline removed draft sync to server side:" + draft.getPageUuid() + "; type:" + draft.getType());
				} catch (PageException e) {
					log.error("Remove draft failed ",e);
				}
			}
			
		}
		return uuidList;
	}


	//JDK1.6 @Override
	public ArrayList<AttachmentModel> removeOfflineDeletedAttachments(ArrayList<AttachmentModel> atts) {
		ArrayList<AttachmentModel> upList = new ArrayList<AttachmentModel>();
		//at moment, this upList only include deleted attachment list, as upload is handled in UploadServlet 
		
		for (AttachmentModel att: atts) {
			if(att.offlineEdited !=  SharedConstants.OFFLINE_DELETED){
				log.error("Unexpected, attachment is marked as non-delete is go in:" + att.filename + "; status " + att.offlineEdited);
				continue;
			}
			//??? always rely on Online version, if offline delete some attachment, but this attachment is also 
			//upload a new version (metadata update is not considered), then stop delete
			try {
				ITicket ticket = repositoryService.login(att.spaceUname, att.spaceUname, att.spaceUname);
				List<FileNode> list = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_ATTACHMENT, att.pageUuid, false);
				//find out current version
				double currVer = 0;
				for (FileNode fileNode : list) {
					currVer = Math.max(NumberUtils.toDouble(fileNode.getVersion()),currVer);
				}
				double oVer = NumberUtils.toDouble(att.version);
				if(oVer < currVer){
					//some new version is uploaded in server side
					log.info("New uploaded in server side, give up delete on file " + att.filename + ";Version:" + oVer + ">" +currVer);
				}else if(oVer > currVer){
					AuditLogger.error("Unexpected - offline deleted attachment version greater than online version?!" + oVer + ">" +currVer);
				}else{
					//equals, then delete attachment
					log.info("Attachment "+att.nodeUuid+ "is removed from offline mode for page " +att.pageUuid +" on space " + att.spaceUname);
					pageService.removeAttachment(att.spaceUname, att.pageUuid, att.nodeUuid,null);
				}
				//!!!NOTE, this att is from server side also bring spaceUname, pageUuid, KEEP them, as client side need it
				//whatever, delete it permanently on client side 
				upList.add(att);
			} catch (RepositoryException e) {
				log.error("Unable to get attachment to delete it: " + att.filename + " on page " + att.pageUuid,e);
			} catch (RepositoryTiemoutExcetpion e) {
				log.error("Unable to get attachment to delete it: " + att.filename + " on page " + att.pageUuid,e);
			}
		}
		
		return upList;
	}


	/**
	 * @param uuidList
	 * @param model
	 * @throws PageException
	 */
	private String saveToDraft(PageModel model,int type) throws PageException {
		//save it to conflict draft then
		Draft draft = new Draft();
		model.isRichContent = true;
		PageUtil.copyModelToPage(model, draft, renderService);
		pageService.saveDraft(WikiUtil.getUser(), draft, type);
		
		return draft.getPageUuid();
	}

	//********************************************************************
	//               private methods
	//********************************************************************

	/**
	 * @param spaceUname
	 * @return
	 */
	private ArrayList<String> getSpaceAvailablePagesUuid(String spaceUname) {
		User viewer = WikiUtil.getUser();
		ArrayList<String> uuids = new ArrayList(pageService.getPagesUuidInSpace(spaceUname, viewer));
		return uuids;
	}

	private SpaceModel getSpace(String spaceUname) {
		
		User viewer = WikiUtil.getUser();
		if(!securityService.isAllowSpaceReading(spaceUname, viewer)){
			log.info("Space " + spaceUname + " no read perm on user " + viewer);
			return null;
		}
		
		Space space = spaceService.getSpaceByUname(spaceUname);
		
		if(space == null){
			log.info("Space " + spaceUname + " does not exist.");
			return null;
		}
		
		SpaceModel model = new SpaceModel();
		
		securityService.fillSpaceWikiOperations(WikiUtil.getUser(),space);
		SpaceUtil.copySpaceToModel(space, model, viewer, themeService);
		if(space.isRemoved())
			model.delayRemoveHours = Integer.valueOf(spaceService.getRemovedSpaceLeftHours(spaceUname)).toString();
		
		//special for offline model value
		model.homepageUuid = space.getHomepage() != null?space.getHomepage().getPageUuid():null;
		
		return model;
	}
	private PageListModel getSpacePages(String spaceUname,  int option, Long touched) {
		Date touchedDate = (touched==null || touched ==0)? null :new Date(touched);
		
		PageListModel model = new PageListModel();
		
		User viewer = WikiUtil.getUser();
		List<Page> list = pageService.getPagesInSpace(spaceUname, touchedDate, 0, viewer);
		if(list != null){
			for (Page page : list) {
				if(page == null)
					continue;
	
				PageModel pModel = new PageModel();
				renderService.renderHTML(page);
				
				PageUtil.copyPageToModel(page, pModel, userReadingService, PageUtil.NOT_COPY_ATTACHMENT);
					
				//some special value only for offline sync
				pModel.isHistory = false;
				
				if((option & SharedConstants.OPTION_SYNC_ATTACHMENT) > 0){
					//Special requirement: need put all attachments URL to PageModel.attachmentList 
					try {
						page.setAttachments(pageService.getPageAttachment(spaceUname, page.getPageUuid(),true,true,viewer));
					} catch (RepositoryException re) {
						log.error("Failed get page attachment in repository " + page.getPageUuid(), re);
					}
					List<FileNode> attList = page.getAttachments();
					if(attList != null && attList.size() > 0){
						pModel.attachmentNodes = new ArrayList<AttachmentModel>();
						for (FileNode node : attList) {
							//this node is manual draft or auto draft
							if(node.getStatus()  > 0){
								if(!StringUtils.equalsIgnoreCase(node.getCreateor(),viewer.getUsername())){
									//don't copy draft attachment which not belong to viewer 
									continue;
								}
							}
							AttachmentModel attModel = new AttachmentModel();
							PageUtil.copyAttachmentToModel(node,attModel,viewer);
							pModel.attachmentNodes.add(attModel);
						}
					}
				}
	
				if((option & SharedConstants.OPTION_SYNC_HISTORY) > 0){
					List<History> histories = pageService.getHistoryPages(spaceUname, page.getPageUuid(),0 , -1, touchedDate);
					for (History history : histories) {
						PageModel hModel = new PageModel();
						renderService.renderHTML(history);
						PageUtil.copyPageToModel(history, hModel, userReadingService, PageUtil.NOT_COPY_ATTACHMENT);

						//some special value only for offline sync
						hModel.isHistory = true;
						model.pages.add(hModel);
					}
					
				}
				if((option & SharedConstants.OPTION_SYNC_COMMENT) > 0){
					//offline button is space level, but space or some page may disable comment reading permission
					//need pre-check comment reading permission for each page
					
					boolean allowCommentRead = false;
					List<WikiOPERATIONS> perms = page.getWikiOperations();
					for (WikiOPERATIONS wikiOper: perms) {
						if(OPERATIONS.COMMENT_WRITE.equals(wikiOper.operation)){
							allowCommentRead = true;
							break;
						}
					}
					if(allowCommentRead){
						try {
							List<PageComment> comments = commentService.getPageComments(spaceUname,page.getPageUuid());
							if(comments != null && comments.size() > 0){
								pModel.commentList = new ArrayList<CommentModel>();
								for (PageComment comment : comments) {
									CommentModel cModel = PageUtil.copyCommentToModel(comment,viewer);
									
									//pageUid only use for offline model; bring back to save local DB. 
									cModel.pageUuid = page.getPageUuid();
									
									pModel.commentList.add(cModel);
								}
							}
						} catch (CommentException e) {
							log.error("Unable to get page comment for offline sync. PageUuid is " + page.getPageUuid());
						}
					}
				}
				
				
				model.pages.add(pModel);
			}
		}
		//must also check user is NOT anonymous, otherwise, PageService.getDraftPages() will throw AccessDenyException and redirect to login page
		if((option & SharedConstants.OPTION_SYNC_DRAFT) > 0 && !viewer.isAnonymous()){
			//get all draft, but must filter out the offline conlict draft
			List<Draft> drafts = pageService.getDraftPages(viewer,SharedConstants.NONE_DRAFT);
			for (Draft draft: drafts) {
				
				//keep other non-offline spaces draft is not good idea, here I only offline drafts which its spaces is offline 
				if(!StringUtils.equalsIgnoreCase(draft.getSpace().getUnixName(),spaceUname))
					continue;
				
				PageModel dModel = new PageModel();
				renderService.renderHTML(draft);
				PageUtil.copyPageToModel(draft, dModel, userReadingService, PageUtil.NOT_COPY_ATTACHMENT);
				
				//TODO: how to get attachment from draft: some draft may has not any saved version!
				//some special value only for offline sync
//				dModel.isHistory = true;
				model.pages.add(dModel);
			}
			
		}
		model.spaceUname = spaceUname;
		return model;
	}

	//********************************************************************
	//               Set / Get
	//********************************************************************
	public SpaceService getSpaceService() {
		return spaceService;
	}

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public SecurityService getSecurityService() {
		return securityService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public ThemeService getThemeService() {
		return themeService;
	}

	public void setThemeService(ThemeService themeService) {
		this.themeService = themeService;
	}
	public void setPageService(PageService pageService) {
		this.pageService = pageService;
	}
	public void setRenderService(RenderService renderService) {
		this.renderService = renderService;
	}
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public void setSecurityDummy(SecurityDummy securityDummy) {
		this.securityDummy = securityDummy;
	}

	public void setActivityLog(ActivityLogService activityLog) {
		this.activityLog = activityLog;
	}
	
}

