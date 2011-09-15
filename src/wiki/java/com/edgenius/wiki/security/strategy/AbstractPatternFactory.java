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
package com.edgenius.wiki.security.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.dao.PermissionDAO;
import com.edgenius.core.dao.ResourceDAO;
import com.edgenius.core.dao.RoleDAO;
import com.edgenius.core.model.Permission;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.wiki.security.Policy;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;


/**
 * Method and URL security policy definition. For method, it could be given input arguments in (), such as
 * <code>method(s=$1$,p=$2$,u=$3#)</code>. It also can be given return value after "->", such as 
 * <code>method() -> s=$1$</code>.  So far, there 4 possible variables:
 * <li>s: spaceUname</li>
 * <li>u: pageUuid</li>
 * 
 * 
 * IMPORTANT: Service class method is ONLY can be put into one Patter String[] array. Any duplicated value will happen unexpected replacement.
 * Please refer to PatternStrategy.afterPropertiesSet();
 * 
 * Policy usually means <code>WikiPrivilegeStrategy.WikiOPERATIONS</code>
 * 
 * @author Dapeng.Ni
 */
public abstract class AbstractPatternFactory implements PatternFactory {
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	protected Map<String,WikiOPERATIONS> urlPatternMap = new HashMap<String,WikiOPERATIONS>();
	protected Map<String,WikiOPERATIONS> methodPatternMap = new HashMap<String,WikiOPERATIONS>();
	
	protected RoleDAO roleDAO;
	protected ResourceDAO resourceDAO;
	protected PermissionDAO permissionDAO;

	/**
	 * Resource->Type->Operation->ConfigureAttributes
	 * @param permission
	 * @param args
	 * @return
	 */
	protected Policy getPolicy(Permission permission, String resourceName){
		
		Set<Role> roles = permission.getRoles();
		Set<User> users = permission.getUsers();
		 
		Policy policy = new Policy();
		policy.setOperation(permission.getOperation());
		policy.setType(permission.getResource().getType());
		policy.setResourceName(resourceName);
		if(roles != null){
			for (Role role : roles) {
				policy.addAttribute(role.getName());
			}
		}
		if(users != null){
			for (User user : users) {
				//put user name attribute as "USER_xxx"
				policy.addAttribute(Role.USER_PREFIX+user.getUsername());
			}
		}			
		
		//this permission has no user&role link. It is useless, so just return null
		if(policy.size() == 0)
			return null;
		
		return policy;
	}
	
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public void setPermissionDAO(PermissionDAO permissionDAO) {
		this.permissionDAO = permissionDAO;
	}

	public void setResourceDAO(ResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}

	public void setRoleDAO(RoleDAO roleDAO) {
		this.roleDAO = roleDAO;
	}




}
