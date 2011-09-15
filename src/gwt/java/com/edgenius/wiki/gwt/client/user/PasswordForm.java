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

import java.util.Iterator;
import java.util.Vector;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonBar;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class PasswordForm extends Composite implements SubmitHandler, AsyncCallback<String>, ClickHandler{
	
	private PersonalProfile profile = new PersonalProfile();
	private Vector<UserUpdateListener> listener = new Vector<UserUpdateListener>();
	private Hidden userUid = new Hidden("userUid");
	private MessageWidget message = new MessageWidget();
	private FormPanel form = new FormPanel();
	private Button signup = new Button(Msg.consts.update(),ButtonIconBundle.tickImage());
	private Button cancel = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
	public PasswordForm(){
//		form.setAction(GwtUtils.getBaseUrl() + "user/password.do");
		form.setMethod(FormPanel.METHOD_POST);
		form.addSubmitHandler(this);
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(userUid);
		panel.add(message);
		FlexTable inputPanel = new FlexTable();
		profile.userInfo(inputPanel,form, PersonalProfile.STYLE_PASSWORD);
		panel.add(inputPanel);
		
		signup.addClickHandler(this);
		cancel.addClickHandler(this);
		
		ButtonBar h6 = new ButtonBar();
		h6.add(signup);
		h6.add(cancel);

		panel.add(h6);
		form.setWidget(panel);
		initWidget(form);
	}

	public void onSubmit(SubmitEvent event) {
		if(profile.isValidPasswordForm()){
			signup.setEnabled(false);
			SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
			securityController.savePassword(new Integer(userUid.getValue()), profile.getPassword(), this);
		}
		//always cancel, submit will do by RPC call
		event.cancel();
		
	}

	public void onFailure(Throwable error) {
		signup.setEnabled(true);
		GwtClientUtils.processError(error);
		message.cleanMessage();
		message.error(Msg.consts.fail_update());
	}


	public void onSuccess(String errorCode) {
		signup.setEnabled(true);
		if(errorCode == null){
			//success
			for(Iterator<UserUpdateListener> iter = listener.iterator();iter.hasNext();){
				iter.next().userPasswordUpdated();
			}
		}else{
			//how to show error msg from code?message.
			message.cleanMessage();
			message.error(errorCode);
		}
	}
	
	public void fillFields(UserModel model){
		userUid.setValue(model.getUid()+"");
	}
	public void addListener(UserUpdateListener lis){
		listener.add(lis);
	}

	public void onClick(ClickEvent event) {
		if(event.getSource() == signup){
			form.submit();
		}else if(event.getSource() == cancel){
			for(Iterator<UserUpdateListener> iter = listener.iterator();iter.hasNext();){
				iter.next().userUpdateCancelled();
			}
		}
		
	}
}
