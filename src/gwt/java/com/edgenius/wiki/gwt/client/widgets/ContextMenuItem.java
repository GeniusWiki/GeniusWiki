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
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Dapeng.Ni
 */
public class ContextMenuItem extends Composite implements MouseOverHandler,ClickHandler, MouseOutHandler, HasMouseOutHandlers, HasMouseOverHandlers, HasClickHandlers{
	
	private Command command;
	private ContextMenu menu;
	private FlowPanel item = new FlowPanel();
	//any object, such as status, cached object etc.
	private Object object;
	public ContextMenuItem(String text, Command command){
		this(text, null, command);
		
	}
	public ContextMenuItem(String text, Image img){
		this(text,img,null);
	}
	public ContextMenuItem(String text, Image img, Command command){
		this.command = command;
		
		this.sinkEvents(Event.ONCLICK);
		this.sinkEvents(Event.ONMOUSEOUT);
		this.sinkEvents(Event.ONMOUSEOVER);
		
		this.addClickHandler(this);
		this.addMouseOutHandler(this);
		this.addMouseOverHandler(this);
		
		setTextImage(text, img);
		
		FlowPanel panel = new FlowPanel();
		panel.add(item);
		this.initWidget(panel);
		this.setStyleName("x-menu-list-item");
		
	}
	/**
	 * Reset text and image
	 * @param text
	 * @param img
	 * @return
	 */
	public void setTextImage(String text, Image img) {
		item.clear();
		
		Label label = new Label(text);
		if(img != null){
			item.add(img);
			img.setStyleName("x-menu-item-icon");
		}
		item.add(label);
		item.setStyleName("x-menu-item");
		
	}


	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		return this.addHandler(handler, MouseOutEvent.getType());
	}

	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		return this.addHandler(handler, MouseOverEvent.getType());
	}

	
	public void onMouseOver(MouseOverEvent event) {
		menu.movein(this);
	}

	
	public void onMouseOut(MouseOutEvent event) {
		menu.moveout(this);
	}

	public void onClick(ClickEvent event) {
		if(command != null)
			command.execute();
		
		menu.hide();
	}

	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return this.addHandler(handler, ClickEvent.getType());
	}


	void setMenu(ContextMenu contextMenu) {
		menu = contextMenu;
	}
	
	public void setCommand(Command command) {
		this.command = command;
	}
	public Object getObject() {
		return object;
	}
	public void setObject(Object object) {
		this.object = object;
	}
	
}
