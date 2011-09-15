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

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class ForgetPasswordForm extends SimplePanel implements SubmitHandler, AsyncCallback<Integer>{
	private MessageWidget message = new MessageWidget();
	private FormPanel form = new FormPanel();
	private TextBox email = new TextBox();
	private KeyPressHandler cleanMessageListener = new CleanMessageListener();
	private Button sender;
	
	public ForgetPasswordForm(Button sender){
		this.sender = sender;
		
		VerticalPanel main = new VerticalPanel();
		main.add(message);
		
		FlowPanel layout = new FlowPanel();
		Label emailL = new Label(Msg.consts.email());
		layout.add(emailL);
		layout.add(email);
		
		main.add(layout);
		
		
		//first disable,only this user failed login several times later in given period, captcha will show up
		email.addKeyPressHandler(cleanMessageListener);
		
		form.add(main);
		form.addSubmitHandler(this);
		
		DOM.setStyleAttribute(emailL.getElement(), "paddingLeft", "120px");
		DOM.setStyleAttribute(emailL.getElement(), "paddingRight", "20px");
		DOM.setStyleAttribute(layout.getElement(), "marign", "10px");
		form.setStyleName(Css.FORM);
		email.setStyleName(Css.FORM_INPUT);
		emailL.setStyleName(Css.FORM_LABEL);
		main.setWidth("100%");
		this.setWidget(form);
	}


	public void onSubmit(SubmitEvent event) {
		String text = email.getText();
		String es;
		boolean errors = false;
		if((es=GwtUtils.validateEmail(text)) !=null){
			errors = true;
			message.error(es);
		}
		
		if(!errors){
			if(sender != null)
				sender.setEnabled(false);
			SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
			securityController.sendForgetPassword(text,this);
		}
		
		//always cancel
		event.cancel();
	}


	public void submit() {
		form.submit();
	}


	public void onFailure(Throwable error) {
		if(sender != null)
			sender.setEnabled(true);
		
		GwtClientUtils.processError(error);
	}

	public void onSuccess(Integer  result) {
		if(sender != null)
			sender.setEnabled(true);
		if(result == null)
			return;
		
		if( SharedConstants.RET_NO_EMAIL.equals(result)){
			message.cleanMessage();
			message.error(Msg.consts.email_no_exist());
		}else if( SharedConstants.RET_SEND_MAIL_FAILED.equals(result)){
			message.cleanMessage();
			message.error(Msg.consts.email_send_failed());
		}else{
			message.cleanMessage();
			message.info(Msg.consts.password_reset_success());
		
		}
		
	}
	
	//********************************************************************
	//               private class
	//********************************************************************
	public class CleanMessageListener implements KeyPressHandler{

		public void onKeyPress(KeyPressEvent event) {
			message.fadeout();
		}
	}

}
