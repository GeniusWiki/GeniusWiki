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
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractContent implements Cloneable,Serializable{
	private static transient final Logger log = LoggerFactory.getLogger(AbstractContent.class);

	@Column(name="CONTENT_TYPE")
	private int type;

	@Type(type="text")
	@Column(name="CONTENT", length=1073741824) //1G
	private String content ="";

	//********************************************************************
	//               Clone method
	//********************************************************************
	public Object clone(){
		AbstractContent cPageContent = null;
		try {
			cPageContent = (AbstractContent) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cPageContent;
	}
	//********************************************************************
	//               Set / Get
	//********************************************************************
	
	/**
	 * @param object
	 */
	public abstract void setUid(Integer uid);

	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

	public String getContent() {
		return content == null?"":content;
	}

	public void setContent(String content) {
		this.content = content;
	}


}
