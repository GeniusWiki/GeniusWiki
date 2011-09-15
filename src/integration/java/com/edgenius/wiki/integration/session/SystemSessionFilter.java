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
package com.edgenius.wiki.integration.session;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Create and manage system wide (across multiple webapps) session.
 * @author Dapeng Ni 
 * 
 */
public class SystemSessionFilter implements Filter {

    // The session name to trace shared session
    public static final String SYS_SESSION_COOKIE = "JSESSIONID";

    public static final String SSO_SESSION_COOKIE = "JSESSIONIDSSO";

    public void init(FilterConfig config) throws ServletException {
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
	    ServletException {

	// Skip non-http request/response
	if (!(req instanceof HttpServletRequest && res instanceof HttpServletResponse)) {
	    chain.doFilter(req, res);
	    return;
	}

	SessionManager.startSession(req, res);

	// do following part of chain
	chain.doFilter(req, res);

	SessionManager.endSession();

    }

    public void destroy() {
	// do nothing
    }
}
