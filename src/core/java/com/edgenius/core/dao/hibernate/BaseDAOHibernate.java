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
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import com.edgenius.core.dao.DAO;
import com.edgenius.core.util.GenericsUtils;

/**
 * 
 * @author Dapeng.Ni
 */
@SuppressWarnings("unchecked")
public class BaseDAOHibernate<T>  implements DAO<T> {
	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected Class<T> entityClass;

	@Resource(name="sessionFactory")
	private SessionFactory sessionFactory;
	
	public Session getCurrentSesssion(){
		return sessionFactory.getCurrentSession();
	}
	/**
	 */
	public BaseDAOHibernate() {
		entityClass = GenericsUtils.getSuperClassGenricType(getClass());
	}

	public T get(Serializable id) {
		return (T) getCurrentSesssion().get(entityClass, id);
	}

	public List<T> getObjects() {
		
		Criteria criteria = getCurrentSesssion().createCriteria(entityClass);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return criteria.list();
		
	}

	public void remove(Serializable id) {
		getCurrentSesssion().delete(get(id));
		
	}
	public void removeObject(Object obj) {
		getCurrentSesssion().delete(obj);
	}

	public void saveOrUpdate(T o) {
		getCurrentSesssion().saveOrUpdate(o);
		
	}
	public void refresh(T o) {
		if(o != null)
			getCurrentSesssion().refresh(o);
		
	}
	public void cleanTable(){
		bulkUpdate("delete from " + entityClass.getName());
	}

	public void merge(T o) {
		if(o != null)
			getCurrentSesssion().merge(o);
		
	}
	
	//********************************************************************
	//               Method original from HibernateTemplate
	//********************************************************************
	protected List find(String queryString) throws DataAccessException {
		return find(queryString, (Object[]) null);
	}

	protected List find(String queryString, Object value) throws DataAccessException {
		return find(queryString, new Object[] {value});
	}
	
	protected List find(final String queryString, final Object... values) throws DataAccessException {
		Query queryObject = getCurrentSesssion().createQuery(queryString);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(i, values[i]);
			}
		}
		return queryObject.list();
	}
	
	protected int bulkUpdate(String queryString) throws DataAccessException {
		return bulkUpdate(queryString, (Object[]) null);
	}

	protected int bulkUpdate(String queryString, Object value) throws DataAccessException {
		return bulkUpdate(queryString, new Object[] {value});
	}

	protected int bulkUpdate(final String queryString, final Object... values) throws DataAccessException {
		Query queryObject = getCurrentSesssion().createQuery(queryString);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(i, values[i]);
			}
		}
		return queryObject.executeUpdate();
	}

	protected void deleteAll(final Collection entities) throws DataAccessException {
		for (Object entity : entities) {
			getCurrentSesssion().delete(entity);
		}
	}
	
	protected List findByNamedParam(String queryString, String paramName, Object value)
			throws DataAccessException {

		return findByNamedParam(queryString, new String[] {paramName}, new Object[] {value});
	}

	protected List findByNamedParam(final String queryString, final String[] paramNames, final Object[] values)
			throws DataAccessException {

		if (paramNames.length != values.length) {
			throw new IllegalArgumentException("Length of paramNames array must match length of values array");
		}
		Query queryObject = getCurrentSesssion().createQuery(queryString);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				applyNamedParameterToQuery(queryObject, paramNames[i], values[i]);
			}
		}
		return queryObject.list();
	}
	
	protected void applyNamedParameterToQuery(Query queryObject, String paramName, Object value)
			throws HibernateException {

		if (value instanceof Collection) {
			queryObject.setParameterList(paramName, (Collection) value);
		}
		else if (value instanceof Object[]) {
			queryObject.setParameterList(paramName, (Object[]) value);
		}
		else {
			queryObject.setParameter(paramName, value);
		}
	}
}
