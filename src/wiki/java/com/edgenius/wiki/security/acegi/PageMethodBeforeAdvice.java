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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import com.edgenius.core.model.User;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
public class PageMethodBeforeAdvice  implements MethodBeforeAdvice{
	private static final Logger log = LoggerFactory.getLogger(PageMethodBeforeAdvice.class);
    private MethodExceptionHandler methodExceptionHandler; 
    
  //JDK1.6 @Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		String name = method.getName();
		try{
			if(name.equals(PageService.getDraft)
				|| name.equals(PageService.getDraftPages)
				|| name.equals(PageService.removeDraft)
				|| name.equals(PageService.saveDraft)){
				User user = (User) args[0];
				User loginUser = WikiUtil.getUser();
				if(loginUser.isAnonymous()){
					//anonymous
					throw new AccessDeniedException("Anonymous can not call any draft function methods. Security exception.");
				}else if (!loginUser.equals(user)){
					throw new AccessDeniedException("Only user can get/remove his/her owned drafts");
				}
			}
        } catch (AccessDeniedException e) {
        	log.warn("access denied for PageService method " + name);
        	methodExceptionHandler.handleException(e);
        	//throw exception, so give target method a chance to stop futher process 
        	throw e;
		} catch (AuthenticationException e) {
			log.warn("Authentication failed for PageService  method " + name);
			methodExceptionHandler.handleException(e);
			//throw exception, so give target method a chance to stop futher process 
			throw e;
		}
	}
	
	//********************************************************************
	//               set / get
	//********************************************************************
	public void setMethodExceptionHandler(MethodExceptionHandler methodExceptionHandler) {
		this.methodExceptionHandler = methodExceptionHandler;
	}
}
