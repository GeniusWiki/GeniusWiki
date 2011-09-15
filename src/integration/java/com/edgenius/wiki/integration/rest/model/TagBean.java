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

import java.io.Serializable;

/**
 * @author Dapeng.Ni
 */
public class TagBean implements Serializable{
	private static final long serialVersionUID = 7339424704016019332L;
	public static final int TYPE_PAGE = 1;
	public static final int TYPE_SPACE = 2;
	private String name;
	
	//space or page
	private int type;
	
	//how many items with this tag - so far not return?
	private int count;
	//********************************************************************
	//               Function method
	//********************************************************************
	public String toString(){
		return name + ":" + type;
	}
	public int hashCode(){
		return name != null ?(name.hashCode()+type): 0; 
	}
	public boolean equals(Object obj){
		if(!(obj instanceof TagBean))
			return false;
		
		return name != null ?(type == ((TagBean)obj).type && name.equals(((TagBean)obj).name)): false; 
	}
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

}
