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
package com.edgenius.wiki.security;

import java.util.Map;

import com.edgenius.core.SecurityValues;
import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.SecurityValues.RESOURCE_TYPES;

/**
 * @author Dapeng.Ni
 */
public class WikiSecurityValues {
	// !!! OK, I give up to use space default permission(static varaibles) to replace database persisted value design(09/11/2010)
	// The beginning of story is,  user cache saves roles, then save role's permissions. This causes memory problem if
	// too many spaces scenarios.  As per space, it has 8(not exactly) permissions in database. For user has either admin or user 
	// role, there are too many linked permissions to cache! Except memory problem, Role.getPermissions() also has performance issue.
	// So, I thought put space default permissions into static fields, and permission table only save reversed value. 
	// Although this design should have very good performance, especially, when there are large volume spaces.  
	// However, this design makes code complicated. The most importatn -- I don't have enough time to refactor and test.
	
//	//READ, COMMENT_READ,COMMENT_WRITE, OFFLINE
//	public static Permission[] DEFAULT_SPACE_PERMISSIONS = new Permission[]{
//		new Permission(OPERATIONS.READ, new HashSet<Role>(Arrays.asList(
//				new Role(SYSTEM_ROLES.ANONYMOUS.ordinal(),SYSTEM_ROLES.ANONYMOUS.getName())
//				,new Role(SYSTEM_ROLES.USERS.ordinal(),SYSTEM_ROLES.USERS.getName())
//				,new Role(SYSTEM_ROLES.ADMIN.ordinal(),SYSTEM_ROLES.ADMIN.getName()))))
//		, new Permission(OPERATIONS.COMMENT_READ, new HashSet<Role>(Arrays.asList(
//				new Role(SYSTEM_ROLES.ANONYMOUS.ordinal(),SYSTEM_ROLES.ANONYMOUS.getName())
//				,new Role(SYSTEM_ROLES.USERS.ordinal(),SYSTEM_ROLES.USERS.getName())
//				,new Role(SYSTEM_ROLES.ADMIN.ordinal(),SYSTEM_ROLES.ADMIN.getName()))))
//		, new Permission(OPERATIONS.COMMENT_WRITE, new HashSet<Role>(Arrays.asList(
//				new Role(SYSTEM_ROLES.ANONYMOUS.ordinal(),SYSTEM_ROLES.ANONYMOUS.getName())
//				,new Role(SYSTEM_ROLES.USERS.ordinal(),SYSTEM_ROLES.USERS.getName())
//				,new Role(SYSTEM_ROLES.ADMIN.ordinal(),SYSTEM_ROLES.ADMIN.getName()))))
//		, new Permission(OPERATIONS.OFFLINE, new HashSet<Role>(Arrays.asList(
//				new Role(SYSTEM_ROLES.ANONYMOUS.ordinal(),SYSTEM_ROLES.ANONYMOUS.getName())
//				,new Role(SYSTEM_ROLES.USERS.ordinal(),SYSTEM_ROLES.USERS.getName())
//				,new Role(SYSTEM_ROLES.ADMIN.ordinal(),SYSTEM_ROLES.ADMIN.getName()))))
//		, new Permission(OPERATIONS.WRITE, new HashSet<Role>(Arrays.asList(
//				new Role(SYSTEM_ROLES.ADMIN.ordinal(),SYSTEM_ROLES.ADMIN.getName()))))
//		, new Permission(OPERATIONS.REMOVE, new HashSet<Role>(Arrays.asList(
//				new Role(SYSTEM_ROLES.ADMIN.ordinal(),SYSTEM_ROLES.ADMIN.getName()))))
//		, new Permission(OPERATIONS.RESTRICT, new HashSet<Role>(Arrays.asList(
//				new Role(SYSTEM_ROLES.ADMIN.ordinal(),SYSTEM_ROLES.ADMIN.getName()))))
//		, new Permission(OPERATIONS.ADMIN, new HashSet<Role>(Arrays.asList(
//				new Role(SYSTEM_ROLES.ADMIN.ordinal(),SYSTEM_ROLES.ADMIN.getName()))))
//		, new Permission(OPERATIONS.EXPORT,new HashSet<Role>(Arrays.asList(
//				new Role(SYSTEM_ROLES.ADMIN.ordinal(),SYSTEM_ROLES.ADMIN.getName()))))
//		};
//	
//	
//	/**
//	 * Input must be PO whatever spaceResource and system roles(3 defaults).
//	 * 
//	 * @param spaceResource
//	 * @param systemRoles
//	 * @return Calculated space level permissions for given space resource base on database persisted value and system default value.  
//	 * The permission is not PO list as it may  includes non-PO permission.  However all permission has PO resource, operation, roles and users.
//	 * i.e.,  permission.getUid() may be return null, but all others get methods will return correct PO objects. 
//	 * 
//	 */
//	public static  Set<Permission> getSpaceResourcePermissions(Resource spaceResource, List<Role> systemRoles) {
//		//only for space level resource!
//		Assert.isTrue(spaceResource.getType()==RESOURCE_TYPES.SPACE);
//		
//		List<Permission> defaultPerms = Arrays.asList(DEFAULT_SPACE_PERMISSIONS);
//		
//		//clone resource.getPermissions() as it is PO list, don't update directly
//		Set<Permission> perms = new HashSet<Permission>();
//		Set<Permission> persistPerms = new HashSet<Permission>();
//		for (Permission perm : spaceResource.getPermissions()) {
//			persistPerms.add((Permission) perm.shadowClone());
//		}
//		
//		
//		if(persistPerms.size()>0){
//			//reverse from default permissions
//			for (Permission defaultPerm :defaultPerms) {
//				for (Permission persistPerm :persistPerms) {
//					//only need compare operation: They are must space resource, and defaultPerm resource is null,i.e., all resources(spaces)
//					if(defaultPerm.getOperation() == persistPerm.getOperation()){
//						Set<Role> pRoles = persistPerm.getRoles();
//						//there is no reversed role permission...
//						if(pRoles.size()  == 0)
//							break;
//						
//						Set<Role> dRoles = defaultPerm.getRoles();
//						//merge default and persisted roles
//						for(Iterator<Role> dIter = dRoles.iterator();dIter.hasNext(); ){
//							Role dRole = dIter.next();
//							//if persisted contain default role, then means this is reversed tick off - role won't have this permission.
//							if(pRoles.remove(dRole)){
//								dIter.remove();
//							}
//						}
//						//This permission also includes other non-system roles or users, so keep it
//						if(persistPerm.getRoles().size() > 0 || persistPerm.getUsers().size() > 0){
//							perms.add(persistPerm);
//						}
//						break;
//					}
//				}
//				//some roles of default permissions don't be tick off, put this default permission into return list.  
//				if(defaultPerm.getRoles().size() > 0){
//					defaultPerm.setResource(spaceResource);
//					replaceRoleByPO(defaultPerm, systemRoles);
//					perms.add(defaultPerm);
//				}
//			}
//		}else{
//			//for this space, there is not change from default permissions, so adopt all of default permissions.
//			for (Permission defaultPerm : defaultPerms) {
//				defaultPerm.setResource(spaceResource);
//				replaceRoleByPO(defaultPerm, systemRoles);
//				perms.add(defaultPerm);
//			}
//		}
//		
//		return perms;
//	}
//	
//
//	/**
//	 * Replace transient object by persisted object in role list of given permission. 
//	 * @param defaultPerm
//	 */
//	private static void replaceRoleByPO(Permission perm,List<Role> systemRoles) {
//		Set<Role> roles = perm.getRoles();
//		if(roles == null || roles.size() == 0)
//			return;
//		
//		List<Role> pRoles = new ArrayList<Role>();
//		for (Iterator<Role> iter = roles.iterator();iter.hasNext();){
//			Role role = iter.next();
//			int idx = systemRoles.indexOf(role);
//			if(idx != -1){
//				pRoles.add(systemRoles.get(idx));
//				iter.remove();
//			}
//		}
//		
//		roles.addAll(pRoles);
//	}


	//********************************************************************
	//               Security constants value for WIKI
	//********************************************************************
	//the dependencies relationship among INSTANCE, SPACE and PAGE
	public static enum WikiOPERATIONS {
		//FIX_ROLE_PERMISSION
		INSTANCE_RESTRICT(RESOURCE_TYPES.INSTANCE,OPERATIONS.RESTRICT,null),
		//access any spaces
		INSTANCE_READ(RESOURCE_TYPES.INSTANCE,OPERATIONS.READ,null),
		//instance level admin
		INSTANCE_ADMIN(RESOURCE_TYPES.INSTANCE,OPERATIONS.ADMIN,new WikiOPERATIONS[]{INSTANCE_READ}),
		//create spaces
		INSTANCE_WRITE(RESOURCE_TYPES.INSTANCE,OPERATIONS.WRITE,new WikiOPERATIONS[]{INSTANCE_READ}),
		//offline spaces
		INSTANCE_OFFLINE(RESOURCE_TYPES.INSTANCE,OPERATIONS.OFFLINE,new WikiOPERATIONS[]{INSTANCE_READ}),
		
		//view pages of this space
		SPACE_PAGE_READ(RESOURCE_TYPES.SPACE,OPERATIONS.READ,new WikiOPERATIONS[]{INSTANCE_READ}),
		//create/modify pages of this space
		SPACE_PAGE_WRITE(RESOURCE_TYPES.SPACE,OPERATIONS.WRITE,new WikiOPERATIONS[]{SPACE_PAGE_READ}),
		//remove pages of this space
		SPACE_PAGE_REMOVE(RESOURCE_TYPES.SPACE,OPERATIONS.REMOVE,new WikiOPERATIONS[]{SPACE_PAGE_READ}),
		//comments
		SPACE_COMMENT_READ(RESOURCE_TYPES.SPACE,OPERATIONS.COMMENT_READ,new WikiOPERATIONS[]{SPACE_PAGE_READ}),
		SPACE_COMMENT_WRITE(RESOURCE_TYPES.SPACE,OPERATIONS.COMMENT_WRITE,new WikiOPERATIONS[]{SPACE_PAGE_READ}),
		//restrict page admin
		SPACE_PAGE_RESTRICT(RESOURCE_TYPES.SPACE,OPERATIONS.RESTRICT,new WikiOPERATIONS[]{SPACE_PAGE_WRITE}),
		//export pages of this space
		SPACE_EXPORT(RESOURCE_TYPES.SPACE,OPERATIONS.EXPORT,new WikiOPERATIONS[]{SPACE_PAGE_READ}),
		SPACE_ADMIN(RESOURCE_TYPES.SPACE,OPERATIONS.ADMIN,new WikiOPERATIONS[]{SPACE_PAGE_READ}),
		SPACE_OFFLINE(RESOURCE_TYPES.SPACE,OPERATIONS.OFFLINE,new WikiOPERATIONS[]{INSTANCE_OFFLINE,SPACE_PAGE_READ}),
		
		//following WikiOPERATIONS is useless because page only include forbidden type permission, it will handle separately
		//view this pages 
		PAGE_READ(RESOURCE_TYPES.PAGE,OPERATIONS.READ,new WikiOPERATIONS[]{SPACE_PAGE_READ}),
		//modify this pages  
		PAGE_WRITE(RESOURCE_TYPES.PAGE,OPERATIONS.WRITE,new WikiOPERATIONS[]{SPACE_PAGE_WRITE,PAGE_READ}),
		//remove this pages 
		PAGE_REMOVE(RESOURCE_TYPES.PAGE,OPERATIONS.REMOVE,new WikiOPERATIONS[]{SPACE_PAGE_REMOVE,PAGE_READ}),
		PAGE_COMMENT_READ(RESOURCE_TYPES.PAGE,OPERATIONS.COMMENT_READ,new WikiOPERATIONS[]{SPACE_COMMENT_READ,PAGE_READ}),
		PAGE_COMMENT_WRITE(RESOURCE_TYPES.PAGE,OPERATIONS.COMMENT_WRITE,new WikiOPERATIONS[]{SPACE_COMMENT_WRITE,PAGE_READ}),
		PAGE_OFFLINE(RESOURCE_TYPES.SPACE,OPERATIONS.OFFLINE,new WikiOPERATIONS[]{SPACE_OFFLINE,PAGE_READ});
		
		private WikiOPERATIONS(SecurityValues.RESOURCE_TYPES type, SecurityValues.OPERATIONS operation, WikiOPERATIONS[] liveConditions){
			this.type = type;
			this.operation = operation;
			this.liveConditions = liveConditions;
			
		}
		public SecurityValues.RESOURCE_TYPES type;
		public SecurityValues.OPERATIONS operation;
		public WikiOPERATIONS[] liveConditions;
		public Map<RESOURCE_TYPES,String> values;
		/**
		 * Return true if it is for same resource and same operation
		 * @param policy
		 * @return
		 */
		public boolean isSame(RESOURCE_TYPES type , OPERATIONS operation){
			if(operation == this.operation 
					&& type == this.type)
				return true;
			else
				return false;
		}
		
		public static WikiOPERATIONS get(RESOURCE_TYPES type , OPERATIONS operation){
			WikiOPERATIONS[] perms = WikiOPERATIONS.values();
			for (WikiOPERATIONS perm : perms) {
				if(perm.isSame(type,operation)){
					return perm;
				}
			}
	
			return null;
		}
	}


}
