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

import org.jdom.Namespace;

import com.sun.syndication.feed.module.Module;

/**
 * @author Dapeng.Ni
 */
public interface PageRSSModule extends Module{
	String NS_PREFIX = "page";
	String URI = "http://geniuswiki.com/rss/1.0/modules/page/";
	Namespace NAMESPACE = Namespace.getNamespace(PageRSSModule.NS_PREFIX, PageRSSModule.URI);
	
	//~~~~~~~~~~~~~~~~~ Fields of Module
	String SPACEUNAME = "spaceUname";
	String PAGE_UUID = "pageUuid";
	String CHANNEL = "channel";
	
	String MODIFIER = "modifier";
	String CREATOR = "creator";
	String CREATE_DATE = "createDate";
	String MODIFIED_DATE = "modifiedDate";
	String VERSION = "version";
	
	public String getPageUuid();

	public void setPageUuid(String pageUuid);

	public String getSpaceUname() ;

	public void setSpaceUname(String spaceUname);

	public int getVersion();

	public void setVersion(int version);
	public String getCreator();
	public void setCreator(String creator) ;
	
	public String getModifier() ;
	public void setModifier(String modifier);
	
	public Date getCreateDate() ;
	public void setCreateDate(Date createDate);
	
	public Date getModifiedDate();
	public void setModifiedDate(Date modifiedDate) ;
}
