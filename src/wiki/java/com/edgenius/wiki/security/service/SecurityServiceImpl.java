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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.SecurityValues;
import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.core.SecurityValues.SYSTEM_ROLES;
import com.edgenius.core.dao.PermissionDAO;
import com.edgenius.core.dao.ResourceDAO;
import com.edgenius.core.dao.RoleDAO;
import com.edgenius.core.model.Permission;
import com.edgenius.core.model.Resource;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.dao.SpaceDAO;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.Draft;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.security.Policy;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;
import com.edgenius.wiki.security.strategy.PatternStrategy;
import com.edgenius.wiki.util.ProxyLoginUtil;

/**
 * @author Dapeng.Ni
 */
@Transactional
public class SecurityServiceImpl implements SecurityService, InitializingBean {
	private static final Logger log = LoggerFactory.getLogger(SecurityServiceImpl.class);
	//
	private static final String PRIVATE_SPACE_PREFIX = "private_";
	private static final String ANONYMOUS_KEY = "changeThis";
	private static final String ANONYMOUS_PASSWORD = "anonymousUser";
	
	private PatternStrategy patternStrategy;
	private ProviderManager authenticationManager;
	
	private boolean initialSpaceReadingCacheAtStart;
	private RoleDAO roleDAO;
	private UserReadingService userReadingService;
	private PageDAO pageDAO;
	private SpaceDAO spaceDAO;
	private ResourceDAO resourceDAO;
	private PermissionDAO permissionDAO;
	
	private Cache spaceReadingCache;
	private Cache pageReadingCache;
	//********************************************************************
	//               Implement SecurityService methods
	//********************************************************************
	public Policy findUrlPolicy(String url){
		WikiOPERATIONS wikiType = patternStrategy.findURLRuntimePattern(url);
		
		//TODO: so far only support INSTANCE/SPACE scope url matching
		if(wikiType != null){
			List<Policy> policies = null;
			if(wikiType.type == RESOURCE_TYPES.INSTANCE){
				policies = patternStrategy.getPolicies(RESOURCE_TYPES.INSTANCE);
				
			}else if(wikiType.type == RESOURCE_TYPES.SPACE){
				policies = patternStrategy.getPolicies(RESOURCE_TYPES.SPACE,wikiType.values.get(RESOURCE_TYPES.SPACE));
			}
			//get all resources in given RESOURCE_TYPES and OPERATION
			//now, need check policy in cache to find out if this type/operation policy exist. 
			//For example, it may not exist if page permission is not defined.
			if(policies != null){
				for (Policy policy : policies) {
					//match: resource Type, name and operation
					if(policy.getOperation() == wikiType.operation
						&& policy.getType() == wikiType.type){
						return policy;
					}
				}
			}
		}
		
		return null;
		
	}
	public Policy findBeforeMethodPolicy(String clz, String method, Object[] args){
		return findMethodPolicy(clz, method, args, PatternStrategy.BEFORE_METHOD);
	}
	public Policy findAfterMethodPolicy(String clz, String method, Object returnedObject) {
		return findMethodPolicy(clz, method, new Object[]{returnedObject}, PatternStrategy.AFTER_METHOD);
	}

	public void fillPageWikiOperations(User user, AbstractPage page){
		String spaceUname = page.getSpace().getUnixName();
		
		String pageUuid = page.getPageUuid();
		
		//this will return space and also page policy in this space
		List<Policy> policyList = patternStrategy.getPolicies(RESOURCE_TYPES.SPACE,spaceUname);
		List<String> roleUserList = getRoleUserNameList(user);
		
		//page policy prior to space's,e.g., if page policy exist, and not include this user. Then this user won't have this policy's operaion
		//even this page's space give user this policy. 
		int[] p1 = new int[10];
		int[] s1= new int[10];
		for (Policy policy : policyList) {
			if(RESOURCE_TYPES.PAGE.equals(policy.getType())){
				if(StringUtils.equals(policy.getResourceName(),pageUuid)){
					//it is this page's policy, need check if this policy allow current user
					//for page policy, if this policy does not contain user, means not allow!!! 
					operMatrix(p1, roleUserList, policy);
				}
			}else if(RESOURCE_TYPES.SPACE.equals(policy.getType())){
				if(StringUtils.equals(policy.getResourceName(),spaceUname)){
					operMatrix(s1, roleUserList, policy);
				}
			}
		}

		//get page permission according to new page, draft or existed page
		List<WikiOPERATIONS> wikiOpers = new ArrayList<WikiOPERATIONS>();
		if(page instanceof Draft){
			//TODO does it need special configure? it will confuse if this page has exist page and draft 
		}else{
//			exist page/create new page permission
			//get all policies for this page for current user, 
			if((p1[OPERATIONS.READ.ordinal()] == 0 && s1[OPERATIONS.READ.ordinal()] == 1) 
				|| p1[OPERATIONS.READ.ordinal()] == 1){
				wikiOpers.add(WikiOPERATIONS.PAGE_READ);
			}
			if((p1[OPERATIONS.WRITE.ordinal()] == 0 && s1[OPERATIONS.WRITE.ordinal()] == 1) 
					|| p1[OPERATIONS.WRITE.ordinal()] == 1){
				wikiOpers.add(WikiOPERATIONS.PAGE_WRITE);
			}
			if((p1[OPERATIONS.REMOVE.ordinal()] == 0 && s1[OPERATIONS.REMOVE.ordinal()] == 1) 
					|| p1[OPERATIONS.REMOVE.ordinal()] == 1){
				wikiOpers.add(WikiOPERATIONS.PAGE_REMOVE);
			}
			if((p1[OPERATIONS.COMMENT_READ.ordinal()] == 0 && s1[OPERATIONS.COMMENT_READ.ordinal()] == 1) 
					|| p1[OPERATIONS.COMMENT_READ.ordinal()] == 1){
				wikiOpers.add(WikiOPERATIONS.PAGE_COMMENT_READ);
			}
			if((p1[OPERATIONS.COMMENT_WRITE.ordinal()] == 0 && s1[OPERATIONS.COMMENT_WRITE.ordinal()] == 1) 
					|| p1[OPERATIONS.COMMENT_WRITE.ordinal()] == 1){
				wikiOpers.add(WikiOPERATIONS.PAGE_COMMENT_WRITE);
			}
			if((p1[OPERATIONS.OFFLINE.ordinal()] == 0 && s1[OPERATIONS.OFFLINE.ordinal()] == 1) 
					|| p1[OPERATIONS.COMMENT_WRITE.ordinal()] == 1){
				wikiOpers.add(WikiOPERATIONS.PAGE_OFFLINE);
			}
			//space level page permission:
			if(s1[OPERATIONS.RESTRICT.ordinal()] == 1)
				wikiOpers.add(WikiOPERATIONS.SPACE_PAGE_RESTRICT);
			if(s1[OPERATIONS.ADMIN.ordinal()] == 1)
				wikiOpers.add(WikiOPERATIONS.SPACE_ADMIN);
			if(s1[OPERATIONS.EXPORT.ordinal()] == 1)
				wikiOpers.add(WikiOPERATIONS.SPACE_EXPORT);
		}

		
		page.setWikiOperations(wikiOpers);
	}

	public void fillSpaceWikiOperations(User user, Space space) {
		List<String> roleUserList = getRoleUserNameList(user);
		String spaceUname = space.getUnixName();
		List<Policy> policyList = patternStrategy.getPolicies(RESOURCE_TYPES.SPACE, spaceUname);
		int[] s1= new int[10];
		for (Policy policy : policyList) {
			if(RESOURCE_TYPES.SPACE.equals(policy.getType()) 
				&& StringUtils.equals(policy.getResourceName(),spaceUname)){
				operMatrix(s1, roleUserList, policy);
			}
		}
		//get all policies for this page for current user, 
		List<WikiOPERATIONS> wikiOpers = new ArrayList<WikiOPERATIONS>();
		if(s1[OPERATIONS.READ.ordinal()] == 1)
			wikiOpers.add(WikiOPERATIONS.SPACE_PAGE_READ);
		if(s1[OPERATIONS.WRITE.ordinal()] == 1)
			wikiOpers.add(WikiOPERATIONS.SPACE_PAGE_WRITE);
		if(s1[OPERATIONS.REMOVE.ordinal()] == 1)
			wikiOpers.add(WikiOPERATIONS.SPACE_PAGE_REMOVE);
		if(s1[OPERATIONS.COMMENT_READ.ordinal()] == 1)
			wikiOpers.add(WikiOPERATIONS.SPACE_COMMENT_READ);
		if(s1[OPERATIONS.COMMENT_WRITE.ordinal()] == 1)
			wikiOpers.add(WikiOPERATIONS.SPACE_COMMENT_WRITE);
		if(s1[OPERATIONS.RESTRICT.ordinal()] == 1)
			wikiOpers.add(WikiOPERATIONS.SPACE_PAGE_RESTRICT);
		if(s1[OPERATIONS.EXPORT.ordinal()] == 1)
			wikiOpers.add(WikiOPERATIONS.SPACE_EXPORT);
		if(s1[OPERATIONS.ADMIN.ordinal()] == 1)
			wikiOpers.add(WikiOPERATIONS.SPACE_ADMIN);
		if(s1[OPERATIONS.OFFLINE.ordinal()] == 1)
			wikiOpers.add(WikiOPERATIONS.SPACE_OFFLINE);
		
		space.setWikiOperations(wikiOpers);
		
	}

	//JDK1.6 @Override
	public void initResourcePermission(Widget widget) {
		Role adminRole = roleDAO.getByName(SYSTEM_ROLES.ADMIN.getName());
		Role registerRole = roleDAO.getByName(SYSTEM_ROLES.USERS.getName());
		Role anonymousRole = roleDAO.getByName(SYSTEM_ROLES.ANONYMOUS.getName());
		Set<Role> anonyUsersAdmin = new HashSet<Role>();
		anonyUsersAdmin.add(adminRole);
		anonyUsersAdmin.add(registerRole);
		anonyUsersAdmin.add(anonymousRole);
		
		Set<Role> adminRoles = new HashSet<Role>();
		adminRoles.add(adminRole);
		
		//create resource
		Resource resource = new Resource();
		resource.setType(RESOURCE_TYPES.WIDGET);
		resource.setResource(widget.getUuid());
		
		//create default permission as well
		Set<User> users = new HashSet<User>();
		if(widget.getCreator() != null)
			users.add(widget.getCreator());
		
		//give space create all permission:
		Permission read = new Permission();
		read.setOperation(OPERATIONS.READ);
		read.setResource(resource);
		//create permission
		read.setUsers(users);
		if(widget.isShared()){ // EVEN disable system admin???
			//give admin/register/anonymous user role "READ" permission
			read.setRoles(anonyUsersAdmin);
		}
		permissionDAO.saveOrUpdate(read);
		
		Permission write = new Permission();
		write.setOperation(OPERATIONS.WRITE);
		write.setResource(resource);
		write.setUsers(users);
		if(widget.isShared()){ // EVEN disable system admin???
	//		give admin user role "WRITE" permission
			write.setRoles(adminRoles);
		}
		permissionDAO.saveOrUpdate(write);
		
		
		//also need add permissions to resource here, although it is not necessary for persist
		//but it is necessay when initCache() when using resource.getPermssions() in PatternFactory
		Set<Permission> permissions = new HashSet<Permission>();
		permissions.add(read);
		permissions.add(write);
		resource.setPermissions(permissions);
		resourceDAO.saveOrUpdate(resource);
	}
	
	//JDK1.6 @Override
	public void initResourcePermission(Space space) {
		Role adminRole = roleDAO.getByName(SYSTEM_ROLES.ADMIN.getName());
		Role registerRole = roleDAO.getByName(SYSTEM_ROLES.USERS.getName());
		Role anonymousRole = roleDAO.getByName(SYSTEM_ROLES.ANONYMOUS.getName());
		Set<Role> anonyUsersAdmin = new HashSet<Role>();
		anonyUsersAdmin.add(adminRole);
		anonyUsersAdmin.add(registerRole);
		anonyUsersAdmin.add(anonymousRole);
		
		Set<Role> regsiterUsersAdmin = new HashSet<Role>();
		regsiterUsersAdmin.add(adminRole);
		regsiterUsersAdmin.add(registerRole);
		Set<Role> adminRoles = new HashSet<Role>();
		adminRoles.add(adminRole);
		
		//create resource
		Resource resource = new Resource();
		resource.setType(RESOURCE_TYPES.SPACE);
		resource.setResource(space.getUnixName());
		
		//create default permission as well
		Set<User> users = new HashSet<User>();
		if(space.getCreator() != null)
			users.add(space.getCreator());
		
		//give space create all permission:
		Permission read = new Permission();
		read.setOperation(OPERATIONS.READ);
		read.setResource(resource);
		//create permission
		read.setUsers(users);
		if(space.isPrivate())
			//private, only admin ROLE has read perm 
			read.setRoles(adminRoles);
		else
			//give admin/register/anonymous user role "READ" permission
			read.setRoles(anonyUsersAdmin);
		permissionDAO.saveOrUpdate(read);
		
		Permission write = new Permission();
		write.setOperation(OPERATIONS.WRITE);
		write.setResource(resource);
		write.setUsers(users);
//		give admin user role "WRITE" permission
		write.setRoles(adminRoles);
		permissionDAO.saveOrUpdate(write);
		
		Permission remove = new Permission();
		remove.setOperation(OPERATIONS.REMOVE);
		remove.setResource(resource);
		remove.setUsers(users);
//		give admin user role "REMOVE" permission
		remove.setRoles(adminRoles);
		permissionDAO.saveOrUpdate(remove);
		
		Permission restrict = new Permission();
		restrict.setOperation(OPERATIONS.RESTRICT);
		restrict.setResource(resource);
		restrict.setUsers(users);
//		give admin user role "REMOVE" permission
		restrict.setRoles(adminRoles);
		permissionDAO.saveOrUpdate(restrict);
		
//		Permission export = new Permission();
//		export.setOperation(OPERATIONS.EXPORT);
//		export.setResource(resource);
//		export.setUsers(users);
//		//give admin/register user role "READ" permission
//		export.setRoles(regsiterUsersAdmin);
//		permissionDAO.saveOrUpdate(export);
		
		Permission admin = new Permission();
		admin.setOperation(OPERATIONS.ADMIN);
		admin.setResource(resource);
		admin.setUsers(users);
		//give admin user role "admin" permission
		admin.setRoles(adminRoles);
		permissionDAO.saveOrUpdate(admin);

		Permission commentRead = new Permission();
		commentRead.setOperation(OPERATIONS.COMMENT_READ);
		commentRead.setResource(resource);
		commentRead.setUsers(users);
//		give any user role "READ comment" permission
		commentRead.setRoles(anonyUsersAdmin);
		permissionDAO.saveOrUpdate(commentRead);
		
		Permission commentWrite = new Permission();
		commentWrite.setOperation(OPERATIONS.COMMENT_WRITE);
		commentWrite.setResource(resource);
		commentWrite.setUsers(users);
//		give any role "WRITE comment" permission
		commentWrite.setRoles(anonyUsersAdmin);		
		permissionDAO.saveOrUpdate(commentWrite);
		
		Permission offline = new Permission();
		offline.setOperation(OPERATIONS.OFFLINE);
		offline.setResource(resource);
		offline.setUsers(users);
//		give any role "WRITE comment" permission
		offline.setRoles(anonyUsersAdmin);		
		permissionDAO.saveOrUpdate(offline);
		
		//also need add permissions to resource here, although it is not necessary for persist
		//but it is necessay when initCache() when using resource.getPermssions() in PatternFactory
		Set<Permission> permissions = new HashSet<Permission>();
		permissions.add(read);
		permissions.add(write);
		permissions.add(remove);
		permissions.add(restrict);
//		permissions.add(export);
		permissions.add(admin);
		permissions.add(commentRead);
		permissions.add(commentWrite);
		permissions.add(offline);
		resource.setPermissions(permissions);
		resourceDAO.saveOrUpdate(resource);
		
	}
	
	@Transactional(readOnly= false, propagation = Propagation.REQUIRES_NEW)
	public Resource getResourceByName(String resourceName,RESOURCE_TYPES resourceType) {
		
		return resourceDAO.getByName(resourceName);
	}
	
	public Resource saveResource(String resourceName, RESOURCE_TYPES resourceType) {
		Resource resource;
		//create resource
		resource = new Resource();
		resource.setType(resourceType);
		resource.setResource(resourceName);
		resourceDAO.saveOrUpdate(resource);
		return resource;
	}
	
	public Permission getPermissionByOperationResource(OPERATIONS operation, String resourceName) {
		return permissionDAO.getByOperationResource(operation,resourceName);
	}

	public String getResourceMasks(String resourceName){
		//currently, only page has masks feature: mask operations that any role/user which added new user in space level 
		Resource res = resourceDAO.getByName(resourceName);
		if(res != null)
			return res.getMasks();
		else
			return "";
	}
	
	/**
	 * This method gets matrix of user/role and operations of specific resource.
	 */
	//FIXME: annotation not work!!! in readonly true, any change in permission, will persist
	@Transactional(readOnly=true, propagation = Propagation.REQUIRED)
	public List<Permission> getResourcePermission(RESOURCE_TYPES resourceType, String resourceName) {
		
		if(!RESOURCE_TYPES.PAGE.equals(resourceType)){
			List<Policy> policies = patternStrategy.getPolicies(resourceType, resourceName);
			List<Permission> permList = permissionDAO.getByResource(resourceName);
//			mark dead policy: e.g., space A allow R1 to read, but INSTANCE does not allow R1 read, so, policy on R1 read space A is dead
			for (Policy policy : policies) {
				//find this resource's policies in cache: which already finish "purge", "conflict" handle etc.
				//possible resource are instance and space
				if(resourceType.equals(policy.getType())
					&& StringUtils.equalsIgnoreCase(policy.getResourceName(),resourceName)){
					//check this policies role/user if remove from Permission, if so, put such role/user into Permission.deadRoleUserList
					List<String> nameList = new ArrayList<String>();
					for(Iterator<ConfigAttribute> iter = policy.getMutableAttributeDefinition().iterator();iter.hasNext();){
						nameList.add(iter.next().getAttribute());
					}
					findDeadRoleUser(permList, policy.getOperation(),nameList, false);
				}
			}
			
			return permList;
		}
		//For page, it inherit permission from its space, so do following
		//1. retrieve policyCache get its space policies,  create a permission object according to this policy
		//2. get page permission from DB(NOT from cache, cache maybe be modified because space READ permission will interfere to other policies!). 
		//3. then, reset user/role permission created on 1 step by page policy, and put any removed user/role into Permission.deadRoleUserList
		// actually, current Permission.deadRoleUserList becomes a liveRoleUserList(live in space level, but dead by page permission)
		
		List<Permission> pagePermList = new ArrayList<Permission>();
		Resource res = resourceDAO.getByName(resourceName);
		
		Set<Permission> perms = new HashSet<Permission>();
		//for page resource, resource may not exist if user never set permission for this page
		if(res != null)
			perms = res.getPermissions();
		
		Page page = pageDAO.getCurrentByUuid(resourceName);
		if(page == null){
			log.error("Could not find page by uuid:" + resourceName);
			return new ArrayList<Permission>();
		}
		
		String spaceUname = page.getSpace().getUnixName();
		List<Policy> policies = patternStrategy.getPolicies(RESOURCE_TYPES.SPACE, spaceUname);
		for (Policy policy : policies) {
			if(RESOURCE_TYPES.SPACE.equals(policy.getType())
				&& StringUtils.equalsIgnoreCase(policy.getResourceName(),spaceUname)){
				//get this page's space policy
				Permission perm = new Permission();
				Set<Role> roles = new HashSet<Role>();
				Set<User> users = new HashSet<User>();
				for(Iterator<ConfigAttribute> iter = policy.getMutableAttributeDefinition().iterator();iter.hasNext();){
					ConfigAttribute att = iter.next();
					if(att.getAttribute().startsWith(Role.ROLE_PREFIX)){
						Role role = roleDAO.getByName(att.getAttribute());
						roles.add(role);
					}else{
						User user = userReadingService.getUserByName(att.getAttribute().substring(Role.USER_PREFIX.length()));
						if(user != null)
							users.add(user);
					}
				}
				perm.setRoles(roles);
				perm.setUsers(users);
				perm.setOperation(policy.getOperation());
				//just create a proxy resource to hold type
				Resource resource = new Resource();
				resource.setType(RESOURCE_TYPES.PAGE);
				perm.setResource(resource);
				//not necessary fields in permission, don't set others
				
				pagePermList.add(perm);
			}
		}

		for (Permission permission : perms) {
			List<String> nameList = new ArrayList<String>();
			Set<User> users = permission.getUsers();
			if(users != null){
				for (User user : users) {
					nameList.add(Role.USER_PREFIX+user.getUsername());
				}
			}
			Set<Role> roles = permission.getRoles();
			if(roles != null){
				for (Role role : roles) {
					nameList.add(role.getName());
				}
			}
			findDeadRoleUser(pagePermList, permission.getOperation(),nameList, true);
		}
		
		return pagePermList;
//		COMMENTS: don't use database read, it won't check space live conditions

			
	}

	public void updateResource(Resource res) {
		resourceDAO.saveOrUpdate(res);
	}

	public void saveUpdatePermission(Permission newPerm) {
		permissionDAO.saveOrUpdate(newPerm);
		
	}
	public void login(String username, String confirmPassword) {
        // log user in automatically
        Authentication auth = new UsernamePasswordAuthenticationToken(username, confirmPassword);
        SecurityContextHolder.getContext().setAuthentication(authenticationManager.authenticate(auth));
	}
	public void resetPolicyCache(RESOURCE_TYPES type, String resourceName) {
		log.info("Resource " + resourceName + " of type " + type.name() + "  policy cache is reset.");
		patternStrategy.resetCache(type,resourceName);
		
	}
	public void refreshResource(Resource res){
		resourceDAO.refresh(res);
	}
	

	public boolean isAllowWidget(OPERATIONS operation, String widgetUuid, User user) {
		
		List<Policy> policyList = patternStrategy.getPolicies(RESOURCE_TYPES.WIDGET, widgetUuid);
		List<String> readPerm = new ArrayList<String>();
		if(policyList != null){
			for (Policy policy : policyList) {
				if(policy.getOperation() == operation){
					Iterator<ConfigAttribute> iter = policy.getMutableAttributeDefinition().iterator();
					while(iter.hasNext()){
						ConfigAttribute att = iter.next();
						readPerm.add(att.getAttribute());
					}
				}
			}
		}
		List<String> roleUserList = getRoleUserNameList(user);
		return isAllow(roleUserList, readPerm);
	}
	

	public boolean isAllowInstanceReading(User user) {
		if(!user.isEnabled())
			return false;
		
		List<Policy> policyList = patternStrategy.getPolicies(RESOURCE_TYPES.INSTANCE);
		List<String> readPerm = new ArrayList<String>();
		if(policyList != null){
			for (Policy policy : policyList) {
				if(policy.getOperation() == OPERATIONS.READ){
					Iterator<ConfigAttribute> iter = policy.getMutableAttributeDefinition().iterator();
					while(iter.hasNext()){
						ConfigAttribute att = iter.next();
						readPerm.add(att.getAttribute());
					}
				}
			}
		}
		List<String> roleUserList = getRoleUserNameList(user);
		return isAllow(roleUserList, readPerm);
	}
	public boolean isPrivateSpace(String spaceUname) {
		Element perm = getSpacePrivate(spaceUname);
		//just for failure tolerance
		if(perm == null)
			return false;
		
		Boolean pri = (Boolean) perm.getValue();
		return pri.booleanValue();
	}
	
	@SuppressWarnings("unchecked")
	public boolean isAllowSpaceReading(String spaceUname, User user) {
		if(isAllowResourceAdmin(SharedConstants.INSTANCE_NAME,SecurityValues.RESOURCE_TYPES.INSTANCE, user)){
			//quick check, if system admin, always allow
			return true;
		}
		if(!isAllowInstanceReading(user))
			return false;
		
		Element perm = getSpaceReadPermission(spaceUname);
		//just for failure tolerance
		if(perm == null){
			if(!SharedConstants.SYSTEM_SPACEUNAME.equals(spaceUname))
				AuditLogger.error("Unexpected case:Unable find space reading cache for space " + spaceUname);
			return false;
		}
		
		List<String> roleUserList = getRoleUserNameList(user);
		
		List<String> readPerm = (List<String>) perm.getValue();
		
		return isAllow(roleUserList, readPerm);
		
	}


	@SuppressWarnings("unchecked")
	public boolean isAllowPageReading(String spaceUname, String pageUuid, User user) {
		if(isAllowResourceAdmin(SharedConstants.INSTANCE_NAME,SecurityValues.RESOURCE_TYPES.INSTANCE, user)){
			//quick check, if system admin, always allow
			return true;
		}
		
		if(!isAllowInstanceReading(user))
			return false;
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//here, check spaceScop reading permission, does not user isAllowSpaceReading(), 
		// just for roleUserList will resue in page reading check
		Element spacePerm = getSpaceReadPermission(spaceUname);
		List<String> roleUserList = getRoleUserNameList(user);
		//just for failure tolerance
		if(spacePerm == null){
			AuditLogger.warn("Unexpected case: Unable to get space " + spaceUname + " reading permission!");
			return false;
		}
		
		List<String> readPerm = (List<String>) spacePerm.getValue();
		//if the space does not allow reading, then no necessary to check further.
		if(!isAllow(roleUserList, readPerm)){
			//while for RSS function happens in MQ receiver side, user information is null.
			log.info("Space " + spaceUname + " does not allow " + (user==null?"anonymous":user.getUsername()) + " reading page " + pageUuid);
			return false;
		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		//NOW, check page permission.
//			Note, page permission is forbidden list. It means only users/roles 
//			who has NO permission will appear in list. This list will work together with its space permission.
		Element pagePerm = getPageReadPermission(pageUuid);
		//if page no extra permission, then its permission inherit from Space, it should be ALLOW read. 
		if(pagePerm == null || pagePerm.getValue() == null)
			return true;
		
		List<String> pageForbidPerm = (List<String>) pagePerm.getValue();
		//remove all page forbidden users/roles in space permission, and check again
		
		//Important: clone readPerm, so that readPerm value does not change, which should be readonly, otherwise, cache value will impact.
		List<String> pageReadPerm = new ArrayList<String>();
		pageReadPerm.addAll(readPerm);
		for (String forbid: pageForbidPerm) {
			pageReadPerm.remove(forbid);
		}
		
		return isAllow(roleUserList, pageReadPerm);
		
	}
	public void resetSpacePrivateCache(String spaceUname){
		log.info("Space " + spaceUname + " private cache is reset.");
		spaceReadingCache.remove(PRIVATE_SPACE_PREFIX + spaceUname);
		getSpacePrivate(spaceUname);
	}
	
	public void resetSpaceReadingCache(String spaceUname){
		log.info("Space " + spaceUname + " reading cache is reset.");
		spaceReadingCache.remove(spaceUname);
		getSpaceReadPermission(spaceUname);
		
	}
	public void removeResource(String resourceName){
		
		Resource res = resourceDAO.getByName(resourceName);
		if(res != null){
			Set<Permission> perList = new HashSet<Permission>();
			Set<Permission> list = res.getPermissions();
			for (Iterator<Permission> iter = list.iterator(); iter.hasNext();) {
				Permission perm = iter.next();
				perm.setResource(null);
				perList.add(perm);
				iter.remove();
			}
			
			//NOTE: permission will remove by cascade.
			resourceDAO.removeObject(res);
			
			for (Permission permission : perList) {
				permission.getUsers().clear();
				permission.getRoles().clear();
				permissionDAO.removeObject(permission);
			}
		
			if(res.getType() == RESOURCE_TYPES.SPACE){
				//should be spaceUname
				spaceReadingCache.remove(resourceName);
			}else if(res.getType() == RESOURCE_TYPES.PAGE){
				//should be pageUuid
				pageReadingCache.remove(resourceName);
			}
			//	no handle to instance
			//reset corresponding policy cache
			patternStrategy.resetCache(res.getType(),resourceName);
			log.info("Resource type " + res.getType() + " name " + resourceName + " policy cache is reset.");
			
		}
		
	}
	public void resetPageReadingCache(String pageUuid) {
		log.info("PageUuid " + pageUuid + " reading cache is reset.");
		pageReadingCache.remove(pageUuid);
		getPageReadPermission(pageUuid);
	}
	


	public List<Resource> getResourceOfUserHasOperation(User user, OPERATIONS oper) {
		List<Resource> resList = new ArrayList<Resource>();
		if(!user.isEnabled())
			return resList;
		
		List<Permission> allPerms = new ArrayList<Permission>();
			
		allPerms.addAll(user.getPermissions());
		
		Set<Role> roles = user.getRoles();
		for (Role role : roles) {
			//performance consideration: don't use role.getPermission() as admin or user has too many permissions in large space volume case.
			List<Permission> rolePerms = permissionDAO.getByRoleOperation(role.getName(), oper);
			allPerms.addAll(rolePerms);
		}
		
		for (Permission perm: allPerms) {
			if(perm.getOperation() != oper)
				continue;
			
			resList.add(perm.getResource());
		}
		
		return resList;
	}
	

	public boolean isAllowResourceAdmin(String resourceName, RESOURCE_TYPES resourceType, User user){
		
		boolean found = false;
		if(!user.isEnabled())
			return found;
			
		List<Permission> allPerms = new ArrayList<Permission>();
		allPerms.addAll(user.getPermissions());
		
		Set<Role> roles = user.getRoles();
		for (Role role : roles) {
			//performance consideration: don't use role.getPermission() as admin or user has too many permissions in large space volume case.
			List<Permission> rolePerms = permissionDAO.getByRoleResource(role.getName(), resourceName);
			allPerms.addAll(rolePerms);
		}
		
		for (Permission perm: allPerms) {
			if(perm.getOperation() != OPERATIONS.ADMIN)
				continue;
			if(perm.getResource().getType() != resourceType
				|| !StringUtils.equalsIgnoreCase(resourceName, perm.getResource().getResource()))
				continue;
			
			found = true;
		}
		
		return found;
	}
	
	
	public void proxyLogout() {
		try {
			SecurityContextHolder.getContext().setAuthentication(null);
			SecurityContextHolder.clearContext();
			ProxyLoginUtil.setRequester(null);
		} catch (Exception e) {
			log.error("Proxy logout failed",e);
		}
	}
	public void proxyLogin(String username) {
		Authentication auth;
		User user = userReadingService.getUserByName(username);
		if(user == null || user.isAnonymous()){
			auth = new AnonymousAuthenticationToken(ANONYMOUS_KEY, ANONYMOUS_PASSWORD, Arrays.asList((GrantedAuthority)roleDAO.getByName(SYSTEM_ROLES.ANONYMOUS.getName())));
		}else{
			//there is no plain password available, so just set Authenticated as true
			auth = new UsernamePasswordAuthenticationToken(user.getUsername(),"", user.getAuthorities());
		}
		
		ProxyLoginUtil.setRequester(user);
		SecurityContextHolder.getContext().setAuthentication(auth);
		
	}
	public String proxyLoginAsSystemAdmin(){
		//don't assume system admin is null...
		Set<String> admins = userReadingService.getSystemAdminMailList();
		User admin = userReadingService.getUserByEmail(admins.iterator().next());
		//login proxy as system admin
		this.proxyLogin(admin.getUsername());
		
		return admin.getUsername();
	}
	public String proxyLoginAsSpaceAdmin(String spaceUname){
		//don't assume system admin is null...
		Set<String> admins = userReadingService.getSpaceAdminMailList(spaceUname);
		if(admins.size() > 0){
			User admin = userReadingService.getUserByEmail(admins.iterator().next());
			if(admin != null){
				//login proxy as system admin
				this.proxyLogin(admin.getUsername());
				return admin.getUsername();
			}
		}
		return null;
	}
	@Transactional(readOnly= true, propagation = Propagation.REQUIRED)
	public void afterPropertiesSet() throws Exception {
		//NOTE: because spring limitation, spaceDAO does not work during spring startup. this method CANNOT be use.
		if(initialSpaceReadingCacheAtStart){
			//initial space reading permission at once. this may be a very time consuming method if spaces amount is huge.
			//But it is much better let Server startup slow but allow user get quick result during first search(NOTE, 
			//spaceReadingCache is using by search SecurityFilter)
			List<Space> spaces = spaceDAO.getObjects();
			for (Space space : spaces) {
				getSpaceReadPermission(space.getUnixName());
				
			}
		}
		
	}
	//********************************************************************
	//               Private methods
	//********************************************************************
	//strategy to check if readable: if any one of values in user has appear in permission list, it will pass
	private boolean isAllow(List<String> roleUserList, List<String> readPerm) {
		
		boolean allow = false;
		for(String name:roleUserList){
			if(readPerm.contains(name)){
				allow = true;
				break;
			}
		}
		return allow;
	}
	/*	
	 * Try to get space private flat from cache, if not exist, then initial it from database and cache it.
	 * 
	 */
	private Element getSpacePrivate(String spaceUname){
		Element perm = spaceReadingCache.get(PRIVATE_SPACE_PREFIX + spaceUname);
		if(perm != null){
			return perm;
		}
		
		Space space = spaceDAO.getByUname(spaceUname);
		if(space == null){
			log.error("Unable to find space " + spaceUname + " from database.");
			return null;
 		}
		log.info("Space private flag is initial in cache " + spaceUname + " status: " + space.isPrivate());
		perm = new Element(PRIVATE_SPACE_PREFIX + spaceUname,space.isPrivate());
		spaceReadingCache.put(perm);
		
		return perm;
	}
	/*	
	 * Try to get space read permission from cache, if not exist, then initial it from database and cache it.
	 * IMPORTANT: spaceReadPermissionCache is pure space reading permission list
	 * it won't impact by Instance level permission. This means, any users/roles in a space will always cache.  
	 * even Instance block them
	 * 
	 */
	private Element getSpaceReadPermission(String spaceUname) {
		
		//TODO: so far, it is not thread-safe, for performance reason. So, read DB may happen multiple times form same space
		//when Multiple Search happen.
		Element element = spaceReadingCache.get(spaceUname);
		if(element != null){
			return element;
		}
		
		log.info("Initial space reading permission for space " + spaceUname);
		List<String> readPerm = getSpacePerm(spaceUname, OPERATIONS.READ);
		if(readPerm != null){
			element = new Element(spaceUname,readPerm);
			spaceReadingCache.put(element);
		}
		return element;
	}
	/**
	 * Return list of users(username with Role.USER_PREFIX) and roles(role name) who have specified permission on given space.
	 * @param spaceUname
	 * @param operation
	 * @return
	 */
	private List<String> getSpacePerm(String spaceUname,OPERATIONS operation) {
		Resource spaceRes = resourceDAO.getByName(spaceUname);
		if(spaceRes == null){
			//in some case, Index contains some spaces, but they already removed from Database
			log.error("Space does not exist by spaceUname: " + spaceUname);
			return null;
		}
		Set<Permission> perms = spaceRes.getPermissions();
		List<String> readPerm = new ArrayList<String>();
		
		if(perms != null){
			for (Permission permission : perms) {
				if(permission.getOperation() != operation)
					continue;
				Set<Role> roles = permission.getRoles();
				Set<User> users = permission.getUsers();
				if(roles != null){
					for (Role role : roles) {
						log.info("Space " + spaceUname +  " allow role  "+ operation.name()+" " + role.getName());
						readPerm.add(role.getName());
					}
				}
				if(users != null){
					for (User u : users) {
						//put user name attribute as "USER_xxx"
						log.info("Space " + spaceUname + " allow user " + operation.name()+" " + u.getUsername());
						readPerm.add(Role.USER_PREFIX+u.getUsername());
					}
				}	
				break;
			}
			if(readPerm.size() == 0){
				//!!!
				if(!SharedConstants.SYSTEM_SPACEUNAME.equals(spaceUname))
					AuditLogger.error("Must BUG! space " + spaceUname + " can not find any "+operation.name()+" permission. It is impossible case. Please trace log!");
				return null;
			}else{
				return readPerm;
			}
		}else{
			log.error("Space can not find permission " + spaceUname);
			return null;
		}
	}
	private Element getPageReadPermission(String pageUuid) {
		Element perm = pageReadingCache.get(pageUuid);
		if(perm != null){
			return perm; 
		}

		log.debug("Initial page reading permission for page " + pageUuid);
		Resource pageRes = resourceDAO.getByName(pageUuid);
		if(pageRes == null){
			//Most page has no permission setting, so it is better put a dummy page permission element into cache
			//otherwise, pageReadingCache is useless - most page have to read database(resourceDAO.getByName()) again
			//to check this page has not permission.
			pageReadingCache.put(new Element(pageUuid,null));
			log.debug("Given page resource does not exist " + pageUuid +". Any permission will inherit from space.");
			return null;
		}
		
		Set<Permission> perms = pageRes.getPermissions();
		List<String> readPerm = new ArrayList<String>();
		
		if(perms != null){
			for (Permission permission : perms) {
				if(permission.getOperation() != OPERATIONS.READ)
					continue;
				Set<Role> roles = permission.getRoles();
				Set<User> users = permission.getUsers();
				if(roles != null){
					for (Role role : roles) {
						log.info("Page " + pageUuid + " forbidden role reading " + role.getName());
						readPerm.add(role.getName());
					}
				}
				if(users != null){
					for (User u : users) {
						//put user name attribute as "USER_xxx"
						log.info("Page " + pageUuid + " forbidden user reading " + u.getUsername());
						readPerm.add(Role.USER_PREFIX+u.getUsername());
					}
				}	
				break;
			}
			perm = new Element(pageUuid,readPerm);
			pageReadingCache.put(perm);
		}else{
			log.info("Page can not find permission " + pageUuid);
		}
		return perm;
	}
	
	/*
	 * Find method policy: 
	 * <li>find out RESOURCE_TYPES and OPERATION according to class+method name</li>
	 * <li>retrieve policies to find out policy which has same RESOURCE_TYPES, OPERATION and same ResourceName</li>
	 * 
	 * The return should be policy if there is match or null if no match.
	 */
	private Policy findMethodPolicy(String clz, String method, Object[] args ,int beforeAfter) {
		//try to get by resource (page -> space -> instance) level policy for this resource: spaceUname,PageUuid, Instance
		//find out wikiOperation according to pattern.
		WikiOPERATIONS wikiType = patternStrategy.findMethodRuntimePattern(clz,method,args,beforeAfter);
		//if class method has no mapping pattern, skip authentication: return null

		if(wikiType != null){
			//get all resources for special resource in given RESOURCE_TYPES: 
			List<Policy> policies = patternStrategy.getPolicies(wikiType.type,wikiType.values.get(RESOURCE_TYPES.SPACE),wikiType.values.get(RESOURCE_TYPES.PAGE));
			//now, need check policy in cache to find out if this type/operation policy exist. 
			//For example, it may not exist if page permission is not defined.
			for (Policy policy : policies) {
				//match: resource Type, name and operation
				if(StringUtils.equalsIgnoreCase(policy.getResourceName(),wikiType.values.get(wikiType.type))
					&& policy.getOperation() == wikiType.operation
					&& policy.getType() == wikiType.type){
					return policy;
				}
			}
		}
		
		return null;
	}

	

	/*
	 * What is dead role/user? For example, A user, Instance forbidden page write, but space allow page write. Obviously, this 
	 * user has no permission on page write because Instance overrides space permission. This user on page write permission will mark
	 * as dead.  
	 * 
	 * <BR>
	 * The reason to find out dead user/list on special resource is, it gives possibility on Client UI to highlight some permission 
	 * doesn't work becuase of override reason.(as example, you mark space page write is on, but instance is off).
	 */
	private void findDeadRoleUser(Collection<Permission> list, OPERATIONS operation, List<String> nameList, boolean deadlist) {
		for (Permission permission : list) {
			if(permission.getOperation().equals(operation)){
				//the input nameList is just deadlist, no need compare with upper level attributes list
				if(deadlist){
					permission.setDeadRoleUserList(nameList);
					continue;
				}
				//now iterate all
				List<String> dead = new ArrayList<String>();
				Set<Role> roles = permission.getRoles();
				if(roles != null){
					for (Iterator<Role> iter = roles.iterator();iter.hasNext();) {
						Role role = iter.next();
						//does this role is live?
						if(!nameList.contains(role.getName())){
							dead.add(role.getName());
						}
					}
				}
				Set<User> users = permission.getUsers();
				if(users != null){
					for (Iterator<User> iter = users.iterator();iter.hasNext();) {
						User user = iter.next();
						//does this user is live?
						if(!nameList.contains(Role.USER_PREFIX+user.getUsername())){
							dead.add(Role.USER_PREFIX+user.getUsername());
						}
					}
				}

				permission.setDeadRoleUserList(dead);
			}
		}
	}
	private List<String> getRoleUserNameList(User user){
		List<String> roleUserList = new ArrayList<String>();
		if(user == null || user.isAnonymous()){
			roleUserList.add(SYSTEM_ROLES.ANONYMOUS.getName());
		}else{
			roleUserList.add(Role.USER_PREFIX+user.getUsername());
			Set<Role> roles = user.getRoles();
			for (Role role : roles) {
				roleUserList.add(role.getName());
			}
		}
		
		return roleUserList;
	}
	private void operMatrix(int[] p1, List<String> roleUserList, Policy policy) {
		boolean contain = false;
		for(Iterator<ConfigAttribute> iter = policy.getMutableAttributeDefinition().iterator();iter.hasNext();){
			ConfigAttribute attr = iter.next();
			//if this (attribute)role is not included in live condition(policy), then remove this role
			if(roleUserList.contains(attr.getAttribute())){
				contain = true;
				p1[policy.getOperation().ordinal()] = 1;
				break;
			}
		}
		//this policy does not contain any role/user for current user's, so remove it
		if(!contain){
			p1[policy.getOperation().ordinal()] = 2;
		}
		
	}

	//********************************************************************
	//                       Set / Get
	//********************************************************************
	public void setResourceDAO(ResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}
	public void setPermissionDAO(PermissionDAO permissionDAO) {
		this.permissionDAO = permissionDAO;
	}

	public void setRoleDAO(RoleDAO roleDAO) {
		this.roleDAO = roleDAO;
	}

	public void setPatternStrategy(PatternStrategy wikiPrivilegeStrategy) {
		this.patternStrategy = wikiPrivilegeStrategy;
	}


	public void setPageDAO(PageDAO pageDAO) {
		this.pageDAO = pageDAO;
	}

	public void setAuthenticationManager(ProviderManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public void setSpaceReadingCache(Cache spaceReadPermissionCache) {
		this.spaceReadingCache = spaceReadPermissionCache;
	}
	public void setInitialSpaceReadingCacheAtStart(boolean initialSpaceReadingCache) {
		this.initialSpaceReadingCacheAtStart = initialSpaceReadingCache;
	}
	public void setSpaceDAO(SpaceDAO spaceDAO) {
		this.spaceDAO = spaceDAO;
	}
	public void setPageReadingCache(Cache pageReadingCache) {
		this.pageReadingCache = pageReadingCache;
	}
	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}

}
