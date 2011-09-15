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

import org.apache.commons.lang.StringUtils;

import com.edgenius.wiki.gwt.client.model.MessageListModel;
import com.edgenius.wiki.gwt.client.server.NotificationController;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.handler.GWTSpringController;
import com.edgenius.wiki.model.Notification;
import com.edgenius.wiki.service.NotificationService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class NotificationControllerImpl  extends GWTSpringController implements NotificationController {
	
	private NotificationService notificationService;
	
	/**
	 * @param targetType - null means all users; See values from com.edgenius.wiki.gwt.client.server.utils.SharedConstants.MEG_TARGET_TYPE_*. 
	 * @param targetName - spaceUname or User name
	 * @param messagePortlet
	 */

	public MessageListModel sendMessage(Integer targetType, String targetName, String text, boolean withResponsMsgList) {
		//if send to admin, the targetName is null, but targetType is not
		if(targetType == null && StringUtils.isBlank(targetName)){
			targetType = SharedConstants.MSG_TARGET_ALL_USERS;
		}
		
		notificationService.sendMessage(text,targetType,targetName, NotificationService.SEND_MAIL_ONLY_HAS_RECEIVERS);
		
		if(withResponsMsgList){
			//post new message, then always return 1st page.
			return getMessages(1);
		}else{
			return new MessageListModel();
		}
	}
	public MessageListModel sendTwitterMessage(String text, boolean withResponsMsgList) {

		notificationService.sendTwitterMessage(text, NotificationService.SEND_MAIL_ONLY_HAS_RECEIVERS);
		
		if(withResponsMsgList){
			//post new message, then always return 1st page.
			return getMessages(1);
		}else{
			return new MessageListModel();
		}
	}

	/**
	 * Get current login user message with current page.
	 * @param currentPage
	 * @return
	 */
	public MessageListModel getMessages(int currentPage) {
		
		if(currentPage < 1)
			currentPage = 1;
		int start = (currentPage-1) *  SharedConstants.MSG_ITEM_COUNT_IN_MSG_BOARD;
		
		//try to return 1 more than request - then know if it has next page.
		List<Notification> notifications = notificationService.getUserMessages(WikiUtil.getUser(), start, SharedConstants.MSG_ITEM_COUNT_IN_MSG_BOARD +1);
		
		MessageListModel model = MessageUtil.copyMessageToListModel(notifications, userReadingService);
		model.hasNxt = notifications.size() > SharedConstants.MSG_ITEM_COUNT_IN_MSG_BOARD;
		model.hasPre = currentPage > 1;
		model.currentPage = currentPage;
		
		return model;
	}

	public MessageListModel deleteMessage(int currentPage, Integer msgUid) {
		if(msgUid != null)
			notificationService.removeMessage(msgUid);
		
		return getMessages(currentPage);
	}

	//********************************************************************
	//               set / get
	//********************************************************************
	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}


}
