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
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.dao.CommentDAO;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.CommentService;
import com.edgenius.wiki.service.NotifyMQObject;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@Transactional
public class CommentIndexInterceptor extends IndexInterceptor {
	private CommentDAO commentDAO;
	private Queue notifyQueue;
	
	@SuppressWarnings("unchecked")
	public void afterReturning(Object retValue, Method method, Object[] args, Object target) throws Throwable {

		if(StringUtils.equals(method.getName(), CommentService.createComment)){
			PageComment comment =  (PageComment) retValue;
			
			log.info("JMS message send for comment index creating/updating.");
			IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_INSERT_COMMENT, comment.getUid());
			jmsTemplate.convertAndSend(queue, mqObj);
			
			int requireNotify = (Integer) args[3];
			sendPostNotify(comment, requireNotify);
			
		}else if(StringUtils.equals(method.getName(), CommentService.sendDailyCommentNotify)){
			log.info("Send daily comment notify is invoked.");
			List<Integer> pageUidList = commentDAO.getNeedNotifyCommentPageUids();
			if(pageUidList != null){
				for (Integer pageUid : pageUidList) {
					sendEmailNotify(null, pageUid);
				}
			}
			//clean all comment notify flag, whatever it is daily summary or per post 
			commentDAO.cleanNotifyFlag();
		}else if(StringUtils.equals(method.getName(), CommentService.removePageComments)){
			List<PageComment> comments =  (List<PageComment>) retValue;
			if(comments != null){
				for (PageComment pageComment : comments) {
					log.info("JMS message send for comment index delete.");
					IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_REMOVE_COMMENT,pageComment.getUid());
					jmsTemplate.convertAndSend(queue, mqObj);
				}
			}
		}

	}

	/**
	 * @param comment
	 * @param page
	 * @param setting
	 * @param requireNotify
	 * 
	 */
	private void sendPostNotify(PageComment comment, int requireNotify)
			throws Exception {

		//this is also for avoid LazyInitializationException
		Page page = comment.getPage();
		SpaceSetting setting = page.getSpace().getSetting();
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// EMAIL NTOIFY MESSAGE
		int nType =setting.getCommentNotifyType();
		if((requireNotify & WikiConstants.NOTIFY_EMAIL) > 0 && (nType & SpaceSetting.COMMENT_NOTIFY_FEQ_EVERY_POST) > 0){
			//if need send per post, it also need check how many sent out today! to avoid spam
			int count = commentDAO.getNotifySentCount(page.getUid());
			if(count <= setting.getCommentNotifyMaxPerDay()){
				sendEmailNotify(comment.getUid(), page.getUid());
			}
		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// BLOG NOTIFY MESSSAGE
		if((requireNotify & WikiConstants.NOTIFY_BLOG) > 0 && page.getSpace().containExtLinkType(Space.EXT_LINK_BLOG)){
			List<BlogMeta> blogs = setting.getLinkedMetas();
			if(blogs != null && blogs.size() > 0){
				for (BlogMeta blog : blogs) {
					//don't update setting.linkMeta as it will impact persist object in database
					BlogMeta meta = (BlogMeta) blog.clone();
					
					meta.setPassword(setting.restorePlainPassword(meta.getPassword()));
					
					NotifyMQObject pnObj = new NotifyMQObject(NotifyMQObject.TYPE_EXT_POST_COMMENT, WikiUtil.getUserName(), meta , String.valueOf(comment.getUid())); 
					jmsTemplate.convertAndSend(notifyQueue, pnObj);
				}
			}
		}			
		

	}

	/**
	 * @param pageUid
	 */
	private void sendEmailNotify(Integer commentUid, Integer pageUid) {
		NotifyMQObject pnObj = new NotifyMQObject(NotifyMQObject.TYPE_COMMENT_NOTIFY, WikiUtil.getUserName(), pageUid, commentUid); 
		jmsTemplate.convertAndSend(notifyQueue, pnObj);
	}

	//********************************************************************
	//               set / get
	//********************************************************************
	public void setCommentDAO(CommentDAO commentDAO) {
		this.commentDAO = commentDAO;
	}

	public void setNotifyQueue(Queue pageNotifyQueue) {
		this.notifyQueue = pageNotifyQueue;
	}

}
