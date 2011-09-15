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
package com.edgenius.wiki.gwt.client.model;

import java.util.HashMap;





/**
 * Indicator for portlet object.
 * @author Dapeng.Ni
 */
public class PortletModel extends GeneralModel {

	public static final String SPACE = "com.edgenius.wiki.widget.SpaceWidget";
	public static final String DRAFT_LIST = "com.edgenius.wiki.widget.MyDraftWidget";
	public static final String WATCHED_LIST = "com.edgenius.wiki.widget.MyWatchedWidget";
	public static final String FAVORITE_LIST = "com.edgenius.wiki.widget.MyFavoriteWidget";
	public static final String MESSAGE_BOARD = "com.edgenius.wiki.widget.MyMessageWidget";
	public static final String QUICK_BOARD = "com.edgenius.wiki.widget.QuickNoteWidget";
	public static final String ACTIVITYLOG_BOARD = "com.edgenius.wiki.widget.ActivityLogWidget";
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//               These fields are only for portlet, not from widgetObject
	public int column;
	public int row;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//               These attribute will come from WidgetObject or Space
	//display text on portlet head
	public String title;
	public String type;
	//spaceUname, widget uuid, my xxx(message, draft etc) portlet short name
	public String key;
	//hover hint text on portlet link 
	public String description;
	//portlet attributes
	public HashMap<String, String> attributes;

	//if portlet title could go to another link, then give this URL
	public String titleURL;
	
	public GeneralModel renderContent;
	public boolean shared = true;
	
	//read or write this widgets
	public int[] perms;

	//For pagination - only used by ActivityPortlet, so far.
	public boolean hasPre;
	public boolean hasNxt;
	public int currentPage;


}
