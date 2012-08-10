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
package com.edgenius.wiki.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.SecurityValues;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.DateUtil;
import com.edgenius.wiki.ActivityType;
import com.edgenius.wiki.ActivityType.SubType;
import com.edgenius.wiki.WikiConstants.REGISTER_METHOD;
import com.edgenius.wiki.dao.ActivityLogDAO;
import com.edgenius.wiki.gwt.client.model.RenderMarkupModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.BooleanUtil;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.UserUtil;
import com.edgenius.wiki.model.ActivityLog;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.ActivityLogService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@Transactional
public class ActivityLogServiceImpl implements ActivityLogService {
	private static final Logger log = LoggerFactory.getLogger(ActivityLogServiceImpl.class);
	
	@Autowired private ActivityLogDAO activityLogDAO;
	
	@Autowired private MessageService messageService; 
	@Autowired private SecurityService securityService; 
	@Autowired private UserReadingService userReadingService;
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// View 
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	@Transactional(readOnly=true)
	public List<ActivityLog> getUserActivities(int start, int count, User user, User viewer){
		
		List<ActivityLog> activities = activityLogDAO.getByUser(start,count, user);
		if(activities == null || activities.size() == 0){
			return new ArrayList<ActivityLog>();
		}
		
		return buildActivitiesMessage(viewer, activities);
	}
	
	@Transactional(readOnly=true)
	public List<ActivityLog> getActivities(int start, int count, User viewer){
		
		List<ActivityLog> logs = new ArrayList<ActivityLog>();
		
		int retry =0;
		do{
			//get double size of expected count, to minor database read.
			List<ActivityLog> activities = activityLogDAO.getByCount(start,count*2);
			if(activities == null || activities.size() == 0){
				break;
			}
			
			//here, some log messages maybe filter out for permission reason
			logs.addAll(buildActivitiesMessage(viewer, activities));
			//already get all logs in database
			if(count <= 0)
				break;
			
			//try to read more log
			start = start + (count*2);
			retry++;
		}while(logs.size() < count && retry < 5);
		
		if(logs.size() > count){
			logs = logs.subList(0, count);
		}
		return logs;
	}
	/**
	 * @param viewer
	 * @param msgs
	 * @param activities
	 * @return 
	 */
	private List<ActivityLog> buildActivitiesMessage(User viewer,  List<ActivityLog> activities) {
		String spaceUname;
		List<ActivityLog> retActivities = new ArrayList<ActivityLog>();
		for (ActivityLog activity : activities) {
			spaceUname = null;
			if(activity.getType() == ActivityType.Type.PAGE_EVENT.getCode()
				||activity.getType() ==  ActivityType.Type.COMMENT_EVENT.getCode()
				||activity.getType() ==  ActivityType.Type.ATTACHMENT_EVENT.getCode()){
				 spaceUname = activity.getExtroInfo();
			}else if(activity.getType() ==  ActivityType.Type.SPACE_EVENT.getCode()){
				spaceUname = activity.getSrcResourceName();
			}
			//for activities, only private space stuff(space, page, comment) needs to be check if hide or not.
			if(spaceUname != null && securityService.isPrivateSpace(spaceUname)){
				if(!securityService.isAllowSpaceReading(spaceUname, viewer)){
					log.debug("Activity {} is private and not allow to view on user {}",activity.getType(),viewer);
					continue;
				}
			}
			String msg = null;
			if(activity.getType() == ActivityType.Type.PAGE_EVENT.getCode()){
				if(activity.getSubType() == ActivityType.SubType.COPY.getCode() 
					|| activity.getSubType() == ActivityType.SubType.MOVE.getCode()){
					//moves page {1} to {2} in space {3}  #title, new title and  space, time
					msg = messageService.getMessage("activity.page."+SubType.valueOfCode(activity.getSubType())
							, new Object[]{
						WikiUtil.getPageRelativeTokenLink(activity.getExtroInfo(), activity.getSrcResourceName(),null)
						,WikiUtil.getPageRelativeTokenLink(activity.getExtroInfo(), activity.getTgtResourceName(),null)
						,activity.getExtroInfo()
						,DateUtil.toDisplayDateWithPrep(viewer, activity.getCreatedDate(),messageService)});
						
				}else if(activity.getSubType() == ActivityType.SubType.REVERT.getCode()){
					//Reverts page {1} from history version {2} {3}  # title, version, time
					msg = messageService.getMessage("activity.page."+SubType.valueOfCode(activity.getSubType())
							, new Object[]{
						WikiUtil.getPageRelativeTokenLink(activity.getExtroInfo(), activity.getSrcResourceName(),null)
						,activity.getTgtResourceName()
						,DateUtil.toDisplayDateWithPrep(viewer, activity.getCreatedDate(),messageService)});
						
				}else{
					//ActivityType.SubType.CREATE, UPDATE, RESTORE, PERMANENT_DELETE or DELETE
					//Creates new page {1} {2}  #title, time
					msg = messageService.getMessage("activity.page."+SubType.valueOfCode(activity.getSubType())
							, new Object[]{
						WikiUtil.getPageRelativeTokenLink(activity.getExtroInfo(), activity.getSrcResourceName(),null)
						,DateUtil.toDisplayDateWithPrep(viewer, activity.getCreatedDate(),messageService)});
						
						
				}
			}else if(activity.getType() == ActivityType.Type.COMMENT_EVENT.getCode()){
				//ActivityType.SubType.CREATE, UPDATE
				//Posts  comment on page {1} {2} #  page title, time
				msg = messageService.getMessage("activity.comment."+SubType.valueOfCode(activity.getSubType())
						, new Object[]{
					WikiUtil.getPageRelativeTokenLink(activity.getExtroInfo(), activity.getTgtResourceName(),null)	
					,DateUtil.toDisplayDateWithPrep(viewer, activity.getCreatedDate(),messageService)});
					
			}else if(activity.getType() == ActivityType.Type.ATTACHMENT_EVENT.getCode()){
				//ActivityType.SubType.CREATE, DELETE
				//uploads attachment {1} to page {2} {3} #  attachment title, page title, time
				msg = messageService.getMessage("activity.attachment."+SubType.valueOfCode(activity.getSubType())
						, new Object[]{
					activity.getSrcResourceName()
					,WikiUtil.getPageRelativeTokenLink(activity.getExtroInfo(), activity.getTgtResourceName(),null)	
					,DateUtil.toDisplayDateWithPrep(viewer, activity.getCreatedDate(),messageService)});
					
			}else if(activity.getType() == ActivityType.Type.SPACE_EVENT.getCode()){
				//ActivityType.SubType.CREATE, RESTORE,PERMANENT_DELETE,DELETE
				//creates new space {0} {1} # space, time
				//restored space {0} {1}  #space,  time
				//puts space {0} to trash bin {1}  # space, time
				//Deleted space {0} permanently {1}  #space, time
				msg = messageService.getMessage("activity.space."+SubType.valueOfCode(activity.getSubType())
						, new Object[]{
					//this space is delete by Quartz job, so mark the user as "System" rather than anonymous.
					//extra info is space name.
					WikiUtil.getSpaceRelativeTokenLink(activity.getSrcResourceName(), activity.getExtroInfo())	
					,DateUtil.toDisplayDateWithPrep(viewer, activity.getCreatedDate(),messageService)});
					
			}else if(activity.getType() == ActivityType.Type.USER_EVENT.getCode()){
				if(activity.getSubType() == ActivityType.SubType.CREATE.getCode()){
					//Sign-up {0}.
				    
				    String status = StringUtils.trimToNull(activity.getExtroInfo());
				    if(status == null){
				        msg =messageService.getMessage("activity.user.approved."+SubType.valueOfCode(activity.getSubType())
                                , new Object[]{DateUtil.toDisplayDateWithPrep(viewer, activity.getCreatedDate(),messageService)});
				    }else if(REGISTER_METHOD.approval.name().equals(status)){
				        msg =messageService.getMessage("activity.user.wait."+SubType.valueOfCode(activity.getSubType())
	                            , new Object[]{DateUtil.toDisplayDateWithPrep(viewer, activity.getCreatedDate(),messageService)});
				    }else{ //REGISTER_METHOD.signup.name().equals(status)
				        msg =messageService.getMessage("activity.user."+SubType.valueOfCode(activity.getSubType())
					        , new Object[]{DateUtil.toDisplayDateWithPrep(viewer, activity.getCreatedDate(),messageService)});
				    }
					
				}else if(activity.getSubType() == ActivityType.SubType.UPDATE.getCode()){
					//Updates status {1} {2}.
					msg = messageService.getMessage("activity.user."+SubType.valueOfCode(activity.getSubType())
							, new Object[]{
					activity.getExtroInfo()
					,DateUtil.toDisplayDateWithPrep(viewer, activity.getCreatedDate(),messageService)});
					
				}else if(activity.getSubType() == ActivityType.SubType.FOLLOW.getCode()
					|| activity.getSubType() == ActivityType.SubType.UNFOLLOW.getCode()){
					//{0} follows {1} {2}.
					msg = messageService.getMessage("activity.user."+SubType.valueOfCode(activity.getSubType())
							, new Object[]{
						//this space is delete by Quartz job, so mark the user as "System" rather than anonymous.
						getUserLink(userReadingService.getUserByName(activity.getSrcResourceName()))
						,getUserLink(userReadingService.getUserByName(activity.getTgtResourceName()))
						,DateUtil.toDisplayDateWithPrep(viewer, activity.getCreatedDate(),messageService)});
						
				}
			}else if(activity.getType() == ActivityType.Type.SYSTEM_EVENT.getCode()){
				if(activity.getSubType() == ActivityType.SubType.VERSION_PING.getCode()){
					msg = messageService.getMessage("activity.system."+SubType.valueOfCode(activity.getSubType())
							, new Object[]{activity.getExtroInfo()});
					
				}else if(activity.getSubType() == ActivityType.SubType.REDEPLOY_SHELL.getCode()){
					msg = messageService.getMessage("activity.system."+SubType.valueOfCode(activity.getSubType())
							, new Object[]{DateUtil.toDisplayDateWithPrep(viewer, activity.getCreatedDate(),messageService)});
					
				}else  if(activity.getSubType() == ActivityType.SubType.REBUILD_INDEX.getCode()){
					if(activity.getStatus() > 0){
						msg = messageService.getMessage("activity.system."+SubType.valueOfCode(activity.getSubType())
							, new Object[]{DateUtil.toDisplayDateWithPrep(viewer, activity.getCreatedDate(),messageService)});
					}
				}
			}
			
			if(msg != null){
				activity = (ActivityLog) activity.clone();
				//anonymous
				if(activity.getCreator() == null)
					activity.setCreator(userReadingService.getUserByName(null));
				//system
				if((activity.getType() == ActivityType.Type.SPACE_EVENT.getCode() 
					&& activity.getSubType() == ActivityType.SubType.PERMANENT_DELETE.getCode())
					|| activity.getType() == ActivityType.Type.SYSTEM_EVENT.getCode()){
					activity.setCreator(null);
				}
				
				activity.setMessage(msg);
				retActivities.add(activity);
			}else{
				AuditLogger.error("System has unknown activitiy can not be render: " + activity);
			}
		}
		
		return retActivities;
	}

	/**
	 * @param activity
	 * @return
	 */
	private String getUserLink(User user) {
		return user.getFullname();
	}

	// Page log
	public void logPageSaved(Page page){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.PAGE_EVENT.getCode());
			activity.setSubType(page.getVersion() == 1?ActivityType.SubType.CREATE.getCode():ActivityType.SubType.UPDATE.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.PAGE.ordinal());
			activity.setSrcResourceName(page.getTitle());
			
			//put spaceUname as extro info so that easy to do security check at retrieving
			activity.setExtroInfo(page.getSpace().getUnixName());
			
			activity.setCreator(page.getModifier());
			activity.setCreatedDate(page.getModifiedDate());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for save page",e);
		}
	}
	public void logPageRestored(Page page){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.PAGE_EVENT.getCode());
			activity.setSubType(ActivityType.SubType.RESTORE.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.PAGE.ordinal());
			activity.setSrcResourceName(page.getTitle());
			
			//put spaceUname as extro info so that easy to do security check at retrieving
			activity.setExtroInfo(page.getSpace().getUnixName());
			
			activity.setCreator(page.getModifier());
			activity.setCreatedDate(page.getModifiedDate());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for save page",e);
		}
	}
	
	public void logPageRemoved(Page page, boolean permanent, User activityRequester){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.PAGE_EVENT.getCode());
			activity.setSubType(permanent?ActivityType.SubType.PERMANENT_DELETE.getCode():ActivityType.SubType.DELETE.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.PAGE.ordinal());
			activity.setSrcResourceName(page.getTitle());
			
			//put spaceUname as extro info so that easy to do security check at retrieving
			activity.setExtroInfo(page.getSpace().getUnixName());
			
			activity.setCreator(activityRequester);
			activity.setCreatedDate(new Date());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for remove page",e);
		}
	}
	public void logPageCopied(Page src, Page tgt){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.PAGE_EVENT.getCode());
			activity.setSubType(ActivityType.SubType.COPY.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.PAGE.ordinal());
			activity.setSrcResourceName(src.getTitle());
			activity.setTgtResourceType(SecurityValues.RESOURCE_TYPES.PAGE.ordinal());
			activity.setTgtResourceName(tgt.getTitle());
			
			//put spaceUname as extro info so that easy to do security check at retrieving
			activity.setExtroInfo(src.getSpace().getUnixName());
			
			activity.setCreator(tgt.getModifier());
			activity.setCreatedDate(tgt.getModifiedDate());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for copy page",e);
		}
	}
	public void logPageMoved(Page src, Page tgt){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.PAGE_EVENT.getCode());
			activity.setSubType(ActivityType.SubType.MOVE.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.PAGE.ordinal());
			activity.setSrcResourceName(src.getTitle());
			activity.setTgtResourceType(SecurityValues.RESOURCE_TYPES.PAGE.ordinal());
			activity.setTgtResourceName(tgt.getTitle());
			
			//put spaceUname as extro info so that easy to do security check at retrieving
			activity.setExtroInfo(src.getSpace().getUnixName());
			
			activity.setCreator(tgt.getModifier());
			activity.setCreatedDate(tgt.getModifiedDate());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for move page",e);
		}
	}
	
	public void logPageReverted(Page page, int version){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.PAGE_EVENT.getCode());
			activity.setSubType(ActivityType.SubType.REVERT.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.PAGE.ordinal());
			activity.setSrcResourceName(page.getTitle());
			activity.setTgtResourceName(String.valueOf(version));
			
			//put spaceUname as extro info so that easy to do security check at retrieving
			activity.setExtroInfo(page.getSpace().getUnixName());
			
			activity.setCreator(page.getModifier());
			activity.setCreatedDate(page.getModifiedDate());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for revert page",e);
		}
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Comment log
	public void logComment(PageComment comment){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.COMMENT_EVENT.getCode());
			activity.setSubType(comment.getParent() == null?ActivityType.SubType.CREATE.getCode():ActivityType.SubType.UPDATE.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.COMMENT.ordinal());
			activity.setSrcResourceName(comment.getUid().toString());
			activity.setTgtResourceName(comment.getPage().getTitle());
			
			//put spaceUname as extro info so that easy to do security check at retrieving
			activity.setExtroInfo(comment.getPage().getSpace().getUnixName());
			
			activity.setCreator(comment.getModifier());
			activity.setCreatedDate(comment.getModifiedDate());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for comment",e);
		}
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Attachment log
	public void logAttachmentUploaded(String spaceUname, String pageTitle, User creator, List<FileNode> attachment){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.ATTACHMENT_EVENT.getCode());
			activity.setSubType(ActivityType.SubType.CREATE.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.ATTACHMENT.ordinal());
			//put spaceUname as extro info so that easy to do security check at retrieving
			StringBuffer files = new StringBuffer();
			for (FileNode fileNode : attachment) {
				files.append(fileNode.getFilename()).append(",");
			}
			if(files.length() > 0){
				//delete last comma
				files.deleteCharAt(files.length()-1); 
			}
			
			activity.setSrcResourceName(files.toString());
			activity.setTgtResourceName(pageTitle);
			
			activity.setExtroInfo(spaceUname);
			
			activity.setCreator(creator);
			activity.setCreatedDate(new Date());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for comment",e);
		}
	}
	public void logAttachmentRemoved(String spaceUname, String pageTitle, User creator, FileNode attachment){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.ATTACHMENT_EVENT.getCode());
			activity.setSubType(ActivityType.SubType.DELETE.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.ATTACHMENT.ordinal());
			activity.setSrcResourceName(attachment.getFilename().toString());
			activity.setTgtResourceName(pageTitle);
			
			activity.setExtroInfo(spaceUname);
			
			activity.setCreator(creator);
			activity.setCreatedDate(new Date());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for comment",e);
		}
	}
	
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Space log
	public void logSpaceCreated(Space space){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.SPACE_EVENT.getCode());
			activity.setSubType(ActivityType.SubType.CREATE.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.SPACE.ordinal());
			activity.setSrcResourceName(space.getUnixName());
			activity.setExtroInfo(space.getName());
			
			activity.setCreator(space.getCreator());
			activity.setCreatedDate(space.getCreatedDate());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for create space",e);
		}
	}
	public void logSpaceRemoved(Space space, User requestor, boolean permanent){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.SPACE_EVENT.getCode());
			activity.setSubType(permanent?ActivityType.SubType.PERMANENT_DELETE.getCode():ActivityType.SubType.DELETE.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.SPACE.ordinal());
			activity.setSrcResourceName(space.getUnixName());
			activity.setExtroInfo(space.getName());
			
			activity.setCreator(requestor);
			activity.setCreatedDate(space.getModifiedDate());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for remove space",e);
		}
	}
	public void logSpaceRestored(String spaceUname, String name, User requestor){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.SPACE_EVENT.getCode());
			activity.setSubType(ActivityType.SubType.RESTORE.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.SPACE.ordinal());
			activity.setSrcResourceName(spaceUname);
			activity.setExtroInfo(name);
			
			activity.setCreator(requestor);
			activity.setCreatedDate(new Date());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for undo remove space",e);
		}
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User log
	public void logUserSignup(User user, REGISTER_METHOD registerStatus){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.USER_EVENT.getCode());
			activity.setSubType(ActivityType.SubType.CREATE.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.USER.ordinal());
			activity.setSrcResourceName(user.getUsername());
			
			//put user enable status here, this will check if the signup needs to be approved.
			activity.setExtroInfo(registerStatus==null?null:registerStatus.name());
			
			activity.setCreator(user);
			activity.setCreatedDate(user.getCreatedDate());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for user sign-up",e);
		}
	}
	
	public void logUserStatusUpdate(User user){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.USER_EVENT.getCode());
			activity.setSubType(ActivityType.SubType.UPDATE.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.USER.ordinal());
			activity.setSrcResourceName(user.getUsername());
			activity.setExtroInfo(StringUtils.abbreviate(user.getSetting().getStatus(),120));
			
			activity.setCreator(user);
			activity.setCreatedDate(new Date());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for user update status",e);
		}
	}
	public void logUserFollowing(User user, User follower){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.USER_EVENT.getCode());
			activity.setSubType(ActivityType.SubType.FOLLOW.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.USER.ordinal());
			activity.setSrcResourceName(user.getUsername());
			activity.setTgtResourceType(SecurityValues.RESOURCE_TYPES.USER.ordinal());
			activity.setTgtResourceName(follower.getUsername());
			
			activity.setCreator(user);
			activity.setCreatedDate(new Date());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for following",e);
		}
	}
	public void logUserUnFollowing(User user, User follower){
		try {
			ActivityLog activity = new ActivityLog();
			activity.setType(ActivityType.Type.USER_EVENT.getCode());
			activity.setSubType(ActivityType.SubType.UNFOLLOW.getCode());
			activity.setSrcResourceType(SecurityValues.RESOURCE_TYPES.USER.ordinal());
			activity.setSrcResourceName(user.getUsername());
			activity.setTgtResourceType(SecurityValues.RESOURCE_TYPES.USER.ordinal());
			activity.setTgtResourceName(follower.getUsername());
			
			activity.setCreator(user);
			activity.setCreatedDate(new Date());
			this.save(activity);
		} catch (Exception e) {
			log.warn("Activity log failed for unfollowing",e);
		}
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// General log method
	public void save(ActivityLog activity){
		activityLogDAO.saveOrUpdate(activity);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Others...
	public List<ActivityLog> getByTarget(int typeCode, int subTypeCode, int tgtResourceType, String tgtResourceName) {
		return  activityLogDAO.getByTarget(typeCode,subTypeCode, tgtResourceType,tgtResourceName);
	}

	public void purgeActivityLog(int days) {
		activityLogDAO.removeOldByDays(days);  
		
	}

	public List<ActivityLog> getBySource(int typeCode, int subTypeCode, int srcResourceType, String srcResourceName) {
		return  activityLogDAO.getBySource(typeCode,subTypeCode, srcResourceType,srcResourceName);
	}

	/**
	 * @param list
	 * @return
	 */
	public RenderMarkupModel renderActivities(List<ActivityLog> list) {
		ArrayList<RenderPiece> pieces = new ArrayList<RenderPiece>();
		if(list != null && list.size() > 0){
			User user = null;
			boolean first = true; //as user==null is not enough to tell it is first line because log.getCreator() could be null 
			for (ActivityLog log : list) {
				if(first || (user != null && !user.equals(log.getCreator())) || (user == null && log.getCreator() != null)){
					first = false;
					
					pieces.add(new TextModel("<div class='widget-activity-user'>"));
					user = log.getCreator();
					if(user == null){
						//TODO: hardcode for "System"
						//system user, no link on it
						pieces.add(new TextModel(GwtUtils.getUserPortraitHTML(UserUtil.getPortraitUrl(null),
								"System", SharedConstants.PORTRAIT_SIZE_SMALL)
								+"<div class='username'>System</div>"));
					}else{
						//hover popup user
						pieces.add(new TextModel(GwtUtils.getUserPortraitHTML(UserUtil.getPortraitUrl(user.getPortrait())
								, user.getFullname(),SharedConstants.PORTRAIT_SIZE_SMALL)
								+"<div class='username'>"));
						
						pieces.add(WikiUtil.createUserLinkModel(user));
						pieces.add(new TextModel("</div>"));
					}
					
					pieces.add(new TextModel("</div>"));
					
				}
				pieces.add(new TextModel("<div class='widget-activity-text'>"
						+log.getMessage()+"</div>"));
			}
		}
		RenderMarkupModel model = new RenderMarkupModel();
		model.renderContent = pieces;
		return model;
	}
}
