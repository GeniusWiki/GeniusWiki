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
package com.edgenius.wiki.gwt.client.widgets;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Follow/Unfollow user function link.
 * @author Dapeng.Ni
 */
public class FollowLink extends ClickLink implements ClickHandler, AsyncCallback<UserModel>{

	private String username;
	private int following;
	
	private HandlerRegistration handler;
	
	public FollowLink(int following, String username){
		super(following>0?Msg.consts.unfollow():Msg.consts.follow());
		if(following < 0){
			//diable this link
			this.setText("");
		}else{
			this.following = following;
			this.username = username;
			handler = this.addClickHandler(this);
		}
	}
	public void onClick(ClickEvent event) {
		//disable click
		this.setText("...");
		handler.removeHandler();
		
		SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
		//get user recent updated pages for this space
		securityController.followUser(username, following==0, this);
		
	}
	public void onFailure(Throwable err) {
		this.setText(following>0?Msg.consts.unfollow():Msg.consts.follow());
	}
	public void onSuccess(UserModel follow) {
		following = follow.getFollowing();
		this.setText(following>0?Msg.consts.unfollow():Msg.consts.follow());
		handler = this.addClickHandler(this);
		
//		MessageDialog msg = new MessageDialog(MessageDialog.TYPE_CONFIRM, Msg.consts.follow(), 
//				follow.getFollowing()==1?("You start following " + follow.getFullname())
//						:("You stop following " + follow.getFullname()));
//		msg.showbox();
	}
}
