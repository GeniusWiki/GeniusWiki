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
import java.util.Map;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.handler.NameMacroHandler;
import com.edgenius.wiki.render.object.ObjectPosition;

/**
 * Open comments tab and focus input to "new comment box"
 * {comment:title=input your comment}
 * 
 * @author Dapeng.Ni
 * 
 */
public class CommentMacro extends BaseMacro {
	
	private final static String HANDLER = NameMacroHandler.class.getName();
	
	//JDK1.6 @Override
	public String[] getName() {
		return new String[]{"comment"};
	}
	@Override
	public String getHTMLIdentifier() {
		return "<div aid='comment'>";
	}
	//JDK1.6 @Override
	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
	
		RenderContext context = params.getRenderContext();
		ObjectPosition obj = new ObjectPosition(params.getStartMarkup());
		obj.uuid = context.createUniqueKey(true);
		obj.serverHandler = HANDLER;
		obj.values.put(NameConstants.MACRO, SharedConstants.MACRO_COMMENT);
		
		Map<String, String> ps = params.getParams();
		if(ps != null){
			//put default title
			String title = ps.get(NameConstants.TITLE);
			if(StringUtil.isBlank(title)){
				title = "comment";
				ps.put(NameConstants.TITLE, title);
			}
			obj.values.putAll(ps);
		}
		
		context.getObjectList().add(obj);
		buffer.append(obj.uuid);
	}
	public boolean isPaired(){
		return false;
	}
	
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		resetMacroMarkup(node, "comment");
		resetInsideNode(node, iter);
	}
}
