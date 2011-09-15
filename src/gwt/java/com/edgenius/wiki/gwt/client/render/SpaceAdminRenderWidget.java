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
package com.edgenius.wiki.gwt.client.render;

import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.space.SpaceAdminInfoPanel;
import com.edgenius.wiki.gwt.client.space.SpacePermissionPanel;
import com.edgenius.wiki.gwt.client.space.TrashedPagesPanel;
import com.edgenius.wiki.gwt.client.widgets.URLTabPanel;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 */
public class SpaceAdminRenderWidget extends SimplePanel implements  RenderWidget{

	
	public SpaceAdminRenderWidget(final String spaceUname) {
		
		TrashedPagesPanel trash = new TrashedPagesPanel(spaceUname);
		SpacePermissionPanel permPanel = new SpacePermissionPanel(spaceUname);
		SpaceAdminInfoPanel general = new SpaceAdminInfoPanel(spaceUname);
		
		final URLTabPanel panel = new URLTabPanel();
		final int gid = panel.addItem(general, Msg.consts.general(), false);
		panel.addItem(GwtClientUtils.getBaseUrl()+"space/admin!setting.do?spaceUname="+ URL.encodeQueryString(spaceUname), Msg.consts.setting());
		panel.addItem(GwtClientUtils.getBaseUrl()+"space/admin!shell.do?spaceUname="+ URL.encodeQueryString(spaceUname), Msg.consts.shell());
		panel.addItem(permPanel, Msg.consts.security(), true);
		panel.addItem(trash, Msg.consts.trash(), true);

		this.setWidget(panel);
		panel.setSelected(gid,true);
		
	}

	
	public void onLoad(String widgetKey, UserModel user, RenderWidgetListener listener){
		//This widget has not a ajax loading while render 
		//this.listener = listener;
		//at moment - it is not necessary to pass meaningful text - just tell listener, it is success with non-empty content 
		listener.onSuccessLoad(widgetKey, "SPACE ADMIN"); //NON-I18N

	}	
	public void onUserChanged(UserModel user) {
	}
	
}
