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

import static com.edgenius.wiki.service.SecurityDummy.checkPageRead;
import static com.edgenius.wiki.service.SecurityDummy.checkPageRestrict;
import static com.edgenius.wiki.service.SecurityDummy.checkSpaceAdmin;
import static com.edgenius.wiki.service.SecurityDummy.checkSpaceOffline;
import static com.edgenius.wiki.service.SecurityDummy.checkSpaceRead;
import static com.edgenius.wiki.service.SecurityDummy.checkSpaceWrite;
import static com.edgenius.wiki.service.impl.ExportMethodValueProvider.exportSpace;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.core.model.Permission;
import com.edgenius.core.model.Resource;
import com.edgenius.wiki.security.Policy;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.SecurityDummy;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.impl.PageServiceImpl;
import com.edgenius.wiki.service.impl.RenderServiceImpl;
import com.edgenius.wiki.service.impl.SpaceServiceImpl;

/**
 * 
 * @author Dapeng.Ni
 */
public class SpacePatternFactory extends AbstractPatternFactory{
	//view space pages

	static final String[] S_READ_URL_PATTERNS = new String[]{
		"/feed/*",
		"/**/feed.do*",
	};
	
	//!!! This is one to one mapping with S_READ_URL_PATTERNS to declare which parameter name figure out the SpaceUname
	static final Object[] S_READ_URL_PARAMS = new Object[]{
		Pattern.compile("^/feed/([^/]+)(?:(?:/$)|$)"),   	// regex pattern of spaceUname in URL 
		"s", 		//spaceUname of parameter for feed.do
	};
	static final String[] S_READ_PAGE_METHOD_PATTERNS = new String[]{
		PageServiceImpl.class.getName() + "." + PageService.getPageTree,
		SecurityDummy.class.getName() +"." + checkSpaceRead,
		SecurityDummy.class.getName() +"." + checkPageRead,
	};
	//create space pages
	static final String[] S_WRITE_PAGE_METHOD_PATTERNS = new String[]{
		PageServiceImpl.class.getName() + "." + PageService.move,
		PageServiceImpl.class.getName() + "." + PageService.copy,
		PageServiceImpl.class.getName() + "." + PageService.restorePage,
		PageServiceImpl.class.getName() + "." + PageService.restoreHistory,
		SecurityDummy.class.getName() +"." + checkSpaceWrite,
	};
	
	//remove pages
	static final String[] S_REMOVE_PAGE_METHOD_PATTERNS = new String[]{
	};
	
	static final String[] S_COMMENT_READ_METHOD_PATTERNS = new String[]{
	};
	static final String[] S_COMMENT_WRITE_METHOD_PATTERNS = new String[]{
	};
	
	//restrict pages:page security panel
	static final String[] S_RESTRICT_PAGE_METHOD_PATTERNS = new String[]{		
		SecurityDummy.class.getName() +"." + checkPageRestrict,
	};
	
//	TODO: no exist service now	
	//export space pages
	static final String[] S_EXPORT_METHOD_PATTERNS = new String[]{
		RenderServiceImpl.class.getName() + "." + exportSpace,
		
	};

	//space admin: update, security, remove, remove page permanent
	static final String[] S_SPACE_ADMIN_METHDO_PATTERNS = new String[]{
		SpaceServiceImpl.class.getName() + "." + SpaceService.updateSpace,
		SpaceServiceImpl.class.getName() + "." + SpaceService.uploadLogo,
		SpaceServiceImpl.class.getName() + "." + SpaceService.removeSpaceInDelay,
		SpaceServiceImpl.class.getName() + "." + SpaceService.undoRemoveSpace,
		SecurityDummy.class.getName() +"." + checkSpaceAdmin,
	};
	
	static final String[] S_OFFLINE_METHOD_PATTERNS = new String[]{
		SecurityDummy.class.getName() +"." + checkSpaceOffline,
		
	};
	
	//********************************************************************
	//               methods
	//********************************************************************
	public List<Policy> getPolicies(Resource resource){
		List<Policy> policies = new ArrayList<Policy>();
		//if it is not space resources, return empty list
		if(resource.getType() != RESOURCE_TYPES.SPACE)
			return policies;
		
		Set<Permission> perms = resource.getPermissions();
		for (Permission permission : perms) {
			Policy policy =  getPolicy(permission,resource.getResource());
			if(policy != null)
				policies.add(policy);
		}
		
		return policies;
	}


	//********************************************************************
	//               Set / Get
	//********************************************************************
}
