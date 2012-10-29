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
package com.edgenius.wiki;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import com.edgenius.core.Global;
import com.edgenius.license.CryptUtil;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;

/**
 * @author Dapeng.Ni
 */
public class SpaceSetting implements Serializable{
	private static final long serialVersionUID = -7053188259188156569L;
	
	public static final int COMMENT_NOTIFY_FEQ_DAILY  = 1<<11;
	public static final int COMMENT_NOTIFY_FEQ_EVERY_POST  = 1<<10;
	
	public static final int COMMENT_NOTIFY_TO_SPACE_OWNEER = 1<<2;
	public static final int COMMENT_NOTIFY_TO_ALL_CONTRIBUTOR = 1<<1;
	public static final int COMMENT_NOTIFY_TO_AUTHOR = 1;
	
	public static final int WIDGET_STYLE_ITEM_SHORT_BY_MODIFIED_DATE = SharedConstants.WIDGET_STYLE_ITEM_SHORT_BY_MODIFIED_DATE; //default
	public static final int WIDGET_STYLE_ITEM_SHORT_BY_CREATE_DATE = SharedConstants.WIDGET_STYLE_ITEM_SHORT_BY_CREATE_DATE;
	public static final int WIDGET_STYLE_HOME_PAGE = SharedConstants.WIDGET_STYLE_HOME_PAGE;
	public static final int WIDGET_STYLE_SHOW_PORTRAIT = SharedConstants.WIDGET_STYLE_SHOW_PORTRAIT;
	
	private static final int DEFAULT_LIST_AMOUNT = 10;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// values
	private int rssContentLen = 0;
	private int rssItemsCount = 20;
	
	//how to display widget on dashboard
	private int widgetStyle = WIDGET_STYLE_ITEM_SHORT_BY_MODIFIED_DATE;
	//display modifier portrait, only work when widgetStyle is item_short_by_..., aka, display as list 
	private boolean hidePortrait = false;
	//TODO: not implement yet
	//this this another refine option on security. The reason I put it here is I don't want to make security setting too complex.
	//if true, user have reading permission won't see page history anymore. at least who has write permission could view histories
	//use "forbid" rather than "allow" as fieldName(aka meaning) is because of this is new property after system is online.
	//if value is not set, its default is false, then meaning allow reader view history, which compatible with existed system
	private boolean forbidReaderViewHistory = false; 
	
	private boolean customizedTheme = false;
	//theme name - it is default to defaultWiki if blank 
	private String theme;
	//Customized page level theme, currently, only sidebar is able to saved.
	private List<PageTheme> pageThemes;
	
	
	private int commentNotifyType = COMMENT_NOTIFY_FEQ_EVERY_POST | COMMENT_NOTIFY_TO_ALL_CONTRIBUTOR;
	private int commentNotifyMaxPerDay = Global.MaxCommentsNotifyPerDay;
	//how many items display on portlet
	private int itemAmount = DEFAULT_LIST_AMOUNT;
	
	private List<BlogMeta> linkedMetas;
	private Map<String,String> securityKeys;
	
	private Set<MenuItem> menuItems;
	
	//so far, only one shell support, but in future, may allow one space links to multiple shell
	//suppose key is some ID of space in Shell, value is themeName 
	private Map<String, String> shellNames;

	private boolean adDisabled;
	//********************************************************************
	//               Function methods
	//********************************************************************
	/**
	 * @param password
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */

	public String createSecurityKey(String plainPassword) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
		if(StringUtils.isBlank(plainPassword))
			return null;
		
		String secValue = CryptUtil.genSecurityKey(plainPassword);
		
		//save this security key into list
		Map<String, String> securityKeys = this.getSecurityKeys();
		if(securityKeys == null){
			securityKeys = new HashMap<String, String>();
			this.setSecurityKeys(securityKeys);
		}
		String secKey;
		do{
			//ensure this security key is brand new in security key list
			secKey = RandomStringUtils.randomAlphanumeric(WikiConstants.UUID_KEY_SIZE).toLowerCase();
		}while(securityKeys.get(secKey) != null);
		
		securityKeys.put(secKey,secValue);
		
		return secKey;
	}
	public String restorePlainPassword(String securityKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
		if(this.getSecurityKeys() != null){
			String encrypt = this.getSecurityKeys().get(securityKey);
			return CryptUtil.decryptSecurityKey(encrypt);
		}
		return null;
	}

	/**
	 * @param password
	 */
	public void removeSecurityKey(String secKey) {
		Map<String, String> securityKeys = this.getSecurityKeys();
		if(securityKeys != null){
			securityKeys.remove(secKey);
		}
	}
	/**
	 * @param key
	 * @return
	 */
	public BlogMeta getBlogMeta(String blogKey) {
		if(linkedMetas != null){
			for (BlogMeta blogMeta : linkedMetas) {
				if(StringUtils.equals(blogMeta.getKey(), blogKey))
					return blogMeta;
			}
		}
		return null;
	}
	
	public void setShellTheme(String spaceShellUid, String themeName) {
		if(shellNames == null)
			this.shellNames = new HashMap<String, String>();
		
		this.shellNames.put(spaceShellUid, themeName);
	}
	//********************************************************************
	//               set / get 
	//********************************************************************
	public int getCommentNotifyMaxPerDay() {
		return commentNotifyMaxPerDay;
	}

	public void setCommentNotifyMaxPerDay(int commentNotifyMaxPerDay) {
		this.commentNotifyMaxPerDay = commentNotifyMaxPerDay;
	}
	
	public int getCommentNotifyType() {
		return commentNotifyType;
	}

	public void setCommentNotifyType(int commentNotifyType) {
		this.commentNotifyType = commentNotifyType;
	}

	public int getRssContentLen() {
		return rssContentLen;
	}

	public void setRssContentLen(int rssContentLen) {
		this.rssContentLen = rssContentLen;
	}

	public int getRssItemsCount() {
		return rssItemsCount;
	}

	public void setRssItemsCount(int rssItemsCount) {
		this.rssItemsCount = rssItemsCount;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public boolean isCustomizedTheme() {
		return customizedTheme;
	}

	public int getWidgetStyle() {
		return widgetStyle;
	}

	public void setWidgetStyle(int widgetStyle) {
		this.widgetStyle = widgetStyle;
	}

	public void setCustomizedTheme(boolean customizedTheme) {
		this.customizedTheme = customizedTheme;
	}

	public boolean isForbidReaderViewHistory() {
		return forbidReaderViewHistory;
	}

	public void setForbidReaderViewHistory(boolean forbidReaderViewHistory) {
		this.forbidReaderViewHistory = forbidReaderViewHistory;
	}

	public boolean isHidePortrait() {
		return hidePortrait;
	}

	public void setHidePortrait(boolean hidePortrait) {
		this.hidePortrait = hidePortrait;
	}

	public int getItemAmount() {
		return itemAmount <= 0?DEFAULT_LIST_AMOUNT:itemAmount;
	}

	public List<PageTheme> getPageThemes() {
		return pageThemes;
	}
	public void setPageThemes(List<PageTheme> pageThemes) {
		this.pageThemes = pageThemes;
	}
	public void setItemAmount(int itemAmount) {
		this.itemAmount = itemAmount;
	}

	public List<BlogMeta> getLinkedMetas() {
		return linkedMetas;
	}

	public void setLinkedMetas(List<BlogMeta> linkedMetas) {
		this.linkedMetas = linkedMetas;
	}

	public Map<String, String> getSecurityKeys() {
		return securityKeys;
	}

	public void setSecurityKeys(Map<String, String> securityKeys) {
		this.securityKeys = securityKeys;
	}
	public Set<MenuItem> getMenuItems() {
		return menuItems;
	}
	public void setMenuItems(Set<MenuItem> menuItems) {
		this.menuItems = menuItems;
	}
	public Map<String, String> getShellNames() {
		return shellNames;
	}
	public void setShellNames(Map<String, String> shellNames) {
		this.shellNames = shellNames;
	}
	public boolean isAdDisabled() {
		return adDisabled;
	}
	public void setAdDisabled(boolean adDisabled) {
		this.adDisabled = adDisabled;
	}


}
