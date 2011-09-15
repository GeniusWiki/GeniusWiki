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
package com.edgenius.wiki.service;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.model.User;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageTag;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.SpaceTag;
import com.edgenius.wiki.security.ValidateMethod;

/**
 * @author Dapeng.Ni
 */
@Transactional
public interface TagService {
	String SERVICE_NAME= "tagService";
	public static final String saveUpdateSpaceTag = "saveUpdateSpaceTag";
	
	public static final String saveUpdatePageTag = "saveUpdatePageTag";
	public static final String internalSavePageTag = "internalSavePageTag";

	//this method don't do any @ValidateMethod for security check
	//it should has 2 level, while create space, it is instance write permission,
	//while update space, it is space write permission. 
	List<SpaceTag> saveUpdateSpaceTag(Space space,String tagString);
	
	@ValidateMethod
	List<PageTag> saveUpdatePageTag(Page page, String tagString);
	
	
	/**
	 *  Get page tag name list from cache if it exists in cache, otherwise, get from database.
	 *  Please note, Space's tag also save into Tag Cache, but it is name is WikiConstants.CONST_INSTANCE_RESOURCE_NAME.
	 *  So, actually, this method also could return spaces scope tags.
	 *  
	 * @param spaceUname
	 * @return key is tag name, value is how many pages tagged by this tag 
	 */
	Map<String,Integer> getPageTagsNameList(String spaceUname);
	/**
	 * Get all page tags in given space.
	 * @param spaceUname
	 * @return
	 */
	List<PageTag> getPageTags(String spaceUname);
	
	List<Page> getPagesByTag(User viewer, String spaceUname, String tagname, int count);
	PageTag getPageTagByName(String spaceUname, String name);
	
	/**
	 * same with getPageTagsNameList( WikiConstants.CONST_INSTANCE_RESOURCE_NAME);
	 * @return
	 */
	Map<String,Integer> getSpaceTagsNameList();
	List<SpaceTag> getSpaceTags();
	List<Space> getSpaceByTag(User viewer, String tagName, int count);

	
	/**
	 * This method is call internal, does not do MethodSecurityInterceptor check.
	 * Tag must not exist in specific space(Save only!)
	 */
	PageTag internalSavePageTag(PageTag tag);




}
