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
package com.edgenius.wiki.security.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;
import com.edgenius.wiki.security.acegi.URLObject;
import com.edgenius.wiki.security.acegi.URLValueProvider;

/**
 * @author Dapeng.Ni
 */
public class SpaceURLValueProvider implements URLValueProvider {

	public Map<RESOURCE_TYPES,String> getParameters(String sourceUrl, String pattern){
		Map<RESOURCE_TYPES,String> map = new HashMap<RESOURCE_TYPES, String>();
		for (int idx=0;idx< SpacePatternFactory.S_READ_URL_PATTERNS.length;idx++) {
			if(SpacePatternFactory.S_READ_URL_PATTERNS[idx].equals(pattern)){
				Object param = SpacePatternFactory.S_READ_URL_PARAMS[idx];
				String[] values = null;
				if(param instanceof Pattern){
					Matcher matcher = ((Pattern)param).matcher(sourceUrl);
					if(matcher.find()){
						MatchResult rs = matcher.toMatchResult();
						int count = rs.groupCount();
						values = new String[count];
						for (int gIdx = 1; gIdx <= count; gIdx++) {
							values[gIdx-1] = rs.group(gIdx);
						}
					}
				}else{
					//parameter name
					URLObject obj = new URLObject(sourceUrl);
					values = obj.getParameterMap().get((String)param);
				}
				if(values != null)
					map.put(RESOURCE_TYPES.SPACE, values[0]);
				break;
			}
		}
		
		return map;
	}
	
	public boolean isSupport(WikiOPERATIONS wikiType){
		if(wikiType.type ==  RESOURCE_TYPES.SPACE)
			return true;
		else
			return false;
	}
}
