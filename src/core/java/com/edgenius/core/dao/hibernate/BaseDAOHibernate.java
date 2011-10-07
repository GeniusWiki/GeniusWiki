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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.edgenius.core.dao.DAO;
import com.edgenius.core.util.GenericsUtils;

/**
 * 
 * @author Dapeng.Ni
 */
@SuppressWarnings("unchecked")
public class BaseDAOHibernate<T> extends HibernateDaoSupport  implements DAO<T> {
	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected Class<T> entityClass;

	@Resource(name="sessionFactory")
	public void initSessionFactory(SessionFactory factory) {
	    setSessionFactory(factory);
	}
	
	/**
	 */
	public BaseDAOHibernate() {
		entityClass = GenericsUtils.getSuperClassGenricType(getClass());
	}

	public T get(Serializable id) {
		return (T) getHibernateTemplate().get(entityClass, id);
	}

	public List<T> getObjects() {
		//Don't user loadAll() API as it may return duplicated objects, refer to http://www.jroller.com/wireframe/entry/hibernate_loadall_feature
		//and refer to http://jira.springframework.org/browse/SPR-5007
		return (List<T>)getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria criteria = session.createCriteria(entityClass).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
				return criteria.list();
			}
			
		});
	}

	public void remove(Serializable id) {
		getHibernateTemplate().delete(get(id));
		
	}
	public void removeObject(Object obj) {
		getHibernateTemplate().delete(obj);
	}

	public void saveOrUpdate(T o) {
		getHibernateTemplate().saveOrUpdate(o);
		
	}
	public void refresh(T o) {
		if(o != null)
			getHibernateTemplate().refresh(o);
		
	}
	public void cleanTable(){
		getHibernateTemplate().bulkUpdate("delete from " + entityClass.getName());
	}

	public void merge(T o) {
		if(o != null)
			getHibernateTemplate().merge(o);
		
	}
	

}
