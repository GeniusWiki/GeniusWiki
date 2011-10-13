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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.Constants;
import com.edgenius.core.SecurityValues.SYSTEM_ROLES;
import com.edgenius.core.dao.RoleDAO;
import com.edgenius.core.model.Permission;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.service.RoleService;
import com.edgenius.core.service.UserService;

/**
 * Implementation of RoleService interface.</p>
 * 
 */
@Service(RoleService.SERVICE_NAME)
@Transactional(readOnly=true)
public class RoleServiceImpl implements RoleService {
	private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);
	
	@Autowired
    private RoleDAO roleDAO;
    private UserService userService;

    public Role getRoleByName(String rolename) {
        return roleDAO.getByName(rolename);
    }
    @Transactional(readOnly=false)
    public Role createRole(Role role) {
        return roleDAO.saveRole(role);
    }
    @Transactional(readOnly=false)
    public Role saveRole(Role role) {
    	log.info("Role update: " + role);
    	return roleDAO.saveRole(role);
    }
    @Transactional(readOnly=false)
    public void removeRole(String rolename) {
    	Role role = roleDAO.getByName(rolename);
    	Set<Permission> perms = role.getPermissions();
    	if(perms != null){
	    	for (Iterator<Permission> iter = perms.iterator(); iter.hasNext();) {
				Permission perm = iter.next();
				perm.getRoles().remove(role);
				iter.remove();
			}
    	}
    	Set<User> users = role.getUsers();
    	for (User user : users) {
    		user.getRoles().remove(role);
    		userService.interalUpdateUser(user);
		}
    	
    	roleDAO.removeObject(role);

    }
	/* (non-Javadoc)
	 * @see com.edgenius.core.service.RoleService#getDefaultRole()
	 */
    
	public Set<Role> getDefaultRole() {
		Set<Role> roles = new HashSet<Role>();
		Role role = roleDAO.getByName(SYSTEM_ROLES.USERS.getName());
		roles.add(role);
		return roles;
	}
	public List<Role> getRoles(int roleType, String filter) {
		if(roleType == Constants.ROLE_TYPE_ALL)
			return roleDAO.getAllRoles();
		
		return roleDAO.getRoles(roleType, filter);
	}
	public Role getRole(Integer uid) {
		return roleDAO.get(uid);
	}

	public Map<Integer, Long> getRolesUsersCount() {
		return roleDAO.getRolesUsersCount();
	}

	//********************************************************************
	//               set / get
	//********************************************************************
    

	public void setUserService(UserService userService) {
		this.userService = userService;
	}


}
