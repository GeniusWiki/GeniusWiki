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
package com.edgenius.wiki.gwt.client.offline;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.CloseButton;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.Popup;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.gears.client.GearsException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ProgressBar;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.ProgressBar.TextFormatter;
/**
 * @author Dapeng.Ni
 */
public class SpaceSyncProgressPopup extends Popup implements CloseHandler<PopupPanel>, SyncProgressListener{

	private CheckBox attG = new CheckBox(Msg.consts.sync_atts());
	private CheckBox draftG = new CheckBox(Msg.consts.sync_drafts());
	private CheckBox historyG = new CheckBox(Msg.consts.sync_histories());
	private CheckBox commentG = new CheckBox(Msg.consts.sync_comments());
	
	private String spaceUname;
	private DeckPanel deck = new DeckPanel();
	private int chosenOptions;
	private ProgressBar progressBar =  new ProgressBar();
	private MessageWidget message = new MessageWidget(false);
	
	public SpaceSyncProgressPopup(UIObject target, final UserModel user,final  String spaceUname){
		super(target);
		
		this.spaceUname = spaceUname;
		
		progressBar.setVisible(false);
		FlexTable table = new FlexTable();
		table.setWidget(0, 0, attG );
		table.setWidget(0, 1, draftG );
		
		table.setWidget(1, 0, historyG );
		table.setWidget(1, 1, commentG );
		
		table.setCellPadding(5);
		table.setCellSpacing(5);
		
		DisclosurePanel optionsPanel = new DisclosurePanel(Msg.consts.advance());
		optionsPanel.setContent(table);

		HorizontalPanel funcP = new HorizontalPanel();
		funcP.add(message);
		funcP.add(progressBar);
		CloseButton cButt = new CloseButton();
		cButt.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				SpaceSyncProgressPopup.this.hide();
			}
		});
		funcP.add(cButt);
		funcP.setCellWidth(cButt, "20");
		funcP.setWidth("100%");

		
		VerticalPanel panel = new VerticalPanel();
		panel.add(funcP);
		panel.add(optionsPanel);
		boolean isReady = OfflineUtil.isReadyForSpace(user.getUid(),spaceUname);
		if(isReady){
			ClickLink disLink = new ClickLink(Msg.consts.disconnect_offline());
			disLink.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					if(Window.confirm(Msg.consts.confirm_remove_offline_content())){
						Sync sync = new Sync();
						sync.disconnectSpace(user,spaceUname);
					}
				}
			});
			panel.add(disLink);
		}
		
		FlowPanel busyPanel = new FlowPanel();
		busyPanel.add(IconBundle.I.loading());
		deck.insert(busyPanel, 0);
		deck.insert(panel, 1);
		deck.showWidget(0);
		this.setWidget(deck);
		
		progressBar.addStyleDependentName(Css.SLIM_PROGRESS);
		panel.setSize("100%", "100%");
		table.setSize("100%", "100%");
		optionsPanel.setWidth("100%");
		this.setStyleName(Css.GEAR_POPUP);
		
	}
	public void setOptions(int chosenOptions){
		
		this.chosenOptions = chosenOptions;
		
		attG.setValue(false);
		draftG.setValue(false);
		historyG.setValue(false);
		commentG.setValue(false);
		if(chosenOptions == SharedConstants.OPTION_NONE){ 
			return;
		}
		
		if(chosenOptions == SharedConstants.OPTION_ALL || (chosenOptions & SharedConstants.OPTION_SYNC_ATTACHMENT) > 0)
			attG.setValue(true);
		if(chosenOptions == SharedConstants.OPTION_ALL ||(chosenOptions & SharedConstants.OPTION_SYNC_DRAFT) > 0)
			draftG.setValue(true);
		if(chosenOptions == SharedConstants.OPTION_ALL || (chosenOptions & SharedConstants.OPTION_SYNC_HISTORY) > 0)
			historyG.setValue(true);
		if(chosenOptions == SharedConstants.OPTION_ALL || (chosenOptions & SharedConstants.OPTION_SYNC_COMMENT) > 0)
			commentG.setValue(true);
		
		//show options panel
		deck.showWidget(1);
	}
	
	public void onClose(CloseEvent<PopupPanel> event) {
		super.onClose(event);
		//temporary: save options while popup close
		saveOptions();
	}
	
	private void saveOptions(){
		if(spaceUname == null)
			return;
		
		int options = 0;
		if(attG.getValue())
			options |= SharedConstants.OPTION_SYNC_ATTACHMENT;
		if(draftG.getValue())
			options |= SharedConstants.OPTION_SYNC_DRAFT;
		if(historyG.getValue())
			options |= SharedConstants.OPTION_SYNC_HISTORY;
		if(commentG.getValue())
			options |= SharedConstants.OPTION_SYNC_COMMENT;

		//if nothing choose, then reset it to 1 rather than 0, because 
		if(options == 0)
			options = SharedConstants.OPTION_NONE;
		
		if(options == chosenOptions)
			return;
		
		try {
			GearsDB.getUserDB(OfflineUtil.getUser().getUid()).saveOptions(spaceUname,options);
		} catch (GearsException e) {
			Log.error("Unable persist user options for space " + spaceUname,e);
		}
	}
	
	public MessageWidget getMessage() {
		return message;
	}
	public void error(String spaceUname, String errorCode) {
		if(!spaceUname.equals(this.spaceUname))
			return;

		progressBar.setVisible(false);
		message.error(ErrorCode.getMessage(errorCode, null));
	}
	public void percent(String spaceUname, int percent) {
		if(!spaceUname.equals(this.spaceUname))
			return;

		if(percent == 100){
			progressBar.setVisible(false);
			message.info(Msg.consts.sync_success());
			return;
		}
		
		message.cleanMessage();
		progressBar.setVisible(true);
		progressBar.setTextFormatter(new TextFormatter(){
			protected String getText(ProgressBar bar, double curProgress) {
				String truckName = SpaceSyncProgressPopup.this.spaceUname.length() > 30? 
						SpaceSyncProgressPopup.this.spaceUname.substring(0,25)+ "..."
						:SpaceSyncProgressPopup.this.spaceUname+"...";
				return  Msg.params.confirm_sync_space(truckName);
			}
		});
		progressBar.setProgress(percent);
	}
}
