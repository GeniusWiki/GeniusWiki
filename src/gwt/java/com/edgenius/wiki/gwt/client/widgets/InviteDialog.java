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
package com.edgenius.wiki.gwt.client.widgets;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.InvitationModel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.server.HelperControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class InviteDialog extends DialogBox implements AsyncCallback<InvitationModel>, SubmitHandler{

	private VerticalPanel panel = new VerticalPanel();
	private MessageWidget message = new MessageWidget();
	private TextBox emailBox = new TextBox();
	private TextArea contentBox = new TextArea();
	
	public InviteDialog(){
		this.setText(Msg.consts.invite_friends());
		this.setIcon(new Image(IconBundle.I.get().email()));
		final FormPanel form = new FormPanel();
		form.setMethod(FormPanel.METHOD_POST);
		form.addSubmitHandler(this);
		form.setStyleName(Css.FORM);
		form.setWidget(panel);
		
		
		panel.add(message);
		panel.add(new Label(Msg.consts.friends_email()));
		panel.add(emailBox);
		panel.add(new Label(Msg.consts.msg_to_friends()));
		panel.add(contentBox);

		ButtonBar btnBar = getButtonBar();
		Button send  = new Button(Msg.consts.send()); 
		Button cancel = new Button(Msg.consts.cancel());
		send.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				form.submit();
			}
		});
		cancel.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				InviteDialog.this.hidebox();
			}
		});
		btnBar.add(cancel);
		btnBar.add(send);
		
		panel.setSize("100%","100%");
		emailBox.setStyleName(Css.LONG_INPUT);
		contentBox.setStyleName(Css.LONG_TEXTBOX);
		
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		panel.setCellHorizontalAlignment(emailBox,HasHorizontalAlignment.ALIGN_CENTER);
		panel.setCellHorizontalAlignment(contentBox,HasHorizontalAlignment.ALIGN_CENTER);
		
		this.setWidget(form);
		
	}

	public void onFailure(Throwable caught) {
		message.warning(Msg.consts.invite_failed());
	}

	public void onSuccess(InvitationModel model) {
		if(!GwtClientUtils.preSuccessCheck(model,null)){
			return;
		}
		
		//remove input fields...
		panel.clear();
		getButtonBar().clear();
		
		Button close = new Button(Msg.consts.close());
		close.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				InviteDialog.this.hidebox();
			}
		});
		getButtonBar().add(close);
		panel.add(message);
		
		//after server side handle only valid email return and only separator by ","
		String[] emails = model.emailGroup==null?null:model.emailGroup.split(",");
		StringBuffer list = new StringBuffer("<br>"); 
		if(emails != null && emails.length > 0){
			message.info(Msg.consts.invite_success());
			for (int idx=0;idx<emails.length;idx++) {
				String email = emails[idx];
				if(email !=null && email.trim().length() > 0)
					list.append(email).append("<br><br>");
			}
			panel.add(new HTML(list.toString()));
		}else{
			message.info(Msg.consts.invite_email_failed());
		}
		
	}

	public void onSubmit(SubmitEvent event) {
		HelperControllerAsync helperController = ControllerFactory.getHelperController();
		InvitationModel invitation = new InvitationModel();
		invitation.message = contentBox.getText();
		invitation.emailGroup = emailBox.getText();
		invitation.spaceUname = ((PageMain)PageMain.I).getSpaceUname();
		helperController.sendInvitation(invitation, this);

		//always cancel
		event.cancel();
	}

	public boolean validateEmail(){
		boolean errors = false;
		String es;
//		emailError.setText("");
		String text = emailBox.getText();
		if((es=GwtUtils.validateEmail(text)) !=null){
			errors = true;
//			emailError.setText(es);
		}

		return !errors;
	}

}
