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
package com.edgenius.wiki.gwt.client.server.utils;


/**
 * Some constants shared by client side and server side code. So, this class must deployed with geniuswiki.jar.
 * @author Dapeng.Ni
 */
public class SharedConstants {
	//This is temporarily disable as blog function too buggy and hard to fix during sync
	//There are too many problems don't resolved.  For example
	//how to sync tag <-> category/tags
	//how to avoid duplicated sync. Only allow download or upload? 
	//blog allow duplicated title, how to resolve this?
	public static final boolean DISABLE_BLOG_FUNC = true;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// constants value  
	public static enum ALIGN_VALUES{right("right"),left("left"),center("center"),centre("centre");
		String name;
		private ALIGN_VALUES(String name){
			this.name = name;
		}
		public static boolean contains(String floatAtt) {
			for(ALIGN_VALUES value:ALIGN_VALUES.values()){
				if(StringUtil.equalsIgnoreCase(value.name,floatAtt)){
					return true;
				}
			}
			return false;
		}
		
		public String getName() {
			return name;
		}	
	}

	public static enum SIZE_VALUES{big("big"),large("large"),small("small");
		String name;
		
		private SIZE_VALUES(String name){
			this.name = name;
		}
		public static boolean contains(String sizeAtt) {
			for(SIZE_VALUES value:SIZE_VALUES.values()){
				if(StringUtil.equalsIgnoreCase(value.name,sizeAtt)){
					return true;
				}
			}
			return false;
		}
		
		public String getName() {
			return name;
		}	
	}

	public static final String APP_NAME = "GeniusWiki";
	public static final String WEB_URL = "www.geniuswiki.com";
	
	public static final int SPACE = 1 ;
	public static final int PAGE = 1 << 1;
	public static final int HISTORY = 1 << 2;
	public static final int ACTIVITY = 1 << 3;
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//	//Customised page indicator
	public static final String CPAGE_TAG_CLOUD = "tc";
	public static final int CPAGE_TAG_CLOUD_UID = -1;
	
	public static final String CPAGE_USER_PROFILE = "up";
	public static final int CPAGE_USER_PROFILE_UID = -2;
	
	public static final String CPAGE_SEARCH_RESULT = "rs";
	public static final int CPAGE_SEARCH_RESULT_UID = -3;
	
	public static final String CPAGE_SPACEADMIN = "sa";
	public static final int CPAGE_SPACEADMIN_UID = -7;
	
	public static final String CPAGE_SYSADMIN_NOTIFY = "ad";
	public static final int CPAGE_SYSADMIN_NOTIFY_UID = -8;
	
	public static final String CPAGE_TEMPLATE_LIST = "tl";
	public static final int CPAGE_TEMPLATE_LIST_UID = -9;

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// macro name
	public static final String MACRO_SPACE_ADMIN = "sapceadmin";
//	public static final String MACRO_FRIENDS = "friends";
//	public static final String MACRO_SPACE_GROUP_USERS = "spacegroup";
	public static final String MACRO_NOTIFY_SYSADMIN = "notifysysadmin";
	public static final String MACRO_TAG_CLOUD = "tagcloud";
	public static final String MACRO_SEARCH = "search";
	public static final String MACRO_USER_PROFILE ="profile";
	public static final String MACRO_SAVE_ME = "saveme";
	public static final String MACRO_FEEDBACK = "feedback";
	public static final String MACRO_PORTAL = "portal";
	public static final String MACRO_SIGNUP= "signup";
	public static final String MACRO_VISIBLE = "visible";
	public static final String MACRO_COMMENT = "comment";
	public static final String MACRO_INCLUDE = "include";
	public static final String MACRO_TEMPLATE_LIST ="templatelist";
	
	//param name must be lower case as they may convert in Macro Render
	public static final String MACRO_PARAM_SPACE_NAME = "space";
	public static final String MACRO_PARAM_MESSAGE_ID = "msgid";
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//	//Customised page indicator
	public static final int OFFLINE_CONFLICT_DRAFT = 3;
	public static final int AUTO_DRAFT = 2;
	public static final int MANUAL_DRAFT = 1;
	public static final int NONE_DRAFT = 0;
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//	Search Result indicator
	public static final int SEARCH_PAGE_TAG = 1;
	public static final int SEARCH_PAGE = 2;
	public static final int SEARCH_SPACE = 3;
	public static final int SEARCH_USER = 4;
	public static final int SEARCH_SPACE_TAG = 5;
	public static final int SEARCH_ATTACHMENT = 6;
	public static final int SEARCH_COMMENT = 7;
	public static final int SEARCH_WIDGET = 8;
	public static final int SEARCH_ROLE = 9;
	

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public static final String SYSTEM_SPACEUNAME = "$SYSTEM$";
	public static final String INSTANCE_NAME = "$instance$";
	public static final String FORM_RET_HEADER = "MESSAGE";
	public static final String FORM_RET_ERROR = "0";
	public static final String FORM_RET_HEADER_ERROR_IN_USERPASS = "1";
	public static final String FORM_RET_HEADER_ERROR_CAPTCHA = "2";
	public static final String FORM_RET_ACCESS_DENIED_EXP = "3";
	public static final String FORM_RET_AUTH_EXP = "4";
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// share by page change email notify
	public static final String TOKEN_CPAGE = "$CPAGE";
	public static final String TOKEN_CLINK = "$CL";
	public static final String TOKEN_COMMENT = "$COMMENT";
	public static final String TOKEN_EDIT_TEMPLATE = "$ET";
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// space permission base value for OPERATIONS.ADMIN or OPERATIONS.EXPORT
	public static final int PERM_SPACE_BASE = 10;
	public static final int PERM_INSTNACE_MGM = 19;
	public static final int PAGE_PERM_SIZE = 20;

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//space type
	public static final short PUBLIC_SPACE = 0;
	public static final short PRIVATE_SPACE = 1;
	
	//should equals with Role.TYPE_SYSTEM and Role.TYPE_SPACE
	public static final int ROLE_TYPE_ALL = -1; //same value with Constants.ROLE_TYPE_ALL
	public static final int ROLE_TYPE_SYSTEM = 0;
	public static final int ROLE_TYPE_SPACE = 1;
	
	//it is same value with Role.SPAC_ROLE_PREFIX
	public static final String ROLE_SPACE_PREFIX = "ROLE_SPACE_";
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// use for permission forbid or allow.               
	public static final int FORBID = 0;
	public static final int ALLOW = 1;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//               Restore page stautus: restore normal page, 
	// home page and current home page not exist, and, home page but a new home page created. 

	public static final int RESTORE_NORMAL = 0;
	public static final int RESTORE_HOMEPAGE_EXIST = 1;
	public static final int RESTORE_HOMEPAGE_NO_EXIST = 2;
	
	public static final String NO_PORTRAIT_IMG = "noportrait.jpg";
	public static final String INSANCE_LOGO = "geniuswiki.png";
	public static final int PAGE_SIZE = 10;
	
	public static final String LOGO_SEP = "$";
	//!!!There is same constants in Contants.PORTLET_SEP
	public static final String PORTLET_SEP = "$"; //can not use & as it will be entity prefix
	public static final char MACRO_PARAM_SEP = '|';

	//0 should be default option, means all selected, this weird design is just for space can use all options in defualt model
	public final static int OPTION_ALL= 0;
	public final static int OPTION_NONE= 1;
	public final static int OPTION_SYNC_DRAFT= 1<<1;
	public final static int OPTION_SYNC_COMMENT= 1<<2;
	public final static int OPTION_SYNC_HISTORY= 1<<3;
	public final static int OPTION_SYNC_ATTACHMENT= 1<<4;
	
	public static final String NEXT_LINK = "&raquo;";
	public static final String PREV_LINK = "&laquo;";
	
	public static final String NO_RENDER_TAG = "norender";
	public static final String RENDER_ERROR_TAG = "rendererror";
	//********************************************************************
	//               Javascript method name
	//********************************************************************
	public static final String JS_USER_POP = "userPopup";
	
	//********************************************************************
	//               offline update status
	//********************************************************************
	public final static int OFFLINE_DOWNLOAD_FROM_SERVER= 0;
	//attachment
	public final static int OFFLINE_DELETED= 1;
	public final static int OFFLINE_UPLOAD= 2;
	public static final int OFFLINE_SUBMISSIONING = 3;
	//page
	public static final int OFFLINE_EDITED = 1;
	
	public static final String SIDEBAR_TYPE_HOME = "1";
	public static final String SIDEBAR_TYPE_CURRENT = "2";
	//this option is DISALBED at moment: overwrite model: overwrite all sidebar, even it has special value.
	public static final String SIDEBAR_TYPE_ALL = "3";
	public static final String SIDEBAR_TYPE_DEFAULT = "4";
	
	
	public static final String THEME_PAGE_SCOPE_HOME = "home";
	public static final String THEME_PAGE_SCOPE_DEFAULT = "any";
	
	//********************************************************************
	//               space widget style on dashboard
	//********************************************************************
	public static final int WIDGET_STYLE_ITEM_SHORT_BY_MODIFIED_DATE = 0;
	public static final int WIDGET_STYLE_ITEM_SHORT_BY_CREATE_DATE = 1;
	public static final int WIDGET_STYLE_HOME_PAGE = 2;
	public static final int WIDGET_STYLE_SHOW_PORTRAIT = 3;
	
	public static final int TITLE_MAX_LEN = 60;
	public static final String CSS_RENDER_IMAGE = "renderImage";
	

	//********************************************************************
	//Page flag options
	//********************************************************************
	public static final int USER_PAGE_TYPE_FAVORITE = 1;
	public static final int USER_PAGE_TYPE_WATCH = 2;
	//!!! MUST equal or greater than 10 - Please refer to SQL query
	public static final int USER_PAGE_TYPE_PINTOP = 10;
	//********************************************************************
	//export
	//********************************************************************
	public static final int EXPORT_TYPE_HTML = 0;
	public static final int EXPORT_TYPE_PDF = 1;
	
	
	public static final Integer RET_NO_EMAIL = 1;
	public static final Integer RET_SEND_MAIL_FAILED = 2;
	
	public static final String DRAFT_KEY = "My drafts";
	public static final String FAVORITE_KEY = "my favoite";
	public static final String MESSAGE_BOARD_KEY = "Message board";
	public static final String QUICKNOTE_KEY = "Quick note";
	public static final String ACTIVITYLOG_KEY = "Recently update";
	public static final String WATCHLIST_KEY = "My watchlist";
	
	//VERY important!!! - if DEFAULT_DAHSBOARD_MARKUP change, please also revise getDashboard() and addWidgetToDashboardPortal() in PortalControllerImpl
	//because they both assume the default dashboard include only {portal}
	public static final String DEFAULT_DAHSBOARD_MARKUP = "{portal:showLogo=true}";
	
	public static final int DEFAULT_PORTAL_COLUMNS = 3;
	
	//com.edgenius.core.SecurityValues has same hardcode definition! They also used in JSP code! 
	public static final String ROLE_ADMIN = "ROLE_SYS_ADMIN";
	public static final String ROLE_ANONYMOUS = "ROLE_SYS_ANONYMOUS";
	public static final String ROLE_REGISTERED = "ROLE_SYS_USERS";
	//this value is as prefix of space group desc and display name...
	public static final String SPACE_ROLE_DEFAULT_PREFIX = "Group of Space " ;
	
	public static final String TABLE_BORDER_DEFAULT_WIDHT= "1";
	
	public static final String TABLE_BG_DEFAULT_COLOR= "#FFFFFF";
	public static final String TABLE_BORDER_DEFAULT_COLOR= "#BBB";
	public static final String TABLE_TH_DEFAULT_COLOR= "#333";
	public static final String TABLE_TH_DEFAULT_BG_COLOR= "#EDEDED";

	public static final int TAB_TYPE_COMMENT = 1;
	public static final int TAB_TYPE_HISTORY = 1<<1;
	public static final int TAB_TYPE_CHILDREN = 1<<2;
	public static final int TAB_TYPE_LEFT_SIDEBAR = 1<<3; //8
	public static final int TAB_TYPE_RIGHT_SIDEBAR= 1<<4; //16
	
	//Note - TAB_TYPE_DEFAULT_VISIBLE only applied on Page bottom tab panel.
	//if open a page, the default tab will be shown.  if -1, then all tabs are hidden. Otherwise, choose one of above first 3 value
	public static final int TAB_TYPE_DEFAULT_VISIBLE = TAB_TYPE_COMMENT;
	
	public static final String[] IMAGE_FILTERS = new String[]{".bmp",".jpg",".jpeg",".png",".gif"};
	
	//these values use in ADV_SOURCE_TYPES
	public static final int ADV_SEARCH_KEYWORD_ANY = 0; //default 
	public static final int ADV_SEARCH_KEYWORD_ALL = 1; 
	public static final int ADV_SEARCH_KEYWORD_EXACT = 2; 
	public static final int ADV_SEARCH_KEYWORD_EXCEPT = 3; 
	
	//these values use in ADV_SOURCE_TYPES
	public static final int ADV_SEARCH_INDEX_PAGE = 1;
	public static final int ADV_SEARCH_INDEX_SPACE = 1<<1;
	public static final int ADV_SEARCH_INDEX_COMMENT = 1<<2;
	public static final int ADV_SEARCH_INDEX_USER = 1<<3;
	public static final int ADV_SEARCH_INDEX_ATTACHMENT = 1<<4;
	public static final int ADV_SEARCH_INDEX_TAGONPAGE = 1<<5;
	public static final int ADV_SEARCH_INDEX_TAGONSPACE = 1<<6;
	
	public static final int ADV_SEARCH_INDEX_ALL = ADV_SEARCH_INDEX_PAGE|ADV_SEARCH_INDEX_SPACE
			|ADV_SEARCH_INDEX_COMMENT|ADV_SEARCH_INDEX_USER|ADV_SEARCH_INDEX_ATTACHMENT
			|ADV_SEARCH_INDEX_TAGONPAGE|ADV_SEARCH_INDEX_TAGONSPACE;
	
	//these 2 not implement yet
	public static final int ADV_SEARCH_INDEX_ROLE = 1<<7;
	public static final int ADV_SEARCH_INDEX_WIDGET = 1<<8;
	
	//these values use in ADV_GROUP_BY
	public static final int ADV_SEARCH_GROUP_TYPE = 1;
	public static final int ADV_SEARCH_GROUP_SPACE = 2;

	
	public static final int MSG_TARGET_FOLLOWERS = 1;
	public static final int MSG_TARGET_INSTANCE_ADMIN_ONLY = 1 << 1;
	public static final int MSG_TARGET_SPACE_ADMIN_ONLY = 1<<2;
	//I don't thing to all users(has READ permission) is useful. Most spaces allow anonymous and all registers read, then 
	//to all reader of space becomes useless...
	public static final int MSG_TARGET_SPACE_CONTRIBUTE_USERS = 1<<3; 
	public static final int MSG_TARGET_USER = 1 << 4;	
	public static final int MSG_TARGET_ALL_USERS = 1 << 5;
	public static final int MSG_ITEM_COUNT_IN_MSG_BOARD = 10;
	public static final int ITEM_COUNT_IN_ACTIVITY_BOARD = 15;

	public static final String URL_HOME = "index.jsp";
	public static final String URL_INSTANCE_ADMIN = "sysadmin";
	
	//for ajax URL
	public static final String URL_PAGE = "page";
	public static final String URL_TINY_PAGE = "p";
	public static final String URL_TINY_SPACE = "s";
	//both below for read-only URL prefix.
	public static final String URL_VIEW = "view";
	public static final String URL_TINY_VIEW = "v";
	public static final String URL_TINY_READONLY_SPACE = "vs";
	public static final int NOTIFY_BLOG = 1<<1;
	
	public static final int PORTRAIT_SIZE_SMALL = 35;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// 			Space list Sort by option
	//Below options can be combination to string with separator "|"
	public static final int SORT_BY_SPACEKEY = 1;
	public static final int SORT_BY_SPACE_TITLE = 1<<1;
	public static final int SORT_BY_PAGE_COUNT = 1<<2;
	public static final int SORT_BY_PAGE_SCORE = 1<<3;
	public static final int SORT_BY_CREATEBY = 1 <<4;
	public static final int SORT_BY_CREATEON = 1 <<5;
	
	public static final String USERSETTING_PROP_NAME_EMAIL = "Email"; //UserSetting.CONTACT_EMAIL_NAME;

	//suppress functions - There is exactly same value in Constants.java - that is for Global.java
	public static enum SUPPRESS{
	    
		SIGNUP(1),
		//If system integrated with third party and login from external, then remove logout button from UI 
		LOGOUT(1<<1);
		
		private int value;
		private SUPPRESS(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}
	}
	
}
