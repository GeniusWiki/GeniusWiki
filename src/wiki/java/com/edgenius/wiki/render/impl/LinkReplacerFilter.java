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
package com.edgenius.wiki.render.impl;

import java.util.regex.MatchResult;

import org.apache.commons.lang.StringUtils;

import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.filter.LinkFilter;

/**
 * A special filter only for change link information in text. This method must
 * call after an EscapeFilter.
 * @author Dapeng.Ni
 */
public class LinkReplacerFilter extends LinkFilter{

	//TODO: not finished....
	@Override
	public void replace(StringBuffer buffer, MatchResult result, RenderContext context) {
		LinkModel link = getLinkModel(result,context.getCurrentRegion());
		LinkReplacer replacer = (LinkReplacer) context.getGlobalParam(LinkReplacer.class.getName());
		
		if(replacer.getType() == WikiConstants.AUTO_FIX_COPY_LINK){
			//change spaceUname: append @spaceUname after link
			if(StringUtils.isBlank(link.getSpaceUname())|| StringUtils.equals(link.getSpaceUname(),replacer.getFromSpaceUname())){
				buffer.append(getLink(replacer.getFromSpaceUname(),link));
				return;
			}
		}else if(replacer.getType() == WikiConstants.AUTO_FIX_TITLE_CHANGE_LINK){
			//change pageTitle: change old title to new one
			
			//link has not assign space, then from(content's space) must be same with the space of updated title page 
			if(((StringUtils.isBlank(link.getSpaceUname()) 
					&& StringUtils.equalsIgnoreCase(replacer.getFromSpaceUname(),replacer.getToSpaceUname()))
				//if assigned space, then it must be same with the space of updated title page
				|| StringUtils.equalsIgnoreCase(link.getSpaceUname(),replacer.getToSpaceUname()))
				//title must be same
				&& StringUtils.equalsIgnoreCase(StringUtils.trim(link.getLink()),StringUtils.trim(replacer.getOldTitle()))){
				link.setLink(replacer.getNewTitle());
				buffer.append(getLink(null, link));
				return;
			}
		}
		
		String body;
		//append original text if no change
		if(context.getCurrentRegion() != null){
			body = context.getCurrentRegion().getBody();
		}else{
			body = result.group(0);
		}
		
		buffer.append(body);
		
	}

	@Override
	public String getPatternKey() {
		return null;
	}
	private String getLink(String spaceUname, LinkModel link) {
		
		String anchor = link.getAnchor() == null?"":"#"+link.getAnchor();
		String space;
		if(spaceUname == null){
			//keep original spaceUname if has
			space = StringUtils.isBlank(link.getSpaceUname())?"":"@"+link.getSpaceUname();
		}else{
			//at this case, replacer.toSpaceUname must different with replacer.fromSpaceUname, so always append @spaceUname
			space = "@"+spaceUname;
		}
		return "["+link.getView()+">"+link.getLink()+anchor+space+"]";
	}
	
}
