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
package com.edgenius.wiki.webapp.admin.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.Global;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.ITicket;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.CompareToComparator;
import com.edgenius.core.util.DateUtil;
import com.edgenius.core.webapp.taglib.PageInfo;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.SpaceUtil;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.ActivityLogService;
import com.edgenius.wiki.service.SpaceException;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.ThemeService;
import com.edgenius.wiki.util.WikiUtil;
import com.edgenius.wiki.webapp.action.BaseAction;

/**
 *  Space admin on Instance admin page
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class SpaceAdminAction extends BaseAction{
	private static final int PAGE_SIZE = 15;
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Input parameters
	private int uid;
	//page number
	private int page;

	private int sortBy;
	private String filter;
	private boolean sortByDesc;
	private long quota;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// service
	private SpaceService spaceService;
	private ThemeService themeService;
	private MessageService messageService;
	private RepositoryService repositoryService;
	private ActivityLogService activityLog;

	//********************************************************************
	//               function methods
	//********************************************************************
	public String list(){
		
		filter();
		return SUCCESS;
	}
	public String filter(){
		User viewer = WikiUtil.getUser();
		
		//page 0(null) or 1 is same
		page = page==0?1:page;
		
		//minus one is for system space
		int total  = spaceService.getSpaceCount(filter) -1;
		Map<Integer, Long> totalPageSummary = spaceService.getAllSpacePagesCount();
		
		List<SpaceDTO> list = new ArrayList<SpaceDTO>();
		List<Space> spaces;
		
		if(sortBy == Space.SORT_BY_PAGE_COUNT){
			int cs = CompareToComparator.TYPE_KEEP_SAME_VALUE|CompareToComparator.DESCEND;
			//a little tracker here - normally, user want to see maximum pages of space then descend to less
			//but default, if users click sort link, the initial is sort by Ascend. So here is special for page_count 
			//which sortByDesc means sortByAsc...
			if(sortByDesc){
				cs = CompareToComparator.TYPE_KEEP_SAME_VALUE|CompareToComparator.ASCEND;
			}
			Map<Long, Integer> sortedPageCount = new TreeMap<Long, Integer>(new CompareToComparator<Long>(cs));
			for (Entry<Integer,Long> e : totalPageSummary.entrySet()) {
				sortedPageCount.put(e.getValue(), e.getKey());
			}
			List<Integer> sortedUid = new ArrayList<Integer>(sortedPageCount.values());
			
			spaces = new ArrayList<Space>();
			if(StringUtils.isBlank(filter)){
				//SortBy PageCount won't as sub-prime search keyword - it is hard to implemented, so only it is primary keyword, sort them
				
				//now, sort totalPageSummary, then get space one by one
				int from = (page-1)*PAGE_SIZE;
				if(from < sortedUid.size()){
					//get sub-list 
					int end = Math.min(sortedUid.size(), from + PAGE_SIZE);
					sortedUid = sortedUid.subList(from, end);
					for (Integer spaceUid : sortedUid) {
						spaces.add(spaceService.getSpace(spaceUid));
					}
				
				}
			}else{
				//if filter has some value. It is possible retrieve all spaces one by one if filter doesn't actually
				//match any spaces! It is disaster! So we try to find out all spaces with that filter first. then 
				//sub list it.
				Map<Integer, Space> uidMap = new HashMap<Integer, Space>();
				List<Space> fitlerOutSpaces = spaceService.getSpaces(viewer, 0, -1, null, filter, false);
				//we save spaces with uid, so it is easier to find out it by uid
				for (Space space : fitlerOutSpaces) {
					uidMap.put(space.getUid(), space);
				}
				for (Integer spaceUid : sortedUid) {
					Space space = uidMap.get(spaceUid);
					if(space != null){
						spaces.add(space);
						if(spaces.size() > PAGE_SIZE)
							break;
					}
				}
				
			}
		}else{
			String sortSeq = getSortBySequence(WikiConstants.SESSION_NAME_SPACE_SORTBY, sortBy);
			//don't input Space.SORT_BY_PAGE_COUNT! it is not valid sort parameter for this method, cause unexpected sort result
			spaces = spaceService.getSpaces(viewer, (page-1)*PAGE_SIZE, PAGE_SIZE, sortSeq, filter, sortByDesc);
		}
		

		
		for (Space space : spaces) {
			//skip system DAO
			if(SharedConstants.SYSTEM_SPACEUNAME.equals(space.getUnixName())){
				continue;
			}
			SpaceDTO dto = new SpaceDTO(); 
			dto.setSpace(space);
			
			dto.setCreatedDate(DateUtil.toDisplayDate(viewer,space.getCreatedDate(),messageService));
			
			dto.setSmallLogoUrl(SpaceUtil.getSpaceLogoUrl(space, themeService, space.getLogoSmall(), false));
			Long totalP = totalPageSummary.get(space.getUid());
			if(totalP == null){
				AuditLogger.error("Unexpected : space " + space.getUnixName() + " cannot get page count");
				dto.setTotalPages(0);
			}else{
				dto.setTotalPages(totalP.longValue());
			}
			
			list.add(dto);
		}
		
		PageInfo pInfo = new PageInfo();
		pInfo.setCurrentPage(page);
		pInfo.setTotalPage(total/PAGE_SIZE + (total%PAGE_SIZE>0?1:0));
		
		getRequest().setAttribute("total", total);
		getRequest().setAttribute("spaces",list);
		getRequest().setAttribute("pagination", pInfo);
		
		return "list";
	}

	public String detail(){
		User viewer = WikiUtil.getUser();
		
		SpaceDTO dto = new SpaceDTO();
		Space space = spaceService.getSpace(uid);
		dto.setSpace(space);

		Page page = spaceService.getLastUpdatedPage(space.getUnixName());
		if(page != null){
			dto.setLastUpdatePageModifiedDate(DateUtil.toDisplayDate(viewer,page.getModifiedDate(),messageService));
			dto.setLastUpdatePageTitle(page.getTitle());
		}
		if(space.isRemoved())
			dto.setDelayRemoveHours(spaceService.getRemovedSpaceLeftHours(space.getUnixName()));
		
		dto.setPrivateSpace(space.isPrivate()?messageService.getMessage("yes"):messageService.getMessage("no"));
		try {
			setDTOQuota(dto, space);
		} catch (RepositoryException e) {
			log.error("Unable to get space quota. " + space.getUnixName(), e);
		}

		
		dto.setLargeLogoUrl(SpaceUtil.getSpaceLogoUrl(space, themeService, space.getLogoLarge(), true));
		getRequest().setAttribute("dto", dto);
		return "detail";
	}

	
	
	public String remove(){
		SpaceDTO dto = new SpaceDTO();
		Space space = spaceService.getSpace(uid);
		dto.setSpace(space);
		try {
			spaceService.removeSpaceInDelay(space.getUnixName(), Global.DelayRemoveSpaceHours);
			
			activityLog.logSpaceRemoved(space, WikiUtil.getUser(), Global.DelayRemoveSpaceHours <= 0);
		} catch (SpaceException e) {
			log.error("Unable remove space " + uid,e);
		}
		
		dto.setDelayRemoveHours(spaceService.getRemovedSpaceLeftHours(space.getUnixName()));
		getRequest().setAttribute("dto", dto);
		return "func";
	}
	public String restore(){
		SpaceDTO dto = new SpaceDTO();
		Space space = spaceService.getSpace(uid);
		dto.setSpace(space);
		try {
			spaceService.undoRemoveSpace(space.getUnixName());
		} catch (SpaceException e) {
			log.error("Unable restore space " + uid,e);
		}
		getRequest().setAttribute("dto", dto);
		return "func";
	}
	
	/**
	 * A new space created in administrator page.
	 * @return
	 */
	public String created(){
		getRequest().setAttribute("message", messageService.getMessage("space.create.success"));
		return list();
	}
	public String changeQuota(){
		Space space = spaceService.getSpace(uid);
		if(space != null){
			quota = quota * 1024 * 1024;
			repositoryService.updateWorkspaceQuota(space.getUnixName(), quota);
			SpaceDTO dto = new SpaceDTO();
			dto.setSpace(space);
			try {
				setDTOQuota(dto, space);
			} catch (RepositoryException e) {
				log.error("Unable to get space quota. " + space.getUnixName(), e);
			}
			getRequest().setAttribute("dto", dto);
		}
		return "quota";
	}
	/**
	 * @param dto
	 * @param space
	 * @throws RepositoryException
	 */
	private void setDTOQuota(SpaceDTO dto, Space space) throws RepositoryException {
		ITicket ticket = repositoryService.login(space.getUnixName(), space.getUnixName(), space.getUnixName());
		long[] quota = repositoryService.getSpaceQuoteUsage(ticket, space.getUnixName());
		if(quota == null || quota.length != 2){
			dto.setQuota(messageService.getMessage("unkonwn"));
		}else{
			String total;
			if(quota[1] <= 0)
				total =  messageService.getMessage("unlimited");
			else
				total = GwtUtils.convertHumanSize(quota[1]);
			dto.setQuota(messageService.getMessage("space.quota.data",new Object[]{GwtUtils.convertHumanSize(quota[1] - quota[0]),total}));
			dto.setQuotaNum(quota[1]/1024/1024);
		}
	}
	//********************************************************************
	//               set /get 
	//********************************************************************
	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public void setThemeService(ThemeService themeService) {
		this.themeService = themeService;
	}
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	/**
	 * @param repositoryService the repositoryService to set
	 */
	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int p) {
		this.page = p;
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}

	/**
	 * @param sortBy the sortBy to set
	 */
	public void setSortBy(int sortBy) {
		this.sortBy = sortBy;
	}

	/**
	 * @param sortByDesc the sortByDesc to set
	 */
	public void setSortByDesc(boolean sortByDesc) {
		this.sortByDesc = sortByDesc;
	}

	/**
	 * @return the sortBy
	 */
	public int getSortBy() {
		return sortBy;
	}

	/**
	 * @return the sortByDesc
	 */
	public boolean isSortByDesc() {
		return sortByDesc;
	}

	/**
	 * @return the quota
	 */
	public long getQuota() {
		return quota;
	}

	/**
	 * @param quota the quota to set
	 */
	public void setQuota(long quota) {
		this.quota = quota;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public void setActivityLog(ActivityLogService activityLog) {
		this.activityLog = activityLog;
	}
}
