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
package com.edgenius.wiki.search.interceptor;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.jms.Queue;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.Global;
import com.edgenius.wiki.Shell;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.blogsync.BlogSyncService;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.rss.RSSMQObject;
import com.edgenius.wiki.service.NotifyMQObject;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
public class SpaceIndexInterceptor extends IndexInterceptor {

	private Queue rssQueue;
	private Queue notifyQueue;

	public void afterReturning(Object retValue, Method method, Object[] args, Object target) throws Throwable {
		Space space = null;
		String removedSpaceUname = null;
		
		if(StringUtils.equals(method.getName(), SpaceService.saveHomepage)){
			//save space: does not interceptor saveSpace(),  saveHomepage() will call immediately after saveSpace();
			space = (Space) retValue;

			if(space.containExtLinkType(Space.EXT_LINK_BLOG)){
				sendBlogLinkNotify(space);
			}
		}else if(StringUtils.equals(method.getName(),SpaceService.uploadLogo)){
			if(Shell.enabled && Global.restServiceEnabled){
				//send space update indexing request - although this not necessary for indexing but will trigger Shell request
				space = (Space) args[0];
				if(space != null){
					IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_INSERT_SPACE,space.getUid());
					jmsTemplate.convertAndSend(queue, mqObj);
				}
			}
			
			//don't do further update
			return;
		}else if(StringUtils.equals(method.getName(),SpaceService.updateSpace)){
			space = (Space) retValue;
			
			if(space.containExtLinkType(Space.EXT_LINK_BLOG)){
				sendBlogLinkNotify(space);
			}
			
			//don't update space index - so don't continue run
			if(!((Boolean) args[1]))
				return;
		}else if(StringUtils.equals(method.getName(), SpaceService.removeSpace)){
			removedSpaceUname =  (String) args[0];
			//delete RSS feed:
			log.info("JMS message send for space RSS feed remove. Title: " + removedSpaceUname);
			RSSMQObject mqObj = new RSSMQObject(RSSMQObject.TYPE_DELETE,((Space)retValue).getUid());
			jmsTemplate.convertAndSend(rssQueue, mqObj);
		}else if(StringUtils.equals(method.getName(), SpaceService.removeSpaceInDelay)){
			//only send out notification, but does not update Index!!!
			space =  (Space) retValue;
			int removeDelayHours =  (Integer) args[1];
			//please note, here must transfer space object rather than spaceUid etc. especially when removeDelayHours==0,
			//because the space may be already removed from database.
			NotifyMQObject pnObj = new NotifyMQObject(WikiUtil.getUserName(), space,removeDelayHours); 
			jmsTemplate.convertAndSend(notifyQueue, pnObj);
			if(removeDelayHours == 0){
				//this will send removeSpaceIndex message but not invoke Index insert Q message.
				removedSpaceUname = space.getUnixName();
				space = null;
			}
		}
		
		if(space != null && !space.isRemoved()){
			log.info("JMS message send for Space index creating. Title: " + space.getName() + ". Unixname:" + space.getUnixName());
			IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_INSERT_SPACE,space.getUid());
			jmsTemplate.convertAndSend(queue, mqObj);
		}
		if(removedSpaceUname != null){
			log.info("JMS message send for Space index remove. Title: " + removedSpaceUname);
			IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_REMOVE_SPACE,removedSpaceUname);
			jmsTemplate.convertAndSend(queue, mqObj);
		}
		
		
	}

	/**
	 * @param space
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws UnsupportedEncodingException
	 */
	private void sendBlogLinkNotify(Space space) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			UnsupportedEncodingException {
		SpaceSetting setting = space.getSetting();
		List<BlogMeta> blogs = setting.getLinkedMetas();
		if(blogs != null && blogs.size() > 0){
			for (BlogMeta blog : blogs) {
				//don't update setting.linkMeta as it will impact persist object in database
				BlogMeta meta = (BlogMeta) blog.clone();
				meta.setPassword(setting.restorePlainPassword(meta.getPassword()));
				
				//pass login user - ie, the space owner (admin) username as the MQ consumer can not detect what is current login user.
				NotifyMQObject pnObj = new NotifyMQObject(WikiUtil.getUserName(), space.getUnixName(),meta , BlogSyncService.INIT_DOWNLOAD_LIMIT); 
				jmsTemplate.convertAndSend(notifyQueue, pnObj);
			}
		}
	}
	
	public void setRssQueue(Queue rssQueue) {
		this.rssQueue = rssQueue;
	}

	public void setNotifyQueue(Queue notifyQueue) {
		this.notifyQueue = notifyQueue;
	}
}
