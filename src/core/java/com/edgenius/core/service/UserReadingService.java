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
import java.util.Set;

import com.edgenius.core.model.User;

/**
 * @author Dapeng.Ni
 */
public interface UserReadingService {

	String SERVICE_NAME = "userReadingService";

	/**
	 * If user name is blank or null, return anonymous User object. if user does not exist, return null;
	 * 
	 * Please note, this user can be get from Cache instead of Database. So it has not guarantee to following method chain works: 
	 * <li>User.getPermissions()[n].getResource().getPermissions() - It means the permissions of resource could be null. See Permission.clone() and Resource.clone()</li>
	 * <li>User.getPermissions()[n].getUsers() </li>
	 * <li>User.getPermissions()[n].getRoles() </li>
	 * <li>User.getRoles()[n].getUsers() - It means the users of role could be null. See User.clone() and Role.clone()</li>
	 *  
	 * @param user name
	 * @return
	 */
	User getUserByName(String username);

	/**
	 * @param uid
	 * @return
	 */
	User getUser(Integer uid);
	


	/**
	 * @param sortBy see com.edgenius.core.model.User SORTY_BY_ constants
	 *  
	 * @param returnSize 
	 * @param start 
	 * @param sortBy  Its values is combination from User.SORT_BY_* and separated by "|".  The result can be sorted by 
	 * 				contact email, user name, full-name and create date. 
	 * @param filter filter by user name, full-name or email address.
	 * @sortByDesc true is sort result by desc, otherwise asc.
	 * 
	 * @return all users in system, this is Database reading rather than reading from Cache.
	 */
	List<User> getUsers(int start, int returnSize, String sortBy, String filter, boolean sortByDesc);

	/**
	 * @return
	 */
	int getUserTotalCount(String filter);

	/**
	 * @param email
	 * @return
	 */
	User getUserByEmail(String email);
	
	Set<String> getSystemAdminMailList();
	/**
	 * @param spaceUname
	 */
	Set<String> getSpaceAdminMailList(String spaceUname);
	
	Set<String> getSpaceContributorMailList(String spaceUname);
	
	/**
	 * Delete user from user cache - there isn't any impact on user data on database.
	 * If parameter of user is null, this will clear user cache.
	 * @param user
	 */
	void removeUserFromCache(User user);

	/**
	 * Check if myself is following the 2nd user in parameters.
	 * @param myself
	 * @param following
	 * @return
	 */
	boolean isFollowing(User myself, User following);
}
