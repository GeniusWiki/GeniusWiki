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

import java.util.ListIterator;
import java.util.regex.MatchResult;

import org.apache.commons.lang.StringUtils;

import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;

/**
 * @author Dapeng.Ni
 */
public class NewlineFilter extends BasePatternTokenFilter {
	//TODO: heading, table, image etc are a new paragraph divider, so how to tidy out a paragraph with <p>foo</p>?
	private static final String PARAGRAPH_TAG ="<p>";
	public static final String NEWLINE_TAG ="<br />";
	
	//don't append </span> after this string as it does equals compare with single <span> tag
	public static final String NEWLINE_HIDDEN_TAG = "<span aid=\""+NewlineFilter.class.getName()+"\">";
	
	private static final String NEWLINE ="\n";

	@Override
	public void replace(StringBuffer buffer, MatchResult matchResult, RenderContext context) {
		String all = matchResult.group(0);
		// space, \t \r etc. 
		String prefix = matchResult.group(1);
		//remove all \r but keep space or \t etc
		prefix = prefix.replaceAll("\\r", "");
		buffer.append(prefix);
		
		int count = StringUtils.countMatches(all, NEWLINE);
		if(buffer.length() >= WikiConstants.UUID_KEY_SIZE - 1 
				&& context.isUniqueKey(buffer.substring(buffer.length()-WikiConstants.UUID_KEY_SIZE + 1)+"\n")){
			if(count == 1){
				buffer.append(all);
				return;
			}else{
				buffer.append("\n");
				count--;
			}
		}
		//TODO: bug: if before newline, there is a region, which will be replaced to a string. how to detect isEndByBlockHtmlTag()?
		
		//if it is after a block HTML, such as after Hr, page needn't append the first br.
		//if following tag is Block html tag or not, it does not matter if there are br or not.
		//I checked in FF3, the Block HTML tag will ignore its the first <br> before it.
		if (RenderUtil.isEndByBlockHtmlTag(buffer)){
			count--;
			//TinyMCE eat "\n"
			//but, keep this "\n" information to render text, it is useful in RichTag render
			buffer.append(NEWLINE_HIDDEN_TAG).append("</span>");
		}
		
		
		int para = count / 2;
		for (int idx=0;idx<para;idx++) {
			if(buffer.length() == 0){
				//for any empty new lines on the beginning of content, use <br> instead <p>
				//The reason do so, is, in rich to markup convert, first <p> at text begin will be ignore 
				buffer.append(NEWLINE_TAG).append(NEWLINE_TAG);
			}else{
				//can not user <p class="xxx"/> to replace <p class="xxx"></p>, at least FF show wrong if use <p/> 
				buffer.append(PARAGRAPH_TAG);
			}
		}
		
		int br = count % 2;
		if(br > 0)
			buffer.append(NEWLINE_TAG);
	}


	public String getPatternKey() {
		return "filter.newline";
	}
	
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> nodeIter, RenderContext context) {
		if(!node.isCloseTag() && StringUtils.equalsIgnoreCase(node.getTagName(),"p")){
			StringBuffer leadText = new StringBuffer();
			//this is only check if this <p> is at beginning of text, don't need add any new line
			//such as <p>my start</p>, it is not necessary convert to \n\nmy start
			node.reset("",true);
			for(HTMLNode subnode = node.previous();subnode != null;subnode= subnode.previous()){
				if(subnode.isTextNode()){
					leadText.append(subnode.getText().trim());
					if(leadText.length() > 0){
						//append 2 newline
						node.reset(NEWLINE+NEWLINE, true);
						break;
					}
				}
			}
			//TextNut: it has format like this: <p class="p2"><br></p>, here will try to remove <br>
			if(node.getPair() != null){
				HTMLNode brnode = null;
				for(HTMLNode subnode = node.next();subnode != null;subnode= subnode.next()){
					if(subnode == node.getPair())
						break;
					if(subnode.isTextNode()){
						//skip blank text
						if(StringUtils.isBlank(subnode.getText())){
							continue;
						}
						
						//non-empty text, don't remove BR then
						brnode = null;
						break;
					}
					if(!subnode.isTextNode() &&  StringUtils.equalsIgnoreCase(subnode.getTagName(),"br")){
						brnode = null;
						break;
					}
					brnode = subnode;
				}
				
				if(brnode != null)
					brnode.reset("", true);
			}
		}else if(!node.isCloseTag() && StringUtils.equalsIgnoreCase(node.getTagName(),"div")){
			if(node.getAttributes() == null || node.getAttributes().size() == 0){
				node.reset("", true);
			}
		}else if(StringUtils.equalsIgnoreCase(node.getTagName(),"br")){
			//append 1 newline
			node.reset(NEWLINE, true);
		}else if(StringUtils.equalsIgnoreCase(node.getText(),NEWLINE_HIDDEN_TAG)){
			node.reset(NEWLINE, true);
			//clean spaces between <span aid=""> </span>, these spaces normally are added by TinyMCE.
			for(HTMLNode subnode = node.next();subnode != null && subnode != node.getPair();subnode= subnode.next()){
				if(subnode.isTextNode() && StringUtils.isBlank(subnode.getText()))
					subnode.reset("", true);
			}
		}
		
		if(node.getPair() != null){
			//empty it
			node.getPair().reset("", true);
		}
	}

	public void init() {
		regexProvider.compile(getRegex(), java.util.regex.Pattern.MULTILINE);
	}

}
