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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import com.edgenius.wiki.service.PageService;

/**
 * @author Dapeng.Ni
 */
public class SitemapServiceImpl implements InitializingBean {

	private Resource indexLocation;
	@Autowired private PageService pageService;
	
	
	File sitemapFile;
	public void createSiteMap() throws IOException{
		// <url>
		//  <loc>http://example.com/</loc>
        // 	<lastmod>2006-11-18</lastmod>
		// </url>
		
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if(!indexLocation.exists() && !indexLocation.getFile().mkdirs()){
			throw new BeanInitializationException("Failed creating public sitemap location.");
		}
		
		sitemapFile = new File(indexLocation.getFile(), "sitemap.xml");
		if (!sitemapFile.exists()){
			//create empty sitemap
			List<String> lines = new ArrayList<String>();
			lines.add("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			lines.add("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
			lines.add("</urlset>");
			FileUtils.writeLines(sitemapFile, lines);
			
		}
		
		//copy existed sitemap file to explode directory
		
		
	}
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public void setIndexLocation(Resource indexLocation) {
		this.indexLocation = indexLocation;
	}

}
