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
package com.edgenius.wiki.rss;

import java.util.List;

import com.edgenius.core.model.User;
import com.edgenius.wiki.model.Page;
import com.sun.syndication.io.FeedException;

/**
 * @author Dapeng.Ni
 */
public interface RSSService {
	
	String SERVICE_NAME = "rssService";
	
	void createFeed(String spaceUname);
	/**
	 * @param spaceUid removed space UID
	 */
	void removeFeed(Integer spaceUid);
	/**
	 * Write feed to out stream according to page permission as well.<br>
	 * This output feed will filer out these pages which viewer has no permission to read.
	 * @param spaceUid
	 * @param spaceUname
	 * @param viewer
	 * @param out
	 * @throws FeedException 
	 */
	String outputFeed(Integer spaceUid,String spaceUname, User viewer) throws FeedException;
	/**
	 * Get page basic information, (SpaceUname,PageUUID,Version, Modified/Create Date) from RSS feed file.<br> 
	 * This output feed will filer out these pages which viewer has no permission to read.
	 * 
	 * @param spaceUid
	 * @param spaceUname
	 * @param viewer
	 * @return
	 * @throws FeedException
	 */
	List<Page> getPagesFromFeed(Integer spaceUid, String spaceUname, User viewer) throws FeedException;
	/**
	 * Check if this page item existed in current RSS, if yes, rebuild entire RSS from database then.
	 * This means this method does not remove item from RSS feed file, it only rebuild RSS according to database.
	 * @param spaceUname
	 * @param removePageUuid
	 */
	boolean removeFeedItem(String spaceUname, String removePageUuid);
	/**
	 * Delete all RSS file under system RSS directory.
	 */
	void cleanAllRss();
	
}
