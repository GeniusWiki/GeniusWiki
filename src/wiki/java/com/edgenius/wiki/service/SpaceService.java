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
package com.edgenius.wiki.service;

import java.util.List;
import java.util.Map;

import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.security.ValidateMethod;

/**
 * @author Dapeng.Ni
 */
public interface SpaceService {

	String SERVICE_NAME = "spaceService";
	String removeSpace = "removeSpace";
	String saveHomepage = "saveHomepage";
	String undoRemoveSpace = "undoRemoveSpace";
	String removeSpaceInDelay = "removeSpaceInDelay";
	String updateSpace = "updateSpace";
	String createSpace = "createSpace";
	String uploadLogo = "uploadLogo";

	Space getSpaceByUname(String spaceUname);
	
	Space getSpaceByTitle(String title);

	/**
	 * There is recursive reference for page and space. In design, it is no problem, but hibernate and transaction not working well. 
	 * I tried flush() space once spaceDAO.save() called. but it does not commit immediately(why???). And it is not good transaction control 
	 * as well. 
	 * <BR>
	 * If space create success, a non-null page object will return. In program next step, it must manually tie this page to space as home page.
	 * @param space
	 * @return home page
	 */
	@ValidateMethod
	Page createSpace(Space space) throws SpaceException ;
	
	@ValidateMethod
	Space updateSpace(Space space, boolean updateIndex);
	
	Space getSpace(Integer uid);
	/**
	 * Put save homepage into separate method with create space, because recursive call happen in create space and page.
	 * This method is controlled by security interceptor because so far, it only call after createSpace() method. 
	 * @param space
	 * @param homepage
	 */
	Space saveHomepage(Space space, Page homepage);
	/**
	 * Update load space logo. One is large, another is 16*16 smaller.
	 * @param space
	 * @param smallLogo
	 * @param largeLogo
	 */
	@ValidateMethod
	void uploadLogo(Space space, FileNode smallLogo, FileNode largeLogo);
	
	/**
	 * Delay remove space in given hours later.
	 * @param spaceUname
	 * @param delayHours
	 * @return
	 * @throws SpaceException
	 */
	@ValidateMethod
	Space removeSpaceInDelay(String spaceUname, int delayHours) throws SpaceException;
	

	@ValidateMethod
	int undoRemoveSpace(String spaceUname) throws SpaceException;
	/**
	 * 
	 * @return the removed space. 
	 * @throws SpaceException
	 */
	//this method does not call direct thru HttpRequest call, so don't do 
	//@ValidateMethod
	Space removeSpace(String spaceUname, boolean persist) throws SpaceException;

	/**
	 * Only return current pages (history does not return) which are marked as "removed"
	 * @param spaceUname
	 * @return
	 */
	List<Page> getRemovedCurrentPages(String spaceUname);
	/**
	 * Return all recently pages by given retCount number. This method won't do any security check and filter. 
	 * @param spaceUname could be null, means all spaces recent pages
	 * @param sortByModify if false, then it means sortByCreateDate
	 * @return
	 */
	List<Page> getRecentPages(String spaceUname, int retCount,  boolean sortByModify);
	
	/**
	 * 
	 * @param spaceUname
	 * @param start
	 * @return
	 */
	List<Page> getRecentPages(String spaceUname, int start,int retCount, boolean sortByModify);

	/**
	 * Get all spaces's unixName without any limitation but return does not contain $SYSTEM$ space.
	 * @return
	 */
	List<String> getAllSpaceUnames();
	/**
	 * Get space by pagination model for given user who has at least permission to read this space. 
	 * The results is ordered by Space Rating value. Note, permission[] in returned Space does not filled.
	 * 
	 * For public space, it will always return. For private space, only users have read permission will be returned.
	 * 
	 * The spaces marked with remove flag are also returned.
	 * @param viewer
	 * @param fromItem
	 * @param returnSize
	 * @param sortBy value combination from  Space.SORT_BY_* with separator "|"
	 * @return
	 */
	
	List<Space> getSpaces(User viewer, int fromItem, int returnSize, String sortBy, String filter, boolean sortByDesc);
	
	/**
	 * Get given user created space list.<br>
	 * 
	 * This method will filter out these space which are private and the viewer has no reading permission.  
	 * For public space, it will always return but it will set space.isReadable() flag to indicate if this space allow read.
	 * 
	 * <BR>
	 * Do not simply use SecurityInceptor to check if allow current user call this method because it uses space 
	 * reading filter inside itself.
	 * 
	 * @param username
	 * @param limit how many returned 
	 * @return
	 */
	List<Space> getUserAllCreatedSpaces(String username, int limit, User viewer);

	int getUserAuthoredSpaceSize(String username);
	
	/**
	 * @return space type = -1, this space use to hold instance level pages, such search result, user profile etc.
	 */
	Space getSystemSpace();

	/**
	 * How many hours left for a space will be removed.
	 * @param spaceUname
	 * @return
	 */
	int getRemovedSpaceLeftHours(String spaceUname);

	/**
	 * @return all spaces count even it is private and mark removed. 
	 */
	int getSpaceCount(String filter);

	/**
	 * Get all spaces' page count, exclude the space that already marked remove flag . 
	 * @return Map<spaceUid,pageCount>
	 */
	Map<Integer,Long> getAllSpacePagesCount();

	/**
	 * @param unixName
	 * @return
	 */
	Page getLastUpdatedPage(String unixName);

	/**
	 * Get all attachments file list from given space
	 * @param spaceUname
	 * @return
	 */
	List<FileNode> getAttachments(String spaceUname, boolean withHistory, boolean withDraft, User user) throws RepositoryException;

}
