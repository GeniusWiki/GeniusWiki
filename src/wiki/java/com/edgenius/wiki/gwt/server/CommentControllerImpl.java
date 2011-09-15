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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.captcha.CaptchaServiceProxy;

import com.edgenius.core.SecurityValues;
import com.edgenius.core.model.User;
import com.edgenius.core.service.MessageService;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.CommentListModel;
import com.edgenius.wiki.gwt.client.model.CommentModel;
import com.edgenius.wiki.gwt.client.server.CommentController;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.server.handler.GWTSpringController;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.ActivityLogService;
import com.edgenius.wiki.service.CommentException;
import com.edgenius.wiki.service.CommentService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class CommentControllerImpl extends GWTSpringController implements CommentController{
	private static final Logger log = LoggerFactory.getLogger(CommentControllerImpl.class);
	private CommentService commentService;
	private SecurityService securityService;
	private MessageService messageService;
	private ActivityLogService activityLog;
	
	private CaptchaServiceProxy captchaService;
	//JDK1.6 @Override
	public CommentModel createComment(String spaceUname, String pageUuid, CommentModel msg) {
		if(!WikiUtil.captchaValid(captchaService, msg))
			return null;
		
		PageComment comment = PageUtil.copyModelToComment(msg);
		try {
			comment = commentService.createComment(spaceUname,pageUuid, comment, WikiConstants.NOTIFY_ALL);
			
			activityLog.logComment(comment);
		} catch (CommentException e) {
			CommentModel model = new CommentModel();
			model.errorCode = ErrorCode.COMMENT_CREATE_FAILED;
		}
		User user = WikiUtil.getUser();
		CommentModel model = PageUtil.copyCommentToModel(comment,user);
		
		try {
			model.pageCommentCount = commentService.getPageCommentCount(spaceUname,pageUuid);
		} catch (Exception e) {
			log.error("Unable get page comment count. sapce " + spaceUname + " uuid " + pageUuid,e);
			
		}
		
		return model;
		
	}
	//JDK1.6 @Override
	public CommentListModel getPageComments(String spaceUname, String pageUuid) {
		CommentListModel listModel = new CommentListModel();
		try {
			List<PageComment> list = commentService.getPageComments(spaceUname,pageUuid);
			
			listModel.comments = new ArrayList<CommentModel>();
			
			User user = WikiUtil.getUser();
			for (PageComment pageComment : list) {
				CommentModel commentModel = PageUtil.copyCommentToModel(pageComment,user);
				if(commentModel.hide){
					commentModel.body = messageService.getMessage("comment.body.hidden");
				}
				listModel.comments.add(commentModel);
			}
			listModel.perms = commentService.getCommentsPermissions(spaceUname,pageUuid);
			listModel.isSpaceAdmin = securityService.isAllowResourceAdmin(spaceUname,SecurityValues.RESOURCE_TYPES.SPACE
					,WikiUtil.getUser());
		} catch (CommentException e) {
			listModel.errorCode =  ErrorCode.COMMENT_GET_FAILED;
		}

		
		return listModel;
	}

	public CommentModel hideComment(Integer uid, boolean hide) {
		CommentModel model = new CommentModel();
		if(uid == null){
			model.errorCode = ErrorCode.COMMENT_HIDE_FAILED;
			return model;
		}
		PageComment comment = commentService.getComment(uid);
		if(comment == null){
			model.errorCode = ErrorCode.COMMENT_HIDE_FAILED;
			return model;
		}
		
		
		//comment author or space admin has the permission to hide
		//anonymous user comment only allow space admin to hide.
		if(comment.getModifier() == null || !comment.getModifier().equals(WikiUtil.getUser())){
			if(!securityService.isAllowResourceAdmin(comment.getPage().getSpace().getUnixName(),SecurityValues.RESOURCE_TYPES.SPACE
					,WikiUtil.getUser())){
				model.errorCode =  ErrorCode.COMMENT_NO_PERM_HIDE;
				return model;
			}
		}
		
		comment = commentService.hideComment(uid, hide);
		model.hide = comment.isHide();
		if(hide)
			//model.body + "<p><b>"+messageService.getMessage("comment.body.hidden")+"</b></p>";
			model.body = messageService.getMessage("comment.body.hidden");
		else
			model.body = comment.getBody();
		
		return model;
	}
	//********************************************************************
	//               set /get
	//********************************************************************
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	public void setCaptchaService(CaptchaServiceProxy captchaService) {
		this.captchaService = captchaService;
	}
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	public void setActivityLog(ActivityLogService activityLog) {
		this.activityLog = activityLog;
	}
	

}
