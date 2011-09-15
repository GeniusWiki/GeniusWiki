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
package com.edgenius.wiki.gwt.client.page;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.PageAttribute;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.URLTabPanel;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 */
public class PageTabPanel extends SimplePanel implements BeforeSelectionHandler<Integer>, SelectionHandler<Integer>, PinPanelListener{
	public static String TABS_ANCHOR_NAME = HTMLPanel.createUniqueId();
	
	private static final String COMMENT_HEADER_TEXT = Msg.consts.comments();
	private static final String HISTORY_HEADER_TEXT = Msg.consts.history();
	private static final String CHILDREN_HEADER_TEXT = Msg.consts.children();
	private static final Image commentTitleImg = new Image(IconBundle.I.get().comment());;
	private static final Image historyTitleImg = new Image(IconBundle.I.get().history());
	private static final Image childrenTitleImg  = new Image(IconBundle.I.get().tree());
	
	private int tabCommentIdx = -1;
	private int tabHistoryIdx = -1;
	private int tabChildrenIdx = -1;
	
	private boolean allowVisibleOnHistory = false;
	private boolean allowVisibleOnComment = false;
	private boolean allowVisibleOnChildren = false;
	private HistoryPanel historyPanel;
	private CommentPanel commentPanel;
	private ChildrenPanel childrenPanel;
	
	private URLTabPanel tabPanel = new URLTabPanel();
	private PageMain main;
	
	
	public PageTabPanel(PageMain main){
		this.main = main;
		historyPanel = new HistoryPanel(main);
		commentPanel = new CommentPanel(main,this);
		childrenPanel = new ChildrenPanel(main);
		
		historyPanel.addPinPanelListener(this);
		commentPanel.addPinPanelListener(this);
		childrenPanel.addPinPanelListener(this);
		
		tabPanel.addBeforeSelectionHandler(this);
		tabPanel.addSelectionHandler(this);
		
		//style:
		tabPanel.setStyleName(Css.PAGE_TAB_BAR);
		
		//deselect all, it also will hide all panel
		tabPanel.setSelected(-1,false);
		
		FlowPanel panel = new FlowPanel();
		panel.add(tabPanel);
		panel.add(new HTML("<a href=\"#\" id=\""+TABS_ANCHOR_NAME+"\"></a>"));
		this.setWidget(panel);
	}

	public void clear(){
		tabPanel.clear();
	}

	public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
		Integer index = event.getItem();
		if(index == tabCommentIdx){
			if(allowVisibleOnComment){
				//try to switch panel as busy so that when the tab is set to visible, it won't display the old content.
				//ie, showBusy()->showTab()->loadContent
				commentPanel.showBusy(true);
			}
		}else if(index == tabHistoryIdx){
			if(allowVisibleOnHistory){
				historyPanel.showBusy(true);
			}
		}else if(index == tabChildrenIdx){
			if(allowVisibleOnChildren){
				childrenPanel.showBusy(true);
			}
		}
	}
	public void onSelection(SelectionEvent<Integer> event) {
		Integer index = event.getSelectedItem();
		if(index == tabCommentIdx){
			if(allowVisibleOnComment){
				//refresh from Server side if force refresh or switch from other tab
				commentPanel.loadComment();
			}
		}else if(index == tabHistoryIdx){
			if(allowVisibleOnHistory){
				historyPanel.loadHistory();
			}
		}else if(index == tabChildrenIdx){
			if(allowVisibleOnChildren){
				childrenPanel.load();
			}
		}
	}
	

	/**
	 * A tab panel will refresh the current visible tab content if a new page fill in. 
	 * @param commentWrite 
	 * @param commentRead 
	 */
	public void reset(int commentRead, int tab) {
		tabPanel.clear();
		
		//does not allow read or page attribute declares no comment, then clear and hide tag
		if(commentRead == SharedConstants.FORBID || GwtUtils.contains(main.getPageAttribute(),PageAttribute.NO_COMMENT)){
			allowVisibleOnComment = false;
			tabCommentIdx = -1;
		}else{
			//lazyLoading is implemented far later than current lazying loading way, so here don't change old style TODO
			tabCommentIdx = tabPanel.addItem(commentPanel, COMMENT_HEADER_TEXT,commentTitleImg, false);
			allowVisibleOnComment = true;
		}
		if(GwtUtils.contains(main.getPageAttribute(),PageAttribute.NO_HISTORY)){
			allowVisibleOnHistory = false; 
			tabHistoryIdx = -1;
		}else{
			//lazyLoading is implemented far later than current lazying loading way, so here don't change old style TODO
			tabHistoryIdx = tabPanel.addItem(historyPanel,HISTORY_HEADER_TEXT,historyTitleImg, false);
			allowVisibleOnHistory = true; 
		}
		if(GwtUtils.contains(main.getPageAttribute(),PageAttribute.NO_CHILDREN)){
			allowVisibleOnChildren = false; 
			tabChildrenIdx = -1;
		}else{
			//lazyLoading is implemented far later than current lazying loading way, so here don't change old style TODO
			tabChildrenIdx= tabPanel.addItem(childrenPanel,CHILDREN_HEADER_TEXT,childrenTitleImg, false);
			allowVisibleOnChildren = true; 
		}
		
		
		//input tab parameter is fixed value, need change according to displayed tab index
		if(tab == SharedConstants.TAB_TYPE_COMMENT){
			tab = tabCommentIdx;
		}else if(tab == SharedConstants.TAB_TYPE_HISTORY){
			tab = tabHistoryIdx;
		}else if(tab == SharedConstants.TAB_TYPE_CHILDREN){
			tab = tabChildrenIdx;
		}
		//must fire event so that the panel can be reload
		tabPanel.setSelected(tab, true);
		
		if(allowVisibleOnComment || allowVisibleOnChildren){
			//only load comment and children number, ignore history - looks useless
			loadCounter(allowVisibleOnComment , allowVisibleOnChildren);
		}
	}	
	/**
	 * load how many comments/children on current page 
	 */
	private void loadCounter(boolean allowVisibleOnComment, boolean allowVisibleOnChildren) {
		PageControllerAsync action = ControllerFactory.getPageController();
		action.getPageTabCount(main.getSpaceUname(),main.getPageUuid(),allowVisibleOnComment,allowVisibleOnChildren, new CounterAsync());
	}
	void resetCommentCount(String count){
		tabPanel.resetTabTitle(tabCommentIdx, COMMENT_HEADER_TEXT+count, commentTitleImg);
	}
	void resetChildrenCount(String count){
		tabPanel.resetTabTitle(tabChildrenIdx, CHILDREN_HEADER_TEXT+count, childrenTitleImg);
	}
	/**
	 * 
	 */
	public void newComment() {
		
		if(!allowVisibleOnComment)
			//does not allow comment
			return;
		
		tabPanel.setSelected(tabCommentIdx, true);
		
		//add new textarea box to ready for inputing
		commentPanel.addBox(null);	
	}
	
	public void toggleComment() {
		if(allowVisibleOnComment || tabPanel.getSelectedIndex() != tabCommentIdx){
			tabPanel.setSelected(tabCommentIdx, true);
		}else{
			tabPanel.setSelected(-1, false);
		}
		
	}

	public void toggleHistory() {
		if(allowVisibleOnHistory && tabPanel.getSelectedIndex() != tabHistoryIdx){
			//show history: fireEvent will ask loading history
			tabPanel.setSelected(tabHistoryIdx, true);
		}else{
			tabPanel.setSelected(-1, false);
		}
	}
	public void toggleChildren() {
		if(allowVisibleOnChildren && tabPanel.getSelectedIndex() != tabChildrenIdx){
			tabPanel.setSelected(tabChildrenIdx, true);
		}else{
			tabPanel.setSelected(-1, false);
		}
	}

	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//               Implementation of PinPanelListener
	public void close() {
		tabPanel.setSelected(-1, false);
	}

	public void pin(boolean on) {
		
	}
	private class CounterAsync implements AsyncCallback<Integer[]>{
		public void onFailure(Throwable error) {
			//reset tab header to zero?
			
			resetCommentCount("");
			resetChildrenCount("");
			GwtClientUtils.processError(error);
		}
		public void onSuccess(Integer[] counts) {
			resetCommentCount("");
			resetChildrenCount("");
			if(counts == null || counts.length != 2){
				return;
			}
			//update tab header.
			if(counts[0] != null && counts[0] != -1)
				resetCommentCount(" ("+counts[0]+")");
			if(counts[1] != null && counts[1] != -1)
				resetChildrenCount(" ("+counts[1]+")");
		}
	}

}
