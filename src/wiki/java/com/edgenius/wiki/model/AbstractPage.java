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

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.model.SensitiveTouchedInfo;
import com.edgenius.core.repository.FileNode;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;

/**
 * Draft should be saved frequently because page auto-save function. For performance reason, 
 * split Page(with history) and draft into 2 tables.
 *  
 * @since version 0.3 
 * @author Dapeng.Ni
 */

@MappedSuperclass
public abstract class AbstractPage extends SensitiveTouchedInfo implements Cloneable,Serializable{
	public static enum PAGE_TYPE{PAGE, DRAFT, HISTORY}; 
	
	@Transient
	protected transient final Logger log = LoggerFactory.getLogger(this.getClass());

	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SPACE_PUID",nullable=false)
	private Space space;
	
	//0: is current page or history page,  draft page:Manual(1) or Auto draft(2).
	@Column(name="P_TYPE")
	private int type;
	
	@Column(name="VERSION")
	private int version;
	
	@Column(name="TITLE")
	private String title;
	
	@Column(name="TREE_LEVEL")
	private int level;
	
	@Column(name="PAGE_UUID")
	private String pageUuid;
	
	@ManyToOne
	@JoinColumn(name="PARENT_PAGE_PUID",nullable=true)
	private Page parent;
	
	@Column(name="UNIX_NAME")
	private String unixName;

	//*******************************************************
	//DAO fields: just for display usage
	//this fields hold page render content, which contains conjunct String and Links (PageLink)
	@Transient
	private List<RenderPiece> renderPieces;
	@Transient
	private List<RenderPiece> sidebarRenderPieces;
	@Transient
	private List<RenderPiece> spaceMenuPieces;

	@Transient
	private List<FileNode> attachments;
	
	@Transient
	private List<AbstractPage> ancestorList;
	
	//identify this page possible operations(read, write, remove etc.) conjunct with a user.
	@Transient
	private List<WikiOPERATIONS> wikiOperations;
	
	//full info like: Added by Dapeng, last edited by Owen on 17:20 Apr. 20, 2005 
	@Transient
	private String authorInfo;

	//refrence from com.edgenius.wiki.gwt.client.PageAttribute, not necessary save to Database.
	@Transient
	private int attribute;
	@Transient
	private String[] visibleAttachmentNodeList;

	//use for phase render.... Don't reset Content is because it maybe dangerous for PO. It also indicate this render content doesn't apply space theme.
	@Transient
	private String phaseContent = null;

	//********************************************************************
	//               Function methods
	//********************************************************************
	public Object clone() throws CloneNotSupportedException{
		return super.clone();
	}
	
	public boolean equals(Object obj){
		if(obj instanceof AbstractPage){
			AbstractPage p = (AbstractPage) obj;
			return StringUtils.equals(p.getPageUuid(),this.pageUuid);
		}
		return false;
	}
	public int hashCode(){
		return new HashCodeBuilder().append(this.pageUuid).toHashCode();
	}
	public String toString(){
		return "Page on space " + (space != null? space.getUnixName():"NONE") + ", Title " + title + ", Uuid " + pageUuid; 
	}
	/**
	 * This method only copy necessary values to a new page instance for pageTree display use.
	 * @return
	 */
	public AbstractPage getTreeItem() {
		AbstractPage item = new Page();
		item.setUid(this.getUid());
		item.setTitle(this.getTitle());
		item.setParent(this.getParent());
		return item;
	}

	//********************************************************************
	//               Set / Get methods
	//********************************************************************
	public abstract void setUid(Integer uid);
	public abstract Integer getUid();
	
	public List<FileNode> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<FileNode> attachments) {
		this.attachments = attachments;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}


	public Space getSpace() {
		return space;
	}

	public void setSpace(Space space) {
		this.space = space;
	}

	public String getPageUuid() {
		return pageUuid;
	}

	public void setPageUuid(String pageUuid) {
		this.pageUuid = pageUuid;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getUnixName() {
		return unixName;
	}

	public void setUnixName(String unixName) {
		this.unixName = unixName;
	}


	public Page getParent() {
		return parent;
	}
	public void setParent(Page parentPage) {
		this.parent = parentPage;
	}


	public List<RenderPiece> getRenderPieces() {
		return renderPieces;
	}


	public List<RenderPiece> getSidebarRenderPieces() {
		return sidebarRenderPieces;
	}

	public void setSidebarRenderPieces(List<RenderPiece> sidebarRenderPieces) {
		this.sidebarRenderPieces = sidebarRenderPieces;
	}

	public void setRenderPieces(List<RenderPiece> renderPieces) {
		this.renderPieces = renderPieces;
	}


	public int getLevel() {
		return level;
	}


	public void setLevel(int level) {
		this.level = level;
	}

	public List<AbstractPage> getAncestorList() {
		return ancestorList;
	}

	public String[] getVisibleAttachmentNodeList() {
		return visibleAttachmentNodeList;
	}

	public void setVisibleAttachmentNodeList(String[] visibleAttachmentNodeList) {
		this.visibleAttachmentNodeList = visibleAttachmentNodeList;
	}

	public void setAncestorList(List<AbstractPage> ancestorList) {
		this.ancestorList = ancestorList;
	}

	public List<WikiOPERATIONS> getWikiOperations() {
		return wikiOperations;
	}

	public void setWikiOperations(List<WikiOPERATIONS> wikiOperations) {
		this.wikiOperations = wikiOperations;
	}

	public String getAuthorInfo() {
		return authorInfo;
	}

	public void setAuthorInfo(String authorInfo) {
		this.authorInfo = authorInfo;
	}
	public int getAttribute() {
		return attribute;
	}
	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	/**
	 * @return the phaseContent
	 */
	public String getPhaseContent() {
		return phaseContent;
	}

	/**
	 * @param phaseContent the phaseContent to set
	 */
	public void setPhaseContent(String phaseContent) {
		this.phaseContent = phaseContent;
	}

	public List<RenderPiece> getSpaceMenuPieces() {
		return spaceMenuPieces;
	}

	public void setSpaceMenuPieces(List<RenderPiece> spaceMenuPieces) {
		this.spaceMenuPieces = spaceMenuPieces;
	}


}
