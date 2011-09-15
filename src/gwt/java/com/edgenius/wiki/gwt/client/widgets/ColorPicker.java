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
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 */
public class ColorPicker extends SimplePanel implements ClickHandler, PaletteListener{

	//pick widget may appear multiple in same page, so use ID to identify them...
	private String id = HTMLPanel.createUniqueId();
	
	private ClickLink link = new ClickLink();
	private String color;

	/**
	 * @param defaultColor format is #FFFFFF
	 * @param text: if just want to display color block, set it as blank
	 */
	public ColorPicker(final String defaultColor, String text){
		if(StringUtil.isBlank(text)){
			text = " ";
		}
		link.setText(text);
		
		
		color = defaultColor;
		if(StringUtil.isBlank(color))
			color = "#FFFFFF";
	
		this.setStyleName(Css.COLOR_PICKER);
		link.setStyleName(Css.COLOR_BLOCK);
		DOM.setElementAttribute(link.getLinkElement(), "id", "colorPicker"+id);
		
		
		link.addClickHandler(this);
		this.setWidget(link);
		setColor(defaultColor);
	}


	public void onClick(ClickEvent event) {
		//popup palette panel close to the pick 
		PaletteDialog pop = new PaletteDialog(id, color);
		pop.addPaletteListener(this);
		pop.showbox();
	}
	
	public void setColor(String newColor){
		if(GwtClientUtils.isIE()){
			setBgColor(id, newColor);
		}else{
			DOM.setElementAttribute(link.getLinkElement(),"style", "background-color: " + newColor);
		}
		this.color = newColor;	
	}
	
	public String getColor(){
		return color;
	}

	public void selectedColor(String receiverID, String color) {
		if(id.equals(receiverID))
			this.setColor(color);
	}
	//I don't know why IE does not work using GWT setAttribute(background...), so use JQuery method instead
	public native void setBgColor(String id, String color)/*-{
		$wnd.$("#colorPicker"+id).css("backgroundColor",color);
	}-*/;


}
