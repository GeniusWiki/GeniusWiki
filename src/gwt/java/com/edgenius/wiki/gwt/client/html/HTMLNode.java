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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;


/**
 * @author Dapeng.Ni
 */
public class HTMLNode{
	//some markup ask must start from line start, but it won't create a new line if it already is in line start. the put this tag as flag
	//Please node, <linestart> will remove close following any spaces until found the non-blank text node or tag node
	//The reason is 
	//for example, list <ol>   <li>something</li></ol>,  li is with LINE_START_TAG tag, however, spaces between
	//<ol> and <li> are harmful (converted mark line start with space: "  # something), so remove blank here.
	public static final String LINE_START_TAG = "<linestart>";
	public static final String LINE_END_TAG = "<lineend>";

//	private static final Pattern tagNamePattern =  Pattern.compile("</?(\\w+)[\\s/>]");

	private HTMLNode pair;
	//only for Tag, null if node is pure textNode
	private String tagname = null;
	private Map<String, String> attributes = null;
	private Map<String, String> style = null;
	//clone of style, but only for external getStyle() usage
	private Map<String, String> unmodifiableStyle = null;


	private boolean textNode;
	//tag string start with "</" - close tag
	private boolean closeTag;
	//this may be text content or tag string according to above textNode flag
	private String text;
	
	HTMLNode next;
	HTMLNode previous;

	/**
	 * @param text
	 * @param isText
	 */
	public HTMLNode(String text, boolean isText) {
		reset(text,isText);
	}
	public String toString(){
		return text;
	}
	public HTMLNode next(){
		return next;
	}
	public HTMLNode previous(){
		return previous;
	}

	/**
	 * This mehthod will same with object equals 
	 */
	public boolean equals(Object obj){
		return this == obj;
	}
	public int hashCode(){
		return this.text==null?-1:this.text.hashCode();
	}
	/**
	 * Same with constructor method but won't create a new HTMLNode class instance
	 * @param htmlPrint
	 * @param isText
	 */
	public void reset(String text, boolean isText) {
		
		setTextNode(isText);
		this.text = text;
		if(!isText){
			//tag: need parse it to tag object
			parseTag(text);
		}else{
			tagname = null;
			attributes = null;
		}
	}
	/**
	 * Reset tag name only, keep them attributes
	 * @param tagName
	 */
	public void resetTagName(String newTagName) {
		//don't process text node
		if(textNode)
			return;
		if(closeTag){
			text = text.replaceFirst("</"+tagname, "</"+newTagName);
		}else{
			text = text.replaceFirst("<"+tagname, "<"+newTagName);
		}
		
		reset(text, false);
	}

	public String getText() {
		return text;
	}

	/**
	 * The any change on returned Map won't reflect backup to current tagString.
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}
	
	/**
	 * Return an unmodifiable map! 
	 */
	public Map<String, String> getStyle() {
		return unmodifiableStyle;
	}
	
	/**
	 * set attribute, for existing one, it will replace its old value with new.
	 * @param attrName
	 * @param attrValue
	 */
	public void setAttribute(String attrName, String attrValue){
		if(attributes == null)
			attributes = new HashMap<String, String>();

		attributes.put(attrName, attrValue);

		//rebuild HTML tag text
		text = HTMLUtil.buildTagString(tagname, null, attributes);
		
	}
	public String removeAttribute(String attrName){
		if(attributes == null)
			return null;
		
		String att = attributes.remove(attrName);
		if(att == null)
			return null;

		//rebuild HTML tag text
		text = HTMLUtil.buildTagString(tagname, null, attributes);
		
		return att;
	}
	/**
	 * Remove style from current tag string; 
	 * 
	 * 
	 * ---------------------BELOW comment are obsoleted as I change getSytle() return an unmodifiable map----------------
	 * Please note, the style will be reset from getStyle(); This means, if there is call before, such as, getStyle().remove('foo'), 
	 * the getStyle() may still get value getStyle().get('foo') after this method try to remove other style, ie, removeStyle("boo"); 
	 * Example, <tag style="foo:12;boo:34"/>
	 * 
	 * getStyle().remove("foo");
	 * getStyle().get("foo") == null;  //It is true! 
	 * removeStyle("boo");
	 * getStyle().get("foo") == "12";  //It is true! style array is reset by remvoeStyle();
	 * -------------------------------------------------------------------------------------------------------------------
	 * @param styleName
	 * @param value
	 * @return
	 */
	public String removeStyle(String styleName, String value){
		if(textNode || closeTag || style == null || style.size() == 0)
			return null;
		
		String ret = null;
		if(value == null || "*".equals(value)){
			ret = style.remove(styleName);
		}else if(StringUtil.equalsIgnoreCase(value,style.get(styleName))){
			//only remove special value style
			ret = style.remove(styleName);
		}

		if(style.size() == 0){
			style = null;
			unmodifiableStyle = null;
		}else{
			unmodifiableStyle = Collections.unmodifiableMap(style);
		}
			
		if(ret != null){
			//reset tagString
			String newStyle = HTMLUtil.buildStyleAttribute(style);
			if(!StringUtil.isBlank(newStyle))
				attributes.put(NameConstants.STYLE, newStyle);
			else
				attributes.remove(NameConstants.STYLE);
			text = HTMLUtil.buildTagString(tagname, null, attributes);
		}
		
		return ret;
		
	}
	
	public boolean isTextNode() {
		return textNode;
	}

	public void setTextNode(boolean textNode) {
		this.textNode = textNode;
	}
	
	public boolean isPaired(HTMLNode node) {
		if(this.tagname == null)
			return false;
		
		return node.closeTag != this.closeTag && this.tagname.equalsIgnoreCase(node.getTagName());
	}

	public void setPair(HTMLNode pair) {
		this.pair = pair;
	}
	
	public HTMLNode getPair() {
		return this.pair;
	}

	public boolean isCloseTag() {
		return closeTag;
	}

	public String getTagName() {
		return tagname;
	}
	
	/**
	 * Check whether any HTMLNode in given HTML Identifier list is contained in current node.
	 * For example, identifier could be <strong> and <span style="font-weight:bold">, if this node
	 * is <strong> or <span style="font-weight:bold">, then return true; 
	 * 
	 * @param htmlIDList
	 * @return
	 */
	public boolean isIdentified(List<HTMLNode> htmlIDList) {
		if(htmlIDList == null)
			return false;
		
		for (HTMLNode node : htmlIDList) {
			if(this.contains(node))
				return true;
		}
		
		return false;
	}

	/**
	 * This node must same tag name, and attributes contains given node attributes.
	 * TextNode always return false
	 * @param node
	 * @return
	 */
	public boolean contains(HTMLNode node) {
		//TextNode always return false
		if(node == null || node.isTextNode())
			return false;
		
		//if given node use * as tag name, it means any tag could be 
		if("*".equals(node.tagname) || StringUtil.equalsIgnoreCase(this.tagname,node.tagname)){
			if(node.getAttributes() != null){
				//check attributes
				if(this.getAttributes() == null){
					//ID node has some attribute, but this does not have any attribute, contains() must failed. 
					return false;
				}
				
				for (Entry<String, String> entry:node.getAttributes().entrySet()) {
					String value = this.getAttributes().get(entry.getKey());
					//TODO: so far, only simple compare value... no exactly, such as spaces handling
					if(NameConstants.STYLE.equalsIgnoreCase(entry.getKey())){
						//style contain - ie, style="text-algin: left"
						if(this.getStyle() == null && node.getStyle() != null){
							//normally node.getStyle() must be not null as its style attribute is not blank...
							return false;
						}
						if(node.getStyle() != null){
							//this node must contains all styles in given node
							for(Entry<String, String> styleEntry : node.getStyle().entrySet()){
								String styleV = this.getStyle().get(styleEntry.getKey());
								if(styleV == null){
									//some style required, but not exist in this node
									return false;
								}
								if(!"*".equals(styleEntry.getValue()) && !StringUtil.equalsIgnoreCase(styleV, styleEntry.getValue())){
									return false;
								}
							}
						}
					}else{
						if(!"*".equals(entry.getValue()) && !StringUtil.equalsIgnoreCase(value, entry.getValue())){
							return false;
						}
					}
				}
			}
			return true;
		}else{
			return false;
		}
	}

	//********************************************************************
	//               private method
	//********************************************************************

	private void parseTag(String text) {
		text = StringUtil.trimToEmpty(text);
		tagname = HTMLUtil.getTagName(text);
		//note: self close tag won't be close tag: text.endsWith("/>");
		closeTag = HTMLUtil.isCloseTag(text);
		if(tagname != null){
			attributes = HTMLUtil.parseAttributes(text);
			if(attributes != null && attributes.get(NameConstants.STYLE) != null){
				//parse CSS from style attribute, such as, "display:inline" etc.
				style = HTMLUtil.parseStyle(attributes.get(NameConstants.STYLE));
				if(style == null){
					unmodifiableStyle = style;
				}else{
					unmodifiableStyle = Collections.unmodifiableMap(style);
				}
			}
		}else{
			Log.warn("Unable parse out tag name from string " + text);
		}
		
	}

}
