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

import java.util.ArrayList;
import java.util.List;


/**
 * @author Dapeng.Ni
 */
public class StringUtil {
	private static final String WORD="abcdefghijklmnopqrstuvwxyz";
	/**
	 * find char index of sep, but not start with  odd number '\' 
	 */
	public static int indexSeparatorWithoutEscaped(String str, String sep){
		int start = 0;
		int idx;
		boolean found = false;
		do {
			idx = indexOf(str, sep, start);
			if(idx == 0){
				//line of start
				found = true;
				break;
			}
			if (idx != -1) {
				int count=0;
				for(int reIdx =idx-1;reIdx>=0;reIdx--){
					if((str.charAt(reIdx) != '\\')){
						break;
					}
					count++;
				}
				if(count % 2 == 0){
					found = true;
					break;
				}
			}
			start = idx+1;
		} while (idx != -1);
		
		return found?idx:-1;
	}
	
	/**
	 * @return
	 */
	public static String[] splitWithoutEscaped(String str, String sep) {
		if(str == null || "".equals(str))
			return new String[]{""};
		
		List<String> list = new ArrayList<String>();
		int end = indexSeparatorWithoutEscaped(str, sep);
		while(end != -1){
			list.add(str.substring(0,end));
			str = str.substring(end + sep.length()); 
			end = indexSeparatorWithoutEscaped(str, sep);
		}
		list.add(str);
		
		return list.toArray(new String[list.size()]);
	}
    public static int indexOf(String str, String searchChar, int startPos) {
        if (isEmpty(str)) {
            return -1;
        }
        return str.indexOf(searchChar, startPos);
    }
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    public static boolean isBlank(String str) {
    	return str == null || str.trim().length() == 0;
    }

    /**
     * If str contains any char in exist char list. Case sensitive
     * @param str
     * @param exist
     * @return List of invalid characters, or zero size array if no exist.
     */
    public static char[] containsAny(String str, char[] exist) {
    	char[] ret = new char[0];
    	if(str == null || str.length() ==0) return ret;
    	
    	char[] strc = str.toCharArray();
    	char[] invalid = new char[strc.length];
    	int size =0;
    	for (char c : exist) {
			if (contains(strc, c)){
				invalid[size++] = c;
			}
		}
    	
    	if(size > 0){
    		ret = new char[size];
			System.arraycopy(invalid, 0, ret, 0, size);
    	}
    	return ret;
    }
	/**
	 * Check if <code>chars</code> contains <code>exist</code> 
	 * @param chars
	 * @param exist
	 * @return
	 */
    public static boolean contains(char[] chars, char exist) {
		boolean found = false;
		for(char c:chars){
			if(c == exist){
				found = true;
				break;
			}
		}
		return found;
	}
	/**
	 * Check if <code>container<code> contain input string and ignore case.
	 * @param container
	 * @param input
	 * @return
	 */
	public static boolean containsIgnoreCase(String[] container, String input){
		if(container == null || container.length == 0 || input == null)
			return false;
		
		for (String str : container) {
			if(input.equalsIgnoreCase(str))
				return true;
		}
		return false;
	}

	/**
	 * If given string end with any of string by given array, return true; 
	 * @param urlStr
	 * @param expectations
	 * @return
	 */
	public static boolean endOfAny(String str, String[] expectations) {
		if ((str == null) || (expectations == null)) {
			return false;
		}
		for (String search : expectations) {
			if (search == null) {
				continue;
			}
			if (str.endsWith(search)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * @param text
	 * @param strings
	 * @return
	 */
	public static boolean startOfAny(String str, String[] expectations) {
		if ((str == null) || (expectations == null)) {
			return false;
		}
		for (String search : expectations) {
			if (search == null) {
				continue;
			}
			if (str.startsWith(search)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Remove any continuous spaces inside given string. 
	 * This method also trim the string. It doesn't remove any other invisible character except space.
	 * @param src
	 * @return
	 */
	public static String shrinkSpaces(String src) {
		if (src ==null)
			return src;
		
		src = src.trim();
		char[] charr = src.toCharArray();
		int len = charr.length;
		StringBuilder buf = new StringBuilder();
		boolean foundSpace = false;
		for(int idx=0;idx<len;idx++){
			char ch = charr[idx];
			if(ch == ' '){
				if(foundSpace)
					continue;
				foundSpace = true;
			}else{
				foundSpace = false;
			}
			buf.append(ch);
		}
		
		return buf.toString();
	}

	/**
	 * @param src
	 * @return
	 */
	public static String trim(String src) {
		return src ==null?"":src.trim();
	}

	/**
	 * 
	 * Same with java.utils.Patter.compile("\w"): [a-z][A-Z][0-9]
	 * Here does not user String.matches() is only for performance reason...
	 * @param ch
	 * @return
	 */
	public static boolean isWord(char ch) {
		if(NumberUtil.isDigit(ch))
			return true;
		
		char lch = Character.toLowerCase(ch);
		return WORD.indexOf(lch) != -1;
	}

	/**
	 * @param name
	 * @param align
	 * @return
	 */
	public static boolean equalsIgnoreCase(String str1, String str2) {
		  return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
	}

	public static boolean equals(String str1, String str2) {
		return str1 == null ? str2 == null : str1.equals(str2);
	}

	/**
	 * This method does not trim control character such as \r or \n etc.
	 * @param string
	 * @return
	 */
	public static String trimEndSpace(String string) {
		if(string == null)
			return null;
		
		int maxIdx = string.length()-1;
		int end=0;
		//does not trim control character such as \r or \n etc. so, here use == ' ' rather than <= ' '
		for(int idx=maxIdx;idx>=0 && string.charAt(idx) == ' ';idx--){
			end++;
		}
		if(end > 0)
			return string.substring(0,maxIdx-end + 1);
		
		return string;
	}
	
	public static String trimStartSpace(String string) {
		if(string == null)
			return null;
		
		int maxIdx = string.length()-1;
		int start=0;
		//does not trim control character such as \r or \n etc. so, here use == ' ' rather than <= ' '
		for(int idx=0;idx<maxIdx && string.charAt(idx) == ' ';idx++){
			start++;
		}
		if(start > 0)
			return string.substring(start);
		
		return string;
	}


	/**
	 * @param text
	 * @return
	 */
	public static String trimToEmpty(String text) {
		return text==null?"":text.trim();
	}
	/**
	 * @param value
	 * @return
	 */
	public static String trimToNull(String text) {
		return text==null?null:text.trim();
	}
	/**
	 * @param string
	 * @return
	 */
	public static String nullToEmpty(String str) {
		return str==null?"":str;
	}

	/**
	 * Join string by given separator string. Treat it as empty if joined string is null.
	 * @param separator
	 * @return
	 */
	public static String join(String separator, String ... joinedStr) {
		if(joinedStr == null || joinedStr.length == 0)
			return "";
		if(separator == null)
			separator = "";
		
		StringBuffer buf = new StringBuffer();
		for (String str : joinedStr) {
			if(str == null) str = "";
			buf.append(str).append(separator);
		}
		buf.delete(buf.length()-separator.length(), buf.length());
		
		return buf.toString();
	}

	/**
	 * @param text
	 * @return true if the given string is null or only include ' ' 
	 */
	public static boolean isSpacesOnly(String text) {
		if(text == null)
			return true;
		int len = text.length();
		for(int idx=0;idx<len;idx++){
			if(text.charAt(idx) != ' ')
				return false;
		}
		return true;
	}

	/**
	 * NullPoint safe.
	 * @param str
	 * @param start
	 * @return
	 */
	public static boolean startWith(String str, String start) {
		if(str == null)
			return false;
		
		return str.startsWith(start);
	}


}
