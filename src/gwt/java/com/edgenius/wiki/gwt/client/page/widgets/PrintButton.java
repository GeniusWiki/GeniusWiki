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

import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
/**
 * @author Dapeng.Ni
 */
public class PrintButton extends SimplePanel implements ClickHandler{

	private PageMain main;
	public PrintButton(PageMain pageMain, boolean showText) {
		this.main = pageMain;
		
		HorizontalPanel panel = new HorizontalPanel();
		if(showText){
			ClickLink locationBtn = new ClickLink(Msg.consts.print());
			locationBtn.addClickHandler(this);
			panel.add(locationBtn);
		}
		Image print = new Image(IconBundle.I.get().printer());
		print.addClickHandler(this);
		panel.add(print);
		panel.setCellVerticalAlignment(print, HasVerticalAlignment.ALIGN_BOTTOM);
		
		this.setWidget(panel);
	}
	public void onClick(ClickEvent event) {
		String token = GwtClientUtils.getToken();
		if(token.indexOf(PageMain.TOKEN_HISTORY) != -1){
			Window.open(GwtClientUtils.getBaseUrl()+"print.do?i="+main.getPageUuid()+"&v="+main.getPageVersion()+"&h=true",SharedConstants.APP_NAME+"Print","");
		}else{
			Window.open(GwtClientUtils.getBaseUrl()+"print.do?i="+main.getPageUuid(),SharedConstants.APP_NAME+"Print","");
		}
	}
}
