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
import com.edgenius.wiki.dao.PageLinkDAO;
import com.edgenius.wiki.model.PageLink;

/**
 * @author Dapeng.Ni
 */
@Repository("pageLinkDAO")
public class PageLinkDAOHibernate extends BaseDAOHibernate<PageLink> implements PageLinkDAO {
	private static final String GET_BY_NAME="from "+ PageLink.class.getName() + " as p where p.type=? and p.spaceUname=? and p.link=?";
	/* (non-Javadoc)
	 * @see com.edgenius.wiki.dao.PageLinkDAO#getByName(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<PageLink> getLinksFromSpace(String spaceUname, String link) {
		return getHibernateTemplate().find(GET_BY_NAME,new Object[]{PageLink.TYPE_INTERNAL, spaceUname,link});
	}

}
