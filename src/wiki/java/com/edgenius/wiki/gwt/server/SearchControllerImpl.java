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
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.edgenius.wiki.gwt.client.model.QueryModel;
import com.edgenius.wiki.gwt.client.model.SearchResultItemModel;
import com.edgenius.wiki.gwt.client.model.SearchResultModel;
import com.edgenius.wiki.gwt.client.server.SearchController;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.handler.GWTSpringController;
import com.edgenius.wiki.search.service.RoleSearchService;
import com.edgenius.wiki.search.service.SearchException;
import com.edgenius.wiki.search.service.SearchResult;
import com.edgenius.wiki.search.service.SearchService;
import com.edgenius.wiki.search.service.SpaceSearchService;
import com.edgenius.wiki.search.service.UserSearchService;
import com.edgenius.wiki.search.service.WidgetSpaceSearchService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class SearchControllerImpl extends GWTSpringController implements SearchController {
	private SearchService searchService;
	private SpaceSearchService spaceSearchService;
	private UserSearchService userSearchService;
	private RoleSearchService roleSearchService;
	private WidgetSpaceSearchService widgetSpaceSearchService;
	
	public SearchResultModel search(QueryModel query, int selectPageNumber,int returnCount) {
		
		SearchResultModel model = new SearchResultModel();
		try {
			List<String> adv = new ArrayList<String>();
			
			if(query.keywordType != 0){
				adv.add(Character.valueOf(SearchService.ADV_KEYWORD_TYPE).toString() + query.keywordType);
			}
			if(!StringUtils.isBlank(query.from)){
				adv.add(Character.valueOf(SearchService.ADV_DATE_SCOPE).toString() + StringUtils.trimToEmpty(query.from) + ":" + StringUtils.trimToEmpty(query.to));
			}
			
			if(!StringUtils.isBlank(query.space)){
				adv.add(Character.valueOf(SearchService.ADV_SPACE).toString() + StringUtils.trimToEmpty(query.space));
			}

			if(query.type != 0){
				adv.add(Character.valueOf(SearchService.ADV_SOURCE_TYPES).toString() + query.type);
			}
			if(query.sortBy != 0){
				adv.add(Character.valueOf(SearchService.ADV_GROUP_BY).toString() + query.sortBy);
			}
			
			String[] advQ = null; 
			if(adv.size() > 0)
				advQ = adv.toArray(new String[adv.size()]);
			
			SearchResult searchResult = searchService.search(query.keyword,selectPageNumber,returnCount, WikiUtil.getUser(),advQ);
			SearchUtil.copyResultToModel(searchResult,model);
			return model;
		} catch (SearchException e) {
			model.errorCode = ErrorCode.SEARCH_ERROR;
			return model;
		}
		
	}
	public SearchResultModel searchSpace(String keyword, int selectPageNumber,int returnCount) {
		SearchResultModel model = new SearchResultModel();
		try {
			SearchResult searchResult = spaceSearchService.searchSpace(keyword,selectPageNumber,returnCount, WikiUtil.getUser());
			SearchUtil.copyResultToModel(searchResult,model);
			return model;
		} catch (SearchException e) {
			model.errorCode = ErrorCode.SEARCH_ERROR;
			return model;
		}
		
	}
	//JDK1.6 @Override
	public SearchResultModel searchWidgetSpace(String keyword, int selectPageNumber, int returnCount) {
		SearchResultModel model = new SearchResultModel();
		try {
			SearchResult searchResult = widgetSpaceSearchService.search(keyword,selectPageNumber,returnCount, WikiUtil.getUser());
			SearchUtil.copyResultToModel(searchResult,model);
			return model;
		} catch (SearchException e) {
			model.errorCode = ErrorCode.SEARCH_ERROR;
			return model;
		}
	}
	public SearchResultModel searchUserAndSpace(String keyword, int selectPageNumber, int returnCount) {
		SearchResultModel model = new SearchResultModel();
		try {
			SearchResult searchResult = searchService.search(keyword,selectPageNumber,returnCount, WikiUtil.getUser()
				,Character.valueOf(SearchService.ADV_SOURCE_TYPES).toString()+(SearchService.INDEX_USER|SearchService.INDEX_SPACE));
			SearchUtil.copyResultToModel(searchResult,model);
			return model;
		} catch (SearchException e) {
			model.errorCode = ErrorCode.SEARCH_ERROR;
			return model;
		}
	}

	public SearchResultModel searchUser(String keyword, int selectPageNumber, int returnCount) {
		SearchResultModel model = new SearchResultModel();
		try {
			SearchResult searchResult = userSearchService.searchUser(keyword,selectPageNumber,returnCount, WikiUtil.getUser());
			SearchUtil.copyResultToModel(searchResult,model);
			return model;
		} catch (SearchException e) {
			model.errorCode = ErrorCode.SEARCH_ERROR;
			return model;
		}
		
	}

	
	public SearchResultModel searchRoles(String keyword, int selectPageNumber, int returnCount) {
		SearchResultModel model = new SearchResultModel();
		try {
			SearchResult searchResult = roleSearchService.searchRole(keyword,selectPageNumber,returnCount, WikiUtil.getUser());
			
			//There are 2 types of role, one is space role, another is system role.  Space role can search by space
			SearchUtil.copyResultToModel(searchResult,model);
			
			//some sucks hacker... just following SearchResult and convert Space Type role to corresponding type
			for (SearchResultItemModel resultItem : model.results) {
				if(resultItem.type == SharedConstants.SEARCH_SPACE){
					resultItem.type = SharedConstants.ROLE_TYPE_SPACE;
					resultItem.spaceUname = SharedConstants.ROLE_SPACE_PREFIX+resultItem.spaceUname;
					resultItem.title = SharedConstants.SPACE_ROLE_DEFAULT_PREFIX + resultItem.title;
					resultItem.desc = SharedConstants.SPACE_ROLE_DEFAULT_PREFIX + resultItem.title;
				}
			}
			
			return model;
		} catch (SearchException e) {
			model.errorCode = ErrorCode.SEARCH_ERROR;
			return model;
		}
	}
	//********************************************************************
	//               Set methods
	//********************************************************************
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}


	public void setSpaceSearchService(SpaceSearchService spaceSearchService) {
		this.spaceSearchService = spaceSearchService;
	}
	public void setUserSearchService(UserSearchService userSearchService) {
		this.userSearchService = userSearchService;
	}
	/**
	 * @param roleSearchService the roleSearchService to set
	 */
	public void setRoleSearchService(RoleSearchService roleSearchService) {
		this.roleSearchService = roleSearchService;
	}
	public void setWidgetSpaceSearchService(WidgetSpaceSearchService widgetSpaceSearchService) {
		this.widgetSpaceSearchService = widgetSpaceSearchService;
	}

}
