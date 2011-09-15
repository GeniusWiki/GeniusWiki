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
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;

/**
 * @author Dapeng.Ni
 */
public class AlignMacro extends BaseMacro{


	//JDK1.6 @Override
	public String[] getName() {
		return new String[]{"align"};
	}
	//JDK1.6 @Override
	public boolean isPaired() {
		return true;
	}

	@Override
	public String getHTMLIdentifier() {
		return "<* style=\"text-align: *;\">";
	}
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		if(node.getPair() == null){
			log.warn("Unexpect case: No close tag for " + this.getClass().getName());
			return;
		}
		if(node.getStyle() != null && node.getStyle().get(NameConstants.TEXT_ALIGN) != null
			&& (node.getAttributes() == null || StringUtils.isBlank(node.getAttributes().get(NameConstants.AID)))){
			String align = node.getStyle().get(NameConstants.TEXT_ALIGN);
			HTMLNode alignNode = null;
			
			//what ever the NODE is, such as <td style="align:center;colspan=4"> etc, the align attribute need be remove
			node.removeStyle("text-align", null);
			if("div".equalsIgnoreCase(node.getTagName()) 
				|| "span".equalsIgnoreCase(node.getTagName()) 
				|| "p".equalsIgnoreCase(node.getTagName())){
				alignNode = node; 
			}else{
				//maybe <td> tag,  it can not be simply reset -- otherwise, table will lost one cell.So here will add a paired <span> node
				//!!! don't insert <div> pair as it may broken TableMacro.isSimpleTableSupport()
				//enclose by current node 
				//?? need handle default left?
				if("center".equalsIgnoreCase(align) || "right".equalsIgnoreCase(align)){
						//build a proxy tag, their tagname does not matter as it will reset following code
						alignNode = new HTMLNode("<span>",false);
						HTMLNode closeAlignNode = new HTMLNode("</span>",false);
						alignNode.setPair(closeAlignNode);
		
						iter.add(alignNode);
						
						//example, go to </td> tag 
						moveIteratorCursorTo(node.getPair(), iter, true);
						//this is that node before </td>, need confirm it is not <td> (basically, it is impossible a <div> insert after <td>) 
						HTMLNode nd = iter.previous();
						if(nd != node){
							iter.add(closeAlignNode);
							//move iterator back to node
							moveIteratorCursorTo(node, iter, false);
						}
				}
			}
			
			if(alignNode != null){
				if("center".equalsIgnoreCase(align)){
					alignNode.reset("{align:align=center}",true);
					alignNode.getPair().reset("{align}",true);
					
				}else if("right".equalsIgnoreCase(align)){
					alignNode.reset("{align:align=right}",true);
					alignNode.getPair().reset("{align}",true);
				}
			}
		}
	
	}

	//JDK1.6 @Override
	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		String align = params.getParam(NameConstants.ALIGN);
		if(StringUtil.isBlank(align) || !StringUtil.containsIgnoreCase(new String[]{"center","centre","left","right"}, align)){
			buffer.append(RenderUtil.renderError("Please set " +EscapeUtil.toEntity('[') +"center|left|right " 
					+EscapeUtil.toEntity(']') +" on align attributes.",params.getStartMarkup()));
			//keep original text
			buffer.append(params.getContent());
			buffer.append("{align}");
			return;
		}

		//"centre" is only for typo in British English
		if("center".equalsIgnoreCase(align) || "centre".equalsIgnoreCase(align)){
			buffer.append("<p style=\"text-align: center;\">");
			buffer.append(params.getContent());
			buffer.append("</p>");
		}else if("left".equalsIgnoreCase(align)){
			buffer.append("<div style=\"text-align: left;\">");
			buffer.append(params.getContent());
			buffer.append("</div>");
		}else if("right".equalsIgnoreCase(align)){
			buffer.append("<div style=\"text-align: right;\">");
			buffer.append(params.getContent());
			buffer.append("</div>");
		}
		
	}

}
