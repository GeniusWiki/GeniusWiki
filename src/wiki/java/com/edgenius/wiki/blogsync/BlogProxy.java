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
public interface BlogProxy  {
	
	List<BlogMeta> getUsersBlogs(int type, String xmlrpc, String user, String pass)  throws BlogSyncException;
	void downloadPosts(String spaceUname, BlogMeta blog, int limit) throws BlogSyncException;
	void post(BlogMeta blog, Page page) throws BlogSyncException;
	
	void removePost(BlogMeta blog, String postID) throws BlogSyncException;
	void postComment(BlogMeta blog, PageComment comment) throws BlogSyncException;
	void downloadComments(String spaceUname, BlogMeta blog) throws BlogSyncException;
	
	void updateCategories(String spaceUname, BlogMeta blog) throws BlogSyncException;
		
}
