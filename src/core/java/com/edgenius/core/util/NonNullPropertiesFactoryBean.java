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
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.support.PropertiesLoaderSupport;

/**
 * This bean get properties from applicationContext.xml setting. But if any properties is blank(empty or null),
 * the property will be removed from list. 
 * 
 * For real scenario, Hibernate property "hibernate.default_schema" only valid if it has value. Otherwise, Hibernate 
 * cannot work well if its value is null or empty. But this is most common things, such as MySQL or Oracle, schema normally 
 * is empty. 
 * Refer to http://tersesystems.com/post/by_id/9700062
 * 
 * @author Dapeng.Ni
 */
public class NonNullPropertiesFactoryBean extends PropertiesLoaderSupport implements FactoryBean {


	private Properties properties;

	public Object getObject() throws IOException {
		Enumeration<Object> keys = properties.keys();
		while(keys.hasMoreElements()){
			String key = (String) keys.nextElement();
			if(StringUtils.isBlank(properties.getProperty(key))){
				properties.remove(key);
			}
		}
		
		return properties;
	}

	public Class getObjectType() {
		return Properties.class;
	}


	public boolean isSingleton() {
		return true;
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

}
