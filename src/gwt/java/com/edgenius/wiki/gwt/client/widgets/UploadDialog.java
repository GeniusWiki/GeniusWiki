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
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Dapeng.Ni
 */
public class UploadDialog extends DialogBox implements DialogListener{

    private AttachmentPanel attachmentPanel;
	private String spaceUname;
	private String pageUuid;
	private int draft;

	public UploadDialog(AttachmentPanel attachmentPanel, String spaceUname, String pageUuid, int draft){
        this.setText(Msg.consts.upload());
        this.setIcon(new Image(IconBundle.I.get().upload()));
        
        this.attachmentPanel = attachmentPanel;
        this.spaceUname = spaceUname;
        this.pageUuid = pageUuid;
        this.draft = draft;
        
        Frame upload = new Frame(GwtClientUtils.getBaseUrl() + "pages/upload?uname="+URL.encodeQueryString(spaceUname)+"&puuid="+ pageUuid+"&draft="+draft);
        this.setWidget(upload);
        upload.setSize("100%", "100%");
        this.addStyleName(Css.UPLOAD_DIALOG_BOX);
        this.addDialogListener(this);
        
    }

	@Override
	public boolean dialogOpening(DialogBox dialog) {
		return true;
	}

	@Override
	public void dialogOpened(DialogBox dialog) {}

	@Override
	public boolean dialogClosing(DialogBox dialog) {
		if(isDirty()){
			attachmentPanel.refresh(spaceUname, pageUuid, draft);
		}
		return true;
	}

	@Override
	public void dialogClosed(DialogBox dialog) {}

	@Override
	public void dialogRelocated(DialogBox dialog) {}
	
	public native boolean isDirty()/*-{
		return $wnd.isAttachmentDirty();
	}-*/;
}
