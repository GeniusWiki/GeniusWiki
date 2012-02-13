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
package com.google.gwt.user.client.ui;

import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;

/**
 * This HyperLink can accept Mouse over and out event.
 * @author Dapeng.Ni
 */
public class EventfulHyperLink extends Anchor implements HasMouseOutHandlers, HasMouseOverHandlers{
	
	public EventfulHyperLink(String text, boolean asHTML, String token){
		super(text, asHTML, token);
		this.addStyleName("ajaxlink");
//		sinkEvents(Event.ONMOUSEOUT);
//		sinkEvents(Event.ONMOUSEOVER);
	}
	public EventfulHyperLink(String text, String token){
		this(text, false, token);
	}
	@Override
	public void setStyleName(String style){
		super.setStyleName(style);
		this.addStyleName("ajaxlink");
	}
//
//	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
//		return this.addHandler(handler, MouseOverEvent.getType());
//	}
//
//	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
//		return this.addHandler(handler, MouseOutEvent.getType());
//	}
//	public void onBrowserEvent(Event event) {
//		if(DOM.eventGetType(event) == Event.ONMOUSEOUT) {
//			MouseOutEvent.fireNativeEvent(event, this);
//			DOM.eventPreventDefault(event);
//		}else if(DOM.eventGetType(event) == Event.ONMOUSEOVER) {
//			MouseOverEvent.fireNativeEvent(event, this);
//			DOM.eventPreventDefault(event);
//		}
//	}

}
