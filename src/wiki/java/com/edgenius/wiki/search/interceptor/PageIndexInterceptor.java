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

import java.lang.reflect.Method;
import java.util.List;

import javax.jms.Queue;

import org.apache.commons.lang.StringUtils;

import com.edgenius.wiki.Shell;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.rss.RSSMQObject;
import com.edgenius.wiki.service.NotifyMQObject;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * Please be advised, this interceptor is after transaction interceptor, so any exception in this class won't roll back the transaction.
 * 
 * This class also handle sending RSS feed rebuild message. This is not good mix, but it seems no necessary 
 * to create a new intercepter for RSS. 
 * @author Dapeng.Ni
 */
public class PageIndexInterceptor extends IndexInterceptor {
	
	private Queue rssQueue;
	private Queue notifyQueue;
	
	public void afterReturning(Object retValue, Method method, Object[] args, Object target) throws Throwable {
		//PageService.savePage()
		Page page = null;
		String spaceUname = null;
		String removedPageUuid = null;
		boolean needRebuildRss = false;

		if(StringUtils.equals(method.getName(), PageService.savePage)){
			page = (Page) retValue;
			int reqireSendNotify = (Integer) args[1];
			spaceUname = page.getSpace().getUnixName();
			needRebuildRss = true;
			
			sendNotifyMessage(page, reqireSendNotify);
		}else if(StringUtils.equals(method.getName(), SpaceService.saveHomepage)){
			page = (Page) args[1];
			spaceUname = page.getSpace().getUnixName();
			needRebuildRss = true;
		}else if(StringUtils.equals(method.getName(), PageService.copy)){
			page = (Page) retValue;
			spaceUname = page.getSpace().getUnixName();
			//rebuild target space RSS
			needRebuildRss = true;
		}else if(StringUtils.equals(method.getName(), PageService.removePage)){
			//new page
			spaceUname = (String) args[0];
			removedPageUuid =  (String) args[1];
			page = (Page) retValue;
			
			sendPageRemoveMessage(removedPageUuid, spaceUname,page);
			
		}else if(StringUtils.equals(method.getName(), PageService.restorePage)){
			page = (Page) retValue;
			//Dirty FIX: MQ consumer can not get content correctly since lazy loading reason, initialize here.
			page.getContent().getContent();
			spaceUname = page.getSpace().getUnixName();
			needRebuildRss = true;
		}else if(StringUtils.equals(method.getName(), PageService.restoreHistory)){
			page = (Page) retValue;
			//Dirty FIX: MQ consumer can not get content correctly since lazy loading reason, initialize here.
			page.getContent().getContent();
			spaceUname = page.getSpace().getUnixName();
			needRebuildRss = true;
		}else if(StringUtils.equals(method.getName(), PageService.move)){
			//new page
			//also need do delete from index
			String fromSpaceUname =  (String) args[0];
			//this flag will build move target space RSS
			spaceUname = (String) args[2];

			if(!StringUtils.equalsIgnoreCase(fromSpaceUname, spaceUname)){
				page = (Page) retValue;
				removedPageUuid =  (String) args[1];
				needRebuildRss = true; //rebuild to  space RSS 
				sendPageRemoveMessage(removedPageUuid, fromSpaceUname,page); //here will remove page RSS from fromSpaceUname
			}else{
				//if move happens in same space, then we need don't refresh index and rss!
				//actually, even no return, all below index and rss service also skipped, here just for easy code. 
				return;
			}
		}
		
		if(page != null && removedPageUuid == null && !page.isRemoved()){
			log.info("JMS message send for Page index creating. Title: " + page.getTitle() + ". Uuid:" + page.getPageUuid());
			//the come in page bring too many information, such as user permission, roles etc. 
			//in test, the page object is almost 680k in a ~700 user system for admin user.
			//so, as trade-off, low the traffice in MQ, but add more database query in consumer side... 
			IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_INSERT_PAGE,page.getUid());
			jmsTemplate.convertAndSend(queue, mqObj);

		}
		if(needRebuildRss){
			//send RSS feed rebuild message
			RSSMQObject mqObj = new RSSMQObject(RSSMQObject.TYPE_REBUILD,spaceUname);
			jmsTemplate.convertAndSend(rssQueue, mqObj);
		}
		

	}

	/**
	 * @param page
	 * @param reqireSendNotify

	 */
	private void sendNotifyMessage(Page page, int reqireSendNotify) throws Exception{
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// EMAIL NOTIFY MESSAGE
		if((reqireSendNotify & WikiConstants.NOTIFY_EMAIL) > 0){
			NotifyMQObject pnObj = new NotifyMQObject(NotifyMQObject.TYPE_PAGE_UPDATE, WikiUtil.getUserName(), page.getUid()); 
			jmsTemplate.convertAndSend(notifyQueue, pnObj);
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// EMAIL NOTIFY MESSAGE
		if(Shell.enabled && page.isMenuUpdated() && page.getSpace().isPrivate()){
			NotifyMQObject pnObj = new NotifyMQObject(NotifyMQObject.TYPE_SPACE_MEUN_UPDATED, page.getSpace().getUnixName()); 
			jmsTemplate.convertAndSend(notifyQueue, pnObj);
		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// BLOG NOTIFY MESSAGE
		if((reqireSendNotify & WikiConstants.NOTIFY_BLOG) > 0 && page.getSpace().containExtLinkType(Space.EXT_LINK_BLOG)){
			SpaceSetting setting = page.getSpace().getSetting();
			List<BlogMeta> blogs = setting.getLinkedMetas();
			if(blogs != null && blogs.size() > 0){
				for (BlogMeta blog : blogs) {
					//don't update setting.linkMeta as it will impact persist object in database
					BlogMeta meta = (BlogMeta) blog.clone();
					meta.setPassword(setting.restorePlainPassword(meta.getPassword()));
					
					//load lazy loading object
					page.getPageProgress();
					NotifyMQObject pnObj = new NotifyMQObject(NotifyMQObject.TYPE_EXT_POST, WikiUtil.getUserName(), meta , String.valueOf(page.getUid())); 
					jmsTemplate.convertAndSend(notifyQueue, pnObj);
				}
			}
		}

	}
	/**
	 * @param removedPageUuid
	 * @param spaceUname
	 */
	private void sendPageRemoveMessage(String removedPageUuid, String spaceUname, Page page) throws Exception{
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// INDEX MESSAGE
		log.info("JMS message send for Page index remove. Removed Page UUID: " + removedPageUuid);
		IndexMQObject idxObj = new IndexMQObject(IndexMQObject.TYPE_REMOVE_PAGE, new String[]{spaceUname, removedPageUuid});
		jmsTemplate.convertAndSend(queue, idxObj);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// RSS MESSAGE
		//try to rebuild both from/to space RSS
		//this will try to rebuild source space RSS if this page is inside current RSS
		RSSMQObject mqObj = new RSSMQObject(RSSMQObject.TYPE_ITEM_REMOVE,spaceUname,removedPageUuid);
		jmsTemplate.convertAndSend(rssQueue, mqObj);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// BLOG NOTIFY MESSAGE
		if(page != null && page.getSpace().containExtLinkType(Space.EXT_LINK_BLOG)
			&& page.getPageProgress() != null && page.getPageProgress().getLinkExtID() != null){
			SpaceSetting setting = page.getSpace().getSetting();
			List<BlogMeta> blogs = setting.getLinkedMetas();
			if(blogs != null && blogs.size() > 0){
				for (BlogMeta blog : blogs) {
					//don't update setting.linkMeta as it will impact persist object in database
					BlogMeta meta = (BlogMeta) blog.clone();
					meta.setPassword(setting.restorePlainPassword(meta.getPassword()));
					
					NotifyMQObject pnObj = new NotifyMQObject(NotifyMQObject.TYPE_EXT_REMOVE_POST, WikiUtil.getUserName(), meta , page.getPageProgress().getLinkExtID()); 
					jmsTemplate.convertAndSend(notifyQueue, pnObj);
				}
			}
		}

	}

	//********************************************************************
	//               set / get
	//********************************************************************
	public void setRssQueue(Queue rssQueue) {
		this.rssQueue = rssQueue;
	}

	public void setNotifyQueue(Queue pageNotifyQueue) {
		this.notifyQueue = pageNotifyQueue;
	}

}
