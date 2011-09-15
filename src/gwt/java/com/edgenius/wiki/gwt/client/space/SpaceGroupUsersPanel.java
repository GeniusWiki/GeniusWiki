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
import com.edgenius.wiki.gwt.client.model.UserListModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.ZebraTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class SpaceGroupUsersPanel  extends SimplePanel implements AsyncCallback<UserListModel>, ClickHandler{

	private FlowPanel msgLabel = new FlowPanel();
	private ZebraTable table = new ZebraTable();
	private SpacePermissionPanel parent;
	Label title = new Label();
	
	public SpaceGroupUsersPanel(SpacePermissionPanel parent){
		this.parent = parent;
		
		ClickLink backLink1 = new ClickLink(SharedConstants.PREV_LINK+Msg.consts.back_to_space_security(), true);
		backLink1.addClickHandler(this);
		
		
		ClickLink backLink2 = new ClickLink(SharedConstants.PREV_LINK+Msg.consts.back_to_space_security(), true);
		backLink2.addClickHandler(this);
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(title);
		panel.add(backLink1);
		panel.add(msgLabel);
		panel.add(table);
		panel.add(backLink2);
		
		panel.setSpacing(5);
		title.setStyleName(Css.HEADING1);
		setWidget(panel);

	}
	public void onLoad(String senderSpace, String receiverSpace){
		
		SecurityControllerAsync securityControl = ControllerFactory.getSecurityController();
		securityControl.getSpaceGroupUsers(senderSpace, receiverSpace, this);
		
		title.setText(Msg.params.group_users(receiverSpace));
		
		//clean display
		int rowSize = table.getRowCount();
		for(int idx=rowSize-1;idx>=0;idx--){
			table.removeRow(idx);
		}
	}	
	
	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
	}

	public void onSuccess(UserListModel model) {
		
		if(!GwtClientUtils.preSuccessCheck(model, null)){
			//just simple display message on panel
			msgLabel.clear();
			msgLabel.add(ErrorCode.getMessage(model.errorCode, model.errorCode));
			return;
		}
		//show all users in this space group in table;
		fillPanel(model.getUserModelList());
	}

	/**
	 * @param userModelList
	 */
	private void fillPanel(List<UserModel> userModelList) {
		
		int currRow = 0;
		table.setWidget(currRow++, 0, new Label(Msg.consts.name()));
		for (Iterator<UserModel> iter = userModelList.iterator(); iter.hasNext();) {
			table.setWidget(currRow++, 0, new Label( iter.next().getFullname()));
		}
	}
	public void onClick(ClickEvent event) {
		parent.showSpaceSecurity();
		
	}
}
