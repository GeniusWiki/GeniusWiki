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
package com.edgenius.wiki.gwt.client.model;

import java.util.HashMap;

import com.edgenius.wiki.gwt.client.server.utils.StringUtil;



/**
 * A simple page fields list for displaying on home page: draft, favorite, watch and recently 
 * @author Dapeng.Ni
 */
public class PageItemModel extends GeneralModel {

	public Integer uid;
	public String uuid;
	public String title;
	public String spaceUname;
	//fullname
	public String modifier;
	public String modifierUsername;
	public long modifiedDate;
	public String modifierPortrait;
	
	public int version;
	//0: is not draft, 1 is manual, 2 auto draft
	public int type;
	//is history version?
	public boolean isCurrent;
	//for client side use only: when user click this item mark it as checked
	public boolean checked;
	
	public boolean pinTop = false;
	
	//This variable uses in UserProfile - list all contributed versions of this user.
	//key:versoin number(or 0==creator, Integer.MAX==current for first and latest version)
	//value: history - only include uid and modifiedDate 2 fields 
	public HashMap<Integer, PageItemModel> versionHistory;
//	draft, favorite, watch and recently updated page
	
	public boolean equals(Object obj){
		if(obj instanceof PageItemModel){
			PageItemModel p = (PageItemModel) obj;
			return StringUtil.equals(p.uuid,this.uuid);
		}
		return false;
	}
	public int hashCode(){
		return uuid!=null?uuid.hashCode():super.hashCode();
	}
}
