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

import java.text.MessageFormat;
import java.util.ListIterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;


/**
 * @author Dapeng.Ni
 */
public class UrlFilter  extends BasePatternTokenFilter {
	private MessageFormat formatter;
	private Pattern END_PATTERN = Pattern.compile("([!:.,;'?]+)");
	
	//JDK1.6 @Override
	public void init() {
		regexProvider.compile(getRegex(), Pattern.MULTILINE);
		
	    formatter = new MessageFormat(getReplacement());
	}

	public String getPatternKey() {
		return "filter.url";
	}
	
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> nodeIter, RenderContext context) {
		
		node.reset("", true);
		if(node.getPair() != null)
			node.getPair().reset("", true);
	}
	@Override
	public void replace(StringBuffer buffer, MatchResult matchResult, RenderContext context) {
		int count = matchResult.groupCount();
		if(count != 2){
			buffer.append(matchResult.group(0));
			return;
		}
		
		String front = matchResult.group(1);
		String link = matchResult.group(2);
		String end = "";
		Matcher m = END_PATTERN.matcher(StringUtils.reverse(link));
		if(m.lookingAt()){
			end = StringUtils.reverse(m.group(1));
			link = link.substring(0, link.length() - end.length());
		}
		
		buffer.append(formatter.format(new Object[]{front,link,end,RenderUtil.getExternalImage(context)}));
		
	}
}
