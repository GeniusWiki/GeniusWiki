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

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Dapeng.Ni
 */
public class SyncButton extends FlowPanel implements SyncProgressListener{

	private String spaceUname;
	private SpaceSyncProgressPopup pop;
	
	private Image offlineImg = new Image(IconBundle.I.get().disconnect());
	private Image connectImg = new Image(IconBundle.I.get().connect());
	private Image errorImg = new Image(IconBundle.I.get().connect_error());
	private Image busyImg = IconBundle.I.indicator();
	
	/**
	 * @param user 
	 * @param unixName
	 */
	public SyncButton(final UserModel user, String spaceUname,  boolean textOnly) {
		this.spaceUname = spaceUname; 
		
		if(!textOnly){
			offlineImg.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					sync(user);
				}
				
			});
			offlineImg.setStyleName(Css.PORTLET_FOOT_IMG);
			this.add(offlineImg);
			connectImg.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					sync(user);
				}
				
			});
			connectImg.setStyleName(Css.PORTLET_FOOT_IMG);
			this.add(connectImg);
			
			errorImg.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					sync(user);
				}
				
			});
			errorImg.setStyleName(Css.PORTLET_FOOT_IMG);
			this.add(errorImg);
			
			busyImg.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					sync(user);
				}
				
			});
			busyImg.setStyleName(Css.PORTLET_FOOT_IMG);
			this.add(busyImg);
			
			if(OfflineUtil.isReadyForSpace(user.getUid(),spaceUname)){
				offlineImg.setVisible(false);
				connectImg.setVisible(true);
				errorImg.setVisible(false);
				busyImg.setVisible(false);
			}else{
				offlineImg.setVisible(true);
				connectImg.setVisible(false);
				errorImg.setVisible(false);
				busyImg.setVisible(false);
			}
		}
		
		ClickLink offlineLink = new ClickLink(Msg.consts.offline());
		offlineLink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				sync(user);
			}
		});
		this.add(offlineLink);
		
		DOM.setStyleAttribute(offlineLink.getLinkElement(), "verticalAlign", "top");
		DOM.setStyleAttribute(this.getElement(), "display", "inline");
	}
	private void sync(UserModel user){
		if(!OfflineUtil.checkGearsInstalled(this))
			return;
		
		if(spaceUname != null){
			if(!OfflineUtil.isReadyForSpace(user.getUid(),spaceUname) && OfflineUtil.isAlreadyPermitForSite()){
				//only ask while first time download space, and user already choose "allow" 
				//on Gears default security dialog(don't bother to ask user choose 2 dialog)
				if(!Window.confirm(Msg.consts.confirm_offline_space())){
					return;
				}
			}
			//show progress panel: after initSpace() method, because pop allow user change options, initSpace() can
			//ensure space exist in database.
			pop = new SpaceSyncProgressPopup(this, user,spaceUname);
			//start offline given space and all needUpdateSpaces!
			Sync sync = new Sync();
			sync.addSyncProgressListener(this);
			sync.addSyncProgressListener(pop);
			
			sync.sync(user, spaceUname);
			
			int options = sync.getSyncOptions(user.getUid(),spaceUname);
			Log.info("Download space by options " + options);
			pop.setOptions(options);
			pop.pop();
		}
	}

	public void percent(String spaceUname, int percent) {
		if(!spaceUname.equals(this.spaceUname))
			return;
		
		if(percent == 100){
//			don't close pop auto, because it provides "advance options" to allow user choose.
//			if(pop != null && pop.isVisible())
//				pop.hide();
			busyImg.setVisible(false);
			offlineImg.setVisible(false);
			connectImg.setVisible(true);
			errorImg.setVisible(false);
		}else{
			busyImg.setVisible(true);
			offlineImg.setVisible(false);
			connectImg.setVisible(false);
			errorImg.setVisible(false);
		}
	}

	public void error(String spaceUname, String errorCode) {
		if(!spaceUname.equals(this.spaceUname))
			return;
		
		if(pop != null && pop.isVisible()){
			//show error message on pop
			pop.getMessage().error(ErrorCode.getMessage(errorCode, null));
		}
		busyImg.setVisible(false);
		offlineImg.setVisible(false);
		connectImg.setVisible(false);
		errorImg.setVisible(true);
		
	}
}
