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
package com.edgenius.wiki.service;

import java.util.List;

import com.edgenius.core.model.User;
import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.widget.WidgetException;
import com.edgenius.wiki.widget.WidgetTemplate;

/**
 * @author Dapeng.Ni
 */
public interface WidgetService {

	String SERVICE_NAME = "widgetService";
	String saveOrUpdateWidget = "saveOrUpdateWidget";
	String removeWidget = "removeWidget";
	
	/**
	 * @param viewer
	 */
	List<Widget> getListedWidgets(User viewer);

	/**
	 * @param id
	 * @return
	 */
	WidgetTemplate getWidget(String id);

	/**
	 * @param type
	 * @param key
	 * @return
	 */
	Widget invokeWidget(String type, String key, User viewer) throws WidgetException;

	/**
	 * Many WidgetTemplate is cached as spring singleton bean. This method will clean all cached object. In next widget,
	 * invoke, the widgetTemplate will be create again. 
	 */
	void resetWidgets();
	/**
	 * @param key
	 * @return
	 */
	Widget getWidgetByKey(String key);
	/**
	 * If uuid is not null, then update, otherwise, create new widget.
	 * @param uuid
	 * @param type
	 * @param content
	 * @param title
	 * @param description
	 * @param share
	 * @return
	 */
	Widget saveOrUpdateWidget(String uuid, String type, String content, String title, String description, boolean share);
	
	Widget removeWidget(String key);
	
}
