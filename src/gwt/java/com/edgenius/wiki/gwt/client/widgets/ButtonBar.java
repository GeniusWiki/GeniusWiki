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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * @author Dapeng.Ni
 */
public class ButtonBar extends AbsolutePanel {
	private int style;
	
	public static final int LEFT = 1;
	public static final int CENTER = 1<<1;
	public static final int RIGHT = 1<<2;
	
	public static final int NEGATIVE = 1<<3;
	public static final int NORMAL = 1<<4;
	public static final int POSITIVE = 1<<5;
	
	public ButtonBar(){
		this(ButtonBar.RIGHT|ButtonBar.NORMAL);
		
	}
	
	public ButtonBar(int style){
		this.style = style;
		
		this.setWidth("100%");
		
		if((style & ButtonBar.NEGATIVE) > 0
			|| (style & ButtonBar.POSITIVE) > 0){
			this.addStyleName(Css.BUTTONS);
		}else{
			this.addStyleName(Css.SMALL_BUTTONS);
		}
		if((style & ButtonBar.CENTER) > 0)
			DOM.setElementAttribute(this.getElement(), "align", "center");
		else if((style & ButtonBar.LEFT) > 0)
			DOM.setElementAttribute(this.getElement(), "align", "left");
		else if((style & ButtonBar.RIGHT) > 0)
			DOM.setElementAttribute(this.getElement(), "align", "right");
		
		DOM.setElementAttribute(this.getElement(), "margin", "8px");
	}
	
	
	public void add(Widget btn){
		
		super.add(btn);

		if((style & ButtonBar.NORMAL) > 0){
			btn.setStyleName(null);
		}else if((style & ButtonBar.NEGATIVE) > 0){
			btn.setStyleName(Css.NEGATIVE);
		}else{
			btn.setStyleName(Css.POSITIVE);
		}
	}
}
