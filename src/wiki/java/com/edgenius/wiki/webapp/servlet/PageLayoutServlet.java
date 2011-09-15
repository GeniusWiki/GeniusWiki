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
package com.edgenius.wiki.webapp.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.edgenius.core.webapp.BaseServlet;
import com.edgenius.wiki.Skin;
import com.edgenius.wiki.service.ThemeService;

/**
 * This servlet will read system Skin and render Page view and edit layout to Javascript variables. These
 * varaible will be used for render page.
 * 
 * @author Dapeng.Ni
 */
public class PageLayoutServlet extends BaseServlet {
	private static final long serialVersionUID = 7523192521267236666L;
	private ThemeService themeService = null;

	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		
		Skin skin = getThemeService().getAppliedSkin();
		String viewLayout = skin.getViewLayout();
		String editLayout = skin.getEditLayout();
		request.setAttribute("view_layout", viewLayout);
		request.setAttribute("edit_layout", editLayout);
		request.getRequestDispatcher("/WEB-INF/pages/layout.jsp").forward(request, response);
		
	}
	private ThemeService getThemeService(){
		if(themeService != null)
			return themeService;
		
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
		themeService = (ThemeService) ctx.getBean(ThemeService.SERVICE_NAME);
		return themeService;
	}
}
