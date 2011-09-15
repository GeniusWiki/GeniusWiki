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
package com.edgenius.core.model;

import static com.edgenius.core.Constants.TABLE_PREFIX;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.SecurityValues.OPERATIONS;
/**
 * Note: there are 2 types permission: allow and forbidden.<BR>
 * For example, in wiki system, instance and space are allow type, ie, resource can be operated only if this permission exists. 
 * In reverse, page is fobidden type, ie, resource can NOT be operated if this permission exists. Page permission will inherit from
 * its space permission.
 * 
 * @author Dapeng.Ni
 */
@Entity
@Table(name = TABLE_PREFIX+"PERMISSIONS")
public class Permission implements Serializable, Cloneable {
	public static final String ROLE_PERMISSION_JOIN_TABLE = Constants.TABLE_PREFIX+ "ROLE_PERMISSIONS";
	private static final long serialVersionUID = 3842517024962619687L;
	
	private static final transient Logger log = LoggerFactory.getLogger(Permission.class);
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator="key_seq")
	@SequenceGenerator(name="key_seq", sequenceName=Constants.TABLE_PREFIX+"PERMISSIONS_SEQ")
	@Column(name="PUID")
	private Integer uid;
	
	@ManyToMany(targetEntity = Role.class)
	@JoinTable(name = ROLE_PERMISSION_JOIN_TABLE, 
		joinColumns = { @JoinColumn(name = "PERMISSION_PUID") },
		inverseJoinColumns={ @JoinColumn(name = "ROLE_PUID") })
	private Set<Role> roles;
	
	@ManyToMany(targetEntity = User.class)
	@JoinTable(name = Constants.TABLE_PREFIX+"USER_PERMISSIONS", 
		joinColumns = { @JoinColumn(name = "PERMISSION_PUID") },
		inverseJoinColumns={ @JoinColumn(name = "USER_PUID") })
	private Set<User> users;
	
	@ManyToOne
	@JoinColumn(name="RESOURCE_PUID")
	private Resource resource;

	//this will become INTEGER value: OPERATIONS.read.ordinal() : the its position in its enum declaration 
	@Column(name="OPERATION")
	private OPERATIONS operation;
	
	//********************************************************************
	//               DTO fields
	//some role/user is block by this permission's upper level permission. Such as INSTANCE permission possible interfere
	//space permission. Some role/user in space permission are blocked by INSTANCE permission
	@Transient
	private List<String> deadRoleUserList;
	
	//********************************************************************
	//               Constructor
	//********************************************************************
	public Permission(){
		//default constructor
	}
	/**
	 * @param read
	 * @param role
	 */
	public Permission(OPERATIONS oper, Set<Role> roles) {
		this.operation = oper;
		this.roles = roles;
	}

	//********************************************************************
	//               Function methods
	//********************************************************************
	public Object shadowClone() {
		Permission perm = null;
		try {
			perm = (Permission) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return perm;
	}

	/**
	 * Clone permission object, to avoid clone deadlock. Does not deep clone users / roles, 
	 * but for resource, as it has special clone() method to set null to permissions() so here deep clone(actually, not really "deep") resources.
	 */
	public Object clone(){
		Permission perm = null;
		try {
			perm = (Permission) super.clone();
			perm.setResource((Resource) perm.getResource().clone());
			perm.setUsers(null);
			perm.setRoles(null);
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return perm;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		if(roles != null){
			sb.append("Role(");
			for (Role role : roles) {
				sb.append(role.getName()).append(",");
			}
			sb.append(")");
		}
		if(users != null){
			sb.append("|User(");
			for (User user : users) {
				sb.append(user.getUsername()).append(",");
			}
			sb.append(")");
		}
		sb.append(" on resource (").append(resource.getResource()).append(")").append(" do ").append(operation);
		
		return sb.toString();
	}
	
	public int hashCode(){
		if(uid != null)
			return uid;
		
		return (resource != null?resource.hashCode():0) + operation.ordinal();
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof Permission))
			return false;
		
		Permission in = (Permission) obj;
		
		if(uid != null)
			return uid.equals(in.uid);
		
		return (resource != null?resource.equals(in.resource):false) && (operation == in.operation);
	}
	//****************************************************************************
	//Get & Set 
	//****************************************************************************	
	public OPERATIONS getOperation() {
		return operation;
	}
	public void setOperation(OPERATIONS operate) {
		this.operation = operate;
	}
	public Resource getResource() {
		return resource;
	}
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public Integer getUid() {
		return uid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public Set<Role> getRoles() {
		return roles;
	}
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	public Set<User> getUsers() {
		return users;
	}
	public void setUsers(Set<User> users) {
		this.users = users;
	}
	public List<String> getDeadRoleUserList() {
		return deadRoleUserList;
	}
	public void setDeadRoleUserList(List<String> deadRoleUserList) {
		this.deadRoleUserList = deadRoleUserList;
	}
	
}
