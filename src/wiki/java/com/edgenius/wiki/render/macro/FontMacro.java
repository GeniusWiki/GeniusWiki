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

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;

/**
 * @author Dapeng.Ni
 */
public class FontMacro  extends BaseMacro{
	//JDK1.6 @Override
	public String[] getName() {
		return new String[]{"font"};
	}
	//JDK1.6 @Override
	public boolean isPaired() {
		return true;
	}

	@Override
	public String getHTMLIdentifier() {
		return "<span style=\"color: *;\">||<span style=\"background-color: *;\">||<span style=\"font-family: *;\">||<span style=\"font-size: *;\">";
	}
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		if(node.getPair() == null){
			log.warn("Unexpect case: No close tag for " + this.getClass().getName());
			return;
		}
		StringBuffer sb = new StringBuffer("{font:");
		if(node.getStyle() != null){
			Map<String, String> styles = node.getStyle();
			boolean has=false;
			String att = styles.get("color");
			if(!StringUtil.isBlank(att)){
				sb.append(NameConstants.COLOR).append("=").append(EscapeUtil.escapeMacroParam(att));
				has=true;
			}
			att = styles.get("background-color");
			if(!StringUtil.isBlank(att)){
				if(has) sb.append("|");
				sb.append(NameConstants.BKCOLOR).append("=").append(EscapeUtil.escapeMacroParam(att));
				has=true;
			}
			att = styles.get("font-family");
			if(!StringUtil.isBlank(att)){
				if(has) sb.append("|");
				sb.append(NameConstants.FONT).append("=").append(EscapeUtil.escapeMacroParam(att));
				has=true;
			}
			att = styles.get("font-size");
			if(!StringUtil.isBlank(att)){
				if(has) sb.append("|");
				sb.append(NameConstants.SIZE).append("=").append(EscapeUtil.escapeMacroParam(att));
				has=true;
			}
			sb.append("}");
			if(has){
				node.reset(sb.toString(),true);
				node.getPair().reset("{font}",true);
			}else{
				node.reset("",true);
				node.getPair().reset("",true);
			}
		}
	
	}

	//JDK1.6 @Override
	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		String color = params.getParam(NameConstants.COLOR);
		String bkcolor = params.getParam(NameConstants.BKCOLOR);
		String font = params.getParam(NameConstants.FONT);
		String size = params.getParam(NameConstants.SIZE);
		if(StringUtil.isBlank(color) &&
			StringUtil.isBlank(bkcolor)&&
			StringUtil.isBlank(font) &&
			StringUtil.isBlank(size)){
			buffer.append(RenderUtil.renderError("Please assign one of " +EscapeUtil.toEntity('[') +"color|bkcolor|font|size" 
					+EscapeUtil.toEntity(']') +" attributes", params.getStartMarkup()));
			//keep original text
			buffer.append(params.getContent());
			buffer.append("{font}");
			return;
		}
		
		buffer.append("<span style=\"");
		if(!StringUtil.isBlank(color)){
			buffer.append("color:").append(color).append(";");
		}
		if(!StringUtil.isBlank(bkcolor)){
			buffer.append("background-color:").append(bkcolor).append(";");
		}
		if(!StringUtil.isBlank(font)){
			buffer.append("font-family:").append(font).append(";");
		}
		if(!StringUtil.isBlank(size)){
			buffer.append("font-size:").append(size).append(";");
		}
		buffer.append("\">");
		buffer.append(params.getContent());
		buffer.append("</span>");
		
	}

}
