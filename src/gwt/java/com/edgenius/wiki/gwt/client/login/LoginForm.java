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

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.CaptchaWidget;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.FormPasswordTextBox;
import com.edgenius.wiki.gwt.client.widgets.FormTextBox;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Widget;

/**
 * Use in Login Dialog and Login Page
 * 
 * @author Dapeng.Ni
 */
public class LoginForm extends Composite implements SubmitHandler, SubmitCompleteHandler{
	
	@UiTemplate("offline-login.ui.xml")
	interface OfflineUiBinder extends UiBinder<Widget, LoginForm> {}
	private static OfflineUiBinder offlineBinder = GWT.create(OfflineUiBinder.class);
	@UiTemplate("online-login.ui.xml")
	interface OnlineUiBinder extends UiBinder<Widget, LoginForm> {}
	private static OnlineUiBinder onlineBinder = GWT.create(OnlineUiBinder.class);
	
	@UiField MessageWidget message;
	@UiField FormPanel form;
	@UiField FormPasswordTextBox password;
	@UiField FormTextBox username;
	@UiField CheckBox remeberme;
	@UiField CaptchaWidget captcha;
	@UiField Hidden redir;
	
	private KeyPressHandler cleanMessageListener = new CleanMessageListener();
	private DialogBox dialog;
	private Button sender;
	private boolean offline;
	
	public LoginForm(DialogBox dialog, Button sender, String redirUrl, final boolean offline){
		this.offline = offline;
		this.dialog = dialog;
		this.sender = sender;

		if(offline){
			this.initWidget(offlineBinder.createAndBindUi(this));
		}else{
			this.initWidget(onlineBinder.createAndBindUi(this));
		}
		
		final String containerID = HTMLPanel.createUniqueId();
		DOM.setElementAttribute(form.getElement(), "id", containerID);
		
		form.addSubmitHandler(this);
		form.addSubmitCompleteHandler(this);
		
		form.setAction(GwtClientUtils.getBaseUrl() + "j_spring_security_check");
		form.setMethod(FormPanel.METHOD_POST);
		
		KeyPressHandler enterSubmit = GwtClientUtils.createEnterSubmitListener(form);
    	
		username.addKeyPressHandler(enterSubmit);
		username.addKeyPressHandler(cleanMessageListener);

		password.addKeyPressHandler(enterSubmit);
		password.addKeyPressHandler(cleanMessageListener);
		
		remeberme.addKeyPressHandler(enterSubmit);
		
		//first disable,only this user failed login several times later in given period, captcha will show up
		captcha.disable();
		captcha.getCaptchaInputWidget().addKeyPressHandler(cleanMessageListener);
		captcha.getCaptchaInputWidget().addKeyPressHandler(enterSubmit);
		
		//hardcode for server side parameter name
		redir.setValue(redirUrl);
		focus();

	}
	
	public void submit(){
		//only do form submit, keep consist with "Enter" while typing in input field.
		form.submit();
	}


	public void onSubmit(SubmitEvent event) {
	    //remove some offline_code here(0726)
//		if(offline){
//			
//			String userN = username.getText();
//			if(StringUtil.isBlank(userN)){
//				username.setError(Msg.params.required(Msg.consts.user_name()));
//				event.cancel();
//				return;
//			}
//			String retEvent;
//			if(!OfflineLoginService.login(userN)){
//				//login failed
//				retEvent = SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_HEADER_ERROR_IN_USERPASS;
//			}else{
//				Cookies.setCookie(OfflineConstants.COOKIE_LOGIN, userN);
//				retEvent = "";
//			}
//			//simulate login
//			doLogin(retEvent);
//			//always cancel
//			event.cancel();
//		}else{
			if(StringUtil.isBlank(username.getText())){
				username.setError(Msg.params.required(Msg.consts.user_name()));
				event.cancel();
				return;
			}
			if(StringUtil.isBlank(password.getText())){
				password.setError(Msg.params.required(Msg.consts.password()));
				event.cancel();
				return;
			}
			
			if(sender != null)
				sender.setEnabled(false);
//		}
	}
	
	//see AuthenticationProcessingFilter.sendRedirect();
	public void onSubmitComplete(SubmitCompleteEvent event) {
		if(sender != null)
			sender.setEnabled(true);
		
		//return URL to redirect after login.
		String result = GwtClientUtils.getFormResult(event);
		Log.info("Login result:" + result);
		if(result == null)
			return;
		
		doLogin(result);
	}

	/**
	 * @param result
	 */
	private void doLogin(String result) {
		if(result.startsWith(SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_HEADER_ERROR_IN_USERPASS)){
			//return: MESSAGE:username
			message.cleanMessage();
			if(offline){
				message.error(Msg.consts.user_offline_disable());
			}else{
				message.error(Msg.consts.user_password_error());
			}
			//clear password
			password.setText("");
			//it also need refresh captcha image because it is another request
			if(captcha.isEnabled())
				captcha.refresh();
			int len = (SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_HEADER_ERROR_IN_USERPASS).length();
			String requireCaptcha = result.substring(len,len+1);
			if(requireCaptcha.equals("1")){
				captcha.enable();
			}else{
				captcha.disable();
			}
		}else if(result.startsWith(SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_HEADER_ERROR_CAPTCHA)){
			//need refresh captcha to do verify again 
			message.cleanMessage();
			message.error(Msg.consts.wrong_captcha());
			captcha.refresh();
		}else{
			if(dialog != null)
				dialog.hidebox();
			
			GwtClientUtils.reload(result);
		}
	}

	public void onLoad(){
		username.setFocus(true);
	}

	public void focus() {
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				username.setFocus(true);
		}}); 
	}
	
	public class CleanMessageListener implements KeyPressHandler{

		public void onKeyPress(KeyPressEvent event) {
			message.fadeout();
		}
	}

}
