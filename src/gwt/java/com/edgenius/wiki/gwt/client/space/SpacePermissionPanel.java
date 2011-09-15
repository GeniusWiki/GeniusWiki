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
package com.edgenius.wiki.gwt.client.space;

import com.edgenius.wiki.gwt.client.widgets.LazyLoadingPanel;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Show space security and group users list which is for display the users in a group.
 *  
 * @author Dapeng.Ni
 */
public class SpacePermissionPanel extends SimplePanel implements LazyLoadingPanel { 
	private DeckPanel deck = new DeckPanel();
	private SpaceGroupUsersPanel groupUsersPanel;
	private SpaceSecurityPanel securityPanel;
	
	public SpacePermissionPanel(String resourceName){
		securityPanel = new SpaceSecurityPanel(resourceName,this);
		groupUsersPanel = new SpaceGroupUsersPanel(this);
		deck.insert(securityPanel, 0);
		deck.insert(groupUsersPanel, 1);
		deck.showWidget(0);
		
		this.setWidget(deck);
	}
	public void showGroupUsers(String senderSpace, String receiverSpace) {
		groupUsersPanel.onLoad(senderSpace, receiverSpace);
		deck.showWidget(1);
	}
	public void showSpaceSecurity() {
		
		deck.showWidget(0);
	}
	public void load() {
		securityPanel.load();
	}
	
}
