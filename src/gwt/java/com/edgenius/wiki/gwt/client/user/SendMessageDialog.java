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

import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.MessageListModel;
import com.edgenius.wiki.gwt.client.server.NotificationControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.AutoResizeTextArea;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonBar;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class SendMessageDialog extends DialogBox implements SubmitHandler, AsyncCallback<MessageListModel>, KeyUpHandler {
	private VerticalPanel panel = new VerticalPanel();
	private MessageWidget message = new MessageWidget();
	private AutoResizeTextArea contentBox = new AutoResizeTextArea();
	private String username;
	private Label limit = new Label(String.valueOf(ClientConstants.LIMIT_CHAR));
	
	public SendMessageDialog(String username){
		this.username = username;
		this.setText(Msg.consts.send_message());
		this.setIcon(new Image(IconBundle.I.get().message()));
		final FormPanel form = new FormPanel();
		form.setMethod(FormPanel.METHOD_POST);
		form.addSubmitHandler(this);
		form.setStyleName(Css.FORM);
		form.setWidget(panel);
		
		
		Label header = new Label(Msg.params.send_message_to(username));
		panel.add(header);
		header.setStyleName(Css.HEADING2);
		panel.setCellHorizontalAlignment(header, HasHorizontalAlignment.ALIGN_CENTER);
		
		panel.add(message);
		panel.add(limit);
		limit.setStyleName(Css.TWEET_COUNTER);
		panel.setCellHorizontalAlignment(limit, HasHorizontalAlignment.ALIGN_RIGHT);
		
		panel.add(contentBox);
		contentBox.addKeyUpHandler(this);
		
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
				SendMessageDialog.this.hidebox();
			}
		});
		btnBar.add(cancel);
		btnBar.add(send);
		
		panel.setSize("100%","100%");
		contentBox.setStyleName(Css.TWEET_BOX);
		
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		panel.setCellHorizontalAlignment(contentBox,HasHorizontalAlignment.ALIGN_CENTER);
		
		this.setWidget(form);
	}

	public void onSubmit(SubmitEvent evt) {
		String text = contentBox.getText();
		if(!StringUtil.isBlank(text)){
			NotificationControllerAsync notifyController = ControllerFactory.getNotificationController();
			notifyController.sendMessage(SharedConstants.MSG_TARGET_USER,username , text, false, this);
		}else{
			message.error(Msg.params.can_be_empty(Msg.consts.message()));
		}
		
		//always cancel
		evt.cancel();
	}

	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
	}

	public void onSuccess(MessageListModel model) {
		if(!GwtClientUtils.preSuccessCheck(model,message)){
			return;
		}
		this.hidebox();
	}

	
	public void onKeyUp(KeyUpEvent event) {
		if(contentBox.getText().length() > ClientConstants.LIMIT_CHAR){
			contentBox.setText(contentBox.getText().substring(0,ClientConstants.LIMIT_CHAR));
		}
		limit.setText(String.valueOf(ClientConstants.LIMIT_CHAR - contentBox.getText().length()));		
	}
		
	
}
