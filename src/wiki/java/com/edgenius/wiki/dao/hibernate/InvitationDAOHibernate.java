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
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.wiki.dao.InvitationDAO;
import com.edgenius.wiki.model.Invitation;

/**
 * @author Dapeng.Ni
 */
public class InvitationDAOHibernate extends BaseDAOHibernate<Invitation> implements InvitationDAO {
	private static String GET_BY_SPACEUNAME_UUID = "from " + Invitation.class.getName() + " as i where i.spaceUname=? and i.uuid=? ";
	private static String REMOVE_BY_AGE = "delete from " + Invitation.class.getName() + " where createdDate < :cDate ";

	@SuppressWarnings("unchecked")
	public Invitation getByUuid(String spaceUname, String invitationUuid) {
		List list = getHibernateTemplate().find(GET_BY_SPACEUNAME_UUID,new String[]{spaceUname,invitationUuid});
		if(list == null || list.size() == 0){
			return null;
		}
		
		return (Invitation) list.get(0);
	}


	public void removeOldInvitations(final int olderThanHours) {
		
		getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Date date = DateUtils.addHours(new Date(), -olderThanHours);
				session.createQuery(REMOVE_BY_AGE).setDate("cDate", date).executeUpdate();
				
				return null;
			}
		});
	}

	
}
