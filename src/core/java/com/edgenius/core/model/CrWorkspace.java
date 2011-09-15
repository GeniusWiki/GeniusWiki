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
package com.edgenius.core.model;

import static com.edgenius.core.Constants.TABLE_PREFIX;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;

/**
 * @author Dapeng.Ni
 */
@Entity
@Table(name=TABLE_PREFIX+"CR_WORKSPACE")
public class CrWorkspace implements Cloneable,Serializable{
	private static final long serialVersionUID = 7704138115007457480L;

	private static final transient Logger log = LoggerFactory.getLogger(CrWorkspace.class);

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator="key_seq")
	@SequenceGenerator(name="key_seq", sequenceName=Constants.TABLE_PREFIX+"CR_WORKSPACE_SEQ")
	@Column(name="PUID")
	private Integer uid;
	
	@Column(name="NAME",unique=true)
	private String name;
	
	@Column(name="SPACE_UUID",unique=true)
	private String spaceUuid;
	
	@Column(name="USERNAME")
	private String username;
	@Column(name="PASSWORD")
	private String password;
	
	@Column(name="QUOTA")
	private long quota;
	//********************************************************************
	//               Functional methods
	//********************************************************************
	public Object clone(){
		CrWorkspace cWs = null;
		try {
			cWs = (CrWorkspace) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cWs;
	}

	//********************************************************************
	//               Set / Get
	//********************************************************************
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Integer getUid() {
		return uid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getName() {
		return name;
	}
	public void setName(String workspaceName) {
		this.name = workspaceName;
	}

	public String getSpaceUuid() {
		return spaceUuid;
	}

	public void setSpaceUuid(String uuid) {
		this.spaceUuid = uuid;
	}

	public long getQuota() {
		return quota;
	}

	public void setQuota(long quota) {
		this.quota = quota;
	}
}
