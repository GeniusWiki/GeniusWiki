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

import org.apache.commons.lang.StringUtils;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;

/**
 * @author Dapeng.Ni
 */
public class PieceMacro extends BaseMacro {
	
	//JDK1.6 @Override
	public String[] getName() {
		return new String[]{"piece"};
	}
	@Override
	public String getHTMLIdentifier() {
		return "<div aid='piece'>";
	}
	//JDK1.6 @Override
	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		String name = params.getParam(NameConstants.NAME);
		if(StringUtils.isBlank(name)){
			buffer.append("Name attribute must have value");
			return;
		}
		
		buffer.append("<div aid='piece' name='").append(name).append("' style='display:inline'>")
			.append(params.getContent()).append("</div>");
		
		
	}
	public boolean isPaired(){
		return true;
	}
	
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		if(node.getPair() != null && node.getAttributes() != null 
			&& !StringUtils.isBlank(node.getAttributes().get(NameConstants.NAME)) ){
			node.reset("{piece:name="+node.getAttributes().get(NameConstants.NAME)+"}", true);
			node.getPair().reset("{piece}", true);
		}
	}
}
