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
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.Global;
import com.edgenius.core.model.Configuration;
import com.edgenius.core.model.SensitiveTouchedInfo;
import com.edgenius.wiki.PageTheme;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.gwt.client.model.BlogCategory;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;
import com.edgenius.wiki.util.WikiUtil;
import com.thoughtworks.xstream.XStream;

@Entity
@Table(name=Constants.TABLE_PREFIX+"SPACES")
public class Space extends SensitiveTouchedInfo implements Cloneable,Serializable{
	private static final long serialVersionUID = -7655443898524654754L;

	private static final transient Logger log = LoggerFactory.getLogger(Space.class);

	//Below options can be combination to string with separator "|"
	public static final int SORT_BY_SPACEKEY = SharedConstants.SORT_BY_SPACEKEY;
	public static final int SORT_BY_SPACE_TITLE = SharedConstants.SORT_BY_SPACE_TITLE;
	public static final int SORT_BY_PAGE_COUNT = SharedConstants.SORT_BY_PAGE_COUNT;
	public static final int SORT_BY_PAGE_SCORE = SharedConstants.SORT_BY_PAGE_SCORE;
	public static final int SORT_BY_CREATEBY = SharedConstants.SORT_BY_CREATEBY;
	public static final int SORT_BY_CREATEON = SharedConstants.SORT_BY_CREATEON;

	//external link type. So far support BLOG and Shell
	//Don't use bitwise format, as this field needs to be good for SQL query. I am not sure if all databases support bit function query.
	//So, there is "ALL" format for combination.
	public static final int EXT_LINK_BLOG = 1;
	//NOTE: link to shell is disable model. If a space created, its default value "0" means space is able to deploy to shell if system level shell service is enabled.
	//The good is, if system shell service is disabled at beginning, and no need to reset space level option, all non-private space will 
	//deploy to shell once system function is turned on again. Only space admin manually turns off this option, space will stop deploy to shell.
	public static final int EXT_LINK_SHELL_DISABLED = 2;
	public static final int EXT_LINK_ALL = 10;
	
	//private space
	public static final int TYPE_PREIVATE = SharedConstants.PRIVATE_SPACE;
	//system initial system name, which will hold Search Result, User Profile file page etc. which they are not belong to any space
	public static final String SYSTEM_SPACEUNAME = SharedConstants.SYSTEM_SPACEUNAME;

	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "key_seq")  
	@SequenceGenerator(name = "key_seq", sequenceName = Constants.TABLE_PREFIX+"SPACES_SEQ")  
	@Column(name="PUID")
	private Integer uid;
	
	//public - 0, private space -1
	@Column(name="S_TYPE")
	private short type;
	
	@Column(name="DESCRIPTION")
	private String description;
	
	@Column(name="NAME", unique=true)
	private String name;
	
	@Column(name="UNIX_NAME",unique=true)
	private String unixName;
	
//	@ManyToOne(cascade={CascadeType.ALL},fetch=FetchType.EAGER)
//	@JoinColumn(name="cr_workspace_uid")
//	private CrWorkspace crWorkspace;
	
	@ManyToOne(cascade={CascadeType.ALL})
	@JoinColumn(name="CONFIGURATION_PUID")
	private Configuration configuration;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="HOMEPAGE_PUID")
	private Page homepage;

	
	@ManyToMany(targetEntity = SpaceTag.class, cascade = { CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH }
	, fetch = FetchType.LAZY)
	@JoinTable(name = Constants.TABLE_PREFIX+"SPACES_TAGS", 
	joinColumns = { @JoinColumn(name = "SPACE_PUID")},
	inverseJoinColumns={ @JoinColumn(name = "TAG_PUID") })
	private List<SpaceTag> tags;
	
	@Column(name="SCORE")
	private int score;
	@Column(name="REMOVED")
	private boolean removed;

	//logo image NODE UUID
	@Column(nullable=true, name="LOGO_LARGE")
	private String logoLarge;
	
	@Column(nullable=true, name="LOGO_SMALL")
	private String logoSmall;
	
	//identify if this space is linked with external blogs etc. For BlogSyncService query.
	//Or linked by Shell, for shell notification
	//@see Space.EXT_LINK_*;  Default is 0 and means no external link.
	@Column(name="EXT_LINK_TYPE")
	private int extLinkType;
	//********************************************************************
	// DTO Object
	@Transient
	private SpaceSetting setting;
	//identify this space possible operations(read, write, remove etc.) conjunct with a user.
	@Transient
	private List<WikiOPERATIONS> wikiOperations;

	@Transient
	private Set<String> adminMailList;
	//********************************************************************
	//               Functional methods
	//********************************************************************
	public boolean containExtLinkType(int extLink){
		if(this.extLinkType  == EXT_LINK_ALL)
			return true;
		
		return this.extLinkType == extLink;
	}
	public void removeExtLinkType(int extLink) {
		if(this.extLinkType == extLink){
			this.extLinkType = 0;
		}else if(this.extLinkType == Space.EXT_LINK_ALL){
			if(extLink == Space.EXT_LINK_BLOG){
				//if remove blog, and its original value is all, then left SHELL_DISABLED
				this.extLinkType = Space.EXT_LINK_SHELL_DISABLED;
			}else if(extLink == Space.EXT_LINK_SHELL_DISABLED){
				//if remove shell_disabled, and its original value is all, then left LINK_BLOG
				this.extLinkType = Space.EXT_LINK_BLOG;
			}
				
		}
	}
	public void addExtLinkType(int extLink) {
		if(this.extLinkType == 0){
			this.extLinkType = extLink;
		}else if(this.extLinkType != extLink){
			this.extLinkType = Space.EXT_LINK_ALL;
		}
	}

	public Object clone(){
		Space cSpace = null;
		try {
			cSpace = (Space) super.clone();
			//deep clone
			if(homepage != null){
				cSpace.setHomepage((Page) homepage.clone());
			}
//			if(crWorkspace != null)
//				cSpace.setCrWorkspace((CrWorkspace) crWorkspace.clone());
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cSpace;
	}

	public boolean equals(Object obj){
		if(!(obj instanceof Space))
			return false;
		Space s = (Space) obj;
		
		return new EqualsBuilder().append(this.unixName,s.unixName).isEquals();
	}
	public int hashCode(){
		return new HashCodeBuilder().append(this.unixName).toHashCode();
	}
	public boolean isResource(String resource){
		return StringUtils.equalsIgnoreCase(unixName,resource);
	}
	/**
	 * This method will reset <code>tags</code> which new tag instance. But the new instance does not include tag creator info etc,
	 *  which need be set by following code. 
	 * 
	 * @param tagString
	 */
	public void setTagString(String tagString) {
		tags = WikiUtil.parseSpaceTagString(tagString);
	}
	public String getTagString() {
		StringBuffer sb = new StringBuffer();
		if (tags == null)
			return "";
		
		for (SpaceTag tag : tags) {
			sb.append(tag.getName()).append(", ");
		}
		String tagString = sb.toString().trim();
		if(tagString.endsWith(","))
			tagString = tagString.substring(0,tagString.length()-1);
		
		return tagString;
	}
	//********************************************************************
	//               Get / Set
	//********************************************************************
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

//	public CrWorkspace getCrWorkspace() {
//		return crWorkspace;
//	}
//	public void setCrWorkspace(CrWorkspace crWorkspace) {
//		this.crWorkspace = crWorkspace;
//	}
	public Integer getUid() {
		return uid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public String getUnixName() {
		return unixName;
	}
	public void setUnixName(String uuid) {
		this.unixName = uuid;
	}
	public Configuration getConfiguration() {
		return configuration;
	}
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	//Transient, only has get() method
	public SpaceSetting getSetting() {
		//singleton to improve performance: note the user cache need reset once user change setting.
		if(setting != null)
			return setting;
		
		if(configuration != null && configuration.getValue() != null){
			XStream xstream = new XStream();
			xstream.processAnnotations(PageTheme.class);
			xstream.processAnnotations(BlogMeta.class);
			xstream.processAnnotations(BlogCategory.class);
			setting = (SpaceSetting) xstream.fromXML(configuration.getValue());
			//normally it not necessary do this, but as the official site run long time and this is new value...(24/09/2008)
			if(setting.getCommentNotifyType() == 0){
				setting.setCommentNotifyType(SpaceSetting.COMMENT_NOTIFY_FEQ_EVERY_POST | SpaceSetting.COMMENT_NOTIFY_TO_ALL_CONTRIBUTOR);
			}
			if(setting.getCommentNotifyMaxPerDay() == 0){
				setting.setCommentNotifyMaxPerDay(Global.MaxCommentsNotifyPerDay);
			}

		}
		if(setting == null){
			//return default value
			setting = new SpaceSetting();
		}
			
		return setting;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public Page getHomepage() {
		return homepage;
	}

	public void setHomepage(Page homepage) {
		this.homepage = homepage;
	}

	public boolean isRemoved() {
		return removed;
	}

	public void setRemoved(boolean removed) {
		this.removed = removed;
	}

	public void setSetting(SpaceSetting setting) {
		this.setting = setting;
	}

	public List<WikiOPERATIONS> getWikiOperations() {
		return wikiOperations;
	}

	public void setWikiOperations(List<WikiOPERATIONS> wikiOperations) {
		this.wikiOperations = wikiOperations;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public List<SpaceTag> getTags() {
		return tags;
	}

	public void setTags(List<SpaceTag> tags) {
		this.tags = tags;
	}

	public boolean isPrivate(){
		return (type == TYPE_PREIVATE)?true:false;
			
	}


	public Set<String> getAdminMailList() {
		return adminMailList;
	}

	public void setAdminMailList(Set<String> adminMailList) {
		this.adminMailList = adminMailList;
	}

	public String getLogoLarge() {
		return logoLarge;
	}

	public void setLogoLarge(String logoLarge) {
		this.logoLarge = logoLarge;
	}

	public String getLogoSmall() {
		return logoSmall;
	}

	public void setLogoSmall(String logoSmall) {
		this.logoSmall = logoSmall;
	}

	/**
	 * 
	 * @deprecated Please use containExtLinkType(int) to replace this method.
	 * @return
	 */
	public int getExtLinkType() {
		return extLinkType;
	}

	/**
	 *  @deprecated Please use addExtLinkType() or removeExtLinkType() to replace this method.
	 * @param linkExt
	 */
	public void setExtLinkType(int linkExt) {
		this.extLinkType = linkExt;
	}


}
