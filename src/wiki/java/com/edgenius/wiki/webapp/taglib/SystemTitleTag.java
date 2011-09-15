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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Global;
import com.edgenius.wiki.WikiConstants;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class SystemTitleTag  extends TagSupport {
	private static final Logger log = LoggerFactory.getLogger(SystemTitleTag.class);
	
	@Override
	public int doStartTag() throws JspException {
		JspWriter writer = pageContext.getOut();
		try {
			writer.print((StringUtils.isBlank(Global.SystemTitle)?"":Global.SystemTitle +"|") + WikiConstants.APP_NAME);
		} catch (IOException e) {
			log.error("Write system title failed.",e);
		}
		return TagSupport.SKIP_BODY;
	}

}
