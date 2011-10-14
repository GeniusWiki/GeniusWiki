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

import org.springframework.context.ApplicationContext;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;

/**
 * @author Dapeng.Ni
 */
public class DynamicContextLoader extends ContextLoader{
	
	private String configLocation = CONFIG_LOCATION_PARAM;

//	/**
//	 * 100% copy from override method, except 
//	 * 
//	 * wac.setConfigLocation(servletContext.getInitParameter(CONFIG_LOCATION_PARAM));
//	 */
//	@Override
//	protected WebApplicationContext createWebApplicationContext(
//			ServletContext servletContext, ApplicationContext parent) throws BeansException {
//
//		Class contextClass = determineContextClass(servletContext);
//		if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
//			throw new ApplicationContextException("Custom context class [" + contextClass.getName() +
//					"] is not of type [" + ConfigurableWebApplicationContext.class.getName() + "]");
//		}
//
//		ConfigurableWebApplicationContext wac =
//				(ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);
//		wac.setParent(parent);
//		wac.setServletContext(servletContext);
//		
//		//only one line different with super method
//		wac.setConfigLocation(servletContext.getInitParameter(configLocation));
//		
//		customizeContext(servletContext, wac);
//		wac.refresh();
//
//		return wac;
//	}
	/**
	 * 100% copy from override method, except 
	 * 
	 * wac.setConfigLocation(servletContext.getInitParameter(CONFIG_LOCATION_PARAM));
	 */
	protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac, ServletContext sc) {
		if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
			// The application context id is still set to its original default value
			// -> assign a more useful id based on available information
			String idParam = sc.getInitParameter(CONTEXT_ID_PARAM);
			if (idParam != null) {
				wac.setId(idParam);
			}
			else {
				// Generate default id...
				if (sc.getMajorVersion() == 2 && sc.getMinorVersion() < 5) {
					// Servlet <= 2.4: resort to name specified in web.xml, if any.
					wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
							ObjectUtils.getDisplayString(sc.getServletContextName()));
				}
				else {
					wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
							ObjectUtils.getDisplayString(sc.getContextPath()));
				}
			}
		}

		// Determine parent for root web application context, if any.
		ApplicationContext parent = loadParentContext(sc);

		wac.setParent(parent);
		wac.setServletContext(sc);
		
		//!!!NDPNDP: only one line different with super method
		wac.setConfigLocation(sc.getInitParameter(configLocation));
		
		customizeContext(sc, wac);
		wac.refresh();
	}
	public void setConfigLocation(String configLocation){
		this.configLocation = configLocation;
	}
}
