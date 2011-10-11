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
import com.edgenius.wiki.dao.WidgetDAO;
import com.edgenius.wiki.model.Widget;

/**
 * @author Dapeng.Ni
 */
@Repository("widgetDAO")
public class WidgetDAOHibernate extends BaseDAOHibernate<Widget> implements WidgetDAO {
	private static final String GET_BY_UUID= "from " + Widget.class.getName() + " as w where w.uuid=?";

	@SuppressWarnings("unchecked")
	//JDK1.6 @Override
	public Widget getByUUID(String key) {
		List<Widget> list = getHibernateTemplate().find(GET_BY_UUID,key);
		if(list == null || list.isEmpty())
			return null;
		
		return (Widget) list.get(0);
	}

}
