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
package com.edgenius.wiki.dao;

import java.util.Collection;
import java.util.List;

import com.edgenius.core.dao.DAO;
import com.edgenius.core.model.Resource;
import com.edgenius.core.model.User;
import com.edgenius.wiki.model.Notification;

/**
 * @author Dapeng.Ni
 */
public interface NotificationDAO  extends DAO<Notification>{
	/**
	 * Get messages for user.
	 *   
	 * @param user
	 * @param sysAdmin if this user is system admin(INSTANCE ADMIN permission), if it is true, spaceResList and adminResList input should be null.
	 * @param spaceResList the resources list of this user has WRITE permission, this resources list is not limited on SPACE resource, so 
	 * 	must use filter to check if resource is space
	 * @param adminResList the resources list of this user has ADMIN permission, this resources list is not limited on SPACE resource, so 
	 * 	must use filter to check if resource is space
	 * 
	 * 
	 * @param retCount
	 * @return
	 */
	Collection<? extends Notification> getResourceMessages(final User user, final boolean sysAdmin, 
			final List<Resource> spaceResList, final List<Resource> adminResList, int start, int retCount);

}
