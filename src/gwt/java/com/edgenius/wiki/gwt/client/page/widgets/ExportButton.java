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

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Dapeng.Ni
 */
public class ExportButton  extends FlowPanel{

	private PageMain main;


	public ExportButton(){
		
		ClickLink feedLink = new ClickLink(Msg.consts.export());
		feedLink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				openExportDlg();
			}
		});
		Image feedImg = new Image(IconBundle.I.get().export());
		feedImg.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				openExportDlg();
			}
		});
		feedImg.setStyleName(Css.PORTLET_FOOT_IMG);
		
		this.add(feedImg);
		this.add(feedLink);
		
		DOM.setStyleAttribute(this.getElement(), "display", "inline");
	}
	
	public void setSpaceUname(PageMain main) {
		this.main = main;
		if(this.main == null){
			this.setVisible(false);
		}else{
			this.setVisible(true);
		}
	}
	private void openExportDlg() {
		ExportDialog dialog = new ExportDialog(main);
		dialog.showbox();
	}
}
