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

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 */
public class PrettyUrlButton extends SimplePanel implements ClickHandler{
	private PrettyUrlPanel pUrlPanel;
	private boolean pined = false;
	
	public PrettyUrlButton(PrettyUrlPanel purlPanel){
		this.pUrlPanel = purlPanel;
		
		FlowPanel panel = new FlowPanel();
		ClickLink clipLink = new ClickLink(Msg.consts.pretty_url());
		clipLink.addClickHandler(this);
//		clipLink.addMouseOutHandler(this);
//		clipLink.addMouseOverHandler(this);
		
		panel.add(clipLink);
		
		this.addStyleName(Css.ATT_BTN);
		this.setWidget(panel);
	}
	
	@Override
	public void setVisible(boolean visible){
		//this does not mean prettyURLPanel is show up - it is onClick()
		if(visible){
			pUrlPanel.refresh();
		}else{
			//if button invisible, then prettyURLPanel must be invisible
			pUrlPanel.setVisible(false);
		}
		super.setVisible(visible);
	}
	
	public void onClick(ClickEvent event) {
		boolean visible = !pUrlPanel.isVisible();
		pUrlPanel.setVisible(visible);
		pined = visible;
	}

//	public void onMouseOut(MouseOutEvent event) {
//		//if not pin, and visible, then hide it.
//		if(pUrlPanel.isVisible() && !pined){
//			pUrlPanel.setVisible(false);
//		}
//		
//	}
//
//	public void onMouseOver(MouseOverEvent event) {
//		//show it
//		if(!pUrlPanel.isVisible()){
//			pUrlPanel.setVisible(true);
//		}
//	}
}
