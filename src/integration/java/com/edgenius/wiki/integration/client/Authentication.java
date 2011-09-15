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
package com.edgenius.wiki.integration.client;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import com.edgenius.wiki.integration.WsContants;

/**
 * 
 * Logout use URL: /j_spring_security_logout
 * @author Dapeng.Ni
 */
public class Authentication {
	//Must greater than 0
	private int tokenValiditySeconds  = 7776000;
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param username
	 * @param encryptPassword Encrypt password, i.e., password is directly from database. 
	 * @param targetWebContext, web context of GeniusWiki, for example, http://gw_url/genius is GeniusWiki root URL, then web context
	 * is /genius.
	 */
	public void  login(HttpServletRequest request, HttpServletResponse response,
			String username, String encryptPassword, String targetWebContext){
		if(username == null || username.trim().length() == 0 || encryptPassword == null || encryptPassword.trim().length() == 0)
			return;
		
		targetWebContext = targetWebContext.trim();
		if(targetWebContext.length() != 0){
			if(!targetWebContext.startsWith("/"))
				targetWebContext = "/" + targetWebContext;
			if(targetWebContext.length() > 1 && targetWebContext.endsWith("/"))
				targetWebContext = targetWebContext.substring(0,targetWebContext.length() -1);
		}else{
			targetWebContext  = "/";
		}
		
        long expiryTime = System.currentTimeMillis() + 1000L*tokenValiditySeconds;
        String signatureValue = makeTokenSignature(expiryTime, username, encryptPassword);
		setCookie(new String[] {username, Long.toString(expiryTime), signatureValue}, tokenValiditySeconds,targetWebContext, request, response);
		
	}


    private void setCookie(String[] tokens, int maxAge, String targetWebContext, HttpServletRequest request, HttpServletResponse response) {
        String cookieValue = encodeCookie(tokens);
        Cookie cookie = new Cookie(WsContants.REMEMBER_ME_COOKIE_NAME, cookieValue);
        cookie.setMaxAge(maxAge);
        cookie.setPath(targetWebContext);
        response.addCookie(cookie);
    }
 
	private String makeTokenSignature(long tokenExpiryTime, String username, String password) {
		return DigestUtils.md5Hex(username + ":" + tokenExpiryTime + ":" + password + ":" + WsContants.REMEMBERME_COOKIE_KEY);
	}
    /**
     * @param cookieTokens the tokens to be encoded.
     * @return base64 encoding of the tokens concatenated with the ":" delimiter.
     */
    private String encodeCookie(String[] cookieTokens) {
        StringBuffer sb = new StringBuffer();
        for(int i=0; i < cookieTokens.length; i++) {
            sb.append(cookieTokens[i]);

            if (i < cookieTokens.length - 1) {
                sb.append(WsContants.DELIMITER);
            }
        }

        String value = sb.toString();

        sb = new StringBuffer(new String(Base64.encodeBase64(value.getBytes())));

        while (sb.charAt(sb.length() - 1) == '=') {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

	
}
