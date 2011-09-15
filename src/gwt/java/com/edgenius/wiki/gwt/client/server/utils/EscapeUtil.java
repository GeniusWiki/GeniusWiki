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

import java.util.LinkedHashMap;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;

/**
 * Basically, there are 3 kinds escape and unescape.<br>
 * HTML Entity, which format is "&#(int c);", can be display correctly in HTML browser. <br>
 * Token Entity, one character shorter than HTML enity ""&(int c);", but it won't display on HTML browser.<br> 
 * Slash Entity, add a slash before the character. The leading slash must be odd number.<br>
 * 
 * All entity must escape itself keyword as well, such as HTML/TOKEN need escape "&", and slash need escape "\".
 * 
 * <br>
 * There are lots place need do escape and unescape, following are scenarios:
 * 
 * <ul>
 * <li>Markup text need convert html keyword to HTML entity, such as ">" to &gt;" </li>
 * <li>Wajax attribute for rich tag (such as ajax link) need convert keyword to TokenEnity, such as "=" </li>
 * <li>GWT HyperLink token need convert to TokenEntity, such as "#"</li>
 * <li>Macro parameter value need convert Macro separator "|" to slash escape</li>
 * <li>Markup to rich render, if markup has slash escaped filter keyword, then conver it to HTML Entity</li>
 * <li>Rich to markup render, any markup filter keyword need do slash escape.</li>
 * <li>Portlet layout string use "$" as separator, then do Token Entity escape/unescape</li>
 * <li></li>
 * </ul> 
 * @author Dapeng.Ni
 */
public class EscapeUtil {

	private  static final char[] LINK_ESCAPED_CHARS = new char[]{'#','@','>','\\','^',']'};

	//| and : are used by WAJAX separator, & > < are used by HTML entity and tag. ' and " are used by attribute enclose characters
	private  static final char[] WAJAX_ATT_ESCAPED_CHARS = new char[]{'\\', '|', ':', '&', '<', '>', '\'', '"'};
	private final static Map<String,String> ESCAPED_TOKEN_CHARS = new LinkedHashMap<String,String>();
	private static final Map<String, String> WAJAX_ATT_ESCAPED_ENTITY_CHARS =  new LinkedHashMap<String,String>();
	
	static {
//		WAJAX_ATT_ESCAPED_ENTITY_CHARS.put("\\", toEntity('\\'));
//		WAJAX_ATT_ESCAPED_ENTITY_CHARS.put("|", toEntity('|'));
//		WAJAX_ATT_ESCAPED_ENTITY_CHARS.put(":", toEntity(':'));
		WAJAX_ATT_ESCAPED_ENTITY_CHARS.put("&", toEntity('&'));
		WAJAX_ATT_ESCAPED_ENTITY_CHARS.put("<", toEntity('<'));
		WAJAX_ATT_ESCAPED_ENTITY_CHARS.put(">", toEntity('>'));
		WAJAX_ATT_ESCAPED_ENTITY_CHARS.put("'", toEntity('\''));
		WAJAX_ATT_ESCAPED_ENTITY_CHARS.put("\"", toEntity('\"'));
	}

	static {
		//# is special for anchor, $ is special for token prefix, & is Entity, 
		//+ is treat as space, so here need convert it as well. 
		//% -- use in prefix of entity code. such as %20(space) etc
		// signle forward slash is divider of token
		ESCAPED_TOKEN_CHARS.put("#", toTokenEntity('#'));
		ESCAPED_TOKEN_CHARS.put("$", toTokenEntity('$'));
		ESCAPED_TOKEN_CHARS.put("&", toTokenEntity('&'));
		ESCAPED_TOKEN_CHARS.put("+", toTokenEntity('+'));
		ESCAPED_TOKEN_CHARS.put(" ", "+");   // must after "+"
		ESCAPED_TOKEN_CHARS.put("/", toTokenEntity('/'));
		ESCAPED_TOKEN_CHARS.put("%", toTokenEntity('%')); //must last
	}

	//********************************************************************
	//               method
	//********************************************************************
	/**
	 * Remove '\' from string, but only when '\' is before markup link "[view>link#anchor@space]" separator keyword "#", "@", ">"
	 * @param str
	 * @return
	 */
	public static String unescapeMarkupLink(String str){
		return removeSlashEscape(str, LINK_ESCAPED_CHARS);
	}
	public static String escapeMarkupLink(String str){
		return escapeBySlash(str, LINK_ESCAPED_CHARS);
	}

	/**
	 * Escape HTML to entity.  So far, only replace &,<, > and ".  
	 * This method <code>org.apache.common.lang.StringEscapeUtil.unescapeHMTL()<.code> to do unescape. To be note, the unescape method
	 * is unavailable for client side code. 
	 * @param input 
	 * 
	 */
	public static String escapeHTML(String input) {
		if(input ==null || input.trim().length() == 0)
			return input;
		
		StringBuffer sb = new StringBuffer();
		int len = input.length();
//		int slash = 0;
		//don't check if their have leading slash 
		////their before does not have odd number slash "\".
		//boolean even;
		for(int idx=0;idx<len;idx++){
			char c = input.charAt(idx);
//			if(c == '\\'){
//				slash++;
//				sb.append(c);
//				continue;
//			}
//			//zero or even number slash, then, do entity conversion
//			even = slash % 2 ==0;
			if(c == '&') // && even)
				sb.append("&amp;");
			else if(c == '<') // && even)
				sb.append("&lt;");
			else if(c == '>') // && even)
				sb.append("&gt;");
			else if(c == '"') // && even)
				sb.append("&quot;");
			else
				sb.append(c);
			
//			slash = 0;
		}
		
		return sb.toString();
	}
	/**
	 * If server side code, recommend use StringEscapeUtils.unescapeHtml(); 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static String unescapeHTML(String input) {
		if(input == null || input.trim().length() == 0)
			return input;
		
		StringBuffer result = new StringBuffer();
		int len = input.length();
		boolean foundEntity = false;
		String entity = "";
		for (int idx = 0; idx < len; idx++) {
			char c = input.charAt(idx);
			if (c == '&') {
				foundEntity = true;
				if(idx < len-1 && input.charAt(idx+1) == '#'){
					//skip '#'
					idx++;
				}
				continue;
			}
			if (!foundEntity) {
				result.append(c);
			}
			if (foundEntity && c == ';') {
				// convert entity to char
				try{
					char ec = (char) Integer.decode(entity).intValue();
					result.append(ec);
				}catch (Exception e) {
					//this may cause by name entity, such as &amp;
					if("amp".equalsIgnoreCase(entity)){
						result.append('&');
					}else if("lt".equalsIgnoreCase(entity)){
						result.append('<');
					}else if("gt".equalsIgnoreCase(entity)){
						result.append('>');
					}else if("quot".equalsIgnoreCase(entity)){
						result.append('"');
					}else if("nbsp".equalsIgnoreCase(entity)){
						result.append(' ');
					}else{
						Log.error("Unable convert HTML entity: " + entity);
					}
				}
				// reset for next entity
				entity = "";
				foundEntity = false;
				continue;
			}
			if (foundEntity) {
				entity += c;
			}
		}
	
		return result.toString();

	}

	/**
	 * Remove illegal character in HTML attribute (especial for wajax attribute). 
	 * @param att
	 * @return
	 */
	public static String escapeWajaxAttribute(String att){
		//WJAX uses in HTML tag, so won't use toTokenEntity, which includes "&" it will convert to &amp; after from rich editor
		//if simply use toEntity, it convert back to character again after from rich editor. only use "\" is bad as well
		//because HTMLParser cannot handle like this wajax="ab\"some \> char", so convert necessary char into entity as well...
		att = escapeBySlash(att, WAJAX_ATT_ESCAPED_CHARS);
		return escapeTo(att, WAJAX_ATT_ESCAPED_ENTITY_CHARS);
	}
	public static String unescapeWajaxAttribute(String att){
		att = unescapeHTML(att);
		return removeSlashEscape(att, WAJAX_ATT_ESCAPED_CHARS);
	}
	/**
	 * This method is limited only for GWT Token escape, but also TOKEN ENTITY type escape, see this class jdoc.
	 * 
	 * If url include "#" (anchor),"$"(Token prefix),"&"(Entity prefix) will
	 * convert to Entity code. 
	 * @param url
	 * @return
	 */
	public static String escapeToken(String url){
		return escapeTo(url,ESCAPED_TOKEN_CHARS);
	}
	/**
	 * @param substring
	 * @return
	 */
	public static String unescapeToken(String url) {
		StringBuffer result = new StringBuffer();
		int len = url.length();
		boolean foundEntity = false;
		String entity = "";
		for (int idx = 0; idx < len; idx++) {
			char c = url.charAt(idx);
			if(c=='+'){
				result.append(" ");
				continue;
			}
			if (!foundEntity && c != '&') {
				result.append(c);
			}
			if (c == '&') {
				foundEntity = true;
				continue;
			}
			if (foundEntity && c == ';') {
				// convert entity to char
				try{
					char ec = (char) Integer.decode(entity).intValue();
					result.append(ec);
				}catch (Exception e) {
					//this may cause by name entity, such as &amp;
					try {
						result.append(unescapeHTML(entity));
					} catch (Exception e2) {
						result.append("&"+entity+";");
					}
				}
				// reset for next entity
				entity = "";
				foundEntity = false;
				continue;
			}
			if (foundEntity) {
				entity += c;
			}
		}
	
		return result.toString();
	}


	/**
	 * Macro parameter separate by "|" (MacroParameter.SEP), if parameter has "|" then
	 * it has to be escaped by "\".  Further, "\" need escape in double "\\" 
	 * @param param
	 * @return
	 */
	public static String escapeMacroParam(String param){
		
		return escapeBySlash(param, new char[]{SharedConstants.MACRO_PARAM_SEP,'\\'});
	}
	/**
	 * Unescape macro parameter from slash "\" escape.
	 * @param param
	 * @return
	 */
	public static String unescapeMacroParam(String param){
		return removeSlashEscape(param, new char[]{SharedConstants.MACRO_PARAM_SEP,'\\'});
	}


	/**
	 * put "\" before the <code>needEscape</code>
	 * @param str
	 * @param needEscape
	 */
	public static String escapeBySlash(String str, char[] needEscape){
		if(str == null)
			return str;
		
		StringBuffer sb = new StringBuffer();
		int len = str.length();
		
		for(int idx=0;idx<len;idx++){
			char c = str.charAt(idx);
			if(StringUtil.contains(needEscape,c)){
				//this char need do escape
				sb.append("\\");
			}
			sb.append(c);
		}
		return sb.toString();
	}
	
	/**
	 * Remove any odd slash character from string.
	 * @param str
	 * @return
	 */
	public static String removeSlashEscape(String str){
		return removeSlashEscape(str, null);
	}
	/**
	 * Remove '\' from string, but only when '\' is before <code>beforeChars</code>
	 * The first character after slash will be keep and it won't be treat new function splash even if it is slash.
	 * For example, \\\'  will to \'  
	 * @param str
	 * @return
	 */
	public static String removeSlashEscape(String str, char[] beforeChars){
		if(str == null)
			return str;
		
		int start = 0;
		int idx;
		StringBuffer sb = new StringBuffer(str);
		int len = sb.length();
		do {
			
			idx = sb.indexOf("\\",start);
			start = idx + 1;
			if(start == len)
				//line of end
				break;
			if (idx != -1) {
				char append = sb.charAt(start);
				if(beforeChars == null || StringUtil.contains(beforeChars, append)){
					sb.deleteCharAt(idx);
					len--;
					if(start == len) break;
				}
			}
		} while (idx != -1);
		
		return sb.toString();
	}

	

	/**
	 * Escape character into HTML Entity which could display correct on HTML browser.
	 */
	public static String toEntity(int c) {
		return "&#" + c + ";";
	}
	
	
	//********************************************************************
	//               private method
	//********************************************************************
	/**
	 * This is just for replace some character into "&'(int)c';" format, which COULD NOT display on browser.  And 
	 * use unescapeToken() method to go back its original character. 
	 * @param c
	 * @return
	 */
	private static String toTokenEntity(int c) {
		return "&" + c + ";";
	}
	

	/**
	 * Convert char to entity if given input has this char in escaped char list
	 * @param input
	 * @param escapedChars
	 * @return
	 */
	private static String escapeTo(String input,Map<String,String> escapedChars) {
		if(input == null){
			return "";
		}
		
		StringBuffer result = new StringBuffer();
		
		int len = input.length();
		for (int idx = 0; idx < len; idx++) {
			String c = Character.valueOf(input.charAt(idx)).toString();
			if (escapedChars.containsKey(c)) {
				result.append(escapedChars.get(c));
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}
}
