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

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonBar;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class PortraitForm extends SimplePanel implements SubmitHandler, SubmitCompleteHandler, ClickHandler{
	private Hidden userUid = new Hidden("userUid");
	private Vector<UserUpdateListener> listener = new Vector<UserUpdateListener>();
	private MessageWidget message = new MessageWidget();
	private Button update = new Button(Msg.consts.update(),ButtonIconBundle.tickImage());
	private Button cancel = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
	private FormPanel form = new FormPanel();
	
	public PortraitForm(){
		form.setAction(GwtClientUtils.getBaseUrl() + "user/portrait.do");
		form.setMethod(FormPanel.METHOD_POST);
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.addSubmitHandler(this);
		form.addSubmitCompleteHandler(this);
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(userUid);
		Label uploadLabel = new Label(Msg.consts.portrait());
		uploadLabel.setStyleName(Css.FORM_LABEL);
		FileUpload upload = new FileUpload();
		upload.setName("file");
		panel.add(uploadLabel);
		panel.add(upload);
		form.setWidget(panel);
		
		
		update.addClickHandler(this);
		
		cancel.addClickHandler(this);
		

		ButtonBar btnBar = new ButtonBar();
		btnBar.add(update);
		btnBar.add(cancel);
		
		VerticalPanel main = new VerticalPanel();
		main.add(form);
		main.add(btnBar);
		setWidget(main);
	}


	public void fillFields(UserModel model){
		userUid.setValue(model.getUid()+"");
	}

	public void addListener(UserUpdateListener lis){
		listener.add(lis);
	}

	public void onSubmit(SubmitEvent event) {
		boolean errors = false;
		message.fadeout();
		//if there are errors, cancel login
		if(errors){
			event.cancel();
		}else{
			update.setEnabled(false);
		}
	}

	public void onSubmitComplete(SubmitCompleteEvent event) {
		update.setEnabled(true);
		String rs = GwtClientUtils.getFormResult(event);
		if(rs == null)
			return;
		if(!rs.startsWith(SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_ERROR)){
			//show real picture
			//success
			for(Iterator<UserUpdateListener> iter = listener.iterator();iter.hasNext();){
				iter.next().userPortraitUpdated(rs);
			}
		}else{
			//error on upload, keep original image and show error message.
			message.error(Msg.consts.fail_upload());
		}
	}


	public void onClick(ClickEvent event) {
		if(event.getSource() == update){
			form.submit();
		}else if(event.getSource() == cancel){
			//return to link panel
			for(Iterator<UserUpdateListener> iter = listener.iterator();iter.hasNext();){
				iter.next().userUpdateCancelled();
			}	
		}
		
	}
}
