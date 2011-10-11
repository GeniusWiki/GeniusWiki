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

import java.util.List;

import org.springframework.stereotype.Repository;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.wiki.dao.SpaceTagDAO;
import com.edgenius.wiki.model.SpaceTag;

/**
 * @author Dapeng.Ni
 */
@Repository("spaceTagDAO")
public class SpaceTagDAOHibernate  extends BaseDAOHibernate<SpaceTag> implements SpaceTagDAO {
	private static String GET_BY_NAME = "from " + SpaceTag.class.getName() + " as t where t.name=?";
	
	//JDK1.6 @Override
	@SuppressWarnings("unchecked")
	public SpaceTag getByName(String tagName) {
		List<SpaceTag> list = getHibernateTemplate().find(GET_BY_NAME,new Object[]{tagName});
		if(list == null || list.size() == 0)
			return null;
		return list.get(0);
	}
}
