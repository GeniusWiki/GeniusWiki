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
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class ProfileForm extends Composite implements SubmitHandler, AsyncCallback<UserModel>, ClickHandler{
	
	private ContactPanel profile = new ContactPanel(true);
	private MessageWidget message = new MessageWidget();
	private Vector<UserUpdateListener> listener = new Vector<UserUpdateListener>();
	
	//This field is just for bug: http://code.google.com/p/google-web-toolkit/issues/detail?id=1585&sort=-id
	private boolean cancelAction = false;
	private Button signup = new Button(Msg.consts.update(),ButtonIconBundle.tickImage());
	private Button cancel = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
	private FormPanel form = new FormPanel();
	public ProfileForm(){
		
		form.setMethod(FormPanel.METHOD_POST);
		form.addSubmitHandler(this);
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(profile);
		
		signup.addClickHandler(this);
		cancel.addClickHandler(this);
		
		ButtonBar h6 = new ButtonBar();
		h6.add(signup);
		h6.add(cancel);
		panel.add(h6);
		form.setWidget(panel);
		initWidget(form);
	}
	/**
	 * Set initial value on fields on profile panel
	 * @param user
	 */
	public void fillFields(UserModel user) {
		profile.setUser(user);
	}

	//********************************************************************
	//               Listener methods
	//********************************************************************
	public void onSubmit(SubmitEvent event) {

		if(cancelAction){
			event.cancel();
			return;
		}
		if(profile.isValidForm()){
			signup.setEnabled(false);
			SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
			securityController.saveProfile(profile.getUserModel(), this);
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

	public void onSuccess(UserModel model) {
		signup.setEnabled(true);
		
		if(!GwtClientUtils.preSuccessCheck(model,message)){
			return;
		}
		//success
		for(Iterator<UserUpdateListener> iter = listener.iterator();iter.hasNext();){
			iter.next().userUpdated(model);
		}
	}
	public void addListener(UserUpdateListener lis){
		listener.add(lis);
	}
	public void onClick(ClickEvent event) {
		if(event.getSource() == signup){
			cancelAction = false;
			form.submit();
		}else if(event.getSource() == cancel){
			cancelAction = true;
			for(Iterator<UserUpdateListener> iter = listener.iterator();iter.hasNext();){
				iter.next().userUpdateCancelled();
			}	
		}
	}
}
