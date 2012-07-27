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
package com.edgenius.wiki.gwt.client;

import java.util.Iterator;
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.login.LoginDialog;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.page.PinPanel;
import com.edgenius.wiki.gwt.client.page.PinPanelListener;
import com.edgenius.wiki.gwt.client.page.widgets.HelpButton;
import com.edgenius.wiki.gwt.client.server.HelperControllerAsync;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.user.ProfileDialog;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.ContextMenu;
import com.edgenius.wiki.gwt.client.widgets.ContextMenuItem;
import com.edgenius.wiki.gwt.client.widgets.HelpDialog;
import com.edgenius.wiki.gwt.client.widgets.HintTextBox;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.NavItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
/**
 * 
 * @author Dapeng.Ni
 */
public abstract class BaseEntryPoint extends AbstractEntryPoint implements NativePreviewHandler{
	

	//These are <DIV> in page.jsp
	protected final static String CONTENT_PANEL = "content";
	protected static final String LEFT_SIDEBAR_PANEL = "leftmenu";
	protected static final String LEFT_SIDEBAR_BUTTON = "leftsidebarbtn";
	protected static final String RIGHT_SIDEBAR_PANEL = "rightmenu";
	protected static final String RIGHT_SIDEBAR_BUTTON = "rightsidebarbtn";
	protected static final String GLOBAL_MESSAGE = "globalMessage";
	
	public static BaseEntryPoint I;
	
	private HTMLPanel navMainPanel;
	private String navID0 = HTMLPanel.createUniqueId();
	private String navID1 = HTMLPanel.createUniqueId();
	
	private HintTextBox searchBox = new HintTextBox(Msg.consts.search());
	
	private static final int NAV_LIMIT_LENGTH = 80;
	//the minimum size of navbar from first to breadcrumbs ...
	private static final int NAV_LEAVE_TO_FIRST = 25;
	//how to shrink nav breadcrumbs? example, a>b>c>d from right: a>b>,c>d, from left: a>,b>c>d 
	//Issue #45: try to keep from right (from last), ie, NAV_SHRINK_FROM_RIGHT=false
	private static final boolean NAV_SHRINK_FROM_RIGHT = false;
	
	private FlowPanel leftDeck = new FlowPanel();
	private FlowPanel rightDeck = new FlowPanel();
	
	private FlowPanel navbarPanel = new FlowPanel();

	private FlowPanel loginPanel = new FlowPanel();
	private HorizontalPanel headLoginPanel = new HorizontalPanel();
	private HorizontalPanel tailLoginPanel = new HorizontalPanel();

	private ClickLink profile = new ClickLink();
	private MessageWidget globalMessage = new MessageWidget(); 

	public void onModuleLoad() {
		Log.setUncaughtExceptionHandler();
		GWT.setUncaughtExceptionHandler(new BaseUncaughtExceptionHandler());
		I  = this;
		Event.addNativePreviewHandler(this);
		
		navMainPanel = new HTMLPanel("<span id='" + navID0 + "'></span><span id='" + navID1 + "'></span>");
		navbarPanel.setStyleName(Css.NAVBAR);
		loginPanel.setStyleName(Css.LOGINBAR);

		RootPanel.get(GLOBAL_MESSAGE).add(globalMessage);
		//must before sidebar method as it will call gwt method.
		
		bindJsMethod();
		// don't disturb order of following methods:
		
		initLoginPanel();
		
		initContentPanel();
		reload();
		
		initNavbar();
		initSearch();
		initSideMenu();
		
		initialisedCallback();
		
		
		GwtClientUtils.hideLoading(true);
		
	}
	
	private native void bindJsMethod()/*-{

		$wnd.gwtNotifyPinPanelStatus= function (tabPanelID,visible) {
	          @com.edgenius.wiki.gwt.client.BaseEntryPoint::notifyPinPanelStatus(IZ)(tabPanelID,visible);
	   };
	}-*/;
	
	
	/*
	 * Add some widget before "Dashboard>", So far only use for location button.
	 */
	public void addBeforeNav(Widget widget){
		widget.setStyleName(Css.LEFT);
		navMainPanel.add(widget, navID0);
	}
	/*
	 * Add some widgets after login information, so far only use for page print button and help button
	 */
	public HTML addAfterLogin(Widget printBtn) {
		HTML sep = new HTML("|");
		tailLoginPanel.add(sep);
		tailLoginPanel.add(printBtn);
		return sep;
	}
	//so far, add offline button for space 
	public void addBeforeLogin(Widget offlineBtn) {
		headLoginPanel.insert(offlineBtn,0);
	}
	public void cleanBeforeLogin() {
		headLoginPanel.clear();
		
		headLoginPanel.setSpacing(2);
		headLoginPanel.add(new HelpButton(true));
		headLoginPanel.add(new HTML("|"));
		headLoginPanel.add(loginPanel);
		headLoginPanel.add(tailLoginPanel);

	}
	
	/*
	 * Reset user login name: it happens when user change its name in profile dialogue  
	 */
	public void resetLoginUser(String name) {
		profile.setText(name);
	}
	public boolean isSessionExpired(String currUsername){
		String username = (String) profile.getObject();
		if(StringUtil.equals(username, currUsername))
			//both null, maybe
			return false;
		
		if(username != null && !username.equalsIgnoreCase(currUsername)){
			//user changed
			profile.setObject(currUsername);
			showLoginPanel();
			//show a warning to user
			HorizontalPanel msg = new HorizontalPanel();
			HTML label = new HTML(Msg.consts.session_expired() + "&nbsp; ");
			ClickLink login = new ClickLink(Msg.consts.login());
			login.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					LoginDialog dialogue = new LoginDialog(LoginDialog.LOGIN);
					dialogue.showbox();
				}
			});
			msg.add(label);
			msg.add(login);
			globalMessage.error(msg);
			//session expired, logout, broadcast
			login(null);
			return true;
		}
		
		return false;
	}

	public void setSideMenuPanel(final int left, PinPanel panel){
		if(left == ClientConstants.LEFT){
			leftDeck.clear();
			leftDeck.add(panel);
		}else{
			rightDeck.clear();
			rightDeck.add(panel);
		}
		panel.addPinPanelListener(new PinPanelListener(){
			public void close() {
				setSidebarVisible(left, false);
			}
			public void pin(boolean on) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	public void setSidebarButtonVisible(int left, boolean visible){
		showSidebarButtonNative(left,visible);
	}
	public void setSidebarVisible(int left, boolean visible){
		showSidebarNative(left,visible);
	}
	/**
	 * if new state is visible, then return true, otherwise, false.
	 */
	public boolean toggleSideMenu(int left){
		return toggleSideMenuNative(left);
	}

	private static void notifyPinPanelStatus(int tabPanelID, boolean visible){
		//so far, only remember right side bar status
		if(tabPanelID == SharedConstants.TAB_TYPE_RIGHT_SIDEBAR){
			HelperControllerAsync helperService = ControllerFactory.getHelperController();
			helperService.notifyPinPanelStatus(SharedConstants.TAB_TYPE_RIGHT_SIDEBAR, visible, new PinPanelAsync());
		}
	}
	/**
	 * if new state is visible, then return true, otherwise, false.
	 */
	private static native boolean toggleSideMenuNative(int left)/*-{
		return $wnd.toggleSidebar(left);
	}-*/;
	private static native void showSidebarButtonNative(int left,boolean visible)/*-{
		$wnd.showSidebarButton(left,visible);
	}-*/;
	private static native void showSidebarNative(int left,boolean visible)/*-{
		$wnd.showSidebar(left,visible);
	}-*/;
	
	
	public abstract void initContentPanel();

	public abstract void login(UserModel user);
	
	/**
	 * Reload entire page by ajax call, it is not browser refresh page.
	 * It need do checkLogin() first, so that system can get timezone before other call.
	 */
	public abstract void reload();
	

	public void onPreviewNativeEvent(NativePreviewEvent event){
		int type = event.getTypeInt();
		
		//IE only work for Event.ONKEYDOWN but not Event.ONKEYPRESS (FF is OK)
		if (!event.isCanceled() &&  type == Event.ONKEYDOWN) {
			NativeEvent evt = event.getNativeEvent();
			int keyCode = evt.getKeyCode();
			
			boolean ret =  bindGloablKeyShortcut(evt.getCtrlKey(), evt.getAltKey(),evt.getShiftKey(),evt.getMetaKey(), keyCode);

			if(!ret){
				event.cancel();
			}
		}

	}

	/**
	 * @param keyCode
	 * @param modifier
	 */
	public boolean bindGloablKeyShortcut(boolean ctrlKey, boolean altKey,
		      boolean shiftKey, boolean metaKey, int keyCode) {
		if(keyCode == ClientConstants.KEY_F1){
			HelpDialog help = new HelpDialog();
			help.showbox();
			//in FF, it block the FF help window.
			return false;
		}
		
		//KeyCaptureListener.globalCapture means some textbox is getting input focus so far, so all shortcut won't work
		if(KeyCaptureListener.globalCapture && KeyMap.isKey(KeyMap.GOTO_DASHBOARD, ctrlKey, altKey, shiftKey, metaKey, keyCode)){
			//Ctrl-Alt-d: go back dashboard
			if(this instanceof PageMain){
				History.newItem("");
			}else{
				//from system admin page
				GwtClientUtils.redirect(SharedConstants.URL_HOME);
			}
			return false;
		}
		
		//return dependent on this method, so that let sub class decide if continue broadcasting this event.
		return executeKeyShortcut(ctrlKey, altKey, shiftKey, metaKey, keyCode);
	}
	//********************************************************************
	//               Protected methods
	//********************************************************************

	protected void checkLogin(){
		SecurityControllerAsync securityService = ControllerFactory.getSecurityController();
		securityService.checkLogin(getJsInfoModel(), new LoginAsync());

		
	}


	/**
	 * This method will be override by subclass if it want to have owned shortcut key in Web Page Body scope.
	 * @param keyCode
	 * @param modifier
	 * @return false, this will stop other key event handle, includes system level handle, such F1, Ctrl-A etc.
	 */
	protected boolean executeKeyShortcut(boolean ctrlKey, boolean altKey,
		      boolean shiftKey, boolean metaKey, int keyCode){
		return true;
	}

	/**
	 * @param list The NavItem list
	 */
	protected void refreshNavbar(List<NavItem> linkList) {
		
		navbarPanel.clear();
		if(linkList != null && linkList.size() > 0){
			//shorten a>b>c>d to a>b>...>d
			shrinkNavbar(linkList);
			
			//OK, length is short, then list all nav items normally.
			int size = linkList.size();
			for (int idx=0; idx < size; idx++) {
				//this link may contain navMenu(ClickLink)
				Widget link = (Widget) linkList.get(idx);
				
				navbarPanel.add(link);
				
				//append >> sign
				if(idx != (size -1))
					navbarPanel.add(new HTML(SharedConstants.NEXT_LINK));
			}
		}
	}


	// ********************************************************************
	// Private methods
	// ********************************************************************
	/**
	 * @param linkList
	 * @return
	 */
	protected static void shrinkNavbar(List<NavItem> linkList) {
		int size = linkList.size();
		//check if this nav bar is too long, if so, it need shrink and add ... to popup nav menu
		StringBuffer sb = new StringBuffer();
		for (int idx=0; idx < size; idx++) {
			//summary all length
			NavItem link = linkList.get(idx);
			sb.append(link.getText());
		}
		
		//try to keep last item (current item must be full size display), then try to keep first to last second, 
		//if overflow, then create popup menu for nav
		if(sb.length() > NAV_LIMIT_LENGTH){
			//build navMenu
			NavItem navMenu = buildNavMenu(linkList);

			//try to fill left from first until before last
			sb = new StringBuffer();
			boolean enough = false;
			
			if(NAV_SHRINK_FROM_RIGHT){
				NavItem lastNav = linkList.get(size-1);
				String last = lastNav.getText();
				
				int leave = NAV_LIMIT_LENGTH - last.length();
				//if the last one is too long, it also need shrink
				if(last.length() > NAV_LIMIT_LENGTH){
					//whatever, you cannot keep entire last item, then shrink last to some size
					int beginIndex = last.length() - NAV_LIMIT_LENGTH + NAV_LEAVE_TO_FIRST;
					if(beginIndex < last.length())
						lastNav.setText("..." + last.substring(beginIndex));
					leave = NAV_LEAVE_TO_FIRST;
				}
				
				for (Iterator<NavItem> iter = linkList.iterator();iter.hasNext();) {
					if(enough){
						//length is enough, then remove following item until last 
						iter.next();
						iter.remove();
						continue;
					}
					NavItem link = iter.next();
					sb.append(link.getText());
					if(sb.length() > leave){
						iter.remove();
						enough = true;
					}
				}
				
				if(enough){
					//last one is removed in enough, then append it again.
					linkList.add(lastNav);
					//size already change, need reset
					size = linkList.size();
					linkList.add(size - 1, navMenu);
				}
			}else{
				//NAV_SHRINK_FROM_LEFT - now it is default
				NavItem lastNav = linkList.get(size-1);
				String last = lastNav.getText();
				int spaceLen = 0;
				if(linkList.size() > 2){
					//it at least has dashboard - space - first title, then try to sum up dashboard and space length
					spaceLen = linkList.get(0).getText().length() + linkList.get(1).getText().length();
				}
				int leave = NAV_LIMIT_LENGTH - last.length();
				//if the last one is too long, it also need shrink
				if((last.length() + spaceLen) > NAV_LIMIT_LENGTH){
					//now, if only show "dashboard-space-...- last title" is already over size, then  put it as enough
					//and also check page title if it is over size, then shrink it.
					if(last.length() > NAV_LIMIT_LENGTH){
						int beginIndex = last.length() - NAV_LIMIT_LENGTH + NAV_LEAVE_TO_FIRST;
						if(beginIndex < last.length())
							lastNav.setText("..." + last.substring(beginIndex));
					}
					leave = 0;
					enough = true;
				}
				//now, the last nav item already caculate to leave size, then put nav bar first(Dashboard) and second(Space Name)
				//then caculate how many left side items could appends
				int start = 1; //set to 1 - if enough is true, then at least spaceTitle is displayed
				int end = linkList.size() - 2;
				
				if(!enough){
					for (start=0;start < linkList.size() && start<2;start++) {
						//first(Dashboard) and second(Space Name)
						sb.append(linkList.get(start).getText());
					}
					//start now is 2,
					start = 1;
					if(sb.length() > leave){
						enough = true;
					}
					if(!enough){
						//end is second last - except the must displayed last one - sucks comment:(
						for (;end > start;end--) {
							//first(Dashboard) and second(Space Name)
							sb.append(linkList.get(end).getText());
							if(sb.length() > leave){
								enough = true;
								break;
							}
						}
					}				
				}
				//now, we get start(equals 1) and end(the last index which is over size), then will need remove
				//all elements between them(exclude start index, but include end index) and append NavMenu after start index
				//if only has 2 items, and 2nd is very long, start may less or equal end, 
				//then it not need remove nav items and insert navMenu
				if(start < end){
					int idx = 0;
					for (Iterator<NavItem> iter = linkList.iterator();iter.hasNext();idx++) {
						iter.next();
						if (idx>start){
							iter.remove();
						}
						
						if(idx >= end)
							break;
					}
					//now insert navMenu
					linkList.add(start+1, navMenu);
				}
				
			}
		}
	}
	/**
	 * @param linkList
	 * @return
	 */
	private static NavItem buildNavMenu(List<NavItem> linkList) {
		ClickLink navMenu = new ClickLink("...");
		ContextMenu menuBar = new ContextMenu(navMenu);
		for(Iterator<NavItem> iter = linkList.iterator();iter.hasNext();){
			final NavItem nav = iter.next();
			ContextMenuItem item = new ContextMenuItem(nav.getText(), new Command(){
				public void execute() {
					nav.invoke();
				}
			});
			menuBar.addItem(item);
		}
		return new NavItem(navMenu);
	}

	/**
	 * Left side menu
	 */
	private void initSideMenu() {
		if(RootPanel.get(LEFT_SIDEBAR_PANEL) != null){
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// left side
			leftDeck.setVisible(false);
		    leftDeck.setStyleName(Css.LEFT_SIDEBAR);
		    RootPanel.get(LEFT_SIDEBAR_PANEL).add(leftDeck);
		}
		if(RootPanel.get(RIGHT_SIDEBAR_PANEL) != null){
		    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// Right side
		    rightDeck.setVisible(false);
		    rightDeck.setStyleName(Css.RIGHT_SIDEBAR);
		    RootPanel.get(RIGHT_SIDEBAR_PANEL).add(rightDeck);		
		}
		
	}
	private void initSearch() {
		if(isOffline()){
			return;
		}
		
		String id1 = HTMLPanel.createUniqueId();
		HTMLPanel panel = new HTMLPanel("<span id='" + id1 + "'></span>");
		searchBox.addFocusHandler(KeyCaptureListener.instance());
		searchBox.addBlurHandler(KeyCaptureListener.instance());
		searchBox.setStyleName(Css.SEARCH_INPUT);
		searchBox.addKeyDownHandler(new KeyDownHandler() {
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					doSearch(searchBox.getText());
				}
			}

		});

		panel.add(searchBox, id1);

		RootPanel.get("search").add(panel);

	}


	private void initNavbar() {

		navMainPanel.add(navbarPanel, navID1);
		RootPanel.get("navbar").add(navMainPanel);
	}
	private void initLoginPanel() {
		// set initial status: assume user not login
		login(null);
		
		cleanBeforeLogin();
		
		RootPanel.get("login").add(headLoginPanel);
	}

	private void doSearch(String keywords) {
		if (keywords == null || keywords.trim().length() == 0)
			return;

		String token = GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_SEARCH_RESULT), keywords);
		if(this instanceof PageMain){
			GwtClientUtils.refreshToken(token);
		}else{
			GwtClientUtils.redirect(SharedConstants.URL_PAGE+"#"+token);
		}

	}
	
	
	private void showLoginPanel(){
		loginPanel.clear();
		
		if(!isOffline() && AbstractEntryPoint.isAllowPublicSignup()){
			ClickLink signup = new ClickLink(Msg.consts.signup());
			signup.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					LoginDialog dialogue = new LoginDialog(LoginDialog.SINGUP);
					dialogue.showbox();
				}
			});
			loginPanel.add(signup);
			loginPanel.add(new HTML("&nbsp;|&nbsp;"));
		}
		
		ClickLink login = new ClickLink(Msg.consts.login());
		login.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				LoginDialog dialogue = new LoginDialog(LoginDialog.LOGIN);
				dialogue.showbox();
			}
		});
		loginPanel.add(login);
		
		profile.setObject(null);
		
	}


	//********************************************************************
	//               Private class
	//********************************************************************

	public static class PinPanelAsync implements AsyncCallback<Integer> {

		public void onFailure(Throwable error) {
		}

		public void onSuccess(Integer status) {
		}
		
	}
	public class LoginAsync implements AsyncCallback<UserModel> {

		public void onFailure(Throwable arg0) {
			showLoginPanel();
			// empty
			login(null);
		}


		public void onSuccess(final UserModel user) {

			if(!GwtClientUtils.preSuccessCheck(user,null)){
				return;
			}
			loginPanel.clear();
			
			//put it to static, so that if session logout, or other case, the signup button is not show up
			AbstractEntryPoint.setSuppress(user.getSuppress());
			
			 //remove some offline_code here(0726)
//			if(!isOffline() && OfflineUtil.isReadyForUser(user.getUid())){
//				Timer syncTimer = new Timer(){
//					public void run() {
//						//Check current login user's offline status, do download sync if need download.
//						Sync sync = new Sync();
//						sync.sync(user, null);
//					}
//				};
//				
//				//run immediately, then schedule run in period
//				syncTimer.run();
//				if(user.getDelaySyncMin() > 0 ){
//					syncTimer.scheduleRepeating(user.getDelaySyncMin()*60000);
//				}else{
//					//default 30 minutes
//					syncTimer.scheduleRepeating(30*60000);
//				}
//			}
			
			if (GwtUtils.isAnonymous(user)) {
				// anonymous user
				showLoginPanel();
			} else {
				profile.setText(user.getFullname());
				profile.setObject(user.getLoginname());
				profile.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						ProfileDialog userPanel = new ProfileDialog();
						userPanel.showbox();
						// GwtUtils.redirect("user/admin.do");
					}
				});
				ClickLink logout = new ClickLink(Msg.consts.logout());
				logout.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						profile.setObject(null);
						login(null);
						 //remove some offline_code here(0726)
//						if(isOffline()){
//							OfflineLoginService.logout();
//							//redir to Dashboard home.
//							GwtClientUtils.redirect("");
//						}else{
							//put this after all finish, otherwise Safari will throw null point exception in JS.
							GwtClientUtils.redirect("j_spring_security_logout");
//						}
					}
				});
				 //remove some offline_code here(0726)
//				if(isOffline()){
//					loginPanel.add(new Label(user.getFullname()));
//				}else{
					loginPanel.add(profile);
//				}
				if(isAllowLogout()){
					loginPanel.add(new Label(" | "));
					loginPanel.add(logout);
				}
			}
			// fire user login event
			login(user);
		}
	}
	
	private static class BaseUncaughtExceptionHandler implements UncaughtExceptionHandler {

		public void onUncaughtException(Throwable ex) {
			//Comment this warning - normally it only bother user and doesn't make any sense.
			if(Log.isLoggingEnabled()){
				Window.alert(Msg.params.system_busy(SharedConstants.APP_NAME) + "\n");
			}
			Log.error(ex == null ? "No deail" : printStackTrace(ex));
		}
		
		private String printStackTrace(Throwable ex) {
			  String output = ex.toString() + "\n";
			  Object[] stackTrace = ex.getStackTrace();
			  for (Object line : stackTrace) {
			    output += line + "\n";
			  }
			  return output;
		}
	}

	public MessageWidget getGlobalMessage() {
		return globalMessage;
	}

	public static String getSystemTitle() {

		// hidden type
		Element titleDiv = DOM.getElementById("systemTitle");
		if(titleDiv != null){
			String title = DOM.getElementAttribute(titleDiv, "value");
			return title;
		}
		
		return SharedConstants.APP_NAME;
	}
	
	private native void initialisedCallback() /*-{
	    $wnd.documentReadyCallback();
	}-*/;
}
