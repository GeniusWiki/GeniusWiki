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

import java.util.List;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.wiki.ext.calendar.dao.CalendarDAO;
import com.edgenius.wiki.ext.calendar.model.Calendar;

/**
 * @author Dapeng.Ni
 */
public class CalendarDAOHibernate extends BaseDAOHibernate<Calendar> implements CalendarDAO {

	private final static String GET_PAGE_CALENDARS = "from " + Calendar.class.getName() + " as c where c.pageUuid=?";
	private static final String GET_CALENDAR = "from " + Calendar.class.getName() + " as c where c.pageUuid=? and c.name=?";

	@SuppressWarnings("unchecked")
	public List<Calendar> getPageCalendars(String pageUuid) {
		return find(GET_PAGE_CALENDARS, pageUuid);
	}
	
	@SuppressWarnings("unchecked")
	public Calendar getCalendar(String pageUuid, String calendarName) {
		List<Calendar> cals  = find(GET_CALENDAR, new Object[]{pageUuid,calendarName});
		if(cals != null && cals.size() > 0)
			return cals.get(0);
		
		return null;
	}
	

}
