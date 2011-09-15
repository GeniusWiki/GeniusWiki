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

import java.io.IOException;

import javax.servlet.ServletOutputStream;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.model.User;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.rss.RSSService;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.util.WikiUtil;
import com.sun.syndication.io.FeedException;

/**
 * This Action will be authenticated by Basic Authentication method. 
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class RSSFeedAction extends BaseAction{
	private static final Logger log = LoggerFactory.getLogger(RSSFeedAction.class);
	
	private SpaceService spaceService;
	private RSSService rssService;
	//spaceUname
	private String s;
	//space Uid - while spaceUname has invalid characters for URL, it will use this as parameter
	//TODO: current, this is not implemented in client side as spaceUid is not passed to client side and 
	// spaceUid is not good value on URL as it may change after database backup/restore... 
	private String suid;
	public String execute(){
		Space space;
		if(NumberUtils.toInt(suid,-1) != -1){
			int spaceUid = NumberUtils.toInt(suid);
			space = spaceService.getSpace(spaceUid);
		}else{
			space = spaceService.getSpaceByUname(s);
		}
		if(space == null)
			return ERROR;
		
		User user = WikiUtil.getUser();
		String out = null;
		try {
			out = rssService.outputFeed(space.getUid(),space.getUnixName(),user);
		} catch (FeedException e) {
			log.error("Read feed error " , e);	
		}
		try{
			ServletOutputStream writer = getResponse().getOutputStream();
			if(out != null){
				//out must XML format
				getResponse().setContentType("text/xml");
				writer.write(out.getBytes(Constants.UTF8));
			}else{
				//feed does not exist for some reason, try to re-generate
				writer.write(("Please wait a while for RSS feed generating in system. Refresh later.").getBytes(Constants.UTF8));
				writer.flush();
				rssService.createFeed(space.getUnixName());
			}
		} catch (IOException e) {
			log.error("unable write out feed" , e);	
			return ERROR;
		}
		return null;
	}
	
	//********************************************************************
	//               set /get 
	//********************************************************************
	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public void setRssService(RSSService rssService) {
		this.rssService = rssService;
	}
	public String getS() {
		return s;
	}
	public void setSuid(String suid) {
		this.suid = suid;
	}

	public void setS(String s) {
		this.s = s;
	}
}
