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

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;

/**
 * {rating:score=3.5/5|shape=star|size=15}
 * 
 * TODO: shape is only one choice at the moment..
 * @author Dapeng.Ni
 */
public class RatingMacro extends BaseMacro{
	static final Map<String, String> SHAPES = new HashMap<String, String>();

	public String[] getName() {
		return new String[]{"rate","rating","star"};
	}

	@Override
	public String getHTMLIdentifier() {
		return "<div class='macroRating'>";
	}
	
	public boolean isPaired() {
		return false;
	}
	
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context){
		if(node.getPair() == null){
			log.warn("Unexpect case: No close tag for " + this.getClass().getName());
			return;
		}
		
		resetMacroMarkup(TIDY_STYLE_BLOCK, node, iter, getMacroMarkupString(node, "rating"), null);
		resetInsideNode(node, iter);
		
	}
	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		buffer.append("<div class='macroRating'");
		//default value
		float score = 0;
		int full = 5;
		String shape = "star";
		String size= null;
		
		Map<String, String> pm = params.getParams();
		if(pm != null){
			String wajax = RichTagUtil.buildWajaxAttributeString(this.getClass().getName(),pm);
			buffer.append(" wajax=\"").append(wajax).append("\"");
			
			String scoreStr = pm.get("score");
			int sep = scoreStr.indexOf("/") ;
			if(sep != -1){
				score = NumberUtils.toFloat(scoreStr.substring(0,sep));
				full =  NumberUtils.toInt(scoreStr.substring(sep+1),full);
			}
			
			//TODO: only one shape support
			shape = pm.get("shape");
			if(!"star".equalsIgnoreCase(shape)){
				shape = "star";
			}
			shape.toLowerCase();
			
			String sizeStr = pm.get("size");
			if(!StringUtils.isBlank(sizeStr)){
				size = sizeStr;
			}
		}
		
		buffer.append(">");
		RenderContext context = params.getRenderContext();
		for(int idx=1;idx<=full;idx++){
			String imgName;
			if(idx > score){
				if(idx < score + 1){
					//half
					imgName = shape+"half.png";
				}else{
					//blank
					imgName = shape+"blank.png";
				}
			}else{
				//full
				imgName = shape+".png";
			}
			String[] attr = null;
			if(size !=null){
				attr = new String[]{NameConstants.WIDTH,size, NameConstants.HEIGHT, size};
			}
			buffer.append(context.buildSkinImageTag("render/rating/"+imgName, attr));
		}
	
		buffer.append("</div>");
		
	}
}
