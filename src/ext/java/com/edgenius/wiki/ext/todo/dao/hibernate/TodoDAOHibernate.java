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

import java.util.List;

import org.springframework.stereotype.Repository;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.wiki.ext.todo.dao.TodoDAO;
import com.edgenius.wiki.ext.todo.model.Todo;

/**
 * @author Dapeng.Ni
 */
@Repository("todoDAO")
public class TodoDAOHibernate extends BaseDAOHibernate<Todo> implements TodoDAO {
	private final static String GET_PAGE_TODOS = "from " + Todo.class.getName() + " as t where t.pageUuid=?";
	private static final String GET_TODO = "from " + Todo.class.getName() + " as t where t.pageUuid=? and t.name=?";

	@SuppressWarnings("unchecked")
	public List<Todo> getPageTodos(String pageUuid) {
		return find(GET_PAGE_TODOS, pageUuid);
	}

	@SuppressWarnings("unchecked")
	public Todo getByNameInPage(String pageUuid, String todoName) {
		List<Todo> list = find(GET_TODO, new Object[]{pageUuid, todoName});
		if(list == null || list.size() == 0){
			return null;
		}
		return list.get(0);
	}

}
