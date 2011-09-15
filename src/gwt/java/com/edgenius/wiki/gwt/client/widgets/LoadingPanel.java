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
import com.edgenius.wiki.gwt.client.page.MessagePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class LoadingPanel extends MessagePanel {

	private Image indicator = new Image(IconBundle.I.get().loadingBar());
	FlowPanel panel = new FlowPanel();
	public LoadingPanel(){
		panel.add(indicator);
		panel.add(message);
		message.setVisible(false);
		indicator.setStyleName(Css.LOADING_INDICATOR);
		this.setStyleName("loading");
		this.setWidget(panel);
	}

	public void addWidget(Widget widget){
		panel.add(widget);
	}
	/**
	 * 
	 */
	public void reset() {
		message.cleanMessage();
		message.setVisible(false);
		indicator.setVisible(true);
	}
}
