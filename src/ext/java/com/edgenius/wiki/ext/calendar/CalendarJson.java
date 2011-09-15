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
package com.edgenius.wiki.ext.calendar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import com.edgenius.wiki.ext.calendar.model.CalendarEvent;

/**
 * A json special for xgCalendar input format 
 * @author Dapeng.Ni
 */
public class CalendarJson implements Serializable{
	private static final long serialVersionUID = 584237656359466695L;

	private List<List<Object>> events;
	private boolean isSorted = true;
	private Date start;
	private Date end;
	private CalendarErrorJson error;
	
	public static CalendarJson toJson(List<CalendarEvent> calEvents, Date start, Date end){
		CalendarJson json = new CalendarJson();
		json.events = new ArrayList<List<Object>>();
		for (CalendarEvent evt : calEvents) {
			List<Object> eventList = new ArrayList<Object>();
			eventList.add(String.valueOf(evt.getUid()));
			eventList.add(evt.getSubject());
			
//			eventList.add("\\/Date("+evt.getStart().getTime()+"\\/");
//			eventList.add("\\/Date("+evt.getEnd().getTime()+"\\/");
			eventList.add("@"+evt.getStart().getTime()+"@");
			eventList.add("@"+evt.getEnd().getTime()+"@");
			
			eventList.add(evt.isAllDayEvent()?1:0);
			eventList.add(!DateUtils.isSameDay(evt.getStart(),evt.getEnd())?1:0);
			eventList.add(!StringUtils.isBlank(evt.getRepeatRule())?1:0);
			eventList.add(evt.getCategory());
			eventList.add(1);
			eventList.add(StringUtils.trimToEmpty(evt.getLocation()));
			eventList.add(StringUtils.trimToEmpty(evt.getInvitees()));
			
			json.events.add(eventList);
			
		}
		json.start = start;
		json.end = end;
		//error
		
		return json;
	}
	/**
	 * @return the events
	 */
	public List<List<Object>> getEvents() {
		return events;
	}
	/**
	 * @param events the events to set
	 */
	public void setEvents(List<List<Object>> events) {
		this.events = events;
	}
	/**
	 * @return the isSorted
	 */
	public boolean isSorted() {
		return isSorted;
	}
	/**
	 * @param isSorted the isSorted to set
	 */
	public void setSorted(boolean isSorted) {
		this.isSorted = isSorted;
	}
	/**
	 * @return the start
	 */
	public Date getStart() {
		return start;
	}
	/**
	 * @param start the start to set
	 */
	public void setStart(Date start) {
		this.start = start;
	}
	/**
	 * @return the end
	 */
	public Date getEnd() {
		return end;
	}
	/**
	 * @param end the end to set
	 */
	public void setEnd(Date end) {
		this.end = end;
	}
	/**
	 * @return the error
	 */
	public CalendarErrorJson getError() {
		return error;
	}
	/**
	 * @param error the error to set
	 */
	public void setError(CalendarErrorJson error) {
		this.error = error;
	}
	
}
