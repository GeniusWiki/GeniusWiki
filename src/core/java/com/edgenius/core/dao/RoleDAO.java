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
package com.edgenius.core.dao;

import java.util.List;
import java.util.Map;

import com.edgenius.core.model.Role;
/**
 * 
 * @author dapeng
 *
 */
public interface RoleDAO extends DAO<Role> {
    /**
     * Gets roles information based on login name.
     * @param rolename the current rolename
     * @return role populated role object
     */
    public Role getByName(String rolename);

    /**
     * Gets a list of roles based on parameters passed in.
     * @param roleType 
     *
     * @return List populated list of roles
     */
    public List<Role> getRoles(int roleType, String filter);
    public List<Role> getAllRoles();

    /**
     * Saves a role's information
     * @param role the object to be saved
     */
    public Role saveRole(Role role);

    /**
     * Removes a role from the database by name
     * @param rolename the role's rolename
     */
    public void removeByName(String rolename);

	/**
	 *  @return Role.TYPE_SYSTEM and Role.TYPE_GROUP (except ROle.TYPE_SPACE) include users count.
	 */
	public Map<Integer, Long> getRolesUsersCount();
}
