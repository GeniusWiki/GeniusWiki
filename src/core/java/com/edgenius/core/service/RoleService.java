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
package com.edgenius.core.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.model.Role;

/**
 * @author Dapeng.Ni
 */
public interface RoleService {
	String SERVICE_NAME = "roleService";

	static final String createRole = "createRole";

	@Transactional(readOnly = true)
	public Role getRoleByName(String rolename);

	@Transactional(readOnly = true)
	public Role getRole(Integer uid);

	/**
	 * This method will be RoleIndexService intercepted
	 * 
	 * @param role
	 */
	public Role createRole(Role role);

	/**
	 * This method will NOT be RoleIndexService intercepted
	 * 
	 * @param role
	 */
	public Role saveRole(Role role);

	/**
	 * This method is not potent to do security check as it only called by
	 * removeSpace() which is invoked by QuartzJob in most case. Then it is
	 * impossible to get current user information as WikiUtil.getUser() only
	 * works in Servlet Request.
	 * 
	 * @param rolename
	 */
	public void removeRole(String rolename);

	/**
	 * 
	 * @return system default role for new signup user
	 */
	@Transactional(readOnly = true)
	public Set<Role> getDefaultRole();

	/**
	 * Return roles according to role type.
	 * 
	 * @param roleType
	 *            roleType -1: return all type of role. otherwise should be
	 *            Role.TYPE_SYSTEM or Role.TYPE_SPACE
	 */
	@Transactional(readOnly = true)
	public List<Role> getRoles(int roleType, String filter);

	/**
	 * @return Role.TYPE_SYSTEM and Role.TYPE_GROUP (except ROle.TYPE_SPACE)
	 *         include users count. Please note, if role does not include
	 *         users(count==0), it does not return.
	 */
	@Transactional(readOnly = true)
	public Map<Integer, Long> getRolesUsersCount();
}
