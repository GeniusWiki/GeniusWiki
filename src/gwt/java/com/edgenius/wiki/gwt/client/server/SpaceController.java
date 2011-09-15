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
package com.edgenius.wiki.gwt.client.server;

import java.util.ArrayList;

import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.gwt.client.model.BlogMetaList;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.SpaceListModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;

/**
 * @author Dapeng.Ni
 */
public interface SpaceController extends RemoteService{
	String MODULE_ACTION_URI = "space.rpcs";

	PageItemListModel getRemovedPages(String spaceUname);

	SpaceModel removeSpace(String spaceUname);
	SpaceModel restoreSpace(String spaceUname);
	
	boolean isDuplicatedSpace(String nameOrUname, boolean isName);
	SpaceModel createSpace(SpaceModel space);
	SpaceModel updateSpace(SpaceModel space);
	SpaceModel updateTheme(String spaceUname, String themeName);
	SpaceModel getSpace(String spaceUname);
	
	SpaceListModel getSpacesInfo(String filterText, int returnCount, String sortBy);
	Boolean updateCommentNotifyType(String spaceUname,int type);
	
	BlogMetaList getBlogs(int type, String url, String user, String pwd);
	/**
	 * @param spaceUname
	 * @param selectedBlog
	 * @param updateLinkedBlogSync
	 */
	BlogMetaList updateLinkedBlog(String spaceUname, ArrayList<BlogMeta> selectedBlog);
	
	String updateShellLink(String spaceUname, boolean link);

}
