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

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.Global;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.FileUtil;
import com.edgenius.wiki.ext.calendar.CalendarEventJson;
import com.edgenius.wiki.ext.calendar.dao.CalendarDAO;
import com.edgenius.wiki.ext.calendar.dao.CalendarEventDAO;
import com.edgenius.wiki.ext.calendar.model.Calendar;
import com.edgenius.wiki.ext.calendar.model.CalendarEvent;
import com.edgenius.wiki.plugin.PluginServiceProvider;
import com.edgenius.wiki.plugin.PluginServiceProviderException;
import com.edgenius.wiki.service.DataBinder;
import com.edgenius.wiki.service.EventContainer;
import com.edgenius.wiki.service.PageEventHanderException;
import com.edgenius.wiki.service.PageEventListener;
import com.edgenius.wiki.service.PageException;
import com.edgenius.wiki.util.WikiUtil;
import com.google.gson.GsonBuilder;

/**
 * @author Dapeng.Ni
 */
@Transactional
public class CalendarServiceImpl  implements CalendarService, PageEventListener, PluginServiceProvider{
	private static final Logger log = LoggerFactory.getLogger(CalendarServiceImpl.class);
	
	private CalendarDAO calendarDAO;  
	private CalendarEventDAO calendarEventDAO;
	private EventContainer eventContainer;
	private UserReadingService userReadingService; 
	private static DateFormat ruleDateParser = new SimpleDateFormat("MMddyyyy");
	
	public String invokePluginService(String operation, String[] params) throws PluginServiceProviderException {
		if("getEvent".equalsIgnoreCase(operation)){
			if(params == null || params.length != 1 || NumberUtils.toInt(params[0],-1) == -1){
				throw new PluginServiceProviderException("Unable to get correct event ID in parameter");
			}
			
			Integer eventID = NumberUtils.toInt(params[0]);
			CalendarEvent event = calendarEventDAO.get(eventID);
			if(event == null){
				throw new PluginServiceProviderException("Unable to get event.");
			}
			
			GsonBuilder gsonBuild = new GsonBuilder();
			return gsonBuild.create().toJson(CalendarEventJson.from(event));
		}else if("saveEvent".equalsIgnoreCase(operation)){
//			pageUuid.getValue(),
//			calendarName.getValue(),
//			eventID.getValue(),
//			color.getText(),
//			subject.getText(), 
//			String.valueOf(st.getTime()), 
//			String.valueOf(ed.getTime()), 
//			location.getText(),
//			description.getText(),
//			String.valueOf(isAllDayEvent.getValue()),
//			repeatRule.getText()
			if(params == null || params.length != 11){
				throw new PluginServiceProviderException("Unexpected parameters for saveEvent");
			}
			int idx=0;
			
			String pageUuid = params[idx++];
			String calendarName = params[idx++];
			int eventID = NumberUtils.toInt(params[idx++]);
			int colorType = NumberUtils.toInt(params[idx++]);
			String subject = params[idx++];
			
			long time = NumberUtils.toLong(params[idx++]);
			if(time == 0)
				throw new PluginServiceProviderException("Unexpected start time for saveEvent " + time);
			Date st = new Date(time);
			
			time = NumberUtils.toLong(params[idx++]);
			if(time == 0)
				throw new PluginServiceProviderException("Unexpected end time for saveEvent " + time);
			
			Date ed = new Date(time);
			String location = params[idx++];
			String desc = params[idx++];
			boolean isAllDay = BooleanUtils.toBoolean(params[idx++]);
			String repeatRule = params[idx++];
			
			CalendarEvent event;
			if(eventID == 0){
				log.info("new event saved for ");
				event = new CalendarEvent();
				Calendar cal = this.getCalendar(pageUuid, calendarName);
				if(cal == null)
					throw new PluginServiceProviderException("Failed get calendar from page " + pageUuid + " by name " + calendarName);
				
				event.setCalendar(cal);
			}else{
				event = calendarEventDAO.get(eventID);
				if(event == null){
					throw new PluginServiceProviderException("Failed get event by id" + eventID);
				}
			}
			
			event.setCategory(colorType);
			event.setSubject(subject);
			event.setStart(st);
			event.setEnd(ed);
			event.setLocation(location);
			event.setContent(desc);
			event.setAllDayEvent(isAllDay);
			event.setRepeatRule(repeatRule);
			WikiUtil.setTouchedInfo(userReadingService, event);
			calendarEventDAO.saveOrUpdate(event);
			return "";
		}else{
			throw new PluginServiceProviderException("Invalid operation request " + operation);
		}
	}
	

	
	public void saveOrUpdateCalendar(Calendar cal){
		if(cal.getUid() == null){
			//add this calendar to PageEventListener
			eventContainer.addPageEventListener(cal.getPageUuid(), this);
		}
		
		calendarDAO.saveOrUpdate(cal);
		
	}
	public List<Calendar> getCalendars() {
		
		return calendarDAO.getObjects();
	}
	public void removePageCalendars(String pageUuid) {
		List<Calendar> cals = calendarDAO.getPageCalendars(pageUuid);
		//We assume it won't have many calendar in same page, so below has minor performance impact.
		for (Calendar cal : cals) {
			calendarDAO.removeObject(cal);
		}
	}
	
	public Calendar getCalendar(String pageUuid, String calendarName) {
		
		return calendarDAO.getCalendar(pageUuid,calendarName);
	}
	
	public List<CalendarEvent> getEvents(String calName, String pageUuid, Date calStart, Date calEnd){
		return calendarEventDAO.getEvents(calName,pageUuid,calStart,calEnd);
	}
	public void saveOrUpdateEvent(CalendarEvent evt){
		calendarEventDAO.saveOrUpdate(evt);
	}
	
	public void removeEvent(int eventID){
		calendarEventDAO.remove(eventID);
	}
	
	public CalendarEvent getEvent(int eventID) {
		return calendarEventDAO.get(eventID);
	}
	/**
	 * Method call by Spring "init-method" bean initialise.
	 */
	public void initListeners(){
		
		//initial listeners for pages
		List<Calendar> cals = this.getCalendars();
		for (Calendar cal : cals) {
			eventContainer.addPageEventListener(cal.getPageUuid(), this);
		}
	}
	//********************************************************************
	//               Methods from PluginServiceProvider
	//********************************************************************
	public void backup(DataBinder binder) {
		List<Calendar> cals = calendarDAO.getObjects();
		for (Calendar cal : cals) {
			cal.setEvents(null);
		}
		binder.addAll(Calendar.class.getName(),cals);
		
		List<CalendarEvent> events = calendarEventDAO.getObjects();
		binder.addAll(CalendarEvent.class.getName(),events);
		
		
	}

	public void resorePreClean() {
		calendarEventDAO.cleanTable();
		calendarDAO.cleanTable();
	}

	public void restore(DataBinder binder) {

		List<Calendar> cals = (List<Calendar>) binder.get(Calendar.class.getName());
		if(cals != null){
			for (Calendar cal : cals) {
				cal.setUid(null);
				calendarDAO.saveOrUpdate(cal);
			}
		}		
		List<CalendarEvent> events = (List<CalendarEvent>) binder.get(CalendarEvent.class.getName());
		if(events != null){
			for (CalendarEvent event : events) {
				event.setUid(null);
				calendarEventDAO.saveOrUpdate(event);
			}
		}		
		
		initListeners();
	}
	
	public Map<File, String> exportResources() {
		Map<File, String> map = new HashMap<File, String>();
		map.put(new File(FileUtil.getFullPath(Global.ServerInstallRealPath, "plugins","calendar","resources","cal-print.css")),
				FileUtil.getFullPath( "plugins","calendar","resources"));
		return map;
	}

	
	//********************************************************************
	//               Methods from PageEventListener
	//********************************************************************
	public void pageRemoving(String pageUuid, boolean permanent) throws PageEventHanderException, PageException {
		if(!permanent)
			return;
		
		this.removePageCalendars(pageUuid);
		eventContainer.removePageEventListeners(pageUuid);
	}

	public void pageSaving(String pageUuid) throws PageEventHanderException, PageException {
		//Do nothing
		
	}
	//********************************************************************
	//               Private method
	//********************************************************************

	/**
	 * Check if this repeat event appear in given period. 
	 * @param event
	 * @param start
	 * @param end
	 */
	@SuppressWarnings("deprecation")
	private List<CalendarEvent> getRepeatEventsInPeriod(CalendarEvent event, Date start, Date end){
		String rule = event.getRepeatRule();
		if(StringUtils.isBlank(rule))
			return null;
		
		//if first event is after scope, then return null
		if(event.getStart().after(end))
			return null;
		
		String[] parts = rule.split(",");
		if(parts.length != 5){
			return null;
		}
		
		if(parts[4].length() !=0){
			//if end is early than start, then simple return null.
			try {
				Date rEnd = ruleDateParser.parse(parts[4]);
				if(rEnd.before(start))
					return null;
			} catch (ParseException e) {
				log.error("Unable to parse end date of event " + event.getUid());
			}
		}
		
		//parse and calculate according to type, repeat and step
		int step = NumberUtils.toInt(parts[2]) + 1;
		
		List<Date[]> candidates = new ArrayList<Date[]>(); 
		Date evtS = event.getStart();
		Date evtE = event.getEnd();
		
		Date candidateS;
		Date candidateE;
		
		if("yearly".equals(parts[0])){
			if((start.getYear() - evtS.getYear())%step ==0){
				int idx = start.getYear();
				do{
					
				}while(idx <= end.getYear());
			}
		}else if ("monthly".equals(parts[0])){
			
		}else if ("weekly".equals(parts[0])){
			
		}else if ("daily".equals(parts[0])){
			
		}
		return null;
	}
	//********************************************************************
	//               set / get
	//********************************************************************
	public void setCalendarDAO(CalendarDAO calendarDAO) {
		this.calendarDAO = calendarDAO;
	}

	public void setCalendarEventDAO(CalendarEventDAO calendarEventDAO) {
		this.calendarEventDAO = calendarEventDAO;
	}

	public void setEventContainer(EventContainer eventContainer) {
		this.eventContainer = eventContainer;
	}

	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}

}
