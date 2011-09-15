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

import com.edgenius.core.Constants;
import com.edgenius.core.model.User;
/**
 * Save use favoriate, watched page etc.
 * (14/08/2010) From 2.0, this class is not speified "User" mark.  It allow add a kind of global mark on page. 
 * So far, we can see it is pin-top. At this case, type must equal or greater than 10. And user is become the 
 * update user - it is not key element now.
 * 
 * @author dapeng
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name=Constants.TABLE_PREFIX+"USER_PAGE_MARKS")
public class UserPageMark implements Serializable{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "key_seq")  
	@SequenceGenerator(name = "key_seq", sequenceName = Constants.TABLE_PREFIX+"USER_PAGE_MARKS_SEQ")  
	@Column(name="PUID")
	private Integer uid;
	
	@ManyToOne
	@JoinColumn(name="USER_PUID")
	private User user;
	
	@ManyToOne
	@JoinColumn(name="PAGE_PUID")
	private Page page;
	
	//favorate, watched, pin-top, @see WikiConstants.USER_PAGE_TYPE_*
	@Column(name="MARK_TYPE")
	private int type;

	@Column(name="CREATED_DATE")
	private Date createdDate;
	
	
	//********************************************************************
	//                       Set / Get
	//********************************************************************

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
}
