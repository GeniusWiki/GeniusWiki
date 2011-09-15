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

import java.util.ArrayList;
import java.util.List;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Frame;
/**
 * @author Dapeng.Ni
 */
public class PaletteDialog extends DialogBox implements ClickHandler{
	

	private Frame palette = new Frame(GwtClientUtils.getBaseUrl() +"widgets/tiny_mce/themes/advanced/color_picker.htm");

	private Button okBtn = new Button(Msg.consts.ok());
	private Button cancelBtn = new Button(Msg.consts.cancel());
	private List<PaletteListener> listeners = new ArrayList<PaletteListener>();
	private String receiverID;

	
	public PaletteDialog(String receiverID, final String initColor){
		this.receiverID = receiverID;
		this.setText(Msg.consts.palette());
		this.addStyleName(Css.PALETTE_DIALOG);
		DOM.setElementAttribute(palette.getElement(), "frameborder", "0");
		DOM.setElementAttribute(palette.getElement(), "scrolling", "no");
		
		DOM.setElementProperty(palette.getElement(), "id", "pickerFrame");
		ButtonBar btnPanel = getButtonBar();
		btnPanel.add(okBtn);
		btnPanel.add(cancelBtn);
		
		cancelBtn.addClickHandler(this);
		okBtn.addClickHandler(this);
		this.setWidget(palette);
		bindJsMethod(initColor);
	}

	public void addPaletteListener(PaletteListener listener){
		listeners.add(listener);
	}
	public void onClick(ClickEvent event) {
		if(event.getSource() == okBtn){
			submitPicker(this);
		}
		
		this.hidebox();
	}
	private native void bindJsMethod(String initColor)/*-{
		$wnd.gwtGetPaletteInitColor = function(){
			return initColor;
		}
	}-*/;
	
	
	private native void submitPicker(PaletteDialog palette)/*-{
	   var color = $wnd.document.getElementById('pickerFrame').contentWindow.document.getElementById("color").value;
	   palette.@com.edgenius.wiki.gwt.client.widgets.PaletteDialog::colorChanged(Ljava/lang/String;)(color);
	   
	}-*/;
	
	//call by JS
	private void colorChanged(String color){
		for (PaletteListener lis : listeners) {
			lis.selectedColor(receiverID ,color);
		}
	}
}
