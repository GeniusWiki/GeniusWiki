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
public class KeyFilter extends BasePatternFilter{
	private static final String pattern = "((?:ctrl|alt|shift)-[^ ]+)";
	
	public String getPatternKey() {
		return "filter.key";
	}
	//JDK1.6 @Override
	public void init(){
		//case insensitive
		regexProvider.compile(getRegex(), Pattern.DOTALL|Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
	}
	@Override
	public String getMarkupPrint() {
		return pattern;
	}
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> nodeIter, RenderContext context) {
		HTMLNode paired = node.getPair();
		if(paired == null){
			log.error("can not find correct text for key");
		}else{
			paired.reset("", true);
		}
		node.reset("", true);
		
	}

}
