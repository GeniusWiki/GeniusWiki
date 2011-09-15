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

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ProgressBar;
import com.google.gwt.user.client.ui.ProgressBar.TextFormatter;

/**
 * @author Dapeng.Ni
 */
public class SoftwareSyncProgressPopup extends PopupPanel implements SyncProgressListener{


	private ProgressBar progressBar =  new ProgressBar();
	private MessageWidget message = new MessageWidget();
	
	public SoftwareSyncProgressPopup(){
		super(false);
		HorizontalPanel panel = new HorizontalPanel();
//		CloseButton cButt = new CloseButton();
//		cButt.addClickHandler(new ClickHandler(){
//			public void onClick(ClickEvent event) {
//				SoftwareSyncProgressPopup.this.hide();
//			}
//		});
		
		progressBar.addStyleDependentName(Css.SLIM_PROGRESS);
		progressBar.setWidth("100%");
		
		panel.add(message);
		panel.add(progressBar);
//		panel.add(cButt);
		
		panel.setWidth("100%");
//		panel.setCellWidth(cButt, "20");
		
		this.setStyleName(Css.SOFTWARE_SYNC_POPUP);
		this.setWidget(panel);
	}
	public void error(String spaceUname, String errorCode) {
		if(!OfflineConstants.SOFTWARE_NAME.equals(spaceUname))
			return;
		
		progressBar.setVisible(false);
		message.error(ErrorCode.getMessage(errorCode, null));
	}

	
	public void percent(String spaceUname, final int percent) {
		if(!OfflineConstants.SOFTWARE_NAME.equals(spaceUname))
			return;
		
		progressBar.setVisible(true);
		message.cleanMessage();
		
		progressBar.setTextFormatter(new TextFormatter(){
			protected String getText(ProgressBar bar, double curProgress) {
				return "Synchronizing software " + percent + "/100";
			}
		});
		progressBar.setProgress(percent);
	}
	/**
	 * If close, then popup, otherwise, do nothing.
	 */
	public void open() {
		this.setPopupPosition(Window.getClientWidth()-230, 21);
		this.show();
	}

}
