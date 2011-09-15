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
package com.edgenius.wiki.gwt.client.offline.controller;

import java.util.ArrayList;

import com.allen_sauer.gwt.log.client.Log;
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
import com.edgenius.wiki.gwt.client.offline.OfflineUtil;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Dapeng.Ni
 */
public class SecurityOfflineControllerImpl  extends AbstractOfflineControllerImpl  implements SecurityControllerAsync {

	
	public void checkLogin(JsInfoModel jsModel, AsyncCallback<UserModel> async) {
		UserModel user = OfflineUtil.getUser();
		Log.debug("Check login user:" + user);
		
		OfflineUtil.setLoginInfo(user);
		async.onSuccess(user);
	}

	
	public void createUser(UserModel model, boolean login, AsyncCallback<UserModel> callback) {
		// TODO Auto-generated method stub

	}

	
	
	public void getResourcePermissions(int resourceTypeOrdinal, String resourceName,
			AsyncCallback<PermissionListModel> callback) {
		// TODO Auto-generated method stub

	}

	
	public void getRoleList(int roleType, AsyncCallback<RoleListModel> callback) {
		// TODO Auto-generated method stub

	}

	
	public void getSpaceGroupUsers(String senderSpaceUname, String receiverSpaceUname,
			AsyncCallback<UserListModel> callback) {
		// TODO Auto-generated method stub

	}

	
	public void getUser(Integer userUid, AsyncCallback<UserModel> callback) {
		// TODO Auto-generated method stub

	}

	
	public void getUser(String username, AsyncCallback<UserModel> callback) {
		// TODO Auto-generated method stub

	}

	
	
	public void savePassword(Integer userUid, String password, AsyncCallback<String> callback) {
		// TODO Auto-generated method stub

	}

	
	public void saveProfile(UserModel model, AsyncCallback<UserModel> callback) {
		// TODO Auto-generated method stub

	}

	
	public void saveRole(String name, String desc, AsyncCallback<RoleModel> async) {
		// TODO Auto-generated method stub

	}

	
	public void updatePermission(int resourceTypeOrdinal, ArrayList<PermissionModel> changedModelList,
			AsyncCallback<PermissionListModel> callback) {
		// TODO Auto-generated method stub

	}


	public void getUserProfile(String userFullname, AsyncCallback<UserProfileModel> callback) {
		// TODO Auto-generated method stub
		
	}


	public void sendForgetPassword(String password, AsyncCallback<Integer> callback) {
		// TODO Auto-generated method stub
		
	}


	public void addUsersToRole(int roleUid, ArrayList<String> usernameList, AsyncCallback<Integer> callback) {
		// TODO Auto-generated method stub
		
	}


	public void captchaValid(CaptchaCodeModel model, AsyncCallback<Integer> callback) {
		// TODO Auto-generated method stub
		
	}


	public void saveUserStatus(Integer userUid, String text, AsyncCallback<Boolean> asyncCallback) {
		// TODO Auto-generated method stub
		
	}


	public void saveUserQuickNote(String text, AsyncCallback<QuickNoteModel> asyncCallback) {
		// TODO Auto-generated method stub
		
	}


	public void followUser(String username, boolean following, AsyncCallback<UserModel> asyncCallback) {
		// TODO Auto-generated method stub
		
	}


	public void getUserContributed(String username, int type, AsyncCallback<UserProfileModel> callback) {
		// TODO Auto-generated method stub
		
	}

}
