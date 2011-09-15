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

import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.model.SpacePagesModel;
import com.edgenius.wiki.gwt.client.offline.SyncButton;
import com.edgenius.wiki.gwt.client.page.widgets.FunctionWidget;
import com.edgenius.wiki.gwt.client.page.widgets.RSSFeedButton;
import com.edgenius.wiki.gwt.client.portal.PortletListener;
import com.edgenius.wiki.gwt.client.render.PageRender;
import com.edgenius.wiki.gwt.client.render.WikiRenderPanel;
import com.edgenius.wiki.gwt.client.server.PortalControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Dapeng.Ni
 */
public class SpacePortlet extends PageListPortlet implements AsyncCallback<PortletModel>{
	

	private WikiRenderPanel renderPanel;
	/**
	 * @param allowOffline
	 */
	public SpacePortlet() {
	}

	public void render() {
			
		//to long if append unix name, then just put title
//		+ " - "+spaceModel.unixName
		// +" - " + portletModel.description
		setPortletTitle(portletModel.title, Msg.consts.spacekey() + ":"+ portletModel.key ,portletModel.titleURL);
		
		//try to get recently page
		refresh();
	}

	public void onSuccess(PortletModel portlet){
		busy(false);
		
		if(!GwtClientUtils.preSuccessCheck(portlet,null)){
			//show error message on item part rather than general error message on HTML page above
			container.clear();
			container.add(ErrorCode.getMessage(portlet.errorCode, portlet.errorMsg));
			return;
		}
	
		final SpacePagesModel model = (SpacePagesModel) portlet.renderContent;
		if(model.space == null)
			return;
		

		setPortletLogo(model.space.smallLogoUrl);
		
		clearControl();
		
		//RSS feed
		Hyperlink homeLink = new Hyperlink(Msg.consts.home(), GwtUtils.getSpacePageToken(model.space.unixName, null));
		Image homeImg = new Image(IconBundle.I.get().home());
		homeImg.setStyleName(Css.PORTLET_FOOT_IMG);
		addControl(homeImg);
		addControl(homeLink);
		
		if(!portlet.isOffline){
			//RSS feed
			addSeparator();
			RSSFeedButton rssFeedBtn = new RSSFeedButton(model.space.unixName);
			addControl(rssFeedBtn);
			
			if(model.space.permissions[ClientConstants.ADMIN] == 1){
				//XXX:i18n
//				Image frdImg = new Image(IconBundle.I.get().group());
//				Hyperlink frdLink = new Hyperlink(Msg.consts.friends(),FunctionWidget.viewFriendsToken(portletModel.key));
//				addControl(frdImg);
//				addControl(frdLink);
//				Label sep2 = new Label(" | ");
//				sep2.setStyleName(Css.PORTLET_WEAK_TEXT);
//				addControl(sep2);
//				frdImg.setStyleName(Css.PORTLET_FOOT_IMG);
				
				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				//               admin button
				addSeparator();
				Hyperlink adminLink = new Hyperlink(Msg.consts.admin(),FunctionWidget.viewSpaceAdminToken(portletModel.key));
				Image adminImg = ButtonIconBundle.adminImage();
				addControl(adminImg);
				addControl(adminLink);
				adminImg.setStyleName(Css.PORTLET_FOOT_IMG);
			}
			
			if(model.space.permissions[ClientConstants.OFFLINE] == 1){
				addSeparator();
				SyncButton offlineBtn = new SyncButton(viewUser, model.space.unixName,false);
				addControl(offlineBtn);
				//special style, there is conflict with login panel offlineBtn, so here I have to add special CSS
				DOM.setStyleAttribute(offlineBtn.getElement(), "verticalAlign", "bottom");
			}
		}
		if(model.isPrivate){
			Image privateImg = new Image(IconBundle.I.get().lock());
			privateImg.setTitle(Msg.consts.private_space());
			addControl(privateImg);
			DOM.setStyleAttribute(privateImg.getElement(), "float", "right");
		}
		
		if(model.widgetStyle == SharedConstants.WIDGET_STYLE_HOME_PAGE){
			fillHomepage(model.space.unixName, model.page);
		}else{
			int style = (model.hidePortrait)?0:STYLE_SHOW_PORTRAIT;
			if(model.widgetStyle == SharedConstants.WIDGET_STYLE_ITEM_SHORT_BY_CREATE_DATE){
				style = style | STYLE_SHOW_CREATED_BY;
			}else  if(model.widgetStyle == SharedConstants.WIDGET_STYLE_ITEM_SHORT_BY_MODIFIED_DATE){
				style = style | STYLE_SHOW_MODIFIED_BY;
			}
				
			fillList(model.pages,FILL_TYPE_PAGE,style);
		}
	}

	/**
	 * 
	 */
	private void addSeparator() {
		Label sep0 = new Label(" | ");
		sep0.setStyleName(Css.PORTLET_WEAK_TEXT);
		addControl(sep0);
	}

	/**
	 * @param page
	 * @param spaceUname 
	 */
	private void fillHomepage(String spaceUname, PageModel page) {
		container.clear();
		renderPanel = new WikiRenderPanel();
		container.add(renderPanel);
		
		PageRender render = new PageRender(renderPanel);
		render.renderContent(spaceUname, page, page.renderContent, false);
		
		
	}

	public void onFailure(Throwable error) {
		busy(false);
		GwtClientUtils.processError(error);
	}

	public void close() {
		if(Window.confirm(Msg.params.confirm_close_portlet(portletModel.title ))){
			for(PortletListener listener : listeners){
				listener.close(this);
			}
		}
		
	}
	public void refresh() {
		busy(true);
		PortalControllerAsync portalController = ControllerFactory.getPortalController();
		portalController.invokePortlet(portletModel.type, portletModel.key, this);
		
	}


}
