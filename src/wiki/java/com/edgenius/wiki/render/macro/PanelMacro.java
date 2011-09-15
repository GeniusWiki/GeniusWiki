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

import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.BooleanUtil;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.MacroMaker;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;

/**
 * 
 * {panel:title=Demo}...{panel}
 * {panel:title=Demo|expand=false}...{panel}
 * 
 * @author Dapeng.Ni
 */
public class PanelMacro extends BaseMacro{

	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		String title = params.getParam(NameConstants.TITLE);
		String key = params.getRenderContext().createUniqueKey(false);
		//default expand
		boolean expand = BooleanUtil.toBooleanTrue(params.getParam("expand"));
		MacroMaker.buildPanel(key, buffer, title, EscapeUtil.escapeHTML(params.getContent()), expand);
	}
	@Override
	public String getHTMLIdentifier() {
		return "<div aid='panel'>";
	}
	public String[] getName() {
		return new String[]{"panel"};
	}
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		if(node.getPair() == null){
			log.warn("Unexpect case: No close div tag for " + this.getClass().getName());
			return;
		}
		StringBuilder title = new StringBuilder();
		HTMLNode titlePair = null;
		for(HTMLNode subnode = node.next();subnode !=null;){
			if(subnode == node.getPair())
				break;
			if(subnode == titlePair){
				titlePair = null;
				subnode = subnode.next();
				continue;
			}
			if(titlePair != null){
				//clean all tags between <div aid=panelTitle>foo</div>
				if(subnode.isTextNode()){
					title.append(subnode.getText());
				}
				subnode.reset("", true);
				subnode = subnode.next();
				continue;
			}
			if(subnode.isCloseTag() || subnode.isTextNode()){
				subnode = subnode.next();
				continue;
			}
			if("div".equalsIgnoreCase(subnode.getTagName()) && subnode.getAttributes() != null){
				
				if("paneltitle".equalsIgnoreCase(subnode.getAttributes().get(NameConstants.AID))){
					subnode.reset("", true);
					titlePair = subnode.getPair();
					if(titlePair == null){
						AuditLogger.error("PanelTitle DIV has not paired end tag:" );
					}else{
						titlePair.reset("", true);
					}
				}else if("panelcontent".equalsIgnoreCase(subnode.getAttributes().get(NameConstants.AID))){
					//at <div class=content></div> break out
					subnode.reset("", true);
					if(subnode.getPair() != null){
						subnode.getPair().reset("", true);
					}
					break;
				}
			}
			subnode = subnode.next();
		}
		StringBuilder mark = new StringBuilder("{panel");
		if(!StringUtils.isBlank(title.toString())){
			mark.append(":title=").append(EscapeUtil.escapeMacroParam(title.toString().trim()));
		}
		mark.append("}");
		resetMacroMarkup(TIDY_STYLE_BLOCK, node, iter, mark.toString(), "{panel}");

	}
	
	public boolean isPaired(){
		return true;
	}



}
