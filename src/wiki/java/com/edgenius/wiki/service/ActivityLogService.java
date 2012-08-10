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
package com.edgenius.wiki.service;

import java.util.List;

import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.wiki.WikiConstants.REGISTER_METHOD;
import com.edgenius.wiki.gwt.client.model.RenderMarkupModel;
import com.edgenius.wiki.model.ActivityLog;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.Space;

/**
 * @author Dapeng.Ni
 */
public interface ActivityLogService {
	String SERVICE_NAME = "activityLog";
	/**
	 * Get specified user's activities, i.e., the user is activity creator.
	 * @param startUid is the ActivityLog.uid, return list has uid less than it. Return from latest activity if it is less or equal to zero. 
	 * @param count
	 * @param user
	 * @return
	 */
	List<ActivityLog> getUserActivities(int startUid, int count, User user, User viewer);
	/**
	 * Get all activities.
	 * @param startUid
	 * @param count
	 * @param viewer
	 * @return
	 */
	List<ActivityLog> getActivities(int startUid, int count, User viewer);
	
	void logPageSaved(Page page);
	void logPageRestored(Page page);
	void logPageRemoved(Page page, boolean permanent, User activityRequester);
	void logPageCopied(Page src, Page tgt);
	void logPageMoved(Page src, Page tgt);
	void logPageReverted(Page page, int version);
	
	void logComment(PageComment comment);
	void logAttachmentUploaded(String spaceUname, String pageTitle, User creator, List<FileNode> attachment);
	void logAttachmentRemoved(String spaceUname, String pageTitle, User creator, FileNode attachment);
	 
	void logSpaceCreated(Space space);
	void logSpaceRemoved(Space space,User requestor, boolean permanent);
	void logSpaceRestored(String spaceUname, String name, User user);
	
	/**
	 * 
	 * @param user
	 * @param registerStatus signup or approval. If null, treat as sign-up is approved.
	 */
	void logUserSignup(User user, REGISTER_METHOD registerStatus);
	void logUserStatusUpdate(User user);
	void logUserFollowing(User user, User follower);
	void logUserUnFollowing(User user, User follower);
	
	/**
	 * Please ensure only "SAVE" but not "UPDATE" by activity because of pagination sorted by an incremental UID.
	 * Update won't impact the UID then may reflect wrong pagination list.
	 * 
	 * @param activity
	 */
	void save(ActivityLog activity);

	/**
	 * @return
	 */
	List<ActivityLog> getByTarget(int typeCode, int subTypeCode, int tgtResourceType, String tgtResourceName);
	List<ActivityLog> getBySource(int typeCode, int subTypeCode, int srcResourceType, String srcResourceName);

	/**
	 * 
	 */
	void purgeActivityLog(int days);
	/**
	 * @param list
	 * @return
	 */
	RenderMarkupModel renderActivities(List<ActivityLog> list);

}
