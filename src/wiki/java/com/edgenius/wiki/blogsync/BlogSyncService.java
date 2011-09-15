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

import java.util.List;

import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;

/**
 * @author Dapeng.Ni
 */
public interface BlogSyncService {
	
	int INIT_DOWNLOAD_LIMIT = 2000;

	/**
	 * Verify given blog's username, password, url is able to return specified blog (BlogMeta.getName())
	 * 
	 * Please note, this method also update given blog's URL to canonical URL, for example, user won't type http:// before the URL,
	 * here will fill it in.
	 * @param blog
	 * @return
	 */
	boolean verifyBlog(BlogMeta blog);
	
	/**
	 * Add ext_link flag to space and blogMeta to spaceSetting, save change into database. 
	 * @param blog
	 * @param spaceUname
	 * @throws BlogSyncException
	 */
	BlogMeta linkToSpace(BlogMeta blog, String spaceUname) throws BlogSyncException;
	/**
	 * Remove ext_link flag to space and blogMeta to spaceSetting, save change into database. 
	 * @param spaceUname
	 */
	void disconnectFromSpace(String spaceUname, String blogKey);
	
	void updateCategories(String spaceUname, BlogMeta blog) throws BlogSyncException;
	/**
	 * Get list of blog by URL with given user and password.
	 * @param type
	 * @param xmlrpc
	 * @param user
	 * @param pass
	 * @return
	 * @throws BlogSyncException
	 */
	List<BlogMeta> getUsersBlogs(int type, String xmlrpc, String user, String pass) throws BlogSyncException;
	
	/**
	 * Download blog post and save them as pages in specified space
	 * @param spaceUname
	 * @param blog
	 * @param limit
	 * @throws BlogSyncException
	 */
	void downloadPosts(String spaceUname, BlogMeta blog, int limit) throws BlogSyncException;
	void postComment(BlogMeta blog, PageComment comment) throws BlogSyncException;
	void downloadComments(String spaceUname, BlogMeta blog) throws BlogSyncException;
	void removePost(BlogMeta blog, String postID) throws BlogSyncException;
	/**
	 * Insert or update post. If the page has postID, then this post will be update. Otherwise, insert new post 
	 * @param blog
	 * @param page
	 * @throws BlogSyncException
	 */
	void post(BlogMeta blog, Page page) throws BlogSyncException;
}
