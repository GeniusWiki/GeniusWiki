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

import java.util.List;

import org.springframework.stereotype.Repository;

import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.dao.PermissionDAO;
import com.edgenius.core.model.Permission;

/**
 * @author Dapeng.Ni
 */
@Repository("permissionDAO")
public class PermissionDAOHibernate extends BaseDAOHibernate<Permission>  implements PermissionDAO {
	private static final String GET_BY_RESOURCE	= " from " + Permission.class.getName() + " as p " 
											+ " where p.resource.resource=?"; 
	private static final String GET_BY_OPERATION_RESOURCE= " from " + Permission.class.getName() + " as p " 
					+ " where p.operation=? and p.resource.resource=?";
	
	private static final String GET_BY_ROLE_OPERATION = "select distinct p from " + Permission.class.getName()  
			+ " as p inner join p.roles as r where p.operation=? and r.name=? ";

	private static final String GET_BY_ROLE_RESOURCE = "select distinct p from " + Permission.class.getName() 
		+ " as p inner join p.roles as r where r.name=? and p.resource.resource=? ";
	
						
	@SuppressWarnings("unchecked")
	public List<Permission> getByResource(String resourceName) {
		
		return getHibernateTemplate().find(GET_BY_RESOURCE,resourceName);
	}

	@SuppressWarnings("unchecked")
	public Permission getByOperationResource(OPERATIONS operation, String resourceName) {
		List<Permission> list = getHibernateTemplate().find(GET_BY_OPERATION_RESOURCE,new Object[]{operation,resourceName});
		if(list == null || list.size() == 0)
			return null;
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	public List<Permission> getByRoleOperation(String roleName, OPERATIONS operation) {
		return getHibernateTemplate().find(GET_BY_ROLE_OPERATION,new Object[]{operation,roleName});
	}

	@SuppressWarnings("unchecked")
	public List<Permission> getByRoleResource(String roleName, String resourceName){
		return getHibernateTemplate().find(GET_BY_ROLE_RESOURCE,new Object[]{roleName,resourceName});
	}
}
