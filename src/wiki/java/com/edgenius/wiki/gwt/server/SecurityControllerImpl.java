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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.captcha.CaptchaServiceProxy;

import com.edgenius.core.Constants;
import com.edgenius.core.Constants.REGISTER_METHOD;
import com.edgenius.core.Global;
import com.edgenius.core.SecurityValues;
import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.core.SecurityValues.SYSTEM_ROLES;
import com.edgenius.core.UserSetting;
import com.edgenius.core.UserSetting.QuickNote;
import com.edgenius.core.model.Permission;
import com.edgenius.core.model.Resource;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.service.MailService;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.service.RoleService;
import com.edgenius.core.service.UserExistsException;
import com.edgenius.core.service.UserOverLimitedException;
import com.edgenius.core.util.CodecUtil;
import com.edgenius.core.util.DateUtil;
import com.edgenius.core.util.ServletUtils;
import com.edgenius.core.util.TimeZoneUtil;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.ActivityModel;
import com.edgenius.wiki.gwt.client.model.CaptchaCodeModel;
import com.edgenius.wiki.gwt.client.model.JsInfoModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.PermissionListModel;
import com.edgenius.wiki.gwt.client.model.PermissionModel;
import com.edgenius.wiki.gwt.client.model.QuickNoteModel;
import com.edgenius.wiki.gwt.client.model.RoleListModel;
import com.edgenius.wiki.gwt.client.model.RoleModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.UserListModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.model.UserProfileModel;
import com.edgenius.wiki.gwt.client.server.SecurityController;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.handler.GWTSpringController;
import com.edgenius.wiki.model.ActivityLog;
import com.edgenius.wiki.model.Friend;
import com.edgenius.wiki.model.History;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.security.UserSignUpDiabledException;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.ActivityLogService;
import com.edgenius.wiki.service.FriendService;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.SecurityDummy;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.ThemeService;
import com.edgenius.wiki.service.TouchService;
import com.edgenius.wiki.util.WikiUtil;


/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class SecurityControllerImpl  extends GWTSpringController implements SecurityController{
	private static final Logger log = LoggerFactory.getLogger(SecurityControllerImpl.class);
	private static final int OWNER_TYPE_ROLE = 1;
	private static final int OWNER_TYPE_USER = 2;
	private static final int USER_QUICK_NOTE_MAX_VERSION = 5;
	
	private SecurityService  securityService;
	private RenderService  renderService;
	private PageService pageService;
	private SpaceService spaceService;
	private ThemeService themeService;
	private FriendService friendService;
	private SecurityDummy securityDummy;
	private RoleService roleService;
	private MailService mailService;
	private TouchService touchService;
	private CaptchaServiceProxy captchaService;
	private SettingService settingService;
	private ActivityLogService activityLog;
	private MessageService messageService;
	
	public UserModel getUser(String username) {
		User user;
		if(StringUtils.isBlank(username)){
			//get current login user:
			username = ServletUtils.getRequest().getRemoteUser();
		}
		user = userReadingService.getUserByName(username);
		UserModel model = UserUtil.copyUserToModel(null, user);
		
		//user status is not sync in copyUserModel(), sync here then..
		model.setStatus(user.getSetting().getStatus());
		return model;
	}

	public UserModel getUser(Integer userUid) {
		User user;
		if(userUid == null || userUid <=0){
			//get current login user:
			String username = ServletUtils.getRequest().getRemoteUser();
			user = userReadingService.getUserByName(username);
		}else{
			user = userReadingService.getUser(userUid);
		}
		
		UserModel model = UserUtil.copyUserToModel(null, user);
		UserUtil.copyContactsToModel(user, model, true, messageService);
		//user status is not sync in copyUserModel(), sync here then..
		model.setStatus(user.getSetting().getStatus());
		
		return model;
		
	}
	public UserListModel getSpaceGroupUsers(String senderSpaceUname, String receiverSpaceUname){
		UserListModel model = new UserListModel();
		Friend frd = friendService.getFriendship(senderSpaceUname, receiverSpaceUname);
		if(frd == null){
			model.errorCode = ErrorCode.FRIEND_NOT_MAKE;
		}else if (frd.isPending()){
			model.errorCode = ErrorCode.FRIEND_PENDING;
		}else if (frd.isRejected()){
			model.errorCode = ErrorCode.FRIEND_REJECT;
		}
		if(!StringUtils.isBlank(model.errorCode)){
			return model;
		}
		
		List<User> users = friendService.getSpaceGroupUsers(receiverSpaceUname);
		for (User user : users) {
			model.add(UserUtil.copyUserToModel(null, user));
		}
		
		return model;
	}
	public String savePassword(Integer userUid,String password){
		User user  = null;
		if(userUid == null || userUid <=0){
//			get current login user:
			user = WikiUtil.getUser();
			
			//reload from database again
			user = userReadingService.getUser(user.getUid());
		}else{
			//sys admin should use this one
			user = userReadingService.getUser(userUid);
		}
		
		if(user != null && !user.isAnonymous()){
			if(Global.EncryptPassword){
	            String algorithm = Global.PasswordEncodingAlgorithm;
	    
	            if (algorithm == null) { 
	                algorithm = "MD5";
	            }
	            user.setPassword(CodecUtil.encodePassword(password, algorithm));
	        }else
	        	user.setPassword(password);
			userService.updateUser(user);
			return null;
		}		
		return "failed";
	}
	public UserModel saveProfile(UserModel model){
		User user  = null;
		String username = ServletUtils.getRequest().getRemoteUser();
		if(model.getUid() == null || model.getUid() <= 0){
//			get current login user:
			user = userReadingService.getUserByName(username);
			if(user != null){
				//reload from database again
				user = userReadingService.getUser(user.getUid());
			}
		}else{
			user = userReadingService.getUser(model.getUid());
		}
		
		if(user != null){
			//check if it is current login user
			if(StringUtils.equalsIgnoreCase(username, user.getUsername())){
				model.setLogin(true);
			}else{
				model.setLogin(false);
			}
			//profile does not update username,password and uid
			UserUtil.copyProfileToUser(model,user);
			//also update user Lucene index
			userService.updateUserWithIndex(user);
			
			//need update setting as UserSettting.contact is updated.
			settingService.saveOrUpdateUserSetting(user, user.getSetting());
			
			//copy back - as UserSettting.contact may need to sort and group 
			UserUtil.copyContactsToModel(user, model, true, messageService);
			
			//user status is not sync in copyUserModel(), sync here then..
			model.setStatus(user.getSetting().getStatus());
			
			return model;
		}		
		return model;
	}



	public UserModel createUser(UserModel model, boolean login) {
		if(!WikiUtil.captchaValid(captchaService, model))
			return null;
		
		User user = new User();
		UserUtil.copyModelToUser(model, user);
		String plainPassword = user.getPassword();
    	if(Global.EncryptPassword){
            String algorithm = Global.PasswordEncodingAlgorithm;
    
            if (algorithm == null) { // should only happen for test case
                if (log.isDebugEnabled()) {
                    log.debug("assuming testcase, setting algorithm to 'MD5'");
                }
                algorithm = "MD5";
            }
        
            user.setPassword(CodecUtil.encodePassword(user.getPassword(), algorithm));
        }
        
    	//now, only signup and approval 2 method, here treat signup method is as default method. 
    	//So only Global.registerMethod=="approval", the enable is false, otherwise, user always be enabled.
    	boolean enable = !REGISTER_METHOD.approval.name().equals(Global.registerMethod);
        user.setEnabled(enable);
        
        // Set the default user role on this new user
        user.setRoles(roleService.getDefaultRole());

        user.setCreatedDate(new Date());
        try {
            userService.saveUser(user);
            //for compatible, not directly use Global.registerMethod(if it is null, treat as signup)
            activityLog.logUserSignup(user, enable?REGISTER_METHOD.signup:REGISTER_METHOD.approval);
            
            //set back user uid
            model.setUid(user.getUid());
            if(login && enable){
            	//login flag use for admin: if admin add user, then need not do login and send account email
            	securityService.login(user.getUsername(), model.getPassword());
            }
            
         	try {
    	        // Send create account email
            	SimpleMailMessage msg = new SimpleMailMessage();
				msg.setFrom(Global.DefaultNotifyMail);
				msg.setTo(user.getContact().getEmail());
				Map<String,Object> map = new HashMap<String,Object>();
				map.put(WikiConstants.ATTR_USER, user);
				map.put(WikiConstants.ATTR_PAGE_LINK, WebUtil.getHostAppURL());
				
				if(login){
				    if(enable){
				        mailService.sendPlainMail(msg, WikiConstants.MAIL_TEMPL_SIGNUP_NOTIFICATION, map);
				    }else{
				        //if user needs approval, then send waiting approval email
				        UserSetting setting = user.getSetting();
				        setting.setRequireSignupApproval(true);
		                settingService.saveOrUpdateUserSetting(user, setting);
		                
				        mailService.sendPlainMail(msg, WikiConstants.MAIL_TEMPL_SIGNUP_WAIT_APPROVAL_USER, map);
				        mailService.sendPlainMail(msg, WikiConstants.MAIL_TEMPL_SIGNUP_WAIT_APPROVAL_ADMIN, map);
				    }
				}else{
					//this is admin add new user, send user name and password as well.
					map.put(WikiConstants.ATTR_PASSWORD, plainPassword);
					mailService.sendPlainMail(msg, WikiConstants.MAIL_TEMPL_USER_ADDED_NOTIFICATION, map);
				}
				log.info("Email sent to " + user.getFullname() + " for signup notify.");
			} catch (Exception e) {
				log.error("Failed to send sign-up email:" + user.getContact().getEmail(),e);
			}
        } catch (UserExistsException e) {
            log.warn(e.getMessage());

            // redisplay the unencrypted passwords
            user.setPassword(user.getConfirmPassword());
            model.errorCode = ErrorCode.USER_ALREADY_EXIST_ERR;
		} catch (UserSignUpDiabledException e) {
			log.warn(e.getMessage());
			model.errorCode = ErrorCode.USER_SIGNUP_DISABLED;
		} catch (UserOverLimitedException e) {
			log.error("User is over license limitation.",e);
			model.errorCode = ErrorCode.USER_OVER_LIMITED;
		}
		return model;
	}

	
	/**
	 * Get all roles, users and anonymous user permission matrix. For security Panel display.
	 */
	public PermissionListModel getResourcePermissions(int resourceTypeOrdinal, String resourceName){
		PermissionListModel listModel = new PermissionListModel();
		RESOURCE_TYPES resourceType = RESOURCE_TYPES.values()[resourceTypeOrdinal];
		
		//Please note: this method actually call by {macro:friends}, if user without admin permission use this macro
		//in page, and this user does not have admin permission. It may lead the whole page can not be access anymore
		if(resourceType == RESOURCE_TYPES.INSTANCE){
			//check if current user has instance admin permission
			securityDummy.checkInstanceAdmin();
		}else if(resourceType == RESOURCE_TYPES.SPACE){
			securityDummy.checkSpaceAdmin(resourceName);
		}else if(resourceType == RESOURCE_TYPES.PAGE){
			Page page = pageService.getCurrentPageByUuid(resourceName);
			if(page == null)
				return null;
			
			securityDummy.checkPageRestrict(page.getSpace().getUnixName(), resourceName);
		}

		
		Map<PermissionModel,PermissionModel> permSet = new HashMap<PermissionModel, PermissionModel>();
		boolean foundAnonymous = false;
		
		List<Permission> permList = securityService.getResourcePermission(resourceType, resourceName);
//		So far does not user Mask concept, it is too complex for common user.
//		if(resourceType == RESOURCE_TYPES.PAGE){
//			String masks = securityService.getResourceMasks(resourceName);
//			PermissionModel model = new PermissionModel();
//			model.mask = true;
//			//for hashcode use
//			model.ownerUid = "-999";
//			model.ownerType  = -999;
//			if(!StringUtils.isBlank(masks)){
//				String[] maskIdxStr = masks.split(",");
//				int[] maskIdx  = new int[maskIdxStr.length];
//				for (int idx =0;idx<maskIdxStr.length;idx++) {
//					maskIdx[idx] = NumberUtils.toInt(maskIdxStr[idx], -1);
//				}
//				for (int idx : maskIdx) {
//					if(idx == -1)
//						continue;
//					model.operations[idx] = true;
//				}
//			}else{
//				//default, all masks are true
//				Arrays.fill(model.operations, true);
//			}
//			permSet.put(model, model);
//		}
		
		int resType = -1;
		for (Permission permission : permList) {
			if(resType == -1)
				//initial value at first time during loop
				resType = permission.getResource().getType().ordinal();
			
			Set<Role> roles = permission.getRoles();
			List<String> dead = permission.getDeadRoleUserList();
			if(roles != null){
				for (Role role : roles) {
					if(SYSTEM_ROLES.ANONYMOUS.getName().equalsIgnoreCase(role.getName())){
						foundAnonymous = true;
					}
					if(SYSTEM_ROLES.ADMIN.getName().equalsIgnoreCase(role.getName())){
						//if system does allow show admin permission control, then skip all permission for role of Admin.
						if(!Global.EnableAdminPermControl){
							continue;
						}
					}
						
					PermissionModel model = new PermissionModel();
					model.ownerType = OWNER_TYPE_ROLE;
					model.ownerName = role.getName();
					model.ownerDisplayName = role.getDisplayName();
					model.ownerDesc = role.getDescription();
					model.resourceType = resType;
					model.roleType = role.getType();
					//get same owner's permission
					PermissionModel exist = permSet.get(model);
					if(exist != null){
						//update exist, don't create new one: to group same role/user's permission
						exist.operations[permission.getOperation().ordinal()] = true;
						if(dead != null && dead.contains(role.getName()))
							exist.dead[permission.getOperation().ordinal()] = true;
						else
							exist.dead[permission.getOperation().ordinal()] = false;
					}else{
						model.operations[permission.getOperation().ordinal()] = true;
						if(dead != null && dead.contains(role.getName()))
							model.dead[permission.getOperation().ordinal()] = true;
						else
							model.dead[permission.getOperation().ordinal()] = false;
						model.resourceName = resourceName;
						permSet.put(model, model);
					}
				}
			}

			
			Set<User> users = permission.getUsers();
			if(users != null){
				for (User user : users) {
					PermissionModel model = new PermissionModel();
					model.ownerType = OWNER_TYPE_USER;
					model.ownerName = user.getUsername();
					model.ownerDisplayName = user.getFullname();
					model.ownerDesc = user.getFullname();
					model.resourceType = resType;

					PermissionModel exist = permSet.get(model);
					if(exist != null){
						exist.operations[permission.getOperation().ordinal()] = true;
						if(dead != null && dead.contains(Role.USER_PREFIX+user.getUsername()))
							exist.dead[permission.getOperation().ordinal()] = true;
						else
							exist.dead[permission.getOperation().ordinal()] = false;
					}else{
						model.operations[permission.getOperation().ordinal()] = true;
						if(dead != null && dead.contains(Role.USER_PREFIX+user.getUsername()))
							model.dead[permission.getOperation().ordinal()] = true;
						else
							model.dead[permission.getOperation().ordinal()] = false;
						model.resourceName = resourceName;
						permSet.put(model, model);
					}
				}
			}
		}
		//always show anonymous group for SPACE and INSTANCE!
		if(permList.size() > 0 && !foundAnonymous && resourceType != RESOURCE_TYPES.PAGE){
			Role anony = roleService.getRoleByName(SecurityValues.SYSTEM_ROLES.ANONYMOUS.getName());
			PermissionModel model = new PermissionModel();
			model.ownerType = OWNER_TYPE_ROLE;
			model.ownerName = anony.getName();
			model.roleType = anony.getType();
			model.ownerDisplayName = anony.getDisplayName();
			model.ownerDesc = anony.getDescription();
			model.resourceName = resourceName;
			model.resourceType = resType;
			permSet.put(model, model);
		}
		
		listModel.list = new ArrayList<PermissionModel>(permSet.values());
		return listModel;
	}

	/**
	 * 
	 */
	public PermissionListModel updatePermission(int resourceTypeOrdinal, ArrayList<PermissionModel> changedModelList) {
		
		if(changedModelList == null || changedModelList.size() == 0)
			return null;
		
		String resourceName = changedModelList.get(0).resourceName;
		RESOURCE_TYPES resourceType = RESOURCE_TYPES.values()[resourceTypeOrdinal];
		
		if(resourceType == RESOURCE_TYPES.INSTANCE){
			//check if current user has instance admin permission
			securityDummy.checkInstanceAdmin();
		}else if(resourceType == RESOURCE_TYPES.SPACE){
			securityDummy.checkSpaceAdmin(resourceName);
		}else if(resourceType == RESOURCE_TYPES.PAGE){
			Page page = pageService.getCurrentPageByUuid(resourceName);
			if(page == null)
				return null;
			
			
			securityDummy.checkPageRestrict(page.getSpace().getUnixName(), resourceName);
		}
		
		//!!!Use set so that remove duplicated user/role name
		Set<String> roleNames = new HashSet<String>();
		Set<String> userNames = new HashSet<String>();
		String masks = "";
		
		//find out unique role/user from changeModelList, which is for each operation on each role/user
		for(PermissionModel model : changedModelList){
			if(model.mask){
				masks += model.operation +",";
			}else if(model.ownerType == OWNER_TYPE_ROLE){
				roleNames.add(model.ownerName);
			}else{
				userNames.add(model.ownerName);
			}
		}
		//masks: currently it is useless
		if(masks.length() != 0){
			Resource res = securityService.getResourceByName(resourceName, resourceType);
			if(res == null){
				res = securityService.saveResource(resourceName, resourceType);
			}

			res.setMasks(masks);
			securityService.updateResource(res);
		}
		
		if(resourceType == RESOURCE_TYPES.SPACE){
			//check friendship between new added role and this space role
			//NOTE: resourceName is spaceUname if resourceType is RESOURCE_TYPES.SPACE
			friendService.makeFriendsWithSpaceGroups(resourceName, new ArrayList<String>(roleNames));
		}
		
		boolean dirty = false;
		
		//some roles permission have potential updated, so here update them.
		for (String roleName : roleNames) {
			Role role = roleService.getRoleByName(roleName);

			//get this role all permission, and update according to incoming values
			Set<Permission> perm = role.getPermissions();
			dirty = updatePermission(changedModelList, perm, null, role);
			roleService.saveRole(role);
			
			if(isUpdateInstance(changedModelList,null,role)){
				//users may be in UserCache. And if any permission modified, these modification must be immediately
				//update to those cached users. Here will refresh permissions
				if(SYSTEM_ROLES.ANONYMOUS.getName().equalsIgnoreCase(role.getName())){
					//for anonymous user, it has a user in useCache as well, its username is SYSTEM_ROLES.ANONYMOUS.getName() or just null
					userService.removeUserFromCache(WikiUtil.getAnonymous());
				}else if(SYSTEM_ROLES.USERS.getName().equals(role.getName())){
					//here is performance consideration: user of ROLE registered may be huge number, just reset all user cacahe then.
					userService.removeUserFromCache(null);
				}else{
					Set<User> users = role.getUsers();
					if(users != null){
						if(users.size() > 1000){
							//still performance consideratino, too many users in this role, then reset userCache to empty anyway.
							userService.removeUserFromCache(null);
						}else{
							for (User user : users) {
								userService.removeUserFromCache(user);
							}
						}
					}
				}
			}
			
		}
		
		//some users permission have potential updated, so here update them.
		for (String username : userNames) {
			User user = userReadingService.getUserByName(username);
			if(user != null){
				//user may from cache, here do reload from DB
				user = userReadingService.getUser(user.getUid());
				
				Set<Permission> perm = user.getPermissions();
				dirty = updatePermission(changedModelList, perm,user,null);
				
				//skip UserMethodBeforeAdvise to do security check: as space admin may not instance admin, 
				//and not the user whose permission is updated.
				userService.interalUpdateUser(user);
				
				if(isUpdateInstance(changedModelList,user,null)){
					//User value should be in UserCache. Here get user and refresh its instancePermission, then put back to cache. 
					userService.removeUserFromCache(user);
				}
			}
		}
		
		//At this case that resource is page. The resource will be new one if first time update. 
		//Any permission won't be reflect to this resource. It means resource can not get back corresponding Permission
		//so, here must re-load(refresh) Resource object from database.
		//!!! This method also need call before securityService.resetSpaceReadingCache() and resetPageReadingCache();
		Resource resource = securityService.getResourceByName(resourceName, resourceType);
		securityService.refreshResource(resource);
		
		
		if(resourceType == RESOURCE_TYPES.SPACE){
			//so far, resource already is with latest users/roles list, so refresh 
			//resourceName is spaceUname if resourceType is RESOURCE_TYPES.SPACE
			friendService.refreshSpaceGroupUsers(resource);
		}
		
		//some permission updated, refresh permission cache
		if(dirty){
			securityService.resetPolicyCache(resourceType,resourceName);
			if(resourceType == RESOURCE_TYPES.SPACE
					|| resourceType == RESOURCE_TYPES.PAGE){
				//need check if update spaceReadingCache
				for(Iterator<PermissionModel> iter = changedModelList.iterator();iter.hasNext();){
					PermissionModel model = iter.next();
					if(model.operation == OPERATIONS.READ.ordinal()){
						if(resourceType == RESOURCE_TYPES.SPACE){
							securityService.resetSpaceReadingCache(resourceName);
						}else if (resourceType == RESOURCE_TYPES.PAGE){
							securityService.resetPageReadingCache(resourceName);
						}
						break;
					}
				}
				//update SensitiveTouchInfo
				if(resourceType == RESOURCE_TYPES.SPACE){
					touchService.touchSpace(resourceName);
				}else if (resourceType == RESOURCE_TYPES.PAGE){
					touchService.touchPage(resourceName);
				}
			}
			

		}
		 
		return getResourcePermissions(resourceTypeOrdinal,resourceName);
	}
	public UserModel checkLogin(JsInfoModel jsModel) {
		
		User user = WikiUtil.getUser();
		UserModel userModel = UserUtil.copyUserToModel(null, user);

		if(user.isAnonymous() || user.getSetting().getTimeZone() == null){
			//jsModel.timezoneOffset format is hour.minutes, such as 6.30
			TimeZone timezone = TimeZoneUtil.guessTimeZone(jsModel.timezoneOffset);
			HttpSession session = WebUtil.getRequest().getSession();
			session.setAttribute(Constants.TIMEZONE, timezone);
			
			log.info("User timezone is set to {} with timezone offset {}", timezone.getDisplayName(),jsModel.timezoneOffset);
		}
		
		return userModel;
	}
	
	public RoleModel saveRole(String name, String desc) {
		name = StringUtils.trimToEmpty(name);
		
		RoleModel model = new RoleModel();
		Role role = null;
		 
		//need check if this role already exist
		role = roleService.getRoleByName(Role.GROUP_ROLE_PREFIX+name.toUpperCase());
		if(role == null){
			//it is better won't ask user create system default role name, although it is allowed in database
			role = roleService.getRoleByName(Role.SYSTEM_ROLE_PREFIX+name.toUpperCase());
		}
		
		if(role != null){
			model.errorCode = ErrorCode.ROLE_NAME_CONFLICT_ERR;
			return model;
		}
		
		
		role = new Role();
		role.setName(Role.GROUP_ROLE_PREFIX+name.toUpperCase());
		role.setType(Role.TYPE_GROUP);
		role.setDisplayName(name);
		role.setDescription(desc);
		WikiUtil.setTouchedInfo(userReadingService, role);
		roleService.createRole(role);
		
		model.setUid(role.getUid());
		model.setName(role.getName());
		model.setDisplayName(name);
		model.setDesc(desc);
		
		return model;
	}

	public RoleListModel getRoleList(int roleType) {
		List<Role> roles = roleService.getRoles(roleType,null);
		List<RoleModel> modelList = new ArrayList<RoleModel>();
		for (Role role : roles) {
			//if system does allow show admin permission control, then skip all role of Admin.
			if(SYSTEM_ROLES.ADMIN.getName().equalsIgnoreCase(role.getName())){
				if(!Global.EnableAdminPermControl){
					continue;
				}
			}
			modelList.add(UserUtil.copyRoleToModel(role));
		}
		RoleListModel model = new RoleListModel();
		model.total = roles.size();
		model.addAll(modelList);
		return model;
	}

	public Integer addUsersToRole(int roleUid, ArrayList<String> usernameList){
		if(usernameList == null || usernameList.size() == 0){
			return null;
		}
		Role role = roleService.getRole(roleUid);
		if(role == null)
			return null;
		
		boolean needSave = false;
		for (String username : usernameList) {
			User user = userReadingService.getUserByName(username);
			if(user != null){
				//as above user maybe come from cache, so read it again to confirm it is Hibernate PO 
				user = userReadingService.getUser(user.getUid());
				Set<User> users = role.getUsers();
				boolean has = false;
				for (User existUser: users) {
					if(existUser.equals(user)){
						has = true;
						break;
					}
				}
				if(!has){
					user.getRoles().add(role);
					users.add(user);
					needSave = true;
				}
				
				//It needs reset user from user cache, otherwise, user roles won't be updated
				userReadingService.removeUserFromCache(user);
			}
		}
		if(needSave){
			roleService.saveRole(role);
			return roleUid;
		}else{
			return null;
		}
		
	}


	//JDK1.6 @Override
	public UserProfileModel getUserContributed(String username, int type) {
		User viewer = WikiUtil.getUser();
		
		UserProfileModel profile = new UserProfileModel();
		
		if(type == 0 || (type & SharedConstants.SPACE) > 0){
			//this indicate to client side to say spaceList is refreshed.
			profile.spaces = new ArrayList<SpaceModel>();
			
			List<Space> spaces  = spaceService.getUserAllCreatedSpaces(username,-1,viewer);
			for (Space space : spaces) {
				SpaceModel model = new SpaceModel();
				SpaceUtil.copySpaceToModel(space, model,viewer,themeService);
				profile.spaces.add(model);
			}
		}
		
		//Now, history and page are same
		if(type == 0 || (type & SharedConstants.PAGE) > 0 || (type & SharedConstants.HISTORY) > 0){
			//this indicate to client side to say pageList is refreshed.
			profile.pages = new ArrayList<PageItemModel>();
			
			List<Page>  pages = pageService.getUserAllContributedPages(username,-1,viewer);
			List<History>  histories = pageService.getUserAllContributedHistories(username,viewer);
			
			Map<String, PageItemModel> map = new HashMap<String, PageItemModel>();
			//merge them by pageUUID
			for (Page page : pages) {
				PageItemModel item = map.get(page.getPageUuid());
				if(item == null){
					item = PageUtil.copyToPageItem(page);
					//sort versionHistory creator->2,...5->current etc.
					item.versionHistory = new HashMap<Integer, PageItemModel>();
					map.put(page.getPageUuid(), item);
				}
				if((page.getCreator() != null && StringUtils.equals(page.getCreator().getUsername(),username))
					|| (page.getCreator() == null && (username == null || User.ANONYMOUS_USERNAME.equalsIgnoreCase(username)))){
					PageItemModel ver = new PageItemModel();
					ver.uid = page.getUid();
					ver.modifiedDate = DateUtil.getLocalDate(viewer, page.getCreatedDate());
					item.versionHistory.put(0, ver);
				}
				if((page.getModifier() != null && StringUtils.equals(page.getModifier().getUsername(),username))
					|| (page.getModifier() == null && (username == null || User.ANONYMOUS_USERNAME.equalsIgnoreCase(username)))){
					PageItemModel ver = new PageItemModel();
					ver.uid = page.getUid();
					ver.modifiedDate = DateUtil.getLocalDate(viewer, page.getModifiedDate());
					item.versionHistory.put(Integer.MAX_VALUE, ver);
				}
			}
			for (History history : histories) {
				PageItemModel item = map.get(history.getPageUuid());
				if(item == null){
					item = PageUtil.copyToPageItem(history);
					item.versionHistory = new HashMap<Integer, PageItemModel>();
					map.put(history.getPageUuid(), item);
				}
				PageItemModel ver = new PageItemModel();
				ver.uid = history.getUid();
				ver.modifiedDate = DateUtil.getLocalDate(viewer, history.getModifiedDate());
				item.versionHistory.put(history.getVersion(), ver);
			}
			
			//default sort pages by space name
			TreeMap<String, PageItemModel> sortedPages = new TreeMap<String, PageItemModel>(new Comparator<String>(){
				public int compare(String o1, String o2) {
					int ret = o1.compareTo(o2);
					//don't overwrite same spaceUname pages
					return ret == 0?1:ret;
				}
				
			});
			for(PageItemModel item : map.values()){
				sortedPages.put(item.spaceUname, item);
			}
			
			profile.pages = new ArrayList<PageItemModel>(sortedPages.values());
		}
		if(type == 0 || (type & SharedConstants.ACTIVITY) > 0){
			//this indicate to client side to say spaceList is refreshed.
			profile.activities = new ArrayList<ActivityModel>();
			User user = userReadingService.getUserByName(username);
			//TODO: now only get first 15 activities;
			List<ActivityLog> msgs = activityLog.getUserActivities(0, 15, user, WikiUtil.getUser());
			for (ActivityLog msg : msgs) {
				ActivityModel act = new ActivityModel();
				act.activity = msg.getMessage();
				profile.activities.add(act);
			}
			
		}
		return profile;
	}
	public UserProfileModel getUserProfile(String username) {
		
		User viewer = WikiUtil.getUser();
		
		UserProfileModel profile = new UserProfileModel();
		User user = userReadingService.getUserByName(username);
		profile.profile = UserUtil.copyUserToModel(viewer, user);
		//user status is not sync in copyUserModel(), sync here then..
		profile.profile.setStatus(user.getSetting().getStatus());
		//hide private contact info
		UserUtil.copyContactsToModel(user, profile.profile, false, messageService);
		
		if(user.isAnonymous() || user.equals(WikiUtil.getUser()) || WikiUtil.getUser().isAnonymous()){
			profile.profile.setFollowing(-1);
		}else{
			profile.profile.setFollowing(userReadingService.isFollowing(viewer, user)?1:0);
		}
		
		//get user network - need refresh from database again as user cache doesn't save followers/following
		user = userReadingService.getUser(user.getUid());
		List<User> followers = user.getFollowers();
		if(followers != null){
			for (User fer : followers) {
				profile.followers.add(UserUtil.copyUserToModel(viewer, fer));
			}
		}
		
		List<User> following = user.getFollowings();
		if(following != null){
			for (User fer : following) {
				profile.following.add(UserUtil.copyUserToModel(viewer, fer));
			}
		}
		
		return profile;
	}

	//JDK1.6 @Override
	public int sendForgetPassword(String email) {
		User user = userReadingService.getUserByEmail(email);
		if(user == null){
			//email does not exist
			return SharedConstants.RET_NO_EMAIL;
		}
		//reset this user password and send out
		String newPass = RandomStringUtils.randomAlphabetic(8);
		//so far, it is plain text - just for email body. It will be encrypted in UserSerivce.resetPassword()
		
		String plainPass = newPass;
		user.setPassword(newPass);
		//TODO: Does it need warranty only email send out, the password can be reset?
		//if so, I need change 2 things. Use mailEngine instead of mailService, change mailEngine throw exception 
		//rather than catch all Exception
		userService.resetPassword(user,newPass);
		//after above method, the password is encrypted one, so need keep plain one and send email
		
		//send email
       	try {
	        // Send create account email
        	SimpleMailMessage msg = new SimpleMailMessage();
			msg.setFrom(Global.DefaultNotifyMail);
			msg.setTo(user.getContact().getEmail());
			
			Map<String,Object> map = new HashMap<String,Object>();
			map.put(WikiConstants.ATTR_PASSWORD, plainPass);
			map.put(WikiConstants.ATTR_USER, user);
			mailService.sendPlainMail(msg, WikiConstants.MAIL_TEMPL_FORGET_PASSWORD_NOTIFICATION, map);
			log.info("Email sent to " + user.getFullname() + " for password reset.");
		} catch (Exception e) {
			log.error("Failed to send reset passowrd email:" + user.getContact().getEmail(),e);
			return SharedConstants.RET_SEND_MAIL_FAILED;
		}
		
		return 0;
	}
	public int captchaValid(CaptchaCodeModel captcha) {
		if(!WikiUtil.captchaValid(captchaService, captcha))
			return -1;
		
		return 0;
	}

	public boolean saveUserStatus(Integer userUid, String text) {
		User user = userReadingService.getUser(userUid);
		if(user != null){
			UserSetting setting = user.getSetting();
			setting.setStatus(text);
			settingService.saveOrUpdateUserSetting(user, setting);
			
			activityLog.logUserStatusUpdate(user);
			return true;
		}
		return false;
	}
	
	public QuickNoteModel saveUserQuickNote(String content){
		User user = WikiUtil.getUser();
		QuickNoteModel model = new QuickNoteModel();
		if(!user.isAnonymous()){
			UserSetting setting = user.getSetting();
			List<QuickNote> notes = setting.getQuickNotes();
			int version = 1;
			if(notes == null || notes.size() == 0){
				notes = new ArrayList<QuickNote>();
				setting.setQuickNotes(notes);
			}else{
				for (QuickNote note : notes) {
					if(version  < note.getVersion()){
						//get max version number
						version = note.getVersion();
					}
				}
				version++;
				int diff = notes.size() - USER_QUICK_NOTE_MAX_VERSION  + 1;
				//remove old version
				for(Iterator<QuickNote> iter = notes.iterator();iter.hasNext() && diff > 0; diff--){
					iter.next();
					iter.remove();
				}
			}
			QuickNote note = new QuickNote();
			note.setCreateDate(new Date());
			note.setNote(content);
			note.setVersion(version);
			notes.add(note);
			settingService.saveOrUpdateUserSetting(user, setting);
			log.info("User {} quick note is saved",user.getUsername());
			
			model.renderContent = renderService.renderHTML(content);
			model.content = content;
			model.version = version;
			model.createDate = note.getCreateDate();
		}
		
		return model;
	}
	
	public UserModel followUser(String username, boolean following) {
		User user = userReadingService.getUserByName(username);
		User myself = WikiUtil.getUser();
		UserModel model = UserUtil.copyUserToModel(myself, user);
		
		if(following){
			userService.follow(myself, user);
			model.setFollowing(1);
		}else{
			userService.unfollow(myself, user);
			model.setFollowing(0);
		}
		return model;
	}


	//********************************************************************
	//               private methods
	//********************************************************************
	/*
	 * Check if given user/role's instance permission has been updated in this request  <code>changedModelList</code>
	 */
	private boolean isUpdateInstance(List<PermissionModel> changedModelList, User user, Role role) {
		boolean dirty = false;
		//retrieve all new changed operations for this role
		for(Iterator<PermissionModel> iter = changedModelList.iterator();iter.hasNext();){
			PermissionModel model = iter.next();
			if(model.resourceType != RESOURCE_TYPES.INSTANCE.ordinal())
				continue;
			
			//skip other permission not belong to this user/role
			if(user != null){
				if(model.ownerType != OWNER_TYPE_USER ||  !model.ownerName.equalsIgnoreCase(user.getUsername()))
					continue;
			}else if (role != null){
				if(model.ownerType != OWNER_TYPE_ROLE || !model.ownerName.equalsIgnoreCase(role.getName()))
					continue;
			}
			dirty = true;
			break;
		}
		return dirty;
	}
	/*
	 * Update special user or role permissions
	 */
	private boolean updatePermission(List<PermissionModel> changedModelList, Set<Permission> perm, User user, Role role) {
		boolean dirty = false;
		//retrieve all new changed operations for this role
		for(Iterator<PermissionModel> iter = changedModelList.iterator();iter.hasNext();){
			PermissionModel model = iter.next();
			
			//skip other permission not belong to this user/role
			if(user != null){
				if(model.ownerType != OWNER_TYPE_USER ||  !model.ownerName.equalsIgnoreCase(user.getUsername()))
					continue;
			}else if (role != null){
				if(model.ownerType != OWNER_TYPE_ROLE || !model.ownerName.equalsIgnoreCase(role.getName()))
					continue;
			}
			
			log.info("change model :" + model);
			boolean found = false;
			Resource resource = null;
			//compare with existed operations, if found, then it means this operation  status must switch off, must remove
			for (Iterator<Permission>  permIter = perm.iterator(); permIter.hasNext();) {
				Permission permission = permIter.next();
				if(model.operation == permission.getOperation().ordinal()
					&& model.resourceType == permission.getResource().getType().ordinal()
					&& StringUtils.equalsIgnoreCase(model.resourceName,permission.getResource().getResource())){
					permIter.remove();
					dirty = true;
					if(user != null){
						log.info("User "+ user.getUsername() + " remove permission: " + permission);
						Set<User> users = permission.getUsers();
						for(Iterator<User> uIter = users.iterator();uIter.hasNext();){
							if(user.getUid().equals(uIter.next().getUid())){
								uIter.remove();
							}
						}
					}else if(role != null){
						log.info("Role "+ role.getName() + " remove permission:" + permission);
						Set<Role> roles = permission.getRoles();
						for(Iterator<Role> rIter = roles.iterator();rIter.hasNext();){
							if(role.getUid().equals(rIter.next().getUid())){
								rIter.remove();
							}
						}
					}
					found = true;
//					removed , not necessary update anymore
//					securityService.saveUpdatePermission(permission);
					break;
				}
			}
			//if could not find the operation, it means a new operation is tick (space,instance) on or off(page): create new permission
			if(!found){
				OPERATIONS operation = getOperation(model.operation);
				Permission newPerm = securityService.getPermissionByOperationResource(operation, model.resourceName);
				if(newPerm == null){
					//only page,otherwise, Permission must already exists
					newPerm = new Permission();
					newPerm.setOperation(operation);
					//for this iterator loop, it must be same resource, so just get once
					resource = securityService.getResourceByName(model.resourceName, RESOURCE_TYPES.values()[model.resourceType]);
					if(resource == null){
						resource = securityService.saveResource(model.resourceName, RESOURCE_TYPES.values()[model.resourceType]);
					}
					newPerm.setResource(resource);
				}
				if(user != null){
					Set<User> users = newPerm.getUsers();
					if(users == null)
						users = new HashSet<User>();
					users.add(user);
					newPerm.setUsers(users);
					log.info("User "+ user.getUsername() + " add permission:" + newPerm);
				}else if(role != null){
					Set<Role> roles = newPerm.getRoles();
					if(roles == null)
						roles = new HashSet<Role>();
					roles.add(role);
					newPerm.setRoles(roles);
					log.info("Role "+ role.getName() + " add permission:" + newPerm);
				}
				dirty = true;
				securityService.saveUpdatePermission(newPerm);
				perm.add(newPerm);
			}
		}
		return dirty;
	}
	/**
	 * @param operation
	 * @return
	 */
	private OPERATIONS getOperation(int operation) {
		for(SecurityValues.OPERATIONS oper : SecurityValues.OPERATIONS.values()){
			if(oper.ordinal() == operation)
				return oper;
		}
		return null;
	}

	//********************************************************************
	//               Set / Get
	//********************************************************************
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}

	public void setCaptchaService(CaptchaServiceProxy captchaService) {
		this.captchaService = captchaService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public void setSecurityDummy(SecurityDummy securityDummy) {
		this.securityDummy = securityDummy;
	}
	public void setFriendService(FriendService friendService) {
		this.friendService = friendService;
	}

	public TouchService getTouchService() {
		return touchService;
	}

	public void setTouchService(TouchService touchService) {
		this.touchService = touchService;
	}

	public void setPageService(PageService pageService) {
		this.pageService = pageService;
	}

	public void setActivityLog(ActivityLogService activityLog) {
		this.activityLog = activityLog;
	}

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}


	public void setThemeService(ThemeService themeService) {
		this.themeService = themeService;
	}

	/**
	 * @param settingService the settingService to set
	 */
	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}

	public void setRenderService(RenderService renderService) {
		this.renderService = renderService;
	}

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}


}
