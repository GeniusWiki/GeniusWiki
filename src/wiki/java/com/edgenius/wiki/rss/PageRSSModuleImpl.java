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
package com.edgenius.wiki.rss;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.module.ModuleImpl;

/**
 * @author Dapeng.Ni
 */
public class PageRSSModuleImpl extends ModuleImpl implements PageRSSModule{
	private static final long serialVersionUID = -3476635731890718898L;
	private static final Logger log = LoggerFactory.getLogger(PageRSSModuleImpl.class);
	
	private String spaceUname;
	private String pageUuid;
	private int version;
	private String creator;
	private String modifier;
	private Date createDate;
	private Date modifiedDate;
	
	public PageRSSModuleImpl() {
		super(PageRSSModule.class, URI);
	}

	public void copyFrom(Object obj) {
		if(!(obj instanceof PageRSSModule)){
			log.error("Unpexected object when copy from " + obj);
			return;
		}
		
		PageRSSModule mobj = (PageRSSModule)obj;
		this.setPageUuid(mobj.getPageUuid());
		this.setSpaceUname(mobj.getSpaceUname());
	}

	public Class<PageRSSModule> getInterface() {
		return PageRSSModule.class;
	}

	
	public String getPageUuid() {
		return pageUuid;
	}

	public void setPageUuid(String pageUuid) {
		this.pageUuid = pageUuid;
	}

	public String getSpaceUname() {
		return spaceUname;
	}

	public void setSpaceUname(String spaceUname) {
		this.spaceUname = spaceUname;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
}
