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

import com.edgenius.core.model.TouchedInfo;
import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.DateUtil;
import com.edgenius.wiki.gwt.client.model.MessageListModel;
import com.edgenius.wiki.gwt.client.model.MessageModel;
import com.edgenius.wiki.model.Notification;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
public class MessageUtil {

	/**
	 * @param list
	 * @return
	 */
	public static MessageListModel copyMessageToListModel(List<Notification> list,  UserReadingService userReadingService) {
		MessageListModel model = new MessageListModel(); 
		if(list != null){
			for (Notification notification : list) {
				//only show title in MesagePortlet
				MessageModel msg = new MessageModel();
				copyMessageToModel(notification,msg, userReadingService);
				model.list.add(msg);
			}
		}
		return model;
	}
	public static  MessageModel copyMessageToModel(Notification msg, MessageModel model, UserReadingService userReadingService) {
		copyTouchInfo(msg, model,userReadingService);
		
		model.targetType = msg.getTargetType();
		model.target = msg.getTargetName();
		model.text = msg.getMessage();
		model.uid = msg.getUid();
		model.removable = msg.isRemovable();
		
		return model;
	}
	
	private  static void copyTouchInfo(TouchedInfo msg, MessageModel model, UserReadingService userReadingService) {
		if(msg.getCreator() != null){
			model.authorUid = msg.getCreator().getUid();
			model.authorUsername= msg.getCreator().getUsername();
			model.authorPortrait =  UserUtil.getPortraitUrl(msg.getCreator().getPortrait());
		}else{
			//Anonymous
			User anony = WikiUtil.getAnonymous(userReadingService);
			model.authorUid = anony.getUid();
			model.authorUsername =  anony.getUsername();
			model.authorPortrait =  UserUtil.getPortraitUrl(null);
		}
		
		User currUser = WikiUtil.getUser();
		model.date = DateUtil.getLocalDate(currUser, msg.getCreatedDate());
	}
}
