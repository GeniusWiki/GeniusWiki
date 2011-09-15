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
package com.edgenius.wiki.gwt.client.html;

import java.util.HashMap;
import java.util.Map;

import com.edgenius.wiki.gwt.client.model.RichTagPiece;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;

/**
 * This class does not use as Transfer object between server/client like others. It only 
 * use for serializing Image markup to/from HTML &gt;img> tag
 * <br>
 * WAJAX attribute only contains "image file name" which is attachment name from page.
 * 
 * @author Dapeng.Ni
 */
public class ImageModel implements RichTagPiece{
	public static final String SMALL_THUMBNAIL_WIDTH = "120";
	public static final String LARGE_THUMBNAIL_WIDTH = "280";
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// properties
	public String filename;
	//external image link (http://...img.jpg) or internal attachment repo url link
	public String url;
	public String align;
	public String width;
	public String height;
	public String title;
	public Map<String, String> attributes = new HashMap<String, String>();
	
	//********************************************************************
	//               methods
	//********************************************************************
	public void fillToObject(String tagString, String enclosedText) {
		//encloseText has nothing at moment
		
		Map<String,String> map = HTMLUtil.parseAttributes(tagString);
		//get these attribute from standard <img> tag attributes
		this.width = map.remove(NameConstants.WIDTH);
		this.height = map.remove(NameConstants.HEIGHT);
		this.title = EscapeUtil.unescapeHTML(map.remove(NameConstants.TITLE));
		this.url = map.remove(NameConstants.SRC);
		String style = map.remove(NameConstants.STYLE);
		if(style != null){
			Map<String, String> smap = HTMLUtil.parseStyle(style);
			this.align = smap.get(NameConstants.FLOAT);
			if(StringUtil.isBlank(this.align)){
				if("block".equalsIgnoreCase(smap.get(NameConstants.DISPLAY))
					&& "auto".equalsIgnoreCase(smap.get("margin-left"))
					&& "auto".equalsIgnoreCase(smap.get("margin-right"))){
					this.align= SharedConstants.ALIGN_VALUES.center.getName();
				}
			}
		}
		
		String wajax = map.remove(NameConstants.WAJAX);
		if(!StringUtil.isBlank(wajax)){
			//get wajax attribute value
			Map<String,String> wmap = RichTagUtil.parseWajaxAttribute(wajax);
			//get these 2 from wajax as they are not starndard attributes
			this.filename = wmap.get(NameConstants.FILENAME);
		}
		//This case is from TextNut - it embedded image has format:<img src="file:///Picture1.png" alt="Picture1.png">
		if(this.url != null && this.url.startsWith("file://") && this.url.length() > 7){
			this.filename = this.url.substring(this.url.lastIndexOf("/")+1);
		}
		//remove some unnecessary attributes
		map.remove(NameConstants.ALT);
		//any left attributes except above
		attributes = map;
	}
	/**
	 * @param attributes
	 * @return
	 */
	public String toRichAjaxTag() {
		
		StringBuffer imgBuf = new StringBuffer();
		String htmlTitle = EscapeUtil.escapeHTML(this.title);
		
		//image has size attribute - then need a url link to original image size 
		if(!StringUtil.isBlank(this.width)){
			imgBuf.append("<a aid=\""+SharedConstants.NO_RENDER_TAG+"\" class='").append(SharedConstants.CSS_RENDER_IMAGE);
			imgBuf.append("' target='_blank' href=\"");
			imgBuf.append(this.url);
			if(!StringUtil.isBlank(this.title)){
				imgBuf.append("\" title=\"").append(htmlTitle);
			}
			imgBuf.append("\">");
		}
		
		//default: float on left, if has align the, add style attribute
		if(!StringUtil.isBlank(align)){
			if(StringUtil.equalsIgnoreCase(SharedConstants.ALIGN_VALUES.center.getName(),align)
				|| StringUtil.equalsIgnoreCase(SharedConstants.ALIGN_VALUES.centre.getName(),align) ){
				//special handle for center image
				attributes.put(NameConstants.STYLE, "display: block; margin-left: auto;margin-right: auto");
			}else{
				//left or right, use float style
				attributes.put(NameConstants.STYLE, "float:"+align);
			}
		}
		if(!StringUtil.isBlank(title))
			attributes.put(NameConstants.TITLE, htmlTitle);
		if(!StringUtil.isBlank(width))
			attributes.put(NameConstants.WIDTH,width);
		if(!StringUtil.isBlank(height))
			attributes.put(NameConstants.HEIGHT,height);
		
		//attachment image - build wajax attribute
		if(!StringUtil.isBlank(filename)){
			Map<String, String> map = new HashMap<String, String>();
			map.put(NameConstants.FILENAME, filename);
			
			String wajax = RichTagUtil.buildWajaxAttributeString(ImageModel.class.getName(),map);
			attributes.put(NameConstants.WAJAX, wajax);
		}
			
		//image HTML tag: put 
		attributes.put(NameConstants.SRC, url);
		imgBuf.append(HTMLUtil.buildTagString("img",null, attributes));

		//end of link for original image size
		if(!StringUtil.isBlank(this.width)){
			imgBuf.append("</a>");
		}
		
		return imgBuf.toString();
	}
}
