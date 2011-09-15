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
package com.edgenius.wiki.webapp.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.facebook.api.Facebook;
import com.facebook.api.FacebookException;
import com.facebook.api.FacebookRestClient;
import com.facebook.api.schema.FriendsGetAppUsersResponse;


/**
 *
 * @author Fei Yang
 */
public class FacebookServlet extends HttpServlet {
	private static final long serialVersionUID = 3376869919243208844L;
	private static transient Logger log = LoggerFactory.getLogger(FacebookServlet.class);
	
	/** 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		log.info("query string is " + req.getQueryString());
		req.getSession().setAttribute(Constants.FACEBOOK_SKIN, true);
		String apiKey = this.getInitParameter("apiKey");
		String secretKey = this.getInitParameter("secretKey");
		Facebook fb = new Facebook (req, resp,apiKey, secretKey);
		fb.requireFrame("");
		
		if(fb.requireLogin("")){
			return;
		}
		if(!fb.isAdded()){
			log.info("Wibork is not added by facebook user " + fb.get_loggedin_user() + " yet");
			fb.requireAdd("");
		}else{// user has added system
			if("fbml".equals(req.getParameter("fb_force_mode"))) {
				String appUserFriends = getAppUserFriends(fb.getFacebookRestClient());
				req.setAttribute("apiKey", apiKey);
				req.setAttribute("appUserFriends", appUserFriends);
				req.getRequestDispatcher("/facebook.jsp").forward(req, resp);
			}else{
				req.getRequestDispatcher("/index.jsp").forward(req, resp);
			}
		}
	}

	private String getAppUserFriends(FacebookRestClient facebookClient) throws IOException{
		try{
			facebookClient.friends_getAppUsers();
		    FriendsGetAppUsersResponse response = (FriendsGetAppUsersResponse)facebookClient.getResponsePOJO();
		    List<Long> friends = response.getUid();
		    StringBuilder sb = new StringBuilder();
		    if(friends != null){
		    	int count = 0;
		    	for(Long friend : friends){
		    		if(count > 0){
		    			sb.append(",");
		    		}
		    		sb.append(friend);
		    		count++;
		    	}
		    }
		    return sb.toString();
		}catch(FacebookException e){
			log.info(e.getMessage(), e);
			return "";
		}
	}
	/** 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}
