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
package com.edgenius.wiki.gwt.server.handler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.AuthenticationException;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.edgenius.core.Global;
import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.service.UserService;
import com.edgenius.core.util.ServletUtils;
import com.edgenius.core.webapp.filter.AjaxRedirectFilter.RedirectResponseWrapper;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.GeneralModel;
import com.edgenius.wiki.gwt.client.model.SerializableWhiteList;
import com.edgenius.wiki.gwt.client.server.CaptchaVerifiedException;
import com.edgenius.wiki.gwt.client.server.ClientAccessDeniedException;
import com.edgenius.wiki.gwt.client.server.ClientAuthenticationException;
import com.edgenius.wiki.util.WikiUtil;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;

/**
 * Hack to <code>com.google.gwt.user.server.rpc.RemoteServiceServlet</code> give a chance to add flag 
 * on attribute of HttpServletRequest to identify it is ajax call.
 * 
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class GWTSpringController  extends RemoteServiceServlet implements Controller,ServletContextAware {
	private static final Logger log = LoggerFactory.getLogger(GWTSpringController.class);
	private ServletContext servletContext;
	
	//!!! Must keep setting with AbstractProcessingFilter in applicationContext_security.xml
	protected boolean alwaysUseDefaultTargetUrl = true;
	protected UserService userService;
	protected UserReadingService userReadingService;
	protected boolean offline;
	
	public void setOffline(boolean offline){
		this.offline = offline;
	}
	public boolean isOffline(){
		return offline;
	}
	public String processCall(String payload) throws SerializationException {
		try {
			// Copy & pasted & edited from the GWT 1.4.3 RPC documentation
			RPCRequest rpcRequest = RPC.decodeRequest(payload,this.getClass(),this);
			
			Method targetMethod = rpcRequest.getMethod();
			Object[] targetParameters = rpcRequest.getParameters();
			SerializationPolicy policy = rpcRequest.getSerializationPolicy();
			try {
				Object result = targetMethod.invoke(this, targetParameters);
				String encodedResult;
				String redir = getRedir();
				if(redir != null){
					Throwable ae;
					if(redir.endsWith(WikiConstants.URL_CAPTCHA_VERIFIED_ERROR)){
						ae = new CaptchaVerifiedException(redir);
					}else if(redir.endsWith(WikiConstants.URL_ACCESS_DENIED)){
						ae = new ClientAccessDeniedException(redir);
					}else{
						//OK, maybe accessDenied or authentication or other error, then let Gwt redirect it anyway.
						ae = new ClientAuthenticationException(redir);
					}
					log.info("Send redir value " + redir);
					encodedResult = RPC.encodeResponseForFailure(null, ae,policy);
				}else{
					if(result instanceof GeneralModel){
						//set current user info,so that client can check if its session is expired for each request
						User user = WikiUtil.getUser();
						((GeneralModel)result).loginUserFullname = user.getFullname();
						((GeneralModel)result).loginUsername = user.getUsername();
						((GeneralModel)result).loginUserUid= user.getUid();
					}
					//return correct result
					encodedResult = RPC.encodeResponseForSuccess(rpcRequest.getMethod(), result,policy);
				}
				return encodedResult;
			}catch (IllegalArgumentException e) {
				log.error("Handle ajax call for service "+ this + " with exception.",e);
				SecurityException securityException = new SecurityException("Blocked attempt to invoke method " + targetMethod);
				securityException.initCause(e);
				throw securityException;
			} catch (IllegalAccessException e) {
				log.error("Handle ajax call for service "+ this + " with exception.",e);
				SecurityException securityException = new SecurityException("Blocked attempt to access inaccessible method " + targetMethod
						+ (this!= null ? " on service " + this : ""));
				securityException.initCause(e);
				throw securityException;
			} catch (InvocationTargetException e) {
				log.error("Handle ajax call for service "+ this + " with exception.",e);
				Throwable cause = e.getCause();
				log.warn("Get RPC InvocationTargetException exception, the root cause is " + cause);
				//following handle are not exactly response if redir value is null: accessDenied is only for login user, here does not check this. 
				//so MethodExceptionHandler.handleException() give more exactly response.
				//MethodSecurityInterceptor.invoke() may throw Authentication and AccessDenied Exception, if targetMethod does not
				//capture these exception, then they will go to here!! The example is PageControllerImpl.viewPage(), it handle the accessDenied
				//exception, if it does not, then here will handle it...
				if(cause instanceof AuthenticationException){
					String redir = getRedir();
					if(redir == null)
						//system default value
						redir = WikiConstants.URL_LOGIN;
					log.info("Send Authentication redirect URL " + redir);
					ClientAuthenticationException ae = new  ClientAuthenticationException(redir);
					return RPC.encodeResponseForFailure(null, ae,policy);
				}else if (cause instanceof AccessDeniedException){
					String redir = getRedir();
					if(redir == null)
						//system default value
						redir = WikiConstants.URL_ACCESS_DENIED;
					log.info("Send AccessDenied redirect URL " + redir);
					ClientAccessDeniedException ae = new  ClientAccessDeniedException(redir);
					return RPC.encodeResponseForFailure(null, ae,policy);
				}
				
				log.info("Return unexpected exception to client side " + cause);
				String failurePayload = RPC.encodeResponseForFailure(rpcRequest.getMethod(), cause,policy);
				return failurePayload;
			}
		} catch (IncompatibleRemoteServiceException e) {
			log.warn(e.getMessage());
			return RPC.encodeResponseForFailure(null, e);
		} catch (Exception e) {
			log.error("Ajax call failed",e);
			throw new RuntimeException(e);
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//Override  org.springframework.web.servlet.mvc.Controlle
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			ServletUtils.setRequest(request);
			ServletUtils.setResponse(response);
			ServletUtils.setServletContext(servletContext);
			
			Integer suppress = (Integer) request.getSession().getAttribute(WikiConstants.ATTR_SUPPRESS);
			if(suppress == null){
				suppress = Global.suppress;
			}
			Global.setCurrentSuppress(suppress);
			
			doPost(request, response);
		} finally {
			ServletUtils.setRequest(null);
			ServletUtils.setResponse(null);
			ServletUtils.setServletContext(null);
			
		}

		return null;
	}


	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Implement  ServletContextAware
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}
	
	
	/**
	 * This method is dummy method. To avoid to rpcRequest.getSerializationPolicy() method does not throw  NotSerializableException.
	 * It puts some Model which is going to used in Gwt client side, but these Models do not show in any functional methods. So, these
	 * models will merge into GWT SerializationPolicy during GWT compiling.
	 * 
	 * @see com.edgenius.wiki.gwt.client.server.RemoteService
	 */
	public SerializableWhiteList serialPolicy(SerializableWhiteList list){
	    throw new RuntimeException("This Method is only used to put Beans to the SerializationPolicy, please do not call it"); 
	}
	
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}
	//********************************************************************
	//               private method
	//********************************************************************
	private String getRedir()  {
		String redir = null;
		HttpServletResponse re = ServletUtils.getResponse();
		if(re instanceof RedirectResponseWrapper){
			RedirectResponseWrapper response = (RedirectResponseWrapper) re; 
		    if (response.getRedirect() != null) {
		    	redir = response.getRedirect();
		    }
		}
		
		if(redir != null && redir.trim().length() == 0)
			redir = null;
		
		return redir;
	}
}
