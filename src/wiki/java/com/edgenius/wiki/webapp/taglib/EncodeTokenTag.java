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
package com.edgenius.wiki.webapp.taglib;

import java.net.URLEncoder;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;

/**
 * Encode string to Token format
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class EncodeTokenTag  extends TagSupport {
	private static final Logger log = LoggerFactory.getLogger(EncodeTokenTag.class);
	private String value;
	private String var;
	
	@Override
	public int doStartTag() throws JspException {
		try {
			JspWriter writer = pageContext.getOut();
			String encoded = URLEncoder.encode(EscapeUtil.escapeToken(value),Constants.UTF8);
			
			if(var != null){
				pageContext.setAttribute(var, encoded);
			}else{
				writer.println(encoded);
			}
		} catch (Exception e) {
			log.error("Unable escape value " + value + " to token",e);
		}
		return TagSupport.SKIP_BODY;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public void setVar(String var) {
		this.var = var;
	}

}
