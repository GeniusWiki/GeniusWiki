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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import com.edgenius.core.SecurityValues;
import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.core.model.Resource;
import com.edgenius.core.model.User;
import com.edgenius.wiki.dao.NotificationDAO;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Notification;

/**
 * @author Dapeng.Ni
 */
@Repository("notificationDAO")
public class NotificationDAOHibernate extends BaseDAOHibernate<Notification>  implements NotificationDAO {
	private final static String GET_MESSAGES_PREFIX = "from " + Notification.class.getName() 
		+ " as n ";
	
	private final static String GET_MESSAGES_SUFFIX = " order by n.createdDate desc";

	@SuppressWarnings("unchecked")
	public List<Notification> getResourceMessages(final User user, final boolean sysAdmin, final List<Resource> spaceResList, 
			final List<Resource> adminResList, final int start, final int retCount) {
		StringBuffer sql = new StringBuffer(GET_MESSAGES_PREFIX);
		
		Map<Integer,String> queryParams = new HashMap<Integer,String>();
		int queryPos = 0;
		//if users is instance admin, then get all messages... is it good????
		if(!sysAdmin){
			//first, get messages to all users and specified to given user
			sql.append(" where n.targetType=").append(SharedConstants.MSG_TARGET_ALL_USERS).append(" or ");
			sql.append(" (n.targetType=").append(SharedConstants.MSG_TARGET_USER).append(" and n.targetName=:p")
				.append(queryPos).append(") ");
			queryParams.put(queryPos++, user.getUsername());
			
			if(adminResList != null && adminResList.size() > 0){
				sql.append(" or ");
				
				for (int idx=0;idx< adminResList.size();idx++) {
					Resource resource = adminResList.get(idx);
					if(SecurityValues.RESOURCE_TYPES.SPACE.equals(resource.getType())){
						//spaces admin permissions
						sql.append(" (n.targetType=").append(SharedConstants.MSG_TARGET_SPACE_ADMIN_ONLY).append(" and ");
						sql.append(" n.targetName=:p").append(queryPos + idx).append(") or ");
						queryParams.put(queryPos + idx, resource.getResource());
					}
				}
				//remove last "or"
				sql.delete(sql.length() - 3,sql.length());
			}
			
			if(spaceResList != null && spaceResList.size() > 0){
				sql.append(" or ");
				queryPos = queryParams.size();
				for (int idx=0;idx< spaceResList.size();idx++) {
					Resource resource = spaceResList.get(idx);
					if(SecurityValues.RESOURCE_TYPES.SPACE.equals(resource.getType())){
						//spaces all users
						sql.append(" (n.targetType=").append(SharedConstants.MSG_TARGET_SPACE_CONTRIBUTE_USERS).append(" and ");
						sql.append(" n.targetName=:p").append(queryPos+idx).append(") or ");
						queryParams.put(queryPos+idx, resource.getResource());
					}
				}
				//remove last "or"
				sql.delete(sql.length() - 3,sql.length());
			}
			
		}

		sql.append(GET_MESSAGES_SUFFIX);
		Query query = getCurrentSesssion().createQuery(sql.toString());
		for (Entry<Integer,String>entry: queryParams.entrySet()) {
			query.setString("p"+entry.getKey(), entry.getValue());
		}
		
		if(start > 0)
			query.setFirstResult(start);
		if(retCount > 0)
			query.setMaxResults(retCount);
		
		return query.list();
	}
}
