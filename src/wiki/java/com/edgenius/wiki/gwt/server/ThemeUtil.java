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

import java.util.ArrayList;
import java.util.List;

import com.edgenius.wiki.PageTheme;
import com.edgenius.wiki.Theme;
import com.edgenius.wiki.gwt.client.model.PageThemeModel;
import com.edgenius.wiki.gwt.client.model.ThemeModel;
import com.edgenius.wiki.service.RenderService;

/**
 * @author Dapeng.Ni
 */
public class ThemeUtil {

	/**
	 * @param theme
	 * @param model
	 */
	public static void copyToModel(Theme theme, ThemeModel model) {
		model.previewImageName = theme.getPreviewImageURL();
		model.title= theme.getTitle();
		model.description = theme.getDescription();
		model.name = theme.getName();
		model.type = theme.getCategory();
		
	}

	/**
	 * Copy for offline theme GearsDB saved data. 
	 */
	public static void copyContentToModel(Theme theme, ThemeModel model,RenderService renderService) {
		List<PageTheme> list = theme.getPageThemes();
		
		if(list != null && list.size() > 0){
			model.pageThemes = new ArrayList<PageThemeModel>();
			PageTheme defaultTheme = theme.getPageThemeByScope(PageTheme.SCOPE_DEFAULT);
			
			for (PageTheme pTheme : list) {
				//all page theme must be already inherited values, as client side won't handle inherited
				pTheme.inheritValue(defaultTheme);
				
				model.pageThemes.add(copyToPageThemeModel(pTheme,renderService));
			}
		}
		
	}

	private static PageThemeModel copyToPageThemeModel(PageTheme theme, RenderService renderService) {
		PageThemeModel model = new PageThemeModel();
		model.type = theme.getScope();
		model.welcome = theme.getWelcome();
		model.bodyMarkup = theme.getBodyMarkup();
		model.sidebarMarkup = renderService.renderRichHTML(null, null, renderService.renderHTML(theme.getSidebarMarkup()));
		
		return model;
	}

}
