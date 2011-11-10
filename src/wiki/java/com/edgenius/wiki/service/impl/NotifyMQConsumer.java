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
package com.edgenius.wiki.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;

import com.edgenius.core.Global;
import com.edgenius.core.model.User;
import com.edgenius.core.service.MailService;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.Shell;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.blogsync.BlogSyncException;
import com.edgenius.wiki.blogsync.BlogSyncService;
import com.edgenius.wiki.dao.UserPageDAO;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.UserPageMark;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.CommentException;
import com.edgenius.wiki.service.CommentService;
import com.edgenius.wiki.service.NotifyMQObject;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * The reason why using this MQConsumer to send email rather than using MailMQProducer is, 
 * this one handles all email build logic (such as organize mail body, mail list address etc.)
 * This may save performance as this class is executing behind MQ consumer.
 * 
 * @author Dapeng.Ni
 */
public class NotifyMQConsumer {
	private static final Logger log = LoggerFactory.getLogger(NotifyMQConsumer.class);
	
	//directory user mailEngine rather using a MQ mail service
	private MailService mailEngine;
	private UserPageDAO userPageDAO;
	private UserReadingService userReadingService; 
	private PageService pageService;
	private CommentService commentService;
	private BlogSyncService blogSyncService;
	private SecurityService securityService;

	//********************************************************************
	//               Function methods
	//********************************************************************
	public void handleMessage(Object msg){
		NotifyMQObject mqObj;
		try {
			if(msg instanceof NotifyMQObject){
				mqObj = (NotifyMQObject) msg;
				securityService.proxyLogin(mqObj.getUsername());
				
				if(mqObj.getType() == NotifyMQObject.TYPE_PAGE_UPDATE){
					sendPageUpdateNodification(mqObj.getPageUid());
				}else if(mqObj.getType() == NotifyMQObject.TYPE_SPACE_REMOVE){
					sendSpaceRemovingNotification(mqObj.getSpace(),mqObj.getRemoveDelayHours());
				}else if(mqObj.getType() == NotifyMQObject.TYPE_COMMENT_NOTIFY){
					sendPageCommentsNotification(mqObj.getUsername(), mqObj.getPageUid(), mqObj.getCommentUid());
				}else if(mqObj.getType() == NotifyMQObject.TYPE_EXT_LINK_BLOG){
					syncExtBlog( mqObj.getSpaceUname(),mqObj.getBlogMeta(), mqObj.getSyncLimit());
				}else if(mqObj.getType() == NotifyMQObject.TYPE_EXT_POST){
					postBlog(mqObj.getBlogMeta(), mqObj.getId());
				}else if(mqObj.getType() == NotifyMQObject.TYPE_EXT_POST_COMMENT){
					postBlogComment(mqObj.getBlogMeta(), mqObj.getId());
				}else if(mqObj.getType() == NotifyMQObject.TYPE_EXT_REMOVE_POST){
					removeBlogPost(mqObj.getUsername(), mqObj.getBlogMeta(), mqObj.getId());
				}else if(mqObj.getType() == NotifyMQObject.TYPE_SPACE_MEUN_UPDATED){
					updateSpaceMenu(mqObj.getSpaceUname());
				}
			}else{
				AuditLogger.error("Unexpected object in Index Counsumer " + msg);
				return;
			}
		}finally{
			securityService.proxyLogout();
		}
			
		
	}
	
	/**
	 * Shell request - to update space menu for this space
	 * @param spaceUname
	 */
	private void updateSpaceMenu(String spaceUname) {
		Shell.notifySpaceCreate(spaceUname);
	}

	/**
	 * @param username
	 * @param blogMeta
	 * @param postID
	 */
	private void removeBlogPost(String username, BlogMeta blogMeta, String postID) {
		try {
			blogSyncService.removePost(blogMeta, postID);
		} catch (BlogSyncException e) {
			log.error("Unable to post blog comment:" + blogMeta,e);
		} 
	}

	/**
	 * @param username
	 * @param blogMeta
	 * @param pageComment
	 */
	private void postBlogComment(BlogMeta blogMeta, String commentID) {
		try {
			PageComment pageComment = commentService.getComment(NumberUtils.toInt(commentID));
			if(pageComment != null)
				blogSyncService.postComment(blogMeta, pageComment);
		} catch (BlogSyncException e) {
			log.error("Unable to post blog comment:" + blogMeta,e);
		} 
	}

	/**
	 * @param username
	 * @param blogMeta
	 * @param page
	 */
	private void postBlog(BlogMeta blogMeta, String pageUid) {
		try {
			Page page = pageService.getPage(NumberUtils.toInt(pageUid));
			if(page != null)
				blogSyncService.post(blogMeta, page);
		} catch (BlogSyncException e) {
			log.error("Unable to post blog:" + blogMeta,e);
		}
		
	}

	/**
	 * @param spaceUname
	 * @param xmlrpc
	 * @param username
	 * @param password
	 */
	private void syncExtBlog(String spaceUname,BlogMeta meta, int limit) {
		try {
			blogSyncService.downloadPosts(spaceUname, meta, limit);
			blogSyncService.downloadComments(spaceUname, meta);
			
			
			//only upload pages which anonymous is able to see.
			List<Page> pages = pageService.getPagesInSpace(spaceUname, null, 0, userReadingService.getUserByName(null));
			for (Page page : pages) {
				blogSyncService.post(meta, page);
				List<PageComment> comments;
				try {
					comments = commentService.getPageComments(spaceUname, page.getPageUuid());
					for (PageComment pageComment : comments) {
						blogSyncService.postComment(meta, pageComment);
					}
				} catch (CommentException e) {
					log.error("Unable to get page comment",e);
				}
			}
			
		} catch (BlogSyncException e) {
			log.error("Unable to download blog:" + meta,e);
		}
	}

	/**
	 * @param space
	 * @param removeDelayHours
	 */
	private void sendPageCommentsNotification(String username, Integer pageUid, Integer commentUid) {

		Page page = pageService.getPage(pageUid);
		if(page == null){
			log.error("Unable to get page by uid {}, send page comment notificaiton failed.", pageUid);
			return;
		}
		
		String comment = null;
		if(commentUid == null){
			//send daily digest
			comment = ""; //TODO
		}else{
			PageComment pageComment = commentService.getComment(commentUid);
			if(pageComment != null)
				comment = pageComment.getBody();
		}
		
		if(comment == null){
			log.error("Unable to get comment by uid {}, send page comment notificaiton failed.", commentUid);
			return;
		}
		
		int nType = page.getSpace().getSetting().getCommentNotifyType();
		String spaceUname = page.getSpace().getUnixName();
		Set<String> bccList =  new HashSet<String>();
		if((nType & SpaceSetting.COMMENT_NOTIFY_TO_AUTHOR) != 0){
			if(page.getCreator() != null && page.getCreator().getContact() != null) 
				bccList.add(page.getCreator().getContact().getEmail());
		}
		
		if((nType & SpaceSetting.COMMENT_NOTIFY_TO_ALL_CONTRIBUTOR) != 0){
			Set<User> contributor = pageService.getPageContributors(page.getPageUuid());
			for (User user : contributor) {
				if(user.getContact() != null)
					bccList.add(user.getContact().getEmail());
			}
		}
		if((nType & SpaceSetting.COMMENT_NOTIFY_TO_SPACE_OWNEER) != 0){
			Set<String> adminList = userReadingService.getSpaceAdminMailList(spaceUname);
			if(adminList != null)
				bccList.addAll(adminList);
		}
		
		Map<String,Object> model = new HashMap<String,Object>();
		String link = WikiUtil.getPageRedirFullURL(spaceUname, page.getTitle(),page.getPageUuid());
		model.put(WikiConstants.ATTR_PAGE_LINK, link);
		model.put(WikiConstants.ATTR_PAGE_TITLE, page.getTitle());
		model.put(WikiConstants.ATTR_CONTENT, comment);
		if((nType & SpaceSetting.COMMENT_NOTIFY_FEQ_EVERY_POST) != 0){
			//send every post
			sendMail(WikiConstants.MAIL_TEMPL_COMMENT_PER_POST,bccList, model);
			
		}else{
			//send daily summary
			model.put(WikiConstants.ATTR_COUNT, commentService.getDailyCommentCount(pageUid));
			sendMail(WikiConstants.MAIL_TEMPL_COMMENT_DAILY_SUM,bccList, model);
		}	
		
	}

	/**
	 * @param spaceUname
	 */
	private void sendSpaceRemovingNotification(Space space, int removeDelayHours) {
		
		String spaceUname = space.getUnixName();
		
		//all email receivers are in BCC list, rather than TO or CC
		Set<String> bccList = space.getAdminMailList();
		
		//if space removed, bccList are already filled before space permanently deleted.
		//if space is going remove, here will fill bccList.
		if(bccList == null){
			bccList = userReadingService.getSpaceAdminMailList(spaceUname);
		}
		if(bccList.size() > 0){
			Map<String,Object> model = new HashMap<String,Object>();
			model.put(WikiConstants.ATTR_ADMIN_URL, WikiUtil.getSystemAdminTokenLink());
			model.put(WikiConstants.ATTR_SPACE, space);
			SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mma");
			model.put(WikiConstants.MODIFIED_DATE, format.format(space.getModifiedDate()));
			String link = WikiUtil.getPageRedirFullURL(spaceUname,null,null);
			model.put(WikiConstants.ATTR_PAGE_LINK, link);
			model.put(WikiConstants.ATTR_REMOVE_DELAY_HOURS,removeDelayHours );
			if(removeDelayHours > 0){
				sendMail(WikiConstants.MAIL_TEMPL_SPACE_REMOVING,bccList, model);
			}else{
				sendMail(WikiConstants.MAIL_TEMPL_SPACE_REMOVED,bccList, model);
			}
		}
	}

	/**
	 * @param page
	 */
	private void sendPageUpdateNodification(Integer pageUid) {
		List<UserPageMark> watched = userPageDAO.getWatchedByPageUid(pageUid);
		if(watched != null && watched.size() != 0){
			Page page = pageService.getPage(pageUid);
			if(page != null){ 
				log.info("Prepare sending email for page : " + page.getTitle());
				
				List<String> bccList = new ArrayList<String>();
				for (UserPageMark userPageMark: watched) {
					String email = null;
					try {
						email = userPageMark.getUser().getContact().getEmail();
					} catch (Exception e) {
						log.warn("Can not find user contact email for " + userPageMark.getUser());
					}
					if(email != null)
						bccList.add(email);
				}
				if(bccList.size() > 0){
					Map<String,Object> model = new HashMap<String,Object>();
					model.put(WikiConstants.ATTR_PAGE, page);
					String modifier;
					if(page.getModifier()!=null){
						modifier  = page.getModifier().getFullname();
					}else{
						//Anonymous
						User anony = WikiUtil.getAnonymous(userReadingService);
						modifier =  anony.getFullname();
					}
					model.put(WikiConstants.MODIFIER_FULLNAME, modifier);
					SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mma");
					model.put(WikiConstants.MODIFIED_DATE, format.format(page.getModifiedDate()));
					String link = WikiUtil.getPageRedirFullURL(page.getSpace().getUnixName(),page.getTitle(),page.getPageUuid());
					model.put(WikiConstants.ATTR_PAGE_LINK, link);
					
					sendMail(WikiConstants.MAIL_TEMPL_PAGE_CHANGE_NOTIFICATION, bccList, model);
				}
			}else{
				log.warn("Page {} doesn't exist, No watched email send out.", pageUid);
			}
		}else{
			log.info("No user watches for this page. No email send out.");
		}
	}

	/**
	 * @param msg
	 * @param bccList
	 * @param model
	 */
	private void sendMail(String templName, Collection<String> bccList, Map<String, Object> model) {

		SimpleMailMessage msg = new SimpleMailMessage();
		//So far don't user setBcc(bccList) to all user since if one of them mail address is bad, it may cause 
		//al emails can not be send.
		for (String bcc : bccList) {
			//because message "TO" user can not be blank, so here just add default mail address but add 
			//the really recipient on BCC in order to hide user private email info.
			msg.setTo(bcc);
			msg.setFrom(Global.DefaultNotifyMail);
			try {
				//whatever error happen, go on to next user
				mailEngine.sendPlainMail(msg, templName, model);
			} catch (Throwable e) {
				log.error("Send Page Notify mail failed to " + bcc + ". "  , e);
			}
		}
		log.info("Email sent to " + bccList.size() + " users.");
	}

	//********************************************************************
	//               set / get
	//********************************************************************
	public void setUserPageDAO(UserPageDAO userPageDAO) {
		this.userPageDAO = userPageDAO;
	}

	public void setMailEngine(MailService mailEngine) {
		this.mailEngine = mailEngine;
	}

	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}

	public void setPageService(PageService pageService) {
		this.pageService = pageService;
	}

	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	public void setBlogSyncService(BlogSyncService blogSyncService) {
		this.blogSyncService = blogSyncService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

}
