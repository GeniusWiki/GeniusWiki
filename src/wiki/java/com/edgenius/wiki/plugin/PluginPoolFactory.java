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
package com.edgenius.wiki.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Dapeng.Ni
 */
public class PluginPoolFactory extends BaseKeyedPoolableObjectFactory implements ApplicationContextAware{
	private static final Logger log = LoggerFactory.getLogger(PluginPoolFactory.class);
	private ApplicationContext context;
	
	public Object makeObject(Object key) {
		if(!(key instanceof String))
			return null;
		
		String clz = (String) key;
		Object obj = null;
		try {
			obj = Class.forName(clz).newInstance();
			((Plugin)obj).init(context);
		} catch (InstantiationException e) {
			log.error("Failed initializing object handler " + clz + " by error ",e );
		} catch (IllegalAccessException e) {
			log.error("Failed initializing object handler " + clz + " by error ",e );
		} catch (ClassNotFoundException e) {
			log.error("Failed initializing object handler " + clz + " by error ",e );
		}
		return obj;
	}

	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.context = context;
	}


}
