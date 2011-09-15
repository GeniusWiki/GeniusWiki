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
import com.edgenius.wiki.gwt.client.server.utils.MacroMaker;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.widgets.HintTextArea;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;

/**
 * @author Dapeng.Ni
 */
public class MCEMacroMessageDialog extends MCEDialog {

	//if need valid content is not empty, then input
//	private MessageWidget message = new MessageWidget();
	private String type;
	private HintTextArea title = new HintTextArea(Msg.consts.optional());
	private TextArea content = new TextArea();
	
	public MCEMacroMessageDialog(TinyMCE tiny, String type){
		super(tiny);
		this.type = type.toLowerCase();
		
		this.setText(Msg.consts.insert() + " " + type + " " + Msg.consts.macro());
		
		Label l1 = new Label(Msg.consts.title());
		Label l2 = new Label(Msg.consts.content());
		
		int row = 0;
		FlexTable panel = new FlexTable();
//		panel.setWidget(row, 0, message);
//		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
//		row++;
		
		panel.setWidget(row, 0, l1);
		panel.setWidget(row, 1, title);
		row++;
		
		panel.setWidget(row, 0, l2);
		panel.setWidget(row, 1, content);
		
		l1.setStyleName(Css.FORM_LABEL);
		l2.setStyleName(Css.FORM_LABEL);
		title.setStyleName(Css.FORM_INPUT);
		content.setStyleName(Css.DESC);
		
		this.setWidget(panel);
	}


	@Override
	protected void okEvent() {
		StringBuffer buffer = new StringBuffer();
		String titleStr = title.getText();
		String contentStr = content.getText();
		
		MacroMaker.buildMessage(buffer, titleStr, contentStr, type, getAid());
		this.tiny.insertContent(buffer.toString());
		
		this.hidebox();
	}
	
	private String getAid() {
		if(NameConstants.ERROR.equals(type)){
			return "com.edgenius.wiki.render.macro.ErrorMacro";
		}else if(NameConstants.WARNING.equals(type)){
			return "com.edgenius.wiki.render.macro.WarningMacro";
		}else{
			return "com.edgenius.wiki.render.macro.InfoMacro";
		}
	}

}
