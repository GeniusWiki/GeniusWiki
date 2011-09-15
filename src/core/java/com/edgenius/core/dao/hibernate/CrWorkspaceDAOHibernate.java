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
package com.edgenius.core.dao.hibernate;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.edgenius.core.dao.CrWorkspaceDAO;
import com.edgenius.core.model.CrWorkspace;

/**
 * @author Dapeng.Ni
 */
public class CrWorkspaceDAOHibernate extends BaseDAOHibernate<CrWorkspace> implements CrWorkspaceDAO {

	private static String GET_BY_SPACENAME = "from " + CrWorkspace.class.getName() + " as c where c.name=?"; 
	private static String UPDATE_ALL_QUOTA = "update  " + CrWorkspace.class.getName() + " set quota=?";
	private static String UPDATE_SPACE_QUOTA = "update  " + CrWorkspace.class.getName() + " set quota=? where name=?";
	
	/* (non-Javadoc)
	 * @see com.edgenius.text.engine.dao.CrWorkspaceDAO#getBySpaceName(java.lang.String)
	 */
	public CrWorkspace getBySpaceName(String spaceName) {
		List list = this.getHibernateTemplate().find(GET_BY_SPACENAME,spaceName);
		if(list == null || list.size() ==0)
			return null;
		
		return (CrWorkspace) list.get(0);
	}

	public int updateExistWorkspacesQuota(final long size) {
		return (Integer) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				int updates = session.createQuery(UPDATE_ALL_QUOTA).setLong(0, size).executeUpdate();
				return Integer.valueOf(updates);
			}
		});

	}

	public void updateWorkspacesQuota(final String spacename, final long size) {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery(UPDATE_SPACE_QUOTA);
				query.setLong(0, size);
				query.setString(1, spacename);
				query.executeUpdate();
				return null;
			}
		});
		
	}

}
