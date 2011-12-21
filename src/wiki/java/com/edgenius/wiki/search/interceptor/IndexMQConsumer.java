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
package com.edgenius.wiki.search.interceptor;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.dao.RoleDAO;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.Shell;
import com.edgenius.wiki.dao.CommentDAO;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.dao.PageTagDAO;
import com.edgenius.wiki.dao.SpaceDAO;
import com.edgenius.wiki.dao.SpaceTagDAO;
import com.edgenius.wiki.dao.WidgetDAO;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.PageTag;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.SpaceTag;
import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.search.service.IndexService;
import com.edgenius.wiki.security.service.SecurityService;

/**
 * @author Dapeng.Ni
 */
@Transactional(readOnly=true, propagation=Propagation.REQUIRES_NEW, noRollbackFor=Exception.class)
public class IndexMQConsumer {
	private static final Logger log = LoggerFactory.getLogger(IndexMQConsumer.class);
	
	//Please note: these inject are NOT auto-wired. If you add or delete, please also update applicationContext-search.xml.
	private IndexService indexService;
	private PageDAO pageDAO;
	private SpaceDAO spaceDAO;
	private CommentDAO commentDAO;
	private SecurityService securityService;
	private UserReadingService userReadingService;
	
	private PageTagDAO pageTagDAO;
	private SpaceTagDAO spaceTagDAO;
	private WidgetDAO widgetDAO;
	private RoleDAO roleDAO;
	
	public void handleMessage(Object msg){
		IndexMQObject mqObj;
		if(msg instanceof IndexMQObject){
			mqObj = (IndexMQObject) msg;
		}else{
			AuditLogger.error("Unexpected object in Index Counsumer " + msg);
			return;
		}
		
		boolean done = buildPageIndex(mqObj);
		if(done)
			return;
		done = buildSpaceIndex(mqObj);
		if(done)
			return;
		done = buildUserIndex(mqObj);
		if(done)
			return;
		done = buildPageTagIndex(mqObj);
		if(done)
			return;
		done = buildSpaceTagIndex(mqObj);
		if(done)
			return;
		done = buildAttachmentIndex(mqObj);
		if(done)
			return;
		done = buildCommentIndex(mqObj);
		if(done)
			return;
		done = buildWidgetIndex(mqObj);
		if(done)
			return;
		done = buildRoleIndex(mqObj);
		if(done)
			return;
	}
	


	//********************************************************************
	//               private methods
	//********************************************************************


	private boolean buildCommentIndex(IndexMQObject mqObj) {
		//create page tag
		if(mqObj.getType() == IndexMQObject.TYPE_INSERT_COMMENT){
			try {
				Integer uid = (Integer) mqObj.getObj();
				PageComment comment = commentDAO.get(uid);
				if(comment == null){
					log.error("Comment not found before index. UID: {}", uid);
					return true;
				}
				log.info("JMS message received for comment create.");
				indexService.saveOrUpdateComment(comment);
				log.info("Index success created comment. ");
			} catch (Exception e) {
				log.error("Index for comment create failed: " , e);
			}
			return true;
		}
		//remove comment
		if(mqObj.getType() == IndexMQObject.TYPE_REMOVE_COMMENT){
			try {
				Integer commentUid = (Integer) mqObj.getObj();
				log.info("JMS message received for comment index delete. Uid " + commentUid);
				indexService.removeComment(commentUid);
				log.info("Index success delete for comment index . Uid " + commentUid);
			} catch (Exception e) {
				log.error("Index for comment  remove failed: " , e);
			}
			return true;
		}
		return false;
	}



	private boolean buildAttachmentIndex(IndexMQObject mqObj) {
		//create attachment 
		String pageUuid = null;
		if(mqObj.getType() == IndexMQObject.TYPE_INSERT_ATTACHMENT_BATCH){
			try {
				log.info("Shell service for attachmnets create receieved");
				notifyShellAttachments((String) mqObj.getObj());
			} catch (Exception e) {
				log.error("Shell service request for attachment create failed.", e);
			}
			return true;
		}
		
		if(mqObj.getType() == IndexMQObject.TYPE_INSERT_ATTACHMENT){
			try {
				Object[] objs = (Object[]) mqObj.getObj();
				String spaceUname = (String) objs[0]; 
				FileNode node= (FileNode) objs[1]; 
				log.info("JMS message received for attachment create. Attachment name: " + node.getFilename());
				indexService.saveOrUpdateAttachment(spaceUname,node, false);
				log.info("Index success created Attachment. Attachment name: " + node.getFilename());
			} catch (Exception e) {
				log.error("Index for attachment create  failed: " , e);
			}
			return true;
		}
		//remove attachment
		if(mqObj.getType() == IndexMQObject.TYPE_REMOVE_ATTACHMENT){
			try {
				String[] tag= (String[]) mqObj.getObj();
				pageUuid = (String) tag[0];
				log.info("JMS message received for Attachment index delete. Node uuid: " + tag[1] + ". Version:" + tag[2]);
				indexService.removeAttachment(tag[1],tag[2]);
				log.info("Index success delete.  Node uuid: " + tag[1] + ". Version:" + tag[2]);
				
				notifyShellAttachments(pageUuid);
			} catch (Exception e) {
				log.error("Index for attachment  remove failed: " , e);
			}
			return true;
		}
		if(mqObj.getType() == IndexMQObject.TYPE_UPDATE_ATTACHMENT){
			try {
				Object[] objs = (Object[]) mqObj.getObj();
				String spaceUname = (String) objs[0]; 
				pageUuid = (String) objs[1]; 
				FileNode node= (FileNode) objs[2]; 
				log.info("JMS message received for Attachment meta data update. Attachment name: " + node.getFilename());
				indexService.saveOrUpdateAttachment(spaceUname,node, true);;
				log.info("Index success update.   Node uuid: " + objs[0]);
				
				notifyShellAttachments(pageUuid);
			} catch (Exception e) {
				log.error("Index for attachment update failed: " , e);
			}
			return true;
		}
		
		return false;
	}


	private void notifyShellAttachments(String pageUuid) {
		if(Shell.enabled && pageUuid != null){
			Page page = pageDAO.getCurrentByUuid(pageUuid);
			if(page != null && page.getSpace() != null && !page.getSpace().isPrivate() 
					&& !page.getSpace().containExtLinkType(Space.EXT_LINK_SHELL_DISABLED) 
					&& !StringUtils.isEmpty(page.getPageUuid())){
				//check if page allow anonymous to read
				if(securityService.isAllowPageReading(page.getSpace().getUnixName(), page.getPageUuid(), userReadingService.getUser(null))){
					boolean rs = Shell.notifyPageAttachments(page.getSpace().getUnixName(), pageUuid);
					log.info("Shell update page attachment return {}", rs);
				}
			}
		}
	}
	
	
	private boolean buildPageTagIndex(IndexMQObject mqObj) {
		//create page tag
		if(mqObj.getType() == IndexMQObject.TYPE_INSERT_PTAG_BATCH){
			try{
				String pageUuid = (String) mqObj.getObj();
				log.info("Shell service for page tag create received {}.", pageUuid);
				
				if(Shell.enabled && pageUuid != null){
					Page page = pageDAO.getCurrentByUuid(pageUuid);
					if(page != null && page.getSpace() != null && !page.getSpace().isPrivate() 
							&& !page.getSpace().containExtLinkType(Space.EXT_LINK_SHELL_DISABLED) 
							&& !StringUtils.isEmpty(page.getPageUuid())){
						//check if page allow anonymous to read
						if(securityService.isAllowPageReading(page.getSpace().getUnixName(), page.getPageUuid(), userReadingService.getUser(null))){
							boolean rs = Shell.notifyPageTags(page.getSpace().getUnixName(), pageUuid);
							log.info("Shell update page tags return {}", rs);
						}
					}
				}
			}catch(Exception e){
				log.error("Shell service for page tag failed." , e);
			}
			return true;
		}
		if(mqObj.getType() == IndexMQObject.TYPE_INSERT_PTAG){
			try {
				Integer uid = (Integer) mqObj.getObj();
				PageTag tag = pageTagDAO.get(uid);
				if(tag == null){
					log.error("PageTag not found before index. UID: {}", uid);
					return true;
				}
				log.info("JMS message received for page tag index create. Tag name: " + tag.getName());
				indexService.saveOrUpdatePageTag(tag);
				log.info("Index success created for tag. Tag name: " + tag.getName());
			} catch (Exception e) {
				log.error("Index for page tag create failed: " , e);
			}
			return true;
		}
		//remove page tag
		if(mqObj.getType() == IndexMQObject.TYPE_REMOVE_PTAG){
			try {
				PageTag tag= (PageTag) mqObj.getObj();
				log.info("JMS message received for page tag index delete. Tag name: " + tag.getName());
				indexService.removePageTag(tag.getName());
				log.info("Index for page tag  success delete. Tag name: " + tag.getName());
			} catch (Exception e) {
				log.error("Index for page tag  remove failed: " , e);
			}
			return true;
		}
		return false;
	}

	private boolean buildSpaceTagIndex(IndexMQObject mqObj) {
		//create space tag
		if(mqObj.getType() == IndexMQObject.TYPE_INSERT_STAG){
			try {
				Integer uid = (Integer) mqObj.getObj();
				SpaceTag tag = spaceTagDAO.get(uid);
				if(tag == null){
					log.error("SpaceTag not found before index. UID: {}", uid);
					return true;
				}
				log.info("JMS message received for space tag index create. Tag name: " + tag.getName());
				indexService.saveOrUpdateSpaceTag(tag);
				log.info("Index success created for space tag. Tag name: " + tag.getName());
			} catch (Exception e) {
				log.error("Index for space tag create failed: " , e);
			}
			return true;
		}
		//remove space tag
		if(mqObj.getType() == IndexMQObject.TYPE_REMOVE_STAG){
			try {
				SpaceTag tag= (SpaceTag) mqObj.getObj();
				log.info("JMS message received for space tag index delete. Tag name: " + tag.getName());
				indexService.removeSpaceTag(tag.getName());
				log.info("Index success space tag delete. Tag name: " + tag.getName());
			} catch (Exception e) {
				log.error("Index for space tag remove failed: " , e);
			}
			return true;
		}
		return false;
	}



	private boolean buildRoleIndex(IndexMQObject mqObj) {
		if(mqObj.getType() == IndexMQObject.TYPE_INSERT_ROLE){
			try {
				Integer uid = (Integer) mqObj.getObj();
				Role role = roleDAO.get(uid);
				if(role == null){
					log.error("Role not found before index. UID: {}", uid);
					return true;
				}
				log.info("JMS message received for role index create. Role name: " + role.getName());
				indexService.saveOrUpdateRole(role);
				log.info("Index success created for role. Role name: " + role.getName());
			} catch (Exception e) {
				log.error("Index for role create failed: " , e);
			}
			return true;
		}
		return false;
	}
	private boolean buildUserIndex(IndexMQObject mqObj) {
		//create user
		if(mqObj.getType() == IndexMQObject.TYPE_INSERT_USER){
			try {
				User user= (User) mqObj.getObj();
				log.info("JMS message received for user index create. Username: " + user.getUsername());
				indexService.saveOrUpdateUser(user);
				log.info("Index success created for user. Username: " + user.getUsername());
			} catch (Exception e) {
				log.error("Index for user create failed: " , e);
			}
			return true;
		}
		//remove user
		if(mqObj.getType() == IndexMQObject.TYPE_REMOVE_USER){
			try {
				String username	= (String) mqObj.getObj();
				log.info("JMS message received for user index delete. Username: " + username);
				indexService.removeUser(username);
				log.info("Index for user success delete. Username: " + username);
			} catch (Exception e) {
				log.error("Index for user remove failed: " , e);
			}
			return true;
		}
		return false;
	}

	private boolean buildSpaceIndex(IndexMQObject mqObj) {
		//create space
		if(mqObj.getType() == IndexMQObject.TYPE_INSERT_SPACE){
			try {
				Integer uid = (Integer) mqObj.getObj();
				Space space = spaceDAO.get(uid); 
				if(space == null){
					log.error("Space not found before index. UID: {}", uid);
					return true;
				}
				log.info("JMS message received for space index create. Title: " + space.getName() + ". Unixname:" + space.getUnixName());
				indexService.saveOrUpdateSpace(space);
				log.info("Index success created for space. Title: " + space.getName() + ". Unixname:" + space.getUnixName());
				
				// Shell service
				if(Shell.enabled && !space.isPrivate() && !space.containExtLinkType(Space.EXT_LINK_SHELL_DISABLED) && !StringUtils.isEmpty(space.getUnixName())){
					boolean rs = Shell.notifySpaceCreate(space.getUnixName());
					log.info("Shell save/update space service return {}", rs);
				}
			} catch (Exception e) {
				log.error("Index for space create failed: " , e);
			}
			return true;
		}
		//remove space
		if(mqObj.getType() == IndexMQObject.TYPE_REMOVE_SPACE){
			try {
				String spaceUname = (String) mqObj.getObj();
				log.info("JMS message received for space index delete. Uname: " + spaceUname);
				indexService.removeSpace(spaceUname);
				log.info("Index for space  delete success. Uname: " + spaceUname);
				
				// Shell service - please note, here doesn't pass Space.extLinkType, some reasons:
				//First, original design doesn't pass space object, and this moment, space can not get from database as well.
				//I don't want to add more new parameter so  far.
				//Second, it is not harmful to always try to notify shell to remove space. 
				if(Shell.enabled && !StringUtils.isEmpty(spaceUname)){
					boolean rs = Shell.notifySpaceRemove(spaceUname);
					log.info("Shell remove space service return {}", rs);
				}
			} catch (Exception e) {
				log.error("Index for space  remove failed: " , e);
			}
			return true;
		}
		return false;
	}

	private boolean buildPageIndex(IndexMQObject mqObj) {
		//add page
		if(mqObj.getType() == IndexMQObject.TYPE_INSERT_PAGE){
			Page page = null;
			try {
				Integer uid = (Integer) mqObj.getObj();
				page = pageDAO.get(uid); 
				if(page == null){
					log.error("Page not found before index. UID: {}", uid);
					return true;
				}
				//index need render, however some render need permission check, such as PageIndexMacro will call PageService.getPageTree() 
				//which needs space read permission, so here just login as space admin.
				securityService.proxyLoginAsSpaceAdmin(page.getSpace().getUnixName());
				
				log.info("JMS message received for page index create. Title: " + page.getTitle() + ". Uuid:" + page.getPageUuid());
				indexService.saveOrUpdatePage(page);
				log.info("Index success created . Title: " + page.getTitle() + ". Uuid:" + page.getPageUuid());
				
			} catch (Exception e) {
				log.error("Index create failed for page", e);
			} finally{
				securityService.proxyLogout();
			}
			
			// Shell service - better after securityService.proxyLogout();
			if(Shell.enabled && page != null && page.getSpace() != null && !page.getSpace().isPrivate() 
					&& !page.getSpace().containExtLinkType(Space.EXT_LINK_SHELL_DISABLED) 
					&& !StringUtils.isEmpty(page.getPageUuid())){
				//check if page allow anonymous to read
				if(securityService.isAllowPageReading(page.getSpace().getUnixName(), page.getPageUuid(), userReadingService.getUser(null))){
					//in this scenarios, don't acquire page attachments, as AttachmentIndexIntercepor will send update attachments request to shell.
					boolean rs = Shell.notifyPageCreate(page.getSpace().getUnixName(), page.getPageUuid(),false);
					log.info("Shell save/update page service return {}", rs);
				}
			}
			
			return true;
		}
//		remove page
		if(mqObj.getType() == IndexMQObject.TYPE_REMOVE_PAGE){
			try {
				String[] objs = ((String[]) mqObj.getObj());
				String spaceUname = objs[0];
				String removedPageUuid = objs[1];
				log.info("JMS message received for page index delete. Page Uuid: " + removedPageUuid);
				indexService.removePage(removedPageUuid);
				log.info("Index success delete. Page Uuid: " + removedPageUuid);
				

				// Shell service - here doesn't do any checking if this page is already used shell service....
				if(Shell.enabled && !StringUtils.isEmpty(spaceUname) && !StringUtils.isEmpty(removedPageUuid)){
					boolean rs = Shell.notifyPageRemoved(spaceUname, removedPageUuid);
					log.info("Shell remove page service return {}", rs);
				}
			} catch (Exception e) {
				log.error("Index remove failed for page.",e);
			}
			return true;
		}
		return false;
	}
	private boolean buildWidgetIndex(IndexMQObject mqObj) {
		//create widget
		if(mqObj.getType() == IndexMQObject.TYPE_INSERT_WIDGET){
			try {
				Integer uid = (Integer) mqObj.getObj();
				Widget widget = widgetDAO.get(uid);
				if(widget == null){
					log.error("Widget not found before index. UID: {}", uid);
					return true;
				}
				log.info("JMS message received for widget index create. Title: " + widget.getTitle() + ". Key:" + widget.getUuid());
				indexService.saveOrUpdateWidget(widget);
				log.info("Index success created for widget. Title: " + widget.getTitle() + ". Key:" + widget.getUuid());
			} catch (Exception e) {
				log.error("Index for widget create failed: " , e);
			}
			return true;
		}
		//remove widget
		if(mqObj.getType() == IndexMQObject.TYPE_REMOVE_WIDGET){
			try {
				String key = (String) mqObj.getObj();
				log.info("JMS message received for widget index delete. Widget Key: " + key);
				indexService.removeWidget(key);
				log.info("Index for widget delete success. key: " + key);
			} catch (Exception e) {
				log.error("Index for widget  remove failed: " , e);
			}
			return true;
		}
		return false;
	}
	//********************************************************************
	//               set / get
	//********************************************************************
	public void setIndexService(IndexService indexService) {
		this.indexService = indexService;
	}
	public void setPageDAO(PageDAO pageDAO) {
		this.pageDAO = pageDAO;
	}
	public void setSpaceDAO(SpaceDAO spaceDAO) {
		this.spaceDAO = spaceDAO;
	}
	public void setCommentDAO(CommentDAO commentDAO) {
		this.commentDAO = commentDAO;
	}
	public void setPageTagDAO(PageTagDAO pageTagDAO) {
		this.pageTagDAO = pageTagDAO;
	}
	public void setSpaceTagDAO(SpaceTagDAO spaceTagDAO) {
		this.spaceTagDAO = spaceTagDAO;
	}
	public void setWidgetDAO(WidgetDAO widgetDAO) {
		this.widgetDAO = widgetDAO;
	}
	public void setRoleDAO(RoleDAO roleDAO) {
		this.roleDAO = roleDAO;
	}
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}
}
