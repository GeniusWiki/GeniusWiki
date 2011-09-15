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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.edgenius.core.Constants;
import com.edgenius.core.model.TouchedInfo;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
@Entity
@Table(name=Constants.TABLE_PREFIX+"NOTIFICATION")
public class Notification extends TouchedInfo{

	public static final int STATUS_OPEN = 0;
	public static final int STATUS_CLOSED = 1;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO , generator = "key_seq")  
	@SequenceGenerator(name = "key_seq", sequenceName = Constants.TABLE_PREFIX+"NOTIFICATION_SEQ")  
	@Column(name="PUID")
	private Integer uid;

	@Type(type="text")
	@Column(name="NOTIFY_MESSAGE",length=5012)
	private String message;
	
	@Column(name="EXPIRED_DATE", nullable=true)
	private Date expiredDate;
	
	//could be spaceUname, Instance Name, or PageUuid, role name or user name
	@Column(name="TARGET_TYPE",nullable=true)
	private int targetType;
	
	@Column(name="TARGET_NAME",nullable=true)
	private String targetName;
	
	@Column(name="STATUS")
	private int status;
	
	//identify if viewer has permission to delete this message - for render purpose only
	@Transient
	private transient boolean removable;
	
	//********************************************************************
	//               Set / Get
	//********************************************************************

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getMessage() {
		return message == null?"":message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getExpiredDate() {
		return expiredDate;
	}

	public void setExpiredDate(Date expiredDate) {
		this.expiredDate = expiredDate;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public int getTargetType() {
		return targetType;
	}

	public void setTargetType(int targetType) {
		this.targetType = targetType;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isRemovable() {
		return removable;
	}

	public void setRemovable(boolean removable) {
		this.removable = removable;
	}
}
