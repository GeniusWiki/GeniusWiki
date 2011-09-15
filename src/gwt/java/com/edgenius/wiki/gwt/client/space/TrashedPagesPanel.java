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
package com.edgenius.wiki.gwt.client.space;

import java.util.Iterator;
import java.util.List;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.SpaceControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.LazyLoadingPanel;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.ZebraTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class TrashedPagesPanel  extends SimplePanel implements AsyncCallback<PageItemListModel>, LazyLoadingPanel {

	
	private static final int ACTION_COLUMN = 3;
	private VerticalPanel panel = new VerticalPanel();
	private ZebraTable table = new ZebraTable(ZebraTable.STYLE_LIST, true);
	private MessageWidget message = new MessageWidget();
	private String spaceUname;
	private Label nonLabel = new Label(Msg.consts.no_trash_page());
	public TrashedPagesPanel(String spaceUname) {
		
		this.spaceUname = spaceUname;
		panel.add(message);
		panel.add(table);
		panel.add(nonLabel);
		setWidget(panel);
		nonLabel.setStyleName(Css.BLANK_MSG);
		nonLabel.setVisible(false);
		
		panel.setCellVerticalAlignment(table, HasVerticalAlignment.ALIGN_TOP);
		panel.setCellHorizontalAlignment(table, HasHorizontalAlignment.ALIGN_CENTER);
		table.setWidth("99%");
		panel.setSize("100%", "100%");
		this.setSize("100%", "100%");
		
	}
	
	public void load(){
		SpaceControllerAsync action = ControllerFactory.getSpaceController();
		action.getRemovedPages(spaceUname, this);
	}

	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
	}

	public void onSuccess(PageItemListModel model ) {
		// TODO : error handle

		if(!GwtClientUtils.preSuccessCheck(model,message)){
			return;
		}
		List<PageItemModel>  pageList = model.itemList;
		message.cleanMessage();
		//clean table;
		int rowCount = table.getRowCount();
		for(int idx=rowCount - 1;idx>=0;idx--){
			table.removeRow(idx);
		}
		
		if(pageList != null && pageList.size() > 0){
			nonLabel.setVisible(false);
			int row = 0, col=0;;
			//build header
			col++; //page title - width is as max as possible
			table.getCellFormatter().setWordWrap(row, col, false);
			table.getColumnFormatter().setWidth(col++,"1%"); //person
			table.getCellFormatter().setWordWrap(row, col, false);
			table.getColumnFormatter().setWidth(col++,"1%"); //date
			table.getCellFormatter().setWordWrap(row, col, false);
			table.getColumnFormatter().setWidth(col++,"1%"); //restore & remove
			col = 0;
			table.setText(row, col++, Msg.consts.page_title());
			table.setText(row, col++, Msg.consts.remover());
			table.setText(row, col++, Msg.consts.date());
			table.setText(row, col++, Msg.consts.action());
			
			for(Iterator<PageItemModel>  iter = pageList.iterator();iter.hasNext();){
				row++;
				col = 0;
				final PageItemModel pageItem = iter.next();
				Label title = new Label();
				title.setText(pageItem.title);
				Label rPerson = new Label(pageItem.modifier);
				Label rDate = new Label(GwtClientUtils.toDisplayDate(pageItem.modifiedDate));
				ClickLink restore = new ClickLink(Msg.consts.restore());
				restore.setObject(pageItem.uuid);
				restore.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						PageControllerAsync action = ControllerFactory.getPageController();
						//need check if this restore page has duplicated title page exist in space, to ask user if really want to override. 
						action.restoreCheck(pageItem.uuid, new PreRestoreAsync(pageItem.spaceUname, pageItem.uuid, pageItem.title));
					}
				});
				ClickLink remove = new ClickLink(Msg.consts.remove());
				remove.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						if(Window.confirm(Msg.consts.confirm_remove_page())){
							PageControllerAsync action = ControllerFactory.getPageController();
							action.removePage(null, pageItem.spaceUname, pageItem.uuid,true, new RemoveAsync(pageItem.uuid));
						}
					}
				});
				
				table.setWidget(row,col++, title);
				table.getCellFormatter().setWordWrap(row, col, false);
				table.setWidget(row,col++,rPerson);
				table.getCellFormatter().setWordWrap(row, col, false);
				table.setWidget(row,col++,rDate);
				
				FlowPanel actionP = new FlowPanel();
				actionP.setStyleName(Css.NOWRAP);
				actionP.add(restore);
				actionP.add(new Label(" | "));
				actionP.add(remove);
				table.setWidget(row,ACTION_COLUMN,actionP);

			}
		}else{
			nonLabel.setVisible(true);
		}
	}
	/**
	 * //remove give row by pageUuid 
	 */
	private void removeRow(String pageUuid) {
		int count = table.getRowCount();
		//skip first title row
		for(int idx =1;idx<count;idx++){
			FlowPanel panel = (FlowPanel) table.getWidget(idx, ACTION_COLUMN);
			ClickLink restore = (ClickLink) panel.getWidget(0);
			if(pageUuid.equals(restore.getObject())){
				table.removeRow(idx);
				break;
			}
		}
		count = table.getRowCount();
		if(count == 1){
			//remove header as well
			table.removeRow(0);
			nonLabel.setVisible(true);
		}
	}
	
	private class PreRestoreAsync implements AsyncCallback<String>{
		private String pageUuid;
		private String spaceUname;
		private String pageTitle;
		public PreRestoreAsync(String spaceUname, String uuid, String title) {
			this.spaceUname = spaceUname;
			this.pageUuid = uuid;
			this.pageTitle = title;
		}

		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
		}

		public void onSuccess(String obj) {
			PageControllerAsync action = ControllerFactory.getPageController();
			if(obj == null){
				if(Window.confirm(Msg.params.restore_no_exist_confirm(pageTitle))){
					action.restorePage(spaceUname, pageUuid,false, true, new RestoreAsync(pageUuid));
				}
				
			}else{
				int status = new Integer(obj.substring(0,1)).intValue();
				String title = obj.substring(1);
				if(status == SharedConstants.RESTORE_HOMEPAGE_EXIST){
					if(Window.confirm(Msg.params.restore_replace_homepage(title))){
						action.restorePage(spaceUname, pageUuid,true,true, new RestoreAsync(pageUuid));
					}else if(Window.confirm(Msg.params.restore_home_to_general(title))){
						action.restorePage(spaceUname, pageUuid,false,true, new RestoreAsync(pageUuid));
					}
				}else if(status == SharedConstants.RESTORE_HOMEPAGE_NO_EXIST){
					if(!title.equals(pageTitle)){
						if(Window.confirm(Msg.params.restore_home_rename(title))){
							//restore with history, means this removed page history also restored.
							action.restorePage(spaceUname, pageUuid,true,true, new RestoreAsync(pageUuid));
						}
					}else{
						if(Window.confirm(Msg.params.restore_home(title))){
							action.restorePage(spaceUname, pageUuid,true,true, new RestoreAsync(pageUuid));
						}
					}
				}else{
					//normal page restore
					if(!title.equals(pageTitle)){
						if(Window.confirm(Msg.params.restore_rename(title))){
							//restore with history, means this removed page history also restored.
							action.restorePage(spaceUname, pageUuid,false,true, new RestoreAsync(pageUuid));
						}
					}else{
						if(Window.confirm(Msg.params.restore_confirm(pageTitle))){
							action.restorePage(spaceUname, pageUuid,false,true, new RestoreAsync(pageUuid));
						}
					}
				}
			}
		}
		
	}
	private class RestoreAsync implements AsyncCallback<PageModel>{
		private String pageUuid;
		public RestoreAsync(String pageUuid) {
			this.pageUuid = pageUuid;
		}
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
			
		}
		public void onSuccess(PageModel model) {
	
			if(!GwtClientUtils.preSuccessCheck(model,message)){
				return;
			}
			message.info(Msg.params.restore_success(model.title));
			removeRow(pageUuid);
			
		}
		
	}
	private class RemoveAsync implements AsyncCallback<PageModel>{
		private String pageUuid;
		public RemoveAsync(String uuid) {
			pageUuid = uuid;
		}
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
		}
		public void onSuccess(PageModel obj) {
			
			message.info(Msg.consts.delete_perm());
			removeRow(pageUuid);
		}
		
		
	}
}
