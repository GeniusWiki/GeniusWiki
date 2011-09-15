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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * The reason, don't use IFrame widget from GWT is, it has some bug, and it is very hard to implement onload event also.
 * for detail, do search "frame onload" from GWT Google group...
 * 
 * @author Dapeng.Ni
 */
public class URLPanel extends SimplePanel{
	//How to reszie URLPanel if content size is changed by some Ajax call:
	//see javascript resetURLPanelHeight(): it retrieve all element by name, and compare the content page urlPanelChildUid value,
	//if it is same means this URLPanel is container of this page, then resize URLPanel
	private static final String PANEL_NAME = "URLPanel";
	
	private final String uuid = HTMLPanel.createUniqueId();
	HTML html = new HTML("<iframe onload=\"javascript:this.style.height=this.contentWindow.document.body.scrollHeight+'px'\" "
                    +" id=\""+uuid+"\" name=\""+PANEL_NAME+"\" style=\"width:100%;height:0px;border:0px\" frameborder=\"0\" scrolling=\"no\"></iframe>");
	public URLPanel(){
		this.setWidget(html);
	}

	/**
	 * !!!IMPORTANT: this method must call after the widget is put into HMTL body(means it already attachment to HTML), otherwise,
	 * you will get doc.getElementById() no attribute exception. See OfflineUtil.checkGearsInstalled(), 
	 * @param url
	 */
	public void setURL(String url){
		DOM.getElementById(uuid).setAttribute("src", url);
		
	}

	
	
}
