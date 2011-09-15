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
package com.edgenius.wiki.ext.tabs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.plugin.PluginRenderException;
import com.edgenius.wiki.plugin.PluginService;
import com.edgenius.wiki.render.Macro;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.render.object.RenderHandlerException;


/**
 * @author Dapeng.Ni
 */
public class TabsHandler  implements ObjectHandler{
	private static final Logger log = LoggerFactory.getLogger(TabsHandler.class);
	private PluginService pluginService;
	/*
	 * replace image text with download servlet url: /download?spaceUname=xxx&uuid=xxx
	 */
	public List<RenderPiece> handle(RenderContext renderContext, Map<String,String> values) throws RenderHandlerException {
		
		Map<String, Map<String,String>> tabsMap= (Map<String, Map<String,String>>) renderContext.getGlobalParam(TabsHandler.class.getName());
		if(tabsMap == null || tabsMap.size() == 0){
			//No valid tab found for tabs
			return null;
		}
		
		String grpKey = values.get(Macro.GROUP_KEY);
	
		//tabKey and tabName - see TabMacro.java
		Map<String,String> tabList = tabsMap.get(grpKey);
		if(tabList == null || tabList.size() == 0){
			//No valid tab found for tabs
			return null;
		}
		
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("tabsID", grpKey);
			String sel = StringUtils.trim(values.get("select"));
			if(!StringUtils.isBlank(sel)){
				int idx = 0;
				for (Entry<String,String> entry: tabList.entrySet()) {
					if(StringUtils.equalsIgnoreCase(entry.getValue(), sel)){
						map.put("selectTab", idx);
						break;
					}
					idx++;
				}
			}
			map.put("tabMap", tabList);
			return Arrays.asList((RenderPiece)new TextModel(pluginService.renderMacro("tabs",map,renderContext)));
		} catch (PluginRenderException e) {
			log.error("Unable to render Tab plugin",e);
			throw new RenderHandlerException("Tabs can't be rendered.");
		}
		
	}

	public void renderStart(AbstractPage page) {
		
	}

	public void renderEnd() {
		
	}

	public void init(ApplicationContext context) {
		pluginService = (PluginService) context.getBean(PluginService.SERVICE_NAME);
	}
}
