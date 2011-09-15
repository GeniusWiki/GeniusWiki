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

import java.util.Iterator;
import java.util.Vector;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.AbstractEntryPoint;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonBar;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class LoginSignupDeck extends SimplePanel implements ClickHandler{
	
	private DeckPanel deck = new DeckPanel();
	private LoginForm loginForm;
	//autologin
	private SignupForm signupForm;
	private ForgetPasswordForm forgetForm;
	private ButtonBar btnBar;
	private DialogBox dialog;
	
	private Button signupBtn = new Button(Msg.consts.signup());
	private ClickLink gotoSignupBtn = new ClickLink(Msg.consts.signup_now());
	private ClickLink gotoLoginBtn = new ClickLink(Msg.consts.login_now());
	private Button loginBtn  = new Button(Msg.consts.login()); 
	private Button sendBtn  = new Button(Msg.consts.send()); 
	private Button cancelBtn = new Button(Msg.consts.cancel());
	private ClickLink forgetPassLink = new ClickLink(Msg.consts.forget_password(), true); 
	
	private Image loginIcon = new Image(IconBundle.I.get().lock());
	private Image signupIcon = new Image(IconBundle.I.get().lock_add());
	private Image forgetIcon = new Image(IconBundle.I.get().email());
	
	private Vector<UserCreateListener> listeners = new Vector<UserCreateListener>();
	
	public LoginSignupDeck(String redirUrl, final DialogBox dialog, int loginOrSignup){
		Log.info("Login redir URL is " + redirUrl);
		    
		this.dialog = dialog;
		loginForm = new LoginForm(dialog, loginBtn, redirUrl, AbstractEntryPoint.isOffline());
		if(AbstractEntryPoint.isAllowPublicSignup()){
			signupForm = new SignupForm(dialog,signupBtn, redirUrl);
		}
		forgetForm = new ForgetPasswordForm(sendBtn);
		
		VerticalPanel loginPanel = new VerticalPanel();
		loginPanel.add(loginForm);
		loginBtn.setIcon(ButtonIconBundle.login());
		cancelBtn.setIcon(ButtonIconBundle.crossImage());
		signupBtn.setIcon(ButtonIconBundle.createImage());
		sendBtn.setIcon(ButtonIconBundle.email());
		
		deck.insert(loginPanel, LoginDialog.LOGIN);
		
		if(dialog != null){
			btnBar = dialog.getButtonBar();
			this.setWidget(deck);
		}else{
			btnBar = new ButtonBar();
			VerticalPanel panel = new VerticalPanel();
			panel.add(deck);
			panel.add(btnBar);
			this.setWidget(panel);
			//In chrome browser, the login / signup form don't align to center, add this line to fix.
			DOM.setElementAttribute(panel.getElement(), "align", "center");
		}
		
		if(!AbstractEntryPoint.isOffline()){
			gotoLoginBtn.addClickHandler(this);
			btnBar.add(gotoLoginBtn);
			//must set style after btnBar.add() as it will reset style.
			gotoLoginBtn.setStyleName(Css.ACTION);
			
			forgetPassLink.addClickHandler(this);
			btnBar.add(forgetPassLink);
			//must set style after btnBar.add() as it will reset style.
			forgetPassLink.setStyleName(Css.ACTION);
			
			gotoSignupBtn.addClickHandler(this);
			btnBar.add(gotoSignupBtn);
			//must set style after btnBar.add() as it will reset style.
			gotoSignupBtn.setStyleName(Css.ACTION);
		}
		
		//first button
		if(dialog != null){
			cancelBtn.addClickHandler(this);
			btnBar.add(cancelBtn);
		}
		
		if(!AbstractEntryPoint.isOffline()){

			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			//               Forgot password
			VerticalPanel forgetPanel = new VerticalPanel();
			forgetPanel.add(forgetForm);
			deck.insert(forgetPanel, LoginDialog.FORGET_PASSWORD);
			
			sendBtn.addClickHandler(this);
			btnBar.add(sendBtn);
			
			if(AbstractEntryPoint.isAllowPublicSignup()){
				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				//               Signup
				VerticalPanel signupPanel = new VerticalPanel();
				signupPanel.add(signupForm);
				deck.insert(signupPanel, LoginDialog.SINGUP);
				signupBtn.addClickHandler(this);
				btnBar.add(signupBtn);
			}
		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               Login
		loginBtn.addClickHandler(this);
		btnBar.add(loginBtn);

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Initial status
		signupBtn.setVisible(false);
		sendBtn.setVisible(false);
		loginBtn.setVisible(false);
		cancelBtn.setVisible(false);
		forgetPassLink.setVisible(false);
		gotoLoginBtn.setVisible(false);
		gotoSignupBtn.setVisible(false);
		
		if(loginOrSignup == LoginDialog.SINGUP)
			showSignup();
		else
			//login or offline login
			showLogin();

	}
	
	public void addUserCreateListener(UserCreateListener listener){
		listeners.add(listener);
		if(signupForm != null){
			signupForm.addListener(listener);
		}
	}
	
	private void showLogin(){
		
		deck.showWidget(LoginDialog.LOGIN);
		if(dialog != null){
			dialog.setText(Msg.consts.login());
			dialog.setIcon(loginIcon);
			cancelBtn.setVisible(true);
		}
		signupBtn.setVisible(false);
		sendBtn.setVisible(false);
		loginBtn.setVisible(true);
		forgetPassLink.setVisible(true);
		if(!AbstractEntryPoint.isAllowPublicSignup()){
			gotoSignupBtn.setVisible(false);
		}else{
			gotoSignupBtn.setVisible(true);
		}
		gotoLoginBtn.setVisible(false);
		
	}


	private void showSignup(){
		if(!AbstractEntryPoint.isAllowPublicSignup()){
			Window.alert(ErrorCode.getMessageText(ErrorCode.USER_SIGNUP_DISABLED, null));
			return;
		}
			
		deck.showWidget(LoginDialog.SINGUP);
		if(dialog != null){
			dialog.setText(Msg.consts.signup());
			dialog.setIcon(signupIcon);
			cancelBtn.setVisible(true);
		}
		signupBtn.setVisible(true);
		sendBtn.setVisible(false);
		loginBtn.setVisible(false);
		gotoLoginBtn.setVisible(true);
		
		gotoSignupBtn.setVisible(false);
		forgetPassLink.setVisible(false);
	}
	private void showForgetPassword(){
		deck.showWidget(LoginDialog.FORGET_PASSWORD);
		if(dialog != null){
			dialog.setText(Msg.consts.forget_password());
			dialog.setIcon(forgetIcon);
			cancelBtn.setVisible(true);
		}
		signupBtn.setVisible(false);
		sendBtn.setVisible(true);
		loginBtn.setVisible(false);
		gotoLoginBtn.setVisible(true);
		
		gotoSignupBtn.setVisible(false);
		forgetPassLink.setVisible(false);
	}

	public void onClick(ClickEvent event) {
		Object src = event.getSource();
		if(src == gotoLoginBtn){
			showLogin();
			if(dialog != null){
				dialog.center();
			}
		}else if(src == gotoSignupBtn){
			showSignup();
			if(dialog != null){
				dialog.center();
			}
		}else if(src == forgetPassLink){
			showForgetPassword();
			if(dialog != null){
				dialog.center();
			}
		}else if(src == sendBtn){
			forgetForm.submit();
		}else if(src == signupBtn){
			signupForm.submit();
		}else if(src == loginBtn){
			loginForm.submit();
		}else if(src == cancelBtn){
			for(Iterator<UserCreateListener> iter = listeners.iterator();iter.hasNext();){
				iter.next().userCreateCancelled();
			}
		}
	}

	
}
