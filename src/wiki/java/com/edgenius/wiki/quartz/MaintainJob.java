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
package com.edgenius.wiki.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Global;
import com.edgenius.wiki.search.service.IndexService;
import com.edgenius.wiki.service.ActivityLogService;
import com.edgenius.wiki.service.FriendService;
import com.edgenius.wiki.service.SitemapService;


/**
 * @author Dapeng.Ni
 */
public class MaintainJob  extends AuthenticatedQuartzJobBean {
	private static final Logger log = LoggerFactory.getLogger(MaintainJob.class);


	@Override
	public void executeInternal(JobExecutionContext context) throws JobExecutionException {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//  Purge old activity logs
		if(Global.PurgeDaysOldActivityLog > 0){
			try{
				ActivityLogService activityLog = (ActivityLogService) applicationContext.getBean(ActivityLogService.SERVICE_NAME);
				activityLog.purgeActivityLog(Global.PurgeDaysOldActivityLog); //days
			}catch (Exception e) {
				log.error("Unable to complete maintain job for purgeActivityLog",e);
			}
		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//  Remove expired invitations
		try{
			FriendService friendService = (FriendService) applicationContext.getBean(FriendService.SERVICE_NAME);
			friendService.removeExpiredInvitations(72); //hours
		}catch (Exception e) {
			log.error("Unable to complete maintain job for removeExpiredInvitations",e);
		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//  Optimise Search Index
		try{
			proxyLoginAsSystemAdmin();
			IndexService indexService = (IndexService) applicationContext.getBean(IndexService.SERVICE_NAME);
			indexService.optimize();
			log.info("Index optimize complete successfully.");
		}catch (Exception e) {
			log.error("Unable to complete maintain job for Index optimize",e);		
		}finally{
			logout();
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//  Sitemap generator
		if(Global.PublicSearchEngineAllow){
			try{
				SitemapService sitemapService = (SitemapService) applicationContext.getBean(SitemapService.SERVICE_NAME);
				sitemapService.createSitemap();
			}catch (Exception e) {
				log.error("Unable to complete sitemap generation job",e);
			}
		}
	}
	

}
