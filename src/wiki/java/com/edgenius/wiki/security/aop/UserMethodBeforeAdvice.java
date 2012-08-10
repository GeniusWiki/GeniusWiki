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
package com.edgenius.wiki.security.aop;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import com.edgenius.core.Global;
import com.edgenius.core.SecurityValues;
import com.edgenius.core.model.User;
import com.edgenius.core.service.UserService;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.security.UserSignUpDiabledException;
import com.edgenius.wiki.security.acegi.MethodExceptionHandler;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
public class UserMethodBeforeAdvice implements MethodBeforeAdvice{
	private static final Logger log = LoggerFactory.getLogger(UserMethodBeforeAdvice.class);
	private SecurityService securityService; 
    private MethodExceptionHandler methodExceptionHandler; 
    
  //JDK1.6 @Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		String name = method.getName();
		try{
			User loginUser = WikiUtil.getUser();
			if(name.equals(UserService.saveUser)){
			    //if public sign up is disabled and not system admin, then throw exception.
				if(Global.hasSuppress(SharedConstants.SUPPRESS.SIGNUP)
						//administrator allows to add user from system admin page 
					&& !securityService.isAllowResourceAdmin(SharedConstants.INSTANCE_NAME, SecurityValues.RESOURCE_TYPES.INSTANCE,loginUser)){
					throw new UserSignUpDiabledException("User sign up is disabled.");
				}
			}else if(name.equals(UserService.updateUser)
					|| name.equals(UserService.updateUserWithIndex)
					|| name.equals(UserService.uploadPortrait)){
				User user = (User) args[0];
				if(loginUser == null || loginUser.isAnonymous()){
					//anonymous
					throw new AccessDeniedException("Anonymous can not update user. Security exception.");
				}else if (!loginUser.equals(user)&& 
							!securityService.isAllowResourceAdmin(SharedConstants.INSTANCE_NAME,
								SecurityValues.RESOURCE_TYPES.INSTANCE,loginUser)){
					throw new AccessDeniedException("User has no system admin permssion and try to update other user.Security exception.");
				}
				
			}else if(name.equals(UserService.removeUser)){
				if(!securityService.isAllowResourceAdmin(SharedConstants.INSTANCE_NAME,
								SecurityValues.RESOURCE_TYPES.INSTANCE,loginUser)){
					throw new AccessDeniedException("Only system admin user can do removeUser.Security exception.");
				}
			}
        } catch (AccessDeniedException e) {
        	log.warn("access denied for UserService method " + name);
        	methodExceptionHandler.handleException(e);
        	//throw exception, so give target method a chance to stop futher process 
        	throw e;
		} catch (AuthenticationException e) {
			log.warn("Authentication failed for UserService  method " + name);
			methodExceptionHandler.handleException(e);
			//throw exception, so give target method a chance to stop futher process 
			throw e;
		}
		
	}
	
	//********************************************************************
	//               set / get
	//********************************************************************
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setMethodExceptionHandler(MethodExceptionHandler methodExceptionHandler) {
		this.methodExceptionHandler = methodExceptionHandler;
	}
}
