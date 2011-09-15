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

import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;

public interface UserService{

	String SERVICE_NAME = "userService";
	
	//some method need validation
	static final String saveUser = "saveUser"; 
	
	static final String updateUser = "updateUser"; 
	static final String updateUserWithIndex = "updateUserWithIndex";
	
	static final String uploadPortrait = "uploadPortrait"; 
	static final String removeUser = "removeUser"; 
	
	/*
	 * The return value is used for UserIndexInterceptor.	
	 */
	User saveUser(User user) throws UserExistsException, UserOverLimitedException;
	
	/**
	 * Remove user from userCache, if input is null, userCache will be cleaned.
	 * @param user
	 */
	void removeUserFromCache(User user);


	/**
	 * @param user
	 */
	User updateUser(User user);
	/**
	 * This method update user but skip  UserMethodBeforeAdvise security check.  
	 * For example, Security Update, if a space admin (user "A") update space permission, which may contains a special user "B",
	 * UserMethodBeforeAdvise will forbidden this update as user "A" is not system admin, and not user "B". In this 
	 * scenario, call this method
	 * 
	 * @param user
	 * @return
	 */
	User interalUpdateUser(User user);
	/**
	 * The only different this method with updateUser() is this method will update Lucene Index as well
	 * @param user
	 */
	User updateUserWithIndex(User user);

	/**
	 * @param my
	 * @param portrait
	 */
	void uploadPortrait(User my, FileNode portrait);

	/**
	 * @param uid
	 * @return
	 */
	User removeUser(Integer uid) throws UserRemoveException;

	/**
	 * This method won't do security check!
	 * @param user
	 * @param newPass
	 */
	void resetPassword(User user, String newPass);

	void follow(User myself, User follow);
	void unfollow(User myself, User follow);
}
