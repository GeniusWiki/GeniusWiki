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
package com.edgenius.wiki.ext.calendar.service;

import java.util.Date;
import java.util.List;

import com.edgenius.wiki.ext.calendar.model.Calendar;
import com.edgenius.wiki.ext.calendar.model.CalendarEvent;

/**
 * @author Dapeng.Ni
 */
public interface CalendarService {

	String SERVICE_NAME = "calendarService";
	
	void saveOrUpdateCalendar(Calendar cal);
	/**
	 * @return All calendars of system.
	 */
	List<Calendar> getCalendars();

	/**
	 * Remove all calendars attached on this page.
	 * @param pageUuid
	 */
	void removePageCalendars(String pageUuid);

	/**
	 * Get events list in given calendar within the specified date range.
	 * @param calName
	 * @param pageUuid
	 * @param calStart
	 * @param calEnd
	 * @return
	 */
	List<CalendarEvent> getEvents(String calName, String pageUuid, Date calStart, Date calEnd);
	
	void saveOrUpdateEvent(CalendarEvent evt);
	void removeEvent(int eventID);
	CalendarEvent getEvent(int eventID);
	
	/**
	 * @param pageUuid
	 * @param calendarName
	 * @return
	 */
	Calendar getCalendar(String pageUuid, String calendarName);
	
	
}
