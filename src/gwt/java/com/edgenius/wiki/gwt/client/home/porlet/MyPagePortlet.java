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
package com.edgenius.wiki.gwt.client.home.porlet;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.server.PortalControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;

/**
 * Could be my draft, my favourite, my watched.
 * @author Dapeng.Ni
 */
public class MyPagePortlet extends PageListPortlet implements AsyncCallback<PortletModel>{
	
	public void render() {
		this.header.addStyleDependentName(Css.DEP_WIDGET);
		this.addStyleDependentName(Css.DEP_WIDGET);
		
		setPortletTitle(portletModel.title ,portletModel.title, null);
		
		if(PortletModel.DRAFT_LIST == portletModel.type)
			setPortletLogo(new Image(IconBundle.I.get().draft()));
		
		else if(PortletModel.FAVORITE_LIST == portletModel.type)
			setPortletLogo(new Image(IconBundle.I.get().favorite()));
		
		else if(PortletModel.WATCHED_LIST == portletModel.type)
			setPortletLogo(new Image(IconBundle.I.get().watch()));
		
		//try to get page
		refresh();
	}

	public void onFailure(Throwable error) {
		busy(false);
		GwtClientUtils.processError(error);
	}
	public void onSuccess(PortletModel portlet) {
		busy(false);
		
		if(!GwtClientUtils.preSuccessCheck(portlet,null)){
			//show error message on item part rather than general error message on HTML page above
			container.clear();
			container.add(ErrorCode.getMessage(portlet.errorCode, portlet.errorMsg));
			return;
		}
	
		PageItemListModel model = (PageItemListModel) portlet.renderContent;
		if(PortletModel.DRAFT_LIST == portletModel.type){
			fillList(model.itemList,FILL_TYPE_DRAFT,0);
		}else{
			fillList(model.itemList,FILL_TYPE_MYPAGE,0);
		}
	}
	

	public void refresh() {
		busy(true);
		PortalControllerAsync portalController = ControllerFactory.getPortalController();
		portalController.invokePortlet(portletModel.type,null, this);
		
	}

}
