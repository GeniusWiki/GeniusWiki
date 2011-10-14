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
package com.edgenius.core.webapp.filter;

import javax.servlet.Filter;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;


/**
 * This class allows the Filter delegate can be reload when Spring ApplicationContext reloaded. 
 * 
 * @see org.springframework.web.filter.DelegatingFilterProxy
 * 
 * @author Dapeng.Ni
 */
public class ContextAwareDelegatingFilterProxy extends DelegatingFilterProxy implements ApplicationListener {
	private static final Logger log = LoggerFactory.getLogger(ContextAwareDelegatingFilterProxy.class);
	
	protected Filter initDelegate(WebApplicationContext wac) throws ServletException {
		
		// See bug on spring 3.0+ : https://jira.springsource.org/browse/SPR-6228
		//!!! Add context event listener
		SourceFilteringListener listener = new SourceFilteringListener(wac, this);
		((ConfigurableWebApplicationContext)wac).addApplicationListener(listener);
		((XmlWebApplicationContext)wac).getApplicationListeners().add(listener);
		
		return super.initDelegate(wac);
	}
	
	
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			onRefresh(((ContextRefreshedEvent) event).getApplicationContext());
		}
		
	}
	/**
	 * @param applicationContext
	 */
	private void onRefresh(ApplicationContext applicationContext) {
		destroy();
		
		try {
			initFilterBean();
		} catch (ServletException e) {
			log.error("Refresh application context, initial filter bean failed.",e);
		}
	}
	

}
