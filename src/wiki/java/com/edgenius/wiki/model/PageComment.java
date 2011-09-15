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

import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.model.TouchedInfo;
import com.edgenius.wiki.gwt.client.model.CascadeObject;

@Entity
@Table(name=Constants.TABLE_PREFIX+"PAGE_COMMENTS")
public class PageComment extends TouchedInfo implements Serializable,Cloneable, CascadeObject<PageComment>{
	private static final long serialVersionUID = -4097558201110769057L;

	private static transient final Logger log = LoggerFactory.getLogger(PageComment.class);
	
	public static final int NOTIFY_SENT = 0;
	public static final int NOTIFY_SENT_PER_POST = 1;
	public static final int NOTIFY_SENT_SUMMARY = 2;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "key_seq")  
	@SequenceGenerator(name = "key_seq", sequenceName = Constants.TABLE_PREFIX+"PAGE_COMMENTS_SEQ")  
	@Column(name="PUID")
	private Integer uid;
	
	@Column(name="PHASE_ID")
	private int phaseID;
	
	//so far, normal comment has not subject, but this field is used to save commentID if Space links external blog.
	@Column(name="SUBJECT")
	private String subject;
	
	@Type(type="text")
	@Column(name="BODY",length=409600)
	private String body;
	
	@Column(name="LAST_REPLY_DATE")
	private Date lastReplyDate;

	@Column(name="TREE_LEVEL")
	private int level;
	@ManyToOne
	@JoinColumn(name="ROOT_PUID",nullable=true)
	private PageComment root;
	
	@ManyToOne
	@JoinColumn(name="PARENT_PUID",nullable=true)
	private PageComment parent;
	@ManyToOne
	@JoinColumn(name="PAGE_PUID",nullable=false)
	private Page page;
	
	@Column(name="HIDE")
	private boolean hide;
	
	@Column(name="NOTIFY_STATUS")
	private int notifyStatus;
	//********************************************************************
	//               Function method
	//********************************************************************
	/**
	 * Deep clone user object, all clone will set its Uid as null, to future saving.
	 */
	public Object clone(){
		PageComment comment= null;
		try {
			comment = (PageComment) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return comment;
	}

	//from CascadeObject interface
	//JDK1.6 @Override
	public boolean before(PageComment comment) {
		
		return this.getCreatedDate().before(comment.getCreatedDate());
	}

	//********************************************************************
	//                       Set / Get
	//********************************************************************

	public String getBody() {
		return body == null?"":body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public boolean isHide() {
		return hide;
	}
	public void setHide(boolean hide) {
		this.hide = hide;
	}
	public Date getLastReplyDate() {
		return lastReplyDate;
	}
	public void setLastReplyDate(Date lastReplyDate) {
		this.lastReplyDate = lastReplyDate;
	}
	public Page getPage() {
		return page;
	}
	public void setPage(Page page) {
		this.page = page;
	}
	public PageComment getParent() {
		return parent;
	}
	public void setParent(PageComment parent) {
		this.parent = parent;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public Integer getUid() {
		return uid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public PageComment getRoot() {
		return root;
	}
	public void setRoot(PageComment root) {
		this.root = root;
	}

	public int getNotifyStatus() {
		return notifyStatus;
	}

	public void setNotifyStatus(int notifyStatus) {
		this.notifyStatus = notifyStatus;
	}

	public int getPhaseID() {
		return phaseID;
	}

	public void setPhaseID(int phaseID) {
		this.phaseID = phaseID;
	}

	
}
