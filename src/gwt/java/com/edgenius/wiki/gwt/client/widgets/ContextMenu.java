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

import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.UIObject;

/**
 * @author Dapeng.Ni
 */
public class ContextMenu extends Popup{
	
	private FlowPanel panel = new FlowPanel();
	private List<ContextMenuItem> list = new ArrayList<ContextMenuItem>();

	public ContextMenu(UIObject parent){
		super(parent,true, true, true);
		this.setStyleName("x-menu");
		if(GwtClientUtils.isIE6()){
			this.addStyleName("gwt-ie6");
		}
		panel.setStyleName("x-menu-list");
		this.setWidget(panel);
		
		this.setAutoPopDeplyTime(100);
		this.setAutoHideDeplyTime(2000);
		
	}
	
	public void addItem(ContextMenuItem item){
		list.add(item);
		
		item.setMenu(ContextMenu.this);
		
		panel.add(item);
	}
	public void addSeparator(){
		panel.add(new Sep());
	}

	public void showMenu() {
		this.pop();
	}
	public void pop(){
		super.pop();
		if(GwtClientUtils.isIE()){
		//this require for separator in IE(test in IE7, IE6 looks not necessary) 
			String w = (this.getOffsetWidth() - 6)+"px";
			DOM.setStyleAttribute(panel.getElement(), "width", w);
		}
	}
	public int getItemSize() {
		return list.size();
	}
	
	void moveout(ContextMenuItem item) {
		item.removeStyleName("x-menu-item-active");
		
	}

	void movein(ContextMenuItem item) {
		for (ContextMenuItem m : list) {
			m.removeStyleName("x-menu-item-active");
		}
		item.addStyleName("x-menu-item-active");
		
	}

	//********************************************************************
	//               Private classes
	//********************************************************************
	private static class Sep extends Composite{
		public Sep(){
			HTML html = new HTML();
			html.setHTML("<span class=\"x-menu-sep\">&nbsp;</span>");
			html.setStyleName("x-menu-list-item");
			initWidget(html);
		}
	}
	
}
