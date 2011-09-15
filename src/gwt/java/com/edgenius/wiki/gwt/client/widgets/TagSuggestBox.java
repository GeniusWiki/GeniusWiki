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

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.KeyCaptureListener;
import com.edgenius.wiki.gwt.client.model.TagListModel;
import com.edgenius.wiki.gwt.client.model.TagModel;
import com.edgenius.wiki.gwt.client.server.TagControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;

/**
 * @author Dapeng.Ni
 */
public class TagSuggestBox extends SimplePanel implements BlurHandler, AsyncCallback<TagListModel>, KeyUpHandler{
	/**
	 * 
	 */
	private static final char[] INVALID_TAG_PATTERN = "~`!@#$%^&*(){}[]\\|?:;'\"<>/+=".toCharArray();
	public final static int TYPE_PAGE_TAG = 1; 
	public final static int TYPE_SPACE_TAG = 2; 
	
	private	MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
	private SeparatorTextbox textBox = new SeparatorTextbox(new String[]{" ",","});
	private SuggestBox box = new SuggestBox(oracle,textBox);
	private List<String> tags = new ArrayList<String>();
	private boolean filled = false;
	private String spaceUname;
	private String originalText;
	private int type;
	
	
	@UiConstructor public TagSuggestBox(int type){
		this.type = type;
		this.setWidget(box);
		this.addBlurHandler(this);
		this.addKeyUpHandler(this);
		
		DOM.setElementProperty(textBox.getElement(), "maxLength", "60");
		box.setStyleName(Css.TAG_BOX);
		box.getTextBox().addFocusHandler(KeyCaptureListener.instance());
		box.getTextBox().addBlurHandler(KeyCaptureListener.instance());
	}
	public void setHint(String hintText) {
		textBox.setHint(hintText);
	}
	public void setStyleName(String name){
		box.setStyleName(name);
	}
	public void setSpaceUname(String spaceUname){
		if(spaceUname != null && !spaceUname.equalsIgnoreCase(this.spaceUname)){
			//switch new space
			filled = false;
			tags.clear();
		}
		this.spaceUname = spaceUname;
	}
	public void onBlur(BlurEvent event) {
		//so far, every focus will get tag from list again.
		tags.clear();
		filled = false;
	}


	public void onKeyUp(KeyUpEvent event) {
		if(event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE){
			//esc will lost focus
			return;
		}
		
		if(isValid()){
			box.removeStyleName(Css.TAG_BOX_ERROR);

			if(!filled){
				filled = true;
				TagControllerAsync tagControler = ControllerFactory.getTagController();
				if(TYPE_PAGE_TAG == type){
					tagControler.getPageScopeTags(spaceUname, this);
				}else{
					//space type
					tagControler.getSpaceScopeTags(this);
				}
			}
		}
	}
	/**
	 * @return
	 */
	public boolean isValid() {
		char[] input = this.getText().toCharArray();
		
		boolean valid = true;
		for (char c : input) {
			if(StringUtil.contains(INVALID_TAG_PATTERN,c)){
				valid = false;
				break;
			}
		}
		if(!valid && box.getStyleName().indexOf(Css.TAG_BOX_ERROR) == -1){
			box.addStyleName(Css.TAG_BOX_ERROR);
		}
		return valid;
	}
	
	
	/**
	 * Get tags list from server side suggest
	 */
	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
	}
	public void onSuccess(TagListModel model) {
		if(!GwtClientUtils.preSuccessCheck(model,null)){
			return;
		}
		if(model.tags != null){
			for(TagModel tag : model.tags){
				tags.add(tag.name);
			}
		}
		oracle.clear();
		oracle.addAll(tags);
	}
	
	//********************************************************************
	//               Method for Textbox
	//********************************************************************
	public void setTabIndex(int i) {
		box.setTabIndex(i);
		
	}
	public void addFocusHandler(FocusHandler listener) {
		box.getTextBox().addFocusHandler(listener);
		
	}
	public void addBlurHandler(BlurHandler listener) {
		box.getTextBox().addBlurHandler(listener);
		
	}
	public String getText() {
		return textBox.getWholeText();
	}
	public void setText(String string) {
		textBox.setWholeText(string);
		this.originalText = string;
	}
	public void addKeyUpHandler(KeyUpHandler listener) {
		box.addKeyUpHandler(listener);
	}
	
	public void cancelEdit() {
		textBox.setWholeText(originalText);
	}
	public void doneEdit() {
		this.originalText = this.getText();
	}
	public void setFocus(boolean focus) {
		box.setFocus(focus);
		
	}
	//********************************************************************
	//               private class
	//********************************************************************
	/*
	 * This is class is trick for suggest box with separator SuggestBox, it means, if input has separators characters 
	 * exists, the they are divide several parts, each part will bring pop-up menu independently.
	 */
	private class SeparatorTextbox extends HintTextBox{
		private String[] sepChars;
		protected SeparatorTextbox(String[] seps) {
//			super(DOM.createInputText());
			if(seps != null)
				this.sepChars = seps;
			else
				this.sepChars = new String[0];
		}
		
		public String getText(){
			String sepText = super.getText();
			int sep = getSepPos(sepText);
			if(sep > 0){
				//always return last separator part text, so that suggest menu could popup for this part
				return sepText.substring(sep+1);
			}else
				return sepText;
		}

		public void setText(String text){
			String sepText = super.getText();
			int sep = getSepPos(sepText);
			if(sep > 0){
				//there are some separators exist, use append mode 
				sepText = sepText.substring(0,sep+1);
				super.setText(sepText + text);
			}else{
				//no sep exist, the no necessary append
				super.setText(text);
			}
		}
		//normal get text to textBoxl 
		public String getWholeText(){
			return super.getText();
		}
		//normal set text to textBoxl 
		public void setWholeText(String string) {
			super.setText(string);
			
		}
		
		private int getSepPos(String sepText) {
			int sep = 0;
			for(int idx=0;idx<sepChars.length;idx++){
				int comma = sepText.lastIndexOf(sepChars[idx]);
				sep = comma > sep? comma:sep;
			}
			return sep;
		}
	}
	
}
