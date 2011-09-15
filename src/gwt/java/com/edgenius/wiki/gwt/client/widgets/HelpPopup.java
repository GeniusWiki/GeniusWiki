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
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
/**
 * @author Dapeng.Ni
 */
public class HelpPopup extends Popup{

	public static final int SPACE_PRIVATE = 1;
	public static final int BULK_ATTACHMENT_UPLOAD = 2;
	public static final int LINK_BLOG = 3;
	private VerticalPanel info = new VerticalPanel();
	public HelpPopup(UIObject target, int helpID) {
		super(target,true, true, true);
		
		if(helpID == SPACE_PRIVATE){
			info.add(new HTML(Msg.consts.help_pop_private_space()));
		}else if(helpID == BULK_ATTACHMENT_UPLOAD){
			info.add(new HTML(Msg.consts.help_pop_bulk_upload()));
		}else if(helpID == LINK_BLOG){
			info.add(new HTML(Msg.consts.help_pop_link_blog()));
		}
		VerticalPanel panel = new VerticalPanel();
		panel.add(info);
		panel.add(new Label(Msg.consts.help_pop_general()));
		this.setStyleName(Css.TIP_POPUP);
		this.setWidget(panel);
	}


	
}
