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
package com.edgenius.wiki.gwt.client.page;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.page.widgets.LocationButton;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.widgets.UserProfileLink;
import com.edgenius.wiki.gwt.client.widgets.ZebraTable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Dapeng.Ni
 */
public class ChildrenPanel extends PinPanel implements AsyncCallback<PageItemListModel> {

	private LocationButton treeBtn;
	private ZebraTable table;;
	
	public ChildrenPanel(PageMain main) {
		super(main);
		treeBtn = new LocationButton(main, true);
		
		header.add(treeBtn, DockPanel.WEST);
	}

	public void load() {
		showBusy(true);
		PageControllerAsync action = ControllerFactory.getPageController();
		action.getPageChildren(main.getSpaceUname(), main.getPageUuid(), this);	
	}
	
	public int buildTableHeader() {
		int row = 0;
		int col = 0;
//		table.setText(row,col++,"");
		table.setText(row,col,Msg.consts.title());
		table.getColumnFormatter().setWidth(col, "75%");
		col++;
		table.setText(row,col,Msg.consts.modifier());
		table.getColumnFormatter().setWidth(col, "24%");
		table.getFlexCellFormatter().addStyleName(row, col, Css.NOWRAP);
		col++;
		table.setText(row,col,Msg.consts.modified_date());
		table.getColumnFormatter().setWidth(col, "1%");
		table.getFlexCellFormatter().addStyleName(row, col, Css.NOWRAP);
		col++;
		
		return ++row;
	}

	public void onFailure(Throwable error) {
		showBusy(false);
		GwtClientUtils.processError(error);
		
	}

	public void onSuccess(PageItemListModel model) {
		showBusy(false);
		
		if(!GwtClientUtils.preSuccessCheck(model,message)){
			return;
		}
		content.clear();
		if(model.itemList == null || model.itemList.size() == 0){
			//no any children
			Label no = new Label(Msg.consts.no_child_page());
			no.setStyleName(Css.BLANK_MSG);
			content.add(no);
		}else{
			table = new ZebraTable(ZebraTable.STYLE_LIST,true);
			content.add(table);
			int start = buildTableHeader();
			int size = model.itemList.size();
			
			for(int idx=0;idx<size;idx++){
				final PageItemModel item = (PageItemModel) model.itemList.get(idx);
				
				Label modifiedDate = new Label(GwtClientUtils.toDisplayDate(item.modifiedDate));
				Hyperlink titleLink = new Hyperlink(item.title,GwtUtils.getSpacePageToken(main.getSpaceUname(), item.title)); 
			
				UserProfileLink modifier = new UserProfileLink(item.modifier, main.getSpaceUname(),item.modifierUsername,item.modifierPortrait);
				int row = start + idx;
				int col = 0;
				table.setWidget(row, col++, titleLink);
				table.setWidget(row, col, modifier);
				table.getFlexCellFormatter().setStyleName(row, col++, Css.NOWRAP);
				table.setWidget(row, col, modifiedDate);
				table.getFlexCellFormatter().setStyleName(row, col++, Css.NOWRAP);
			}
			
		}
	}
}
