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
package com.edgenius.wiki.render.macro;

import java.util.ListIterator;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.handler.NameMacroHandler;
import com.edgenius.wiki.render.object.ObjectPosition;

/**
 * {tagcloud}
 * Display space or page tag cloud. The spaceUname is decide by Client side viewPanel's spaceUname.
 * If spaceUname is blank, the spaces tag cloud is displayed, otherwise, pages tag cloud is displayed.
 * 
 * @author Dapeng.Ni
 */
public class TagCloudMacro  extends BaseMacro {
	private final static String HANDLER = NameMacroHandler.class.getName();
	
	public String[] getName() {
		return  new String[]{"tagcloud"};
	}

	public void execute(StringBuffer writer, MacroParameter params) throws MalformedMacroException{
		RenderContext context = params.getRenderContext();

		ObjectPosition obj = new ObjectPosition(params.getStartMarkup());
		//whatever, treat it as multiple line content
		obj.uuid = context.createUniqueKey(true);
		obj.serverHandler = HANDLER;
		obj.values.put(NameConstants.MACRO, SharedConstants.MACRO_TAG_CLOUD);
		obj.values.putAll(params.getParams());
		
		context.getObjectList().add(obj);
		
		writer.append(obj.uuid);
		
//		Map<String, String> map = new HashMap<String, String>();
//		String wajax = RichTagUtil.buildWajaxAttributeString(this.getClass().getName(), map);
//		
//		//create <div wajax=...>
//		Map<String, String> attMap = new HashMap<String, String>();
//		attMap.put(NameConstants.WAJAX, wajax);
//		writer.append(RichTagUtil.buildTagString("div", null, attMap)+"</div>");
	}
	public boolean isPaired(){
		return false;
	}
	@Override
	public String getHTMLIdentifier() {
		return "<div aid='"+SharedConstants.MACRO_TAG_CLOUD+"'>";
	}
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		resetMacroMarkup(TIDY_STYLE_BLOCK, node, iter, getMacroMarkupString(node, "tagcloud"), null);
		resetInsideNode(node, iter);
	}
}
