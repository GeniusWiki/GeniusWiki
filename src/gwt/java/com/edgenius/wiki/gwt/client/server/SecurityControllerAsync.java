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
import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * @author Dapeng.Ni
 */
public interface SecurityControllerAsync extends RemoteServiceAsync{
	void getResourcePermissions(int resourceTypeOrdinal, String resourceName, AsyncCallback<PermissionListModel> callback);
	void updatePermission(int resourceTypeOrdinal,ArrayList<PermissionModel> changedModelList, AsyncCallback<PermissionListModel> callback);
	void getRoleList(int roleType, AsyncCallback<RoleListModel> callback);
	void checkLogin(JsInfoModel jsModel, AsyncCallback<UserModel> async);
	void saveRole(String name, String desc, AsyncCallback<RoleModel> async);
	/**
	 * Get user by Uid. Return current login user if Uid is less or equal to 0.  
	 * This method will return full user contact information(personal contact, social link etc. from UserSettting.contacts) 
	 * @param userUid
	 * @param callback
	 */
	void getUser(Integer userUid,AsyncCallback<UserModel> callback);
	void getUser(String username, AsyncCallback<UserModel> callback);
	void getSpaceGroupUsers(String senderSpaceUname, String receiverSpaceUname, AsyncCallback<UserListModel> callback);
	void savePassword(Integer userUid,String password,AsyncCallback<String> callback);
	void saveProfile(UserModel model,AsyncCallback<UserModel> callback);
	void createUser(UserModel model, boolean login, AsyncCallback<UserModel> callback);

	void addUsersToRole(int roleUid, ArrayList<String> usernameList, AsyncCallback<Integer> callback);
	/**
	 * OK, the first parameter change back again(15/04/09), the very beginning is username, but I believed it has security 
	 * issue, at lease, looks not good as username(login ID) has to be displayed in URL. Then, I changed to userFullname,
	 * but finally, I found it is sucks idea - user fullname can be duplicated... So anyway, just display user name on URL, so what...
	 * 
	 * This method will return only public user contact information(social link etc. from UserSettting.contacts). Personal contact 
	 * won't display.
	 * 
	 * @param username
	 * @param callback
	 */
	void getUserProfile(String username,AsyncCallback<UserProfileModel> callback);
	/**
	 * @param username
	 * @param type SharedConstant. SPACE, PAGE, HISTORY, ACTIVITY, or 0(All)
	 * @param callback
	 */
	void getUserContributed(String username,int type, AsyncCallback<UserProfileModel> callback);
	/**
	 * @param text
	 * @param forgetPasswordForm
	 */
	void sendForgetPassword(String email, AsyncCallback<Integer> callback);
	/**
	 * @param model
	 * @param moveOrCopyCaptchaAsync
	 */
	void captchaValid(CaptchaCodeModel model, AsyncCallback<Integer> callback);
	/**
	 * @param userUid
	 * @param text
	 * @param asyncCallback
	 */
	void saveUserStatus(Integer userUid, String text, AsyncCallback<Boolean> asyncCallback);
	/**
	 * @param text
	 * @param quickNotePortlet
	 */
	void saveUserQuickNote(String text, AsyncCallback<QuickNoteModel> asyncCallback);
	/**
	 * @param username
	 * @param followAsync
	 */
	void followUser(String username, boolean following, AsyncCallback<UserModel> asyncCallback);

}
