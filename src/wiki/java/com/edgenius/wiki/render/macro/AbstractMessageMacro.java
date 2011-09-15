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

import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.MacroMaker;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.render.Macro;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;

/**
 * This is super class for InfoMacro, WarningMacor and ErrorMacro
 * @author Dapeng.Ni
 */
public abstract class AbstractMessageMacro extends BaseMacro{

	public boolean isPaired(){
		return true;
	}
	
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		if(node.getPair() == null){
			log.warn("Unexpect case: No close div tag for " + this.getClass().getName());
			return;
		}
		
		StringBuffer titleSb = new StringBuffer();
		
		//retrieve all nodes between message <div>
		for(HTMLNode subnode = node.next();subnode !=null;){
			if(subnode == node.getPair())
				break;
			if(subnode.isCloseTag()){
				subnode = subnode.next();
				continue;
			}
				
			if(subnode.getAttributes() != null && NameConstants.TITLE.equals(subnode.getAttributes().get(NameConstants.AID))){
				if(subnode.getPair() != null){
					for(HTMLNode titleNode = subnode.next();titleNode != null;){
						//although rich editor may add style to message title, we have to discard it as it not valid markup format
						//so here just sum up all text node in title div 
						if(titleNode == subnode.getPair())
							break;
						if(titleNode.isTextNode()){
							titleSb.append(titleNode.getText());
						}
						//reset all embedded tag as blank
						titleNode.reset("", true);
						titleNode = titleNode.next();
					}
				}
				cleanPair(subnode);
				
			}else if(subnode.getAttributes() != null && NameConstants.CONTENT.equals(subnode.getAttributes().get(NameConstants.AID))){
				cleanPair(subnode);
				//don't need handle content part
				break;
			}
			//reset all node to empty text between <div aid="com.edgenius.wiki.render.macro.WarningMacro" class="macroMessage">
			// and <div aid="content">, it is good for remove unnecessary "newline" 
			subnode.reset("", true);
			subnode = subnode.next();
		}
		
		String title = titleSb.toString().trim();
		String titleStr="";
		if(!StringUtils.isEmpty(title))
			titleStr = ":"+NameConstants.TITLE+"=" + EscapeUtil.escapeMacroParam(title);
		
		resetMacroMarkup(Macro.TIDY_STYLE_BLOCK, node,iter,"{"+getMessageType()+titleStr+"}","{"+getMessageType()+"}");
	}
	
	
	
	//JDK1.6 @Override
	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		String title = params.getParam(NameConstants.TITLE);
		String content = params.getContent();
		String type = getMessageType();
		String clz = this.getClass().getName();
		
		MacroMaker.buildMessage(buffer, title, content, type, clz);
	}


	protected abstract String getMessageType();
}
