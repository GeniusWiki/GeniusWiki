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
package com.edgenius.wiki.gwt.client.server.utils;

import java.io.Serializable;


/**
 * @author Dapeng.Ni
 */
public class PageAttribute implements Serializable{
	private static final long serialVersionUID = -8996331986761972871L;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Those attribute are special: they can not independent, means, they need not use bit OR to merge with others
	//NOTE: these are maybe not put into Attribute field. But because of no conflict with others, so just left it as is so far
	//AND those value are set when a link click, and value given according to PageLink.FLAG, then pass those value to server side to 
	//allow savePage() knows it is home page or not.
	public static final int NEW_HOMEPAGE = -4;
	public static final int NEW_PAGE = -3;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//               This field is declare prefix of all possible page attribute fields name prefix.
	//!!!!!!!! IMPORTANT: following fields with NO_XXX is corresponding with PageAttributeMacro, please be carefully when any new add/remove/modify
	public static final String ATTRIBUTE_PREFIX = "NO_";
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Those attribute can merge by bit OR
	//Identify which widget will show on page: to decide what function will show together with this page permission.
	// no create button, but if the page has link to 
	public static final int NO_CREATE = 1;
	//even the page has non-exist-page-link, it dose not allow to create.??? Does this useful?
	public static final int NO_LINK_CREATE = 1 << 1;
	//not allow to edit, move and change permission
	public static final int NO_EDIT = 1 << 2;
	public static final int NO_MOVE = 1 << 3;
	public static final int NO_PERMISSION = 1 << 4;
	public static final int NO_COMMENT = 1 << 5;
	public static final int NO_HISTORY= 1 << 6;
	//this page also hide "attachment,   
	public static final int NO_ATTACHMENT= 1 << 7;
	//add favorite
	public static final int NO_FAVORITE= 1 << 8;
	//no that drop down menu
	public static final int NO_MENU= 1 << 9;
	public static final int NO_COPY= 1 << 10;
	public static final int NO_PRINT= 1 << 11;
	public static final int NO_REMOVE = 1<<12;

	//no title, tag, author info 
	public static final int NO_TITLE = 1<<13;
	public static final int NO_TAG = 1<<14;
	public static final int NO_CREATOR = 1<<15;
	
	//does not allow save page as draft
	public static final int NO_CREATE_DRAFT = 1 << 16;
	
	public static final int NO_RSS = 1<<17;
	//add watched" function.
	public static final int NO_WATCHED= 1 << 18;
	public static final int NO_MODIFIER = 1<<19;
	
	public static final int NO_SIDE_BAR = 1<<20;
	public static final int NO_CHILDREN = 1<<21;
	
	//this page also hide "attachment,   
	public static final int NO_PRETTY_URL = 1 << 22;	//this page also hide "PrettyURL",   
	public static final int NO_EXPORT = 1<<23;
	public static final int NO_SPACE_MENU = 1<<25;
	//this include all page tab bar, such as comments, history and children
	public static final int NO_UNDER_TAB_BAR = NO_CHILDREN|NO_COMMENT|NO_HISTORY;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//this is pre-defined value, so far, used in TagCloud, Page_NOT_FOUND, HOME_PAGE_NOT_FOUND, and UserProfile page.
	//this value contains all of them except "NO_LINK_CREATE"
	public static final int FUNCTION_PAGE = NO_CREATE|NO_EDIT|NO_MOVE|NO_PERMISSION|NO_COMMENT|NO_HISTORY|NO_CHILDREN|NO_REMOVE
										|NO_ATTACHMENT|NO_FAVORITE|NO_WATCHED|NO_MENU|NO_COPY|NO_PRINT|NO_TAG|NO_CREATOR|NO_MODIFIER
										|NO_CREATE_DRAFT|NO_RSS|NO_SIDE_BAR|NO_UNDER_TAB_BAR|NO_PRETTY_URL|NO_EXPORT|NO_SPACE_MENU;
	
	public static final int OFFLINE_HISTORY = PageAttribute.NO_MENU| PageAttribute.NO_FAVORITE|PageAttribute.NO_WATCHED
						|PageAttribute.NO_ATTACHMENT |PageAttribute.NO_TAG|PageAttribute.NO_RSS|PageAttribute.NO_EXPORT;

	//these attribute will merge with page itself's attribute
	public static final int OFFLINE_PAGE = PageAttribute.NO_MENU|PageAttribute.NO_FAVORITE|PageAttribute.NO_WATCHED
					|PageAttribute.NO_RSS|PageAttribute.NO_EXPORT;

	
}
