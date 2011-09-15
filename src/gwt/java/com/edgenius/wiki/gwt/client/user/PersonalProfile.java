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

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.CaptchaWidget;
import com.edgenius.wiki.gwt.client.widgets.FormPasswordTextBox;
import com.edgenius.wiki.gwt.client.widgets.FormTextBox;
import com.edgenius.wiki.gwt.client.widgets.FormTextBoxValidCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Dapeng.Ni
 */
public class PersonalProfile implements FormTextBoxValidCallback {
	//system admin add user
	public static final int STYLE_ADD = 1;
	public static final int STYLE_SIGNUP = 1<<1;
	public static final int STYLE_PASSWORD = 1<<3;
	
	public static final int LOGIN_FULLNAME_LEN = 60;
	public static final int LOGIN_EMAIL_LEN = 150;
	private static final int LOGIN_NAME_MAX_LEN = 25;
	private static final int LOGIN_PASSWORD_LEN = 30;
	
	private FormTextBox loginName = new FormTextBox();
	private FormPasswordTextBox password = new FormPasswordTextBox();
	private FormPasswordTextBox repassword = new FormPasswordTextBox();
	private FormTextBox email = new FormTextBox();
	private FormTextBox fullname = new FormTextBox();
	private CaptchaWidget captcha = new CaptchaWidget();

	/**
	 * 
	 * @param panel
	 * @param form 
	 * @param require, 0: signup (all fields), 1:profile(except password and loginname), 2: password only
	 */
	public void userInfo(FlexTable panel, FormPanel form, int style) {
		KeyPressHandler enterSubmit = GwtClientUtils.createEnterSubmitListener(form);
		
		int row = 0;
		panel.setStyleName(Css.SIGNUP_TABLE);
		//signup
		if((style & STYLE_SIGNUP) > 0 || (style & STYLE_ADD) > 0){
			Label loginNameLabel = new Label(Msg.consts.login_name());
			loginNameLabel.setStyleName(Css.FORM_LABEL);
			loginName.setName("user.username");
			loginName.valid(Msg.consts.login_name(), true, 4, LOGIN_NAME_MAX_LEN, this);
			loginName.addKeyPressHandler(enterSubmit);
			loginName.setStyleName(Css.FORM_INPUT);
			
			panel.setWidget(row, 0, loginNameLabel);
			panel.setWidget(row, 1, loginName);
			row++;
		}		
		
		//signup, password
		if((style & STYLE_SIGNUP) > 0 || (style & STYLE_ADD) > 0|| (style & STYLE_PASSWORD) > 0){
			Label passwordLabel = new Label(Msg.consts.password());
			passwordLabel.setStyleName(Css.FORM_LABEL);
			password.setName("user.password");
			password.addKeyPressHandler(enterSubmit);
			password.addKeyPressHandler(new KeyPressHandler(){
				public void onKeyPress(KeyPressEvent event) {
					String text = password.getText();
					if(text.length() > LOGIN_PASSWORD_LEN){
						text = text.substring(0, LOGIN_PASSWORD_LEN);
						password.setText(text);
					}
				}
			});
			password.valid(Msg.consts.password(), true, 6, LOGIN_PASSWORD_LEN, null);
			password.setStyleName(Css.FORM_INPUT);
			
			Label repasswordLabel = new Label(Msg.consts.confirm_password());
			repassword.setName("user.confirmPassword");
			repassword.addKeyPressHandler(enterSubmit);
			repassword.valid(Msg.consts.password(), false, 0, LOGIN_PASSWORD_LEN, this);
			repassword.setStyleName(Css.FORM_INPUT);
			repasswordLabel.setStyleName(Css.FORM_LABEL);

			panel.setWidget(row, 0, passwordLabel);
			panel.setWidget(row, 1, password);
			row++;
			
			panel.setWidget(row, 0, repasswordLabel);
			panel.setWidget(row, 1, repassword);
			row++;

			if((style & STYLE_PASSWORD) > 0){
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					public void execute() {
						password.setFocus(true);
				}}); 
			}
		}
		//signup, profile
		if((style & STYLE_SIGNUP) > 0 || (style & STYLE_ADD) > 0){
			Label fullnameLabel = new Label(Msg.consts.full_name());
			fullnameLabel.setStyleName(Css.FORM_LABEL);
			fullname.setName("user.fullname");
			fullname.addKeyPressHandler(enterSubmit);
			fullname.valid(Msg.consts.full_name(), true, 0, LOGIN_FULLNAME_LEN, null);
			fullname.setStyleName(Css.FORM_INPUT);

			panel.setWidget(row, 0, fullnameLabel);
			panel.setWidget(row, 1, fullname);
			row++;

		
			Label emailLabel = new Label(Msg.consts.email());
			email.setName("user.contact.email");
			email.addKeyPressHandler(enterSubmit);
			email.valid(Msg.consts.email(), true, 0, LOGIN_EMAIL_LEN, this);
			email.setStyleName(Css.FORM_INPUT);
			emailLabel.setStyleName(Css.FORM_LABEL);
			panel.setWidget(row, 0, emailLabel);
			panel.setWidget(row, 1, email);
			row++;
		}
		//signup
		if((style & STYLE_SIGNUP) > 0){
			captcha.enable();
			captcha.getCaptchaInputWidget().addKeyPressHandler(enterSubmit);
			panel.getFlexCellFormatter().setColSpan(row, 0, 2);
			panel.setWidget(row, 0, captcha);
			row++;
			
		}
		
		panel.setWidth("100%");
	}

	public UserModel getUserModel(){
		UserModel model = new UserModel();
		model.setLoginname(loginName.getText());
		model.setFullname(fullname.getText());
		model.setEmail(email.getText());
		model.setPassword(password.getText());
		if(captcha.isEnabled()){
			model.reqireCaptcha = true;
			model.captchaCode = captcha.getCaptchaInput();
		}
		return model;
	}
	//********************************************************************
	//               Validate methods
	//********************************************************************
	public void setFullname(String fullnameText){
		fullname.setText(fullnameText);
	}
	public void setEmail(String emailText){
		email.setText(emailText);
	}
	public void setPassword(String text){
		password.setText(text);
	}
	public void setConfirmPassword(String text){
		repassword.setText(text);
	}
	public void setLoginName(String text){
		loginName.setText(text);
	}
	public String getEmail() {
		return email.getText();
	}

	public String getFullname() {
		return fullname.getText();
	}
	public String getPassword() {
		return password.getText();
	}
	public void forcsToLoginName() {
		loginName.setFocus(true);
	}
	public String onBlurValid(Object source) {
		if(source == repassword){
			if(!repassword.getText().equals(password.getText())){
				return Msg.consts.not_matched();
			}
		}else if(source == email){
			return GwtUtils.validateEmail(email.getText());
		}else if(source ==loginName){
			return validLogin();
		}
		return null;
	}

	public String onKeyUpValid(Object source) {
		String es = null;
		if(source ==loginName){
			es = validLogin();
		}
		return es;
	}

	private String validLogin() {
		String es = null;
		
		String value = StringUtil.trimToEmpty(loginName.getText());
		if(value.length() > 0 && (value.matches("[\\-_.]+") || StringUtil.startOfAny(value, new String[]{"-","_","."})
					|| StringUtil.endOfAny(value, new String[]{"-","_","."}))){
			es = Msg.consts.login_name_invalid_start();
		}
		if(es == null && value.length() > 0){
			es = GwtUtils.validateMatch(value,"[a-zA-Z0-9\\-_.]" ,Msg.consts.login_name());
		}
		
		return es;
	}

	/**
	 * 
	 */
	public void refreshCaptcha() {
		if(captcha.isEnabled())
			captcha.refresh();
		
	}

	/**
	 * @return
	 */
	public boolean isValidSignupForm() {
		boolean valid;
		//CANNOT use && join them as it will stop when method return false.
		valid = email.isValidForSubmit();
		valid = fullname.isValidForSubmit() && valid;
		valid = loginName.isValidForSubmit()&& valid;
		valid = password.isValidForSubmit() && valid;
		valid = repassword.isValidForSubmit() && valid;
		
		if(!StringUtil.equals(password.getText(), repassword.getText())){
			repassword.setError(Msg.consts.not_matched());
			valid = false;
		}
		
		String msg = GwtUtils.validateEmail(email.getText());
		if(msg != null){
			email.setError(msg);
			valid = false;
		}
		
		msg = GwtUtils.validateMatch(loginName.getText(),"[a-zA-Z0-9\\-_.]" ,Msg.consts.login_name());
		if(msg != null){
			loginName.setError(msg);
			valid = false;
		}
		
		return valid;
	}

	/**
	 * @return
	 */
	public boolean isValidPasswordForm() {
		//CANNOT use && join them as it will stop when method return false.
		boolean valid  = password.isValidForSubmit();
		return repassword.isValidForSubmit() && valid;
	}

	/**
	 * @return
	 */
	public boolean isValidProfileForm() {
		//CANNOT use && join them as it will stop when method return false.
		boolean valid  = email.isValidForSubmit();
		return fullname.isValidForSubmit() && valid;
	}

}
