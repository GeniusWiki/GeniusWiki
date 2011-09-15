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
package com.edgenius.wiki.gwt.client.server;

import java.util.ArrayList;

import com.edgenius.wiki.gwt.client.model.PortalModel;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.model.RenderMarkupModel;
import com.edgenius.wiki.gwt.client.model.WidgetModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Dapeng.Ni
 */
public interface PortalControllerAsync extends RemoteServiceAsync{
	
	/**
	 * Please note- There is big trick here. withMarkup is also a flag to do security check, if it is true.
	 * The only system admin has this permission!!!!
	 * 
	 * @param withMarkup bring backup Dashboard markup
	 * @param callback
	 */
	void getDashboard(boolean withMarkup, AsyncCallback<RenderMarkupModel> callback);
	/**
	 * Only system admin has permission to call this method.
	 * @param markup
	 * @param callback
	 */
	void previewDashboard(String markup, AsyncCallback<RenderMarkupModel> callback);
	
	/**
	 * Scenario - in common search results, users click a widget link, then it is expected to add to Dashboard portal.
	 * There are several cases:
	 * <li> One portal Macro in dashboard - then success add</li>
	 * <li> Multiple portal Macro in dashboard - ?? choose one of them add or add all of them?</li>
	 * <li> No portal Macro in dashboard - return error</li>
	 *  
	 * @param widgetKey
	 * @param callback
	 */
	void addWidgetToDashboardPortal(String widgetType, String widgetKey, AsyncCallback<WidgetModel> callback);
	/**
	 * Only system admin has permission to call this method.
	 * @param markup
	 * @param callback
	 */
	void saveDashboard(String markup, AsyncCallback<RenderMarkupModel> callback);
	
	void getPortal(int column, AsyncCallback<PortalModel> callback);
	
	void updatePortal(ArrayList<String> list, AsyncCallback<Boolean> callback);
	/**
	 * Return home page of system default layout. It is allow system admin show default home page in instance manage page.
	 * 
	 * @param loadAsyncCallback
	 */
	void getDefaultPortal(int column, AsyncCallback<PortalModel> callback);
	/**
	 * @param list
	 * @param saveAsyncCallback
	 */
	void updateDefaultPortal(ArrayList<String> list, AsyncCallback<Boolean> callback);
	/**
	 * Return list of system available widgets. Used in "add more space" dialog to provide list to ask user choose.
	 * @param spaceListSize
	 * @param widgetCallback
	 */
	void getListedWidgets(int listSize,int selectPageNumber, AsyncCallback<PortalModel> callback);
	
	/**
	 * Refresh portlet will invoke this method. Usually, this method will read out portlet render content and 
	 * send back.
	 * 
	 * @param type
	 * @param key
	 */
	void invokePortlet(String type, String key, AsyncCallback<PortletModel> callback);
	/**
	 * Save or update (dependent if widget.key is null or not) widget
	 * @param widget
	 * @param portletCreateDialog
	 */
	void saveOrUpdateWidget(WidgetModel widget, AsyncCallback<PortletModel> callback);
	
	void removeWidget(String key, AsyncCallback<PortletModel> callback);
	/**
	 * @param key
	 * @param loadingAsync
	 */
	void getWidget(String key, AsyncCallback<WidgetModel> callback);



	
}
