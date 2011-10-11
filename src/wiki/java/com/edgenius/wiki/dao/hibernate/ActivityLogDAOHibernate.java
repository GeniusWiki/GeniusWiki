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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.core.model.User;
import com.edgenius.wiki.dao.ActivityLogDAO;
import com.edgenius.wiki.model.ActivityLog;

/**
 * @author Dapeng.Ni
 */
@Repository("activityLogDAO")
public class ActivityLogDAOHibernate extends BaseDAOHibernate<ActivityLog>  implements ActivityLogDAO {
	private static final String GET_BY_TARGET = "from " + ActivityLog.class.getName() 
		+ " as a where a.type=? and a.subType=? and a.tgtResourceType=? and a.tgtResourceName=? order by a.createdDate desc";
	private static final String GET_BY_SOURCE = "from " + ActivityLog.class.getName() 
		+ " as a where a.type=? and a.subType=? and a.srcResourceType=? and a.srcResourceName=? order by a.createdDate desc";
	
	private static String REMOVE_BY_AGE = "delete from " + ActivityLog.class.getName() + " where createdDate < :cDate ";
	protected static final String GET_BY_COUNT = "from " + ActivityLog.class.getName() + " order by createdDate desc";
	protected static final String GET_BY_USER_COUNT = "from " + ActivityLog.class.getName() +
					" where creator.uid=:user " +
					" order by createdDate desc";
	protected static final String GET_BY_ANONYMOUS_COUNT = "from " + ActivityLog.class.getName() +
		" where creator.uid is null order by createdDate desc";
					
	@SuppressWarnings("unchecked")
	public List<ActivityLog> getByTarget(int typeCode, int subTypeCode, int tgtResourceType, String tgtResourceName) {
		return getHibernateTemplate().find(GET_BY_TARGET,new Object[]{typeCode,subTypeCode,tgtResourceType,tgtResourceName});
	}

	@SuppressWarnings("unchecked")
	public List<ActivityLog> getBySource(int typeCode, int subTypeCode, int srcResourceType, String srcResourceName) {
		return getHibernateTemplate().find(GET_BY_SOURCE,new Object[]{typeCode,subTypeCode,srcResourceType,srcResourceName});
	}

	public void removeOldByDays(final int days) {
		getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Date date = DateUtils.addDays(new Date(), -days);
				session.createQuery(REMOVE_BY_AGE).setDate("cDate", date).executeUpdate();
				return null;
			}
		});
	}

	@SuppressWarnings("unchecked")
	public List<ActivityLog> getByCount(final int start, final int count) {
		return (List<ActivityLog>) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery(GET_BY_COUNT);
				if(start > 0)
					query.setFirstResult(start);
				if(count > 0)
					 query.setMaxResults(count);
				
				return query.list(); 
			}
		});
	}

	@SuppressWarnings("unchecked")
	public List<ActivityLog> getByUser(final int start, final int count, final User user) {

		return (List<ActivityLog>) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query;
				if(user == null || user.isAnonymous()){
					query = session.createQuery(GET_BY_ANONYMOUS_COUNT);
				}else{
					query = session.createQuery(GET_BY_USER_COUNT);
					query.setInteger("user", user.getUid());
				}
				if(start > 0)
					query.setFirstResult(start);
				if(count > 0)
					 query.setMaxResults(count);
				
				return query.list(); 
			}
		});
	}


}
