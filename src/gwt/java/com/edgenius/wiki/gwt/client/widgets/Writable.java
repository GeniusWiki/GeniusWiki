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

import java.util.Vector;

import com.edgenius.wiki.gwt.client.Css;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class Writable extends SimplePanel {
	
	private DeckPanel deck = new DeckPanel();
	private TextBox textbox = new TextBox();
	private Vector<WritableListener> listeners = new Vector<WritableListener>();
	private String text;
	private boolean readonly;
	
	public Writable(Widget widget, String initialText, boolean readonly){
		this.readonly = readonly;
		this.text = initialText;
		
		textbox.setText(initialText);
		
		deck.insert(widget,0);
		deck.insert(textbox,1);
		deck.showWidget(0);
		textbox.setStyleName(Css.BOX);
		DOM.setStyleAttribute(textbox.getElement(), "width", "98%");
		DOM.setStyleAttribute(textbox.getElement(), "height", "98%");
		this.setWidget(deck);
		
	}
	public Writable(Widget widget, String initialText){
		this(widget,initialText,false);
	}

	public void resetWidget(Widget widget){
		deck.remove(0);
		deck.insert(widget,0);
	}
	public String getText(){
		return text;
	}
	public String getEditingText(){
		return textbox.getText();
	}
	public void addListener(WritableListener listener){
		listeners.add(listener);
	}
	public void addFocusHandler(FocusHandler handler){
		textbox.addFocusHandler(handler);
	}
	public void addBlurHandler(BlurHandler handler){
		textbox.addBlurHandler(handler);
	}

	public boolean isReadonly(){
		return readonly;
	}
	public void enableEdit() {
		if(readonly){
			deck.showWidget(0);
			return;
		}
		
		//show edit box
		deck.showWidget(1);
		textbox.setFocus(true);
		textbox.setText(text == null?"":text);
		for(WritableListener lis : listeners){
			lis.editing(this);
		}
	}
	public void doneEdit() {
		//show label;
		String newText = textbox.getText().trim();
		
		//it has update in textbox, then update original text and fire event.
		if(text == null || (text != null && !text.trim().equals(newText))){
			this.text = textbox.getText();
			for(WritableListener lis : listeners){
				this.text = textbox.getText();
				lis.editDone(this, text);
			}
		}
		deck.showWidget(0);		
	}
	/*
	 * Reset editbox to original text, same meaning with cancel
	 */
	public void cancelEditing(){
		for(WritableListener lis : listeners){
			lis.editCancelled(this, text);
		}
		deck.showWidget(0);
	}
}
