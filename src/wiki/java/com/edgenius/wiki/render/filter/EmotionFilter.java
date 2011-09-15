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

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.FileUtil;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.render.FilterRegxConstants;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;

/**
 * @author Dapeng.Ni
 */
public class EmotionFilter  extends BasePatternTokenFilter {
	static final Map<String, String> EMOTIONS = new HashMap<String, String>();
	static{
		EMOTIONS.put(":)", "emotions/smiley-smile.gif");
		EMOTIONS.put(":(", "emotions/smiley-cry.gif");
		EMOTIONS.put(":D", "emotions/smiley-laugh.gif");
		EMOTIONS.put(";)", "emotions/smiley-wink.gif");
		EMOTIONS.put("(y)", "emotions/smiley-yes.gif");
		EMOTIONS.put("(n)", "emotions/smiley-no.gif");
		EMOTIONS.put("(!)", "emotions/smiley-lightbulb.gif");
		EMOTIONS.put("(*)", "emotions/smiley-star.gif");
		
		//eswcbaifdml
		EMOTIONS.put("(e)", "emotions/yes.png");
		EMOTIONS.put("(s)", "emotions/no.png");
		EMOTIONS.put("(w)", "emotions/warning.png");
		EMOTIONS.put("(c)", "emotions/cake.png");
		EMOTIONS.put("(b)", "emotions/bell.png");
		EMOTIONS.put("(a)", "emotions/award.png");
		EMOTIONS.put("(bg)", "emotions/bulletgreen.png");
		EMOTIONS.put("(br)", "emotions/bulletred.png");
		
		EMOTIONS.put("(fg)", "emotions/flaggreen.png");
		EMOTIONS.put("(fy)", "emotions/flagyellow.png");
		EMOTIONS.put("(fr)", "emotions/flagred.png");
		EMOTIONS.put("(i)", "emotions/idea.png");
		EMOTIONS.put("(f)", "emotions/flash.png");
		EMOTIONS.put("(d)", "emotions/dollar.png");
		EMOTIONS.put("(m)", "emotions/magnifier.png");
		EMOTIONS.put("(l)", "emotions/lock.png");
	}
	//JDK1.6 @Override
	public void init() {
		regexProvider.compile(getRegex(), Pattern.MULTILINE);
	}

	//JDK1.6 @Override
	public String getPatternKey() {
		
		return "filter.emotion";
	}
	
	@Override
	public void replace(StringBuffer buffer, MatchResult matchResult, RenderContext context) {

		String emotion = matchResult.group(1);
		String emotionImg = EMOTIONS.get(emotion);
		if(emotionImg == null){
			//no valid replacement
			buffer.append(emotion);
		}else{
			String html = context.buildSkinImageTag("render/" + emotionImg
					,NameConstants.AID, EmotionFilter.class.getName());
			
			if(html != null){
				buffer.append(html);
			}else{
				//nothing found from smiley candidate list, then put back original text
				buffer.append(emotion);
			}
		}
		
		//keep tailed text
		buffer.append(matchResult.group(2));
		
	}
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> nodeIter, RenderContext context) {
		String src = node.getAttributes().get(NameConstants.SRC);
		
		String emotion=null;
		for(Entry<String,String> entry:EMOTIONS.entrySet()){
			//TinyMCE convert context to ../widget/tiny_MCE/ rather than /widget/tiny_MCE/, so I have to extract file name and indexOf()
			if(entry.getValue().indexOf(FileUtil.getFileName(src)) != -1){
				emotion = entry.getKey();
				break;
			}
		}
		if(emotion != null){
			//need check if the smile end by space etc
			String sep = getSeparatorFilter(node);
			//surround emotion by separator
			node.reset(sep+emotion+sep, true);
			if(node.getPair() != null)
				node.getPair().reset("", true);
		}
	}
	protected String getSeparatorFilter(HTMLNode node){
		//!!!this method only check end of character, the super class method will check leading of character as well

		HTMLNode cursor;
		boolean needSeparator = false;
		
		//check ending character, if it is not special character, then this markup need separator filter
		cursor = node.next();
		while(cursor != null){
			if(cursor.isTextNode()){
				String text = cursor.getText();
				if(text != ""){
					text = Character.valueOf(text.charAt(0)).toString();
					//See our issue http://bug.edgenius.com/issues/34
					//and SUN Java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337993
					try {
						if(!FilterRegxConstants.SPACE_PATTERN.matcher(text).find()){
							needSeparator = true;
							break;
						}
					} catch (StackOverflowError e) {
						AuditLogger.error("StackOverflow Error in EmotionFilter.getSeparatorFilter. Input[" 
								+ text+"]  Pattern [" + FilterRegxConstants.SPACE_PATTERN.pattern()+ "]");
					} catch (Throwable e) {
						AuditLogger.error("Unexpected error in EmotionFilter.getSeparatorFilter. Input[" 
								+ text+"]  Pattern [" + FilterRegxConstants.SPACE_PATTERN.pattern()+ "]",e);
					}
				}
			}else{
				//visible tag, such as <img src="img.jgp"> (will conver to !img.jpg!), they will convert some Word, 
				//so it means a separator are required. For example, <img src="..smiley.gif"><img src="some-att.jpg"> 
				// will convert to %:(%!img.jpg!, rather than :(!img.jpg!
				if(RenderUtil.isVisibleTag(cursor)){
					needSeparator = true;
					break;
				}
				
				//need check if it is BLOCKHTML, block HTML tag does not need separator
				if(RenderUtil.isBlockTag(cursor)){
					needSeparator = false;
					break;
				}
			}
			cursor = cursor.next();
		}
		if(needSeparator)
			return "%%";
		else
			return "";
	}
}
