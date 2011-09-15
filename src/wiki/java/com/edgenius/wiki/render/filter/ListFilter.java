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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.Region;
import com.edgenius.wiki.render.RegionContentFilter;
import com.edgenius.wiki.render.RenderContext;

/**
 * <pre>
 * * list
 * ** sub list
 * 
 * # order list
 * ## sub order list
 * 
 * i. order list with roman 
 * ii. sub order list with roman
 * 
 * - minus list
 * -- sub minus list
 * 
 * </pre>
 * @author Dapeng.Ni
 */
public class ListFilter extends BasePatternTokenFilter implements RegionContentFilter{

	private static Logger log = LoggerFactory.getLogger(ListFilter.class);

	private final static Map<Character, String> openList = new HashMap<Character, String>();

	private final static Map<Character, String> closeList = new HashMap<Character, String>();

	private static final String UL_CLOSE = "</ul>";

	private static final String OL_CLOSE = "</ol>";
	private Pattern bulletPattern = Pattern.compile("[-#*]+|[iIaAgkKj]+\\.");
	
	public String getPatternKey() {
		return "filter.list";
	}
	public ListFilter() {
		super();
		openList.put(Character.valueOf('-'), "<ul class=\"minus\">");
		openList.put(Character.valueOf('*'), "<ul class=\"star\">");
		openList.put(Character.valueOf('#'), "<ol>");
		openList.put(Character.valueOf('i'), "<ol class=\"roman\">");
		openList.put(Character.valueOf('I'), "<ol class=\"ROMAN\">");
		openList.put(Character.valueOf('a'), "<ol class=\"alpha\">");
		openList.put(Character.valueOf('A'), "<ol class=\"ALPHA\">");
		openList.put(Character.valueOf('g'), "<ol class=\"greek\">");
		openList.put(Character.valueOf('h'), "<ol class=\"hiragana\">");
		openList.put(Character.valueOf('H'), "<ol class=\"HIRAGANA\">");
		openList.put(Character.valueOf('k'), "<ol class=\"katakana\">");
		openList.put(Character.valueOf('K'), "<ol class=\"KATAKANA\">");
		openList.put(Character.valueOf('j'), "<ol class=\"HEBREW\">");
		closeList.put(Character.valueOf('-'), UL_CLOSE);
		closeList.put(Character.valueOf('*'), UL_CLOSE);
		closeList.put(Character.valueOf('#'), OL_CLOSE);
		closeList.put(Character.valueOf('i'), OL_CLOSE);
		closeList.put(Character.valueOf('I'), OL_CLOSE);
		closeList.put(Character.valueOf('a'), OL_CLOSE);
		closeList.put(Character.valueOf('A'), OL_CLOSE);
		closeList.put(Character.valueOf('g'), OL_CLOSE);
		closeList.put(Character.valueOf('G'), OL_CLOSE);
		closeList.put(Character.valueOf('h'), OL_CLOSE);
		closeList.put(Character.valueOf('H'), OL_CLOSE);
		closeList.put(Character.valueOf('k'), OL_CLOSE);
		closeList.put(Character.valueOf('K'), OL_CLOSE);
		closeList.put(Character.valueOf('j'), OL_CLOSE);
	};
	//JDK1.6 @Override
	public void init(){
		regexProvider.compile(getRegex(), Pattern.MULTILINE|Pattern.DOTALL);
	}
	
	public List<Region> getRegions(CharSequence buffer) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void replace(StringBuffer buffer, MatchResult result, RenderContext context) {
		try {
			//this text does not contain leading newline (\n) but contain all tailed newline.
			String wholeText = result.group(0);
			
			BufferedReader reader = new BufferedReader(new StringReader(wholeText));
			addList(buffer, reader);
			
			//append tailing newlines
			buffer.append(result.group(3));
		} catch (Exception e) {
			log.warn("ListFilter: unable get list content", e);
		}
	}
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> nodeIter, RenderContext context){
		buildMarkup("",node, context);
		HTMLNode paired = node.getPair();
		if(paired != null){
			//because list markup must end with an empty line or the end of text. 
			//if text is coming from RichEditor, the </ul> maybe not tailed by empty lines. So need mandatory add it.
			HTMLNode nextnode = paired.next();
			int newline = 0;
			if(nextnode != null){
				do{
					if(nextnode.isTextNode() && !StringUtils.isEmpty(nextnode.getText())){
						newline = 10; //give some number bigger than 2
						break;
					}
						
					if (nextnode.isCloseTag()){
						nextnode = nextnode.next();
						continue;
					}
					
					if("br".equalsIgnoreCase(nextnode.getTagName())){
						newline++;
					}else if("p".equalsIgnoreCase(nextnode.getTagName())){
						newline +=2;
					}else if("ul".equalsIgnoreCase(nextnode.getTagName()) || "ol".equalsIgnoreCase(nextnode.getTagName())){
						//the next tag is ul/ol, then it should be ok - all list markup will process together... 
						newline = 10; //give some number bigger than 2
						break;
					}else if("span".equalsIgnoreCase(nextnode.getTagName())
						&& nextnode.getAttributes() != null
						&& nextnode.getAttributes().get(NameConstants.AID) != null
						&& NewlineFilter.class.getName().equalsIgnoreCase(nextnode.getAttributes().get(NameConstants.AID))){
						newline++;
					}else
						break;
					
					if(newline > 1)
						break;
					
					nextnode = nextnode.next();
				}while(nextnode != null);
				
				if(newline < 2 && nextnode != null){
					//if there is no newline, and not end of text... append newline
					StringBuffer buf = new StringBuffer();
					for(int idx=newline;idx < 2;idx++){
						//append newline
						buf.append("\n");
					}
					paired.reset(buf.toString(), true);
				}
			}
		}
		
	}
	/**
	 * @param node
	 * @param nodeIndex
	 * @param nodeList
	 * @param context
	 */
	private void buildMarkup(String htmlPrint, HTMLNode node, RenderContext context) {
		//OK, find a tag which can be handled by this filter....
		HTMLNode paired = node.getPair();
		if(paired == null){
			log.error("No end tab for list tag " + node);
			return;
		}
		
		if(StringUtils.equalsIgnoreCase(node.getTagName(),"ul")){
			htmlPrint += "*";
		}else if(StringUtils.equalsIgnoreCase(node.getTagName(),"ol")){
			htmlPrint += "#";
		}
		
		//find the sub array from open to close tag for this list
		//must iterator from current node next one, so that won't to dead loop
		HTMLNode subnode = node.next();
		HTMLNode lastLiNode = null;
		while(subnode != null){
			if(subnode == paired){
				break;
			}
			if(subnode.isCloseTag() || subnode.isTextNode()){
				subnode = subnode.next();
				continue;
			}
			if(subnode.isIdentified(htmlIDList)){
				//recursive call for embed ul or ol
				buildMarkup(htmlPrint, subnode, context);
			}
			if(StringUtils.equalsIgnoreCase(subnode.getTagName(),"li")){
				subnode.reset(htmlPrint+" ", true);
				if(subnode.getPair() != null){
					//use line start, so that any leading spaces will be removed
					subnode.getPair().reset(HTMLNode.LINE_START_TAG, false);
					lastLiNode = subnode.getPair();
				}
			}
			subnode = subnode.next();
		}
		
		//clear ul or ol tag
		node.reset(HTMLNode.LINE_START_TAG, false);
		paired.reset(HTMLNode.LINE_END_TAG, false);
		
		//last LI, then doesn't need end with line start
		if(lastLiNode != null)
			lastLiNode.reset("", true);
		
		//final text looks: <linestart># a<linestart># b<lineend>
	}
	
	/**
	 * Adds a list to a buffer
	 */
	private void addList(StringBuffer buffer, BufferedReader reader) throws IOException {
		char[] lastBullet = new char[0];
		String line = null;
		String trimLine = null;
		boolean requireLiEnd = false;
		while ((line = reader.readLine()) != null) {
			if (StringUtils.trim(line).length() == 0) {
				//new empty line - end of list
				break;
			}
			//only trim leading spaces, keep tailed spaces
			trimLine = StringUtil.trimStartSpace(line);
			
			int bulletEnd = StringUtils.indexOfAny(trimLine, new String[]{" ","\t"});
			if (bulletEnd < 1) {
				//if this line is not valid bullet line, then means this li has multiple lines...
				//please note, here append original line rather than trimmed and with newline - that eat by read.readLine()
				buffer.append("\n").append(line);
				continue;
			}
			
			String bStr = trimLine.substring(0, bulletEnd).trim();
			if(!bulletPattern.matcher(bStr).matches()){
				//if this line is not valid bullet line, then means this li has multiple lines...
				//please note, here append original line rather than trimmed and with newline - that eat by read.readLine()
				buffer.append("\n").append(line);
				continue;
			}
			
			//remove the possible dot, for example, #i. 
			if (bStr.charAt(bStr.length()-1) == '.') {
				bStr = bStr.substring(0,bStr.length()-1);
			}
			
			char[] bullet = bStr.toCharArray();
			if(requireLiEnd){
				buffer.append("</li>");
				requireLiEnd = false;
			}
			
			// check whether we find a new sub list, for example 
			//* list
			//** sublist
			int sharedPrefixEnd;
			for (sharedPrefixEnd = 0;; sharedPrefixEnd++) {
				if (bullet.length <= sharedPrefixEnd || lastBullet.length <= sharedPrefixEnd
						|| bullet[sharedPrefixEnd] != lastBullet[sharedPrefixEnd]) {
					break;
				}
			}

			for (int i = sharedPrefixEnd; i < lastBullet.length; i++) {
				// Logger.log("closing " + lastBullet[i]);
				buffer.append(closeList.get(Character.valueOf(lastBullet[i])));
			}
			
			for (int i = sharedPrefixEnd; i < bullet.length; i++) {
				// Logger.log("opening " + bullet[i]);
				buffer.append(openList.get(Character.valueOf(bullet[i])));
			}
			buffer.append("<li>");
			buffer.append(trimLine.substring(StringUtils.indexOfAny(trimLine, new String[]{" ","\t"}) + 1));
			requireLiEnd = true;
			lastBullet = bullet;
		}

		if(requireLiEnd){
			buffer.append("</li>");
		}
		for (int i = lastBullet.length - 1; i >= 0; i--) {
			buffer.append(closeList.get(Character.valueOf(lastBullet[i])));
		}

	}

	

}
