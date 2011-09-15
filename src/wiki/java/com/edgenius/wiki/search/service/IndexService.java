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
package com.edgenius.wiki.search.service;

import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.PageTag;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.SpaceTag;
import com.edgenius.wiki.model.Widget;

/**
 * @author Dapeng.Ni
 */
public interface IndexService {
	String SERVICE_NAME = "indexService";
	//method call in spring ApplicationContext
	void initOptimizeJob();
	void cleanIndexes(IndexRebuildListener listener);
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// don't call these method directly, as they are only for rebuildIndex() for multiple thread usage, to put them here just for
	// OpenSessionInViewInterceptor
//	void rebuildPageIndex();		
//	void rebuildCommentIndex();		
//	void rebuildSpaceIndex();
//	void rebuildUserIndex();
//	void rebuildPageTagIndex();
//	void rebuildSpaceTagIndex();
//	void rebuildAttachmentIndex();

	void optimize();

	void saveOrUpdatePage(Page page);
	void removePage(String removedPageUuid);

	void saveOrUpdateComment(PageComment comment);
	void removeComment(Integer commentUid);
	
	void saveOrUpdateSpace(Space space);
	void removeSpace(String spaceUname);

	void saveOrUpdateWidget(Widget widget);
	void removeWidget(String widgetKey);
	
	void saveOrUpdatePageTag(PageTag tag);
	void removePageTag(String tag);
	
	void saveOrUpdateSpaceTag(SpaceTag tag);
	void removeSpaceTag(String tag);
	
	void saveOrUpdateUser(User user);
	void removeUser(String username);

	void saveOrUpdateAttachment(String spaceUname,FileNode node, boolean keepFileContent);
	void removeAttachment(String nodeUuid, String version);

	void saveOrUpdateRole(Role role);
	
	
	
}
