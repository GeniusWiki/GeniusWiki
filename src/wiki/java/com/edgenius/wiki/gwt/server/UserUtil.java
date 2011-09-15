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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.cxf.common.util.StringUtils;

import com.edgenius.core.Global;
import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.UserSetting;
import com.edgenius.core.model.Contact;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.util.DateUtil;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.RoleModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;

/**
 * @author Dapeng.Ni
 */
public class UserUtil {

	private static final LinkedHashMap<String,String> CONTACTS_GROUPS = new LinkedHashMap<String, String>();
	static{
		//EMAIL is skipped as default
		CONTACTS_GROUPS.put(UserSetting.CONTACT_PHONE_NAME, "Contact"); //hardcode "Contact"
		CONTACTS_GROUPS.put(UserSetting.CONTACT_IM_NAME, "Contact");
		CONTACTS_GROUPS.put(UserSetting.CONTACT_WEBSITE_NAME, "Contact");
		
		CONTACTS_GROUPS.put(UserSetting.CONTACT_TWITTER_NAME, "Social"); //hardcode "Social"
		CONTACTS_GROUPS.put(UserSetting.CONTACT_FACEBOOK_NAME, "Social");
		CONTACTS_GROUPS.put(UserSetting.CONTACT_LINKEDIN_NAME, "Social");
		
		
	}

	public static void copyContactsToModel(User user, UserModel model, boolean forPrivate, MessageService messageService) {
		//There are some tricks here:
		//1) If a contact has "linked" attribute, that needs to pass to client as an indicator image will be displayed.
		//   For example, Twitter_linked properties pair will pass to client.
		//2) Browser side needs a full list of accounts even it is empty value. For example, user doesn't input facebook.
		//   However, for display purpose, we also pass "facebook":"" to browser.
		//3) For historic design issue, some contact is put into directly user.getContact()
		
		model.setContacts(new LinkedHashMap<String, LinkedHashMap<String,String>>());
		
		String groupName = "Contact";
		LinkedHashMap<String, String> contactsMap = new LinkedHashMap<String, String>();
		model.getContacts().put(messageService.getMessage(groupName.toLowerCase(),null,groupName), contactsMap); 
		
		//default - email.
		if(forPrivate){
			//I prefer to hide email for public at this moment - will be totally move to configuration files later.
			contactsMap.put(messageService.getMessage(UserSetting.CONTACT_EMAIL_NAME.toLowerCase(),null,UserSetting.CONTACT_EMAIL_NAME)
					,user.getContact().getEmail());
		}
		
		LinkedHashMap<String, LinkedHashMap<String, String>> map = user.getSetting().getContacts();
		LinkedHashMap<String, String> props;
		for (Entry<String,String> groups : CONTACTS_GROUPS.entrySet()) {
			String key = groups.getKey();
			if(!groupName.equals(groups.getValue())){
				//a new group
				groupName = groups.getValue();
				contactsMap = new LinkedHashMap<String, String>();
				
				//get group i18n name, default by groupName, like contact, social etc.
				model.getContacts().put(messageService.getMessage(groupName.toLowerCase(),null,groupName), contactsMap); 
			}
			props = null;
			if(map != null){
				props = map.get(key);
			}
			if(props != null){
				contactsMap.put(messageService.getMessage(key.toLowerCase(),null,key),props.get(UserSetting.PROP_ACCOUNT));
//				//for public, they don't need to know if this users link to some services(such as, don't need tell my account is linked to twitter)
//				if(forPrivate && UserSetting.CONTACT_TWITTER_NAME.equals(key)){ //at present, only twitter support link
//					contactsMap.put(key+"_linked",Boolean.valueOf((BooleanUtils.toBoolean(props.get(UserSetting.PROP_LINKED)))).toString());
//				}
			}else{
				//always put value so that display an empty title in browser
				contactsMap.put(messageService.getMessage(key.toLowerCase(),null,key),null);
//				if(forPrivate && UserSetting.CONTACT_TWITTER_NAME.equals(key)){
//					contactsMap.put(key+"_linked", null);
//				}
			}
		}
		
	}
	/**
	 * Same with copyModelToUser, but profile does not update username,password and uid
	 * @param model
	 * @param user
	 */
	public static void copyProfileToUser(UserModel model, User user) {
		if(model == null)
			return;
		
		Contact contact = user.getContact();
		if(contact == null){
			contact = new Contact();
		}
		contact.setEmail(model.getEmail());
		user.setContact(contact);
		user.setFullname(model.getFullname());
		
		LinkedHashMap<String, LinkedHashMap<String, String>> settingContacts = user.getSetting().getContacts();
		if(settingContacts == null){
			settingContacts = new LinkedHashMap<String, LinkedHashMap<String,String>>();
			user.getSetting().setContacts(settingContacts);
		}
		LinkedHashMap<String, LinkedHashMap<String, String>> map = model.getContacts();
		//assume only 1 group from client, i.e., map only has one key-value pair.
		for(LinkedHashMap<String, String> props : map.values()){
			for (Entry<String, String> entry : props.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				
				String propName = UserSetting.PROP_ACCOUNT;
				if(key.endsWith("_linked")){
					key = key.substring(0,key.length()-7);
					propName = UserSetting.PROP_LINKED;
				}
				LinkedHashMap<String, String> type = settingContacts.get(key);
				if(type == null){
					settingContacts.put(key, new LinkedHashMap<String, String>());
					type = settingContacts.get(key);
				}
				if(StringUtils.isEmpty(value)){
					type.remove(propName);
					if(type.size() == 0){
						settingContacts.remove(key);
					}
				}else{
					type.put(propName,value);
				}
				
			}
		}
	}

	
	public static UserModel copyUserToModel(User viewer, User user) {
		UserModel userModel = new UserModel();
		if(user == null)
			return userModel;
		
		if(user != null && !user.isAnonymous())
			userModel.setEmail(user.getContact().getEmail());
		
		userModel.setFullname(user.getFullname());
		userModel.setLoginname(user.getUsername());
		userModel.setUid(user.getUid());
		userModel.setPortrait(getPortraitUrl(user.getPortrait()));
		
		//some case, createDate is useless, so just skip it
		if(viewer != null){
			userModel.setCreateDate(DateUtil.getLocalDate(viewer, user.getCreatedDate()));
		}
		int[] permissions = new int[10];
		List<OPERATIONS> perms = user.getWikiPermissions();
		if(perms != null){
			for (OPERATIONS perm : perms) {
				permissions[perm.ordinal()] = 1;
			}
		}
		userModel.setPermissions(permissions);
		Set<Role> roles = user.getRoles();
		ArrayList<RoleModel> roleModels = new ArrayList<RoleModel>();
		for (Role role : roles) {
			roleModels.add(copyRoleToModel(role));
		}
		userModel.setRoles(roleModels);
		
		//skip password
		userModel.setDisable(user.isAccountLocked());
		
		userModel.setServerTime(System.currentTimeMillis());
		userModel.setDelaySyncMin(Global.DelayOfflineSyncMinutes);
		userModel.setOfflineDBVersion(WikiConstants.offlineDBVersion);
		userModel.setOfflineMainDBVersion(WikiConstants.offlineMainDBVersion);
		userModel.setSuppress(Global.getCurrentSuppress());

		return userModel;
	}

	public static  void copyModelToUser(UserModel model, User user) {
			if(model == null)
				return;
			
			Contact contact = user.getContact();
			if(contact == null){
				contact = new Contact();
			}
			contact.setEmail(model.getEmail());
			user.setContact(contact);
			user.setFullname(model.getFullname());

			//only set when a user register
			user.setUsername(model.getLoginname());
			user.setPassword(model.getPassword());
			user.setUid(model.getUid());
			
	}

	/**
	 * @param role
	 * @return
	 */
	public static RoleModel copyRoleToModel(Role role) {
		RoleModel model = new RoleModel();
		model.setUid(role.getUid());
		model.setName(role.getName());
		model.setDisplayName(role.getDisplayName());
		model.setDesc(role.getDescription());
		model.setType(role.getType());
		return model;
	}

	public static String getPortraitUrl(String portraitNodeUuid) {
		String imgUrl;
		if (portraitNodeUuid == null || portraitNodeUuid.trim().length() == 0) {
			// return default user portrait
			imgUrl = WebUtil.getWebConext() + "static/images/"+SharedConstants.NO_PORTRAIT_IMG;
		} else {
			imgUrl = WebUtil.getWebConext() + "download?portrait=" + portraitNodeUuid;
		}
		return imgUrl;
	}



}
