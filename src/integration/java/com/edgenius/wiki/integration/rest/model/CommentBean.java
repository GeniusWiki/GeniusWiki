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

/**
 * @author Dapeng.Ni
 */
public class CommentBean extends AbstractBean {
	private static final long serialVersionUID = -1183619309851360610L;

	//It is just Comment.PUID
	private String uuid;
	private String parentUuid;
	private int level;
	private String content;
	
	//Response value: only can ensure pageUUID is not null.
	private PageBean page;
	//********************************************************************
	//               Function method
	//********************************************************************
	public String toString(){
		return (page!= null?page.getTitle():"NULL") + " : " + uuid;
	}
	public int hashCode(){
		return uuid != null ?uuid.hashCode(): 0; 
	}
	public boolean equals(Object obj){
		if(!(obj instanceof CommentBean))
			return false;
		
		return uuid != null ?uuid.equals(((CommentBean)obj).uuid): false; 
	}	
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getParentUuid() {
		return parentUuid;
	}
	public void setParentUuid(String parentUuid) {
		this.parentUuid = parentUuid;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public PageBean getPage() {
		return page;
	}
	public void setPage(PageBean page) {
		this.page = page;
	}
}
