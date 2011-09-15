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
package com.edgenius.wiki.gwt.client.server;

import com.edgenius.wiki.gwt.client.model.MessageListModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Dapeng.Ni
 */
public interface NotificationControllerAsync  extends RemoteServiceAsync{

	/**
	 * @param targetType - null means all users; See values from com.edgenius.wiki.gwt.client.server.utils.SharedConstants.MEG_TARGET_TYPE_*. 
	 * @param targetName - spaceUname or User name
	 * @param messagePortlet
	 */
	void sendMessage(Integer targetType, String targetName, String text, boolean withResponsMsgList, AsyncCallback<MessageListModel>  callback);

	/**
	 * This method text could include receiver message by a kind of twitter format, such as
	 *  Message format: 
	 * <li>@receiver msg</li>
	 * <li>@@space msg</li>
	 * <li>@@'space with space' msg</li>
	 * <li>@receiver1 @receiver2 @'space name' msg</li>
	 * 
	 * @param text
	 * @param withResponsMsgList
	 * @param callback
	 */
	void sendTwitterMessage(String text, boolean withResponsMsgList, AsyncCallback<MessageListModel>  callback);
	/**
	 * Get pagination messages for current login user. The page count is decide by constants. 
	 */
	void getMessages( int currentPage,  AsyncCallback<MessageListModel>  callback);

	/**
	 * @param currentPage
	 * @param uid
	 * @param notificationCallback
	 */
	void deleteMessage(int currentPage, Integer msgUid, AsyncCallback<MessageListModel>  callback);

}
