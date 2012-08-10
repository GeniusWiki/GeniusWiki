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


/**
 * @author dapeng
 */
public class Constants {

	//********************************************************************
	//               Just for KEY of name, used in request.getParameter() or request.setAttribute() etc.
	//********************************************************************
	public static final String ATTR_CONFIG = "globalConfig";
	public final static String ATTR_USER="user";
	
	public final static String TABLE_PREFIX="EDG_";
	//!!! There is same value at WikiConstants
	public static final String INSTANCE_NAME = "$instance$";
	
	//********************************************************************
	//               I18N key 
	//********************************************************************


	public static final String I18N_ANONYMOUS_USER = "anonymous.user.name";
	public static final String UTF8 = "UTF-8";
	
	//HttpSession value name, timezone and text direction in HTML
	public static final String TIMEZONE = Constants.class.getName()+ ".timezone";
	public static final String DIRECTION = Constants.class.getName()+".direction";
	
	/** Key used in request to get the required direction. Used by the HTML tag */
	public static final String DIRECT_LEFT = "LEFT";
	public static final String DIRECT_RIGHT = "RIGHT";
	
	////same value with SharedConstants.ROLE_TYPE_ALL = -1; 
	public static final int ROLE_TYPE_ALL = -1;
	//********************************************************************
	// Available theme name, Future, it will loaded from Database  edgenius_themes table
	// Currently, just use constant
	//********************************************************************
	public static final String SkinDir = "skins";
	public static final String ThemesDir = "themes";
	public static final String FACEBOOK_SKIN = "facebook";
	public static final String SE_ROBOT = "SEROBOT";
	
	//!!!There is same constants in SharedContants.PORTLET_SEP
	public static final String PORTLET_SEP = "$";
	//!!!suppress functions - There is exactly same value in SharedConstants.java
	public static enum SUPPRESS{
		SIGNUP(1),
		LOGOUT(1<<1);
		
		private int value;
		private SUPPRESS(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}
	}
}
