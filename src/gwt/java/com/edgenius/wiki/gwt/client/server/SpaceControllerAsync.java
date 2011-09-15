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
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Dapeng.Ni
 */
public interface SpaceControllerAsync extends RemoteServiceAsync{

	/**
	 * @param spaceUname
	 * @param panel
	 */
	void getRemovedPages(String spaceUname, AsyncCallback<PageItemListModel> callback);
	void removeSpace(String spaceUname, AsyncCallback<SpaceModel> callback);
	void getSpace(String spaceUname, AsyncCallback<SpaceModel> callback);
	void updateSpace(SpaceModel space, AsyncCallback<SpaceModel> callback);
	void updateTheme(String spaceUname, String themeName, AsyncCallback<SpaceModel> callback);
	void isDuplicatedSpace(String nameOrUname, boolean isName, AsyncCallback<Boolean> callback);
	
	void createSpace(SpaceModel space, AsyncCallback<SpaceModel> callback);
	
	/**
	 * @param spaceUname
	 * @param restoreSpaceAsync
	 */
	void restoreSpace(String spaceUname, AsyncCallback<SpaceModel> callback);
	
	void updateCommentNotifyType(String spaceUname, int type, AsyncCallback<Boolean> callback);
	/**
	 * Get all spaces simple info(title, spaceUname and description only) for performance sake.
	 */
	void getSpacesInfo(String filterText, int returnCount, String sorterBy, AsyncCallback<SpaceListModel> callback);
	/**
	 * Get available blog list from the url with specified authentication.
	 */
	void getBlogs(int type, String url, String user, String pwd, AsyncCallback<BlogMetaList> callback);
	/**
	 * @param spaceUname
	 * @param selectedBlog
	 * @param updateLinkedBlogSync
	 */
	void updateLinkedBlog(String spaceUname, ArrayList<BlogMeta> selectedBlogs,  AsyncCallback<BlogMetaList> callback);
	/**
	 * @param spaceUname
	 * @param link
	 * @param shellDialog
	 */
	void updateShellLink(String spaceUname, boolean link,  AsyncCallback<String> callback);


}
