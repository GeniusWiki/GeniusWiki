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
package com.edgenius.wiki.security.strategy;

import static com.edgenius.wiki.service.BackupService.backup;
import static com.edgenius.wiki.service.BackupService.restore;
import static com.edgenius.wiki.service.SecurityDummy.checkInstanceAdmin;
import static com.edgenius.wiki.service.SecurityDummy.checkInstanceRead;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.core.SecurityValues.SYSTEM_ROLES;
import com.edgenius.core.model.Permission;
import com.edgenius.core.model.Resource;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.security.Policy;
import com.edgenius.wiki.service.BackupService;
import com.edgenius.wiki.service.SecurityDummy;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.impl.SpaceServiceImpl;
/**
 * NOTE: All url definition must be convert to lowercase!!! 
 * @author Dapeng.Ni
 */
public class InstancePatternFactory extends AbstractPatternFactory {
	static final String[] FIX_PERMISSION_URL_POLICIES = new String[]{
		"/**/invite!accept.do*"
	};
	//THIS role map to same index with FIX_PERMISSION_URL_POLICIES
	static final SYSTEM_ROLES[] FIX_PERMISSION_URL_POLICIES_ROLES = new SYSTEM_ROLES[]{
		SYSTEM_ROLES.USERS  //invite!accept.do URL need register user role.
	};
	
	//NOTE: some url, such *.gif, *.css etc will skip directly before any policy check
	//please refer to com.edgenius.wiki.security.acegi.DBFilterInvocationDefinitionSource.lookupAttributes(String)
	//system default policy, allow any user/role to access
	static final String[] DEFAULT_URL_POLICIES = new String[]{
		"/status*",
		"/signin*",
		"/**/invite.do*",
		"/**/login.do*",
		//current allow all user signup, in future, it should be a configurable choice
		"/**/signup.do*",
		"/**/captcha.do*",
		//404,403,index jsps
		"/**/*.jsp*"
		
		///**/*.rpcs is already skipped in DBFilterInvocationDefinitionSource!
	};
	
	static final String[] I_READ_URL_POLICIES = new String[]{
		"/"+SharedConstants.URL_PAGE+"*",
		"/"+SharedConstants.URL_TINY_PAGE+"*",
		"/"+SharedConstants.URL_VIEW+"*",
		"/"+SharedConstants.URL_TINY_VIEW+"*",
		"/"+SharedConstants.URL_TINY_SPACE+"*",
		"/"+SharedConstants.URL_TINY_READONLY_SPACE+"*",
		"/**/*.do*",
	};
	static final String[] I_ADMIN_URL_POLICIES = new String[]{
		"/"+SharedConstants.URL_INSTANCE_ADMIN+"*",
		"/**/instance/*.do*"
	};

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//               Method
	//access instance(spaces)
	static final String[] I_READ_METHOD_POLICIES = new String[]{
		SecurityDummy.class.getName() +"." + checkInstanceRead,
	};
	//create new space
	static final String[] I_WRITE_METHOD_POLICIES = new String[]{
		SpaceServiceImpl.class.getName()+"." + SpaceService.createSpace,
	};
	
	//instance admin permission
	static final String[] I_ADMIN_METHOD_POLICIES = new String[]{
		SecurityDummy.class.getName() +"." + checkInstanceAdmin,
		BackupService.class.getName() +"." + restore,
		BackupService.class.getName() +"." + backup,
	};
	
//	static final String[] I_OFFLINE_METHOD_POLICIES = new String[]{
//		//TODO
//		
//	};
	
	//********************************************************************
	//               methods
	//********************************************************************
	public List<Policy> getPolicies(Resource resource) {
		
		List<Policy> policies = new ArrayList<Policy>();
		
		//if it is not instance resources, return default list
		if(resource.getType() != RESOURCE_TYPES.INSTANCE)
			return policies;
		
		//put FIX_ROLE permission into policy cache
		for(SYSTEM_ROLES role :FIX_PERMISSION_URL_POLICIES_ROLES){
			Policy policy = new Policy();
			policy.setOperation(OPERATIONS.RESTRICT);
			policy.setType(RESOURCE_TYPES.INSTANCE);
			policy.setResourceName(WikiConstants.CONST_INSTANCE_RESOURCE_NAME);
			policy.addAttribute(role.getName());
			policies.add(policy);
		}
		
		//retrieve all permission of this page, get back policies
		Set<Permission> perms = resource.getPermissions();
		for (Permission permission : perms) {
			Policy policy = getPolicy(permission,WikiConstants.CONST_INSTANCE_RESOURCE_NAME);
			if(policy != null)
				policies.add(policy);
		}

		return policies;
	}

}
