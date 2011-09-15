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

/**
 * Although most case are case insensitive... but TinyMCE requires lower case as attribute name, so that I have to put them all to lower case
 * Lower case becomes constant standard across system!!! 
 * some constants used in tag attributes, tag name, macro parameter name etc..
 * @author Dapeng.Ni
 */
public class NameConstants {
	public static final String TYPE = "type";
	public static final String LINK = "link";
	public static final String ANCHOR = "anchor";
	public static final String HREF = "href";
	
	public static final String SRC = "src";
	public static final String TITLE = "title";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String ALIGN = "align";
	public static final String STYLE = "style";
	public static final String FILENAME = "filename";
	public static final String ALT = "alt";
	public static final String FLOAT = "float";
	public static final String CLASS = "class";
	public static final String WAJAX = "wajax";
	public static final String AID = "aid";
	public static final String KEYWORD = "keyword";
	public static final String NAME = "name";
	public static final String COUNT = "count";
	//show in ordered or unordered
	public static final String ORDERED = "ordered";
	public static final String DEEP = "deep";
	public static final String VIEW = "view";
	public static final String SPACE = "SPACE";
	public static final String CODE = "code";
	public static final String SOURCE = "source";
	public static final String CREATOR = "creator";
	public static final String MODIFIER = "modifier";
	public static final String SPACEUNAME = "spaceuname";
	public static final String ANAME = "aname";
	public static final String TEXT_ALIGN = "text-align";
	public static final String INLINE = "inline";
	public static final String DISPLAY = "display";
	public static final String CONTENT = "content";
	
	public static final String COLOR = "color";
	public static final String BKCOLOR = "bkcolor";
	public static final String FONT = "font";
	public static final String SIZE = "size";
	public static final String HASTITLE = "hastitle";
	public static final String IMAGE = "image";
	public static final String ON = "on";
	public static final String LOGOUT = "logout";
	public static final String LOGIN = "login";
	public static final String SHOWLOGO = "showlogo";
	public static final String COLUMNS = "columns";
	public static final String COLUMN = "column";
	public static final String COLSPAN = "colspan";
	public static final String ROWSPAN = "rowspan";
	
	//these 3 are hardcode in javascript - tinyMCE openMessageDialog in wbmacro plugin
	public static final String ERROR = "error";
	public static final String INFO = "info";
	public static final String WARNING = "warning";
	public static final String MACRO = "macro";
	public static final String USERNAME = "username";
	public static final String ID = "id";
	public static final String ORDER = "order";
	public static final String PARENT = "parent";
	public static final String PARENT_UUID = "parentuuid";

}
