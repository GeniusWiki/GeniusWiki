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
import com.edgenius.wiki.gwt.client.page.widgets.QuickHelpDictionary;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
/**
 * 
 * @author Dapeng.Ni
 */
public class PlainEditor extends SimplePanel implements SwitchEditorListener, HasChangeHandlers{

	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//               For quick help
	private FlowPanel quickHelpBar = new FlowPanel();
	private int quickHelpHolderKey = 0;
	private long quickHelpHolderStart = 0;
	private FlowPanel topPanel = new FlowPanel();
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// switch to rich editor
	private ClickLink switchLink = new ClickLink(SharedConstants.PREV_LINK+" " + Msg.consts.rich_editor(),true);
	private Image switchBusy = IconBundle.I.indicator();
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// TextArea
	private TextArea textArea = new TextArea();
	private String id = HTMLPanel.createUniqueId();
	private Editor editor;
	//********************************************************************
	//               method
	//********************************************************************
	public PlainEditor(final Editor editor){
		this.editor = editor;
		textArea.addKeyPressHandler(new ContentChangeListener());
		textArea.addKeyDownHandler(new ContentChangeListener());
		textArea.addKeyPressHandler(new QuickHelpKeyListener());

		HorizontalPanel funcPanel = new HorizontalPanel();
		if(Editor.RICH_ENABLE){
			switchLink.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					switchBusy.setVisible(true);
					switchLink.setVisible(false);
					editor.toRichEditor();
				}
			});
			HorizontalPanel switchPanel = new HorizontalPanel();
			switchPanel.add(switchBusy);
			switchPanel.add(switchLink);
			switchLink.addStyleName(Css.RIGHT);
			switchBusy.addStyleName(Css.RIGHT);
			switchPanel.setCellWidth(switchLink, "100");
			switchBusy.setVisible(false);
			funcPanel.add(switchPanel);
		}		
		
//		HelpButton helpLink = new HelpButton(true);
//		funcPanel.add(helpLink);
		
		
		topPanel.add(quickHelpBar);
		topPanel.add(funcPanel);

	    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// main
		VerticalPanel panel = new VerticalPanel();
		panel.add(topPanel);
		panel.add(textArea);
		panel.setWidth("100%");
		
		funcPanel.addStyleName(Css.RIGHT);
	    topPanel.setStyleName(Css.EDIT_TOOLBAR);
	    quickHelpBar.setStyleName(Css.QUICK_HELP);
	    textArea.setStyleName(Css.MARKUP_TEXTAREA);
	    //this is require of TinyMCE richeditor - to auto resize according to browser window. The class attribute link outter style is not working
	    textArea.setWidth("100%");
        DOM.setElementAttribute(textArea.getElement(), "id", id);
        
		this.setWidget(panel);
	}

	/**
	 * TextArea element ID
	 * @return
	 */
	public String getID(){
		return id;
	}

	public String getText() {
		return textArea.getText();
	}
	public void setText(String text) {
		textArea.setText(text);
	}

	/**
	 * if disable, only hide top panel as TextArea will replace by TinyMCE.
	 */
	public void enable(boolean enable) {
		if(enable){
			topPanel.setVisible(true);
		}else{
			topPanel.setVisible(false);
		}
		
	}
	private class QuickHelpKeyListener implements KeyPressHandler{

		public void onKeyPress(KeyPressEvent event) {
			String help = QuickHelpDictionary.findQuickHelpText(event.isControlKeyDown(), event.isAltKeyDown(),
					event.isShiftKeyDown(), event.isMetaKeyDown(), (int)event.getCharCode());
			int key = new Integer(help.substring(0,2)).intValue(); 
			help = help.substring(2);
			
			//15s, or a new quick is not default and different with old quick help text
			if((System.currentTimeMillis() - quickHelpHolderStart > 15000)
				||(key != 0 && key != quickHelpHolderKey)){
				quickHelpBar.clear();
				quickHelpBar.add(new HTML(help));
				quickHelpHolderStart = System.currentTimeMillis();
				quickHelpHolderKey = key;
			}
				
		}
	}

	public void failedOnSwitch() {
		switchBusy.setVisible(false);
		switchLink.setVisible(true);
		
	}

	public void successOnSwitch() {
		switchBusy.setVisible(false);
		switchLink.setVisible(true);
	}

	/**
	 * @param focus
	 */
	public void setFocus(boolean focused) {
		textArea.setFocus(focused);
		
	}

	/**
	 * @param idx
	 */
	public void setTabIndex(int idx) {
		textArea.setTabIndex(idx);
	}

	public void addFocusHandler(FocusHandler listener) {
		textArea.addFocusHandler(listener);
	}
	public HandlerRegistration addChangeHandler(ChangeHandler handler) {
		return this.addHandler(handler, ChangeEvent.getType());
	}

	/**
	 * @param listener
	 */
	public void addKeyPressHandler(KeyPressHandler listener) {
		textArea.addKeyPressHandler(listener);
		
	}
	public void addKeyUpHandler(KeyUpHandler listener) {
		textArea.addKeyUpHandler(listener);
		
	}
	public void addKeyDownHandler(KeyDownHandler listener) {
		textArea.addKeyDownHandler(listener);
		
	}

	private class ContentChangeListener implements KeyDownHandler, KeyPressHandler{

		public void onKeyPress(KeyPressEvent event) {
			
			//Comment: don't check modifier to skip, since Ctrl-V may copy some text from clipboard, the content of textarea
			//can modified then....
			
//			skip any non-printing event
//			TODO: I have no idea about META key, I have no Mac!!! Hope get one soon.
//			if((modifiers == KeyboardListener.MODIFIER_ALT)
//				|| (modifiers == KeyboardListener.MODIFIER_CTRL)
//				|| (modifiers == (KeyboardListener.MODIFIER_ALT|KeyboardListener.MODIFIER_SHIFT))
//				|| (modifiers == (KeyboardListener.MODIFIER_ALT|KeyboardListener.MODIFIER_CTRL))
//				|| (modifiers == (KeyboardListener.MODIFIER_SHIFT|KeyboardListener.MODIFIER_CTRL)))
//				return;
			
//			1. System shortcut use onKeyDown() to check if there are shortcut invoke. If here uses keyPress(), it means
//			This method still be executed if user click "ctrl-s". In this scenario, the saveDraftTime will be setup after page 
//			 are saved by shortcut "ctrl-s". Then a draft will saved even after page already jump to viewpanel.
//			2. same reaons, if preview shortcut is typed. 
			//ctrl-alt-s: return to view page, but auto-draft still saved after seconds
			//ctrl-alt-q: preview to PreviewPanel first while previewReady flag is true, but touch() will set it false and clear PreviewPanel!
			
//			NOTE: comment it to avoid above some side effect,  
//			touch();
		}
		/**
		 * many browsers do not generate keypress events for non-printing keyCode values, 
		 * such as KeyboardListener.KEY_ENTER  or arrow keys<br>.But they can be capture in keyDown/keyUp method
		 * 
		 * Here will invoke saveDraftTimer only for printing keyCode, includes ENTER.
		 */
		public void onKeyDown(KeyDownEvent event) {
//			if(keyCode == KeyboardListener.KEY_ENTER)
//			OK, arrow key also invoke touch(), it is not good, but if using keyPress(), it also has bugs:
			//ctrl-alt-s: return to view page, but auto-draft still saved after seconds
			//ctrl-alt-q: preview to PreviewPanel first while previewReady flag is true, but touch() will set it false and clear PreviewPanel!
			
			
			//TODO: do I need compare content, content may be very long, every key type do compare maybe not good idea....
			ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), PlainEditor.this);
		}
		
	}

}
