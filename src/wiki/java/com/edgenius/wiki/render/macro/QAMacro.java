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
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;

/**
 * Question and Answer macro.
 * {qa}
 * First line is as question.
 * Then answer is all rest part.
 * {qa}
 * 
 * @author Dapeng.Ni
 */
public class QAMacro  extends BaseMacro {
	
	public String[] getName() {
		return new String[]{"qa"};
	}
	public String getHTMLIdentifier() {
		return "<div class='macroQA'>";
	}
	public boolean isPaired(){
		return true;
	}

	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		String question = StringUtils.trim(params.getContent());
		
		String answer="";
		int idx = question.indexOf("\n");
		if(idx != -1){
			answer = question.substring(idx+1);
			question = question.substring(0, idx);
		}
		//surround \n in question text - so that it can render correctly. For example, if question has table macro, then the 
		//last \n can correct separate table filter and <div>. Looks |table cell|\n</div> otherwise, </div> is treated as a part of table.
		buffer.append("<div class=\"macroQA\"><div class=\"question\">").append(question).append("</div><div class=\"answer\">\n")
			.append(answer).append("\n</div></div>");
	}
	
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		if(node.getPair() == null){
			log.warn("Unexpect case: No close div tag for " + this.getClass().getName());
			return;
		}
		
		//clean <div class="question"> and <div class="answer">
		HTMLNode subnode = node;
		while(subnode != null){
			subnode = subnode.next();
			if(subnode == node.getPair())
				break;
			
			if(!subnode.isTextNode() && "div".equalsIgnoreCase(subnode.getTagName())
				&& subnode.getAttributes() != null 
				&& ("answer".equalsIgnoreCase(subnode.getAttributes().get("class")) 
					|| "question".equalsIgnoreCase(subnode.getAttributes().get("class")))){
				

				subnode.reset("", true);
				if(subnode.getPair() != null){
					subnode.getPair().reset("", true);
				}
			}
		}
		resetMacroMarkup(TIDY_STYLE_BLOCK, node, iter, "{qa}", "{qa}");
	}
}
