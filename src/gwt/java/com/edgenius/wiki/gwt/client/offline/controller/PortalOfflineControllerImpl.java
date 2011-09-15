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
package com.edgenius.wiki.gwt.client.offline.controller;

import java.util.ArrayList;
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.MacroModel;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.PortalModel;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.model.RenderMarkupModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.SpacePagesModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.model.WidgetModel;
import com.edgenius.wiki.gwt.client.offline.GearsDB;
import com.edgenius.wiki.gwt.client.offline.OfflineUtil;
import com.edgenius.wiki.gwt.client.server.PortalControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.google.gwt.gears.client.GearsException;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Dapeng.Ni
 */
public class PortalOfflineControllerImpl extends AbstractOfflineControllerImpl implements PortalControllerAsync,OfflineService {

	public void getDashboard(boolean withMarkup, AsyncCallback<RenderMarkupModel> callback) {
		//TODO: Offline - always only show portal
		RenderMarkupModel model = new RenderMarkupModel();
		
		MacroModel macro = new MacroModel();
		macro.macroName=SharedConstants.MACRO_PORTAL;
		macro.values.put(NameConstants.SHOWLOGO, Boolean.TRUE.toString());
		model.renderContent.add(macro);
		
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);
	}

	public void getPortal(int column, AsyncCallback<PortalModel> callback) {

		PortalModel model = new PortalModel();
		
		UserModel user = OfflineUtil.getUser();
		List<String> layout = GearsDB.getHomeLayout(user.getUid());
		Log.info("Get login user home layout:" +layout);
		model = parsePortalLayout(column, user,layout);
		
		//check if it contains draft portlet, if no, always add draft portlet
		boolean found = false;
		if(model.portlets != null){
			for (PortletModel portlet : model.portlets) {
				if(portlet.type ==  PortletModel.DRAFT_LIST){
					found = true;
					break;
				}
			}
		}
		if(!found && !GwtUtils.isAnonymous(user)){
			PortletModel portlet = new PortletModel();
			portlet.type = PortletModel.DRAFT_LIST;
			portlet.row = 0;
			portlet.column = 0;
			portlet.key = "My draft pages";
			portlet.title = "My draft pages list";
			portlet.description = "My draft pages list";
			
			model.portlets.add(portlet);
		}
		
		
		model.viewingUser = user;
		Log.info("Dashboard return portal model: " + model);
		
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);
		
	}
	
	public void invokePortlet(String type, String key, AsyncCallback<PortletModel> callback) {
		
		UserModel user = OfflineUtil.getUser();
		
		PortletModel porlet = new PortletModel();
		Log.info("Invoke porlet for type " + type + " by key " + key);
		try {
			GearsDB userDB = GearsDB.getUserDB(user.getUid());
			if(PortletModel.DRAFT_LIST.equals(type)){
				PageItemListModel model = new PageItemListModel(); 
				//List<PageItemModel>
				model.itemList = userDB.getMyDrafts();
				
				porlet.renderContent = model;
			}else if(PortletModel.SPACE.equals(type)){
				//key is spaceUname
				SpacePagesModel model = new SpacePagesModel();
				//List<PageItemModel>
				model.space = userDB.getSpace(key);
				if(model.space == null){
					porlet.errorCode = ErrorCode.SPACE_NOT_EXIST_ERR;
				}else{
					model.pages = userDB.getPagesFromSpace(key,10);
					porlet.renderContent = model;
				}
			}else{
				porlet.errorCode = ErrorCode.UNSUPPORT_OFFLINE;
			}
		} catch (GearsException e) {
			Log.error("Unable to invoke portlet " + type + ", key is " + key,e);
			porlet.errorCode = ErrorCode.SPACE_NOT_EXIST_ERR;
		}
		
		OfflineUtil.setLoginInfo(porlet);
		callback.onSuccess(porlet);
	}
	

	public void getListedWidgets(int listSize, int selectPageNumber, AsyncCallback<PortalModel> callback) {
		// TODO Auto-generated method stub
	}




	
	public void updatePortal(ArrayList<String> list, AsyncCallback<Boolean> callback) {
		if(list == null){
			callback.onSuccess(false);
			return;
		}
		
		UserModel user = OfflineUtil.getUser();
		//anonymous does not have personal configuration data, skip it.
		if(GwtUtils.isAnonymous(user)){
			callback.onSuccess(false);
			return;
		}
		
		try {
			GearsDB.updatePortal(user.getUid(),list);
			callback.onSuccess(true);
			return;
		} catch (GearsException e) {
			Log.error("Update user " + user.getUid() + " portal layout failed.",e);
		}
		
		callback.onSuccess(false);
	}

	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//               Does not implement in offline model
	public void getDefaultPortal(int column, AsyncCallback<PortalModel> callback) {}
	public void updateDefaultPortal(ArrayList<String> list, AsyncCallback<Boolean> callback) {}
	//********************************************************************
	//               private method
	//********************************************************************
	private PortalModel parsePortalLayout(int columns, UserModel viewer, List<String> layout) {
		PortalModel model = new PortalModel();
		if(layout == null){
			Log.info("User layout is null");
			return model;
		}
		model.totalColumns = columns;
		model.portlets = new ArrayList<PortletModel>(); 
		
		for (String portletStr : layout) {
			if(portletStr == null || portletStr.trim().length() == 0)
				continue;
			//string separated by ",". Construct by spaceUname or $x, x is the customized protlet type number
			String[] values = portletStr.split("\\"+SharedConstants.PORTLET_SEP);
			if(values.length != 4 ){
				Log.error("Unexpected case: porlet setting does not have 4 fields " + portletStr);
				continue;
			}
			
			String type = values[0];
			String uName = EscapeUtil.unescapeToken(values[1]);
			//Construct String 4: anyPortletModelType,shortTitle(spaceUname if space type portlet),row,column
			
			PortletModel portlet = new PortletModel();
			portlet.type = type;
			portlet.row = Integer.parseInt(values[2]);
			portlet.column = Integer.parseInt(values[3]);
			
			//only Space and Draft available in offline model
			if(type == PortletModel.SPACE){
				try {
					SpaceModel space = GearsDB.getUserDB(viewer.getUid()).getSpace(uName);
					portlet.key = space.unixName;
					portlet.title = space.name;
					portlet.description = space.description;
					
					Log.info("Get space portlet: " + space);
				} catch (GearsException e) {
					Log.error("Unable to get space by uname" + uName);
					continue;
				}
			}else if(type == PortletModel.DRAFT_LIST){
				//HARDCODE:
				portlet.key = SharedConstants.DRAFT_KEY;
				portlet.title = Msg.consts.draft_portlet_title();
				portlet.description = Msg.consts.draft_portlet_desc();
			}
			model.portlets.add(portlet);
		}
		
		return model;
	}


	public void saveOrUpdateWidget(WidgetModel widget, AsyncCallback<PortletModel> callback) {
		//no implementation on offline model
	}


	public void removeWidget(String key, AsyncCallback<PortletModel> callback) {
		//no implementation on offline model
	}

	public void getWidget(String key, AsyncCallback<WidgetModel> callback) {
		//no implementation on offline model
	}

	public void previewDashboard(String markup, AsyncCallback<RenderMarkupModel> callback) {
		//no implementation on offline model
		
	}

	public void saveDashboard(String markup, AsyncCallback<RenderMarkupModel> callback) {
		//no implementation on offline model
		
	}

	public void addWidgetToDashboardPortal(String widgetType, String widgetKey, AsyncCallback<WidgetModel> callback) {
		// TODO Auto-generated method stub
		
	}

}
