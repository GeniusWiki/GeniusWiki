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
package com.edgenius.wiki.integration.rest;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.edgenius.wiki.Shell;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.SpaceService;

/**
 *  *
 * This Servlet copes with some shell validation service:
 * <li>Accept shell sky from Shell</li>
 * <li>Space deletion confirmation</li>
 * <li>Page deletion confirmation</li>
 * <li></li>
 * @author Dapeng.Ni
 */
public class ShellValidatorServlet extends HttpServlet {
	private static final Logger log = LoggerFactory.getLogger(ShellValidatorServlet.class);
	private static final long serialVersionUID = 2055170482931420558L;
	
	private SpaceService spaceService;
	private PageService pageService;
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//shell key 
		String key = request.getParameter("key");
		//page remove confirm request
		String pageUuid = request.getParameter("puuid");
		//space remove confirm request
		String spaceUname = request.getParameter("uname");
		
		if(!StringUtils.isEmpty(key)){
			log.info("Shell validation for shell key...");
			Shell.updateShellKey(key);
		}else if(!StringUtils.isEmpty(pageUuid)){
			log.info("Page remove confirm request:{}", pageUuid);
			Page page = getPageServiceBean().getCurrentPageByUuid(pageUuid);
			if(page == null)
				response.getWriter().print(pageUuid);
			else
				response.getWriter().print("Faked deleted request on page.");
			
		}else if(!StringUtils.isEmpty(spaceUname)){
			log.info("Space remove confirm request:{}", spaceUname);
			Space space = getSpaceServiceBean().getSpaceByUname(spaceUname);
			if(space == null || space.containExtLinkType(Space.EXT_LINK_SHELL_DISABLED))
				response.getWriter().print(spaceUname);
			else
				response.getWriter().print("Faked deleted request on space");
		}
	}

	private PageService getPageServiceBean() {
		if(pageService == null){
			ServletContext context = this.getServletContext();
			ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
			pageService = (PageService) ctx.getBean(PageService.SERVICE_NAME);
		}
		return pageService;
	}
	private SpaceService getSpaceServiceBean() {
		if(spaceService == null){
			ServletContext context = this.getServletContext();
			ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
			spaceService = (SpaceService) ctx.getBean(SpaceService.SERVICE_NAME);
		}
		return spaceService;
	}

}
