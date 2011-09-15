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
public class FileBean extends AbstractBean{
	private static final long serialVersionUID = -8645034113803629131L;
	
	private String uuid;
	private String version;
	//pageUuid(attachments) or username(portrait) etc.
	private String identifier;
	
	private String contentType;
	private String filename;
	private String description;
	private long size;
	
	//********************************************************************
	//               Function method
	//********************************************************************
	public String toString(){
		return identifier+ " : " + uuid;
	}
	public int hashCode(){
		return uuid != null ?uuid.hashCode(): 0; 
	}
	public boolean equals(Object obj){
		if(!(obj instanceof FileBean))
			return false;
		
		return uuid != null ?uuid.equals(((FileBean)obj).uuid): false; 
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
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}

	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
}
