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

import java.util.Collection;
import java.util.List;

/**
 * @author Dapeng.Ni
 */
public class SpaceModel extends GeneralModel  {
	//this field is database record uid, won't be used in any space read case!
	//!!!at moment, only offline model copy this uid form offline DB...!
	public int uid;
	
	public String name;
	public String unixName;
	public String description;
	public long createdDate;
	
	//space permission: refer to WikiPrivilegeStrategy.WikiOPERATIONS
	public int[] permissions = new int[10];
	
	public String themeName;
	
	//useless so far, copy from Space Persist model
	public short type;
	
	public boolean isRemoved;
	public String delayRemoveHours;
	public String tags;
	public String largeLogoUrl;
	public String smallLogoUrl;
	
	//offline space need version check if space updated, this value is space.ModifiedDate.getTime()
	public String homepageUuid;
	
	//offline sync date(server side time only)
	public long syncDate;
	
	public UserModel viewer;

	//attachment quote:[0] used, [1] total
	public long[] quota;
	
	
	public boolean isShellEnabled;
	//decide if show link to shell dialog
	public boolean isShellAutoEnabled;
	//Format like this: http://localhost:8888/theme?instance=1
	public String shellThemeBaseURL;
	public List<String> shellName;
	
	public Collection<BlogMeta> linkBlogMetas;


	
	public String toString(){
		return "Name: " + name + "; SpaceUname: " + unixName + "; descrption: " + description;
	}
}
