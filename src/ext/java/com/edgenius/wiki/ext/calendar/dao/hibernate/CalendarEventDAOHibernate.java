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
package com.edgenius.wiki.ext.calendar.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.wiki.ext.calendar.dao.CalendarEventDAO;
import com.edgenius.wiki.ext.calendar.model.CalendarEvent;

/**
 * @author Dapeng.Ni
 */
@Repository("calendarEventDAO")
public class CalendarEventDAOHibernate extends BaseDAOHibernate<CalendarEvent> implements CalendarEventDAO {
	private static final String REMOVE_CAL_EVENTS = "delete " + CalendarEvent.class.getName() + " as e where e.calendar.uid=?";
	
	private static final String GET_CALENDAR = "from " + CalendarEvent.class.getName() 
	+ " as e where e.calendar.name=:name and e.calendar.pageUuid=:pageUuid " 
	+ " and ((e.start >=:start and e.start<:end) or (e.start>=:start and e.end<:end) or (e.start<:start and e.end>:end)) "
	+ " order by e.start";
	
	public void removeEvents(Integer calUid) {
		//WARNING: Hibernate can not construct join properly in buldUpdate()!!! 
		//use calendar Uid, it could be a simple query only for event table.
		bulkUpdate(REMOVE_CAL_EVENTS,calUid);
		
	}

	@SuppressWarnings("unchecked")
	public List<CalendarEvent> getEvents(final String calName, final String pageUuid, final Date start, final Date end) {
		Query query = getCurrentSesssion().createQuery(GET_CALENDAR);
		query.setString("name", calName);
		query.setString("pageUuid", pageUuid);
		query.setDate("start", start);
		query.setDate("end", end);
		
		return query.list();
	}
	
}
