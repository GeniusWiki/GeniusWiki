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
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 */
public class LogoRenderWidget extends SimplePanel implements RenderWidget, ClickHandler{
	
	public LogoRenderWidget(){
		Image logo = GwtClientUtils.logo();
		logo.addClickHandler(this);
		this.setWidget(logo);
	}

	public void onLoad(String widgetKey, UserModel user, RenderWidgetListener listener){
		//This widget has not ajax action
		//at moment - it is not necessary to pass meaningful text - just tell listener, it is success with non-empty content 
		listener.onSuccessLoad(widgetKey, "LOGO"); //NON-I18N

	}

	public void onClick(ClickEvent event) {
		//go home page
		GwtClientUtils.redirect(GwtClientUtils.getBaseUrl());
	}

	public void onUserChanged(UserModel user) {
	}

}
