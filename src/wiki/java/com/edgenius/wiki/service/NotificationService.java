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
package com.edgenius.wiki.service;

import java.util.List;

import com.edgenius.core.model.User;
import com.edgenius.wiki.model.Notification;
import com.edgenius.wiki.security.service.SecurityCheckException;

/**
 * @author Dapeng.Ni
 */
public interface NotificationService {

	String SERVICE_NAME = "notificationService";

	public static final int SEND_MAIL_DISABLED=0;
	//send email only 
	public static final int SEND_MAIL_ONLY_HAS_RECEIVERS=1;
	//send email always
	public static final int SEND_MAIL_ALAWYS=2;
	
	//method call in spring ApplicationContext
	void initScheduledJob();
	/**
	 * 
	 * @param msgUid
	 * @param viewer
	 * @return
	 * @throws SecurityCheckException if view has no permission to read this message
	 */
	Notification getMessage(Integer msgUid,User viewer) throws SecurityCheckException;

	/**
	 * Get all message for this user.
	 * It may send to space, in which the user is admin role.
	 * It may system message, all user will get.
	 * TODO:
	 * If user is anonymous, Does it can not see system message? Currently, no because the anonymous can not add
	 * message portlet to dashboard so far.  
	 * @param user
	 * @param retCount
	 * @param i 
	 * @return
	 */
	List<Notification> getUserMessages(User user, int start, int retCount);

	void sendMessage(String message, int targetType, String targetName,int withEmail);

	/**
	 * This method text could include receiver message by a kind of twitter format, such as
	 *  Message format: 
	 * <li>@receiver msg</li>
	 * <li>@@space msg</li>
	 * <li>@@'space with space' msg</li>
	 * <li>@receiver1 @receiver2 @'space name' msg</li>
	 * 
	 * @param text
	 * @param sendEmail - see static variables SEND_MAIL_*
	 */
	void sendTwitterMessage(String text, int sendEmail);
	/**
	 * Maybe this is good place to put this method! I just put it temporarily before I find(or create) a suitable Service bean.
	 * 
	 * Check Application version, if new version found, send message to system administrator.
	 */
	void doVersionCheck();
	/**
	 * @param msgUid
	 */
	void removeMessage(Integer msgUid);

}
