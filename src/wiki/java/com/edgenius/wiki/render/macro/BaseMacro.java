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
package com.edgenius.wiki.render.macro;


import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.html.HTMLNodeContainer;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.Macro;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;

/**
 * @author Dapeng.Ni
 */
public abstract class BaseMacro implements Macro{
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private List<HTMLNode> htmlIDList;

	protected ApplicationContext applicationContext;
	
	public boolean isProcessEmbedded(){
		return true;
	}
	//JDK1.6 @Override
	public void init(ApplicationContext applicationContext){
		this.applicationContext = applicationContext;
		htmlIDList = RenderUtil.parseHtmlIdentifier(getHTMLIdentifier());
	}
	//JDK1.6 @Override
	public String getHTMLIdentifier() {
		return "<div aid=\""+this.getClass().getName()+"\">";
	}
	
	public Map<String,Object> getTemplValues(MacroParameter params , RenderContext renderContext, ApplicationContext appContext){
		//return null means don't use template to render
		return null;
	}
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
				replaceHTML(node,iter,context);
			}
		}

		return nodeList;
	}


	/**
	 * an empty method is just for easy for inherent class which does not need do iterator 
	 * @param node
	 * @param iter !!! You must very careful to handle ListIterator's pointer! It may impact entire markup filter 
	 * flow as this ListIterator shared by entire replace process flow. For example, if you use iter.next() to retrieve
	 * all of node in HTMLNodeContainer in this method, and you don't reset it back to original position, this causes
	 * all macro process complete.
	 */
	protected abstract void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context);
	
	
	//********************************************************************
	//               Function helper methods
	//********************************************************************
	/**
	 * @param node
	 * @param name
	 * @return
	 */
	protected String getMacroMarkupString(HTMLNode node, String name) {
		StringBuffer markup = new StringBuffer("{").append(name);
		if(node.getAttributes() != null && !StringUtil.isBlank(node.getAttributes().get(NameConstants.WAJAX))){
			
			String wajax = node.getAttributes().get(NameConstants.WAJAX);
			Map<String, String> map = RichTagUtil.parseWajaxAttribute(wajax);
			map.remove(NameConstants.ANAME);
			//remove name as !!!
			map.remove(NameConstants.MACRO);
			
			boolean first = true;
			if(map.size() > 0 ){
				markup.append(":");
				for (Entry<String,String> entry : map.entrySet()) {
					if(!first)
						markup.append("|");
					first = false;
					markup.append(entry.getKey()).append("=").append(EscapeUtil.escapeMacroParam(entry.getValue()));
				}
			}
		}
		
		markup.append("}");
		return markup.toString();
	}
	/**
	 * Please attention: This method normally assumes using for NameMacroHandler(MacroModel) as it will try to remove "MACRO" 
	 * attribute from wajax attributes. You must be carefully to use it if the macro needs get back an attribute which key is "MACRO"!!! 
	 * see, MacroModel.toRichAjaxTag();
	 * 
	 * Function method to build macro according to macro Rich tag.
	 * 
	 * @param node
	 */
	protected void resetMacroMarkup(HTMLNode node, String name) {
		resetMacroMarkup(Macro.TIDY_STYLE_NO, node, null, getMacroMarkupString(node, name), null);
	}
	
	/**
	 * This is facility method to help you handle reset HTMLNode to markup text with given  tidy style. 
	 * 
	 * !! You must ensure both endMarkup and node.getPair() is not null if you want to put endMarkup into node.getPair() node!
	 * 
	 * !!!!
	 * If tidyStyle is TIDY_STYLE_BLOCK, this node will be insert some LINE_START or LINE_END tags.  
	 * Iter.next() is unaffected. However, Iter.previous() will return new inserted LINK_END tag. 
	 * 
	 */
	protected void resetMacroMarkup(int tidyStyle, HTMLNode node, ListIterator<HTMLNode> iter, String startMarkup, String endMarkup){
		if(tidyStyle == Macro.TIDY_STYLE_BLOCK){
			//add HTMLNode.LINE_START and END surrounding markup
			node.reset(HTMLNode.LINE_START_TAG,false);
			iter.add(new HTMLNode(startMarkup,true));
			HTMLNode lastNode = new HTMLNode(HTMLNode.LINE_END_TAG,false);
			iter.add(lastNode);
			
			if(node.getPair() != null){
				if(!StringUtils.isBlank(endMarkup)){
					while(iter.hasNext()){
						HTMLNode cursor = iter.next();
						if(node.getPair() == cursor){
							node.getPair().reset(HTMLNode.LINE_START_TAG,false);
							iter.add(new HTMLNode(endMarkup,true));
							iter.add(new HTMLNode(HTMLNode.LINE_END_TAG,false));
							
							//!!! reset ListIterator back to last node - to ensure iter.next() is unaffected, use lastNode
							moveIteratorCursorTo(lastNode,iter,false);
							break;
						}
					}
				}else{
					node.getPair().reset("", true);
				}
			}
		} else{
			node.reset(startMarkup, true);
			if(node.getPair() != null){
				if(!StringUtils.isBlank(endMarkup)){
					node.getPair().reset(endMarkup, true);
				}else{
					node.getPair().reset("", true);
				}
				
			}
		}
	}
	/**
	 * Move Iterator cursor backward or forward to given node. You must ensure then direction is correct! 
	 * This location will only following "forward inertia". <br>
	 * "Inertia" means, if iterator goes previous, then next, the same cursor will return, likewise in next->previous.
	 * (see {@link com.edgenius.wiki.gwt.client.html.TestHTMLNodeContainer#testIteratorAdd()}) <br>
	 * 
	 * This method will if call next(), it already return next HTMNode from current cursor. But if call previous(), 
	 * it returns current cursor at first time. 
	 *  
	 * @param node
	 * @param iter
	 * @param forward
	 */
	protected void moveIteratorCursorTo(HTMLNode node,  ListIterator<HTMLNode> iter, boolean forward){
		if(forward){
			if(iter.hasNext()){
				HTMLNode cursor = iter.next();
				if(cursor.previous() != node){ //this is just ensure, iter current position is not equals with given node 
					while(cursor != node && iter.hasNext()){
						cursor = iter.next();
					}
				}
			}
		}else{
			//backward
			if(iter.hasPrevious()){
				HTMLNode cursor = iter.previous();
				if(cursor.next() != node){ //this is just ensure, iter current position is not equals with given node 
					while(cursor != node && iter.hasPrevious()){
						cursor = iter.previous();
					}
				}
				//delete inertia
				iter.next();
			}
		}
	}
	/**
	 * @param node
	 */
	protected void cleanPair(HTMLNode node) {
		node.reset("", true);
		if(node.getPair() != null)
			node.getPair().reset("", true);
	}

	public String[] hasChildren(){
		return null;
	}
	/**
	 * Reset all inside node to blank text node.
	 * !!! Here changes ListIterator cursor position!!!
	 * @param node
	 * @param iter
	 */
	protected void resetInsideNode(HTMLNode node, ListIterator<HTMLNode> iter) {
		HTMLNode subnode;
		for(;iter.hasNext();){
			subnode = iter.next();
			if(subnode == node.getPair())
				break;
			
			if(!subnode.isTextNode()){
				subnode.reset("", true);
				if(subnode.getPair() != null)
					subnode.getPair().reset("", true);
			}else{
				subnode.reset("", true);
			}
		}
	}
	
}
