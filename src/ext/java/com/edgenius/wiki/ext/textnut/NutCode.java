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
package com.edgenius.wiki.ext.textnut;

/**
 * @author Dapeng.Ni
 */
public class NutCode {
	
	//success
	public static final int PAGE_CREATED = 200;
	public static final int PAGE_UPDATED = 201;
	public static final int PAGE_DELETED = 202;
	
	public static final int SPACE_CREATED = 211;
	public static final int SPACE_FOUND = 212;
	
	public static final int AUTHENTICATION_SUCCESS = 220;
	
	//warning
	public static final int PAGE_DUPLICATED_TITLE = 300;
	
	//error
	public static final int PAGE_CREATED_FAILED = 401;
	public static final int PAGE_UPDATE_FAILED = 402;
	public static final int PAGE_DELETE_FAILED = 403;
	
	public static final int SPACE_CREATED_FAILED = 410;
	public static final int SPACE_NOT_FOUND = 411;
	public static final int SPACE_DUPLICATED_ERROR = 412;
	public static final int SPACE_WRITE_PERMISSION_DENIED = 413;
	
	public static final int AUTHENTICATION_ERROR = 420;
	public static final int AUTHORIZATION_ERROR = 421;
	public static final int USER_NOT_FOUND = 423;
	
	public static final int UNKNOWN_ERROR = 440;
	public static final int UNKNOWN_ACTION = 441;
	
}
