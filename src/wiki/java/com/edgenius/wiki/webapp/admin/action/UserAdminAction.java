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
package com.edgenius.wiki.webapp.admin.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.mail.SimpleMailMessage;

import com.edgenius.core.Global;
import com.edgenius.core.SecurityValues.SYSTEM_ROLES;
import com.edgenius.core.UserSetting;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.service.MailService;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.util.DateUtil;
import com.edgenius.core.util.WebUtil;
import com.edgenius.core.webapp.taglib.PageInfo;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.service.CommentService;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.util.WikiUtil;
import com.edgenius.wiki.webapp.action.BaseAction;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class UserAdminAction  extends BaseAction{
	private static final int PAGE_SIZE = 15;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// parameters
	//page number
	private int page;
	private int sortBy;
	private boolean sortByDesc;
	//user uid for retrieve single user detail
	private int uid;
	private boolean enable;
	private String filter;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// service 
	private SpaceService spaceService;
	private PageService pageService;
	private CommentService commentService;
	private MessageService messageService;
	private MailService mailService;
	private SettingService settingService;
	
	//********************************************************************
	//               method
	//********************************************************************
	public String list(){
		filter();
		
		return SUCCESS;
	}
	
	public String filter(){
		User viewer = WikiUtil.getUser();
		
		//page 0(null) or 1 is same
		page = page==0?1:page;
		
		String sortSeq = getSortBySequence(WikiConstants.SESSION_NAME_USER_SORTBY, sortBy);
		if("".equals(sortSeq)){
			//default sort by register date desc - latest register user is first
			sortSeq = User.SORT_BY_CREATED_DATE +"";
			//pass back to JSP
			sortBy = User.SORT_BY_CREATED_DATE ; 
			sortByDesc = true;
		}

		List<User> users = userReadingService.getUsers((page-1)*PAGE_SIZE, PAGE_SIZE,sortSeq,filter,sortByDesc);
		List<UserDTO> dtos = new ArrayList<UserDTO>();
		for (User user : users) {
			UserDTO dto = new UserDTO();
			dto.setUser(user);
			dto.setCreatedDate(DateUtil.toDisplayDate(viewer,user.getCreatedDate(),messageService));
			dtos.add(dto);
		}
		

		int total = userReadingService.getUserTotalCount(filter);
		PageInfo pInfo = new PageInfo();
		pInfo.setCurrentPage(page);
		pInfo.setTotalPage(total/PAGE_SIZE + (total%PAGE_SIZE>0?1:0));
		
		getRequest().setAttribute("pagination", pInfo);
		getRequest().setAttribute("total", total);
		getRequest().setAttribute("users", dtos);
		
		return "list";
	}
	public String detail(){
		User user = userReadingService.getUser(uid);
		//!!! Must CLONE!!! Otherwise, this cause serious bug as this change will persist into database - it means this role is removed from user role list!!!
		user = (User) user.clone();
		//this role isn't meaningful, hide it...
		user.getRoles().remove(new Role(SYSTEM_ROLES.USERS.getName()));
		
		UserDTO dto = new UserDTO();
		dto.setUser(user);
		
		dto.setSpaceAuthorSize(spaceService.getUserAuthoredSpaceSize(user.getUsername()));
		dto.setAuthorSize(pageService.getUserAuthoredPageSize(user.getUsername()));
		dto.setModifierSize(pageService.getUserModifiedPageSize(user.getUsername()));
		dto.setCommentSize(commentService.getUserCommentSize(user.getUsername()));
		
		dto.setRemovable((dto.getSpaceAuthorSize() + dto.getAuthorSize() + dto.getModifierSize()+dto.getCommentSize())==0);
		getRequest().setAttribute("dto", dto);
		return "detail";
	}
	
	public String enable() {
		
		User user = userReadingService.getUser(uid);
		user.setAccountLocked(!enable);
		user.setEnabled(enable);
		
		//this will update user cache as well
		userService.updateUser(user);
		
		//Retreive UserSetting.requireSignupApproval, if it is true, then send out email to tell user whose account is enabled.
		UserSetting setting = user.getSetting();
		if(setting.isRequireSignupApproval()){
		    try {
		        SimpleMailMessage msg = new SimpleMailMessage();
		        msg.setFrom(Global.DefaultNotifyMail);
		        msg.setTo(user.getContact().getEmail());
		        Map model = new HashMap();
		        model.put(WikiConstants.ATTR_USER, user);
		        model.put(WikiConstants.ATTR_PAGE_LINK, WebUtil.getHostAppURL());
		        //send sign up success email
                mailService.sendPlainMail(msg, WikiConstants.MAIL_TEMPL_SIGNUP_NOTIFICATION, model);
                
                //reset flag and save into database
                setting.setRequireSignupApproval(false);
                settingService.saveOrUpdateUserSetting(user, setting);
                
                //according to this method convention, null means this account approved.
                activityLog.logUserSignup(user,null);
            } catch (Exception e) {
                log.error("Failed to send out user signup approvaed email:" + user.getUsername(), e);
            }
		   
		}
		UserDTO dto = new UserDTO();
		dto.setUser(user);
		getRequest().setAttribute("dto", dto);
		return "func";
	}

	/**
	 * A new user created in administrator page
	 * @return
	 */
	public String created(){
		getRequest().setAttribute("message", messageService.getMessage("user.create.success"));
		return list();
	}


	//********************************************************************
	//               set / get
	//********************************************************************
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	
	public int getSortBy() {
		return sortBy;
	}

	public void setSortBy(int sortBy) {
		this.sortBy = sortBy;
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public boolean isEnable() {
		return enable;
	}
	public boolean isSortByDesc() {
		return sortByDesc;
	}
	public void setSortByDesc(boolean sortByDesc) {
		this.sortByDesc = sortByDesc;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}
	public void setPageService(PageService pageService) {
		this.pageService = pageService;
	}
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public void setSettingService(SettingService settingService) {
        this.settingService = settingService;
    }
}
