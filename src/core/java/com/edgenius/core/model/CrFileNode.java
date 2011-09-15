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
package com.edgenius.core.model;

import static com.edgenius.core.Constants.TABLE_PREFIX;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;


/**
 * @author Dapeng.Ni
 */
@Entity
@Table(name=TABLE_PREFIX+"CR_FILENODES")
public class CrFileNode implements Cloneable, Serializable{
	private static final long serialVersionUID = 1830806129384694239L;

	private static final transient Logger log = LoggerFactory.getLogger(CrFileNode.class);
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator="key_seq")
	@SequenceGenerator(name="key_seq", sequenceName=Constants.TABLE_PREFIX+"CR_FILENODES_SEQ")
	@Column(name="PUID")
	private Integer uid;
	
	//Repository.TYPE_*, except page, it also could be User(for portrait in default space) or Space(for logo for each space)
	@Column(name="NODE_TYPE")
	private String nodeType;
	
	//???Because rebuild attachment index need spaceUname, although it is possible to get spaceUname by identifierUuid,
	//but it is very low performance, so just add new fields here
	
	@Column(name="SPACE_UNAME")
	private String spaceUname;
	
	//basically, it is page uuid
	@Column(name="IDENTIFIER_UUID")
	@Index(name="CRNODE_IDENTIFIER_INDEX")
	private String identifierUuid;
	
	@Column(name="NODE_UUID")
	@Index(name="CRNODE_NODE_UUID_INDEX")
	private String nodeUuid;
	
	
	@Column(name="FILENAME")
	private String filename;
	
	@Column(name="DESCRIPTION")
	private String comment;
	
	@Column(name="VERSION")
	private int version;
	
	@Column(name="CONTENT_TYPE")
	private String contentType;
	
	@Column(name="CREATOR_NAME")
	private String creator;
	
	@Column(name="FILE_SIZE")
	private long size;
	
	@Column(name="MODIFIED_DATE")
	private Date modifiedDate;
	
	//file node text encoding, such as WORD, PDF file's text encoding
	@Column(name="ENCODING")
	private String encoding;
	
	//Optional MD5 digest of file
	@Column(name="MD5_DIGEST", length=64, nullable=true)
	private String md5Digest;
	
	//if this node is shared, then may have more than one node refer to it. The file of reference node will point to this node's physical file. 
	@Column(name="SHARED")
	private boolean shared;
	//if this is not null means this node is refer to a shared node, and this node won't have physical file exist. 
	@Column(name="REFERENCE_NODE_UUID")
	private String referenceNodeUuid;
	
	@Column(name="STATUS")
	private int status;
	//********************************************************************
	//               function method
	//********************************************************************
	public Object clone(){
		CrFileNode node = null;
		try {
			node = (CrFileNode) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed ", e);
		}
		return node;
	}
	//********************************************************************
	//               set / get 
	//********************************************************************
	public Integer getUid() {
		return uid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public String getNodeType() {
		return nodeType;
	}
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	public String getIdentifierUuid() {
		return identifierUuid;
	}
	public void setIdentifierUuid(String identifierUuid) {
		this.identifierUuid = identifierUuid;
	}
	public String getNodeUuid() {
		return nodeUuid;
	}
	public void setNodeUuid(String nodeUuid) {
		this.nodeUuid = nodeUuid;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}

	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}

	public boolean isShared() {
		return shared;
	}
	public void setShared(boolean shared) {
		this.shared = shared;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public String getReferenceNodeUuid() {
		return referenceNodeUuid;
	}
	public void setReferenceNodeUuid(String referenceNodeUuid) {
		this.referenceNodeUuid = referenceNodeUuid;
	}
	public String getSpaceUname() {
		return spaceUname;
	}
	public void setSpaceUname(String spaceUname) {
		this.spaceUname = spaceUname;
	}
	public String getMd5Digest() {
		return md5Digest;
	}
	public void setMd5Digest(String md5Digest) {
		this.md5Digest = md5Digest;
	}
}
