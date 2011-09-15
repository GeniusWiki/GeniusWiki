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

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.UserSetting;
import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.integration.WsContants;
import com.edgenius.wiki.integration.dto.UserList;
import com.edgenius.wiki.integration.webservice.WsDashboardService;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.widget.SpaceWidget;

/**
 * @author Dapeng.Ni
 */
@WebService(serviceName="DashboardWebService", endpointInterface = "com.edgenius.wiki.integration.webservice.WsDashboardService", targetNamespace=WsContants.NS)
public class WsDashboardServiceImpl implements WsDashboardService {
	private static final Logger log = LoggerFactory.getLogger(WsDashboardServiceImpl.class);
	private UserReadingService userReadingService;
	private SettingService settingService;
	private SpaceService spaceService;
	
	public String attachSpaceToUsers(String spaceKey, UserList usernameList) {
		if(usernameList == null || usernameList.getUsername() == null || usernameList.getUsername().size() == 0){
			throw new WebServiceException("No user name on request");
		}
		Space space = spaceService.getSpaceByUname(spaceKey);
		if(space == null){
			throw new WebServiceException("Space ["+spaceKey+"] does not exist");
		}
		
		String widget = SpaceWidget.class.getName() + SharedConstants.PORTLET_SEP + spaceKey + SharedConstants.PORTLET_SEP + 0 + SharedConstants.PORTLET_SEP  + 0;
		StringBuffer msg = new StringBuffer();
		for (String username : usernameList.getUsername()) {
			//reload user from Database rather than Cache. 
			User user = userReadingService.getUserByName(username);
			if(user == null){
				msg.append("User ["+username+"] does not exist.\n");
			}
			UserSetting setting = user.getSetting();
			List<String> list;
			if(setting != null){
				list = setting.getHomeLayout();
				if(list == null)
					list = new ArrayList<String>();
			}else{
				setting = new UserSetting();
				list = new ArrayList<String>();
			}
			list.add(widget);
			setting.setHomeLayout(list);
			settingService.saveOrUpdateUserSetting(user, setting);
			log.info("User [{}] dashboard add space {}.", username, spaceKey);
		}
		if(msg.length() == 0)
			msg.append("All users' dashboard updated successfully");
			
		return msg.toString();
	}

	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}

	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

}
