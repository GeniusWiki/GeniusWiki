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
package com.edgenius.wiki.gwt.client;

import com.edgenius.wiki.gwt.client.offline.controller.CommentOfflineControllerImpl;
import com.edgenius.wiki.gwt.client.offline.controller.PageOfflineControllerImpl;
import com.edgenius.wiki.gwt.client.offline.controller.PortalOfflineControllerImpl;
import com.edgenius.wiki.gwt.client.offline.controller.SecurityOfflineControllerImpl;
import com.edgenius.wiki.gwt.client.offline.controller.TagOfflineControllerImpl;
import com.edgenius.wiki.gwt.client.server.CommentController;
import com.edgenius.wiki.gwt.client.server.CommentControllerAsync;
import com.edgenius.wiki.gwt.client.server.HelperController;
import com.edgenius.wiki.gwt.client.server.HelperControllerAsync;
import com.edgenius.wiki.gwt.client.server.NotificationController;
import com.edgenius.wiki.gwt.client.server.NotificationControllerAsync;
import com.edgenius.wiki.gwt.client.server.OfflineController;
import com.edgenius.wiki.gwt.client.server.OfflineControllerAsync;
import com.edgenius.wiki.gwt.client.server.PageController;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.PluginController;
import com.edgenius.wiki.gwt.client.server.PluginControllerAsync;
import com.edgenius.wiki.gwt.client.server.PortalController;
import com.edgenius.wiki.gwt.client.server.PortalControllerAsync;
import com.edgenius.wiki.gwt.client.server.SearchController;
import com.edgenius.wiki.gwt.client.server.SearchControllerAsync;
import com.edgenius.wiki.gwt.client.server.SecurityController;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.server.SpaceController;
import com.edgenius.wiki.gwt.client.server.SpaceControllerAsync;
import com.edgenius.wiki.gwt.client.server.TagController;
import com.edgenius.wiki.gwt.client.server.TagControllerAsync;
import com.edgenius.wiki.gwt.client.server.TemplateController;
import com.edgenius.wiki.gwt.client.server.TemplateControllerAsync;
import com.edgenius.wiki.gwt.client.server.ThemeController;
import com.edgenius.wiki.gwt.client.server.ThemeControllerAsync;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;


/**
 * Don't put controller generator method to each Controller interface because offline controller
 * may be created in that method, but it is not expected compiled into wiki jar file.
 * @author Dapeng.Ni
 */
public class ControllerFactory {

	public static CommentControllerAsync getCommentController(){
		CommentControllerAsync service;
		if(AbstractEntryPoint.isOffline()){
			service = (CommentControllerAsync) GWT.create(CommentOfflineControllerImpl.class);
		}else{
			service = (CommentControllerAsync) GWT.create(CommentController.class);
			ServiceDefTarget endpoint = (ServiceDefTarget) service;
			String baseUrl = GwtClientUtils.getBaseUrl();
			endpoint.setServiceEntryPoint(baseUrl + CommentController.MODULE_ACTION_URI);
		}
		return service;
	}

	public static PortalControllerAsync getPortalController(){
		
		PortalControllerAsync service;
		if(AbstractEntryPoint.isOffline()){
			service = (PortalControllerAsync) GWT.create(PortalOfflineControllerImpl.class);
		}else{
			service = (PortalControllerAsync) GWT.create(PortalController.class);
			ServiceDefTarget endpoint = (ServiceDefTarget) service;
			String baseUrl = GwtClientUtils.getBaseUrl();
			endpoint.setServiceEntryPoint(baseUrl + PortalController.MODULE_ACTION_URI);
		}
		return service;
	}

	public static PageControllerAsync getPageController(){
		PageControllerAsync service;
		if(AbstractEntryPoint.isOffline()){
			service = (PageControllerAsync) GWT.create(PageOfflineControllerImpl.class);
		}else{
			service = (PageControllerAsync) GWT.create(PageController.class);
			ServiceDefTarget endpoint = (ServiceDefTarget) service;
			String baseUrl = GwtClientUtils.getBaseUrl();
			endpoint.setServiceEntryPoint(baseUrl + PageController.MODULE_ACTION_URI);
		}
		return service;
	}

	public static TagControllerAsync getTagController(){
		TagControllerAsync service;
		if(AbstractEntryPoint.isOffline()){
			service = (TagControllerAsync) GWT.create(TagOfflineControllerImpl.class);
		}else{
			service = (TagControllerAsync) GWT.create(TagController.class);
			ServiceDefTarget endpoint = (ServiceDefTarget) service;
			String baseUrl = GwtClientUtils.getBaseUrl();
			endpoint.setServiceEntryPoint(baseUrl + TagController.MODULE_ACTION_URI);
		}
		return service;
	}
	public static HelperControllerAsync getHelperController(){
		HelperControllerAsync service = (HelperControllerAsync) GWT.create(HelperController.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) service;
		String baseUrl = GwtClientUtils.getBaseUrl();
		endpoint.setServiceEntryPoint(baseUrl + HelperController.MODULE_ACTION_URI);
		return service;
	}

	public static ThemeControllerAsync getThemeController(){
		ThemeControllerAsync service = (ThemeControllerAsync) GWT.create(ThemeController.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) service;
		String baseUrl = GwtClientUtils.getBaseUrl();
		endpoint.setServiceEntryPoint(baseUrl + ThemeController.MODULE_ACTION_URI);
		return service;
	}


	public static SpaceControllerAsync getSpaceController(){
		SpaceControllerAsync service = (SpaceControllerAsync) GWT.create(SpaceController.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) service;
		String baseUrl = GwtClientUtils.getBaseUrl();
		endpoint.setServiceEntryPoint(baseUrl + SpaceController.MODULE_ACTION_URI);
		return service;
	}

	public static SecurityControllerAsync getSecurityController(){
		SecurityControllerAsync  service;
		if(AbstractEntryPoint.isOffline()){
			service = (SecurityControllerAsync) GWT.create(SecurityOfflineControllerImpl.class);
		}else{
			service = (SecurityControllerAsync) GWT.create(SecurityController.class);
			ServiceDefTarget endpoint = (ServiceDefTarget) service;
			String baseUrl = GwtClientUtils.getBaseUrl();
			endpoint.setServiceEntryPoint(baseUrl + SecurityController.MODULE_ACTION_URI);
		}
		return service;
	}

	public static SearchControllerAsync getSearchController(){
		SearchControllerAsync service = (SearchControllerAsync) GWT.create(SearchController.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) service;
		String baseUrl = GwtClientUtils.getBaseUrl();
		endpoint.setServiceEntryPoint(baseUrl + SearchController.MODULE_ACTION_URI);
		return service;
	}

	public static PluginControllerAsync getPluginController(){
		PluginControllerAsync service = (PluginControllerAsync) GWT.create(PluginController.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) service;
		String baseUrl = GwtClientUtils.getBaseUrl();
		endpoint.setServiceEntryPoint(baseUrl + PluginController.MODULE_ACTION_URI);
		return service;
	}

	public static OfflineControllerAsync getOfflineController(){
		OfflineControllerAsync service = (OfflineControllerAsync) GWT.create(OfflineController.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) service;
		String baseUrl = GwtClientUtils.getBaseUrl();
		endpoint.setServiceEntryPoint(baseUrl + OfflineController.MODULE_ACTION_URI);
		return service;
	}

	public static NotificationControllerAsync getNotificationController(){
		NotificationControllerAsync service = (NotificationControllerAsync) GWT.create(NotificationController.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) service;
		String baseUrl = GwtClientUtils.getBaseUrl();
		endpoint.setServiceEntryPoint(baseUrl + NotificationController.MODULE_ACTION_URI);
		return service;
	}

	/**
	 * @return
	 */
	public static TemplateControllerAsync getTemplateController() {
		TemplateControllerAsync service = (TemplateControllerAsync) GWT.create(TemplateController.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) service;
		String baseUrl = GwtClientUtils.getBaseUrl();
		endpoint.setServiceEntryPoint(baseUrl + TemplateController.MODULE_ACTION_URI);
		return service;
	}

}
