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
package com.edgenius.wiki.gwt.client.editor;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class MCEAnchorDialog extends MCEDialog {
	private static final String AID = "com.edgenius.wiki.render.filter.AnchorFilter";
	
	private boolean update = false;
	private TextBox box = new TextBox();
	private MessageWidget message = new MessageWidget();
	public MCEAnchorDialog(TinyMCE tiny){
		super(tiny);
		
		this.setText(Msg.consts.insert_anchor());
		Label label = new Label(Msg.consts.anchor_name());
		
		HorizontalPanel func = new HorizontalPanel();
		func.add(label);
		func.add(box);
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(func);
		
		panel.setSize("100%", "100%");
		func.setSize("100%", "100%");
		label.setStyleName(Css.FORM_LABEL);
		box.setStyleName(Css.FORM_INPUT);
		this.setWidget(panel);
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				box.setFocus(true);
			}
		});
	}
	@Override
	protected void okEvent() {
		String name = StringUtil.trim(box.getText());
		if(name == null || name.length() == 0){
			message.info(Msg.consts.non_empty());
			return;
		}
		tiny.restoreEditorBookmark();

		if(update){
			insertAnchor("update", AID, name);
		}else{
			insertAnchor("insert", AID, name);
		}
		close();
	}
	public void initField(String name){
		if(!StringUtil.isBlank(name)){
			box.setText(name);
			update = true;
		}
	}
	
	private native void insertAnchor(String action, String aid, String anchorName) /*-{
		 $wnd.action=action;
	 	 $wnd.updateAnchor(aid, anchorName);
	}-*/;	
		
}
