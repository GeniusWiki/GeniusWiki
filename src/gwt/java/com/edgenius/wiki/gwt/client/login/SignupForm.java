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

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.CaptchaVerifiedException;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.user.PersonalProfile;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class SignupForm extends SimplePanel implements SubmitHandler, AsyncCallback<UserModel>{
	private MessageWidget message = new MessageWidget();
	private PersonalProfile profile = new PersonalProfile();
	private Vector<UserCreateListener> listener = new Vector<UserCreateListener>();
	
	private FormPanel form = new FormPanel();
	private DialogBox dialog;
	private String redirUrl;
	private Button sender;
	/**
	 * Used in user signup and system admin adds new user
	 * @param dialog 
	 * @param sender 
	 * @param redirUrl 
	 */
	public SignupForm(DialogBox dialog, Button sender, String redirUrl){
		this.dialog = dialog;
		this.sender = sender;
		this.redirUrl = redirUrl;
		form.setMethod(FormPanel.METHOD_POST);
		form.addSubmitHandler(this);
		
		FlexTable inputPanel = new FlexTable();
		profile.userInfo(inputPanel,form, PersonalProfile.STYLE_SIGNUP);
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(inputPanel);
		
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		panel.setSize("450px","100%");
		
		form.setWidget(panel);
		
		this.setWidget(form);
	}

	
	public void onSubmit(SubmitEvent event) {
		
		if(profile.isValidSignupForm()){
			if(sender != null)
				sender.setEnabled(false);
			SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
			UserModel model = profile.getUserModel();
			model.setRedirUrl(redirUrl);
			//for user signup: need automatically get user login
			securityController.createUser(model,true, this);
		}
		//always cancel, form submit replaced by RPC call
		event.cancel();
	}
	public void addListener(UserCreateListener lis){
		listener.add(lis);
	}

	public void onFailure(Throwable error) {
		if(sender != null)
			sender.setEnabled(true);
		if(error instanceof CaptchaVerifiedException){
			message.error(Msg.consts.wrong_captcha());
			profile.refreshCaptcha();
		}else{
			GwtClientUtils.processError(error);
		}
	}

	public void onSuccess(UserModel model) {
		if(sender != null)
			sender.setEnabled(true);

		//don't user presuccessCheck: need not check session expire
//		if(!GwtClientUtils.preSuccessCheck(model,message)){
		if(ErrorCode.hasError(model)){
			if(message != null){
				message.error(ErrorCode.getMessage(model.errorCode, model.errorMsg));
			}
			return;
		}
		
		if(dialog != null){
			dialog.hidebox();
		}
		
		for(Iterator<UserCreateListener> iter = listener.iterator();iter.hasNext();){
			iter.next().userCreated(model);
		}
		
	}


	/**
	 * clean all input field to  empty value: ready for next new input
	 */
	public void clean() {
		profile.setEmail("");
		profile.setFullname("");
		profile.setPassword("");
		profile.setConfirmPassword("");
		profile.setLoginName("");
	}


	/**
	 * set cursor focus
	 */
	public void focus() {
		profile.forcsToLoginName();
		
	}
	public void submit() {
		//only do form submit, keep consist with "Enter" while typing in input field.
		form.submit();
	}

}
