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
package com.edgenius.wiki.gwt.client.user;

import java.util.Iterator;
import java.util.List;

import com.edgenius.wiki.gwt.client.AbstractEntryPoint;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.FollowLink;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.Popup;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.EventfulHyperLink;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class UserInfoPanel extends SimplePanel implements AsyncCallback<PageItemListModel> {
	private VerticalPanel pagesPanel = new VerticalPanel();
	
	private Image loadingImg = IconBundle.I.loading();
	private MessageWidget message = new MessageWidget();
	private Label status = new Label();
	private FlowPanel portraitPanel = new FlowPanel();
	private boolean showPortrait;
	private Widget parent;
	private HorizontalPanel func = new HorizontalPanel();
	private String username;
	
	public UserInfoPanel(final Widget parent, String spaceUname,final String username,String portrait, boolean showPortrait) {
		this.parent = parent;
		this.username = username;
		
		VerticalPanel main = new VerticalPanel();
		HorizontalPanel center = new HorizontalPanel();
		FlowPanel profile = new FlowPanel();
		this.showPortrait = showPortrait;
		if(showPortrait && !StringUtil.isBlank(portrait)){
			portraitPanel.add(GwtClientUtils.createUserPortrait(portrait));
		}
		center.add(portraitPanel);
		center.add(profile);
		
		status.setStyleName(Css.STATUS_SNAP);
		
		Label lb = new Label(Msg.consts.recent_update_pages());
		lb.setStyleName(Css.HEADING3);
		lb.addStyleName(Css.UNDERLINE);

		profile.add(status);
		profile.add(lb);
		profile.add(pagesPanel);
		profile.add(loadingImg);
		
		main.add(message);
		main.add(center);
		if(!AbstractEntryPoint.isOffline()){
			func.setWidth("100%");
			func.setStyleName(Css.ACTION_TABS);
			main.add(func);
		}		
		
		center.setCellWidth(profile, "100%");
		center.setSpacing(2);
		profile.setSize("100%", "100%");
		center.setSize("100%", "100%");
		main.setSize("100%", "100%");
		center.setStyleName(Css.PROFILE);
		
		this.setWidget(main);
		loadingImg.setVisible(true);
		PageControllerAsync pageController = ControllerFactory.getPageController();
		//get user recent updated pages for this space
		pageController.getUserPagesInSpace(spaceUname, username, 5, this);
		
	}

	private void refresh(final PageItemListModel model) {
		List<PageItemModel> pages = model.itemList;
		pagesPanel.clear();
		status.setText(model.userStatus);
		//don't need update... this is just for user portrait is not input when the user portrait URL is not available when popup initial.
		if(this.showPortrait && portraitPanel.getWidgetCount() == 0){
			portraitPanel.add(GwtClientUtils.createUserPortrait(model.userPortrait));
		}
		if (pages != null && pages.size() > 0) {
			for (Iterator<PageItemModel> iter = pages.iterator(); iter.hasNext();) {
				PageItemModel item = iter.next();
				EventfulHyperLink link = new EventfulHyperLink(item.title,GwtUtils.getSpacePageToken(item.spaceUname, item.title));
				if(parent instanceof Popup){
					link.addClickHandler(new ClickHandler(){
						public void onClick(ClickEvent event) {
							((Popup)parent).hide();
						}
					});
				};
				pagesPanel.add(link);
			}
		}else{
			pagesPanel.add(new HTML("("+Msg.consts.none()+")"));
		}
		

		if(!AbstractEntryPoint.isOffline()){
			if(model.isFollowing >= 0){
				
				ClickLink sendMsg = new ClickLink(Msg.consts.send_message());
				sendMsg.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						if(parent instanceof Popup){
							((Popup)parent).hide();
						}
						//show send message dialogue
						SendMessageDialog msgDlg = new SendMessageDialog(username);
						msgDlg.showbox();
					}
				});
				func.add(sendMsg);
		
				func.add(new FollowLink(model.isFollowing, username));
			}
			
			//so far, have to block profile page in offline model as the macro render logic is on MacroHandler side, it is not easy to do in 
			//offline model.
			EventfulHyperLink link = new EventfulHyperLink(Msg.consts.goto_profile(), 
					GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_USER_PROFILE), username));
			if(parent instanceof Popup){
				link.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						((Popup)parent).hide();
					}
				});
			}
			func.add(link);
		}
		if(parent instanceof Popup){
			((Popup)parent).resized();
		}
	}

	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
		loadingImg.setVisible(false);
	}

	public void onSuccess(PageItemListModel model) {
		loadingImg.setVisible(false);
		
		if (!GwtClientUtils.preSuccessCheck(model, message)) {
			return;
		}
		refresh(model);
	}

	
}
