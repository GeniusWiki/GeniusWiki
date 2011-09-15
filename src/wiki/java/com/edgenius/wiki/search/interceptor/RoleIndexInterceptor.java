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

import com.edgenius.core.model.Role;
import com.edgenius.core.service.RoleService;

/**
 * @author Dapeng.Ni
 */
public class RoleIndexInterceptor  extends IndexInterceptor {

	public void afterReturning(Object retValue, Method method, Object[] args, Object target) throws Throwable {
		Role role= null;
		if(StringUtils.equals(method.getName(), RoleService.createRole)){
			role = (Role) retValue;
		}
		if(role == null)
			return;
		
		log.info("JMS message send for Role index creating/updating. Role: " + role.getName());
		IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_INSERT_ROLE,role.getUid());
		jmsTemplate.convertAndSend(queue, mqObj);
	}

}
