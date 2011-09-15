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

import java.util.ListIterator;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.render.Macro;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.macro.BaseMacro;
import com.edgenius.wiki.render.object.ObjectPosition;

/**
 * {todo:name=work|status=new,working,delay,done,delete|deleteOn=delete}
 * 
 * @author Dapeng.Ni
 */
public class TodoMacro extends BaseMacro{

	private static final String HANDLER = TodoHandler.class.getName();
	public static final String DEFAULT_STATUS = "new,working,delay,done,delete";
	public static final String DEFAULT_DELETE_ON = "delete";
	
	public String[] getName() {
		return new String[]{"todo","todos"};
	}

	public boolean isPaired() {
		return false;
	}
	
	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		
		RenderContext context = (RenderContext) params.getRenderContext();
		ObjectPosition obj = new ObjectPosition(params.getStartMarkup());
		//whatever, treat it as multiple line content
		obj.uuid = context.createUniqueKey(true);
		obj.serverHandler = HANDLER;
		obj.values = params.getParams();
		obj.values.put("markup", params.getStartMarkup());
		context.getObjectList().add(obj);
		
		buffer.append(obj.uuid);
	}

	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		resetMacroMarkup(Macro.TIDY_STYLE_BLOCK, node, iter,  getMacroMarkupString(node, "todo"), null);
		resetInsideNode(node, iter);
	}

	@Override
	public String getHTMLIdentifier() {
		return "<div class=\"macroTodo\">";
	}

}
