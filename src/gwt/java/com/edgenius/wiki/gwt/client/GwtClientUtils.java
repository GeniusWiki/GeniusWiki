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

import java.util.Date;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.GeneralModel;
import com.edgenius.wiki.gwt.client.server.ClientAccessDeniedException;
import com.edgenius.wiki.gwt.client.server.ClientAuthenticationException;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * GwtUtils won't require any Class from GWT client side, like BaseEntryPoint etc. So here create
 * a pure client Util class which will compile with Gwt Client Code together.
 * @author Dapeng.Ni
 */
public class GwtClientUtils {
	static boolean obsoleteVerWin = false;
	/**
	 * Most AsyncCallback.onSuccess() method will use this method to handle success. Except:<br>
	 * EditPanel.SaveExitAsync class: need special handle for version conflict
	 * 
	 * @param model
	 * @return
	 */
	public static boolean preSuccessCheck(GeneralModel model, MessageWidget message) {
		//NOTE: current signupForm does not use this method, if add extra functions, please check it. 
		//try to check if session timeout
		if(BaseEntryPoint.I != null)
			//LoginMain is not extend from BaseEntryPoint, then it should be null.
			BaseEntryPoint.I.isSessionExpired(model.loginUsername);
		
		if(ErrorCode.hasError(model)){
			if(message != null)
				message.error(ErrorCode.getMessage(model.errorCode,model.errorMsg));
			return false;
		}
		
		return true;
		
	}
	/**
	 * @param obj error object
	 * @return true, online exception, false, system is current offline. 
	 */
	public static boolean processError(Throwable obj){
		if(obj instanceof IncompatibleRemoteServiceException){
			if(!obsoleteVerWin){
				//only popup this message once, until user refresh page, otherwise, each RPC call will popup on, Dashboard may contain large 
				//amount RCP call....
				Window.alert(((IncompatibleRemoteServiceException)obj).getMessage());
				obsoleteVerWin = true;
			}
			return true;
		}else if(obj instanceof ClientAuthenticationException){
			String loginUrl = ((ClientAuthenticationException)obj).getLoginUrl();
			if(AbstractEntryPoint.isOffline()){
				Window.alert("auth exp");
			}else{
				//append webcontext before loginURL
				gotoLogin(getRediectURL(loginUrl),getLocation());
			}
			//return withError flag
			return true;
		}else if(obj instanceof ClientAccessDeniedException){
			String errorUrl = ((ClientAccessDeniedException)obj).getErrorUrl();
			
			if(AbstractEntryPoint.isOffline()){
				Window.alert("Access denied.");
			}else{
				redirectByForm(errorUrl);
			}
			return true;
		}else if (obj instanceof InvocationException){
			//IE, Opera, Safari will throw InvocationException if server is not available (or offline model)
			
			return false;
		}else if (obj instanceof RuntimeException){
			//???Firefox will throw RuntimeException if server is not available (or offline model)
			
			return false;
		}
		
		Window.alert("Unexpected error " + obj);
		
		return true;
	}
	//JQuery dependence method - although slideUp(80) looks better, but in some case, loading bar can not be hidden. The sample is from 
	//local Safari from view page to view template, it is so quick, less than 80ms! This causes hidden method is executed before loading complete!
	public static native void hideLoading(boolean hide) /*-{
		if(hide){
			$wnd.$('#loading').hide();
		}else{
			$wnd.$('#loading').show();
		}
	}-*/;
	public static native void hidePagePin(boolean hide) /*-{
		if(hide){
			$wnd.$('#_page_pin').hide();
		}else{
			$wnd.$('#_page_pin').show();
		}
	}-*/;
	
		
	
	public static native void gotoLogin(String loginUrl,String currLocation) /*-{
		$wnd.callLogin(loginUrl,currLocation);
	}-*/;
	

	/**
	 * @return
	 */
	public static Image logo() {
		Image logo = new Image(getBaseUrl()+"download?instance=logo");
		logo.setStyleName(Css.LEFT);
		return logo;
	}

	/**
	 * @param url
	 */
	public static void reload(String url) {
		if(BaseEntryPoint.I != null){
			BaseEntryPoint.I.reload();
		}else{
			//some other URL, just send redirect, this may won't cause browser refresh page, if the given URL equals current URL.
			redirect(url);
		}
	}
	/**
	 * @param event
	 * @return
	 */
	public static String getFormResult(SubmitCompleteEvent event) {
		String results = event.getResults() == null ? "" : event.getResults().trim();
		Log.info("Form submit result:" + results);
		
		//Test case: Page access denied, login then redirect...
		//http://foo/page#/abc & 123 <kk> title
		//& need unescapeHTML; <> looks works in FF, but not in Safari - no 100% sure URL.decodeComponent() is required.
		//Anyway, this is may have bug for page title which has some special character combination. But happen rarely, so 
		//mark it as low
		results = URL.decodePathSegment(results);
		results = EscapeUtil.unescapeHTML(results);
		Log.info("Form submit result after decoded:" + results);
		
		
		//Some browser pre tag contain style info, so just check <pre rather than <pre>
		if (results.toLowerCase().startsWith("<pre")) {
			results = results.substring(results.indexOf(">")+1);
			if (results.toLowerCase().endsWith("</pre>"))
				results = results.substring(0, results.length() - 6);
		}
		
		//handle redir request
		if(results.startsWith(SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_ACCESS_DENIED_EXP)){
			gotoLogin(results.substring((SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_ACCESS_DENIED_EXP).length())
					,getLocation());
			return null;
		}
		if(results.startsWith(SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_AUTH_EXP)){
			redirectByForm(results.substring((SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_AUTH_EXP).length()));
			return null;
		}
		return results;
	}
	/**
	 * This method is for add page leaving confirmation message. Specially for editing content won't lose if page is redirect.
	 */
	public static native void onBeforeUnload(String msg)/*-{
		$wnd.message = msg;
		if($wnd.attachEvent) { 
			$wnd.attachEvent("onbeforeunload", $wnd.beforeUnload); 
		}else { 
			$wnd.onbeforeunload = $wnd.beforeUnload; 
		}
	}-*/;
	
	public static native void clearBeforeUnload()/*-{
		if($wnd.detachEvent) { 
			$wnd.detachEvent("onbeforeunload", $wnd.beforeUnload); 
		}else { 
			$wnd.onbeforeunload = null; 
		}
	}-*/;
	/**
	 * get base context url: http://localhost/wibok/page/ to
	 * http://localhost/wibok/ <BR>
	 * Limited on on level deep
	 * 
	 * @param moduleUrl
	 * @return
	 */
	public static String getBaseUrl() {
		String moduleUrl = GWT.getModuleBaseURL();
		return moduleUrl.substring(0, moduleUrl.substring(0, moduleUrl.length() - 1).lastIndexOf('/') + 1);
	
	}
	public static void redirect(String relativeUrl) {
		if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")){
			redirectUrl(relativeUrl);
		}else 
			redirectUrl(getBaseUrl() + relativeUrl);
	}
	public static void redirectByForm(String relativeUrl) {
		if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://"))
			redirectUrlByForm(relativeUrl);
		else{
			if(relativeUrl.length() > 0 && relativeUrl.charAt(0) == '/')
				relativeUrl = relativeUrl.substring(1);
			redirectUrlByForm(getBaseUrl() + relativeUrl);
		}
	}
	public static String getRediectURL(String relativeUrl) {
		if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://"))
			return relativeUrl;
		else{
			if(relativeUrl.length() > 0 && relativeUrl.charAt(0) == '/')
				relativeUrl = relativeUrl.substring(1);
			return getBaseUrl() + relativeUrl;
		}
	}
	public static native void redirectUrl(String url) /*-{
		$wnd.open (url,"_self",null);
	}-*/;
	/**
	 * Safari browser does not work by GwtUtils.redirect() if this method is called inside ajax return method.
	 * User form submit instead of "location.href=newUrl" to fix redirect issue.
	 * @param redirUrl
	 */
	public static native void redirectUrlByForm(String redirUrl) /*-{
		$wnd.redirForm(redirUrl);
	}-*/;
	/**
	 * @return
	 */
	public native static String getLocation() /*-{
	   		return window.top.location.href;
	}-*/;
	/**
	 * Use JQuery scrollTo plugin
	 * It has 2 scenarios. First, by element ID, that any elements can be anchor, 
	 * such as <div id=XX>.  Page Index macro just used this format.
	 * 
	 * Second, by anchor, <a name=xxx> format. 
	 * @param anchor
	 */
	public static native void gotoAnchor(String anchorID) /*-{
		  if($wnd.$('#'+anchorID).length > 0){
	      	$wnd.$.scrollTo('#'+anchorID,500,{margin:true});
	      }else if($wnd.$("a[name='"+anchorID+"']").length > 0){
	      	$wnd.$.scrollTo("a[name='"+anchorID+"']",500,{margin:true});
	      }
	
	}-*/;
	/**
	 * This method limited on, page only can has one form. Otherwise, the form
	 * which added last will be submit.
	 * 
	 * !!!!!!!!!!!!!! NOTE: this method must be refactor if you want to user it,
	 * because you must must removeEventPreview() after this EnterSubmit finish
	 * function. Otherwise, system scope short cut will lost function.
	 * !!!!!!!!!!!!!!
	 * 
	 * @param form
	 */
	public static void enableEnterSubmit(final FormPanel form) {
		// make user click enter in textbox and form automatically submit:
		// default action in HTML form, but GWT does not work
	
		Event.addNativePreviewHandler(new NativePreviewHandler(){
			public void onPreviewNativeEvent(NativePreviewEvent event) {
				if (!event.isCanceled() &&  event.getTypeInt() == Event.ONKEYDOWN) {
					int keyCode = event.getNativeEvent().getKeyCode();
					// Return clicked
					if (keyCode == KeyCodes.KEY_ENTER) {
						form.submit();
						event.cancel();
					}
				}
			}

		});
	}
	/**
	 * Please don't use this method when you can use
	 * <code>createUserPortrait</code> method. Because this method will force
	 * refresh image from server every visit.
	 * 
	 * @param username
	 * @return
	 */
	public static Widget createUserPortraitByUsername(String username) {
		String imgUrl;
		if (username == null || username.trim().length() == 0) {
			// return default user portrait
			imgUrl = getBaseUrl() + "static/images/noportrait.jpg";
		} else {
			imgUrl = getBaseUrl() + "download?user=" + URL.encodeQueryString(username) + "&refresh=" + System.currentTimeMillis();
		}
	
		return new HTML("<img src=\"" + imgUrl + "\"" + " title=\"portrait\">");
	}
	public static HTML buildDownloadURLWidget(String spaceUname, String filename, String nodeUuid, String version) {
		String url = buildAttachmentURL(spaceUname, filename, nodeUuid,  version,true);
		//Must use double quote for URL, single quote may appear in spaceUname even aftere encoding...
		return new HTML("<a href=\"" + url + "\" title='"+Msg.consts.download_attachment()+"'>" + filename + "</a>");
	}
	/**
	 * This method only can be use on client gwt program as it use GwtUtils.getBaseUrl() 
	 * @param spaceUname
	 * @param nodeUuid
	 * @param version
	 * @return
	 */
	public static String buildAttachmentURL(String spaceUname,  String filename, String nodeUuid, String version,boolean download) {
		String verParm = version != null ? ("&version=" + version):"";
		
		return new StringBuffer(getBaseUrl()).append("download?space=").append(URL.encodeQueryString(spaceUname))
			.append("&uuid=").append(nodeUuid)
			.append("&file=").append(URL.encodeQueryString(filename))
			.append(verParm)
			.append("&download=").append(download).toString();	
		
	}
	/**
	 * If simply using History.newItem(token), the page won't refresh if current
	 * token equals given token. This method will refresh page (Ajax style
	 * refresh) whatever current token is.
	 * 
	 * For sample scenario, delete page, token won't change, but system need go server side
	 * to get "page not found" message by refresh same token(page title).
	 * @param token:
	 *            could not be null!
	 */
	public static void refreshToken(String token) {
		History.newItem(token, true);
		String currToken = History.getToken();
		if (currToken != null && currToken.trim().equalsIgnoreCase(token.trim())) {
			// Only use History.newItem(), it won't refresh page if it has same token,
			// Test scenarios:  delete home page when token is spaceToken(no page title).
			// After home page delete it will try to redirect to spaceToken, so it is same token, but need refresh page.
			History.fireCurrentHistoryState();
		}
	
	}

	
	public static native boolean isIE()/*-{
		return navigator.appName.indexOf("Microsoft") !=-1;
	}-*/;
	public static native boolean isIE6()/*-{
		if(navigator.appName.indexOf("Microsoft") !=-1){
			 var ua = navigator.userAgent;
      		 var re  = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
      		if (re.exec(ua) != null)
         		if(parseFloat( RegExp.$1 ) == 6)
         			return true;
			}
		
		return false;
	}-*/;
	public static boolean isBlack(String color) {
		if(color == null)
			return false;
		if(color.startsWith("#"))
			color = color.substring(1);
		
		char[] c = color.toUpperCase().toCharArray();
		for (char d : c) {
			if(d != '0')
				return false;
		}
		return true;
	}
	
	public static boolean isWhite(String color) {
		if(color == null)
			return false;
		if(color.startsWith("#"))
			color = color.substring(1);
		
		char[] c = color.toUpperCase().toCharArray();
		for (char d : c) {
			if(d != 'F')
				return false;
		}
		return true;
	}
	/**
	 * This method should keep consistent with server side DateUtil.toDisplayDate() method
	 * Format long(saved in String format) to display date
	 */
	@SuppressWarnings("deprecation")
	public static String toDisplayDate(long time) {
		Date date = new Date(time);
		int year = date.getYear();
		int month = date.getMonth()+1;
		int day = date.getDate();
		int min = date.getMinutes();
			
		Date now = new Date();
		int nyear = now.getYear();
		int nmonth = now.getMonth()+1;
		int nday = now.getDate();
	
		StringBuffer sb = new StringBuffer();
		sb.append(date.getHours()).append(":").append((min < 10)?("0"+min):min).append(" ");
		if(day != nday || month != nmonth || year != nyear){
			sb.append(GwtClientUtils.getMonthText(month));
			sb.append(" ").append(day);
			if(year != nyear){
				year =year+1900;
				sb.append(" ").append(year);
			}
		}else{
			sb.append(" Today");
		}
		return  sb.toString();
		
	}
	/**
	 * @param month
	 * @return
	 */
	private static String getMonthText(int month) {
		if(month == 1){
			return Msg.consts.jan();
		}else if(month == 2){
			return Msg.consts.feb();
		}else if(month == 3){
			return Msg.consts.mar();
		}else if(month == 4){
			return Msg.consts.apr();
		}else if(month == 5){
			return Msg.consts.may();
		}else if(month == 6){
			return Msg.consts.jun();
		}else if(month == 7){
			return Msg.consts.jul();
		}else if(month == 8){
			return Msg.consts.aug();
		}else if(month == 9){
			return Msg.consts.sep();
		}else if(month == 10){
			return Msg.consts.oct();
		}else if(month == 11){
			return Msg.consts.nov();
		}
		return Msg.consts.dec();
	}
	/**
	 * @return
	 */
	public static String getToken() {
		//page#/xxx - xxx must do URLEncode otherwise it will broken URL even its value in anchor part
		//I found FF looks OK if there is no this decode, but Chrome does work. Anyway, it is safe to decode here and no side-effect.

		return URL.decodeQueryString(History.getToken());
		
	}
	/**
	 * @param form
	 * @return
	 */
	public static KeyPressHandler createEnterSubmitListener(final FormPanel form) {
		return new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				//ClientConstants.KEY_ENTER
				if (event.getCharCode() == 13) {
					form.submit();
				}
	
			}
		};
	}

	public static Widget createUserPortrait(String portraitUrl) {
		return new HTML(GwtUtils.getUserPortraitHTML(portraitUrl, null, -1));
	}
	public static Widget createUserSmallPortrait(String portraitUrl,String fullname, int size) {
		return new HTML(GwtUtils.getUserPortraitHTML(portraitUrl, fullname, size));
	}
	public static Widget createUserSmallPortrait(String portraitUrl, int size) {
		return new HTML(GwtUtils.getUserPortraitHTML(portraitUrl, null, size));
	}
	/**
	 * @param largeLogoUrl
	 * @return
	 */
	public static Widget createSpaceLogo(String imgUrl) {
		return new HTML("<img src=\"" + imgUrl + "\"" + " title=\"logo\">");
	}
}
