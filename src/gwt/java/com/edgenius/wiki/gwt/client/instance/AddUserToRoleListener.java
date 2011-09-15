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
package com.edgenius.wiki.gwt.client.instance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.ListDialogueListener;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Dapeng.Ni
 */
public class AddUserToRoleListener  implements ListDialogueListener, AsyncCallback<Integer>{
	//DO NOT use Integer! this is come from javascript then goes to server side by RPC call, 
	//if Integer, it will cause error (JavaScriptObject can not be serialized)
	private int roleUid;

	/**
	 * @param roleUid
	 */
	public AddUserToRoleListener(int roleUid) {
		this.roleUid = roleUid;
	}

	public void dialogClosed(DialogBox sender, List keyValueList) {
		
		ArrayList<String> usernameList  = new ArrayList<String>();
		for(Iterator iter = keyValueList.iterator();iter.hasNext();){
			UserModel user = (UserModel) iter.next();
			//use default operation: all false
			usernameList.add(user.getLoginname());
		}
		
		//TODO: now the page has a stun period - nothing display until ajax call back -- need a busy page? 
		if(usernameList.size() > 0){
			//add this user to role 
			SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
			securityController.addUsersToRole(roleUid, usernameList, this);
		}
	}
	public void onFailure(Throwable caught) {
		GwtClientUtils.processError(caught);
		
	}

	public void onSuccess(Integer result) {
		if(result == null){
			//TODO: there are some error;
			return;
		}
		//this will call JSP page javascript then refresh roleUserDetail page
		userToRoleAdded(roleUid);
	}
	
	public static native void userToRoleAdded(Integer roleUid)/*-{
	   $wnd.userToRoleAdded(roleUid);
	}-*/;

}
