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
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.model.TouchedInfo;

/**
 * @author Dapeng.Ni
 */
@Entity
@Table(name=TABLE_PREFIX+"EXT_CAL",uniqueConstraints={@UniqueConstraint(columnNames={"PAGE_UUID","CAL_NAME"})})
@org.hibernate.annotations.Table(appliesTo = Constants.TABLE_PREFIX+"EXT_CAL",
indexes = { @Index(name = "CAL_PUUID_INDEX", columnNames = {"PAGE_UUID"})})
public class Calendar extends TouchedInfo implements Cloneable, Serializable{
	private static final long serialVersionUID = 8161465759131906336L;

	private static final Logger log = LoggerFactory.getLogger(Calendar.class);
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator="key_seq")
	@SequenceGenerator(name="key_seq", sequenceName=Constants.TABLE_PREFIX+"CALENDAR_SEQ")
	@Column(name="PUID")
	private Integer uid;
	
	@Column(name="PAGE_UUID")
	private String pageUuid;
	
	@Column(name="CAL_NAME")
	private String name;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="calendar", orphanRemoval=true)
	private Set<CalendarEvent> events;
	
	//********************************************************************
	//               function method
	//********************************************************************
	public Object clone(){
		Calendar node = null;
		try {
			node = (Calendar) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed ", e);
		}
		return node;
	}
	
	//********************************************************************
	//               set / get
	//********************************************************************
	public Integer getUid() {
		return uid;
	}
	public String getPageUuid() {
		return pageUuid;
	}

	public void setPageUuid(String pageUuid) {
		this.pageUuid = pageUuid;
	}

	
	public Set<CalendarEvent> getEvents() {
		return events;
	}

	public void setEvents(Set<CalendarEvent> events) {
		this.events = events;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


}
