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
package com.edgenius.wiki.webapp.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.edgenius.core.Installation;
import com.edgenius.core.Version;

/**
 * @author Dapeng.Ni
 */
public class InstallFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		
		if(!Installation.DONE || Version.LICENSE_STATUS > 0){
			String uri = request.getRequestURI();
			if(!uri.endsWith(".js") && !uri.endsWith(".css") && !uri.endsWith(".gif") && !uri.endsWith(".png") && !uri.endsWith(".jpg")){
				request.getRequestDispatcher("/install").forward(request, response);
			}else{
				request.getRequestDispatcher(request.getServletPath()).forward(request, response);
			}
		
		}else if(chain != null){
			chain.doFilter(request, response);
		}
	}

}
