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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.wiki.dao.TemplateDAO;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Template;

/**
 * @author Dapeng.Ni
 */
public class TemplateDAOHibernate extends BaseDAOHibernate<Template> implements TemplateDAO {
	private static String GET_ALL = "from " + Template.class.getName() + " as t order by t.name";
	private static String GET_BY_SPACE = "from " + Template.class.getName() + " as t where t.space.unixName=? order by t.name";
	private static String GET_BY_SPACE_WITH_SHARED = "from " + Template.class.getName() + " as t where t.space.unixName=? " +
			" or t.shared=true order by t.name";
	
	private static final String REMOVE_SPACE_TEMPLATES = "delete " + Template.class.getName() + " as t where t.space.uid=?";

	@SuppressWarnings("unchecked")
	public List<Template> getSpaceTemplates(String spaceUname, boolean withAllShared) {
		if(StringUtils.isBlank(spaceUname) ||SharedConstants.SYSTEM_SPACEUNAME.equals(spaceUname))
			return getHibernateTemplate().find(GET_ALL);
		if(!withAllShared)
			return getHibernateTemplate().find(GET_BY_SPACE,spaceUname);
		
		List<Template> list = new ArrayList<Template>();
		List<Template> permList = getHibernateTemplate().find(GET_BY_SPACE_WITH_SHARED,spaceUname);
		//put all given spaceUname template ahead others.
		int pointer = 0;
		for (Template templ : permList) {
			if(templ.getSpace().getUnixName().equals(spaceUname)){
				list.add(pointer, templ);
				pointer++;
			}else
				list.add(templ);
		}
		
		return list;
	}

	public void removeSpaceTemplates(Integer spaceUid) {
		//WARNING: Hibernate can not construct join properly in buldUpdate()!!! 
		//if using spaceUname, SQL should be a join-sql,unfortunately, it cause exception.
		//use spaceUid, it could be a simple query only for template table.
		getHibernateTemplate().bulkUpdate(REMOVE_SPACE_TEMPLATES,spaceUid);
		
	}

}
