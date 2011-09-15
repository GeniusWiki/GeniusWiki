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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.util.AuditLogger;

/**
 * @author Dapeng.Ni
 */
public class RSSMQConsumer {
	private static final Logger log = LoggerFactory.getLogger(RSSMQConsumer.class);
	
	private RSSService rssService;
	public void handleMessage(Object msg){
		RSSMQObject mqObj;
		log.info("RSS messsage recieved.");
		if(msg instanceof RSSMQObject){
			mqObj = (RSSMQObject) msg;
		}else{
			AuditLogger.error("Unexpected object in RSS Counsumer " + msg);
			return;
		}
		if(mqObj.getType() == RSSMQObject.TYPE_REBUILD){
			//build RSS feed
			log.info("RSS CREATE messsage recieved.");
			rssService.createFeed(mqObj.getSpaceUname());
		}else if(mqObj.getType() == RSSMQObject.TYPE_DELETE){
			//delete RSS feed
			log.info("RSS REMOVE messsage recieved.");
			rssService.removeFeed(mqObj.getSpaceUid());
		}else if(mqObj.getType() == RSSMQObject.TYPE_ITEM_REMOVE){
			//delete page item from RSS feed
			log.info("RSS REMOVE FEED ITEM messsage recieved.");
			rssService.removeFeedItem(mqObj.getSpaceUname(),mqObj.getRemovePageUuid());
		}
	}
	
	public void setRssService(RSSService rssService) {
		this.rssService = rssService;
	}
		
}
