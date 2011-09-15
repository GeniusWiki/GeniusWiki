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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.SecurityValues.RESOURCE_TYPES;

@SuppressWarnings("serial")
@Entity
@Table(name = TABLE_PREFIX+"RESOURCES")
public class Resource implements Serializable, Cloneable{
	private static final Logger log = LoggerFactory.getLogger(Resource.class);
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator="key_seq")
	@SequenceGenerator(name="key_seq", sequenceName=Constants.TABLE_PREFIX+"RESOURCES_SEQ")
	@Column(name="PUID")
	private Integer uid;
	
	@Column(name="R_TYPE", length=8)
	private RESOURCE_TYPES type;
	
	@Column(name="RESOURCE_NAME",unique=true)
	private String resource;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumn(name="RESOURCE_PUID")
	private Set<Permission> permissions = new HashSet<Permission>();
	
	@Column(name="MASKS", nullable=true)
	private String masks;

	//********************************************************************
	//               method
	//********************************************************************
	public Resource() {
		//default
	}
	/**
	 * @param resourceTypeOrdial
	 * @param resourceName
	 */
	public Resource(RESOURCE_TYPES resourceType, String resourceName) {
		this.resource = resourceName;
		this.type = resourceType;
	}
	public Object clone(){
		Resource cResource = null;
		try {
			cResource = (Resource) super.clone();
			//!!! As resource will clone by Permission, so here just set permission to null
			//this may cause problem if call user.getPermission()->getResource().getPermission()...
			cResource.setPermissions(null);
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cResource;
	}
	
	public int hashCode(){
		if(uid != null)
			return uid;
		
		return (resource != null?resource.hashCode():0) + type.ordinal();
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof Permission))
			return false;
		
		Resource in = (Resource) obj;
		
		if(uid != null)
			return uid.equals(in.uid);
		
		return (resource != null?resource.equals(in.resource):false) && (type ==in.type);
	}

	//****************************************************************************
	//Get & Set 
	//****************************************************************************	
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public RESOURCE_TYPES getType() {
		return type;
	}
	public void setType(RESOURCE_TYPES type) {
		this.type = type;
	}
	public Integer getUid() {
		return uid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public Set<Permission> getPermissions() {
		return permissions;
	}
	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}
	public String getMasks() {
		return masks;
	}
	public void setMasks(String masks) {
		this.masks = masks;
	}	
}
