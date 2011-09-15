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
package com.edgenius.wiki.gwt.client.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.edgenius.wiki.gwt.client.server.utils.StringUtil;

/**
 * @author Dapeng.Ni
 */
public class UserModel extends CaptchaCodeModel{
	
	private Integer uid;
	private String loginname = null;
	private String fullname = null;
	private String password = null;
	private String email = null;
	//Key: title bar(e.g., Personal, Social, Company etc.)
	//ValueMap: Key: name(i18n, e.g. twitter, email, website etc.)  Value:info
	private LinkedHashMap<String, LinkedHashMap<String, String>> contacts;
	
	private long createDate;
	private long modifiedDate;
	//identify if this user is current login user;
	private boolean login;
	private boolean disable;
	private String portrait;
	private String redirUrl;
	//value combination from SharedConstants.SUPPRESS_*
	private int suppress;
	
	//1: yes, 0: false, -1: yourself or anonymous(not be able to follow)
	private int following;
	
	private ArrayList<RoleModel> roles;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//only for profile
	private String status;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Fields for Offline function, so far only initialize while SecurityController.checkLogin();
	private long serverTime;
	private int delaySyncMin;
	private int offlineDBVersion;
	private int offlineMainDBVersion;
	
	
	//user permission: only includes instance permission. refer to WikiOPERATIONS.INSTANCE_*
	private int[] permissions = new int[10];
	
	public boolean equals(Object obj){
		if(obj instanceof UserModel){
			UserModel model = (UserModel) obj;
			if(model.loginname.equalsIgnoreCase(this.loginname))
				return true;
			else
				return false;
		}
		return false;
	}
	public int hashCode(){
		return this.loginname == null?0:this.loginname.toUpperCase().hashCode();
	}
	public String toString(){
		return "Login name:" + loginname;
	}

	public int[] getPermissions() {
		return permissions;
	}
	public void setPermissions(int[] permissions) {
		this.permissions = permissions;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = StringUtil.trim(email);
	}
	public String getLoginname() {
		return loginname;
	}
	public void setLoginname(String loginname) {
		this.loginname = StringUtil.trim(loginname);
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = StringUtil.trim(fullname);
	}
	public Integer getUid() {
		return uid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public boolean isLogin() {
		return login;
	}
	public LinkedHashMap<String, LinkedHashMap<String, String>> getContacts() {
		return contacts;
	}
	public void setContacts(LinkedHashMap<String, LinkedHashMap<String, String>>  contacts) {
		this.contacts = contacts;
	}
	public void setLogin(boolean login) {
		this.login = login;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = StringUtil.trim(password);
	}
	public boolean isDisable() {
		return disable;
	}
	public void setDisable(boolean disable) {
		this.disable = disable;
	}
	public void setRoles(ArrayList<RoleModel> roles) {
		this.roles = roles;
	}

	public List<RoleModel> getRoles() {
		return roles;
	}


	public String getPortrait() {
		return portrait;
	}


	public int getFollowing() {
		return following;
	}
	public void setFollowing(int following) {
		this.following = following;
	}
	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}


	public String getRedirUrl() {
		return redirUrl;
	}


	public void setRedirUrl(String redirUrl) {
		this.redirUrl = redirUrl;
	}


	public long getCreateDate() {
		return createDate;
	}


	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}
	
	public long getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(long modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public int getDelaySyncMin() {
		return delaySyncMin;
	}
	public void setDelaySyncMin(int delaySyncMin) {
		this.delaySyncMin = delaySyncMin;
	}

	public int getOfflineDBVersion() {
		return offlineDBVersion;
	}
	public void setOfflineDBVersion(int offlineDBVersion) {
		this.offlineDBVersion = offlineDBVersion;
	}
	public int getOfflineMainDBVersion() {
		return offlineMainDBVersion;
	}
	public void setOfflineMainDBVersion(int offlineMainDBVersion) {
		this.offlineMainDBVersion = offlineMainDBVersion;
	}

	public long getServerTime() {
		return serverTime;
	}
	public void setServerTime(long serverTime) {
		this.serverTime = serverTime;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	public int getSuppress() {
		return suppress;
	}
	public void setSuppress(int suppress) {
		this.suppress = suppress;
	}
}
