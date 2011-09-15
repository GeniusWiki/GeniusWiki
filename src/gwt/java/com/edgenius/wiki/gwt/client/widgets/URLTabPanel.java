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

import java.util.HashMap;
import java.util.Map;

import com.edgenius.wiki.gwt.client.Css;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.HasBeforeSelectionHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class URLTabPanel extends SimplePanel implements BeforeSelectionHandler<Integer>, SelectionHandler<Integer>, 
		HasBeforeSelectionHandlers<Integer>,HasSelectionHandlers<Integer> {
	private int URL_PANEL_INDEX = -1;
	
	private URLTabBar tabBar = new URLTabBar();
	private DeckPanel deck = new DeckPanel();
	private URLPanel urlPanel= new URLPanel();
	private Map<Integer, String> urlMap = new HashMap<Integer, String>(); 
	private Map<Integer, Integer> deckMap = new HashMap<Integer, Integer>(); 
	private int selected = -1;
	
	private Map<Integer, LazyLoadingPanel> lazy = new HashMap<Integer, LazyLoadingPanel>();
	public URLTabPanel(){
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(tabBar);
		panel.add(deck);
		tabBar.addSelectionHandler(this);
		tabBar.addBeforeSelectionHandler(this);
		
		deck.setStyleName(Css.TAB_DECK);
		panel.setSize("100%", "100%");
		
		this.setStyleName(Css.URL_TAB_PANEL);
		this.setWidget(panel);
	}
	public void clear(){
		urlMap.clear();
		deckMap.clear();
		tabBar.clear();
		deck.clear();
		
		URL_PANEL_INDEX = -1;
		selected = -1;
	}
	public int addItem(Widget widget, String title, boolean lazyLoading){
		return addItem(widget,title,null, lazyLoading);
	}
	
	public int addItem(final String url, String title){
		return addItem(url,title,null);
	}
	
	public int addItem(Widget widget, String title, Image titleIcon, boolean lazyLoading){
		int tabIdx = tabBar.addTab(title, titleIcon);
		deck.add(widget);
		deckMap.put(tabIdx, deck.getWidgetCount()-1);
		
		if(widget instanceof LazyLoadingPanel)
			lazy.put(tabIdx,(LazyLoadingPanel) widget);
		
		return tabIdx;
	}

	public int addItem(final String url, String title, Image titleIcon){
		int tabIdx = tabBar.addTab(title, titleIcon);
		urlMap.put(tabIdx, url);
		
		return tabIdx;
	}

	public void resetTabTitle(int uniqueID, String title, Image titleIcon){
		tabBar.resetTabTitle(uniqueID,title,titleIcon);
	}
	/**
	 * !!! This method must call after the widget is put into HMTL body. Please refer to 
	 * URLPanel.setURL()
	 * 
	 * @param uniqueID
	 * @param fireEvent: TODO: 
	 */
	public void setSelected(int uniqueID, boolean fireEvent) {
		boolean go = true;
		if(fireEvent){
			BeforeSelectionEvent<Integer> evt = BeforeSelectionEvent.fire(this, uniqueID);
			go = evt != null?!evt.isCanceled():false;
		}
		if(go){
			tabBar.select(uniqueID);
			select(uniqueID);
			if(fireEvent)
				SelectionEvent.fire(this, uniqueID);
				
		}
		
	}


	public HandlerRegistration addBeforeSelectionHandler(BeforeSelectionHandler<Integer> handler) {
		return this.addHandler(handler, BeforeSelectionEvent.getType());
	}
	public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler) {
		return  this.addHandler(handler, SelectionEvent.getType());
	}
	/**
	 * The reason call uniqueID rather than tabIdx is,  tabIdx maybe change for each tab if there is
	 * insert etc method. But uniqueID won't. Actually, here uniqueID == tabIdx
	 */
	public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
		//OK,if choose same tab, do nothing 
		Integer uniqueID = event.getItem();
		if(selected == uniqueID){
			event.cancel();
			return;
		}
		
		
		BeforeSelectionEvent.fire(this, uniqueID);
		
		LazyLoadingPanel lazyPanel = lazy.get(uniqueID);
		if(lazyPanel != null){
			lazyPanel.load();
		}
		
	}

	public void onSelection(SelectionEvent<Integer> event) {
		Integer uniqueID = event.getSelectedItem();
		//OK,if choose same tab, do nothing , it is import to improve performance: for example 	commentPanel.loadComment(); 
		//will load if there are tabPanel select event, if user multiple click same tab, it may load multiple times
		if(selected == uniqueID){
			return ;
		}
		
		select(uniqueID);
		
		SelectionEvent.fire(this, uniqueID);
	}

	public int getSelectedIndex() {
		return selected;
	}
	
	/**
	 * @param uniqueID
	 */
	private void select(int uniqueID) {
		selected = uniqueID;
		
		//have to hide entire deck, as I cannot find a method to hide some widget
		deck.setVisible(false);
		if(uniqueID < 0){
			return;
		}
		
		//is it URL?
		String url = urlMap.get(uniqueID);
		if(url != null){
			if(URL_PANEL_INDEX == -1){
				//please note, URL panel must be last one of deck otherwise in Chrome or Safari browser, it has a bug:
				//First go to view some page, then go to space admin from drop down menu, then go back view page, then go to admin again.
				//After above steps, the admin page can not display correctly, the tab link doesn't response, the general page doesn't show logo and functions etc.
				deck.add(urlPanel);
				URL_PANEL_INDEX= deck.getWidgetIndex(urlPanel);
			}
			deck.setVisible(true);
			urlPanel.setURL(url);
			deck.showWidget(URL_PANEL_INDEX);
		}else{
			//OK, should be widget tab, or uniqueID is invalid (-1)
			Integer deckIdx = deckMap.get(uniqueID);
			
			if(deckIdx != null){
				deck.setVisible(true);
				deck.showWidget(deckIdx);
			}
		}
	}

}
