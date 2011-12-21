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
package com.edgenius.wiki.ext.todo.dao.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.wiki.ext.todo.dao.TodoItemDAO;
import com.edgenius.wiki.ext.todo.model.TodoItem;

/**
 * @author Dapeng.Ni
 */
public class TodoItemDAOHibernate  extends BaseDAOHibernate<TodoItem> implements TodoItemDAO {
	private static final String REMOVE_TODO_ITEMS = "delete " + TodoItem.class.getName() + " as e where e.todo.uid=?";
	private static final String GET_BY_PUUID_TODONAME = "from "+ TodoItem.class.getName() + " as e where e.todo.pageUuid=? " +
		" and e.todo.name=? order by e.createdDate desc";
	
	private static final String GET_TODO_STATUS_COUNT = "select count(status),status from " + TodoItem.class.getName() 
				+ " as t where t.todo.pageUuid=? and t.todo.name=? group by status";


	public void removeItems(Integer todoUid) {
		//WARNING: Hibernate can not construct join properly in buldUpdate()!!! 
		//use calendar Uid, it could be a simple query only for event table.
		bulkUpdate(REMOVE_TODO_ITEMS,todoUid);
	}

	@SuppressWarnings("unchecked")
	public List<TodoItem> getItemsByNameInPage(String pageUuid, String todoName) {
		return find(GET_BY_PUUID_TODONAME,new Object[]{pageUuid,todoName});
	}
	public Map<String,Long> getStatusCount(String pageUuid, String todoName){
		List<Object[]> rs = find(GET_TODO_STATUS_COUNT,new Object[]{pageUuid,todoName});
		
		Map<String,Long> map = new HashMap<String, Long>();
		if(rs != null && rs.size() > 0){
			for (Object[] obj : rs) {
				map.put(((String)obj[1]).toLowerCase(), (Long)obj[0]);
			}
		}
		return map;
	}
}
