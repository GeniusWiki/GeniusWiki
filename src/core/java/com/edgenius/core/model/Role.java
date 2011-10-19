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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

import com.edgenius.core.Constants;

@Entity
@Table(name=Constants.TABLE_PREFIX+"ROLES")
public class Role extends TouchedInfo  implements  GrantedAuthority,Cloneable{
	private static final long serialVersionUID = 1698073338726402269L;
	private static final transient Logger log = LoggerFactory.getLogger(Role.class);
	public static final int SORT_BY_DISPLAYNAME = 1;
	public static final int SORT_BY_DESC = 1<<1;
	public static final int SORT_BY_USERS_COUNT = 1<<2;
	public static final int SORT_BY_TYPE = 1<<3;
	
	//USER_Prefix will add in program, never save into DB, just for indicate for user name in security policy
	public static final String USER_PREFIX = "USER_";
	//All role saved into database should with this prefix before role name, this requirement for security permission check program
	public static final String ROLE_PREFIX = "ROLE_";
	
	public static final String SYSTEM_ROLE_PREFIX = ROLE_PREFIX+"SYS_";
	//for each space, it will has one mapped role, this role should avoid name conflict with system role, so that 
	//role name does not impact spaceUname, for example, if user create admin space, a role ROLE_SPACE_admin will created,
	//it has no conlict with ROLE_admin which is system existed one.
	
	//THIS VALUE MUST SAME with SharedContants.ROLE_SPACE_PREFIX
	public static final String SPACE_ROLE_PREFIX = ROLE_PREFIX+"SPACE_";
	
	public static final String GROUP_ROLE_PREFIX = ROLE_PREFIX+"GROUP_";
	
	public static final int TYPE_SYSTEM = 0;
	//TODO: this is dependent on wiki package, broken core dependencies.
	//if this role is create with a space, then type should be this
	public static final int TYPE_SPACE = 1;
	//this system admin created role from admin page
	public static final int TYPE_GROUP = 2;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator="key_seq")
	@SequenceGenerator(name="key_seq", sequenceName=Constants.TABLE_PREFIX+"ROLES_SEQ")
	@Column(name="PUID")
	private Integer uid;
	
	@Column(name="NAME", unique=true)
	private String name;

	@Column(name="DISPLAY_NAME")
	private String displayName;
	
	@Column(name="DESCRIPTION")
	private String description;
	
	@ManyToMany(targetEntity = User.class, fetch = FetchType.LAZY,
			cascade = { CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH},mappedBy="roles")
	private Set<User> users = new HashSet<User>();

	@ManyToMany(targetEntity = Permission.class, fetch = FetchType.LAZY,
			cascade = { CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH},mappedBy="roles")
	private Set<Permission> permissions = new HashSet<Permission>();
	
	//it is system role, or space role
	@Column(name="ROLE_TYPE")
	private int type;
	
	//********************************************************************
	//               Constructor
	//********************************************************************
	public Role(){
		//default for Hibernate
	}
	
	public Role(String fullname){
		this.name = fullname;
	}
	
	//********************************************************************
	//               Function methods
	//********************************************************************
	public Role(int type, String name){
		if(type == TYPE_SYSTEM){
			this.name = SYSTEM_ROLE_PREFIX+name;
		}else if(type == TYPE_SPACE){
			this.name = SPACE_ROLE_PREFIX+name;
		}else if(type == TYPE_GROUP){
			this.name = GROUP_ROLE_PREFIX+name;
		}
	}
	/**
	 * Clone role object, to avoid clone deadlock, does not deep clone users 
	 */
	public Object clone(){
		Role cRole = null;
		try {
			cRole = (Role) super.clone();
			//For performance/memory consideration, don't clone permission as well
			//as role will put into userCache with user. Admin,User,Anonymous role could have too many permissions.
			cRole.setPermissions(null);
			
			//!!! To avoid infinite looping and bring too much list to Cachex, skip users.
			cRole.setUsers(null);
			cRole.setCreator(null);
			cRole.setModifier(null);
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cRole;
	}
	//JDK1.6 @Override
	public int compareTo(Object obj) {
		if(!(obj instanceof Role)){
			return 1;
		}
		
		return ((Role)obj).getName().compareTo(this.getName());
	}
	
	public String toString(){
		return type + "||" + name+"||" + displayName;
	}
	
	public boolean equals(Object obj){
		if(this == obj) return true;
		if (!(obj instanceof Role))
			return false;
		
		//don't compare puid!!! As we have case puid is null but hope it return true if only role name are same.
		return StringUtils.equals(this.name,((Role)obj).name);
	}
	public int hashCode(){
		return this.name == null? super.hashCode() : this.name.hashCode();
	}
	//****************************************************************************
	//Get & Set 
	//****************************************************************************
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public String getAuthority() {
		return name;
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
}
