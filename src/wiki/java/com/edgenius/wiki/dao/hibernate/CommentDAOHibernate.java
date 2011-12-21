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
package com.edgenius.wiki.dao.hibernate;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.wiki.dao.CommentDAO;
import com.edgenius.wiki.model.PageComment;
/**
 * @author Dapeng.Ni
 */
@Repository("commentDAO")
public class CommentDAOHibernate extends BaseDAOHibernate<PageComment> implements CommentDAO{
	private static final String GET_BY_PAGE_UID= "from " + PageComment.class.getName() 
				+ " as c where c.page.uid=?";
	
	private static final String GET_COUNT_BY_PAGE_UID= "select count(*) from " + PageComment.class.getName() 
				+ " as c where c.page.uid=?";
	
	private static final String GET_COUNT_BY_USER_AUTHORED= "select count(c.uid) from " + PageComment.class.getName() 
			+ " as c where c.creator.username=?";
	
	private static final String GET_NEED_DAILY_NOTIFY_PAGE_UIDS = "select distinct c.page.uid from " + PageComment.class.getName() 
			+ " as c where c.notifyStatus="+PageComment.NOTIFY_SENT_SUMMARY;

	//not exactly must "today", just some time period according to Global setting
	private static final String GET_COUNT_SENT_BY_TODAY = "select count(c.uid) from " + PageComment.class.getName() 
			+ " as c where c.page.uid=? and c.notifyStatus="+PageComment.NOTIFY_SENT_PER_POST;
	
	private static final String RESET_NOTIFY_FLAG = "update "+ PageComment.class.getName() 
		+ " set notifyStatus=0 where notifyStatus>0";
	
	@SuppressWarnings("unchecked")
	public List<PageComment> getCommentsByPageUid(Integer pageUid) {
		return find(GET_BY_PAGE_UID,pageUid);
	}

	@SuppressWarnings("unchecked")
	public int getCommentCountByPageUid(Integer pageUid) {
		List list = find(GET_COUNT_BY_PAGE_UID,pageUid);
		if(list != null && list.size() > 0){
			return (int) ((Long)list.get(0)).longValue();
		}
		return 0;
	}

	//JDK1.6 @Override
	@SuppressWarnings("unchecked")
	public int getUserCommentSize(String username) {
		List list = find(GET_COUNT_BY_USER_AUTHORED,username);
		if(list != null && list.size() > 0){
			return (int) ((Long)list.get(0)).longValue();
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	//JDK1.6 @Override
	public List<Integer> getNeedNotifyCommentPageUids() {
		return find(GET_NEED_DAILY_NOTIFY_PAGE_UIDS);
	}

	//JDK1.6 @Override
	public void cleanNotifyFlag() {
		bulkUpdate(RESET_NOTIFY_FLAG);
	}

	//JDK1.6 @Override
	@SuppressWarnings("unchecked")
	public int getNotifySentCount(Integer pageUid) {
		List list = find(GET_COUNT_SENT_BY_TODAY,pageUid);
		if(list != null && list.size() > 0){
			return (int) ((Long)list.get(0)).longValue();
		}
		return 0;
	}
}
