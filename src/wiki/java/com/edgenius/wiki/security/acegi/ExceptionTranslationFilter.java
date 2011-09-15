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
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.AuthenticationException;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.savedrequest.SavedRequest;
import org.springframework.util.Assert;

import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.WikiConstants;

/**
 * @author Dapeng.Ni
 */
public class ExceptionTranslationFilter extends org.springframework.security.ui.ExceptionTranslationFilter {
	private static final Logger log = LoggerFactory.getLogger(ExceptionTranslationFilter.class);
	
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(getPortResolver(), "portResolver must be specified");
		Assert.notNull(getAuthenticationTrustResolver(), "authenticationTrustResolver must be specified");
	}
	protected void sendStartAuthentication(ServletRequest request, ServletResponse response, FilterChain chain,
			AuthenticationException reason) throws ServletException, IOException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		SavedRequest savedRequest = new SavedRequest(httpRequest, getPortResolver());

		if (log.isDebugEnabled()) {
			log.debug("Authentication entry point being called; SavedRequest added to Session: " + savedRequest);
		}

		//Never put request URL to session, it is useless because it does not contain anchor token info
//		if (isCreateSessionAllowed()) {
//			// Store the HTTP request itself. Used by AbstractProcessingFilter
//			// for redirection after successful authentication (SEC-29)
//			httpRequest.getSession().setAttribute(AbstractProcessingFilter.ACEGI_SAVED_REQUEST_KEY, savedRequest);
//		}

		// SEC-112: Clear the SecurityContextHolder's Authentication, as the
		// existing Authentication is no longer considered valid
		SecurityContextHolder.getContext().setAuthentication(null);

		PrintWriter writer = response.getWriter();

		//ask client side to acquire current URL then jump to login.do, so that, when login form submit, it can send back redirectURL
		//see AuthenticationProcessingFilter.determineTargetUrl();
		String signUpURL = WikiConstants.URL_LOGIN; 
		if(WikiConstants.URL_LOGIN.startsWith("/")){
			signUpURL = WikiConstants.URL_LOGIN.substring(1);
		}
		writer.write("<html><body>" +
				"<form id='loginForm' name='loginForm' action='"+WebUtil.getHostAppURL()+ signUpURL + "' method='post'>" +
				"<input type='hidden' name='redir' id='redir' value=''>"+
				"</form>" +
				"<script type='text/javascript'>" +
				"document.getElementById('redir').value=window.location;" +
				"document.getElementById('loginForm').submit();" +
				"</script>" +
				"</body></html>");
//		getAuthenticationEntryPoint().commence(httpRequest, (HttpServletResponse) response, reason);
	}
	
}
