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

import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;

/**
 * {logo}
 * {logo:align=left/center/right}
 * @author Dapeng.Ni
 */
public class LogoMacro  extends BaseMacro{

	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		String slogan = "";
		//It is dangerous to put slogan here as it may contain markup - causes logo can not display finally...
//		try {
//			slogan = URLEncoder.encode(Global.SystemTitle,Constants.UTF8);
//		} catch (UnsupportedEncodingException e) {
//			//nothing
//		}
		String alignParam = params.getParam("align");
		String align;
		if("right".equalsIgnoreCase(alignParam)){
			align = "text-align:right";
		}else if("center".equalsIgnoreCase(alignParam)){
			align = "text-align:center";
		}else{
			align = "display:inline;";
		}
		buffer.append("<div aid=\""+this.getClass().getName()+"\" style='"+align+"'><img title='").append(slogan)
			.append("' src='").append(WebUtil.getHostAppURL()).append("download?instance=logo'></div>");
		
	}
	@Override
	protected void replaceHTML(HTMLNode node,ListIterator<HTMLNode> iter,  RenderContext context) {
		if(node.getPair() == null){
			node.reset("", true);
			log.warn("Unexpect case: No close div tag for " + this.getClass().getName());
			return;
		}

		
		HTMLNode subnode = node.next();
		String align = "";
		if(node.getStyle() != null){
			String alignParam = node.getStyle().get("text-align");
			if("center".equals(alignParam)){
				 align = ":align=center";
			}else if("right".equals(alignParam)){
				align = ":align=right";
			}
				
		}
		while(subnode != null && subnode != node.getPair()){
			if("img".equalsIgnoreCase(subnode.getTagName())){
				subnode.reset("{logo"+align+"}", true);
				if(subnode.getPair() != null)
					subnode.getPair().reset("", true);
				break;
			}
			subnode = subnode.next();
		}
		
		node.reset("", true);
		node.getPair().reset("", true);
	}
	
	public String[] getName() {
		return new String[]{"logo"};
	}
	public boolean isPaired(){
		return false;
	}
}
