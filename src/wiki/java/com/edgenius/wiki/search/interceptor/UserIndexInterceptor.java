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
package com.edgenius.wiki.search.interceptor;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.model.User;
import com.edgenius.core.service.UserService;

/**
 * @author Dapeng.Ni
 */
public class UserIndexInterceptor extends IndexInterceptor {

	public void afterReturning(Object retValue, Method method, Object[] args, Object target) throws Throwable {
		User user = null;
		String removedUsername = null;
		if(StringUtils.equals(method.getName(), UserService.saveUser)){
			user = (User) retValue;
		}else if(StringUtils.equals(method.getName(), UserService.updateUserWithIndex)){
			user = (User) retValue;
			if(!user.isEnabled()){
				//if user is disabled. remove it from index
				removedUsername =  user.getUsername();
			}
		}else if(StringUtils.equals(method.getName(), UserService.removeUser)){
			user = (User) retValue;
			removedUsername =  user.getUsername();
			//clean user
			user = null;
		}
		
		
		if(user != null && user.isEnabled()){
			log.info("JMS message send for User index creating/updating. User: " + user.getFullname());
			IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_INSERT_USER,user);
			jmsTemplate.convertAndSend(queue, mqObj);
		}else if(removedUsername != null){
			log.info("JMS message send for User index deleting. User: " + user.getFullname());
			IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_REMOVE_USER,removedUsername);
			jmsTemplate.convertAndSend(queue, mqObj);
		}
		
	}


}
