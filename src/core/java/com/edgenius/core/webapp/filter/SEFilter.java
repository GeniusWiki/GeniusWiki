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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import com.edgenius.core.Constants;
import com.edgenius.core.Global;
import com.edgenius.core.util.WebUtil;

/**
 * Search engine filter - to disable JSessionID=xxxx
 * see http://www.digitalsanctuary.com/tech-blog/general/jboss-jsessionid-parameter-remove.html
 * @author Dapeng.Ni
 */
public class SEFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(SEFilter.class);
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		
		if(Global.PublicSearchEngineAllow){
			boolean robot = WebUtil.isPublicSearchEngineRobot(request.getHeader("User-Agent"));
			if(robot){
				log.info("SE [{}] agent is coming...", request.getHeader("User-Agent"));
				request.setAttribute(Constants.SE_ROBOT, Boolean.TRUE);
				//use SEResponseWrapper to disable JSessionID=xxxx
				chain.doFilter(request, new SEResponseWrapper(response));
				return;
			}
		}
		
		chain.doFilter(request, response);
        
	}

}
