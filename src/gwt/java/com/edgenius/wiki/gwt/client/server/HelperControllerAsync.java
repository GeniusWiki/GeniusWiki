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

import com.edgenius.wiki.gwt.client.model.FeedbackModel;
import com.edgenius.wiki.gwt.client.model.InvitationModel;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.model.UploadProgressModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Dapeng.Ni
 */
public interface HelperControllerAsync extends RemoteServiceAsync{
	
	public void checkUploadingStatus( AsyncCallback<UploadProgressModel> callback);
	public void sendFeedback(FeedbackModel feedback, AsyncCallback<Boolean> callback);
	public void sendInvitation(InvitationModel invitation , AsyncCallback<InvitationModel> callback);
	/**
	 * TODO: if attachment could be shared in pages, this method becomes impossible, so far(25/06/08) it is true to return one page
	 * by an attachment nodeUuid;
	 * @param itemUid
	 * @param asyncCallback
	 */
	public void getPageTitleByAttachmentNodeUuid(String spaceUname,String nodeUuid, AsyncCallback<String> callback);

	/**
	 * @param panelID - see SharedConstants.TAB_TYPE_*
	 * @param visible
	 * @param sidebarAsync
	 */
	void notifyPinPanelStatus(int panelID, boolean visible, AsyncCallback<Integer> asyncCallback);
	
	void getActivityLogs(int currentPageNum, AsyncCallback<PortletModel> callback);
}
