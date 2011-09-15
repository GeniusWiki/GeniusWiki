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

import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.render.GroupProcessor;
import com.edgenius.wiki.render.GroupProcessorMacro;
import com.edgenius.wiki.render.Macro;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.render.macro.BaseMacro;
import com.edgenius.wiki.render.object.ObjectPosition;

/**
 * {tabs:select=tabName}
 * @author Dapeng.Ni
 */
public class TabsMacro extends BaseMacro implements GroupProcessorMacro{
	private static final String HANDLER = TabsHandler.class.getName();
	public static final String NAME0 = "tabs";
	public static final String NAME1 = "deck";

	public String getHTMLIdentifier() {
		return "<div class=\"macroTabs\">";
	}

	public String[] getName() {
		return new String[]{NAME0, NAME1};
	}

	public String[] hasChildren(){
		return new String[]{TabMacro.NAME};
	}
	public boolean isPaired() {
		return true;
	}

	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		String tabKey = params.getParam(Macro.GROUP_KEY);
		if(StringUtils.isBlank(tabKey)){
			buffer.append(RenderUtil.renderError("Unable to render tabs macro. No correct group message.",params.getStartMarkup()));
			buffer.append(params.getContent());
			buffer.append("{tabs}");
			return;
		}
		
		//default to heading 3
		RenderContext context = params.getRenderContext();
		ObjectPosition obj = new ObjectPosition(params.getStartMarkup());
		//use handler to add <ul> for tab headers
		obj.serverHandler = HANDLER;
		//whatever, treat it as multiple line content
		obj.uuid = context.createUniqueKey(false);
		obj.values = params.getParams();
		context.getObjectList().add(obj);
		
	
		String content = params.getContent();
		buffer.append("<div class=\"macroTabs\" id=\"macroTabs-").append(tabKey).append("\">").append(obj.uuid).append(content)
			.append("</div>");

		
	}


	public GroupProcessor newGroupProcessor(Macro macro, int start, int end) {
		return new TabsGroupProcessor(macro,start,end);
	}

	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		HTMLNode pair = node.getPair();
		if(pair == null){
			log.error("Unexpected: no close tabs tag.");
			return;
		}

		resetMacroMarkup(TIDY_STYLE_BLOCK, node, iter,"{tabs}", "{tabs}");
		
		//clean all tags/content between <div name="macroTabName">...</div>
		HTMLNode subnode = node.next();
		HTMLNode namePairNode = null;
		while(subnode != null){
			if(subnode == pair || subnode == namePairNode)
				break;
			
			if(namePairNode != null){
				subnode.reset("", true);
			}else if(subnode.getAttributes() != null){
				String name = subnode.getAttributes().get(NameConstants.NAME);
				if(StringUtils.equalsIgnoreCase("macroTabNames", name)){
					subnode.reset("", true);
					namePairNode = subnode.getPair();
					if(namePairNode == null){
						log.warn("Invalid macro tab name div - no close div");
						break;
					}
					namePairNode.reset("", true);
				}
			}
			
			subnode = subnode.next();
		}
	}
}
