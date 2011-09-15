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
package com.edgenius.wiki.service.impl;

import java.util.Properties;

/**
 * @author Dapeng.Ni
 */
public class SchedulerFactoryBean extends org.springframework.scheduling.quartz.SchedulerFactoryBean{
	
	public void setQuartzProperties(Properties quartzProperties) {
		//patch for HSQLDB issue on spring+quartz, see
		//http://forums.opensymphony.com/thread.jspa?threadID=5193&messageID=18002#18002
		if(quartzProperties.getProperty("org.quartz.jobStore.driverDelegateClass").indexOf("HSQLDBDelegate") != -1
			|| quartzProperties.getProperty("org.quartz.jobStore.driverDelegateClass").indexOf("MSSQLDelegate") != -1){
			quartzProperties.setProperty("org.quartz.jobStore.selectWithLockSQL", "SELECT * FROM {0}LOCKS UPDLOCK WHERE LOCK_NAME = ?");
		}
		super.setQuartzProperties(quartzProperties);
	}
}
