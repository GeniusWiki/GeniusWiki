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


/**
 * @author Dapeng.Ni
 */
public class SpaceWidgetTemplate {//  extends AbstractWidget {
//	//how many items of each portlet show
//	
//	private static final Logger log = LoggerFactory.getLogger(SpaceWidgetTemplate.class);
//	private SpaceService spaceService;
//	private RenderService renderService;
//	private RSSService rssService;
//	private ThemeService themeService;
//	private UserReadingService userReadingService;
//	private SecurityService securityService;
//	private User viewer;
//
//	public void init(ApplicationContext applicationContext){
//		spaceService = (SpaceService) applicationContext.getBean(SpaceService.SERVICE_NAME);
//		rssService = (RSSService) applicationContext.getBean(RSSService.SERVICE_NAME);
//		themeService = (ThemeService) applicationContext.getBean(ThemeService.SERVICE_NAME);
//		userReadingService = (UserReadingService) applicationContext.getBean(UserReadingService.SERVICE_NAME);
//		securityService = (SecurityService) applicationContext.getBean(SecurityService.SERVICE_NAME);
//		renderService = (RenderService) applicationContext.getBean(RenderService.SERVICE_NAME);
//	}
//
//	@Override
//	public boolean isAllowView(User viewer) {
//		//this viewer is used for decide if space allow offline
//		this.viewer = viewer; 
//		//TODO even space is not allow, the widget is still allow to view by other, except this 
//		//widget is private and view has no explicit read permission 
//		return true;
//	}
//	@Override
//	public Widget invoke(String spaceUname,User viewer) throws WidgetException {
//		
//		Widget widget = new Widget();
//		Space space = spaceService.getSpaceByUname(spaceUname);
//		
//		SpacePagesModel model = new SpacePagesModel();
//		if(space == null){
//			//if space does not exist, return error. 
//			throw new WidgetException(ErrorCode.SPACE_NOT_EXIST_ERR);
//		}
//		
//		securityService.fillSpaceWikiOperations(viewer,space);
//
//		try {
//			model.widgetStyle = space.getSetting().getWidgetStyle();
//			if(model.widgetStyle == SpaceSetting.WIDGET_STYLE_ITEM_SHORT_BY_MODIFIED_DATE
//					|| model.widgetStyle == SpaceSetting.WIDGET_STYLE_ITEM_SHORT_BY_CREATE_DATE){
//				List<Page> pages = rssService.getPagesFromFeed(space.getUid(), spaceUname, viewer);
//				model.pages = PageUtil.copyPageItem(pages,PORTLET_ITEM_COUNT);
//			}else if(model.widgetStyle == SpaceSetting.WIDGET_STYLE_HOME_PAGE){
//				model.page = new PageModel();
//				Page home = space.getHomepage();
//				renderService.renderHTML(home);
//				PageUtil.copyPageToModel(home, model.page, userReadingService, PageUtil.NOT_COPY_ATTACHMENT);
//			}
//			
//			model.space = new SpaceModel();
//			SpaceUtil.copySpaceToModel(space, model.space,themeService);
//			
//			//copy viewer info: for offline button
//			model.space.viewer = UserUtil.copyUserToModel(viewer,viewer);
//			log.info("Get user " + viewer.getUsername() + " recent pages from space " + spaceUname);
//		} catch (FeedException e) {
//			model.errorCode = ErrorCode.SPACE_RSS_READ_ERROR;
//			//OK, try to recreate this space RSS feed, then user could get it in next time(hope so).
//			rssService.createFeed(spaceUname);
//		}
//		
//		WidgetMetadata obj = new WidgetMetadata();
//		obj.setRenderContent(model);
//		return obj;
//	}

}
