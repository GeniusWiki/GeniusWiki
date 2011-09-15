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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Dapeng.Ni
 */
@XStreamAlias("PageTheme")
public class PageTheme implements Serializable, Cloneable {
	private static final long serialVersionUID = 955187427702752483L;
	private static final Logger log = LoggerFactory.getLogger(PageTheme.class);
	//current only 2 scope, may extend later
	//theme will contain any amount page level theme, 
	//the possible key of page is SCOPE(following Value) or PageUuid, 
	//the priority is , pageUuid theme will override pageScope theme
	public static final String SCOPE_HOME = SharedConstants.THEME_PAGE_SCOPE_HOME;
	public static final String SCOPE_DEFAULT = SharedConstants.THEME_PAGE_SCOPE_DEFAULT;
	
	//This also as key.  Please note, in schema, it only allow "any" and "home". It is valid for third party customise theme.
	//However, in SpaceSetting.PageThemes, it allow uses PageUUID as scope to link specified page theme.
	private String scope;
	//this message will show up when user enter a new blank page. 
	private String welcome;
	private String bodyMarkup;
	private String spaceMenuMarkup;
	private String sidebarMarkup;

	
	//********************************************************************
	//               constructor
	//********************************************************************
	public PageTheme() {
	}

	//********************************************************************
	//               function methods
	//********************************************************************
	/**
	 *  All theme values also do inherit (except type): 
	 * <li>If value is not empty (blank is also valid value), using current value.</li>
	 * <li>If value is null, extends from default page theme(TYPE_DEFAULT=0)</li>
	 */
	public void inheritValue(PageTheme defaultValue) {
		if(defaultValue == null)
			return;
		
		if(this.welcome == null){
			this.welcome = defaultValue.getWelcome();
		}
		if(this.bodyMarkup == null){
			this.bodyMarkup = defaultValue.getBodyMarkup();
		}
		if(this.sidebarMarkup == null){
			this.sidebarMarkup = defaultValue.getSidebarMarkup();
		}
		if(this.spaceMenuMarkup == null){
			this.spaceMenuMarkup = defaultValue.getSpaceMenuMarkup();
		}
		
	}
	
	public Object clone(){
		PageTheme cTheme = null;
		try {
			cTheme = (PageTheme) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cTheme;
	}
	public int hashCode(){
		return this.getScope() != null?this.getScope().hashCode():1;
	}
	public boolean equals(Object obj){
		if(!(obj instanceof PageTheme))
			return false;
		
		return StringUtils.equalsIgnoreCase(((PageTheme)obj).getScope(),this.getScope());
	}
	//********************************************************************
	//               set /get 
	//********************************************************************
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getBodyMarkup() {
		return StringUtils.trim(bodyMarkup);
	}
	public void setBodyMarkup(String bodyMarkup) {
		this.bodyMarkup = bodyMarkup;
	}
	public String getSidebarMarkup() {
		return StringUtils.trim(sidebarMarkup);
	}
	public void setSidebarMarkup(String sidebarMarkup) {
		this.sidebarMarkup = sidebarMarkup;
	}
	public String getWelcome() {
		return StringUtils.trim(welcome);
	}
	public void setWelcome(String welcome) {
		this.welcome = welcome;
	}

	public String getSpaceMenuMarkup() {
		return StringUtils.trim(spaceMenuMarkup);
	}

	public void setSpaceMenuMarkup(String spaceMenuMarkup) {
		this.spaceMenuMarkup = spaceMenuMarkup;
	}

}
