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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.Global;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.FileUtil;
import com.edgenius.wiki.ext.todo.dao.TodoDAO;
import com.edgenius.wiki.ext.todo.dao.TodoItemDAO;
import com.edgenius.wiki.ext.todo.model.Todo;
import com.edgenius.wiki.ext.todo.model.TodoItem;
import com.edgenius.wiki.ext.todo.model.TodoStatus;
import com.edgenius.wiki.plugin.PluginServiceProvider;
import com.edgenius.wiki.plugin.PluginServiceProviderException;
import com.edgenius.wiki.service.DataBinder;
import com.edgenius.wiki.service.EventContainer;
import com.edgenius.wiki.service.PageEventHanderException;
import com.edgenius.wiki.service.PageEventListener;
import com.edgenius.wiki.service.PageException;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@Transactional
public class TodoServiceImpl implements TodoService, PageEventListener,PluginServiceProvider {

	private EventContainer eventContainer;
	private TodoDAO todoDAO;
	private TodoItemDAO todoItemDAO;
	private UserReadingService userReadingService;
	
	//********************************************************************
	//              Service methods
	//********************************************************************
	@Transactional(readOnly=true)
	public void fillTodosAndStatuses(Map<String, Object> map, Todo todo) {
		
		if(todo.getUid() == null){
			//This todo is not persist yet, it shouldn't have todo items. So only fill in status list
			map.put("statuslist", todo.getStatuses());
			return;
		}
		
		//here doesn't use todo.getItems() for 2 reasons:
		//1. if todo is new one and just persisted, it won't refresh items - although this can be fix by adding items to todo.setItems() list
		//2. For a new item, it won't sort correctly as it won't get real createDate from database. 
		List<TodoItem> todos = getTodoItems( todo.getPageUuid(), todo.getName());
		
		//status from current macro - some status tag maybe not tag any TodoItem yet. so we need merge them - database and macro.
		List<TodoStatus> statuses = todo.getStatuses();
		
		Map<String, Long> taggedStatuses = todoItemDAO.getStatusCount(todo.getPageUuid(), todo.getName());
		
		//fill in TodoItem.statusObject
		for (TodoItem item : todos) {
			int idx = -1;
			for (int findIdx=0;findIdx<statuses.size();findIdx++) {
				if(StringUtils.equalsIgnoreCase(statuses.get(findIdx).getText(),item.getStatus())){
					idx = findIdx;
					break;
				}
			}
			 
			TodoStatus status;
			if(idx == -1){
				//this TodoItem status not available in current macro status list(user have updated macro and remove some statuses)
				//then create a temporarily one.
				status = new TodoStatus();
				status.setText(item.getStatus());
				status.setSequence(statuses.size());
				status.setPersisted(false);
				//fill it to current status list
				statuses.add(status);
			}else{
				status = statuses.get(idx);
			}
		
			//lower case - see todoItemDAO.getStatusCount()
			Long count = taggedStatuses.get(status.getText().toLowerCase());
			if(count == null)
				status.setItemsCount(0);
			else
				status.setItemsCount(count.intValue());

			//update status object
			item.setStatusObj(status);
		}
			
		map.put("todos", todos);
		map.put("statuslist", statuses);

		
	}
	@Transactional(readOnly=true)
	public List<TodoItem> getTodoItems(String pageUuid, String todoName) {
		return todoItemDAO.getItemsByNameInPage(pageUuid, todoName);
	}

	public void saveOrUpdateTodo(Todo todo) {
		if(todo.getUid() == null){
			//add this calendar to PageEventListener
			eventContainer.addPageEventListener(todo.getPageUuid(), this);
		}
		
		WikiUtil.setTouchedInfo(userReadingService, todo);
		todoDAO.saveOrUpdate(todo);
	}
	public void saveOrUpdateItem(TodoItem item) {
		todoItemDAO.saveOrUpdate(item);
	}

	@Transactional(readOnly=true)
	public Todo getTodoByName(String pageUuid, String todoName) {
		return todoDAO.getByNameInPage(pageUuid, todoName);
	}
	public TodoItem updateItemStatus(Integer todoItemID, String status) {
		TodoItem item = todoItemDAO.get(todoItemID);
		if(item != null){
			item.setStatus(status);
			WikiUtil.setTouchedInfo(userReadingService, item);
			todoItemDAO.saveOrUpdate(item);
		}
		
		return item;
	}
	public TodoItem deleteItem(Integer todoItemID) {
		TodoItem item = todoItemDAO.get(todoItemID);
		if(item != null)
			todoItemDAO.removeObject(item);
		
		return item;
	}

	//********************************************************************
	//              PluginServiceProvider API
	//********************************************************************
	public String invokePluginService(String operation, String[] params) throws PluginServiceProviderException {
		return null;
	}

	public void backup(DataBinder binder) {
		List<Todo> todos = todoDAO.getObjects();
		for (Todo todo : todos) {
			todo.setStatuses(null);
			todo.setItems(null);
		}
		binder.addAll(Todo.class.getName(),todos);
		
		List<TodoItem> items = todoItemDAO.getObjects();
		binder.addAll(TodoItem.class.getName(),items);

		
		
	}

	public void resorePreClean() {
		todoItemDAO.cleanTable();
		todoDAO.cleanTable();
	}

	@SuppressWarnings("unchecked")
	public void restore(DataBinder binder) {

		List<Todo> todos = (List<Todo>) binder.get(Todo.class.getName());
		if(todos != null){
			for (Todo todo : todos) {
				todo.setUid(null);
				todoDAO.saveOrUpdate(todo);
			}
		}		
		List<TodoItem> items = (List<TodoItem>) binder.get(TodoItem.class.getName());
		if(items != null){
			for (TodoItem item : items) {
				item.setUid(null);
				todoItemDAO.saveOrUpdate(item);
			}
		}
		
		initListeners();
	}
	
	/**
	 * Method call by Spring "init-method" bean initialise.
	 */
	public void initListeners(){
		
		//initial listeners for pages
		List<Todo> todos = todoDAO.getObjects();
		for (Todo todo : todos) {
			eventContainer.addPageEventListener(todo.getPageUuid(), this);
		}
	}
	public Map<File, String> exportResources() {
		Map<File, String> map = new HashMap<File, String>();
		map.put(new File(FileUtil.getFullPath(Global.ServerInstallRealPath, "plugins","todo","resources","todo-print.css")),
				FileUtil.getFullPath( "plugins","todo","resources"));
		return map;
	}

	//********************************************************************
	//              PageEventListener API
	//********************************************************************
	public void pageRemoving(String pageUuid, boolean permanent) throws PageEventHanderException, PageException {
		if(!permanent)
			return;
		
		this.removePageTodos(pageUuid);
		eventContainer.removePageEventListeners(pageUuid);
		
	}

	public void pageSaving(String pageUuid) throws PageEventHanderException, PageException {
		//Do nothing
	}
	//********************************************************************
	//             Private methods
	//********************************************************************
	/**
	 * @param pageUuid
	 */
	private void removePageTodos(String pageUuid) {
		List<Todo> todos = todoDAO.getPageTodos(pageUuid);
		//We assume it won't have many todo in same page, so below has minor performance impact.
		for (Todo todo : todos) {
			todoDAO.removeObject(todo);
		}
		
	}
	//********************************************************************
	//               set / get
	//********************************************************************
	public void setEventContainer(EventContainer eventContainer) {
		this.eventContainer = eventContainer;
	}
	public void setTodoDAO(TodoDAO todoDAO) {
		this.todoDAO = todoDAO;
	}
	public void setTodoItemDAO(TodoItemDAO todoItemDAO) {
		this.todoItemDAO = todoItemDAO;
	}
	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}

}
