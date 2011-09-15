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
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonBar;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * @author Dapeng.Ni
 */
public abstract class MCEDialog extends DialogBox{
	protected TinyMCE tiny;
	
	public MCEDialog(TinyMCE tiny, boolean autoHide,boolean withBackground, boolean modal){
		super(tiny.getEditor(),autoHide,withBackground, modal);
		
		//register this dialog to Editor, then this dialog can be hide if panel switched. 
		this.addDialogListener(tiny.getEditor());
		
		this.tiny = tiny;
		this.addStyleName(Css.MCE_DIALOG);
		
		ButtonBar btnBar = getButtonBar();
		Button okBtn = new Button(Msg.consts.ok());
		Button cancelBtn = new Button(Msg.consts.cancel());

		okBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				okEvent();
			}

		});
		cancelBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				cancelEvent();
			}

			
		});
		btnBar.add(cancelBtn);
		btnBar.add(okBtn);
	}
	public MCEDialog(TinyMCE tiny){
		this(tiny, false, true, true);
	}
	
	public void open(){
		tiny.saveEditorBookmark();
		
		this.showbox();
	}
	
	public void close(){
		this.hidebox();
	}
	protected abstract void okEvent();
	protected void cancelEvent(){
		this.hidebox();
	}
}
