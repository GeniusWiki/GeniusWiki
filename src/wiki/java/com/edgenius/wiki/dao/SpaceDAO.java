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
import java.util.Map;

import com.edgenius.core.dao.DAO;
import com.edgenius.core.model.Resource;
import com.edgenius.wiki.model.Space;

/**
 * @author Dapeng.Ni
 */
public interface SpaceDAO extends DAO<Space>{

	/**
	 * @param uuid spaceUname
	 * @return
	 */
	Space getByUname(String spaceUname);

	/**
	 * @param spaceUname
	 * @return
	 */
	List<Resource> getSpacePageResources(String spaceUname);

	List<Space> getSpaces(final int start, final int returnSize, String sortBy, String filter, boolean sortByDesc);

	/**
	 * This method can not return anonymous user space (actually, anonymous does not allow create space,but system default
	 * space is no creator).
	 * @param username
	 * @param limit
	 * @return
	 */
	List<Space> getUserCreatedSpaces(String username, int limit);

	/**
	 * @return space type = -1, this space use to hold instance level pages, such search result, user profile etc. 
	 */
	Space getSystemSpace();
	
	/**
	 * How many space available except removed spaces.
	 * @return
	 */
	int getSpaceCount(String filter);

	/**
	 * @param spaceUname
	 * @return
	 */
	Space getByTitle(String title);

	/**
	 * @param username
	 * @return
	 */
	int getUserAuthoredSize(String username);

	/**
	 * All space unix name except $SYSTEM$ space.
	 * @return
	 */
	List<String> getAllSpaceUnames();

	/**
	 * @return page count per space, spaceUid:pageCount
	 */
	Map<Integer, Long> getAllSpacePageCount();
}
