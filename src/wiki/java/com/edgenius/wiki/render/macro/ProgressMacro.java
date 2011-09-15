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
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;

/**
 * {progress:complete=50%|width=200|height=15|text=50%}
 * 
 * All parameters are optional, default is above sample value except text(blank as default) and complete=0%.
 *   
 * @author Dapeng.Ni
 */
public class ProgressMacro extends BaseMacro{

	private static final String DEFAULT_HEIGHT = "15px";
	private static final String DEFAULT_WIDTH = "200px";
	private static final String DEFAULT_VALUE = "0%";
	
	public String[] getName() {
		return new String[]{"progress"};
	}
	@Override
	public String getHTMLIdentifier() {
		return "<div class='macroProgress'>";
	}

	public boolean isPaired() {
		return false;
	}
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context){
		if(node.getPair() == null){
			log.warn("Unexpect case: No close tag for " + this.getClass().getName());
			return;
		}
		node.getPair().reset("", true);
		
		String value=null,height=null,width=null,text="";
		
		if(node.getStyle() != null){
			height = node.getStyle().get("height");
			width = node.getStyle().get("width");
		}
		if(DEFAULT_HEIGHT.equalsIgnoreCase(height)){
			height = null;
		}
		if(DEFAULT_WIDTH.equalsIgnoreCase(width)){
			width = null;
		}
		
		//reset all inside node to blank
		//!!! Here changes ListIterator cursor position!!!
		HTMLNode reqireText = null;
		HTMLNode subnode;
		for(;iter.hasNext();){
			subnode = iter.next();
			if(subnode == node.getPair())
				break;
			
			if(!subnode.isTextNode()){
				if(subnode.getAttributes() != null){
					if(subnode.getAttributes().get("class").equalsIgnoreCase("value") && subnode.getStyle() != null){
						value = subnode.getStyle().get("width");
					}else if(subnode.getAttributes().get("class").equalsIgnoreCase("text")){
						reqireText = subnode.getPair();
					}
				}
				if(reqireText != null && reqireText == subnode){
					reqireText = null;
				}
				subnode.reset("", true);
				if(subnode.getPair() != null)
					subnode.getPair().reset("", true);
			}else{
				if(reqireText != null){
					text += subnode.getText();
				}
				subnode.reset("", true);
			}
		}
		
		if(DEFAULT_VALUE.equalsIgnoreCase(value)){
			value = null;
		}
		
		StringBuffer sb = new StringBuffer("{progress");
		boolean dirty = false;
		if(value != null){
			dirty = true;
			sb.append(":complete=").append(value);
		}
		if(width != null){
			sb.append(dirty?"|":":").append("width=").append(GwtUtils.removeUnit(width));
			dirty = true;
		}
		if(height != null){
			sb.append(dirty?"|":":").append("height=").append(GwtUtils.removeUnit(height));
			dirty = true;
		}
		if(!StringUtils.isBlank(text)){
			sb.append(dirty?"|":":").append("text=").append(text.trim());
			dirty = true;
		}
		sb.append("}");
		
		node.reset(sb.toString(), true);
	}
	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		String value=DEFAULT_VALUE;
		String width = DEFAULT_WIDTH;
		String height = DEFAULT_HEIGHT;
		String text = null;
		Map<String, String> pm = params.getParams();
		if(pm != null){
			String val = pm.get("complete");
			if(!StringUtils.isBlank(val)){
				value = val.trim();
				if(!value.endsWith("%")){
					value +="%";
				}
			}
			
			val = pm.get("width");
			if(!StringUtils.isBlank(val)){
				width = val.trim();
				width = GwtUtils.removeUnit(width);
				if(!width.endsWith("%")){
					width +="px";
				}
			}
			val = pm.get("height");
			if(!StringUtils.isBlank(val)){
				height = val.trim();
				height = GwtUtils.removeUnit(height);
				if(!height.endsWith("%")){
					height +="px";
				}
			}
			val = pm.get("text");
			if(!StringUtils.isBlank(val)){
				text = val;
			}
		}
		buffer.append("<div class=\"macroProgress\" style=\"width:").append(width).append(";height:").append(height).append(";\">");
		buffer.append("<div class=\"value\" style=\"background-color: #0a0; width:").append(value)
			.append(";height:").append(height).append(";\"></div>");
		
		if(!StringUtils.isBlank(text)){
			String fontSize = "12px";
			String ht = GwtUtils.removeUnit(height);
			int barHt = NumberUtils.toInt(ht);
			if(barHt != 0)
				fontSize = ((int)(barHt * 0.9))+"px";
			
			text = text.trim();
//			//normally there are 2 "%" in this marco if user want to display percentage text, but they will transferred by separator filter
//			//so here is not very perfect fix...
//			if(text.endsWith("%")){
//				text = text.substring(0,text.length()-1) + EscapeUtil.toEntity('%');
//			}
			buffer.append("<div class=\"text\" style=\"font-size:").append(fontSize).append(";line-height:").append(fontSize)
				.append(";width:").append(width).append(";height:").append(height).append(";\">");
			buffer.append(text);
			buffer.append("</div>");
		}
		
		buffer.append("</div>");
	}


}
