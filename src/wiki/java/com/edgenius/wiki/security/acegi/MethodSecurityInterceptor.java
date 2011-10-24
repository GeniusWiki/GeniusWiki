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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.core.AuthenticationException;

/**
 * @author Dapeng.Ni
 */
public class MethodSecurityInterceptor extends AbstractSecurityInterceptor implements MethodInterceptor{
	private static final Logger log = LoggerFactory.getLogger(MethodSecurityInterceptor.class);
	
    private SecurityMetadataSource securityMetadataSource;
    private MethodExceptionHandler methodExceptionHandler; 
	@Override
	public Class getSecureObjectClass() {
		 return MethodInvocation.class;
	}

	public Object invoke(MethodInvocation mi) throws Throwable {
		Object result = null;
		
        try {
	        InterceptorStatusToken token = super.beforeInvocation(mi);
	        try {
	            result = mi.proceed();
	        } finally {
	            result = super.afterInvocation(token, result);
	        }
	    //!!!NDPNDP below catch() is different with SpringSecurity default MethodSecurityInterceptor.java
        } catch (AccessDeniedException e) {
        	log.warn("access denied for method " + mi.getMethod().getName());
        	methodExceptionHandler.handleException(e);
        	//throw exception, so give target method a chance to stop futher process 
        	throw e;
		} catch (AuthenticationException e) {
			log.warn("Authentication failed for method " + mi.getMethod().getName());
			methodExceptionHandler.handleException(e);
			//throw exception, so give target method a chance to stop futher process 
			throw e;
		}

        return result;
	}
	
	@Override
	public SecurityMetadataSource obtainSecurityMetadataSource() {
		return this.securityMetadataSource;
	}
    //********************************************************************
	//               set / get
	//********************************************************************
    public void setSecurityMetadataSource(SecurityMetadataSource newSource) {
        this.securityMetadataSource = newSource;
    }
    
	public void setMethodExceptionHandler(MethodExceptionHandler methodExceptionHandler) {
		this.methodExceptionHandler = methodExceptionHandler;
	}



}
