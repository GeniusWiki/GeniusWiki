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
package com.edgenius.wiki.blogsync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.SpaceService;

/**
 * @author Dapeng.Ni
 */
public class BlogSyncServiceImpl implements BlogSyncService {
	private static final Logger log = LoggerFactory.getLogger(BlogSyncServiceImpl.class);
	
	private SpaceService spaceService;
	private SettingService settingService;
	private Map<Integer, BlogProxy> proxies;
	
	public boolean verifyBlog(BlogMeta blog){
		if(blog == null)
			//non blog, then it treats as correct one
			return true;
		
		try {
			List<BlogMeta> blogs = getUsersBlogs(blog.getType(), blog.getUrl(), blog.getUsername(), blog.getPassword());
			if(blogs == null)
				return false;
			for (BlogMeta meta: blogs) {
				if(StringUtils.equals(meta.getName(),blog.getName()))
					//here, a little bit tricky, update url with canonical one
					blog.setUrl(meta.getUrl());
			}
		} catch (BlogSyncException e) {
			return false;
		}
		return true;
	}

	public BlogMeta linkToSpace(BlogMeta blog, String spaceUname) throws BlogSyncException {
		Space space = spaceService.getSpaceByUname(spaceUname);
		space.addExtLinkType(Space.EXT_LINK_BLOG);
		SpaceSetting setting = space.getSetting();
		
		if(StringUtils.isBlank(blog.getKey())){
			//new!
			blog.setKey(UUID.randomUUID().toString());
		}
		
		//convert password to security key - as this will update password to encrypt one, so clone a new one to avoid old one broken
		BlogMeta newMeta = (BlogMeta) blog.clone();
		try {
			newMeta.setPassword(setting.createSecurityKey(blog.getPassword()));
		} catch (Exception e) {
			log.error("Unable to create security key, link blog failed because of security reason",e);
			throw new BlogSyncException(e);
		}
		
		List<BlogMeta> map = setting.getLinkedMetas();
		if(map == null){
			map = new ArrayList<BlogMeta>();
		}
		map.add(newMeta);
		setting.setLinkedMetas(map);
		
		//update linkExt field
		spaceService.updateSpace(space, false);
		//update LinkMeta object
		settingService.saveOrUpdateSpaceSetting(space, setting);
		
		//update categories list - here will save spaceSetting again 
		this.updateCategories(spaceUname, blog);
		
		return blog;
	}

	public void disconnectFromSpace(String spaceUname, String blogKey) {
		Space space = spaceService.getSpaceByUname(spaceUname);
		space.removeExtLinkType(Space.EXT_LINK_BLOG);
		
		SpaceSetting setting = space.getSetting();
		List<BlogMeta> blogs = setting.getLinkedMetas();
		BlogMeta blog = setting.getBlogMeta(blogKey);
		if(blog != null){
			setting.removeSecurityKey(blog.getPassword());
			blogs.remove(blog);
		}
		if(blogs.size() == 0)
			setting.setLinkedMetas(null);
		else
			setting.setLinkedMetas(blogs);
		

		//update linkExt field
		spaceService.updateSpace(space, false);
		//update LinkMeta object
		settingService.saveOrUpdateSpaceSetting(space, setting);
	}
	
	public void updateCategories(String spaceUname, BlogMeta blog) throws BlogSyncException{
		proxies.get(blog.getType()).updateCategories(spaceUname, blog);
	}
	

	public void post(BlogMeta blog, Page page) throws BlogSyncException{
		proxies.get(blog.getType()).post(blog, page);
	}
	public List<BlogMeta> getUsersBlogs(int type, String xmlrpc, String user, String pass) throws BlogSyncException {
		return proxies.get(type).getUsersBlogs(type, xmlrpc, user, pass);
	}
	

	public void downloadPosts(String spaceUname, BlogMeta blog, int limit) throws BlogSyncException {
		proxies.get(blog.getType()).downloadPosts(spaceUname, blog, limit);
	}

	public void postComment(BlogMeta blog, PageComment comment) throws BlogSyncException{
		proxies.get(blog.getType()).postComment(blog, comment);
		
	}

	public void downloadComments(String spaceUname, BlogMeta blog) throws BlogSyncException {
		proxies.get(blog.getType()).downloadComments(spaceUname, blog);
	}
	
	public void removePost(BlogMeta blog, String postID) throws BlogSyncException{
		proxies.get(blog.getType()).removePost(blog, postID);
		
	}
	

	//********************************************************************
	//               set / get methods
	//********************************************************************
	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}
	
	public void setProxies(Map<Integer, BlogProxy> proxies) {
		this.proxies = proxies;
	}

}
