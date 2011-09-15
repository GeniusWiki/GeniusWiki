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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.page.widgets.AttachmentPanel;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.google.gwt.gears.client.GearsException;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A Form to submission offline captured files to server side. 
 *   
 * @author Dapeng.Ni
 */
public class OfflineUploadFormPanel  extends SimplePanel  implements SubmitCompleteHandler{
	private VerticalPanel panel = new VerticalPanel();
	private FormPanel uploadForm = new FormPanel();
	private Hidden spaceUnameHidden = new Hidden("spaceUname");
	private Hidden pageUuidHidden = new Hidden("pageUuid");
	private Hidden draftHidden = new Hidden("draft");
	private int uploadingIndex=0;

	private Map<String,AttachmentModel> requestList = new HashMap<String, AttachmentModel>();
	private UserModel loginUser;
	
	public OfflineUploadFormPanel() {
		String baseUrl = GwtClientUtils.getBaseUrl();

		uploadForm.setAction(baseUrl + AttachmentPanel.MODULE_ACTION_URI);
		uploadForm.setMethod(FormPanel.METHOD_POST);
		uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		uploadForm.addSubmitCompleteHandler(this);
		
		panel.add(spaceUnameHidden);
		panel.add(pageUuidHidden);
		panel.add(draftHidden);
		
		uploadForm.setWidget(panel);
		this.setWidget(uploadForm);
		
	}

	public void bindUploadPage(String spaceUname, String pageUuid, String draftStatus){
		//set values
		spaceUnameHidden.setValue(spaceUname);
		pageUuidHidden.setValue(pageUuid);
		draftHidden.setValue(draftStatus);
	}
	/**
	 * use by offline model to submission offline files. need to keep consistent with  addUploderItem();
	 * @return 
	 */
	public Element bindUploadItem(String filename, String comment,boolean isShared, String nodeUuid, String version){

		final HorizontalPanel itemPanel = new HorizontalPanel();
		//ID still important as it will transfer back from server side as map key. 
		String id = Integer.valueOf(++uploadingIndex).toString();

		//put upload attachment to cache, so that could map back to offline item
		AttachmentModel model = new AttachmentModel();
		model.nodeUuid = nodeUuid;
		model.version = version;
		Log.info("Put attachment "+model+" to cache " + uploadingIndex);
		requestList.put(id,model);
		
		Element link;
		if(GwtClientUtils.isIE()){
			link = DOM.createElement("link");
			DOM.appendChild(itemPanel.getElement(), link);
			DOM.setElementAttribute(link, "name", "file" + id);
		}else{
			FileUpload upload = new FileUpload();
			link = upload.getElement();
			itemPanel.add(upload);
			upload.setName("file" + id);
		}
		final Hidden name = new Hidden();
		final Hidden desc = new Hidden();
		final Hidden shared = new Hidden();

		//filename could not put back into <input type="file"> tag again, so put it into a hidden variable
		name.setName("name" + id);
		desc.setName("desc" + id);
		shared.setName("shar" + id);
		
		itemPanel.add(name);
		itemPanel.add(desc);
//		itemPanel.add(shared);
		
		panel.add(itemPanel);
		

		name.setValue(filename);
		desc.setValue(comment);
//		shared, no use at moment
		return link;
		
	}
	public void submitForm(UserModel loginUser) {
		this.loginUser = loginUser;
		uploadForm.submit();
	}

	public void onSubmitComplete(SubmitCompleteEvent event) {
		Log.info("offline file submit complete");
		String results = GwtClientUtils.getFormResult(event);
		if(results == null)
			return;
		
		List<AttachmentModel> list = AttachmentPanel.parseAttachmentJSON(results);
		
		//server side upload does not bring back spaceUname and pageUuid, reset them here
		//need reset these uploaded files, so that they won't upload again
		try {
			GearsDB userDB = GearsDB.getUserDB(loginUser.getUid());
			String spaceUname = spaceUnameHidden.getValue();
			String pageUuid = pageUuidHidden.getValue();
			SpaceModel space = userDB.getSpace(spaceUname);
			if(space != null){
				for (AttachmentModel att : list) {
					//!!!Please note, these attachment nodeUuid and version already update to server side value. 
					//Here it has to update by attachment index
					//only can remove this from panel after upload complete
					AttachmentModel oldAtt = requestList.get(att.index);
					//delete oldAtt and insert new att(server side value)
					if(oldAtt != null){
						try {
							userDB.removeAttachment(pageUuid, oldAtt.nodeUuid,oldAtt.version, true);
						} catch (GearsException e) {
							Log.error("Unable to delete uploaded attachment " + oldAtt.nodeUuid + ":ver:" + oldAtt.version,e);
						}
						
						GearsServer server = new GearsServer();
						server.renamecaptureFile(space.uid, spaceUname,oldAtt.filename, oldAtt.nodeUuid,oldAtt.version
								, att.filename, att.nodeUuid, att.version);
					}else{
						Log.error("unexpected: can not get mapped attachment from request uploading map on index " + att.index);
					}
					//save current new attachment, and rename capture file url
					try {
						userDB.saveOrUpdateAttachment(spaceUname, pageUuid, att, SharedConstants.OFFLINE_DOWNLOAD_FROM_SERVER);
					} catch (GearsException e) {
						Log.error("Unable to update uploading attachment " + att.nodeUuid + ":ver:" + att.version,e);
					}
					
				}
			}else{
				Log.error("unabel to find sapce " + spaceUname + "; attachment uploaded handle is skipped");
			}
		} catch (GearsException e1) {
			Log.error("Failed update uploaded attachment",e1);
		}
		
		RootPanel.get(OfflineConstants.DIV_OFFLINE_ATTACHMENT).remove(this);
		
	}
}
