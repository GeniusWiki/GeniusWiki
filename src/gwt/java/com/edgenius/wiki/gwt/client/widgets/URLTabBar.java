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

import java.util.Iterator;

import com.edgenius.wiki.gwt.client.Css;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.HasBeforeSelectionHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class URLTabBar extends SimplePanel implements HasBeforeSelectionHandlers<Integer>,HasSelectionHandlers<Integer> {
	private FlowPanel tabBar = new FlowPanel();
	private int uniqueID = 0;
	public URLTabBar(){
		this.setStyleName(Css.TAB_BAR);
		tabBar.setSize("100%", "100%");
		this.setWidget(tabBar);
	}
	
	public void clear(){
		tabBar.clear();
	}
	public int addTab(String title, Image icon){
		FlowPanel tab = new FlowPanel();
		if(icon != null)
			tab.add(icon);
		
		ClickLink link = new ClickLink(title);
		Label label = new Label(title);
		tab.add(link);
		tab.add(label);
		link.setStyleName(Css.TEXT);
		label.setStyleName(Css.TEXT);
		//init status: only show link
		label.setVisible(false);
		//tab.setStyleName(Css.DESELECTED);
		tabBar.add(tab);
		
		//increase uniqueID every new tab
		final int tabIndex = uniqueID++;
		link.setObject(tabIndex);
		
		link.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				BeforeSelectionEvent<Integer> evt = BeforeSelectionEvent.fire(URLTabBar.this, tabIndex);
				if(evt != null && !evt.isCanceled()){
					select(tabIndex);
					SelectionEvent.fire(URLTabBar.this, tabIndex);
					
				}
			}
		});
		
		return tabIndex;
	}
	
	protected void select(int uniqueID) {
		
		for(Iterator<Widget> iter1 = tabBar.iterator();iter1.hasNext();){
			FlowPanel tab = (FlowPanel) iter1.next();
			
			if(getTabUniqueID(tab) == uniqueID){
				//select
				tab.addStyleName(Css.SELECTED);
				tab.removeStyleName(Css.DESELECTED);
				//show label, hide link
				for(Iterator<Widget> iter = tab.iterator();iter.hasNext();){
					Widget w = iter.next();
					if(w instanceof ClickLink){
						w.setVisible(false);
					}else if(w instanceof Label){
						w.setVisible(true);
					}
				}
			}else{
				//de-select
				tab.removeStyleName(Css.SELECTED);
				tab.addStyleName(Css.DESELECTED);
				//show link, hide label
				for(Iterator<Widget> iter = tab.iterator();iter.hasNext();){
					Widget w = iter.next();
					if(w instanceof ClickLink){
						w.setVisible(true);
					}else if(w instanceof Label){
						w.setVisible(false);
					}
				}
			}
		}
	}
	public void resetTabTitle(int uniqueID, String title, Image titleIcon) {
		for(Iterator<Widget> iter1 = tabBar.iterator();iter1.hasNext();){
			FlowPanel tab = (FlowPanel) iter1.next();
			if(getTabUniqueID(tab) == uniqueID){
				for(Iterator<Widget> iter = tab.iterator();iter.hasNext();){
					Widget w = iter.next();
					if(w instanceof ClickLink){
						((ClickLink)w).setText(title);
					}else if(w instanceof Label){
						((Label)w).setText(title);
					}else if(w instanceof Image){
						//remove current image
						iter.remove();
					}
				}
				//add new image to first 
				if(titleIcon != null)
					tab.insert(titleIcon, 0);
				break;
			}
		}
	}


	/**
	 * @param tab
	 * @return
	 */
	private int getTabUniqueID(FlowPanel tab) {
		for(Iterator<Widget> iter = tab.iterator();iter.hasNext();){
			Widget w = iter.next();
			if(w instanceof ClickLink){
				return (Integer)((ClickLink)w).getObject();
			}
		}
		return -1;
	}

	public HandlerRegistration addBeforeSelectionHandler(BeforeSelectionHandler<Integer> handler) {
		return this.addHandler(handler, BeforeSelectionEvent.getType());
	}

	public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler) {
		return this.addHandler(handler, SelectionEvent.getType());
	}
}
