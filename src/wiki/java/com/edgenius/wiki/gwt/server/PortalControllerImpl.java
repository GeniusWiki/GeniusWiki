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
package com.edgenius.wiki.gwt.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.UserSetting;
import com.edgenius.core.model.User;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.InstanceSetting;
import com.edgenius.wiki.gwt.client.model.MacroModel;
import com.edgenius.wiki.gwt.client.model.PortalModel;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.model.RenderMarkupModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.WidgetModel;
import com.edgenius.wiki.gwt.client.server.PortalController;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.server.handler.GWTSpringController;
import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.SecurityDummy;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.WidgetService;
import com.edgenius.wiki.util.WikiUtil;
import com.edgenius.wiki.widget.SpaceWidget;
import com.edgenius.wiki.widget.WidgetException;
import com.edgenius.wiki.widget.WidgetTemplate;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class PortalControllerImpl extends GWTSpringController implements PortalController{
	private static final Logger log = LoggerFactory.getLogger(PortalControllerImpl.class);

	private RenderService renderService;
	private SettingService settingService;
	private SecurityDummy securityDummy;
	private WidgetService widgetService;
	
	//JDK1.6 @Override
	public RenderMarkupModel getDashboard(boolean withMarkup) {
		if(withMarkup){
			// A big trick! withMarkup is true, means it comes from System page, so do security check here!
			securityDummy.checkInstanceAdmin();
		}
		
		RenderMarkupModel model = new RenderMarkupModel();
		InstanceSetting instance = settingService.getInstanceSetting();
		
		if(instance == null || StringUtil.isBlank(instance.getDashboardMarkup())
			|| StringUtil.equalsIgnoreCase(StringUtils.trim(instance.getDashboardMarkup()),SharedConstants.DEFAULT_DAHSBOARD_MARKUP)){
			//here just handle default directly to MacroModel - for performance reason - most scenario are default 
			//
			MacroModel macro = new MacroModel();
			macro.macroName=SharedConstants.MACRO_PORTAL;
			macro.values.put(NameConstants.SHOWLOGO, Boolean.TRUE.toString());
			model.renderContent.add(macro);
			if(withMarkup){
				model.markup = SharedConstants.DEFAULT_DAHSBOARD_MARKUP;
			}
		}else{
			if(withMarkup){
				model.markup = instance.getDashboardMarkup();
			}
			List<RenderPiece> pieces = renderService.renderHTML(instance.getDashboardMarkup());
			model.renderContent.addAll(pieces);
		}
		return model;
	}
	public RenderMarkupModel previewDashboard(String markup) {
		//only System administrator has permission to read this!
		securityDummy.checkInstanceAdmin();
		
		RenderMarkupModel model = new RenderMarkupModel();
		List<RenderPiece> pieces = renderService.renderHTML(markup);
		model.renderContent.addAll(pieces);
		return model;
	}

	public RenderMarkupModel saveDashboard(String markup) {
		//only System administrator has permission to save this!
		
		InstanceSetting instance = settingService.getInstanceSetting();
		instance.setDashboardMarkup(markup);
		
		settingService.saveOrUpdateInstanceSetting(instance);
		
		return getDashboard(false);
	}



	/*
	 * get current user home page
	 */
	//JDK1.6 @Override
	public PortalModel getPortal(int column) {
		
		securityDummy.checkInstanceRead();
		
		User viewer = WikiUtil.getUser();
		
		PortalModel model;
		if(viewer.isAnonymous()){
			//show system default home page
			model = getDefaultPortal(column);
		}else{
			UserSetting setting = viewer.getSetting();
			List<String> layout = setting.getHomeLayout();
			if(layout != null){
				model = parsePortalLayout(column, layout,viewer);
			}else
				model = getDefaultPortal(column);
		}
		
		return model;
	}

	//JDK1.6 @Override
	public PortalModel getDefaultPortal(int column) {
		InstanceSetting set = settingService.getInstanceSetting();
		List<String> layout = set.getHomeLayout();
		
		User viewer = WikiUtil.getUser();
		return  parsePortalLayout(column, layout,viewer);
	}



	/**
	 * List is string divided by comma, columns are "porletUid, row, column"
	 */
	//JDK1.6 @Override
	public boolean updatePortal(ArrayList<String> list) {
		if(list == null){
			return false;
		}
		
		User user = WikiUtil.getUser();
		//anonymous does not have personal configuration data, skip it.
		if(user == null || user.isAnonymous())
			return false;
		
		//reload user from Database rather than Cache. 
		user = userReadingService.getUser(user.getUid());
		UserSetting setting = user.getSetting();
		if(setting != null){
			setting.setHomeLayout(list);
		}else{
			setting = new UserSetting();
			setting.setHomeLayout(list);
		}
		settingService.saveOrUpdateUserSetting(user, setting);
		
		return true;
	}


	//JDK1.6 @Override
	public boolean updateDefaultPortal(ArrayList<String> list) {
		if(list == null){
			return false;
		}
		
		//reload user from Database rather than Cache. 
		InstanceSetting setting = settingService.getInstanceSetting();
		if(setting != null){
			setting.setHomeLayout(list);
		}else{
			setting = new InstanceSetting();
			setting.setHomeLayout(list);
		}
		settingService.saveOrUpdateInstanceSetting(setting);
		
		return true;
	}

	//JDK1.6 @Override
	public PortletModel invokePortlet(String type, String key) {
		PortletModel model = new PortletModel();
		try {
			User viewer = WikiUtil.getUser();
			Widget renderedWidget = widgetService.invokeWidget(type,key,viewer);
			copyWidgetToPortlet(renderedWidget, model);
		} catch (WidgetException e) {
			model.errorCode = e.getMessage();
		}
		
		
		return model;
	}

	//JDK1.6 @Override
	public PortalModel getListedWidgets(int listSize, int selectPageNumber) {
		PortalModel model = new PortalModel();

		
		User viewer = WikiUtil.getUser();
		List<Widget> widgets = widgetService.getListedWidgets(viewer);
		
		copyWidgetListToPortal(widgets,model);
		return model;
		
	}

	//JDK1.6 @Override
	public PortletModel saveOrUpdateWidget(WidgetModel model) {
		
		Widget widget = widgetService.saveOrUpdateWidget(model.key, model.type, StringUtil.trim(model.getContent())
				, StringUtil.trim(model.title), StringUtil.trim(model.description), model.shared);
		
		PortletModel portlet = new PortletModel();
		copyWidgetToPortlet(widget, portlet);
		
		return portlet;
	}
	
	//JDK1.6 @Override
	public PortletModel removeWidget(String key) {
		
		Widget widget = widgetService.removeWidget(key);
		
		PortletModel portlet = new PortletModel();
		copyWidgetToPortlet(widget, portlet);
		
		return portlet;
	}
	
	//JDK1.6 @Override
	public WidgetModel getWidget(String key){
		Widget widget = widgetService.getWidgetByKey(key);
		WidgetModel model = new WidgetModel();
		model.description = widget.getDescription();
		model.type = widget.getType();
		model.title = widget.getTitle();
		model.shared = widget.isShared();
		model.content = widget.getContent();
		
		return model;
	}

	public WidgetModel addWidgetToDashboardPortal(String widgetType, String widgetKey) {
		Widget widget = widgetService.getWidgetByKey(widgetKey);
		WidgetModel model =  new WidgetModel();
		if(widget == null){
			model.errorCode = ErrorCode.WIDGET_NOT_FOUND;
			return model;
		}
		
		securityDummy.checkInstanceRead();
		User viewer = WikiUtil.getUser();

		//get dashboard content and check if it has {portal} macro first
		InstanceSetting instance = settingService.getInstanceSetting();
		
		int count = 1;
		if(instance == null || StringUtil.isBlank(instance.getDashboardMarkup())
			|| StringUtil.equalsIgnoreCase(StringUtils.trim(instance.getDashboardMarkup()),SharedConstants.DEFAULT_DAHSBOARD_MARKUP)){
			//default dashboard - then it must have {portal} - see SharedContants.DEFAULT_DAHSBOARD_MARKUP
		}else{
			List<RenderPiece> pieces = renderService.renderHTML(instance.getDashboardMarkup());
			for (RenderPiece renderPiece : pieces) {
				if(renderPiece instanceof MacroModel){
					MacroModel rs = ((MacroModel)renderPiece);
					if(SharedConstants.MACRO_PORTAL.equals(rs.macroName)){
					    //here try to confirm one and only one {portal} in Dashboard markup
						count++;
					}
				}
			}
		}
		if(count != 1){
		    //we hope one and only one {portal} in Dashboard markup, otherwise, try to give some error here.
			model.errorCode = ErrorCode.WIDGET_TO_MULTIPLE_PORTAL;
			return model;
		}
		
		if(viewer.isAnonymous()){
			model.errorCode = ErrorCode.WIDGET_ADD_TO_ANONYMOUS_PORTAL;
			return model;
		}else{
			//finally update this user's portal in Dashboard
			
			//reload user from Database rather than Cache. 
            viewer = userReadingService.getUser(viewer.getUid());
            UserSetting setting = viewer.getSetting();
            setting.addWidgetToHomelayout(widgetType, widgetKey);
            settingService.saveOrUpdateUserSetting(viewer, setting);
		}
		
		return model;
	}
	//********************************************************************
	//               private methods
	//********************************************************************

	public static void copyWidgetListToPortal(List<Widget> widgets, PortalModel model) {
		model.portlets = new ArrayList<PortletModel>();
		if(widgets != null){
			for(Widget widget:widgets){
				PortletModel portlet = new PortletModel();
				copyWidgetToPortlet(widget, portlet);
				model.portlets.add(portlet);
			}
		}
	}

	public static void copyWidgetToPortlet(Widget widget, PortletModel model){
		
		model.key = widget.getUuid();
		model.title = widget.getTitle();
		model.type = widget.getType();
		
		model.titleURL = widget.getTitleURL();
		model.description = widget.getDescription();
		model.renderContent = widget.getRenderContent();
		model.perms = widget.getPermimssion();
		
		//so far, only for activityLog portlet
		model.hasNxt = widget.getFlag() > 0;
		model.hasPre = false;
		//start from page 1
		model.currentPage = 1;
	}
	/**
	 * @param layout
	 * @param viewer 
	 * @return
	 */
	private PortalModel parsePortalLayout(int column, List<String> layout, User viewer) {
		PortalModel model;
		model = new PortalModel();
		model.totalColumns = column;
		model.portlets = new ArrayList<PortletModel>(); 
		for (String portletStr : layout) {
			//string separated by ",". Construct by spaceUname or $x, x is the customized protlet type number
			String[] values = portletStr.split("\\"+SharedConstants.PORTLET_SEP);
			if(values.length < 4 ){
				AuditLogger.error("Unexpected case: porlet setting does not have 4 or 5 fields " + portletStr);
				continue;
			}
			
			String type = values[0];
			String key = EscapeUtil.unescapeToken(values[1]);
			//Construct String 4: anyPortletModelType,shortTitle(spaceUname if space type portlet),row,column
			WidgetTemplate widgetTempl = widgetService.getWidget(type);
			if(widgetTempl == null){
				AuditLogger.warn("Unable to get type ["+values[0]+ "] widget");
				//this is fatal/unexpected error... so this widget will be delete from portal.
				continue;
			}
			if(!widgetTempl.isAllowView(viewer)){
				log.info("Widget type ["+values[0]+ "]: key [" + key +"] does not allow user " + viewer.getUsername() + " view. Skip it.");
				
				//TODO: if remove this code, how can forbid for anonymous user view this in default dashboard?
				//cause Resource & permission only limit if widget is invoke or not. It can not hide widget frame.
				continue;
			}
			
			PortletModel portlet = new PortletModel();
			portlet.row = NumberUtils.toInt(values[2]);
			portlet.column = NumberUtils.toInt(values[3]);
			
			if(values.length == 5){
				//this portlet has attributes value
				String propStr = values[4];
				if(!StringUtils.isBlank(propStr)){
					propStr = EscapeUtil.unescapeToken(propStr);
					portlet.attributes = parsePortletAttributes(propStr); 
				}
			}
			
			Widget pObj = widgetTempl.createWidgetObject(key);
			if(pObj == null){
				log.warn("Unable get widget by key " + key +":type"+type+". This may be caused if this widget is already disabled or deleted.");
				portlet.errorCode = ErrorCode.WIDGET_INIT_OBJ_FAILED;
			}else{
				copyWidgetToPortlet(pObj,portlet);
			}
			
			model.portlets.add(portlet);
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               Some general variable for Portal
		model.viewingUser = UserUtil.copyUserToModel(viewer, viewer);
		
		return model;
	}

	/**
	 * @param props
	 * @return
	 */
	private HashMap<String,String> parsePortletAttributes(String propStr) {
		HashMap<String,String> prop = new HashMap<String, String>();
		//string split by \n
		String[] lines = propStr.split("\n");
		for (String line : lines) {
			int sep = line.indexOf("=");
			if(sep == -1){
				AuditLogger.warn("Unexpected - Portlet property has wrong line:" + line);
				continue;
			}
			String name = StringUtils.trimToEmpty(line.substring(0,sep));
			String value = StringUtils.trimToEmpty(line.substring(sep+1));
			
			if(name.length() == 0 || value.length() == 0){
				AuditLogger.warn("Unexpected - Portlet property has wrong line:" + line);
				continue;
			}
			prop.put(name, value);
		}
		
		return prop;
	}

	//********************************************************************
	//               Set  / Get
	//********************************************************************
	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}

	public void setSecurityDummy(SecurityDummy securityDummy) {
		this.securityDummy = securityDummy;
	}
	
	public void setWidgetService(WidgetService widgetService) {
		this.widgetService = widgetService;
	}

	public void setRenderService(RenderService renderService) {
		this.renderService = renderService;
	}

}
