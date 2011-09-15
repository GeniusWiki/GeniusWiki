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
package com.edgenius.wiki.integration.rest.model;

import java.util.List;


/**
 * @author Dapeng.Ni
 */
public class PageBean extends AbstractBean{
	private static final long serialVersionUID = -8489895471002370612L;

	private String uuid;
	private String title;
	private String content;
	
	private String spaceUname;
	
	private List<TagBean> tags;
	
	private List<CommentBean> comments;
	
	private List<FileBean> attachments;
	//********************************************************************
	//               Function method
	//********************************************************************
	public String toString(){
		return spaceUname + " : " + title;
	}
	public int hashCode(){
		return uuid != null ?uuid.hashCode(): 0; 
	}
	public boolean equals(Object obj){
		if(!(obj instanceof PageBean))
			return false;
		
		return uuid != null ?uuid.equals(((PageBean)obj).uuid): false; 
	}
	
	//********************************************************************
	//               set / get
	//********************************************************************

	public String getUuid() {
		return uuid;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}


	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<TagBean> getTags() {
		return tags;
	}

	public void setTags(List<TagBean> tags) {
		this.tags = tags;
	}

	public List<CommentBean> getComments() {
		return comments;
	}

	public void setComments(List<CommentBean> comments) {
		this.comments = comments;
	}
	public List<FileBean> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<FileBean> attachments) {
		this.attachments = attachments;
	}
	public String getSpaceUname() {
		return spaceUname;
	}
	public void setSpaceUname(String spaceUname) {
		this.spaceUname = spaceUname;
	}


	
}
