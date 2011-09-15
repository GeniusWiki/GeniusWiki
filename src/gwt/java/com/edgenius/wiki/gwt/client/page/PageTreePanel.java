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

import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.page.widgets.PageTreeWidget;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * @author Dapeng.Ni
 */
public class PageTreePanel extends PinPanel implements SelectionHandler<TreeItem>{
	private PageTreeWidget tree;
	
	public PageTreePanel(PageMain main){
		super(main);
		//page tree
		TreeItem root = new TreeItem();
		root.setUserObject("-1");
		//XXX:hardcode
		root.setText(Msg.consts.dashboard());
		
		tree = new PageTreeWidget(root);
		tree.addSelectionHandler(this);
		
		//I don't use scroll panel as it always display the vertical scrollbar
		FlowPanel panel = new FlowPanel();
		panel.add(tree);
		panel.setWidth(ClientConstants.LEFT_SIDE_MENU_WIDTH+"px");
		DOM.setStyleAttribute(panel.getElement(), "overflowY", "hidden");
		DOM.setStyleAttribute(panel.getElement(), "overflowX", "auto");
		//this style is important for IE, otherwise, overflow X text will be displayed outside.
		DOM.setStyleAttribute(panel.getElement(), "position", "relative");
		content.add(panel);
		
		header.add(tree.getFunctionButtons(), DockPanel.EAST);
		DOM.setStyleAttribute(this.getElement(), "minHeight", (Window.getClientHeight() - ClientConstants.DEFAULT_MENU_TOP) +"px");
		
	}
	public void onSelection(SelectionEvent<TreeItem> event) {
//		view page by page title (and current space uname)
		String selectPageUuid = (String) event.getSelectedItem().getUserObject();
		if("-1".equals(selectPageUuid)){
			//dashboard, url redirect to home page of instance
			History.newItem("");
		}else{
			//some pages, !!! getSpaceUname from tree, which is corresponding with your click tree item!
			History.newItem(GwtUtils.getSpacePageToken(tree.getSpaceUname(),event.getSelectedItem().getText()));
		}
		
		//if current page is scrolled down, we needs go to top of page so that use can see new page from beginning. 
		GwtClientUtils.gotoAnchor(PageMain.PAGE_TOP_ANCHOR_NAME);
	}

	public void setSelection(String pageUuid){
		tree.setSelection(pageUuid);
	}
	/**
	 * @param model
	 */
	public void refreshTreeItem(String spaceUname, String pageUuid) {
		//XXX: if don't refresh here, must update PageMain.refreshTreeItem() as well.
		//TODO: need performance optimize: always refresh from server side? although server side use cache?!
		//refresh from server side to avoid other users already create new page/update page title.
		tree.refreshTree(spaceUname, pageUuid);
	}


}
