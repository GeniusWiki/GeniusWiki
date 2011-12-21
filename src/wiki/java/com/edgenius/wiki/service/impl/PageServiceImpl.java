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
package com.edgenius.wiki.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.Global;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.ITicket;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.repository.RepositoryQuotaException;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.repository.RepositoryTiemoutExcetpion;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.MenuItem;
import com.edgenius.wiki.MenuItem.MenuItemComparator;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.dao.DraftDAO;
import com.edgenius.wiki.dao.HistoryDAO;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.dao.PageLinkDAO;
import com.edgenius.wiki.dao.PageProgressDAO;
import com.edgenius.wiki.dao.SpaceDAO;
import com.edgenius.wiki.dao.UserPageDAO;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.MacroModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.PageAttribute;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.Draft;
import com.edgenius.wiki.model.DraftContent;
import com.edgenius.wiki.model.History;
import com.edgenius.wiki.model.HistoryContent;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageContent;
import com.edgenius.wiki.model.PageLink;
import com.edgenius.wiki.model.PageProgress;
import com.edgenius.wiki.model.PageTag;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.UserPageMark;
import com.edgenius.wiki.render.macro.MenuItemMacro;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.CommentService;
import com.edgenius.wiki.service.DuplicatedPageException;
import com.edgenius.wiki.service.EventContainer;
import com.edgenius.wiki.service.PageEventHanderException;
import com.edgenius.wiki.service.PageEventListener;
import com.edgenius.wiki.service.PageException;
import com.edgenius.wiki.service.PageSaveTiemoutExcetpion;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.SpaceNotFoundException;
import com.edgenius.wiki.service.TagService;
import com.edgenius.wiki.service.TouchService;
import com.edgenius.wiki.service.VersionConflictException;
import com.edgenius.wiki.util.PageComparator;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@Transactional
public class PageServiceImpl implements PageService {
	private static final Logger log = LoggerFactory.getLogger(PageServiceImpl.class);

	//Basic refer services
	private UserReadingService userReadingService;
	private RepositoryService repositoryService;
	private TagService tagService;

	private TouchService touchService;
	private CommentService commentService;
	private RenderService renderService;
	private SecurityService securityService;
	@Autowired private SettingService settingService;
	
	//For memory saving reason, cached page only save uid, Uuid, level, spaceUname, title 
	//and parent info(parent page also includes same info)
	private Cache pageTreeCache;
	
	private EventContainer eventContainer;
	
	//save: pageUuid -> map of username and time editing. 
	private Cache pageEditingCache;
	//DAO 
	private PageDAO pageDAO;
	private PageProgressDAO pageProgressDAO;
	private DraftDAO draftDAO;
	private HistoryDAO historyDAO;
	private SpaceDAO spaceDAO;
	private PageLinkDAO pageLinkDAO;
	private UserPageDAO userPageDAO;
	//********************************************************************
	//                       Methods
	//********************************************************************
	public Page savePage(Page pageValue,int requireNotified,boolean forceSave) throws PageException, VersionConflictException, 
			DuplicatedPageException, PageSaveTiemoutExcetpion {
		Page page = null;
		String spaceUname = pageValue.getSpace().getUnixName();
		String newPageTitle = pageValue.getTitle();
		Integer newPageUid = pageValue.getUid();
		
		log.info("Page saving for " + pageValue.getTitle() + " on space "+ spaceUname);
		Space space; 
		//page already exist, need clone then save a new record in database
		String oldTitle = null; 
		boolean needRefreshCache = false; 
		
		if(newPageUid != null){
			//The page will create  old version to new record but update same UID as current 
			//it would get same result by pageDAO.getCurrentByUuid() but a little bit faster in performance.
			page = pageDAO.get(newPageUid);
		}else if (!StringUtils.isBlank(pageValue.getPageUuid())){
			//if user choose a item from My Draft in Dashboard, this won't bring in a newPageUid 
			//There are 3 scenarios for this case. 
			//1. it is a existed page draft.Following method returns current page,
			//2. non-existed page draft. Following method returns null.
			//3. non-existed page but page has a copy in trash bin! The below method return null as well, but the uuid is already invalid
			// as it is used by trashed page - so need further check - if it has trashed page, reset pageUUID
			page = pageDAO.getCurrentByUuid(pageValue.getPageUuid());
			
			if(page == null){
				Page removedPage = pageDAO.getByUuid(pageValue.getPageUuid());
				if(removedPage != null && removedPage.isRemoved()){
					//case 3, treat it as new page
					pageValue.setPageUuid(null);
				}
			}
		}
		
		if(!forceSave && !checkVersion(pageValue, page)){
			throw new VersionConflictException(page.getVersion()); 
		}
		
		
		//!!!Title duplicated problem: user try to create a new page or rename a page but same title already exist in space 
		Page sameTitlePage= pageDAO.getCurrentPageByTitle(spaceUname,newPageTitle);
		if(page != null){
			if(sameTitlePage != null){
				if(!sameTitlePage.getPageUuid().equals(page.getPageUuid()))
					throw new DuplicatedPageException();
			}
	
			//keep old page :NOTE: this piece code has duplicate with fixLinksToTitle() method
			History oldPage = (History) page.cloneToHistory();
			//put this page to history page:create a new record with cloned value except Uid
	//			history page does not save link, tag and attachment info. 
	//			The key is save content change!
			oldPage.setAttachments(null);
			oldPage.setParent(null);
			historyDAO.saveOrUpdate(oldPage);
			
			if(!StringUtils.equalsIgnoreCase(oldPage.getTitle(),newPageTitle)){
				// oldTitle is not null, so that update PageLink on below
				oldTitle = oldPage.getTitle();
				needRefreshCache = true;
				//remove old page with old title from cache first, new page should add after page saved
				removePageCache(spaceUname, page);
			}
			//update current page with new value
			space = page.getSpace();
			
			copyValueFromView(page,pageValue);
	//			page.setUnixName(WikiUtil.getPageUnixname(newPageTitle));
			WikiUtil.setTouchedInfo(userReadingService, page);
			page.setVersion(page.getVersion() + 1);
		}else{
			//for new create page: same title page must not exist
			if(sameTitlePage != null){
				throw new DuplicatedPageException("Page has duplicated title:" + newPageTitle);
			}
			
			needRefreshCache = true;
			//a new page first time save:
			page = new Page();
			copyValueFromView(page,pageValue);
			
			space = spaceDAO.getByUname(spaceUname);
			page.setSpace(space);
	
			
			//??? CascadeType.PERSIST seems does not work well. I must explicit call save(), but in CascadeType.ALL, it is not necessary.
			pageProgressDAO.saveOrUpdate(page.getPageProgress());
			
			page.setVersion(1);
			//if there is draft existed before page first create, keep draft uuid as page uuid!!! 
			if(StringUtils.isBlank(pageValue.getPageUuid()))
				page.setPageUuid(WikiUtil.createPageUuid(spaceUname,spaceUname,spaceUname,repositoryService));
			else 
				page.setPageUuid(pageValue.getPageUuid());
	//			page.setUnixName(WikiUtil.getPageUnixname(newPageTitle));
			WikiUtil.setTouchedInfo(userReadingService, page);
			
			if(pageValue.getParent() != null && !StringUtils.isBlank(pageValue.getParent().getPageUuid())){
				Page parentPage = pageDAO.getCurrentByUuid(pageValue.getParent().getPageUuid());
				if(parentPage != null){
					//maybe parent page is deleted as well.
					page.setParent(parentPage);
					page.setLevel(parentPage.getLevel() + 1);
				}else{
					log.warn("page parent page does not exist. Page title is " 
							+ pageValue.getTitle() + ". Parent page uuid is "+ pageValue.getParent().getPageUuid());
				}
			}else
				//root page, such as home page
				page.setLevel(0);
		}
	
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//update page tags
		tagService.saveUpdatePageTag(page,pageValue.getTagString());
		
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// !!!! Important: this update attachments status must before  renderService.renderHTML(page)
		// otherwise, the drafts attachment won't render in {attach} or {gallery} macro....
		
		// Update page attachment status 
		//	remove this user's draft, does not use getDraft() then remove, because following error: 
		//		org.hibernate.HibernateException: Found shared references to a collection: com.edgenius.wiki.model.Page.tags
		try {
			User viewer = WikiUtil.getUser(userReadingService); 
			mergeAttahment(getPageAttachment(spaceUname, page.getPageUuid(), true, true, viewer), pageValue.getAttachments()
					,spaceUname, viewer, Draft.NONE_DRAFT);
			upgradeAttachmentStatus(spaceUname, page.getPageUuid(), page.getModifier(), Draft.NONE_DRAFT);
		} catch (RepositoryException e) {
			//not critical exception, just log:
			log.error("Update attachment status during saving page:" + page.getPageUuid() + " in space "+spaceUname + ".Error: " , e);
		} catch (RepositoryTiemoutExcetpion e) {
			log.error("Merge attachment saving page:" + page.getPageUuid() + " in space "+spaceUname+ ".Error: " , e);
		}
		
		List<RenderPiece> pieces = renderService.renderHTML(page);
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//update page links
		Set<PageLink> links = page.getLinks();
		if(links == null){
			links = new HashSet<PageLink>();
			page.setLinks(links);
		}
		
		List<PageLink> newLinks = new ArrayList<PageLink>();
		for (RenderPiece object : pieces) {
			if(object instanceof LinkModel){
				LinkModel ln = (LinkModel) object;
				//!!! Only linkToCreate and LinkToView support at moment(29/10/2008)
				if(ln.getType() == LinkModel.LINK_TO_CREATE_FLAG || ln.getType() == LinkModel.LINK_TO_VIEW_FLAG){
					if(StringUtils.length(ln.getLink()) > SharedConstants.TITLE_MAX_LEN){
						log.warn("Found invalid link(too long), skip it on PageLink table:" + ln.getLink()+" on page " + newPageTitle);
					}else{
						PageLink link = PageLink.copyFrom(page, ln);
						newLinks.add(link);
					}
				}
			}
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//update menu item for space
		//found other if current page has menuItem macro.
		
		
		MenuItem menuItem = null;
		//if this marked true, it will trigger the Shell update space request, so update Shell menu.
		page.setMenuUpdated(false);
		
		for (RenderPiece renderPiece : pieces) {
			if(renderPiece instanceof MacroModel && MenuItemMacro.NAME.equalsIgnoreCase(((MacroModel)renderPiece).macroName)){
				//copy value to MenuItem object
				menuItem = new MenuItem();
				HashMap<String, String> values = ((MacroModel)renderPiece).values;
				if(values != null){
					menuItem.setTitle(values.get(NameConstants.TITLE));
					menuItem.setOrder(NumberUtils.toInt(values.get(NameConstants.ORDER)));
					menuItem.setParent(values.get(NameConstants.PARENT_UUID));
				}
				menuItem.setPageTitle(page.getTitle());
				menuItem.setPageUuid(page.getPageUuid());
				//suppose only one menuItem in a page, if multiple, even also only use first of them.
				break;
			}
		}
		
		Set<MenuItem> menuItems = space.getSetting().getMenuItems();
		if(menuItem != null){
			//update menu list in current space setting
			if(menuItems == null){
				menuItems = new TreeSet<MenuItem>(new MenuItemComparator());
				space.getSetting().setMenuItems(menuItems);
			}else{
				//try to remove old value
				menuItems.remove(menuItem);
			}
			
			log.info("Menu item is add or update to page {}.", page.getPageUuid());
			menuItems.add(menuItem);
			settingService.saveOrUpdateSpaceSetting(space, space.getSetting());
			page.setMenuUpdated(true);
		}else if(menuItems != null){
			//need check if menu item is deleted from page if it had. Try to remove it.
			if(menuItems.remove(new MenuItem(page.getPageUuid()))){
				log.info("Menu item is removed from page {}.", page.getPageUuid());
				settingService.saveOrUpdateSpaceSetting(space, space.getSetting());
				page.setMenuUpdated(true);
			}
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//merge new links and existed links
		//delete non-existed
		for (Iterator<PageLink> iter = links.iterator();iter.hasNext();) {
			PageLink ln = iter.next();
			ln.setAmount(0);
			for (Iterator<PageLink> newIter =newLinks.iterator();newIter.hasNext();) {
				PageLink nlnk = newIter.next();
				if(ln.equals(nlnk)){
					ln.setAmount(ln.getAmount()+1);
					newIter.remove();
				}
			}
			if(ln.getAmount() == 0){
				iter.remove();
			}
		}
		if(newLinks.size() > 0){
			ArrayList<PageLink> linksList = new ArrayList<PageLink>(links);
			//there some new added links
			int idx;
			for (PageLink newLnk : newLinks) {
				if((idx =linksList.indexOf(newLnk)) != -1){
					PageLink ln = linksList.get(idx);
					ln.setAmount(ln.getAmount() +1);
				}else{
					linksList.add(newLnk);
				}
			}
			links.clear();
			links.addAll(linksList);
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//persistent
		page.setType(0);
		pageDAO.saveOrUpdate(page);
		
		//!!!NOTE: follow 3 lines code must after pageDAO.saveOrUpdate(),otherwise, if home page
		// is new page and contain link, method pageDAO.getCurrentByTitle() in LinkRenderHelper.exist() 
		//method will throw exception!!!(15/03/2007_my new car Honda Accord arrives in home:)
		if(pageValue.getNewPageType() == PageAttribute.NEW_HOMEPAGE){
			space.setHomepage(page);
			spaceDAO.saveOrUpdate(space);
		}
				
		//update cache only when a new page created or page title updated
		if(needRefreshCache)
			addPageCache(spaceUname, page);
	
		refreshAncestors(spaceUname, page);

		//page title change so change all page which refer link to this page.
		//only title change,oldTitle is not null.
		if((Global.AutoFixLinks & WikiConstants.AUTO_FIX_TITLE_CHANGE_LINK) > 0 && oldTitle != null){
			String newTitle = page.getTitle();
			try {
				fixLinksToTitle(spaceUname, oldTitle, newTitle);
			} catch (Exception e) {
				log.error("Unable to fix page title change on other pages content.",e);
			}
		}

		//remove all draft whatever auto or manual
		removeDraftInternal(spaceUname,page.getPageUuid(), page.getModifier(),Draft.NONE_DRAFT,false);

		//MOVE to PageIndexInterceptor
//		if(requireNotified)
//			sendNodification(page);
	
		log.info("Page saved " + newPageTitle + " on space "+ spaceUname + ". Page uid: " + newPageUid);

		PageEventListener[] listeners = eventContainer.getPageEventListeners(page.getPageUuid());
		if(listeners != null && listeners.length > 0){
			log.info("Page saved event dispatching...");
			for (PageEventListener listener : listeners) {
				try {
					listener.pageSaving(page.getPageUuid());
				} catch (PageEventHanderException e) {
					log.error("Page saved event processed failed on " + listener.getClass().getName(), e);
				}
			}
		}
		return page;

	}


	public Draft saveDraft(User user, Draft pageValue, int type) throws PageException{
		String spaceUname = pageValue.getSpace().getUnixName();
		
		log.info("Draft saving for page " + pageValue.getTitle() + " on space "+ spaceUname);
		Draft draft = null;
		User owner = WikiUtil.getUser(userReadingService);
		if(pageValue.getPageUuid() != null){
			//check whether current user has draft for this page
			draft = draftDAO.getDraftByUuid(spaceUname, pageValue.getPageUuid(), owner, type);
		}
		
		if(draft != null){
			//update current page with new value
			copyValueFromView(draft,pageValue);
			draft.setType(type);
//			draft.setUnixName(WikiUtil.getPageUnixname(pageValue.getTitle()));
			WikiUtil.setTouchedInfo(userReadingService, draft);
			draftDAO.saveOrUpdate(draft);
		}else{
			//a new page first time save:
			draft = new Draft();
			copyValueFromView(draft,pageValue);
			Space space = spaceDAO.getByUname(spaceUname);
			draft.setSpace(space);
			draft.setType(type);
			//draft will remember current version, later, if it will save to Page, it also need do version check.
			draft.setVersion(pageValue.getVersion());
			//if there is page existed before draft first create, keep page uuid as page uuid!!! 
			if(StringUtils.isBlank(pageValue.getPageUuid()))
				draft.setPageUuid(WikiUtil.createPageUuid(spaceUname,spaceUname,spaceUname,repositoryService));
			else 
				draft.setPageUuid(pageValue.getPageUuid());
//			draft.setUnixName(WikiUtil.getPageUnixname(pageValue.getTitle()));
			WikiUtil.setTouchedInfo(userReadingService, draft);
			//root page, such as home page
			if(pageValue.getParent() != null && pageValue.getParent().getPageUuid() != null){
				Page parentPage = pageDAO.getCurrentByUuid(pageValue.getParent().getPageUuid());
				if(parentPage != null){
					draft.setParent(parentPage);
					draft.setLevel(parentPage.getLevel() + 1);
				}else{
					log.warn("Draft page parent page does not exist. Draft title is " 
							+ pageValue.getTitle() + ". Parent page uuid is "+ pageValue.getParent().getPageUuid());
				}
			}else
				draft.setLevel(0);
//          Since version 0.3, page, draft are split into 2 tables. So tags, many-to-many relation,  becomes complex. 
			//and it is most useless, if saving tag also with draft, so just comment this piece so far.
//			Set<PageTag> tags = draft.getTags();
//			if(tags == null){
//				tags = new HashSet<PageTag>();
//				draft.setTags(tags);
//			}
//			Set<PageTag> newTags = pageValue.getTags();
//			if(newTags != null){
//				for (PageTag tag : newTags) {
//					PageTag t = pageTagDAO.getByName(spaceUname,tag.getName());
//					if(t == null){
//						WikiUtil.setTouchedInfo(userService, tag);
//						tag.setSpace(space);
//						pageTagDAO.saveOrUpdate(tag);
//						tags.add(tag);
//					}else{
//						tags.add(t);
//					}
//				}
//			}
			if(draft.getPageProgress() != null)
				pageProgressDAO.saveOrUpdate(draft.getPageProgress());
			draftDAO.saveOrUpdate(draft);
		}
		//manual drafted saved, auto draft removed. But in reverse, auto draft can not replace manual draft anyway.
		try{
			//IMPORTANT: can not re-order following 3 methods
			mergeAttahment(getPageAttachment(spaceUname, draft.getPageUuid(),true, true, owner), pageValue.getAttachments(),spaceUname,owner, type);
			if(type == Draft.MANUAL_DRAFT){
				upgradeAttachmentStatus(spaceUname, draft.getPageUuid(), draft.getModifier(), Draft.MANUAL_DRAFT);
				removeDraftInternal(spaceUname, draft.getPageUuid(), draft.getModifier(), Draft.AUTO_DRAFT,false);
			}
		} catch (RepositoryException e) {
			//not critical exception, just log:
			log.error("Remove draft failed during saving draft:" + draft.getPageUuid() + " in space "+spaceUname + ".Error " ,e);
		} catch (RepositoryTiemoutExcetpion e) {
			log.error("Merge draft failed during saving draft:" + draft.getPageUuid() + " in space "+spaceUname + ".Error " ,e);
		}
		
		//TODO: need confirm draft's permission setting.
		renderService.renderHTML(draft);
		//does not need set pagelink
		
		log.info("Draft saved for page " + pageValue.getTitle() + " on space "+ spaceUname + ". Page uid: " + draft.getUid());
		return draft;
	}
	public List<History> getHistoryPages(String spaceUname,String pageUuid, int startVer, int returnCount, Date touchedDate){
		
		return historyDAO.getByUuid(pageUuid,startVer, returnCount, touchedDate);
	}

	//JDK1.6 @Override
	public boolean existCurrentPageByTitle(String spaceUname, String pageTitle) {
		if(StringUtils.isBlank(pageTitle) ||StringUtils.isBlank(spaceUname))
			return false;
		
		return pageDAO.getCurrentPageByTitle(spaceUname, pageTitle) != null;
	}
	/* (non-Javadoc)
	 * @see com.edgenius.wiki.service.PageService#getPageByUnixName(java.lang.String)
	 */
	public Page getCurrentPageByTitleWithoutSecurity(String spaceUname,String pageTitle, boolean render) throws SpaceNotFoundException {
		if(StringUtils.isBlank(pageTitle) ||StringUtils.isBlank(spaceUname))
			return null;
		Page page = pageDAO.getCurrentPageByTitle(spaceUname, pageTitle);
		//page not found
		if(page == null){
			log.error("Page not found by spaceUname (" + spaceUname + ") and title (" + pageTitle + ")");
			//need check if space exist, if space is available just return null, it means "Page Not Found" page will show 
			//and allow user to create new page if he/she has permission. If even the space does not exist, then just throw 
			//exception
			if(spaceDAO.getByUname(spaceUname) == null){
				throw new SpaceNotFoundException(spaceUname);
			}
			return null;
		}
		if(render){
			renderService.renderHTML(page);
			refreshAncestors(spaceUname,page);
		}
		return page; 
	}
	public Page getCurrentPageByTitle(String spaceUname,String pageTitle) throws SpaceNotFoundException {
		return getCurrentPageByTitleWithoutSecurity(spaceUname, pageTitle, true);
	}

	public Page getCurrentPageByUnixName(String spaceUname, String unixName) {
		return pageDAO.getCurrentByUnixName(spaceUname,unixName);
	}

	public Page getCurrentPageByUuid(String pageUuid, boolean render){
		if(!render){
			return pageDAO.getCurrentByUuid(pageUuid);
		}else{
			Page page = pageDAO.getCurrentByUuid(pageUuid);
			if(page != null){
				String spaceUname = page.getSpace().getUnixName();
				renderService.renderHTML(page);
				refreshAncestors(spaceUname,page);
			}
			return page; 
		}
	}
	public Page getCurrentPageByUuid(String pageUuid){
		return pageDAO.getCurrentByUuid(pageUuid);
	}
	/* 
	 * @see com.edgenius.wiki.service.PageService#getPage(java.lang.Integer)
	 */
	public Page getPage(Integer pageUid) {
		return pageDAO.get(pageUid);
	}
	
	public History getHistoryObject(Integer historyUid) {
		if(historyUid == null)
			return null;
		
		return historyDAO.get(historyUid);
	}
	public History getHistory(Integer historyUid) {
		
		History history = historyDAO.get(historyUid);
		String spaceUname = history.getSpace().getUnixName();
		renderService.renderHTML(history);
		
		refreshAncestors(spaceUname, history);
		return history; 
	}
	public History getHistoryByVersion(String pageUuid, Integer version){
		return historyDAO.getVersionByUuid(version, pageUuid);
	}

	public List<AbstractPage> getPageAncestors(String spaceUname, String pageUuid){
		if(StringUtils.isBlank(pageUuid)){
			//could be home page,
			return null;
		}
		Page page = pageDAO.getCurrentByUuid(pageUuid);
		if(page == null){
			log.warn("Unable find page according to uuid " + pageUuid + " when try to get page ancestors.");
			return null;
		}
		
		refreshAncestors(spaceUname, page);
		return page.getAncestorList();
	}
	
	public Draft getDraft(User user, Integer draftUid) {
		if(draftUid == null)
			return null;
		
		Draft draft = draftDAO.get(draftUid);
		if(draft != null){
			String spaceUname = draft.getSpace().getUnixName();
			refreshAncestors(spaceUname, draft);
		}
		return draft; 
		
	}
	public Page getHomepage(String spaceUname) {
		Space space = spaceDAO.getByUname(spaceUname);
		if(space == null){
			log.warn("Try get homepage from non-exist space failed {}", spaceUname);
			return null;
		}
			
		Page page = space.getHomepage();
		//home page deleted!
		if(page == null)
			return page;
		
		renderService.renderHTML(page);
		
		//refresh navbar info:only home page itself
		List<AbstractPage> parentList = new ArrayList<AbstractPage>();
		parentList.add(page);
		page.setAncestorList(parentList);
		
		return page;
	}

	public List<FileNode> uploadAttachments(String spaceUname,  String pageUuid, List<FileNode> files,boolean compareMd5Digest) 
		throws RepositoryException, RepositoryTiemoutExcetpion, RepositoryQuotaException {
		ITicket ticket = repositoryService.login(spaceUname,spaceUname, spaceUname);
		
		List<FileNode> checkedFiles = new ArrayList<FileNode>();
		for (FileNode file : files) {
			//this method also update file.setNodeUuid(); and setVersion()
			//doesn't force save if compareMd5Digest is true and MD5 digests are equals - return null if digests are equals.
			List<FileNode> nodes = repositoryService.saveFile(ticket,file, compareMd5Digest, compareMd5Digest);
			if(nodes != null){
				User user = userReadingService.getUserByName(file.getCreateor());
				//normally, it only has one except bulk upload....
				for (FileNode node : nodes) {
					node.setUserFullname(user.getFullname());
					checkedFiles.add(node);
				}
			}
		}
		if(checkedFiles.size() > 0)
			touchService.touchPage(pageUuid);
		
		return checkedFiles;
	}


	public FileNode removeAttachment(String spaceUname, String pageUuid, String nodeUuid, String nodeVersion)
		throws RepositoryException, RepositoryTiemoutExcetpion {
		
		ITicket ticket = repositoryService.login(spaceUname,spaceUname ,spaceUname);
		FileNode node = repositoryService.removeFile(ticket,nodeUuid,nodeVersion);
		touchService.touchPage(pageUuid);
		
		return node;
	}
	
	@SuppressWarnings("unchecked")
	public List<Page> getPageTree(String spaceUname){
		if(Space.SYSTEM_SPACEUNAME.equals(spaceUname)){
			//for system space, return empty.
			return new ArrayList<Page>();
		}
		
		Element element = pageTreeCache.get(spaceUname);
		if(element == null){
			Set<Page> sortedSet = new TreeSet<Page>();
			List<Page> list = pageDAO.getTree(spaceUname);
			log.info("Init page tree for " + spaceUname + " in size " + list.size());
			
			if(list != null && list.size() > 0){
				//because parent of page on list only has Uid, PageComparator() need parent title, uuid refer link. 
				//here to build up page's parent refer link(parent's parent's parent etc.)
				Space spaceModel = new Space();
				spaceModel.setUnixName(spaceUname);
				for (Page page : list) {
					//fill spaceUname
					page.setSpace(spaceModel);
					
					if(page.getParent() == null)
						continue;
					Integer pUid = page.getParent().getUid();
					//loop all page and find parent.
					boolean found = false;
					for (Page parent : list) {
						if(parent == page)
							continue;
						if(parent.getUid().equals(pUid)){
							page.setParent(parent);
							found = true;
							break;
						}
					}
					if(!found)
						AuditLogger.error("Date integrity error. Page has parent is not in space " + spaceUname + " .PageUid: " 
								+ page.getUid() + ". ParentUid: " + pUid);
				}
				//parent refer link built, now PageComparator() works
				//always put homepage in the first node, so check here
				Space space = spaceDAO.getByUname(spaceUname);
				Page home = space.getHomepage();
				sortedSet  = new TreeSet<Page>(new PageComparator(home != null?home.getPageUuid():null));
				sortedSet.addAll(list);
				
				log.info("Page tree " + spaceUname + " put into cache " + sortedSet.size());
				//put into cache
				element = new Element(spaceUname,sortedSet);
				pageTreeCache.put(element);
			}				
			return list;
		}else
			return new ArrayList<Page>((Set<Page>)element.getValue());
		
	}
	/*
	 * Copy page, if there is same title page exist in target space(maybe in same space). A new title will given (Copy of ...).
	 */
	public Page copy(String fromSpaceUname,String fromPageUuid, String toSpaceUname, String toParentUuid, boolean withChildren) throws PageException{
		Page page = pageDAO.getCurrentByUuid(fromPageUuid);
		if(page == null){
			log.error("Unable find page for copy " + fromPageUuid);
			return null;
		}
		if(page.isRemoved()){
			log.error("User try to copy from a removed page " + page.getTitle() + " on space " + page.getSpace().getUnixName());
			return null;
		}
		
		Integer fromPageUid = page.getUid();
		
		Page toParent = null;
		if(toParentUuid != null){
			toParent = pageDAO.getCurrentByUuid(toParentUuid);
			if(!StringUtils.equalsIgnoreCase(toParent.getSpace().getUnixName(),toSpaceUname)){
				log.warn("Target space and page parent are different spaces. Set parent as null(root)");
				log.warn("To SpaceUname:" + toSpaceUname + ". To ParentUuid:" + toParentUuid + " of space " + toParent.getSpace().getUnixName());
				toParent = null;
			}
		}
		
		if(!StringUtils.equalsIgnoreCase(page.getSpace().getUnixName(),fromSpaceUname)){
			String error = "Given space name is inconsistent with pageUuid during copy." + page.getSpace().getUnixName() 
			+ " is pageUuid's space, but given is " + fromSpaceUname;
			log.error(error);
			throw new PageException(error);
		}
		
		log.info("Page " + fromPageUuid + " is going to copy from space " + fromSpaceUname + " to " 
					+ toSpaceUname + (toParent == null?" root node":", parent title is " + toParent.getTitle()));
		
		//copy to different space
		//TODO: avoid copy to its owned children, deadloop, same with move.
		
		Page newPage = (Page) page.clone();
		newPage.setUid(null);
		if(newPage.getContent() != null)
			newPage.getContent().setUid(null);
		
		String newUuid = WikiUtil.createPageUuid(toSpaceUname, toSpaceUname, toSpaceUname, repositoryService);
		Space toSpace = spaceDAO.getByUname(toSpaceUname);
		//set new space, progress etc.
		newPage.setSpace(toSpace);
		newPage.setPageUuid(newUuid);
//				TODO: how to handle draft? some use won't have permission to target space 
//				if(newPage.isDraft())
		String title = page.getTitle();
		title = getIdeniticalTitle(toSpaceUname, title, "Copy of ");
		newPage.setTitle(title);
		
		newPage.setParent(toParent);
		newPage.setLevel(toParent == null?0:toParent.getLevel()+1);
		
		//try to duplicated tags
		List<PageTag> tags = newPage.getTags();
		List<PageTag> newTags = new ArrayList<PageTag>();
		if(tags != null && tags.size() > 0){
			if(StringUtils.equalsIgnoreCase(toSpaceUname,fromSpaceUname)){
				//same space, tag already exist, then just use persistent tag replace transient tag
				//during clone: tag Uid is removed, here just simply copy back original tag
				newPage.setTags(page.getTags());
			}else{
				//different spaces, need check tag one by one, if not exist, create new one
				for (PageTag tag : tags) {
					PageTag pTag = tagService.getPageTagByName(toSpaceUname, tag.getName());
					if(pTag != null){
						newTags.add(pTag);
					}else{
						//create new
						tag.setUid(null);
						WikiUtil.setTouchedInfo(userReadingService, tag);
						tag.setSpace(toSpace);
						tag.setPages(null);
						tagService.internalSavePageTag(tag);
						newTags.add(tag);
					}
				}
				newPage.setTags(newTags);
			}
		}
		try {
			//copy attachment 
			ITicket fromTicket = repositoryService.login(fromSpaceUname, fromSpaceUname, fromSpaceUname);
			ITicket toTicket = repositoryService.login(toSpaceUname, toSpaceUname, toSpaceUname);
			repositoryService.copy(fromTicket, RepositoryService.TYPE_ATTACHMENT, fromPageUuid
					, toTicket,RepositoryService.TYPE_ATTACHMENT,newPage.getPageUuid());
		} catch (RepositoryException e) {
			throw new PageException("Copy page failed on exception: " +e);
		} catch (RepositoryTiemoutExcetpion e) {
			throw new PageException("Copy page failed on exception: " +e);
		}

		//copy its children if require
		if(withChildren){
			List<Page> children = pageDAO.getChildren(page.getUid());
			for (Page child : children) {
				//recursive call to retrieve all child to copy
				copy(fromSpaceUname,child.getPageUuid(), toSpaceUname, page.getPageUuid(), withChildren);
			}
		}
		
		//need persist page progress first, which is clone from old page but with null uid.
		PageProgress pageProgress = newPage.getPageProgress();
		pageProgress.setUid(null);
		pageProgressDAO.saveOrUpdate(pageProgress);

		//update page links if space is different
		if((Global.AutoFixLinks & WikiConstants.AUTO_FIX_COPY_LINK) > 0 && !StringUtils.equals(fromSpaceUname, toSpaceUname)){
			try{
				fixLinksToSpace(newPage, fromSpaceUname,toSpaceUname);
			} catch (Exception e) {
				log.error("Unable to fix page space change on other pages content.",e);
			}
		}
		Set<PageLink> links = newPage.getLinks();
		if(links != null && links.size() > 0){
			//any pageLink will create a new record in database
			for (PageLink link: links) {
				link.setUid(null);
			}
		}
		//persist
		WikiUtil.setTouchedInfo(userReadingService, newPage);
		pageDAO.saveOrUpdate(newPage);
		//only save current page, skip history 
		addPageCache(toSpaceUname, newPage);
		
		
		commentService.copyComments(fromPageUid, newPage);
		//copy history
		List<History> histories = historyDAO.getByUuid(page.getPageUuid());
		if(histories != null){
			for (History history : histories) {
				//if not current page(page histories), simply copy history page : no attachment, no children handle
				History newHistory = (History) history.clone();
				newHistory.setUid(null);
				if(newHistory.getContent()!=null)
					newHistory.getContent().setUid(null);
				//set new space, progress etc.
				newHistory.setSpace(toSpace);
				newHistory.setPageUuid(newUuid);
				WikiUtil.setTouchedInfo(userReadingService, newPage);
				historyDAO.saveOrUpdate(newHistory);
			}
		}
			
			//DON'T Copy favorite/watch: permission is problem
			//permission need be checked : if this user allow access that space?
//				List<UserPageMark> favorites = userPageDAO.getByPageUid(page.getUid());
//				if(favorites != null){
//					for (UserPageMark favorite : favorites) {
//						favorite.getUser()
//						favorite.setUid(null);
//						favorite.setPage(newPage);
//						userPageDAO.saveOrUpdate(favorite);
//					}
//				}

		
		//do all necessary things for a new page
		renderService.renderHTML(newPage);
		refreshAncestors(toSpaceUname, newPage);
		return newPage;
	}

	/*
	 * If in same space, only update current page parent(history pages' parent is null). Otherwise, do copy then remove.
	 */
	public Page move(String fromSpaceUname, String fromPageUuid, String toSpaceUname, String toParentUuid, boolean withChildren) throws PageException {
		Page currPage = pageDAO.getCurrentByUuid(fromPageUuid);
		if(currPage == null){
			log.error("Unable find page for move " + fromPageUuid);
			return null;
		}
		if(currPage.isRemoved()){
			log.error("User try to move from a removed page " + currPage.getTitle() + " on space " + currPage.getSpace().getUnixName());
			return null;
		}
		
		
		Page toParent = null;
		if(toParentUuid != null){
			toParent = pageDAO.getCurrentByUuid(toParentUuid);
			if(!StringUtils.equalsIgnoreCase(toParent.getSpace().getUnixName(),toSpaceUname)){
				log.warn("Target space and page parent are different spaces. Set parent as null(root)");
				log.warn("To SpaceUname:" + toSpaceUname + ". To ParentUuid:" + toParentUuid + " of space " + toParent.getSpace().getUnixName());
				toParent = null;
			}
		}
		
		if(!StringUtils.equalsIgnoreCase(currPage.getSpace().getUnixName(),fromSpaceUname)){
			String error = "Given space name is inconsistent with pageUuid during moving page." + currPage.getSpace().getUnixName() 
			+ " is pageUuid's space, but given is " + fromSpaceUname;
			log.error(error);
			throw new PageException(error);
		}
		
		
		log.info("Page UUID("+fromPageUuid+ ") is going to move from space " + fromSpaceUname + " to " + toSpaceUname);
		
		if(StringUtils.equalsIgnoreCase(toSpaceUname,fromSpaceUname)){
			if(currPage.equals(toParent) || currPage.getParent().equals(toParent)){
				//does not move to same parent or itself as parent case.
				log.info("Same space moving, either same parent or itself as parent , do nothing. Page title is " 
						+ currPage.getTitle() + ". ToParrent title is " + (toParent==null?"":toParent.getTitle()));
			}else{
				log.info("Same space moving inside " + toSpaceUname);
				log.info("Moving source page title is " + currPage.getTitle());
				Page oldParent = currPage.getParent();
				currPage.setParent(toParent);
				currPage.setLevel(toParent == null?0:toParent.getLevel()+1);
				pageDAO.saveOrUpdate(currPage);
				
				
				// reset all directly children (son/daughter:) parentUid to
				// removed page's parent
				List<Page> children = pageDAO.getChildren(currPage.getUid());
				for (Page child : children) {
					removePageCache(toSpaceUname, child);
					child.setParent(oldParent);
					child.setLevel(oldParent == null ? 0 : oldParent.getLevel() + 1);
					pageDAO.saveOrUpdate(child);
					addPageCache(toSpaceUname, child);
				}
				
				//do all necessary things for a new page render: 
				//update pageTree cache, update ancestors, render page
				removePageCache(toSpaceUname,currPage);
				addPageCache(toSpaceUname, currPage);
			}			
			//for render correctly:
			renderService.renderHTML(currPage);
			refreshAncestors(toSpaceUname, currPage);
		}else{
			currPage = copy(fromSpaceUname, fromPageUuid, toSpaceUname, toParentUuid, withChildren);
			removePage(fromSpaceUname, fromPageUuid, withChildren, true);
		}
	
		return currPage;
	}
	/*
	 * Return removed page object
	 * NOTE:withChildren case never be tested!!!! It assume not works.
	 */
	public Page removePage(String spaceUname, String pageUuid, boolean withChildren , boolean permanent) throws PageException {
		//this will return all page with such uuid whatever it has removed mark or not
		Page page = pageDAO.getByUuid(pageUuid);
		if(page == null){
			return null;
		}
		
		log.info("Page UUID({})-Title({}) is going to removed from space {}", new String[]{pageUuid, page.getTitle(), spaceUname} );
		
		spaceUname = page.getSpace().getUnixName();

		// remove attachment
		if (permanent) {
			try {
				ITicket fromTicket = repositoryService.login(spaceUname, spaceUname, spaceUname);
				repositoryService.removeIdentifier(fromTicket, RepositoryService.TYPE_ATTACHMENT, page.getPageUuid());
			} catch (RepositoryException e) {
				log.error("Remove page attachment failed " , e);
			} catch (RepositoryTiemoutExcetpion e) {
				log.error("Remove page attachment failed " , e);
			}
		}
		// remove its children if require
		List<Page> children = pageDAO.getChildren(page.getUid());
		if (withChildren) {
			for (Page child : children) {
				// recursive call to retrieve all child to copy
				removePage(spaceUname, child.getPageUuid(), withChildren, permanent);
			}
		} else {
			// reset all directly children (son/daughter:) parentUid to
			// removed page's parent
			Page parent = page.getParent();
			for (Page child : children) {
				// remove child from cache first, then add it back after
				// its parent update is done.
				removePageCache(spaceUname, child);
				child.setParent(parent);
				child.setLevel(parent == null ? 0 : parent.getLevel() + 1);
				pageDAO.saveOrUpdate(child);
				addPageCache(spaceUname, child);
			}
		}
		
		// check if this page is homepage, if so, reset space home page as null
		Space space = page.getSpace();
		Page home = space.getHomepage();
		boolean removeHomepage = false;
		if (home != null && StringUtils.equals(page.getPageUuid(), home.getPageUuid())) {
			space.setHomepage(null);
			spaceDAO.saveOrUpdate(space);
			removeHomepage = true;
			log.info("Space home page removed: " + spaceUname);
		}
		
		if (!permanent) {
			if(removeHomepage)
				page.setRemoved(Page.REMOVED_HOMEPAGE);
			else
				page.setRemoved(Page.REMOVED);
			pageDAO.saveOrUpdate(page);
			// try to remove this page from cache: because the page.isRemoved() return true, it will removed from Cache.
			addPageCache(spaceUname, page);
		} else {
			page.setParent(null);
			//remove comments:must before pageDAO.removeObject().
			commentService.removePageComments(page.getUid());
			
			WikiUtil.setTouchedInfo(userReadingService, page);
			PageProgress progress = page.getPageProgress();
			pageDAO.removeObject(page);
			
			//after pageDAO.removeObject(page);
			if(progress != null)
				pageProgressDAO.removeObject(progress);
			
			//remove page history
			List<History> histories = historyDAO.getByUuid(page.getPageUuid());
			if(histories != null){
				for (History history : histories) {
					historyDAO.removeObject(history);
				}
			}

		}
		// remove UserPageMark:favorite and watch
		userPageDAO.removeByPageUid(page.getUid());

		log.info("Page removed successed. Title  " + page.getTitle());

		
		// this page exist in space, already removed, need remove from cache as well
		removePageCache(spaceUname, page);
		securityService.removeResource(pageUuid);
		
		
		PageEventListener[] listeners = eventContainer.getPageEventListeners(page.getPageUuid());
		if(listeners != null && listeners.length > 0){
			log.info("Page saved event dispatching...");
			for (PageEventListener listener : listeners) {
				try {
					listener.pageRemoving(page.getPageUuid(), permanent);
				} catch (PageEventHanderException e) {
					log.error("Page saved event processed failed on " + listener.getClass().getName(), e);
				}
			}
		}
		
		return page;
		
	}

	public String restorePageCheck(String restoredPageUuid) {
		//this will return page whatever if it is marked as removed
		Page page = pageDAO.getByUuid(restoredPageUuid);
		
		//if any unexpected case, just silence return true, ask restorePage() to throw exception.
		if(page == null )
			return null;
		
		if(!page.isRemoved()){
			return null;
		}
		
		String title = page.getTitle();
		//try to get an new title if space already has same title page exist
		title = getIdeniticalTitle(page.getSpace().getUnixName(), title, "Restored of ");
		
		int status = SharedConstants.RESTORE_NORMAL;
		if(page.getRemoved()==Page.REMOVED_HOMEPAGE){
			//OK, the resorted page was home page, need check if current homepage has created again?
			Page homepage = page.getSpace().getHomepage();
			if(homepage != null){
				status = SharedConstants.RESTORE_HOMEPAGE_EXIST;
			}else{
				status = SharedConstants.RESTORE_HOMEPAGE_NO_EXIST;
			}
		}
		return status+title;
		
	}

	public Page restorePage(String spaceUname, String pageUuid, boolean homepage, boolean withHistory) throws PageException{
		//this will return all page with such uuid whatever it has removed mark or not
		Page page = pageDAO.getByUuid(pageUuid);
		if(page == null)
			return null;
		
		log.info("Page UUID(" + pageUuid + ") is going to restore from space " + spaceUname );
		
		if(!page.isRemoved()){
			String msg = "page was not removed cleanly. Page " + page.getPageUuid() + "  version " + page.getVersion() + " restore failed";
			log.error(msg);
			throw new PageException(msg);
		}
		// need check if the restore page parent if exist or not, if no, just simple set as null
		//try to get available parent, if failed, then set it as null. This parent will update to all version of this page.
		Page existParent = null;
		Page parent = page.getParent();
		if(parent != null){
			existParent = pageDAO.getCurrentByUuid(parent.getPageUuid());
			if(existParent == null){
				log.info("Restored page original parent does not exist. The original parent page UUID is " + parent.getPageUuid());
			}
		}
		
		page.setParent(existParent);
		WikiUtil.setTouchedInfo(userReadingService, page);
		String title = page.getTitle();
		title = getIdeniticalTitle(spaceUname, title, "Restored of ");
		if(title != null)
			page.setTitle(title);
		else{
			AuditLogger.error("Unexpected case: can not get back page title in restore.");
			throw new PageException("Unexpected case: can not get back page title in restore.");
		}
		
		//MUST: setRemoved() after title check, otherwise, the title should exist because it marked itself removed as false.
		page.setRemoved(Page.REMOVE_FLAG_NO);
		pageDAO.saveOrUpdate(page);
		
		if(!withHistory){
			//remove all history if it need not histories
			List<History> histories = historyDAO.getByUuid(page.getPageUuid());
			for (History history : histories) {
				historyDAO.removeObject(history);
			}
		}
		if(homepage){
			//restore page is home page, need update space object to link
			Space space = spaceDAO.getByUname(spaceUname);
			space.setHomepage(page);
			spaceDAO.saveOrUpdate(space);

		}
		
		log.info("Page retore successed. Page uuid " + page.getPageUuid() + " Page version " + page.getVersion());
		// well, update page cache
		addPageCache(spaceUname, page);
		return page;
		
	}

	//JDK1.6 @Override
	public Page restoreHistory(String spaceUname, String currPageUuid, int version) 
			throws PageException,DuplicatedPageException, PageSaveTiemoutExcetpion {
		
		Page page = pageDAO.getCurrentByUuid(currPageUuid);
		if(page == null){
			throw new PageException("Page not found for uuid " + currPageUuid  + " on space " + spaceUname);
		} 
		History history = historyDAO.getVersionByUuid(version, page.getPageUuid());
		if(history == null){
			throw new PageException("Histroy not found for verison "+version+ ". Page current title " 
					+ page.getTitle() + " on space " + spaceUname);
		}
		
		Page newPage = null;
		try {
			newPage = history.cloneToPage();
			//OK, to avoid duplicated title exception, use current page title to replace history one.
			newPage.setTitle(page.getTitle());
			
			//this is not very necessary, as for performance reason, give Uid, then in savePage()method will use key to get page...
			newPage.setUid(page.getUid());
			newPage = savePage(newPage,WikiConstants.NOTIFY_NONE,true);
		} catch (VersionConflictException e) {
			//this exception won't happen as checkVersion flag is false
			throw new PageException(e);
		}
		return newPage;
	}

	
	public List<UserPageMark> getPageMarks(User user, Page page) {
		
		return userPageDAO.getByUserAndPage(user,page);
	}
	
	public List<Draft> hasDraft(String spaceUname, String pageTitle, User owner) {
		
		return draftDAO.hasDraftByTitle(spaceUname, pageTitle, owner);
	}
	
	
	public Draft removeDraft(User user, String spaceUname, String pageUuid, int type) throws PageException {
		return removeDraftInternal(spaceUname, pageUuid, user, type, true);
	}

	public List<Draft> getDraftPages(User user, int type) {
		return draftDAO.getDrafts(null, user.getUsername(), type);
	}
	public List<Page> getFavoritePages(String username) {
		List<Page> pages = new ArrayList<Page>();
		List<UserPageMark> marks = userPageDAO.getFavorites(null, username);
		if(marks != null)
			for (UserPageMark mark : marks) {
				pages.add(mark.getPage());
			}
		return pages;
	}
	public List<Page> getWatchedPages(String username) {
		List<Page> pages = new ArrayList<Page>();
		List<UserPageMark> marks = userPageDAO.getWatched(null, username);
		if(marks != null)
			for (UserPageMark mark : marks) {
				pages.add(mark.getPage());
			}
		return pages;
	}
	
	@Transactional(readOnly=true)
	public List<Page> getPinTopPages(Integer spaceUid, String spaceUname, User viewer){
		List<Page> list = pageDAO.getPinTopPagesInSpace(spaceUid);
		
		if(list != null && viewer != null){
			//filter out the page which is not allow viewer to read
			for (Iterator<Page> iter = list.iterator();iter.hasNext();) {
				Page page = iter.next();
				if(!securityService.isAllowPageReading(page.getSpace().getUnixName(), page.getPageUuid(), viewer))
					iter.remove();
			}
		}
		
		
		return list;
	}
	public List<Page> getUserUpdatedPagesInSpace(String spaceUname, String username, int returnNum, User viewer) {
		User user = userReadingService.getUserByName(username);
		List<Page> list = pageDAO.getUserUpdatedPagesInSpace(spaceUname,user,returnNum);
		
		if(list != null && viewer != null){
			//filter out the page which is not allow viewer to read
			for (Iterator<Page> iter = list.iterator();iter.hasNext();) {
				if(!securityService.isAllowPageReading(spaceUname, iter.next().getPageUuid(), viewer))
					iter.remove();
			}
		}
		return list;
	}

	public List<Page> getUserAllContributedPages(String username, int limit, User viewer) {
		User user = userReadingService.getUserByName(username);
		List<Page> list = pageDAO.getUserContributedPages(user,limit);
		if(list != null && viewer != null){
			//filter out the page which is not allow viewer to read
			for (Iterator<Page> iter = list.iterator();iter.hasNext();) {
				Page page = iter.next();
				if(!securityService.isAllowPageReading(page.getSpace().getUnixName(), page.getPageUuid(), viewer))
					iter.remove();
			}
		}
		
		return list;

	}
	public List<History> getUserAllContributedHistories(String username, User viewer){
		User user = userReadingService.getUserByName(username);
		List<History> list = historyDAO.getUserContributedHistories(user);
		if(list != null && viewer != null){
			//filter out the page which is not allow viewer to read
			for (Iterator<History> iter = list.iterator();iter.hasNext();) {
				History page = iter.next();
				if(!securityService.isAllowPageReading(page.getSpace().getUnixName(), page.getPageUuid(), viewer))
					iter.remove();
			}
		}
		
		return list;

	}
	//JDK1.6 @Override
	public List<Page> getPagesInSpace(String spaceUname, Date touchedDate,int returnNum, User viewer) {
		List<Page> list = pageDAO.getPagesInSpace(spaceUname,touchedDate, returnNum);
		if(list != null && viewer != null){
			//filter out the page which is not allow viewer to read
			for (Iterator<Page> iter = list.iterator();iter.hasNext();) {
				Page page = iter.next();
				if(!securityService.isAllowPageReading(page.getSpace().getUnixName(), page.getPageUuid(), viewer))
					iter.remove();
			}
		}
		return list;
	}

	//JDK1.6 @Override
	public List<String> getPagesUuidInSpace(String spaceUname, User viewer) {
		//TODO: will get from TreeCache or reading cache?
		List<String> list = pageDAO.getPagesUuidInSpace(spaceUname);
		if(list != null && viewer != null){
			//filter out the page which is not allow viewer to read
			for (Iterator<String> iter = list.iterator();iter.hasNext();) {
				String uuid = iter.next();
				if(!securityService.isAllowPageReading(spaceUname, uuid , viewer))
					iter.remove();
			}
		}
		return list;
	}

	/**
	 * Get page method will call this method to fill attachment.
	 * Save/saveDraft does not call, because it is possible new attachment submit once after save/savedraft done.
	 * E.g, when user choose several upload files and click save button, or, pageUuid is null, attachment auto saving timer
	 * will call saveDraft() first.
	 * 
	 * @param pageUuid
	 * @return
	 * @throws RepositoryException 
	 */
	public List<FileNode> getPageAttachment(String spaceUname,String pageUuid, boolean withHistory, boolean withDraft, User viewer) throws RepositoryException{
		//just compare nodeUuid(file name could change, cannot comparable), if same, compare nodeVersion then.
		Set<FileNode> set = new TreeSet<FileNode>(new Comparator<FileNode>(){
			public int compare(FileNode o1, FileNode o2) {
				if(o1.getNodeUuid().equals(o2.getNodeUuid())){
					//from large to small
					return o2.getVersion().compareTo(o1.getVersion());
				}else
					return o1.getNodeUuid().compareTo(o2.getNodeUuid());
			}
			
		});
		ITicket ticket = repositoryService.login(spaceUname, spaceUname, spaceUname);
		//don't get file stream, only get necessary description information
		List<FileNode> atts = repositoryService.getAllIdentifierNodes(ticket,RepositoryService.TYPE_ATTACHMENT, pageUuid,false);
		
		for (FileNode node : atts) {
			if(node.getStatus() > 0){
				if(!withDraft){
					continue;
				}else if(viewer == null || !StringUtils.equalsIgnoreCase(node.getCreateor(),viewer.getUsername())){
					//viewer is anonymous or the attachment is uploaded by this viewer, skip
					continue;
				}
			}
			String username = node.getCreateor();
			User user = userReadingService.getUserByName(username);
			//pass back user fullname
			node.setUserFullname(user.getFullname());
			set.add(node);
		}
		if(!withHistory){
			//remove history version
			List<String> nodeUuids = new ArrayList<String>();
			for (Iterator<FileNode> iter = set.iterator();iter.hasNext();) {
				FileNode node = iter.next();
				String uuid = node.getNodeUuid();
				if(nodeUuids.contains(uuid)){
					iter.remove();
					continue;
				}
				nodeUuids.add(uuid);
			}
		}
		return new ArrayList<FileNode>(set);
	}
	
	public FileNode updateAttachmentMetaData(String spaceUname,String pageUuid,  String nodeUuid, String name, String desc)
		throws RepositoryException{
		
		ITicket ticket = repositoryService.login(spaceUname, spaceUname, spaceUname);
		return repositoryService.updateMetaData(ticket, nodeUuid, name, desc);
	}
	//JDK1.6 @Override
	public long getUserAuthoredPageSize(String username) {
	
		return pageDAO.getUserAuthoredSize(username);
	}

	//JDK1.6 @Override
	public long getUserModifiedPageSize(String username) {
		return pageDAO.getUserModifiedSize(username);
	}
	

	//JDK1.6 @Override
	public Set<User> getPageContributors(String pageUuid) {
		Set<User> contributors = new HashSet<User>();
		Page page = pageDAO.getCurrentByUuid(pageUuid);
		
		User user = page.getCreator() == null?userReadingService.getUser(-1):page.getCreator();
		contributors.add(user);
		user = page.getModifier() == null?userReadingService.getUser(-1):page.getModifier();
		contributors.add(user);
		
		List<History> histories = historyDAO.getByUuid(pageUuid);
		if(histories != null){
			for (History history : histories) {
				user = history.getCreator() == null?userReadingService.getUser(-1):history.getCreator();
				contributors.add(user);
				user = history.getModifier() == null?userReadingService.getUser(-1):history.getModifier();
				contributors.add(user);
			}
		}
		return contributors;
	}

	//JDK1.6 @Override
	public List<Page> getPageChildren(String pageUuid) {
		Page page = pageDAO.getCurrentByUuid(pageUuid);
		if(page != null)
			return pageDAO.getChildren(page.getUid());
		
		return null;
	}
	public Integer getPageChildrenCount(String pageUuid) {
		Page page = pageDAO.getCurrentByUuid(pageUuid);
		if(page != null)
			return pageDAO.getChildrenCount(page.getUid());
		
		return 0;
	}

	/*
	 * This method does not consider concurrent issue - as this is not critcial task.
	 */
	@SuppressWarnings("unchecked")
	public void startEditing(String pageUuid, User user) {
		
		ConcurrentMap<String, Long> map = null;
		
		Element ele = pageEditingCache.get(pageUuid);
		if(ele != null){
			//if ele is not null, then keep existed editing users list
			//this maybe cause users not expired in 1 hour(echache.xml setting), but it is not harmful
			pageEditingCache.remove(pageUuid);
			map = (ConcurrentMap<String, Long>) ele.getValue();
		}
		if(map == null)
			map = new ConcurrentHashMap<String, Long>();
		
		map.put(user.getUsername(), System.currentTimeMillis());
		
		ele = new Element(pageUuid, map);
		pageEditingCache.put(ele);
	}


	@SuppressWarnings("unchecked")
	public void stopEditing(String pageUuid, User user) {
		Element ele = pageEditingCache.get(pageUuid);
		if(ele != null){
			ConcurrentMap<String, Long> map = (ConcurrentMap<String, Long>) ele.getValue();
			if(map != null)
				map.remove(user.getUsername());
			if(map == null || map.size() == 0)
				pageEditingCache.remove(pageUuid);
		}
	}
	public Map<String, Long> isEditing(String pageUuid) {
		Element ele = pageEditingCache.get(pageUuid);
		if(ele != null)
			return (ConcurrentMap<String, Long>) ele.getValue();
		
		return null;
	}
	public boolean markPageFlag( int type, String pageUuid, String username, boolean add) {
		User user = userReadingService.getUserByName(username);
		if(user == null)
			return false;
		
		Page page = pageDAO.getCurrentByUuid(pageUuid);
		if(page == null)
			return false;
		
		UserPageMark up = new UserPageMark();
		up.setPage(page);
		up.setUser(user);
		up.setType(type);
		up.setCreatedDate(new Date());
		
		UserPageMark persistMark = userPageDAO.getByObject(up);
		if(add){
			if(persistMark  == null){
				//don't add duplicated
				userPageDAO.saveOrUpdate(up);
			}
		}else{
			if(persistMark != null){
				userPageDAO.removeObject(persistMark);
			}
		}
		
		return true;
	}
	public Page getPageByExtLinkID(String spaceUname, String extLinkID) {
		return pageDAO.getPageByProgressExtLinkID(spaceUname, extLinkID);
	}


	public void saveOrUpdatePageProgress(PageProgress pageProgress) {
		pageProgressDAO.saveOrUpdate(pageProgress);
	}


	//********************************************************************
	//               Private Methods
	//********************************************************************

	private Draft removeDraftInternal(String spaceUname, String pageUuid, User user, int type, boolean removeAttahcmnet) 
		throws PageException {
		Draft draft;
		try {
			
			if(type == Draft.NONE_DRAFT){
				//remove both
				Draft draft1 = draftDAO.removeDraftByUuid(spaceUname, pageUuid, user, Draft.AUTO_DRAFT);
				Draft draft2 = draftDAO.removeDraftByUuid(spaceUname, pageUuid, user, Draft.MANUAL_DRAFT);
				draft = draft1 == null?draft2:draft1;
			}else{
				draft = draftDAO.removeDraftByUuid(spaceUname, pageUuid, user, type);
			}
			if(draft != null && draft.getPageProgress() != null){
				pageProgressDAO.removeObject(draft.getPageProgress());
			}
			if(draft != null && removeAttahcmnet){
				boolean pageNotExist = false;
				if(type == Draft.AUTO_DRAFT){
					//if auto draft removing, then need check if there is manual draft and page, if no, then remove all from repository
					if(draftDAO.getDraftByUuid(spaceUname, pageUuid, user, Draft.MANUAL_DRAFT) == null && 
						pageDAO.getByUuid(pageUuid) == null){
						pageNotExist = true;
					}
				}else if(type == Draft.MANUAL_DRAFT){
					//if manual draft removing, need check if page exist, if no, all remove from repository.
					if(pageDAO.getByUuid(pageUuid) == null){
						pageNotExist = true;
					}
				}else if(type == 0){
					if(pageDAO.getByUuid(pageUuid) == null){
						pageNotExist = true;
					}
					//for non-draft normal page, all attachment will removed whoever uploaded. this set only use for pageNotExist = false.
					user = null;
				}
				
				
				if(pageNotExist){
					ITicket ticket = repositoryService.login(spaceUname, spaceUname, spaceUname);
					repositoryService.removeIdentifier(ticket, RepositoryService.TYPE_ATTACHMENT, pageUuid);
				}else{
					//page exist, then only remove specified type attachment
					removeDraftAttachment(spaceUname, pageUuid, user,type);
				}
			}else{
				if(draft == null)
					log.info("unable find any draft for page " + pageUuid + " on space " + spaceUname);
			}
		} catch (RepositoryException e) {
			log.error("Remove draft failed" , e);
			throw new PageException("Remove draft failed:" +e);
		} catch (RepositoryTiemoutExcetpion e) {
			log.error("Remove draft failed" , e);
			throw new PageException("Remove draft failed:" +e);
		}
		
		return draft;
	}
	
	/**
	 * Try to get a unique page Title in given space. The initial title value is input parameter title. If it is already unique,
	 * just return itself.
	 *  
	 * @param spaceUname
	 * @param title
	 * @return
	 */
	private String getIdeniticalTitle(String spaceUname, String title , String prefix) {
		boolean done = false;
		String orgTitle = title;
		//try 10 times 
		for(int idx=0;idx<10;idx++){
			if(pageDAO.getCurrentPageByTitle(spaceUname, title) == null){
				if(idx > 0)
					log.info("An existed page has same title by " + orgTitle + ". The target page will be renamed to " + title);
				done = true;
				break;
			}
			title = prefix + title;
		}
		if(!done){
			//failure tolerance: OK, so many copy of copy of, then add a UUID to page title
			title = orgTitle +  UUID.randomUUID().toString();
		}
		return title;
	}
	

	/**
	 * If user has drafted attachment before, but the user did not resume the draft(whatever auto/manual) when editing then save,
	 * This means the previous drafted attachment must be removed because they are not invisible for time being. The remove 
	 * strategy is:<Br>
	 * Any auto saved attachments which are not in comeinList, will be removed whatever the type (MANUAL-DRAFT or NON_DRAFT or AUTO-DRAFT)
	 * Manual saved attachments which are not in comeinList, will be remove only the type is MANUAL-DRAFT or NON_DRAFT  
	 * @throws RepositoryTiemoutExcetpion 
	 * @throws RepositoryException 
	 */
	private void mergeAttahment(List<FileNode> existList, List<FileNode> comeinList,String spaceUname, User user, int status) throws RepositoryException, RepositoryTiemoutExcetpion {
		if(comeinList != null && existList != null){
			ITicket ticket = repositoryService.login(spaceUname, spaceUname, spaceUname);
			for (FileNode node : existList) {
				//exist item is current user's draft, and it has same status or higher (auto if given is manual)
				if(node.getStatus() > 0 && node.getStatus() >= status 
					&& StringUtils.equalsIgnoreCase(node.getCreateor(),user.getUsername())){
					//check if this draft is in comeinList, if not, it means it will be removed.
					boolean found =false;
					for (FileNode comeNode : comeinList) {
						if(comeNode.getNodeUuid().equals(node.getNodeUuid())){
							found = true;
							break;
						}
					}
					if(!found){
						repositoryService.removeFile(ticket, node.getNodeUuid(), null);
						log.info("Attachment {} is removed.", node.getNodeUuid());
					}
				}		
			}
		}
		
	}
	private void upgradeAttachmentStatus(String spaceUname, String pageUuid, User user,int status) throws RepositoryException{
		List<FileNode> list = getPageAttachment(spaceUname, pageUuid, true, true, user);
		if(list != null){
			ITicket ticket = repositoryService.login(spaceUname, spaceUname, spaceUname);
			for (FileNode node : list) {
				//auto < manual < normal, this logic sequence is reverse with actual number of their hold.  
				if(node.getStatus() > status && StringUtils.equalsIgnoreCase(node.getCreateor(),user!=null?user.getUsername():null)){
					//remove draft flag in attachment file node
					node.setStatus(status);
					repositoryService.updateMetaData(ticket, node);
				}
			}
		}
	}
	private void removeDraftAttachment(String spaceUname, String pageUuid, User user,int type) throws RepositoryException, RepositoryTiemoutExcetpion{
		//remove this user's attachment from repository
		
		ITicket ticket = repositoryService.login(spaceUname, spaceUname, spaceUname);
		List<FileNode> list = getPageAttachment(spaceUname, pageUuid, true, true, user);
		if(list != null){
			for (FileNode node : list) {
				if(node.getStatus() >= type){
					//user if not null, then only special user's attachment(draft status) will remove.
					if((user != null &&  StringUtils.equalsIgnoreCase(node.getCreateor(),user.getUsername())) || user == null)
						repositoryService.removeFile(ticket,node.getNodeUuid(),node.getVersion());
				}
			}
		}
	}
	
	/**
	 * @param pageValue
	 * @param page
	 * @return
	 */
	private boolean checkVersion(Page myPage, Page currentPage) {
		//new page, version can not be conflict
		if(currentPage == null)
			return true;
		
		if(myPage.getVersion() != currentPage.getVersion())
			return false;
		else
			return true;
	}

	/*
	 * Remove give page from cache
	 */
	@SuppressWarnings("unchecked")
	private void removePageCache(String spaceUname, Page page){
		if(page == null)
			return;
		
		Element treeItem = pageTreeCache.get(spaceUname);
		Integer removedUid = null;
		if(treeItem != null){
			Set<Page> sortedSet = (Set<Page>) treeItem.getValue();
			for (Iterator<Page> iter = sortedSet.iterator();iter.hasNext();) {
				Page inPage = iter.next();
				if(page.getUid().equals(inPage.getUid())){
					log.info("remove page from tree cache.Uid:"+page.getUid() + " title:" + page.getTitle());
					iter.remove();
					removedUid = inPage.getUid();
					break;
				}
			}
			//rebuild child page, reset child page's parent to this page's parent
			if(removedUid != null){
				Page parentPage = null;
				//find given page's parent, use this value reset its children's parent (so bad comments:(
				//e.g., A->B->C, if B is removed. C's parent will reset to A: A->C
				if(page.getParent() != null){
					for (Page parent : sortedSet) {
						if(page.getParent().getUid().equals(parent.getUid())){
							parentPage = parent;			
							break;
						}
					}
				}
				for (Page child : sortedSet) {
					//this node's parent is removed node, then reset it removed node's parent.
					if(child.getParent() != null && child.getParent().getUid().equals(page.getUid())){
						child.setParent(parentPage);
					}
				}
			}
		}
	}
	/**
	 * PageCache only save current page. If page is any draft, history, removed, it will removed from cache.
	 * @param spaceUname
	 * @param page
	 */
	@SuppressWarnings("unchecked")
	private void addPageCache(String spaceUname, Page page){
		if(page == null)
			return;
		
		Element treeItem = pageTreeCache.get(spaceUname);
		
		if(treeItem != null){
			Set<Page> sortedSet = (Set<Page>) treeItem.getValue();
			//non-current: remove from page cache
			if(page.isRemoved()){
				removePageCache(spaceUname,page);
			}else{
				log.info("add/update from tree cache.Uid:"+page.getUid() + " title:" + page.getTitle());
				//here does not use clone(): to avoid overuse fields copy.
				//This duplicated fields must keep consist with PageDAOHibernate.getTree() method
				Page cPage = new Page();
				cPage.setUid(page.getUid());
				cPage.setTitle(page.getTitle());
				cPage.setParent(page.getParent());
				cPage.setLevel(page.getLevel());
				cPage.setPageUuid(page.getPageUuid());
				
				Space spaceModel = new Space();
				spaceModel.setUnixName(spaceUname);
				cPage.setSpace(spaceModel);
				
				//because above does not deep clone parent, so here use exist page replace its lazy parent object
				if(cPage.getParent() != null){
					for (Page parent : sortedSet) {
						if(cPage.getParent().getUid().equals(parent.getUid())){
							cPage.setParent(parent);
							break;
						}
					}
				}
				sortedSet.add(cPage);
				pageTreeCache.put(treeItem);
			}
		}else{
			//retreive all page and rebuild pageTreeCache
			getPageTree(spaceUname);
		}

	}
	/**
	 * Copy value: content, title, type , tags
	 * @param pageValue
	 */
	private void copyValueFromView(AbstractPage page, AbstractPage pageValue) {
		if(page instanceof Page){
			PageContent content = ((Page)page).getContent();
			if(content == null)
				content = new PageContent();
			content.setContent(((Page)pageValue).getContent().getContent());
			((Page)page).setContent(content);
			
			PageProgress progress = ((Page) page).getPageProgress();
			if(progress == null)
				progress = new PageProgress();
			
			((Page)page).setPageProgress(progress);
			
			if(((Page)pageValue).getPageProgress() != null){
				progress.setLinkExtID(((Page)pageValue).getPageProgress().getLinkExtID());
				progress.setLinkExtInfo(((Page)pageValue).getPageProgress().getLinkExtInfo());
			}
		}else if(page instanceof Draft){
			DraftContent content = ((Draft)page).getContent();
			if(content == null)
				content = new DraftContent();
			content.setContent(((Draft)pageValue).getContent().getContent());
			((Draft)page).setContent(content);
			
			if(((Draft)pageValue).getPageProgress() != null){
				//draft page progress is optional
				PageProgress progress = ((Draft) page).getPageProgress();
				if(progress == null)
					progress = new PageProgress();
				
				progress.setLinkExtID(((Draft)pageValue).getPageProgress().getLinkExtID());
				progress.setLinkExtInfo(((Draft)pageValue).getPageProgress().getLinkExtInfo());
				((Draft)page).setPageProgress(progress);
			}
		}else if(page instanceof History){
			HistoryContent content = ((History)page).getContent();
			if(content == null)
				content = new HistoryContent();
			content.setContent(((History)pageValue).getContent().getContent());
			((History)page).setContent(content);
		}
		
		page.setTitle(pageValue.getTitle());
		page.setVisibleAttachmentNodeList(pageValue.getVisibleAttachmentNodeList());
		//currently, does not copy attribute
	}



	/**
	 * Set this page's ancestor page list for navbar use.
	 * @param spaceUname
	 * @param page
	 */
	private void refreshAncestors(String spaceUname, AbstractPage page) {

		//get page parent navigation information
		List<AbstractPage> parentList = new ArrayList<AbstractPage>();
		AbstractPage myPage = page;
		if(page instanceof Draft || page instanceof History){
			//it maybe is page history version, so get back current, because Cache only saving current page info.
			myPage = pageDAO.getCurrentByUuid(page.getPageUuid());
			if(myPage == null){
				if(page instanceof Draft){
					//this is draft without formal page saving, so just get from its parent page if it has
					myPage = page.getParent();
					if(myPage == null){
						//if it is root page, just simple return without any Ancenstoer information
						return;
					}
				}else{
					log.error("Unpexected case: Page can not get correct ancenstor information. UUID: " 
							+ page.getPageUuid() + ". Title: " + page.getTitle());
					return;
				}
			}
		}

		//add itself, it will be first one, the last one after reverse.
		parentList.add(myPage);
		
		if(myPage.getParent() != null){
			List<Page> tree = getPageTree(spaceUname);
			Page cachePage = null;
			for (Page pg : tree) {
				if(pg.getPageUuid().equals(myPage.getPageUuid())){
					cachePage = pg;
					break;
				}
			}
			//failure tolerance: only deep into 20 level
			int failure = 20;
			int idx = 0;
			while(cachePage != null && idx++ < failure){
				cachePage = cachePage.getParent();
				if(cachePage == null)
					break;
				parentList.add(cachePage);
			}
		}
		//sort from root parent
		Collections.reverse(parentList);
		
		page.setAncestorList(parentList);
	}
	/**
	 * If a page title changed, some links to this page becomes obsolete. This method 
	 * will retrieve all page links in instance(space?) to update PageLink table and 
	 * page content text in PageContent table. 
	 * <br>
	 * 
	 * This method only update the pages which current login user has write permission. After method,
	 * page version is increased.
	 * 
	 *  
	 * @param spaceUname
	 * @param oldTitle
	 * @param newTitle
	 */
	private void fixLinksToTitle(String spaceUname, String oldTitle, String newTitle) {
		//get back links according to space and title
		List<PageLink> linkList = pageLinkDAO.getLinksFromSpace(spaceUname, oldTitle);
		
		List<Integer> pageUids = new ArrayList<Integer>();
		User loginUser = WikiUtil.getUser(userReadingService);
		boolean readonly;
		
		for (PageLink oldLink : linkList) {
			//do I need skip current page itself? As current page will save after this method
			Page page = oldLink.getPage();
			if(StringUtils.equalsIgnoreCase(oldTitle, page.getTitle())){
				continue;
			}
			//don't update same page twice
			if(pageUids.contains(page.getUid())){
				continue;
			}
			
			//don't allow update page without write permission
			readonly = true;
			securityService.fillPageWikiOperations(loginUser, page);
			for (WikiOPERATIONS wikiOPERATIONS : page.getWikiOperations()) {
				if (WikiOPERATIONS.PAGE_WRITE.equals(wikiOPERATIONS)) {
					readonly = false;
					break;
				}
			}
			if(readonly){
				StringBuffer buf = new StringBuffer("Page ");
				log.info(buf.append(page.getTitle()).append(":").append(spaceUname).append(" does not do fix for new title:")
						.append(newTitle).append(" becuase user has no write permission.").toString());
				continue;
			}
			
			//upgrade version???
			History oldPage = (History) page.cloneToHistory();
			oldPage.setAttachments(null);
			oldPage.setParent(null);
			historyDAO.saveOrUpdate(oldPage);
			WikiUtil.setTouchedInfo(userReadingService, page);
			page.setVersion(page.getVersion() + 1);
			
			//update page content - is it dangerous?
			pageUids.add(page.getUid());
			PageContent referContent = page.getContent();
			String content = referContent.getContent();
			
			//replace old link with new 
			content = renderService.changeLinkTitle(content,page.getSpace().getUnixName(), spaceUname, oldTitle,newTitle);
			referContent.setContent(content);
			
			//persist
			oldLink.setLink(newTitle);
			pageDAO.saveOrUpdate(page);
			pageLinkDAO.saveOrUpdate(oldLink);
		}
	}
	

	/**
	 * Change all given links space(if equals fromSpaceUname) to toSpaceUname. And update page content 
	 * to add spaceUanme suffix, such as "[link@toSpaceUname]".
	 * 
	 * @param fromSpaceUname
	 * @param toSpaceUname
	 * @param links
	 */
	private void fixLinksToSpace(Page page, String fromSpaceUname, String toSpaceUname) {
		Set<PageLink> links = page.getLinks();
		if(links == null || links.size() == 0)
			return;
		
		Set<String> titles = new HashSet<String>();
		for (PageLink link: links) {
			//if link has special spaceUname rather than fromSpaceUname, skip it
			//for example original space is SpaceA, if [link@spaceXXX], which won't do space replacement
			//but [link@spaceA] or [link] will do
			if(StringUtils.equals(link.getSpaceUname(),fromSpaceUname)){
				titles.add(link.getLink());
			}
		}
		
		if(titles.size() > 0){
			String content = page.getContent().getContent();
			//replace old link with new 
			content = renderService.changeLinkSpace(content,fromSpaceUname,toSpaceUname);
			page.getContent().setContent(content);
		}
	}
	
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public void setPageDAO(PageDAO pageDAO) {
		this.pageDAO = pageDAO;
	}

	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}
	public void setSpaceDAO(SpaceDAO spaceDAO) {
		this.spaceDAO = spaceDAO;
	}
	
	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}
	public void setPageLinkDAO(PageLinkDAO pageLinkDAO) {
		this.pageLinkDAO = pageLinkDAO;
	}
	public void setPageTreeCache(Cache pageTreeCache) {
		this.pageTreeCache = pageTreeCache;
	}
	
	public void setUserPageDAO(UserPageDAO userPageDAO) {
		this.userPageDAO = userPageDAO;
	}
	
	public void setDraftDAO(DraftDAO draftDAO) {
		this.draftDAO = draftDAO;
	}
	public void setPageProgressDAO(PageProgressDAO pageProgressDAO) {
		this.pageProgressDAO = pageProgressDAO;
	}
	public void setTagService(TagService tagService) {
		this.tagService = tagService;
	}
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	public void setRenderService(RenderService renderService) {
		this.renderService = renderService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setHistoryDAO(HistoryDAO historyDAO) {
		this.historyDAO = historyDAO;
	}
	public void setTouchService(TouchService touchService) {
		this.touchService = touchService;
	}


	public void setPageEditingCache(Cache pageEditingCache) {
		this.pageEditingCache = pageEditingCache;
	}


	/**
	 * @param eventContainer the eventContainer to set
	 */
	public void setEventContainer(EventContainer eventContainer) {
		this.eventContainer = eventContainer;
	}


}
