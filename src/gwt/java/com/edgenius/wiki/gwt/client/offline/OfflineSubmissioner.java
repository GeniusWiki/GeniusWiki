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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.google.gwt.dom.client.Element;
import com.google.gwt.gears.client.Factory;
import com.google.gwt.gears.client.GearsException;
import com.google.gwt.gears.client.localserver.FileSubmitter;
import com.google.gwt.gears.client.localserver.LocalServer;
import com.google.gwt.gears.client.localserver.ResourceStore;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Submit offline captured file into server side.
 * @author Dapeng.Ni
 */
public class OfflineSubmissioner {

	private UserModel loginUser;

	/**
	 * @param user : this user is not offline user(read from cookie), it is online system current login user!!!
	 */
	public OfflineSubmissioner(UserModel user) {
		this.loginUser = user;
	}
	/**
	 * Upload all offline uploaded attachment to server side
	 * @param addedAtts 
	 * @param uploadingAtts 
	 * @throws GearsException 
	 */
	public void submission(List<AttachmentModel> addedAtts, List<AttachmentModel> uploadingAtts)
		throws GearsException{
		
		GearsDB userDB = GearsDB.getUserDB(loginUser.getUid());
		//check each uploading files, determine if it need retry again...
		for (AttachmentModel att : uploadingAtts) {
			if((new Date().getTime() - att.submissionDate) > 10 * 60000){
				//put it to addedAtts
				if(att.submissionRetry > 5){
					Log.info("Upload failed too many times, stop retry: " + att.nodeUuid + " on page " + att.pageUuid + " on space " + att.spaceUname);
					//TODO: need send some message to let user know
					OfflineUtil.deleteAttachment(userDB, att);
				}else{
					addedAtts.add(att);
				}
			}
		}
		
		//need update status of these uploading file. They maybe big size and time consuming. So if it keep 
		//original status, system may upload same file again as BaseEntryPoint.LoginAsync{} set, system will
		//do sync check every 30 minutes.... Here will check submission date, and if it is over maximum (10 hours), then 
		//retry, if retry time is over 5, then give up (delete it from offline store)
		for (AttachmentModel att : addedAtts) {
			Log.info("Attachment to try uploading:" + att.nodeUuid + " on page " + att.pageUuid + " on space " + att.spaceUname);
			att.submissionRetry++;
			att.submissionDate = new Date().getTime();
			userDB.updateAttachmentStatus(att.nodeUuid,att.version, SharedConstants.OFFLINE_SUBMISSIONING);
		}

		if(addedAtts.size() == 0){
			return;
		}
		
		//sort same spaceUname, pageUuid and draftStatus
		Map<AttachmentKey, List<AttachmentModel>> pageAtts = new HashMap<AttachmentKey, List<AttachmentModel>>(); 
		for (AttachmentModel att : addedAtts) {
			AttachmentKey key = new AttachmentKey(att.spaceUname,att.pageUuid,att.status);
			List<AttachmentModel> list = pageAtts.get(key);
			if(list == null){
				list = new ArrayList<AttachmentModel>();
				pageAtts.put(key, list);
			}
			list.add(att);
		}
		
		LocalServer server = Factory.getInstance().createLocalServer();
		//submit by space,page and its draftStatus
		for (Entry<AttachmentKey, List<AttachmentModel>> entry : pageAtts.entrySet()) {
			
			SpaceModel space = userDB.getSpace(entry.getKey().spaceUname);
			OfflineUploadFormPanel panel = new OfflineUploadFormPanel();
			panel.setVisible(false);
			//bind it to page
			RootPanel.get(OfflineConstants.DIV_OFFLINE_ATTACHMENT).add(panel);
			
			panel.bindUploadPage(entry.getKey().spaceUname,entry.getKey().pageUuid,""+entry.getKey().draftStatus);
			
			for (AttachmentModel att : entry.getValue()) {
				//don't upload nodeUuid and version, as it need decide by server side
				String url = GwtClientUtils.buildAttachmentURL(att.spaceUname,att.filename, att.nodeUuid, att.version, true);
				Element ele = panel.bindUploadItem(att.nodeUuid,att.desc,false,att.nodeUuid,att.version);
				

				ResourceStore store = server.openStore("space"+ space.uid);
				
				FileSubmitter submitter = store.createFileSubmitter();
				submitter.setFileInputElement(ele, url);
			}
			
			panel.submitForm(loginUser);
		}
		
	}

	//********************************************************************
	//               class
	//********************************************************************
	private class AttachmentKey{
		String spaceUname;
		String pageUuid;
		int draftStatus;
		/**
		 * @param spaceUname2
		 * @param pageUuid2
		 * @param status
		 */
		public AttachmentKey(String spaceUname, String pageUuid, int status) {
			this.spaceUname = spaceUname;
			this.pageUuid = pageUuid;
			this.draftStatus = status;
		}
		public boolean equals(Object obj){
			if(!(obj instanceof AttachmentKey))
				return false;
			AttachmentKey aobj= (AttachmentKey) obj;
			if(spaceUname.equalsIgnoreCase(aobj.spaceUname) && pageUuid.equals(aobj.pageUuid) && draftStatus == aobj.draftStatus)
				return true;
			
			return false;
		}
		public int hashCode(){
			return (spaceUname==null?0:spaceUname.toUpperCase().hashCode()) 
			+ (pageUuid==null?0:pageUuid.toUpperCase().hashCode())
			+ draftStatus; 
		}
	}
}
