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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.BaseEntryPoint;
import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.KeyCaptureListener;
import com.edgenius.wiki.gwt.client.KeyMap;
import com.edgenius.wiki.gwt.client.constant.PageSaveMethod;
import com.edgenius.wiki.gwt.client.editor.EditorRegistry;
import com.edgenius.wiki.gwt.client.home.DashboardPanel;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.page.widgets.AttachmentListener;
import com.edgenius.wiki.gwt.client.page.widgets.AttachmentPanel;
import com.edgenius.wiki.gwt.client.page.widgets.LocationButton;
import com.edgenius.wiki.gwt.client.page.widgets.PrintButton;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.PluginControllerAsync;
import com.edgenius.wiki.gwt.client.server.TemplateControllerAsync;
import com.edgenius.wiki.gwt.client.server.constant.PageType;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.NumberUtil;
import com.edgenius.wiki.gwt.client.server.utils.PageAttribute;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.space.ShellDialog;
import com.edgenius.wiki.gwt.client.widgets.CalendarDetailDialog;
import com.edgenius.wiki.gwt.client.widgets.LoadingPanel;
import com.edgenius.wiki.gwt.client.widgets.NavItem;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * @author Dapeng.Ni
 */
public class PageMain extends BaseEntryPoint implements ValueChangeHandler<String>, AttachmentListener{
	public static final int LOADING_PANEL = 0;
	public static final int VIEW_PANEL = 1;
	public static final int EDIT_PANEL = 2;
	public static final int PREVIEW_PANEL = 3;
	public static final int DASHBOARD_PANEL = 4;
	public static final int EDIT_SIDEBAR_PANEL = 5;
	public static final int EDIT_TEMPLATE_PANEL = 6;
	public static final String PAGE_TOP_ANCHOR_NAME = HTMLPanel.createUniqueId();
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//URL: /page#/spaceUname/pageTitle
	public static final String TOKEN_VIEW = "";
	//URL: /page#/spaceUname/pageTitle/commentUid
	public static final String TOKEN_COMMENT = SharedConstants.TOKEN_COMMENT;
	//URL: /page#/$CREATE[$CHOME]/spaceUname/newPageTitle
	public static final String TOKEN_CREATE = "$CREATE";
	public static final String TOKEN_CREATE_HOME = "$CHOME";
	
	//URL: /page#/$EDIT/spaceUname/pageUuid 
	public static final String TOKEN_EDIT = "$EDIT";
	//URL: /page#/$ES/spaceUname/sidebarUuid
	public static final String TOKEN_EDIT_SIDEBAR = "$ES";
	//URL: /page#/$ET/spaceUname/templateUid : if TemplateID is blank, display blank page for create new one
	public static final String TOKEN_EDIT_TEMPLATE = SharedConstants.TOKEN_EDIT_TEMPLATE;
	//URL: /page#/$DRAFT/spaceUname/draftType/draftUid
	public static final String TOKEN_DRAFT = "$DRAFT";
	//URL: /page#/$HISTORY/spaceUname/historyUid
	public static final String TOKEN_HISTORY = "$HISTORY";
	
	//URL: /page#/$HISTORY/spaceUname/historyUid
	public static final String TOKEN_DIFF = "$DIFF";
	//no use so far
	public static final String TOKEN_OPEN_NEW_WINDOW = "$POPUP";
	
	//customised page token, used in TagCloud,etc.
	//Customised page indicator, format like: #CustomerPageId_TOKEN_
	//URL: /page#/$CPAGE/cpageUID/parameters
	public static final String TOKEN_CPAGE = SharedConstants.TOKEN_CPAGE;
	//URL: /page#/$CL/spaceUname/cLinkToken
	public static final String TOKEN_CLINK = SharedConstants.TOKEN_CLINK;

	
	private String oldSpaceUname;
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Field in GWT Module scope 
	private static String sysParentPageUuid;
	private static String sysSpaceUname;
	private int sysPageType;
	private int sysPageAttribute;
	private static String sysPageUuid;
	private int sysPageVersion;
	private static String sysPageTitle;
	private boolean sysPreviewReady;
	private int sysFromPanelIndex;;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public PrintButton printBtn;
	private HTML printSep;
	//hold view/edit panel
	private DeckPanel deck = new DeckPanel();

	private Frame downloadFrm = new Frame();
	
	//before ViewPanel and EditPanel initialized - as they will put attachment panel into this Vector. 
	private Vector<AttachmentPanel> attachmentPanels = new Vector<AttachmentPanel>();
	private LoadingPanel loadingPanel = new LoadingPanel();
	//initial before EditPanel and EditSidebarPanel
	public EditorRegistry editorRegistry = new EditorRegistry();
	public DashboardPanel dashboardPanel = new DashboardPanel(this);
	public ViewPanel viewPanel = new ViewPanel(this);
	public EditPanel editPanel = new EditPanel(this);
	public SideBar sidebar = new SideBar(); 
	public EditSidebarPanel editSidebarPanel = new EditSidebarPanel(this);
	public EditTemplatePanel editTemplatePanel = new EditTemplatePanel(this);
	public PreviewPanel previewPanel = new PreviewPanel(this);
	
	private LocationButton location;
	private static UserModel loginUser = null;

	private List<PanelSwitchListener> panelSwitchListeners;
	
	public void reload() {
		checkLogin();
		
	    String initToken = GwtClientUtils.getToken();
		Log.info("Page reload token:" + initToken);
		
	    if (initToken.length() > 0) {
	    	 History.fireCurrentHistoryState();
	    } else {
			//go to dashboard!
	    	loading();
	    	dashboardPanel.showPanel();
	    }
	    
	    getGlobalMessage().cleanMessage();
	}

	public void login(UserModel user) {
		//broadcast login message
		dashboardPanel.login(user);
		viewPanel.login(user);
		editPanel.login(user);
		editSidebarPanel.login(user);
		editTemplatePanel.login(user);
		loginUser = user;
	}
	
	public void addPanelSwitchListener(PanelSwitchListener listener){
		if(panelSwitchListeners == null){
			panelSwitchListeners = new ArrayList<PanelSwitchListener>();
		}
		panelSwitchListeners.add(listener);
	}
	public boolean executeKeyShortcut(boolean ctrlKey, boolean altKey,
		      boolean shiftKey, boolean metaKey, int keyCode){
		
		//KeyCaptureListener.globalCapture means some textbox is getting input focus so far, so all shortcut won't work
		if(!KeyCaptureListener.globalCapture)
			return true;
		
		int idx = deck.getVisibleWidget();
		//DEBUG key: Shift_Alt_F12
		if(KeyMap.isKey(KeyMap.DEBUG, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				String str = 
				" pageUuid= "+PageMain.getPageUuid()+ "  \n"
				+" version= "+this.getPageVersion()+ "  \n"				
				+" pageAttribute= "+this.getPageAttribute()+ "  \n"
				+" parentPageUuid= "+getParentPageUuid()+ "  \n"
				+" pageTitle= "+PageMain.getCurrentPageTitle() + "  \n"
				+" spaceUname= "+ getSpaceUname()+ "  \n"				
				+" previewReady= "+this.isPreviewReady()+ "  \n"				
				+" newPageType(New, home or common page)= "+this.getNewPageType()+ "  \n"				
				+" fromPanelIdx= "+this.getFromPanelIndex()+ "  \n"
				+" editingDirty= "+this.editPanel.isDirty() + "  \n"
				;				
				Window.alert(str);
		}
		
		if(idx == VIEW_PANEL){
			//shift-E to edit page, shift-N to create new page
			if(KeyMap.isKey(KeyMap.VIEW_CREATE, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				viewPanel.functionBtnBar.createPage();
				return false;
			}else if(KeyMap.isKey(KeyMap.VIEW_EDIT, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				viewPanel.functionBtnBar.editPage();
				return false;
			}else if(KeyMap.isKey(KeyMap.VIEW_NEW_TAG, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				viewPanel.tags.editTag();
				return false;
			}else if(KeyMap.isKey(KeyMap.VIEW_NEW_COMMENT, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				viewPanel.tabPanel.newComment();
				GwtClientUtils.gotoAnchor(PageTabPanel.TABS_ANCHOR_NAME);
				return false;
			}else if(KeyMap.isKey(KeyMap.VIEW_TOGGLE_COMMENT, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				viewPanel.tabPanel.toggleComment();
				GwtClientUtils.gotoAnchor(PageTabPanel.TABS_ANCHOR_NAME);
				return false;
			}else if(KeyMap.isKey(KeyMap.VIEW_TOGGLE_ATTACHMENT, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				viewPanel.toggleAttachmnet();
				GwtClientUtils.gotoAnchor(PageMain.PAGE_TOP_ANCHOR_NAME);
				return false;
			}else if(KeyMap.isKey(KeyMap.VIEW_NEW_ATTACHMENT, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				viewPanel.viewAttachment();
				GwtClientUtils.gotoAnchor(PageMain.PAGE_TOP_ANCHOR_NAME);
				return false;
			}else if(KeyMap.isKey(KeyMap.VIEW_TOGGLE_HISTORY, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				viewPanel.tabPanel.toggleHistory();
				GwtClientUtils.gotoAnchor(PageTabPanel.TABS_ANCHOR_NAME);
				return false;
			}else if(KeyMap.isKey(KeyMap.VIEW_CLOSE_PIN_PANEL, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				viewPanel.tabPanel.close();
				return false;
			}else if(KeyMap.isKey(KeyMap.VIEW_TOGGLE_TREE, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				if(toggleSideMenu(ClientConstants.LEFT)){
					GwtClientUtils.gotoAnchor(PageMain.PAGE_TOP_ANCHOR_NAME);
					location.refresh(getSpaceUname(), getPageUuid());
				}
				return false;
			}
		}else if(idx==EDIT_SIDEBAR_PANEL ){
		}else if(idx==EDIT_TEMPLATE_PANEL){
			
		}else if(idx == EDIT_PANEL ){
			//ctrl-S/shift-alt-s to save-page-exit, shift-S to save-draft-exit, shift-c to Cancel, shift-p to preview 
			if(KeyMap.isKey(KeyMap.EDIT_PUBLISH, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				editPanel.functionBtnBar.savePage();
				return false;
			}else if(KeyMap.isKey(KeyMap.EDIT_SAVE, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				//false: don't exit to view page
				editPanel.functionBtnBar.saveDraftPage(false);
				return false;
			}else if(KeyMap.isKey(KeyMap.EDIT_CANCEL, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				editPanel.functionBtnBar.cancelPage();
				return false;
			}else if(KeyMap.isKey(KeyMap.EDIT_PREVIEW, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				editPanel.functionBtnBar.previewPage();
				return false;
			}else if(KeyMap.isKey(KeyMap.EDIT_FORCE_SAVE, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				editPanel.functionBtnBar.forceSave();
				return false;
			}
		}else if(idx == PREVIEW_PANEL){
			//shift-r resume from preview
			if(KeyMap.isKey(KeyMap.EDIT_PUBLISH, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				previewPanel.functionBtnBar.savePage();
				return false;
			}else if(KeyMap.isKey(KeyMap.EDIT_SAVE, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				previewPanel.functionBtnBar.saveDraftPage(false);
				return false;
			}else if(KeyMap.isKey(KeyMap.PREVIEW_RESUME, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				previewPanel.functionBtnBar.resumePreviewPage();
				return false;
			}else if(KeyMap.isKey(KeyMap.EDIT_FORCE_SAVE, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
				previewPanel.functionBtnBar.forceSave();
				return false;
			}		
		}
		
		return true;
	}

	public void initContentPanel() {
		//clean navbar
		List<NavItem> linkList = new ArrayList<NavItem>();
		linkList.add(new NavItem(Msg.consts.dashboard(),"", null));
		refreshNavbar(linkList);
		
		//location
		location = new LocationButton(this,false);
		printBtn = new PrintButton(this,false);
		
		Anchor topAnchor = new Anchor();
		topAnchor.setName(PAGE_TOP_ANCHOR_NAME);
		addBeforeNav(topAnchor);
		
		addBeforeNav(location);
		printSep = addAfterLogin(printBtn);
		//this will be reset when page filled: according to PageAttribute.NO_PRINT
		setPrintBtnVisible(false);
		
		History.addValueChangeHandler(this);
		
		downloadFrm.setVisible(false);
		downloadFrm.setSize("0px","0px");
		DOM.setStyleAttribute(downloadFrm.getElement(), "border", "0px");
		DOM.setStyleAttribute(downloadFrm.getElement(), "position", "absolute");
		
		loadingPanel.addWidget(downloadFrm);
		deck.insert(loadingPanel,LOADING_PANEL);
		deck.insert(viewPanel,VIEW_PANEL);
		deck.insert(editPanel,EDIT_PANEL);
		deck.insert(previewPanel,PREVIEW_PANEL);
		deck.insert(dashboardPanel,DASHBOARD_PANEL);
		deck.insert(editSidebarPanel,EDIT_SIDEBAR_PANEL);
		deck.insert(editTemplatePanel,EDIT_TEMPLATE_PANEL);
		
		this.setSideMenuPanel(ClientConstants.RIGHT,sidebar);
		
	    deck.setStyleName(Css.MAIN_PANEL);
	    
	    RootPanel.get(CONTENT_PANEL).add(deck);		
	    
	    exposeJSMethods();
	}


	/**
	 * Expose GWT methods to native javascript 
	 */
	private void exposeJSMethods() {
		CalendarDetailDialog.bindJsMethod();
		ShellDialog.bindJsMethod();
	}

	/**
	 * 
	 * Monitoring all HyperLink click 
	 */
	public void onValueChange(ValueChangeEvent<String> event) {
		String token = event.getValue();
		//page#/xxx - xxx must do URLEncode otherwise it will broken URL even its value in anchor part
		//I found FF looks OK if there is no this decode, but Chrome does work. Anyway, it is safe to decode here and no side-effect.
		token = URL.decodeQueryString(token);
		
		Log.info("Page onHistoryChanged event for " + token);
		//show loading page first - this is give a change to stop switch panel, useful if editing page is dirty, user clicks nav bar or 
		//page tree item to switch to view another page accident.
		if(!loading())
			return;
		
		if(token == null || token.trim().length() == 0){
			//dashboard is pure URL like: /page#
			dashboardPanel.showPanel();
			return;
		}
		token = token.trim();
		
		//clean attachment panel for new page
		//broadcast event to all attachment panel - for example, the attachment panel in editPanel and viewPanel
		resetAttachmentPanel();
		
		String[] tokens = GwtUtils.parseToken(token);
		Log.info("Parsed tokens:" + Arrays.toString(tokens));
		
		//just try to set a value to spaceUname - it is either second parameter(first is action identifier) in URL or set to default 
		//the exception is view page - spaceUname is first parameter, - customised page(no spaceUname)
		String spaceUname = GwtUtils.getToken(tokens,1);
		if("".equals(spaceUname)){
			spaceUname = getSpaceUname();
			Log.info("Set potential spaceUname: " + spaceUname);
		}
		
		String actionID = tokens[0].toUpperCase();
		//don't use action check startsWith() as pageTitle could be something link Defined TOKEN, such as $CPAGE etc. 
		//token string is safe as it is unescaped
		if(!(token.startsWith("/")?token.startsWith("/$"):token.startsWith("$")) 
				|| TOKEN_COMMENT.equals(actionID)){

			String pageTitle;
			String cid = null;
			if(TOKEN_COMMENT.equals(actionID)){
				//goto special comment of this page: /page#/$COMMNET/spaceUname/pageTitle/commnentUid
				//try to remove anchor from whole page title if it has
				pageTitle = GwtUtils.getToken(tokens, 2);
				cid = GwtUtils.getToken(tokens, 3);
			}else{
				//view page token: /page#/spaceUname/pageTtitle
				spaceUname = GwtUtils.getToken(tokens, 0);
				//try to remove anchor from whole page title if it has
				pageTitle = GwtUtils.getToken(tokens, 1);
			}
			//viewPanel async success method will call "showPanel()"
			PageControllerAsync action = ControllerFactory.getPageController();
			action.viewPage(spaceUname, pageTitle ,cid, viewPanel);
		}else if(TOKEN_CLINK.equals(actionID)){
			//get customised link action: /page#/$CL/spaceUname/cLinkToken
			String params = GwtUtils.getToken(tokens,2);
			
			PluginControllerAsync action = ControllerFactory.getPluginController();
			action.invokeLink(spaceUname,  params, viewPanel);
		}else if(TOKEN_CREATE_HOME.equals(actionID) || TOKEN_CREATE.equals(actionID)){
			//create page: /page#/$CREATE[$CHOME]/spaceUname/newPageTitle
			int newPageType;
			if(TOKEN_CREATE_HOME.equals(actionID)){
				newPageType = PageAttribute.NEW_HOMEPAGE;
			}else{
				newPageType = PageAttribute.NEW_PAGE;
			}
			String newTitle = GwtUtils.getToken(tokens, 2);
			
			//comment: don't directly jump to EditPanel although it save server loading
			//the reason does not switch to EditPanel directly even in create mode:
			//if user refresh page, for this token, it may lose navigation bar information.
			PageControllerAsync action = ControllerFactory.getPageController();
			this.setPreviewReady(false,null);
			action.createPage(spaceUname, newTitle, getPageUuid(),newPageType, editPanel);
		}else if(actionID.endsWith(TOKEN_EDIT)){
			//Edit URL: /page#/$EDIT/spaceUname/pageUuid (It is not pageTitle here!)
			//edit page, need loading content, show loading panel first,
			String uuid =  GwtUtils.getToken(tokens, 2);
			PageControllerAsync action = ControllerFactory.getPageController();
			//after loaded, setPreviewReady(true);
			this.setPreviewReady(false,null);
			action.editPage(spaceUname,uuid, editPanel);
		}else if(actionID.endsWith(TOKEN_EDIT_TEMPLATE)){
			//Edit template URL: /page#/$ET/spaceUname/templateUid
			int templID =  NumberUtil.toInt(GwtUtils.getToken(tokens, 2),-1);
			TemplateControllerAsync action = ControllerFactory.getTemplateController();
			if(templID == -1){
				//create
				action.createTemplate(spaceUname,editTemplatePanel);
			}else{
				//edit
				action.editTemplate(spaceUname,templID, editTemplatePanel);
			}
		}else if(actionID.endsWith(TOKEN_EDIT_SIDEBAR)){
			//Edit sidebar URL: /page#/$ES/spaceUname/sidebarUuid
			//edit page sidebar, need loading page content(at least pageUuid, in order to return), show loading panel first,
			String uuid =  GwtUtils.getToken(tokens, 2);
			PageControllerAsync action = ControllerFactory.getPageController();
			action.editPageSidebar(spaceUname,uuid, editSidebarPanel);
		}else if(actionID.endsWith(TOKEN_HISTORY)){
			//View history URL: /page#/$HISTORY/spaceUname/historyUid
			Integer historyUid =  Integer.valueOf(GwtUtils.getToken(tokens, 2));
			PageControllerAsync action = ControllerFactory.getPageController();
			//don't get attachment information
			action.getHistoryByUid(historyUid,false, viewPanel);
		}else if(actionID.endsWith(TOKEN_DIFF)){
			//View 2 page diff: /page#/$DIFF/historyUid1/historyUid2
			Integer historyUid1 =  Integer.valueOf(GwtUtils.getToken(tokens, 1));
			Integer historyUid2 =  Integer.valueOf(GwtUtils.getToken(tokens, 2));
			PageControllerAsync action = ControllerFactory.getPageController();
			action.diff(historyUid1, historyUid2, viewPanel.versionAsync);
		}else if(actionID.indexOf(TOKEN_DRAFT) != -1){
			//Edit draft URL: /page#/$DRAFT/spaceUname/draftType/draftUid
		    PageType draftType = PageType.fromOrdial(Integer.valueOf(GwtUtils.getToken(tokens, 2)));
			Integer draftPageUid = Integer.valueOf(GwtUtils.getToken(tokens, 3));
			PageControllerAsync action = ControllerFactory.getPageController();
			this.setPreviewReady(false,null);
			action.editDraft(draftPageUid,draftType,true,editPanel);
		}else if(actionID.indexOf(TOKEN_CPAGE) != -1){
			//customised pages URL: /page#/$CPAGE/cpageUID/parameters...
			String cid =  GwtUtils.getToken(tokens, 1);
			String[] params = null;  
			if(tokens.length > 2){
				params = new String[tokens.length - 2];
				for (int idx = 0; idx < tokens.length -2 ; idx++) {
					params[idx] = tokens[idx+2];
				}
			}
			
			PageControllerAsync action = ControllerFactory.getPageController();
			action.getCustomizedPage(cid, params, viewPanel);
		}

	}
	/**
	 * @param model
	 */
	protected void fillEditNavBar(final PageModel model) {
		//also reset navbar, it is not only in ViewPanel, also here, the reason is user may user browser refresh.
		String token = GwtClientUtils.getToken();
		if( model.ancenstorList == null){
			model.ancenstorList = new ArrayList<PageModel> ();
		}
		
		String[] tokens = GwtUtils.parseToken(token);
		String actionID = GwtUtils.getToken(tokens, 0).toUpperCase();
		//for edit navbar, the nav list get from server side
		String viewStr = null;
		if(TOKEN_EDIT.equals(actionID)){
			viewStr = Msg.consts.edit();
		}else if(TOKEN_EDIT_SIDEBAR.equals(actionID)){
			viewStr = Msg.consts.edit_sidebar();
		}else if(TOKEN_CREATE.equals(actionID)){
			viewStr = Msg.consts.add_page();
		}else if(TOKEN_DRAFT.equals(actionID)){
			viewStr = Msg.consts.edit_draft();
		}else if(TOKEN_CREATE_HOME.equals(actionID))
			viewStr = Msg.consts.create_homepage();
		
		if(viewStr != null){
			PageModel navM = new PageModel();
			navM.navToken = token;
			navM.title = viewStr;
			model.ancenstorList.add(navM);
		}
		resetNavbar(model);
	}
	

	//********************************************************************
	//               System input parameter
	//********************************************************************
	
	public void registerAttachmentPanel(AttachmentPanel panel){
		attachmentPanels.add(panel);
		panel.addAttachmentListener(this);
	}

	public void addOrUpdateItem(List<AttachmentModel> modelList) {
		//broadcast event to all attachment panel - for example, the attachment panel in editPanel and viewPanel
		for(AttachmentPanel panel: attachmentPanels){
			panel.addOrUpdateItem(modelList);
		}
	}

	public void removeItem(String nodeUuid) {
		//broadcast event to all attachment panel - for example, the attachment panel in editPanel and viewPanel
		for(AttachmentPanel panel: attachmentPanels){
			panel.removeItem(nodeUuid);
		}
	}
	@Override
	public void resetAttachmentPanel() {
		//broadcast event to all attachment panel - for example, the attachment panel in editPanel and viewPanel
		for(AttachmentPanel panel: attachmentPanels){
			panel.resetAttachmentPanel();
		}
	}
	/**
	 * Switch from Preview,Edit, View panel.
	 * @return false, then stop switch. This happens when user is in editing, but click some link, then confirm if exit editing, and user choose no.
	 */
	public boolean switchTo(int index){
		
		//source deck index
		int vIdx = deck.getVisibleWidget();
		
//		//if target are not loading, then hide loading anyway..
//		if(index != LOADING_PANEL){
//			//hide loading first, it will display again if request is LOADING
//			GwtClientUtils.hideLoading(true);
//		}
		
		Log.info("Visible deck index:" + vIdx +  ". To deck index:" + index);
		
		Widget widget = null;
		if(vIdx >=0){
			widget = deck.getWidget(deck.getVisibleWidget());
		}
		
		//firePanelSwitching
		if(panelSwitchListeners != null){
			for(PanelSwitchListener listener: panelSwitchListeners){
				if(!listener.onPanelSwitching(widget, index))
					return false;
			}
		}
		
		if(index == PREVIEW_PANEL){
			setFromPanelIndex(PageMain.PREVIEW_PANEL);

			if(!isPreviewReady()){
				//save draft first, need show loading page, 
				index = LOADING_PANEL;
				editPanel.saveDraft(PageSaveMethod.SAVE_DRAFT_LOAD_PREVIEW);
			}
		}else if(index == EDIT_PANEL){
			setFromPanelIndex(PageMain.PREVIEW_PANEL);
		}else if(index == EDIT_SIDEBAR_PANEL){
			setFromPanelIndex(PageMain.PREVIEW_PANEL);
		}else if(index == EDIT_TEMPLATE_PANEL){
			setFromPanelIndex(PageMain.PREVIEW_PANEL);
		}else if(index == LOADING_PANEL){
			GwtClientUtils.clearBeforeUnload();
			Window.setTitle(getSystemTitle()+ " - " + Msg.consts.loading());
//			//show loading
//			GwtClientUtils.hideLoading(false);
		}else if(index == VIEW_PANEL){
			GwtClientUtils.clearBeforeUnload();
			//EDIT_PANEL, PREVIEW_PANEL VIEW_PANEL: page title, 
//			here do nothing, it already set in ViewPanel
		}else{
			//dashboard
			GwtClientUtils.clearBeforeUnload();
			Window.setTitle(getSystemTitle());
		}
		
		deck.showWidget(index);
		if(panelSwitchListeners != null){
			for(PanelSwitchListener listener: panelSwitchListeners){
				listener.onPanelSwitched(widget, index);
			}
		}
		return true;
	}
	/**
	 * @return which panel is visible from Preview,Edit, View panel.
	 */
	public int getVisiblePanelIndex(){
		return deck.getVisibleWidget();
	}
	//just a easy access to loading page
	public boolean loading() {
		return switchTo(LOADING_PANEL);
	}

	/**
	 * Show error message on loading panel and hide indicator.
	 */
	public void errorOnLoadingPanel(String errorMsg) {
		loading();
		getGlobalMessage().error(errorMsg);
	}
	/**
	 * Show error message on current visible panel.
	 */
	public void errorOnVisiblePanel(String errorMsg) {
		int idx = deck.getVisibleWidget();
		if(idx == LOADING_PANEL){
			getGlobalMessage().error(errorMsg);
		}else{
			((MessagePanel)deck.getWidget(idx)).showError(errorMsg);
		}
	}
	/**
	 * TODO: so far only clean visible panel error msg, need clear all panel msg?
	 */
	public void cleanMessage() {
		int idx = deck.getVisibleWidget();
		if(idx == LOADING_PANEL){
			getGlobalMessage().cleanMessage();
		}else{
			((MessagePanel)deck.getWidget(idx)).message.cleanMessage();
		}
	}

	/**
	 * 
	 */
	public void resumeFromDiff() {
		int idx = deck.getVisibleWidget();
		if(idx == VIEW_PANEL){
			viewPanel.diffResume();
		}else if(idx == EDIT_PANEL){
			editPanel.diffResume();
		}else if(idx == PREVIEW_PANEL){
			previewPanel.diffResume();
		}
		//DASHBORARD_PANEL: never resume from it
	}
	/*
	 * Reset navbar according to given PageModel
	 */
	public void resetNavbar(PageModel model) {
		//refresh navbar
		List<NavItem> linkList = new ArrayList<NavItem>();
		linkList.add(new NavItem(Msg.consts.dashboard(),"",null));
		if(model != null && model.ancenstorList != null){
			//home page for this space: Page title is blank. Skip System Space
			NavItem link;
			if(!SharedConstants.SYSTEM_SPACEUNAME.equals(model.spaceUname)){
				//use spaceTitle as navbar title
				link = new NavItem(model.spaceTitle,GwtUtils.getSpacePageToken(model.spaceUname, null), Msg.consts.spacekey() + ":" + model.spaceUname);
				linkList.add(link);
			}
			for(Iterator<PageModel> iter = model.ancenstorList.iterator();iter.hasNext();){
				PageModel pmodel = iter.next();
				//only check if it is null! For home page not found, navToken may be "", it is still need link as first.
				if(pmodel.navToken != null){
					link = new NavItem(pmodel.title,pmodel.navToken,null);
				}else{
					link = new NavItem(pmodel.title,GwtUtils.getSpacePageToken(model.spaceUname, pmodel.title),null);
				}
				linkList.add(link);
			}
		}
		super.refreshNavbar(linkList);
	}

	/**
	 * view panel will call this method to refresh pageTree panel
	 */
	public void refreshTreeItem(String spaceUname, String pageUuid) {
		//currently, refresh tree only it is visible. Otherwise, when tree panel open, it will refresh from server side
		location.refresh(spaceUname, pageUuid);
	}
	public void selectTreeItem(String pageUuid) {
		location.setSelection(pageUuid);
	}

	/**
	 * Set attachment count value inside attachment button: e.g. : attachment(2)
	 * ugly code here: uncleared relation among main, view, edit, attachment, attachment button. Change later
	 */
	public void setAttachmentCount(int uploadedCount) {
		this.viewPanel.setAttachmentCount(uploadedCount);
		this.editPanel.setAttachmentCount(uploadedCount);
		
	}

	public void setPrintBtnVisible(boolean visible){
		printBtn.setVisible(visible);
		printSep.setVisible(visible);
	}
	/**
	 * @return
	 */
	public static UserModel getLoginUser() {
		return loginUser;
	}
	/**
	 * @return
	 */
	public static boolean isAnonymousLogin() {
		return GwtUtils.isAnonymous(loginUser);
	}

	/**
	 * Get page attachment list, Because both EditPanel and ViewPanel have AttachmentPanel, but they are synchronised by 
	 * AttachmentListener, so it does not matter to get attachment list by which panel.
	 * @return
	 */
	public List<AttachmentModel> getAttachmentList() {
		return viewPanel.attPanel.getUploadedItems();
	}

	/**
	 * @return
	 */
	public String[] getAttachmentNodeUuidList() {
		List<AttachmentModel> list = this.getAttachmentList();
		if(list == null)
			return new String[0];
		
		String[] nodes = new String[list.size()];
		int idx = 0;
		for (AttachmentModel node : list) {
			nodes[idx] = node.nodeUuid;
			idx++;
		}
		return nodes;
	}	
	//********************************************************************
	// these set/get only initialize in PageMain GWT module 
	// although these set/get could be static, but some other GWT module(instance, login) may 
	// use them by accidently, so I put them most as non-static
	//********************************************************************
	public static String getSpaceUname() {
		return sysSpaceUname;
	}
	public void setSpaceUname(String spaceUname) {
		sysSpaceUname = spaceUname;
		 //remove some offline_code here(0726)
		if(!StringUtil.equals(oldSpaceUname,sysSpaceUname) ){  //&& !AbstractEntryPoint.isOffline()
			//remove 
			cleanBeforeLogin();
//			if(!StringUtil.isBlank(spaceUname)){
//				//space change: maybe use event/listener model in future
//				SpaceControllerAsync spaceController = ControllerFactory.getSpaceController();
//				spaceController.getSpace(spaceUname, new SpaceChangeAsync());
//			}
			oldSpaceUname = sysSpaceUname;
		}
	}
	public static String getCurrentPageTitle() {
		return sysPageTitle;
	}
	/**
	 * Only set latest(current) page title!
	 * @param pageTitle
	 */
	public static void setCurrentPageTitle(String pageTitle) {
		sysPageTitle = pageTitle;
	}

	public int getPageVersion() {
		return this.sysPageVersion;
		
	}
	public void setPageVersion(int pageVer) {
		this.sysPageVersion = pageVer;
	}
	public static String getPageUuid() {
	    return sysPageUuid;
	}
	public static void setPageUuid(String pageUuid) {
		sysPageUuid = pageUuid;
	}
	public int getPageAttribute() {
		return this.sysPageAttribute;
	}

	public void setPageAttribute(int pageAttribute) {
		this.sysPageAttribute = pageAttribute;
	}
	public int getNewPageType() {
		return this.sysPageType;
	}
	
	public void setNewPageType(int newPageType) {
		this.sysPageType = newPageType;
	}
	public static String getParentPageUuid(){
		return sysParentPageUuid;
	}
	public static void setParentPageUuid(String uuid){
		sysParentPageUuid = uuid;
	}
	
	public boolean isPreviewReady() {
		return this.sysPreviewReady;
	}
	public void setPreviewReady(boolean previewReady, PageModel model) {
		if(previewReady){
			//because system only check Wiki TextArea, for title and tag text input, need refresh from edit box
			//The reason is why don't refresh model.title and model.tagString please refer to JDoc on fillPreview() method.
			previewPanel.fillPreview(editPanel.getEditTitle(), editPanel.getEditTagString(),model);
		}else 
			//clear all fields
			previewPanel.fillPreview("","",null);
		
		this.sysPreviewReady = previewReady;
		
	}
	//there are 2 possible panel to jump to DiffPanel: Preview or Edit (both can save, then may cause version conflict, then diff. 
	public void setFromPanelIndex(int panelIndex) {
		this.sysFromPanelIndex = panelIndex;
	}
	
	public int getFromPanelIndex() {
		return this.sysFromPanelIndex;
		
	}
	public void download(String url) {
		//Set download URL to iframe to allow a download happen.
		downloadFrm.setUrl(url);
	}
	public static String getShellUrl() {
		// hidden type
		Element titleDiv = DOM.getElementById("shellUrl");
		if(titleDiv != null){
			String title = DOM.getElementAttribute(titleDiv, "value");
			if(!StringUtil.isEmpty(title) && !"null".equals(title)){
				return title;
			}
		}
		
		return null;
	}
	 //remove some offline_code here(0726)
//	private class SpaceChangeAsync implements AsyncCallback<SpaceModel>{
//		public void onFailure(Throwable error) {
//			GwtClientUtils.processError(error);
//		}
//
//		public void onSuccess(SpaceModel model) {
//			if(model.permissions[ClientConstants.OFFLINE] == 1 && !model.isRemoved){
//				//display offline button on login part
//				addBeforeLogin(new HTML(" | "));
//				addBeforeLogin(new SyncButton(model.viewer,model.unixName,false));
//			}else{
//				//remove 
//				cleanBeforeLogin();
//			}
//		}
//	}



}
