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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.core.model.User;
import com.edgenius.wiki.dao.HistoryDAO;
import com.edgenius.wiki.model.History;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;

/**
 * @author Dapeng.Ni
 */
public class HistoryDAOHibernate  extends BaseDAOHibernate<History> implements HistoryDAO {
	//don't simple get p.User, if that, the p.User==null(anonymous user) won't be in result set.
	private static String GET_HISTORY_BY_UUID = "from " + History.class.getName() 
		+ " as p where p.pageUuid=? and p.version<? order by p.version desc";
	private static final String GET_HISTORY_BY_UUID_OLDER_DATE = "from " + History.class.getName() 
			+ " as p where p.pageUuid=? and touchedDate>? and p.version<? order by p.version desc";
	
	private static String GET_VERSION_BY_UUID = "from " + History.class.getName() + " as p where p.pageUuid=? and p.version =?";

	protected static final String GET_USER_CONTRIBUTED_HISTORIES = "select h.uid, h.title, h.pageUuid, h.version, h.space.unixName" +
			",h.creator.username, h.createdDate, h.modifier.username,h.modifiedDate " +
			" from " + History.class.getName() + " as h, " 
			+ Page.class.getName() + " as p where h.pageUuid=p.pageUuid and h.modifier=:user" 
			+ " and p.removed=0";

	//please note, if check modifier.puid is null, then "h.creator.username" won't be used
	//as it will join User table by user id and User table won't have a id is null record, then 
	//the whole sql won't return any value
	protected static final String GET_ANONYMOUS_CONTRIBUTED_HISTORIES = "select h.uid, h.title, h.pageUuid, h.version, h.space.unixName" +
			",h.createdDate, h.modifiedDate " +
			" from " + History.class.getName() + " as h, "
			+ Page.class.getName() + " as p where h.pageUuid=p.pageUuid and h.modifier is null"
			+ " and p.removed=0";
	
	@SuppressWarnings("unchecked")
	public List<History> getByUuid(final String uuid,final int startVer, final int returnCount, final Date touchedDate) {
		return (List<History>) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query;
				if(touchedDate == null){
					query = session.createQuery(GET_HISTORY_BY_UUID);
					query.setString(0, uuid);
					query.setInteger(1, startVer <=0? Integer.MAX_VALUE:startVer);
				}else{
					query = session.createQuery(GET_HISTORY_BY_UUID_OLDER_DATE);
					query.setString(0, uuid);
					query.setDate(1, touchedDate);
					query.setInteger(2, startVer <=0? Integer.MAX_VALUE:startVer);
				}
				if(returnCount > 0){
					query.setMaxResults(returnCount);
				}
				return query.list(); 
			}
		});
	
	}

	//JDK1.6 @Override
	@SuppressWarnings("unchecked")
	public History getVersionByUuid(int version, String pageUuid) {
		List<History> list = getHibernateTemplate().find(GET_VERSION_BY_UUID,new Object[]{pageUuid,version});
		if(list == null || list.size() == 0){
			return null;
		}
		return list.get(0);
	}
	
	//JDK1.6 @Override
	public List<History> getByUuid(String fromPageUuid) {
	
		return getByUuid(fromPageUuid,0 , -1, null);
	}

	@SuppressWarnings("unchecked")
	public List<History> getUserContributedHistories(final User user) {
		return (List<History>) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query;
				boolean anonymous = false;
				if(user == null || user.isAnonymous()){
					anonymous = true;
					query = session.createQuery(GET_ANONYMOUS_CONTRIBUTED_HISTORIES);
				}else{
					query = session.createQuery(GET_USER_CONTRIBUTED_HISTORIES);
					query.setEntity("user", user);
				}
				List<History> histories =new ArrayList<History>();
				List<Object[]> list = query.list();
				for (Object[] obj : list) {
					History his = new History();
					Space space = new Space();
					User creator= new User();
					User modifier= new User();
					int idx=0;
					
					his.setUid((Integer) obj[idx++]);
					his.setTitle((String) obj[idx++]);
					his.setPageUuid((String) obj[idx++]);
					his.setVersion((Integer) obj[idx++]);

					space.setUnixName((String) obj[idx++]);
					his.setSpace(space);
					
					if(!anonymous){
						creator.setUsername((String) obj[idx++]);
					}else{
						creator.setUsername(User.ANONYMOUS_USERNAME);
					}
					his.setCreator(creator);;
					his.setCreatedDate((Date) obj[idx++]);
					if(!anonymous){
						modifier.setUsername((String) obj[idx++]);
					}else{
						modifier.setUsername(User.ANONYMOUS_USERNAME);
					}
					his.setModifier(modifier);;
					his.setModifiedDate((Date) obj[idx++]);
					
					histories.add(his);
				}
				
				return histories;
			}
		});
	}

}
