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
package com.edgenius.wiki.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.edgenius.core.repository.FileNode;
import com.edgenius.wiki.PageTheme;
import com.edgenius.wiki.Skin;
import com.edgenius.wiki.Theme;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.Space;

/**
 * Skin  - apply to system level.
 * Theme - apply to space.  It may contains certain page's theme.
 * 
 * @author Dapeng.Ni
 */
public interface ThemeService {

	String SERVICE_NAME = "themeService";

	
	void uploadSystemLogo(FileNode logo);
	/**
	 * @return system logo nodeUUid if has, otherwise, return null
	 */
	String getSystemLogo();
	
	List<Skin> getAvailableSkins();
	
	/**
	 * Return Global.Skin applied system skin. Please note, this method also fill in
	 * view_layout and edit_layout.  If Skin doesn't contains these 2 layout files, i.e., {skin}/view_layout.html and
	 * {skin}/edit_layout.html,  it will use system default layout instead.  
	 * 
	 * These 2 layout is Javascript safed string.
	 * 
	 * @return
	 * @throws IOException
	 */
	Skin getAppliedSkin() throws IOException;
	
	List<Theme> getAvailableThemes(boolean includeDisabled);
	
	/**
	 * Save customized PageTheme into given space.
	 * @param spaceUid
	 * @param theme
	 * @throws ThemeSaveException
	 */
	public void saveOrUpdatePageTheme(Space space, PageTheme theme) throws ThemeSaveException;
	/**
	 * Get space level theme, which includes all customized page themes.
	 * @param space
	 * @return
	 */
	Theme getSpaceTheme(Space space);
	/**
	 * Get particular page theme, such as home page. It will fill the PageTheme value into Theme.setCurrentPageTheme().
	 * 
	 * @param page, must contain valid space object to decide Space level page, refer to getTheme(Space) 
	 * @param pageScope refer to Theme.SCOPE_*
	 * @return
	 */
	Theme getPageTheme(AbstractPage page, String pageScope);
	/**
	 * Valid, save and install theme
	 * @param theme
	 */
	void installTheme(File theme) throws ThemeInvalidException;
	/**
	 * Valid, save and install skin
	 * @param skin
	 */
	void installSkin(File skin) throws ThemeInvalidException;
	
	/**
	 * @param themeName
	 */
	boolean removeSkin(String themeName) throws ThemeSaveException;
	/**
	 * @param name
	 * @return
	 */
	File downloadSkin(String name);
	
	boolean removeTheme(String themeName) throws ThemeSaveException;

	File downloadTheme(String name);
	
	void enableTheme(String name, boolean enable);
	
	
}
