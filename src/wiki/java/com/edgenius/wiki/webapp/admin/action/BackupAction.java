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
package com.edgenius.wiki.webapp.admin.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.UncategorizedSQLException;

import com.edgenius.core.Constants;
import com.edgenius.core.service.CacheService;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.util.DateUtil;
import com.edgenius.core.util.FileUtil;
import com.edgenius.license.InvalidLicenseException;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.NumberUtil;
import com.edgenius.wiki.quartz.BackupJobInvoker;
import com.edgenius.wiki.quartz.ExportedJob;
import com.edgenius.wiki.rss.RSSService;
import com.edgenius.wiki.search.service.IndexRebuildListener;
import com.edgenius.wiki.search.service.IndexService;
import com.edgenius.wiki.search.service.IndexServiceImpl;
import com.edgenius.wiki.service.BackupException;
import com.edgenius.wiki.service.BackupService;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.util.WikiUtil;
import com.edgenius.wiki.webapp.action.BaseAction;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class BackupAction  extends BaseAction {
	private static final int SCHEDULE_TYPE_MONTHLY = 1;
	private static final int SCHEDULE_TYPE_WEEKLY = 2;
	private static final int SCHEDULE_TYPE_DAILY = 3;
	
	private BackupService backupService; 
	private RSSService rssService; 
	private MessageService messageService; 
	private IndexService indexService;
	private CacheService cacheService;
	private SettingService settingService;
	private BackupJobInvoker backupJobInvoker;
	
	private File restoreFile;
	private String restoreFileFileName;
	
	//this parameter is download file name
	private String filename;
	
	//backup input comment;
	private String comment;
	
	private String scheduleDisplay;
	private int scheduleType;
	private int dayOfMth;
	private String dayOfWeek;
	private int hours;
	private int minutes;
	//0 - from backup list; 1 - from restore list
	private int type;
	/**
	 * List all available backup and restore files in system assigned directory
	 */
	public String execute(){
		
		ExportedJob jobs = backupJobInvoker.exportJob();
		if(jobs != null){
			List<Map<String, Object>> jd = jobs.getJobs();
			if(jd != null && jd.size() > 0){
				Map<String, Object> data = jd.get(0);
				if(data != null){
					String cron = (String) data.get(BackupJobInvoker.CRON_EXPR);
					if(cron != null){
						try {
							scheduleDisplay = parseCron(cron);
						} catch (Exception e) {
							log.error("Unable parse cron string " + cron,e);
						}
					}
				}
			}
		}
		//get backup schedule information
		List<File> flist = backupService.getBackupFileList();
		if(flist.size() > 10)
			flist.subList(0, 10);
		List<FileInfoDTO> bList = convertToInfo(flist);

		getRequest().setAttribute("bList", bList);
		
		//restore
		flist = backupService.getRestoreFileList();
		if(flist.size() > 10)
			flist.subList(0, 10);
		List<FileInfoDTO> rList = convertToInfo(flist);

		getRequest().setAttribute("rList", rList);
		
		return SUCCESS;
	}
	
	//adjust schedule
	public String schedule(){
		//second
		StringBuffer buf = new StringBuffer("0 ");
		
		if(scheduleType == SCHEDULE_TYPE_MONTHLY){
			//dateOfMonth:hh:mm
			buf.append(minutes).append(" ").append(hours).append(" ").append(dayOfMth).append(" * ?");
		}else if(scheduleType == SCHEDULE_TYPE_WEEKLY){
			//dateOfWeek:hh:mm
			buf.append(minutes).append(" ").append(hours).append(" ? * ").append(dayOfWeek);
		}else if(scheduleType == SCHEDULE_TYPE_DAILY){
			//hh:mm
			buf.append(minutes).append(" ").append(hours).append(" * * ?");
		}else{
			//none
			buf = null;
		}
		HttpServletResponse response = getResponse();
		
		try {
			if(buf == null){
				backupJobInvoker.cancelJob();
			}else{
				backupJobInvoker.invokeJob(buf.toString());
			}
			try {
				//can not use getWriter().print(string) as it is not compatible with i18n,e.g., Chinese
				response.getOutputStream().write(new String("0"+parseCron(buf == null?null:buf.toString())).getBytes(Constants.UTF8));
			} catch (IOException e) {
				log.error("Response failed",e);
			}
		} catch (Exception e) {
			log.error("Schedule setup failed:" + (buf == null?"none":buf.toString()),e);
			try {
				response.getOutputStream().write(new String("1"+messageService.getMessage("schedule.failed")).getBytes(Constants.UTF8));
			} catch (IOException e1) {
				log.error("Response failed",e1);
			}
		}
		
		return null;
		
	}

	public String backup(){
		String filename = null;
		try {
			filename = backupService.backup(BackupService.BACKUP_DEFAULT, comment);
		} catch (BackupException e) {
			log.error("Failed on backup.", e);
			getRequest().setAttribute("error", messageService.getMessage("backup.failed"));
		}
		
		execute();
		
		if(filename != null){
			getRequest().setAttribute("message", messageService.getMessage("backup.success",new String[]{FileUtil.getFileName(filename)}));
		}
		return SUCCESS;
	}
	/**
	 * Delete restore or backup file
	 * @return
	 */
	public String delete(){
		List<File> rList;
		if(type == 0){
			rList = backupService.getBackupFileList();
		}else{
			rList = backupService.getRestoreFileList();	
		}
		File dFile= null;
		for (File info : rList) {
			if(StringUtils.equals(filename,info.getName())){
				dFile = info;
				break;
			}
		}
		if(dFile == null){
			getRequest().setAttribute("error", messageService.getMessage("backup.file.no.exist"));
		}else{
			FileUtils.deleteQuietly(dFile);
		}
		execute();
		return SUCCESS;
	}
	
	/**
	 * Download backup file zip.
	 * @return
	 */
	public String download(){
		List<File> rList;
		if(type == 0){
			rList = backupService.getBackupFileList();
		}else{
			rList = backupService.getRestoreFileList();	
		}
	
		File dFile= null;
		for (File info : rList) {
			if(StringUtils.equals(filename,info.getName())){
				dFile = info;
				break;
			}
		}
		if(dFile == null){
			getRequest().setAttribute("error", messageService.getMessage("backup.file.no.exist"));
		}else{
			downloadFile(dFile.getName(),dFile);

		}
		return null;
	}
	
	/**
	 * Restore from existing restore file list.
	 * @return
	 */
	public String restoreFromName(){
		List<File> rList;
		if(type == 0){
			rList = backupService.getBackupFileList();
		}else{
			rList = backupService.getRestoreFileList();	
		}
		
		File rInfo= null;
		for (File info : rList) {
			if(StringUtils.equals(filename,info.getName())){
				rInfo = info;
				break;
			}
		}
		
		if(rInfo == null){
			getRequest().setAttribute("error", messageService.getMessage("restore.file.no.exist"));
		}else{
			boolean rs = restoreFile(rInfo);
			if(rs){
				if(type == 0){
					//move backup file to restore list
					backupService.moveBackupFileToRestoreList(rInfo);
				}
				getRequest().setAttribute("message", messageService.getMessage("restore.success"));
				getRequest().setAttribute("logout", "true");
			}
		}
		
		execute();
		return SUCCESS;
	}
	
	/**
	 * Restore from upload file
	 * @return
	 */
	public String restore(){
		if(restoreFile == null){
			getRequest().setAttribute("error", messageService.getMessage("choose.file.first"));
		}else{
			boolean rs = restoreFile(restoreFile);
			if(rs){
				//save successed restore file into restore directory.
				backupService.addFileToRestoreList(restoreFile, restoreFileFileName);
				getRequest().setAttribute("message", messageService.getMessage("restore.success"));
				getRequest().setAttribute("logout", "true");
			}
		}
		
		execute();
		return SUCCESS;
	}
	
	//********************************************************************
	//               set /get 
	//********************************************************************
	public File getRestoreFile() {
		return restoreFile;
	}

	public void setRestoreFile(File restoreFile) {
		this.restoreFile = restoreFile;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String restoreFileName) {
		this.filename = restoreFileName;
	}

	//********************************************************************
	//               Private
	//********************************************************************
	
	/**
	 * @param string
	 * @return
	 * @throws Exception 
	 */
	private String parseCron(String cron) throws Exception {
		if(cron == null){
			return "("+messageService.getMessage("none")+")";
		}
		//quartz string s m h D M Y w
		String[] list = cron.split(" ");
		if(list.length != 6){
			log.error("Unexpected cron string:" +cron);
			throw new Exception("Unexpected cron string:" +cron);
		}
		
		StringBuffer buf = new StringBuffer();
		dayOfMth = 1;
		if(!"?".equals(list[3]) && !"*".equals(list[3]) ){
			dayOfMth = NumberUtil.toInt(list[3], 1);
			buf.append(messageService.getMessage("monthly"))
			.append(" - ").append(dayOfMth).append(", ");
			
			scheduleType = SCHEDULE_TYPE_MONTHLY;
		}else if(!"?".equals(list[5]) && !"*".equals(list[5])){
			dayOfWeek = list[5];
			buf.append(messageService.getMessage("weekly"))
			.append(" - ").append(messageService.getMessage(dayOfWeek.toLowerCase())).append(", ");
			
			scheduleType = SCHEDULE_TYPE_WEEKLY;
		}else{
			buf.append(messageService.getMessage("daily")).append(" - ");
			scheduleType = SCHEDULE_TYPE_DAILY;
		}
		minutes = NumberUtil.toInt(list[1], 0);
		hours = NumberUtil.toInt(list[2], 0);
		buf.append(hours).append(":").append(minutes);
		
		return buf.toString();
	}

	private boolean restoreFile(File file){
		try {

			backupService.restore(file);
			log.info("Backup transction is done.");
			
			log.info("Reset all cache...");
			cacheService.reset(CacheService.CACHE_ALL);
			settingService.resetSetting();
			
			log.info("Rebuilding index for restored data...");
			//rebuild index 
			IndexRebuildListener listener = new IndexRebuildListener(){
				public void indexComplete(int type) {
					//TODO: process of rebuild index....
				}
			};
			rebuildIndexes(listener, (IndexServiceImpl) indexService);
			
			//clean old RSS - they are need recreated...
			log.info("Clean old RSS ...");
			rssService.cleanAllRss();
			
			//I comment RSS recreate: it is very very slow if it run with rebuild index together while all cache is empty.
			//so I prefer while user open RSS in dashboard or subscription, RSS is generated on demand.
			//rebuild RSS
//			log.info("Rebuilding RSS for restored spaces...");
//			List<String> spaces = spaceService.getAllSpaceUnames();
//			if(spaces != null && spaces.size() > 0){
//				for (String spaceUname : spaces) {
//					try {
//						rssService.createFeed(spaceUname);
//					} catch (Exception e) {
//						log.error("Failed to create space RSS: " + spaceUname,e);
//					}
//				}
//			}
			
			return true;
		} catch (InvalidLicenseException e) {
			log.warn("Invalid license and failed in restore");
			getRequest().setAttribute("error",  messageService.getMessage("restore.failed") + 
					 messageService.getMessage("invalid.license.reason.1"));
		} catch (UncategorizedSQLException e) {
			log.error("Failed on restore with SQL exception.", e.getSQLException() != null? 
					e.getSQLException().getNextException():e.getSQLException());
			getRequest().setAttribute("error", messageService.getMessage("error.restore"));
		} catch (Exception e) {
			log.error("Failed on restore.", e);
			getRequest().setAttribute("error", messageService.getMessage("error.restore"));
		}
		
		return false;
	}
	/**
	 * @param backupFileList
	 * @return
	 */
	private List<FileInfoDTO> convertToInfo(List<File> fileList) {
		List<FileInfoDTO> list = new ArrayList<FileInfoDTO>();
		if(fileList != null){
			for (File f : fileList) {
				FileInfoDTO dto = new FileInfoDTO();
				dto.setComment(backupService.getFileComment(f));
				dto.setName(f.getName());
				dto.setSize(GwtUtils.convertHumanSize(f.length()));
				String date = DateUtil.toDisplayDate(WikiUtil.getUser(),new Date(f.lastModified()),messageService);
				dto.setDate(date);
				list.add(dto);
			}
		}
		return list;
	}

	//********************************************************************
	//               set / get
	//********************************************************************
	public void setBackupService(BackupService backupService) {
		this.backupService = backupService;
	}
	public void setIndexService(IndexService indexService) {
		this.indexService = indexService;
	}
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	public void setRssService(RSSService rssService) {
		this.rssService = rssService;
	}

	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}

	public void setBackupJobInvoker(BackupJobInvoker backupJobInvoker) {
		this.backupJobInvoker = backupJobInvoker;
	}

	public int getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(int scheduleType) {
		this.scheduleType = scheduleType;
	}

	public int getDayOfMth() {
		return dayOfMth;
	}

	public void setDayOfMth(int dateOfMth) {
		this.dayOfMth = dateOfMth;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dateofWeek) {
		this.dayOfWeek = dateofWeek;
	}

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the scheduleDisplay
	 */
	public String getScheduleDisplay() {
		return scheduleDisplay;
	}

	/**
	 * @param scheduleDisplay the scheduleDisplay to set
	 */
	public void setScheduleDisplay(String scheduleDisplay) {
		this.scheduleDisplay = scheduleDisplay;
	}

	public String getRestoreFileFileName() {
		return restoreFileFileName;
	}

	public void setRestoreFileFileName(String restoreFileFileName) {
		this.restoreFileFileName = restoreFileFileName;
	}

}
