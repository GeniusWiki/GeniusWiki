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

import com.edgenius.wiki.gwt.client.AbstractEntryPoint;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.server.TagControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.Hr;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.Popup;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author Dapeng.Ni
 */
public class TagPopup extends Popup implements AsyncCallback<PageItemListModel> {
	private static final int POPUP_TAG_PAGE_COUNT = 7;
	private VerticalPanel pagesPanel = new VerticalPanel();
	private MessageWidget message = new MessageWidget();
	private String spaceUname;
	private String tagname;
	
	public TagPopup(UIObject target, String spaceUname, String tagname){
		super(target, true, true, true);
		this.spaceUname = spaceUname;
		this.tagname = tagname;
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(new HTML("<b>"+tagname+"</b>"));
		panel.add(new Hr());
		panel.add(pagesPanel);
		if(!AbstractEntryPoint.isOffline()){
			//so far, have to block tag cloud in offline model as the macro render logic is on MacroHandler side, it is not easy to do in 
			//offline model.
			Hyperlink tagCloud = new Hyperlink(Msg.consts.goto_tagcloud(),
					GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_TAG_CLOUD), spaceUname));
			tagCloud.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					TagPopup.this.hide();
				}
			});
			panel.add(tagCloud);
			panel.setCellHorizontalAlignment(tagCloud, HasHorizontalAlignment.ALIGN_RIGHT);
		}		
		panel.setSize("100%", "100%");
		this.setWidget(panel);
		
	
	}

	public void pop(){
		//only when window pop, it will do Ajax call - to avoid call server when Popup initialize
		TagControllerAsync tagController = ControllerFactory.getTagController();
		tagController.getTagPages(spaceUname, tagname, POPUP_TAG_PAGE_COUNT, this);
		super.pop();
	}
	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
	}
	
	public void onSuccess(PageItemListModel model) {
		if(!GwtClientUtils.preSuccessCheck(model,message)){
			return;
		}
		
		pagesPanel.clear();
		for(PageItemModel item : model.itemList){
			Hyperlink link = new Hyperlink(item.title,GwtUtils.getSpacePageToken(item.spaceUname,item.title));
			link.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					TagPopup.this.hide();
				}
			});
			pagesPanel.add(link);
		}
		
		resized();
	}
	
}
