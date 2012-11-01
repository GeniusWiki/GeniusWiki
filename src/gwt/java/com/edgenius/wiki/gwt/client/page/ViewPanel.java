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
package com.edgenius.wiki.gwt.client.page;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.page.widgets.AttachmentButton;
import com.edgenius.wiki.gwt.client.page.widgets.AttachmentPanel;
import com.edgenius.wiki.gwt.client.page.widgets.ExportButton;
import com.edgenius.wiki.gwt.client.page.widgets.FavoriteButton;
import com.edgenius.wiki.gwt.client.page.widgets.PrettyUrlButton;
import com.edgenius.wiki.gwt.client.page.widgets.PrettyUrlPanel;
import com.edgenius.wiki.gwt.client.page.widgets.RSSFeedButton;
import com.edgenius.wiki.gwt.client.page.widgets.TagsPanel;
import com.edgenius.wiki.gwt.client.page.widgets.WatchButton;
import com.edgenius.wiki.gwt.client.render.PageRender;
import com.edgenius.wiki.gwt.client.render.RenderContentListener;
import com.edgenius.wiki.gwt.client.render.RenderPanel;
import com.edgenius.wiki.gwt.client.render.WikiRenderPanel;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.constant.PageType;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.PageAttribute;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.UploadDialog;
import com.edgenius.wiki.gwt.client.widgets.UserProfileLink;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class ViewPanel extends DiffPanel implements AsyncCallback<PageModel>, PanelSwitchListener{
	
	private FavoriteButton favorite = new FavoriteButton();
	private WatchButton watch = new WatchButton(); 
	private RSSFeedButton rssFeedBtn = new RSSFeedButton(); 
	private ExportButton exportBtn = new ExportButton(); 
	
	private AttachmentButton attachmentBtn;
	AttachmentPanel attPanel;
	private PrettyUrlButton  pUrlBtn;
	private PrettyUrlPanel pUrlPanel = new PrettyUrlPanel();
	
	private Label title = new Label();
	TagsPanel tags = new TagsPanel();

	private RenderPanel contentPanel = new WikiRenderPanel();
	private RenderPanel spaceMenu = new WikiRenderPanel();
	
	public PageTabPanel tabPanel;
	
	public ViewPanel(final PageMain main) {
		super(main);
		
		main.addPanelSwitchListener(this);
		this.setWidget(buildViewPanel(main));
	}

	/**
	 * @param main
	 * @return
	 */
	private HTMLPanel buildViewPanel(final PageMain main) {
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Attachment and PrettyURL
		attPanel = new AttachmentPanel(main,true);
		attachmentBtn = new AttachmentButton(attPanel);
		pUrlBtn = new PrettyUrlButton(pUrlPanel);
		pUrlPanel.setVisible(false);
		attPanel.setVisible(false);
		main.registerAttachmentPanel(attPanel);
		
		//turn on/off depends on the login status
		favorite.setVisible(false);
		watch.setVisible(false);
		
		title.setStyleName(Css.RENDER_TITLE);
	  
	    //main content panel, contains contentPanel, diffContent and SideBar panel.
		FlexTable mainPanel = new FlexTable();
	    mainPanel.setWidget(0, 0, contentPanel);
	    mainPanel.setWidget(0, 1, diffContent);
	    
	    mainPanel.getCellFormatter().setAlignment(0, 0,HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
	    mainPanel.getCellFormatter().setAlignment(0, 1,HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
	    mainPanel.setWidth("100%");
	    
	    diffMessage.setVisible(false);
	    //build panel
		tabPanel = new PageTabPanel(main);

		HTMLPanel hpanel = new HTMLPanel(readViewLayout());
		hpanel.add(spaceMenu, "_spacemenu");
		hpanel.add(message, "_message");
		hpanel.add(diffMessage, "_diff_message");
		hpanel.add(title, "_title");
		hpanel.add(tags, "_tags");
		hpanel.add(functionBtnBar, "_actions");
		hpanel.add(attachmentBtn, "_attchments");
		hpanel.add(attPanel, "_attchments_panel");
		hpanel.add(pUrlBtn, "_urls");
		hpanel.add(pUrlPanel, "_urls_panel");
		hpanel.add(mainPanel, "_body");
		hpanel.add(favorite, "_favorite");
		hpanel.add(watch, "_watch");
		hpanel.add(rssFeedBtn, "_rss");
		hpanel.add(exportBtn, "_export");
		//history,comment, summary tab panel.
		hpanel.add(tabPanel, "_tabs");
		
		return hpanel;
	}


	/**
	 * Reset token to view page token. But it does not invoke the event
	 */
	public void resetToken() {
		//remove $EDIT or $CREATE suffix
		History.newItem(GwtUtils.getSpacePageToken(main.getSpaceUname(),main.getCurrentPageTitle()),false);
	}
	
	/**
	 * Set panel as visible.
	 * @param model
	 */
	public void showPanel(PageModel model){
		message.cleanMessage();
		//model == null when first time load empty page.
		fillPanel(model);
		main.switchTo(PageMain.VIEW_PANEL);
	}
	/**
	 * Only put value to panel, does not change visible attribute of this panel.
	 * @param model
	 */
	public void fillPanel(final PageModel model) {
		//hide pin image always at first
		GwtClientUtils.hidePagePin(true);
		if(model == null){
			//edit is disable: current, this is just failure tolerance handle. Model never expect be null.
			functionBtnBar.loadViewFunc(null);
			return;
		}
		//view panel switch to HistoryCompare, if user go Dashboard, then go back same page, it is still in diffPanel
		//, here will switch it back to normal.
		diffResume();

		//default value for hide all tabs
		PageRender render = new PageRender(contentPanel);
		
		if(model.isHistory){
			//NOTE: this title may not equals model.title as Page Title maybe changed in editing history.
			historyDiffMessage(model);
			
			functionBtnBar.loadHistoryFunc(model.permissions, model.currentTitle,model.pageVersion);
			//history page can not update main.setPageTitle() and main.PageUid() 
			main.setCurrentPageTitle(model.currentTitle);
			//always show history tab if users viewing history
			model.tabIndex = SharedConstants.TAB_TYPE_HISTORY;
			
		}else{
			functionBtnBar.loadViewFunc(model);
			if(model.pintop > 0){
				GwtClientUtils.hidePagePin(false);
			}
			main.setCurrentPageTitle(model.title);
			
			// here is just for hide welcome message (validate if page has render content)
			//as PageRender logic, this method is must call before render.renderContent() method so 
			//that the listener can be invoked
			render.addRenderContentListener(new WelcomeMessageListener(model.pageUuid));
		}
		
		Window.setTitle(model.title+ " - " + main.getSystemTitle());
		
		//update page info
//		boolean sameSpace = StringUtil.equals(main.getSpaceUname(),model.spaceUname);
		main.setSpaceUname(model.spaceUname);
		main.setPageAttribute(model.attribute);
		main.setPageVersion(model.pageVersion);
		main.setPageUuid(model.pageUuid == null?"":model.pageUuid.toString());
		
		main.resetNavbar(model);
		
		main.setPrintBtnVisible(!GwtUtils.contains(model.attribute, PageAttribute.NO_PRINT));
		if(GwtUtils.contains(model.attribute,PageAttribute.NO_FAVORITE)){
			//logout: hide add favorite/watch button
			favorite.setVisible(false);
		}else{
			//login
			favorite.setVisible(true);
			//enable add favorite, watch 
			favorite.setEnable(model.favorite==1?true:false);
		}
		if(GwtUtils.contains(model.attribute,PageAttribute.NO_WATCHED)){
			watch.setVisible(false);
		}else{
			watch.setVisible(true);
			watch.setEnable(model.watched==1?true:false);
		}
		
		if(GwtUtils.contains(model.attribute,PageAttribute.NO_TITLE)){
			title.setText("");
		}else{
			title.setText(model.title);
		}
		
		if(GwtUtils.contains(model.attribute,PageAttribute.NO_TAG)){
			//clean tag panel.
			tags.hide();
		}else{
			//display tags.
			tags.display(model.spaceUname, model.tagString, model.permissions[ClientConstants.WRITE] == 1);
		}

		main.sidebar.fillPanel(model);
		
		if(!GwtUtils.contains(model.attribute,PageAttribute.NO_ATTACHMENT)){
			attachmentBtn.setVisible(true);
			attPanel.mergeAttachments(model.attachmentJson);
			//does this user has permission to edit attachment?
			//page write permission
			//Anonymous cannot allow update attachment -
			//* it is dangerous 
			//* uploaded attachment in editing model, the initial status of attachment is draft status, 
			//  then it cannot decide how to set attachment status to normal as same page may have many anonymous uploading
			//* if anonymous cancel from editing - it also can not decide which attachments need to be deleted. 
			if(model.permissions[ClientConstants.WRITE] == 1 && !GwtUtils.isAnonymous(main.getLoginUser())){
				attPanel.setReadonly(false);
			}else{
				attPanel.setReadonly(true);
			}
		}else{
			//hide all attachment and panel...
			setAttachmentCount(0);
			attachmentBtn.setVisible(false);
			attPanel.setVisible(false);
			attPanel.reset();
		}
		
		if(!GwtUtils.contains(model.attribute,PageAttribute.NO_PRETTY_URL)){
			pUrlBtn.setVisible(true);
		}else{
			pUrlBtn.setVisible(false);
		}
		
		
		render.renderContent(model.spaceUname, model,model.renderContent, false);
		
		PageRender spaceMenuRender = new PageRender(spaceMenu);
		spaceMenuRender.renderContent(model.spaceUname, model,model.spaceMenuContent, false);
		
		if(!GwtUtils.contains(model.attribute,PageAttribute.NO_UNDER_TAB_BAR)){
			//reset if a new page view: set comment read permission
			tabPanel.setVisible(true);
			tabPanel.reset(model.permissions[ClientConstants.COMMENT_READ], model.tabIndex);
			if(model.tabFocus){
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					public void execute() {
						GwtClientUtils.gotoAnchor(PageTabPanel.TABS_ANCHOR_NAME);
					}
				});
			}
		}else{
			tabPanel.clear();
			tabPanel.setVisible(false);
		}
		
		if (!GwtUtils.contains(model.attribute, PageAttribute.NO_RSS)){
			rssFeedBtn.setSpaceUname(model.spaceUname);
		}else{
			//this will hide rssFeedBtn
			rssFeedBtn.setSpaceUname(null);
		}
		
		if(!GwtUtils.contains(model.attribute,PageAttribute.NO_EXPORT)){
			exportBtn.setSpaceUname(main);
		}else{
			//this will hide exportBtn
			exportBtn.setSpaceUname(null);
		}
		
		//always needs to refresh, even same space - if a new page added or page title updated.
		main.refreshTreeItem(model.spaceUname,model.pageUuid);
//		if(!sameSpace){
//			main.refreshTreeItem(model.spaceUname,model.pageUuid);
//		}else{
//			main.selectTreeItem(model.pageUuid);
//		}
		//locate to anchor
		String token = GwtClientUtils.getToken();
		final String anchor = GwtUtils.getAnchor(token);
		if(anchor != null){
			Log.info("Get anchor " + anchor);
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				public void execute() {
					GwtClientUtils.gotoAnchor(anchor);
				}
			});
		}
	}

	public void login(UserModel user) {
		
		
	}

	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
	}
	/**
	 * Show content after page content loaded.
	 */
	public void onSuccess(PageModel model) {

		if(!GwtClientUtils.preSuccessCheck(model,message)){
			main.errorOnLoadingPanel(ErrorCode.getMessageText(model.errorCode,model.errorMsg));
			return;
		}
		showPanel(model);
	}
	/*
	 * Show history comparison diff panel
	 */
	protected void diffRendered() {
		super.diffRendered();
		
		functionBtnBar.loadVersionDiff(true);
		diffContent.setVisible(true);
		contentPanel.setVisible(false);
		
	}
	/*
	 * Read-only diff: history compare
	 */
	protected void diffResume() {
		super.diffResume();
		
		functionBtnBar.resume();
		diffContent.setVisible(false);
		contentPanel.setVisible(true);
	}
	
	public void setAttachmentCount(int attsCount){
		attachmentBtn.setCount(attsCount);
	}

	/**
	 * Allow attachment panel is open and a new item upload display (if permission allow)
	 */
	public void viewAttachment(){
		if(attachmentBtn.isVisible()){
			attPanel.setVisible(true);
			if(!attPanel.isReadonly()){
				UploadDialog dialog = new UploadDialog(attPanel, PageMain.getSpaceUname(), PageMain.getPageUuid(), PageType.NONE_DRAFT);
	            dialog.showbox();
			}
		}
	}
	public void toggleAttachmnet(){
		if(attachmentBtn.isVisible()){
			attPanel.setVisible(!attPanel.isVisible());
		}
	}
	public void onPanelSwitched(Widget widget, int toIndex) {
		if(widget != this)
			return;
		
		if(toIndex != PageMain.VIEW_PANEL){
			//this is for hiding CopyClip flash - side effect is, it is different with attachment panel which is keep open/close status when switch back 
			//from other panel, for example, from View->Edit->View, if both prettyURL and attachment open at beginning, but finally, only attachment panel is open 
			pUrlPanel.setVisible(false);
		}
	}

	public boolean onPanelSwitching(Widget widget, int toIndex) {
		return true;
	}

	/**
	 * @return View layout from javascript.
	 */
	private native String readViewLayout()/*-{
	   return $wnd._view_layout;
	}-*/;

	private void historyDiffMessage(final PageModel model) {
		String spaceUname = model.spaceUname;
		String currentTitle = model.currentTitle;
		
		message.cleanMessage();
		
		String id1 = HTMLPanel.createUniqueId();
		String id2 = HTMLPanel.createUniqueId();
		StringBuffer buf = new StringBuffer("<div class='historyAction'><div class='msg' id='").append(id1)
				.append("'></div><div class='return' id='").append(id2).append("'></div>");
		buf.append("<div class='action'>");
		
		String idp1 = null,idp2 = null,idp3 = null,idp4 = null,idn1 = null,idn2 = null,idn3 = null,idn4 = null;
		if(model.nextHistoryItem != null){
			idn1 = HTMLPanel.createUniqueId();
			idn2 = HTMLPanel.createUniqueId();
			idn3 = HTMLPanel.createUniqueId();
			idn4 = HTMLPanel.createUniqueId();
			
			buf.append("<div class='round next'><div class='version' id='").append(idn1).append("'></div><div class='author' id='")
			.append(idn2).append("'></div><div class='date' id='").append(idn3).append("'></div><div class='diff' id='").append(idn4).append("'></div></div>");
			
		}
		buf.append("<div class='current'>").append(Msg.consts.revision()).append(" ").append(model.pageVersion).append("</div>");
		if(model.prevHistoryItem != null){
			idp1 = HTMLPanel.createUniqueId();
			idp2 = HTMLPanel.createUniqueId();
			idp3 = HTMLPanel.createUniqueId();
			idp4 = HTMLPanel.createUniqueId();
				
			buf.append("<div class='round prev'><div class='version' id='").append(idp1).append("'></div><div class='author' id='")
				.append(idp2).append("'></div><div class='date' id='").append(idp3).append("'></div><div class='diff' id='").append(idp4).append("'></div></div>");
		}
		buf.append("</div></div>");
		HTMLPanel msgPanel = new HTMLPanel(buf.toString());
		Hyperlink retCurrentVerBtn = new Hyperlink(Msg.consts.return_latest_version(), GwtUtils.getSpacePageToken(spaceUname,currentTitle));
		msgPanel.add(new Label(Msg.consts.view_history()), id1);
		msgPanel.add(retCurrentVerBtn, id2);
		
		if(model.prevHistoryItem != null){
			historyNextPrevMsg(msgPanel, model.prevHistoryItem, model.uid, idp1, idp2, idp3, idp4);
		}
		if(model.nextHistoryItem != null){
			historyNextPrevMsg(msgPanel, model.nextHistoryItem, model.uid, idn1, idn2, idn3, idn4);
		}
		
		HorizontalPanel panel = new HorizontalPanel();
		panel.add(msgPanel);
		message.warning(panel,false);
	}

	private void historyNextPrevMsg(HTMLPanel msgPanel, final PageItemModel history, final Integer pageUid, 
			String idp1,String idp2, String idp3, String idp4) {
		
		String spaceUname = history.spaceUname;
		Hyperlink preLink;
		if(history.version == SharedConstants.CURRENT){
			//latest page
			preLink= new Hyperlink(Msg.consts.latest(), GwtUtils.getSpacePageToken(spaceUname, history.title));
		}else{
			preLink= new Hyperlink(Msg.consts.revision() + " " + history.version
					,GwtUtils.buildToken(PageMain.TOKEN_HISTORY,spaceUname, String.valueOf(history.uid)));
		}
		
		UserProfileLink modifier = new UserProfileLink(history.modifier, spaceUname, history.modifierUsername,history.modifierPortrait);
		Label modifiedDate = new Label(GwtClientUtils.toDisplayDate(history.modifiedDate));
		//do compare
        ClickLink compareButton = new ClickLink(Msg.consts.compare());
        compareButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                PageControllerAsync action = ControllerFactory.getPageController();
                //a little confuse here, version and uid both as same value if it is current page... 
                action.diff(history.version == SharedConstants.CURRENT ? SharedConstants.CURRENT: history.uid, pageUid, versionAsync);
            }
        });
//		Hyperlink compareButton = new Hyperlink(Msg.consts.compare()
//				, GwtUtils.buildToken(PageMain.TOKEN_DIFF,String.valueOf(history.version==SharedConstants.CURRENT ? SharedConstants.CURREN:history.uid), String.valueOf(pageUid)));
		
		msgPanel.add(preLink,idp1);
		msgPanel.add(modifier, idp2);
		msgPanel.add(modifiedDate, idp3);
		msgPanel.add(compareButton, idp4);
	}

	
	//********************************************************************
    //               inner class
    //********************************************************************

	private class WelcomeMessageListener implements RenderContentListener, AsyncCallback<PageModel>{
		private String pageUuid;

		private StringBuffer content = new StringBuffer();
		public WelcomeMessageListener(String pageUuid) {
			this.pageUuid = pageUuid;
		}

		public void render(String text) {
			content.append(text);
		}

		public void renderEnd(String text) {
			//whatever render() - normally input is pure text or link - or any Ajax loading RenderWidget,
			//if they don't have any input text, then display Welcome message on page - getEmptyPageDefaultContent() only return
			//Welcome message for home page.
			if(StringUtil.isBlank(text) && StringUtil.isBlank(content.toString())){
				//if page has empty render content, then try to get default empty message, at moment, only home page render meaningful messsage...  
				PageControllerAsync action = ControllerFactory.getPageController();
				action.getEmptyPageDefaultContent(pageUuid, this);
			}
			
		}


		public void renderStart() {
			content = new StringBuffer();
		}
		
		public void onFailure(Throwable caught) {
			
		}

		public void onSuccess(PageModel page) {
			//but be careful, in callback, it must check if current display content is still same with before ajax sent out
			//in some case, user may switch page so quick, the page already is another page while callback returns
			if(StringUtil.equals(page.pageUuid, main.getPageUuid()) && page.pageVersion == main.getPageVersion()){
				PageRender render = new PageRender(contentPanel);
				render.renderContent(page.spaceUname, page,page.renderContent, false);				
			}
		}
	}


}
