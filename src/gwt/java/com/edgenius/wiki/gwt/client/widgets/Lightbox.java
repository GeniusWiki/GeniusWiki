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

import java.util.List;

import com.edgenius.wiki.gwt.client.Css;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
/**
 * @author dapeng
 */
public class Lightbox {

	private PopupPanel background;
	private PopupPanel popup;
	private HandlerRegistration evtReg;
	private UIObject owner;

	/**
	 * Only show background mask on owner scope. if owner is null, then it is entire page scope. 
	 * @param owner
	 * @param popup
	 */
	public Lightbox(UIObject owner, final PopupPanel popup) {
		this.popup = popup;
		this.owner = owner;
		background = new PopupPanel();
		background.setStyleName(Css.LIGHT_BOX_BK);
		
		if(owner == null){
			DOM.setStyleAttribute(background.getElement(), "width", "100%");
			DOM.setStyleAttribute(background.getElement(), "height", "5000px"); //Window.getClientHeight()

			evtReg = Window.addResizeHandler(new ResizeHandler() {
				public void onResize(ResizeEvent event) {
					//background need be adjust size, but popup won't display if it is not showing. 
					if(popup.isShowing()){
						popup.center();
						if(popup instanceof DialogBox){
							List<DialogListener> listeners = ((DialogBox)popup).getDialogListeners();
							if(listeners != null){
								for (DialogListener listener : listeners) {
									listener.dialogRelocated((DialogBox)popup);
								}
							}
						}
					}
				}
			});
		}else{
			background.setPopupPosition(owner.getAbsoluteLeft(), owner.getAbsoluteTop());
			DOM.setStyleAttribute(background.getElement(), "width", owner.getOffsetWidth()+"px");
			DOM.setStyleAttribute(background.getElement(), "height", owner.getOffsetHeight()+"px");
		}
		
	}
	public Lightbox(final PopupPanel popup) {
		this(null,popup);
	}
	/**
	 * Do not call this method in inherit popup.show()
	 */
	public void show(){
		if(owner == null){
			background.center();
		}else{
			background.show();
		}
		popup.center();
	    
	}
	public void hide(){
        popup.hide();
        background.hide();
        
		if (evtReg != null) {
			evtReg.removeHandler();
			evtReg = null;
		}

	}
	

}
