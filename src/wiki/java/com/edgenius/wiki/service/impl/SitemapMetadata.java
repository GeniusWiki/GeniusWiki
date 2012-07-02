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
package com.edgenius.wiki.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

//********************************************************************
//               Private class
//********************************************************************
@XStreamAlias("SitemapMetadata")
class SitemapMetadata{

	private static final String SEP = "|";
	
	private Date modifiedDate;
	//Save the page into which sitemap. Key: pageUuid, Value: sitemapIndex
	private LinkedHashMap<String, String> pageMap = new LinkedHashMap<String, String>();
	
	public SitemapMetadata(){};
	
	public void addPageMap(String pageUuid, String sitemapIndex, String pageurl){
		pageMap.put(pageUuid, sitemapIndex + SEP + pageurl);
	}
	
	/**
	 * @param pageUuid
	 * @return
	 */
	public String getPageUrl(String pageUuid) {
		String value = pageMap.get(pageUuid);
		String[] str = StringUtils.split(value, SEP);
		if(str != null && str.length == 2){
			return str[1];
		}
		return null;
	}

	/**
	 * @param pageUuid
	 * @return
	 */
	public String getSitemapIndex(String pageUuid) {
		String value = pageMap.get(pageUuid);
		String[] str = StringUtils.split(value, SEP);
		if(str != null && str.length == 2){
			return str[0];
		}
		return null;
	}

	public void removePageMap(String pageUuid){
		pageMap.remove(pageUuid);
	}
	
	public void save(File root) {
		
		File sitemapIndexFile = new File(root,"sitemap-metadata.xml");
		FileOutputStream fos = null;
		try {
			
			modifiedDate = new Date();
			
			fos = new FileOutputStream(sitemapIndexFile);
			XStream xs = new XStream();
			xs.processAnnotations(SitemapMetadata.class);
			xs.toXML(this, fos);
		} catch (FileNotFoundException e) {
			SitemapServiceImpl.log.error("Unable to write sitemap-metadata.xml",e);
		} finally{
			IOUtils.closeQuietly(fos);
		}
		
	}
	
	public static SitemapMetadata load(File root){
		
		File sitemapIndexFile = new File(root,"sitemap-metadata.xml");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(sitemapIndexFile);
			XStream xs = new XStream();
			xs.processAnnotations(SitemapMetadata.class);
			SitemapMetadata meta = (SitemapMetadata) xs.fromXML(fis);
			return meta;
		} catch (FileNotFoundException e) {
			SitemapServiceImpl.log.warn("Sitemap-metadata.xml not found");
		} finally{
			IOUtils.closeQuietly(fis);
		}
		return new SitemapMetadata(); 
	}
	//********************************************************************
	//  Set / Get
	//********************************************************************
	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public LinkedHashMap<String, String> getPageMap() {
		return pageMap;
	}

	public void setPageMap(LinkedHashMap<String, String> pageMap) {
		this.pageMap = pageMap;
	}
}
