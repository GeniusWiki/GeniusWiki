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

import com.allen_sauer.gwt.log.client.Log;


/**
 * Parse html string to ArrayList of HTMLNode.
 * @author Dapeng.Ni
 */
public class HtmlNodeListenerImpl implements HtmlListener {
	
	private HTMLNodeContainer nodeContainer = new HTMLNodeContainer();
	private StringBuffer cBuffer = new StringBuffer();

	//********************************************************************
	//               implementation methods
	//********************************************************************
	public void content(String content) {
		cBuffer.append(content);
		
	}

	public void endDocument() {
		
		if(cBuffer.length() != 0){
			HTMLNode node = new HTMLNode(cBuffer.toString(),true);
			nodeContainer.add(node);
		}
	}

	public void startDocument() {
		
	}

	public void tag(String tagStr) {
		if(cBuffer.length() != 0){
			HTMLNode node = new HTMLNode(cBuffer.toString(),true);
			nodeContainer.add(node);
			//reset for next String content
			cBuffer = new StringBuffer();
		}
		HTMLNode node = new HTMLNode(tagStr,false);
		//must before call node.previous() as it need set preview in container
		nodeContainer.add(node);
		
		//find paired tag...
		if(tagStr.trim().startsWith("</")){
			//find paired start tag, if not found, report warning and discard this tag (unexpected case!!!)
			//reverse lookup, until find possible start tag
			boolean hasPair = false;
			HTMLNode pair = node.previous();
			while(pair != null){
				//<ul><ul>aa</ul></ul>, check null is for last </ul>
				if(pair.isPaired(node) && pair.getPair() == null){
					pair.setPair(node);
					node.setPair(pair);
					hasPair = true;
					break;
				}
				pair = pair.previous();
			}
			if(!hasPair){
				Log.error("Unexpected case: end tag " + tagStr + " could not find paired start tag.");
			}
		}
		
	}

	public HTMLNodeContainer getHtmlNode() {
		return nodeContainer;
	}
	
}
