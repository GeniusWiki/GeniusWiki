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

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.core.model.User;
import com.edgenius.wiki.dao.UserPageDAO;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.UserPageMark;

/**
 * @author Dapeng.Ni
 */
public class UserPageDAOHibernate extends BaseDAOHibernate<UserPageMark> implements UserPageDAO {
	private static final String GET_BY_OBJECT ="from " + UserPageMark.class.getName() 
				+ " as u where u.page=? and u.user=? and u.type=?";
	private static final String GET_BY_OBJECT_NO_USER ="from " + UserPageMark.class.getName() 
				+ " as u where u.page=? and u.type=?";
	private static final String GET_BY_USER_PAGE = "from " + UserPageMark.class.getName() 
				+ " as u where ((u.user=? and type < 10) or type > 9) and u.page=?";
	
	private static final String REMOVE_BY_PAGE_UID="delete from " + UserPageMark.class.getName() + " as u where u.page.uid= :uid";
	
	
	private static final String GET_FAVORITES =  "from " + UserPageMark.class.getName() 
	+ " as u where u.user.username=? and u.type=1 and u.page.removed=0 order by u.page.modifiedDate";
	private static final String GET_SPACE_FAVORITES =  "from " + UserPageMark.class.getName() 
		+ " as u where u.page.space.unixName=? and u.user.username=? and u.type=1 and u.page.removed=0  order by u.page.modifiedDate";
	
	private static final String GET_WATCHED_BY_PAGE_UID="from " + UserPageMark.class.getName() + " as u where u.page.uid=? and u.type=2";
	
	private static final String GET_WATCHEDS =  "from " + UserPageMark.class.getName() 
	+ " as u where u.user.username=? and u.type=2 and u.page.removed=0  order by u.page.modifiedDate";
	
	private static final String GET_SPACE_WATCHEDS =  "from " + UserPageMark.class.getName() 
	+ " as u where u.page.space.unixName=? and u.user.username=? and u.type=2 and u.page.removed=0 order by u.page.modifiedDate";
	
	
	@SuppressWarnings("unchecked")
	public UserPageMark getByObject(UserPageMark up) {
		List<UserPageMark> list;
		if(up.getType() < 10){
			//these marks are user specified
			list = getHibernateTemplate().find(GET_BY_OBJECT,new Object[]{up.getPage(),up.getUser(),up.getType()});
		}else{
			//these marks are global scope 
			list = getHibernateTemplate().find(GET_BY_OBJECT_NO_USER,new Object[]{up.getPage(),up.getType()});
		}
		
		if(list == null || list.size() == 0)
			return null;
		
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	public List<UserPageMark> getWatchedByPageUid(Integer uid) {
		return  getHibernateTemplate().find(GET_WATCHED_BY_PAGE_UID,uid);
	}

	public int removeByPageUid(final Integer uid) {
		return (Integer)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				int deleted = session.createQuery(REMOVE_BY_PAGE_UID).setInteger("uid", uid).executeUpdate();
				return deleted;
			}
		});
		
	}

	@SuppressWarnings("unchecked")
	public List<UserPageMark> getByUserAndPage(User user, Page page) {
		return  getHibernateTemplate().find(GET_BY_USER_PAGE,new Object[]{user,page});
	}
	@SuppressWarnings("unchecked")
	public List<UserPageMark> getFavorites(String spaceUname, String username) {
		if(StringUtils.isBlank(spaceUname))
			return getHibernateTemplate().find(GET_FAVORITES,new Object[]{username});
		else
			return getHibernateTemplate().find(GET_SPACE_FAVORITES,new Object[]{spaceUname, username});
	}

	@SuppressWarnings("unchecked")
	public List<UserPageMark> getWatched(String spaceUname, String username) {
		if(StringUtils.isBlank(spaceUname))
			return getHibernateTemplate().find(GET_WATCHEDS,new Object[]{username});
		else
			return getHibernateTemplate().find(GET_SPACE_WATCHEDS,new Object[]{spaceUname, username});
	}
}
