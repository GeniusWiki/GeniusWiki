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
import com.edgenius.wiki.model.Draft;

/**
 * @author Dapeng.Ni
 */
public interface DraftDAO  extends DAO<Draft>{


	Draft getDraftByUuid(String spaceUname, String uuid, User owner, int type);
	
	List<Draft> hasDraftByTitle(String spaceUname, String title,User owner);
	
	/**
	 * @return pageUuid
	 */
	Draft removeDraftByUuid(String spaceUname, String pageUuid, User owner, int type);

	/**
	 * @param spaceUname
	 * @param username
	 * @param type TODO
	 * @return
	 */
	List<Draft> getDrafts(String spaceUname, String username, int type);

	/**
	 * @param spaceUname
	 */
	void removeSpaceDrafts(Integer spaceUid);

	
}
