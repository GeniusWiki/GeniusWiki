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

import com.edgenius.core.model.User;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;

/**
 * @author Dapeng.Ni
 */
public interface SearchService {
	String SERVICE_NAME = "searchService";

	
	//following by keyword type - the keyword string is from first parameter of this method
	char ADV_KEYWORD_TYPE = '1';
	//space key name, only one space 
	char ADV_SPACE = '2';
	//0-none(ignore), page, space, comment, user, attachment, tag-on-page, tag-on-space, role, widget
	char ADV_SOURCE_TYPES = '3';
	
	//from:to - To is optional, default is now. date is Date.getTime(), i.e., type is long 
	char ADV_DATE_SCOPE = '4';
	//0-none(ignore), 1 - type or 2 - space 
	char ADV_GROUP_BY = '5';
	
	//these values use in ADV_SOURCE_TYPES
	int KEYWORD_ANY = SharedConstants.ADV_SEARCH_KEYWORD_ANY; //default
	int KEYWORD_ALL = SharedConstants.ADV_SEARCH_KEYWORD_ALL;  
	int KEYWORD_EXACT = SharedConstants.ADV_SEARCH_KEYWORD_EXACT; 
	int KEYWORD_EXCEPT = SharedConstants.ADV_SEARCH_KEYWORD_EXCEPT; 
	
	//these values use in ADV_SOURCE_TYPES
	int INDEX_PAGE = SharedConstants.ADV_SEARCH_INDEX_PAGE;
	int INDEX_SPACE = SharedConstants.ADV_SEARCH_INDEX_SPACE;
	int INDEX_COMMENT = SharedConstants.ADV_SEARCH_INDEX_COMMENT;
	int INDEX_USER = SharedConstants.ADV_SEARCH_INDEX_USER;
	int INDEX_ATTACHMENT = SharedConstants.ADV_SEARCH_INDEX_ATTACHMENT;
	int INDEX_TAGONPAGE = SharedConstants.ADV_SEARCH_INDEX_TAGONPAGE;
	int INDEX_TAGONSPACE = SharedConstants.ADV_SEARCH_INDEX_TAGONSPACE;
	int INDEX_ROLE = SharedConstants.ADV_SEARCH_INDEX_ROLE;
	int INDEX_WIDGET = SharedConstants.ADV_SEARCH_INDEX_WIDGET;
	
	//these values use in ADV_GROUP_BY
	int GROUP_TYPE = SharedConstants.ADV_SEARCH_GROUP_TYPE;
	int GROUP_SPACE = SharedConstants.ADV_SEARCH_GROUP_SPACE;
	
	/**
	 * The last advance search is optional except user input advance search query criteria. The advance parameters
	 * is combination of above ADV_foo field and its associated value. For example,
	 * 12 (exactly match keyword from 1st parameter of this method) 
	 * 2MySpace(search from space with space key is "MySpace",
	 * 35(search only for page and comment,5 - is INDEX_PAGE|INDEX_COMMENT value), 
	 * 4123123123:456456456(search from date time is 123123123, end by 456456456)
	 */
	SearchResult search(String keyword, int selectPageNumber,int returnCount, User user, String... advance) throws SearchException ;
}
