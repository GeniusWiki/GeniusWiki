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
package com.edgenius.wiki.widget;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.SpacePagesModel;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.server.PageUtil;
import com.edgenius.wiki.gwt.server.SpaceUtil;
import com.edgenius.wiki.gwt.server.UserUtil;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.rss.RSSService;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.ThemeService;
import com.sun.syndication.io.FeedException;

/**
 * @author Dapeng.Ni
 */
public class SpaceWidget extends AbstractWidgetTemplate {
	//how many items of each portlet show
	

	private static final Logger log = LoggerFactory.getLogger(SpaceWidget.class);
	private SpaceService spaceService;
	private RenderService renderService;
	private RSSService rssService;
	private PageService pageService;
	private ThemeService themeService;
	private UserReadingService userReadingService;
	private SecurityService securityService;

	public void reset() {
	}
	public void init(ApplicationContext applicationContext){
		spaceService = (SpaceService) applicationContext.getBean(SpaceService.SERVICE_NAME);
		rssService = (RSSService) applicationContext.getBean(RSSService.SERVICE_NAME);
		themeService = (ThemeService) applicationContext.getBean(ThemeService.SERVICE_NAME);
		userReadingService = (UserReadingService) applicationContext.getBean(UserReadingService.SERVICE_NAME);
		securityService = (SecurityService) applicationContext.getBean(SecurityService.SERVICE_NAME);
		renderService = (RenderService) applicationContext.getBean(RenderService.SERVICE_NAME);
		pageService = (PageService) applicationContext.getBean(PageService.SERVICE_NAME);
	}

	//JDK1.6 @Override
	public Widget createWidgetObject(String key) {
		if(StringUtils.isBlank(key))
			return null;
		
		Space space = spaceService.getSpaceByUname(key);
		Widget obj = null; 
		if(space == null){
			log.error("Unable to get space object for space widget by spaceUname " + key);
		}else{
			obj = new Widget();
			obj.setType(getType());
			obj.setUuid(key);//spaceUname
			obj.setTitle(space.getName());
			obj.setTitleURL(GwtUtils.getSpacePageToken(key,null));
			obj.setDescription(space.getDescription());
		}
		return obj;
	}

	//JDK1.6 @Override
	public Widget invoke(String spaceUname,User viewer) throws WidgetException {
		Space space = spaceService.getSpaceByUname(spaceUname);
		
		SpacePagesModel model = new SpacePagesModel();
		if(space == null){
			//if space does not exist, return error. 
			throw new WidgetException(ErrorCode.SPACE_NOT_EXIST_ERR);
		}
		
		securityService.fillSpaceWikiOperations(viewer,space);

		try {
			model.isPrivate = space.isPrivate();
			model.widgetStyle = space.getSetting().getWidgetStyle();
			if(model.widgetStyle == SpaceSetting.WIDGET_STYLE_ITEM_SHORT_BY_MODIFIED_DATE
					|| model.widgetStyle == SpaceSetting.WIDGET_STYLE_ITEM_SHORT_BY_CREATE_DATE){
				int listCount = space.getSetting().getItemAmount();
				
				List<Page> toppages = pageService.getPinTopPages(space.getUid(), spaceUname, viewer);
				if(toppages != null && toppages.size() > 0){
					if (model.widgetStyle == SpaceSetting.WIDGET_STYLE_ITEM_SHORT_BY_CREATE_DATE){
						updateModifier(model, toppages);
					}
					model.pages = PageUtil.copyPageItem(toppages,listCount);
					for (PageItemModel item : model.pages) {
						item.pinTop = true;
					}
				}else{
					//to avoid below code NullPointerException
					model.pages = new ArrayList<PageItemModel>();
				}
				
				if(toppages.size() < listCount){
					List<Page> pages = rssService.getPagesFromFeed(space.getUid(), spaceUname, viewer);
					if (model.widgetStyle == SpaceSetting.WIDGET_STYLE_ITEM_SHORT_BY_CREATE_DATE){
						updateModifier(model, pages);
					}
					if(model.pages.size() > 0){
						//merge pin-top pages and rss normal list
						ArrayList<PageItemModel> rssList = PageUtil.copyPageItem(pages,listCount);
						//remove duplicated from RSS list and pin-top list
						rssList.removeAll(model.pages);
						model.pages.addAll(rssList);
						int over = model.pages.size() - listCount;
						if(over > 0){
							//pin pages + RSS pages is over list count, then remove oversize from last of list
							for (int idx = over; idx > 0; idx--) {
								model.pages.remove(model.pages.size()-1);
							}
						}
					}else{
						//only rss normal list
						model.pages = PageUtil.copyPageItem(pages,listCount);
					}
				}
				model.hidePortrait = space.getSetting().isHidePortrait();
			}else if(model.widgetStyle == SpaceSetting.WIDGET_STYLE_HOME_PAGE){
				model.page = new PageModel();
				Page home = space.getHomepage();
				renderService.renderHTML(home);
				PageUtil.copyPageToModel(home, model.page, userReadingService, PageUtil.NOT_COPY_ATTACHMENT);
			}
			
			model.space = new SpaceModel();
			SpaceUtil.copySpaceToModel(space, model.space,viewer, themeService);
			
			//copy viewer info: for offline button
			model.space.viewer = UserUtil.copyUserToModel(viewer,viewer);
			
			log.info("Get user " + viewer.getUsername() + " recent pages from space " + spaceUname);
		} catch (FeedException e) {
			model.errorCode = ErrorCode.SPACE_RSS_READ_ERROR;
			//OK, try to recreate this space RSS feed, then user could get it in next time(hope so).
			rssService.createFeed(spaceUname);
		}
		
		Widget obj = new Widget();
		//go to homepage
		obj.setTitleURL(GwtUtils.getSpacePageToken(spaceUname,null));
		obj.setRenderContent(model);
		return obj;
	}

	/**
	 * @param model
	 * @param toppages
	 */
	private void updateModifier(SpacePagesModel model, List<Page> toppages) {
		//some hacker here: if style is "sort by created date", then bring back creator information in Modifier fields as 
		//PageItemModel list only has modifier information...
		for (Page page : toppages) {
			page.setModifier(page.getCreator());
			page.setModifiedDate(page.getCreatedDate());
		}
	}

}
