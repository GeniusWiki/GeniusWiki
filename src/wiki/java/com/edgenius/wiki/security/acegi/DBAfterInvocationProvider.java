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


import java.util.Collection;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.AfterInvocationProvider;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import com.edgenius.wiki.security.Policy;
import com.edgenius.wiki.security.service.SecurityService;

/**
 * @author Dapeng.Ni
 */
public class DBAfterInvocationProvider implements AfterInvocationProvider{
	private SecurityService securityService;
	private AccessDecisionManager accessDecisionManager;
	
	@Override
	public Object decide(Authentication authentication, Object object, Collection<ConfigAttribute> attributes, Object returnedObject) throws AccessDeniedException {
		if(returnedObject == null)
			return returnedObject;
		
		if (object instanceof MethodInvocation) {
			MethodInvocation miv = (MethodInvocation) object;
		
			Policy policy = securityService.findAfterMethodPolicy(miv.getThis().getClass().getName()
								, miv.getMethod().getName(),returnedObject);
			if(policy != null){
	            try {
	            	accessDecisionManager.decide(authentication, object, policy.getAttributeDefinition());
	            } catch (AccessDeniedException accessDeniedException) {
	                throw accessDeniedException;
	            }
			}
		}
		
		return returnedObject;

	}

	@Override
	public boolean supports(ConfigAttribute attribute) {
		return true;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

	//********************************************************************
	//               set /get 
	//********************************************************************
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setAccessDecisionManager(AccessDecisionManager accessDecisionManager) {
		this.accessDecisionManager = accessDecisionManager;
	}


}
