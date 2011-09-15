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

import java.util.List;

import com.edgenius.wiki.gwt.client.ElementRequester;
import com.edgenius.wiki.gwt.client.ElementRequesterCallback;
import com.edgenius.wiki.gwt.client.model.TreeItemListModel;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;

/**
 * Only for space short name (spaceUname)
 * @author Dapeng.Ni
 */
public class SpaceSuggestBox extends SimplePanel implements ElementRequesterCallback, HasText, KeyDownHandler{
	private HintTextBox box = new HintTextBox();
	private	MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
	private SuggestBox suggest = new SuggestBox(oracle,box);
	private String queryText;
	private ElementRequester requester = new ElementRequester(null);

	//if hasDefaultSpace is true, then blank is treat as current space
	public SpaceSuggestBox(String hintText){
		if(hintText != null){
			box.setHint(hintText);
		}
		requester.addCallback(this);
		suggest.addKeyDownHandler(this);
		
		this.setWidget(suggest);
	}

	
	public void spaceUnameList(List<String> spaces) {
		oracle.clear();
		oracle.addAll(spaces);
	}

	public void spaceUnameListRequestFailed(String errorCode) {
		oracle.clear();
	}

	public void setStyleName(String style){
		box.setStyleName(style);
	}

	
	//can not use spaceBox.onLostFocus() to decide when get PageList, as spaceBox is suggest box, a popup suggest box
	//will trigger the focus event, but at the moment, the space input not finish yet...
//	public void addFocusListener(FocusListener listener) {
//		box.addFocusListener(listener);
//	}
	
	public String getText() {
		return box.getText();
	}
	public void setText(String text) {
		box.setText(text);
	}
	
	//not need implemented
	public void pageTitleList(String spaceUname, List<String> titles) {}
	public void pageTitleListRequestFailed(String errorCode) {}
	public void pageTree(TreeItemListModel model) {}
	public void pageTreeRequestFailed(String errorCode) {}

	/**
	 * @param instance
	 */
	public void addBlurHandler(BlurHandler listener) {
		suggest.getTextBox().addBlurHandler(listener);
	}
	public void addFocusHandler(FocusHandler listener) {
		suggest.getTextBox().addFocusHandler(listener);
	}


	/**
	 * If system has large volume spaces, such in in geniuswiki.com, it is bottle neck if getting 
	 * space list when this suggest box just display.   
	 */
	public void onKeyDown(KeyDownEvent event) {
		String input = StringUtil.trimToEmpty(suggest.getTextBox().getText());
		if(!StringUtil.equalsIgnoreCase(input, queryText) && input.length() > 0){
			queryText = input;
			requester.needSpaceUnameList(queryText);
		}
	}

}
