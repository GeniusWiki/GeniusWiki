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

import com.edgenius.wiki.security.ValidateMethod;

/**
 * This class is just contains dummy methods without any function. 
 * Because some methods is not suitable to intercept by MethodSecurityIntercepor, so it need 
 * manually check some permissions by call these method to invoke <code>MethodSecurityInterceptor</code>.<br>
 *  
 * As example case, if login user session expire, if Instance is forbidden reading,  <code>HomeControllerImpl.getHomePage()</code> 
 * does not contain any methods which could invoke <code>MethodSecurityInterceptor</code>. If user click Dashboard link, then  
 * anonymous user still can view the blank page although it does not break system security. 
 * @author Dapeng.Ni
 */
@ValidateMethod
public class SecurityDummy {
	public static final String checkInstanceRead = "checkInstanceRead";
	public static final String checkInstanceAdmin = "checkInstanceAdmin";
	public static final String checkSpaceAdmin = "checkSpaceAdmin";
	public static final String checkSpaceWrite = "checkSpaceWrite";
	public static final String checkSpaceRead = "checkSpaceRead";
	public static final String checkSpaceOffline = "checkSpaceOffline";
	public static final String checkPageRestrict = "checkPageRestrict";
	public static final String checkPageRead = "checkPageRead";
	
	public void checkInstanceRead() {}
	public void checkInstanceAdmin() {}
	
	public void checkSpaceRead(String spaceUname) {}
	public void checkSpaceWrite(String spaceUname) {}
	public void checkSpaceAdmin(String spaceUname) {}
	public void checkSpaceOffline(String spaceUname) {}
	
	public void checkPageRead(String spaceUname, String pageUuid) {}
	public void checkPageRestrict(String spaceUname, String pageUuid) {}

}
