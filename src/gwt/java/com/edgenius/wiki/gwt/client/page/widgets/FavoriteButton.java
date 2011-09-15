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
package com.edgenius.wiki.gwt.client.page.widgets;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.ImageToggleButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Dapeng.Ni
 */
public class FavoriteButton extends Composite  implements AsyncCallback<Boolean>, ClickHandler{

	private ImageToggleButton favorite = new ImageToggleButton(
			new Image(IconBundle.I.get().favorite()),
					new Image(IconBundle.I.get().nonfavorite()),false);
	private boolean enable;
	
	public FavoriteButton(){
		this(true);
	}
	public FavoriteButton(boolean showText){
		//depends login or not to show this button
		favorite.setOnTitle(Msg.consts.turn_off_favorite());
		favorite.setOffTitle(Msg.consts.turn_on_favorite());
		
		favorite.addClickHandler(this);
		setEnable(true);
		
		FlowPanel panel = new FlowPanel();
		panel.add(favorite);
		if(showText){
			ClickLink text = new ClickLink(Msg.consts.favorite());
			text.addClickHandler(this);
			panel.add(text);
		}
		DOM.setElementAttribute(panel.getElement(), "noWrap", "true");
		this.initWidget(panel);
	}
	
	public void onClick(ClickEvent event) {
		PageControllerAsync action = ControllerFactory.getPageController();
		action.markPage(PageMain.getPageUuid(), SharedConstants.USER_PAGE_TYPE_FAVORITE,!favorite.isOn(), this);
	}
	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
	}		
	public void onSuccess(Boolean obj) {
		this.setEnable(!this.isEnable());
	}

	public void setEnable(boolean b) {
		enable = b;
		favorite.setOn(enable);
	}
	private boolean isEnable() {
		return enable;
	}

}
