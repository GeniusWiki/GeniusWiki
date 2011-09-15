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
import com.edgenius.wiki.model.PageTag;

/**
 * @author Dapeng.Ni
 */
public interface PageTagDAO extends DAO<PageTag>{

	/**
	 * @param tagName
	 * @param count 
	 */
	PageTag getByName(String spaceUname, String tagName);

	/**
	 * @param spaceUname
	 */
	List<PageTag> getSpaceTags(String spaceUname);

	void removeTagsInSpace(String spaceUname);
	/**
	 * @param spaceUname
	 * @param tagname
	 * @param count
	 * @return
	 */
//	List<Page> getPages(String spaceUname, String tagname, int count);

}
