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
package com.edgenius.core.webapp.filter;

import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.edgenius.core.Constants;
import com.edgenius.core.Global;
import com.edgenius.core.UserSetting;
import com.edgenius.core.model.User;
import com.edgenius.core.service.LocaleContextConfHolder;
import com.edgenius.core.service.UserReadingService;
/**
 * LocalFilter request to preferred locale according to user option in database, client browser locale or default locale.
 * The proity is user setting in database is highest, then browser locale, then the default locale. 
 * <p> 
 * This filter also setup Text Direction and TimeZone.
 * 
 * @author Dapeng.Ni
 * 
 */
public class LocaleFilter extends OncePerRequestFilter {
	/**
	 * 
	 */
	private static final String PREFERRED_LOCALE = "PreferredLocale";

//	private static final Logger log = LoggerFactory.getLogger(LocaleFilter.class);
	
	private String encoding;
	
	private UserReadingService userReadingService = null;
	/**
	 * Set the encoding to use for requests. This encoding will be
	 * passed into a ServletRequest.setCharacterEncoding call.
	*/
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
            FilterChain chain) throws IOException, ServletException {
//    	if(log.isDebugEnabled()){
//    		log.debug("Request URL: " + request.getRequestURI());
//    	}
    	
    	//charset encoding
    	if(!StringUtils.isEmpty(this.encoding))
    		request.setCharacterEncoding(encoding);
    	else
    		request.setCharacterEncoding(Constants.UTF8);
    	
    	String direction = null;
    	Locale preferredLocale = null;
    	TimeZone timezone = null;
    	HttpSession session = request.getSession(false);
    	if(getUserService() != null){ //for Install mode, it will return null
	    	User user = getUserService().getUserByName(request.getRemoteUser());
	    	if(user != null && !user.isAnonymous()){
	    		//locale
	    		UserSetting set = user.getSetting();
	    		String userLang = set.getLocaleLanguage();
	    		String userCountry = set.getLocaleCountry();
	    		if(userLang != null && userCountry != null){
	    			preferredLocale = new Locale(userLang,userCountry);
	    		}
	    		//text direction in HTML 
	    		direction = set.getDirection();
	    		//timezone
	    		if(set.getTimeZone() != null)
	    			timezone = TimeZone.getTimeZone(set.getTimeZone());
	    	}
    	}
    	if(preferredLocale == null){
    		if(Global.DetectLocaleFromRequest){
	    		Locale locale = request.getLocale();
	    		if(locale != null){
	    			preferredLocale = locale;
	    		}
    		}
    		if(preferredLocale == null){
    			preferredLocale = Global.getDefaultLocale();
    		}
    	}
    		
    	if(direction == null){
    		direction = Global.DefaultDirection;
    	}
    	
    	if(timezone == null){
    		if (session != null) {
    			//try to get timezone from HttpSession, which will be intial set in SecurityControllerImpl.checkLogin() method
    			timezone = (TimeZone) session.getAttribute(Constants.TIMEZONE);
    		}
    		if(timezone == null)
    			timezone = TimeZone.getTimeZone(Global.DefaultTimeZone);
    	}
    	
        //set locale for STURTS and JSTL
        // set the time zone - must be set for dates to display the time zone
        if (session != null) {
            Config.set(session, Config.FMT_LOCALE, preferredLocale);
            session.setAttribute(Constants.DIRECTION, direction);
            Config.set(session, Config.FMT_TIME_ZONE, timezone);
        }
        
        
        //replace request by LocaleRequestWrapper
        if (!(request instanceof LocaleRequestWrapper)) {
            request = new LocaleRequestWrapper(request, preferredLocale);
            LocaleContextConfHolder.setLocale(preferredLocale);
        }
        
        
        if(chain != null){
        	request.setAttribute(PREFERRED_LOCALE,preferredLocale.toString());
        	chain.doFilter(request, response);
        }
        // Reset thread-bound LocaleContext.
        LocaleContextConfHolder.setLocaleContext(null);
	}

	private UserReadingService getUserService() {
		if (userReadingService == null) {
			ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
			try {
				userReadingService = (UserReadingService) ctx.getBean(UserReadingService.SERVICE_NAME);
			} catch (Exception e) {
				return null;
			}
		}
		return userReadingService;
	}

}
