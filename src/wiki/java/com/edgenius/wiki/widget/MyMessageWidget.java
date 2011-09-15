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
import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.MessageListModel;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.MessageUtil;
import com.edgenius.wiki.model.Notification;
import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.service.NotificationService;

/**
 * @author Dapeng.Ni
 */
public class MyMessageWidget extends AbstractWidgetTemplate {
	Widget obj = null;
	private NotificationService notificationService;
	private UserReadingService userReadingService;
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
			obj.setUuid(SharedConstants.MESSAGE_BOARD_KEY);
			obj.setTitle(messageService.getMessage(WikiConstants.I18N_MSG_BOARD_TITLT));
			obj.setDescription(messageService.getMessage(WikiConstants.I18N_MSG_BOARD_DESC));
		}
		return obj;
	}
	//JDK1.6 @Override
	public Widget invoke(String key, User viewer) throws WidgetException {
		
		//invoke - get first page messages
		//try to return 1 more than request - then know if it has next page.
		List<Notification> list = notificationService.getUserMessages(viewer, 0,  SharedConstants.MSG_ITEM_COUNT_IN_MSG_BOARD +1);
		MessageListModel model = MessageUtil.copyMessageToListModel(list,  userReadingService);
		model.hasNxt = list.size() > SharedConstants.MSG_ITEM_COUNT_IN_MSG_BOARD;
		model.hasPre = false;
		model.currentPage = 1;
		
		Widget obj = new Widget();
		obj.setRenderContent(model);
		return obj;
	}
	public void init(ApplicationContext applicationContext){
		notificationService = (NotificationService) applicationContext.getBean(NotificationService.SERVICE_NAME);
		userReadingService = (UserReadingService) applicationContext.getBean(UserReadingService.SERVICE_NAME);
		messageService = (MessageService) applicationContext.getBean(MessageService.SERVICE_NAME);
	}
	
}
