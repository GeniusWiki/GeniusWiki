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

import java.io.Serializable;
import java.util.List;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class SearchResult implements Serializable{

	private List<SearchResultItem> items;
	private float timeSecond;
	private int totalPage;
	private int totalItem;
	private int currentPage;
	private String keyword;
	
	public List<SearchResultItem> getItems() {
		return items;
	}
	public void setItems(List<SearchResultItem> items) {
		this.items = items;
	}
	public int getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(int total) {
		this.totalPage = total;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int select) {
		this.currentPage = select;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public float getTimeSecond() {
		return timeSecond;
	}
	public void setTimeSecond(float timeSecond) {
		this.timeSecond = timeSecond;
	}
	public int getTotalItem() {
		return totalItem;
	}
	public void setTotalItem(int totalItem) {
		this.totalItem = totalItem;
	}
}
