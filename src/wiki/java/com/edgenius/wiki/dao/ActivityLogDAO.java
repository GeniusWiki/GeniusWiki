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

import java.util.List;

import com.edgenius.core.dao.DAO;
import com.edgenius.core.model.User;
import com.edgenius.wiki.model.ActivityLog;

/**
 * @author Dapeng.Ni
 */
public interface ActivityLogDAO  extends DAO<ActivityLog>{

	/**
	 * @param typeCode
	 * @param subTypeCode
	 * @param tgtResourceType
	 * @param tgtResourceName
	 * @return
	 */
	List<ActivityLog> getByTarget(int typeCode, int subTypeCode, int tgtResourceType, String tgtResourceName);
	/**
	 * @param typeCode
	 * @param subTypeCode
	 * @param srcResourceType
	 * @param srcResourceName
	 * @return
	 */
	List<ActivityLog> getBySource(int typeCode, int subTypeCode, int srcResourceType, String srcResourceName);
	
	/**
	 * @param days
	 */
	void removeOldByDays(int days);

	/**
	 * @param start
	 * @param count
	 */
	List<ActivityLog> getByCount(int start, int count);

	/**
	 * Get this user's activities by given start and return count
	 * @param start
	 * @param count
	 * @param user
	 * @return
	 */
	List<ActivityLog> getByUser(int start, int count, User user);




}
