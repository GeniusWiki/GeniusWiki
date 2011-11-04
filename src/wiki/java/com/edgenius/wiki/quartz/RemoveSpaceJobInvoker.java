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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.wiki.WikiConstants;
/**
 * 
 * @author Dapeng.Ni
 */
public class RemoveSpaceJobInvoker implements ExportableJob{
	private static final Logger log = LoggerFactory.getLogger(RemoveSpaceJobInvoker.class);
	private static final String REMOVE_SPACE_JOB_PRE = "removeSpace";
	private static final String REMOVE_SPACE_TRIGGER = "removeSpaceTrigger:";
	private static final String DELAY_HOURS = "delayHours";

	
	private JobDetail removeSpaceJob;
	private Scheduler scheduler;
	
	public RemoveSpaceJobInvoker(){
	}
	public void invokeJob(String spaceUname, String removerUsername, int delayHours) throws QuartzException{
		//this setting are impact exportJobs() method
		TriggerKey triggerKey = new TriggerKey(REMOVE_SPACE_TRIGGER+ spaceUname, QUARTZ_EXPORTABLE_JOB_GROUP);
		removeSpaceJob = JobBuilder.newJob(RemoveSpaceJob.class)
				 .withIdentity(REMOVE_SPACE_JOB_PRE + spaceUname, QUARTZ_EXPORTABLE_JOB_GROUP)
				 .withDescription(spaceUname+":" + removerUsername)
				 .usingJobData(WikiConstants.ATTR_SPACE_UNAME,spaceUname)
				 .storeDurably()
				 .requestRecovery()
				 .build();
		
		//delay given hours
//		Date startDate = DateUtils.addSeconds(new Date(), delayHours);
		Date startDate = DateUtils.addHours( new Date(), delayHours);
		// start the scheduling job
		try{
			//delete trigger: restore 
			Trigger trigger = scheduler.getTrigger(triggerKey);
			if(trigger != null){
				scheduler.unscheduleJob(triggerKey);
				log.info("Last remove space job is cancelled and ready for new job set");
			}	
			
			trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).startAt(startDate).build();
			scheduler.scheduleJob(removeSpaceJob, trigger);
			log.info("Remove space " + spaceUname + " is scheduled in " + delayHours + " hours later.");
		}catch (SchedulerException e){
			log.error("Error occurred at [RemoveSpace Schedule]- fail to start scheduling:" , e);
			throw new QuartzException("Error occurred at [RemoveSpace Schedule]- fail to start scheduling",e);
		}
	}

	public void cancelJob(String spaceUname) throws QuartzException{
		try {
			scheduler.unscheduleJob(new TriggerKey(REMOVE_SPACE_TRIGGER+ spaceUname, QUARTZ_EXPORTABLE_JOB_GROUP));
		} catch (SchedulerException e) {
			log.error("Error occurred at [RemoveSpace Schedule]- fail to cancel scheduling:" , e);
			throw new QuartzException("Error occurred at [RemoveSpace Schedule]- fail to cancel scheduling",e);
		}
	}
	
	public int getLeftHours(String spaceUname){
		try {
			List<? extends Trigger> triggers = scheduler.getTriggersOfJob(new JobKey(REMOVE_SPACE_JOB_PRE+ spaceUname,QUARTZ_EXPORTABLE_JOB_GROUP));
			if(triggers != null && triggers.size() > 0){
				String hours = DurationFormatUtils.formatDuration(triggers.get(0).getStartTime().getTime() - new Date().getTime(), "H");
				return NumberUtils.toInt(hours);
			}else{
				log.warn("Unable to get job detail for space remove job " + spaceUname);
			}
		} catch (SchedulerException e) {
			log.error("Unable to get job detail for space remove job " + spaceUname,e);
		}
		return 0;
	}
	
	//JDK1.6 @Override
	public ExportedJob exportJob() {
		ExportedJob expJob = null;
		try {
			Set<JobKey> jobNames = scheduler.getJobKeys(GroupMatcher.jobGroupStartsWith(REMOVE_SPACE_JOB_PRE));
			
			List<Map<String,Object>> jobs = new ArrayList<Map<String,Object>>();
			for (JobKey jobKey : jobNames) {
				
				Map<String, Object> jobData = new HashMap<String, Object>();
				String spaceUname = jobKey.getName().substring(REMOVE_SPACE_JOB_PRE.length());

				JobDetail detail = scheduler.getJobDetail(jobKey);
				String desc = detail.getDescription();
				String username = ""; 
				if(desc.startsWith(spaceUname+":"))
					username = desc.substring(spaceUname.length()+1);
				
				List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
				int hours = -1;
				if(triggers != null && triggers.size() > 0 ) {
					//per trigger per job
					Trigger trigger = triggers.get(0);
					long period = trigger.getNextFireTime().getTime() - new Date().getTime();
					//to hours
					if(period > 0)
						hours = (int) (period/3600000);
				}
				if(hours != -1){
					jobData.put(WikiConstants.ATTR_SPACE_UNAME, spaceUname);
					jobData.put(DELAY_HOURS, hours);
					jobData.put(WikiConstants.ATTR_USER, username);
					jobs.add(jobData);
				}
			}
			
			if(jobs.size() > 0){
				//return value
				expJob = new ExportedJob();
				expJob.setJobType(RemoveSpaceJobInvoker.class.getName());
				expJob.setJobs(jobs);
			}
		} catch (SchedulerException e) {
			log.error("Failed export RemoveSpace job.",e);
		}
		return expJob;
	}
	//JDK1.6 @Override
	public void importJob(List<Map<String, Object>> jobs) {
		for (Map<String, Object> map : jobs) {
			String spaceUname = (String) map.get(WikiConstants.ATTR_SPACE_UNAME);
			int delayHours = ((Integer) map.get(DELAY_HOURS)).intValue();
			String username = (String) map.get(WikiConstants.ATTR_USER);
			
			//at least delay 1 hour
			if(delayHours <=0)
				delayHours = 1;
			try {
				invokeJob(spaceUname, username, delayHours);
			} catch (QuartzException e) {
				log.error("Unable to restore remove space job for " + spaceUname,e);
			}
		}
	}
	//********************************************************************
	//               set /get
	//********************************************************************
	public void setRemoveSpaceJob(JobDetail removeSpaceJob) {
		this.removeSpaceJob = removeSpaceJob;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

}
