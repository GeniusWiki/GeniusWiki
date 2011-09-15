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
package com.edgenius.wiki.render.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.util.StringEscapeUtil;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.html.HTMLNodeContainer;
import com.edgenius.wiki.gwt.client.html.HtmlNodeListenerImpl;
import com.edgenius.wiki.gwt.client.html.HtmlParser;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.Filter;
import com.edgenius.wiki.render.FilterPipe;
import com.edgenius.wiki.render.MarkupUtil;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.render.RichRenderEngine;
import com.edgenius.wiki.render.filter.MacroFilter;
import com.edgenius.wiki.util.WikiUtil;

/**
 * This render engine input HTML content and return Markup content
 * @author Dapeng.Ni
 */
public class RichRenderEngineImpl implements RichRenderEngine{
	public static final Logger log = LoggerFactory.getLogger(RichRenderEngineImpl.class);

	private FilterPipe filterProvider;

	public RichRenderEngineImpl(FilterPipe filterProvider){
		this.filterProvider = filterProvider;
	}
	/**
	 * Input HTML return Markup. RenderContext only contains spaceUname
	 */
	public String render(String htmlText, RenderContext context) {
		long start = System.currentTimeMillis();
		
		htmlText = htmlText.replaceAll("\\r", "");
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Parse html text into HTMLNode list
		HtmlNodeListenerImpl listener = new HtmlNodeListenerImpl();
		HtmlParser htmlParser = new HtmlParser();
		htmlParser.scan(htmlText, listener);
		//get HTML node list
		HTMLNodeContainer nodeContainer = listener.getHtmlNode();
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// remove all tag which has id or aid is "norender"
		RichTagUtil.removeNoRenderTag(nodeContainer);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//after TinyMCE, it may contain some useless or duplicated HTML tag, 
		//such tag surrounding empty, or <font size=bb><font size=cc>some text</font></font> etc.
		//here will remove such useless and merge duplicated HTML tag
		optimizeHTML(nodeContainer);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// all Text node need do HTMLEscape and markup escape conversion,e.g, &gt; to >, *bold* to \*bold\* etc.
		//in this method, nodeContainer may be re-initialized to a new instance.
		nodeContainer = escapeText(htmlText, nodeContainer);		
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//finally, arrive here... use all available filters to convert all html tag node to markup
		List<Filter> filters = filterProvider.getFilterList();
		for (Filter filter : filters) {
			nodeContainer = filter.filter(nodeContainer, context);
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//after filter complete, convert HTMLNode to string.
		StringBuffer markupText =  new StringBuffer();
		//this flag is use to avoid surround consequence unknown html tag, such as <object><param><object>, only need surround one paired {html} 
		boolean reqHTMLEnd = false;
		boolean reqNewline = false;
		boolean lineStart = false;
		for (HTMLNode node : nodeContainer) {
			if(node.isTextNode()){
				if(!StringUtils.isEmpty(node.getText())){
					reqNewline = startNewline(markupText,node.getText(), reqNewline);
					if(reqHTMLEnd){
						markupText.append("{html}");
						reqHTMLEnd = false;
					}
					if(lineStart){
						if(!StringUtils.isWhitespace(node.getText())){
							//if node is blank text, and it is just after a line start, skip it.
							//for example, list <ol>   <li>something</li></ol>,  li is with LINE_START_TAG tag, however, spaces between
							//<ol> and <li> are harmful (converted mark line start with space: "  # something), so remove blank here.
							markupText.append(node.getText());
							lineStart = false;
						}
					}else{
						markupText.append(node.getText());
					}
				}//skip empty text
			}else if(HTMLNode.LINE_END_TAG.equals(node.getText())){
				reqNewline = true;
				lineStart = false;
			}else if(HTMLNode.LINE_START_TAG.equals(node.getText())){
				//does next tag will in a new line start? if yes, do nothing, otherwise, append "\n"
				String line = markupText.toString();
				if(line.trim().length() > 0 && !StringUtil.endOfAny(line, new String[]{"\n","\r"})){
					if(reqHTMLEnd){
						markupText.append("{html}");
						reqHTMLEnd = false;
					}
					markupText.append("\n");
				}
				lineStart = true;
			}else{
				lineStart = false;
				//this assume non text node always return non-empty value from node.getText() 
				reqNewline = startNewline(markupText, node.getText(), reqNewline);
				//some tag can not be handled by filters, then surrounding them by {html} macro,
				if(!reqHTMLEnd){
					markupText.append("{html}");
					reqHTMLEnd = true;
				}
				markupText.append(node.getText());
			}
		}
		
		if(reqHTMLEnd)
			markupText.append("{html}");
		
		log.info("Render rich content to markup takes: " +(System.currentTimeMillis() - start)); 

		return markupText.toString();
	}
	/**
	 * @param nodeContainer
	 */
	private void optimizeHTML(HTMLNodeContainer nodeContainer) {
		
		//delete 
//		HTMLNode paired = null;
//		String enclosedText = "";
//		for (HTMLNode node : nodeContainer) {
//			if(paired != null){
//				if(node == paired){
//					if(enclosedText == ""){
//						//nothing enclosed then simply remove such tag
//						paired.reset("", true);
//						paired.getPair().reset("", true);
//					}else{
//						//ok, if the paired include some text, then need decide according to what is enclosed tag
//					}
//				}
//				
//			}
//			paired = node.getPair();
//
//		}
	}
	/**
	 * @param htmlText
	 * @param nodeContainer
	 * @return
	 */
	private HTMLNodeContainer escapeText(String htmlText, HTMLNodeContainer nodeContainer) {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// all Text node need do HTMLEscape and markup escape conversion,e.g, &gt; to >, *bold* to \*bold\* etc.
		// if handle this textNode by textNode, it may run n * f times. n is text node number, f is filter number
		// so here do following: 
		// 1. convert HTMLNode list to string again but replace all non-text node to unique string
		// 1.1 As TinyMCE will save text into entity code even "entity_encoding" set to "raw", so even text inside
		// OriginalTextRequest, it still need do HTML unescapeHtml()
		// 1.2 But, the text which is surrounding by these tag which is identified by OriginalTextRequest, don't 
		// need do any escapeMarkupToSlash().
		// 2. do escape conversion
		// 3. replace back unique string pointer back to Tag node, reset all text node (it may has escape occur)
		
		StringBuffer unescTextSb = new StringBuffer();
		String uniqueKey = WikiUtil.findUniqueKey(htmlText);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// do all textnode HTML unescape
		//do HTML unescape,eg, &gt; to >; Although I only do limited entity escape(see EscapeUtil.escapeHTML()), 
		//but it is still safe use this method 
		for (HTMLNode node : nodeContainer) {
			if(node.isTextNode()){
				unescTextSb.append(node.getText());
			}else{
				unescTextSb.append(uniqueKey);
			}
		}
		String escText = unescTextSb.toString();
		if(!StringUtils.isBlank(escText)){
			escText = StringEscapeUtil.unescapeHtml(escText);
			
			//recover HTMLNode list: reset all text node as their content may changed by conversion
			//this will split N+1 strings,N is tag number.
			String[] textNodes = StringUtils.splitByWholeSeparatorPreserveAllTokens(escText, uniqueKey);
			HTMLNodeContainer newNodeList = new HTMLNodeContainer();
			Iterator<HTMLNode> iter = nodeContainer.iterator();
			for (int idx=0;idx< textNodes.length;idx++) {
				String text = textNodes[idx];
				if(!"".equals(text))
					newNodeList.add(new HTMLNode(text,true));
				for (;idx < textNodes.length -1 && iter.hasNext();) {
					HTMLNode node = iter.next();
					if(!node.isTextNode()){
						newNodeList.add(node);
						break;
					}
				}
			}
			
			nodeContainer = newNodeList;
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// do markup unescape for only text is not inside OriginalTextRequest tag, such preview
		MacroFilter macroFilter = (MacroFilter) filterProvider.getFilter(MacroFilter.class.getName());
		List<HTMLNode> orgTextMacroHTMLIds = macroFilter.getImmutableHTMLIdenifiers();
		
		
		unescTextSb = new StringBuffer();
		HTMLNode preReqCloseNode = null;
		//this container save all tag (or text) which won't do escape:
		//normally, each uniqueKey will mapping to one HTMLNode, but for OriginalTextRequest macro, which may contain multiple 
		//HTMLNodes, so that, the container use List<HTMLNode> as element.
		List<List<HTMLNode>> tagContainer = new ArrayList<List<HTMLNode>>();
		String multipleLineUniqueKeyS = null;
		String multipleLineUniqueKeyE = null;
		for (HTMLNode node : nodeContainer) {
			if(preReqCloseNode != null){
				if(preReqCloseNode == node){
					preReqCloseNode = null;
				}
				//skip all tags between preview text
				continue;
			}
			if(!node.isTextNode()){
				List<HTMLNode> nodes = new ArrayList<HTMLNode>();
				if(node.isIdentified(orgTextMacroHTMLIds) && node.getPair() != null){
					//this is preview, code or some others tags which Macro implements OriginalTextRequest
					//the put all tags/text surrounded by this preview tag(include its self pair) into tagContainer
					preReqCloseNode = node.getPair();
					for(HTMLNode preNode = node;preNode != null && preNode != node.getPair();){
						nodes.add(preNode);
						preNode = preNode.next();
					}
					nodes.add(node.getPair());
				}else{
					nodes.add(node);
				}
				if(RenderUtil.isBlockTag(node)){
					//for example start<p>[test]</p>end. If simply replace <p> and </p> with normal key, this [test] will treat
					//as normal character surrounded, actually, it is newline surrounded, so use multiple lines key
					if(!node.isCloseTag()){
						if(multipleLineUniqueKeyS == null)
							multipleLineUniqueKeyS = "\n" + WikiUtil.findUniqueKey(htmlText)+"\n";
						unescTextSb.append(multipleLineUniqueKeyS);
					}else{
						if(multipleLineUniqueKeyE == null)
							multipleLineUniqueKeyE = "\n" + WikiUtil.findUniqueKey(htmlText)+"\n";
						unescTextSb.append(multipleLineUniqueKeyE);
					}
				}else{
					unescTextSb.append(uniqueKey);
				}
				tagContainer.add(nodes);
			}else{
				unescTextSb.append(node.getText());
			}
		}
		escText = unescTextSb.toString();
		if(!StringUtils.isBlank(escText)){
			
			//do all markup filter escape, eg, *bold* \*bold\*
			escText = MarkupUtil.escapeMarkupToSlash(escText,uniqueKey);
			
			//recover HTMLNode list: reset all text node as their content may changed by conversion
			//this will split N+1 strings,N is tag number.
			if(multipleLineUniqueKeyS != null){
				escText = StringUtils.replace(escText, multipleLineUniqueKeyS, uniqueKey);
			}
			if(multipleLineUniqueKeyE != null){
				escText = StringUtils.replace(escText, multipleLineUniqueKeyE, uniqueKey);
			}
			
			String[] textNodes = StringUtils.splitByWholeSeparatorPreserveAllTokens(escText, uniqueKey);
			HTMLNodeContainer newNodeList = new HTMLNodeContainer();
			Iterator<List<HTMLNode>> iter = tagContainer.iterator();
			for (int idx=0;idx< textNodes.length;idx++) {
				String text = textNodes[idx];
				if(!"".equals(text))
					newNodeList.add(new HTMLNode(text,true));
				//find first available non-text node, the replace the tag-token...
				if (idx < textNodes.length -1 && iter.hasNext()) {
					List<HTMLNode> insertList = iter.next();
					for (HTMLNode insert : insertList) {
						newNodeList.add(insert);
					}
					
				}
			}
			nodeContainer = newNodeList;
		}
		return nodeContainer;
	}
	/**
	 * @param markupText
	 * @param reqNewline
	 * @param node
	 */
	private boolean startNewline(StringBuffer markupText,String appendText, boolean reqNewline) {
		if(reqNewline){
			String line = markupText.toString();
			//the buffer is not end with newline, appendText does not start with new line, then, we need a newline break
			if(line.trim().length() > 0 && !StringUtil.endOfAny(line, new String[]{"\n","\r"})
				&& !StringUtil.startOfAny(StringUtil.trimEndSpace(appendText), new String[]{"\n","\r"})){
				markupText.append("\n");
			}
		}
		//always return false, only for reset reqNewLine flag... reqNewline only can be set while LINE_END_TAG.equals(node.getText())
		return false;
	}

}
