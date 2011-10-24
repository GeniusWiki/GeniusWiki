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

import java.util.Arrays;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;

import com.edgenius.core.Global;

/**
 * @author Dapeng.Ni
 */
public class TokenBasedRememberMeServices extends org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices {
	private String integrationCookieKey;

	// same hardcode from
	// com.edgenius.wiki.integration.client.Authentication.REMEMBERME_COOKIE_KEY
	public static final String REMEMBERME_COOKIE_KEY = "integrationRememberMe";

	public UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request,
			HttpServletResponse response) {

		if (cookieTokens.length != 3) {
			throw new InvalidCookieException("Cookie token did not contain " + 2 + " tokens, but contained '"
					+ Arrays.asList(cookieTokens) + "'");
		}

		long tokenExpiryTime;

		try {
			tokenExpiryTime = new Long(cookieTokens[1]).longValue();
		} catch (NumberFormatException nfe) {
			throw new InvalidCookieException("Cookie token[1] did not contain a valid number (contained '"
					+ cookieTokens[1] + "')");
		}

		if (isTokenExpired(tokenExpiryTime)) {
			throw new InvalidCookieException("Cookie token[1] has expired (expired on '" + new Date(tokenExpiryTime)
					+ "'; current time is '" + new Date() + "')");
		}

		// Check the user exists.
		// Defer lookup until after expiry time checked, to possibly avoid
		// expensive database call.

		UserDetails userDetails = getUserDetailsService().loadUserByUsername(cookieTokens[0]);

		// Check signature of token matches remaining details.
		// Must do this after user lookup, as we need the DAO-derived password.
		// If efficiency was a major issue, just add in a UserCache
		// implementation,
		// but recall that this method is usually only called once per
		// HttpSession - if the token is valid,
		// it will cause SecurityContextHolder population, whilst if invalid,
		// will cause the cookie to be cancelled.
		String expectedTokenSignature = makeTokenSignature(tokenExpiryTime, userDetails.getUsername(),
				userDetails.getPassword());

		if (!expectedTokenSignature.equals(cookieTokens[2])) {
			// NDPDNP - this is part of different with original code
			if (Global.webServiceEnabled || Global.restServiceEnabled) {
				// this is compare different key: it could come from
				// com.edgenius.wiki.integration.client.Authentication.login();
				expectedTokenSignature = makeTokenSignature(tokenExpiryTime, userDetails.getUsername(),
						userDetails.getPassword(), REMEMBERME_COOKIE_KEY);
				if (expectedTokenSignature.equals(cookieTokens[2])) {
					// Remove this login cookie immediately, so that
					// Authentication.login() won't be a "rememberMe" style login - we just implement login by this cookie but not rememberMe.
					cancelCookie(request, response);
					return userDetails;
				}
			}
			throw new InvalidCookieException("Cookie token[2] contained signature '" + cookieTokens[2]
					+ "' but expected '" + expectedTokenSignature + "'");
		}

		return userDetails;
	}

	/**
	 * Calculates the digital signature to be put in the cookie. Default value
	 * is MD5 ("username:tokenExpiryTime:password:key")
	 */
	protected String makeTokenSignature(long tokenExpiryTime, String username, String password, String key) {
		return DigestUtils.md5Hex(username + ":" + tokenExpiryTime + ":" + password + ":" + key);
	}

	public String getIntegrationCookieKey() {
		return integrationCookieKey;
	}

	public void setIntegrationCookieKey(String integrationCookieKey) {
		this.integrationCookieKey = integrationCookieKey;
	}
	
    public SecurityContext createEmptyContext() {
        return new SecurityContextImpl();
    }
}
