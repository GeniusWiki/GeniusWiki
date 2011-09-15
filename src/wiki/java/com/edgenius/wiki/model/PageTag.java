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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.model.TouchedInfo;


@Entity
@Table(name=Constants.TABLE_PREFIX+"PTAGS")
//@Indexed(index="indexes/tags")
//@Searchable
public class PageTag extends TouchedInfo implements Cloneable, Serializable{
	private static final long serialVersionUID = 4291998772108724883L;
	private static final transient Logger log = LoggerFactory.getLogger(PageTag.class);
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "key_seq")  
	@SequenceGenerator(name = "key_seq", sequenceName = Constants.TABLE_PREFIX+"PTAGS_SEQ")  
	@Column(name="PUID")
	private Integer uid;
	
//	@Field( index = Index.UN_TOKENIZED)
//	@SearchableProperty
	@Index(name="PTAG_NAME_INDEX")
	@Column(name="NAME")
	private String name;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SPACE_PUID")
	private Space space;
	
	@ManyToMany(targetEntity = Page.class, 
		cascade = { CascadeType.PERSIST,CascadeType.MERGE },mappedBy="tags")
	private List<Page> pages;
	
	//********************************************************************
	//               System method
	//********************************************************************
	/**
	 * all clone will set its Uid as null, to future saving.
	 */
	public Object clone(){
		PageTag cTag = null;
		try {
			cTag = (PageTag) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cTag;
	}
	public String toString(){
		return this.name;
	}
	public int hashCode(){
		return this.name != null? this.name.hashCode():0;
	}
	public boolean equals(Object obj){
		if(!(obj instanceof PageTag))
			return false;
		
		return StringUtils.equalsIgnoreCase(((PageTag)obj).name, this.name);
	}
	//********************************************************************
	//                       set /get 
	//********************************************************************
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getUid() {
		return uid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public List<Page> getPages() {
		return pages;
	}
	public void setPages(List<Page> pages) {
		this.pages = pages;
	}
	public Space getSpace() {
		return space;
	}
	public void setSpace(Space space) {
		this.space = space;
	}
}
