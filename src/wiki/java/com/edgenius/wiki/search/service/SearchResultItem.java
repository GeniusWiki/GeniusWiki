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

/**
 * @author Dapeng.Ni
 */
public class SearchResultItem implements Serializable{

	private static final long serialVersionUID = 2876937754261592612L;
	
	//page,tag,user,attachment
	private int type;
	//page Uid or userUid etc.
	private String itemUid;
	private String spaceUname;
	//text display as link, could be pageTitle; TagName; UserName; Space name;attachment file name; 
	private String title;
	
	//space description, attachment comment, userfullname
	private String desc;
	
	//all searched fields, page title, content; space uname, desc; tag name; user fullname; attachment file content;
	private String fragment;
	
	//this could be update or create time, depends on the search type
	private String datetime;
	
	//contributor fullname
	private String contributor;
	
	private String ContributorUsername;
	
	public String getContributorUsername() {
		return ContributorUsername;
	}
	public void setContributorUsername(String contributorUsername) {
		ContributorUsername = contributorUsername;
	}
	public String getContributor() {
		return contributor;
	}
	public void setContributor(String contributor) {
		this.contributor = contributor;
	}
	public String getDatetime() {
		return datetime;
	}
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getItemUid() {
		return itemUid;
	}
	public void setItemUid(String key) {
		this.itemUid = key;
	}
	public String getSpaceUname() {
		return spaceUname;
	}
	public void setSpaceUname(String spaceUname) {
		this.spaceUname = spaceUname;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String pageTitle) {
		this.title = pageTitle;
	}
	public String getFragment() {
		return fragment;
	}
	public void setFragment(String fragment) {
		this.fragment = fragment;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}

	
}
