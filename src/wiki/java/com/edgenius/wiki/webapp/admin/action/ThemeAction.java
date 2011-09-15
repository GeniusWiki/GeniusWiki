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
package com.edgenius.wiki.webapp.admin.action;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.edgenius.core.Global;
import com.edgenius.core.GlobalSetting;
import com.edgenius.core.service.MessageService;
import com.edgenius.wiki.Skin;
import com.edgenius.wiki.Theme;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.ThemeInvalidException;
import com.edgenius.wiki.service.ThemeInvalidVersionException;
import com.edgenius.wiki.service.ThemeSaveException;
import com.edgenius.wiki.service.ThemeService;
import com.edgenius.wiki.webapp.action.BaseAction;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class ThemeAction extends BaseAction {
	private static final String VIEW_THEMES = "themes";
	private static final String VIEW_SKINS = "skins";
	@Autowired
	private ThemeService themeService;
	@Autowired
	private SettingService settingService;
	
	@Autowired
	private MessageService messageService;
	
	private File theme;
    private String themeContentType;
    private String themeFileName;
    private int enableTheme;
    
    private String name;
    private File skin;
    private String skinContentType;
    private String skinFileName;
    private String oldSkinName;
    
    //********************************************************************
	//               Function methods
	//********************************************************************
    
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Skin
	public String listSkins(){
		List<Skin> skins = themeService.getAvailableSkins();
		getRequest().setAttribute("skins", skins);
		
		return VIEW_SKINS;
	}
	public String uploadSkin(){
		try {
			themeService.installSkin(skin);
		} catch (ThemeInvalidVersionException e) {
			getRequest().setAttribute("error", "Installed version " + e.getInstallVersion() + " is less than existing version " + e.getExistVersion());
			log.error("Invalid skin", e);
		} catch (ThemeInvalidException e) {
			getRequest().setAttribute("error", "Invalid skin package. Error is " + e.getMessage());
			log.error("Invalid skin", e);
		}
		
		return listSkins();
	}
	
	public String downloadSkin(){
		
		File dFile= themeService.downloadSkin(name);
		if(dFile == null){
			getRequest().setAttribute("error", messageService.getMessage("skin.file.no.exist"));
		}else{
			downloadFile(dFile.getName(), dFile);

		}
		return null;
	}
	
	public String applySkin(){
		try {
			
			GlobalSetting global = settingService.getGlobalSetting();
			//this just for confirm Global and GlobalSetting has consistent value
			Global.syncFrom(global);
			oldSkinName = global.getSkin();
			
			global.setSkin(name);
			settingService.saveOrUpdateGlobalSetting(global);
			
			getRequest().setAttribute("message", "Skin <b>" + name + "</b> is applied. ");
		} catch (Exception e) {
			getRequest().setAttribute("error", "Apply skin failed");
			log.error("Apply skin failed",e);
		}
		
		return listSkins();
	}
	public String deleteSkin(){
		try {
			themeService.removeSkin(name);
		} catch (ThemeSaveException e) {
			getRequest().setAttribute("error", "Delete skin failed. Error is " + e.getMessage());
			log.error("Delete skin failed.", e);
		}
		return listSkins();
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Theme
	public String listThemes(){
		List<Theme> themes = themeService.getAvailableThemes(true);
		getRequest().setAttribute("themes", themes);
		
		return VIEW_THEMES;
	}
	
	public String enableTheme(){
		themeService.enableTheme(name, enableTheme==0?false:true);
		return listThemes();
	}
	public String deleteTheme(){
		try {
			themeService.removeTheme(name);
		} catch (ThemeSaveException e) {
			getRequest().setAttribute("error", "Delete theme failed. Error is " + e.getMessage());
			log.error("Delete theme failed.", e);
		}
		return listThemes();
	}
	public String downloadTheme(){
		
		File dFile= themeService.downloadTheme(name);
		if(dFile == null){
			getRequest().setAttribute("error", messageService.getMessage("theme.file.no.exist"));
		}else{
			downloadFile(dFile.getName(), dFile);

		}
		return null;
	}
	public String uploadTheme(){
		try {
			themeService.installTheme(theme);
		} catch (ThemeInvalidVersionException e) {
			getRequest().setAttribute("error", "Installed version " + e.getInstallVersion() + " is less than existing version " + e.getExistVersion());
			log.error("Invalid theme", e);
		} catch (ThemeInvalidException e) {
			getRequest().setAttribute("error", "Invalid theme package. Error is " + e.getMessage());
			log.error("Invalid theme", e);
		}
		
		return listThemes();
	}
	
	//********************************************************************
	//               Set / Get 
	//********************************************************************

	public File getTheme() {
		return theme;
	}
	public String getThemeContentType() {
		return themeContentType;
	}
	public String getThemeFileName() {
		return themeFileName;
	}
	public File getSkin() {
		return skin;
	}
	public String getSkinContentType() {
		return skinContentType;
	}
	public String getSkinFileName() {
		return skinFileName;
	}

	public void setTheme(File theme) {
		this.theme = theme;
	}
	public void setThemeContentType(String themeContentType) {
		this.themeContentType = themeContentType;
	}
	public void setThemeFileName(String themeFileName) {
		this.themeFileName = themeFileName;
	}
	public void setSkin(File skin) {
		this.skin = skin;
	}
	public void setSkinContentType(String skinContentType) {
		this.skinContentType = skinContentType;
	}
	public void setSkinFileName(String skinFileName) {
		this.skinFileName = skinFileName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getEnableTheme() {
		return enableTheme;
	}
	public void setEnableTheme(int enableTheme) {
		this.enableTheme = enableTheme;
	}
	public String getOldSkinName() {
		return oldSkinName;
	}
	public void setOldSkinName(String oldSkinName) {
		this.oldSkinName = oldSkinName;
	}

}
