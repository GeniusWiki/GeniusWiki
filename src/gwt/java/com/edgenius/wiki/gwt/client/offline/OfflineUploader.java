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
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.google.gwt.gears.client.Factory;
import com.google.gwt.gears.client.localserver.LocalServer;
import com.google.gwt.gears.client.localserver.ResourceStore;
import com.google.gwt.user.client.ui.FileUpload;

/**
 * Capture file into offline resource store
 * @author Dapeng.Ni
 */
public class OfflineUploader{

	
	public List<String[]> items = new ArrayList<String[]>();
	public List<FileUpload> uploaders = new ArrayList<FileUpload>();

	public OfflineUploader() {
		//this is offline model, only use add(), upload() method
	}

	public void add(FileUpload up,String text) {
		String[] item = new String[]{up.getFilename(),text};
		
		//must keep them in same index for same item
		uploaders.add(up);
		items.add(item);
	}

	/**
	 * Offline upload file to local resource store. 
	 * Return uploaded items AttachmentModels JSON string
	 */
	public String upload(String spaceUname,String pageUuid,int draftStatus) {
		List<AttachmentModel> list = new ArrayList<AttachmentModel>();
		try {
			UserModel user = OfflineUtil.getUser();
			GearsDB userDB = GearsDB.getUserDB(user.getUid());
			SpaceModel space = userDB.getSpace(spaceUname);
			if(space == null){
				Log.error("Unexpected case: upload attachment by unable to find space from offline database:" + spaceUname);
				return "";
			}
			
			LocalServer server = Factory.getInstance().createLocalServer();
			ResourceStore store = server.createStore("space"+ space.uid);
			
			for (int idx=0;idx<items.size();idx++){
				FileUpload up = uploaders.get(idx);
				
				//save metadata into DB
				String[] meta = items.get(idx);
				AttachmentModel att = new AttachmentModel();
				att.filename = GwtUtils.getFileName(meta[0]);
				att.desc = meta[1];
				att.date = new Date().getTime();
				att.creator = user.getFullname();
				//??? how to get size?
				//display to "unknown"
				att.size = 0;
				att.status = draftStatus;
				att.index = up.getName().substring(4);
				
				//this method will create a new record with NodeUuid and version
				att = userDB.saveOrUpdateAttachment(spaceUname,pageUuid, att, SharedConstants.OFFLINE_UPLOAD);
				
				//upload offline file to Resource store
				store.captureFile(up.getElement(), GwtClientUtils.buildAttachmentURL(spaceUname,att.filename, att.nodeUuid, att.version, true));
				
				list.add(att);
			}
		} catch (Exception e) {
			Log.error("Upload attachment failed for page " + pageUuid + " on space " + spaceUname,e);
		}
		return OfflineUtil.toAttachmentsJsonObject(list);
		
	}
	
}
