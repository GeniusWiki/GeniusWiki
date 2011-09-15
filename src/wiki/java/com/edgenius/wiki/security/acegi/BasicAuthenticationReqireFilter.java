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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.web.filter.OncePerRequestFilter;

import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;

/**
 * This class will put before BasicProcessingFilter. It will enforce ask user to do Basic Authentication.
 * 
 * Basically, I need:
 * <li>Remember me service could skip Basic Authentication. So, add rememberMeProcessingFilter before this filter</li>
 * <li>User login, then Basic Authentication also skipped</li>
 * <li>It will force user input Basic Authentication information if without any authentication happened before. 
 * So, wrap Basic Authentication string to AuthenticateRequestWrapper </li>
 * 
 * 
 * @author Dapeng.Ni
 */
public class BasicAuthenticationReqireFilter extends OncePerRequestFilter{
	protected UserReadingService userReadingService;
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if ((header != null) && header.startsWith("Basic ")) {
			//this user input authenticate code
			chain.doFilter(request, response);
			return;
		}
		
		//if user does not input Basic Authentication information, then try to use HttpRequest to authenticate
		String enString = "Basic ";
		User user = userReadingService.getUserByName(request.getRemoteUser());
		if(user != null && !user.isAnonymous()){
			enString = user.getUsername() + ":" + user.getPassword();
			enString = "Basic " + new String(Base64.encodeBase64(enString.getBytes()));
		}
		
		request = new AuthenticateRequestWrapper(request,enString);
		chain.doFilter(request, response);
		
	}
	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}

	private static class AuthenticateRequestWrapper extends HttpServletRequestWrapper {

		private String authCode;
		/**
		 * @param request
		 */
		public AuthenticateRequestWrapper(HttpServletRequest request,String authCode) {
			super(request);
			this.authCode = authCode;
		}
		public String getHeader(String name){
			if("Authorization".equalsIgnoreCase(name)){
				return authCode;
			}
			return super.getHeader(name);
			
		}
	}
}
