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
package com.edgenius.wiki.gwt.server;

import java.util.List;

import com.edgenius.wiki.Theme;
import com.edgenius.wiki.gwt.client.model.ThemeListModel;
import com.edgenius.wiki.gwt.client.model.ThemeModel;
import com.edgenius.wiki.gwt.client.server.ThemeController;
import com.edgenius.wiki.gwt.server.handler.GWTSpringController;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.ThemeService;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class ThemeControllerImpl  extends GWTSpringController implements ThemeController {
	private ThemeService themeService; 
	private SpaceService spaceService; 
	
	//JDK1.6 @Override
	public ThemeListModel getAvailableTheme(String spaceUname) {
		ThemeListModel model = new ThemeListModel();
		if(spaceUname != null){
			Space space = spaceService.getSpaceByUname(spaceUname);
			model.chosenThemeName = space.getSetting().getTheme();
		}
		List<Theme> list = themeService.getAvailableThemes(false);
		if(list != null){
			for (Theme theme : list) {
				ThemeModel tModel = new ThemeModel();
				ThemeUtil.copyToModel(theme,tModel);
				model.themeList.add(tModel);
			}
		}
		return model;
	}
	
	//********************************************************************
	//               set /get 
	//********************************************************************
	public void setThemeService(ThemeService themeService) {
		this.themeService = themeService;
	}

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

}
