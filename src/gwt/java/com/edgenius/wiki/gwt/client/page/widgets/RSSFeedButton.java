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
package com.edgenius.wiki.gwt.client.page.widgets;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Dapeng.Ni
 */
public class RSSFeedButton extends FlowPanel{

	private String spaceUname;
	
	public RSSFeedButton(){
		this(null);
	}
	public RSSFeedButton(String spaceUname){
		this.spaceUname = spaceUname; 

		DOM.setStyleAttribute(this.getElement(), "display", "inline");
		if(this.spaceUname == null || this.spaceUname.trim().length() == 0){
			this.setVisible(false);
		}else{
			buildPanel();
		}
	}
	/*
	 * Must ensure spaceUname is not blank before this method because getRSSURL() requires non-blank spaceUname.  
	 */
	private void buildPanel(){
		this.clear();
		
		Anchor feedLink = new Anchor("RSS",getRSSURL(),"_blank");
		Image feedImg = new Image(IconBundle.I.get().feed());
		feedImg.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				openFeed();
			}


		});
		feedImg.setStyleName(Css.PORTLET_FOOT_IMG);
		
		this.add(feedImg);
		//this just for surround <div> in Anchor, so it looks like with other button
		FlowPanel con = new FlowPanel();
		con.setStyleName("gwt-Hyperlink");
		con.add(feedLink);
		this.add(con);
	}
	public void setSpaceUname(String spaceUname){
		this.spaceUname = spaceUname; 
		if(this.spaceUname == null || this.spaceUname.trim().length() == 0){
			this.setVisible(false);
		}else{
			buildPanel();
			this.setVisible(true);
		}
	}

	private void openFeed() {
		if(this.spaceUname == null || this.spaceUname.trim().length() == 0){
			Window.alert(Msg.consts.error_request());
			return;
		}
		//TODO: user spaceUid? it also not good as it stick to database record, which may change after backup/restore
//		if(GwtUtils.isSupportInURL(spaceUname)){
			Window.open(getRSSURL(),SharedConstants.APP_NAME+"RSSFeed","");
//		}else{
//			Window.open(GwtUtils.getBaseUrl()+"feed.do?suid="+spaceUid,SharedConstants.APP_NAME+"RSSFeed","");
//		}
	}
	/**
	 * @return
	 */
	private String getRSSURL() {
		if(GwtUtils.isSupportInURL(spaceUname))
			return GwtClientUtils.getBaseUrl()+"feed/"+URL.encodeComponent(spaceUname);
		else
			//if spaceUname include "/", and put it into URL rather than parameters, I found any web filter won't get it, and it cause weird problem
			//even after encode.
			return GwtClientUtils.getBaseUrl()+"feed.do?s="+URL.encodeComponent(spaceUname);
	}
}
