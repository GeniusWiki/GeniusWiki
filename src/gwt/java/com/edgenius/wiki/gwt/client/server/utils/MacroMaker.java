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
package com.edgenius.wiki.gwt.client.server.utils;



/**
 * This class is for sharing purpose for client and server side code.
 * 
 * @author Dapeng.Ni
 */
public class MacroMaker {
	public static void buildMessage(StringBuffer buffer, String title, String content, String type, String clz) {
		buffer.append("<div aid=\""+clz+"\" class=\"macroMessage\">");
		
		buffer.append("<div aid=\"norender\" class=\""+type+"\">");
		if(!StringUtil.isEmpty(title))
			buffer.append("<div aid=\"").append(NameConstants.TITLE).append("\" class=\"title\">").append(title).append("</div>");
		buffer.append("<div aid=\"").append(NameConstants.CONTENT).append("\">");
		buffer.append(content);
		buffer.append("</div></div></div>");
	}

	/**
	 * @param buffer
	 * @param title
	 * @param string
	 */
	public static void buildPanel(String panelID, StringBuffer buffer, String title, String content, boolean expanded) {
		buffer.append("<div class=\"macroPanel\" aid=\"panel\">");
		if(!StringUtil.isBlank(title)){
			buffer.append("<div aid=\"panelTitle\" class=\"title\">").append(title);
			buffer.append("<div class=\"macroPanelExpander").append(expanded?"":" collapsed")
			.append("\" name=\"").append(panelID).append("\"></div>");
			buffer.append("</div>");
		}
		
		buffer.append("<div aid=\"panelContent\" class=\"content\" id=\"").append(panelID).append("\">");
		buffer.append(content);
		buffer.append("</div></div>");
	}
	
}
