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
package com.edgenius.wiki.ext.todo.service;

import java.util.List;
import java.util.Map;

import com.edgenius.wiki.ext.todo.model.Todo;
import com.edgenius.wiki.ext.todo.model.TodoItem;

/**
 * @author Dapeng.Ni
 */
public interface TodoService {

	String SERVICE_NAME = "todoService";

	/**
	 * This is quite tricky method and only used for Freemaker map data.  It will fill in 2 fields in map.<br>
	 * First is statuslist, which not only TodoStatus fields from database, also includes these status from existed TodoItem.<br>
	 * Second is todolist, which will fill TodoItem.statusObj field with TodoStatus object. 
	 * Todolist is optional if there is no todo items available.<br>
	 * 
	 * 
	 * @param map
	 * @param pageUuid
	 * @param todoName
	 * @param status
	 * @param deleteAction
	 */
	void fillTodosAndStatuses(Map<String, Object> map, Todo todo);
	/**
	 * @param pageUuid
	 * @param todoName
	 * @return order by createdDate
	 */
	List<TodoItem> getTodoItems(String pageUuid, String todoName);

	/**
	 * @param todo
	 */
	void saveOrUpdateTodo(Todo todo);

	/**
	 * @param pageUuid
	 * @param todoName
	 * @return
	 */
	Todo getTodoByName(String pageUuid, String todoName);

	/**
	 * @param item
	 */
	void saveOrUpdateItem(TodoItem item);

	/**
	 * @param todoItemID
	 * @param status
	 */
	TodoItem updateItemStatus(Integer todoItemID, String status);
	/**
	 * @param todoItemID
	 * @return
	 */
	TodoItem deleteItem(Integer todoItemID);
	
}
