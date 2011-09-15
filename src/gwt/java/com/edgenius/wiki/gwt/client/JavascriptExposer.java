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
package com.edgenius.wiki.gwt.client;

import java.util.Arrays;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.user.UserPopup;
import com.google.gwt.user.client.ui.Widget;

/**
 * I want to create a common class which methods can be run in native JS, but now, it is not completed.
 * It looks sucks code....
 * @author Dapeng.Ni
 */
public class JavascriptExposer {

	/**
	 * @param method
	 * @param params
	 * @param sender
	 */
	public static void run(String method, String[] params, Widget sender) {
		if(SharedConstants.JS_USER_POP.equals(method)){
			if(params.length == 3){
				//username,userFullname,portrait
				new UserPopup(sender, PageMain.getSpaceUname(),params[0],params[2]);
			}else{
				Log.error("Unable invoke method [" + method + "] by parameters [" + Arrays.toString(params) + "].");
			}
		}
		
	}

	public static void ajax(String method, String[] params){
		
	}
}
