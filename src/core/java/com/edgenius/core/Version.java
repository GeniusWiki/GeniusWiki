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
package com.edgenius.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.edgenius.core.util.FileUtil;

/**
 * @author Dapeng.Ni
 */
public class Version {
	public static String VERSION = null;
	public static Date RELEASE_DATE;
	
	//max limit of user from license. -1 is unlimited.
	public static int USER_LIMITED;
	//1-user exceed limited; 2-install version is newer than expired date; 3 -invalid license; 4 - others
	public static int LICENSE_STATUS = 0;
	//how many left from license user limit. -1 is unlimited.
	public static int LEFT_USERS = 0;
	
	
	//!!! this is not for checking real time user amount - it only set when system existed users is over limited, and it won't be 
	//a real-time value - i.e., it won't change if user added or removed.
	//and it is only for license-expired.jsp to valid new input license.  
	public static int exist_user_amout;
	static{
		try {
			Properties prop = FileUtil.loadProperties("classpath:geniuswiki/version.properties");
			Version.VERSION = prop.getProperty("geniuswiki.version");
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			Version.RELEASE_DATE = format.parse(prop.getProperty("release.date"));
		} catch (Exception e) {
			throw new SystemInitException("Failed to detect system vesion from version.properties.",e);
		}
	}
}
