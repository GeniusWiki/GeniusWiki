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
package com.edgenius.wiki;

import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;

/**
 * @author Dapeng.Ni
 */
public class WikiConstants {
	public static final String APP_NAME = SharedConstants.APP_NAME;
	
	//********************************************************************
	//               Some status, type variable
	//********************************************************************
	//space home page uuid
	public static final String CONST_HOMT_PAGE_UUID = "Home";
	//!!! There is same value at core.Constants
	public static final String CONST_INSTANCE_RESOURCE_NAME = SharedConstants.INSTANCE_NAME;
	//Space can not start with this string as spaceUname, so it used for the scenarios 
	//which may need save spaceUname and other string into one container, just append this string before the other resource
	//can ensure there no duplicated on space and other resource. For example, widgets will save into policyCache for security,
	//Specified space theme will saved into cache with system scope theme, etc. 
	public static final String CONST_NONSPACE_RESOURCE_PREFIX = "$#$";
	public static final String CONST_SYSTEM_ADMIN = "admin";
	
	//These value has hardcode in EditPanel to coordinate with checkbox.
	public static final int NOTIFY_NONE = 0;
	public static final int NOTIFY_EMAIL = 1;
	public static final int NOTIFY_BLOG = SharedConstants.NOTIFY_BLOG;
	public static final int NOTIFY_TWITTER = 1<<2;
	public static final int NOTIFY_FACEBOOK = 1 << 3;
	public static final int NOTIFY_ALL = NOTIFY_EMAIL|NOTIFY_BLOG|NOTIFY_TWITTER|NOTIFY_FACEBOOK;
	
	//********************************************************************
	//               Just for KEY of name, used in request.getParameter() or request.setAttribute() etc.
	//********************************************************************
	public static final String ATTR_PAGE="page";
	public static final String ATTR_SPACE = "space";
	public static final String ATTR_SPACE_UNAME = "spaceUname";
	public static final String ATTR_PAGE_PARENT_UUID = "pageParentUuid";
	public static final String ATTR_PAGE_TITLE = "pageTitle";
	public static final String ATTR_SPACE_LIST = "spaces";
	public static final String ATTR_USER = "user";
	public static final String ATTR_PASSWORD = "password";
	public static final String ATTR_COUNT = "count";
	public static final String ATTR_PAGE_LINK = "pagelink";
	public static final String ATTR_REMOVE_DELAY_HOURS = "removeDelayHours";
	public static final String ATTR_INVITE_URL = "inviteUrl";
	public static final String ATTR_INVITE_MESSAGE = "inviteMessage";
	
	public static final String MODIFIED_DATE = "modifiedDate";
	public static final String MODIFIER_FULLNAME = "modifierFullname";
	public static final String ATTR_LIST = "list";
	public static final String ATTR_CONTENT = "content";
	public static final String ATTR_PAGINATION_INFO = "pagination";
	public static final String ATTR_ADMIN_URL = "adminURL";

    public static final String ATTR_SIGNUP_SUPRESSED = "signupSuppressed";
	public static final String ATTR_SUPPRESS = "ATTR_SUPPRESS";
	
	public static final String SESSION_NAME_USER_SORTBY = "userSortBy";
	public static final String SESSION_NAME_SPACE_SORTBY = "spaceSortBy";
	//********************************************************************
	//               I18N key
	//********************************************************************

	public static final String I18N_HOME_PAGE_TITLE = "wiki.home.page.title";
	
	public static final String I18N_BLOG_HOME_PAGE_CONTENT = "blog.home.page.content";
	public static final String I18N_PAGE_NOT_FOUND_CONTENT = "wiki.page.not.found.content";
	public static final String I18N_PAGE_NOT_FOUND_CONTENT_READONLY = "wiki.page.not.found.content.readonly";
	public static final String I18N_HOMEPAGE_NOT_FOUND_TITLE = "wiki.homepage.not.found.title";
	public static final String I18N_TAGCLOUD_TITLE = "page.title.tagcloud";
	public static final String I18N_SPACE_TAGCLOUD_TITLE = "page.title.space.tagcloud";
	public static final String I18N_USERPROFILE_TITLE = "page.title.userprofile";
	public static final String I18N_SEARCH_RESULT_TITLE = "page.title.search.result";
	public static final String I18N_SPACE_FRIENDS_PAGE_TITLE = "page.title.space.friends";
	public static final String I18N_SPACE_GROUP_PAGE_TITLE = "page.title.space.group.users";
	public static final String I18N_MESSAGE_BOARD_PAGE_TITLE = "page.title.message.board";
	public static final String I18N_SPACE_ADMIN_PAGE_TITLE = "page.title.space.admin";

	public static final String I18N_NOTIFY_SYSADMIN = "page.title.notify.sysadmin";
	
	public static final String I18N_WATCHLIST_TITLT = "watch.list.title";
	public static final String I18N_WATCHLIST_DESC = "watch.list.desc";
	public static final String I18N_MSG_BOARD_TITLT = "message.board.title";
	public static final String I18N_MSG_BOARD_DESC = "message.board.desc";
	public static final String I18N_FAVORITE_TITLT = "favorite.list.title";
	public static final String I18N_FAVORITE_DESC = "favorite.list.desc";
	public static final String I18N_DRAFTLIST_TITLT = "draft.list.title";
	public static final String I18N_DRAFTLIST_DESC= "draft.list.desc";

	public static final String I18N_INVITE_MESSAGE = "invite.msg";
	public static final String I18N_INVITE_ACCEPT = "invite.accepted.to.sender";

	//********************************************************************
	//               Mail template name
	//********************************************************************
	public static final String MAIL_TEMPL_PAGE_CHANGE_NOTIFICATION = "pagechange.ftl";
	public static final String MAIL_TEMPL_SIGNUP_NOTIFICATION = "signup.ftl";
	public static final String MAIL_TEMPL_USER_ADDED_NOTIFICATION = "useradded.ftl";
	public static final String MAIL_TEMPL_SPACE_REMOVING = "spaceremoving.ftl";
	public static final String MAIL_TEMPL_SPACE_REMOVED = "spaceremoved.ftl";
	public static final String MAIL_TEMPL_MESSAGE = "message.ftl";
	public static final String MAIL_TEMPL_INVITE = "friendinvitation.ftl";
	public static final String MAIL_TEMPL_COMMENT_DAILY_SUM = "commentsummary.ftl";
	public static final String MAIL_TEMPL_COMMENT_PER_POST = "commentperpost.ftl";
	public static final String MAIL_TEMPL_FORGET_PASSWORD_NOTIFICATION = "forgetpass.ftl";
	public static final String MAIL_TEMPL_FRIENDSHIP = "friendshiprequest.ftl";
	public static final String MAIL_TEMPL_VERSION_CHECK = "versioncheck.ftl";
	public static final String MAIL_TEMPL_ADD_INVITED_USER = "adduserrequeest.ftl";
	//This variable not used but has hardcode value in UserServiceImpl.saveUser() because of package dependency restriction  
    public static final String MAIL_TEMPL_USER_VOLUME_EXCEED = "userexceed.ftl";

    public static final String MAIL_TEMPL_SIGNUP_WAIT_APPROVAL_ADMIN = "signupapprovetoadmin.ftl";
    public static final String MAIL_TEMPL_SIGNUP_WAIT_APPROVAL_USER = "signupapprovaltouser.ftl";
	public static final int UUID_KEY_SIZE = 8;
	
	
	public static final String[] ISBLOCK_TAGS = new String[]{
		"table",
		"tr",
		"td",
		"th",
		"caption",
		"div",
		"h1",
		"h2",
		"h3",
		"h4",
		"h5",
		"h6",
		"ol",
		"li",
		"ul",
		"menu",
		"dd",
		"dt",
		"dir",
		"blockquote",
		"br",
		"p",
	};
	public static final int PORTRAIT_WIDTH = 200;
	public static final int LOGO_SMALL_WIDTH = 16;

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//               URL hardcode in some place, these url wont' bring leading "/" (root context)
	public static final String URL_HOME = SharedConstants.URL_HOME;
	public static final String URL_LOGIN = "/signin";
	//this jsp does not exist as it is just flag for ajax redirect at moment
	public static final String URL_CAPTCHA_VERIFIED_ERROR = "/captcha.jsp";
	public static final String URL_ACCESS_DENIED = "/403.jsp";

	public static final int AUTO_FIX_COPY_LINK = 1;
	public static final int AUTO_FIX_TITLE_CHANGE_LINK= 1<<1;

	public static final String MACRO_NAME_KEY = "$MACRO_NAME$";




	public static int offlineDBVersion = 1;
	public static int offlineMainDBVersion = 1;

	public static String mceNonEditable = "mceNonEditable";

	public enum REGISTER_METHOD{signup, approval};
}
