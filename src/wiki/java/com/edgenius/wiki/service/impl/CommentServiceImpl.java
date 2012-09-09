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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.dao.CommentDAO;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.quartz.PageCommentNotifyJobInvoker;
import com.edgenius.wiki.quartz.QuartzException;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.CommentException;
import com.edgenius.wiki.service.CommentService;
import com.edgenius.wiki.service.TouchService;
import com.edgenius.wiki.util.CommentComparator;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@Transactional
public class CommentServiceImpl implements CommentService{
	private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);
	
	private CommentDAO commentDAO;
	private PageDAO pageDAO;
	private UserReadingService userReadingService;
	private SecurityService securityService;
	private TouchService touchService;

	private PageCommentNotifyJobInvoker pageCommentNotifyJobInvoker;
	
	//********************************************************************
	//               function methods
	//********************************************************************
	public void updateComment(PageComment comment){
		commentDAO.saveOrUpdate(comment);
	}
	public PageComment createComment(String spaceUname, String pageUuid, PageComment comment, int notify) throws CommentException {
		WikiUtil.setTouchedInfo(userReadingService, comment);
		Page page = pageDAO.getCurrentByUuid(pageUuid);
		if(page == null){
			log.error("Unable find page for comment. Page Uid is "+ comment.getPage().getUid());
			throw new CommentException("Unable find page for comment. Page Uid is "+ comment.getPage().getUid());
		}
		//consolidate root and parent
		PageComment root = comment.getRoot();
		PageComment parent = comment.getParent();
		if(root != null){
			root = commentDAO.get(root.getUid());
			comment.setRoot(root);
		}
		if(parent != null){
			parent = commentDAO.get(parent.getUid());
			comment.setParent(parent);
			//update level and root
			comment.setLevel(parent.getLevel() + 1);
		}
		//this set will useful when comment in IndexService.
		comment.setPage(page);
		
		int nType = page.getSpace().getSetting().getCommentNotifyType();
		if((nType & SpaceSetting.COMMENT_NOTIFY_FEQ_EVERY_POST) > 0){
			//this flag is clean every night quartz job
			comment.setNotifyStatus(PageComment.NOTIFY_SENT_PER_POST);
		}else{
			comment.setNotifyStatus(PageComment.NOTIFY_SENT_SUMMARY);
		}
		commentDAO.saveOrUpdate(comment);
		
		touchService.touchPage(pageUuid);
		

		return comment;
	}
	
	@Override
	public void initCommentNotifierJob(){
		//create quartz job 
		try {
			if(pageCommentNotifyJobInvoker != null)
				pageCommentNotifyJobInvoker.invokeJob();
			else
				log.warn("Unable find comment daily notifier quartz job bean, are you in UnitTest mode?");
		} catch (QuartzException e) {
			log.error("Unable to create page comment notifier quartz job",e);
		}
	}
	//JDK1.6 @Override
	@Transactional(readOnly=true)
	public int getDailyCommentCount(Integer pageUid){
		
		return commentDAO.getNotifySentCount(pageUid);
	}

	@Transactional(readOnly=true)
	public List<PageComment> getPageComments(String spaceUname, String pageUuid) throws CommentException{
		Page page = pageDAO.getCurrentByUuid(pageUuid);
		if(page == null){
			//failure tolerance
			log.error("Unable to find page by UUID " + pageUuid + " on space " + spaceUname);
			throw new CommentException("Unable to find page by UUID " + pageUuid + " on space " + spaceUname);
		}
		List<PageComment> list = commentDAO.getCommentsByPageUid(page.getUid());
		Set<PageComment> sort = new TreeSet<PageComment>(new CommentComparator());
		sort.addAll(list);
		
		return new ArrayList<PageComment>(sort);
	}
	
	@Transactional(readOnly=true)
	public int getPageCommentCount(String spaceUname, String pageUuid) throws CommentException {
		Page page = pageDAO.getCurrentByUuid(pageUuid);
		if(page == null){
			//failure tolerance
			log.error("unable to find page by UUID " + pageUuid + " on space " + spaceUname);
			throw new CommentException("Unable to find page by UUID " + pageUuid + " on space " + spaceUname);
		}
		return commentDAO.getCommentCountByPageUid(page.getUid());
	}

	public List<PageComment> removePageComments(Integer pageUid){
		//remove comment must delete from bottom of comment tree. otherwise, it will cause Foreign key exception.
		List<PageComment> list = commentDAO.getCommentsByPageUid(pageUid);
		Set<PageComment> sort = new TreeSet<PageComment>(new CommentComparator());
		sort.addAll(list);
		list = new ArrayList<PageComment>(sort);
		Collections.reverse(list);
		
		for (PageComment pageComment : sort) {
			commentDAO.removeObject(pageComment);
		}
		
		return list;
	}
	/*
	 * Check if current login user has comment write/read permission.
	 * size is 2: [0]!=0->read [1]!=0->write
	 */
	public int[] getCommentsPermissions(String spaceUname, String pageUuid){
		Page page = pageDAO.getCurrentByUuid(pageUuid);
		securityService.fillPageWikiOperations(WikiUtil.getUser(), page);
		List<WikiOPERATIONS> perms = page.getWikiOperations();
		int[] ret= new int[]{0,0}; 
		if(perms != null){
			for (WikiOPERATIONS perm : perms) {
				if(OPERATIONS.COMMENT_WRITE.equals(perm.operation)){
					ret[1] = 1;
				}else if(OPERATIONS.COMMENT_READ.equals(perm.operation)){
					ret[0] = 1;
				}
			}
		}
		return ret;
		
	}
	//JDK1.6 @Override
	@Transactional(readOnly=true)
	public int getUserCommentSize(String username) {
		return commentDAO.getUserCommentSize(username);
	}
	//JDK1.6 @Override
	public void copyComments(Integer fromPageUid, Page toPage){
		List<PageComment> list = commentDAO.getCommentsByPageUid(fromPageUid);
		
		Set<PageComment> sort = new TreeSet<PageComment>(new CommentComparator());
		sort.addAll(list);
		list = new ArrayList<PageComment>(sort);
		Collections.reverse(list);
		
		//because this is sorted retrieve, then root, parent must before its sub/child comment. Using <oldUid,newComment>
		//map to reset current comment parent and root value.
		Map<Integer,PageComment> oldList = new HashMap<Integer,PageComment>();
		for (PageComment comment : sort) {
			Integer oldUid = comment.getUid();
			
			comment = (PageComment) comment.clone();
			comment.setUid(null);
			oldList.put(oldUid, comment);
			if(comment.getParent()  != null){
				PageComment parent = oldList.get(comment.getParent().getUid());
				comment.setParent(parent);
			}
			if(comment.getRoot() != null){
				PageComment root = oldList.get(comment.getRoot().getUid());
				comment.setRoot(root);
			}
			comment.setPage(toPage);
			commentDAO.saveOrUpdate(comment);
		}
	}

	public PageComment hideComment(Integer commentUid, boolean hide) {
		PageComment comment = commentDAO.get(commentUid);
		comment.setHide(hide);
		commentDAO.saveOrUpdate(comment);
		
		return comment;
	}
	
	@Transactional(readOnly=true)
	public PageComment getComment(Integer uid){
		if(uid == null || uid < 1)
			return null;
		
		return commentDAO.get(uid);
	}
	

	@Override
	public List<Integer> sendDailyCommentNotify() {
		//Because this method is triggered in Quartz Cron job, no hibernate session available because openSessionInView not turn on.
		//So, put all DAO operation inside this method but not in interceptor.
		log.info("Send daily comment notify is invoked.");
		List<Integer> pageUidList = commentDAO.getNeedNotifyCommentPageUids();
		
		//At this moment, email not sent yet, but we put all database operation here.
		//clean all comment notify flag, whatever it is daily summary or per post 
		commentDAO.cleanNotifyFlag();
		
		return pageUidList;
	}
	//********************************************************************
	//               Set /get 
	//********************************************************************
	public void setCommentDAO(CommentDAO commentDAO) {
		this.commentDAO = commentDAO;
	}

	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}

	public void setPageDAO(PageDAO pageDAO) {
		this.pageDAO = pageDAO;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	public void setTouchService(TouchService touchService) {
		this.touchService = touchService;
	}

	public void setPageCommentNotifyJobInvoker(PageCommentNotifyJobInvoker pageCommentNotifyJobInvoker) {
		this.pageCommentNotifyJobInvoker = pageCommentNotifyJobInvoker;
	}

	
}
