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
package com.edgenius.wiki.render;

import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.dao.SpaceDAO;
import com.edgenius.wiki.render.object.ObjectPosition;

/**
 * @author Dapeng.Ni
 */
public interface LinkRenderHelper {

	boolean exists(String name);

	boolean showCreate();

	ObjectPosition appendLink(StringBuffer buffer, String name, String view, String anchor);

	ObjectPosition appendLink(StringBuffer buffer, String name, String view);

	ObjectPosition appendCreateLink(StringBuffer buffer, String name, String view);

	/**
	 * NOTE: the same image also hardcode in Filter regex in com.edgenius.wiki.render.Filter.properties file
	 * @return
	 */
	String getExternalImage(RenderContext renderContext, String url);
	
	String getNonexistImage(RenderContext renderContext);
	
	String getExtspaceLinkBreakImage(RenderContext renderContext);
	/**
	 * @param renderContext
	 */
	void initialize(RenderContext context,SpaceDAO spaceDAO,PageDAO pageDAO);

	/**
	 * @param spaceUname
	 */
	void setSpaceUname(String spaceUname);

	/**
	 * @param allowCreate
	 */
	void setAllowCreate(boolean allowCreate);

	/**
	 * @param buffer
	 * @param extSpaceUname
	 * @param link
	 * @param view
	 */
	ObjectPosition appendExtSpaceLink(StringBuffer buffer, String extSpaceUname, String link, String view);

	/**
	 * @param buffer
	 * @param extSpaceUname
	 * @param link
	 * @param view
	 * @param anchor
	 */
	ObjectPosition appendExtSpaceLink(StringBuffer buffer, String extSpaceUname, String link, String view, String anchor);

	/**
	 * @param extSpaceUname
	 * @param link
	 * @return
	 */
	boolean exists(String extSpaceUname, String link);

	/**
	 * Get full page URL with website root URL.
	 * @param spaceUname
	 * @param link
	 * @param anchor
	 * @return
	 */
	String getFullURL(RenderContext renderContext, String spaceUname, String pageTitle, String anchor);

	
	
}
