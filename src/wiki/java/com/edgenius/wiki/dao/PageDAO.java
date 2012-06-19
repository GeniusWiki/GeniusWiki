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
package com.edgenius.wiki.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.edgenius.core.dao.DAO;
import com.edgenius.core.model.User;
import com.edgenius.wiki.model.Page;

/**
 * @author Dapeng.Ni
 */
public interface PageDAO extends DAO<Page>{

	/**
	 * Return current page only if this page is NOT removed.
	 * @param uuid
	 * @return
	 */
	Page getCurrentByUuid(String uuid);

	/**
	 * this will return current page with this uuid  even this page is marked as removed
	 * @param fromPageUuid
	 * @return
	 */
	Page getByUuid(String pageUuid);
	
	/**
	 * @param spaceUname
	 * @param pageTitle
	 * @return
	 */
	Page getCurrentPageByTitle(String spaceUname, String pageTitle);
	

	/**
	 * get all current page title and uid pair according to uuid.
	 * @param uuid
	 * @return
	 */
	Map<Integer,String>  getTitlesByUuid(String uuid);
	/**
	 * @param spaceUname
	 * @return
	 */
	List<Page> getTree(String spaceUname);




	/**
	 * @param uid
	 * @return
	 */
	List<Page> getChildren(Integer uid);

	/**
	 * @param spaceUname
	 * @return
	 */
	List<Page> getRemovedPagesInSpace(String spaceUname);

	/**
	 * return all current pages in this space 
	 */
	List<Page> getSpaceAllPages(String spaceUname);


	/**
	 *  Only get current page by given count.
	 * @param spaceUname if null, return all space's(instance) recent pages
	 * @param start
	 * @param count
	 * @param sortByModify if false, sort by created date then
	 * @return
	 */
	List<Page> getRecentPages(String spaceUname, int start, int count, final boolean sortByModify);

	/**
	 * Does not includes page which marked with "removed" flag. The pages doesn't include histories, i.e., the user is 
	 * either creator or modifier. If the users update a page, but overwrite by others, then it won't be returned.
	 * @param spaceUname
	 * @param returnNum
	 * @return
	 */
	List<Page> getUserUpdatedPagesInSpace(String spaceUname, User user, int returnNum);

	/**
	 * @param user
	 * @param limit
	 * @return
	 */
	List<Page> getUserContributedPages(User user, int limit);

	List<Page> getPagesInSpace(String spaceUname,Date touchedDate, int returnNum);

	/**
	 * @param spaceUname
	 * @return
	 */
	List<String> getPagesUuidInSpace(String spaceUname);


	/**
	 * Get all pages in current system, except those already removed page. But the deleted spaces pages still be count in.
	 * @return
	 */
	long getSystemPageCount();

	/**
	 * @param username
	 * @return
	 */
	long getUserAuthoredSize(String username);

	/**
	 * @param username
	 * @return
	 */
	long getUserModifiedSize(String username);

	/**
	 * Return for sitemap - only include these field:
	 * <li>Page uid</li>
	 * <li>Page uuid</li>
	 * <li>Page title</li>
	 * <li>Page modified date</li>
	 * <li>Space Uname</li>
	 * @param lastModifiedDate 
	 * @param start
	 * @param returnNum
	 * @return
	 */
	List<Page> getPageForSitemap(Date lastModifiedDate, final int start, final int returnNum);
	
	/**
	 * Return a manually created Page object(comparing Hibernate), which only include
	 * PageTitle, PageUUID, page.getContent().getContent() and page.getSpace().getUid();
	 * 
	 * @param start
	 * @param returnNum 
	 * @return
	 */
	List<Page> getPageForIndexing(int start, int returnNum);

	/**
	 * @param pageUuid
	 * @return
	 */
	Integer getChildrenCount(Integer parentUid);

	/**
	 * @param spaceUname
	 * @param unixName
	 * @return
	 */
	Page getCurrentByUnixName(String spaceUname, String unixName);

	/**
	 * @param extLinkID
	 * @return
	 */
	Page getPageByProgressExtLinkID(String spaceUname, String extLinkID);

	/**
	 * @param spaceUid
	 * @return
	 */
	List<Page> getPinTopPagesInSpace(Integer spaceUid);
}
