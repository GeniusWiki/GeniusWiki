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

import java.util.Date;

import com.edgenius.wiki.gwt.client.model.JsInfoModel;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * @author Dapeng.Ni
 */
public abstract class AbstractEntryPoint  implements EntryPoint{
	private static int suppress = 0;
	/**
	 * According to hidden variable to decide if page is in offline.jsp
	 * 
	 * @return
	 */
	public static boolean isOffline() {

		// hidden type
		Element offlineDiv = DOM.getElementById("offlineDiv");
		if(offlineDiv != null){
			String offlineStr = DOM.getElementAttribute(offlineDiv, "value");
	
			if ("true".equalsIgnoreCase(offlineStr))
				return true;
		}
		
		return false;
	}
	protected JsInfoModel getJsInfoModel() {
		JsInfoModel jsModel = new JsInfoModel();
		Date now = new Date();
		jsModel.time = now.getTime();
		int offset = now.getTimezoneOffset() * -1;
		int hour = offset / 60;
		int min = offset % 60 % 60;
		jsModel.timezoneOffset = hour + (min == 0?"":"."+min);
		return jsModel;
	}
	public static boolean isAllowPublicSignup(){
		return (suppress & SharedConstants.SUPPRESS.SIGNUP.getValue())  == 0;
	}
	public static boolean isAllowLogout(){
		return (suppress & SharedConstants.SUPPRESS.LOGOUT.getValue())  == 0;
	}

	/**
	 */
	public static void setSuppress(int suppress) {
		AbstractEntryPoint.suppress = suppress;
	}

}
