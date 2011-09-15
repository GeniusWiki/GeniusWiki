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
import com.edgenius.wiki.gwt.client.server.utils.NumberUtil;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Dapeng.Ni
 */
public class MCEMergeCellsDialog extends MCEDialog {
	private TextBox rowBox = new TextBox();
	private TextBox colBox = new TextBox();
	/**
	 * @param tiny
	 */
	public MCEMergeCellsDialog(TinyMCE tiny) {
		super(tiny);
		
		this.setText(Msg.consts.merge_cells());
		Label rowLabel = new Label(Msg.consts.rows());
		Label colLabel = new Label(Msg.consts.cols());
		
		FlexTable layout = new FlexTable();
		layout.setWidget(0, 0, rowLabel);
		layout.setWidget(0, 1, rowBox);
		
		layout.setWidget(1, 0, colLabel);
		layout.setWidget(1, 1, colBox);
		
		layout.setSize("100%", "100%");
		colLabel.setStyleName(Css.FORM_LABEL);
		rowLabel.setStyleName(Css.FORM_LABEL);
		rowBox.setStyleName(Css.FORM_INPUT);
		colBox.setStyleName(Css.FORM_INPUT);
		
		FormPanel form = new FormPanel();
		form.setWidget(layout);
		this.setWidget(form);
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				rowBox.setFocus(true);
			}
		});
		init();
	}

	@Override
	protected void okEvent() {
		int row = NumberUtil.toInt(rowBox.getText(), -1);
		int col =NumberUtil.toInt(colBox.getText(), -1);
		if(row <=0 || col <= 0){
			Window.alert(Msg.consts.error_input_number_only());
			return;
		}
		
		JsArrayString input = (JsArrayString) JavaScriptObject.createArray();
		input.set(0, rowBox.getText());
		input.set(1, colBox.getText());
		
		mergeCells(input);
		close();

	}
	private native void mergeCells(JsArrayString input) /*-{
	 	 $wnd.mergeCells(input);
	}-*/;
	private void init(){
		//put initial values
		rowBox.setText("2");
		colBox.setText("2");
	}
}
