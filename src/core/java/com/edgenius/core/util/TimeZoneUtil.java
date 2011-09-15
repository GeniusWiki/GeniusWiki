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
package com.edgenius.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dapeng.Ni
 */
public class TimeZoneUtil {
	
	private static final Logger log = LoggerFactory.getLogger(TimeZoneUtil.class);
	private static final Map<String,TimeZone> offsetMap = new HashMap<String, TimeZone>();
	private static final Map<String,String> idMap = new HashMap<String, String>();
	
	private static final String TIMEZONE_RES_BUNDLE_BASENAME = "com.edgenius.core.timezone";
	static{
		InputStream is = TimeZoneUtil.class.getResourceAsStream("timezone.list");
		Properties prop = new Properties();
		try {
			prop.load(is);
		} catch (IOException e) {
			log.error("Unable load timezone list file.",e);
		}
		Enumeration<Object> enums = prop.keys();
		String offset,timezoneId;
		while(enums.hasMoreElements()){
			String key = (String) enums.nextElement();
			String value = prop.getProperty(key);
			String[] values = value.split("\\|");
			if(values.length == 3){
				offset = values[0].trim();
				timezoneId = values[1].trim();
				//force overwrite
				offsetMap.put(offset, TimeZone.getTimeZone(timezoneId));
				idMap.put(key, timezoneId);
			}else if(values.length == 2){
				offset = values[0].trim();
				timezoneId = values[1].trim();
				if(offsetMap.get(offset) == null){
					offsetMap.put(offset, TimeZone.getTimeZone(timezoneId));
				}
				idMap.put(key, timezoneId);
			}else{
				//error
				log.error("Failed load timezone property:" + value, new Exception("Failed load timezone property"));
			}
		}
		
		log.info("Load timezone size is " + idMap.size());
		if(log.isDebugEnabled())
			log.debug("Load timezone offset size is " + offsetMap.size() +". List: "+ Arrays.toString(offsetMap.keySet().toArray()));
		
	}
	
	public static TimeZone guessTimeZone(String timezoneOffset){
		return offsetMap.get(timezoneOffset.trim());
	}
	/**
	 * Return timezone ID and Timezone display name map.
	 * @param locale
	 * @return
	 */
	public static Map<String, String> getTimezoneList(Locale locale){
		Map<String,String> map = new HashMap<String, String>();
		ResourceBundle bundle = ResourceBundle.getBundle(TIMEZONE_RES_BUNDLE_BASENAME, locale);
		Enumeration<String> enums =  bundle.getKeys();
		while(enums.hasMoreElements()){
			String key = (String) enums.nextElement();
			String tzId = idMap.get(key);
			String value = bundle.getString(key);
			map.put(tzId, value);
		}
		
		return map;
	}
}
