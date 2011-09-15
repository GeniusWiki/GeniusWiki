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
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.render.ImmutableContentMacro;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;

/**
 * This macro is very special. It is not intend to used by mark, i.e.,  no {xxxx} format in markup.
 * It only for render HTML back to markup for error render markup.  For example, calendar macro needs name parameter
 * otherwise it renders to <span aid="rendererror" hint="xxxx">{cal}</span> to tell user this macro has error.
 * If this error text is render to markup directly, {cal} will be escape to \{cal\}.   To keep original markup,
 * this macro is adopted. It implements ImmutableContentMacro interface to avoid text to be escape.  
 *
 * @author Dapeng.Ni
 */
public class RenderErrorEnclosureMacro extends BaseMacro implements ImmutableContentMacro{

	public String getHTMLIdentifier() {
		return "<span aid='"+SharedConstants.RENDER_ERROR_TAG+"'>";
	}
	public boolean isPaired() {
		return true;
	}
	public boolean isProcessEmbedded(){
		return false;
	}
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		if(node.getPair() != null){
			node.getPair().reset("", true);
		}
		node.reset("", true);
		
	}
	
	//This name is not intended used in markup
	public String[] getName() {return new String[]{"RENDER_ERROR_ENCLOSURE"};}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//NO need Implementation 
	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {}
}
