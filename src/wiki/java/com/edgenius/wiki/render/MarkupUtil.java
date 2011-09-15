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
package com.edgenius.wiki.render;

import org.apache.commons.lang.StringUtils;

import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;

/**
 * @author Dapeng.Ni
 */
public class MarkupUtil {
	public static final String HIDE_TOKEN= "HH";
	public static final Integer TEXT_KEY = -1;
	private static final String ENTITY_SLASH = "&#92;";
	/**
	 * Markup token has odd number leading slash "\", then convert this markup to <em>HTML entity</em>.
	 * @param input
	 * @return
	 */
	public static String escapeMarkupToEntity(String input) {
		if(input == null)
			return null;
		int len = input.length();
		StringBuffer sb = new StringBuffer();
		int slash = 0;
		int currLen;
		boolean odd;
		for(int idx=0;idx<len;idx++){
			char c = input.charAt(idx);
			if(c == '\\'){
				slash++;
				sb.append(c);
				continue;
			}
			
			odd = processDoubleSlash(sb, slash);
			
			//even ">" is not filter pattern keyword, but it use in markup link.
			// For example[view has \\> char>link],here must escape \> to entity, then in LinkFilter could correctly convert \&#(int >); 
			// to  ">", as it will call unescapeMarkupLink() to remove another "\" 
			if(StringUtils.contains(FilterRegxConstants.FILTER_KEYWORD+">", c) && odd){
				currLen = sb.length();
				sb.deleteCharAt(currLen-1);
				//delete last slash
				sb.append(EscapeUtil.toEntity(c));
			}else
				sb.append(c);
			
			slash = 0;	
		}
	
		//string end by some amount "\"
		if(slash > 0){
			odd = processDoubleSlash(sb, slash);
			//TDB:  how to handle last single "\"  to entity?
			if(odd){
				currLen = sb.length();
				sb.deleteCharAt(currLen-1);
				//delete last slash
				sb.append(ENTITY_SLASH);
			}
		}
		return sb.toString();
	}


	/**
	 * Add slash "\" before any markup keyword. The reverse method is escapeMarkupToEntity();
	 * 
	 * OK, this method looks complicated now(2009/05/05).  Because input escText may include some uniqueKey, which is replacement of HTML tag.
	 * These uniqueKey won't be calculated into leadNonWord or endNonWord,i.e., not as border. For example, 
	 * 
	 * my text uniqueK[text]uniqueK has key. (Originally, this text looks like "my text <p>[text]</p> has key.", P tag is replace into uniqueK)
	 * 
	 * Markup [text] won't be treat as surrounding by 'K' and 'u'.  It will be ' '. So the second parameter of this method, skippedTagKey, 
	 * will be skipped during processing.  
	 * 
	 * @param escText
	 * @return
	 */
	public static String escapeMarkupToSlash(String escText, String skippedTagKey) {
		//plus "\" as it is keyword for escape
		if(StringUtils.isBlank(escText))
			return escText;
		
		StringBuffer sb = new StringBuffer();
		int len = escText.length();
		
		//use \n as first start, means text start. 
		char lastCh = '\n';
		//text start, leadNonWord is true
		boolean leadNonWord = true;
		boolean endNonWord = true;
		
		char[] skipped = (skippedTagKey == null || skippedTagKey.length() == 0)? null: skippedTagKey.toCharArray();
		for(int idx=0;idx<len;idx++){
			if(skipped != null){
				//try to see if the following piece of string is skipped key or not. 
				//if it is skipped key, then just append key to result and continue.
				StringBuffer skipBuf = skipKey(escText, idx, skipped);
				if(skipBuf != null){
					sb.append(skipBuf);
					idx += skipBuf.length() -1;
					continue;
				}
			}
			
			char ch = escText.charAt(idx);
			if(StringUtils.contains(FilterRegxConstants.FILTER_ANYTEXT_KEYWORD, ch)){
				sb.append("\\").append(ch);
			}else if(leadNonWord && StringUtils.contains(FilterRegxConstants.FILTER_SURR_NON_WORD_KEYWORD, ch)){
				sb.append("\\").append(ch);
			}else if(lastCh == '\n' && StringUtils.contains(FilterRegxConstants.FILTER_ONLYLINESTART_KEYWORD, ch)){
				sb.append("\\").append(ch);
			}else{
				//check if this char is tailed by non-word character 
				//assume ch is last char, endNonWord is true then
				if(StringUtils.contains(FilterRegxConstants.FILTER_SURR_NON_WORD_KEYWORD, ch)){
					endNonWord = true;
					StringBuffer skipBuf = null;
					if(idx<len-1){
						//get next char to check if this non-word
						if(skipped != null){
							skipBuf = skipKey(escText, idx+1, skipped);
							if(skipBuf != null){
								idx += skipBuf.length();
							}
						}
						//need check idx again as it modified if skipBuf is not empty
						if(idx < len -1){
							char nextCh = escText.charAt(idx+1);
							endNonWord = FilterRegxConstants.NON_WORD_PATTERN.matcher(Character.valueOf(nextCh).toString()).matches();
						}
					}
					if(endNonWord){
						sb.append("\\").append(ch);
					}else{
						sb.append(ch);
					}
					if(skipBuf != null)
						sb.append(skipBuf);
				}else{
					sb.append(ch);
				}
			}
			leadNonWord = FilterRegxConstants.NON_WORD_PATTERN.matcher(Character.valueOf(ch).toString()).matches();
			lastCh = ch;
		}
		//next line is very rough escape, now replace by above exact escape
		//EscapeUtil.escapeBySlash(escText,(FilterRegxConstants.FILTER_KEYWORD+"\\").toCharArray());
		return sb.toString();
	}


	/**
	 * @param escText
	 * @param startIdx
	 * @param skipped
	 * @return
	 */
	private static StringBuffer skipKey(String escText, int startIdx, char[] skipped) {
		boolean skip = true;
		StringBuffer skipSb = new StringBuffer();
		int len = escText.length();
		int keyCount = 0;
		do{
			for(int keyIdx=0;keyIdx<skipped.length;keyIdx++){
				if(startIdx+keyIdx >= len || escText.charAt(startIdx+keyIdx) != skipped[keyIdx]){
					skip = false;
					break;
				}
			}
			if(skip){
				keyCount++;
				startIdx += skipped.length;
			}
		}while(skip);
		
		if(keyCount > 0){
			for(int idx=0;idx<keyCount;idx++)
				skipSb.append(skipped);
			return skipSb;
		}else
			return null;
	}


	/**
	 * Simply replace all filter keywords with leading odd number slash "\" by Hiding character, now it is "HH".
	 * It is useful while only detect if the valid filter exist or not, or get length Index value of valid filter (as the 
	 * replacement does not change the content length)
	 * @param text
	 * @return
	 */
	public static CharSequence hideEscapeMarkup(String input) {
		if(input == null)
			return new StringBuilder();
		
		int len = input.length();
		StringBuilder sb = new StringBuilder();
		int slash = 0;
		boolean odd;
		int currLen;
		for(int idx=0;idx<len;idx++){
			char c = input.charAt(idx);
			if(c == '\\'){
				slash++;
				sb.append(c);
				continue;
			}
			odd = slash % 2 !=0;
			
			//even ">" is not filter pattern keyword, but it use in markup link.
			// For example[view has \\> char>link],here must escape \> to entity, then in LinkFilter could correctly convert \&#(int >); 
			// to  ">", as it will call unescapeMarkupLink() to remove another "\" 
			if(StringUtils.contains(FilterRegxConstants.FILTER_KEYWORD+">", c) && odd){
				currLen = sb.length()-1;
				sb.deleteCharAt(currLen);
				//delete last slash
				sb.append(HIDE_TOKEN);
			}else
				sb.append(c);
			
			slash = 0;	
		}
	
		return sb;
	}


	/**
	 * @param sb
	 * @param slash
	 * @return
	 */
	private static boolean processDoubleSlash(StringBuffer sb, int slash) {
		int currLen;
		boolean odd = slash % 2 !=0;
		if(slash > 1){
			//if the string is like "\\\", then retrieve back from second last slash and convert double slash "\\" to single escape.
			//the last slash will handle in below
			currLen = sb.length();
			//for easy programming. delete all slash here, then append last slash after all double slash converted to HTML entity. 
			for(int sidx=1; sidx <= slash;sidx++){
				sb.deleteCharAt(currLen-sidx);
			}
			//see how many double slash 
			int doubleSlashCount = slash/2;
			for(int sidx=0;sidx<doubleSlashCount;sidx++){
				//replace escaped slash to &#92;
				sb.append(ENTITY_SLASH);
			}
			if(odd)
				sb.append("\\");
		}
		return odd;
	}

}
