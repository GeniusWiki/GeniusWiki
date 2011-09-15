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
package com.edgenius.wiki;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Dapeng.Ni
 */
@XStreamAlias("Skin")
public class Skin implements Serializable, Cloneable{
	private static final long serialVersionUID = -705936992720430677L;
	private static final Logger log = LoggerFactory.getLogger(Skin.class);
	public static final String DEFAULT_PREVIEW_IMAGE = "preview.png";
	public static final String DEFAULT_SKIN = "default";
	public static final int STATUS_CANDIDATE = 0;
	public static final int STATUS_DEPLOYED = 1;
	public static final int STATUS_APPLIED = 2;

	private float version=1;
	private String name;
	private String title;
	private String description;
	private String author;
	private Date updateDate;
	private String previewImageName = DEFAULT_PREVIEW_IMAGE;
	
	//only for DTO bring back to browser
	//Candidate(only uploaded zip)- 0, Deployed - 1, Applied - 2
	private transient int status;
	//identify if this skin is able to deleted
	private transient boolean removable;
	
	//these 2 varables read from view_layout.html and edit_layout.html
	private transient String viewLayout;
	private transient String editLayout;
	//********************************************************************
	//               some function methods
	//********************************************************************
	/**
	 * This method doesn't take care InputStream close.
	 */
	public static Skin fromXML(InputStream is){
		XStream xstream = new XStream();
		xstream.processAnnotations(Skin.class);
		return (Skin) xstream.fromXML(is);
		
	}
	/**
	 * @param file
	 * @throws FileNotFoundException 
	 */
	public void toFile(File file) throws FileNotFoundException {
		XStream xstream = new XStream();
		xstream.processAnnotations(Skin.class);
		FileOutputStream os;
		os = new FileOutputStream(file);
		xstream.toXML(this, os);
		IOUtils.closeQuietly(os);
	}
	public Object clone(){
		Skin cSkin = null;
		try {
			cSkin = (Skin) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cSkin;
	}
	
	public int hashCode(){
		return new HashCodeBuilder().append(version).append(name).hashCode();
	}
	public boolean equals(Object obj){
		if(!(obj instanceof Skin)){
			return false;
		}
		
		return new EqualsBuilder().append(version, ((Skin)obj).getVersion())
			.append(name, ((Skin)obj).getName()).isEquals();
	}

	//********************************************************************
	//               Set / Get
	//********************************************************************
	public float getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getAuthor() {
		return author;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public String getPreviewImageName() {
		return previewImageName;
	}

	public void setVersion(float version) {
		this.version = version;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public void setPreviewImageName(String previewImageName) {
		this.previewImageName = previewImageName;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public boolean isRemovable() {
		return removable;
	}
	public void setRemovable(boolean removable) {
		this.removable = removable;
	}
	public String getViewLayout() {
		return viewLayout;
	}
	public String getEditLayout() {
		return editLayout;
	}
	public void setViewLayout(String viewLayout) {
		this.viewLayout = viewLayout;
	}
	public void setEditLayout(String editLayout) {
		this.editLayout = editLayout;
	}

}
