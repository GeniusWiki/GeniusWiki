package com.edgenius.wiki.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public abstract class AbstractQuartzJobBean implements Job {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	//there is hardcode in applicationContext-quartz.xml
	private static final String APPLICATION_CONTEXT_KEY = "applicationContext";
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException{
		try {
			this.setApplicationContext((ApplicationContext) context.getScheduler().getContext().get(APPLICATION_CONTEXT_KEY));
		} catch (SchedulerException e) {
			log.error("Unable to set applicatoin context to job " + this.getClass(), e);
		}
		
		this.executeInternal(context);
	}
	
	public abstract void executeInternal(JobExecutionContext context) throws JobExecutionException;
	public abstract void setApplicationContext(ApplicationContext applicationContext);
	
}
