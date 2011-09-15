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

import java.util.Map;

import javax.jms.Queue;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.ActivityLogService;
import com.edgenius.wiki.service.NotifyMQObject;
import com.edgenius.wiki.service.SpaceException;
import com.edgenius.wiki.service.SpaceService;

/**
 * @author Dapeng.Ni
 */
public class RemoveSpaceJob extends AuthenticatedQuartzJobBean {
	private static final Logger log = LoggerFactory.getLogger(RemoveSpaceJob.class);


	@SuppressWarnings("unchecked")
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		Space space = null;
		try {
			String adminUsername = proxyLoginAsSystemAdmin();
			
			SpaceService spaceService = (SpaceService) applicationContext.getBean(SpaceService.SERVICE_NAME);
			ActivityLogService activityLog = (ActivityLogService) applicationContext.getBean(ActivityLogService.SERVICE_NAME);
			Map map = context.getJobDetail().getJobDataMap();
			String spaceUname = (String) map.get(WikiConstants.ATTR_SPACE_UNAME);
			log.info("Space remove Quartz job is invoked for " + spaceUname);
			
			space = spaceService.removeSpace(spaceUname, true);
			if(space != null){
				JmsTemplate jmsTemplate = (JmsTemplate) applicationContext.getBean("pageJmsTemplate");
				Queue notifyQueue = (Queue) applicationContext.getBean("notifyDestination");
				NotifyMQObject pnObj = new NotifyMQObject(adminUsername,space,0); 
				jmsTemplate.convertAndSend(notifyQueue, pnObj);
				log.info("Space remove Quartz job done for " + space.getUnixName());
			}
			
			activityLog.logSpaceRemoved(space, null, true);
		} catch (SpaceException e) {
			log.error("Remove space job can not finish ", e);
			throw new JobExecutionException("Remove space job can not finish ", e);
		} finally{
			logout();
		}
		
		
	}


}
