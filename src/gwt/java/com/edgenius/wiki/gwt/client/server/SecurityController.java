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
package com.edgenius.wiki.gwt.client.server;

import java.util.ArrayList;

import com.edgenius.wiki.gwt.client.model.CaptchaCodeModel;
import com.edgenius.wiki.gwt.client.model.JsInfoModel;
import com.edgenius.wiki.gwt.client.model.PermissionListModel;
import com.edgenius.wiki.gwt.client.model.PermissionModel;
import com.edgenius.wiki.gwt.client.model.QuickNoteModel;
import com.edgenius.wiki.gwt.client.model.RoleListModel;
import com.edgenius.wiki.gwt.client.model.RoleModel;
import com.edgenius.wiki.gwt.client.model.UserListModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.model.UserProfileModel;

/**
 * System instance admin controller service
 * @author Dapeng.Ni
 */
public interface SecurityController extends RemoteService{
	String MODULE_ACTION_URI = "security.rpcs";

	//********************************************************************
	//               Function methods
	//********************************************************************

	PermissionListModel getResourcePermissions(int resourceTypeOrdinal, String resourceName);
	/**
	 * @param changedModelList, it has single element for each operation on each role/user. For example role A updated
	 * read and write permission, then this changedModelList will contain 2 element.
	 * 
	 */
	PermissionListModel updatePermission(int resourceTypeOrdinal,ArrayList<PermissionModel> changedModelList);


	/**
	 * 
	 * @param roleType -1: return all type of role. otherwise should be Role.TYPE_SYSTEM or Role.TYPE_SPACE
	 * @return
	 */
	RoleListModel getRoleList(int roleType);
	/**
	 * Add user list to role, if users already exist or any error, return null. otherwise return same roleUid with input.
	 * @param roleUid
	 * @param usernameList
	 * @return
	 */
	Integer addUsersToRole(int roleUid, ArrayList<String> usernameList);
	UserModel checkLogin(JsInfoModel jsModel);
	RoleModel saveRole(String name, String desc);
	
	UserModel getUser(Integer userUid);
	UserModel getUser(String username);
	/**
	 * Get a space group user list. This method will check senderSpaceUname if it has friendship with target view space.
	 * @param senderSpaceUname
	 * @param receiverSpaceUname
	 * @return
	 */
	UserListModel getSpaceGroupUsers(String senderSpaceUname, String receiverSpaceUname);
	String savePassword(Integer userUid,String password);
	UserModel saveProfile(UserModel model);
	
	/**
	 * 
	 * @param model
	 * @param login make this user login
	 * @return
	 */
	UserModel createUser(UserModel model,boolean login);
	
	UserProfileModel getUserProfile(String username);
	UserProfileModel getUserContributed(String username,int type);
	
	int sendForgetPassword(String email);
	
	int captchaValid(CaptchaCodeModel model);
	
	boolean saveUserStatus(Integer userUid, String text);
	
	QuickNoteModel saveUserQuickNote(String note);
	
	UserModel followUser(String username, boolean following);
}
