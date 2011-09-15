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
package com.edgenius.wiki.gwt.client.page.widgets;

import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.page.PageTreePanel;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
/**
 * @author Dapeng.Ni
 */
public class LocationButton extends SimplePanel implements ClickHandler {

	private PageMain main;
	private PageTreePanel pageTreePanel;
	private boolean showText;
	
	public LocationButton(PageMain main, boolean showText) {
		this.main = main;
		HorizontalPanel panel = new HorizontalPanel();
		this.showText = showText;
		
		if(!showText){
			Image tree = new Image(IconBundle.I.get().tree());
			tree.addClickHandler(this);
			panel.add(tree);
			panel.setCellVerticalAlignment(tree, HasVerticalAlignment.ALIGN_BOTTOM);
		}else{
			ClickLink locationBtn = new ClickLink(Msg.consts.view_page_tree());
			locationBtn.setStyleName(Css.LARGE_LINK_BTN);
			locationBtn.addClickHandler(this);
			panel.add(locationBtn);
		}
		
		
		pageTreePanel = new PageTreePanel(main);
		main.setSideMenuPanel(ClientConstants.LEFT,pageTreePanel);
		main.setSidebarButtonVisible(ClientConstants.LEFT, true);
		this.setWidget(panel);
	}

	public void onClick(ClickEvent event) {
		if(main.toggleSideMenu(ClientConstants.LEFT)){
			refresh(main.getSpaceUname(), main.getPageUuid());
			if(showText){
				//this is trick - if show text, it will be on Children Tab panel, so try to go page top
				GwtClientUtils.gotoAnchor(PageMain.PAGE_TOP_ANCHOR_NAME);
			}
		}
		
	}

	public void refresh(String spaceUname, String pageUuid) {
		
		pageTreePanel.refreshTreeItem(spaceUname,pageUuid);
		
	}

	/**
	 * @param pageUuid
	 */
	public void setSelection(String pageUuid) {
		pageTreePanel.setSelection(pageUuid);
	}
}
