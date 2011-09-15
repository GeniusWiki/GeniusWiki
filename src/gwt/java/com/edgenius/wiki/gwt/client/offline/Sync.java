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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.model.CommentModel;
import com.edgenius.wiki.gwt.client.model.OfflineModel;
import com.edgenius.wiki.gwt.client.model.PageListModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.PageThemeModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.UploadModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.OfflineControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.google.gwt.gears.client.GearsException;
import com.google.gwt.gears.client.localserver.ManagedResourceStore;
import com.google.gwt.gears.offline.client.Offline;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Dapeng.Ni
 */
public class Sync {

	private List<SyncProgressListener> listeners = new ArrayList<SyncProgressListener>();
	private static final int MSG_BACK_STEP = 15;
	private static final int MSG_SEND_STEP = 5;
	
	//only upload process complete, start download process....
	//1: sync request sent complete (attachment mark to uploading...), 2: page upload, 3: delete attachment complete
	//I don't wait attachment upload complete, as it may take long time. So step==3, download process will be invoked
	private static final int STEP_UPLOAD_REQ_SENT = 1;
	private static final int STEP_PAGE_UPLOADED = 2;
	private static final int STEP_ATT_UPLOAD_MARKED= 3;
	private static final int STEP_ATT_DEL_UPLOADED= 4;
	private static final int STEP_DB_VER_CHECKED = 5;
	
	private static final int STEP_SUM = 15;
	
	private int stepOfUpload;
	private String mandatorySyncSpaceUname;
	
	//********************************************************************
	//               method
	//********************************************************************

	/**
	 * Upload and download existed offline spaces.
	 * <li>Detect if gear is installed first</li>
	 * <li>Try to upload first, as download may impact offline edited content</li>
	 * 
	 * ??? If  GearsDB.checkForUpdate() is call inside Ajax callback onSuccess(), the database can not be initialize success
	 * ??? no idea why it happen. Try-catch won't get the DB not exist error, then no new DB created.
	 *
	 */
	public void sync(UserModel user, String spaceUname) {
		if(!OfflineUtil.checkGearsInstalled(null)){
			return;
		}
		
		stepOfUpload = 0;
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//synchronise software
		syncServerResource();
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// always upload first, as following operation may reset DB.
		// upload all user offline updated, whatever it is spaces
		if(OfflineUtil.isReadyForUser(user.getUid())){
			upload(user);
		}else{
			//ugly code: skip all step for upload
			canStartDownload(user,STEP_UPLOAD_REQ_SENT);
			canStartDownload(user,STEP_PAGE_UPLOADED);
			canStartDownload(user,STEP_ATT_UPLOAD_MARKED);
			canStartDownload(user,STEP_ATT_DEL_UPLOADED);
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Database version check, if version is obsolete or DB never exist, recreate them.
		// while recreate, user(DefautDB) or space(UserDB) essential will be kept
		if(GearsDB.isNeedResetDefaultDB(user.getOfflineMainDBVersion())){
			//default DB may not exist or version is obsolete
			//whatever, try to get all users from default DB, if has, then download them again
			Map<Integer,UserEssential> existUsers = GearsDB.getUsersEssential();
			
			//put default user
			UserEssential defaultUser = new UserEssential(OfflineConstants.DEFAULT_USER_UID,user.getOfflineMainDBVersion(),null);
			existUsers.put(OfflineConstants.DEFAULT_USER_UID, defaultUser);
			
			//see if input user exist or not, if no insert it, otherwise, just keep its old value! 
			//This makes given user's userDB update can be upgrade
			if(existUsers.get(user.getUid()) == null){
				//this is new user, then put it offlineDB version to -1, so that this user DB always will be reset
				UserEssential givenUser = new UserEssential(user.getUid(),-1,null);
				existUsers.put(user.getUid(), givenUser);
			}
			
			//reset all user with new version DB
			GearsDB.resetDefaultDB(existUsers.values());
		}
		
		//check user DB
		if(GearsDB.isNeedResetUserDB(user.getUid(),user.getOfflineDBVersion())){
			try {
				GearsDB userDB = GearsDB.getUserDB(user.getUid());
				//get spaces
				Map<String,SpaceEssential> existSpaces = userDB.getSpacesEnssential();
				
				//put given space into list as well
				if(spaceUname != null){
					SpaceEssential givenSpace = new SpaceEssential(spaceUname,OfflineUtil.getDefaultSyncOptions());
					existSpaces.put(spaceUname, givenSpace);
				}
				
				//reset all data(pages/comments/attachments) tables by given user DB version
				//this user DB version upgrade at end of this method... GearsDB.saveOrUpdateUser()
				userDB.resetUserDB(existSpaces.values());
			} catch (GearsException e) {
				Log.error("Upgarde user tables failed",e);
			}
		}else{
			if(spaceUname != null){
				//check if this space is already existed. if no, save it with 0 sync_date. It will download after upload complete
				//if existed, put it to mandatory sync space field
				try {
					SpaceModel space = GearsDB.getUserDB(user.getUid()).getSpace(spaceUname);
					if(space == null){
						SpaceModel givenSpace = new SpaceModel();
						givenSpace.unixName = spaceUname;
						GearsDB.getUserDB(user.getUid()).saveOrUpdateSpace(givenSpace);
						GearsDB.getUserDB(user.getUid()).saveOptions(spaceUname, OfflineUtil.getDefaultSyncOptions());
					}else{
						mandatorySyncSpaceUname = spaceUname;
					}
					Log.info("Mandatory sync space: " + mandatorySyncSpaceUname);
				} catch (GearsException e) {
					Log.error("Failed to update user.",e);
				}
			}
		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Check update user 
		try {
			//every time, just simply update user table as user does not have update flag in server side(it even no modified date)
			GearsDB.saveOrUpdateUser(user);
		} catch (GearsException e) {
			Log.error("Failed to update user.",e);
		}

		canStartDownload(user,STEP_DB_VER_CHECKED);
		
	}
	/**
	 * Delete space DB, store etc. all offline content
	 * @param loginUser
	 * @param spaceUname
	 */
	public void disconnectSpace(UserModel loginUser, String spaceUname) {
		if(StringUtil.isBlank(spaceUname))
			return;
		
		if(!OfflineUtil.checkGearsInstalled(null)){
			return;
		}
		
		try{
			Integer uUid = loginUser==null?-1:loginUser.getUid();
			GearsDB userDB = GearsDB.getUserDB(uUid);
			GearsServer server = new GearsServer();
			removeSpace(uUid,spaceUname,userDB,server);
		} catch (GearsException e) {
			Log.error("Unable to get user local DB or server, stop offline download. ", e);
			return;
		}
		
	}
	/**
	 * Return what options used by space to do synchronise
	 * @param userUid
	 * @param spaceUname
	 * @return
	 */
	public int getSyncOptions(Integer userUid, String spaceUname) {
		try {
			return GearsDB.getUserDB(userUid).getOptions(spaceUname);
		} catch (GearsException e) {
			Log.error("Unable to get sync opttions for user " + userUid + " on space " + spaceUname,e);
		}
		
		return SharedConstants.OPTION_ALL;
	}
	public void addSyncProgressListener(SyncProgressListener listener){
		listeners.add(listener);
	}



	//********************************************************************
	//               private methods
	//********************************************************************
	/**
	 * @param user
	 */
	private void upload(UserModel user) {
		try {
			OfflineControllerAsync offlineController = ControllerFactory.getOfflineController();
			GearsDB userDB = GearsDB.getUserDB(user.getUid());
			
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			//  Upload offline edited pages &  edited/removed drafts
			
			//this also include offline drafts
			ArrayList<PageModel> pages = userDB.getOfflineUpdatedPages();
			
			//put all offline edited pages's spaces sync date into Map<spaceUname,date>
			HashMap<String,Long> syncDates = new HashMap<String, Long>();
			for (PageModel pageModel : pages) {
				Long dt = syncDates.get(pageModel.spaceUname);
				if(dt == null){
					long sdt = userDB.getSyncDate(pageModel.spaceUname);
					syncDates.put(pageModel.spaceUname,sdt);
				}
			}
			offlineController.uploadPages(pages,syncDates, new UploadPagesSyncCallback(user));
			
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			//  Upload offline uploaded/removed attachments
			
			//as attachment can be upload in page view, so it must check separately with offline edited pages
			//include removed and new uploaded attachment 
			List<AttachmentModel> atts = userDB.getOfflineUpdatedAttachments();
			 
			//deleted list only need latest version attachment on list
			Map<String,AttachmentModel> deletedAtts = new HashMap<String, AttachmentModel>();
			//added list contains all uploaded attachments
			List<AttachmentModel> addedAtts = new ArrayList<AttachmentModel>();
			
			List<AttachmentModel> uploadingAtts = new ArrayList<AttachmentModel>();
			for (AttachmentModel attachment: atts) {
				if(attachment.offlineEdited == SharedConstants.OFFLINE_DELETED){
					AttachmentModel att = deletedAtts.get(attachment.pageUuid);
					//deleted list only need latest version attachment on list
					if(att != null){
						try {
							double exVer = Double.parseDouble(att.version);
							double newVer = Double.parseDouble(attachment.version);
							if(newVer > exVer){
								deletedAtts.put(attachment.pageUuid,attachment);
							}
						} catch (Exception e) {
							Log.info("attachment does not has number verson:" + att.version + ":" + attachment.version,e);
						}
					}else{
						deletedAtts.put(attachment.pageUuid,attachment);
					}
				}else if (attachment.offlineEdited == SharedConstants.OFFLINE_UPLOAD){
					addedAtts.add(attachment);
				}else if (attachment.offlineEdited == SharedConstants.OFFLINE_SUBMISSIONING){
					uploadingAtts.add(attachment);
				}
			}
			
			if(deletedAtts.size() > 0){
				offlineController.removeOfflineDeletedAttachments(new ArrayList<AttachmentModel>(deletedAtts.values())
						, new DeleteAttachmentsSyncCallback(user));
			}else{
				//don't wait the delete attachment anymore, increase 1 step and see if can start download 
				canStartDownload(user,STEP_ATT_DEL_UPLOADED);
			}
			
			OfflineSubmissioner submissioner = new OfflineSubmissioner(user);
			submissioner.submission(addedAtts,uploadingAtts);
			canStartDownload(user,STEP_ATT_UPLOAD_MARKED);
			
			
		} catch (Exception e) {
			Log.error("Sync: upload is failed for user " + user,e);
		}
		canStartDownload(user,STEP_UPLOAD_REQ_SENT);
	}

	private void download(UserModel user){
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Download
		try {
			long syncDate = user.getServerTime() - (long)user.getDelaySyncMin()*60000L;
			//
			ArrayList<OfflineModel> needSyncSpaces  = GearsDB.getUserDB(user.getUid()).getNeedSyncSpaces(syncDate);
			if(mandatorySyncSpaceUname != null){
				boolean found = false;
				for (OfflineModel model: needSyncSpaces) {
					if(mandatorySyncSpaceUname.equalsIgnoreCase(model.spaceUname)){
						found = true;
						break;
					}
				}
				if(!found){
					SpaceModel space = GearsDB.getUserDB(user.getUid()).getSpace(mandatorySyncSpaceUname);
					OfflineModel model = new OfflineModel();
					model.spaceUname = space.unixName;
					model.options = getSyncOptions(user.getUid(), mandatorySyncSpaceUname);
					model.syncDate = space.syncDate;
					needSyncSpaces.add(model);
				}
				Log.info("Mandatory download space : " + mandatorySyncSpaceUname);
			}
			firePercent(needSyncSpaces,0);
			
			//Put space into offline DB. 
			OfflineControllerAsync offlineController = ControllerFactory.getOfflineController();
			offlineController.downloadSpaces(needSyncSpaces,new SpaceDownloadCallback(needSyncSpaces,user.getUid()));
			firePercent(needSyncSpaces,MSG_SEND_STEP);

		} catch (GearsException e) {
			Log.error("Sync: download is failed for user " + user);
		}

	}
	private void syncServerResource() {
		try {
			final SoftwareSyncProgressPopup softwareProgress  = new SoftwareSyncProgressPopup();
			this.addSyncProgressListener(softwareProgress);
			
			firePercent(OfflineConstants.SOFTWARE_NAME,0);

//			LocalServer server = new LocalServer();

//			if (server.canServeLocally(GwtUtils.getBaseUrl() + "offline.do")) {
//				Log.info("site can be access");
//				firePercent(OfflineConstants.SOFTWARE_NAME,100);
//				softwareProgress.hide();
//			} else {
			final ManagedResourceStore store = Offline.getManagedResourceStore();
			new Timer() {
				int ratio = 0;
				final String oldVersion = store.getCurrentVersion();
				public void run() {
					switch (store.getUpdateStatus()) {
					case ManagedResourceStore.UPDATE_OK:
						if (store.getCurrentVersion().equals(oldVersion)) {
							Log.info("No new version update was available.");
						} else {
							Log.info("Update to " + store.getCurrentVersion()
									+ " was completed.  Please refresh the page to see the changes.");
						}
						firePercent(OfflineConstants.SOFTWARE_NAME,100);
						softwareProgress.hide();
						break;
					case ManagedResourceStore.UPDATE_CHECKING:
					case ManagedResourceStore.UPDATE_DOWNLOADING:
						softwareProgress.open();
						schedule(500);
						ratio = increase(ratio, 1);
						firePercent(OfflineConstants.SOFTWARE_NAME,ratio);
						break;
					case ManagedResourceStore.UPDATE_FAILED:
						Log.error("Update failed: " + store.getLastErrorMessage());
						fireError(OfflineConstants.SOFTWARE_NAME,ErrorCode.OFFLINE_DOWNLOAD_ERR);
						softwareProgress.hide();
						break;
					}
				}
			}.schedule(500);

//			}
		} catch (GearsException e) {
			Log.error("Unexception to check Offline server with exception:" + e.toString(),e);
		}
	}
	/**
	 * @param userUid 
	 * @param spaceUname
	 */
	private void removeSpace(Integer userUid, String spaceUname, GearsDB database, GearsServer server) {
		
		int ratio = MSG_BACK_STEP+MSG_SEND_STEP;
		//add this space to offline Dashboard if this space does not exist in user offline Dashboard
		try {
			int spaceUid = database.removeSpace(spaceUname);
			ratio = increase(ratio,10);
			firePercent(spaceUname, ratio);
			if(spaceUid != -1){
				GearsDB.removeLayout(userUid,spaceUname);
				
				ratio = increase(ratio,10);
				firePercent(spaceUname, ratio);
				
				database.removePagesInSpace(spaceUname);
				
				ratio = increase(ratio,40);
				firePercent(spaceUname, ratio);
				
				server.removeSpaceStore(spaceUid);
				
				firePercent(spaceUname, 100);
			}
		} catch (GearsException e) {
			fireError(spaceUname, ErrorCode.OFFLINE_DOWNLOAD_ERR);
			Log.error("Remove space db and resource failed " + spaceUname,e);
		}
	}

	//********************************************************************
	//               some utility mehtods
	//********************************************************************
	
	//this method only ensure the return does not over or equals to 100
	private int increase(int ratio, int step) {
		ratio += step;
		return ratio > 99?99:ratio;
	}
	private void canStartDownload(UserModel user, int step) {
		this.stepOfUpload += step;
		Log.info("Upload step " + step + " on " + this.stepOfUpload) ;
		if(this.stepOfUpload == STEP_SUM){
			//invoke download process
			download(user);
		}
	}
	private void firePercent(String spaceUname, int ratio) {

		Log.info("Update progress for space " + spaceUname + " on percent " + ratio);
		for (SyncProgressListener listener : listeners) {
			listener.percent(spaceUname,ratio);
		}
	}
	
	private void firePercent(List<OfflineModel> spaces, int ratio) {
		for (OfflineModel space : spaces) {
			firePercent(space.spaceUname, ratio);
		}
	}
	private void fireError(String spaceUname, String errorCode) {
	
		for (SyncProgressListener listener : listeners) {
			listener.error(spaceUname,errorCode);
		}
	}


	private void fireError(List<OfflineModel> spaces, String errorCode) {
		for (OfflineModel space : spaces) {
			fireError(space.spaceUname, errorCode);
		}
	}

	//********************************************************************
	//               private class
	//********************************************************************
	private class DeleteAttachmentsSyncCallback implements AsyncCallback<ArrayList<AttachmentModel>> {
		private UserModel user;
		/**
		 * @param uploader
		 */
		public DeleteAttachmentsSyncCallback(UserModel user) {
			this.user = user;
		}
		public void onFailure(Throwable obj) {
			GwtClientUtils.processError(obj);
			
		}
		public void onSuccess(ArrayList<AttachmentModel> uploads) {
			try {
				GearsDB userDB = GearsDB.getUserDB(user.getUid());
				for (AttachmentModel delAtt: uploads) {
					OfflineUtil.deleteAttachment(userDB, delAtt);
				}
			} catch (Exception e) {
				Log.error("Unable to connect to user DB" + user.getUid(),e);
			}
			
			canStartDownload(user,STEP_ATT_DEL_UPLOADED);
		}

	}
	private class UploadPagesSyncCallback implements AsyncCallback<ArrayList<UploadModel>> {

		private UserModel user;

		public UploadPagesSyncCallback(UserModel user){
			this.user = user;
		}
		public void onFailure(Throwable obj) {
			GwtClientUtils.processError(obj);
			
		}

		public void onSuccess(ArrayList<UploadModel> uuidMap) {
			//according returned page uuid map, delete offline edit version. Then they will be sync from server soon
			for (UploadModel entry: uuidMap) {
				try {
					GearsDB.getUserDB(user.getUid()).removeOfflineEditedPage(entry.oldPageUuid,entry.pageType);
				} catch (Exception e) {
					Log.error("Unable to reset page offline update flag to 0",e);
				}
			}
			
			canStartDownload(user,STEP_PAGE_UPLOADED);
		}
		
	}
	/**
	 * Spaces and pages download from server side. 
	 */
	private class SpaceDownloadCallback implements AsyncCallback<ArrayList<OfflineModel>> {
	
		Integer userUid;
		private List<OfflineModel> spaces;

		public SpaceDownloadCallback(List<OfflineModel> spaces, Integer userUid) {
			this.userUid = userUid;
			this.spaces = spaces;
		}

		public void onFailure(Throwable obj) {
			fireError(spaces,ErrorCode.OFFLINE_DOWNLOAD_ERR);
			GwtClientUtils.processError(obj);
		}

		public void onSuccess(ArrayList<OfflineModel> list) {
			

			GearsDB userDB = null;
			GearsServer server = null;
			try{
				userDB = GearsDB.getUserDB(userUid);
				server = new GearsServer();
			} catch (GearsException e) {
				Log.error("Unable to get user local DB or server, stop offline download. ", e);
				fireError(spaces,ErrorCode.OFFLINE_DOWNLOAD_ERR);
				return;
			}
			
			//message already get, all spaces sync progress go to 10 percent
			firePercent(spaces,MSG_SEND_STEP + MSG_BACK_STEP);
			Log.info("Receive page model, size is " + list.size() + " for user " + userUid);
			for(OfflineModel offline: list){
				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				// Delete space if offline.space is null, this usually causes the space is deleted or permission change to disallow read 
				if(offline.space == null){
					removeSpace(userUid, offline.spaceUname,userDB,server);
					continue;
				}
			
				updateSpace(offline,userDB,server);
			}
		
		}



		private void updateSpace(OfflineModel offline, GearsDB userDB, GearsServer server){
			int ratio = MSG_BACK_STEP+MSG_SEND_STEP;
			
			SpaceModel space = offline.space;
			//add this space to offline Dashboard if this space does not exist in user offline Dashboard
			GearsDB.addSpaceToPortal(userUid,space.unixName);
			
			ratio = increase(ratio,10);
			firePercent(space.unixName,ratio);

			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			//update space
			int spaceUid = -1;
			try{
				spaceUid = userDB.saveOrUpdateSpace(space);
				ratio = increase(ratio,10);
				firePercent(space.unixName,ratio);
			} catch (Exception e) {
				Log.error("Unable to save or update space " + space.unixName + ". All space page update cancelled.", e);
				return;
			}
			
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// update space theme
			if(offline.theme != null){
				for (PageThemeModel pTheme : offline.theme.pageThemes) {
					try {
						userDB.saveOrUpdateTheme(space.unixName, pTheme);
					} catch (Exception e) {
						Log.error("Unable to save or update space theme " + space.unixName + ". This may cause your offline not display correctly.", e);
					}
				}
			}
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			//update pages
			PageListModel pageList = offline.pageList;
			if(pageList != null && pageList.pages != null){
				int step = 50/pageList.pages.size();
				step = step == 0?1:step;

				// this return list of page mixed auto/manual draft, history and current page
				for (PageModel page : pageList.pages) {
					try {
						userDB.saveOrUpdatePage(page,false);
						if(page.tagString != null && page.tagString.trim().length() > 0){
							userDB.saveOrUpdatePageTags(space.unixName,page.pageUuid, page.tagString);
						}
						//need check if some attachment is deleted on server side, compare with exist list and coming new list
						List<AttachmentModel> existAtts = userDB.getPageAttachments(page.pageUuid,page.type);
						for (AttachmentModel att : existAtts) {
							if(att.offlineEdited > 0){
								//may be this is uploading attachment
								continue;
							}
							boolean alive = false;
							if(page.attachmentNodes !=null){ //no any attachment download, then delete all local attachments
								//check if this exist attachments is in new list, if no, then delete it from local
								for (AttachmentModel newAtt : page.attachmentNodes) {
									if(newAtt.equals(att)){
										alive = true;
									}
								}
							}
							if(!alive){
								//delete it!
								Log.info("Attachment is removed from server side, client delete also:" + att);
								OfflineUtil.deleteAttachment(userDB, att);
							}
						}
						if (page.attachmentNodes != null) {
							//add download attachments to local DB
							for (AttachmentModel att : page.attachmentNodes) {
								//inject attachment type from page
								att.status = page.type;
								userDB.saveOrUpdateAttachment(space.unixName,page.pageUuid,att,SharedConstants.OFFLINE_DOWNLOAD_FROM_SERVER);
							}
						}
						if (page.commentList != null) {
							for (CommentModel comment : page.commentList) {
								userDB.saveOrUpdateComment(space.unixName,comment);
							}
						}
						// download page creator,modifier portrait picture, page attachment if required
						server.downloadPageResource(spaceUid, space.unixName, page, !(page.attachmentNodes ==null));
					} catch (Exception e) {
						Log.error("Unable to save page " + page.title + " on space " + page.spaceUname, e);
					}
					ratio = increase(ratio,step);
					firePercent(space.unixName,ratio);
				}
			}
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// Delete non-existed page : compare returned list and exist list, if exist is not in returned list, then delete
			if(offline.existPages != null && offline.existPages.size() > 0){
				List<String> currList = userDB.getPagesUuidInSpace(space.unixName);
				List<String> delList = new ArrayList<String>();
				for (String uuid : currList) {
					if(!offline.existPages.contains(uuid)){
						delList.add(uuid);
					}
				}
				for (String uuid : delList) {
					userDB.removePages(uuid);
				}
			}
			
			firePercent(space.unixName, 100);
		}
	}

}
