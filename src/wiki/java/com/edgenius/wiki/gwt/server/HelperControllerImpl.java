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
package com.edgenius.wiki.gwt.server;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;

import com.edgenius.core.Global;
import com.edgenius.core.UserSetting;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.ITicket;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.service.MailService;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.util.ServletUtils;
import com.edgenius.wiki.gwt.client.model.FeedbackModel;
import com.edgenius.wiki.gwt.client.model.InvitationModel;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.model.UploadProgressModel;
import com.edgenius.wiki.gwt.client.server.HelperController;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.handler.GWTSpringController;
import com.edgenius.wiki.model.ActivityLog;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.service.ActivityLogService;
import com.edgenius.wiki.service.FriendService;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.SecurityDummy;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.util.WikiUtil;
import com.edgenius.wiki.webapp.servlet.UploadStatus;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class HelperControllerImpl  extends GWTSpringController implements HelperController{
	private static final Logger log = LoggerFactory.getLogger(HelperControllerImpl.class);
	
	private MailService mailService;
	private FriendService friendService;
	private RepositoryService repositoryService;
	private PageService pageService;
	private SecurityDummy securityDummy;
	private MessageService messageService;
	private ActivityLogService activityLog;
	private SettingService settingService;
	
	public UploadProgressModel checkUploadingStatus() {
		UploadStatus status = (UploadStatus) ServletUtils.getRequest().getSession().getAttribute(UploadStatus.NAME);
		UploadProgressModel model = new UploadProgressModel();
		if(status != null){
			log.debug("total " +status.getTotalSize() +  ". current reading..."+status.getBytesRead());
			model.status = status.getStatus();
			model.bytesRead = status.getBytesRead();
			model.totalSize = status.getTotalSize();
			model.elapseSecond = status.getElapsedSecond();
			model.currentFileIndex = status.getFileIndex();
			model.currentFilename = status.getFilename();
		}		
		return model;
	}

	public boolean sendFeedback(FeedbackModel feedback) {
		
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setSubject(messageService.getMessage("sent.msg.title"));
		msg.setTo(Global.DefaultReceiverMail);
		msg.setFrom(Global.DefaultNotifyMail);
		msg.setText(feedback.content + "\r\n" + messageService.getMessage("sent.by") + " " +feedback.email);
		mailService.send(msg);
		
		return true;
	}

	//JDK1.6 @Override
	public boolean sendNotify(String receiver, String text) {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setSubject(messageService.getMessage("sent.msg.title"));
		
		if(receiver == null){
			if(!StringUtils.isBlank(Global.DefaultReceiverMail))
				msg.setTo(Global.DefaultReceiverMail);
			
			if(Global.ccToSystemAdmin || StringUtils.isBlank(Global.DefaultReceiverMail)){
				Set<String> bcc = userReadingService.getSystemAdminMailList();
				if(bcc != null && bcc.size() > 0)
					msg.setBcc(bcc.toArray(new String[bcc.size()]));
				
			}
		}else{
			//TODO: send message to special user - receiver is userID or email address?
			
		}
		msg.setFrom(Global.DefaultNotifyMail);
		msg.setText(text + "\r\n" + messageService.getMessage("sent.by") + " "+ WikiUtil.getUser().getFullname());
		mailService.send(msg);
		
		return true;
	}

	
	public InvitationModel sendInvitation(InvitationModel invitation) {
		User user = WikiUtil.getUser();
		//permission check!
		securityDummy.checkSpaceAdmin(invitation.spaceUname);
		
		// return errorCode and valid email group separator by ","
		InvitationModel returnModel = new InvitationModel();
		try {
		    List<String> emails = friendService.sendInvitation(user, invitation.spaceUname, invitation.emailGroup, invitation.message);
			returnModel.emailGroup = StringUtils.join(emails, ",");
		} catch (Exception e) {
			returnModel.errorCode = ErrorCode.INVITATION_FAILED; 
		}
		
		return returnModel;
	}
	//JDK1.6 @Override
	public String getPageTitleByAttachmentNodeUuid(String spaceUname, String nodeUuid) {
		try {
			ITicket ticket = repositoryService.login(spaceUname,spaceUname, spaceUname);
			FileNode node =  repositoryService.getMetaDate(ticket, nodeUuid, null);
			//pageUuid!
			Page page = pageService.getCurrentPageByUuid(node.getIdentifier());
			if(page != null)
				return page.getTitle();
		} catch (Exception e) {
			log.error("Unable to get page information from attachment " + nodeUuid + " in space " + spaceUname, e);
		}
		return null;
	}
	public Integer notifyPinPanelStatus(int panelID, boolean visible) {
		if(panelID == SharedConstants.TAB_TYPE_RIGHT_SIDEBAR){
			User user = WikiUtil.getUser();
			if(user != null && !user.isAnonymous()){
				UserSetting setting = user.getSetting();
				int pin = setting.getFixedPanel();
				//system design, -1 is initial status. For -1 value, right sidebar is turn on
				if(pin < 0) pin = SharedConstants.TAB_TYPE_RIGHT_SIDEBAR;  
				
				//to ensure only save when status is changed.
				if((visible && (pin & panelID) == 0) || (!visible && (pin & panelID) > 0)){
					pin = visible?(pin | panelID):(pin ^ panelID);
					setting.setFixedPanel(pin);
					settingService.saveOrUpdateUserSetting(user,setting);
				}
			}
			return panelID;
		}
		return 0;
	}

	public PortletModel getActivityLogs(int currentPage){
		PortletModel model = new PortletModel();
		
		if(currentPage < 1)
			currentPage = 1;
		
		int start = (currentPage-1) *  SharedConstants.ITEM_COUNT_IN_ACTIVITY_BOARD;
		
		//try to return 1 more than request - then know if it has next page.
		List<ActivityLog> logs = activityLog.getActivities(start, SharedConstants.ITEM_COUNT_IN_ACTIVITY_BOARD+1, WikiUtil.getUser());
		model.hasNxt = logs.size() > SharedConstants.ITEM_COUNT_IN_ACTIVITY_BOARD;
		
		logs.remove(logs.size()-1);
		model.renderContent = activityLog.renderActivities(logs);
		
		model.hasPre = currentPage > 1;
		model.currentPage = currentPage;
		
		return model;
		
	}
	//********************************************************************
	//               set / get 
	//********************************************************************
	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public void setFriendService(FriendService friendService) {
		this.friendService = friendService;
	}

	public void setSecurityDummy(SecurityDummy securityDummy) {
		this.securityDummy = securityDummy;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public void setPageService(PageService pageService) {
		this.pageService = pageService;
	}

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	public void setActivityLog(ActivityLogService activityLog) {
		this.activityLog = activityLog;
	}
	
	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}
}
