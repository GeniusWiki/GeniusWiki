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
package com.edgenius.wiki.security.acegi;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.ui.AccessDeniedHandler;

import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.WikiConstants;

/**
 * @author Dapeng.Ni
 */
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    public static final String ACEGI_SECURITY_ACCESS_DENIED_EXCEPTION_KEY = "ACEGI_SECURITY_403_EXCEPTION";
    protected final static Logger logger = LoggerFactory.getLogger(AccessDeniedHandlerImpl.class);

    //default value
    private String errorPage = WikiConstants.URL_ACCESS_DENIED;

    //********************************************************************
	//               method
	//********************************************************************
    public void handle(ServletRequest request, ServletResponse response, AccessDeniedException accessDeniedException)
        throws IOException, ServletException {
    	// this could happen when Webservice call
    	if(request == null) 
    		return; 
    	
        if (errorPage != null) {
        	if(WebUtil.isAjaxRequest((HttpServletRequest)request)){
        		((HttpServletResponse)response).sendRedirect(errorPage);
        	}else{
	            // Put exception into request scope (perhaps of use to a view)
	            ((HttpServletRequest) request).setAttribute(ACEGI_SECURITY_ACCESS_DENIED_EXCEPTION_KEY,
	                accessDeniedException);
	
	            // Perform RequestDispatcher "forward" - 
	            //please note this error page is not necessary to put webcontext even it is not empty
	            RequestDispatcher rd = request.getRequestDispatcher(errorPage);
	        	rd.forward(request, response);
	        	
	        	if (!response.isCommitted()) {
	                // Send 403 (we do this after response has been written)
	                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
	            }
        	}
        }
    }

    public void setErrorPage(String errorPage) {
        if (((errorPage != null) && !errorPage.startsWith("/")) || errorPage == null) {
            throw new IllegalArgumentException("ErrorPage must begin with '/'");
        }
        this.errorPage = errorPage;
    }

}
