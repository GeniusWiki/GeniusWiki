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
package com.edgenius.wiki.gwt.client.server;

import com.edgenius.wiki.gwt.client.model.QueryModel;
import com.edgenius.wiki.gwt.client.model.SearchResultModel;

/**
 * @author Dapeng.Ni
 */
public interface SearchController extends RemoteService{
	String MODULE_ACTION_URI = "search.rpcs";
	
	public SearchResultModel search(QueryModel query, int selectPageNumber, int returnCount);
	public SearchResultModel searchSpace(String keyword, int selectPageNumber, int returnCount);
	public SearchResultModel searchUser(String keyword, int selectPageNumber, int returnCount);
	public SearchResultModel searchWidgetSpace(String keyword, int selectPageNumber, int returnCount);
	public SearchResultModel searchUserAndSpace(String keyword, int selectPageNumber, int returnCount);
	public SearchResultModel searchRoles(String keyword, int selectPageNumber, int returnCount);
}
