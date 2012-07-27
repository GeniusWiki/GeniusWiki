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
package com.edgenius.wiki.service;

import com.edgenius.core.GlobalSetting;
import com.edgenius.core.Installation;
import com.edgenius.core.Server;
import com.edgenius.core.UserSetting;
import com.edgenius.core.model.Configuration;
import com.edgenius.core.model.User;
import com.edgenius.wiki.InstanceSetting;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.model.Space;

/**
 * Current system has 3 type setting:
 * 
 * <li>Server setting, which saving into classpath:server.properties file, 
 * 	which could be modify by system admin but only efficient after server restart.</li>
 * <li>Global setting, which saving into Global.xml which name is declared in classpath:geniuswiki/geniuswiki.properties file. All fields
 * will synchronize to <code>com.edgenius.core.Global</code> as static variable.</li>
 * <li>User setting, which saving into <code>edgenius_users</code> database table. </li>
 * 
 * For above reasons, this interface won't provide getGlobalSetting() method because it is in static fields. Further, user setting
 * will read out following <code>com.edgenius.core.User</code>, so no reading method as well.
 *  
 * @author Dapeng.Ni
 */
public interface SettingService {
	String SERVICE_NAME= "settingService";
	//***********************************************************
	// Service methods
	//***********************************************************
	
	GlobalSetting getGlobalSetting();
	void saveOrUpdateGlobalSetting(GlobalSetting setting) throws SettingServiceException;
	
	Server getServerSetting();
	InstanceSetting getInstanceSetting();
	void saveOrUpdateInstanceSetting(InstanceSetting setting);
	void saveOrUpdateServerSetting(Server server) throws SettingServiceException;
	
	Configuration saveOrUpdateUserSetting(User user, UserSetting setting);
	Configuration saveOrUpdateSpaceSetting(Space space, SpaceSetting setting);
	/**
	 * This method will be execute after restore from backup system. It clean some cache variables. 
	 */
	void resetSetting();
	/**
	 * @param install
	 */
	void saveInstallation(Installation install);
	
}
