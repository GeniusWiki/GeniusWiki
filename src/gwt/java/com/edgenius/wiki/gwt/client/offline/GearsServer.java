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

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.google.gwt.gears.client.Factory;
import com.google.gwt.gears.client.GearsException;
import com.google.gwt.gears.client.localserver.LocalServer;
import com.google.gwt.gears.client.localserver.ResourceStore;
import com.google.gwt.gears.client.localserver.ResourceStoreUrlCaptureHandler;

/**
 * @author Dapeng.Ni
 */
public class GearsServer {
	
	public void downloadSpaceResource(int spaceUid, SpaceModel space) throws GearsException{
		
		LocalServer server = Factory.getInstance().createLocalServer();
		ResourceStore store = server.createStore("space"+spaceUid);
		String[] urls = new String[]{space.largeLogoUrl,space.smallLogoUrl};
		store.capture(new ResourceStoreUrlCaptureHandler(){
			public void onCapture(ResourceStoreUrlCaptureEvent event) {
				if(event.isSuccess())
					Log.info("Succ get space resource:" + event.getCaptureId() + "::" + event.getCaptureId());
				else
					Log.error("Failed get space resource:" + event.getCaptureId() + "::" + event.getCaptureId());
				
			}
			
		},urls);
	}

	/**
	 * @param spaceUid 
	 * @param page
	 * @return
	 * @throws GearsException
	 */
	public void downloadPageResource(int spaceUid, String spaceUname, PageModel page, boolean withAttachment) throws GearsException {
		String[] urls;
		if(withAttachment){
			int size = 2;
			if(page.attachmentNodes != null)
				size += page.attachmentNodes.size();
			urls= new String[size];
			urls[0]=page.creatorPortrait;
			urls[1]=page.modifierPortrait;
			for(int idx=2;idx<size;idx++){
				//all attachment of page
				AttachmentModel att = page.attachmentNodes.get(idx-2);
				urls[idx]=GwtClientUtils.buildAttachmentURL(spaceUname,att.filename, att.nodeUuid,att.version,true);
			}
		}else{
			urls= new String[]{page.creatorPortrait,page.modifierPortrait};
		}
		LocalServer server = Factory.getInstance().createLocalServer();
		//use spaceUid(local DB Uid) as store name suffix. DON'T use SpaceUname, it may contain characters which is illegal for directory
		ResourceStore store = server.createStore("space"+ spaceUid);
		store.capture(new ResourceStoreUrlCaptureHandler(){
			public void onCapture(ResourceStoreUrlCaptureEvent event) {
				if(event.isSuccess())
					Log.info("Succ get page resource:" + event.getCaptureId() + "::" + event.getCaptureId());
				else
					Log.error("Failed get page resource:" + event.getCaptureId() + "::" + event.getCaptureId());
				
			}
		},urls);
	}
	/**
	 * Delete all versions by given nodeUuid
	 * @param spaceUid
	 * @param spaceUname
	 * @param nodeUuid
	 * @param versions
	 * @throws GearsException
	 */
	public void uncaptureFile(int spaceUid, String spaceUname, String fileName, String nodeUuid, int maxVersion) throws GearsException {
		LocalServer server = Factory.getInstance().createLocalServer();
		ResourceStore store = server.openStore("space"+ spaceUid);
		
		for(int ver=1; ver<=maxVersion;ver++){
			store.remove(GwtClientUtils.buildAttachmentURL(spaceUname, fileName, nodeUuid,ver+"", true));
		}
	}
	public void renamecaptureFile(int spaceUid,String  spaceUname,String  fromFilename, String  fromNodeUuid, String fromVersion,
			String  toFilename, String toNodeUuid, String  toVersion) throws GearsException{
		LocalServer server = Factory.getInstance().createLocalServer();
		ResourceStore store = server.openStore("space"+ spaceUid);
		
		String srcURL = GwtClientUtils.buildAttachmentURL(spaceUname,fromFilename, fromNodeUuid,fromVersion,true);
		String destURL = GwtClientUtils.buildAttachmentURL(spaceUname,toFilename, toNodeUuid,toVersion,true);
		store.rename(srcURL, destURL);
		
		//just assume all attachment may be preview, actually, only image has this chance, but I still  
		//prefer to put all preview URL here to avoid any accident...
		String srcPreviewURL = GwtClientUtils.buildAttachmentURL(spaceUname,fromFilename, fromNodeUuid,fromVersion,false);
		String destPreviewURL = GwtClientUtils.buildAttachmentURL(spaceUname,toFilename, toNodeUuid,toVersion,false);
		store.rename(srcPreviewURL, destPreviewURL);
		
	}
	/**
	 * @param spaceUid
	 * @throws GearsException 
	 */
	public void removeSpaceStore(int spaceUid) throws GearsException {
		LocalServer server = Factory.getInstance().createLocalServer();
		server.removeStore("space"+ spaceUid);
		
	}
}
