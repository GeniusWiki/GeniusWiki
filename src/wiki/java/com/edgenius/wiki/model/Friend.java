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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.edgenius.core.Constants;
import com.edgenius.core.model.TouchedInfo;

/**
 * @author Dapeng.Ni
 */
@Entity
@Table(name=Constants.TABLE_PREFIX+"FRIENDS")
public class Friend extends TouchedInfo{
	private static final long serialVersionUID = -3272862249454158471L;

	public static final String PREFIX_SPACE = "space_";
	
	public static final int STATUS_PENDING = 0;
	public static final int STATUS_CONFIRMED = 1;
	public static final int STATUS_REJECTED = 2;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO , generator = "key_seq")  
	@SequenceGenerator(name = "key_seq", sequenceName = Constants.TABLE_PREFIX+"FRIENDS_SEQ")  
	@Column(name="PUID")
	private Integer uid;
	
	//PREFIX_spaceUname
	@Index(name="FRIEND_SENDER_INDEX")
	@Column(name="SENDER")
	private String sender;
	
	//PREFIX_spaceUname
	@Index(name="FRIEND_RECEIVER_INDEX")
	@Column(name="RECEIVER")
	private String receiver;
	
	@Column(name="STATUS")
	private int status;
	
	//********************************************************************
	//               set /get 
	//********************************************************************
	public Integer getUid() {
		return uid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public boolean isConfirmed(){
		return (status == STATUS_CONFIRMED)?true:false;
	}
	public boolean isRejected(){
		return (status == STATUS_REJECTED)?true:false;
	}
	public boolean isPending(){
		return (status == STATUS_PENDING)?true:false;
	}
}
