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

/**
 * @author Dapeng.Ni
 */
public interface TagController extends RemoteService{
	String MODULE_ACTION_URI = "tag.rpcs";
	
	/**
	 * If spaceUname is blank or null, return space tags, otherwise, return page tags in this space
	 * @param spaceUname
	 * @param callback
	 */
	TagListModel getTags(String spaceUname);
	/**
	 * Get all tags for page in in given space 
	 * @param spaceUname
	 * @return
	 */
	TagListModel getPageScopeTags(String spaceUname);
	/**
	 * Get all tags for space in entire system instance
	 * @return
	 */
	TagListModel getSpaceScopeTags();
	/**
	 * Get page list which is tagged by given tag name
	 * @param spaceUname
	 * @param tagname
	 * @param count
	 * @return
	 */
	PageItemListModel getTagPages(String spaceUname,String tagname, int count);
	
	SpaceListModel getTagSpaces(String tagname,  int count);
	
	TagListModel savePageTags(String spaceUname, String pageUuid,  String text, CaptchaCodeModel captcha);

}
