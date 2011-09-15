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
package com.edgenius.wiki.gwt.client;

import java.util.List;

import com.edgenius.wiki.gwt.client.model.TreeItemListModel;

/**
 * Some element(list) need send request to server side then get back value. This class is callback while these element 
 * become available.
 *  
 * @author Dapeng.Ni
 */
public interface ElementRequesterCallback {

	/**
	 * Get page title list for spaceUname under current login user permission, 
	 * if spaceUname is null or empty, return current space page list.
	 * @param spaceUname
	 * @return
	 */
	public void pageTitleList(String spaceUname,List<String> titles);
	public void pageTitleListRequestFailed(String errorCode);
	
	/**
	 * Get all space unixname under current login user permission
	 * @return
	 */
	public void spaceUnameList(List<String> spaces);
	public void spaceUnameListRequestFailed(String errorCode);
	/**
	 * @param object
	 */
	public void pageTreeRequestFailed(String errorCode);
	/**
	 * @param model
	 */
	public void pageTree(TreeItemListModel model);

	
}
