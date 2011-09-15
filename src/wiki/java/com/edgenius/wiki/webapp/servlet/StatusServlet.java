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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.edgenius.core.service.UserService;
import com.edgenius.core.webapp.BaseServlet;
import com.edgenius.wiki.service.PageService;

/**
 * System status check servlet - most for hosting service to check if this webapp is running.
 * @author Dapeng.Ni
 */
public class StatusServlet extends BaseServlet {
	private static final long serialVersionUID = 6556291757098976964L;
	protected transient final Logger log = LoggerFactory.getLogger(StatusServlet.class);
	
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		boolean running = true;
		if( getUserService() == null || getPageService() == null)
			running = false;
		
		if(running)
			response.getWriter().write("OK");
		else
			response.getWriter().write("FAILED");
	}
	
	private UserService getUserService(){
		
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
		return (UserService) ctx.getBean(UserService.SERVICE_NAME);
	}
	private PageService getPageService(){
		
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
		return (PageService) ctx.getBean(PageService.SERVICE_NAME);
	}

}
