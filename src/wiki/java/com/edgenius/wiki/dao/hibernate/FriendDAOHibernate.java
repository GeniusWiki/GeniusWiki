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
import com.edgenius.wiki.dao.FriendDAO;
import com.edgenius.wiki.model.Friend;

/**
 * @author Dapeng.Ni
 */
@Repository("friendDAO")
public class FriendDAOHibernate extends BaseDAOHibernate<Friend>  implements FriendDAO {

	private static String GET_HISTORY_BY_UUID = "from " + Friend.class.getName() + " as f where f.sender=? and f.receiver=?";

	@SuppressWarnings("unchecked")	
	public Friend getFriendship(String sender, String receiver) {
		List<Friend> friends = getHibernateTemplate().find(GET_HISTORY_BY_UUID,new Object[]{sender,receiver});
		if(friends == null || friends.size() == 0)
			return null;
		
		return friends.get(0);
	}
}
