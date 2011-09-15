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
package com.edgenius.wiki.webapp.admin.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.edgenius.core.SecurityValues.SYSTEM_ROLES;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.service.RoleService;
import com.edgenius.core.util.CompareToComparator;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.webapp.action.BaseAction;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class RoleAdminAction  extends BaseAction{
	private static final int PAGE_SIZE = 15;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// parameters
	//page number
	private int page;
	// role UID
	private int uid;
	private int userUid;
	
	private int sortBy;
	private boolean sortByDesc;
	private String filter;
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// service
	private RoleService roleService;
	private MessageService messageService;

	//********************************************************************
	//               function methods
	//********************************************************************
	public String  list(){
		filter();
		return SUCCESS;
	}
	public String  filter(){
		//I just assume there won't lot of Roles - so get them all then sort...
		
		//list all system default and group roles
		List<RoleDTO> roleDTOs = new ArrayList<RoleDTO>();
		
		//get all groups except SPACE_GROUP
		List<Role> roles = roleService.getRoles(Role.TYPE_SYSTEM, filter);
		//hide anonymous and public group - it is meaningless
		roles.remove(new Role(SYSTEM_ROLES.ANONYMOUS.getName()));
		roles.remove(new Role(SYSTEM_ROLES.USERS.getName()));
		//add manually created group
		roles.addAll(roleService.getRoles(Role.TYPE_GROUP,filter));
		
		//page 0(null) or 1 is same
		page = page==0?1:page;
		
		int size = roles.size();
		int from = (page-1)*PAGE_SIZE;
		int end = Math.min(size, from + PAGE_SIZE);
		
		//this does not includes space type roles count
		//!!! Please note this map does not include 0 user groups!!! So its size may smaller roles.size()
		Map<Integer, Long> totalUserSummary = roleService.getRolesUsersCount();

		if(sortBy == Role.SORT_BY_USERS_COUNT){
			//like space page count, users counts uses reverse meaning with field "sortByDesc" so that the first click in sort link
			//will display maximum users group first
			int cs = CompareToComparator.TYPE_KEEP_SAME_VALUE|CompareToComparator.DESCEND;
			if(sortByDesc){
				cs = CompareToComparator.TYPE_KEEP_SAME_VALUE|CompareToComparator.ASCEND;
			}
			Map<Long, Integer> sortedRoleCount = new TreeMap<Long, Integer>(new CompareToComparator<Long>(cs));
			//looping all roles,if cannot find user count from totalUserSummary, the set it as 0.
			for (Role r : roles) {
				Long total = totalUserSummary.get(r.getUid());
				sortedRoleCount.put(total == null?Long.valueOf(0):total, r.getUid());
			}
			List<Integer> sortedUids = new ArrayList<Integer>(sortedRoleCount.values());
		
			if(from < size){
				//get sub-list 
				sortedUids = sortedUids.subList(from, end);
				for (Integer roleUid : sortedUids) {
					RoleDTO dto = new RoleDTO();
					Role role = null;
					for (Role r : roles) {
						if(roleUid.equals(r.getUid())){
							role = r;
							break;
						}
					}
					if(role != null){
						dto.setRole(role);
						Long total = totalUserSummary.get(roleUid);
						dto.setUsersCount(total == null?Long.valueOf(0):total);
						roleDTOs.add(dto);
					}
				}
			}
		}else{
			
			int cs = CompareToComparator.TYPE_KEEP_SAME_VALUE|CompareToComparator.ASCEND;
			if(sortByDesc){
				cs = CompareToComparator.TYPE_KEEP_SAME_VALUE|CompareToComparator.DESCEND;
			}
			if(sortBy == Role.SORT_BY_DISPLAYNAME){
				TreeMap<String, Role> sorted = new TreeMap<String, Role>(new CompareToComparator<String>(cs));
				for (Role role : roles) {
					sorted.put(role.getDisplayName(), role);
				}
				roles = new ArrayList<Role>(sorted.values());
			}else if(sortBy == Role.SORT_BY_DESC){
				TreeMap<String, Role> sorted = new TreeMap<String, Role>(new CompareToComparator<String>(cs));
				for (Role role : roles) {
					sorted.put(role.getDescription(), role);
				}
				roles = new ArrayList<Role>(sorted.values());
//			}else{
				//default, sort by type
			}
			roles = roles.subList(from, end);
			for (Role role : roles) {
				RoleDTO dto = new RoleDTO();
				dto.setRole(role);
				Long total = totalUserSummary.get(role.getUid());
				dto.setUsersCount(total == null?0:total);
				roleDTOs.add(dto);
			}
		}
		
		getRequest().setAttribute("roles", roleDTOs);
		getRequest().setAttribute("total", size);
		
		return "list";
	}
	public String detail(){
		//list users belong to this role
		Role role = roleService.getRole(uid);
		
		//this check is not quit necessary actually as JSP side already screen these two users...
		if(!SharedConstants.ROLE_ANONYMOUS.equals(role.getName())
			&& !SharedConstants.ROLE_REGISTERED.equals(role.getName())){
			getRequest().setAttribute("totalUsers", role.getUsers().size());
			getRequest().setAttribute("role", role);
			
		}
		return "detail";
	}
	
	/**
	 * A new role(group) is created.
	 */
	public String created(){
		getRequest().setAttribute("message", messageService.getMessage("group.create.success"));
		return list();
	}
	
	
	/**
	 * Remove user from role, if user is not in that role or any error, return null. Otherwise, return same roleUid with input.
	 * @param uid
	 * @param userUid
	 * @return
	 */

	public String deleteUsersFromRole(){
		Role role = roleService.getRole(uid);
		if(role == null){
			return "detail";
		}
		
		User user = userReadingService.getUser(userUid);
		if(user == null){
			return detail();
		}
		
		Set<User> users = role.getUsers();
		if(users.size() == 1 && SYSTEM_ROLES.ADMIN.getName().equals(role.getName())){
			//system admin must have at leave one user, otherwise system lost control~
			getRequest().setAttribute("message", messageService.getMessage("admin.role.must.one.user"));
			return detail();
		}
		for(Iterator<User> iter = users.iterator();iter.hasNext();){
			if(user.equals(iter.next())){
				iter.remove();
				user.getRoles().remove(role);
				
				//It needs reset user from user cache, otherwise, user roles won't be updated
				userReadingService.removeUserFromCache(user);
				
				roleService.saveRole(role);
				return detail();
			}
		}
		
		//this user is not exist in this role
		return detail();
		
	}
	//********************************************************************
	//               set / get
	//********************************************************************
	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	/**
	 * @return the sortBy
	 */
	public int getSortBy() {
		return sortBy;
	}
	/**
	 * @param sortBy the sortBy to set
	 */
	public void setSortBy(int sortBy) {
		this.sortBy = sortBy;
	}
	/**
	 * @return the sortByDesc
	 */
	public boolean isSortByDesc() {
		return sortByDesc;
	}
	/**
	 * @param sortByDesc the sortByDesc to set
	 */
	public void setSortByDesc(boolean sortByDesc) {
		this.sortByDesc = sortByDesc;
	}
	/**
	 * @return the userUid
	 */
	public int getUserUid() {
		return userUid;
	}
	/**
	 * @param userUid the userUid to set
	 */
	public void setUserUid(int userUid) {
		this.userUid = userUid;
	}
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}


}
