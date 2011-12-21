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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import com.edgenius.core.dao.RoleDAO;
import com.edgenius.core.model.Role;


/**
 * This class interacts with Spring's HibernateTemplate to save/delete and
 * retrieve Role objects.
 *
 */
@Repository("roleDAO")
public class RoleDAOHibernate extends BaseDAOHibernate<Role> implements RoleDAO {

	private static final String GET_BY_NAME = "from " + Role.class.getName()  + " as r where r.name=?";
	private static final String GET_ALL = "from " + Role.class.getName()  + " as r order by upper(r.name)";
	
	private static final String GET_BY_TYPE = "from " + Role.class.getName()  + " as r where r.type=:type ";
	private static final String GET_BY_TYPE_ORDERBY = " order by upper(r.name)";

	private static final String GET_ROLES_USERS_COUNT= "select r.uid, count(*) from " + Role.class.getName() 
						+ " as r inner join r.users as u where r.type="+ Role.TYPE_GROUP + " or r.type=" 
						+ Role.TYPE_SYSTEM + " group by r.uid ";
	 
    @SuppressWarnings("unchecked")
	public Role getByName(String rolename) {
    	List<Role> list = find(GET_BY_NAME,rolename);
    	if(list == null || list.size() == 0)
    		return null;
        return (Role) list.get(0);
    }

    public Role saveRole(Role role) {
        
    	getCurrentSesssion().saveOrUpdate(role);
    	
    	return role;
    }

    public void removeByName(String rolename) {
       Role role = getByName(rolename);
       getCurrentSesssion().delete(role);
    }

	@SuppressWarnings("unchecked")
	public List<Role> getAllRoles() {
		  return find(GET_ALL);
	}

	@SuppressWarnings("unchecked")
	public List<Role> getRoles(final int roleType, final String filter) {
	
		String filterWith = "";
		if(!StringUtils.isBlank(filter)){
			filterWith = " and (r.displayName like :filter or r.description like :filter) ";
		}
		Query query = getCurrentSesssion().createQuery(GET_BY_TYPE + filterWith + GET_BY_TYPE_ORDERBY);
		query.setInteger("type", roleType);
		if(!StringUtils.isBlank(filter)){
			query.setString("filter","%"+filter.trim()+"%");
		}
		
		return query.list();
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, Long> getRolesUsersCount() {
		
		List<Object[]> list = find(GET_ROLES_USERS_COUNT);
		
		Map<Integer, Long> usersCount = new HashMap<Integer, Long>();
		if(list == null){
			return usersCount;
		}
		for (Object[] objects : list) {
			Integer uid = (Integer) objects[0];
			Long count = (Long) objects[1];
			usersCount.put(uid, count);
		}
		
		return usersCount;
	}

}
