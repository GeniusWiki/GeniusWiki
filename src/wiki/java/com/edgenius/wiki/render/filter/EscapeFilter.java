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
package com.edgenius.wiki.render.filter;

import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.render.MarkupUtil;
import com.edgenius.wiki.render.RenderContext;

/**
 * @author Dapeng.Ni
 */
public class EscapeFilter extends BasePatternFilter {
	//JDK1.6 @Override
	public void init() {
		//override, so that no pattern compile  
	}
	//JDK1.6 @Override
	public String getPatternKey() {
		return null;
	}
	
	@Override
	public String filter(String input, RenderContext context){

		//escape
		input = EscapeUtil.escapeHTML(input);
		//!!!must after EscapeUtil.escapeHTML() so that "&" cannot be replaced again 
		input = MarkupUtil.escapeMarkupToEntity(input);
		
		//above escape does not handle "\>" correctly, as  EscapeUtil.escapeHTML() 
		//will process it to "\&gt;", and  MarkupUtil.escapeMarkupToEntity() does not handle it anymore, so here do special handle
		//just simple use replaceAll() as even "\\" already replace to HTML entity in MarkupUtil.escapeMarkupToEntity()
		input = input.replaceAll("\\\\&gt;", "\\&#62;");
		
		//restore from region border text
		return  input;

	}
	
}
 
