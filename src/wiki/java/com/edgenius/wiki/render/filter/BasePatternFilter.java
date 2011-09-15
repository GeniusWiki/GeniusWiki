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
package com.edgenius.wiki.render.filter;

import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.html.HTMLNodeContainer;
import com.edgenius.wiki.render.FilterRegxConstants;
import com.edgenius.wiki.render.PatternFilter;
import com.edgenius.wiki.render.RegexProvider;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.render.impl.JdkRegexProvider;

/**
 * Implementation of Regex Filter, default using JDKRegex.
 * @author Dapeng.Ni
 */
public abstract class BasePatternFilter implements PatternFilter{
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	
	protected RegexProvider<Matcher> regexProvider = new JdkRegexProvider();
	
	protected String regex;
	private String replacement;
	private boolean needSpecialSurrounding = false;
	private String htmlIdentifier;
	private String markupPrint;
	protected List<HTMLNode> htmlIDList;

	// ********************************************************************
	// Function methods
	// ********************************************************************
	//JDK1.6 @Override
	public String filter(String input, RenderContext context){
		//this is a kind of default handle.
		return regexProvider.replaceAll(input,getReplacement());
	}
	
	/**
	 * Default handle to convert HTMLNode(tag node) to HTMLNode(text node) which contains String of markup.
	 * This means one HTML tag must mapping to one exact markup. If a HTML tag contains multiple style etc,
	 * it won't replace correctly.
	 *  
	 */
	//JDK1.6 @Override
	public HTMLNodeContainer filter(HTMLNodeContainer nodeList, RenderContext context) {

		for(ListIterator<HTMLNode> iter = nodeList.listIterator();iter.hasNext();){
			HTMLNode node = iter.next();
			if(node.isTextNode())
				continue;
			//close tag assume handled in its open tag(paired), so just skip it
			if(node.isCloseTag())
				continue;
			
			if(node.isIdentified(htmlIDList)){
				//OK, find a tag which can be handled by this filter....
				replaceHTML(node,iter, context);
			}
		}
		
		return nodeList;
	}


	/**
	 * Most time, only extends this method enough for RichTag render.
	 * @param node
	 * @param nodeIter TODO
	 */
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> nodeIter, RenderContext context) {
		if(markupPrint == null){
			log.warn("Filter " + this.getClass().getName() + " does not contain valid markup replacement. Please fix your code.");
		}
		
		//
		String sep = getSeparatorFilter(node);
		node.reset(sep+markupPrint,true);
		if(node.getPair() != null)
			node.getPair().reset(markupPrint+sep, true);
	}
	/**
	 * For example, if RichEdit bolds a piece of text which is inside pure text, the converted
	 * bold markup requires a special surrounding. Here detect this case, if required, return "%" filter markup.
	 * @param node
	 * @return
	 */
	protected String getSeparatorFilter(HTMLNode node){
		if(!isNeedSpecialSurrounding())
			return "";
		
		HTMLNode cursor;
		boolean needSeparator = false;
		
		//check leading character, if it is not special character, then this markup need separator filter
		cursor = node.previous();
		while(cursor != null){
			if(cursor.isTextNode()){
				String text = cursor.getText();
				if(!StringUtils.isEmpty(text)){
					text = Character.valueOf(text.charAt(text.length()-1)).toString();
					//See our issue http://bug.edgenius.com/issues/34
					//and SUN Java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337993
					try {
						needSeparator = !FilterRegxConstants.BORDER_PATTERN.matcher(text).find();
						break;
					} catch (StackOverflowError e) {
						AuditLogger.error("StackOverflow Error in BasePatternFilter.getSeparatorFilter. Input[" 
								+ text+"]  Pattern [" + FilterRegxConstants.BORDER_PATTERN.pattern()+ "]");
					} catch (Throwable e) {
						AuditLogger.error("Unexpected error in BasePatternFilter.getSeparatorFilter. Input[" 
								+ text+"]  Pattern [" + FilterRegxConstants.BORDER_PATTERN.pattern()+ "]",e);
					}
				}
			}else{
				//visible tag, such as <img src="img.jgp"> (will conver to !img.jpg!), they will convert some Word, 
				//so it means a separator are required. For example, <img src="..smiley.gif"><img src="some-att.jpg"> 
				// will convert to %%:(%%!img.jpg!, rather than :(!img.jpg!
				if(RenderUtil.isVisibleTag(cursor)){
					needSeparator = true;
					break;
				}
				//need check if it is BLOCKHTML
				if(RenderUtil.isBlockTag(cursor)){
					needSeparator = false;
					break;
				}
			}
			cursor = cursor.previous();
		}
		if(!needSeparator){
			if(node.getPair() != null)
				cursor = node.getPair().next();
			else
				//this is for sigle tag node, such as image <img> which does not need close tag
				cursor = node.next();
			while(cursor != null){
				if(cursor.isTextNode()){
					String text = cursor.getText();
					if(!StringUtils.isEmpty(text)){
						text = Character.valueOf(text.charAt(0)).toString();
						//See our issue http://bug.edgenius.com/issues/34
						//and SUN Java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337993
						try {
							needSeparator = !FilterRegxConstants.BORDER_PATTERN.matcher(text).find();
							break;
						} catch (StackOverflowError e) {
							AuditLogger.error("StackOverflow Error in BasePatternFilter.getSeparatorFilter. Input[" 
									+ text+"]  Pattern [" + FilterRegxConstants.BORDER_PATTERN.pattern()+ "]");
						} catch (Throwable e) {
							AuditLogger.error("Unexpected error in BasePatternFilter.getSeparatorFilter. Input[" 
									+ text+"]  Pattern [" + FilterRegxConstants.BORDER_PATTERN.pattern()+ "]",e);
						}
					}
				}else{
					//visible tag, such as <img src="img.jgp"> (will conver to !img.jpg!), they will convert some Word, 
					//so it means a separator are required. For example, <img src="..smiley.gif"><img src="some-att.jpg"> 
					// will convert to %:(%!img.jpg!, rather than :(!img.jpg!
					if(RenderUtil.isVisibleTag(cursor)){
						needSeparator = true;
						break;
					}
					//need check if it is BLOCKHTML
					if(RenderUtil.isBlockTag(cursor)){
						needSeparator = false;
						break;
					}
				}
				cursor = cursor.next();
			}
		}
		if(needSeparator)
			return "%%";
		else
			return "";
	}
	//********************************************************************
	//               set / get methods
	//********************************************************************
	//JDK1.6 @Override
	public String getRegex() {
		return regex;
	}
	//JDK1.6 @Override
	public boolean isNeedSpecialSurrounding(){
		return needSpecialSurrounding;
	}
	//JDK1.6 @Override
	public void setRegex(String regex) {
		if(regex.trim().equals(FilterRegxConstants.PATTERN_NORMAL_KEY)){
			needSpecialSurrounding = true;
			regex = FilterRegxConstants.PATTERN_NORMAL_SURROUNDING.replaceAll(FilterRegxConstants.PATTERN_REP_TOKEN, getMarkupPrint());
		}else if(regex.trim().equals(FilterRegxConstants.PATTERN_ANYTEXT_KEY)){
			if(FilterRegxConstants.PRINT_VARIABLE.equals(getMarkupPrint())){
				//variable token is {$xxx}
				//don't use String.replaceFirst(), which is not working properly for {\\$ as replacement
				regex = StringUtils.replaceOnce(FilterRegxConstants.PATTERN_ANYTEXT_SURROUNDING, FilterRegxConstants.PATTERN_REP_TOKEN, "{\\$");
				regex = StringUtils.replaceOnce(regex, FilterRegxConstants.PATTERN_REP_TOKEN, "}");
			}else{
				regex = FilterRegxConstants.PATTERN_ANYTEXT_SURROUNDING.replaceAll(FilterRegxConstants.PATTERN_REP_TOKEN, getMarkupPrint());
			}
		}else if(regex.trim().equals(FilterRegxConstants.PATTERN_SINGLE_KEY)){
			regex = FilterRegxConstants.PATTERN_SINGLE_TOKEN.replaceAll(FilterRegxConstants.PATTERN_REP_TOKEN, getMarkupPrint());
		}else if(regex.trim().equals(FilterRegxConstants.PATTERN_ANCHOR_KEY)){
			needSpecialSurrounding = true;
			regex = FilterRegxConstants.PATTERN_ANCHOR.replaceAll(FilterRegxConstants.PATTERN_REP_TOKEN, getMarkupPrint());
		}
		
		this.regex = regex;
	}
	//JDK1.6 @Override
	public String getReplacement() {
		return replacement;
	}
	//JDK1.6 @Override
	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

	//JDK1.6 @Override
	public String getHTMLIdentifier() {
		return this.htmlIdentifier;
	}
	//JDK1.6 @Override
	public void setHTMLIdentifier(String htmlIdentifier) {
		this.htmlIdentifier = htmlIdentifier;
		htmlIDList = RenderUtil.parseHtmlIdentifier(this.htmlIdentifier);
	}
	//JDK1.6 @Override
	public String getMarkupPrint() {
		return markupPrint;
	}
	//JDK1.6 @Override
	public void setMarkupPrint(String print) {
		markupPrint = print;
	}
	
}
