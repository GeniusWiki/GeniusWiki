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
package com.edgenius.wiki.render.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.RenderContext;


/**
 * @author Dapeng.Ni
 */
public class BaseMacroParameter implements MacroParameter {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	//for example, {font:size=12}abc{font}, the value is start markup string,i.e., {font:size=12}
	//so far, this text is useful to display markup if it has errors - which need highlight and with hover text 
	private String startMarkup;
	
	//content between paired markup
	private String content;
	private String macroName;
	private Map<String,String> params = new HashMap<String, String>();
	private RenderContext renderContext;
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getStartMarkup() {
		return startMarkup;
	}
	public void setStartMarkup(String startMarkup) {
		this.startMarkup = startMarkup;
	}
	public Map<String, String> getParams() {
		return params;
	}
	public void setParams(String paramsStr) {
		//split params String to Map:
		params.clear();
		if(paramsStr != null){
			//split by separator "|"
			List<String> paramPairs = new ArrayList<String>();
			int idx; 
			String str = paramsStr.trim();
			while((idx = StringUtil.indexSeparatorWithoutEscaped(str,""+SEP)) != -1){
				paramPairs.add(str.substring(0,idx));
				str = str.substring(idx+1);
			}
			if(str.length() > 0)
				paramPairs.add(str);
			
			//then going to split by "="
			for (String pair : paramPairs) {
				String[] param = pair.split("=");
				if(param.length ==2){
					//lower-case param name, but keep original case for param value, but remove "slash" for macro parameter value
					params.put(param[0].toLowerCase(),EscapeUtil.unescapeMacroParam(param[1]));
				}else{
					log.error("Unable parse parameter "+ pair + " from macro " + this.getClass().getName());
				}
			}
		}
	}
	public void setParams(Map<String, String> params){
		this.params = params;
	}
	public String getParam(String paramName) {
		return params.get(paramName);
	}
	public RenderContext getRenderContext() {
		
		return renderContext;
	}
	public void setRenderContext(RenderContext renderContext) {
		this.renderContext = renderContext;
	}
	public int getParamsSize() {
		return params.size();
	}
	public String getMacroName() {
		return macroName;
	}
	public void setMacroName(String macroName) {
		this.macroName = StringUtils.lowerCase(macroName);
	}
	
	
}
