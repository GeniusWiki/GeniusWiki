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
package com.edgenius.core.webapp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.edgenius.core.util.ServletUtils;

/**
 * @author Dapeng.Ni
 */
public abstract class BaseServlet extends HttpServlet{
	@Override
	//- Just for warning: don't use this method in its inherit class, user doServer instead.
	@Deprecated 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,	IOException {
		//these are important to security validation:
		//see MethodSecurityInterceptor.invoke(); if request is null, security check will be skipped, it is not
		//expected in following pageService.uploadAttachments() method.
		ServletUtils.setRequest(request);
		ServletUtils.setServletContext(getServletContext());
		ServletUtils.setResponse(response);
		try {
			doService(request,response);
		}finally{
			ServletUtils.setServletContext(null);
			ServletUtils.setRequest(null);
			ServletUtils.setResponse(null);
		}
	}
	@Override
	//- Just for warning: don't use this method in its inherit class, user doServer instead.
	@Deprecated 
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
		//these are important to security validation:
		//see MethodSecurityInterceptor.invoke(); if request is null, security check will be skipped, it is not
		//expected in following pageService.uploadAttachments() method.
		ServletUtils.setRequest(request);
		ServletUtils.setServletContext(getServletContext());
		ServletUtils.setResponse(response);
		try {
			doService(request,response);
		}finally{
			ServletUtils.setServletContext(null);
			ServletUtils.setRequest(null);
			ServletUtils.setResponse(null);
		}
	}
	@Override
	//- Just for warning: don't use this method in its inherit class, user doServer instead.
	@Deprecated 
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//these are important to security validation:
		//see MethodSecurityInterceptor.invoke(); if request is null, security check will be skipped, it is not
		//expected in following pageService.uploadAttachments() method.
		ServletUtils.setRequest(request);
		ServletUtils.setServletContext(getServletContext());
		ServletUtils.setResponse(response);
		try {
			doService(request,response);
		}finally{
			ServletUtils.setServletContext(null);
			ServletUtils.setRequest(null);
			ServletUtils.setResponse(null);
		}
	}

	/**
	 * 
	 */
	protected abstract void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}
