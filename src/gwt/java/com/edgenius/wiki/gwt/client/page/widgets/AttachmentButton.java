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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 */
public class AttachmentButton extends SimplePanel implements ClickHandler{
	private AttachmentPanel attPanel;
	private ClickLink clipLink = new ClickLink(Msg.consts.attachment());
	
	public AttachmentButton(AttachmentPanel attPanel){
		this.attPanel = attPanel;
		
		FlowPanel panel = new FlowPanel();
//		Image clip = IconBundle.I.get().attach().createImage();
//		clip.addClickHandler(this);
//		panel.add(clip);
		
		DOM.setElementAttribute(clipLink.getElement(), "white-space", "nowrap! important");
		DOM.setElementAttribute(clipLink.getElement(), "whiteSpace", "nowrap! important");
		clipLink.addClickHandler(this);
		setCount(0);
		panel.add(clipLink);
		this.setWidget(panel);
		
		this.addStyleName(Css.ATT_BTN);
	}
	public void setCount(int attsCount){
		
		clipLink.setText(Msg.consts.attachment() + "("+attsCount+")"); 
	}
	public void onClick(ClickEvent event) {
		attPanel.setVisible(!attPanel.isVisible());
	}
}
