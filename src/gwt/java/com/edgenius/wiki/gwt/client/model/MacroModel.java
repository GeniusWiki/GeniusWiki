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
package com.edgenius.wiki.gwt.client.model;

import java.util.HashMap;
import java.util.Map;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.html.HTMLUtil;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;

/**
 * @author Dapeng.Ni
 */
public class MacroModel extends GeneralModel implements RenderPiece{
	public String macroName;

	public HashMap<String,String> values = new HashMap<String, String>();
	
	public String toString() {
		return " "+macroName+" ";
	}

	public static boolean isMacroModel(HTMLNode node){
		if(node.getAttributes() == null 
		 || StringUtil.isBlank(node.getAttributes().get(NameConstants.WAJAX)))
			return false;
		
		String wajax = node.getAttributes().get(NameConstants.WAJAX);
		Map<String, String> map = RichTagUtil.parseWajaxAttribute(wajax);
		String aname = map.get(NameConstants.ANAME);
		if(MacroModel.class.getName().equalsIgnoreCase(aname)){
			return true;
		}
		
		return false;
	}
	/**
	 * Parse the tag string and fill in macroName and values
	 */
	@Override
	public void fillToObject(String tagString, String enclosedText) {
		if(StringUtil.isBlank(tagString)){
			return;
		}
		
		HashMap<String,String> map = HTMLUtil.parseAttributes(tagString);
		if(map == null)
			return;
		
		//get wajax attribute value
		String wajax = map.get(NameConstants.WAJAX);
		if(!StringUtil.isBlank(wajax)){
			map = RichTagUtil.parseWajaxAttribute(wajax);
			String aname = map.remove(NameConstants.ANAME);
			//OK, ANAME match, then fill object
			if(MacroModel.class.getName().equalsIgnoreCase(aname)){
				this.macroName = map.remove(NameConstants.MACRO);
				this.values = map;
			}
		}
	}

	
	public String toRichAjaxTag() {

		Map<String,String> wajaxMap = new HashMap<String,String>();
		wajaxMap.put(NameConstants.MACRO,this.macroName);
		if(values != null)
			wajaxMap.putAll(values);
		
		String wajax = RichTagUtil.buildWajaxAttributeString(MacroModel.class.getName(),wajaxMap);
		
		//build tag
		Map<String,String> attributes = new HashMap<String,String>();
		//aid could be null(default) or com...UserFilter etc.
		attributes.put(NameConstants.AID, this.macroName);
		attributes.put(NameConstants.WAJAX, wajax);
		attributes.put(NameConstants.CLASS, "renderMacroMarkup mceNonEditable");
		
		return HTMLUtil.buildTagString("div","{"+this.macroName+"}", attributes);

	}
}
