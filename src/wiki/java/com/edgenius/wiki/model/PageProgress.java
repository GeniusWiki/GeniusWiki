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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.model.User;
import com.edgenius.wiki.gwt.client.model.BlogPostMeta;
import com.thoughtworks.xstream.XStream;

/**
 * This class is intend to save information of page progress(progress engine). But it extends to save some extension 
 * information of page, for example, blog extra fields, in wordpress, it has excerpt, categories etc. 
 * @author Dapeng.Ni
 */
@Entity
@Table(name=Constants.TABLE_PREFIX+"PAGES_PROGRESS")
@org.hibernate.annotations.Table(appliesTo = Constants.TABLE_PREFIX+"PAGES_PROGRESS",
indexes = { @Index(name = "LINK_EXTRA_ID_INDEX", columnNames = {"LINK_EXTRA_ID"})})
public class PageProgress implements Serializable, Cloneable{
	private static final long serialVersionUID = -5497509020741369456L;
	private static transient final Logger log = LoggerFactory.getLogger(PageProgress.class);
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "key_seq")  
	@SequenceGenerator(name = "key_seq", sequenceName = Constants.TABLE_PREFIX+"PAGES_PROGRESS_SEQ")  
	@Column(name="PUID")
	private Integer uid;
	
	@ManyToOne
	@JoinColumn(name="MODIFIER_PUID")
	private User modifier;
	
	@Column(name="START_DATE")
	private Date startDate;
	@Column(name="FINISH_DATE")
	private Date finishDate;
	
	@Type(type="text")
	@Column(name="LINK_EXTRA_INFO",length=409600)
	private String linkExtInfo;
	
	//normally, it is postID - to separate this field from linkExtInfo is for quick query (this field is indexed) to get a page by postID.
	@Column(name="LINK_EXTRA_ID")
	private String linkExtID;

	/**
	 * Deep clone user object, all clone will set its Uid as null, to future saving.
	 */
	public Object clone(){
		PageProgress prog = null;
		try {
			prog = (PageProgress) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return prog;
	}
	/**
	 * @param key
	 * @return
	 */
	public BlogPostMeta getLinkPostExtInfo(String key) {
		List<BlogPostMeta> list = getLinkExtInfoObject();
		if(list != null){
			for(BlogPostMeta post: list){
				if(StringUtils.equals(post.getBlogKey(), key)){
					return post;
				}
			}
		}
		return null;
	}

	public void addLinkExtInfoObject(BlogPostMeta postMeta) {
		List<BlogPostMeta> list = getLinkExtInfoObject();
		if(list == null){
			list = new ArrayList<BlogPostMeta>();
			list.add(postMeta);
		}else{
			for (Iterator<BlogPostMeta> iter = list.iterator();iter.hasNext();) {
				BlogPostMeta post = iter.next();
				if(StringUtils.equals(post.getBlogKey(), postMeta.getBlogKey())){
					//update - remove first
					iter.remove();
					break;
				}
			}
			list.add(postMeta);
		}
		
	}
	/**
	 * A shortcut method to set linkExtInfo from BlogPostMeta
	 * @param postMeta
	 */
	public void setLinkExtInfoObject(List<BlogPostMeta> postMeta) {
		XStream xs = new XStream();
		xs.processAnnotations(BlogPostMeta.class);
		linkExtInfo = xs.toXML(postMeta);
	}
	/**
	 * A shortcut method to get linkExtInfo to BlogPostMeta
	 * @param linkExtInfo
	 */
	public List<BlogPostMeta> getLinkExtInfoObject() {
		if(StringUtils.isBlank(this.linkExtInfo))
			return null;
		try {
			XStream xs = new XStream();
			xs.processAnnotations(BlogPostMeta.class);
			return (List<BlogPostMeta>) xs.fromXML(this.linkExtInfo);
		} catch (Exception e) {
			log.error("Parse BlogPostMeta failed:" + this.linkExtInfo,e);
			return null;
		}
	}
	//********************************************************************
	//               Set / Get 
	//********************************************************************
	public Date getFinishDate() {
		return finishDate;
	}
	public void setFinishDate(Date finishDate) {
		this.finishDate = finishDate;
	}
	public User getModifier() {
		return modifier;
	}
	public void setModifier(User modifier) {
		this.modifier = modifier;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Integer getUid() {
		return uid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public String getLinkExtInfo() {
		return linkExtInfo;
	}
	public void setLinkExtInfo(String linkExtInfo) {
		this.linkExtInfo = linkExtInfo;
	}
	public String getLinkExtID() {
		return linkExtID;
	}
	public void setLinkExtID(String linkExtID) {
		this.linkExtID = linkExtID;
	}


}
