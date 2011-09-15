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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 */
public class ToggleLink extends SimplePanel implements HasClickHandlers{
	private static final int ENABLE = 0;
	private static final int DISALBE = 1;
	private DeckPanel deck = new DeckPanel();
	private boolean enable;
	public ToggleLink(Image enableImg, Image disableImg, String enableMsg, String disableMsg) {
		FlowPanel ePanel = new FlowPanel();
		if(enableImg != null){
			ePanel.add(enableImg);
			enableImg.setStyleName(Css.ICON);
		}
		ClickLink eLink = new ClickLink(enableMsg);
		eLink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				setEnable(false);
				ToggleLink.this.fireEvent(event);
			}
		});
		eLink.addStyleName(Css.ENABLE);
		ePanel.add(eLink);
		
		FlowPanel dPanel = new FlowPanel();
		if(disableImg != null){
			dPanel.add(disableImg);
			disableImg.setStyleName(Css.ICON);
		}
		ClickLink dLink = new ClickLink(disableMsg);
		dLink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				setEnable(true);
				ToggleLink.this.fireEvent(event);
			}

		});
		dPanel.add(dLink);
		dLink.addStyleName(Css.DISABLE);
		
		deck.insert(ePanel, ENABLE);
		deck.insert(dPanel, DISALBE);
		deck.showWidget(ENABLE);
		enable = true;
		
		this.setWidget(deck);
		this.setStyleName(Css.TOGGLE_LINK);
	}

	public void setEnable(boolean enable){
		this.enable = enable;
		if(enable)
			deck.showWidget(ENABLE);
		else
			deck.showWidget(DISALBE);
	}
	
	public boolean isEnable(){
		return enable;
	}

	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return this.addHandler(handler, ClickEvent.getType());
	}
}
