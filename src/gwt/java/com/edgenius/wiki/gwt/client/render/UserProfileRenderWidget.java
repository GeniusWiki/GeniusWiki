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
package com.edgenius.wiki.gwt.client.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.ActivityModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.model.UserProfileModel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.user.ContactPanel;
import com.edgenius.wiki.gwt.client.user.SendMessageDialog;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.FollowLink;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.ZebraTable;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Dapeng.Ni
 */
public class UserProfileRenderWidget extends SimplePanel implements AsyncCallback<UserProfileModel>,RenderWidget, SelectionHandler<Integer>  {

	interface PanelUiBinder extends UiBinder<Widget, UserProfileRenderWidget> {}
	private static PanelUiBinder uiBinder = GWT.create(PanelUiBinder.class);
	
	@UiField MessageWidget message;
	
	@UiField StackLayoutPanel listP;
	@UiField ZebraTable spaceP;
	@UiField ZebraTable pageP;
	@UiField ZebraTable activityP;
	
	@UiField VerticalPanel networkP;
	@UiField FlexTable followingP;
	@UiField FlexTable followerP;
	
	@UiField SimplePanel portrait;
	@UiField Label fullname;
	@UiField Label loginname;
	@UiField HorizontalPanel actionP;
	@UiField Label status;
	
	@UiField ContactPanel contacts;
	
	private boolean activitiesLoaded;
	private boolean pagesLoaded;
	private boolean spacesLoaded;
	private Image loadingBar = new Image(IconBundle.I.get().loadingBar());
	private RenderWidgetListener listener;
	private String username;
	private String componentKey;

	
	@UiFactory ZebraTable makeZebraTable() {
		return new ZebraTable(ZebraTable.STYLE_LIST, true);
	}

	public UserProfileRenderWidget(String username) {
		this.username = username;
		this.setWidget(uiBinder.createAndBindUi(this));
		
		DOM.setStyleAttribute(pageP.getElement(), "marginLeft", "0px");
		DOM.setStyleAttribute(spaceP.getElement(), "marginLeft", "0px");
		DOM.setStyleAttribute(activityP.getElement(), "marginLeft", "0px");
		networkP.setCellHorizontalAlignment(followingP, HasHorizontalAlignment.ALIGN_CENTER);
		networkP.setCellHorizontalAlignment(followerP, HasHorizontalAlignment.ALIGN_CENTER);
		
		listP.addSelectionHandler(this);
		listP.setHeight("90px");
	}
	

	//********************************************************************
	//               private methods
	//********************************************************************
	private void refreshUserProfile(UserProfileModel model) {
		final UserModel user = model.profile;
		portrait.setWidget(GwtClientUtils.createUserPortrait(user.getPortrait()));
		fullname.setText(user.getFullname());
		loginname.setText(user.getLoginname());
		
		contacts.setUser(user);
		
		actionP.clear();
		
		if(user.getFollowing() >= 0){
			//don't allow send or follow anonymous to someone, or some to anonymous, or login and viewing user are same user
			ClickLink sendMsg = new ClickLink(Msg.consts.send_message());
			sendMsg.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					SendMessageDialog dlg = new SendMessageDialog(user.getLoginname());
					dlg.showbox();
				}
			});
			actionP.add(sendMsg);

			//don't allow follow anonymous
			actionP.add(new FollowLink(user.getFollowing(), user.getLoginname()));
		}
		
		status.setStyleName(Css.STATUS);
		status.setText(StringUtil.trimToEmpty(user.getStatus()));
		
		//following and followers
		int row = followingP.getRowCount();
        for(int idx=row -1; idx >= 0 ; idx--){
        	followingP.removeRow(idx);
        } 
    	followingP.setWidget(0, 0,  new Label(Msg.consts.following()));
    	followingP.getFlexCellFormatter().setColSpan(0, 0, 3);
		followingP.getFlexCellFormatter().setStyleName(0, 0, Css.HEADER);

		
        row = followerP.getRowCount();
        for(int idx=row -1; idx >= 0 ; idx--){
        	followerP.removeRow(idx);
        } 
        followerP.setWidget(0, 0,  new Label(Msg.consts.followers()));
        followerP.getFlexCellFormatter().setColSpan(0, 0, 3);
        followerP.getFlexCellFormatter().setStyleName(0, 0, Css.HEADER);
        
		row = 1;
		int col = 0;
		if(model.following.size() > 0){
			for (UserModel fer: model.following) {
				Hyperlink link = new Hyperlink(GwtUtils.getUserPortraitHTML(fer.getPortrait(),fer.getFullname(),55), true,
						GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_USER_PROFILE), fer.getLoginname()));
				followingP.setWidget(row,col,link);
				if(++col > 2){
					row++;
					col = 0;
				}
			}
			if(row == 1 && col < 3){
				//append blank
				for (int idx=col; idx < 3; idx++) {
					followingP.setWidget(row,idx,new HTML("&nbsp;"));
				}
			}
			followingP.getFlexCellFormatter().setWidth(1, 0, "33%");
			followingP.getFlexCellFormatter().setWidth(1, 1, "33%");
			followingP.getFlexCellFormatter().setWidth(1, 2, "33%");
		}else{
			followingP.setWidget(row, 0, new Label(Msg.consts.none()));
		}
		
		row = 1;col = 0;
		if(model.followers.size() > 0){
			for (UserModel fer: model.followers) {
				Hyperlink link = new Hyperlink(GwtUtils.getUserPortraitHTML(fer.getPortrait(),fer.getFullname(),55), true,
						GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_USER_PROFILE), fer.getLoginname()));
				followerP.setWidget(row,col,link);
				if(++col > 2){
					row++;
					col = 0;
				}
			}
			if(row == 1 && col < 3){
				//append blank
				for (int idx=col; idx < 3; idx++) {
					followerP.setWidget(row,idx,new HTML("&nbsp;"));
				}
			}
			followerP.getFlexCellFormatter().setWidth(1, 0, "33%");
			followerP.getFlexCellFormatter().setWidth(1, 1, "33%");
			followerP.getFlexCellFormatter().setWidth(1, 2, "33%");

		}else{
			followerP.setWidget(row, 0, new Label(Msg.consts.none()));
		}
		
	}

	private void refreshActivities(ArrayList<ActivityModel> activities) {
		activityP.removeAllRows();
        
		int row = 0;
		Label l1 = new Label(Msg.consts.activity());
		activityP.setWidget(row, 0, l1);
		
		if(activities != null && activities.size() > 0){
			for(final ActivityModel act : activities){
				activityP.setWidget(++row, 0, new HTML(act.activity));
			}
		}else{
			activityP.setWidget(row, 0, new Label(Msg.consts.none()));
		}
		listP.setHeight((activityP.getOffsetHeight()+90)+"px");
		
	}
	private void refreshSpaces(List<SpaceModel> spaces) {
		spaceP.removeAllRows();
        
		int row = 0;
		Label l1 = new Label(Msg.consts.authored_space());
		Label l2 = new Label(Msg.consts.created_date());
		spaceP.setWidget(row, 0, l1);
		spaceP.setWidget(row, 1, l2);
		
		
		spaceP.getColumnFormatter().setWidth(0, "60%");
		spaceP.getColumnFormatter().setWidth(1, "40%");

		row++;
		if(spaces != null && spaces.size() > 0){
			for(final SpaceModel item : spaces){
				Hyperlink link = new Hyperlink(item.name,GwtUtils.getSpacePageToken(item.unixName,null));
				spaceP.setWidget(row, 0, link);
				Label dl = new Label(GwtClientUtils.toDisplayDate(item.createdDate));
				spaceP.setWidget(row, 1, dl);
				row++;
			}
		}else{
			spaceP.setWidget(row, 0, new Label(Msg.consts.none()));
		}
		listP.setHeight((spaceP.getOffsetHeight()+90)+"px");
	}

	private void refreshPages(List<PageItemModel> pages) {

		pageP.removeAllRows();
        
		int row = 0;
		Label l1 = new Label(Msg.consts.page_title());
		Label l2 = new Label(Msg.consts.spacekey());
		Label l3 = new Label(Msg.consts.contributed_versions());
		pageP.setWidget(row, 0, l1);
		pageP.setWidget(row, 1, l2);
		pageP.setWidget(row, 2, l3);
		pageP.getColumnFormatter().setWidth(0, "50%");
		pageP.getColumnFormatter().setWidth(1, "25%");
		pageP.getColumnFormatter().setWidth(2, "25%");
		
		row++;
		if(pages != null && pages.size() > 0){
			for(final PageItemModel item : pages){
				//MUST bring space in Token, because it maybe different space with current
				Hyperlink link = new Hyperlink(item.title,GwtUtils.getSpacePageToken(item.spaceUname,item.title));
				
				pageP.setWidget(row, 0, link);
				
				//space
				link = new Hyperlink(item.spaceUname,GwtUtils.getSpacePageToken(item.spaceUname, null));
				pageP.setWidget(row, 1, link);
				
				FlowPanel versPanel = new FlowPanel();
				TreeMap<Integer, PageItemModel> vers = new TreeMap<Integer, PageItemModel>(new Comparator<Integer>(){
					public int compare(Integer o1, Integer o2) {
						return o1 - o2;
					}
				});
				vers.putAll(item.versionHistory);
				
				boolean creatorAndNoHistory = false;
				for(Entry<Integer, PageItemModel> entry : vers.entrySet()){
					link = null;
					if(entry.getKey() == 0){
						if(item.version == 1){
							creatorAndNoHistory = true;
							//This user is creator and current page version number is 1, this mean this page no history yet.
							link = new Hyperlink(Msg.consts.initial(),GwtUtils.getSpacePageToken(item.spaceUname,item.title));
						}
					}else if(entry.getKey() == Integer.MAX_VALUE){
						if(!creatorAndNoHistory){
							//if this page no history yet, then only display "inital" is enough - don't want to display initial and current 
							//but point to same current page.
							link = new Hyperlink(Msg.consts.current().toLowerCase(),GwtUtils.getSpacePageToken(item.spaceUname,item.title));
						}
					}else{
						if(entry.getValue().version == 1){
							link = new Hyperlink(Msg.consts.initial(),GwtUtils.buildToken(PageMain.TOKEN_HISTORY,item.spaceUname,String.valueOf(entry.getValue().uid)));
						}else{
							link = new Hyperlink(entry.getKey()+"",GwtUtils.buildToken(PageMain.TOKEN_HISTORY,item.spaceUname, String.valueOf(entry.getValue().uid)));
						}
					}
					
					if(link != null){
						link.setStyleName(Css.LIGHT_LINK);
						link.setTitle(Msg.consts.modified_on() + " " + GwtClientUtils.toDisplayDate(entry.getValue().modifiedDate));
						versPanel.add(link);
						versPanel.add(new HTML(" "));
					}
				}
				pageP.setWidget(row, 2, versPanel);
				row++;
			}
		}else{
			pageP.setWidget(row, 0, new Label(Msg.consts.none()));
		}
		listP.setHeight((pageP.getOffsetHeight()+90)+"px");
	}

	public void onFailure(Throwable caught) {
		listener.onFailedLoad(componentKey, caught.getMessage());
	}


	public void onSuccess(UserProfileModel model) {
		//TODO: at moment - it is not necessary to pass meaningful text - just tell listener, it is success with non-empty content
		listener.onSuccessLoad(componentKey, "User profile"); //NON-i18n
		
		if(model.profile != null){
			refreshUserProfile(model);
		}
		if(model.activities != null){
			refreshActivities(model.activities);
			activitiesLoaded = true;
		}
		if(model.spaces != null){
			refreshSpaces(model.spaces);
			spacesLoaded = true;
		}
		if(model.pages != null){
			refreshPages(model.pages);
			pagesLoaded = true;
		}
		
	}

	public void onLoad(String widgetKey, UserModel user, RenderWidgetListener listener){
		this.listener = listener;
		this.componentKey = widgetKey;
		
		activitiesLoaded = false;
		pagesLoaded = false;
		spacesLoaded = false;
		
		listener.onLoading(componentKey);
		SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
		securityController.getUserProfile(username,this);
		
		//load activities initial
		showLoading(activityP);
		securityController.getUserContributed(username, SharedConstants.ACTIVITY, this);
	}

	public void onUserChanged(UserModel user) {
		activitiesLoaded = false;
		pagesLoaded = false;
		spacesLoaded = false;
	}

	public void onSelection(SelectionEvent<Integer> idx) {
		listP.setHeight("90px");
		if(idx.getSelectedItem() == 0){
			listP.setHeight((activityP.getOffsetHeight()+90)+"px");
			if(!activitiesLoaded){
				showLoading(activityP);
				SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
				securityController.getUserContributed(username, SharedConstants.ACTIVITY, this);
			}
		}else if(idx.getSelectedItem() == 1){
			listP.setHeight((spaceP.getOffsetHeight()+90)+"px");
			if(!spacesLoaded){ 
				showLoading(spaceP);
				SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
				securityController.getUserContributed(username, SharedConstants.SPACE, this);
			}
		}else if(idx.getSelectedItem() == 2){
			listP.setHeight((pageP.getOffsetHeight()+90)+"px");
			if(!pagesLoaded){
				showLoading(pageP);
				SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
				securityController.getUserContributed(username, SharedConstants.PAGE, this);
			}
		}
	}

	/**
	 * 
	 */
	private void showLoading(ZebraTable table) {
		listP.setHeight("180px");
		table.removeAllRows();
		table.setWidget(0, 0, loadingBar);
		table.getFlexCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		table.getFlexCellFormatter().setHeight(0, 0, "55px");
	}
	
}
