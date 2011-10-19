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
package com.edgenius.wiki.webapp.action;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.PortResolver;
import org.springframework.security.web.PortResolverImpl;

import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.core.model.User;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.service.UserService;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.model.Friend;
import com.edgenius.wiki.model.Invitation;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.FriendService;
import com.edgenius.wiki.service.InvitationException;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * URL: <code>invite.do?s=spaceUname&i=inviteUUID</code>
 * to allow invitation acceptor to accept that space reading invitation
 * 
 * @author Dapeng.Ni
 */

@SuppressWarnings("serial")
public class InviteAction extends BaseAction{
	private static final Logger log = LoggerFactory.getLogger(InviteAction.class);
	private static final String URL_INVITATION = "invitation";
	
	//param for friend's invitation
	private String s; //url parameters for spaceUname
	private String i; //url parameters for invitation Uuid
	
	//param for friendship request - add a space to my space friend list, then request view its members list permission.
	private String sender;
	private String receiver;
	private String action;
	
	//service
	private FriendService friendService;
	private MessageService messageService;
	private SecurityService securityService;
	
	private PortResolver portResolver = new PortResolverImpl();
	private SpaceService spaceService;
	
	public String execute(){
		
		Space space = spaceService.getSpaceByUname(s);
		if(space == null){
			log.warn("Space does not exist for invitation:{}", s);
			getRequest().setAttribute("err", messageService.getMessage("invite.space.not.exist",new String[]{s}));
			return URL_INVITATION;
		}
		Invitation invitation = friendService.getInvitation(s,i);
		if(invitation == null){
			log.warn("Invitation doesn't exist:{}:{}", s, i);
			getRequest().setAttribute("err", messageService.getMessage("invite.not.exist"));
			return URL_INVITATION;
		}
		
		User acceptor = WikiUtil.getUser();
		if(acceptor == null || acceptor.isAnonymous()){
			getRequest().setAttribute("btnStyle","anonymous");  //show login/signup or reject button
			getRequest().setAttribute("msg", messageService.getMessage("invite.accept.anonymous"
					,new String[]{invitation.getCreator().getFullname(),space.getName(),WikiConstants.APP_NAME}));
			return URL_INVITATION;
		}else{
			if(invitation != null && invitation.includeUserEmail(acceptor.getContact().getEmail())){
				//if user is already login and email just match invitation email, then let user know if he want to accept
				getRequest().setAttribute("btnStyle","correctUser"); //show accept or reject button
				getRequest().setAttribute("msg", messageService.getMessage("invite.accept.loginuser"
						,new String[]{invitation.getCreator().getFullname(),space.getName(),WikiConstants.APP_NAME,acceptor.getFullname()}));
				return URL_INVITATION;
			}else{
				//TODO: check Activity log to decide if this user already accept this invitation
				
				//login user has different email with current login user, or, this invitation is already accepted
				getRequest().setAttribute("btnStyle","wrongUser"); //show logout button
				getRequest().setAttribute("err", messageService.getMessage("invite.accepted.or.email.invalid"
						,new String[]{acceptor.getFullname(),acceptor.getContact().getEmail()}));
				return URL_INVITATION;
			}
		}

	}
	
	/**
	 * Accept invitation - user must already login
	 * @return
	 */
	public String accept() {
		try {
			
			Space space = spaceService.getSpaceByUname(s);
			if(space == null){
				log.warn("Space does not exist for invitation:{}", s);
				getRequest().setAttribute("err", messageService.getMessage("invite.space.not.exist",new String[]{s}));
				return URL_INVITATION;
			}
			Invitation invitation = friendService.getInvitation(s,i);
			if(invitation == null){
				log.warn("Invitation doesn't exist:{}:{}", s, i);
				getRequest().setAttribute("err", messageService.getMessage("invite.not.exist"));
				return URL_INVITATION;
			}
			
			User acceptor = WikiUtil.getUser();
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// check if this email still inside emailGroup, if not, it means this 
			//invitation already be accepted, don't need continue handle
			if(!invitation.includeUserEmail(acceptor.getContact().getEmail())){
				//invite does not exist
				getRequest().setAttribute("btnStyle","wrongUser"); //show logout button
				getRequest().setAttribute("err", messageService.getMessage("invite.accepted.or.email.invalid"
						,new String[]{acceptor.getFullname(),acceptor.getContact().getEmail()}));
				return URL_INVITATION;
			}
			
			friendService.acceptInvitation(acceptor, invitation);
			getRequest().setAttribute("msg", messageService.getMessage("invite.accepted"
					,new String[]{acceptor.getFullname(),WikiConstants.APP_NAME, WikiUtil.getPageRedirFullURL(s, null, null)}));
//			SavedRequest savedRequest = new SavedRequest(getRequest(), portResolver);
//			getSession().setAttribute(AbstractProcessingFilter.SPRING_SECURITY_SAVED_REQUEST_KEY, savedRequest);
		} catch (InvitationException e) {
			log.error("Inviation accept failed",e);
			getRequest().setAttribute("err", messageService.getMessage("invite.unexpect.error"));
			return URL_INVITATION;
		}
		return URL_INVITATION;
	}
	
	public String friendship(){
		User viewer = WikiUtil.getUser();
		
		if(securityService.isAllowResourceAdmin(sender, RESOURCE_TYPES.SPACE, viewer)){
			if(action != null){
				Friend frd = friendService.getFriendship(sender, receiver);
				if(frd != null && frd.isPending()){
					if(StringUtils.equalsIgnoreCase(action, FriendService.rejectFriendship)){
						friendService.rejectFriendship(sender,receiver);
						getRequest().setAttribute("msg", messageService.getMessage("friendship.rejected"));
					}else if(StringUtils.equalsIgnoreCase(action, FriendService.acceptFriendship)){
						friendService.acceptFriendship(sender,receiver);
						getRequest().setAttribute("msg", messageService.getMessage("friendship.accepted"));
					}
				}else{
					getRequest().setAttribute("msg", messageService.getMessage("friendship.processed"));
				}
			}
		}else{
			getRequest().setAttribute("msg", messageService.getMessage("friendship.no.permission"));
		}
		//view
		return "friendship";
	}
	//********************************************************************
	//               set /get
	//********************************************************************
	
	public void setFriendService(FriendService friendService) {
		this.friendService = friendService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public String getS() {
		return s;
	}
	public void setS(String s) {
		this.s = s;
	}
	public String getI() {
		return i;
	}
	public void setI(String i) {
		this.i = i;
	}

	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	
}
