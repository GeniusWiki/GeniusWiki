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
package com.edgenius.wiki.gwt.client.offline;

import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.model.GeneralModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.URLPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.gears.client.Factory;
import com.google.gwt.gears.client.GearsException;
import com.google.gwt.gears.client.localserver.LocalServer;
import com.google.gwt.gears.client.localserver.ManagedResourceStore;
import com.google.gwt.gears.offline.client.Offline;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.UIObject;


/**
 * @author Dapeng.Ni
 */
public class OfflineUtil {


	private static final int MAX_STORE_NAME_LENGTH = 64;
	private static ManagedResourceStore store;

	/**
	 * Ready: Gears installed, Site has permission, User already did sync (User DB existed)
	 * @param userUid
	 * @return
	 */
	public static boolean isReadyForUser(Integer userUid){
		if (!checkGearsInstalled(null)) {
			return false;
		}
		if (!hasPermission()) {
			return false;
		}
		
		if(!GearsDB.isExistUser(userUid)){
			return false;
		}
		return true;
	}
	/**
	 * Check if this space for this user is synchorized
	 * @param userUid
	 * @param spaceUname
	 * @return
	 */
	public static boolean isReadyForSpace(Integer userUid, String spaceUname) {
		Log.debug("check available regarding Space " + spaceUname + " is for user " + userUid);
		if(!isReadyForUser(userUid)){
			return false;
		}
		Log.debug("Local DB is available for user");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Is software synced?
		try {
			LocalServer server = Factory.getInstance().createLocalServer();
			if (!server.canServeLocally(GwtClientUtils.getBaseUrl() + "offline.do")){
				return false;
			}
		} catch (GearsException e1) {
			return false;
		}
		Log.debug("Software is available for user");
			
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// is space DB synced?
		try {
			SpaceModel space = GearsDB.getUserDB(userUid).getSpace(spaceUname);
			if(space != null)
				return true;
		} catch (Exception e) {
			Log.error("Check sync failed for space " + spaceUname + " for user " + userUid);
		}
		return false;
	}
	/**
	 * If user already install gears and have choose "yes" for permission dialogue for this site, return yet
	 * @return
	 */
	public static boolean isAlreadyPermitForSite(){
		if (!isGearsInstalled()) {
			return false;
		}
		return hasPermission();
	}
	/**
	 * @param parent,
	 *            the message popup parent
	 */
	public static boolean checkGearsInstalled(UIObject parent) {
		if (isGearsInstalled()) {
			return true;
		}
		
		if (parent != null) {
			// Gear is not install show information popup
			DialogBox pop = new DialogBox();
			pop.setText(Msg.consts.alert());
			pop.setIcon(new Image(IconBundle.I.get().comment()));
			URLPanel frame = new URLPanel();
			pop.setWidget(frame);
			pop.showbox();
			
			//!!!Important: must setURL after popup already show up, otherwise, get doc.getElementById() no property error
			frame.setURL(GwtClientUtils.getBaseUrl() + "static/gearreqired.html");
		}
	
		return false;
	}



	/**
	 * @return
	 */
	public static UserModel getUser() {
		String username = OfflineLoginService.getLoginUsername();
		return GearsDB.getUser(username);
	}
	/**
	 * @param user
	 */
	public static void setLoginInfo(GeneralModel model) {
		UserModel user = getUser();
		((GeneralModel)model).loginUserFullname = user.getFullname();
		((GeneralModel)model).loginUsername = user.getLoginname();
		((GeneralModel)model).loginUserUid= user.getUid();
		((GeneralModel)model).isOffline = true;
		
	}


	/**
	 * @param cookieOffline
	 * @return
	 */
	public static ManagedResourceStore getManagedResourceStore(String cookieOffline) throws GearsException {
	    if (store != null) {
	      return store;
	    }

	    String storeName = GWT.getModuleName() + "_offline";
	    if (storeName.length() > MAX_STORE_NAME_LENGTH) {
	      storeName = storeName.substring(storeName.length()- MAX_STORE_NAME_LENGTH);
	    }

		LocalServer server = Factory.getInstance().createLocalServer();
	    store = server.createManagedStore(storeName,cookieOffline);
	    store.setManifestUrl(Offline.getManifestUrl());
	    store.checkForUpdate();
	    return store;
	  }

	/**
	 * @param page
	 * @return
	 */
	public static PageItemModel extractToItem(PageModel page) {
	
		PageItemModel item = new PageItemModel();
		item.uid = page.uid;
		item.uuid = page.pageUuid;
		item.title = page.title;
		item.spaceUname = page.spaceUname;
		item.modifier = page.modifier;
		item.modifierUsername = page.modifierUsername;
		item.modifierPortrait = page.modifierPortrait;
		item.modifiedDate = page.modifiedDate;
		item.version = page.pageVersion;
		item.isCurrent = !page.isHistory;
		item.type = page.type;
		return item;
	}


	public static void copyDraftStatus(List<PageItemModel> drafts, PageModel model) {
		if(drafts !=null){
			for (PageItemModel draft : drafts) {
				if(draft.type == SharedConstants.MANUAL_DRAFT){
					model.draftUid = draft.uid;
					model.draftDate = draft.modifiedDate;
				}else if(draft.type == SharedConstants.AUTO_DRAFT){
					model.autoSaveUid = draft.uid;
					model.autoSaveDate = draft.modifiedDate;
				}
			}
		}
		
	}
	/**
	 * create a temporary UUID is only for offline created page. This UUID must start with "0." This is an indicator to 
	 * different client side uuid or server side one.
	 * @param user 
	 * @return
	 */
	public static String createPageUuid(UserModel user) {
		String uuid = "" + (Math.random());
		//50 is just for failure tolerance, if really can not get an unique UUID, then just return duplicated one:(
		for(int idx=0;idx<50;idx++){
			try {
				//check if this uuid is already exist in page table
				PageModel page = GearsDB.getUserDB(user.getUid()).getCurrentPageByUuid(uuid);
				if(page == null)
					return uuid;
			} catch (Exception e) {
				Log.info("Unable to get an unquie UUID for page, return " + uuid);
				return uuid;
			}
		}
		Log.info("Unable to get an unquie UUID for page, return " + uuid);
		return uuid;
	}
	
	public static String createAttachmentUuid(GearsDB database){
		String uuid = "" + (Math.random());
		//50 is just for failure tolerance, if really can not get an unique UUID, then just return duplicated one:(
		for(int idx=0;idx<50;idx++){
			try {
				//check if this uuid is already exist in page table
				boolean has = database.hasAttachmentByNodeUuid(uuid);
				if(!has)
					return uuid;
			} catch (Exception e) {
				Log.info("Unable to get an unquie UUID for attachment, return " + uuid);
				return uuid;
			}
		}
		Log.info("Unable to get an unquie UUID for attachment, return " + uuid);
		return uuid;
	}
	
	/**
	 * convert AttachmentModel List to JSON object
	 * @param atts
	 * @return
	 */
	public static String toAttachmentsJsonObject(List<AttachmentModel> atts){
		if(atts != null){
			JSONArray arr = new JSONArray();
			for (int idx=0;idx<atts.size();idx++) {
				AttachmentModel attachment = atts.get(idx);
				arr.set(idx,OfflineUtil.toAttachmentJsonObject(attachment));
			}
			return arr.toString();
		}
		return "";
	}
	/**
	 * Convert single AttachmentModel to JSON object
	 * @param model
	 * @return
	 */
	public static JSONObject toAttachmentJsonObject(AttachmentModel model){
		JSONObject obj = new JSONObject();
		obj.put("index", new JSONString(model.index));
		obj.put("nodeUuid", new JSONString(model.nodeUuid));
		obj.put("version", new JSONString(model.version));
		obj.put("filename", new JSONString(model.filename));
		obj.put("userFullname", new JSONString(model.creator));
		obj.put("date", new JSONNumber(model.date));
		obj.put("size",  new JSONNumber(Long.valueOf(model.size)));
		obj.put("comment", new JSONString(model.desc));
		
		//skip recordVersion, it is not necessary to render
		return obj;
	}
	/**
	 * Permanent delete and uncapture file 
	 * @param userDB
	 * @param delAtt
	 */
	public static boolean deleteAttachment(GearsDB userDB, AttachmentModel delAtt) {
		try {
			Log.info("Attachment deleting:" + delAtt.nodeUuid + " on page " + delAtt.pageUuid);
			userDB.removeAttachment(delAtt.pageUuid, delAtt.nodeUuid, null,true);
			SpaceModel space = userDB.getSpace(delAtt.spaceUname);
			if(space == null){
				Log.error("Unable to get space by " + delAtt.spaceUname);
				return false;
			}
			//remove from store
			GearsServer server = new GearsServer();
			int maxVer = Integer.parseInt(delAtt.version);
			server.uncaptureFile(space.uid, delAtt.spaceUname,delAtt.filename, delAtt.nodeUuid, maxVer);
			return true;
		} catch (Exception e) {
			Log.error("Unable to delete attachment on page " + delAtt.pageUuid + "; nodeUuid " + delAtt.nodeUuid,e);
		}
		return false;
	}
	/**
	 * @return
	 */
	public static int getDefaultSyncOptions() {
		return SharedConstants.OPTION_SYNC_ATTACHMENT |SharedConstants.OPTION_SYNC_DRAFT
		| SharedConstants.OPTION_SYNC_COMMENT | SharedConstants.OPTION_SYNC_HISTORY;
	}

	//********************************************************************
	//               private method
	//********************************************************************
	/**
	 * This check won't bring up the permission allow dialog
	 * @return Returns true if the site already has permission to use Gears.
	 */
	private static native boolean hasPermission() /*-{
	     
	    return $wnd.google.gears.factory.hasPermission;

	  }-*/;
	
	/**
	 * @return
	 */
	private native static boolean isGearsInstalled() /*-{
	 	return ($wnd.window.google && $wnd.google.gears);
	}-*/;
}
