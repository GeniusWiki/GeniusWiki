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
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Dapeng.Ni
 */
public class HintTextBox extends TextBox implements FocusHandler, BlurHandler{

	private String hintText;
	private boolean blankAsNone;
	private boolean isHint = false;

	/**
	 * This is non-hint box
	 */
	public HintTextBox() {
		this(null,true);
	}
	public HintTextBox(String hintText){
		this(hintText,true);
	}
	/**
	 * 
	 * @param hintText
	 * @param blankAsNone true, then hint text still display if text box is blank(maybe spaces)
	 */
	public HintTextBox(String hintText, boolean blankAsNone){
		this.blankAsNone = blankAsNone;
		this.addFocusHandler(this);
		this.addBlurHandler(this);
		this.setHint(hintText);
	}

	/**
	 * Try to set hint text.It does nothing if the textbox already includes valid input.
	 */
	public void setHint(String hintText) {
		this.hintText = hintText;
		
		if(hintText == null)
			return;
		
		if((blankAsNone && StringUtil.isBlank(super.getText())) || super.getText() == "" || super.getText() == null){
			super.setText(hintText);
			if(!isHint){
				this.addStyleName(Css.HINT_TEXT_BOX);
				isHint = true;
			}
		}
	}

	public void setText(String text){
		if(text == null || "".equals(text) ||  (blankAsNone && StringUtil.isBlank(text))){
			super.setText(hintText);
			this.addStyleName(Css.HINT_TEXT_BOX);
			this.isHint = true;
		}else{
			super.setText(text);
			this.isHint = false;
			this.removeStyleName(Css.HINT_TEXT_BOX);
		}
	}
	
	public String getText(){
		if(isHint){
			return "";
		}else{
			return super.getText();
		}
	}
	public void setStyleName(String newStyle){
		//call before below setStyleName()
		super.setStyleName(newStyle);
		
		//need restore hint style if it has - don't overwrite
		if(isHint){
			this.addStyleName(Css.HINT_TEXT_BOX);
		}
	}


	public void onFocus(FocusEvent event) {
		if(isHint){
			super.setText("");
			this.removeStyleName(Css.HINT_TEXT_BOX);
			isHint = false;
		}
	}
	public void onBlur(BlurEvent event) {
		setHint(hintText);
	}
	

}
