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



import java.util.Arrays;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.UserModel;

/**
 * @author Dapeng.Ni
 */
public class GwtUtils {
	
	public static String renderErrorText(String errorText){
		//plus [ text ] : &#91 == [,  &#93 == ]
		 return "&#91;<span aid=\""+ SharedConstants.RENDER_ERROR_TAG + "\" class='renderError' hint='"+errorText+"'>" + errorText + "</span>&#93;"; 
	}
		

	public static boolean isAnonymous(UserModel user) {
		if (user == null)
			return true;

		return -1 == user.getUid();
	}

	/**
	 * Simple compare attribute with given value by bit and.
	 */
	public static boolean contains(int attribute, int value) {
		return (attribute & value) > 0 ? true : false;
	}
	
	public static String validateMatch(String text, String regex, String fieldName) {
		String es = null;
		if (text == null)
			return es;
		char[] chars = text.toCharArray();

		String invalid = "";
		for (int idx = 0; idx < chars.length; idx++) {
			if (!new String(new char[] { chars[idx] }).matches(regex)) {
				invalid += chars[idx];
			}
		}

		if (invalid.length() > 0) {
			es = Msg.params.invalid_char(fieldName, invalid);
		}
		return es;
	}

	public static String validateEmail(String text) {
		String es = null;
		if (text == null)
			return es;
		if (!text.trim().matches(
				"^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$")) {
			return Msg.params.invalid_input(Msg.consts.email());
		}
		return null;
	}

	public static String convertHumanSize(long byteSize) {
		//as JSON number does not accept minus value, so treat 0 as unknown
		if(byteSize == 0)
			return Msg.consts.unknown();
		
		String unit = "bytes";
		float size = (float) byteSize;
		if (size > 1024) {
			size = size / 1024;
			unit = "K";
		}
		if (size > 1024) {
			size = size / 1024;
			unit = "M";
		}
		if (size > 1024) {
			size = size / 1024;
			unit = "G";
		}
		String format = formatNumber(size, 1);
		if(format.endsWith(".0"))
			format = format.substring(0,format.length() -2);
		return  format + unit;
	}

	/**
	 * fragmentSize must greater 1. Otherwise please use int a = (int)f;
	 * 
	 * @param num
	 * @param fragmentSize
	 * @return
	 */
	public static String formatNumber(float num, int fragmentSize) {
		String ret = Float.toString(num);
		int len;
		if ((len = ret.indexOf(".")) != -1 && len + fragmentSize + 1 < ret.length()) {
			ret = ret.substring(0, len + fragmentSize + 1);
		}
		return ret;
	}

	/**
	 * This method copy from FileUtil.class
	 * @param fileName
	 * @return
	 */
	public static String getFileName(String fileName){
		if(fileName == null)
			return "";
			
		fileName = fileName.trim();
	
		int dotPos = fileName.lastIndexOf("/");
		int dotPos2 = fileName.lastIndexOf("\\");
		dotPos = Math.max(dotPos,dotPos2);
		if (dotPos == -1){
			return fileName;
		}
		return fileName.substring(dotPos + 1, fileName.length());
		
	}

	/**
	 * 
	 * Detect if the given string only contains valid character [\w\d], so that it can put into 
	 * URL parameters.
	 * <br>
	 * Note: Even set URLEncoding="UTF8" and LocalFilter setRequestEncoding() , they won't fix problem if URL parameter  has non-ascii code.
	 * so this method is useful to check if an URL allow has meaning URL format, i.e, http://foo.com/page.do?p=pageTitle&s=spaceUname
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isSupportInURL(String str) {
		if(StringUtil.isBlank(str))
			return true;
		
		//this pattern must exactly same with urlrewrite.xml pattern
		return str.matches("[\\w\\d- ]+");
	}
	/**
	 * Remove input string unit. such as 18px -> 18; 90% will keep %;
	 * @param string
	 * @return
	 */
	public static String removeUnit(String str) {
		if(StringUtil.isBlank(str))
			return str;
		
		StringBuilder sb = new StringBuilder();
		char[] chs = str.trim().toCharArray();
		for (char c : chs) {
			if(NumberUtil.isDigit(c) || c == '%'){
				sb.append(c);
			}else{
				break;
			}
		}
		
		return sb.toString();
	}
	/**
	 * Add unit, such as 19 -> 19px.
	 * @param str
	 * @return
	 */
	public static String addUnit(String str) {
		if(str == null)
			return "";
		str = str.trim();
		if(NumberUtil.isDigit(str.charAt(str.length()-1))){
			str += "px";
		}
		return str;
	}
	/**
	 * Parse Token(string after anchor # in URL), such as /page#/spaceUname/pageTitle#anchor.
	 * The input string should be after spaceUname/pageTitle#anchor. The anchor part will be discarded if it has.
	 *  
	 * @param token
	 * @return the tokens which is already unescaped.
	 */
	public static String[] parseToken(String token){
		
		token = StringUtil.trimToEmpty(token);
		if(token.length() == 0)
			return new String[]{""};
		
		if(token.charAt(0) == '/')
			token = token.substring(1);
		if(token.charAt(token.length()-1) == '/')
			token = token.substring(0,token.length()-1);
		
		String[] tokens = token.split("/");
		int pos;
		for (int idx = 0;idx<tokens.length; idx++) {
			if((pos = tokens[idx].indexOf("#")) != -1){
				//this token has anchor part,ie, pageTitle#anchor - only keep pageTitle part
				tokens[idx] = tokens[idx].substring(0, pos);
			}
			tokens[idx] = EscapeUtil.unescapeToken(tokens[idx]);
		}
		return tokens;
	}

	/**
	 * This method suppose ONLY possible one anchor exist in token. Aka, it can not be over 1 pageTitle#anchor string in token.
	 *   
	 * @param token
	 * @return
	 */
	public static String getAnchor(String token) {
		int pos;
		String anchor = null;
		if((pos = token.indexOf("#")) != -1){
			//this token has anchor part,ie, pageTitle#anchor - only keep pageTitle part
			anchor = token.substring(pos+1);
			if((pos = anchor.indexOf("/")) != -1){
				EscapeUtil.unescapeToken(anchor = anchor.substring(0,pos));
			}
		}
		return anchor;
	}
	public static String getToken(String[] tokens, int idx) {
		return (idx < tokens.length)?tokens[idx]:"";
	}
	/**
	 * @param tokenCreate
	 * @param linkSpaceUname
	 * @param string
	 * @param tokenName
	 * @return
	 */
	public static String buildToken(String actionIdentifier, String...params) {
		if(params == null)
			return "/"+actionIdentifier;
		if(actionIdentifier == null){
			if(params.length != 2){
				//this is unexpected case: Otherwise, this method has actionIdentifier, or it has space/page params
				Log.error("Misuse buildToken():" + actionIdentifier+"," + Arrays.toString(params) );
			}
			//View space page URL, /page#/pageTitle/space
			String spaceUname = params.length > 0 ? params[0]:null;
			String pageTitle = params.length > 1 ? params[1]:null;
			return getSpacePageToken(spaceUname, pageTitle);
			
		}
		if(!actionIdentifier.startsWith("$")){
			Log.error("Misuse buildToken():" + actionIdentifier+"," + Arrays.toString(params) );
		}
		String[] strs = new String[1+params.length];
		strs[0] = actionIdentifier;
		for(int idx=1;idx<strs.length;idx++){
			strs[idx] = EscapeUtil.escapeToken(params[idx-1]);
		}
		return "/" + StringUtil.join("/", strs);
		
			
	}
	/**
	 * Be careful: this method will encode spaceUname and pageTitle, so pageTitle can not take any valid token,
	 * such as "$CPAGE" etc. 
	 * @param spaceUname
	 * @param pageTitle
	 * @return
	 */
	public static String getSpacePageToken(String spaceUname, String pageTitle) {
		StringBuilder url = new StringBuilder();
		if(!SharedConstants.SYSTEM_SPACEUNAME.equals(spaceUname)){
			//do not need add spaceUname if it is default system space
			url.append(EscapeUtil.escapeToken(spaceUname)).append("/");
		}
		if (!StringUtil.isBlank(pageTitle)){
			url.append(EscapeUtil.escapeToken(pageTitle)).append("/");
		}
		
		int len = url.length();
		if(len == 0)
			return "";
		
		if(url.charAt(len-1) == '/')
			url.delete(len-1, len);
		
		return url.insert(0, "/").toString();
	}


	/**
	 * @param cpageSysadminNotify
	 * @return
	 */
	public static String getCPageToken(String cpageToken) {
		return SharedConstants.TOKEN_CPAGE + "/" + cpageToken;
	}
	public static String getUserPortraitHTML(String portraitUrl, String fullname, int size){
		StringBuffer buf = new StringBuffer("<img src=\"");
		buf.append(portraitUrl).append("\"");
		if(!StringUtil.isBlank(fullname)){
			buf.append(" title=\"").append(fullname).append("\"");
		}
		if(size > 0){
			buf.append(" width=\"").append(size).append("px\" height=\"").append(size).append("px\"");
		}
		buf.append(" class=\"portrait\">");
		return buf.toString();
	}
}

