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

import com.edgenius.wiki.gwt.client.model.CaptchaCodeModel;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.SpaceListModel;
import com.edgenius.wiki.gwt.client.model.TagListModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Dapeng.Ni
 */
public interface TagControllerAsync extends RemoteServiceAsync{
	/**
	 * If spaceUname is blank or null, return space tags, otherwise, return page tags in this space
	 * @param spaceUname
	 * @param callback
	 */
	void getTags(String spaceUname,AsyncCallback<TagListModel> callback);
	
	void getPageScopeTags(String spaceUname,AsyncCallback<TagListModel> callback);
	void getSpaceScopeTags(AsyncCallback<TagListModel> callback);
	
	void getTagPages(String spaceUname,String tagname, int count, AsyncCallback<PageItemListModel> callback);
	void getTagSpaces(String name,  int count, AsyncCallback<SpaceListModel> callback);
	
	void savePageTags(String spaceUname, String pageUuid, String tagString, CaptchaCodeModel captcha, AsyncCallback<TagListModel> callback);

}
