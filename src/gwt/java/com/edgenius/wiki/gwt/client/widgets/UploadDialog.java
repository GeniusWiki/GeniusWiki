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

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.page.widgets.AttachmentPanel;
import com.edgenius.wiki.gwt.client.server.constant.PageType;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Dapeng.Ni
 */
public class UploadDialog extends DialogBox implements DialogListener{

    private AttachmentPanel attachmentPanel;
	private String spaceUname;
	private String pageUuid;
	private PageType draft;
	private DeckPanel deck = new DeckPanel();
	
	public UploadDialog(AttachmentPanel attachmentPanel, String spaceUname, String pageUuid, PageType draft){
        this.setText(Msg.consts.upload());
        this.setIcon(new Image(IconBundle.I.get().upload()));
        
        this.attachmentPanel = attachmentPanel;
        this.spaceUname = spaceUname;
        this.pageUuid = pageUuid;
        this.draft = draft;
        
        Frame upload = new Frame(GwtClientUtils.getBaseUrl() + "pages/upload?uname="+URL.encodeQueryString(spaceUname)+"&puuid="+ pageUuid+"&draft="+draft);
        
        FlowPanel busyPanel = new FlowPanel();
        String id = HTMLPanel.createUniqueId();
        HTMLPanel busyPanelDiv = new HTMLPanel("<span></span><div id='"+ id+"'></div>");
        busyPanelDiv.add(IconBundle.I.loading(), id);
        busyPanel.add(busyPanelDiv);
        busyPanelDiv.setStyleName(Css.BUSY_PANEL);
        busyPanelDiv.addStyleName("upload");
        
		deck.add(busyPanel);
        deck.add(upload);
        
        deck.showWidget(0);
        
        this.setWidget(deck);
        upload.setSize("100%", "100%");
        deck.setSize("100%", "100%");
        this.addStyleName(Css.UPLOAD_DIALOG_BOX);
        this.addDialogListener(this);
        
        this.bindJSMethod(this);
        
    }

	@Override
	public boolean dialogOpening(DialogBox dialog) {
		return true;
	}

	@Override
	public void dialogOpened(DialogBox dialog) {}

	@Override
	public boolean dialogClosing(DialogBox dialog) {
//		if(isDirty()){
		//always refresh attachment panel - until  isDirty() working. 
			attachmentPanel.refresh(spaceUname, pageUuid, draft);
//		}
		return true;
	}

	@Override
	public void dialogClosed(DialogBox dialog) {}

	@Override
	public void dialogRelocated(DialogBox dialog) {}
	
	public void ready(){
		deck.showWidget(1);
	}
	
	public native boolean bindJSMethod(UploadDialog dialog)/*-{
		$wnd.gwtUploadPageReady = function() {
			dialog.@com.edgenius.wiki.gwt.client.widgets.UploadDialog::ready()();
		};
	}-*/;
	public native boolean isDirty()/*-{
		return $wnd.isAttachmentDirty();
	}-*/;
}
