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
package com.edgenius.wiki.gwt.client.home.porlet;

import java.util.Iterator;
import java.util.List;

import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.GeneralModel;
import com.edgenius.wiki.gwt.client.model.MessageListModel;
import com.edgenius.wiki.gwt.client.model.MessageModel;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.model.SearchResultItemModel;
import com.edgenius.wiki.gwt.client.model.SearchResultModel;
import com.edgenius.wiki.gwt.client.portal.Portlet;
import com.edgenius.wiki.gwt.client.render.SearchRenderWidget.TypeImageBundle;
import com.edgenius.wiki.gwt.client.server.NotificationControllerAsync;
import com.edgenius.wiki.gwt.client.server.SearchControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.user.UserPopup;
import com.edgenius.wiki.gwt.client.widgets.AutoResizeTextArea;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.HintTextBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.MoreLessButtonBar;
import com.edgenius.wiki.gwt.client.widgets.MoreLessButtonBarListener;
import com.edgenius.wiki.gwt.client.widgets.Pagination;
import com.edgenius.wiki.gwt.client.widgets.PaginationListener;
import com.edgenius.wiki.gwt.client.widgets.ZebraTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class MessagePortlet extends Portlet implements MoreLessButtonBarListener{
	
	private MoreLessButtonBar controlBar = new MoreLessButtonBar();
	private PostPanel postPanel = new PostPanel();
	private int currentPage = 1;
	private VerticalPanel messagePanel = new VerticalPanel();
	
	public void render() {
		controlBar.addMoreLessButtonBarListener(this);
		this.header.addStyleDependentName(Css.DEP_WIDGET);
		this.addStyleDependentName(Css.DEP_WIDGET);
		
		setPortletTitle(getPortletTitle(),getPortletTitle(), null);
		setPortletLogo(new Image(IconBundle.I.get().message()));
		
		messagePanel.setWidth("100%");
		container.clear();
		container.add(postPanel);
		container.add(messagePanel);
		//try to get page
		refresh();
	}

	public void onSuccess(PortletModel model) {
		MessageListModel msgs = (MessageListModel) model.renderContent;
		
		callback(model, msgs);
		
	}

	
	/**
	 * @return
	 */
	public static String getPortletTitle() {
		return Msg.consts.message_board();
	}

	//********************************************************************
	//               private method
	//********************************************************************
	/**
	 * A common callback success, as this portlet can accept response from Portlet invoke 
	 * or from a message send refresh callback.
	 */
	private void callback(GeneralModel model, MessageListModel msgs) {
		busy(false);
		messagePanel.clear();
		if(!GwtClientUtils.preSuccessCheck(model,null)){
			//show error message on item part rather than general error message on HTML page above
			messagePanel.add(ErrorCode.getMessage(model.errorCode, model.errorMsg));
			return;
		}

		if(msgs.list.size() == 0){
			none.setStyleName(Css.PORTLET_WEAK_TEXT);
			messagePanel.add(none);
			return;
		}
		
		for(Iterator<MessageModel> iter = msgs.list.iterator();iter.hasNext();){
			final MessageModel msg = iter.next();
			String puid = HTMLPanel.createUniqueId();
			String puid1 = HTMLPanel.createUniqueId();
			String puid2 = HTMLPanel.createUniqueId();
			String puid3 = HTMLPanel.createUniqueId();
			HTMLPanel msgPanel = new HTMLPanel("<div style=\"display:inline;font-weight:bold;\" id=\""+puid1+"\"></div>" +
					"<div style=\"display:inline\" id=\""+puid2+"\"></div> <div class=\"weaktext\">" 
					+ Msg.consts.by() + "</div><div class=\"weaklink\" id=\""+puid+"\"></div><div class=\"weaktext\">"
					+Msg.consts.on() + " " + GwtClientUtils.toDisplayDate(msg.date)+"</div>"
					+"<div style=\"display:inline\" id=\""+puid3+"\"></div>");
			
			if((msg.targetType == SharedConstants.MSG_TARGET_USER
				//TODO: how to distinguish the contributor and admin?
				|| msg.targetType == SharedConstants.MSG_TARGET_SPACE_CONTRIBUTE_USERS
				|| msg.targetType == SharedConstants.MSG_TARGET_SPACE_ADMIN_ONLY)
				&& !StringUtil.isBlank(msg.target)){
				msgPanel.add( new Label((msg.targetType == SharedConstants.MSG_TARGET_USER?"@":"@@")+msg.target+ " "), puid1);
			}
			if(msg.targetType == SharedConstants.MSG_TARGET_INSTANCE_ADMIN_ONLY && StringUtil.isBlank(msg.target)){
				//message to system admin
				Image msgAdmin = new Image(IconBundle.I.get().error());
				msgAdmin.setTitle(Msg.consts.msg_to_admin());
				msgPanel.add(msgAdmin, puid1);
			}
			
			//don't use UserProfileLink - click user link will invoke reply message, rather than jump to user profile
			//UserProfileLink authorPop = new UserProfileLink(msg.author, null,msg.author,msg.authorPortrait);
			ClickLink authorPop = new ClickLink(msg.authorUsername);
			//auto popup for user profile
			new UserPopup(authorPop, null,msg.authorUsername,msg.authorPortrait);
			authorPop.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent evt) {
					if(msg.authorUid != -1){
						postPanel.fillTarget(SharedConstants.MSG_TARGET_USER, msg.authorUsername);
					}else{
						//reply anonymous means to all 
						postPanel.fillTarget(null, null);
					}
					postPanel.focusToEnd(true);
				}
			});
			
			msgPanel.add(new Label(msg.text), puid2);
			msgPanel.add(authorPop, puid);
			if(msg.removable){
				Image bin = new Image(IconBundle.I.get().bin_close());
				bin.setTitle(Msg.consts.delete());
				bin.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						if(Window.confirm(Msg.consts.confirm_delete_message())){
							NotificationControllerAsync notifyController = ControllerFactory.getNotificationController();
							notifyController.deleteMessage(currentPage,msg.uid, new NotificationCallback());
						}
					}								
				});
				msgPanel.add(bin, puid3);
			}
			
			DOM.setStyleAttribute(msgPanel.getElement(),"display","inline");
			
			HorizontalPanel itemPanel = new HorizontalPanel();
			Widget portrait = GwtClientUtils.createUserSmallPortrait(msg.authorPortrait, SharedConstants.PORTRAIT_SIZE_SMALL);
			itemPanel.add(portrait);
			itemPanel.setCellWidth(portrait, "50px");
			itemPanel.add(msgPanel);
			messagePanel.add(itemPanel);
		}
		
		clearControl();
	
		controlBar.setPaginationInfo(msgs.hasPre,msgs.hasNxt,msgs.currentPage);
		addControl(controlBar);
	}
	public void pageChange(int currentPageNum) {
		controlBar.busy(true);
		NotificationControllerAsync notifyController = ControllerFactory.getNotificationController();
		notifyController.getMessages(currentPageNum, new NotificationCallback());
	}

	//********************************************************************
	//               Private classes
	//********************************************************************
	
	//This callback is from message send, then refresh list of message
	private class NotificationCallback implements AsyncCallback<MessageListModel>{

		public void onFailure(Throwable error) {
			busy(false);
			controlBar.busy(false);
			GwtClientUtils.processError(error);
		}

		public void onSuccess(MessageListModel result) {
			callback(result, result);
			controlBar.busy(false);
		}
		
	}
	
	//post message input box
	private class PostPanel extends SimplePanel implements ClickHandler{
		private AutoResizeTextArea text = new AutoResizeTextArea();
		private ClickLink findPeople = new ClickLink(Msg.consts.find_people());
		private Label limit = new Label(String.valueOf(ClientConstants.LIMIT_CHAR));
		private Button sendBtn = new Button(Msg.consts.send());
		
		
		public PostPanel(){
			findPeople.addClickHandler(this);
			sendBtn.addClickHandler(this);
			
			FlowPanel toPanel = new FlowPanel();
			toPanel.add(findPeople);
			
			HorizontalPanel border = new HorizontalPanel();
			border.add(toPanel);
			border.add(limit);
			border.setCellHorizontalAlignment(limit, HasHorizontalAlignment.ALIGN_RIGHT);
			
			text.setTitle(Msg.consts.quick_send_tip());
			
			VerticalPanel main = new VerticalPanel();
			main.add(border);
			main.add(text);
			main.add(sendBtn);
			main.setCellHorizontalAlignment(sendBtn, HasHorizontalAlignment.ALIGN_CENTER);
			main.setCellHorizontalAlignment(text, HasHorizontalAlignment.ALIGN_CENTER);
			
			border.setWidth("100%");
			main.setWidth("100%");
			this.setWidth("100%");
			text.setStyleName(Css.TWEET_BOX);
			limit.setStyleName(Css.TWEET_COUNTER);
			this.setWidget(main);
			
			text.addKeyUpHandler(new KeyUpHandler(){
				public void onKeyUp(KeyUpEvent event) {
					if(text.getText().length() > ClientConstants.LIMIT_CHAR){
						text.setText(text.getText().substring(0,ClientConstants.LIMIT_CHAR));
					}
					limit.setText(String.valueOf(ClientConstants.LIMIT_CHAR - text.getText().length()));
					
					if(event.isControlKeyDown() && event.getNativeKeyCode() == 13){
						//ctrl+Enter to submit
						sendMessage();
					}
				}
			});
		}
		public void onClick(ClickEvent event) {
			if(event.getSource() == sendBtn){
				sendMessage();
				
			}else if(event.getSource() == findPeople){
				SendToDialog dialog = new SendToDialog(PostPanel.this);
				dialog.showbox();
			}
		}
		/**
		 * 
		 */
		private void sendMessage() {
			if(StringUtil.isBlank(text.getText()))
				return;
			
			String body = text.getText().trim();
			if(body.length() == 0)
				return;
			
			NotificationControllerAsync notifyController = ControllerFactory.getNotificationController();
			notifyController.sendTwitterMessage(body, true, new NotificationCallback());
			//send to all as default
			fillTarget(null, null);
			text.setText("");
			//tricky: this will reset text area size to minimum height.
			text.setFocus(true);
			text.setFocus(false);
			limit.setText(String.valueOf(ClientConstants.LIMIT_CHAR));
		}
	
		public void fillTarget(Integer type, String name){

			if(!StringUtil.isBlank(name)){
				String msg = "@";
				//double @@ means to space contributors
				if(type == SharedConstants.MSG_TARGET_SPACE_CONTRIBUTE_USERS){
					msg +="@";
					//space name could include "blank", enclose it by single quote, i.e., 'name with space'
					if(name.indexOf(" ") != -1){
						msg += "'"+name+"'";
					}else{
						msg += name;
					}
				}else{
					msg += name;
				}
				
				msg += " " + text.getText();
				text.setText(msg);
			}
		}

		public void focusToEnd(boolean focus){
			text.setFocus(focus);
			if(focus){
				text.setCursorPos(text.getText().length());
			}
		}
	}
	
	//A dialog to choose a target of message - target maybe user or space or all
	private class SendToDialog extends DialogBox implements PaginationListener, AsyncCallback<SearchResultModel>{
		private static final int SPACE_LIST_SIZE = 10;
		
		private HintTextBox filter = new HintTextBox(Msg.consts.input_keyword());
		private MessageWidget message = new MessageWidget();
		private Image searchBusyImg = IconBundle.I.indicator();
		private Pagination pagination = new Pagination();
		private String keyword;
		private ZebraTable searchRsTable = new ZebraTable();
		private Label summary = new Label();

		private PostPanel parent;
		
		public SendToDialog(PostPanel parent){
			this.parent = parent;
			this.setText(Msg.consts.search_space_user());
			this.setIcon(new Image(IconBundle.I.get().application_add()));
			
			FlexTable topPanel = new FlexTable();
			Label label = new Label(Msg.consts.search_space_user());
			topPanel.setWidget(0, 0, label);
			topPanel.setWidget(0, 1, filter);
			topPanel.setWidget(0, 2, searchBusyImg);
			searchBusyImg.setVisible(false);
			
			filter.setStyleName(Css.SEARCH_INPUT);
			filter.addKeyDownHandler(new KeyDownHandler() {
				public void onKeyDown(KeyDownEvent event) {
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
						keyword = filter.getText();
						if (keyword == null || keyword.trim().length() == 0)
							return;
						
						searchBusyImg.setVisible(true);
						SearchControllerAsync action = ControllerFactory.getSearchController();
						//first page, return 10
						action.searchUserAndSpace(keyword,0,SPACE_LIST_SIZE, SendToDialog.this);
					}
				}
			});
			
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			//               Search Tab
			HorizontalPanel h1 = new HorizontalPanel();
			pagination.addPaginationListener(this);
			pagination.setPageSize(SPACE_LIST_SIZE);
			h1.add(pagination);
			h1.add(summary);
			h1.setSpacing(5);
			
			VerticalPanel rPanel = new VerticalPanel();
			rPanel.add(searchRsTable);
			rPanel.add(h1);
			
			VerticalPanel panel = new VerticalPanel();
			panel.add(message);
			panel.add(topPanel);
			panel.add(rPanel);
			initTable(searchRsTable);
			rebuildTable(searchRsTable);
			
			
			panel.setWidth("100%");
			rPanel.setWidth("100%");
			Button cancelBtn = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
			cancelBtn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					hidebox();
				}
			});
			
			getButtonBar().add(cancelBtn);
			this.setWidget(panel);
		}
		
		public void onFailure(Throwable error) {
			searchBusyImg.setVisible(false);
			GwtClientUtils.processError(error);
			
		}

		public void onSuccess(SearchResultModel model) {
			searchBusyImg.setVisible(false);
			if(!GwtClientUtils.preSuccessCheck(model,message)){
				return;
			}
			
			if(model == null ||model.results == null || model.results.size() == 0){
				summary.setText("");
				//anyway, clean last time search result.
				int row = rebuildTable(searchRsTable);
				searchRsTable.setWidget(row, 0, new Label(Msg.consts.no_result()));
				searchRsTable.getFlexCellFormatter().setColSpan(row, 0, 4);
				return;
			}
			message.cleanMessage();

			pagination.setTotalItem(model.totalItems);
			pagination.setCurrentPage(model.currPage);
			summary.setText(Msg.params.total_result(model.totalItems+""));
			
			fillListTable(searchRsTable, model.results);
		}
		
		private void initTable(ZebraTable table) {
			int col;
			col = 0;
			table.addStyleName(Css.SPACE_LIST);
			table.getColumnFormatter().setStyleName(col++, Css.SPACE_LOGO);
			table.getColumnFormatter().setStyleName(col++, Css.SPACE_NAME);
			table.getColumnFormatter().setStyleName(col++, Css.SPACE_DESC);
			table.getColumnFormatter().setStyleName(col++, Css.SPACE_FUNC);
		}
		/**
		 * @param table
		 */
		private int rebuildTable(ZebraTable table) {
			//need PortletModel in list
			int rowSize = table.getRowCount();
			for(int idx=rowSize-1;idx>=0;idx--)
				table.removeRow(idx);
			
			int row = 0,col=1;
			//show logo, then first column is log
			table.setWidget(row, col++, new Label(Msg.consts.name()));
			table.setWidget(row, col++, new Label(Msg.consts.description()));
			table.setWidget(row, col++, new Label(Msg.consts.action()));
			
			++row;
			col = 1;
			table.setWidget(row, col++, new Label(Msg.consts.all()));
			table.setWidget(row, col++, new Label(Msg.consts.tweet_all_desc()));
			
			//first row - to "all" users
			ClickLink add = new ClickLink(Msg.consts.choose());
			add.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					hidebox();
					//to all users
					parent.fillTarget(null, null);
				}
			});
			table.setWidget(row, col++, add);
			return ++row;
		}
		private void fillListTable(ZebraTable table,List<SearchResultItemModel> rsList) {
			
			int row = rebuildTable(table);
			
			int col;

			for(Iterator<SearchResultItemModel> iter =rsList.iterator();iter.hasNext();){
				final SearchResultItemModel sModel = iter.next();
				col = 0;
				Image logo; 
				if(sModel.type == SharedConstants.SEARCH_USER){
					logo = new Image(TypeImageBundle.I.get().user());
					logo.setTitle(Msg.consts.user());
				}else{
					logo = new Image(TypeImageBundle.I.get().space());
					logo.setTitle(Msg.consts.space());
				}
				table.setWidget(row, col++, logo);
				
				//spaceTitle or user name
				table.setWidget(row, col++, new Label(sModel.title));
				
				ClickLink add = new ClickLink(Msg.consts.choose());
				if(sModel.type == SharedConstants.SEARCH_USER){
					add.setTitle(Msg.consts.title_send_msg_to_user());
				}else{
					add.setTitle(Msg.consts.title_send_msg_to_space());
				}
				add.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						hidebox();
						int type = SharedConstants.MSG_TARGET_USER;
						if(sModel.type == SharedConstants.SEARCH_SPACE){
							type = SharedConstants.MSG_TARGET_SPACE_CONTRIBUTE_USERS;
						}
						parent.fillTarget(type, sModel.title);
					}
				});
				
				if(sModel.type == SharedConstants.SEARCH_USER){
					table.setWidget(row, col++, new HTML(sModel.contributor));
				}else{
					table.setWidget(row, col++, new HTML(sModel.desc));
				}
				table.setWidget(row,col++, add);
				row++;
			}
			
		}
		public void pageChanging(int pageno) {
			if (keyword == null || keyword.trim().length() == 0)
				return;
			SearchControllerAsync action = ControllerFactory.getSearchController();
			action.searchUserAndSpace(keyword,pageno,SPACE_LIST_SIZE,this);
		}
	}

}
