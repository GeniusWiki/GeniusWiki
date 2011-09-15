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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.repository.RepositoryQuotaException;
import com.edgenius.core.repository.RepositoryTiemoutExcetpion;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.Draft;
import com.edgenius.wiki.model.History;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageProgress;
import com.edgenius.wiki.model.UserPageMark;
import com.edgenius.wiki.security.ValidateMethod;

/**
 * PageService has 4 AOP interceptors so far (Oct. 9, 2007) 
 * 
 * <li>PageIndexInterceptor will intercept save..() after method to create Lucene index order="10" </li>
 * <li>PageSavingLockInterceptor is around method interceptor.It will control savePage() concurrent lock to avoid same page version conflict. order="20" </li>
 * <li>Transaction. Order = 30</li>  
 * <li>MethodSecurityInterceptor is around method interceptor, to valid method before and after security permission. Order = 40</li>
 * 
 * Above 4 interceptor must following special orders to execute. Above order could get correct results.
 * Some Tips<br>
 * Index service will first come in,but last run "after". It is meaning for it is last method call in all page method is finished. and it is only 
 * methodAfterIntercepor as well.
 * <br>
 * MethodSecurityInterceptor is inside transaction, it provides the ability to make transaction rollback even method is complete method body logic.
 * But, the side-effect is, it should slow all method if this user has not permission to run this method: it after 2 interceptors later...
 * However, usually, permission check does not need care about this kind performance lost.
 * 
 * 
 * !!! Please be very carefully if you want to change method signature or parameters. PageIndexInterceptor and PageMethodValueProvider
 * are quite dependent on method name and parameters's count and order.  
 * @author Dapeng.Ni
 */
public interface PageService {
	String SERVICE_NAME="pageService";
	
	public static final String updateAttachmentMetaData = "updateAttachmentMetaData";
	public static final String copy = "copy";
	public static final String move = "move";
	public static final String restorePage = "restorePage";
	public static final String restoreHistory = "restoreHistory";
	public static final String removePage = "removePage";
	public static final String removeAttachment = "removeAttachment";
	public static final String uploadAttachments = "uploadAttachments";
	public static final String savePage = "savePage";
	public static final String getHistoryPages = "getHistoryPages";
	public static final String getHistory = "getHistory";
	public static final String getPageTree = "getPageTree";
	public static final String getPage = "getPage";
	public static final String getHomepage = "getHomepage";
	public static final String getCurrentPageByTitle = "getCurrentPageByTitle";
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//below method is do security validation on PageMethodBeforeAdvice
	public static final String removeDraft = "removeDraft";
	public static final String getDraftPages = "getDraftPages";
	public static final String getDraft = "getDraft";
	public static final String saveDraft = "saveDraft";
	
	/**
	 * RENDER: YES
	 * ANCESTORS: YES
	 * ATTACHMENT: NO
	 * 
	 * Basically, if new page, just create it.
	 * If existed page (pageUid is not empty), clone existed latest version page to another new record, then update
	 * the exist record. This makes latest page uid never be changed.
	 * @param forceSave 
	 * 
	 * @param pageValue, transient page value from view 
	 * @param requireNotified, whether need send notification - value from WikiConstants.NOTIFY_*
	 * @return saved page
	 * @throws PageException 
	 * @throws DuplicatedPageException 
	 * @throws VersionConflictException 
	 * @throws PageSaveTiemoutExcetpion This exception won't throw inside <code>savePage()</code> directly, but it may throw from <code>PageSavingLockInterceptor</code>.
	 */
	@ValidateMethod
	Page savePage(Page pageValue, int requireNotified, boolean forceSave) throws PageException, VersionConflictException, DuplicatedPageException, PageSaveTiemoutExcetpion;


	/**
	 * RENDER: YES
	 * ANCESTORS:YES
	 * ATTACHMENT: NO
	 * 
	 * This is same with  getCurrentPageByTitle(String,String,true). And this method also do security validation.
	 * @param spaceUname
	 * @param pageTitle cannot be null. Although looks return home is not bad, but this may lead unexpected to get home page
	 * @return
	 */
	@ValidateMethod
	Page getCurrentPageByTitle(String spaceUname, String pageTitle) throws SpaceNotFoundException;
	
	/**
	 * RENDER: PARAMETER
	 * ANCESTORS: PARAMETER
	 * ATTACHMENT: NO
	 * 
	 * This method won't return page attachment list!
	 * PLEASE NOTE: this method does not do security valid!!! Anywhere required permission, please use SecurityService.isAllowPageReading() separately.
	 *  
	 */
	Page getCurrentPageByTitleWithoutSecurity(String spaceUname,String pageTitle, boolean render) throws SpaceNotFoundException;
	
	/**
	 * RENDER: NO
	 * ANCESTORS: NO
	 * ATTACHMENT: NO
	 * 
	 * This method will get current page and render its content, also get its ancestor information. It is same with
	 * 
	 * !!! This method does not do security validate now!!! Please take care if it has potential permission check requirement.
	 * @see SecurityDummy.checkPageRead() method for security check.
	 * 
	 * getCurrentPageByUuid(String,false);
	 * @param pageUuid
	 * @return
	 */
	Page getCurrentPageByUuid(String pageUuid);
	/**
	 * RENDER: PARAMETER
	 * ANCESTORS: PARAMETER
	 * ATTACHMENT: NO
	 * 
	 * This method will get current page and decide if rendering and getting ancestor information
	 * @param pageUuid
	 * @param render
	 * @return
	 */
	Page getCurrentPageByUuid(String pageUuid, boolean render);
	/**
	 * RENDER: NO
	 * ANCESTORS: NO
	 * ATTACHMENT: NO
	 * 
	 * Get all page histories, which does not contains current page 
	 * @param pageUuid
	 * @param returnCount 
	 * @param startVer - return histories less than this startVer 
	 * @param touchedDate 
	 * @return
	 */
	@ValidateMethod
	List<History> getHistoryPages(String spaceUname,String pageUuid, int startVer, int returnCount, Date touchedDate);
	 
	/**
	 * RENDER: YES
	 * ANCESTORS: YES
	 * ATTACHMENT: NO
	 *  
	 * @param spaceUname
	 * @return
	 * @throws PageException
	 */
	@ValidateMethod
	Page getHomepage(String spaceUname) ;

	/**
	 * RENDER: NO
	 * ANCESTORS: NO
	 * ATTACHMENT: NO
	 * 
	 * This method get page and do rendering HTML and bind attachment as well. The return is ready for render to browser.
	 */
	@ValidateMethod
	Page getPage(Integer pageUid) ;
	/**
	 * RENDER: YES
	 * ANCESTORS: YES
	 * ATTACHMENT: NO
	 *   
	 * @param historyUid
	 * @return
	 */
	@ValidateMethod
	History getHistory(Integer historyUid);
	/**
	 * RENDER: NO
	 * ANCESTORS: NO
	 * ATTACHMENT: NO
	 *   
	 * @param historyUid
	 * @return
	 */
	History getHistoryObject(Integer historyUid);
	/**
	 * RENDER: NO
	 * ANCESTORS: NO
	 * ATTACHMENT: NO
	 * 
	 * @param pageUuid
	 * @param historyVersion
	 * @return
	 */
	//don't do valid so far, maybe later has such requirement
	History getHistoryByVersion(String pageUuid, Integer historyVersion);

	/**
	 * Return page  Ancestors list (nav bar list)
	 */
	List<AbstractPage> getPageAncestors(String spaceUname, String  pageUuid);
	/**
	 * get sorted page tree list.<br>
	 * <b>
	 * The page in returned list only contain following page value:
	 * <code>Page.uid, Page.uuid, Page.title, Page.parent.uid, Page.level, Page.space.unixName</code>
	 * </b>
	 *  
	 * @param spaceUname
	 * @return
	 */
	@ValidateMethod
	List<Page> getPageTree(String spaceUname);

	/**
	 * all history if version equals 0 or null.
	 * This method won't remove shared attachment from repository and FileNode table.
	 * @param pageUuid 
	 * @param nodeVersion 
	 * @param attachmentUid
	 * @return removed file node
	 */
	@ValidateMethod
	FileNode removeAttachment(String spaceUname, String pageUuid, String nodeUuid, String nodeVersion) throws RepositoryException , RepositoryTiemoutExcetpion;

	/**
	 * @param spaceUname
	 * @param pageUuid 
	 * @param files
	 * @param pageUuid
	 * @param compareMd5Digest if true, it will compare the uploading files MD5 with its saved same version one.  If saved version 
	 * 						has MD5Digest value and equals, then this uploading won't be saved again. Please note, the new comments
	 * 						also is discarded.  
	 * 						If uploading is accepted, the saved file is calculated MD5 as well.  
	 * 						Please be careful of performance loss when turn on this option.
	 * @return
	 */
	@ValidateMethod
	List<FileNode> uploadAttachments(String spaceUname, String pageUuid, List<FileNode> files, boolean compareMd5Digest) 
		throws RepositoryException, RepositoryTiemoutExcetpion, RepositoryQuotaException ;
	/**
	 * 
	 * @param fromPageUuid
	 * @param toSpaceUname
	 * @param toParentUuid
	 * @param withChildren
	 * @return the new page 
	 * @throws PageException
	 */
	@ValidateMethod
	Page copy(String fromSpaceUname, String fromPageUuid, String toSpaceUname, String toParentUuid, boolean withChildren) throws PageException;
	/**
	 * 
	 * @param fromPageUuid
	 * @param toSpaceUname
	 * @param toParentUuid
	 * @param withChildren
	 * @return the target page (new created page in target space)
	 * @throws PageException
	 */
	@ValidateMethod
	Page move(String fromSpaceUname, String fromPageUuid, String toSpaceUname, String toParentUuid, boolean withChildren) throws PageException;
	/**
	 * remove page with its history, attachment, draft etc.
	 * @param pageUuid
	 * @param withChildren
	 * @param permenant physical remove from db.
	 * @return
	 * @throws PageException
	 */
	@ValidateMethod
	Page removePage(String spaceUname, String pageUuid, boolean withChildren, boolean permenant) throws PageException;
	
	/**
	 * Check if this restored page has a duplicated title page exist in space. This is useful to ask user choose if go ahead to restore.
	 * @param restoredPageUuid
	 * @return the potential title of page. Which dependent on if space has same title page, if no, the return original name, otherwise, will
	 * add prefix on original title. And any exception, it will return null.
	 * NOTE:this check need know, if the restore page is home page, and current home page already exist,
	 * then need warning user, current home page will be replace etc. but I don't want to add new model, so just put a one char string flag:<br>
	 * SharedConstants.RESTORE_NORMAL = 0;<br>
	 * SharedConstants.RESTORE_HOMEPAGE_EXIST = 1;<br>
	 * SharedConstants.RESTORE_HOMEPAGE_NO_EXIST = 2;<br>
	 * <br>
	 * It is trick, bad design, but works.  
	 */
	String restorePageCheck(String restoredPageUuid);
	
	/**
	 * RENDER: NO
	 * ANCESTORS: NO
	 * ATTACHMENT: NO
	 * 
	 * It will check if the restored page has same title in space now, if so, it will rename restored page to "Restore of title"  
	 * 
	 * @param spaceUname
	 * @param pageUuid
	 * @param withHistory
	 * @return
	 * @throws PageException
	 */
	@ValidateMethod
	Page restorePage(String spaceUname, String pageUuid, boolean homepage, boolean withHistory) throws PageException;
	

	/**
	 * RENDER: YES
	 * ANCESTORS: YES
	 * ATTACHMENT: NO
	 * 
	 * Restore a page from history version.
	 * @param spaceUname
	 * @param currPageUuid: use pageUuid rather than pageTitle is for security check reason.
	 * @param version
	 * @return
	 * @throws DuplicatedPageExceptionP
	 * @throws PageSaveTiemoutExcetpion ageSaveTiemoutExcetpion 
	 */
	@ValidateMethod
	Page restoreHistory(String spaceUname, String currPageUuid, int version) 
		throws PageException, DuplicatedPageException, PageSaveTiemoutExcetpion;
	//********************************************************************
	//               Favroite/Watch/PinTop method
	//********************************************************************
	boolean markPageFlag(int flag, String pageUuid, String username, boolean add);
	/**
	 * Return page mark with favorite, watched or pin-top. The first 2 is specified by given user 
	 * @param user
	 * @param page
	 * @return
	 */
	List<UserPageMark> getPageMarks(User user, Page page);

	//********************************************************************
	//               Draft method
	//********************************************************************
	/**
	 * @param user is only for Security validation
	 * @param type Manual:1 or Auto:2
	 * @param pageValue, transient page value from view 
	 * @return saved draft page
	 * 
	 * save draft will valid if current user is not null
	 */
	@ValidateMethod
	Draft saveDraft(User user, Draft pageValue, int type) throws PageException;
	/**
	 * Return auto and manual draft in list, the element of draft only contains pageUid, ModifiedDate and Draft Type.
	 * @param user is only for Security validation
	 * @param spaceUname
	 * @param pageTitle
	 * @param type Draft.AUTO_DRAFT or Draft.MANUAL_DRAFT
	 * @return
	 */
	/**
	 * 	//remove draft will valid according if it is same user between current and draft modifier
	 */
	@ValidateMethod
	Draft removeDraft(User user, String spaceUname, String pageUuid, int type) throws PageException;
	List<Draft> hasDraft(String spaceUname, String pageTitle, User user);
	
	/**
	 * This method only get this users all manual saved draft. Auto-saved draft will be ignored.
	 * @param user is only for Security validation
	 * @param type draft type,  manual(1) , auto(2) or both(0)
	 * @return
	 */
	@ValidateMethod
	List<Draft> getDraftPages(User user, int type);

	/**
	 * RENDER: NO
	 * ANCESTORS: YES
	 * ATTACHMENT: NO
	 * 
	 * Get draft by draft UID
	 * @param user is only for Security validation
	 * @param draftUid
	 * @return
	 */
	@ValidateMethod
	Draft getDraft(User user, Integer draftUid);
	//********************************************************************
	//               Personal page methods
	//********************************************************************
	List<Page> getFavoritePages(String username);
	List<Page> getWatchedPages(String username);
	/**
	 * The returned page does not include rendered content and attachment information.<br>
	 * This method will allow security check to filter out the pages which is allow read on given viewer.<br>
	 * NOTE: <br>
	 * - This method does not need do security filter check as it will filter result according to given viewer.
	 * - Given username and viewer may be different person, in case, some user is viewing other persons authored page from his profile panel.
	 * - Return does not contain pages which marked as "removed"
	 * @param spaceUname
	 * @param username
	 * @param returnNum
	 * @return
	 */
	List<Page> getUserUpdatedPagesInSpace(String spaceUname, String username, int returnNum, User viewer);

	/**
	 * Get given user's created/update pages list.<br>
	 * This method will allow security check to filter out the pages which is allow read on given viewer
	 * NOTE: <br>
	 * - This method does not need do security filter check as it will filter result according to given viewer.
	 * - Given username and viewer may be different person, in case, some user is viewing other persons authored page from his profile panel.
	 * - Return does not contain pages which marked as "removed"
	 * @param username
	 * @param limit how many returned 
	 * @return
	 */
	List<Page> getUserAllContributedPages(String username, int limit, User viewer);
	List<History> getUserAllContributedHistories(String username, User viewer);

	/**
	 * Get all pages which is readable for given viewer in this space and after touchedDate.
	 * NOTE: <br>
	 * - This method does not need do security filter check as it will filter result according to given viewer.
	 * - Given username and viewer may be different person, in case, some user is viewing other persons authored page from his profile panel.
	 * - Return does not contain pages which marked as "removed"
	 * @param spaceUname
	 * @param touchedDate 
	 * @param returnNum
	 * @param viewer
	 */
	List<Page>  getPagesInSpace(String spaceUname, Date touchedDate, int returnNum, User viewer);
	
	/**
	 *  Get all pages UUID which is readable for given viewer
	 * @param spaceUname
	 * @param viewer
	 * @return
	 */
	List<String> getPagesUuidInSpace(String spaceUname, User viewer);

	/**
	 * Get page method will call this method to fill attachment.
	 * Save/saveDraft does not call, because it is possible new attachment submit once after save/savedraft done.
	 * E.g, when user choose several upload files and click save button, or, pageUuid is null, attachment auto saving timer
	 * will call saveDraft() first.
	 * 
	 * If parameter withDraft is false, parameter viewer will be ignored. If true, only the given viewer's draft attachment will be returned. 
	 * 
	 * This method doesn't return file content stream in FileNode.
	 * @param pageUuid
	 * @return if no attachment, return empty List.
	 * @throws RepositoryException 
	 */
	List<FileNode> getPageAttachment(String spaceUname,String pageUuid, boolean withHistory, boolean withDraft, User viewer) throws RepositoryException;
	
	/**
	 * 
	 * @param spaceUname
	 * @param pageUuid this parameter is useless for method body but for MethodSecurityInterceptor.
	 * @param nodeUuid
	 * @param name
	 * @param desc
	 * @throws RepositoryException
	 */
	@ValidateMethod
	FileNode updateAttachmentMetaData(String spaceUname,String pageUuid, String nodeUuid, String name, String desc) throws RepositoryException;


	/**
	 * @param username
	 * @return
	 */
	long getUserAuthoredPageSize(String username);


	/**
	 * @param username
	 * @return
	 */
	long getUserModifiedPageSize(String username);


	/**
	 * @param pageUuid
	 * @return
	 */
	Set<User> getPageContributors(String pageUuid);


	/**
	 * Return page children list without render content.
	 * @param pageUuid
	 * @return
	 */
	List<Page> getPageChildren(String pageUuid);

	Integer getPageChildrenCount(String pageUuid);


	/**
	 * @param spaceUname
	 * @param pageTitle
	 * @return
	 */
	boolean existCurrentPageByTitle(String spaceUname, String pageTitle);

	/**
	 * Set marker to identify the user is editing the page. The marker is automatically remote one hour later.
	 * @param pageUuid
	 * @param user
	 * @see stopEditing(String, User)
	 */
	void startEditing(String pageUuid, User user);
	/**
	 * Remove the editing marker.
	 * @param pageUuid
	 * @param user
	 * @see startEditing(String, User)
	 */
	void stopEditing(String pageUuid, User user);
	/**
	 * @param pageUuid
	 * @return user name and timestamp of he began editing 
	 */
	Map<String, Long> isEditing(String pageUuid);


	/**
	 * @param spaceUname
	 * @param postKey
	 * @return
	 */
	Page getCurrentPageByUnixName(String spaceUname, String postKey);


	/**
	 * @param extLinkID
	 * @return
	 */
	Page getPageByExtLinkID(String spaceUname, String extLinkID);


	/**
	 * @param pageProgress
	 */
	void saveOrUpdatePageProgress(PageProgress pageProgress);


	/**
	 * 
	 * @param uid
	 * @param spaceUname
	 * @param viewer
	 * @return
	 */
	List<Page> getPinTopPages(Integer spaceUid, String spaceUname, User viewer);


	

}
