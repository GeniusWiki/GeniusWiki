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
package com.edgenius.wiki.gwt.client;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;

/**
 * All text box in View Model (or any other model which require single keyCode as shortcut) need 
 * implements this method, so that single keyCode will pass into textbox rather than treat as shortcut.
 * @author Dapeng.Ni
 */
public class KeyCaptureListener implements FocusHandler, BlurHandler {
	public static boolean globalCapture = true;
	private static KeyCaptureListener listener = new KeyCaptureListener();
	static{
		bindJsMethod();
	}
	public void onFocus(FocusEvent event) {
		globalCapture = false;
		
	}
	public void onBlur(BlurEvent event) {
		globalCapture = true;
	}

	public static KeyCaptureListener instance() {
		return listener;
	}

	public static void setCapture(boolean capture){
		globalCapture = capture;
	}
	private static native void bindJsMethod()/*-{
		$wnd.gwtShortcutKeyCapture = function(capture) {
			@com.edgenius.wiki.gwt.client.KeyCaptureListener::setCapture(Z)(capture);
		};
	}-*/;

}
