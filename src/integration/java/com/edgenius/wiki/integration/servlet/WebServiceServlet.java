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
package com.edgenius.wiki.integration.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Global;

/**
 * This is class is just for enable/disable webserive function according to Global.webServiceEnabled.
 * @author Dapeng.Ni
 */
public class WebServiceServlet extends CXFServlet{
	private static final long serialVersionUID = -6524820676284514727L;
	
	private static final Logger log = LoggerFactory.getLogger(WebServiceServlet.class);
    protected void invoke(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    	if(!Global.webServiceEnabled){
	    	try {
				response.getWriter().println("Webservice is disabled, please contact administrator.");
			} catch (IOException e) {
				log.error("Response error", e);
			}
    	}else{
    		super.invoke(request, response);
    	}
    }
}
