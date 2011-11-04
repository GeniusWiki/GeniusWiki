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
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.edgenius.wiki.service.BackupException;
import com.edgenius.wiki.service.BackupService;

/**
 * @author Dapeng.Ni
 */
public class BackupJob extends AuthenticatedQuartzJobBean {
	private static final Logger log = LoggerFactory.getLogger(BackupJob.class);


	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			this.applicationContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
		} catch (SchedulerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		BackupService backupService = (BackupService) applicationContext.getBean(BackupService.SERVICE_NAME);
		try {
			proxyLoginAsSystemAdmin();
			
			String  filename = backupService.backup(BackupService.BACKUP_DEFAULT, "Automatic backup on schedule");
			log.info("Backup scheduled job complete successfully. Backup file name: " + filename);
		} catch (BackupException e) {
			log.info("Backup scheduled job failed.",e);
			throw new JobExecutionException("Backup scheduled job failed.", e);
		}finally{
			logout();
		}
		
	}
	
}
