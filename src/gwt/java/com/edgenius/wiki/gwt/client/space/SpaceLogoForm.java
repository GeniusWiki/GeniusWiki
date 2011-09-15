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
package com.edgenius.wiki.gwt.client.space;

import java.util.Iterator;
import java.util.Vector;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonBar;
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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class SpaceLogoForm  extends SimplePanel  implements SubmitHandler, SubmitCompleteHandler{
	private Hidden spaceUname = new Hidden("spaceUname");
	private Vector<SpaceUpdateListener> listener = new Vector<SpaceUpdateListener>();
	private MessageWidget message = new MessageWidget();
	
	public SpaceLogoForm(){
		final FormPanel form = new FormPanel();
		form.setAction(GwtClientUtils.getBaseUrl() + "space/logo.do");
		form.setMethod(FormPanel.METHOD_POST);
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.addSubmitHandler(this);
		form.addSubmitCompleteHandler(this);
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(spaceUname);
		HorizontalPanel h1 = new HorizontalPanel();
		Label uploadLabel = new Label(Msg.consts.logo());
		uploadLabel.setStyleName(Css.FORM_LABEL);
		FileUpload upload = new FileUpload();
		upload.setName("file");
		h1.add(uploadLabel);
		h1.add(upload);
		panel.add(h1);
		
		form.setWidget(panel);
		
		//
		Button update = new Button(Msg.consts.update());
		update.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				form.submit();
			}
		});
		Button cancel = new Button(Msg.consts.cancel());
		cancel.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				//return to link panel
				for(Iterator<SpaceUpdateListener> iter = listener.iterator();iter.hasNext();){
					iter.next().spaceUpdateCancelled();
				}
			}
		});
		
		ButtonBar btnBar = new ButtonBar();
		btnBar.add(update);
		btnBar.add(cancel);

		VerticalPanel main = new VerticalPanel();
		main.add(form);
		main.add(btnBar);
		setWidget(main);
	}

	public void onSubmit(SubmitEvent event) {
		boolean errors = false;
		message.fadeout();
		//if there are errors, cancel login
		if(errors)
			event.cancel();
	}

	public void onSubmitComplete(SubmitCompleteEvent event) {
		String rs = GwtClientUtils.getFormResult(event);
		if(rs == null)
			return;
		if(!rs.startsWith(SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_ERROR)){
			//show real picture
			int index = rs.indexOf(SharedConstants.LOGO_SEP);
			String small=rs,large=rs;
			if(index != -1){
				small = rs.substring(0,index);
				large = rs.substring(index+1);
			}
			for(Iterator<SpaceUpdateListener> iter = listener.iterator();iter.hasNext();){
				iter.next().logoUpdated(small,large);
			}
		}else{
			//error on upload, keep original image and show error message.
			message.error("Unable upload image, try again.");
		}
	}
	public void fillFields(SpaceModel model){
		spaceUname.setValue(model.unixName);
	}

	public void addListener(SpaceUpdateListener lis){
		listener.add(lis);
	}
}
