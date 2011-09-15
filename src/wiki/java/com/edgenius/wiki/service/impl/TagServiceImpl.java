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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.dao.PageTagDAO;
import com.edgenius.wiki.dao.SpaceTagDAO;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageTag;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.SpaceTag;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.TagService;
import com.edgenius.wiki.service.TouchService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("unchecked")
@Transactional
public class TagServiceImpl implements TagService {

	private PageTagDAO pageTagDAO;
	private SpaceTagDAO spaceTagDAO;
	private Cache tagCache;
	private UserReadingService userReadingService;
	private TouchService touchService;
	private SecurityService securityService; 
	
	public List<PageTag> saveUpdatePageTag(Page page, String tagString) {
		List<PageTag> pageTags = WikiUtil.parsePageTagString(tagString);
		
		//save tag
		Space space = page.getSpace();
		String spaceUname = space.getUnixName();
		List<PageTag> tags = page.getTags();
		if(tags == null){
			tags = new ArrayList<PageTag>();
			page.setTags(tags);
		}else{
			for (PageTag tag : tags) {
				//need substract 1 from tag size, as it may remove from page
				int size = getTagSizeFromCache(spaceUname,tag.getName());
				if(size != -1){
					size--;
					putCache(spaceUname,tag.getName(),size);
				}
			}
		}
		
		//reset, then put new one into list
		tags.clear();
		
		if(pageTags != null && pageTags.size() > 0){
			for (PageTag tag : pageTags) {
				PageTag pTag = pageTagDAO.getByName(spaceUname,tag.getName());
				int size;
				if(pTag == null){
					//new tag
					WikiUtil.setTouchedInfo(userReadingService, tag);
					tag.setSpace(space);
					pageTagDAO.saveOrUpdate(tag);
					tags.add(tag);
					size = 1;
				}else{
					size = getTagSizeFromCache(spaceUname,tag.getName());
					if(size == -1){
						size = pTag.getPages().size();
						if(!pTag.getPages().contains(page)){
							size++;
						}
					}else{
						//get from cache, so need not check if contain this page, as it already minus 1 above
						size++;
					}
					tags.add(pTag);
				}
				putCache(spaceUname,tag.getName(),size);
			}
		}
		
		touchService.touchPage(page.getPageUuid());
		
		return tags;
	}
	public List<SpaceTag> saveUpdateSpaceTag(Space space, String tagString) {
		List<SpaceTag> spaceTags = WikiUtil.parseSpaceTagString(tagString);

		//save tag
		List<SpaceTag> tags = space.getTags();
		if(tags == null){
			tags = new ArrayList<SpaceTag>();
			space.setTags(tags);
		}else{
			for (SpaceTag tag : tags) {
				//need substract 1 from tag size, as it may remove from page
				int size = getTagSizeFromCache(SharedConstants.SYSTEM_SPACEUNAME,tag.getName());
				if(size != -1){
					size--;
					putCache(SharedConstants.SYSTEM_SPACEUNAME,tag.getName(),size);
				}
			}
		}
		tags.clear();
		
		if(spaceTags != null && spaceTags.size() > 0 ){
			for (SpaceTag tag : spaceTags) {
				SpaceTag sTag = spaceTagDAO.getByName(tag.getName());
				int size;
				if(sTag == null){
					WikiUtil.setTouchedInfo(userReadingService, tag);
					spaceTagDAO.saveOrUpdate(tag);
					tags.add(tag);
					size = 1;
				}else{
					size = getTagSizeFromCache(SharedConstants.SYSTEM_SPACEUNAME,tag.getName());
					if(size == -1){
						size = sTag.getSpaces().size();
						if(!sTag.getSpaces().contains(space)){
							size++;
						}
					}else{
						size++;
					}
					tags.add(sTag);
				}
				putCache(SharedConstants.SYSTEM_SPACEUNAME,tag.getName(),size);
			}
			
			touchService.touchSpace(space.getUnixName());
		}
		return tags;
	}
	

	public PageTag internalSavePageTag(PageTag tag) {
		pageTagDAO.saveOrUpdate(tag);
		putCache(tag.getSpace().getUnixName(),tag.getName(),1);
		return tag;
	}
	
	public List<PageTag> getPageTags(String spaceUname){
		List<PageTag> tags = pageTagDAO.getSpaceTags(spaceUname);
		return tags;
	}
	/**
	 * Get page tag name list from cache if it exists in cache, otherwise, get from database.
	 * @param spaceUname
	 * @return
	 */
	public Map<String,Integer> getPageTagsNameList(String spaceUname){
		Element ele = tagCache.get(spaceUname);
		if(ele == null){
			Map<String,Integer> tags = new HashMap<String, Integer>();
			
			if(spaceUname == null || StringUtils.equals(SharedConstants.SYSTEM_SPACEUNAME, spaceUname)){
				spaceUname = SharedConstants.SYSTEM_SPACEUNAME;
				List<SpaceTag> spaceTags = spaceTagDAO.getObjects();
				if(spaceTags != null){
					for (SpaceTag tag : spaceTags) {
						tags.put(tag.getName(),tag.getSpaces().size());
					}
				}
			}else{
				//get from database
				List<PageTag> pageTags = pageTagDAO.getSpaceTags(spaceUname);
				
				if(pageTags != null){
					for (PageTag tag : pageTags) {
						tags.put(tag.getName(),tag.getPages().size());
					}
				}
			}
			putCache(spaceUname, tags);
			return tags;
		}else{
			return (Map<String,Integer>) ele.getValue();
		}
	}

	public PageTag getPageTagByName(String spaceUname, String tagName) {

		return pageTagDAO.getByName(spaceUname, tagName);
	}
	
	//JDK1.6 @Override
	public List<SpaceTag> getSpaceTags() {
		List<SpaceTag> tags = spaceTagDAO.getObjects();
		return tags;
	}
	/**
	 * Get Instance tag name list, which are marked for spaces
	 * @return
	 */
	public Map<String,Integer> getSpaceTagsNameList(){
		return getPageTagsNameList(SharedConstants.SYSTEM_SPACEUNAME);
	}
	public List<Page> getPagesByTag(User viewer, String spaceUname, String tagname, int count){
		List<Page> list = new ArrayList<Page>();
		if(tagname == null)
			return list;
		
		tagname = tagname.trim();
		PageTag tag = pageTagDAO.getByName(spaceUname, tagname);
		if(tag == null)
			return list;
		else{
			List<Page> pages = tag.getPages();
			//filter out the page which is not allow viewer to read
			for (Iterator<Page> iter = pages.iterator();iter.hasNext();) {
				Page page = iter.next();
				if(securityService.isAllowPageReading(spaceUname, page.getPageUuid(), viewer))
					list.add(page);
			}
			
			if(count <= 0){
				return list; 
			}else
				//TODO: so far just simple truncate the returned array:(
				return count > list.size()?list:list.subList(0, count-1);
		}
		
	}
	
	//JDK1.6 @Override
	public List<Space> getSpaceByTag(User viewer, String tagname, int count) {
		List<Space> list = new ArrayList<Space>();
		if(tagname == null)
			return list;
		tagname = tagname.trim();
		SpaceTag tag = spaceTagDAO.getByName(tagname);
		if(tag == null)
			return list;
		else{
			List<Space> spaces= tag.getSpaces();
			//filter out the page which is not allow viewer to read
			for (Iterator<Space> iter = spaces.iterator();iter.hasNext();) {
				Space space = iter.next();
				//all public spaces are listable. Only private spaces need check reading permission - no no read permission, then no listable.
				if(!securityService.isPrivateSpace(space.getUnixName()) || securityService.isAllowSpaceReading(space.getUnixName(), viewer))
					list.add(space);
			}
			if(count <= 0){
				return list; 
			}else{
				//TODO: so far just simple truncate the returned array:(
				return count > list.size()?list:list.subList(0, count-1);
			}
		}
	}
	//********************************************************************
	//               private method
	//********************************************************************

	/**
	 * @param tag
	 */
	private void putCache(String key, String tag, int size) {
		Map<String,Integer> values = new HashMap<String, Integer>();
		values.put(tag, size);
		putCache(key, values);
	}
	private void putCache(String key, Map<String,Integer> values){
		//put tag name into tagCache.
		Element element = tagCache.get(key);
		Map<String,Integer> tagList;
		if(element == null){
			tagList = new HashMap<String, Integer>();
			element = new Element(key, tagList);
		}else
			tagList = (Map<String,Integer>) element.getValue();
		tagList.putAll(values);
		
		tagCache.put(element);
	}
	/**
	 * Get tag from cache, if tag is not in cache, return -1
	 */
	private int getTagSizeFromCache(String spaceUname,String tag){
		Element element = tagCache.get(spaceUname);
		if(element != null){
			Map<String, Integer> tagList = (Map<String,Integer>)element.getValue();
			Integer size = tagList.get(tag);
			if(size != null)
				return size;
		}
		
		return -1;
		
	}
	//********************************************************************
	//               set / get method
	//********************************************************************
	public void setPageTagDAO(PageTagDAO tagDAO) {
		this.pageTagDAO = tagDAO;
	}

	public void setSpaceTagDAO(SpaceTagDAO spaceTagDAO) {
		this.spaceTagDAO = spaceTagDAO;
	}

	public void setTagCache(Cache tagCache) {
		this.tagCache = tagCache;
	}


	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}
	public void setTouchService(TouchService touchService) {
		this.touchService = touchService;
	}
	/**
	 * @param securityService the securityService to set
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

}
