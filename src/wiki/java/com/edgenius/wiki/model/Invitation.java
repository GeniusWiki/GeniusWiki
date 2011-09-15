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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.Constants;
import com.edgenius.core.model.TouchedInfo;

/**
 * @author Dapeng.Ni
 */
@Entity
@Table(name=Constants.TABLE_PREFIX+"INVITATION")
public class Invitation extends TouchedInfo implements Serializable{
	private static final long serialVersionUID = 7566551301152990890L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO , generator = "key_seq")  
	@SequenceGenerator(name = "key_seq", sequenceName = Constants.TABLE_PREFIX+"INVITATION_SEQ")  
	@Column(name="PUID")
	private Integer uid;
	
	@Column(name="INVITE_UUID")
	private String uuid;
	@Column(name="SPACE_UNAME")
	private String spaceUname;
	
	@Column(name="TO_EMAIL_GROUP")
	private String toEmailGroup;
	
	@Column(name="INVITATION_MESSAGE")
	private String message;
	
	/**
	 * @param email
	 * @return
	 */
	public boolean includeUserEmail(String inEmail) {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// check if this email still inside emailGroup, if not, it means this 
		//invitation already be accepted, don't need continue handle
		String emailGroup = this.getToEmailGroup();
		String[] emails = emailGroup.split(",");
		for (String email : emails) {
			if(StringUtils.equalsIgnoreCase(email, inEmail)){
				return true;
			}
		}
		return false;
	}
	
	//********************************************************************
	//               set / get 
	//********************************************************************
	public Integer getUid() {
		return uid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getSpaceUname() {
		return spaceUname;
	}
	public void setSpaceUname(String spaceUname) {
		this.spaceUname = spaceUname;
	}
	public String getToEmailGroup() {
		return toEmailGroup;
	}
	public void setToEmailGroup(String toEmail) {
		this.toEmailGroup = toEmail;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	
}
