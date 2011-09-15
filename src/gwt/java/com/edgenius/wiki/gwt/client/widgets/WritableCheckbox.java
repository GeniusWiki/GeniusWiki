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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 */
public class WritableCheckbox extends SimplePanel {

	private DeckPanel deck = new DeckPanel();

	private CheckBox checkbox = new CheckBox();

	private Image checkedImg = new Image(IconBundle.I.get().tick());
	private Image uncheckedImg = new Image(IconBundle.I.get().cross());

	private boolean defaultChecked;
	private boolean editing;
	private boolean readonly;
	
	private Object object;
	public WritableCheckbox(boolean selected, boolean editing, boolean readonly) {

		
		deck.insert(checkbox, 0);
		deck.insert(checkedImg, 1);
		deck.insert(uncheckedImg, 2);

		this.defaultChecked = selected;
		this.editing = editing;
		this.readonly = readonly;
		
		reset();

		DOM.setStyleAttribute(checkedImg.getElement(),  "width", "16px");
		DOM.setStyleAttribute(checkedImg.getElement(), "height", "16px");
		DOM.setStyleAttribute(uncheckedImg.getElement(),  "width", "16px");
		DOM.setStyleAttribute(uncheckedImg.getElement(), "height", "16px");
		
		deck.setWidth("20px");
		this.setWidth("20px");
		setWidget(deck);
	}
	public void setEditing( boolean editing){
		this.editing = editing;
		if (editing && !readonly) {
			deck.showWidget(0);
		} else {
			if (checkbox.getValue()) 
				deck.showWidget(1);
			else
				deck.showWidget(2);
		}
	}
	public void setReadonly(boolean readonly){
		this.readonly = readonly;
		if(defaultChecked)
			deck.showWidget(1);
		else
			deck.showWidget(2);
	}
	public boolean isChecked(){
		return checkbox.getValue();
	}
	public boolean isChanged(){
		return checkbox.getValue() == defaultChecked?false:true;
	}
	public void changeDone(){
		defaultChecked = checkbox.getValue();
	}
	public void reset(){
		checkbox.setValue(defaultChecked);
		if (editing && !readonly) {
			deck.showWidget(0);
		} else {
			if (defaultChecked) 
				deck.showWidget(1);
			else
				deck.showWidget(2);
		}
	}
	public Object getObject() {
		return object;
	}
	public void setObject(Object object) {
		this.object = object;
	}
	
	
}
