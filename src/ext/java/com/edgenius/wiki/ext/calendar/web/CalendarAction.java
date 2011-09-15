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
package com.edgenius.wiki.ext.calendar.web;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.Constants;
import com.edgenius.core.util.TimeZoneUtil;
import com.edgenius.wiki.ext.calendar.CalendarConstants;
import com.edgenius.wiki.ext.calendar.CalendarErrorJson;
import com.edgenius.wiki.ext.calendar.CalendarJson;
import com.edgenius.wiki.ext.calendar.CalendarUtil;
import com.edgenius.wiki.ext.calendar.QuickReturnJson;
import com.edgenius.wiki.ext.calendar.model.Calendar;
import com.edgenius.wiki.ext.calendar.model.CalendarEvent;
import com.edgenius.wiki.ext.calendar.service.CalendarService;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.util.WikiUtil;
import com.edgenius.wiki.webapp.action.BaseAction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author Dapeng.Ni
 */
public class CalendarAction extends BaseAction{
	private static final long serialVersionUID = -2890473324999630280L;

	private static final String CAL_DATEFORMAT = "MM/dd/yyyy hh:mm";
	
	private CalendarService calendarService;
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//  URL parameters
	private String calendarTitle;
	private String calendarStartTime;
	private String calendarEndTime;
	private int isAllDayEvent;
	private String timezone;
	
	private int calendarId;
	
	private String calendarName;
	private String pageUuid;
	private String viewType;
	private int weekStartDay;
	//********************************************************************
	//               Function methods
	//********************************************************************
	
	/**
	 * Browser all events in calendar.
	 */
	public String execute(){
		CalendarJson json;
		try {
			Date[] scope = CalendarUtil.getCalendarScope(CalendarConstants.VIEW.valueOf(viewType.toUpperCase()), new Date(), weekStartDay);
			List<CalendarEvent> calEvents = calendarService.getEvents(calendarName, pageUuid, scope[0], scope[1]);
			json = CalendarJson.toJson(calEvents, scope[0], scope[1]);
		} catch(Exception e){
			CalendarErrorJson error = new CalendarErrorJson("001","Get calendar data failed");
			json = new CalendarJson();
			json.setError(error);
			log.error("Get calendar failed", e);
		}
		
		try{
			GsonBuilder gsonBuild = new GsonBuilder();
			gsonBuild.registerTypeAdapter(Date.class, new DateSerializer());
			String jsonstr = gsonBuild.create().toJson(json);
			
			//hack - remove "@1111@" to new Date(1111) format. 
			String[] list = StringUtil.splitWithoutEscaped(jsonstr, "\"@");
			jsonstr = StringUtil.join("new Date(", list);
			jsonstr = jsonstr.replaceAll("@\"", ")");
			
			getResponse().getOutputStream().write(jsonstr.getBytes(Constants.UTF8));
		} catch (IOException e) {
			log.error("Calendar view get data failed", e);
		}
		return null;
	}
	 
	public String quickAdd(){
		QuickReturnJson json = new QuickReturnJson();
		if(StringUtils.isBlank(calendarName)){
			json.setIsSuccess(false);
			json.setMsg("Unable to get valid calendar name");
		}else{
			try {
				Calendar cal = calendarService.getCalendar(pageUuid, calendarName);
				if(cal == null){
					cal = new Calendar();
					cal.setName(calendarName);
					cal.setPageUuid(pageUuid);
					WikiUtil.setTouchedInfo(userReadingService, cal);
					calendarService.saveOrUpdateCalendar(cal);
				}
				CalendarEvent evt = new CalendarEvent();
				evt.setCalendar(cal);
				evt.setAllDayEvent(isAllDayEvent>0);
				
				DateFormat format = getDateFormat();
				Date start = format.parse(calendarStartTime);
				Date end = format.parse(calendarEndTime);
				evt.setStart(start);
				evt.setEnd(end);
				WikiUtil.setTouchedInfo(userReadingService, evt);
				evt.setSubject(calendarTitle);
				calendarService.saveOrUpdateEvent(evt);
				
				json.setIsSuccess(true);
				json.setMsg("Save success");
				json.setData(String.valueOf(evt.getUid()));
			} catch (ParseException e) {
				json.setIsSuccess(false);
				json.setMsg("Unable to get valid calendar start/end date");
				log.error("Unable to parse calendar start/end date format", e);
			} catch (Exception e) {
				json.setIsSuccess(false);
				json.setMsg("Save calendar event failed.");
				log.error("Save calendar error", e);
			}
		}
		try {
			Gson gson = new Gson();
			String jsonstr = gson.toJson(json);
			getResponse().getOutputStream().write(jsonstr.getBytes(Constants.UTF8));
		} catch (IOException e) {
			log.error("unable response event save request",e);
		}
		return null;
	}

	public String quickUpdate(){
		QuickReturnJson json = new QuickReturnJson();
		CalendarEvent evt = calendarService.getEvent(calendarId);
		if(evt == null){
			json.setIsSuccess(false);
			json.setMsg("Unable to get event");
		}else{
			try{
				DateFormat format = getDateFormat();
				Date start = format.parse(calendarStartTime);
				Date end = format.parse(calendarEndTime);
				evt.setStart(start);
				evt.setEnd(end);
				WikiUtil.setTouchedInfo(userReadingService, evt);
				calendarService.saveOrUpdateEvent(evt);
				json.setIsSuccess(true);
				json.setMsg("Update success");
			} catch (ParseException e) {
				json.setIsSuccess(false);
				json.setMsg("Unable to get valid event start/end date");
				log.error("Unable to parse calendar start/end date format", e);
			}
		}
		try {
			Gson gson = new Gson();
			String jsonstr = gson.toJson(json);
			getResponse().getOutputStream().write(jsonstr.getBytes(Constants.UTF8));
		} catch (IOException e) {
			log.error("unable response event save request",e);
		}
		
		return null;
	}
	public String delete(){
		try {
			calendarService.removeEvent(calendarId);
			QuickReturnJson json = new QuickReturnJson();
			json.setIsSuccess(true);
			json.setMsg("Remove success");
			Gson gson = new Gson();
			String jsonstr = gson.toJson(json);
			getResponse().getOutputStream().write(jsonstr.getBytes(Constants.UTF8));
		} catch (IOException e) {
			log.error("unable response event save request",e);
		}
		return null;
	}
	

	/**
	 * @return
	 */
	private DateFormat getDateFormat() {
		TimeZone tz = TimeZoneUtil.guessTimeZone(timezone);
		DateFormat format = new SimpleDateFormat(CAL_DATEFORMAT);
		format.setTimeZone(tz);
		return format;
	}
	//********************************************************************
	//               set / get
	//********************************************************************
	public void setCalendarService(CalendarService calendarService) {
		this.calendarService = calendarService;
	}

	/**
	 * @param calendarTitle the calendarTitle to set
	 */
	public void setCalendarTitle(String calendarTitle) {
		this.calendarTitle = calendarTitle;
	}

	/**
	 * @param calendarStartTime the calendarStartTime to set
	 */
	public void setCalendarStartTime(String calendarStartTime) {
		this.calendarStartTime = calendarStartTime;
	}

	/**
	 * @param calendarEndTime the calendarEndTime to set
	 */
	public void setCalendarEndTime(String calendarEndTime) {
		this.calendarEndTime = calendarEndTime;
	}

	/**
	 * @param isAllDayEvent the isAllDayEvent to set
	 */
	public void setIsAllDayEvent(int isAllDayEvent) {
		this.isAllDayEvent = isAllDayEvent;
	}

	/**
	 * @param timezone the timezone to set
	 */
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}


	/**
	 * @param pageUuid the pageUuid to set
	 */
	public void setPageUuid(String pageUuid) {
		this.pageUuid = pageUuid;
	}

	/**
	 * @param calendarName the calendarName to set
	 */
	public void setCalendarName(String calendarName) {
		this.calendarName = calendarName;
	}


	public void setViewType(String viewType) {
		this.viewType = viewType;
	}

	public void setWeekStartDay(int weekStartDay) {
		this.weekStartDay = weekStartDay;
	}


	/**
	 * @return the eventID
	 */
	public int getCalendarId() {
		return calendarId;
	}

	/**
	 * @param eventID the eventID to set
	 */
	public void setCalendarId(int eventID) {
		this.calendarId = eventID;
	}
	
	//********************************************************************
	//               private class
	//********************************************************************
	private class DateSerializer implements JsonSerializer<Date> {
		public JsonElement serialize(Date src, Type type, JsonSerializationContext context) {
			  return new JsonPrimitive("@"+src.getTime()+"@");
//			  return new JsonPrimitive("\\/Date("+src.getTime()+")\\/");
		}
	}

}
