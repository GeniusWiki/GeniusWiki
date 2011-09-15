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
package com.edgenius.wiki.webapp.admin.action;

import javax.servlet.ServletContext;

import org.apache.struts2.util.ServletContextAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;

import com.edgenius.wiki.webapp.listener.DynamicContextLoader;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author Dapeng.Ni
 */
public class InstallAction extends ActionSupport implements ApplicationContextAware, ServletContextAware {
	private static final long serialVersionUID = 5128099393040983972L;

	protected transient final Logger log = LoggerFactory.getLogger(InstallAction.class);

	private ServletContext servletContext;

	private ApplicationContext applicationContext;

	public String execute() {

		((ConfigurableWebApplicationContext) applicationContext).close();

		ContextLoader contextLoader = new DynamicContextLoader();
		contextLoader.closeWebApplicationContext(servletContext);

		((DynamicContextLoader) contextLoader).setConfigLocation(ContextLoader.CONFIG_LOCATION_PARAM);
		contextLoader.initWebApplicationContext(servletContext);
		return SUCCESS;
	}

	//JDK1.6 @Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	//JDK1.6 @Override
	public void setServletContext(ServletContext context) {
		this.servletContext = context;

	}

}
