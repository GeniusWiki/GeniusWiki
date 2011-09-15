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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Dapeng.Ni
 */
public class Button extends com.google.gwt.user.client.ui.Button{

	private Element txtSpan = null;
	private Image defaultIcon;
	private Image currentIcon;
	public Button(String html){
		super(html);
	}
	/**
	 * @param html
	 */
	public Button(String html, Image icon) {
		super(html);
		
		setIcon(icon);
		
	}

	public void setHTML(String text){
		setText(text);
	}
	public void setText(String text){
		Element child = DOM.getChild(this.getElement(), 0);
		int txtIdx = 0;
		if(child != null && child.toString().indexOf("img") != -1){
			//first is image, then try to put text after it
			txtIdx = 1;
			txtSpan = DOM.getChild(this.getElement(), txtIdx);
		}
		
		if(txtSpan == null){
			txtSpan = DOM.createSpan();
			DOM.setInnerHTML(txtSpan,text);
		}
		DOM.insertChild(this.getElement(),txtSpan, txtIdx);
	}
	/**
	 * Please note, don't use this method to busy indicator image! Otherwise this button default icon will be overwrite.
	 * Use setBusy() method instead.
	 * 
	 * @param createImage
	 */
	public void setIcon(Image newIcon) {
		this.setIcon(newIcon, true);
	}

	public void setEnabled(boolean enable) {
		this.setBusy(!enable);
	}
	public void setBusy(boolean busy) {
		super.setEnabled(!busy);
		
		if(busy){
			if(this.defaultIcon != null){
				this.setIcon(IconBundle.I.indicator(), false);
			}
		}else{
			this.setIcon(this.defaultIcon, true);
		}
	}

	private void setIcon(Image newIcon, boolean isDefault){
		if(newIcon != null){
			if(currentIcon != null)
				DOM.removeChild(this.getElement(), currentIcon.getElement());
			
			this.currentIcon = newIcon;
			if(isDefault){
				this.defaultIcon = newIcon;
			}
			newIcon.removeFromParent();
			if(txtSpan != null)
				DOM.insertBefore(this.getElement(),newIcon.getElement(),txtSpan);
			else
				DOM.appendChild(this.getElement(), newIcon.getElement());
		}
	}
}
