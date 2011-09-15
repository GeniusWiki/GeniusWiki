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

import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;

/**
 * @author Dapeng.Ni
 */
public interface CommentService {
	public static final String SERVICE_NAME = "commentService";

	public static final String getPageComments = "getPageComments";
	public static final String createComment = "createComment";
	public static final String sendDailyCommentNotify = "sendDailyCommentNotify";
	public static final String removePageComments = "removePageComments";


	/**
	 * @param spaceUname only useful for security validation
	 * @param pageUuid only useful for security validation
	 * @param notify - send notify to email, blog etc. Values from WikiConstants.NOTIFY_* 
	 */
	PageComment createComment(String spaceUname, String pageUuid, PageComment comment, int notify) throws CommentException ; 
	/**
	 * Use spaceUname and pageUuid as input parameters, just for system authentication require.
	 * @param spaceUname
	 * @param pageUuid
	 * @return
	 * @throws CommentException
	 */
	List<PageComment> getPageComments(String spaceUname, String pageUuid) throws CommentException ;
	/**
	 * @param pageUid
	 * @return int array by 2 element: [0]!=0->read [1]!=0->write
	 */
	int[] getCommentsPermissions(String spaceUname, String pageUuid);
	
	List<PageComment> removePageComments(Integer pageUid);
	
	void copyComments(Integer fromPageUid, Page toPage);
	/**
	 * @param spaceUname
	 * @param pageUuid
	 * @return
	 */
	int getPageCommentCount(String spaceUname, String pageUuid)  throws CommentException ;
	/**
	 * How many comments this use posted.
	 * @param username
	 * @return
	 */
	int getUserCommentSize(String username);
	
	//method call in spring ApplicationContext
	void initCommentNotifierJob();
	
	/**
	 * Send page comments update message, not exactly "daily", depends on Global setting.
	 * @param pageUuid
	 */
	
	int getDailyCommentCount(Integer pageUid);
	/**
	 * Dummy method, just for invoke CommentIndexInterceptor then send email...  a little weird solution
	 * to send email. 
	 * Pros: 
	 * Easy for UnitText(no need initial MQ application context)
	 * Unit for email sending in create comment (also in CommentIndexInterceptor)
	 * 
	 * Crons:
	 * Looks weird~~~
	 * 
	 */
	void sendDailyCommentNotify();
	/**
	 * @param uid
	 */
	PageComment hideComment(Integer commentUid, boolean hide);
	/**
	 * @param uid
	 * @return
	 */
	PageComment getComment(Integer uid);
	/**
	 * Please note: this method won't do security check and update index!!!
	 * @param comment
	 */
	void updateComment(PageComment comment);
}
