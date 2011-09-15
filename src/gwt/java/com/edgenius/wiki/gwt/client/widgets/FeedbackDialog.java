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
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.FeedbackModel;
import com.edgenius.wiki.gwt.client.server.HelperControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class FeedbackDialog extends DialogBox implements AsyncCallback<Boolean>, SubmitHandler, FormTextBoxValidCallback{
	
	
	private VerticalPanel panel = new VerticalPanel();
	private MessageWidget message = new MessageWidget();
	private FormTextBox emailBox = new FormTextBox();
	private FormTextArea feedbackBox = new FormTextArea();
	
	public FeedbackDialog(){
		this.setText(Msg.consts.feedback());
		this.setIcon(new Image(IconBundle.I.get().email()));
		
		final FormPanel form = new FormPanel();
		form.setMethod(FormPanel.METHOD_POST);
		form.addSubmitHandler(this);
		form.setStyleName(Css.FORM);
		form.setWidget(panel);
		
		
		HorizontalPanel h1 = new HorizontalPanel();
		Label emailLabel = new Label(Msg.consts.your_email());
		h1.add(emailLabel);
		h1.add(emailBox);

		feedbackBox.valid(Msg.consts.feedback(), true, 0, 0, null);
		emailBox.valid(Msg.consts.email(), true, 0, 0, this);
		Label feedbackLabel = new Label(Msg.consts.your_feedback());
		panel.add(message);
		panel.add(h1);
		panel.add(feedbackLabel);
		panel.add(feedbackBox);

		ButtonBar btnBar = getButtonBar();
		Button send  = new Button(Msg.consts.send(),ButtonIconBundle.tickImage());
		Button cancel = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
		send.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				form.submit();
			}
		});
		cancel.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				FeedbackDialog.this.hidebox();
			}
		});
		btnBar.add(cancel);
		btnBar.add(send);
		
		panel.setSize("100%","100%");
		emailLabel.setStyleName(Css.FORM_LABEL);
		feedbackLabel.setStyleName(Css.FORM_LABEL);
		emailBox.setStyleName(Css.FORM_INPUT);
		feedbackBox.setStyleName(Css.LONG_TEXTBOX);
		
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		panel.setCellHorizontalAlignment(feedbackBox,HasHorizontalAlignment.ALIGN_CENTER);
		
		this.setWidget(form);
		
	}

	public void onFailure(Throwable caught) {
		message.warning(Msg.consts.error_request());
	}

	public void onSuccess(Boolean result) {
		//remove input fields...
		panel.clear();
		getButtonBar().clear();
		
		Button close = new Button(Msg.consts.close());
		close.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				FeedbackDialog.this.hidebox();
			}
		});
		getButtonBar().add(close);
		panel.add(message);
		
		message.info(Msg.consts.feedback_success());
		
		//auto close dialog 
		closeDialog();
	}
	public void closeDialog(){
		//close dialog after several seconds
		new Timer(){
			public void run() {
				FeedbackDialog.this.hidebox();
			}
		}.schedule(5000);
	}

	public void onSubmit(SubmitEvent event) {
		if(emailBox.isValidForSubmit() && feedbackBox.isValidForSubmit()){
			HelperControllerAsync helperController = ControllerFactory.getHelperController();
			FeedbackModel feedback = new FeedbackModel();
			feedback.content = feedbackBox.getText();
			feedback.email = emailBox.getText();
			helperController.sendFeedback(feedback, this);
		}		
		//always cancel
		event.cancel();
	}


	public String onBlurValid(Object source) {
		if(source == emailBox){
			return GwtUtils.validateEmail(emailBox.getText());
		}
		return null;
	}

	public String onKeyUpValid(Object source) {
		return null;
	}

}
