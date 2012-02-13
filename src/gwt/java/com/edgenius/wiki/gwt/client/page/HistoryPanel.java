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

import java.util.ArrayList;
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.AbstractEntryPoint;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.MoreLessButtonBar;
import com.edgenius.wiki.gwt.client.widgets.MoreLessButtonBarListener;
import com.edgenius.wiki.gwt.client.widgets.UserProfileLink;
import com.edgenius.wiki.gwt.client.widgets.ZebraTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.EventfulHyperLink;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class HistoryPanel extends PinPanel implements AsyncCallback<PageItemListModel>, ClickHandler, MoreLessButtonBarListener{
	private static final int HISTORY_LIST_COUNT = 30;
	
	private ClickLink compareButton = new ClickLink(Msg.consts.compare());
	private List<PageItemModel> historyList = new ArrayList<PageItemModel>();
	private ZebraTable table = new ZebraTable(ZebraTable.STYLE_LIST, true);
	private MoreLessButtonBar moreBtn = new MoreLessButtonBar();

	public HistoryPanel(PageMain main){
		super(main);
		
		buildTableHeader();
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setStyleName("historyPanel");
		moreBtn.setVisible(false);
		
		moreBtn.addMoreLessButtonBarListener(this);
		
		panel.add(table);
		panel.add(moreBtn);
		content.add(panel);
		
		if(!AbstractEntryPoint.isOffline()){
			//compare two version button - not available on offline mode.
			compareButton.addClickHandler(this);
			compareButton.setStyleName(Css.LARGE_LINK_BTN);
			header.add(compareButton,DockPanel.WEST);
		}
	}
		
	public void clear(){
		historyList.clear();
		int rowCount = table.getRowCount();
		for(int idx=rowCount -1;idx>=0;idx--){
			table.removeRow(idx);
		}
		
	}

	/**
	 * 
	 */
	public void loadHistory() {
		historyList.clear();
		
		pageChange(0);
	}
	public void pageChange(int currentPageNum) {
		moreBtn.busy(true);
		PageControllerAsync action = ControllerFactory.getPageController();
		action.getPageHistory(main.getSpaceUname(), main.getPageUuid(), 
				historyList.size() ==0?0:historyList.get(historyList.size()-1).version, 
						HISTORY_LIST_COUNT, this);
	}
	public void onFailure(Throwable error) {
		showBusy(false);
		moreBtn.busy(false);
		GwtClientUtils.processError(error);
	}
	public void onSuccess(PageItemListModel model) {
		showBusy(false);
		moreBtn.busy(false);
		if(!GwtClientUtils.preSuccessCheck(model,message)){
			return;
		}
		int start;
		if(model.itemList.size() > 0 && model.itemList.get(0).isCurrent){
			//must call before next sentence: historyList will clean here.
			this.clear();
			//build header
			start = buildTableHeader();
			historyList = model.itemList;
		}else{
			//this is pagination - after 1 page, then don't clear old data.
			start = table.getRowCount();
			//double check if current return is current page's contiguous history list
			if(historyList.size() == 0 || model.itemList.size() == 0){
				//if return empty, then assume no more history.
				//historyList never be zero here actually, because history table assume always has current version record as first.
				moreBtn.setVisible(false);
				return;
			}
			if(!StringUtil.equals(model.itemList.get(0).uuid,main.getPageUuid())){
				//this response is not for current page, discard response
				Log.info("Unaccpted history as pageUuid different:"+ model.itemList.get(0).uuid);
				return;
			}
			if(model.itemList.get(0).version != historyList.get(historyList.size()-1).version-1){
				//this response doesn't return contiguous history list, discard.
				Log.info("Unaccpted history as version different with exist:"+ historyList.get(historyList.size()-1).version);
				return;
			}
			
			historyList.addAll(model.itemList);
		}
		
		int size = model.itemList.size();
		for(int idx=0;idx<size;idx++){
			final PageItemModel history = (PageItemModel) model.itemList.get(idx);
			CheckBox check = new CheckBox();
			check.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					history.checked = ((CheckBox)event.getSource()).getValue(); 
				}
			});
			HTML rev;
			EventfulHyperLink title;
			if(history.isCurrent){
				rev = new HTML("<b>"+Msg.consts.current()+"</b>");
				title = new EventfulHyperLink(history.title,GwtUtils.getSpacePageToken(history.spaceUname, history.title));
			}else{
				rev = new HTML("<b>"+Integer.valueOf(history.version).toString()+"</b>");
				title = new EventfulHyperLink(history.title,GwtUtils.buildToken(PageMain.TOKEN_HISTORY,history.spaceUname, String.valueOf(history.uid)));
			}
			rev.setStyleName(Css.NOWRAP);
			
			
			UserProfileLink modifier = new UserProfileLink(history.modifier, main.getSpaceUname(),history.modifierUsername,history.modifierPortrait);
			Label modifiedDate = new Label(GwtClientUtils.toDisplayDate(history.modifiedDate));
			int row = start + idx;
			int col = 0;
			table.setWidget(row, col++, check);
			table.setWidget(row, col++, rev);
			table.setWidget(row, col++, title);
			table.setWidget(row, col, modifier);
			table.getFlexCellFormatter().setStyleName(row, col++, Css.NOWRAP);
			table.setWidget(row, col, modifiedDate);
			table.getFlexCellFormatter().setStyleName(row, col++, Css.NOWRAP);
			
			if(idx == (size-1)){
				if(history.version > 1){
					moreBtn.setVisible(true);
					//always hide "less" button, page number is useless.
					moreBtn.setPaginationInfo(false, true, 0);
				}else{
					moreBtn.setVisible(false);
				}
			}
		}
	}
	public int buildTableHeader() {
		int row = 0;
		int col = 0;
		table.setText(row,col,"");
		table.getColumnFormatter().setWidth(col, "1%");
		
		col++;
		table.setText(row,col,Msg.consts.revision());
		table.getColumnFormatter().setWidth(col, "1%");
		col++;
		table.setText(row,col,Msg.consts.title());
		table.getColumnFormatter().setWidth(col, "70%");
		col++;
		table.setText(row,col,Msg.consts.modifier());
		table.getColumnFormatter().setWidth(col, "27%");
		table.getFlexCellFormatter().addStyleName(row, col, Css.NOWRAP);
		col++;
		table.setText(row,col,Msg.consts.modified_date());
		table.getColumnFormatter().setWidth(col, "1%");
		table.getFlexCellFormatter().addStyleName(row, col, Css.NOWRAP);
		col++;
		return ++row;
	}
	
	/*
	 * Compare button click
	 */
	public void onClick(ClickEvent event) {
		Object sender = event.getSource();
		if(sender == compareButton){
			PageItemModel left = null,right=null;
			boolean over = false;
			for(PageItemModel history : historyList){
				if(history.checked){
					if(left == null)
						left = history;
					else if(right == null)
						right = history;
					else{
						over = true;
						break;
					}
				}
			}
			
			if(over || left == null || right == null){
				Window.alert(Msg.consts.choose_two());
			}else{
				//do compare left and right
				PageControllerAsync action = ControllerFactory.getPageController();
				action.diff(right.isCurrent?null:right.uid, left.isCurrent?null:left.uid, main.viewPanel.versionAsync);
				
				//go to page top
				GwtClientUtils.gotoAnchor(PageMain.PAGE_TOP_ANCHOR_NAME);
			}
		}
	}
	
}
