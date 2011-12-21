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

import com.edgenius.core.dao.ConfigurationDAO;
import com.edgenius.core.model.Configuration;
/**
 * @author Dapeng.Ni
 */
@Repository("configurationDAO")
public class ConfigurationDAOHibernate extends BaseDAOHibernate<Configuration> implements ConfigurationDAO {

	private final static String GET_BY_TYPE ="from " + Configuration.class.getName() +" as s where s.type=?";
	
	@SuppressWarnings("unchecked")
	public Configuration getByType(String type) {
		List<Configuration> list = find(GET_BY_TYPE,type);
		if(list == null || list.size() == 0)
			return null;
		return list.get(0);
	}

}
