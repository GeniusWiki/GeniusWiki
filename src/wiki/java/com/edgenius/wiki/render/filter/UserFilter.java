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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.render.ImmutableContentFilter;
import com.edgenius.wiki.render.Region;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.TokenVisitor;
import com.edgenius.wiki.render.handler.UserHandler;
import com.edgenius.wiki.render.object.ObjectPosition;

/**
 * Render markup @userID@ to user popup panel.
 * 
 * @author Dapeng.Ni
 */
public class UserFilter  extends BasePatternTokenFilter  implements ImmutableContentFilter{
	private static final String HANDLER = UserHandler.class.getName();
	public String getPatternKey() {
		return "filter.user";
	}
	//JDK1.6 @Override
	public void init(){
		regexProvider.compile(getRegex(), Pattern.MULTILINE);
	}
	
	//JDK1.6 @Override
	public List<Region> getRegions(CharSequence input) {
		final List<Region> list = new ArrayList<Region>();
		regexProvider.replaceByTokenVisitor(input, new TokenVisitor<Matcher>() {
			public void handleMatch(StringBuffer buffer, Matcher matcher) {
				int contentStart = matcher.start(2);
				int contentEnd= matcher.end(2);
				int start = contentStart -1;
				int end = contentEnd +1;
				list.add(new Region(UserFilter.this,true,start,end,contentStart,contentEnd));
			}

		});
		return list;
	}

	@Override
	public void replace(StringBuffer buffer, MatchResult result, RenderContext context) {
		if(context.getCurrentRegion() == null){
			AuditLogger.error("Unexpected case: Immutable fitler cannot find out current region." + result.group());
		}

		String content = context.getCurrentRegion() != null? context.getCurrentRegion().getContent(): result.group(2);
		String username = content;
		
		StringBuffer sb = new StringBuffer(result.group(1));
		
		if (!StringUtils.isBlank(username)) {
			HashMap<String, String> attValues = new HashMap<String, String>();
			username = username.trim();
			
			attValues.put(NameConstants.NAME, username);
			
			//this image scr will be handler after all page finish scan to HTML
			ObjectPosition userPos = new ObjectPosition(result.group(0));
			userPos.uuid = context.createUniqueKey(false);
			userPos.serverHandler = HANDLER;
			userPos.values = attValues;
			context.getObjectList().add(userPos);
			sb.append(userPos.uuid);
			
			buffer.append(sb);
			
			//append tailed text after filter
			if(result.groupCount() > 2)
				buffer.append(result.group(3));
		} else {
			//if only "@@" in page, do nothing
			buffer.append(result.group(0));
		}
		
	}
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> nodeIter, RenderContext context){
		if(node.getPair() == null){
			AuditLogger.error("Unexpected case: Unable to find close </a> tag");
			return;
		}

		LinkModel link = new LinkModel();
		//we don't need view info,so just skip it
		link.fillToObject(node.getText(),null);
		
		//We put username into last part of anchor: /$CPAGE/up/admin
		String linkURL = link.getAnchor();
		if(StringUtils.endsWith(linkURL,"/")){
			linkURL = StringUtils.removeEnd(linkURL, "/");
		}
		
		int idx = StringUtils.lastIndexOf(linkURL,"/");
		if(idx != -1){
			String username = linkURL.substring(idx+1);
			HTMLNode subnode = node.next();
			while(subnode != null && subnode != node.getPair()){
				if(subnode.isTextNode())
					subnode.reset("", true);
				subnode = subnode.next();
			}
			node.getPair().reset("", true);
			String markupBorder = getSeparatorFilter(node);
			StringBuffer markup = new StringBuffer(markupBorder).append("@");
			markup.append(username);
			markup.append("@").append(markupBorder);
			
			node.reset(markup.toString(), true);
		}			
	}

}
