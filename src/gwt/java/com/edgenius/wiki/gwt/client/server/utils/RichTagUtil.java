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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.html.HTMLNodeContainer;

/**
 * @author Dapeng.Ni
 */
public class RichTagUtil {

	private static final String WAJAX_SEP = "|";
	private static final String WAJAX_VAR_SEP = ":";
	public static final List<HTMLNode> NO_RENDER_TAGS = new ArrayList<HTMLNode>();
	static{
		RichTagUtil.NO_RENDER_TAGS.add(new HTMLNode("<* aid='"+SharedConstants.NO_RENDER_TAG+"'>",false));
		RichTagUtil.NO_RENDER_TAGS.add(new HTMLNode("<a title='Block this object with Adblock Plus'>",false));
	};

	//********************************************************************
	//               Following method are only available for Wajax attribute
	//********************************************************************
	/**
	 * Build attribute string: ANAME:attribute-name|key1:value1|key2:value2...
	 * Note:
	 * All attribute name is convert to upper case
	 * The string does not contain HTML attribute name part, wajax="(returned String)"
	 * 
	 * @param attName attribute-name
	 * @param map
	 * @return
	 */
	public static  String buildWajaxAttributeString(String attName, Map<String, String> map) {
		StringBuffer sb = new StringBuffer();
		sb.append(NameConstants.ANAME).append(WAJAX_VAR_SEP).append(EscapeUtil.escapeWajaxAttribute(attName)).append(WAJAX_SEP);
		if(map != null){
			for (Entry<String,String> entry : map.entrySet()) {
				if(entry.getValue() != null){
					sb.append(entry.getKey().toLowerCase());
					sb.append(WAJAX_VAR_SEP).append(EscapeUtil.escapeWajaxAttribute(entry.getValue())).append(WAJAX_SEP);
				}
			}
		}
		String att = sb.toString();
		if(att.endsWith(WAJAX_SEP))
			att = att.substring(0,att.length() -1 );
		return att;
	}
	/**
	 * Parse wajax attribute string to map. wajax attribute is created by buildWajaxAttributeString() method.
	 * @param tagStr : ANAME:attribute-name|key1:value1|key2:value2
	 * @return
	 */
	public static HashMap<String, String> parseWajaxAttribute(String tagStr){
		HashMap<String,String> map = new HashMap<String,String>();
		if(tagStr == null)
			return map;
		
		tagStr = tagStr.trim();
		
		String[] values = StringUtil.splitWithoutEscaped(tagStr, WAJAX_SEP);
		if(values == null){
			Log.warn("Unable to split to values pairs by | for " + tagStr);
			return map;
		}
		for (String pairStr : values) {
			String[] pair = StringUtil.splitWithoutEscaped(pairStr,WAJAX_VAR_SEP);
			if(pair.length != 2){
				Log.warn("Invalid key/value pair: " + pairStr);
				continue;
			}
			map.put(pair[0], EscapeUtil.unescapeWajaxAttribute(pair[1]));
		}
		
		return map;
	}

	/**
	 * @param nodeContainer
	 */
	public static void removeNoRenderTag(HTMLNodeContainer nodeContainer) {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// remove all tag which has id or aid is "norender"
		List<HTMLNode> removePairs = new ArrayList<HTMLNode>();
		boolean removed;
		for (Iterator<HTMLNode> iter = nodeContainer.iterator();iter.hasNext();) {
			HTMLNode node = iter.next();
			removed = false;
			//here try to close paired tag which must after open tag...
			for (Iterator<HTMLNode> pairIter = removePairs.iterator();pairIter.hasNext();) {
				if(pairIter.next() == node){
					iter.remove();
					pairIter.remove();
					removed = true;
					break;
				}
			}
			if(removed){
				//this node already removed from paired node list
				continue;
			}
			if(node.isIdentified(RichTagUtil.NO_RENDER_TAGS)){
				if(node.getPair() != null){
					removePairs.add(node.getPair());
				}
				iter.remove();
			}
		}
		if(removePairs.size() !=0 ){
			Log.error("Unexpected case: some 'norender' close tags are not removed from node list");
			if(Log.isDebugEnabled()){
				Log.debug("Unremoved paired " + Arrays.toString(removePairs.toArray()));
			}
		}
	}


}
