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

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.server.SpaceControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonBar;
import com.edgenius.wiki.gwt.client.widgets.FormTextArea;
import com.edgenius.wiki.gwt.client.widgets.FormTextBox;
import com.edgenius.wiki.gwt.client.widgets.HelpPopup;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.TagSuggestBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

/**
 * @author Dapeng.Ni
 */
public class SpaceUpdateForm extends Composite implements SubmitHandler,ClickHandler, AsyncCallback<SpaceModel>{
	
	
	protected static final int TITLE_MAX_LEN = 100;
	protected static final int SHORTNAME_MAX_LEN = 25;
	protected static final int DESC_MAX_LEN = 255;
	
	@UiField MessageWidget message;
	@UiField FormPanel form;
	@UiField FormTextBox name;
	@UiField FormTextArea desc;
	@UiField TagSuggestBox tagsEditBox;
	@UiField CheckBox privateBox;
	@UiField Image helpOnPrivate;
	@UiField ButtonBar buttonBar;
	
	interface PanelUiBinder extends UiBinder<Widget, SpaceUpdateForm> {}
	private static PanelUiBinder uiBinder = GWT.create(PanelUiBinder.class);
	
	private Vector<SpaceUpdateListener> listener = new Vector<SpaceUpdateListener>();
	
	private HelpPopup popOnPrivateSpace;
	
	private Button update = new Button(Msg.consts.update());
	private Button cancel = new Button(Msg.consts.cancel());
	private String spaceUname;
	
	public SpaceUpdateForm(){
		this.initWidget(uiBinder.createAndBindUi(this));
		
		popOnPrivateSpace = new HelpPopup(helpOnPrivate, HelpPopup.SPACE_PRIVATE);
		
		helpOnPrivate.addClickHandler(this);
		name.valid(Msg.consts.name(), true, 0, TITLE_MAX_LEN, null);
		desc.valid(Msg.consts.description(), true, 0, DESC_MAX_LEN, null);

		update.addClickHandler(this);
		buttonBar.add(update);
	
		cancel.addClickHandler(this);
		buttonBar.add(cancel);
		
		form.addSubmitHandler(this);
	}

	public void onClick(ClickEvent event) {
		if(event.getSource() == helpOnPrivate){
			popOnPrivateSpace.pop();
		}else if(event.getSource() == update){
			update.setEnabled(false);
			form.submit();
		}else if(event.getSource() == cancel){
			for(Iterator<SpaceUpdateListener> iter = listener.iterator();iter.hasNext();){
				iter.next().spaceUpdateCancelled();
			}
		}
	}

	public void onSubmit(SubmitEvent event) {
		//validate form
		boolean errors = false;
		if(!name.isValidForSubmit())
			errors = true;
	
		if(!desc.isValidForSubmit())
			errors = true;

		//make RPC call
		if(!errors){
			SpaceControllerAsync spaceController = ControllerFactory.getSpaceController();
			SpaceModel space = new SpaceModel();
			space.name = name.getText();
			space.description = desc.getText();
			space.tags = tagsEditBox.getText();
			//space type bring private/public
			space.type = privateBox.getValue()?SharedConstants.PRIVATE_SPACE:SharedConstants.PUBLIC_SPACE;
			space.unixName = spaceUname;
			spaceController.updateSpace(space, this);
	
		}else{
			//allow second time submit.
			update.setEnabled(true);
		}
		
		//always cancel, RPC call could take over
		event.cancel();
	}
	public void refresh(SpaceModel model) {
		name.setText(model.name);
		desc.setText(model.description);
		spaceUname = model.unixName;
		tagsEditBox.setText(model.tags);
		privateBox.setValue(model.type == SharedConstants.PRIVATE_SPACE?true:false);
	}

	public void onFailure(Throwable error) {
		update.setEnabled(true);
		GwtClientUtils.processError(error);
	}
	public void onSuccess(SpaceModel space) {
		//must enable here: for update space, The user could update again, in this case, signin must be enable
		update.setEnabled(true);

		if(!GwtClientUtils.preSuccessCheck(space,message)){
			return;
		}
		for(Iterator<SpaceUpdateListener> iter = listener.iterator();iter.hasNext();){
			iter.next().spaceUpdated(space);
		}
		
	}
	public void addListener(SpaceUpdateListener lis){
		listener.add(lis);
	}
	
	
	
}
