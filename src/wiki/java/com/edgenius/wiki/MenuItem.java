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
package com.edgenius.wiki;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Space menu item data container object.
 * @author Dapeng.Ni
 */
public class MenuItem implements Serializable{
	private static final long serialVersionUID = -7318928252652308441L;
	
	private String title;
	private String pageTitle;
	private String pageUuid;
	//This is parent UUID
	private String parent;
	private int order;
	//********************************************************************
	//               Function
	//********************************************************************
	public MenuItem(){}
	public MenuItem(String pageUuid){
		this.pageUuid = pageUuid;
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof MenuItem))
			return false;
		
		MenuItem other = (MenuItem) obj;
		
		//if same page, then equals
		return new EqualsBuilder()
				.append(pageUuid, other.pageUuid).isEquals();
	}
	
	public int hashCode(){
		
		return new HashCodeBuilder().append(pageUuid).toHashCode();
		
	}
	
	//********************************************************************
	//               Function cloass
	//********************************************************************
	/**
	 * Only ensure same parent menu item has correct order. Doesn't ensure parent relation. 
	 */
	public static class MenuItemComparator implements Comparator<MenuItem>{
		public int compare(MenuItem o1, MenuItem o2) {
			if(StringUtils.equals(o1.getPageUuid(), o2.getPageUuid()))
				return 0;
			
			int sort = 0;
			if(StringUtils.equals(o1.getParent(), o2.getParent())){
				sort = o1.getOrder() - o2.getOrder();
			}
			
			return sort == 0?1:sort;
		}
	}
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPageTitle() {
		return pageTitle;
	}
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}

	public String getPageUuid() {
		return pageUuid;
	}

	public void setPageUuid(String pageUuid) {
		this.pageUuid = pageUuid;
	}

}
