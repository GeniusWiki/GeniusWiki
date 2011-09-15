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
import org.apache.commons.lang.math.NumberUtils;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;

/**
 * {slider:width=923|height=341}
 * 
 * @author Dapeng.Ni
 */
public class ImageSliderMacro extends BaseMacro {
	private static final int WIDTH = 920;
	private static final int HEIGHT = 340;
	
	//JDK1.6 @Override
	public String[] getName() {
		return new String[]{"slider", "slide"};
	}
	@Override
	public String getHTMLIdentifier() {
		return "<div class='macroSlider'>";
	}
	//JDK1.6 @Override
	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
	
		RenderContext renderContext = params.getRenderContext();
		
		//get width and height
		int width = NumberUtils.toInt(params.getParam("width"), 0);
		int height = NumberUtils.toInt(params.getParam("height"), 0);
		width = width==0?WIDTH:width;
		height = height==0?HEIGHT:height;
		
		String sliderId = "macroSliderDiv"+renderContext.createIncremetalKey();
		buffer.append("<div class=\"macroSlider\"  " + NameConstants.WAJAX + "=\"" 
				+ RichTagUtil.buildWajaxAttributeString(this.getClass().getName(),params.getParams())+"\">");
		
		buffer.append("<div class=\"slider-wrapper theme-default\">");
		buffer.append("<div id=\"").append(sliderId).append("\" class=\"nivoSlider\" style=\"width:");
		buffer.append(width).append("px;height:").append(height).append("px\">");
		
		buffer.append(params.getContent());
		buffer.append("</div></div>");
		
		String jsURL = renderContext.buildDownloadURL("widgets/jquery/nivo-slider/jquery.nivo.slider.pack.js");
		if(!RenderContext.RENDER_TARGET_RICH_EDITOR.equals(renderContext.getRenderTarget()) && !StringUtils.isEmpty(jsURL)){
			//append JS and styles - this part not assume for editor but only for viewer.
			buffer.append("<link rel=\"stylesheet\" href=\"")
				.append(renderContext.buildDownloadURL("widgets/jquery/nivo-slider/themes/default/default.css"))
				.append("\" type=\"text/css\" media=\"screen\" />");
			buffer.append("<link rel=\"stylesheet\" href=\"")
				.append(renderContext.buildDownloadURL("widgets/jquery/nivo-slider/nivo-slider.css"))
				.append("\" type=\"text/css\" media=\"screen\" />");
			
			buffer.append("<script type=\"text/javascript\" src=\"").append(jsURL).append("\"></script>");
			buffer.append("<script type=\"text/javascript\"> $(document).ready(function() { $('#").append(sliderId).append("').nivoSlider();});</script>");
		}
		buffer.append("</div>");
		
	}
	public boolean isPaired(){
		return true;
	}
	
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		resetMacroMarkup(TIDY_STYLE_BLOCK, node, iter, getMacroMarkupString(node, "slider"), "{slider}");
		
		//Reset all notes, keep images, remove JS 
		//<div class=macroSlider> <div class=slider-wrapper><div class=nivoSlider> ( keep these parts as they are suppose to be images ) 
		//</div></div> (delete these part, suppose to be javascript </div>
		
		HTMLNode pair = node.getPair();
		if(pair == null){
			AuditLogger.error("Slide macro doesn't find paired div");
			return;
		}
		boolean cleanJS = false;
		HTMLNode subnode = node.next();
		while(subnode != null){
			if(subnode == pair)
				break;
			if(!subnode.isTextNode() && subnode.getAttributes() != null){
				String clz = subnode.getAttributes().get(NameConstants.CLASS);
				if(StringUtils.contains(clz,"slider-wrapper")){
					subnode.reset("", true);
					if(subnode.getPair() != null){
						subnode.getPair().reset("", true);
					}
				}else if("nivoSlider".equals(clz)){
					subnode.reset("", true);
					if(subnode.getPair() != null){
						subnode.getPair().reset("", true);
						cleanJS = true;
						//jump to this nivoSlider div end, in while() end, it suppose move to slider-wrapper end div.
						subnode = subnode.getPair();
					}
				}
			}
			
			if(cleanJS){
				//clean script tag, and inside text and link tag - but now clean all of them
				// ("script".equals(subnode.getTagName()) || "link".equals(subnode.getTagName()))
				subnode.reset("", true);
				if(subnode.getPair() != null){
					subnode.getPair().reset("", true);
				}
			}
			
			subnode = subnode.next();
		}
	}

}
