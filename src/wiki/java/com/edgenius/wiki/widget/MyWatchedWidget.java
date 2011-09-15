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

import java.util.List;

import org.springframework.context.ApplicationContext;

import com.edgenius.core.model.User;
import com.edgenius.core.service.MessageService;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.PageUtil;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.service.PageService;

/**
 * @author Dapeng.Ni
 */
public class MyWatchedWidget extends AbstractWidgetTemplate {
	private Widget obj = null;
	private PageService pageService;
	private MessageService messageService;
	
	@Override
	public boolean isAllowView(User viewer) {
		if(viewer == null || viewer.isAnonymous())
			return false;
		else
			return true;
	}
	public void reset() {
		obj = null;
	}
	//JDK1.6 @Override
	public Widget createWidgetObject(String key) {
		if(obj == null){
			obj = new Widget();
			obj.setType(getType());
			obj.setUuid(SharedConstants.WATCHLIST_KEY);
			obj.setTitle(messageService.getMessage(WikiConstants.I18N_WATCHLIST_TITLT));
			obj.setDescription(messageService.getMessage(WikiConstants.I18N_WATCHLIST_DESC));
		}
		return obj;
	}
	
	public void init(ApplicationContext applicationContext){
		pageService = (PageService) applicationContext.getBean(PageService.SERVICE_NAME);
		messageService = (MessageService) applicationContext.getBean(MessageService.SERVICE_NAME);
	}
	//JDK1.6 @Override
	public Widget invoke(String key,User viewer) {
	
		String username = viewer.getUsername();
		List<Page> pages = pageService.getWatchedPages(username);
		log.info("Get user " + username + " personal page type " + getType());
		
		PageItemListModel model = new PageItemListModel();
		model.itemList =  PageUtil.copyPageItem(pages,0);
		
		Widget obj = new Widget();
		obj.setRenderContent(model);
		return obj;
	}


}
