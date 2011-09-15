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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.core.model.Permission;
import com.edgenius.core.model.Resource;
import com.edgenius.wiki.security.Policy;
import com.edgenius.wiki.service.CommentService;
import com.edgenius.wiki.service.DiffService;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.TagService;
import com.edgenius.wiki.service.impl.CommentServiceImpl;
import com.edgenius.wiki.service.impl.DiffServiceImpl;
import com.edgenius.wiki.service.impl.PageServiceImpl;
import com.edgenius.wiki.service.impl.TagServiceImpl;

/**
 * @author Dapeng.Ni
 */
public class PagePatternFactory extends AbstractPatternFactory {
	
	static final String[] P_READ_METHOD_PATTERNS = new String[]{
		PageServiceImpl.class.getName()+ "." +PageService.getCurrentPageByTitle,
		PageServiceImpl.class.getName() + "." +PageService.getHomepage,
		PageServiceImpl.class.getName() + "." +PageService.getPage,
		PageServiceImpl.class.getName() + "." +PageService.getHistory,
		PageServiceImpl.class.getName() + "." +PageService.getHistoryPages,
		DiffServiceImpl.class.getName() + "." +DiffService.diffToHtml,
		//??? getDraft 
	};

	
	static final String[] P_WRITE_METHOD_PATTERNS = new String[]{
		PageServiceImpl.class.getName() + "." +PageService.savePage,
		PageServiceImpl.class.getName() + "." +PageService.uploadAttachments,
		PageServiceImpl.class.getName() + "." +PageService.removeAttachment,
		PageServiceImpl.class.getName() + "." +PageService.updateAttachmentMetaData,
		TagServiceImpl.class.getName() + "." + TagService.saveUpdatePageTag,
	};
	static final String[] P_REMOVE_METHOD_PATTERNS = new String[]{
		PageServiceImpl.class.getName() + "." +PageService.removePage,
	};
	static final String[] P_COMMENT_READ_METHOD_PATTERNS = new String[]{
		CommentServiceImpl.class.getName() + "." + CommentService.getPageComments,
	};
	static final String[] P_COMMENT_WRITE_METHOD_PATTERNS = new String[]{
		CommentServiceImpl.class.getName() + "." + CommentService.createComment,
	};
	
//	static final String[] P_OFFLINE_METHOD_PATTERNS = new String[]{
//		//TODO:
//	};
	
	//********************************************************************
	//               methods
	//********************************************************************

	public List<Policy> getPolicies(Resource resource){
		
		List<Policy> policies = new ArrayList<Policy>();
		//if it is not page resources, return empty list
		if(resource.getType() != RESOURCE_TYPES.PAGE)
			return policies;
		
		String pageUuid = resource.getResource();
		
		//retrieve all permission of this page, get back URL/METHOD policies
		Set<Permission> perms = resource.getPermissions();
		for (Permission permission : perms) {
			Policy policy =  getPolicy(permission,pageUuid);
			if(policy != null)
				policies.add(policy);
		}
		
		return policies;
	}
	
	//********************************************************************
	//               Set / Get
	//********************************************************************

}
