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
package com.edgenius.wiki.integration.webservice.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceException;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.binding.soap.interceptor.SoapHeaderInterceptor;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Global;
import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.CodecUtil;
import com.edgenius.wiki.security.service.SecurityService;

/**
 * TODO: implement securityService.proxyLogout()
 * This code inspired by http://chrisdail.com/download/BasicAuthAuthorizationInterceptor.java
 */
public class WsSecurityInterceptor extends SoapHeaderInterceptor {

	private static final Logger log = LoggerFactory.getLogger(WsSecurityInterceptor.class);
	
	private UserReadingService userReadingService;
	private SecurityService securityService;  
	
	@Override
	public void handleMessage(Message message) throws Fault {
		// This is set by CXF
		AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);

		// If the policy is not set, the user did not specify credentials
		// This could be anonymous request
		if (policy == null) {
			log.info("User attempted to log in with no credentials, treat it as anonymous");
			securityService.proxyLogin(null);
		}else{
			if (log.isDebugEnabled()) {
				log.debug("Logging in use: " + policy.getUserName());
			}
			// Verify the password
			User user = userReadingService.getUserByName(policy.getUserName());
			// !!! Must do password validation here as SecurityServiceproxyLogin() always give authentication
			String passwd = CodecUtil.encodePassword(policy.getPassword(), Global.PasswordEncodingAlgorithm);
			if (user == null || !StringUtils.equals(user.getPassword(),passwd)) {
				log.warn("Invalid username or password for user: " + policy.getUserName());
				//stopResponse(message, HttpURLConnection.HTTP_FORBIDDEN);
				throw new WebServiceException("Invalid username or password for user: " + policy.getUserName());
			}
			securityService.proxyLogin(user.getUsername());
		}

	}

	private void stopResponse(Message message, int responseCode) {
		Message outMessage = getOutMessage(message);
		outMessage.put(Message.RESPONSE_CODE, responseCode);

		// Set the response headers
		Map<String, List<String>> responseHeaders = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
		if (responseHeaders != null) {
			responseHeaders.put("WWW-Authenticate", Arrays.asList(new String[] { "Basic realm=realm" }));
			responseHeaders.put("Content-Length", Arrays.asList(new String[] { "0" }));
		}
		message.getInterceptorChain().abort();
		try {
			getConduit(message).prepare(outMessage);
			close(outMessage);
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}
	}

	private Message getOutMessage(Message inMessage) {
		Exchange exchange = inMessage.getExchange();
		Message outMessage = exchange.getOutMessage();
		if (outMessage == null) {
			Endpoint endpoint = exchange.get(Endpoint.class);
			outMessage = endpoint.getBinding().createMessage();
			exchange.setOutMessage(outMessage);
		}
		outMessage.putAll(inMessage);
		return outMessage;
	}

	private Conduit getConduit(Message inMessage) throws IOException {
		Exchange exchange = inMessage.getExchange();
		EndpointReferenceType target = exchange.get(EndpointReferenceType.class);
		Conduit conduit = exchange.getDestination().getBackChannel(inMessage, null, target);
		exchange.setConduit(conduit);
		return conduit;
	}

	private void close(Message outMessage) throws IOException {
		OutputStream os = outMessage.getContent(OutputStream.class);
		os.flush();
		os.close();
	}

	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

}
