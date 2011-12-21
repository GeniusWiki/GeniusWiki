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

import com.edgenius.core.dao.ResourceDAO;
import com.edgenius.core.model.Resource;

/**
 * @author Dapeng.Ni
 */
@Repository("resourceDAO")
public class ResourceDAOHibernate extends BaseDAOHibernate<Resource> implements ResourceDAO {
	private static final String GET_BY_NAME = "from " + Resource.class.getName()  + " as r where r.resource=?";
	
	private static final String GET_SORTED_RESOURCE = "from " + Resource.class.getName()  + " as r order by r.type asc";
	
	@SuppressWarnings("unchecked")
	public Resource getByName(String resourceName) {
		//normally, it works for MySQL, but for postgresql, null in query will cause problem.
		//http://opensource.atlassian.com/projects/hibernate/browse/EJB-365
		if(resourceName == null)
			return null;
		
    	List<Resource> list = find(GET_BY_NAME,resourceName);
    	if(list == null || list.size() == 0)
    		return null;
        return (Resource) list.get(0);
	}

	/* (non-Javadoc)
	 * @see com.edgenius.core.dao.ResourceDAO#getReources()
	 */
	@SuppressWarnings("unchecked")
	public List<Resource> getReources() {
		return find(GET_SORTED_RESOURCE);
	}

}
