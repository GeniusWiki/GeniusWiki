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
package com.edgenius.wiki.security.acegi;

import java.lang.reflect.Method;
import java.util.Collection;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.SecurityConfig;
import org.springframework.security.intercept.method.MethodDefinitionSource;
import org.springframework.util.Assert;

import com.edgenius.core.SecurityValues.SYSTEM_ROLES;
import com.edgenius.wiki.security.Policy;
import com.edgenius.wiki.security.service.SecurityService;
/**
 * 
 * @author Dapeng.Ni
 */
public class DBMethodDefinitionSource implements MethodDefinitionSource {
	private static final Logger logger = LoggerFactory.getLogger(DBMethodDefinitionSource.class);
	private SecurityService securityService;
	
	public ConfigAttributeDefinition getAttributes(Object object) {
		Assert.notNull(object, "Object cannot be null");

		if (object instanceof MethodInvocation) {
			MethodInvocation miv = (MethodInvocation) object;
			return this.lookupAttributes(miv.getThis().getClass(), miv.getMethod(), miv.getArguments());
		}

		throw new IllegalArgumentException("Object must be a MethodInvocation");
	}

	public boolean supports(Class clazz) {
		return (MethodInvocation.class.isAssignableFrom(clazz));
	}



	//JDK1.6 @Override
	public Collection getConfigAttributeDefinitions() {
		return null;
	}
	/**
	 *
	 * @param args 
	 * @see org.springframework.security.intercept.method.AbstractMethodDefinitionSource#lookupAttributes(java.lang.reflect.Method)
	 */
	@SuppressWarnings("unchecked")
	protected ConfigAttributeDefinition lookupAttributes(Class clszz, Method mi, Object[] args) {
		Assert.notNull(mi, "lookupAttrubutes in the DBMethodDefinitionSource is null");

		Policy policy = securityService.findBeforeMethodPolicy(clszz.getName(), mi.getName(), args);
		if(policy != null)
			return policy.getAttributeDefinition();
		
		//if there is not policy mapping before method, then return $ALL$ to allow AfterInvocation check:
		//NOTE: if return null, AfterInvocation check also cancelled because MethodSecurityInterceptor
		//will skip AfterInvocation if InterceptorStatusToken(returned by beforeInvocation) is null 
		ConfigAttributeDefinition cd = new ConfigAttributeDefinition(new SecurityConfig(SYSTEM_ROLES.ALL.getName()));
		return cd;
	}


	//********************************************************************
	//               Set / Get
	//********************************************************************
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	//JDK1.6 @Override
	public ConfigAttributeDefinition getAttributes(Method method, Class targetClass) {
		// TODO Auto-generated method stub
		return null;
	}

}
