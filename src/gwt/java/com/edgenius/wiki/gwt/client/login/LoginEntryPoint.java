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
package com.edgenius.wiki.gwt.client.login;

import com.edgenius.wiki.gwt.client.AbstractEntryPoint;
import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This module will work as login page 
 * @author Dapeng.Ni
 */
public class LoginEntryPoint  extends AbstractEntryPoint implements UserCreateListener, NativePreviewHandler, ClickHandler, AsyncCallback<UserModel>{
	private VerticalPanel main = new VerticalPanel();
	private FlowPanel logoBar = new FlowPanel();
	
	public void onModuleLoad() {
		//logo and function bar
		Image logo = GwtClientUtils.logo();
		//align center
		logo.removeStyleName(Css.LEFT);
		logo.addClickHandler(this);
		logoBar.add(logo);
		logoBar.setStyleName("login-logobar");
		
		main.setWidth("100%");
		
		RootPanel.get("content").add(main);
		Event.addNativePreviewHandler(this);
		
		//the reason use another ajax call then display login deck is because need decide if display "sign-up" 
		//it may slow but can not find another good way yet
		SecurityControllerAsync securityService = ControllerFactory.getSecurityController();
		securityService.checkLogin(getJsInfoModel(), this);
	}

	public void userCreateCancelled() {
		//DO nothing, so cancel button in embed mode
	}

	public void userCreated(UserModel model) {
		//GOTO home page or UserModel url from server side
		String url = model.getRedirUrl() == null?SharedConstants.URL_HOME:model.getRedirUrl();
		GwtClientUtils.reload(url);
		
	}

	public void onPreviewNativeEvent(NativePreviewEvent event){
		int type = event.getTypeInt();
		
		//IE only work for Event.ONKEYDOWN but not Event.ONKEYPRESS (FF is OK)
		if (!event.isCanceled() && type == Event.ONKEYDOWN) {
			int keyCode = event.getNativeEvent().getKeyCode();
			if(keyCode == ClientConstants.KEY_F1){
				Window.alert(Msg.params.login_for_help(SharedConstants.APP_NAME));
				//in FF, it block the FF help window.
				event.cancel();
			}
		
		}
	}

	public void onClick(ClickEvent event) {
		//go home page
		GwtClientUtils.redirect(GwtClientUtils.getBaseUrl());
	}

	public void onFailure(Throwable caught) {
		//failure tolerance, anyway just display sign-up, it won't hurt as server side has further check if allow public signup
		showLoginDeck();
	}

	public void onSuccess(UserModel user) {
		if(!GwtClientUtils.preSuccessCheck(user,null)){
			showLoginDeck();
			return;
		}
		//put it to static, so that if session logout, or other case, the signup button is not show up
		AbstractEntryPoint.setSuppress(user.getSuppress());
		showLoginDeck();
		
	}
	
	private void showLoginDeck(){
		main.clear();
		main.add(logoBar);
		
		//get Hidden value from page - this value is redir URL value
	    Element redirDiv = RootPanel.get("redirURLForLogin").getElement();
	    String redirUrl = DOM.getElementAttribute(redirDiv, "value");
	    
	    Element regisgerDiv = RootPanel.get("regisgerDiv").getElement();
	    String regisger = DOM.getElementAttribute(regisgerDiv, "value");
	    
		LoginSignupDeck deck = new LoginSignupDeck(redirUrl, null,"true".equalsIgnoreCase(regisger)?LoginDialog.SINGUP:LoginDialog.LOGIN);
		deck.addUserCreateListener(this);
		main.add(deck);
		DOM.setStyleAttribute(deck.getElement(), "width", "100%");
		
		main.setCellHorizontalAlignment(logoBar, HasHorizontalAlignment.ALIGN_CENTER);
		main.setCellVerticalAlignment(logoBar, HasVerticalAlignment.ALIGN_BOTTOM);
		main.setCellHorizontalAlignment(deck, HasHorizontalAlignment.ALIGN_CENTER);
	}

}
