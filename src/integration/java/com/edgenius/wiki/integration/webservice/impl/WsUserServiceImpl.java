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
package com.edgenius.wiki.integration.webservice.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jws.WebService;
import javax.xml.ws.WebServiceException;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.Global;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.service.RoleService;
import com.edgenius.core.service.UserExistsException;
import com.edgenius.core.service.UserOverLimitedException;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.service.UserService;
import com.edgenius.core.util.CodecUtil;
import com.edgenius.wiki.integration.WsContants;
import com.edgenius.wiki.integration.dto.RoleList;
import com.edgenius.wiki.integration.dto.WsUser;
import com.edgenius.wiki.integration.webservice.WsUserService;
import com.edgenius.wiki.service.ActivityLogService;

/**
 * @author Dapeng.Ni
 */
@WebService(serviceName="UserWebService", endpointInterface = "com.edgenius.wiki.integration.webservice.WsUserService", targetNamespace=WsContants.NS) 
public class WsUserServiceImpl implements WsUserService {

	private UserService userService;
	private UserReadingService userReadingService;
	private RoleService roleService;
	private ActivityLogService activityLog;
	
	public Integer createUser(WsUser wsUser) {
		User user = new User();
		Set<Role> roleObjects = new HashSet<Role>();
		user.setRoles(roleObjects);
		
		//put role objects into role list
		RoleList roles = wsUser.getRoles();
		if(roles != null){
			for (String name : roles.getRolename()) {
				Role role = roleService.getRoleByName(name);
				if(role == null)
					throw new WebServiceException("Role [" + name + "] does not exist.");
				
				roleObjects.add(role);
			}
		}
		//default role - so far only RegisterUser role
		roleObjects.addAll(roleService.getDefaultRole());
		
		wsUser.copyTo(user);
		if(Global.EncryptPassword){
            user.setPassword(CodecUtil.encodePassword(wsUser.getPassword(), Global.PasswordEncodingAlgorithm));
        }
		user.setCreatedDate(new Date());
		
		try {
			user = userService.saveUser(user);
			activityLog.logUserSignup(user);
			
			return user.getUid(); 
		} catch (UserExistsException e) {
			throw new WebServiceException(e);
		} catch (UserOverLimitedException e) {
			throw new WebServiceException(e);
		}
	}
	public Integer updateUser(WsUser wsUser) {
		if(StringUtils.isBlank(wsUser.getUsername())){
			throw new WebServiceException("Username can't be blank when update user.");
		}
		User user = userReadingService.getUserByName(wsUser.getUsername().trim());
		if(user == null){
			throw new WebServiceException("User doesn't exist by username ["+wsUser.getUsername()+"]");
		}
		
		Set<Role> roles = user.getRoles();
		RoleList wsRoles = wsUser.getRoles();
		roles.clear();
		if(wsRoles != null){
			for (String name : wsRoles.getRolename()) {
				Role role = roleService.getRoleByName(name);
				if(role == null)
					throw new WebServiceException("Role [" + name + "] does not exist.");
				
				roles.add(role);
			}
		}else{
			//default role - so far only RegisterUser role
			roles.addAll(roleService.getDefaultRole());
		}
		
		wsUser.copyTo(user);
		if(Global.EncryptPassword){
            user.setPassword(CodecUtil.encodePassword(wsUser.getPassword(), Global.PasswordEncodingAlgorithm));
        }
		
		user = userService.updateUserWithIndex(user);
		
		return user.getUid();
	}


	//********************************************************************
	//               set / get methods
	//********************************************************************
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}
	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}
	public void setActivityLog(ActivityLogService activityLog) {
		this.activityLog = activityLog;
	}



}
