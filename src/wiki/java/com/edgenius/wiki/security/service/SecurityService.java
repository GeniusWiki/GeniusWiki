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
package com.edgenius.wiki.security.service;

import java.util.List;

import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.core.model.Permission;
import com.edgenius.core.model.Resource;
import com.edgenius.core.model.User;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.security.Policy;

public interface SecurityService {

	String SERVICE_NAME = "securityService";
	
	String getResourceMasks(String resourceName);
	List<Permission> getResourcePermission(RESOURCE_TYPES resourceType, String resourceName);
	
	/**
	 * @param resourceName
	 * @param resourceType
	 * @return
	 */
	Resource getResourceByName(String resourceName, RESOURCE_TYPES resourceType);
	Resource saveResource(String resourceName, RESOURCE_TYPES resourceType);
	void saveUpdatePermission(Permission newPerm);
	/**
	 * This method simply return all permission on this resource from table. There is NO any cascade calculation. 
	 * Using fillXXXXWikiOperations() method instead to get cascade permission.
	 * @param operation
	 * @param resourceName
	 * @return
	 */
	Permission getPermissionByOperationResource(OPERATIONS operation, String resourceName);
	void initResourcePermission(Widget widget);
	void initResourcePermission(Space space);
	/**
	 * According to this user's permission, to setup <code>Page.wikiOperations</code> array.
	 * @param user
	 * @param page
	 */
	void fillPageWikiOperations(User user, AbstractPage page);
	void fillSpaceWikiOperations(User user, Space space);
	void updateResource(Resource res);

	/**
	 * Use acegi login to make this user login
	 * @param username
	 * @param confirmPassword
	 */
	void login(String username, String confirmPassword);
	
	Policy findUrlPolicy(String url);
	Policy findBeforeMethodPolicy(String clz, String method, Object[] args);
	Policy findAfterMethodPolicy(String clz, String method, Object returnedObject);

	void resetPolicyCache(RESOURCE_TYPES type, String resourceName);
	/**
	 * Check if the user which may have multiple roles, could access this space. 
	 * strategy to check if readable: if any one of values(user name, or role name) from user has appear in permission list, 
	 * it will pass true.
	 * 
	 * @param user
	 * @return
	 */
	boolean isAllowSpaceReading(String spaceUname, User user);
	/**
	 * Actually, Page permission is forbidden model, which means permission is block if some user/role exist in Permission database
	 * table, so that, page permission verification must do together with its belonged space.

	 * @param spaceUname
	 * @param pageUuid
	 * @param user
	 * @return
	 */
	boolean isAllowPageReading(String spaceUname, String pageUuid, User user);
	/**
	 * check if this space is private
	 * @param spaceUname
	 * @return
	 */
	boolean isPrivateSpace(String spaceUname);
	
	void resetSpacePrivateCache(String spaceUname);
	/**
	 * @param resourceName
	 */
	void resetSpaceReadingCache(String resourceName);
	void removeResource(String resourceName);
	/**
	 * @param user
	 * @return
	 */
	boolean isAllowInstanceReading(User user);
	
	boolean isAllowWidget(OPERATIONS operation, String widgetUuid, User user);
	/**
	 * @param resourceName
	 */
	void resetPageReadingCache(String resourceName);
	/**
	 * Refresh Resource object from database.
	 * @param res
	 */
	void refreshResource(Resource res);


	
	/**
	 * !!! This method doesn't support Page level resource check!
	 * 
	 * Check if given user has admin permission on resource. This method is not calculate cascade, eg, 
	 * if user has space resource admin permission, but he has not instance admin permission, this method
	 * still return true if check space admin permission.
	 *  
	 * @param resourceName
	 * @param resourceType
	 * @param user
	 * @return
	 */
	boolean isAllowResourceAdmin(String resourceName, RESOURCE_TYPES resourceType, User user);

	/**
	 * Get Resource list which user has specified operation.
	 * @param user
	 * @param oper
	 * @return
	 */
	List<Resource> getResourceOfUserHasOperation(User user, OPERATIONS oper);
	
	/**
	 * !!! This method always give valid authentication and grants to give user. So it is very important to do password validate if
	 * this request is outside of system. For example, Webservice request. But for internal request, such as Quartz job, it is OK 
	 * without password valid - actually, it has no way validate password as it can't get plain password.
	 * 
	 * Login given user as if it login from web. This will help some methods to pass security permission authentication. 
	 * For example, Quartz job is running without web, but it may need system  admin (at least, space admin, such as remove space)permission.
	 * 
	 * NOTE, this method only valid if alwaysReauthenticate in MethodSecurityInterceptor.java(AbstractSecurityInterceptor.java) is false.
	 * 
	 * @param username
	 * @see org.springframework.security.intercept.AbstractSecurityInterceptor.authenticateIfRequired()
	 * 
	 */
	void proxyLogin(String username);
	/**
	 * This is dangerous method and won't expected used except you know you are doing:)
	 * 
	 * It will login with system admin. Please remember pair with proxyLogout. 
	 * 
	 * @param login user name
	 */
	String proxyLoginAsSystemAdmin();
	String proxyLoginAsSpaceAdmin(String spaceUname);
	void proxyLogout();
	/**
	 * @param spaceUname
	 */

	
}
