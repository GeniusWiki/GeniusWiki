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

import java.util.Iterator;
import java.util.List;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.QueryModel;
import com.edgenius.wiki.gwt.client.model.SearchResultItemModel;
import com.edgenius.wiki.gwt.client.model.SearchResultModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.model.WidgetModel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.server.HelperControllerAsync;
import com.edgenius.wiki.gwt.client.server.PortalControllerAsync;
import com.edgenius.wiki.gwt.client.server.SearchControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.AdvSearchPanel;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.LoadingPanel;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.Pagination;
import com.edgenius.wiki.gwt.client.widgets.PaginationListener;
import com.edgenius.wiki.gwt.client.widgets.UserProfileLink;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.EventfulHyperLink;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 * Merge SearchMain from GWT Module into SimplePanel and render as Macro {search:keyword=xxx} on Oct. 25th, 2007. 
 *  
 */
public class SearchRenderWidget extends SimplePanel implements AsyncCallback<SearchResultModel>, PaginationListener,RenderWidget{
	private static final int LOADING_PANEL = 0;
	private static final int RESULT_PANEL = 1;
	private DeckPanel deck = new DeckPanel();
	private MessageWidget message = new MessageWidget();
	private VerticalPanel resultPanel = new VerticalPanel();
	private LoadingPanel loadingPanel = new LoadingPanel();
	private Pagination pagination = new Pagination();
	private AdvSearchPanel advSearchPanel = new AdvSearchPanel(this);
	private Label summary = new Label();
	private RenderWidgetListener listener;
	private String componentKey;
	private String keyword;
	
	public SearchRenderWidget(String keyword){
		this.keyword = keyword;
		initContentPanel();
		
	}
	public void initContentPanel() {

		pagination.addPaginationListener(this);
		pagination.setPageSize(SharedConstants.PAGE_SIZE);
		deck.insert(loadingPanel,LOADING_PANEL);
		deck.insert(resultPanel,RESULT_PANEL);
		deck.showWidget(LOADING_PANEL);
		
		HorizontalPanel sumPanel = new HorizontalPanel();
		sumPanel.add(pagination);
		sumPanel.add(summary);
		
		VerticalPanel leftPanel = new VerticalPanel();
		leftPanel.add(message);
		leftPanel.add(deck);
		leftPanel.add(new HTML("<br>"));
		leftPanel.add(sumPanel);
		
		FlowPanel rightPanel = new FlowPanel();
		rightPanel.add(advSearchPanel);
		HorizontalPanel mainPanel = new HorizontalPanel();
		mainPanel.add(leftPanel);
		mainPanel.add(rightPanel);
		
		mainPanel.setCellWidth(rightPanel, AdvSearchPanel.WIDTH);
		mainPanel.setWidth("100%");
		sumPanel.setWidth("100%");
		leftPanel.setWidth("100%");
		deck.setWidth("98%");
		sumPanel.setWidth("98%");
		sumPanel.setCellHorizontalAlignment(summary, HasHorizontalAlignment.ALIGN_RIGHT);
		sumPanel.setCellVerticalAlignment(summary, HasVerticalAlignment.ALIGN_MIDDLE);
		sumPanel.setCellVerticalAlignment(pagination, HasVerticalAlignment.ALIGN_MIDDLE);
		
		sumPanel.setStyleName(Css.SUMMARY);
		mainPanel.setStyleName(Css.SEARCH_RESULT);
		
		advSearchPanel.setKeyword(this.keyword);

		
		this.setWidget(mainPanel);
	}
	
	public QueryModel  getQuery() {
		
		return advSearchPanel.getQuery();
		
//		String token = History.getToken();
//		
//		int start = token.indexOf(PageMain.TOKEN_CPAGE);
//		start = start + PageMain.TOKEN_CPAGE.length();
//		String params = token.substring(start);
//		
//		return params;
	}
	
	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
		listener.onFailedLoad(componentKey, error.getMessage());
	}
	/**
	 * Search request page
	 */
	public void onSuccess(SearchResultModel model) {
		deck.showWidget(RESULT_PANEL);
		
		if(!GwtClientUtils.preSuccessCheck(model,message)){
			listener.onFailedLoad(componentKey, ErrorCode.getMessageText(model.errorCode, model.errorCode));
			return;
		}
		
		//at moment - it is not necessary to pass meaningful text - just tell listener, it is success with non-empty content 
		listener.onSuccessLoad(componentKey, "Search Results"); //NON-I18N
		fillPanel(model);
		
	}

	/**
	 * @param model
	 */
	private void fillPanel(SearchResultModel model) {
		List<SearchResultItemModel> results = model.results;
		resultPanel.clear();
		for(Iterator<SearchResultItemModel> iter = results.iterator();iter.hasNext();){
			SearchItemPanel item = new SearchItemPanel(iter.next());
			resultPanel.add(item);
		}
		pagination.setTotalItem(model.totalItems);
		pagination.setCurrentPage(model.currPage);
		
		summary.setText(Msg.params.total_result(model.totalItems+""));

	}
	
	public void pageChanging(int pageNo) {
		deck.showWidget(LOADING_PANEL);
		SearchControllerAsync action = ControllerFactory.getSearchController();
		try {
			if(pageNo < 1)
				pageNo = 1;
			action.search(getQuery(),pageNo,pagination.getPageSize(), this);
		} catch (Exception e) {
			//view 1st page
			action.search(getQuery(),1,pagination.getPageSize(), this);
		}
	}
	/**
	 * @param query
	 */
	public void search() {
		//show first page
		pageChanging(1);
	}
	public void onLoad(String widgetKey, UserModel user, RenderWidgetListener listener){
		this.listener = listener;
		this.componentKey = widgetKey;
		listener.onLoading(componentKey);
		//show first page
		pageChanging(1);
	}	
	public void onUserChanged(UserModel user) {
	}

	private class SearchItemPanel extends SimplePanel{
		
		public SearchItemPanel(final SearchResultItemModel item) {
			this.setWidget(createLink(item));
			this.setStyleName(Css.SEARCH_ITEM);
		}
		
		private Widget createLink(final SearchResultItemModel item) {
			//item.title is username if result is User type. 
			Widget typeImg = getTypeImage(item.type,item.title);
			typeImg.setStyleName(Css.ICON);
			
			//must use HTML - there highlight html tag here...
			HTML fragment = new HTML(item.fragment == null?"":item.fragment);
			fragment.setStyleName(Css.FRAGMENT);
			FlowPanel firstLine = new FlowPanel();
			FlowPanel bottomLine = new FlowPanel();
			bottomLine.setStyleName(Css.BOTTOM);
			
			
			if(item.type == SharedConstants.SEARCH_PAGE){
				EventfulHyperLink hLink = new EventfulHyperLink(item.title,GwtUtils.getSpacePageToken(item.spaceUname,item.title));
				hLink.setTitle(Msg.consts.view_page());
				firstLine.add(typeImg);
				firstLine.add(hLink);
				firstLine.add(new HTML(" ("));
				EventfulHyperLink sLink = new EventfulHyperLink(item.spaceUname,GwtUtils.getSpacePageToken(item.spaceUname,null));
				sLink.setTitle(Msg.consts.goto_space());
				firstLine.add(sLink);
				firstLine.add(new HTML(")"));
				
				bottomLine.add(new HTML(Msg.params.updated_by(Msg.consts.page() ) + " "));
				bottomLine.add(getContributorLink(item));
				bottomLine.add(new HTML(" " +Msg.params.at(GwtClientUtils.toDisplayDate(item.date))));
				

			}else if(item.type == SharedConstants.SEARCH_COMMENT){
				EventfulHyperLink hLink = new EventfulHyperLink(item.title,GwtUtils.buildToken(PageMain.TOKEN_COMMENT, item.spaceUname,item.title,item.itemUid));
				hLink.setTitle(Msg.consts.goto_owner_page());
				firstLine.add(typeImg);
				firstLine.add(hLink);

				bottomLine.add(new HTML(Msg.params.created_by(Msg.consts.comment()) + " "));
				bottomLine.add(getContributorLink(item));
				bottomLine.add(new HTML(" " +Msg.params.at(GwtClientUtils.toDisplayDate(item.date))));
				
			}else if(item.type == SharedConstants.SEARCH_SPACE){
				//goto home page of space
				EventfulHyperLink hLink = new EventfulHyperLink(item.title,GwtUtils.getSpacePageToken(item.spaceUname,null));
				hLink.setTitle(Msg.consts.goto_space());
				firstLine.add(typeImg);
				firstLine.add(hLink);

				bottomLine.add(new HTML(Msg.params.created_by(Msg.consts.space()) + " "));
				bottomLine.add(getContributorLink(item));
				bottomLine.add(new HTML(" " + Msg.params.at(GwtClientUtils.toDisplayDate(item.date))));
				
			}else if(item.type == SharedConstants.SEARCH_PAGE_TAG){

				EventfulHyperLink tagCloud = new EventfulHyperLink(item.title,
						GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_TAG_CLOUD), item.spaceUname));
				tagCloud.setTitle(Msg.consts.goto_tagcloud());
				
				firstLine.add(typeImg);
				firstLine.add(tagCloud);
				
				firstLine.add(new HTML(" ("));
				EventfulHyperLink sLink = new EventfulHyperLink(item.spaceUname,GwtUtils.getSpacePageToken(item.spaceUname,null));
				sLink.setTitle(Msg.consts.goto_space());
				firstLine.add(sLink);
				firstLine.add(new HTML(")"));
				
				bottomLine.add(new HTML(Msg.params.created_by(Msg.consts.tag_on_page()) + " "));
				bottomLine.add(getContributorLink(item));
				bottomLine.add(new HTML(" " + Msg.params.at(GwtClientUtils.toDisplayDate(item.date))));

				fragment = null;
			}else if(item.type == SharedConstants.SEARCH_SPACE_TAG){
				firstLine.add(typeImg);
				EventfulHyperLink tagCloud = new EventfulHyperLink(item.title, GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_TAG_CLOUD)));
				tagCloud.setTitle(Msg.consts.goto_tagcloud());
				firstLine.add(tagCloud);
				
				bottomLine.add(new HTML(Msg.params.created_by(Msg.consts.tag_on_space()) + " "));
				bottomLine.add(getContributorLink(item));
				bottomLine.add(new HTML(" " + Msg.params.at(GwtClientUtils.toDisplayDate(item.date))));
				
				fragment = null;
			}else if(item.type == SharedConstants.SEARCH_ATTACHMENT){
				ClickLink referPages = new ClickLink(Msg.consts.goto_owner_page());
				referPages.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						//TODO: if attachment could be shared in pages, this may be a popup list all refer page, rather than simple go to page
						//Get back pageTitle etc according to Attachment file NodeUuid, then jump to page
						HelperControllerAsync helperController = ControllerFactory.getHelperController();
						helperController.getPageTitleByAttachmentNodeUuid(item.spaceUname,item.itemUid,new AsyncCallback<String>(){
							public void onFailure(Throwable error) {
								Window.alert(Msg.consts.redir_fail());
							}
							public void onSuccess(String pageTitle) {
								if(pageTitle == null){
									Window.alert(Msg.consts.redir_fail());
									return;
								}
								//jump to that page
								History.newItem(GwtUtils.getSpacePageToken(item.spaceUname, pageTitle));
							}
						});
					}
				});
				firstLine.add(typeImg);
				//item.itemUid is Attachment file NodeUuid
				firstLine.add(GwtClientUtils.buildDownloadURLWidget(item.spaceUname, item.title , item.itemUid, ""));
				firstLine.add(new HTML(" - "));
				firstLine.add(referPages);
				
				bottomLine.add(new HTML(Msg.params.created_by(Msg.consts.attachment()) + " "));
				bottomLine.add(getContributorLink(item));
				bottomLine.add(new HTML(" " + Msg.params.at(GwtClientUtils.toDisplayDate(item.date))));
				
			}else if(item.type == SharedConstants.SEARCH_USER){
				FlexTable mainLine = new FlexTable();
				String username = item.title;
				String userFullname = item.contributor;
				EventfulHyperLink link = new EventfulHyperLink(Msg.consts.goto_profile(), GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_USER_PROFILE), username));
				
				mainLine.setWidget(0, 0, typeImg);
				mainLine.getFlexCellFormatter().setRowSpan(0, 0, 4);
				
				mainLine.setWidget(0, 1, new Label(Msg.consts.id() +": " + username));
				mainLine.setWidget(1, 0, new Label(Msg.consts.user() +": " + userFullname));
				//user status
				Label status = new Label();
				status.setStyleName(Css.STATUS);
				status.setText(item.desc);
				mainLine.setWidget(2, 0, status);
				mainLine.setWidget(3, 0, link);
				
				firstLine.add(mainLine);
				
				bottomLine.add(new HTML(Msg.params.registered_at(Msg.consts.user(), GwtClientUtils.toDisplayDate(item.date))));
				
				fragment = null;
			}else if(item.type == SharedConstants.SEARCH_WIDGET){
				firstLine.add(typeImg);
				ClickLink toDashboardPortal = new ClickLink(item.title);
				toDashboardPortal.setTitle(Msg.consts.add_to_dashboard());
				toDashboardPortal.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						PortalControllerAsync portalController = ControllerFactory.getPortalController();
						portalController.addWidgetToDashboardPortal(item.itemUid, item.spaceUname, new AsyncCallback<WidgetModel>() {
							public void onSuccess(WidgetModel result) {
								if(!StringUtil.isBlank(result.errorCode)){
									Window.alert(ErrorCode.getMessageText(result.errorCode, result.errorMsg));
									return;
								}
								//go to dashboard directly
								History.newItem("");
							}
							
							public void onFailure(Throwable caught) {
								Window.alert(Msg.consts.unknown_error());
							}
						});
					}
				});
				firstLine.add(toDashboardPortal);
				
				bottomLine.add(new HTML(Msg.params.created_by(Msg.consts.widget()) + " "));
				bottomLine.add(getContributorLink(item));
				bottomLine.add(new HTML(" " + Msg.params.at(GwtClientUtils.toDisplayDate(item.date))));

			}
			
			VerticalPanel container = new VerticalPanel();
			container.setSpacing(4);
			container.add(firstLine);
			if(fragment != null)
				container.add(fragment);
			container.add(bottomLine);
			container.setCellHeight(bottomLine, "30px");
			container.setWidth("100%");
			return container;
			
		}

		/**
		 * @param item
		 */
		private UserProfileLink getContributorLink(final SearchResultItemModel item) {
			return new UserProfileLink(item.contributor,  null ,item.contributorUsername,null);
			
		}
		private Widget getTypeImage(int type, String username){
			if(type == SharedConstants.SEARCH_PAGE){
				return new Image(TypeImageBundle.I.get().page());
			}else if(type == SharedConstants.SEARCH_SPACE){
				return new Image(TypeImageBundle.I.get().space());
			}else if(type == SharedConstants.SEARCH_PAGE_TAG){
				return new Image(TypeImageBundle.I.get().ptag());
			}else if(type == SharedConstants.SEARCH_SPACE_TAG){
				return new Image(TypeImageBundle.I.get().stag());
			}else if(type == SharedConstants.SEARCH_ATTACHMENT){
				return new Image(TypeImageBundle.I.get().attachment());
			}else if(type == SharedConstants.SEARCH_USER){
				return GwtClientUtils.createUserPortraitByUsername(username);
			}else if(type == SharedConstants.SEARCH_COMMENT){
				return new Image(TypeImageBundle.I.get().comment());
			}else if(type == SharedConstants.SEARCH_WIDGET){
				return new Image(TypeImageBundle.I.get().widget());
			}
			return new Image(TypeImageBundle.I.get().page());
		}

	}
	public interface TypeImageBundle extends ClientBundle {
		public class I{
			private static TypeImageBundle iconBundle;
			public static TypeImageBundle get(){
				if(iconBundle == null)
					iconBundle = (TypeImageBundle) GWT.create(TypeImageBundle.class);
				return iconBundle;
			}
		}

		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/user.png")
		public ImageResource user();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/page.png")
		public ImageResource page();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/book_open.png")
		public ImageResource space();

		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/ptag.png")
		public ImageResource ptag();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/stag.png")
		public ImageResource stag();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/attach.png")
		public ImageResource attachment();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/comment.png")
		public ImageResource comment();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/wand.png")
		public ImageResource widget();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/type.png")
		public ImageResource type();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/relative.png")
		public ImageResource score();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/user_dis.png")
		public ImageResource user_dis();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/page_dis.png")
		public ImageResource page_dis();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/book_open_dis.png")
		public ImageResource space_dis();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/ptag_dis.png")
		public ImageResource ptag_dis();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/stag_dis.png")
		public ImageResource stag_dis();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/attach_dis.png")
		public ImageResource attachment_dis();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/comment_dis.png")
		public ImageResource comment_dis();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/wand_dis.png")
		public ImageResource widget_dis();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/type_dis.png")
		public ImageResource type_dis();
		
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/relative_dis.png")
		public ImageResource score_dis();
	
	}
	
}
