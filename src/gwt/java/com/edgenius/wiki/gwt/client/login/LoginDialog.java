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
package com.edgenius.wiki.gwt.client.login;

import com.edgenius.wiki.gwt.client.AbstractEntryPoint;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Dapeng.Ni
 */
public class LoginDialog extends DialogBox implements UserCreateListener{
	
	public static final int LOGIN = 0;
	public static final int FORGET_PASSWORD = 1;
	public static final int SINGUP = 2;
	
	public LoginDialog(int loginOrSignup){
		
		if(loginOrSignup == SINGUP && !AbstractEntryPoint.isAllowPublicSignup()){
			setWidget(new Label(ErrorCode.getMessageText(ErrorCode.USER_SIGNUP_DISABLED, null)));
		}else{
			LoginSignupDeck main = new LoginSignupDeck(GwtClientUtils.getLocation(),this,loginOrSignup);
			main.addUserCreateListener(this);
			setWidget(main);
		}
		addStyleName(Css.LOGIN_DIALOG_BOX);
	}
	
	public void userCreateCancelled() {
		hidebox();
	}

	public void userCreated(UserModel model) {
		hidebox();
		//GOTO home page or UserModel url from server side  
		String url = model.getRedirUrl() == null?SharedConstants.URL_HOME:model.getRedirUrl();
		GwtClientUtils.reload(url);
	}
}
