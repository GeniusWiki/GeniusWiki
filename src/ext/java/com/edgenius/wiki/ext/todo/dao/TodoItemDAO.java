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
package com.edgenius.wiki.ext.todo.dao;

import java.util.List;
import java.util.Map;

import com.edgenius.core.dao.DAO;
import com.edgenius.wiki.ext.todo.model.TodoItem;

/**
 * @author Dapeng.Ni
 */
public interface TodoItemDAO extends DAO<TodoItem>{

	/**
	 * @param uid
	 */
	void removeItems(Integer todoUid);

	/**
	 * @param pageUuid
	 * @param todoName
	 * @return order by createdDate
	 */
	List<TodoItem> getItemsByNameInPage(String pageUuid, String todoName);
	
	/**
	 * The reason puts this method under TodoItemDAO rather than TodoDAO, it is because status saved as text field in TodoItem. 
	 * @param pageUuid
	 * @param todoName
	 * @return How many todo items under status. All status are from specified Todo 
	 */
	Map<String,Long> getStatusCount(String pageUuid, String todoName);
}
