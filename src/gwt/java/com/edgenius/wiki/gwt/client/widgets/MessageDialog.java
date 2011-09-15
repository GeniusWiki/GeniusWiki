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

import java.util.ArrayList;
import java.util.List;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.UIObject;

/**
 * Replacement by javascript alert(), confirm().
 * @author Dapeng.Ni
 */
public class MessageDialog  extends DialogBox{
	public static final int TYPE_ALERT = 0;
	public static final int TYPE_CONFIRM = 1;
	
	private List<MessageDialogListener> listeners;
	public MessageDialog(int type,String caption, String message){
		this(null, type, caption, message);
	}
	public MessageDialog(UIObject parent, int type,String caption, String message){
		super(parent);
		
		if(caption != null)
			this.setText(caption);
		else{
			if(type == TYPE_CONFIRM){
				this.setText(Msg.consts.confirm());
				this.setIcon(new Image(IconBundle.I.get().comment()));
			}else{
				this.setText(Msg.consts.alert());
				this.setIcon(new Image(IconBundle.I.get().warning()));
			}
		}
		
		this.addStyleName(Css.MESSAGE_DIALOG);
		ButtonBar btnBar = getButtonBar();
		FlexTable panel = new FlexTable();
		
		if(type == TYPE_CONFIRM){
			panel.setWidget(0,0,new Image(LargeIconBundle.I.get().question()));
			
			Button okBtn = new Button(Msg.consts.ok(),ButtonIconBundle.tickImage());
			Button cancelBtn = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
			okBtn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					confirmEvent();
					MessageDialog.this.hidebox();
				}
			});
			cancelBtn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					cancelEvent();
					MessageDialog.this.hidebox();
				}

	
				
			});
			btnBar.add(cancelBtn);
			btnBar.add(okBtn);
		}else if(type == TYPE_ALERT){
			panel.setWidget(0,0,new Image(LargeIconBundle.I.get().warning()));
			Button okBtn = new Button(Msg.consts.ok(),ButtonIconBundle.tickImage());
			okBtn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					MessageDialog.this.hidebox();
				}
	
			});
			btnBar.add(okBtn);
		}
		
		
		Label lb = new Label(message);
		lb.setStyleName(Css.MESSAGE);
		panel.setWidget(0,1,lb);
		panel.getCellFormatter().setWidth(0, 1, "99%");
		panel.getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setWidth("100%");
		
		this.setWidget(panel);
	}
	public void addMessageListener(MessageDialogListener lis){
		if(listeners == null)
			listeners = new ArrayList<MessageDialogListener>();
		
		listeners.add(lis);
	}
	private void confirmEvent() {
		if(listeners != null){
			for (MessageDialogListener lis : listeners) {
				lis.confirmed();
			}
		}
	}

	private void cancelEvent() {
		if(listeners != null){
			for (MessageDialogListener lis : listeners) {
				lis.cancelled();
			}
		}
		
	}
	/**
	 * @param confirmMsg
	 * @return
	 */
	public static void confirm(String message, MessageDialogListener callback) {
		final MessageDialog dlg = new MessageDialog(TYPE_CONFIRM, Msg.consts.confirm(), message);
		dlg.addMessageListener(callback);
		dlg.addMessageListener(new MessageDialogListener() {
			public void confirmed() {
				dlg.hidebox();
			}
			public void cancelled() {
				dlg.hidebox();
			}
		});
		dlg.showbox();
	}
}
