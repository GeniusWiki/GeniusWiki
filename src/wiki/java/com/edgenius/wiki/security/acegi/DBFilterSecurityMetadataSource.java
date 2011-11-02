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

import java.util.Collection;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;

import com.edgenius.wiki.security.Policy;
import com.edgenius.wiki.security.service.SecurityService;

/**
 * @author Dapeng.Ni
 * This class change back to simple url matcher: security policy change lots, please refer history in 
 * SVN to see its brilliant history: which support URI, to parameter exactly match ect.
 * 
 */
public class DBFilterSecurityMetadataSource implements FilterInvocationSecurityMetadataSource{
	
	//properties
	private boolean convertUrlToLowercaseBeforeComparison = false;
	private SecurityService securityService;


	@Override
	public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        if ((object == null) || !this.supports(object.getClass())) {
            throw new IllegalArgumentException("Object must be a FilterInvocation");
        }

        String url = ((FilterInvocation) object).getRequestUrl();

        return lookupAttributes(url);
	}


	@Override
	public boolean supports(Class<?> clazz) {
		return FilterInvocation.class.isAssignableFrom(clazz);
	}

	@Override
	public Collection<ConfigAttribute> getAllConfigAttributes() {
		return null;
	}
	/**
	 * 
	 */
	public Collection<ConfigAttribute> lookupAttributes(String url) {
		//for performance reason: skip resource checking
		if(url != null && (url.endsWith(".png") || url.endsWith(".gif") || url.endsWith(".jpg")  
				|| url.endsWith(".js") || url.endsWith(".css") || url.endsWith(".rpc") || url.endsWith(".rpcs")
				|| url.endsWith(".html") || url.endsWith(".htm")))
			return null;
		
		if (convertUrlToLowercaseBeforeComparison) {
			url = url.toLowerCase();
		}

		Policy policy = securityService.findUrlPolicy(url);
		if(policy != null)
			return policy.getAttributeDefinition(); 

		return null;
	}



	//********************************************************************
	//               Set / Get
	//********************************************************************
	public void setConvertUrlToLowercaseBeforeComparison(
			boolean convertUrlToLowercaseBeforeComparison) {
		this.convertUrlToLowercaseBeforeComparison = convertUrlToLowercaseBeforeComparison;
	}
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

}
