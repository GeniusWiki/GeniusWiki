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

import java.io.Serializable;

import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.model.Space;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class NotifyMQObject implements Serializable{
	public static final int TYPE_SPACE_REMOVE = 1;
	public static final int TYPE_PAGE_UPDATE = 2;
	public static final int TYPE_COMMENT_NOTIFY = 3;
	public static final int TYPE_EXT_LINK_BLOG = 4;
	public static final int TYPE_EXT_POST_COMMENT = 5;
	public static final int TYPE_EXT_POST = 6;
	public static final int TYPE_EXT_REMOVE_POST = 7;
	public static final int TYPE_SPACE_MEUN_UPDATED = 8;
	
	private int type;
	

	private Space space;
	
	private Integer pageUid;
	private int removeDelayHours;
	
	private BlogMeta blogMeta;
	private int syncLimit;
	private String spaceUname;
	private String username;
	private String id;
	

	public NotifyMQObject(int type, String loginUser, Integer pageUid) {
		this.type = type;
		this.username = loginUser;
		this.pageUid = pageUid;
	}
	public NotifyMQObject(String loginUser, Space space, int removeDelayHours) {
		this.username = loginUser;
		this.space = space;
		this.removeDelayHours = removeDelayHours;
		this.type = TYPE_SPACE_REMOVE;
	}
	
	public NotifyMQObject(String loginUser, String spaceUname, BlogMeta blogMeta, int syncLimit) {
		this.username = loginUser;
		this.spaceUname = spaceUname;
		this.blogMeta = blogMeta;
		this.syncLimit = syncLimit;
		this.type = TYPE_EXT_LINK_BLOG;
	}

	public NotifyMQObject(int type, String loginUser, BlogMeta meta, String id) {
		this.type = type;
		this.username = loginUser;
		this.blogMeta = meta;
		this.id = id;
	}


	/**
	 * @param typeSpaceMeunUpdated
	 * @param unixName
	 * @param menuItems
	 */
	public NotifyMQObject(int type, String spaceUname) {
		this.type = type;
		this.spaceUname = spaceUname;
	}
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setRemoveDelayHours(int removeDelayHours) {
		this.removeDelayHours = removeDelayHours;
	}
	public int getRemoveDelayHours() {
		return removeDelayHours;
	}
	public Space getSpace() {
		return space;
	}
	public void setSpace(Space space) {
		this.space = space;
	}

	public Integer getPageUid() {
		return pageUid;
	}
	public void setPageUid(Integer pageUid) {
		this.pageUid = pageUid;
	}
	public BlogMeta getBlogMeta() {
		return blogMeta;
	}
	public void setBlogMeta(BlogMeta blogMeta) {
		this.blogMeta = blogMeta;
	}
	public int getSyncLimit() {
		return syncLimit;
	}
	public void setSyncLimit(int syncLimit) {
		this.syncLimit = syncLimit;
	}
	public String getSpaceUname() {
		return spaceUname;
	}
	public void setSpaceUname(String spaceUname) {
		this.spaceUname = spaceUname;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	
}
