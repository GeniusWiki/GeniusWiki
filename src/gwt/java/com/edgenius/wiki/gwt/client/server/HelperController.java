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

/**
 * @author Dapeng.Ni
 */
public interface HelperController extends RemoteService{
	String MODULE_ACTION_URI = "helper.rpcs";


	public boolean sendFeedback(FeedbackModel feedback);
	
	public InvitationModel sendInvitation(InvitationModel invitation);
	
	public String getPageTitleByAttachmentNodeUuid(String spaceUname, String nodeUuid);
	
	Integer notifyPinPanelStatus(int panelID, boolean visible);
	
	PortletModel getActivityLogs(int currentPageNum);
}
