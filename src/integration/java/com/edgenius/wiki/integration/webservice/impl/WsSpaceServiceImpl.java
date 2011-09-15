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
package com.edgenius.wiki.integration.webservice.impl;

import javax.jws.WebService;
import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.integration.WsContants;
import com.edgenius.wiki.integration.dto.WsSpace;
import com.edgenius.wiki.integration.webservice.WsSpaceService;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.ActivityLogService;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@WebService(serviceName="SpaceWebService", endpointInterface = "com.edgenius.wiki.integration.webservice.WsSpaceService", targetNamespace=WsContants.NS) 
public class WsSpaceServiceImpl implements WsSpaceService {
	private static final Logger log = LoggerFactory.getLogger(WsSpaceServiceImpl.class);
	
	private SpaceService spaceService;
	private UserReadingService userReadingService;
	private SettingService settingService;
	private ActivityLogService activityLog;
	
	public int createSpace(WsSpace wsSpace) {
		Space pSpace = new Space();
		
		wsSpace.setSpaceKey(StringUtil.trim(wsSpace.getSpaceKey()));
		wsSpace.setSpaceTitle(StringUtil.trim(wsSpace.getSpaceTitle()));
		
		if(spaceService.getSpaceByUname(wsSpace.getSpaceKey()) != null
			|| spaceService.getSpaceByTitle(wsSpace.getSpaceTitle()) != null){
			throw new WebServiceException("Space key or space title is already existed.");
		}
		
		wsSpace.copyTo(pSpace);
		WikiUtil.setTouchedInfo(userReadingService, pSpace);
		try {
			Page homepage = spaceService.createSpace(pSpace);
			if(homepage != null){
				spaceService.saveHomepage(pSpace,homepage);
				settingService.saveOrUpdateSpaceSetting(pSpace, pSpace.getSetting());
			}
			
			activityLog.logSpaceCreated(pSpace);
		} catch (Exception e) {
			log.error("Create space failed with errors during repository worksapce creating :" ,e);
			throw new WebServiceException(e);
		} 
		return pSpace.getUid();
	}

	
	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}
	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}

	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}

	public void setActivityLog(ActivityLogService activityLog) {
		this.activityLog = activityLog;
	}
	
}
