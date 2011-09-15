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

import java.util.Iterator;
import java.util.Vector;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.widgets.CloseButton;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public abstract class PinPanel  extends SimplePanel{
	
	protected DockPanel header = new DockPanel();
	protected MessageWidget message = new MessageWidget();
	protected VerticalPanel content = new VerticalPanel();
	protected PageMain main;
	
	private CloseButton closeBtn = new CloseButton();
	private Image loadingImg = IconBundle.I.loading();
	
	private Vector<PinPanelListener> listeners = new Vector<PinPanelListener>();
	
	
	public PinPanel(){
		this(true, null);
	}
	public PinPanel(PageMain main){
		this(true, main);
	}
	public PinPanel(boolean withHeader, PageMain main){
		this.main = main;
		VerticalPanel panel = new VerticalPanel();
		if(withHeader){
			header.add(closeBtn,DockPanel.EAST);
			closeBtn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					for(Iterator<PinPanelListener> iter = listeners.iterator();iter.hasNext();){
						iter.next().close();
					}
				}
			});
			header.setCellWidth(closeBtn, "16px");
			header.setCellHorizontalAlignment(closeBtn, HasHorizontalAlignment.ALIGN_RIGHT);
		
			panel.add(header);
		}
		panel.add(message);
		panel.add(loadingImg);
		panel.add(content);
		
		loadingImg.setVisible(false);
		DOM.setElementAttribute(header.getElement(), "width", "100%");
		content.setWidth("100%");
	    DOM.setElementAttribute(panel.getElement(), "width", "100%");
	    closeBtn.setStyleName(Css.RIGHT);
	    panel.setWidth("100%");
		this.setWidget(panel);
	}
	public void showBusy(boolean busy){
		if(busy){
			loadingImg.setVisible(true);
		}else{
			loadingImg.setVisible(false);
		}
	}
	public void addPinPanelListener(PinPanelListener listener){
		listeners.add(listener);
	}
}
