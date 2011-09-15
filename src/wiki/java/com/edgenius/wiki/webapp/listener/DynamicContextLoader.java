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
package com.edgenius.wiki.webapp.listener;

import javax.servlet.ServletContext;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Dapeng.Ni
 */
public class DynamicContextLoader extends ContextLoader{
	
	private String configLocation = CONFIG_LOCATION_PARAM;

	/**
	 * 100% copy from override method, except 
	 * 
	 * wac.setConfigLocation(servletContext.getInitParameter(CONFIG_LOCATION_PARAM));
	 */
	@Override
	protected WebApplicationContext createWebApplicationContext(
			ServletContext servletContext, ApplicationContext parent) throws BeansException {

		Class contextClass = determineContextClass(servletContext);
		if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
			throw new ApplicationContextException("Custom context class [" + contextClass.getName() +
					"] is not of type [" + ConfigurableWebApplicationContext.class.getName() + "]");
		}

		ConfigurableWebApplicationContext wac =
				(ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);
		wac.setParent(parent);
		wac.setServletContext(servletContext);
		
		//only one line different with super method
		wac.setConfigLocation(servletContext.getInitParameter(configLocation));
		
		customizeContext(servletContext, wac);
		wac.refresh();

		return wac;
	}
	
	public void setConfigLocation(String configLocation){
		this.configLocation = configLocation;
	}
}
