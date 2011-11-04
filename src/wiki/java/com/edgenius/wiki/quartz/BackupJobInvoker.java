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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dapeng.Ni
 */
public class BackupJobInvoker implements ExportableJob{
	private static final Logger log = LoggerFactory.getLogger(BackupJobInvoker.class);
	private static final String JOB_NAME = "Backup-QuartJob";
	private static final String TRIGGER_NAME = "Backup-Trigger";
	public static final String CRON_EXPR = "cron-str";

	private JobDetail backupJob;

	private Scheduler scheduler;
	private TriggerKey triggerKey;

	public BackupJobInvoker(){
		backupJob = JobBuilder.newJob(BackupJob.class)
				 .withIdentity(JOB_NAME, QUARTZ_EXPORTABLE_JOB_GROUP)
				 .withDescription(JOB_NAME)
				 .storeDurably()
				 .requestRecovery()
				 .build();
		
		triggerKey = new TriggerKey(TRIGGER_NAME, QUARTZ_EXPORTABLE_JOB_GROUP);
	}
	public void invokeJob(String cron) throws QuartzException {


		// start the scheduling job
		try {
			// check if this trigger already exist, if so, need cancel it then recreate
			cancelJob();
			
			CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
					.withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
			scheduler.scheduleJob(backupJob, trigger);

			log.info("Backup job is scheduled in " + cron);
		} catch (SchedulerException e) {
			log.error("Error occurred at [Backup Job Schedule]- fail to start scheduling:", e);
			throw new QuartzException("Error occurred at [Backup Job  Schedule]- fail to start scheduling", e);
		} catch (ParseException e) {
			log.error("Failed parse cron string: " + cron, e);
			throw new QuartzException("Failed parse cron string: " + cron, e);
		}
	}

	public void cancelJob() throws QuartzException {
		try {
			//delete trigger 
			if(scheduler.checkExists(triggerKey)){
				scheduler.unscheduleJob(triggerKey);
				log.info("Last backup job is cancelled and ready for new job set");
			}	
			
			JobKey jobKey = new JobKey(JOB_NAME, QUARTZ_EXPORTABLE_JOB_GROUP);
			if(scheduler.checkExists(jobKey)){
				scheduler.deleteJob(jobKey);
				log.info("Last backup job is cancelled and ready for new job set");
			}
		} catch (SchedulerException e) {
			log.error("Error occurred at [Backup Job Schedule]- fail to cancel scheduling:", e);
			throw new QuartzException("Error occurred at [Backup Job  Schedule]- fail to cancel scheduling", e);
		}
	}
	
	public ExportedJob exportJob() {
		ExportedJob expJob = null;
		try {
			List<Map<String,Object>> jobs = new ArrayList<Map<String,Object>>();
		
			Trigger trigger = scheduler.getTrigger(triggerKey);
			if(trigger != null ) {
				String expr = ((CronTrigger)trigger).getCronExpression();
				
				Map<String, Object> jobData = new HashMap<String, Object>();
				jobData.put(CRON_EXPR,expr);
				jobs.add(jobData);
			}

			if(jobs.size() > 0){
				//return value
				expJob = new ExportedJob();
				expJob.setJobType(BackupJobInvoker.class.getName());
				expJob.setJobs(jobs);
			}
		} catch (Exception e) {
			log.error("Failed export backup job.",e);
		}
		
		return expJob;
	}



	public void importJob(List<Map<String, Object>> jobs) {
		if(jobs != null && jobs.size() > 0){
			//so far only one backup job
			Map<String, Object> jobData = jobs.get(0);
			String cron = (String) jobData.get(CRON_EXPR);
			try {
				invokeJob(cron);
				log.info("Restore backup quartz job successed.");
			} catch (QuartzException e) {
				log.info("Restore backup quartz job failed.",e);
			}
		}
	}

	// ********************************************************************
	// set /get
	// ********************************************************************
	public Scheduler getScheduler() {
		return scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}


}
