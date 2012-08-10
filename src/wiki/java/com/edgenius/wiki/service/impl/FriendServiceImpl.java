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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.Constants;
import com.edgenius.core.Constants.SUPPRESS;
import com.edgenius.core.Global;
import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.core.UserSetting;
import com.edgenius.core.dao.ResourceDAO;
import com.edgenius.core.model.Permission;
import com.edgenius.core.model.Resource;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.service.MailService;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.service.RoleService;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.service.UserService;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.dao.FriendDAO;
import com.edgenius.wiki.dao.InvitationDAO;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Friend;
import com.edgenius.wiki.model.Invitation;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.FriendService;
import com.edgenius.wiki.service.InvitationException;
import com.edgenius.wiki.service.NotificationService;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.util.WikiUtil;
import com.edgenius.wiki.widget.SpaceWidget;

/**
 * @author Dapeng.Ni
 */
@Transactional
public class FriendServiceImpl implements FriendService {

	private static final Logger log = LoggerFactory.getLogger(FriendServiceImpl.class);

	private ResourceDAO resourceDAO; 
	private FriendDAO friendDAO;
	private InvitationDAO invitationDAO;
	
	private RoleService roleService;
	private NotificationService notificationService;
	private SecurityService securityService;
	private UserReadingService userReadingService;
	private UserService userService;
	private SpaceService spaceService;
	private MessageService messageService;
	private MailService mailService;
	
	public void refreshSpaceGroupUsers(Resource resource) {
		if(resource == null || resource.getType() != RESOURCE_TYPES.SPACE){
			log.info("Resource is null or wrong type." + resource);
			return;
		}
		
		//get this space has how much users on any permission
		Map<String, User> availUserMap = new HashMap<String, User>();
		Set<Permission> perms = resource.getPermissions();
		if(perms != null){
			for (Permission perm : perms) {
				Set<User> users = perm.getUsers();
				if(users != null){
					for (User user : users) {
						availUserMap.put(user.getUsername(),user);
					}
				}
			}
		}
		
		//then, refresh its originally space group(role) 
		String spaceUname = resource.getResource();
		Role spaceGroup = roleService.getRoleByName(Role.SPACE_ROLE_PREFIX+spaceUname);
		Set<User> origUsers = spaceGroup.getUsers();
		Collection<User> availUsers = availUserMap.values();
		boolean dirty = false;
		for (Iterator<User> iter = origUsers.iterator(); iter.hasNext();) {
			User origUser = iter.next();
			boolean found = false;
			for (Iterator<User> newIter = availUsers.iterator(); newIter.hasNext();) {
				User newUser = newIter.next();
				if(origUser.equals(newUser)){
					found = true;
					newIter.remove();
					break;
				}
			}
			if(!found){
				//removed user from new list
				dirty = true;
				//remove current role from this user
				origUser.getRoles().remove(spaceGroup);
				iter.remove();
			}
		}
		if(availUsers.size() > 0){
			//new added users from new list
			dirty = true;
			origUsers.addAll(availUsers);
		}
		if(dirty){
			for (User user : origUsers) {
				user.getRoles().add(spaceGroup);
			}
			//update space group(role) with new users list
			roleService.saveRole(spaceGroup);
		}
		
	}
	public void makeFriendsWithSpaceGroups(String spaceUname, List<String> roleNameList) {
		if(roleNameList == null || roleNameList.size() == 0){
			log.info("No new role updated in given input." );
			return;
		}
		
		//skip all non-space role, such as system role
		for (Iterator<String> iter = roleNameList.iterator();iter.hasNext();) {
			String roleName = iter.next();
			if(!roleName.startsWith(Role.SPACE_ROLE_PREFIX))
				iter.remove();
		}
		if(roleNameList.size() == 0){
			log.info("All roles is system role, no need continue to check friendship." );
			return;
		}
		
		//get all available roles(type must be ROLE.TYPE_SPACE) for this space currently.
		List<String> availRoleList = new ArrayList<String>();
		Resource res = resourceDAO.getByName(spaceUname);
		Set<Permission> perms = res.getPermissions();
		if(perms != null){
			for (Permission perm : perms) {
				Set<Role> roles = perm.getRoles();
				if(roles != null){
					for (Role role : roles) {
						if(role.getType() != Role.TYPE_SPACE)
							continue;
						availRoleList.add(role.getName());
					}
				}
	
			}
		}
		
		//filter out which role is new added
		List<String> newRoleList = new ArrayList<String>();
		for(String name: roleNameList){
			boolean found = false;
			for (String exist: availRoleList) {
				if(StringUtils.equalsIgnoreCase(exist, name)){
					found = true;
					break;
				}
			}
			if(!found){
				newRoleList.add(name);
			}
		}
		if(newRoleList.size() == 0){
			log.info("Given new role list does not contain any new added role for space " + spaceUname);
		}else{
			//OK, find new added role for this space. check if it is in friends list already
			int prefixLen = Role.SPACE_ROLE_PREFIX.length();
			for (String newSpace : newRoleList) {
				//get space name from 
				newSpace = newSpace.substring(prefixLen);
				Friend frd = friendDAO.getFriendship(Friend.PREFIX_SPACE+spaceUname, Friend.PREFIX_SPACE+newSpace);
				if(frd != null && frd.isConfirmed()){
					continue;
				}
				if(frd == null){
					//CREATE Pending friendship
					frd = new Friend();
					frd.setSender(Friend.PREFIX_SPACE+spaceUname);
					frd.setReceiver(Friend.PREFIX_SPACE+newSpace);
					WikiUtil.setTouchedInfo(userReadingService, frd);
					frd.setStatus(Friend.STATUS_PENDING);
					friendDAO.saveOrUpdate(frd);
					
					//send message
					String text = "Space " + spaceUname + " adds your space "+ newSpace +" on its friend list.";
//							"{action:id="+rejectFriendship+ "|title=reject|confirmMessage=Do you want to reject this request?|" +
//							"sender="+spaceUname+"|receiver="+newSpace
//							+"} or {action:id="+acceptFriendship+"|title=accept|confirmMessage=Do you want to accept this request?|" +
//							"sender="+spaceUname+"|receiver="+newSpace+"}";

					notificationService.sendMessage(text,SharedConstants.MSG_TARGET_SPACE_ADMIN_ONLY ,newSpace,NotificationService.SEND_MAIL_ONLY_HAS_RECEIVERS);
					
					//send request email
					Set<String> userMails =userReadingService.getSpaceAdminMailList(newSpace);
					for (String mail : userMails) {
						try {
							SimpleMailMessage msg = new SimpleMailMessage();
							msg.setFrom(Global.DefaultNotifyMail);
							Map<String,Object> map = new HashMap<String,Object>();
							map.put("sender", spaceUname);
							map.put("receiver", newSpace);
							map.put(WikiConstants.ATTR_PAGE_LINK, WebUtil.getHostAppURL()
								+"invite!friendship.do?sender="+URLEncoder.encode(spaceUname, Constants.UTF8)
								+"&receiver="+URLEncoder.encode(newSpace, Constants.UTF8));
							msg.setTo(mail); 
							mailService.sendPlainMail(msg, WikiConstants.MAIL_TEMPL_FRIENDSHIP, map);
						} catch (Exception e) {
							log.error("Failed send friendship email:" + mail,e);
						}
					}
					
				}
				//reject,pending do nothing
			}
		}
		
	}
	public boolean acceptFriendship(String sender, String receiver) {
		
		Friend frd = friendDAO.getFriendship(Friend.PREFIX_SPACE+sender, Friend.PREFIX_SPACE+receiver);
		if(frd == null){
			frd = new Friend();
			frd.setSender(Friend.PREFIX_SPACE+sender);
			frd.setReceiver(Friend.PREFIX_SPACE+receiver);
			WikiUtil.setTouchedInfo(userReadingService, frd);
			AuditLogger.warn("Unexpected case: confirmed friendship without pending status.Sender:" + sender + ".Receiver:" + receiver);
			return false;
		}
		frd.setStatus(Friend.STATUS_CONFIRMED);
		friendDAO.saveOrUpdate(frd);
		return true;
		//also put response message to notification
		//notificationService.sendMessage("Space " + receiver + " allows ",SharedConstants.MSG_TARGET_SPACE_ADMIN_ONLY , receiver ,false);
	}

	public boolean rejectFriendship(String sender, String receiver) {

		Friend frd = friendDAO.getFriendship(Friend.PREFIX_SPACE+sender, Friend.PREFIX_SPACE+receiver);
		if(frd == null){
			frd = new Friend();
			frd.setSender(Friend.PREFIX_SPACE+sender);
			frd.setReceiver(Friend.PREFIX_SPACE+receiver);
			WikiUtil.setTouchedInfo(userReadingService, frd);
			AuditLogger.warn("Unexpected case: rejected friendship without pending status.Sender:" + sender + ".Receiver:" + receiver);
			return false;
		}
		frd.setStatus(Friend.STATUS_REJECTED);
		friendDAO.saveOrUpdate(frd);
		return true;
		//also put response message to notification
		//notificationService.sendResponse(msg, "<Rejected>",false);
		
	}


	public Friend getFriendship(String senderSpaceUname, String receiverSpaceUname){
		return getFriend(senderSpaceUname, receiverSpaceUname);
		
	}
	//need check if this requestSpaceUname has friendship to view given space group's user list before call this method 
	public List<User> getSpaceGroupUsers(String spaceUname){
		Role roles = roleService.getRoleByName(Role.SPACE_ROLE_PREFIX+spaceUname);
		
		return new ArrayList<User>(roles.getUsers());
	}
	
	public Invitation getInvitation(String spaceUname, String invitationUuid) {
		return invitationDAO.getByUuid(spaceUname,invitationUuid);
	}

	public void acceptInvitation(User acceptor, Invitation invitation) throws InvitationException {
		
		String spaceUname = invitation.getSpaceUname(); 
		//do following:
		//* allocate read permission this invited user to space
		//* send notification to this space (admin)
		//* remove this acceptor from invitation email group, if email group is zero, delete this invitation record from DB
		//* Add this space to user's dashboard
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               Step 1
		//get this space reading permission then try to figure out if this user has read permission or not
		Resource resource = resourceDAO.getByName(spaceUname);
		Permission readPerm = null;
		Set<Permission> perms = resource.getPermissions();
		for (Permission perm : perms) {
			if(OPERATIONS.READ.equals(perm.getOperation())){
				readPerm = perm;
				break;
			}
		}
		
		//check if this user has read permission or not. if no, need allow this user read.
		boolean alreadyHasPerm = false;
		if(readPerm != null){
			Set<User> users = readPerm.getUsers();
			for (User user : users) {
				if(user.equals(acceptor)){
					//OK, this space already contain this user who has read permission!
					alreadyHasPerm = true;
					break;
				}
			}
			if(!alreadyHasPerm){
				//if this user does not have read permission, then add it.
				readPerm.getUsers().add(acceptor);
				acceptor.getPermissions().add(readPerm);
				userService.updateUser(acceptor);
				
				//update space group: add this user to space group 
				refreshSpaceGroupUsers(resource);
				//reset space permission cache
				securityService.resetSpaceReadingCache(spaceUname);
				//let this use access this space
				securityService.resetPolicyCache(RESOURCE_TYPES.SPACE,spaceUname);
			}
		}else{
			//failure tolerance, as space must have Read-Permission in table as this permission is create at space creating.
			AuditLogger.error("Unexpected case: Space read permission does not exist!");
			throw new InvitationException("Unexpected case: Space read permission does not exist!");
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               Step 2
		//send notification to inviter
		//if this user is new added, then send email as well.
		String message = messageService.getMessage(WikiConstants.I18N_INVITE_ACCEPT,new String[]{acceptor.getFullname(),acceptor.getContact().getEmail()});

		log.info("Invitation to {} is acceptted and notify message will send out", acceptor.getFullname());
		notificationService.sendMessage(message,SharedConstants.MSG_TARGET_SPACE_ADMIN_ONLY,spaceUname, NotificationService.SEND_MAIL_ONLY_HAS_RECEIVERS);

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               Step 3
		//delete current invitation;
		StringBuffer newGroup = new StringBuffer(); 
		//remove accepted from exist email groups
		boolean noMoreEmail = true;
		for (String email : invitation.getToEmailGroup().split(",")) {
			if(!StringUtils.isBlank(email) && !StringUtils.equalsIgnoreCase(email, acceptor.getContact().getEmail())){
				newGroup.append(email).append(",");
				noMoreEmail = false;
			}
		}
		//OK, in this invite group, all email address accept invite, then delete it from DB
		if(noMoreEmail)
			invitationDAO.removeObject(invitation);
		else{
			invitation.setToEmailGroup(newGroup.toString());
			invitationDAO.saveOrUpdate(invitation);
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //               Step 4
		//if user dashboard does not have this space yet, add it.
		UserSetting setting = acceptor.getSetting();
		if(!setting.hasWidgetAtHomelayout(SpaceWidget.class.getName(), spaceUname)){
		    setting.addWidgetToHomelayout(SpaceWidget.class.getName(), spaceUname);
		}
	}

	public List<String> sendInvitation(User sender, String spaceUname, String toEmailGroup, String message) {
		
		String[] emails = toEmailGroup.split("[;,]");
		//check email, and only save valid email to database
		if(emails == null || emails.length == 0){
			//no valid email address
			return null;
		}
		List<String> validEmails = new ArrayList<String>();
		for (String email : emails) {
			if (email.trim().matches("^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$")) {
				validEmails.add(email);
			}else{
				log.warn("Invalid email in friend invite email group, this email will be ignore:" + email);
			}
		}
		if(validEmails.size() == 0){
			//no valid email address
			log.warn("Email group does not include invalid email, invitation cancelled: " + toEmailGroup);
			return null;
		}
		Invitation invite = new Invitation();
		invite.setCreatedDate(new Date());
		invite.setCreator(sender);
		
		invite.setMessage(message);
		invite.setSpaceUname(spaceUname);
		invite.setUuid(RandomStringUtils.randomAlphanumeric(WikiConstants.UUID_KEY_SIZE).toLowerCase());
		
		
		//send mail
		Space space = spaceService.getSpaceByUname(spaceUname);
		message = StringUtils.isBlank(message)?messageService.getMessage(WikiConstants.I18N_INVITE_MESSAGE):message; 
		Map<String,Object> map = new HashMap<String,Object>();
		map.put(WikiConstants.ATTR_USER, sender);
		map.put(WikiConstants.ATTR_SPACE, space);
		
		map.put(WikiConstants.ATTR_INVITE_MESSAGE,message);
		//space home page
		String url = WikiUtil.getPageRedirFullURL(spaceUname,null,null);
		map.put(WikiConstants.ATTR_PAGE_LINK, url);
		
		List<String> validGroup = new ArrayList<String>();
		for (String email: validEmails) {
			try {
			    if(Global.hasSuppress(SUPPRESS.SIGNUP)){
    			    User user = userReadingService.getUserByEmail(email);
    			    if(user == null){
    			        //only unregistered user to be told system is not allow sign-up
    			        map.put(WikiConstants.ATTR_SIGNUP_SUPRESSED, true);
    			    }
			    }else{
			        map.put(WikiConstants.ATTR_SIGNUP_SUPRESSED, false);
			    }
			    
				map.put(WikiConstants.ATTR_INVITE_URL, WebUtil.getHostAppURL()+"invite.do?s="
						+URLEncoder.encode(spaceUname,Constants.UTF8)+"&i="+invite.getUuid());
				SimpleMailMessage msg = new SimpleMailMessage();
				msg.setFrom(Global.DefaultNotifyMail);
				msg.setTo(email); 
				mailService.sendPlainMail(msg, WikiConstants.MAIL_TEMPL_INVITE, map);
				validGroup.add(email);
			} catch (Exception e) {
				log.error("Failed send email to invite " + email,e);
			}
		}
		invite.setToEmailGroup(validGroup.toString());
		invitationDAO.saveOrUpdate(invite);
		
		//if system public signup is disabled, then here need check if that invited users are register users, if not, here need send email to notice system admin to add those users.
		if(Global.hasSuppress(SUPPRESS.SIGNUP)){
		    List<String> unregistereddEmails = new ArrayList<String>();
		    for (String email : validGroup) {
                User user = userReadingService.getUserByEmail(email);
                if(user == null){
                    unregistereddEmails.add(email);
                }
            }
		    
		    if(!unregistereddEmails.isEmpty()){
    		    //send email to system admin.
    		    map = new HashMap<String,Object>();
    		    map.put(WikiConstants.ATTR_USER, sender);
    	        map.put(WikiConstants.ATTR_SPACE, space);
    	        map.put(WikiConstants.ATTR_LIST, unregistereddEmails);
    	        mailService.sendPlainToSystemAdmins(WikiConstants.MAIL_TEMPL_ADD_INVITED_USER,map);
		    }
		}
		return validGroup;
	}
	
	public void removeExpiredInvitations(int hours) {
		//remove 72 hours old invitations
		invitationDAO.removeOldInvitations(hours);
	}

	//********************************************************************
	//               private
	//********************************************************************

	/**
	 * @param senderSpaceUname
	 * @param receiverSpaceUname
	 * @return
	 */
	private Friend getFriend(String senderSpaceUname, String receiverSpaceUname) {
		Friend frd = friendDAO.getFriendship(Friend.PREFIX_SPACE+ senderSpaceUname,Friend.PREFIX_SPACE+ receiverSpaceUname);
		return frd;
	}
	//********************************************************************
	//               set / get 
	//********************************************************************
	public void setResourceDAO(ResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}
	public void setFriendDAO(FriendDAO friendDAO) {
		this.friendDAO = friendDAO;
	}
	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}
	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}

	public void setInvitationDAO(InvitationDAO invitationDAO) {
		this.invitationDAO = invitationDAO;
	}
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}
	
	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

}
