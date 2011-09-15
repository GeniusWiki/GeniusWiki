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
package com.edgenius.wiki.gwt.client.server;

import java.util.ArrayList;
import java.util.HashMap;

import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.model.OfflineModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.UploadModel;
import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * @author Dapeng.Ni
 */
public interface OfflineControllerAsync extends RemoteServiceAsync {

	/**
	 * This method will update space and its pages (may multiple spaces).
	 * @param spaceUname
	 * @param username
	 * @param pageSyncCallback
	 */
	void downloadSpaces(ArrayList<OfflineModel> offlineSpaces, AsyncCallback<ArrayList<OfflineModel>> callback);

	/**
	 * Return map of success uploaded page old uuid(create in client side, if new page) and new uuid(create in server side)
	 * @param pages
	 */
	void uploadPages(ArrayList<PageModel> pages, HashMap<String,Long> lastSyncTime, AsyncCallback<ArrayList<UploadModel>> callback);

	/**
	 * @param atts
	 */
	void removeOfflineDeletedAttachments(ArrayList<AttachmentModel> atts, AsyncCallback<ArrayList<AttachmentModel>> callback);

}
