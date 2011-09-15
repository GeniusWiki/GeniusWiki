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
package com.edgenius.wiki.integration.rest.model;

import java.util.List;


/**
 * @author Dapeng.Ni
 */
public class SpaceBean extends AbstractBean{
	private static final long serialVersionUID = 9082294955659569041L;
	
	private String unixName;
	private String name;
	private String description;
	
	private String homepageUuid;
	
	private List<TagBean> tags;
	
	//large logo file UUID;
	private String logo;
	
	private String menuRender;
	
	//********************************************************************
	//               Function method
	//********************************************************************
	public String toString(){
		return unixName;
	}
	public int hashCode(){
		return unixName != null ?unixName.hashCode(): 0; 
	}
	public boolean equals(Object obj){
		if(!(obj instanceof SpaceBean))
			return false;
		
		return unixName != null ?unixName.equals(((SpaceBean)obj).unixName): false; 
	}
	//********************************************************************
	//               set / get
	//********************************************************************
	public String getUnixName() {
		return unixName;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setUnixName(String unixName) {
		this.unixName = unixName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public List<TagBean> getTags() {
		return tags;
	}
	public void setTags(List<TagBean> tags) {
		this.tags = tags;
	}
	public String getHomepageUuid() {
		return homepageUuid;
	}
	public void setHomepageUuid(String homepageUuid) {
		this.homepageUuid = homepageUuid;
	}
	public String getMenuRender() {
		return menuRender;
	}
	public void setMenuRender(String menuRender) {
		this.menuRender = menuRender;
	}
	
}
