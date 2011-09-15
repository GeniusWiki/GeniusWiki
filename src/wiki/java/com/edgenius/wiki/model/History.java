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

/**
 * @author Dapeng.Ni
 */
@Entity
@Table(name=Constants.TABLE_PREFIX+"HISTORIES")
@org.hibernate.annotations.Table(appliesTo = Constants.TABLE_PREFIX+"HISTORIES",
indexes = { @Index(name = "HISTORY_UUID_INDEX", columnNames = {"PAGE_UUID"}),
		@Index(name = "HISTORY_TITLE_INDEX", columnNames = {"TITLE"}),
		@Index(name = "HISTORY_UNIXNAME_INDEX", columnNames = {"UNIX_NAME"})})		
public class History extends AbstractPage {
	private static final long serialVersionUID = 4432659542092638387L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO , generator = "key_seq")  
	@SequenceGenerator(name = "key_seq", sequenceName = Constants.TABLE_PREFIX+"HISTORIES_SEQ")  
	@Column(name="PUID")
	private Integer uid;
	
	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="CONTENT_PUID",nullable=true)
	private HistoryContent content;
	
	
	public Object clone(){
		History cPage = null;
		try {
			cPage = (History) super.clone();
			//deep clone
			if(content != null){
				cPage.setContent((HistoryContent) content.clone());
			}
		} catch (Exception e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}

		return cPage;

	}
	public HistoryContent getContent() {
		return content;
	}

	public void setContent(HistoryContent content) {
		this.content = content;
	}
	

	/**
	 * Copy history to page.
	 * @return
	 */
	public Page cloneToPage() {
		Page page = new Page();

		page.setUid(null);
		page.setSpace(this.getSpace());
		page.setType(this.getType());
		page.setVersion(this.getVersion());
		page.setTitle(this.getTitle());
		page.setPageUuid(this.getPageUuid());
		page.setParent(this.getParent());
		page.setUnixName(this.getUnixName());
		page.setModifiedDate(this.getModifiedDate());
		page.setModifier(this.getModifier());
		page.setCreatedDate(this.getCreatedDate());
		page.setCreator(this.getCreator());
		
		PageContent content = new PageContent();
		if(this.getContent() != null){
			content.setContent(this.getContent().getContent());
			content.setUid(null);
		}
		page.setContent(content);
		
		return page;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}
}
