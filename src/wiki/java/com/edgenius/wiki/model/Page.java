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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Index;

import com.edgenius.core.Constants;
import com.edgenius.wiki.util.WikiUtil;
/**
 * Page model class<BR>
 * Page unixName is constructed by page title and unique within same space - this only true for the space linked from a blog. 
 * Page uuid is unique for the entire system.
 * Page title is alterable.
 *  
 *  Remove this constraints: save old page, create new page, in same method cause exception?! hibernate...
 *  uniqueConstraints={@UniqueConstraint(columnNames={"page_uuid","version"})}
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
@Entity
@Table(name=Constants.TABLE_PREFIX+"PAGES",uniqueConstraints={@UniqueConstraint(columnNames={"PAGE_UUID"})})
@org.hibernate.annotations.Table(appliesTo = Constants.TABLE_PREFIX+"PAGES",
indexes = { @Index(name = "PAGE_TITLE_INDEX", columnNames = {"TITLE"})
			,@Index(name = "PAGE_UNIXNAME_INDEX", columnNames = {"UNIX_NAME"})})
public class Page extends AbstractPage{
	public static final int REMOVE_FLAG_NO = 0;
	public static final int REMOVED = 1;
	public static final int REMOVED_HOMEPAGE = 2;
	

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO , generator = "key_seq")  
	@SequenceGenerator(name = "key_seq", sequenceName = Constants.TABLE_PREFIX+"PAGES_SEQ")  
	@Column(name="PUID")
	private Integer uid;
	
	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="CONTENT_PUID",nullable=true)
	private PageContent content;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name="PAGE_PUID")
	//TODO: refer to Calendard and Todo macro model and remove this obsolete tag.
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private Set<PageLink> links;
	
	//Review, approved:only current page has mapped Progress. draft or others will set as null.
	@OneToOne(fetch = FetchType.LAZY, cascade={CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH})
	@JoinColumn(name="PROGRESS_PUID",nullable=true)
	private PageProgress pageProgress;

	@ManyToMany(targetEntity = PageTag.class, cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH }
		, fetch = FetchType.LAZY)
	@JoinTable(name = Constants.TABLE_PREFIX+"PAGES_TAGS", 
		joinColumns = { @JoinColumn(name = "PAGE_PUID")},
		inverseJoinColumns={ @JoinColumn(name = "TAG_PUID") })
	private List<PageTag> tags;
	
	@Column(name="REMOVED")
	private int removed;
	
	//identify the new created page is home page or not.
	@Transient
	private int newPageType;

	//identify if this page causes a space menu updated event
	@Transient
	private boolean isMenuUpdated;
	//********************************************************************
	// method               
	//********************************************************************
	/**
	 * Deep clone user object, all clone will set its Uid as null, to future saving.
	 */
	public Object clone(){
		Page cPage = null;
		try {
			cPage = (Page) super.clone();
			//deep clone
			if(tags != null){
				List<PageTag> cTags = new ArrayList<PageTag>();
				for (PageTag tag : tags) {
					cTags.add((PageTag) tag.clone());
				}
				cPage.setTags(cTags);
			}
			//deep clone
			if(content != null){
				cPage.setContent((PageContent) content.clone());
			}
			
			if(links != null){
				Set<PageLink> cLinks = new HashSet<PageLink>();
				for (PageLink link: links) {
					cLinks.add((PageLink) link.clone());
				}
				cPage.setLinks(cLinks);
			}
			if(pageProgress != null){
				cPage.setPageProgress((PageProgress) pageProgress.clone());
			}
	
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cPage;
	}
	


	/**
	 * Copy current page info to history.
	 * @return
	 */
	public History cloneToHistory() {
		History history = new History();

		history.setUid(null);
		history.setSpace(this.getSpace());
		history.setType(this.getType());
		history.setVersion(this.getVersion());
		history.setTitle(this.getTitle());
		history.setPageUuid(this.getPageUuid());
		history.setParent(this.getParent());
		history.setUnixName(this.getUnixName());
		history.setModifiedDate(this.getModifiedDate());
		history.setModifier(this.getModifier());
		history.setCreatedDate(this.getCreatedDate());
		history.setCreator(this.getCreator());
		
		HistoryContent content = new HistoryContent();
		if(this.getContent() != null){
			content.setContent(this.getContent().getContent());
			content.setUid(null);
		}
		history.setContent(content);
		
		return history;
	}
	/**
	 * Copy current page info to draft.
	 * @return
	 */
	public Draft cloneToDraft() {
		Draft draft = new Draft();
		
		draft.setUid(null);
		draft.setSpace(this.getSpace());
		draft.setType(this.getType());
		draft.setVersion(this.getVersion());
		draft.setTitle(this.getTitle());
		draft.setPageUuid(this.getPageUuid());
		draft.setParent(this.getParent());
		draft.setUnixName(this.getUnixName());
		draft.setModifiedDate(this.getModifiedDate());
		draft.setModifier(this.getModifier());
		draft.setCreatedDate(this.getCreatedDate());
		draft.setCreator(this.getCreator());
		
		DraftContent content = new DraftContent();
		if(this.getContent() != null){
			content.setContent(this.getContent().getContent());
			content.setUid(null);
		}
		draft.setContent(content);
		
		return draft;
	}

	public String getTagString() {
		StringBuffer sb = new StringBuffer();
		if (tags == null)
			return "";
		
		for (PageTag tag : tags) {
			sb.append(tag.getName()).append(", ");
		}
		String tagString = sb.toString().trim();
		if(tagString.endsWith(","))
			tagString = tagString.substring(0,tagString.length()-1);
		
		return tagString;
	}

	/**
	 * This method will reset <code>tags</code> which new tag instance. But the new instance does not include tag creator info etc,
	 *  which need be set by following code. 
	 * 
	 * @param tagString
	 */
	public void setTagString(String tagString) {
		tags = WikiUtil.parsePageTagString(tagString);
	}

	//********************************************************************
	//               set / get method
	//********************************************************************
	public Set<PageLink> getLinks() {
		return links;
	}

	public void setLinks(Set<PageLink> links) {
		this.links = links;
	}

	public PageProgress getPageProgress() {
		return pageProgress;
	}

	public void setPageProgress(PageProgress pageProgress) {
		this.pageProgress = pageProgress;
	}
	public List<PageTag> getTags() {
		return tags;
	}

	public void setTags(List<PageTag> tags) {
		this.tags = tags;
	}

	public boolean isRemoved() {
		return removed !=REMOVE_FLAG_NO?true:false;
	}
	public int getRemoved() {
		return removed;
	}

	public void setRemoved(int removed) {
		this.removed = removed;
	}
	/**
	 * @return
	 */
	public boolean containAttributes(int typeNewHome) {
		
		return false;
	}
	public PageContent getContent() {
		return content;
	}
	public void setContent(PageContent content) {
		this.content = content;
	}

	public int getNewPageType() {
		return newPageType;
	}
	
	public void setNewPageType(int newPageType) {
		this.newPageType = newPageType;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}



	public boolean isMenuUpdated() {
		return isMenuUpdated;
	}



	public void setMenuUpdated(boolean isMenuUpdated) {
		this.isMenuUpdated = isMenuUpdated;
	}
}
