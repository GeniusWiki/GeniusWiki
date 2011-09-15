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
import java.util.regex.Pattern;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.render.RenderContext;


/**
 * @author Dapeng.Ni
 */
public class LineFilter extends BasePatternFilter{
	//JDK1.6 @Override
	public void init(){
		regexProvider.compile(getRegex(), Pattern.MULTILINE);
	}
	public String getPatternKey() {
		return "filter.line";
	}
	
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> nodeIter, RenderContext context) {
		
		node.reset(HTMLNode.LINE_START_TAG,false);
		//after new line tag, add new text tag for markup 
		nodeIter.add(new HTMLNode(getMarkupPrint(), true));
		nodeIter.add(new HTMLNode(HTMLNode.LINE_END_TAG, false));
		
		if(node.getPair() != null)
			node.getPair().reset("", true);
		
	}
}
