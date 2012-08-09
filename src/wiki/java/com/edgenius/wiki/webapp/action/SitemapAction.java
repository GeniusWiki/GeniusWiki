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
package com.edgenius.wiki.webapp.action;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.edgenius.core.Global;
import com.edgenius.wiki.service.SitemapService;

/**
 * Download robots.txt and sitemap xml or gz files.
 * @author Dapeng.Ni
 */
public class SitemapAction extends BaseAction{
	private static final long serialVersionUID = -1391271310386121882L;
	private static final Logger log = LoggerFactory.getLogger(SitemapAction.class);
	
	private String file;
	@Autowired private SitemapService sitemapService;
	
	public String execute(){
	    if(!Global.PublicSearchEngineAllow){
	        sendHttpResponseCode(404);
	    }
		try {
			File  sitemapFile = sitemapService.getSitemapFile(file);
			downloadFile(file, sitemapFile);
		} catch (IOException e) {
			log.warn("Sitemap file {} not found", file);
			sendHttpResponseCode(404);
		}
		
		return null;
	}

	
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}

}
