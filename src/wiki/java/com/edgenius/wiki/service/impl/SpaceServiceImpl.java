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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.SecurityValues;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.ITicket;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.repository.RepositoryQuotaException;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.repository.RepositoryTiemoutExcetpion;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.service.RoleService;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.dao.DraftDAO;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.dao.PageProgressDAO;
import com.edgenius.wiki.dao.PageTagDAO;
import com.edgenius.wiki.dao.SpaceDAO;
import com.edgenius.wiki.dao.TemplateDAO;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageContent;
import com.edgenius.wiki.model.PageProgress;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.quartz.QuartzException;
import com.edgenius.wiki.quartz.RemoveSpaceJobInvoker;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.PageException;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.SpaceException;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.TagService;
import com.edgenius.wiki.util.PageComparator;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@Transactional
public class SpaceServiceImpl implements SpaceService {
	private static final Logger log = LoggerFactory.getLogger(SpaceServiceImpl.class);

	private SpaceDAO spaceDAO;

	private PageDAO pageDAO;

	private DraftDAO draftDAO;

	private PageTagDAO pageTagDAO;

	private PageProgressDAO pageProgressDAO;

	private SecurityService securityService;

	private MessageService messageService;

	private UserReadingService userReadingService;

	private TagService tagService;

	private RoleService roleService;
	private RepositoryService repositoryService;

	private PageService pageService;

	private RemoveSpaceJobInvoker removeSpaceJobInvoker;

	private TemplateDAO templateDAO;

	// ********************************************************************
	// Methods
	// ********************************************************************
	public Space getSpaceByUname(String spaceUname) {
		return spaceDAO.getByUname(spaceUname);
	}
	public Space getSpaceByTitle(String spaceUname) {
		return spaceDAO.getByTitle(spaceUname);
	}

	public Space getSpace(Integer uid) {
		return spaceDAO.get(uid);
	}
	//JDK1.6 @Override
	public Page getLastUpdatedPage(String spaceUname) {
		List<Page> list = pageDAO.getRecentPages(spaceUname, 0, 1,true);
		if(list == null || list.size() == 0)
			return null;
		else
			return list.get(0);
	}
	//JDK1.6 @Override
	public Map<Integer,Long>  getAllSpacePagesCount() {
		return spaceDAO.getAllSpacePageCount();
	}

	//!!! The second input parameter is only for SpaceIndexInterceptor to check if do lucene index.
	public Space updateSpace(Space space, boolean updateIndex){
		tagService.saveUpdateSpaceTag(space, space.getTagString());
		
		// update
		WikiUtil.setTouchedInfo(userReadingService, space);
		spaceDAO.saveOrUpdate(space);
		log.info("Space " + space.getUnixName() + " is update");
		return space;
	}

	/**
	 * Do following during creating space.
	 * <li>Create space database record.</li>
	 * <li>Create Home page record.</li>
	 * <li>Create repository of this space.</li>
	 * <li>Initial security resource. All initialized permission, resource and
	 * assign them to corresponding users/roles</li>
	 * <li>Create space group role. Role contains space creator.</li>
	 */
	public Page createSpace(Space space) throws SpaceException {
		try {
			// skip system default Resource name:
			if (space.getUnixName().equalsIgnoreCase(SharedConstants.INSTANCE_NAME))
				throw new SpaceException(SharedConstants.INSTANCE_NAME  + " can not be space name.");
			if (space.getUnixName().startsWith(WikiConstants.CONST_NONSPACE_RESOURCE_PREFIX))
				throw new SpaceException("Space name can not start with " + WikiConstants.CONST_NONSPACE_RESOURCE_PREFIX);

			// save space first,
			spaceDAO.saveOrUpdate(space);
			// TODO: all using spaceUname?
			repositoryService.createWorkspace(space.getUnixName(), space.getUnixName(), space.getUnixName());

			tagService.saveUpdateSpaceTag(space, space.getTagString());
			
			// create default home page
			Page home = new Page();
			home.setTitle(messageService.getMessage(WikiConstants.I18N_HOME_PAGE_TITLE, 
									new String[] { space.getUnixName() }));
			PageContent content = new PageContent();
			
			home.setContent(content);
			home.setSpace(space);
			// some init value
			PageProgress pageProgress = new PageProgress();
			home.setPageProgress(pageProgress);
			pageProgressDAO.saveOrUpdate(pageProgress);

			home.setVersion(1);
			home.setPageUuid(WikiUtil.createPageUuid(space.getUnixName(), space.getUnixName(), space.getUnixName(),
					repositoryService));
			// page.setUnixName(WikiUtil.getPageUnixname(page.getTitle()));
			WikiUtil.setTouchedInfo(userReadingService, home);
			pageDAO.saveOrUpdate(home);

			// space.setCrWorkspace(crWorkspace);
			// initial permission as well
			securityService.initResourcePermission(space);

			// create a new role for new space
			Role role = new Role();
			role.setDescription(SharedConstants.SPACE_ROLE_DEFAULT_PREFIX + space.getUnixName());
			role.setDisplayName(SharedConstants.SPACE_ROLE_DEFAULT_PREFIX +  space.getUnixName());
			role.setName(Role.SPACE_ROLE_PREFIX + space.getUnixName());
			role.setType(Role.TYPE_SPACE);
			Set<User> users = new HashSet<User>();
			role.setUsers(users);
			//must get from DB rather than get from User Cache!
			if(space.getCreator() != null && !space.getCreator().isAnonymous()){
				User admin = userReadingService.getUser(space.getCreator().getUid());
				admin.getRoles().add(role);
				users.add(admin);
			}
			WikiUtil.setTouchedInfo(userReadingService, role);
			//space role won't put into Lucene role index
			roleService.saveRole(role);

			log.info("Space " + space.getUnixName() + " is created");
			return home;
		} catch (Exception e) {
			throw new SpaceException(e);
		}

	}
	public void uploadLogo(Space space, FileNode smallLogo, FileNode largeLogo) {
		try {
			ITicket ticket = repositoryService.login(space.getUnixName(), space.getUnixName(), space.getUnixName());
			
			if(!repositoryService.hasIdentifierNode(ticket, RepositoryService.TYPE_SPACE,space.getUnixName())){
				//check if space identifier exist, create one if no
				repositoryService.createIdentifier(ticket, RepositoryService.TYPE_SPACE,space.getUnixName());
			}
			
			repositoryService.saveFile(ticket, smallLogo, false, false);
			repositoryService.saveFile(ticket, largeLogo, false, false);
			
			//update user portrait
			space.setLogoSmall(smallLogo.getNodeUuid());
			space.setLogoLarge(largeLogo.getNodeUuid());
			
			updateSpace(space, false);
			
		} catch (RepositoryException e) {
			log.error("Repository error " , e);
		} catch (RepositoryTiemoutExcetpion e) {
			log.error("Repository error " , e);		
		} catch (RepositoryQuotaException e) {
			log.error("Repository error " , e);		
		} 
		
	}
	public Space saveHomepage(Space space, Page homepage) {
		space.setHomepage(homepage);
		spaceDAO.saveOrUpdate(space);
		return space;
	}
	
	public List<String> getAllSpaceUnames(){
		return spaceDAO.getAllSpaceUnames();
	}
	
	@Transactional(readOnly=true)
	public List<Space> getSpaces(User viewer, int fromItem, int returnSize, String sortBy, String filter, boolean sortByDesc){
		List<Space> available = new ArrayList<Space>();
		
		// read more spaces than expected, because security filter will remove some
		boolean enough = false;
		int OVER_READ = 2;
		int start = fromItem;
		while (!enough) {
			List<Space> spaces = spaceDAO.getSpaces(start, returnSize * OVER_READ, sortBy, filter, sortByDesc);
			if(spaces == null || spaces.size() == 0)
				break;
			
			if(securityService.isAllowResourceAdmin(SharedConstants.INSTANCE_NAME,SecurityValues.RESOURCE_TYPES.INSTANCE, viewer)){
				//if system admin, then all space is visible, whatever it is private or removed.
				//this is normally at System admin-> list all space page
				available = new ArrayList<Space>(spaces);
				//remove system space as well - it always invisible!
				Space sysSpace = new Space();
				sysSpace.setUnixName(SharedConstants.SYSTEM_SPACEUNAME);
				available.remove(sysSpace);
				
				if(available.size() > returnSize){
					available = available.subList(0, returnSize);
				}
				return available;
			}
			
			//Please note: this filter out is harmful for pagination as fromItem is normally calculate by COUNT_PER_PAGE*PAGE_NUM
			//they are fixed number, however, startItem is for database but not values after filter. 
			for (Space space : spaces){
				if(SharedConstants.SYSTEM_SPACEUNAME.equals(space.getUnixName()))
					continue;
				
				if(!isReadableSpace(space,viewer)) {
					log.info("User " + viewer.getUsername() + " does have enough permission to view space "+ space.getUnixName());
					continue;
				}

	
				available.add(space);
				
				if (returnSize > 0 && available.size() >= returnSize) {
					enough = true;
					break; //break out current looping
				}

			}
			if(returnSize > 0)
				start += returnSize * OVER_READ;
			else
				break;
		}
		return available;
	}
	

	public Space removeSpaceInDelay(String spaceUname, int delayHours) throws SpaceException {
		Space removeSpace;
		if (delayHours <= 0) {
			//remove permanently immediately
			removeSpace = removeSpace(spaceUname, true);
		} else {
			try {
				removeSpaceJobInvoker.invokeJob(spaceUname, WikiUtil.getUserName(), delayHours);
				removeSpace = removeSpace(spaceUname, false);
				// TODO: get space admin users/roles and send out warning email
			} catch (QuartzException e) {
				log.error("Delete space in delay can not set up success. " , e);
				throw new SpaceException(e);
			}
		}
		return removeSpace;
	}

	public int undoRemoveSpace(String spaceUname) throws SpaceException {
		Space space = spaceDAO.getByUname(spaceUname);
		if (space == null) {
			throw new SpaceException("Unable to find given space to remove: " + spaceUname);
		}
		try {
			removeSpaceJobInvoker.cancelJob(spaceUname);
			space.setRemoved(false);
			WikiUtil.setTouchedInfo(userReadingService, space);
			spaceDAO.saveOrUpdate(space);
		} catch (QuartzException e) {
			log.error("Unable undo remove space " + spaceUname + " with exception " , e);
			throw new SpaceException(e);
		}
		return space.getUid();
	}

	/**
	 * Do following during deleting space in permanently.
	 * <li>Delete space database record.</li>
	 * <li>Remove any draft for this space.</li>
	 * <li>Delete all pages in this space.</li>
	 * <li>Remove all page tags in this space.</li>
	 * <li>Remove space group role.</li>
	 * <li>Remove permissions, resources.</li>
	 * <li>Remove repository.</li>
	 * 
	 * Remove space index job will do in SpaceIndexInterceptor class.
	 */
	public Space removeSpace(String spaceUname, boolean persist) throws SpaceException {
		Space space = spaceDAO.getByUname(spaceUname);
		if (space == null) {
			throw new SpaceException("Unable to find given space to remove: " + spaceUname);
		}
		if (persist) {
			// if 2 step remove(1st mark remove flag, 2nd physical remove): need
			// open this comments.
//			if (!space.isRemoved())
//				throw new SpaceException("Space " + spaceUname + " must be trashed before physical removed.");

			log.info("Space {} is going to be removed permanently.", spaceUname);
			
			templateDAO.removeSpaceTemplates(space.getUid());
			
			// remove all draft: draft refer to space, if they do not removed, space can not removed.
			// and draft remove must before page remove as draft refer to page in parent field.
			draftDAO.removeSpaceDrafts(space.getUid());
			// TODO: it is must very slow action: delete page by page. But it is
			// complex as well if bulk delete. There are
			// too many relation table, such as page process, Page
			List<Page> pages = pageDAO.getSpaceAllPages(spaceUname);
			
			//delete children first 
			TreeSet<Page> sortedSet = new TreeSet<Page>(new PageComparator(null));
			sortedSet.addAll(pages);
			
			pages = new ArrayList<Page>(sortedSet);
			Collections.reverse(pages);
			for (Page page : pages) {
				try {
					pageService.removePage(spaceUname, page.getPageUuid(), false, persist);
				} catch (PageException e) {
					log.error("Remove space " + spaceUname + " page failed.Title " + page.getTitle() + ". UUID:"
							+ page.getPageUuid(),e);
				}
			}

			try {
				ITicket fromTicket = repositoryService.login(spaceUname, spaceUname, spaceUname);
				repositoryService.removeWorkspace(fromTicket, spaceUname);
			} catch (RepositoryException e) {
				log.error("Unable remove space " + spaceUname + "'s repository stuff " , e);
				throw new SpaceException("Unable remove space " + spaceUname + "'s repository stuff " + e);
			} catch (RepositoryTiemoutExcetpion e) {
				log.error("Unable remove space " + spaceUname + "'s repository stuff " , e);
				throw new SpaceException("Unable remove space " + spaceUname + "'s repository stuff " + e);
			}

			//before resource delete, get this space admin user mail list so that it could send out notification.
			space.setAdminMailList(userReadingService.getSpaceAdminMailList(spaceUname));
			
			//remove from DB
			pageTagDAO.removeTagsInSpace(spaceUname);
			roleService.removeRole(Role.SPACE_ROLE_PREFIX + spaceUname);
			securityService.removeResource(spaceUname);
			spaceDAO.removeObject(space);
			
			log.info("Space " + spaceUname + " is removed permanently.");
		} else {
			log.info("Space " + spaceUname + " is marked as removed.");
			space.setRemoved(true);
			//delay get admin user mail list until send mail Notify program
			space.setAdminMailList(null);
			WikiUtil.setTouchedInfo(userReadingService, space);
			spaceDAO.saveOrUpdate(space);
		}

		return space;

	}

	public List<Page> getRemovedCurrentPages(String spaceUname) {

		return pageDAO.getRemovedPagesInSpace(spaceUname);
	}

	@Transactional(readOnly=true)
	public List<Page> getRecentPages(String spaceUname, int retCount, boolean sortByModify) {
		// return 10 records of latest updated
		// just get given space's recently pages
		return  pageDAO.getRecentPages(spaceUname, 0, retCount, sortByModify);
	}
	
	public List<Page> getRecentPages(String spaceUname, int start, int retCount, boolean sortByModify) {
		List<Page> pages = pageDAO.getRecentPages(spaceUname, start, retCount,sortByModify);
		for (Page page : pages) {
			page.getContent().getContent();
			page.getSpace().getUnixName();
		}
		return pages;
	}

	public List<Space> getUserAllCreatedSpaces(String username, int limit, User viewer) {
		List<Space> list = spaceDAO.getUserCreatedSpaces(username, limit);
		if (list != null && viewer != null) {
			// filter out the page which is not allow viewer to read
			for (Iterator<Space> iter = list.iterator(); iter.hasNext();) {
				Space space = iter.next();
				if(!isReadableSpace(space, viewer)){
					iter.remove();
				}
			}
		}
		return list;
	}
	
	public List<FileNode> getAttachments(String spaceUname, boolean withHistory, boolean withDraft, User viewer)
		throws RepositoryException {

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
		List<FileNode> atts = repositoryService.getAllSpaceNodes(ticket,RepositoryService.TYPE_ATTACHMENT, spaceUname,false);
		
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

	//JDK1.6 @Override
	public int getUserAuthoredSpaceSize(String username) {
		return spaceDAO.getUserAuthoredSize(username);
	}

	public Space getSystemSpace() {
		return spaceDAO.getSystemSpace();
	}
	public int getSpaceCount(String filter) {
		return spaceDAO.getSpaceCount(filter);
	}

	public int getRemovedSpaceLeftHours(String spaceUname){
		return removeSpaceJobInvoker.getLeftHours(spaceUname);
	}


	// ********************************************************************
	// Private
	// ********************************************************************
	/**
	 * Check if space can be view by given viewer. 
	 */
	private boolean isReadableSpace(Space space, User viewer){
		if (space.isRemoved())
			return false;

		// only private space need check if this space allow read or
		// not, for public space, space is always can be list.
		if (space.isPrivate()) {
			if(!securityService.isAllowSpaceReading(space.getUnixName(), viewer)){
				if(log.isDebugEnabled())
					log.debug("Space {} is private and does not allow read.",space.getUnixName());
				return false;
			}
		}
		
		return true;
	}
	// ********************************************************************
	// Set / Get
	// ********************************************************************
	public void setSpaceDAO(SpaceDAO spaceDAO) {
		this.spaceDAO = spaceDAO;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}

	public void setPageDAO(PageDAO pageDAO) {
		this.pageDAO = pageDAO;
	}

	public void setTemplateDAO(TemplateDAO templateDAO) {
		this.templateDAO = templateDAO;
	}
	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public void setPageService(PageService pageService) {
		this.pageService = pageService;
	}

	public void setPageProgressDAO(PageProgressDAO pageProgressDAO) {
		this.pageProgressDAO = pageProgressDAO;
	}

	public void setPageTagDAO(PageTagDAO pageTagDAO) {
		this.pageTagDAO = pageTagDAO;
	}

	public void setRemoveSpaceJobInvoker(RemoveSpaceJobInvoker removeSpaceJobInvoker) {
		this.removeSpaceJobInvoker = removeSpaceJobInvoker;
	}

	public void setDraftDAO(DraftDAO draftDAO) {
		this.draftDAO = draftDAO;
	}

	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}

	public void setTagService(TagService tagService) {
		this.tagService = tagService;
	}

}
