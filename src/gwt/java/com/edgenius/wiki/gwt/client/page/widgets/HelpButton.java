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

import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.HelpDialog;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 * @obsolete. no use so far, where put? TBD.
 */
public class HelpButton  extends SimplePanel implements ClickHandler{

	public HelpButton(boolean showText) {
		
		HorizontalPanel panel = new HorizontalPanel();
//		Image tree = new Image(IconBundle.I.get().help());
//		tree.addClickHandler(this);
//		
//		panel.add(tree);
		if(showText){
			ClickLink locationBtn = new ClickLink(Msg.consts.help());
			locationBtn.addClickHandler(this);
			panel.add(locationBtn);
		}
//		panel.setCellVerticalAlignment(tree, HasVerticalAlignment.ALIGN_BOTTOM);
		this.setWidget(panel);
		
	}

	public void onClick(ClickEvent event) {
		HelpDialog help = new HelpDialog();
		help.showbox();
//		GWT.runAsync(new RunAsyncCallback() {
//			
//			public void onSuccess() {
//			}
//			
//			public void onFailure(Throwable arg0) {
//			}
//		});
	
	}
}
