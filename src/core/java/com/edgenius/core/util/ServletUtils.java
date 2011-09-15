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
package com.edgenius.core.util;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Dapeng.Ni
 */
public class ServletUtils {
	private static ThreadLocal<HttpServletRequest> servletRequest = new ThreadLocal<HttpServletRequest>();

	private static ThreadLocal<HttpServletResponse> servletResponse = new ThreadLocal<HttpServletResponse>();
	
	private static ThreadLocal<ServletContext> servletContext= new ThreadLocal<ServletContext>();

	public static ServletContext getServletContext() {
		return servletContext.get();
	}

	public static void setServletContext(ServletContext context) {
		servletContext.set(context);
	}

	public static HttpServletRequest getRequest() {
		return servletRequest.get();
	}

	public static HttpServletResponse getResponse() {
		return servletResponse.get();
	}

	public static void setRequest(HttpServletRequest request) {
		servletRequest.set(request);
	}

	public static void setResponse(HttpServletResponse response) {
		servletResponse.set(response);
	}

}
