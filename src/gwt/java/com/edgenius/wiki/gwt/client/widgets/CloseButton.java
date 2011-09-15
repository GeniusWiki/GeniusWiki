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
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 */
public class CloseButton extends SimplePanel implements MouseOutHandler, MouseOverHandler, HasClickHandlers{


	private Image closeBtn = new Image(IconBundle.I.get().close());
	private Image closeDisBtn = new Image(IconBundle.I.get().closeDisable());
	
	public CloseButton(){
		this.setStyleName(Css.CLOSE_BTN);
		
		closeBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				if(closeBtn.isVisible())
					CloseButton.this.fireEvent(event);
			}
		});
		closeBtn.addMouseOverHandler(this);
		closeBtn.addMouseOutHandler(this);
		closeDisBtn.addMouseOverHandler(this);
		closeDisBtn.addMouseOutHandler(this);
		closeDisBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				if(closeDisBtn.isVisible())
					CloseButton.this.fireEvent(event);
			}
		});
		

		FlowPanel panel = new FlowPanel();
		panel.add(closeDisBtn);
		panel.add(closeBtn);
		closeDisBtn.setVisible(true);
		closeBtn.setVisible(false);
		this.setWidget(panel);
	}
	
	

	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return this.addHandler(handler, ClickEvent.getType());
	}

	public void onMouseOut(MouseOutEvent event) {
		//show disable close
		closeDisBtn.setVisible(true);
		closeBtn.setVisible(false);
	}



	public void onMouseOver(MouseOverEvent event) {
		//show enable close
		closeDisBtn.setVisible(false);
		closeBtn.setVisible(true);
	}



}
