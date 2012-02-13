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
package com.edgenius.wiki.gwt.client.widgets;

import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.EventfulHyperLink;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 */
public class NavItem extends SimplePanel{
	
	private String view;
	private ClickLink nav1 = null;
	private EventfulHyperLink nav2 = null;
	private String relativeUrlOrToken;
	
	public NavItem(ClickLink nav){
		this.setWidget(nav);
		DOM.setStyleAttribute(this.getElement(), "display", "inline");
	}
	public NavItem(String view, String token, String toolTip){
		this(view,token,toolTip, false);
	}
	/**
	 * 
	 * @param view
	 * @param relativeUrlOrToken : this URL must already be GwtUtil.escape()!
	 * @param link
	 */
	public NavItem(String view, final String relativeUrlOrToken, String toolTip, boolean link){
		this.view = view;
		this.relativeUrlOrToken = relativeUrlOrToken;
		if(link){
			nav1 = new ClickLink(view);
			if(!StringUtil.isBlank(toolTip)){
				nav1.setTitle(toolTip);
			}
			nav1.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					invoke();
				}
			});
			this.setWidget(nav1);
		}else{
			nav2 =new EventfulHyperLink(view,relativeUrlOrToken);
			if(!StringUtil.isBlank(toolTip)){
				nav2.setTitle(toolTip);
			}
			this.setWidget(nav2);
		}
		
		DOM.setStyleAttribute(this.getElement(), "display", "inline");
		
	}

	public String getText(){
		return view;
	}
	public void setText(String view){
		this.view = view;
		
		if(nav1 != null)
			nav1.setText(view);
		else
			nav2.setText(view);
	}
	/**
	 * invoke click event whatever it is ClickLink or HyperLink
	 */
	public void invoke() {
		if(nav1 != null){
			GwtClientUtils.redirect(relativeUrlOrToken);
		}else{
			GwtClientUtils.refreshToken(relativeUrlOrToken);
		}
		
	}
}
