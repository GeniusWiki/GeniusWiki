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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Dapeng.Ni 
 */
public class ImageToggleButton extends Composite implements HasClickHandlers, ClickHandler{
	private Image onImg;
	private Image offImg;
	private boolean isOn;
	
	private FlowPanel panel = new FlowPanel();
	
	public ImageToggleButton(Image onImg, Image offImg, boolean on) {
		this.onImg = onImg;
		this.offImg = offImg;
		
		initial(on);
	}
	public ImageToggleButton(String onName, String offName, boolean on) {
		onImg = new Image(onName);
		offImg = new Image(offName);
		
		initial(on);
	}
	private void initial(boolean on){
		onImg.addClickHandler(this);
		offImg.addClickHandler(this);
		
		panel.add(onImg);
		panel.add(offImg);
		DOM.setStyleAttribute(panel.getElement(), "display", "inline");
		
		setOn(isOn);
		initWidget(panel);
	}
	
	public void setOnTitle(String currentIsOnTitle){
		this.onImg.setTitle(currentIsOnTitle);
	}
	
	public void setOffTitle(String currentIsOffTitle){
		this.offImg.setTitle(currentIsOffTitle);
	}

	public void setOn(boolean on){
		if(on){
			onImg.setVisible(true);
			offImg.setVisible(false);
		}else{
			onImg.setVisible(false);
			offImg.setVisible(true);
		}
		isOn = on;
	}
	public boolean isOn() {
		return isOn;
	}

	public void onClick(ClickEvent event) {
		if((onImg.isVisible() && event.getSource() == onImg)
			|| (offImg.isVisible() && event.getSource() == offImg)){
			this.fireEvent(event);
		}
	}

	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return this.addHandler(handler, ClickEvent.getType());
	}
}
