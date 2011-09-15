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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class ClickLink extends Widget implements HasHTML, HasClickHandlers, HasMouseOverHandlers, HasMouseOutHandlers, HasMouseMoveHandlers {
	private Element anchorElem;

	private Object object;

	public ClickLink() {
		setElement(DOM.createDiv());
		DOM.appendChild(getElement(), anchorElem = DOM.createAnchor());
		sinkEvents(Event.ONCLICK);
		sinkEvents(Event.ONMOUSEMOVE);
		sinkEvents(Event.ONMOUSEOUT);
		sinkEvents(Event.ONMOUSEOVER);
		setStyleName("gwt-Hyperlink");
		DOM.setElementAttribute(anchorElem, "href", "javascript:;");
	}

	/**
	 * return <a> tag element
	 * 
	 * @return
	 */
	public Element getLinkElement() {
		return anchorElem;
	}

	public ClickLink(String text) {
		this(text, false);
	}

	public ClickLink(String text, boolean ashtml) {
		this();
		if (ashtml)
			setHTML(text);
		else
			setText(text);
	}

	// ********************************************************************
	// Override methods
	// ********************************************************************
	public String getHTML() {
		return DOM.getInnerHTML(anchorElem);
	}

	public void setHTML(String html) {
		DOM.setInnerHTML(anchorElem, html);

	}

	public String getText() {
		return DOM.getInnerText(anchorElem);
	}

	public void setText(String text) {
		DOM.setInnerText(anchorElem, text);

	}


	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return this.addHandler(handler, ClickEvent.getType());
	}
	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		return this.addHandler(handler, MouseOverEvent.getType());
	}

	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		return this.addHandler(handler, MouseOutEvent.getType());
	}

	public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
		return this.addHandler(handler, MouseMoveEvent.getType());
	}

	public void onBrowserEvent(Event event) {
		if (DOM.eventGetType(event) == Event.ONCLICK) {
			ClickEvent.fireNativeEvent(event, this);
			DOM.eventPreventDefault(event);
		}else if(DOM.eventGetType(event) == Event.ONMOUSEMOVE) {
			MouseMoveEvent.fireNativeEvent(event, this);
			DOM.eventPreventDefault(event);
		}else if(DOM.eventGetType(event) == Event.ONMOUSEOUT) {
			MouseOutEvent.fireNativeEvent(event, this);
			DOM.eventPreventDefault(event);
		}else if(DOM.eventGetType(event) == Event.ONMOUSEOVER) {
			MouseOverEvent.fireNativeEvent(event, this);
			DOM.eventPreventDefault(event);
		}
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return this.object;
	}



}
