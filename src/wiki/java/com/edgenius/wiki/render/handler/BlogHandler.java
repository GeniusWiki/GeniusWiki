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
package com.edgenius.wiki.render.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.context.ApplicationContext;

import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.plugin.LinkPlugin;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.render.object.RenderHandlerException;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.BlogService;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * 
 * @author Dapeng.Ni
 */
public class BlogHandler  implements ObjectHandler, LinkPlugin{
	private String spaceUname;
	private SpaceService spaceService;
	private SecurityService securityService;
	private BlogService blogService;
	private UserReadingService userReadingService;


	public List<RenderPiece> handle(RenderContext renderContext,Map<String,String> values) throws RenderHandlerException {
		if(spaceUname == null){
			throw new RenderHandlerException("Blog can not render without space name.");
		}
		List<RenderPiece> pieces = new ArrayList<RenderPiece>();
		int retCount = NumberUtils.toInt(values.get(NameConstants.COUNT),10);
		pieces.addAll(getBlogHomePage(spaceUname, 1 ,retCount));
		
		return pieces;
	}

	public void init(ApplicationContext context) {
		spaceService = (SpaceService) context.getBean(SpaceService.SERVICE_NAME);
		blogService = (BlogService) context.getBean(BlogService.SERVICE_NAME);
		securityService = (SecurityService) context.getBean(SecurityService.SERVICE_NAME);
		userReadingService = (UserReadingService) context.getBean(UserReadingService.SERVICE_NAME);
	}

	public void renderEnd() {
		
	}

	public void renderStart(AbstractPage page) {
		if(page != null && page.getSpace() != null)
			this.spaceUname = page.getSpace().getUnixName();
		
	}

	//JDK1.6 @Override
	public Page invoke(String spaceUname,  String... tokens) {
		int pageNumber = 1;
		int retCount = 10;
		boolean homepagePassed = false;
		if(tokens != null){
			if(tokens.length > 0)
				pageNumber = NumberUtils.toInt(tokens[0],1);
			if(tokens.length > 1)
				retCount = NumberUtils.toInt(tokens[1],10);
			if(tokens.length > 2)
				homepagePassed = BooleanUtils.toBoolean(tokens[2]);
		}
		//must use spaceUname in method parameters
		Space space = spaceService.getSpaceByUname(spaceUname);
		Page page = space.getHomepage(); 
		securityService.fillPageWikiOperations(WikiUtil.getUser(userReadingService), page);
		
		page.setRenderPieces(getBlogHomePage(spaceUname, pageNumber,retCount));
		
		return page;
	}

	//********************************************************************
	//               private methods
	//********************************************************************
	private List<RenderPiece> getBlogHomePage(String spaceUname, int pageNumber, int retCount){
		int start = retCount * (pageNumber - 1);
		
		Space space = spaceService.getSpaceByUname(spaceUname);
		Page home = space.getHomepage();
		//get 3 more: one is for possible home page passed move. one is for possible home inside, one is for "next" button check
		boolean sortyByModify = true;
		if((space.getSetting().getWidgetStyle() & SpaceSetting.WIDGET_STYLE_ITEM_SHORT_BY_CREATE_DATE) > 0){
			sortyByModify = false;
		}
		
		//TODO: doesn't check page permission for viewer
		List<Page> pages = spaceService.getRecentPages(spaceUname, start, retCount + 3,sortyByModify);

		//NOTE: following code will try to remove HomePage from page list, this piece code ONLY works if return page list ordered by ModifiedDate
		if(pages.size() > 0 && home != null && !home.isRemoved()){
			Page first = pages.get(0);
			if(home.getModifiedDate().after(first.getModifiedDate())){
				//home page already removed before this page, the first page in this list already display in last page, then remove it.
				pages.remove(0);
			}
			//check if home page is inside this return list, if so, remove it
			for (Iterator<Page> iter=pages.iterator();iter.hasNext();) {
				if(home.equals(iter.next())){
					iter.remove();
					break;
				}
			}
		}		
		boolean hasNext = pages.size() > retCount;
		boolean hasPrevious = pageNumber > 1;
		if (hasNext) pages = pages.subList(0, retCount);
		return blogService.renderBlog(pages, pageNumber,retCount, hasPrevious, hasNext);

	}
}
