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
package com.edgenius.wiki.gwt.server;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.captcha.CaptchaServiceProxy;

import com.edgenius.core.model.User;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.gwt.client.model.CaptchaCodeModel;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.SpaceListModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.TagListModel;
import com.edgenius.wiki.gwt.client.model.TagModel;
import com.edgenius.wiki.gwt.client.server.TagController;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.handler.GWTSpringController;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageTag;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.TagService;
import com.edgenius.wiki.service.ThemeService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class TagControllerImpl extends GWTSpringController implements TagController{
	private static final Logger log = LoggerFactory.getLogger(TagControllerImpl.class);
	private TagService  tagService;
	private PageDAO pageDAO;
	private ThemeService themeService;
	private CaptchaServiceProxy captchaService;
	
	//JDK1.6 @Override
	public TagListModel getTags(String spaceUname) {
		if(StringUtils.isBlank(spaceUname) || SharedConstants.SYSTEM_SPACEUNAME.equals(spaceUname)){
			return getSpaceScopeTags();
		}else{
			return getPageScopeTags(spaceUname);
		}
	}


	public PageItemListModel getTagPages(String spaceUname, String tagname, int count) {
		PageItemListModel model = new PageItemListModel();
		if(tagname != null){
			User viewer = WikiUtil.getUser();
			List<Page> pages = tagService.getPagesByTag(viewer,spaceUname, tagname.trim(), count);
			
			for(Page page : pages){
				PageItemModel item = PageUtil.copyToPageItem(page);
				model.itemList.add(item); 
				
			}
		}
		return model;
	}

	//JDK1.6 @Override
	public SpaceListModel getTagSpaces(String tagname, int count) {
		SpaceListModel model = new SpaceListModel();
		if(tagname != null){
			User viewer = WikiUtil.getUser();
			List<Space> spaces = tagService.getSpaceByTag(viewer,tagname.trim(), count);
			
			for(Space space : spaces){
				SpaceModel item = new SpaceModel();
				SpaceUtil.copySpaceToModel(space, item,viewer,themeService);
				model.spaceList.add(item); 
			}
		}
		return model;
	}
	public TagListModel getPageScopeTags(String spaceUname) {
		Map<String,Integer> tags = tagService.getPageTagsNameList(spaceUname);
		TagListModel model = new TagListModel();
		model.spaceUname = spaceUname;
		if(tags != null){
			int maxSize = 1;
			for (int size : tags.values()) {
				maxSize = maxSize>size?maxSize:size; 
			}
			for (Entry<String,Integer> entry: tags.entrySet()) {
				TagModel tagModel = new TagModel();
				tagModel.name = entry.getKey();
				tagModel.size = getTagSize(entry.getValue(),maxSize);
				model.tags.add(tagModel); 
				
			}
			log.info("Get " + tags.size() + " tags for space " + spaceUname);
		}
		
		return model;
	}

	
	//JDK1.6 @Override
	public TagListModel getSpaceScopeTags() {
		Map<String,Integer> tags = tagService.getSpaceTagsNameList();
		TagListModel model = new TagListModel();
		model.spaceUname = SharedConstants.SYSTEM_SPACEUNAME;
		if(tags != null){
			int maxSize = 1;
			for (int size : tags.values()) {
				maxSize = maxSize>size?maxSize:size; 
			}
			for (Entry<String,Integer> entry: tags.entrySet()) {
				TagModel tagModel = new TagModel();
				tagModel.name = entry.getKey();
				tagModel.size = getTagSize(entry.getValue(),maxSize);
				model.tags.add(tagModel); 
			}
			log.info("Get " + tags.size() + " instance tags");
		}
		
		return model;
	}
	public TagListModel savePageTags(String spaceUname, String pageUuid, String tagString, CaptchaCodeModel captcha) {
		
		if(!WikiUtil.captchaValid(captchaService, captcha))
			return null;
		
		
		Page page = pageDAO.getCurrentByUuid(pageUuid);
		List<PageTag> tagStrList = tagService.saveUpdatePageTag(page, tagString);
		
		//construct return result
		TagListModel model = new TagListModel();
		model.spaceUname = spaceUname;
		if(tagStrList != null){
			for (PageTag t: tagStrList) {
				TagModel tagModel = new TagModel();
				tagModel.name = t.getName();
				model.tags.add(tagModel); 
			}
		}
		return model;
	}
	//********************************************************************
	//               private method
	//********************************************************************
	//Tag size from 1 to 5, which is corresponding with CSS definition.
	private int getTagSize(int tagNumber,int maxTagNumber){
		if(maxTagNumber <= 1){
			return 1;
		}else{
			int size = tagNumber * 5 / maxTagNumber;
			return size < 1 ? 1 : size;
		}
	}

	//********************************************************************
	//               set method
	//********************************************************************
	public void setTagService(TagService tagService) {
		this.tagService = tagService;
	}

	public void setPageDAO(PageDAO pageDAO) {
		this.pageDAO = pageDAO;
	}

	public void setThemeService(ThemeService themeService) {
		this.themeService = themeService;
	}


	public void setCaptchaService(CaptchaServiceProxy captchaService) {
		this.captchaService = captchaService;
	}


}
