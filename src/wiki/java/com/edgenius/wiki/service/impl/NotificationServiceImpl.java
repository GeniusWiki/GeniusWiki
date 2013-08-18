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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.Global;
import com.edgenius.core.Installation;
import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.core.Version;
import com.edgenius.core.model.Resource;
import com.edgenius.core.model.User;
import com.edgenius.core.service.MailService;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.ActivityType;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.dao.NotificationDAO;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.ActivityLog;
import com.edgenius.wiki.model.Notification;
import com.edgenius.wiki.quartz.QuartzException;
import com.edgenius.wiki.quartz.VersionCheckJobInvoker;
import com.edgenius.wiki.security.service.SecurityCheckException;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.ActivityLogService;
import com.edgenius.wiki.service.NotificationService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */

@Transactional
public class NotificationServiceImpl implements NotificationService {
	private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

	private NotificationDAO notificationDAO;
	private MailService mailService;
	private UserReadingService userReadingService;
	private SecurityService securityService;
	private MessageService messageService;
	private VersionCheckJobInvoker versionCheckJobInvoker;
	@Autowired private ActivityLogService activityLog;

	
	public void initScheduledJob(){
		if(!Global.VersionCheck)
			return;
		
		//when system start, do version check first.
		new Thread(new Runnable() {
			public void run() {
				//as if no connection, this method will block a while, so put it into thread to avoid server start up block.
				doVersionCheck();
			}
		}).start();
		
		//put version on Quartz job.
		try {
			versionCheckJobInvoker.invokeJob();
		} catch (QuartzException e) {
			log.error("Unable start schedule job for version check.",e);
		}
	}
	public void doVersionCheck() {
		HttpURLConnection conn = null;
		try {
			log.info("Version check starting");
			int currVer = (int) (NumberUtils.toFloat(Version.VERSION,0f)*1000);
			//hard code
			URL url = new URL("http://product.edgenius.com/versioncheck/"+SharedConstants.APP_NAME+"/"+Installation.INSTANCE_ID+"/"+currVer);
//			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy url", 80));
//			conn = (HttpURLConnection) url.openConnection(proxy);
			conn = (HttpURLConnection) url.openConnection();
			conn.setAllowUserInteraction(false);     
//			conn.addRequestProperty("Authorization", "Basic "+encrytString(username+":" + password));

			conn.setReadTimeout(20000);
			InputStream in = conn.getInputStream();
			StringBuffer sb = new StringBuffer();
			byte[] b = new byte[1024*10];
			int len;
			while((len = in.read(b)) != -1){
				sb.append(new String(b,0,len));
				
			}
			String content = sb.toString();
//			String content = "<version>3.01</version>";
			int start = content.indexOf("<version>");
			int end = content.indexOf("</version>");
			if( start != -1 && end != -1 && start < end){
				String verStr = content.substring(start+9, end);
				int version = (int) (NumberUtils.toFloat(verStr,0f)*1000);
			
				if(version > 0 && currVer > 0){
					if(version > currVer){
						//check if this new version is already send notification to user, if so, silence.
						
						List<ActivityLog> activities = activityLog.getByTarget(ActivityType.Type.SYSTEM_EVENT.getCode(),ActivityType.SubType.VERSION_PING.getCode(), version,"VERSION_CHECK"); //hardcode
						if(activities == null || activities.size() == 0){
							Map<String,Object> map = new HashMap<String,Object>();
							map.put("newVer",verStr);
							map.put("currVer", Version.VERSION);
							mailService.sendPlainToSystemAdmins(WikiConstants.MAIL_TEMPL_VERSION_CHECK,map);
							log.info("New version {} found and notified to system administrators.", version);
							
							//log activity
							ActivityLog activity = new ActivityLog();
							activity.setType(ActivityType.Type.SYSTEM_EVENT.getCode());
							activity.setSubType(ActivityType.SubType.VERSION_PING.getCode());
							activity.setTgtResourceType(version);
							activity.setTgtResourceName("VERSION_CHECK");//hardcode
							activity.setExtroInfo(verStr);
							activity.setCreatedDate(new Date());
							activityLog.save(activity);
						}else{
							log.info("New version {} found, but this version is already notified so no action takes", 2000);
						}
					}
				}else{
					log.info("Wrong version number returned:{}",content);
				}
			}
			log.info("Version check is done");
			
		
		} catch (Exception e) {
			log.warn("Version check not success. This probably because of your network connection");
		}finally{
			try {
				if(conn != null) conn.disconnect();
			} catch (Exception e2) {
			}
		}
	}

	public Notification getMessage(Integer msgUid,User viewer) throws SecurityCheckException {
		//check if this user has permission to read this message
		Notification msg =  notificationDAO.get(msgUid);
		//NO permission check?
		return msg;
	}

	
	public List<Notification> getUserMessages(User viewer, int start, int retCount) {
		if(viewer == null || viewer.isAnonymous())
			return null;
		
		List<Resource> adminResList = null;
		List<Resource> spaceResList = null;
		boolean isSysAdmin = securityService.isAllowResourceAdmin(SharedConstants.INSTANCE_NAME, RESOURCE_TYPES.INSTANCE, viewer);
		if(!isSysAdmin){
			adminResList = securityService.getResourceOfUserHasOperation(viewer, OPERATIONS.ADMIN);
			spaceResList = securityService.getResourceOfUserHasOperation(viewer, OPERATIONS.WRITE);
		}
		List<Notification> msgs = new ArrayList<Notification>();
		Collection<? extends Notification> list = notificationDAO.getResourceMessages(viewer, isSysAdmin, spaceResList, adminResList,start, retCount);
		
		for (Notification notification : list) {
			if(isSysAdmin)
				notification.setRemovable(true);
			else{
				if(viewer != null && !viewer.isAnonymous() && viewer.equals(notification.getCreator())){
					notification.setRemovable(true);
				}else{
					notification.setRemovable(false);
				}
			}
			msgs.add(notification);
		}
		
		return msgs;
		
	}

	public void sendTwitterMessage(String text, int sendEmail) {
		if(StringUtils.isBlank(text) || "@".equals(text) || "@@".equals(text))
			return;
		
		if(text.trim().startsWith("@")){
			//parse message
			StringBuilder buf = new StringBuilder(text.trim());
			List<String> receivers = parseReceivers(buf);
			if(receivers.size() == 0){
				//not valid receiver pass out, send to all user
				sendMessage(text,SharedConstants.MSG_TARGET_ALL_USERS, null, sendEmail);
			}else{
				String msg = buf.toString();
				
				if(!StringUtils.isBlank(msg)){
					for(String receiver:receivers){
						String piece = receiver.trim();
						int target = -1;
						if(piece.startsWith("@@")){
							target = SharedConstants.MSG_TARGET_SPACE_CONTRIBUTE_USERS;
							piece = piece.substring(2);
						}else if(piece.startsWith("@")){
							target = SharedConstants.MSG_TARGET_USER;
							piece = piece.substring(1);
						}else{
							//unexpected
							AuditLogger.error("Unexpected: Unable to parse message:" + text);
						}
						if(target != -1){
							if(piece.startsWith("'") && piece.endsWith("'")){
								piece = piece.substring(1,piece.length()-1);
							}
							if(!StringUtils.isBlank(piece)){
								log.info("Message sending to {} in type {}",piece, target);
								sendMessage(msg,target,piece, sendEmail);
							}
						}
					}
				}
			}
		}else{
			sendMessage(text,SharedConstants.MSG_TARGET_ALL_USERS, null, sendEmail);
		}
		
	}
	public void removeMessage(Integer msgUid) {
		notificationDAO.remove(msgUid);
		
	}

	public void sendMessage(String text,int targetType, String targetName, int withEmail){
		
		Notification message = new Notification();
		WikiUtil.setTouchedInfo(userReadingService, message);
		message.setMessage(text);
		message.setTargetType(targetType);
		message.setTargetName(targetName);
		notificationDAO.saveOrUpdate(message);
		
		if(withEmail == SEND_MAIL_ALAWYS || withEmail== SEND_MAIL_ONLY_HAS_RECEIVERS){
	        // Send to all user
			if(targetType == SharedConstants.MSG_TARGET_USER && !StringUtils.isBlank(targetName)){
				User user = userReadingService.getUserByName(targetName);
				if(user != null && !user.isAnonymous())
					sendMailToUser(message,user.getContact().getEmail());
			}else if(targetType == SharedConstants.MSG_TARGET_SPACE_ADMIN_ONLY && !StringUtils.isBlank(targetName)){
				//space resource: send email to all admin of this space
				Set<String> users =userReadingService.getSpaceAdminMailList(targetName);
				for (String user : users) {
					sendMailToUser(message ,user);
				}
			}else if(targetType == SharedConstants.MSG_TARGET_SPACE_CONTRIBUTE_USERS && !StringUtils.isBlank(targetName)){
				Set<String> users =userReadingService.getSpaceContributorMailList(targetName);
				for (String user : users) {
					sendMailToUser(message ,user);
				}
			}else if(targetType == SharedConstants.MSG_TARGET_INSTANCE_ADMIN_ONLY){
				Set<String> users =userReadingService.getSystemAdminMailList();
				for (String user : users) {
					sendMailToUser(message ,user);
				}
			}else{
				//TODO --- not implemented yet
				//SharedConstants.MSG_TARGET_ALL_USERS //send to all user?
				//SharedConstants.MSG_TARGET_FOLLOWERS
			}
			log.info("Email sent to {} for message.",  message.getTargetName());
		}
	}
	
	

	//********************************************************************
	//               private method
	//********************************************************************
	public List<String> parseReceivers(StringBuilder buf) {
		int len = buf.length();
		List<String> receivers = new ArrayList<String>();
		StringBuilder rece = new StringBuilder();
		int isReceiver = 0;
		int idx;
		for (idx = 0; idx < len; idx++) {
			char ch = buf.charAt(idx);

			if(ch == '@' && isReceiver != 2){
				rece.append(ch);
				isReceiver = 1; //expect text or '
				continue;
			}
			
			if(isReceiver == 0){
				if(ch != ' '){
					break;
				}else{
					continue;
				}
			}
			
			if(ch == '\'' && isReceiver > 0){
				rece.append(ch);
				if(isReceiver == 1 && idx > 0 && buf.charAt(idx-1) == '@'){
					isReceiver = 2; //expect end '
				}else if(isReceiver == 2){
					receivers.add(rece.toString());
					rece = new StringBuilder();
					isReceiver = 0;
				}
				continue;
			}
			
			if (ch == ' ' &&  isReceiver == 1){
				receivers.add(rece.toString());
				rece = new StringBuilder();
				isReceiver = 0;
				continue;
			}
			if (isReceiver > 0){
				rece.append(ch);
			}
		}
		if(rece.length() != 0){
			//this already means something wrong - no message in text body, only receivers.  
			//however, just put receiver in list and let outside method to process this case 
			receivers.add(rece.toString());
		}
		buf.delete(0, idx);
		
		return receivers;
	}
	/**
	 * @param message
	 */
	private void sendMailToUser(Notification message, String receiveAddr) {
		try {
			User sender = message.getCreator();
			
			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setFrom(Global.DefaultNotifyMail);
			Map<String,Object> map = new HashMap<String,Object>();
			map.put(WikiConstants.ATTR_USER, sender);
			map.put(WikiConstants.ATTR_PAGE_TITLE, messageService.getMessage("new.message.notify.mail.subject"));
			map.put("message", message.getMessage());
			map.put(WikiConstants.ATTR_PAGE_LINK, WebUtil.getHostAppURL());
			msg.setTo(receiveAddr); 
			mailService.sendPlainMail(msg, WikiConstants.MAIL_TEMPL_MESSAGE, map);
		} catch (Exception e) {
			log.error("Failed send notification email:" + receiveAddr,e);
		}
	}
	//********************************************************************
	//               set /get
	//********************************************************************
	public void setNotificationDAO(NotificationDAO notificationDAO) {
		this.notificationDAO = notificationDAO;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	public void setVersionCheckJobInvoker(VersionCheckJobInvoker versionCheckJobInvoker) {
		this.versionCheckJobInvoker = versionCheckJobInvoker;
	}

}
