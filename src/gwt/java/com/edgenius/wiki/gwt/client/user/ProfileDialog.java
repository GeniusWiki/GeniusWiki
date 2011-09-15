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

import com.edgenius.wiki.gwt.client.BaseEntryPoint;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.DialogListener;
import com.edgenius.wiki.gwt.client.widgets.HintTextArea;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
/**
 * @author Dapeng.Ni
 */
public class ProfileDialog extends DialogBox implements AsyncCallback<UserModel>, UserUpdateListener, ClickHandler, DialogListener, ChangeHandler {
	
	interface PanelUiBinder extends UiBinder<Widget, ProfileDialog> {}
	private static PanelUiBinder uiBinder = GWT.create(PanelUiBinder.class);
	
	@UiField DeckPanel deck;
	@UiField Label fullname;
	@UiField SimplePanel portrait;
	
	@UiField DeckPanel editDeck;
	@UiField ProfileForm form1;
	@UiField PasswordForm form2;
	@UiField PortraitForm form3;
	
	@UiField ClickLink profileLink;
	@UiField ClickLink passwordLink;
	@UiField ClickLink portraitLink;
	@UiField HintTextArea statusText;
	@UiField ContactPanel contacts;
	
	private boolean statusDirty = false;
	private Integer userUid;
	
	public ProfileDialog() {
		
		this.setWidget(uiBinder.createAndBindUi(this));
		this.addDialogListener(this);
		addStyleName(Css.PROFILE_DIALOG_BOX);
		
		profileLink.setText(Msg.consts.edit_profile());
		passwordLink.setText(Msg.consts.edit_password());
		portraitLink.setText(Msg.consts.edit_portrait());
		statusText.setHint(Msg.consts.user_status_hint());

		profileLink.addClickHandler(this);
		passwordLink.addClickHandler(this);
		portraitLink.addClickHandler(this);
		statusText.addChangeHandler(this);
		form1.addListener(this);
		form2.addListener(this);
		form3.addListener(this);
		
		editDeck.insert(form1, 1);
		editDeck.insert(form2, 2);
		editDeck.insert(form3, 3);
		
		deck.showWidget(0);
		
		resetTitle("");
		this.setIcon(ButtonIconBundle.userImage());
		
		//refresh page
		SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
		//give -1 to get current login user
		securityController.getUser(-1, this);

	}


	public void onFailure(Throwable error) {
		deck.showWidget(1);
		GwtClientUtils.processError(error);
	}

	public void onSuccess(UserModel user) {
		deck.showWidget(1);
		
		if(!GwtClientUtils.preSuccessCheck(user,null)){
			return;
		}
		
		//user exist - then fill all user information form and display information.
		if(user.getUid() != null && user.getUid() > 0){
			fillPanel(user);
			form1.fillFields(user);
			form2.fillFields(user);
			form3.fillFields(user);
			editDeck.showWidget(0);
		}
		//image loading and center
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				ProfileDialog.this.center();
			}
		});
		//TODO: user not exist, error message display
	}
	public void onClick(ClickEvent event) {
		Object sender = event.getSource();
		if(sender == profileLink){
			editDeck.showWidget(1);
		}else if(sender == passwordLink){
			editDeck.showWidget(2);
		}else if(sender == portraitLink){
			editDeck.showWidget(3);
		}else {
			editDeck.showWidget(0);
		}
	}

	public void userUpdateCancelled() {
		//return to link panel
		editDeck.showWidget(0);

	}

	public void userUpdated(UserModel model) {
		
		//reset profileForm value
		form1.fillFields(model);		
		fillPanel(model);
		editDeck.showWidget(0);
	}
	public void userPasswordUpdated() {
		editDeck.showWidget(0);
		
	}

	public void userPortraitUpdated(String portraitUuid) {
		portrait.clear();
		portrait.setWidget(GwtClientUtils.createUserPortrait(portraitUuid));

		editDeck.showWidget(0);
	}
	//********************************************************************
	//               
	//********************************************************************

	private void resetTitle(String name) {
		if(name == null || name.trim().length() == 0)
			this.setText(Msg.consts.user_profile());
		else
			this.setText(Msg.consts.user_profile() + " - " + name);
		
	}

	private void fillPanel(UserModel model) {
		if(model == null)
			return; 
		
		resetTitle(model.getLoginname());
		//if it's login user, refresh navbar user name
		if(model.isLogin())
			BaseEntryPoint.I.resetLoginUser(model.getFullname());
		
//		set display info on left part
		fullname.setText(model.getFullname());

		contacts.setUser(model);
		statusText.setText(StringUtil.trimToEmpty(model.getStatus()));
		this.userUid = model.getUid();
		
		if(model.getPortrait() != null){
			//while user update Profile, the portrait won't refresh. See userPortraitUpdated() method
			portrait.clear();
			portrait.setWidget(GwtClientUtils.createUserPortrait(model.getPortrait()));
		}
	}


	public void dialogRelocated(DialogBox dialog) {}
	public void dialogClosed(DialogBox dialog) {}
	public void dialogOpened(DialogBox dialog) {}
	public boolean dialogOpening(DialogBox dialog) {return true;}
	public boolean dialogClosing(DialogBox dialog) {
		//save status if any change
		statusText.setFocus(false);
		if(statusDirty && userUid != null){
			SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
			//give -1 to get current login user
			securityController.saveUserStatus(userUid, statusText.getText(), new AsyncCallback<Boolean>(){
				public void onFailure(Throwable caught) {
					
				}
				public void onSuccess(Boolean result) {
					
				}
			});
		}
		return true;
	}

	public void onChange(ChangeEvent event) {
		statusDirty = true;
		String str = statusText.getText();
		if(str.length() > 140){
			//truncate if too long
			statusText.setText(str.substring(0,140) + "...");
		}
	}


}
