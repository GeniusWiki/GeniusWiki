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
package com.edgenius.core.service.impl;

import java.util.Properties;

import javax.mail.Session;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jndi.JndiObjectFactoryBean;

/**
 * @author Dapeng.Ni
 */
public class JavaMailSenderImpl extends org.springframework.mail.javamail.JavaMailSenderImpl implements InitializingBean{
	private Session mailSession;
	private String mailProperties;
	
	/**
	 * MailSession JDNI name changed, reload JNDI object. 
	 * 
	 */
	public void resetMailSessionByJNDI(String jndiName) throws IllegalArgumentException, NamingException {
		//always reset session to null, so make "reset" meaningful. This is also a trick, as setSession() doesn't allow null
		//however, this.setJavaMailProperties(prop) will reset Session to null internally...
		this.setJavaMailProperties(new Properties());
		
		JndiObjectFactoryBean factory = new JndiObjectFactoryBean();
		factory.setJndiName(jndiName);
		factory.afterPropertiesSet();
		if(factory.getObject() instanceof Session)
			this.setSession((Session) factory.getObject());
		else
			throw new IllegalArgumentException("JNDI object is not mail session instance");
	}
	/**
	 * mailProperties changed, this method will reload mail session
	 */
	public void resetMailSessionByProperties() {
		//always reset session to null, so make "reset" meaningful. This is also a trick, as setSession() doesn't allow null
		//however, this.setJavaMailProperties(prop) will reset Session to null internally...
		this.setJavaMailProperties(new Properties());
		
		//multiple properties separated by ";;" and, each property separated by ":" with name and value pair 
		String[] properties = mailProperties.split(";;");
		int sep;
		for (String property : properties) {
			sep = StringUtils.indexOf(property, ":");
			if(sep < 0){
				continue;
			}
			String name = StringUtils.trimToEmpty(property.substring(0, sep));
			String value = StringUtils.trimToEmpty(property.substring(sep+1));
			Properties prop = new Properties();
			if(!"".equals(name)){
				prop.setProperty(name, value);
			}
			//this method will reset session to null as well
			this.setJavaMailProperties(prop);
		}
	}
	//This method also accept invalid string, improve the failure tolerance capability of system
	public void setMailPort(String port){
		int portNum = NumberUtils.toInt(port, DEFAULT_PORT);
		this.setPort(portNum);
	}
	//********************************************************************
	//              InitializingBean
	//********************************************************************
	public void afterPropertiesSet() throws Exception {
		//Host is empty but JNDI is not, will use JNDI way to send mail
		if(mailSession != null){
			this.setSession(mailSession);
		}else if(!StringUtils.isEmpty(mailProperties)){
			//only if mail session is not set, javaMailProperties will be valid.
			resetMailSessionByProperties();
		}
	}
	
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public void setMailSession(Session mailSession) {
		this.mailSession = mailSession;
	}
	public void setMailProperties(String mailProperties) {
		this.mailProperties = mailProperties;
	}
}
