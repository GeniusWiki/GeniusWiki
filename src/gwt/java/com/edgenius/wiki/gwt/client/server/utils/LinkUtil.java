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

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.model.LinkModel;

/**
 * This class method uses in both client and server side.<br>
 *  

 * 
 * @author Dapeng.Ni
 */
public class LinkUtil {

	//to make sure plugin UUID is always start from character
	private static final char UUID_PREFIX='a';
	private static final char CLINK_SEP=':';
	
	/**
	 * This link parse string [view>link#anchor@space] to LinkModel object.
	 * Originally, this function is only in LinkFilter, but as client RichEditor need such function as well. So extract 
	 * out this function to this class.<br>
	 * Note, input are plain text(no escape text), the output "view" will be HTMLEscape string.
	 * @param full
	 * @return
	 */
	public static LinkModel parseMarkup(String full){
		
		String view = null, link = null, anchor = null, extSpaceUname = null;
		int sep;
		// get view and link. Link and View can be separator >"
		if ((sep = StringUtil.indexSeparatorWithoutEscaped(full, ">")) != -1) {
			view = full.substring(0, sep);
			link = full.substring(sep + 1);
		} else {
			// in case: [onlyview], so link and view should same content
			view = full;
			//[^file_attachment_name] - remove ^ from view
			if(StringUtil.startWith(view,"^")){
				view = view.substring(1);
			}
			//DON'T remove escape at this point, as link will be parse further
			link = full;
		}
		
		int type = -1;//default
		if(StringUtil.startWith(link,"^")){
			//only find out this type - as after unescapeMarkupLink(), there is no way to distinguish if it is escaped or not.
			type = LinkModel.LINK_TO_ATTACHMENT;
		}
		
		//For file attachment link, it won't include extSpaceUname or Anchor
		if(!isExtLink(link) && type != LinkModel.LINK_TO_ATTACHMENT){
			// separate ext space it from "view" rather than from link
			if ((sep = StringUtil.indexSeparatorWithoutEscaped(link, "@")) != -1) {
				extSpaceUname = link.substring(sep + 1);
				link = link.substring(0, sep);
			}
	
			//separate anchor from link
			if ((sep = StringUtil.indexSeparatorWithoutEscaped(link, "#")) != -1) {
				anchor = link.substring(sep + 1);
				link = link.substring(0, sep);
			}
		}

		LinkModel model = new LinkModel();
		model.setView(EscapeUtil.unescapeMarkupLink(view));
		model.setLink(EscapeUtil.unescapeMarkupLink(link));
		model.setType(type);
		model.setSpaceUname(EscapeUtil.unescapeMarkupLink(extSpaceUname));
		model.setAnchor(EscapeUtil.unescapeMarkupLink(anchor));
		return model;
	}
	

	public static boolean isExtLink(String link){
		if(link != null){
			link = link.trim();
			return link.startsWith("http://") || link.startsWith("https://") || link.startsWith("ftp://")
			||link.startsWith("mailto:") || link.startsWith("news://") || link.startsWith("nntp://");
		}
		return false;
	}


	/**
	 * Build customized link token, which format looks like(double quote and + is not in String)
	 * "actionLen"+"token1Len:toke2Len: ... :tokenNLen"+"a(UUID_PREFIX)"+"action"+"Toke1Token2...TokenN"
	 * 
	 * @param linkPluginClz
	 * @param tokens
	 * @return
	 */
	
	public static String createCLinkToken(String action, String... tokens) {
		StringBuffer retSb =  new StringBuffer();
		StringBuffer sizeToken = new StringBuffer().append(action.length()).append(CLINK_SEP);
		StringBuffer sbToken = new StringBuffer();
		if(tokens != null && tokens.length > 0 ){
			for(String token:tokens){
				if(token ==null) token = "";
				sizeToken.append(token.length()).append(CLINK_SEP);
				sbToken.append(token);
			}
			retSb.append(sizeToken);
			retSb.append(UUID_PREFIX+action);
			retSb.append(sbToken.toString());
		}else{
			//only has PluginUUID
			retSb.append(UUID_PREFIX+action);
		}
		return retSb.toString();
	}


	/**
	 * Return String array, first element always is "action", then following token string
	 * "action" could be plugin class UUID, or client side javascript method name etc..
	 * @param params
	 * @return
	 */
	public static String[] parseCLinkParamters(String params) {
		if(StringUtil.isBlank(params))
			return null;
		
		try {
			int phase = 0, actionLen=0;
			StringBuffer uuid = new StringBuffer();
			StringBuffer sizeSb = new StringBuffer();
			String tokenStr = null; 
			
			List<Integer> sizeList = new ArrayList<Integer>(); 
			for(int idx=0;idx<params.length();idx++){
				char pchar = params.charAt(idx);
				if(phase ==0 && UUID_PREFIX != pchar){
					if(pchar == CLINK_SEP){
						int size = NumberUtil.toInt(sizeSb.toString(),-1);
						if(size != -1)
							sizeList.add(size);
						sizeSb = new StringBuffer();
					}else{
						sizeSb.append(pchar);
					}
				}else if(phase == 0 && UUID_PREFIX == pchar){
					phase =1;
					continue;
				}else if(phase == 1){
					//sum up UUID
					uuid.append(pchar);
					//first size is action length
					if(++actionLen == sizeList.get(0)){
						//phase 2: parameter
						if(idx < params.length())
							tokenStr = params.substring(idx+1);
						break;
					}
				}
			}
			
			List<String> retList = new ArrayList<String>();
			if(uuid.length() == sizeList.get(0))
				retList.add(uuid.toString());
			
			if(sizeList.size() > 1 && tokenStr != null){
				//split token by length
				for (int idx=1;idx<sizeList.size();idx++) {
					int size = sizeList.get(idx);
					if(size < tokenStr.length()){
						retList.add(tokenStr.substring(0,size));
						tokenStr = tokenStr.substring(size);
					}else{
						//size is equal over token string, put left string
						retList.add(tokenStr);
						tokenStr = "";
					}
				}
			}
			
			if(retList.size() > 0)
				return retList.toArray(new String[retList.size()]);
		} catch (Exception e) {
			Log.error("Failed on parasing paramters: " + params);
		}
	
		return null;
	}


	/**
	 * For href link &lt;a&gt; tag:
	 * This method assume URL in download?space=XX&uuid=XX&file=XX format will be in-site download file request.
	 * This is not 100% warranty if any URL occasionally use same format!  However, I don't use AID attribute to 
	 * identify this because users may copy URL from somewhere, such as attachment panel, repo macro etc. These
	 * places doesn't apply AID yet. 
	 * 
	 * For object link &lt;obj&gt; tag:
	 * This method assume in file 
	 * @param link
	 */
	public static boolean isAttachmentLink(String linkURL) {

		//from TinyMCE return, it start with "download?
		if(linkURL.indexOf("download?") >= 0 
				// don't add "&" before str, as it may be &amp;file=XX format
			&& linkURL.indexOf("space=") > 0 && linkURL.indexOf("uuid=") > 0  
			&& linkURL.indexOf("file=") > 0 ){
			return true;
		}
		return false;
		
	}


	/**
	 * @param link
	 * @return
	 */
	public static String getAttachmentFile(String linkURL) {
		int start = linkURL.indexOf("file=");
		if(start > 0){
			start +=5;
			int end = linkURL.indexOf("&",start);
			if(end == -1){
				return linkURL.substring(start);
			}else{
				return linkURL.substring(start, end);
			}
		}
		return null;
	}

//	public static void main(String[] args) {
//		String str1 = LinkUtil.createCLinkToken("userPopup", "admin",null,"administrator");
//		System.out.println(str1);
//		String[] str = LinkUtil.parseCLinkParamters("9:5:13:0:auserPopupadminadministrator");
//		System.out.println(str);
//	}
}
