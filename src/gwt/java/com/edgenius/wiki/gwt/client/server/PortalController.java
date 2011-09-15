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

/**
 * @author Dapeng.Ni
 */
public interface PortalController extends RemoteService{
	String MODULE_ACTION_URI = "portal.rpcs";
	

	
	//********************************************************************
	//               Service methods
	//********************************************************************
	/**
	 *  Please note- There is big trick here. withMarkup is also a flag to do security check, if it is true.
	 * The only system admin has this permission!!!!
	 */
	public RenderMarkupModel getDashboard(boolean withMarkup);
	public RenderMarkupModel  previewDashboard(String markup);
	public RenderMarkupModel  saveDashboard(String markup);

	/**
	 * String in List is combination of string by portlet Uid,row,column. 
	 */
	public boolean updatePortal(ArrayList<String> list);
	public PortalModel getPortal(int column);
	
	public PortalModel  getDefaultPortal(int column);
	/**
	 * String in List is combination of string by portlet Uid,row,column. 
	 */
	public boolean updateDefaultPortal(ArrayList<String> list);
	
	PortalModel getListedWidgets(int listSize,int selectPageNumber);
	
	PortletModel invokePortlet(String type, String key);
	
	PortletModel saveOrUpdateWidget(WidgetModel widget);
	
	PortletModel removeWidget(String key);
	
	WidgetModel getWidget(String key);
	
	WidgetModel addWidgetToDashboardPortal(String widgetType, String widgetKey);
}
