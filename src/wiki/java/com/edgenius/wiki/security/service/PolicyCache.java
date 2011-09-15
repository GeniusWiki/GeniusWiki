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
package com.edgenius.wiki.security.service;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import com.edgenius.wiki.security.Policy;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("unchecked")
public class PolicyCache {
	
	private Cache cache;

	/**
	 * @param resourceName
	 * @return
	 */
	public List<Policy> getPolicies(String resourceName) {
		Element ele = cache.get(resourceName);
		return (List<Policy>) (ele==null? null: ele.getValue());
	}
	public void setPolicies(String resourceName, List<Policy> policies) {
		Element element = new Element(resourceName,policies);
		cache.put(element);
	}

	public void removeAll() {
		cache.removeAll();
	}
	public void remove(String resourceName){
		cache.remove(resourceName);
	}
	//********************************************************************
	//               set / get
	//********************************************************************
	public void setCache(Cache cache) {
		this.cache = cache;
	}
	
}
