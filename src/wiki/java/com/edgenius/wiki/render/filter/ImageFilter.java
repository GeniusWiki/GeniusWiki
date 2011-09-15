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
package com.edgenius.wiki.render.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.html.ImageModel;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.ImmutableContentFilter;
import com.edgenius.wiki.render.MarkupUtil;
import com.edgenius.wiki.render.Region;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.render.TokenVisitor;
import com.edgenius.wiki.render.handler.ImageHandler;
import com.edgenius.wiki.render.object.ObjectPosition;

/**
 * Could value is split by "|", image src, float value[left,right,center] and size[large(same with big),small].<br>
 * The reason don't use ":" as first separator (like macro) is because image may be an URL,i.e., http://foo.com/img.jpg<br>
 * For example:<br>
 * <code>
 * !image-src-url.jpg|title=photo|left|big|xxx*yyy|class=abc|...!
 * </code>
 * 
 * xxx is number of width; yyy is number of height<br>
 * class=abc is possible any valid image attribute, please note, these string won't do any escape, so user must be careful themselves.
 * 
 * Default value of float value is align on left but no text wrap ( text will wrap image if you assign "left" explicitly).
 * Default value of size is image original size. Big or small both give thumbnail image.
 * 
 * 
 * Some old design comments(27/04/2009):
 * In old style, title can be add like !title>image.jpg!.  But I found ">" has conflict with LinkFilter. And link can embed image.
 * [!title>image.jpg!>mylink] is quite confused format. This format is very hard to avoid from edit editor converting to markup.
 * 
 * @author Dapeng.Ni
 */
public class ImageFilter extends BasePatternTokenFilter implements ImmutableContentFilter{
	private static final String HANDLER = ImageHandler.class.getName();
	public static final String SRC = NameConstants.SRC;
	public static final String TITLE = NameConstants.TITLE;
	public static final String WIDTH = NameConstants.WIDTH;
	private static final String HEIGHT = NameConstants.HEIGHT;
	public static final String ALIGN = NameConstants.ALIGN;
	
	//JDK1.6 @Override
	public void init(){
		regexProvider.compile(getRegex(), Pattern.MULTILINE);
	}
	public String getPatternKey() {
		return "filter.img";
	}
	//JDK1.6 @Override
	public List<Region> getRegions(CharSequence input) {
		final List<Region> list = new ArrayList<Region>();
		regexProvider.replaceByTokenVisitor(input, new TokenVisitor<Matcher>() {
			public void handleMatch(StringBuffer buffer, Matcher matcher) {
				int contentStart = matcher.start(2);
				int contentEnd= matcher.end(2);
				int start = contentStart -1;
				int end = contentEnd +1;
				
				//!image! only 1 region, and it is immutable 
				list.add(new Region(ImageFilter.this, true, start,end,contentStart,contentEnd));
			}
		});
		return list;
	}


	@Override
	public void replace(StringBuffer buffer, MatchResult result, RenderContext context) {
		String src;
		Region bodyRegion = context.getCurrentRegion();
		if(bodyRegion != null){
			src = bodyRegion.getContent();
		}else{
			AuditLogger.error("Unexpected case: Immutable fitler cannot find out current region." + result.group());
			src = result.group(1);
		}
		
		HashMap<String, String> attValues = new HashMap<String, String>();
		//append leading text before the real markup !image.png!
		StringBuffer sb = new StringBuffer(result.group(1));
		
		if ( src != null && src.trim().length() > 0) {
			src = src.trim();

			//there are some other attributes, such as float
			String[] split = StringUtil.splitWithoutEscaped(src, "|");
			//as splitwidhtouEscaped() always return non-zero length String[], 
			//ie, if there is "|" then return input string as first element of string[], so it is safe to check split[0]
			src = split[0];
			
			for (int idx = 1;idx<split.length;idx++) {
				String att = split[idx].trim();
				if(SharedConstants.ALIGN_VALUES.contains(att)){
					attValues.put(ALIGN, att);
				}else if(SharedConstants.SIZE_VALUES.contains(att)){
					if (StringUtil.equalsIgnoreCase(SharedConstants.SIZE_VALUES.big.getName(), att)
							|| StringUtil.equalsIgnoreCase(SharedConstants.SIZE_VALUES.large.getName(), att)) {
						// large thumbnail
						attValues.put(WIDTH, ImageModel.LARGE_THUMBNAIL_WIDTH);
					} else {
						// small thumbnail
						attValues.put(WIDTH, ImageModel.SMALL_THUMBNAIL_WIDTH);
					}
				}else{
					if(att.indexOf("=") != -1){
						String[] pair = att.split("=");
						if(pair.length == 2){
							attValues.put(pair[0], EscapeUtil.removeSlashEscape(pair[1]));
						}
					}else{
						//is it xxx * yyy ?
						String[] wh = att.split("\\*");
						if(wh.length == 2){
							int w = NumberUtils.toInt(wh[0],-1);
							int h = NumberUtils.toInt(wh[1],-1);
							if(w != -1 && h != -1){
								attValues.put(WIDTH, ""+w);
								attValues.put(HEIGHT, ""+h);
							}
						}
					}						
				}
			}

			attValues.put(SRC, src);
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// NOTE: always user ImageHandler rather that simple append "http://image-file-url" 
			// after buffer. The reason is image file name may be any, for example, my--pic--1.jpg,
			// if append to buffer, file name will replace by StrightThrough filter! 
			ObjectPosition pos = new ObjectPosition(result.group(0));
			pos.uuid = context.createUniqueKey(false);
			pos.serverHandler = HANDLER;
			pos.values = attValues;
			context.getObjectList().add(pos);
			sb.append(pos.uuid);
			buffer.append(sb);
			
			if(bodyRegion != null && bodyRegion.getSubRegion() != null && pos != null){
				bodyRegion.getSubRegion().setKey(pos.uuid);
			}

			
			//append tailed text after markup
			if(result.groupCount() > 2)
				buffer.append(result.group(3));
			return;
		} else {
			//TODO need i18n
			buffer.append(RenderUtil.renderError("Image filter needs src attribute to declare the image URL.", result.group(0)));
		}
	}

	
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> nodeIter, RenderContext context){
		String aid = node.getAttributes().get(NameConstants.AID);
		//image macro or filter only handle pure <img> tag, except that, don't use this macro
		//for example, aid could be EmotionFilter, or NameMacro class etc
		if(aid != null)
			return;
		
		ImageModel image = new ImageModel();
		//no enclosed Text
		image.fillToObject(node.getText(),null);
		
		//this markup could be enclose inside %% filter
		String markupBorder = getSeparatorFilter(node);
		
		StringBuffer markup = new StringBuffer(markupBorder+getMarkupPrint());
		if(StringUtils.isBlank(image.filename)){
			markup.append(image.url);
		}else{
			markup.append(image.filename);
		}
		if(!StringUtils.isBlank(image.title)){
			String title = image.title;
			title = MarkupUtil.escapeMarkupToSlash(title, null);
			//I really hate these escape - but I also don't want to limit user input:(
			//above escapeMarkupToSlash only escape "|" if it is line first characeter. So here do special handling..
			if(!image.title.startsWith("|"))
				title = EscapeUtil.escapeBySlash(title, new char[]{'|'});
			markup.append("|").append(NameConstants.TITLE).append("=").append(title);
		}
		if(!StringUtils.isBlank(image.width)){
			if(StringUtils.isBlank(image.height)){
				//could be use size attribute defined 
				if(image.width.equals(ImageModel.LARGE_THUMBNAIL_WIDTH)){
					markup.append("|").append(SharedConstants.SIZE_VALUES.big.getName());
				}else if(image.width.equals(ImageModel.SMALL_THUMBNAIL_WIDTH)){
					markup.append("|").append(SharedConstants.SIZE_VALUES.small.getName());
				}
			}else{
				markup.append("|").append(image.width + "*" + image.height);
			}
		}
		if(!StringUtils.isBlank(image.align)){
			markup.append("|").append(image.align);
		}
		if(image.attributes != null && image.attributes.size() > 0){
			//there are extra attribute
			for (Entry<String,String> entry : image.attributes.entrySet()) {
				markup.append("|").append(entry.getKey()).append("=").append(entry.getValue());
			}
		}
		markup.append(getMarkupPrint()+markupBorder);
		
		node.reset(markup.toString(), true);
		if(node.getPair() != null)
			node.getPair().reset("", true);
	}



}
