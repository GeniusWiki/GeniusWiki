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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.render.ReferenceContentFilter;
import com.edgenius.wiki.render.Region;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.TokenVisitor;
import com.edgenius.wiki.render.macro.TOCMacro;

/**
 * @author Dapeng.Ni
 */
public class HeadingFilter extends BasePatternTokenFilter implements ReferenceContentFilter{
	
	private MessageFormat formatter;
	
	public String getPatternKey() {
		return "filter.heading";
	}
	
	//JDK1.6 @Override
	public void init() {

		regexProvider.compile(getRegex(), Pattern.MULTILINE);
		
	    formatter = new MessageFormat("");
	    formatter.applyPattern(getReplacement());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void replace(StringBuffer buffer, MatchResult matchResult, RenderContext context) {
		int i = matchResult.groupCount();
		if(i<3){
			//failure tolerance
			buffer.append(matchResult.group(0));
			return;
		}
		String lead = matchResult.group(1);
		String level = matchResult.group(3);
		//please note this piece text should be regionKey rather than original text.
		String title = matchResult.group(4);
		
		String tail= matchResult.group(5);
		
		List<HeadingModel> list = (List<HeadingModel>) context.getGlobalParam(TOCMacro.class.getName());
		if(list == null){
			list = new ArrayList<HeadingModel>();
			context.putGlobalParam(TOCMacro.class.getName(),list);
		}
		//put anchor data to RenderContext.global for later TOCMacro render (if page contain TOCMacro) 
		//The anchor must not change for each render - otherwise it is can not be redirect again once page refresh.
		String headerAnchor = "HeaderAnchor" + list.size();
		HeadingModel head = new HeadingModel();
		head.setOrder(context.createIncremetalKey());
		head.setLevel(NumberUtils.toInt(level));
		head.setTitle(title);
		head.setAnchor(headerAnchor);
		
		list.add(head);
		
		//group(2): number of heading, 
		buffer.append(formatter.format(new Object[]{lead,level,title,tail,headerAnchor}));
	}

	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> nodeIter, RenderContext context) {
//		if(node.getTagName().length() != 2){
//			log.error("Unexpectd head tag, should be 2 characters " + node.getTagName());
//			return;
//		}
		if(node.getPair() == null){
			log.error("No close tag for heading tag");
			return;
		}
			
		int head = NumberUtils.toInt("" + (node.getTagName().charAt(1)),-1);
		if(head < 1 || head >6){
			log.error("Unexpectd head tag, should be h[1-6]" + node.getTagName());
			return;
		}
		
		node.reset(HTMLNode.LINE_START_TAG,false);
		//after new line tag, add new text tag for markup 
		nodeIter.add(new HTMLNode("h"+head+". ", true));
		node.getPair().reset(HTMLNode.LINE_END_TAG, false);
		
	}

	public List<Region> getRegions(CharSequence input) {
		final List<Region> list = new ArrayList<Region>();
		regexProvider.replaceByTokenVisitor(input, new TokenVisitor<Matcher>() {
			public void handleMatch(StringBuffer buffer, Matcher matcher) {
				int contentStart = matcher.start(4);
				int contentEnd= matcher.end(4);
				int start = contentStart;
				int end = contentEnd;
				list.add(new Region(HeadingFilter.this,false,start,end,contentStart,contentEnd));
			}

		});
		return list;
	}

}
