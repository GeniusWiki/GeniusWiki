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
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Global;

/**
 * @author Dapeng.Ni
 */
public class VersionCheckJobInvoker {
	private static final Logger log = LoggerFactory.getLogger(VersionCheckJobInvoker.class);
	private static final String JOB_NAME = "VersionCheck-QuartJob";
	private static final String TRIGGER_NAME =  "VersionCheck-Trigger";
	
	private JobDetail versionCheckJob;
	private Scheduler scheduler;
	private TriggerKey triggerKey;
	
	public VersionCheckJobInvoker() {
		versionCheckJob = JobBuilder.newJob(VersionCheckJob.class)
				 .withIdentity(JOB_NAME, Scheduler.DEFAULT_GROUP)
				 .withDescription(JOB_NAME)
				 .storeDurably()
				 .requestRecovery()
				 .build();
		
		triggerKey = new TriggerKey(TRIGGER_NAME,Scheduler.DEFAULT_GROUP);
	}
	public void invokeJob() throws QuartzException{
		if(!Global.VersionCheck)
			return;
		
		// start the scheduling job
		try{
	
			//check if this trigger already exist, if so, need cancel it then recreate
			Trigger trigger = scheduler.getTrigger(triggerKey);
			if(trigger != null){
				scheduler.unscheduleJob(triggerKey);
				log.info("Last version check job is cancelled and ready for new job set");
			}
			
			trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(CronScheduleBuilder.cronSchedule(Global.VersionCheckCron)).build();
			
			scheduler.scheduleJob(versionCheckJob, trigger);
			
			log.info("Version check is scheduled in " + Global.VersionCheckCron);
		}catch (SchedulerException e){
			log.error("Error occurred at [VersionCheck Schedule]- fail to start scheduling:" , e);
			throw new QuartzException("Error occurred at [VersionCheck Schedule]- fail to start scheduling",e);
		} catch (ParseException e) {
			log.error("Failed parse cron string: " + Global.VersionCheckCron,e);
			throw new QuartzException("Failed parse cron string: " + Global.VersionCheckCron,e);
		}
	}
	public void cancelJob() throws QuartzException{
		try {
			scheduler.unscheduleJob(triggerKey);
			log.info("Version check job is cancelled." );
		} catch (SchedulerException e) {
			log.error("Error occurred at [VersionCheck Schedule]- fail to cancel scheduling:" , e);
			throw new QuartzException("Error occurred at [VersionCheck Schedule]- fail to cancel scheduling",e);
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
