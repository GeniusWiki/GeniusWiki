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

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.springframework.jndi.JndiObjectFactoryBean;

/**
 * This class allow lookup a object that is optional configurable. It will return null by getObject() if input JNDIname is empty.
 * Sample scenario: See JavaMail setting - user can setup JNDI or directly SMTP host.  If JNDI name is empty, then JavaMail 
 * will use host rather than throw exception.
 *
 * @author Dapeng.Ni
 */
public class NullableJndiObjectFactoryBean extends  JndiObjectFactoryBean{
	public void afterPropertiesSet() throws IllegalArgumentException, NamingException{
		if(StringUtils.isEmpty(getJndiName())){
			//do nothing
			return;
		}
		super.afterPropertiesSet();
	}
	
}
