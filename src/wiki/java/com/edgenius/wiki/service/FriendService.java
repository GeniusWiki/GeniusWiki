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
package com.edgenius.wiki.service;

import java.util.List;

import com.edgenius.core.model.Resource;
import com.edgenius.core.model.User;
import com.edgenius.wiki.model.Friend;
import com.edgenius.wiki.model.Invitation;

/**
 * @author Dapeng.Ni
 */
public interface FriendService {
	String SERVICE_NAME="friendService";
	

	static final String rejectFriendship = "reject";
	static final String acceptFriendship = "accept";
	
	/**
	 * Check if any role in the given roleList is new added role for this space. If so, need check: <br> 
	 * if role type is Role.TYPE_SPACE then, <br>
	 * if this role is not friends with given space yet <br>
	 * send out "make friend" request to given space admin
	 * 
	 * This method must be call before space resource updated to latest new role
	 * @param spaceUname
	 * @param roleList new role name list from security update panel. It may contain SYSTEM role, skip them.
	 */
	 void makeFriendsWithSpaceGroups(String spaceUname, List<String> roleNameList);

	 /**
	  * If space add any new user to allow read/write space, then this user need added to this space role.
	  * Likewise, the removed user from space security panel, also need removed from this space role.
	  * 
	  * Update space group role with latest space users. 
	  * @param resource, must be updated resource
	  */
	void refreshSpaceGroupUsers(Resource resource);
	

	Friend getFriendship(String senderSpaceUname, String receiverSpaceUname);
	//need check if this requestSpaceUname has friendship to view given space group's user list before call this method 
	List<User> getSpaceGroupUsers(String spaceUname);

	/**
	 * @param actionsParams
	 * @param msg 
	 */
	boolean acceptFriendship(String sender, String receiver);

	/**
	 * @param actionsParams
	 * @param msg 
	 */
	boolean rejectFriendship(String sender, String receiver);

	//********************************************************************
	// invitation system: space admin send email to someone, 
	// someone use invite.do?s=spaceUname&i=inviteUUID to accept this invitation
	/**
	 * @param toEmailGroup, multiple email separator by "," or ";"
	 * @return valid email group separator by ","
	 */
	String sendInvitation(User sender,String spaceUname, String toEmailGroup, String message);
	
	/**
	 * Current policy is, space admin send invitation to some email address. the receiver need sign-up/login to accept
	 * invitation. If a new register user, he/she must use same email address with invitation request's
	 * If invitation is accepted, this user will assign space read permission. 
	 * Once invitation is accepted, the same invitation cannot be use again(redirect to error page).
	 * 
	 * @param acceptor
	 * @param invitation
	 * @throws InvitationException
	 */
	void acceptInvitation(User acceptor,Invitation invitation) throws InvitationException;

	/**
	 * @param s
	 * @param i
	 * @return
	 */
	Invitation getInvitation(String spaceUname, String invitationUuid);

	/**
	 * Remove 3 days old invitations - hardcode for 72 hours.
	 */
	void removeExpiredInvitations(int hours);
}
