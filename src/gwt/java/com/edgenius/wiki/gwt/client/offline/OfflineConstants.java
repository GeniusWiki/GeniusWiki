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

/**
 * 
 * @author Dapeng.Ni
 */
public class OfflineConstants {

	//this name use server side default space Uname, so that it can ensure never duplicate with others space
	public static final String SOFTWARE_NAME = "$SYSTEM$";
	
	public static final String DEFAULT_DB = "edgenius_geniuswiki";
	public final static String DATABASE_NAME_PREFIX="edgenius_";

	public static final String COOKIE_LOGIN = "edgenius-login";
	public static final String COOKIE_OFFLINE = "edgenius-offline=true";
	
	//server side node won't start by "0." 
	public static final String OFFLINE_UUID_PREFIX = "0.";

	public static final String DIV_OFFLINE_ATTACHMENT = "offlineAttachmentDiv";

	public static final int DEFAULT_USER_UID = -2;
}
