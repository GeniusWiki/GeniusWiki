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

import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.GeneralModel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class ErrorCode {
	//U, I , S,  P, A, D, C , L , F, R, B,M,N,H,O,W,T,E
	//********************************************************************
	//               User error
	//********************************************************************
	public static final String USER_ALREADY_EXIST_ERR = "ERR-U-1";
	public static final String USER_RMOVE_ERR = "ERR-U-2";
	public static final String USER_SIGNUP_DISABLED = "ERR-U-3";
	public static final String USER_OVER_LIMITED  = "ERR-U-4";
	public static final String ROLE_NAME_CONFLICT_ERR  = "ERR-U-5";
	//********************************************************************
	//               Space error
	//********************************************************************
	//instance saving error.
	public static final String INSTANCE_SAVING_ERROR = "ERR-I-1";
	
	public static final String SPACE_NOT_EXIST_ERR = "ERR-S-3";
	public static final String SAVEUPDATE_ERR = "ERR-S-2";
	public static final String DUPLICATE_SPACE_TITLE_ERR = "ERR-S-1";
	public static final String REMOVE_SPACE_ERR = "ERR-S-4";
	public static final String SPACE_NO_READ_PERM = "ERR-S-5";
	public static final String DUPLICATE_SPACE_KEY_ERR = "ERR-S-6";

	public static final String PAGE_SAVE_ERROR = "ERR-P-1";
	public static final String PAGE_VERSION_CONFLICT = "ERR-P-2";
	public static final String PAGE_SAVE_DRAFT_ERROR = "ERR-P-3";
	public static final String PAGE_GET_ERROR = "ERR-P-4";
	public static final String PAGE_COPY_ERROR =  "ERR-P-5";
	public static final String PAGE_MOVE_ERROR = "ERR-P-6";
	//return null: given page which will removed can not found
	public static final String PAGE_REMOVE_NOT_FOUND = "ERR-P-7";
	public static final String PAGE_HOME_CANNOT_REMOVE = "ERR-P-8";
	public static final String PAGE_REMOVE_FAILED = "ERR-P-8";
	public static final String PAGE_DUPLICATED_TITLE = "ERR-P-9";
	public static final String PAGE_SAVE_TIME_OUT = "ERR-P-10";
	public static final String PAGE_RESTORE_ERROR = "ERR-P-11";
	//phase render
	public static final String PAGE_PHASE_RENDER_NO_PAGE = "ERR-P-12";
	public static final String PAGE_PHASE_RENDER_NO_ALLOW = "ERR-P-13";
	public static final String PAGE_PHASE_RENDER_NO_PHASE = "ERR-P-14";
	public static final String PAGE_PHASE_RENDER_FAILED = "ERR-P-15";


	//attachment metdata data error;
	public static final String ATT_METADATA_UPDATE_FAILED = "ERR-A-1";
	public static final String ATT_REMOVE_FAILED = "ERR-A-2";
	
	public static final String DIFF_ORIG_PAGE_NOT_FOUND = "ERR-D-1";
	public static final String DIFF_FAILED = "ERR-D-2";
	
	public static final String COMMENT_CREATE_FAILED = "ERR-C-1";
	public static final String COMMENT_GET_FAILED = "ERR-C-2";
	public static final String COMMENT_NO_PERM_HIDE = "ERR-C-3";
	public static final String COMMENT_HIDE_FAILED = "ERR-C-4";
	
	public static final String SEARCH_ERROR = "ERR-L-1";
	
	//attachment upload error
	public static final String UPLOAD_FAILED = "ERR-F-1";
	
	//space attachment quota achieve
	public static final String SPACE_QUOTA_ERROR = "ERR-F-2";
	
	//space RSS feed reading error
	public static final String SPACE_RSS_READ_ERROR = "ERR-R-1";
	
	
	public static final String FRIEND_NOT_MAKE = "ERR-B-1";
	public static final String FRIEND_PENDING = "ERR-B-2";
	public static final String FRIEND_REJECT = "ERR-B-3";
	
	public static final String MSG_NO_PERM_READ_ERROR = "ERR-M-1";
	public static final String MSG_NOT_EXIST_ERROR = "ERR-M-2";
	
	public static final String INVITATION_FAILED = "ERR-N-1";
	public static final String HISTORY_RESTORE_ERROR = "ERR-H-1";
	public static final String HISTORY_RESTORE_DUPLICATE_TITLE_ERROR = "ERR-H-2";
	public static final String HISTORY_RESTORE_SAVE_TIME_OUT_ERROR = "ERR-H-3";
	public static final String HISTORY_GET_ERROR = "ERR-H-4";
	
	public static final String UNSUPPORT_OFFLINE = "ERR-O-1";
	public static final String OFFLINE_DOWNLOAD_ERR = "ERR-O-2";
	
	public static final String WIDGET_INIT_OBJ_FAILED = "ERR-W-1";
	public static final String PORTAL_LOAD_FAILED = "ERR-W-2";
	public static final String WIDGET_NOT_FOUND = "ERR-W-3";
	public static final String WIDGET_TO_MULTIPLE_PORTAL = "ERR-W-4";
	public static final String WIDGET_ADD_TO_ANONYMOUS_PORTAL = "ERR-W-5";
	
	public static final String TMEPL_NO_PERMISSION =  "ERR-T-1";
	public static final String TMEPL_LOAD_FAILED ="ERR-T-2";
	
	public static final String PLUGIN_LOAD_FAILED ="ERR-E-1";
	public static final String PLUGIN_INVOKE_FAILED ="ERR-E-2";



	
	/**
	 * @param errorCode
	 * @return
	 */
	public static boolean hasError(GeneralModel model) {
		return !StringUtil.isBlank(model.errorCode)
			|| !StringUtil.isBlank(model.errorMsg);
	}
	/**
	 * @param errorCode
	 * @return
	 */
	public static HorizontalPanel getMessage(String errorCode, String defaultErrorMsg) {
		return getMessage(errorCode, defaultErrorMsg, null);
	}
	
	public static HorizontalPanel getMessage(String errorCode, String defaultErrorMsg, Widget[] widgets) {
		
		HorizontalPanel panel = new HorizontalPanel();
		buildMessage(panel,errorCode, defaultErrorMsg, widgets);
		return panel;
	}
	
	/**
	 * This can not handle message which has parameters, ie, some text {0} and some {1}; 
	 *  
	 * @param errorCode
	 * @return
	 */
	public static String getMessageText(String errorCode, String defaultErrorMsg){
		String msg = getMessageString(errorCode);
		if(msg == null)
			msg = defaultErrorMsg;
		
		if(msg == null)
			msg = Msg.consts.unknown_error() + " :" + errorCode;
		
		return msg;
	}
	/**
	 * @param errorCode
	 * @return
	 */
	private static String getMessageString(String errorCode) {
		if(errorCode == null ||errorCode.trim().length() == 0)
			return null;
		
		String error = null;
		
		if(SPACE_NOT_EXIST_ERR.equals(errorCode)){
			error = Msg.consts.err_space_not_exist();
		}else if(SAVEUPDATE_ERR.equals(errorCode)){
			error = Msg.consts.err_space_save();
		}else if(DUPLICATE_SPACE_TITLE_ERR.equals(errorCode)){
			error = Msg.consts.err_dup_space_name();
		}else if(DUPLICATE_SPACE_KEY_ERR.equals(errorCode)){
			error = Msg.consts.err_dup_space_unixname();
		}else if(REMOVE_SPACE_ERR.equals(errorCode)){
			error = Msg.consts.err_remove_space();
		}else if(PAGE_SAVE_ERROR.equals(errorCode)){
			error = Msg.consts.err_save_page();
		}else if(PAGE_VERSION_CONFLICT.equals(errorCode)){
			error = Msg.consts.err_newer_version();
		}else if(PAGE_HOME_CANNOT_REMOVE.equals(errorCode)){
			error = Msg.consts.err_homepage_remove();
		}else if(PAGE_SAVE_DRAFT_ERROR.equals(errorCode)){
			error = Msg.consts.err_session_expired();
		}else if(PAGE_GET_ERROR.equals(errorCode)){
			error = Msg.consts.err_load_page();
		}else if(PAGE_COPY_ERROR.equals(errorCode)){
			error = Msg.consts.err_copy_page();
		}else if(PAGE_MOVE_ERROR.equals(errorCode)){
			error = Msg.consts.err_move_page();
		}else if(PAGE_REMOVE_NOT_FOUND.equals(errorCode)){
			error = Msg.consts.err_page_not_found();
		}else if(PAGE_HOME_CANNOT_REMOVE.equals(errorCode)){
			error = Msg.consts.err_homepage_remove_failed();
		}else if(PAGE_DUPLICATED_TITLE.equals(errorCode)){
			error = Msg.consts.err_same_title();
		}else if(PAGE_REMOVE_FAILED.equals(errorCode)){
			error = Msg.consts.err_remove_page();
		}else if(PAGE_SAVE_TIME_OUT.equals(errorCode)){
			error = Msg.consts.err_save_timeout();
		}else if(PAGE_RESTORE_ERROR.equals(errorCode)){
			error = Msg.consts.err_restore_page();
		}else if(PAGE_PHASE_RENDER_NO_ALLOW.equals(errorCode)){
			error = Msg.consts.err_phase_render_no_allow();
		}else if(PAGE_PHASE_RENDER_NO_PAGE.equals(errorCode)){
			error = Msg.consts.err_phase_render_no_page();
		}else if(PAGE_PHASE_RENDER_NO_PHASE.equals(errorCode)){
			error = Msg.consts.err_phase_render_no_phase();
		}else if(PAGE_PHASE_RENDER_FAILED.equals(errorCode)){
			error = Msg.consts.err_phase_render_failed();
		}else if(USER_ALREADY_EXIST_ERR.equals(errorCode)){
			error = Msg.consts.err_user_exist();
		}else if(ROLE_NAME_CONFLICT_ERR.equals(errorCode)){
			error = Msg.consts.err_role_name_conflict();
		}else if(USER_RMOVE_ERR.equals(errorCode)){
			error = Msg.consts.err_remove_user();
		}else if(USER_SIGNUP_DISABLED.equals(errorCode)){
			error = Msg.consts.err_signup_disable();
		}else if(USER_OVER_LIMITED.equals(errorCode)){
			error = Msg.consts.err_over_license();
		}else if(ATT_METADATA_UPDATE_FAILED.equals(errorCode)){
			error = Msg.consts.err_get_attachment_meta();
		}else if(ATT_REMOVE_FAILED.equals(errorCode)){
			error = Msg.consts.err_remove_attachment();
		}else if(DIFF_ORIG_PAGE_NOT_FOUND.equals(errorCode)){
			error = Msg.consts.err_orig_page_not_found();
		}else if(DIFF_FAILED.equals(errorCode)){
			error = Msg.consts.err_version_compare();
		}else if(COMMENT_CREATE_FAILED.equals(errorCode)){
			error = Msg.consts.err_post_comment();
		}else if(COMMENT_GET_FAILED.equals(errorCode)){
			error = Msg.consts.err_get_comment();
		}else if(COMMENT_NO_PERM_HIDE.equals(errorCode)){
			error = Msg.consts.err_hide_perm_comment();
		}else if(COMMENT_HIDE_FAILED.equals(errorCode)){
			error = Msg.consts.err_hide_comment();
		}else if(SEARCH_ERROR.equals(errorCode)){
			error = Msg.consts.err_search();
		}else if(UPLOAD_FAILED.equals(errorCode)){
			error = Msg.consts.err_upload();
		}else if(SPACE_QUOTA_ERROR.equals(errorCode)){
			error = Msg.consts.err_quota_exhaust();
		}else if(INSTANCE_SAVING_ERROR.equals(errorCode)){
			error = Msg.consts.err_save_system();
		}else if(SPACE_RSS_READ_ERROR.equals(errorCode)){
			error = Msg.consts.err_read_rss();
		}else if(FRIEND_NOT_MAKE.equals(errorCode)){
			error = Msg.consts.err_not_friend();
		}else if(FRIEND_PENDING.equals(errorCode)){
			error = Msg.consts.err_friend_wait_approve();
		}else if(FRIEND_REJECT.equals(errorCode)){
			error = Msg.consts.err_friend_reject();
		}else if(MSG_NO_PERM_READ_ERROR.equals(errorCode)){
			error = Msg.consts.err_no_perm_on_message();
		}else if(MSG_NOT_EXIST_ERROR.equals(errorCode)){
			error = Msg.consts.err_message_exist();
		}else if(INVITATION_FAILED.equals(errorCode)){
			error = Msg.consts.err_invite_fail();
		}else if(HISTORY_RESTORE_ERROR.equals(errorCode)){
			error = Msg.consts.err_history_restore();
		}else if(HISTORY_RESTORE_DUPLICATE_TITLE_ERROR.equals(errorCode)){
			error = Msg.consts.err_history_dup_title();
		}else if(HISTORY_RESTORE_SAVE_TIME_OUT_ERROR.equals(errorCode)){
			error = Msg.consts.err_restore_history_timeout();
		}else if(HISTORY_GET_ERROR.equals(errorCode)){
			error = Msg.consts.err_get_history();
		}else if(SPACE_NO_READ_PERM.equals(errorCode)){
			error = Msg.consts.err_no_perm_on_space();
		}else if(UNSUPPORT_OFFLINE.equals(errorCode)){
			error = Msg.consts.err_unsupport_offline();
		}else if(OFFLINE_DOWNLOAD_ERR.equals(errorCode)){
			error = Msg.consts.err_fail_download();
		}else if(WIDGET_INIT_OBJ_FAILED.equals(errorCode)){
			error = Msg.consts.err_init_widget();
		}else if(PORTAL_LOAD_FAILED.equals(errorCode)){
			error = Msg.consts.err_portal_load();
		}else if(WIDGET_NOT_FOUND.equals(errorCode)){
			error = Msg.consts.err_widget_not_found();
		}else if(WIDGET_TO_MULTIPLE_PORTAL.equals(errorCode)){
			error = Msg.consts.err_multiple_portal_in_dashboard();
		}else if(WIDGET_ADD_TO_ANONYMOUS_PORTAL.equals(errorCode)){
			error = Msg.consts.err_no_allow_widget_to_anonymous();
		}else if(TMEPL_NO_PERMISSION.equals(errorCode)){
			error = Msg.consts.err_no_perm_view_templates();
		}else if(TMEPL_LOAD_FAILED.equals(errorCode)){
			error = Msg.consts.err_load_templates();
		}else if(PLUGIN_INVOKE_FAILED.equals(errorCode)){
			error = Msg.consts.err_failed_plugin_service();
		}else if(PLUGIN_LOAD_FAILED.equals(errorCode)){
			error = Msg.consts.err_failed_load_plugin();
		}
		return error;
	}
	private static void buildMessage(HorizontalPanel panel, String errorCode, String defaultErrorMsg, Widget[] widgets) {
		String msg = getMessageString(errorCode);
		if(msg == null)
			msg = defaultErrorMsg;
		
		if(widgets != null && widgets.length > 0){
			//parse string to find out {x} and split them
			int size = msg.length();
			int leftIndex = -1;
			String str="";
			String index = "";
			for(int idx=0;idx<size;idx++){
				char c = msg.charAt(idx);
				//always sum char to string, if it is token, the str will substract the token string
				str += Character.toString(c);
				if('{' == c){
					leftIndex = idx;
				}else if(leftIndex > 0 && NumberUtil.isDigit(c)){
					index += Character.toString(c);
				}else if(leftIndex > 0  && '}' == c){
					int num = new Integer(index).intValue();
					if(num < widgets.length){
						// a valid replace token
						//first adjust str to before {
						str = str.substring(0,str.length()-index.length()-2);
						
						panel.add(new HTML(str));
						//append space before widget, otherwise HTML will ignore last space 
						if(str.endsWith(" "))
							panel.add(new HTML("&nbsp;"));
						//then add widget
						panel.add(widgets[num]);
						if(idx<size-1){
							//append space after widget, otherwise HTML will ignore first space 
							if(msg.charAt(idx+1) == ' ')
								panel.add(new HTML("&nbsp;"));
						}
						str = "";
					}
					leftIndex = -1;
					index="";
				}else if(leftIndex > 0 ){
					//next char is not number, then ignore this msg token
					leftIndex = -1;
					index="";
				}
			}
			if(str.length() > 0)
				panel.add(new HTML(str));
		}else{
			panel.add(new HTML(msg));
		}
		
	}

	
}
