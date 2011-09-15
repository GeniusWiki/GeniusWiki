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
package com.edgenius.wiki.ext.todo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;

import com.edgenius.wiki.ext.todo.model.Todo;
import com.edgenius.wiki.ext.todo.service.TodoService;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.plugin.PluginRenderException;
import com.edgenius.wiki.plugin.PluginService;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.render.object.RenderHandlerException;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;

/**
 * @author Dapeng.Ni
 */
public class TodoHandler  implements ObjectHandler{
	private PluginService pluginService;
	private TodoService todoService;
	private boolean editable;
	
	public List<RenderPiece> handle(RenderContext renderContext, Map<String, String> values) throws RenderHandlerException {
			
		if(values == null || StringUtils.isBlank(values.get(NameConstants.NAME))){
			throw new RenderHandlerException("TODO must have a name, i.e., {todo:name=work}");
		}
		
		try {
			//get unique index ID for same page Todos render
			Integer pageKey = (Integer) renderContext.getGlobalParam(TodoHandler.class.getName());
			if(pageKey == null)
				pageKey = 1;
			else 
				++pageKey;
			
			renderContext.putGlobalParam(TodoHandler.class.getName(), pageKey);
			
			String todoName = values.get(NameConstants.NAME).trim();
			String statusString = StringUtils.trimToEmpty(values.get("status"));
			String deleteAction = StringUtils.trimToEmpty(values.get("deleteon"));

			Map<String, Object> map = new HashMap<String, Object>();
			//keep original markup - but not put it into wajax attribute
			map.put("todoMarkup", values.remove("markup"));
			
			map.put("todoWajax", RichTagUtil.buildWajaxAttributeString(this.getClass().getName(),values));
			
			map.put("readonly",!editable);
			map.put("pageUuid", renderContext.getPageUuid());
			map.put("todoKey", pageKey);
			map.put("todoName", todoName);
			map.put("statuses", statusString);
			map.put("deleteAction", deleteAction);

			Todo todo = todoService.getTodoByName(renderContext.getPageUuid(), todoName);
			if(todo == null){
				todo = new Todo();
			}
			todo.setStatusString(statusString, deleteAction);
			
			todoService.fillTodosAndStatuses(map,todo);
			
			return Arrays.asList((RenderPiece)new TextModel(pluginService.renderMacro("todo",map,renderContext)));
		} catch (PluginRenderException e) {
			throw new RenderHandlerException("Todo can't be rendered.");
		}
	}


	public void init(ApplicationContext context) {
		pluginService = (PluginService) context.getBean(PluginService.SERVICE_NAME);
		todoService = (TodoService) context.getBean(TodoService.SERVICE_NAME);
		
	}

	public void renderEnd() {
		//nothing
	}

	public void renderStart(AbstractPage page) {
		//!!! must set false for each render - This handler is cache in ObjectPool
		editable = false;
		List<WikiOPERATIONS> perms = page.getWikiOperations();
		if(perms != null){
			//this is true when IndexService - it doesn't fill page permission at the moment
			for (WikiOPERATIONS wikiOPERATIONS : perms) {
				if (WikiOPERATIONS.PAGE_WRITE.equals(wikiOPERATIONS)) {
					editable = true;
					break;
				}
			}
		}
	}
}
