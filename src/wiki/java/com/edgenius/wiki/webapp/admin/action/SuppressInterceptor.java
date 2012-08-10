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
package com.edgenius.wiki.webapp.admin.action;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants.SUPPRESS;
import com.edgenius.core.Global;
import com.edgenius.wiki.WikiConstants;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * STRUCTS Interecptor plugged into default Interecptor stack.
 * @author Dapeng.Ni
 */
public class SuppressInterceptor  extends AbstractInterceptor{
	private static final long serialVersionUID = -2765308334507911243L;
	private static final Logger log = LoggerFactory.getLogger(SuppressInterceptor.class);

	public String intercept(ActionInvocation invocation) throws Exception {
		
	    Map<String, Object> session = ActionContext.getContext().getSession();
	    
		Integer supVal = (Integer) session.get(WikiConstants.ATTR_SUPPRESS);
		if(supVal == null){
			supVal = Global.suppress;
			session.put(WikiConstants.ATTR_SUPPRESS, supVal);
		}
		Map<String, Object> params = ActionContext.getContext().getParameters();
		String[] suppresses = (String[]) params.get("suppress");
		if(suppresses != null && suppresses.length > 0){
			String suppress = suppresses[0];
			if(!StringUtils.isBlank(suppress)){
				String[] funcs = suppress.split(",");
				for (String func : funcs) {
					try {
						SUPPRESS sup = SUPPRESS.valueOf(func.toUpperCase());
						if(sup != null)
							supVal |= sup.getValue();
					} catch (Exception e) {
						log.error("Unexpected value in suppress parameters {}.", func);
					}
				}
				session.put(WikiConstants.ATTR_SUPPRESS, supVal);
			}
		}
		
		Global.setCurrentSuppress(supVal);
		return invocation.invoke();
	}

}
