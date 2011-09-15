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

import java.util.Collection;
import java.util.List;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.wiki.dao.PageTagDAO;
import com.edgenius.wiki.model.PageTag;

/**
 * @author Dapeng.Ni
 */
public class PageTagDAOHibernate  extends BaseDAOHibernate<PageTag> implements PageTagDAO {
	
	private static final String GET_TAGS_IN_SAPCE = "from " +  PageTag.class.getName() + " as t where t.space.unixName=?";
	private static String GET_BY_NAME = "from " + PageTag.class.getName() + " as t where t.space.unixName=? and t.name=?";
	private static String GET_ALL_TAG_OF_SPACE = "from " + PageTag.class.getName() + " as t where t.space.unixName=?";

	@SuppressWarnings("unchecked")
	public PageTag getByName(String spaceUname, String tagName) {
		List<PageTag> list = getHibernateTemplate().find(GET_BY_NAME,new Object[]{spaceUname,tagName});
		if(list == null || list.size() == 0)
			return null;
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	public List<PageTag> getSpaceTags(String spaceUname) {
		return getHibernateTemplate().find(GET_ALL_TAG_OF_SPACE,new Object[]{spaceUname});
	}

	@SuppressWarnings("unchecked")
	public void removeTagsInSpace(final String spaceUname) {
		Collection entities = getHibernateTemplate().find(GET_TAGS_IN_SAPCE,new Object[]{spaceUname});;
		getHibernateTemplate().deleteAll(entities );

	}

}
