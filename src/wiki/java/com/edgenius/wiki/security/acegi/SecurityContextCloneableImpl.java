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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextImpl;

/**
 * For ajax, simultaneously thread for one user usually happens. If only use same instance from HttpSession. AnonymousFilter will
 * set it as "null" finally after Filter. But this may cause other thread throw (usually is MethodSecurityInterceptor) throw
 * creidentialNotFoundException. For more detail see Acegi SEC-356. 
 *   
 * @author Dapeng.Ni
 */
public class SecurityContextCloneableImpl extends SecurityContextImpl implements Cloneable{
	private static final long serialVersionUID = -8035349708637186122L;
	private static final Logger log = LoggerFactory.getLogger(SecurityContextCloneableImpl.class);
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		//??? does shadow clone enough? 
		SecurityContext context = null;
		try {
			context = (SecurityContext) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return context;
	}

}
