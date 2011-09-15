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


/**
 * @author Dapeng.Ni
 */
public class UserBean extends AbstractBean{
	private static final long serialVersionUID = -5252825664735511311L;

	private String username;
	private String fullname;
	//nodeUUID of portrait
	private String portrait;
	
	private String password;

	//********************************************************************
	//               Function method
	//********************************************************************
	public String toString(){
		return username + " : " + fullname;
	}
	public int hashCode(){
		return username != null ?username.hashCode(): 0; 
	}
	public boolean equals(Object obj){
		if(!(obj instanceof UserBean))
			return false;
		
		return username != null ?username.equals(((UserBean)obj).username): false; 
	}	
	//********************************************************************
	//               set / get
	//********************************************************************
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public String getPortrait() {
		return portrait;
	}
	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}
	
}
