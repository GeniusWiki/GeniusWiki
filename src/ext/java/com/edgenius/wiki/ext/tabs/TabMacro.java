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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.render.Macro;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.render.macro.BaseMacro;

/**
 * {tabs}
 * 	{tab:name=work}
	working one something
	{tab}
	{tab:name=life}
	life for fun
	{tab}
	{tabs}
 * @author Dapeng.Ni
 */
public class TabMacro extends BaseMacro{
	
	public static final String NAME = "tab";

	private static final String DEFAULT_TAB_NAME = "Tab";

	public String getHTMLIdentifier() {
		return "<div class=\"macroTab\">";
	}

	public String[] getName() {
		return new String[]{NAME};
	}


	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		String tabKey = params.getParam(Macro.GROUP_KEY);
		RenderContext context = params.getRenderContext();
		if(StringUtils.indexOf(tabKey, '-') == -1){
			errorGroup(buffer, params, tabKey);
		}else{
			String[] keys = tabKey.split("-");
			if(keys.length != 2){
				errorGroup(buffer, params, tabKey);
			}else{
				String grpKey = keys[0];
				String name = StringUtils.trim(params.getParam(NameConstants.NAME));
				if(StringUtils.isBlank(name)){
					//default name
					name = DEFAULT_TAB_NAME;
				}
				//OK, save tabKey and tabName information into Global parameter, used by TabsHandler
				Map<String, Map<String,String>> tabsMap= (Map<String,  Map<String,String>>) context.getGlobalParam(TabsHandler.class.getName());
				if(tabsMap == null){
					tabsMap = new HashMap<String,  Map<String,String>>();
					context.putGlobalParam(TabsHandler.class.getName(), tabsMap);
				}
				 Map<String,String> tabList = tabsMap.get(grpKey);
				if(tabList == null){
					//ordered - so LinkedHashMap
					tabList = new LinkedHashMap<String,String>();
					tabsMap.put(grpKey, tabList);
				}
				tabList.put(tabKey,name);
				
				if(RenderContext.RENDER_TARGET_EXPORT.equals(context.getRenderTarget())
					|| RenderContext.RENDER_TARGET_PLAIN_VIEW.equals(context.getRenderTarget())){
					//for print and export, tab name will be just before the content body
					buffer.append("<div class=\"macroTabName\">").append(name).append("</div>");
				}
				
				//use "tab-"+tabKey as div ID, so that TabsHanlder can process render tabs correctly.
				String content = params.getContent();
				buffer.append("<div class=\"macroTab\" id=\"tab-").append(tabKey).append("\" name=\"").append(name).append("\">")
					.append(content).append("</div>");
			}
		}
	}


	/**
	 * @param buffer
	 * @param params
	 * @param tabKey
	 */
	private void errorGroup(StringBuffer buffer, MacroParameter params, String tabKey) {
		AuditLogger.error("Unable to find out correct tab key from "+ tabKey);
		buffer.append(RenderUtil.renderError("Unable to locate tabs macro. Please ensure this tab is inside {tabs} macro.",params.getStartMarkup()));
		buffer.append(params.getContent());
		buffer.append("{tab}");
	}


	public boolean isPaired() {
		return true;
	}


	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		HTMLNode pair = node.getPair();
		if(pair == null){
			log.error("Unexpected: no close tab tag.");
			return;
		}
		
		String name=null;
		if(node.getAttributes() != null){
			name = node.getAttributes().get(NameConstants.NAME);
		}
		name = StringUtils.isBlank(name)||DEFAULT_TAB_NAME.equals(name)?"":(":name="+name);
		
		resetMacroMarkup(TIDY_STYLE_BLOCK, node, iter,"{tab"+name+"}", "{tab}");
		
	
	}

}
