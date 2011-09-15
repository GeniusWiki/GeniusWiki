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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.wiki.gwt.client.model.LinkModel;


/**
 * @author Dapeng.Ni
 */

@Entity
@Table(name=Constants.TABLE_PREFIX+"PAGE_LINKS")
@org.hibernate.annotations.Table(appliesTo = Constants.TABLE_PREFIX+"PAGE_LINKS",
indexes = { @Index(name = "PAGE_LINK_INDEX", columnNames = {"SPACE_UNIX_NAME","LINK","LINK_TYPE"})})
public class PageLink implements Cloneable,Serializable{
	private static final long serialVersionUID = -6009761484937051123L;
	
	private static transient final Logger log = LoggerFactory.getLogger(PageLink.class);
	public static final int TYPE_INTERNAL = 0;
	//at moment, only internal link will saved into table, external link(ie, http:// etc) won't save 
	public static final int TYPE_EXTERNAL= 1;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "key_seq")  
	@SequenceGenerator(name = "key_seq", sequenceName = Constants.TABLE_PREFIX+"PAGE_LINKS_SEQ")  
	@Column(name="PUID")
	private Integer uid;
	
	//owner page
	@ManyToOne
	@JoinColumn(name="PAGE_PUID")
	private Page page;
	
	@Column(name="LINK_TYPE")
	private int type;
	
	//page title
	@Column(name="LINK")
	private String link;
	
	@Column(name="NEW_LINK", nullable=true)
	private String newLink;

	@Column(name="ANCHOR", nullable=true)
	private String anchor;
	
	
	@Column(name="SPACE_UNIX_NAME",nullable=true)
	private String spaceUname;
	
	//how many equals() PageLink(spaceUname,link,view,anchor)
	
	@Column(name="LINK_COUNT")
	private int amount;
	
	@Column(name="HIT")
	private int hit;

	//********************************************************************
	//               methods
	//********************************************************************
	/**
	 * Deep clone user object, all clone will set its Uid as null, to future saving.
	 */
	public Object clone(){
		PageLink cLink = null;
		try {
			cLink = (PageLink) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cLink;
	}
	public boolean equals(Object obj){
		if(!(obj instanceof PageLink))
			return false;
		PageLink ln = (PageLink) obj;
		
		return StringUtils.equalsIgnoreCase(ln.spaceUname, this.spaceUname)
			&& StringUtils.equalsIgnoreCase(ln.link, this.link)
			&& ln.type == this.type;
	}
	public int hashCode(){
		return new HashCodeBuilder().append(this.spaceUname).append(this.link).append(this.type).toHashCode();
	}
	/**
	 * @param object
	 * @return
	 */
	public static PageLink copyFrom(Page ownerPage, LinkModel model) {
		PageLink link = new PageLink();
		
		//hardcode, as we don't save external link so far
		link.type = TYPE_INTERNAL;
		link.link = model.getLink();
		link.anchor = model.getAnchor();
		link.spaceUname = model.getSpaceUname();
		link.page = ownerPage;
		//start for 1
		link.amount = 1;
		return link;
	}
	//********************************************************************
	//               Set / Get
	//********************************************************************

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getAnchor() {
		return anchor;
	}

	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}
	public String getSpaceUname() {
		return spaceUname;
	}
	public void setSpaceUname(String spaceUname) {
		this.spaceUname = spaceUname;
	}
	public int getHit() {
		return hit;
	}

	public void setHit(int hit) {
		this.hit = hit;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public String getNewLink() {
		return newLink;
	}
	public void setNewLink(String newLink) {
		this.newLink = newLink;
	}
	
}
