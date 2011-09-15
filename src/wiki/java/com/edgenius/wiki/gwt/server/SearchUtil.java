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

import org.apache.commons.lang.math.NumberUtils;

import com.edgenius.wiki.gwt.client.model.SearchResultItemModel;
import com.edgenius.wiki.gwt.client.model.SearchResultModel;
import com.edgenius.wiki.search.service.SearchResult;
import com.edgenius.wiki.search.service.SearchResultItem;

/**
 * @author Dapeng.Ni
 */
public class SearchUtil {

	public static void copyResultToModel(SearchResult result, SearchResultModel model) {
		if(result == null)
			return;
		
		model.currPage = result.getCurrentPage();
		model.timeSecond = result.getTimeSecond();
		model.totalItems = result.getTotalItem();
		model.totalPage = result.getTotalPage();
		
		ArrayList<SearchResultItemModel> list = new ArrayList<SearchResultItemModel>();
		List<SearchResultItem> items = result.getItems();
		if(items != null){
			for (SearchResultItem resultItem : items) {
				SearchResultItemModel itemModel = new SearchResultItemModel();
				itemModel.fragment = resultItem.getFragment();
				itemModel.itemUid = resultItem.getItemUid();
				itemModel.type = resultItem.getType();
				itemModel.title = resultItem.getTitle();
				itemModel.spaceUname = resultItem.getSpaceUname();
				itemModel.desc = resultItem.getDesc();
				itemModel.date = NumberUtils.toLong(resultItem.getDatetime());
				itemModel.contributor = resultItem.getContributor();
				itemModel.contributorUsername = resultItem.getContributorUsername();
				list.add(itemModel);
			}
		}
		model.results = list;
	}

}
