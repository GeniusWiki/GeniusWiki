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
package com.edgenius.wiki.gwt.client.user;

import com.edgenius.wiki.gwt.client.widgets.Popup;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Show user profile, portrait, recent authored pages etc. Currently limitation:
 *  This popup  must inside PageApplication.gwt.xml scope because it show page link as page title token style.
 *  Only given space page list can show on this popup
 * @author Dapeng.Ni
 */
public class UserPopup extends Popup{
	private VerticalPanel panel = new VerticalPanel();
	private String portrait;
	private String username;
	private String spaceUname;

	public void pop(){
		//only when window pop, the UserInfoPanel is initialize as it has a Ajax server call
		//it is not good idea happen in UserPopup() as this class can be added a lot in same page - UserProfileLink <-PageListPortlet
		panel.clear();
		UserInfoPanel info = new UserInfoPanel(this, spaceUname, username,portrait,true);
		info.setSize("100%", "100%");
		panel.add(info);
		super.pop();
	}
	public UserPopup(UIObject target, String spaceUname, String username, String portrait){
		super(target,true, true, true);
		
		this.spaceUname = spaceUname;
		this.username = username;
		this.setWidget(panel);
		panel.setSize("100%", "100%");

	}


}
