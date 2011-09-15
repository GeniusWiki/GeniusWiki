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
package com.edgenius.wiki.gwt.client.html;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.edgenius.wiki.gwt.client.server.utils.StringUtil;

/**
 * @author Dapeng.Ni
 */
public class HTMLUtil {
	public static final String TAG_CLOSE_SUFFIX = "/>";
	
	/**
	 * build tag string <tagname key1="value1" key2="value2" ...>content</tag> 
	 * If content is empty, only start tag return, ie, <tagname key="value">
	 * 
	 * Please note, if attribute value contains illegal value, please do necessary escape outside this method. 
	 * For example, customized wajax attribute must be escape before put into this method:
	 * <code>
	 * GwtUtils.escapeString(value,TAG_ESCAPED_CHARS)
	 * </code>
	 * @param attribute
	 * @return
	 */
	public static  String buildTagString(String tagname, String content,Map<String, String> attribute) {
		StringBuffer sb = new StringBuffer("<").append(tagname);
		if(attribute != null){
			for (Entry<String,String> entry : attribute.entrySet()) {
				if(!StringUtil.isBlank(entry.getValue())){
					sb.append(" ").append(entry.getKey().toLowerCase());
					sb.append("=\"").append(entry.getValue()).append("\"");
				}
			}
		}
		if(content == null){
			sb.append(">");
		}else{
			sb.append(">").append(content).append("</").append(tagname).append(">");
		}
		return sb.toString();
	}
	
	/**
	 * Build style part of HTML tag, ie,  style1:value1;style2:value2...
	 * @param styles
	 * @return
	 */
	public static String buildStyleAttribute(Map<String, String> styles){
		if(styles == null)
			return "";
		
		StringBuffer buf = new StringBuffer();
		for(Entry<String,String> entry:styles.entrySet()){
			buf.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
		}
		
		return buf.toString();
	}

	/**
	 * parse CSS from style attribute, such as, "display:inline" etc.
	 * @param string
	 * @return
	 */
	public static Map<String, String> parseStyle(String style) {
		if(style == null)
			return null;
		Map<String,String> map = new HashMap<String, String>();
		String[] values = style.split(";");
		for (String value : values) {
			String[] pair = value.split(":");
			if(pair.length == 2){
				map.put(pair[0].trim().toLowerCase(), pair[1].trim());
			}
			
		}
		return map;
	}

	/**
	 * Parse tag attribute <tagname key1="value1" key2="value2" ...> to Map with key and value pair.
	 * All attribute name would be trimmed and convert to lower case in this method
	 * @return
	 */
	public static HashMap<String, String> parseAttributes(String tagStr) {
		
		if(tagStr == null)
			return null;
		
		tagStr = tagStr.trim();
		
		//1 is length of "<"
		int tagSep = tagStr.indexOf(" ");
		if(tagSep == -1)
			return null;
		
		String attStr = tagStr.substring(tagSep).trim();
		if(attStr.endsWith(TAG_CLOSE_SUFFIX))
			//end with  "/>"
			attStr = attStr.substring(0,attStr.length()-TAG_CLOSE_SUFFIX.length()).trim();
		else
			//end with ">" 
			attStr = attStr.substring(0,attStr.length()-1).trim();
			
		
		int len = attStr.length();
		StringBuffer val = new StringBuffer();
		String key=null, value;
		int scope =0; //0, key; 1, first double quote, 2,value
		char req = ' ';
		
		HashMap<String,String> map = null;
		for(int idx=0;idx < len;idx++){
			char c = attStr.charAt(idx);
			if('=' == c && scope == 0){
				key = val.toString();
				val = new StringBuffer();
				scope = 1;
				continue;
			}else if(scope == 1){
				if('\"' == c || '\'' == c){
					req = c;
				}else{
					//this attribute does not embedded by quote
					req = ' ';
				}
				scope = 2;
				val.append(c);
				continue;
			}else if((scope == 2 && (req == c || idx == len -1))){
				val.append(c);
				value = val.toString();
				if(req != ' '){
					//could surrounding by quote, remove them from value
					value = value.substring(1,value.length()-1);
				}else{
					value = value.trim();
				}
				//initial here
				if(map == null)
					 map = new HashMap<String,String>();
				map.put(key.trim().toLowerCase(), StringUtil.trimToNull(value));
				scope = 0;
				val = new StringBuffer();
				continue;
			}
			val.append(c);
		}
		return map;
	}

	public static boolean isCloseTag(String tagStr){
		if(tagStr == null)
			return false;
			
		tagStr = tagStr.trim();
		//note: self close tag won't be close tag: text.endsWith("/>");
		return tagStr.startsWith("</");
			
	}

	/**
	 * Simple parse out end tag name such as "</abc>" or "<abc/>" or "<abc att=...>"then result should be "abc".
	 * @param tagStr
	 * @return
	 */
	public static String getTagName(String tagStr){
		if(tagStr == null)
			return null;
		
		tagStr = tagStr.trim();
		
		StringBuffer name = new StringBuffer();
		int len = tagStr.length();
		boolean found = false;
		for(int idx=0;idx<len;idx++){
			char ch = tagStr.charAt(idx);
			if((ch == '<' && idx==0) || (ch=='/' && idx==1))
				continue;
			
			//<* att=...> could be correct as it is wild card tag(any tagname
			boolean word = StringUtil.isWord(ch) || ch=='*';
			if(found && !word){
				//if char is not word, such as '>', '/' or space, then means tagname is complete
				return name.toString();
			}else if(!found && !word){
				//the possible third or second char is not valid word, mean tag is not valid, then return null
				return null;
			}else{
				name.append(ch);
				found = true;
			}
		}
		return null;
	}

}
