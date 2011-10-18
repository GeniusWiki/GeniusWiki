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
import java.util.List;
import java.util.Set;

import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.Constants;
import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.SecurityValues.SYSTEM_ROLES;
import com.edgenius.core.dao.ResourceDAO;
import com.edgenius.core.model.Permission;
import com.edgenius.core.model.Resource;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.AuditLogger;

/**
 * If put getXXX() method into UserService, as UserService class may do security check by SecurityService. 
 * So SecurityService cannot refer to UserService. Likewise,UserMethodBeforeAdvisor also need SecurityService 
 * for permission validation. In such case, Spring injection circle reference occurs. To avoid this, split UserService into
 * this class.
 * <br>
 * This class won't refer to any service classes, so it could be injected into any of services.
 * 
 * @author Dapeng.Ni
 */
@Service(UserReadingService.SERVICE_NAME)
@Transactional(readOnly=true)
public class UserReadingServiceImpl extends AbstractUserService implements UserReadingService {

	private ResourceDAO resourceDAO;
	
	public void removeUserFromCache(User user){
		if(user == null)
			userCache.removeAll();
		
		userCache.remove(user.getUsername());
		
	}
	
	@Transactional(readOnly= true, propagation = Propagation.REQUIRED)
	public User getUserByName(String username){
		Element element;
		if(StringUtils.isBlank(username) || User.ANONYMOUS_USERNAME.equalsIgnoreCase(username)){
			//anonymous user, its name in cache is User.ANONYMOUS_USERNAME
			element = userCache.get(User.ANONYMOUS_USERNAME);
		}else{
			element = userCache.get(username);
		}
		User user = null;
		if(element == null){
			if(StringUtils.isBlank(username) || User.ANONYMOUS_USERNAME.equalsIgnoreCase(username)){
	//			anonymous user, 
				user = getAnonymousUser();
			}else{
				try {
					user = userDAO.getUserByName(username);
				} catch (ObjectRetrievalFailureException e) {
					log.warn("User does not exist:"+username);
				} catch (Exception e) {
					log.error("Get user with error:"+username,e);
				}
			}
			if(user != null)
				putUserToCache(user);
		}else
			user = (User) element.getValue();
		
		return user;
	}
	//JDK1.6 @Override
	@Transactional(readOnly= true, propagation = Propagation.REQUIRED)
	public User getUser(Integer uid){
		if(uid == null || uid == 0 || uid == -1)
			return getAnonymousUser();
		
		return userDAO.get(uid);
	}
	//JDK1.6 @Override
	public List<User> getUsers(int start, int returnSize, String sortBy, String filter, boolean sortByDesc) {
		
		return userDAO.getUsers(start, returnSize,sortBy,filter, sortByDesc);
	}
	
	//JDK1.6 @Override
	public int getUserTotalCount(String filter) {

		return userDAO.getUserTotalCount(filter);
	}
	//JDK1.6 @Override
	public User getUserByEmail(String email) {
		return userDAO.getUserByEmail(email);
	}
	public Set<String> getSystemAdminMailList() {
		Set<String>  bccList = new HashSet<String>();
		Resource instanceRes = resourceDAO.getByName(Constants.INSTANCE_NAME);
		if(instanceRes == null){
			AuditLogger.error("Could not find instance resource ");
			return bccList;
		}
		
		Set<Permission> perms = instanceRes.getPermissions();
		for (Permission permission : perms) {
			if(permission.getOperation() == OPERATIONS.ADMIN){
				Set<User> users = permission.getUsers();
				if(users != null){
					for (User user : users) {
						if(user.isEnabled()){
							bccList.add(user.getContact().getEmail());
						}
					}
				}
				Set<Role> roles = permission.getRoles();
				if(roles != null){
					for (Role role : roles) {
						users = role.getUsers();
						if(users != null){
							for (User user : users) {
								if(user.isEnabled()){
									bccList.add(user.getContact().getEmail());
								}
							}
						}
					}
				}
			}
		}	
		
		return bccList;
	}
	
	public Set<String> getSpaceContributorMailList(String spaceUname) {
		return getSpaceUsersMailList(spaceUname,OPERATIONS.WRITE);
	}
	public Set<String> getSpaceAdminMailList(String spaceUname) {
		return getSpaceUsersMailList(spaceUname,OPERATIONS.ADMIN );
	}
	/**
	 * @param spaceUname
	 * @return
	 */
	private Set<String> getSpaceUsersMailList(String spaceUname, OPERATIONS oper) {
		Set<String>  bccList = new HashSet<String>();
		Resource spaceRes = resourceDAO.getByName(spaceUname);
		if(spaceRes == null){
			log.error("Could not find space resource " + spaceUname);
			return bccList;
		}
		
		Set<Permission> perms = spaceRes.getPermissions();
		for (Permission permission : perms) {
			if(permission.getOperation() == oper){
				Set<User> users = permission.getUsers();
				if(users != null){
					for (User user : users) {
						if(user.isEnabled()){
							bccList.add(user.getContact().getEmail());
						}
					}
				}
				Set<Role> roles = permission.getRoles();
				if(roles != null){
					for (Role role : roles) {
						if(SYSTEM_ROLES.ANONYMOUS.getName().equalsIgnoreCase(role.getName())){
							continue;
						}
						users = role.getUsers();
						if(users != null){
							for (User user : users) {
								if(user.isEnabled()){
									bccList.add(user.getContact().getEmail());
								}
							}
						}
					}
				}
			}
		}	
	
		return bccList;
	}

	public boolean isFollowing(User myself, User following) {
		myself = reload(myself);
		
		if(myself.getFollowings() != null){
			return myself.getFollowings().contains(following);
		}
		
		return false;
	}

	public void setResourceDAO(ResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}


}
