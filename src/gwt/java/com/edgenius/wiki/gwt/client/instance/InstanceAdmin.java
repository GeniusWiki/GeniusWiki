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
import java.util.List;

import com.edgenius.wiki.gwt.client.BaseEntryPoint;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.ListDialogue;
import com.edgenius.wiki.gwt.client.widgets.NavItem;
import com.edgenius.wiki.gwt.client.widgets.URLTabPanel;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Dapeng.Ni
 */
public class InstanceAdmin  extends BaseEntryPoint {
	
	public static InstanceAdmin I;

	public void reload() {
		checkLogin();

	    cleanBeforeLogin();
	    
	    getGlobalMessage().cleanMessage();
	}
	
	public void login(UserModel user) {
		// TODO Auto-generated method stub
	}

	public void initContentPanel() {
		I = this;
		
		//Refresh navbar
		List<NavItem> linkList = new ArrayList<NavItem>();
		linkList.add(new NavItem(Msg.consts.dashboard(), SharedConstants.URL_HOME, null, true));
		linkList.add(new NavItem(Msg.consts.system_admin(), SharedConstants.URL_INSTANCE_ADMIN, null, true));
		refreshNavbar(linkList);
		
		//initial content
		final URLTabPanel panel = new URLTabPanel();
		InstanceSecurityPanel security = new InstanceSecurityPanel(SharedConstants.INSTANCE_NAME);
		InstanceDashboardPanel dashPanel  = new InstanceDashboardPanel();
		
		final int gid = panel.addItem(GwtClientUtils.getBaseUrl()+"instance/general.do", Msg.consts.general());
		panel.addItem(GwtClientUtils.getBaseUrl()+"instance/users.do", Msg.consts.users());
		panel.addItem(GwtClientUtils.getBaseUrl()+"instance/roles.do", Msg.consts.groups());
		panel.addItem(GwtClientUtils.getBaseUrl()+"instance/spaces.do", Msg.consts.spaces());
		panel.addItem(GwtClientUtils.getBaseUrl()+"instance/theme!listSkins.do", Msg.consts.skins());
		panel.addItem(GwtClientUtils.getBaseUrl()+"instance/theme!listThemes.do", Msg.consts.themes());
		panel.addItem(GwtClientUtils.getBaseUrl()+"instance/backup.do", Msg.consts.backup_restore());
		panel.addItem(dashPanel, Msg.consts.default_dashboard(),true);
		panel.addItem(security, Msg.consts.security(),true);
		panel.addItem(GwtClientUtils.getBaseUrl()+"instance/advance.do", Msg.consts.advance());

		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				//to avoid URLPanel.setURL() failed because panel does not initial to HTML
				panel.setSelected(gid, false);
			}
		});
		
		RootPanel.get(CONTENT_PANEL).add(panel);
		
		bindOpenMethod();

	}

	//!!!DON'T REMOVE!!! call by native
	private static void showCreateUserBox(){
		InstanceCreateUserDialog dialog = new InstanceCreateUserDialog();
		dialog.showbox();
	}
	//!!!DON'T REMOVE!!! call by native
	private static void showCreateRoleBox(){
		InstanceCreateGroupDialog dialog = new InstanceCreateGroupDialog();
		dialog.showbox();
	}
	//!!!DON'T REMOVE!!! call by native
	private static void showCreateSpaceBox(){
		InstanceCreateSpaceDialog dialog = new InstanceCreateSpaceDialog();
		dialog.showbox();
	}
	
	//!!!DON'T REMOVE!!! call by native
	private static void addUserToRoleBox(int roleUid){
		ListDialogue userListDialogue = new ListDialogue(Msg.consts.add_user(), ListDialogue.USER);
		userListDialogue.setIcon(ButtonIconBundle.userImage());
		userListDialogue.addListener(new AddUserToRoleListener(roleUid));
		userListDialogue.showbox();
	}
	
	public native void bindOpenMethod()/*-{
	   $wnd.gwtCreateUserDialog= function () {
	         @com.edgenius.wiki.gwt.client.instance.InstanceAdmin::showCreateUserBox()();
	   };
	   $wnd.gwtCreateRoleDialog= function () {
	         @com.edgenius.wiki.gwt.client.instance.InstanceAdmin::showCreateRoleBox()();
	   };
	   $wnd.gwtCreateSpaceDialog= function () {
	         @com.edgenius.wiki.gwt.client.instance.InstanceAdmin::showCreateSpaceBox()();
	   };
	   $wnd.gwtAddUserToRoleDialog= function (roleUid) {
	         @com.edgenius.wiki.gwt.client.instance.InstanceAdmin::addUserToRoleBox(I)(roleUid);
	   };
	}-*/;

}
