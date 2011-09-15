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

import java.util.ListIterator;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.render.RenderContext;

/**
 * Need consideration
 * How to get space variables? from RenderContext - need lazying loading and cache
 * How to treat it in WYSIWYG editor?
 * 
 * @author Dapeng.Ni
 */
public class VariableFilter  extends BasePatternTokenFilter {
	
	//JDK1.6 @Override
	public void init(){
		regexProvider.compile(getRegex(), Pattern.MULTILINE);
	}
	@Override
	public void replace(StringBuffer buffer, MatchResult result, RenderContext context) {
		
	}
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> nodeIter, RenderContext context) {
		//reset all node 
	}
	//JDK1.6 @Override
	public String getPatternKey() {
		return "filter.variable";
	}
}
