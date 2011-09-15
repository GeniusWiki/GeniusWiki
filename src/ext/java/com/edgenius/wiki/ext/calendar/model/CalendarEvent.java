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
package com.edgenius.wiki.ext.calendar.model;

import static com.edgenius.core.Constants.TABLE_PREFIX;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.model.TouchedInfo;

/**
 * @author Dapeng.Ni
 */
@Entity
@Table(name=TABLE_PREFIX+"EXT_CAL_EVENTS")
public class CalendarEvent extends TouchedInfo implements Cloneable, Serializable{
	private static final long serialVersionUID = -3381706830953283441L;
	private static final Logger log = LoggerFactory.getLogger(CalendarEvent.class);
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator="key_seq")
	@SequenceGenerator(name="key_seq", sequenceName=Constants.TABLE_PREFIX+"CAL_EVENT_SEQ")
	@Column(name="PUID")
	private Integer uid;
	
	@Column(name="EVENT_SUBJECT")
	private String subject;
	
	@Type(type="text")
	@Column(name="EVENT_CONTENT", length=4000)
	private String content;
	
	@Column(name="ALL_DAY_EVENT")
	private boolean isAllDayEvent;

	//basic, 5 parts separate by comma, in part, separate by "-"
	//type: [yearly, monthly, weekly, daily]
	//repeat on month/week: [W(1-7)|M(1-31)] (if monthly) [1-7] (if weekly)
	//step: any number, default to 0
	//exception: [MMddyyyy,...] - if some specified event is deleted.
	//end: MMddyyyy - if some specified event is deleted.
	//example: 
	// yearly,,,,   --- yearly for ever
	// monthly,M10,2,,12102010  -- every 3 month, 10th, end on 12102010
	// weekly,1-2-3-4-5,0,01122010-01252010,  -- every weekday(Mon to Fri) except by 01/12/2010 and 01/25/2010(delete by users)
	@Column(name="REPEAT_RULE", length=32)
	private String repeatRule;
	
	@Column(name="START_TIME")
	private Date start;
	
	@Column(name="END_TIME")
	private Date end;
	
	//color
	@Column(name="EVENT_CATEGORY")
	private int category;
	
	//could be email?
	@Column(name="EVENT_URL", length=255)
	private String url;
	
	@Column(name="EVENT_CHAIR", length=255)
	private String chair;
	
	@Column(name="EVENT_INVITEES", length=255)
	private String invitees;
	
	@Column(name="EVENT_LOCATION", length=255)
	private String location;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CAL_PUID", nullable=false)
	private Calendar calendar;
	
	//********************************************************************
	//               function method
	//********************************************************************
	public Object clone(){
		CalendarEvent node = null;
		try {
			node = (CalendarEvent) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed ", e);
		}
		return node;
	}

	//********************************************************************
	//               set /get 
	//********************************************************************
	public Integer getUid() {
		return uid;
	}


	public void setUid(Integer uid) {
		this.uid = uid;
	}


	public String getSubject() {
		return subject;
	}


	public void setSubject(String subject) {
		this.subject = subject;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}


	public boolean isAllDayEvent() {
		return isAllDayEvent;
	}


	public void setAllDayEvent(boolean isAllDayEvent) {
		this.isAllDayEvent = isAllDayEvent;
	}


	public Date getStart() {
		return start;
	}


	public void setStart(Date start) {
		this.start = start;
	}


	public Date getEnd() {
		return end;
	}


	public void setEnd(Date end) {
		this.end = end;
	}


	public int getCategory() {
		return category;
	}


	public void setCategory(int category) {
		this.category = category;
	}


	public String getLocation() {
		return location;
	}


	public void setLocation(String location) {
		this.location = location;
	}

	public String getChair() {
		return chair;
	}

	public void setChair(String chair) {
		this.chair = chair;
	}

	public String getInvitees() {
		return invitees;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setInvitees(String invitees) {
		this.invitees = invitees;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
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
