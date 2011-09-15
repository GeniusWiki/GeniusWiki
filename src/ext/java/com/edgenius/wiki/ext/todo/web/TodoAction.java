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
package com.edgenius.wiki.ext.todo.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.edgenius.core.Constants;
import com.edgenius.wiki.ext.todo.model.Todo;
import com.edgenius.wiki.ext.todo.model.TodoItem;
import com.edgenius.wiki.ext.todo.service.TodoService;
import com.edgenius.wiki.plugin.PluginService;
import com.edgenius.wiki.util.WikiUtil;
import com.edgenius.wiki.webapp.action.BaseAction;

import freemarker.template.TemplateException;

/**
 * @author Dapeng.Ni
 */
public class TodoAction extends BaseAction{
	private static final long serialVersionUID = 1323902071946445680L;
	
	//input parameters for adding todo item 
	String pageUuid;
	String todoKey;
	String todoName;
	int priority;
	String content;
	
	//when macro render, this parameters is fill in "add todo" form, so that it will save/update into Todo table. 
	private String statuses;
	private String deleteAction;

	//update status input
	private String status;
	Integer todoItemID;
	
	private TodoService todoService;
	private PluginService pluginService;

	/**
	 * Save todo item into specified todo, if todo doesn't exist, create new one.
	 */
	public String saveItem(){
		try {
			Todo todo = todoService.getTodoByName(pageUuid, todoName);
			if(todo == null){
				//here have to save Todo once page rendered
				//create new one
				todo = new Todo();
				todo.setName(todoName);
				todo.setPageUuid(pageUuid);
				todoService.saveOrUpdateTodo(todo);
			}
			todo.setStatusString(statuses,deleteAction);
			
			TodoItem item = new TodoItem();
			item.setPriority(priority);
			item.setContent(content);
			//For a new Todo item, it always uses default status - first one in list
			item.setStatus(todo.getStatuses().get(0).getText());
			WikiUtil.setTouchedInfo(userReadingService, item);
			item.setTodo(todo);
			todoService.saveOrUpdateItem(item);
			
			listItems(todo);
		} catch (Exception e) {
			log.error("Unable to save TODO item",e);
		}
		
		
		return null;
	}


	public String status(){
		TodoItem item = todoService.updateItemStatus(todoItemID, status);
		if(item != null){
			Todo todo = item.getTodo();
			todo.setStatusString(statuses,deleteAction);
			listItems(todo);
		}else{
			try {
				getResponse().getWriter().write("");
			} catch (IOException e) {
				log.error("Unable to list TODO item",e);
			}
		}
		return null;
		
	}
	
	public String delete(){
		TodoItem item = todoService.deleteItem(todoItemID);
		if(item != null){
			Todo todo = item.getTodo();
			todo.setStatusString(statuses,deleteAction);
			listItems(todo);
		}else{
			try {
				getResponse().getWriter().write("");
			} catch (IOException e) {
				log.error("Unable to delete TODO item",e);
			}
		}
		return null;
		
	}
	//********************************************************************
	//             Private
	//********************************************************************
	/**
	 * @param list 
	 * @param set 
	 * @throws IOException
	 * @throws TemplateException
	 */
	private void listItems(Todo todo){
		//response - get back all todo items under  specified todoName.
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("todoKey", todoKey);
		//as above changeStatus,save,delete must have write permission, so here readonly is always false
		map.put("readonly",false);
		//input null as status string as we believe Todo must exist in database when this method is called.
		todoService.fillTodosAndStatuses(map, todo);
		//write response
		try {
			getResponse().getOutputStream().write(FreeMarkerTemplateUtils.processTemplateIntoString(
					pluginService.getPluginTemplate("todo", "/todolist.ftl"), map).getBytes(Constants.UTF8));
		} catch (IOException e) {
			log.error("Unable to list TODO item",e);
		} catch (TemplateException e) {
			log.error("Unable to list TODO item",e);
		}
	}
	//********************************************************************
	//              Set / Get 
	//********************************************************************
	public String getPageUuid() {
		return pageUuid;
	}

	public void setPageUuid(String pageUuid) {
		this.pageUuid = pageUuid;
	}

	public String getTodoName() {
		return todoName;
	}

	public void setTodoName(String todoName) {
		this.todoName = todoName;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getTodoItemID() {
		return todoItemID;
	}

	public void setTodoItemID(Integer todoItemID) {
		this.todoItemID = todoItemID;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTodoKey() {
		return todoKey;
	}

	public void setTodoKey(String pageKey) {
		this.todoKey = pageKey;
	}

	public String getStatuses() {
		return statuses;
	}


	public void setStatuses(String statuses) {
		this.statuses = statuses;
	}


	public String getDeleteAction() {
		return deleteAction;
	}


	public void setDeleteAction(String deleteAction) {
		this.deleteAction = deleteAction;
	}


	public void setTodoService(TodoService todoService) {
		this.todoService = todoService;
	}

	public void setPluginService(PluginService pluginService) {
		this.pluginService = pluginService;
	}
}
