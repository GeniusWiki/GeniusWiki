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

import com.edgenius.wiki.gwt.client.KeyCaptureListener;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Work together with jQuery auto-resize plugin to create auto-resize textarea. 
 * 
 * Please use setFocus() to invoke if switch back from invisible deck.
 * 
 * @author Dapeng.Ni
 */
public class AutoResizeTextArea extends TextArea{
	private String id;
	
	public AutoResizeTextArea(){
		super();
		id = HTMLPanel.createUniqueId();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				DOM.setElementAttribute(AutoResizeTextArea.this.getElement(), "id",id);
				initAutoResize(id);
			}
		});
		this.addFocusHandler(KeyCaptureListener.instance());
		this.addBlurHandler(KeyCaptureListener.instance());
	}
	
	//call native jQuery method to initial auto resize plugin
	private native void initAutoResize(String id)/*-{
	    $wnd.$("textarea#"+id).autoResize({
		    onResize : function() {
		        $wnd.$(this).css({opacity:0.8});
		    },
		    animateCallback : function() {
		        $wnd.$(this).css({opacity:1});
		    },
		    animateDuration : 300,
		    extraSpace : 40
		});
	}-*/;
}
