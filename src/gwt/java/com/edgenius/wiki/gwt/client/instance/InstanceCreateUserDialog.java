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
package com.edgenius.wiki.gwt.client.instance;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.user.PersonalProfile;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class InstanceCreateUserDialog extends DialogBox{
	private AddUserForm form1;
	private Image signupIcon = new Image(IconBundle.I.get().lock_add());
	
	public InstanceCreateUserDialog(){
		Button okBtn = new Button(Msg.consts.add());
		Button cancelBtn = new Button(Msg.consts.cancel());
		
		okBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				form1.submit();
			}
		});
		cancelBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				InstanceCreateUserDialog.this.hidebox();
			}
		});
		getButtonBar().add(okBtn);
		getButtonBar().add(cancelBtn);
		
		this.setText(Msg.consts.add_user());
		this.setIcon(signupIcon);
		form1 = new AddUserForm(okBtn);
		this.setWidget(form1);

	}
	public native void userAdded(String fullname)/*-{
	   $wnd.userCreated(fullname);
	}-*/;

	private class AddUserForm extends Composite implements SubmitHandler, AsyncCallback<UserModel>{
		private MessageWidget message = new MessageWidget();
		private PersonalProfile profile = new PersonalProfile();
		private Button sender;
		private FormPanel form = new FormPanel();
		
		public AddUserForm(Button sender){
			this.sender = sender;
			form.setMethod(FormPanel.METHOD_POST);
			form.addSubmitHandler(this);
			
			FlexTable inputPanel = new FlexTable();
			profile.userInfo(inputPanel, form, PersonalProfile.STYLE_ADD);
			
			VerticalPanel panel = new VerticalPanel();
			panel.add(message);
			panel.add(inputPanel);
			
			form.setWidget(panel);
			this.initWidget(form);
		}
		/**
		 * 
		 */
		public void submit() {
			form.submit();
		}
		public void onSubmit(SubmitEvent event) {
			
			if(profile.isValidSignupForm()){
				if(sender != null)
					sender.setEnabled(false);
				SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
				UserModel model = profile.getUserModel();
				//for user signup: need automatically get user login
				securityController.createUser(model,false, this);
			}
			//always cancel, form submit replaced by RPC call
			event.cancel();
		}


		public void onFailure(Throwable obj) {
			if(sender != null)
				sender.setEnabled(true);
			GwtClientUtils.processError(obj);
		}

		public void onSuccess(UserModel user) {
			if(sender != null)
				sender.setEnabled(true);
			if(!GwtClientUtils.preSuccessCheck(user, message)){
				return;
			}
			InstanceCreateUserDialog.this.hidebox();
			userAdded(user.getFullname());
		}
		
	}
}
