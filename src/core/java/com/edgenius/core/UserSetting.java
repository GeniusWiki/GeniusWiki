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
package com.edgenius.core;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * TODO: bind with wiki package!!!
 * @author dapeng
 *
 */
@SuppressWarnings("serial")
public class UserSetting implements Serializable{
	
	public static final String PROP_LINKED="linked"; //indicate if GW activities or messages will post to twitter
	public static final String PROP_ACCOUNT="account";
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//  Static variables
	public static final String CONTACT_TWITTER_NAME="Twitter"; 
	//twitter properties
	public static final String CONTACT_TWITTER_PROP_ACCESS_TOKEN="token"; 
	public static final String CONTACT_TWITTER_PROP_ACCESS_SECRET="secret"; 
	
	public static final String CONTACT_FACEBOOK_NAME="Facebook";
	
	public static final String CONTACT_LINKEDIN_NAME="LinkedIn";
	
	public static final String CONTACT_IM_NAME = "IM";
	//please note: below information save into User table directly for historic design reason.
	public static final String CONTACT_PHONE_NAME = "Phone";  //TODO: need i18n?
	public static final String CONTACT_WEBSITE_NAME = "Website"; //TODO: need i18n?
	//MUST NOT CHNAGE - IT IS HARDCODE IN 
	public static final String CONTACT_EMAIL_NAME = "Email";//TODO: need i18n?
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Fields
	private String direction = null;
	private String localeLanguage = null;
	private String localeCountry = null;
	private String timeZone = null;
	private String theme = "defaultwiki";
	private String skin = null;
	//User could override default {portal:colunm=4} value. If it is zero, then system wide value will be used.
	private int portalColumn;
	//string concatenation by SharedConstant.PORTLET_SEP
	private List<String> homeLayout = null;
	private boolean usingRichEditor = true;
	//BAD: relative to wiki
	//in my design, some bottom panel can be pin: comments , history , summary 
	//e.g., if user click "pin" button on comments panel, then all pages while this user open, its comments panel always appear on.
	//!!! But now, only rightSidebar use this field! 
	//system design, -1 is initial status. For -1 value, right sidebar is turn on
	private int fixedPanel = -1;
	
	private String status;
	private List<QuickNote> quickNotes;
	
	//Key:name,i.e., Twitter,LinkedIn, Facebook, Position etc. 
	//Value: Map - Key: property name, Value: property value.
	// ----------------------------------------
	// Twitter: 4 properties: twitter account, twitter linked flag(auto post to twitter), twitterAccessToken, twitterAccessTokenSecret.
	// if Global.twitterOauthConsumerKey is blank, the last 3 value will be ignored.
	// ----------------------------------------
	// Facebook: only account name -- ???TODO
	private LinkedHashMap<String, LinkedHashMap<String,String>> contacts;
	
	public static class QuickNote implements Serializable{
		private int version;
		private String note;
		private Date createDate;
		
		public int getVersion() {
			return version;
		}
		public void setVersion(int version) {
			this.version = version;
		}
		public String getNote() {
			return note;
		}
		public void setNote(String note) {
			this.note = note;
		}
		public Date getCreateDate() {
			return createDate;
		}
		public void setCreateDate(Date createDate) {
			this.createDate = createDate;
		}
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTheme() {
		return theme;
	}
	public void setTheme(String theme) {
		this.theme = theme;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getLocaleCountry() {
		return localeCountry;
	}
	public void setLocaleCountry(String localeCountry) {
		this.localeCountry = localeCountry;
	}
	public String getLocaleLanguage() {
		return localeLanguage;
	}
	public void setLocaleLanguage(String localeLanguage) {
		this.localeLanguage = localeLanguage;
	}
	public String getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	public int getFixedPanel() {
		return fixedPanel;
	}
	public void setFixedPanel(int fixedPanel) {
		this.fixedPanel = fixedPanel;
	}
	public List<String> getHomeLayout() {
		return homeLayout;
	}
	public void setHomeLayout(List<String> homeLayout) {
		this.homeLayout = homeLayout;
	}
	public boolean isUsingRichEditor() {
		return usingRichEditor;
	}
	public void setUsingRichEditor(boolean usingRichEditor) {
		this.usingRichEditor = usingRichEditor;
	}
	public String getSkin() {
		return skin;
	}
	public void setSkin(String skin) {
		this.skin = skin;
	}
	/**
	 * @return the portalColumn
	 */
	public int getPortalColumn() {
		return portalColumn;
	}
	/**
	 * @param portalColumn the portalColumn to set
	 */
	public void setPortalColumn(int portalColumn) {
		this.portalColumn = portalColumn;
	}
	public List<QuickNote> getQuickNotes() {
		return quickNotes;
	}
	public void setQuickNotes(List<QuickNote> quickNotes) {
		this.quickNotes = quickNotes;
	}
	public LinkedHashMap<String, LinkedHashMap<String, String>> getContacts() {
		return contacts;
	}
	public void setContacts(LinkedHashMap<String, LinkedHashMap<String, String>> socialAccount) {
		this.contacts = socialAccount;
	}
	
}
