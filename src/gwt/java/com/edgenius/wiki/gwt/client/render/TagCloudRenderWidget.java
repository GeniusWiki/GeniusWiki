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
package com.edgenius.wiki.gwt.client.render;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.SpaceListModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.TagListModel;
import com.edgenius.wiki.gwt.client.model.TagModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.TagControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.ZebraTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.EventfulHyperLink;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * 
 * @author Dapeng.Ni
 */
public class TagCloudRenderWidget extends SimplePanel implements AsyncCallback<TagListModel>, RenderWidget{
	private static String STYLE_TAB_SIZE_PREFIX = "tagcloud";
	
	private FlexTable cloudPanel = new FlexTable();
	private ZebraTable listsPanel = new ZebraTable(ZebraTable.STYLE_LIST, true);
	private MessageWidget message = new MessageWidget();

	private RenderWidgetListener listener;
	private String componentKey;
	private String spaceUname;
	
	public TagCloudRenderWidget(final String spaceUname) {
		this.spaceUname = spaceUname;
		
		FlexTable panel = new FlexTable();
		panel.setWidget(0, 0, message);
		panel.getFlexCellFormatter().setColSpan(0, 0, 3);
		
		panel.setWidget(1, 0, cloudPanel);
		//just separator between tag cloud and its list.
		panel.setWidget(1, 1, new HTML("&nbsp;"));
		panel.setWidget(1, 2, listsPanel);
		
		panel.getCellFormatter().setWidth(1, 0 , "30%");
		panel.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
		panel.getCellFormatter().setWidth(1, 1 , "20px");
		panel.getCellFormatter().setWidth(1, 2 , "70%");
		panel.getCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
		
		panel.setSize("100%", "100%");
		this.setWidget(panel);
		cloudPanel.setStyleName(Css.TAGCLOUD);

	}

	
	
	public void onFailure(Throwable caught) {
		GwtClientUtils.processError(caught);
		
		listener.onFailedLoad(componentKey, caught.getMessage());
		
	}
	public void onSuccess(TagListModel list) {
		if(!GwtClientUtils.preSuccessCheck(list,message)){
			listener.onFailedLoad(componentKey, ErrorCode.getMessageText(list.errorCode, list.errorMsg));
			return;
		}
		int row = cloudPanel.getRowCount();
        for(int idx=row -1; idx >= 0 ; idx--){
        	cloudPanel.removeRow(idx);
        } 
		
		boolean first = true;
		//go to special tag, list its pages
		final TagControllerAsync tagController = ControllerFactory.getTagController();
		
		final String spaceUname = list.spaceUname;
		StringBuffer tagSb = new StringBuffer();
		Label header;
		if(SharedConstants.SYSTEM_SPACEUNAME.equalsIgnoreCase(spaceUname) || StringUtil.isBlank(spaceUname)){
			header = new Label(Msg.consts.space_tagcolud_title());
		}else{
			header = new Label(Msg.consts.page_tagcolud_title());
		}
		header.setStyleName(Css.HEADER);
		cloudPanel.setWidget(0, 0, header);
		
		FlowPanel cloud = new FlowPanel();
		cloudPanel.setWidget(1, 0, cloud);

		if(list.tags != null && list.tags.size() > 0){
			for(final TagModel model : list.tags){
				ClickLink link = new ClickLink("<span class='"+STYLE_TAB_SIZE_PREFIX+model.size+"'>"+model.name+"</span>",true);
				link.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						//return all page for this tag so far.
						if(SharedConstants.SYSTEM_SPACEUNAME.equalsIgnoreCase(spaceUname) || StringUtil.isBlank(spaceUname)){
							tagController.getTagSpaces(model.name, -1,  new SpaceTagAsncy());
						}else{
							tagController.getTagPages(spaceUname, model.name, -1, new PageTagAsncy());
						}
					}
				});
				cloud.add(link);
				//just for wrap of line, otherwise all tag will be in same line.
				cloud.add(new HTML(" "));
				tagSb.append(model.name);
				
				//show first tag pages at initial status
				if(first){
					first = false;
					if(SharedConstants.SYSTEM_SPACEUNAME.equalsIgnoreCase(spaceUname)){
						tagController.getTagSpaces(model.name, -1, new SpaceTagAsncy());
					}else{
						tagController.getTagPages(spaceUname, model.name, -1,new PageTagAsncy());
					}
				}
			}
		}else{
			cloud.add(new Label(Msg.consts.none()));
		}
		listener.onSuccessLoad(componentKey, tagSb.toString());
		
	}
	public void onLoad(String widgetKey, UserModel user, RenderWidgetListener listener){
		this.listener = listener;
		this.componentKey = widgetKey;
		
		listener.onLoading(componentKey);
		TagControllerAsync tagController = ControllerFactory.getTagController();
		tagController.getTags(spaceUname, this);

	}	
	public void onUserChanged(UserModel user) {
	}
	
	//********************************************************************
	//               private classes
	//********************************************************************
	private class PageTagAsncy  implements AsyncCallback<PageItemListModel> {
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
			
		}
		/*
		 * user choose special tag, return page list
		 */
		public void onSuccess(PageItemListModel model) {
			int row = listsPanel.getRowCount();
	        for(int idx=row -1; idx >= 0 ; idx--){
	        	listsPanel.removeRow(idx);
	        } 
	        
			if(!GwtClientUtils.preSuccessCheck(model,null)){
				return;
			}
			
			row = 0;
			Label l1 = new Label(Msg.consts.page_title());
			Label l2 = new Label(Msg.consts.spacekey());
			Label l3 = new Label(Msg.consts.modified_date());
			listsPanel.setWidget(row, 0, l1);
			listsPanel.setWidget(row, 1, l2);
			listsPanel.setWidget(row, 2, l3);
			
			listsPanel.getColumnFormatter().setWidth(0, "50%");
			listsPanel.getColumnFormatter().setWidth(1, "25%");
			listsPanel.getColumnFormatter().setWidth(2, "25%");
			row++;
			
			if(model.itemList != null && model.itemList.size() > 0){
				for(PageItemModel item : model.itemList){
					EventfulHyperLink link = new EventfulHyperLink(item.title,GwtUtils.getSpacePageToken(item.spaceUname,item.title));
					listsPanel.setWidget(row, 0, link);
					link = new EventfulHyperLink(item.spaceUname,GwtUtils.getSpacePageToken(item.spaceUname,null));
					listsPanel.setWidget(row, 1, link);
					listsPanel.setWidget(row, 2, new Label(GwtClientUtils.toDisplayDate(item.modifiedDate)));
					
					row++;
				}
			}else{
				listsPanel.setWidget(row, 0, new Label(Msg.consts.none()));
			}
			
		}
	}
	private class SpaceTagAsncy  implements AsyncCallback<SpaceListModel> {
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
			
		}
		/*
		 * user choose special tag, return page list
		 */
		public void onSuccess(SpaceListModel model) {
			int row = listsPanel.getRowCount();
	        for(int idx=row -1; idx >= 0 ; idx--){
	        	listsPanel.removeRow(idx);
	        } 
	        
			if(!GwtClientUtils.preSuccessCheck(model,null)){
				return;
			}

			row = 0;
			listsPanel.setWidget(row, 0, new Label(Msg.consts.spacekey()));
			listsPanel.setWidget(row, 1, new Label(Msg.consts.created_date()));
			listsPanel.getRowFormatter().setStyleName(row, Css.HEADER);
			listsPanel.getColumnFormatter().setWidth(0, "75%");
			listsPanel.getColumnFormatter().setWidth(1, "25%");
			row++;
			
			if(model.spaceList != null && model.spaceList.size() > 0){
				for(SpaceModel item : model.spaceList){
					EventfulHyperLink link = new EventfulHyperLink(item.name,GwtUtils.getSpacePageToken(item.unixName, null));
					listsPanel.setWidget(row, 0, link);
					listsPanel.setWidget(row, 1, new Label(GwtClientUtils.toDisplayDate(item.createdDate)));
					row++;
				}
			}else{
				listsPanel.setWidget(row, 0, new Label(Msg.consts.none()));
			}
		}
	}
}

