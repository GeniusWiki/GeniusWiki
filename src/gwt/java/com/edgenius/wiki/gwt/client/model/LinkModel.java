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
package com.edgenius.wiki.gwt.client.model;

import java.util.HashMap;
import java.util.Map;

import com.edgenius.wiki.gwt.client.html.HTMLUtil;
import com.edgenius.wiki.gwt.client.server.utils.LinkUtil;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.NumberUtil;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;


/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class LinkModel extends GeneralModel  implements RenderPiece {
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//               PageLink.Type
	public static final int LINK_TO_CREATE_FLAG = 0;
	public static final int LINK_TO_VIEW_FLAG = 1;
	public static final int LINK_TO_CREATE_HOME_FLAG = 2;
	
	public static final int LINK_TO_OPEN_NEW_WIN = 4;
	//LinkModel.getLink() is just token in HyperLink(view,token)
	//use HyperLink() to invoke server side plugin service: 
	//This makes page has browser history, also, page still can hold if refresh page as URL is changed  
	public static final int LINK_TO_HYPER_TOKEN = 5;
	//use ClickLink() to invoke server side plugin service 
	//URL won't change, no browser history 
	public static final int LINK_TO_SERVER_CLICK_LINK = 6;
	//invoke client side java script: must know javascript method name first...
	public static final int LINK_TO_CLIENT_CLICK_LINK = 7;
	//link start with http:// or https:// etcn
	public static final int LINK_TO_READONLY = 8;
	public static final int LINK_TO_ATTACHMENT = 9;

	//identifier to this link is Rich to Markup handler, default is null, but it maybe a UserFilter, ie,  
	//@user@ also render to a link, but this aid should be com...UserFilter
	private String aid;
	//internal: create/view: 0:1
	//external: open in same window/open in new window : 2:3
	private int type;
	private String spaceUname;
	private String link;
	private String view;
	private String anchor;

	//this field is for final URL according to different render scenarios
	//!!!IMPORTANT: if user initial LinkModel class(such as HeaderMacro or UserMacro for special link), this string must manually filled 
	//for NativeHTML render purpose(RSS, Print etc).
	private String linkTagStr;
	//********************************************************************
	//               method
	//********************************************************************
	public String getAid() {
		return aid;
	}
	public void setAid(String aid) {
		this.aid = aid;
	}

	public String getAnchor() {
		return anchor;
	}
	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getView() {
		return view;
	}
	public void setView(String view) {
		this.view = view;
	}
	public String getLinkTagStr() {
		return linkTagStr;
	}
	
	public void setLinkTagStr(String url) {
		this.linkTagStr = url;
	}
	public String getSpaceUname() {
		return spaceUname;
	}
	public void setSpaceUname(String spaceUname) {
		this.spaceUname = spaceUname;
	}
	
	public void fillToObject(String tagString, String enclosedText) {
		Map<String,String> map = HTMLUtil.parseAttributes(tagString);
		//get wajax attribute value
		String wajax = map.get(NameConstants.WAJAX);
		if(!StringUtil.isBlank(wajax)){
			map = RichTagUtil.parseWajaxAttribute(wajax);
			this.type = NumberUtil.toInt(map.get(NameConstants.TYPE), LinkModel.LINK_TO_VIEW_FLAG);
			this.spaceUname = map.get(NameConstants.SPACEUNAME);
			this.link = map.get(NameConstants.LINK);
			this.anchor = map.get(NameConstants.ANCHOR);
			this.view = enclosedText;
		}else{
			//external link, just use href as link and encloseText is view
			this.link = map.get(NameConstants.HREF);
			this.view = enclosedText;
		}
		
		
	}
	
	public String toRichAjaxTag() {
		if(LinkUtil.isExtLink(link)){
			String anchorTxt = anchor ==null?"":"#" + anchor;
			return new StringBuffer("<a href='").append(link).append(anchorTxt)
			.append(link.trim().startsWith("mailto:")?"'":"' target='_blank'")
			.append(">").append(view).append("</a>").toString();
		}else{
			Map<String,String> wajaxMap = new HashMap<String,String>();
			wajaxMap.put(NameConstants.TYPE, String.valueOf(type));
			wajaxMap.put(NameConstants.SPACEUNAME, spaceUname);
			wajaxMap.put(NameConstants.LINK, link);
			wajaxMap.put(NameConstants.ANCHOR, anchor);
			
			String wajax = RichTagUtil.buildWajaxAttributeString(LinkModel.class.getName(),wajaxMap);
			
			//build tag
			Map<String,String> attributes = new HashMap<String,String>();
			attributes.put(NameConstants.HREF, "#");
			//aid could be null(default) or com...UserFilter etc.
			attributes.put(NameConstants.AID, aid);
			attributes.put(NameConstants.WAJAX, wajax);
			
			return HTMLUtil.buildTagString("a",view, attributes);
		}
	}
	
	public String toString(){
		//??
		return " "+getView()+" ";
	}
}
