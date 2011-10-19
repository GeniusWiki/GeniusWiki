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

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.PortResolver;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.Assert;

import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.WikiConstants;

/**
 * This class most code form <code>org.springframework.security.ui.ExceptionTranslationFilter</code>
 * @author Dapeng.Ni
 */
public class MethodExceptionHandler implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(MethodExceptionHandler.class);

    private AccessDeniedHandler accessDeniedHandler = new AccessDeniedHandlerImpl();
    private AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();
    private PortResolver portResolver = new PortResolverImpl();
    private boolean createSessionAllowed = true;

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(portResolver, "portResolver must be specified");
        Assert.notNull(authenticationTrustResolver, "authenticationTrustResolver must be specified");
    }

    public void handleException(RuntimeException exception) throws IOException, ServletException {
    	HttpServletRequest request = WebUtil.getRequest();
    	HttpServletResponse response = WebUtil.getResponse();
    	
        if (exception instanceof AuthenticationException) {
            if (logger.isDebugEnabled()) {
                logger.debug("Authentication exception occurred; redirecting to authentication entry point", exception);
            }

            sendStartAuthentication(request, response, (AuthenticationException) exception);
        } else if (exception instanceof AccessDeniedException) {
            if (authenticationTrustResolver.isAnonymous(SecurityContextHolder.getContext().getAuthentication())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Access is denied (user is anonymous); redirecting to authentication entry point",
                        exception);
                }

                sendStartAuthentication(request, response, new InsufficientAuthenticationException("Full authentication is required to access this resource"));
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Access is denied (user is not anonymous); delegating to AccessDeniedHandler",
                        exception);
                }

                accessDeniedHandler.handle(request, response, (AccessDeniedException) exception);
            }
        }
    }

    /**
     * If <code>true</code>, indicates that <code>SecurityEnforcementFilter</code> is permitted to store the
     * target URL and exception information in the <code>HttpSession</code> (the default). In situations where you do
     * not wish to unnecessarily create <code>HttpSession</code>s - because the user agent will know the failed URL,
     * such as with BASIC or Digest authentication - you may wish to set this property to <code>false</code>. Remember
     * to also set the {@link org.springframework.security.context.HttpSessionContextIntegrationFilter#allowSessionCreation} to
     * <code>false</code> if you set this property to <code>false</code>.
     *
     * @return <code>true</code> if the <code>HttpSession</code> will be used to store information about the failed
     *         request, <code>false</code> if the <code>HttpSession</code> will not be used
     */
    public boolean isCreateSessionAllowed() {
        return createSessionAllowed;
    }

    protected void sendStartAuthentication(ServletRequest request, ServletResponse response, AuthenticationException reason) throws ServletException, IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;


        SecurityContextHolder.getContext().setAuthentication(null);

        //send back default login url
        if(httpResponse != null){ // Webservice, anonymous request could cause httpResponse == null
        	if(WebUtil.isAjaxRequest((HttpServletRequest) request)){
        		//if ajax, we can't add web context as it will do in client side. 
        		httpResponse.sendRedirect(WikiConstants.URL_LOGIN);
        	}else{
        		//for nonajax, we should add web context. -- I can not find a scenario when request is not ajax yet!
        		//TODO:use AuthenticationEntryPoint.commence(httpRequest, response, reason) replace below code
        		String redirUrl = WikiConstants.URL_LOGIN;
    			if(WikiConstants.URL_LOGIN.length() > 0 && WikiConstants.URL_LOGIN.charAt(0) == '/'){
    				redirUrl = WikiConstants.URL_LOGIN.substring(1);
    			}
            	httpResponse.sendRedirect(WebUtil.getWebConext()+redirUrl);
        	}
        }
    }

    public void setAccessDeniedHandler(AccessDeniedHandler accessDeniedHandler) {
        Assert.notNull(accessDeniedHandler, "AccessDeniedHandler required");
        this.accessDeniedHandler = accessDeniedHandler;
    }

    public void setAuthenticationTrustResolver(AuthenticationTrustResolver authenticationTrustResolver) {
        this.authenticationTrustResolver = authenticationTrustResolver;
    }

    public void setCreateSessionAllowed(boolean createSessionAllowed) {
        this.createSessionAllowed = createSessionAllowed;
    }

    public void setPortResolver(PortResolver portResolver) {
        this.portResolver = portResolver;
    }
    public AuthenticationTrustResolver getAuthenticationTrustResolver() {
        return authenticationTrustResolver;
    }

    public PortResolver getPortResolver() {
        return portResolver;
    }
}
