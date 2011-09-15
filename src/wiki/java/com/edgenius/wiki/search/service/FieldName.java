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
package com.edgenius.wiki.search.service;


/**
 * @author Dapeng.Ni
 */
public class FieldName {

	public static final String KEY = "key";
	public static final String DOC_TYPE = "docType";
	public static final String UPDATE_DATE = "udpateDate";
	public static final String CONTENT = "content";
	
	public static final String PAGE_UUID = "puuid";
	public static final String PAGE_TITLE = "ptitle";
	public static final String PAGE_CONTENT = "pcontent";
	
	//page, page tag, must use this field to save spaceUname as un-search field.Otherwise, all page, 
	//tag will return under this Space UnixName, it maybe huge result and unexpected 
	//this name is different with SPACE_UNIXNAME, the reason is SEARCH_ALL_FILEDS does not need on this field build
	//but SPACE_UNIXNAME should appear on SEARCH_ALL_FILEDS
	public static final String UNSEARCH_SPACE_UNIXNAME = "spaceuname";
	public static final String UNSEARCH_PAGE_TITLE = "pagetitle";
	public static final String SPACE_UNIXNAME = "uname";
	public static final String SPACE_NAME = "name";
	public static final String SPACE_DESC = "desc";
	
	public static final String PAGE_TAG_NAME = "pname";
	public static final String SPACE_TAG_NAME = "sname";
	
	public static final String USER_NAME = "user";
	public static final String USER_FULLNAME = "fullname";
	

	public static final String ROLE_NAME = "rolename";
	public static final String ROLE_DISPLAY_NAME = "roledisname";
	public static final String ROLE_DESC = "roledesc";
	
	public static final String FILE_NODE_UUID = "nodeuuid";
	public static final String FILE_NAME = "name";
	public static final String FILE_COMMENT = "comment";
	public static final String FILE_CONTENT = "content";
	public static final String FILE_SHARED = "shared";
	
	public static final String WIDGET_KEY = "wkey";
	public static final String WIDGET_TYPE = "wtype";
	public static final String WIDGET_TITLE = "wtitle";
	public static final String WIDGET_DESC = "wdesc";
	public static final String WIDGET_CONTENT = "wcontent";
	
	public static final String COMMENT_UID = "commuid";
	public static final String TEXT = "text";
	public static final String CONTRIBUTOR = "contributor";

}
