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
import com.edgenius.wiki.gwt.client.KeyCaptureListener;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBoxBase;

/**
 * @author Dapeng.Ni
 */
public abstract class FormTextBoxBase extends Composite implements BlurHandler,  KeyUpHandler{
	
	protected Label msg = new Label();
	protected TextBoxBase box;
	
	private int validMaxLen;
	private int validMinLen;
	private boolean validRequired;
	
	private String i18nName;
	private FormTextBoxValidCallback validCallback;
	private boolean callbackValidError;
	
	public FormTextBoxBase(TextBoxBase box){
		this.box = box;
		
		FlowPanel parent = new FlowPanel();
		parent.add(msg);
		parent.add(box);
		
		msg.setVisible(false);
		msg.setStyleName(Css.ERRORDIV);
		
		box.addFocusHandler(KeyCaptureListener.instance());
		box.addBlurHandler(KeyCaptureListener.instance());
		this.initWidget(parent);
	}
	public void setError(String message){
		if(StringUtil.isBlank(message)){
			//clean
			box.removeStyleName(Css.ERROR);
			msg.setText("");
			msg.setVisible(false);
		}else{
			msg.setText(message);
			msg.setVisible(true);
			//remove first, then add again to avoid duplicated added
			box.removeStyleName(Css.ERROR);
			box.addStyleName(Css.ERROR);
		}
	}
	

	public void cleanError(){
		setError(null);
	}
	public Object getEventSource(){
		return box;
	}

	/**
	 * This method give different valid options. Only first input parameter is mandatory. 
	 *  
	 * @param i18nName
	 * @param reqired
	 * @param validMinLen
	 * @param validMaxLen
	 * @param validCallback
	 */
	public void valid(String i18nName, boolean reqired, int validMinLen, int validMaxLen, FormTextBoxValidCallback validCallback ){
		this.i18nName = i18nName;
		this.validMinLen = validMinLen;
		this.validMaxLen = validMaxLen;
		this.validRequired = reqired;
		this.validCallback = validCallback;
		
		box.addBlurHandler(this);
		box.addKeyUpHandler(this);
	}

	public void onKeyUp(KeyUpEvent event) {
		if(event.getSource() == box){
			this.cleanError();
			
			String text = box.getText();
			// only valid max len - if length over, truncate and show error message
			if(validMaxLen > 0 && text.length() > validMaxLen){
				box.setText(text.substring(0,validMaxLen));
				this.setError(Msg.params.max_length(i18nName, String.valueOf(validMaxLen)));
			}
			if(validCallback != null){
				callbackValidError = false;
				String msg = validCallback.onKeyUpValid(this);
				if(!StringUtil.isBlank(msg)){
					callbackValidError = true;
					this.setError(msg);
				}
			}
		}
	}
	public void onBlur(BlurEvent event) {
		if(event.getSource() == box){
			this.cleanError();
			
			doValid();
			if(validCallback != null){
				callbackValidError = false;
				String msg = validCallback.onBlurValid(this);
				if(!StringUtil.isBlank(msg)){
					callbackValidError = true;
					this.setError(msg);
				}
			}
		}
	}
	
	public boolean isValidForSubmit(){
		//as blur and keyPress is not called again here, so if it has callback error, won't clean error
		if(!callbackValidError)
			this.cleanError();
			
		return !doValid() && !callbackValidError;
	}
	
	private boolean doValid() {
		
		boolean hasErrors = false;
		String text = StringUtil.trimToEmpty(box.getText());
		if(validRequired && text.length() == 0){
			hasErrors = true;
			this.setError(Msg.params.required(i18nName));
		}else{
			if(validMinLen > 0 && text.length() < validMinLen){
				hasErrors = true;
				this.setError(Msg.params.min_length(i18nName, String.valueOf(validMinLen)));
			}else if(validMaxLen > 0 && text.length() > validMaxLen){
				hasErrors = true;
				this.setError(Msg.params.max_length(i18nName, String.valueOf(validMaxLen)));
				//truncate
				box.setText(text.substring(0,validMaxLen));
			}
		}
		
		return hasErrors;
	}

	public void setText(String text) {
		box.setText(text);
	}		
	public void setName(String name) {
		box.setName(name);
	}
	public String getName() {
		return box.getName();
	}
	public String getText() {
		return box.getText();
	}
	public void setFocus(boolean b) {
		box.setFocus(b);
	}
	public void addKeyPressHandler(KeyPressHandler handler) {
		box.addKeyPressHandler(handler);
	}
	public void addKeyDownHandler(KeyDownHandler handler) {
	    box.addKeyDownHandler(handler);
	}
	public void addBlurHandler(BlurHandler handler) {
		box.addBlurHandler(handler);
	}
	public void addFocusHandler(FocusHandler handler) {
		box.addFocusHandler(handler);
	}
	public void setTabIndex(int idx){
		box.setTabIndex(idx);
	}
	public void setStyleName(String style){
		box.setStyleName(style);
	}
	public void addStyleName(String style){
		box.addStyleName(style);
	}
	public void removeStyleName(String style){
		box.removeStyleName(style);
	}
}
