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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.context.SecurityContextHolder;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.edgenius.core.DataRoot;
import com.edgenius.wiki.security.acegi.ThreadLocalSecurityContextCloneableHolderStrategy;

/**
 * This class try to set "geniuswiki.data.root". This is not perfect yet!
 * There are possible issues:
 * - It try to use catalina.base - as I still assume Tomcat is major server at the moment.
 * - It try to use data root - if user does not install, there is default data root point to /var/geniuswiki - it is unexpected
 * - It may create geniuswiki.log.tmp directory if no any success anymore.
 * 
 * @author Dapeng.Ni
 */
public class LogListener  implements ServletContextListener{
	private static final Logger log = LoggerFactory.getLogger(LogListener.class);
	static{
		//May not good place put this irrelevant property here, but don't need create a new class for this single line code.
		System.setProperty(SecurityContextHolder.SYSTEM_PROPERTY, ThreadLocalSecurityContextCloneableHolderStrategy.class.getName());
		
		//get system property first 
		String logDir = System.getProperty("geniuswiki.log.dir");
    	if(StringUtils.isBlank(logDir)){
    		//TODO: how to fit for all web server?
    		//try to get server log directory, so far, only support TOMCAT
    		String base = System.getProperty("catalina.base");
    		if(StringUtils.isBlank(base)){
    			//get DataRoot from system properties then
    			base = System.getProperty("geniuswiki.data.root");
    		}
    		if(StringUtils.isBlank(base)){
    			try {
					base = new URL(DataRoot.getDataRoot()).getFile();
				} catch (MalformedURLException e) {
					log.error("Unable to get data root for log file", e);
				}
    		}
    		if(!StringUtils.isBlank(base)){
    			System.setProperty("geniuswiki.log.dir", new File(base,"logs").getAbsolutePath());
    		}
    	}
    	
    	System.out.println("Log directory is reset to [" + System.getProperty("geniuswiki.log.dir") + "].");
    	//reset log configuration file to logback.xml 
    	ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
		if (loggerFactory instanceof LoggerContext) {
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

			loggerContext.stop();
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			URL configUrl = LogListener.class.getClassLoader().getResource("logback.xml");
			try {
				configurator.doConfigure(configUrl);
			} catch (JoranException ex) {
				log.error("Log configuration failed doConfigure on URL {}", configUrl, ex);
			} finally{
				StatusPrinter.print(loggerContext);
			}
		}

	}
	
	
	public void contextInitialized(ServletContextEvent sce) {
		
		
	}

	public void contextDestroyed(ServletContextEvent sce) {
		
	}

}
