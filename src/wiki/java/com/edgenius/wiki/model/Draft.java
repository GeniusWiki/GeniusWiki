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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.edgenius.core.Constants;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
@Entity
@Table(name=Constants.TABLE_PREFIX+"DRAFTS")
@org.hibernate.annotations.Table(appliesTo = Constants.TABLE_PREFIX+"DRAFTS",
indexes = { @Index(name = "DRAFT_UUID_INDEX", columnNames = {"PAGE_UUID"}),
		@Index(name = "DRAFT_TITLE_INDEX", columnNames = {"TITLE"}),
		@Index(name = "DRAFT_UNIXNAME_INDEX", columnNames = {"UNIX_NAME"})})	
public class Draft extends AbstractPage{
	//draft could have 3 type so far: manual(user click "save draft" button) :1, auto (system saving periodically):2, 
	//Offline upload has version conflict, then save it as draft:3
	//draft type:
	public static final int NONE_DRAFT = SharedConstants.NONE_DRAFT;
	public static final int MANUAL_DRAFT = SharedConstants.MANUAL_DRAFT;
	public static final int AUTO_DRAFT = SharedConstants.AUTO_DRAFT;
	public static final int OFFLINE_CONFLICT_DRAFT = SharedConstants.OFFLINE_CONFLICT_DRAFT;
	

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO , generator = "key_seq")  
	@SequenceGenerator(name = "key_seq", sequenceName =Constants.TABLE_PREFIX+ "DRAFTS_SEQ")  
	@Column(name="PUID")
	private Integer uid;
	
	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="CONTENT_PUID",nullable=true)
	private DraftContent content;
	
	//Review, approved:only current page has mapped Progress. draft or others will set as null.
	@OneToOne(fetch = FetchType.LAZY, cascade={CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH})
	@JoinColumn(name="PROGRESS_PUID",nullable=true)
	private PageProgress pageProgress;
	
	//********************************************************************
	//               Function method
	//********************************************************************
	/**
	 * Deep clone user object, all clone will set its Uid as null, to future saving.
	 * @throws CloneNotSupportedException 
	 */
	public Object clone(){
		Draft cPage = null;
		try {
			cPage = (Draft) super.clone();
			//deep clone
			if(content != null){
				cPage.setContent((DraftContent) content.clone());
			}
		} catch (Exception e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cPage;

	}

	public DraftContent getContent() {
		return content;
	}

	public void setContent(DraftContent content) {
		this.content = (DraftContent) content;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public PageProgress getPageProgress() {
		return pageProgress;
	}

	public void setPageProgress(PageProgress pageProgress) {
		this.pageProgress = pageProgress;
	}
}
