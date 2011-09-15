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

import java.util.ArrayList;
import java.util.List;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.GeneralModel;
import com.edgenius.wiki.gwt.client.model.RoleModel;
import com.edgenius.wiki.gwt.client.model.SearchResultItemModel;
import com.edgenius.wiki.gwt.client.model.SearchResultModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.SearchControllerAsync;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class ListPanel extends SimplePanel{
	private static final int PAGE_SIZE = 10;
	
	private ZebraTable table = new ZebraTable();
	private CandidateListPanel selectedList = new CandidateListPanel(); 
	//it is hard to use generic type...
	private HintTextBox filter = new HintTextBox(Msg.consts.input_keyword());
	private String keyword;
	private MessageWidget message = new MessageWidget();
	private int type;
	public ListPanel(final int type){
		this.type = type;
		
		HorizontalPanel funcPanel = new HorizontalPanel();
		
		if(type == ListDialogue.GROUP){
			funcPanel.add(new Label(Msg.consts.search_groups()+" "));
		}else if(type == ListDialogue.USER){
			funcPanel.add(new Label(Msg.consts.search_users() + " "));
		}
		funcPanel.add(filter);
		filter.setStyleName(Css.SEARCH_INPUT);
		filter.setTitle(Msg.consts.input_keyword());
		
		filter.addKeyDownHandler(new KeyDownHandler() {
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					keyword = filter.getText();
					if (keyword == null || keyword.trim().length() == 0)
						return;
					if(type == ListDialogue.GROUP){
						SearchControllerAsync action = ControllerFactory.getSearchController();
						action.searchRoles(keyword, 0,PAGE_SIZE, new SearchAsync());
					}else if(type == ListDialogue.USER){
						SearchControllerAsync action = ControllerFactory.getSearchController();
						action.searchUser(keyword, 0, PAGE_SIZE, new SearchAsync());
					}
				}
			}
		});
		
		funcPanel.add(selectedList);
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(funcPanel);
		panel.add(table);
		
		panel.setSize("100%","100%");

		//To display hint, don't set focus first.
//		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
//			public void execute() {
//				filter.setFocus(true);
//			}
//		});
		this.setWidget(panel);
	}
	
	public void fillPanel(List<? extends GeneralModel> items){
		int rowSize = table.getRowCount();
		for(int idx=rowSize-1;idx>=0;idx--){
			table.removeRow(idx);
		}
		selectedList.clear();
		
		for(int row=0;row<items.size();row++){
			Object obj = items.get(row);
			if(obj instanceof UserModel){
				createUserRow((UserModel)obj);
			}else if(obj instanceof RoleModel){
				createRoleRow((RoleModel)obj);
			}
		}
	}

	/**
	 * @param obj
	 */
	private void createRoleRow(final RoleModel role) {
		int currRow = table.getRowCount();
		if(currRow == 0){
			//insert header
			table.setWidget(currRow, 0, new Label(Msg.consts.name()));
			table.setWidget(currRow, 1, new Label(Msg.consts.description()));
			table.setWidget(currRow, 2, new Label(Msg.consts.action()));
			++currRow;
		}
		table.setWidget(currRow, 0, new Label(role.getDisplayName()));
		table.setWidget(currRow, 1, new Label(role.getDesc()));
		ClickLink addClick = new ClickLink(Msg.consts.add());
		addClick.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				selectedList.addCandidate(role.getDisplayName(), role);
			}
		});
		table.setWidget(currRow, 2, addClick);
	}

	/**
	 * @param obj
	 */
	private void createUserRow(final UserModel user) {
		int currRow = table.getRowCount();
		if(currRow == 0){
			//insert header
			table.setWidget(currRow, 0, new Label(Msg.consts.id()));
			table.setWidget(currRow, 1, new Label(Msg.consts.name()));
			table.setWidget(currRow, 2, new Label(Msg.consts.action()));
			++currRow;
		}
		table.setWidget(currRow, 0, new Label(user.getLoginname()));
		table.setWidget(currRow, 1, new Label(user.getFullname()));
		ClickLink addClick = new ClickLink(Msg.consts.add());
		addClick.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				selectedList.addCandidate(user.getFullname(), user);
			}
		});
		table.setWidget(currRow, 2, addClick);
		
	}

	public List getCandidates(){
		return selectedList.getCandidates();
	}
	
	private class SearchAsync implements AsyncCallback<SearchResultModel>{

		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
			
		}

		public void onSuccess(SearchResultModel model) {
			if(!GwtClientUtils.preSuccessCheck(model, message)){
				return;
			}
			
			if(model == null ||model.results == null || model.results.size() == 0){
				message.info(Msg.consts.no_result());
				return;
			}
			message.cleanMessage();
			List<GeneralModel> list = new ArrayList<GeneralModel>();
			for(SearchResultItemModel item : model.results){
				if(type == ListDialogue.USER){
					UserModel user = new UserModel();
					user.setLoginname(item.title);
					user.setFullname(item.contributor);
					list.add(user);
				}else if(type == ListDialogue.GROUP){
					RoleModel role = new RoleModel();
					role.setName(item.spaceUname);
					role.setDisplayName(item.title);
					role.setDesc(item.desc);
					role.setType(item.type);
					list.add(role);
				}
			}
			
			fillPanel(list);
		}
		
	}
}
