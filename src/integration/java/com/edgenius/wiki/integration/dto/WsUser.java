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
package com.edgenius.wiki.integration.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.edgenius.core.model.Contact;
import com.edgenius.core.model.User;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.integration.WsContants;

/**
 * @author Dapeng.Ni
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)  
@XmlType(name = "User", namespace=WsContants.NS)  
public class WsUser {
	private String username;
	private String fullname;
	private String password;
	private String email;
	private boolean enabled;
	private RoleList roles;

	//********************************************************************
	//               Function method
	//********************************************************************
	public void copyTo(User user) {
		user.setFullname(this.getFullname());
		user.setUsername(this.getUsername());
		user.setPassword(this.getPassword());
		user.setEnabled(this.isEnabled());
		Contact contact = new Contact();
		contact.setEmail(this.getEmail());
		user.setContact(contact);
	}
	
	public void copyFrom(User user) {
		this.setFullname(user.getFullname());
		this.setUsername(user.getUsername());
		this.setPassword(user.getPassword());
		this.setEnabled(user.isEnabled());
		Contact contact = user.getContact();
		this.setEmail(contact!=null?contact.getEmail():null);
	}
	public int hashCode(){
		return username != null?username.hashCode():-1;
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof WsUser)){
			return false;
		}
		
		return StringUtil.equalsIgnoreCase(this.username, ((WsUser)obj).username);
	}
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public RoleList getRoles() {
		return roles;
	}
	
	public void setRoles(RoleList roles) {
		this.roles = roles;
	}
	
}
