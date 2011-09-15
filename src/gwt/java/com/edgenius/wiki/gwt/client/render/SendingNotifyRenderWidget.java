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
package com.edgenius.wiki.gwt.client.render;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.KeyCaptureListener;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.MessageListModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.NotificationControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonBar;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Send a message to system admin(INSTANCE ADMIN)
 * @author Dapeng.Ni
 */
public class SendingNotifyRenderWidget extends SimplePanel implements AsyncCallback<MessageListModel>, RenderWidget{
	
	private TextArea text = new TextArea();
	private Button send  = new Button(Msg.consts.send(),ButtonIconBundle.tickImage());
	private MessageWidget message = new MessageWidget();
	private boolean dirty = true;
	
	public SendingNotifyRenderWidget(){
		VerticalPanel panel = new VerticalPanel();
		ButtonBar bar = new ButtonBar();
		
		panel.add(message);
		panel.add(text);
		panel.add(bar);
		
		text.addChangeHandler(new ChangeHandler(){

			public void onChange(ChangeEvent event) {
				dirty = true;
			}
		});
		send.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				if(StringUtil.isBlank(text.getText())){
					message.warning(Msg.consts.non_empty());
				}else{
					if(!dirty){
						message.warning(Msg.consts.send_same_msg());
					}else{
						send.setEnabled(false);
						sendMessage();
					}
				}
			}
		});
		bar.add(send);
		text.addFocusHandler(KeyCaptureListener.instance());
		text.addBlurHandler(KeyCaptureListener.instance());
		text.setStyleName(Css.LONG_TEXTBOX);
		
		this.setWidget(panel);
		
	}
	private void sendMessage() {
		NotificationControllerAsync notifyController = ControllerFactory.getNotificationController();
		//?send notify to null, then message to system administrators, is it good?
		notifyController.sendMessage(SharedConstants.MSG_TARGET_INSTANCE_ADMIN_ONLY, null, text.getText(),false, this);
		
	}
	public void onFailure(Throwable caught) {
		send.setEnabled(true);
		GwtClientUtils.processError(caught);
		
	}
	public void onSuccess(MessageListModel model) {
		send.setEnabled(true);
		if(!GwtClientUtils.preSuccessCheck(model,message)){
			return;
		}
		
		dirty = false;
		message.info(Msg.consts.send_notify_success());
	}
	
	public void onLoad(String widgetKey, UserModel user, RenderWidgetListener listener){
		//This widget has not a ajax loading while render 
		//this.listener = listener;
		//at moment - it is not necessary to pass meaningful text - just tell listener, it is success with non-empty content 
		listener.onSuccessLoad(widgetKey, "SEND NOTIFICATION"); //NON-I18N

	}	
	public void onUserChanged(UserModel user) {
	}

}
