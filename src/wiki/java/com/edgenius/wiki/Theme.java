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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.util.WebUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This is object of SpaceTheme.themeObject
 * @author Dapeng.Ni
 */
@XStreamAlias("Theme")
public class Theme implements Serializable, Cloneable{
	private static final Logger log = LoggerFactory.getLogger(Theme.class);
	private static final long serialVersionUID = -5312828725759269670L;
	
	public static final String DEFAULT_THEME_WIKI = "defaultwiki";
	public static final String DEFAULT_THEME_BLOG = "defaultblog";
	
	//you can put this place holder to theme Body markup,it will replace by page wiki markup while rendering.
	public static final String BODY_PLACEHOLDER = "{$BODY}"; //NOTE this is not macro!
	public static final String THEME_CATEGORY_BLOG = "blog";
	public static final String THEME_CATEGORY_WIKI = "wiki";

	
	private float version=1;
	private String name;
	private String title;
	private String description;
	private String author;
	//THEME_CATEGORY_WIKI or THEME_CATEGORY_BLOG
	private String category;
	private int displaySequenceNumber;
	
	private String previewImageName = "preview.png";
	private String largeLogo= "logo.png";
	private String smallLogo= "small-logo.png";
	private Date updateDate;
	
	//Enabled - 0, Disabled - 1
	private int status;
	
	//special page themes, key is type
	private List<PageTheme> pageThemes;

	
	//these does not persist to theme XML, only render some page, service method will fill this field according to given page's bodyMarkup
	private transient PageTheme currentPageTheme;
	
	
	//how many spaces using this theme - this value hard to decide so far, so no use:
	//database query: spaceSetting save as string;
	//it not not worth to update theme.xml every time create/delete space...
	private transient int usedBySpaces;
	//********************************************************************
	//               some function methods
	//********************************************************************
	/**
	 * This method doesn't take care InputStream close.
	 */
	public static Theme fromXML(InputStream is){
		XStream xstream = new XStream();
		xstream.processAnnotations(Theme.class);
		xstream.processAnnotations(PageTheme.class);
		return (Theme) xstream.fromXML(is);
		
	}
	/**
	 * @param file
	 * @throws FileNotFoundException 
	 */
	public void toFile(File file) throws FileNotFoundException {
		XStream xstream = new XStream();
		xstream.processAnnotations(Theme.class);
		FileOutputStream os;
		os = new FileOutputStream(file);
		xstream.toXML(this, os);
		IOUtils.closeQuietly(os);
	}
	public Object clone(){
		Theme cTheme = null;
		try {
			cTheme = (Theme) super.clone();
			//deep clone
			if(pageThemes != null){
				List<PageTheme>  cPThemes = new ArrayList<PageTheme>();
				for (PageTheme pt: pageThemes) {
					cPThemes.add((PageTheme) pt.clone());
				}
				cTheme.setPageThemes(cPThemes);
			}
			
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cTheme;
	}

	public int hashCode(){
	
		return new HashCodeBuilder().append(version).append(name).hashCode();
	}
	public boolean equals(Object obj){
		if(!(obj instanceof Theme)){
			return false;
		}
		
	return new EqualsBuilder().append(version, ((Theme)obj).getVersion())
			.append(name, ((Theme)obj).getName()).isEquals();
	}
	public String getPreviewImageURL() {
		return WebUtil.getWebConext() + Constants.ThemesDir + "/" + name+"/" + previewImageName;
	}
	public String getLargeLogoURL() {
		return WebUtil.getWebConext()+ Constants.ThemesDir + "/" + name+"/" + largeLogo;
	}
	public String getSmallLogoURL() {
		return  WebUtil.getWebConext() + Constants.ThemesDir + "/" + name+"/" + smallLogo;
	}

	public PageTheme getPageThemeByScope(String scope) {
		if(pageThemes == null)
			return null;
		
		for (PageTheme pt : pageThemes) {
			if(StringUtils.equalsIgnoreCase(pt.getScope(),scope))
				return pt;
		}
		
		return null;
	}
	
	public boolean isRemovable() {
		return !(DEFAULT_THEME_WIKI.equalsIgnoreCase(this.name) || DEFAULT_THEME_BLOG.equalsIgnoreCase(this.name));
	}

	//********************************************************************
	//               set / get methods
	//********************************************************************
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public List<PageTheme> getPageThemes() {
		return pageThemes;
	}

	public void setPageThemes(List<PageTheme> pageThemes) {
		this.pageThemes = pageThemes;
	}
	public void addPageThemes(PageTheme pTheme) {
		if(pageThemes == null)
			pageThemes = new ArrayList<PageTheme>();
		
		pageThemes.add(pTheme);
	}

	public String getPreviewImageName() {
		return previewImageName;
	}

	public void setPreviewImageName(String previewImageName) {
		this.previewImageName = previewImageName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getLargeLogo() {
		return largeLogo;
	}

	public void setLargeLogo(String largeLogo) {
		this.largeLogo = largeLogo;
	}

	public String getSmallLogo() {
		return smallLogo;
	}

	public void setSmallLogo(String smallLogo) {
		this.smallLogo = smallLogo;
	}

	public PageTheme getCurrentPageTheme() {
		return currentPageTheme;
	}
	public void setCurrentPageTheme(PageTheme currentPageTheme) {
		this.currentPageTheme = currentPageTheme;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getAuthor() {
		return author;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getDisplaySequenceNumber() {
		return displaySequenceNumber;
	}

	public void setDisplaySequenceNumber(int displaySequenceNumber) {
		this.displaySequenceNumber = displaySequenceNumber;
	}

	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String type) {
		this.category = type;
	}
	public float getVersion() {
		return version;
	}
	public void setVersion(float version) {
		this.version = version;
	}

	public int getUsedBySpaces() {
		return usedBySpaces;
	}

	public void setUsedBySpaces(int usedBySpaces) {
		this.usedBySpaces = usedBySpaces;
	}

}
