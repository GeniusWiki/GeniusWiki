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
package com.edgenius.wiki.ext.macro;

import java.util.ListIterator;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.render.Macro;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.macro.BaseMacro;
import com.edgenius.wiki.render.object.ObjectPosition;

/**
 * {sharethis}
 * This macro does not bring any parameters, but it will decide save page or save entire site according to 
 * its location,i.e., the client side code will decide if it has spaceUname and PageTitle. If no spaceUname, 
 * then, the saveme means the entire site.
 * 
 * @author Dapeng.Ni
 */
public class ShareMacro extends BaseMacro {
	
	private final static String HANDLER = ShareMacroHandler.class.getName();
	
	//JDK1.6 @Override
	public String[] getName() {
		return new String[]{"sharethis", "saveme"};
	}
	@Override
	public String getHTMLIdentifier() {
		return "<div class='macroShare'>";
	}
	//JDK1.6 @Override
	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
	
		RenderContext context = params.getRenderContext();
		ObjectPosition obj = new ObjectPosition(params.getStartMarkup());
		obj.uuid = context.createUniqueKey(true);
		obj.serverHandler = HANDLER;
		obj.values.putAll(params.getParams());
		
		context.getObjectList().add(obj);
		
		buffer.append(obj.uuid);
	}
	public boolean isPaired(){
		return false;
	}
	
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		resetMacroMarkup(Macro.TIDY_STYLE_BLOCK, node, iter,  getMacroMarkupString(node, "sharethis"), null);
		resetInsideNode(node, iter);
	}
}
