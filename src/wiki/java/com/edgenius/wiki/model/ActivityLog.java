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
package com.edgenius.wiki.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.model.User;

/**
 * @author Dapeng.Ni
 */
@Entity
@Table(name=Constants.TABLE_PREFIX+"ACTIVITY_LOG")
@org.hibernate.annotations.Table(appliesTo = Constants.TABLE_PREFIX+"ACTIVITY_LOG",
indexes = { @Index(name = "ACTIVITY_TYPE_INDEX", columnNames = {"ACTIVITY_TYPE","ACTIVITY_SUB_TYPE"})})
public class ActivityLog implements Serializable, Cloneable{
	private static final long serialVersionUID = -3399606396724975212L;
	private static final Logger log = LoggerFactory.getLogger(ActivityLog.class);
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO , generator = "key_seq")  
	@SequenceGenerator(name = "key_seq", sequenceName =Constants.TABLE_PREFIX+ "ACTIVITY_LOG_SEQ")  
	@Column(name="PUID")
	private Long uid;
	
	//@see com.edgenius.wiki.ActivityType.getCode()
	@Column(name="ACTIVITY_TYPE")
	private int type;
	@Column(name="ACTIVITY_SUB_TYPE")
	private int subType;
	
	@Column(name="SRC_RESOURCE_TYPE")
	private int srcResourceType;
	@Column(name="SRC_RESOURCE_NAME",nullable=true)
	private String srcResourceName;
	
	@Column(name="TGT_RESOURCE_TYPE")
	private int tgtResourceType;
	@Column(name="TGT_RESOURCE_NAME",nullable=true)
	private String tgtResourceName;

	@Column(name="EXTRO_INFO",nullable=true)
	private String extroInfo;
	
	@ManyToOne
	@JoinColumn(name="CREATOR_PUID",nullable=true)
	private User creator;
	
	@Column(name="CREATED_DATE")
	private Date createdDate;
	
	//private log info etc.
	@Column(name="ACTIVITY_STATUS")
	private int status;
	
	//message build from activity database record.
	@Transient
	private String message;
	
	public Object clone(){
		ActivityLog act = null;
		try {
			act = (ActivityLog) super.clone();
			
		} catch (Exception e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return act;

	}
	public String toString(){
		return new StringBuilder().append(type).append(":").append(subType).append(":").append(srcResourceType)
			.append(":").append(srcResourceName).append(":").append(tgtResourceType)
			.append(":").append(tgtResourceName).toString();
	}
	
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public Long getUid() {
		return uid;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSubType() {
		return subType;
	}

	public void setSubType(int subType) {
		this.subType = subType;
	}

	public int getSrcResourceType() {
		return srcResourceType;
	}

	public void setSrcResourceType(int srcResourceType) {
		this.srcResourceType = srcResourceType;
	}

	public String getSrcResourceName() {
		return srcResourceName;
	}

	public void setSrcResourceName(String srcResourceName) {
		this.srcResourceName = srcResourceName;
	}

	public int getTgtResourceType() {
		return tgtResourceType;
	}

	public void setTgtResourceType(int tgtResourceType) {
		this.tgtResourceType = tgtResourceType;
	}

	public String getTgtResourceName() {
		return tgtResourceName;
	}

	public void setTgtResourceName(String tgtResourceName) {
		this.tgtResourceName = tgtResourceName;
	}

	public String getExtroInfo() {
		return extroInfo;
	}

	public void setExtroInfo(String extroInfo) {
		this.extroInfo = extroInfo;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
