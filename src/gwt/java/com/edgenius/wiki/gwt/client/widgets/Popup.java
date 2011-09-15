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

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.KeyCaptureListener;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;

/**
 * This popup extends following functionality:
 * 1. Allow user uses "ESC" key type to close.
 * 2. Allow mouse move in/out to automatic show/hide popup
 * 3. Only one popup is allowed in current web browser page.
 *  
 * @author Dapeng.Ni
 */
public class Popup extends PopupPanel implements NativePreviewHandler, CloseHandler<PopupPanel>, 
	HasMouseOutHandlers, MouseOutHandler, MouseOverHandler, HasMouseOverHandlers{

	private PopupPanel background;
	private FlowPanel topc;
	private FlowPanel centrec;
	private FlowPanel bottomc;
	private FlowPanel centre;
	
	private HandlerRegistration evtReg;
	private boolean mouseHover = false;
	private Timer hideTimer;
	private Timer showTimer;
	private UIObject target;
	private boolean singletonShowup;
	
	//wait milliseconds to pop when mouse is hover on parent
	private int autoPopDeplyTime = 800;
	//hide popup after milliseconds after mouse move out both parent and popup 
	private int autoHideDeplyTime = 1000;
	
	private static List<Popup> showup = new ArrayList<Popup>();
	
	/**
	 * Default - no mouse move in/out showup, no signleton
	 * @param target
	 */
	public Popup(UIObject target){
		
		this(target, false, false, false);
	}
	public Popup(UIObject target, boolean moveOutAutoHide, boolean singletonShowup, boolean hasBackground){
		super(true);
		this.target = target;
		this.singletonShowup = singletonShowup;
		if(hasBackground){
			this.background = new PopupPanel();
			topc = new FlowPanel();
			centrec = new FlowPanel();
			bottomc = new FlowPanel();
			centre = new FlowPanel();
			
			FlowPanel topl = new FlowPanel();
			topl.setStyleName("xstl");
			topc.setStyleName("xstc");
			FlowPanel topr = new FlowPanel();
			topr.setStyleName("xstr");
			FlowPanel top = new FlowPanel();
			top.setStyleName("xst");
			top.add(topl);
			top.add(topc);
			top.add(topr);
			
			FlowPanel centrel = new FlowPanel();
			centrel.setStyleName("xscl");
			centrec.setStyleName("xscc");
			FlowPanel centrer = new FlowPanel();
			centrer.setStyleName("xscr");
			centre.setStyleName("xsc");
			centre.add(centrel);
			centre.add(centrec);
			centre.add(centrer);
			
			FlowPanel bottoml = new FlowPanel();
			bottoml.setStyleName("xsbl");
			bottomc.setStyleName("xsbc");
			FlowPanel bottomr = new FlowPanel();
			bottomr.setStyleName("xsbr");
			FlowPanel bottom = new FlowPanel();
			bottom.setStyleName("xsb");
			bottom.add(bottoml);
			bottom.add(bottomc);
			bottom.add(bottomr);
			
			FlowPanel shadowPanel = new FlowPanel();
			shadowPanel.add(top);
			shadowPanel.add(centre);
			shadowPanel.add(bottom);
			
			background.setStyleName("x-shadow");
			background.add(shadowPanel);
		}
		this.setStyleName(Css.POPUP);
		this.addCloseHandler(this);
		
		if(moveOutAutoHide){
			if(target instanceof HasMouseOutHandlers && target instanceof HasMouseOverHandlers){
				sinkEvents(Event.ONMOUSEOUT);
				sinkEvents(Event.ONMOUSEOVER);
				this.addMouseOverHandler(this);
				this.addMouseOutHandler(this);
				((HasMouseOutHandlers) target).addMouseOutHandler(this);
				((HasMouseOverHandlers) target).addMouseOverHandler(this);
			}else{
				if(Log.isDebugEnabled()){
					Window.alert("Moveout auto hide target must implement MouseOutHandler and MouseOverHandler");
					Log.error("Moveout auto hide target must implement MouseOutHandler and MouseOverHandler");
				}
			}
		}
	}
	/**
	 * Please use pop() instead!!! 
	 * Deprecated is just for warning developer don't user this method.
	 */
	@Deprecated
	public void show(){
		super.show();
	}
	public void pop(){
		if(this.isShowing()) return;
		

		if(singletonShowup){
			for (Popup pop : showup) {
				pop.hide();
			}
			showup.clear();
			showup.add(this);
		}
		
		
		super.showRelativeTo(target);
		//must after popup displayed
		if(background != null){
			resized();
			DOM.setStyleAttribute(background.getElement(), "zIndex", "1001");
			DOM.setStyleAttribute(this.getElement(), "zIndex", "1002");
			background.show();
		}
		
		if(evtReg != null){
			evtReg.removeHandler();
			evtReg = null;
		}
		KeyCaptureListener.globalCapture = false; 
		evtReg = Event.addNativePreviewHandler(this);
		
	}
	/**
	 * Only useful if popup has background. It will resize background according to new popup size.
	 */
	public void resized() {
		if(background != null){
			background.setPopupPosition(this.getPopupLeft()- 4, this.getPopupTop() + 3);
			int w = this.getOffsetWidth();
			int h = this.getOffsetHeight();
			DOM.setStyleAttribute(background.getElement(), "width", (w + 8) +"px");
			DOM.setStyleAttribute(background.getElement(), "height", (h + 1) +"px");
			DOM.setStyleAttribute(topc.getElement(), "width", (w - 4) +"px");
			DOM.setStyleAttribute(centrec.getElement(), "width", (w - 4) +"px");
			DOM.setStyleAttribute(bottomc.getElement(), "width", (w - 4) +"px");
			DOM.setStyleAttribute(centre.getElement(), "height", (h - 8) +"px");
		}
	}
	
	public void onClose(CloseEvent<PopupPanel> event) {
		if(evtReg != null){
			evtReg.removeHandler();
			evtReg = null;
		}
		KeyCaptureListener.globalCapture = true;
		
		if(background != null){
			background.hide();
		}
	}
	

	public void onPreviewNativeEvent(NativePreviewEvent event){
		int type = event.getTypeInt();
		if(!event.isCanceled()){
	        if(type == Event.ONKEYDOWN){
	            int keyCode = event.getNativeEvent().getKeyCode();
	            // ESC clicked
	            if(keyCode == KeyCodes.KEY_ESCAPE){
	            	this.hide();
	            }
	        }
		}
        super.onPreviewNativeEvent(event);

    }
	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		return this.addHandler(handler, MouseOutEvent.getType());
	}
	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		return this.addHandler(handler, MouseOverEvent.getType());
	}
	
	public void onMouseOver(MouseOverEvent event) {
		this.mouseHover = true;
		if(event.getSource() == target){
			movein();
		}
	}

	
	public void onMouseOut(MouseOutEvent event) {
		this.mouseHover = false;
		moveout();
	}
	private void movein() {
		if(showTimer != null){
			showTimer.cancel();
			showTimer = null;
		}
		
		if(hideTimer != null){
			hideTimer.cancel();
			hideTimer = null;
		}
		if(this.isShowing())
			return;
		
		if(autoPopDeplyTime > 0){
			showTimer = new Timer(){
				@Override
				public void run() {
					if(mouseHover)
						pop();
				}
			};
			showTimer.schedule(autoPopDeplyTime);
		}else{
			pop();
		}
	}
	private void moveout() {
		if(showTimer != null){
			showTimer.cancel();
			showTimer = null;
		}

		
		if(hideTimer != null){
			hideTimer.cancel();
			hideTimer = null;
		}
		if(!this.isShowing())
			return;
		if(autoHideDeplyTime > 0){
			hideTimer = new Timer(){
				@Override
				public void run() {
					if(!mouseHover)
						hide();
				}
			};
			hideTimer.schedule(autoHideDeplyTime);
		}else{
			hide();
		}
	}
	public void setAutoPopDeplyTime(int autoPopDeplyTime) {
		this.autoPopDeplyTime = autoPopDeplyTime;
	}
	public void setAutoHideDeplyTime(int autoHideDeplyTime) {
		this.autoHideDeplyTime = autoHideDeplyTime;
	}
	
}
