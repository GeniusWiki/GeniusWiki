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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.DefaultRedirectStrategy;

import com.edgenius.core.Constants;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.security.service.CaptchaReqiredFilterService;

/**
 * Don't realy redirect url, just send url as string for ajax usage.
 * @author Dapeng.Ni
 */
//upgrade: AbstractAuthenticationProcessingFilter
public class AuthenticationRedirectStrategy extends DefaultRedirectStrategy {
	
	private CaptchaReqiredFilterService captchaReqiredFilterService;
	@Override
	public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url)
			throws IOException {
		String username = request.getRemoteUser();
		if(url.indexOf("/login_error") == -1){
			captchaReqiredFilterService.clean(username);
			response.getWriter().write(url);
		}else{
//			login error, re display user name
			boolean require = captchaReqiredFilterService.reqiredCaptche(username);
			response.getOutputStream().write((SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_HEADER_ERROR_IN_USERPASS+(require?1:0)+username).getBytes(Constants.UTF8));
		}
	}
	/**
	 * This method will be called after success login. This means loginForm should has redirect URL.
	 */
	protected String determineTargetUrl(HttpServletRequest request) {
		//try to get url from login submit form.
		String url = request.getParameter("redir");
		//as super class isAlwaysUseDefaultTargetUrl() is not visible, here just use "false"
		String targetUrl = false ? null : url; 

		if (targetUrl == null) {
//			targetUrl = getDefaultTargetUrl();
		}

		return targetUrl;
	}

	//********************************************************************
	//               set /get
	//********************************************************************
	public void setCaptchaReqiredFilterService(CaptchaReqiredFilterService captchaReqiredFilterService) {
		this.captchaReqiredFilterService = captchaReqiredFilterService;
	}

}
