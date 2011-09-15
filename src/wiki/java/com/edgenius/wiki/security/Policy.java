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
package com.edgenius.wiki.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.ConfigAttribute;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.SecurityConfig;

import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.SecurityValues.RESOURCE_TYPES;


/**
 * Resource(ResourceID:SpaceUname,pageUuid) -> Type(instance/space/page) -> Operation(READ,WRITE...) 
 * -> RuntimeType(Method/URL) -> Patterns<=>ConfigurtionAttribute(User_name,Role_name)
 * 
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class Policy implements Comparable<Policy>,Serializable, Cloneable{
	private static final Logger log = LoggerFactory.getLogger(Policy.class);
	//instance,space or page?
	private RESOURCE_TYPES type;
	private OPERATIONS operation;

	//could be $instance$, spaceUname or pageUuid
	private String resourceName;

	private List<ConfigAttribute> list = new ArrayList<ConfigAttribute>();
	//********************************************************************
	//               Methods
	//********************************************************************
	public Policy(){
	}
	public Object clone(){
		Object obj = null;
		try {
			obj  = super.clone();
			((Policy)obj).removeAllAttribute();
			for(Iterator iter = list.iterator();iter.hasNext();){
				((Policy)obj).addAttribute(((SecurityConfig)iter.next()).getAttribute());
			}
		} catch (CloneNotSupportedException e) {
			log.error("Policy clone failed" , e);
		}
		
		return obj;
	}
	
	public void addAttribute(String attribute){
		SecurityConfig config = new SecurityConfig(attribute);
		list.add(config);
	}
	public void addAllAttribute(List<ConfigAttribute> atts){
		list.addAll(atts);
	}
	
	public boolean hasAttribute(String attribute){
		SecurityConfig config = new SecurityConfig(attribute);
		return list.contains(config);
	}
	
	public boolean removeAttribute(String attribute) {
		SecurityConfig config = new SecurityConfig(attribute);
		Iterator<ConfigAttribute> iter = list.iterator();
		while(iter.hasNext()){
			ConfigAttribute att = iter.next();
			if(att.equals(config)){
				iter.remove();
				return true;
			}
		}
		return false;
		
	}
	public void removeAllAttribute() {
		//NOTE: don't user list.clear(); as list may reference to other policy's list, clear() will impact other policy
		//please refer to clone() method;
		list = new ArrayList<ConfigAttribute>();
	}
	public boolean containAttribute(String name) {
		return list.contains(new SecurityConfig(name));
	}


	public int compareTo(Policy p) {
		//always put Instance > Space > Page : Note: the finally sort will be Collections.reverse() result
		
		//system default policy: such as /**/singup.do* won't have RESOURCE_TYPES
		if(this.type == null)
			return -1;
		if(p.type == null)
			return 1;
		
		return this.type.ordinal()-p.type.ordinal();

	}
	public boolean equals(Object obj){
		if(!(obj instanceof Policy))
			return false;
		Policy p = (Policy) obj;
		return new EqualsBuilder().append(p.type, this.type)
			.append(p.operation, this.operation)
			.append(p.resourceName, this.resourceName).isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder().append(this.type).append(this.operation).append(this.resourceName).toHashCode();
	}
	public int size() {
		return list.size();
	}
	public String toString(){
		return "Policy: Users/Roles("+ Arrays.toString(list.toArray()) +") on resource (" 
		+ (type==null?"":type.name()) + ":"+ resourceName + "} can (" + (operation==null?"":operation.name()) +")\n";
	}
	
	public List<ConfigAttribute> getMutableAttributeDefinition() {
		return list;
	}

	/**
	 * 
	 * @return ConfigAttributeDefinition is immutable collection. 
	 */
	public ConfigAttributeDefinition getAttributeDefinition() {
		return new ConfigAttributeDefinition(list);
	}
	//********************************************************************
	//               Get / Set
	//********************************************************************



	public OPERATIONS getOperation() {
		return operation;
	}

	public void setOperation(OPERATIONS operation) {
		this.operation = operation;
	}

	public RESOURCE_TYPES getType() {
		return type;
	}

	public void setType(RESOURCE_TYPES type) {
		this.type = type;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}


}
