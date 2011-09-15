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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.captcha.CaptchaServiceProxy;

import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;

/**
 * So far, this method only can handle normal Http request, it does not support GWT Ajax call becuase
 * the Captcha code does not contained in ServletRequest.getParameter(captchaValidationParameter);
 * 
 * @author Dapeng.Ni
 */
public class CaptchaValidationProcessingFilter implements InitializingBean, Filter {
    protected static final Logger logger = LoggerFactory.getLogger(CaptchaValidationProcessingFilter.class);
	public static final String ACEGI_SECURITY_CAPTCHA_VALID_FAILED = "ACEGI_CAPTCHA_VALID_FAILED";

    private CaptchaServiceProxy captchaService;
    private String captchaValidationParameter = "j_captcha_response";
    private String errorPage = "captcha.html";
    //********************************************************************
	//               methods
	//********************************************************************
  
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        String captchaResponse = request.getParameter(captchaValidationParameter);
//        if(true)
//        	throw new IOException("EOF");
        if ((request != null) && request instanceof HttpServletRequest && (captchaResponse != null)) {
            logger.debug("captcha validation parameter found");

            //get session
            HttpSession session = ((HttpServletRequest) request).getSession();

            if (session != null) {
                String id = session.getId();
                boolean valid = this.captchaService.validateReponseForId(id, captchaResponse);
                processVerify(request,response,chain, valid);
            } else {
                logger.debug("no session found, user don't even ask a captcha challenge");
            }
        } else{
        	 chain.doFilter(request, response);
        }

      
    }
    public void afterPropertiesSet() throws Exception {
        if (this.captchaService == null) {
            throw new IllegalArgumentException("CaptchaServiceProxy must be defined ");
        }
        if (StringUtils.isBlank(errorPage)) {
        	throw new IllegalArgumentException("errorPage must be defined ");
        }

        if (StringUtils.isBlank(captchaValidationParameter)) {
            throw new IllegalArgumentException("captchaValidationParameter must not be empty or null");
        }
    }

    /**
     * Does nothing. We use IoC container lifecycle services instead.
     */
    public void destroy() {}
    public void init(FilterConfig filterConfig) throws ServletException {}
    
    //********************************************************************
	//               private methods
	//********************************************************************
    private void processVerify(ServletRequest request, ServletResponse response, FilterChain chain, boolean valid)
		throws IOException, ServletException {

        if (valid) {
            //success
        	 chain.doFilter(request, response);
        } else {
        	//failed
    		if(((HttpServletRequest)request).getRequestURI().endsWith("j_spring_security_check")){
    			//as user login using Form submit style authentication rather than RPC's so that I have to do special handling here
    			//code ugly, it could be changed to RPC authentication in future.
    			response.getWriter().write(SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_HEADER_ERROR_CAPTCHA);
    		}else{
	            // Put exception into request scope (perhaps of use to a view)
	            ((HttpServletRequest) request).setAttribute(ACEGI_SECURITY_CAPTCHA_VALID_FAILED,true);
	
	            // Perform RequestDispatcher "forward"
	            RequestDispatcher rd = request.getRequestDispatcher(errorPage);
	        	rd.forward(request, response);
        	}	
        }
		
	}

	//********************************************************************
	//               set /get methods
	//********************************************************************
    public CaptchaServiceProxy getCaptchaService() {
        return captchaService;
    }

    public String getCaptchaValidationParameter() {
        return captchaValidationParameter;
    }


    public void setCaptchaService(CaptchaServiceProxy captchaService) {
        this.captchaService = captchaService;
    }

    public void setCaptchaValidationParameter(String captchaValidationParameter) {
        this.captchaValidationParameter = captchaValidationParameter;
    }
	public void setErrorPage(String errorPage) {
		this.errorPage = errorPage;
	}
}
