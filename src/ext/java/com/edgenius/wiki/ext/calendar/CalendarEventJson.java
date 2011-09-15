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

import org.apache.commons.lang.StringUtils;

import com.edgenius.wiki.ext.calendar.model.CalendarEvent;

/**
 * For ajax call get back event information for edit/view event dialog.
 * @author Dapeng.Ni
 */
public class CalendarEventJson implements Serializable{
	private static final long serialVersionUID = -5681571050664040481L;
	
	private int eventID; 
	private String subject;
	private long startDate;
	private long endDate;
	private int color;
	private boolean isAllDayEvent;
	private String repeatRule;
	private String location;
	private String content;
	
	public static CalendarEventJson from(CalendarEvent evt){
		CalendarEventJson json = new CalendarEventJson();
		json.eventID = evt.getUid();
		json.subject = StringUtils.trimToEmpty(evt.getSubject());
		json.startDate = evt.getStart().getTime();
		json.endDate = evt.getEnd().getTime();
		json.color = evt.getCategory();
		json.isAllDayEvent = evt.isAllDayEvent();
		json.repeatRule = StringUtils.trimToEmpty(evt.getRepeatRule());
		json.location = StringUtils.trimToEmpty(evt.getLocation());
		json.content = StringUtils.trimToEmpty(evt.getContent());
		
		return json;
	}
	public int getEventID() {
		return eventID;
	}
	public void setEventID(int eventID) {
		this.eventID = eventID;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public long getStartDate() {
		return startDate;
	}
	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}
	public long getEndDate() {
		return endDate;
	}
	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}
	public int getColor() {
		return color;
	}
	public void setColor(int color) {
		this.color = color;
	}
	public boolean isAllDayEvent() {
		return isAllDayEvent;
	}
	public void setAllDayEvent(boolean isAllDayEvent) {
		this.isAllDayEvent = isAllDayEvent;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * @return the repeatRule
	 */
	public String getRepeatRule() {
		return repeatRule;
	}
	/**
	 * @param repeatRule the repeatRule to set
	 */
	public void setRepeatRule(String repeatRule) {
		this.repeatRule = repeatRule;
	}

	
}
