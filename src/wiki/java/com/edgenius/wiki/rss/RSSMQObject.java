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
package com.edgenius.wiki.rss;

import java.io.Serializable;

/**
 * @author Dapeng.Ni
 */
public class RSSMQObject implements Serializable{
	private static final long serialVersionUID = 3510192649277451983L;
	
	public static final int TYPE_REBUILD = 1;
	public static final int TYPE_DELETE = 2;
	//only remove page item, maybe this item does not exist in RSS, do nothing then.
	public static final int TYPE_ITEM_REMOVE = 3;


	private int type;
	
	private Integer spaceUid;
	private String spaceUname;
	private String removePageUuid;
	//default constructor for serializable
	public RSSMQObject(){
		
	}
	public RSSMQObject(int type, String spaceUname){
		this.type = type;
		this.spaceUname = spaceUname;
	}
	public RSSMQObject(int type, Integer spaceUid){
		this.type = type;
		this.spaceUid = spaceUid;
	}

	/**
	 * @param typeItemRemove
	 * @param fromSpaceUname
	 * @param removedPageUuid
	 */
	public RSSMQObject(int type, String spaceUname, String removedPageUuid) {
		this(type,spaceUname);
		this.removePageUuid = removedPageUuid;
		
	}
	public int getType() {
		return type;
	}

	public String getRemovePageUuid() {
		return removePageUuid;
	}
	public String getSpaceUname() {
		return spaceUname;
	}
	public Integer getSpaceUid() {
		return spaceUid;
	}
}
