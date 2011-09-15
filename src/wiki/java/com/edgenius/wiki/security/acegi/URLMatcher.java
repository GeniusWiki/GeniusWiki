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
package com.edgenius.wiki.security.acegi;

import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * @deprecated
 * @author Dapeng.Ni
 */
public class URLMatcher implements Matcher{
	private static final Logger log = LoggerFactory.getLogger(URLMatcher.class);
	private final PathMatcher pathMatcher = new AntPathMatcher();
	/**
	 * Check if input URL is matched with pattern.
	 *  
	 * Basically policy,url>patternUrl: 
	 * <li>URI must be regular express matched</li>
	 * <li>pattern url's parameters must be 100% contained by URL's parameters and values must be equals</li>
	 * <li>pattern anchor "#" must regular express matched by URL's </li>
	 */
	public boolean match(String url, String patternUrl) {
		URLObject input=null, pattern = null;
		input = new URLObject(url);
		pattern = new URLObject(patternUrl);
		if(input == null || pattern == null)
			return false;
		
		if(!pathMatcher.match(pattern.getURI(), input.getURI()))
			return false;
		
		//pattern does not need any parameters and anchor, then return true if URI matched 
		if(pattern.getParameterMap().size() == 0 && pattern.getRef() == null){
			return true; 
		}
		//compare anchor: value after "#"
		if(pattern.getRef() != null){
			if(input.getRef() == null || !pathMatcher.match(pattern.getRef(),input.getRef()))
				return false;
		}
		
		//compare parameters: key=value
		Set<Entry<String, String[]>> pParams = pattern.getParameterMap().entrySet();
		for (Entry<String, String[]> entry : pParams) {
			String key = entry.getKey();
			//any key does not exist in url, not match 
			String[] inputValues = input.getParameterMap().get(key);
			if(inputValues == null)
				return false;
			
			String[] values = entry.getValue();
			//values must exactly equals
			if(values == null || values.length != inputValues.length)
				return false;
			
			for (String inV : inputValues) {
				boolean found = false;
				for (String v : values) {
					if(StringUtils.equalsIgnoreCase(inV, v)){
						found = true;
						break;
					}
				}
				if(!found)
					return false;
			}
			
		}
		
		return true;
	}
}
