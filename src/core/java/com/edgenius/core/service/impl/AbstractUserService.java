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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.edgenius.core.Constants;
import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.SecurityValues.SYSTEM_ROLES;
import com.edgenius.core.dao.PermissionDAO;
import com.edgenius.core.dao.RoleDAO;
import com.edgenius.core.dao.UserDAO;
import com.edgenius.core.dao.hibernate.UserDAOHibernate;
import com.edgenius.core.model.Permission;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.service.MessageService;

/**
 * @author Dapeng.Ni
 */
public class AbstractUserService implements InitializingBean{
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	protected String anonymouseFullname;
	protected Cache userCache;
	protected UserDAO userDAO;
	protected RoleDAO roleDAO;
	protected PermissionDAO permissionDAO;
	private MessageService messageService;

	protected User reload(User user) {
		if(user != null && !user.isAnonymous())
			return userDAO.get(user.getUid());
		
		return user;
	}
	
	protected void putUserToCache(User user){
		//need refresh user instance 
		userDAO.refreshInstancePermissionCache(user);
		
		//put cloned object into cache: avoid some lazy loading later
		//TODO: not all object deep cloned because of clone deadlock problem.
		User cUser = (User) user.clone();
		//don't cache followers
		cUser.setFollowers(null);
		cUser.setFollowings(null);
		Element element = new Element(user.getUsername(), cUser);
		log.info("User Cache put: {}" , element.getKey());
		
		userCache.put(element);
	}
	protected User getAnonymousUser() {
		User user;
		user = new User();
		user.setUid(-1);
		user.setUsername(User.ANONYMOUS_USERNAME);
		user.setFullname(anonymouseFullname);
		user.setEnabled(true);
		
		Role role = roleDAO.getByName(SYSTEM_ROLES.ANONYMOUS.getName());
		List<OPERATIONS> wikiPermissions = new ArrayList<OPERATIONS>();
		//performance consideration: don't use role.getPermission() as admin or user has too many permissions in large space volume case.
		Set<Permission> perms = new HashSet<Permission>(permissionDAO.getByRoleResource(SYSTEM_ROLES.ANONYMOUS.getName(),Constants.INSTANCE_NAME));
		UserDAOHibernate.getInstancePermission(wikiPermissions, perms);
		user.setWikiPermissions(wikiPermissions);
		Set<Role> roles = new HashSet<Role>();
		roles.add((Role) role.clone());
		user.setRoles(roles);
		return user;
	}
	//JDK1.6 @Override
	public void afterPropertiesSet() throws Exception {
		anonymouseFullname = messageService.getMessage(Constants.I18N_ANONYMOUS_USER);
	}

	//********************************************************************
	//               set / get
	//********************************************************************
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}
	
	public void setUserCache(Cache userCache) {
		this.userCache = userCache;
	}

	public void setRoleDAO(RoleDAO roleDAO) {
		this.roleDAO = roleDAO;
	}
	public void setPermissionDAO(PermissionDAO permissionDAO) {
		this.permissionDAO = permissionDAO;
	}

}
