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

import com.edgenius.core.model.User;

/**
 * 
 * @author Dapeng.Ni
 */
public interface UserDAO extends DAO<User> {
    /**
     * Gets users information based on login name.
     * @param username the current username
     * @return user populated user object
     */
    public User getUserByName(String username);

    /**
     * Gets a list of users based on parameters passed in.
     * @param sortBy 
     * @param returnSize 
     * @param start 
     *
     * @return List populated list of users
     */
    public List<User> getUsers(int start, int returnSize, String sortBy, String filter, boolean sortByDesc);

    /**
     * Saves a user's information
     * @param user the object to be saved
     */
    public void saveUser(User user);

    /**
     * Removes a user from the database by id
     * @param username the user's username
     */
    public void removeUser(String username);
    
    /**
     * 
     * @param user
     */
    void refreshInstancePermissionCache(User user);


	 
	 User getUserByEmail(String email);
	/**
	 * @return
	 */
	public int getUserTotalCount(String filter);
	
}
