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

import java.text.ParseException;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Global;

/**
 * @author Dapeng.Ni
 */
public class PageCommentNotifyJobInvoker {
	private static final Logger log = LoggerFactory.getLogger(PageCommentNotifyJobInvoker.class);
	private static final String JOB_NAME = "PageCommentNotify-QuartzJob";
	private static final String TRIGGER_NAME =  "PageCommentNotify-Trigger";
	
	private JobDetail commentNotifyJob;
	private Scheduler scheduler;
	private TriggerKey triggerKey;

	public PageCommentNotifyJobInvoker() {
		commentNotifyJob = JobBuilder.newJob(PageCommentNotifyJob.class)
				 .withIdentity(JOB_NAME, Scheduler.DEFAULT_GROUP)
				 .withDescription(JOB_NAME)
				 .storeDurably()
				 .requestRecovery()
				 .build();
		
		triggerKey = new TriggerKey(TRIGGER_NAME,Scheduler.DEFAULT_GROUP);
	}
	public void invokeJob() throws QuartzException{

		
		// start the scheduling job
		try{
			//check if this trigger already exist, if so, need cancel it then recreate
			cancelJob();
			
			CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
					.withSchedule(CronScheduleBuilder.cronSchedule(Global.CommentsNotifierCron)).build(); 
			scheduler.scheduleJob(commentNotifyJob, trigger);
			
			log.info("PageCommentNotify is scheduled in " + Global.CommentsNotifierCron);
		}catch (SchedulerException e){
			log.error("Error occurred at [PageCommentNotify Schedule]- fail to start scheduling:" , e);
			throw new QuartzException("Error occurred at [PageCommentNotify Schedule]- fail to start scheduling",e);
		} catch (ParseException e) {
			log.error("Failed parse cron string: " + Global.CommentsNotifierCron,e);
			throw new QuartzException("Failed parse cron string: " + Global.CommentsNotifierCron,e);
		}
	}
	
	public void cancelJob() throws QuartzException{
		try {
			if(scheduler.checkExists(triggerKey)){
				scheduler.unscheduleJob(triggerKey);
				log.info("PageCommentNotify job trigger is cancelled." );
			}
			
			JobKey jobKey = new JobKey(JOB_NAME, Scheduler.DEFAULT_GROUP);
			if(scheduler.checkExists(jobKey)){
				scheduler.deleteJob(jobKey);
				log.info("PageCommentNotify job is removed." );
			}
		} catch (SchedulerException e) {
			log.error("Error occurred at [PageCommentNotify Schedule]- fail to cancel scheduling:" , e);
			throw new QuartzException("Error occurred at [PageCommentNotify Schedule]- fail to cancel scheduling",e);
		}
	}
	
	//********************************************************************
	//               set /get 
	//********************************************************************
	public Scheduler getScheduler() {
		return scheduler;
	}
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
}
