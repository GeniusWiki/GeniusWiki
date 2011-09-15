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
import java.util.ListIterator;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.RenderContext;

/**
 * Handle simple table: 
 * <pre>
 * 		  ||head1||header2||
 *		  |col1|col2|
 * </pre>
 *  
 * @author Dapeng.Ni
 */
public class TableFilter  extends BasePatternTokenFilter {
	public static final int MAX_ROW_TEXT_LEN = 1000; 
	//JDK1.6 @Override
	public void init(){
		regexProvider.compile(getRegex(), Pattern.MULTILINE);
	}
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> nodeIter, RenderContext context) {
		//need check if this HTML table could be replace by simple table. The basic requirement is
		//1. the text won't be too long, 
		//2. the cells do not contain any block element, such as <br>, <div> etc.
		//3. no colspan, rowspan or nonstandard attribute, style etc. So far,only class attribute support on <tr>

		HTMLNode pair = node.getPair();
		if(pair == null){
			log.error("Unexpected: no close table tag.");
			return;
		}
		
		//convert it to simple table
		HTMLNode subnode = node.next();
		boolean head = false;
		HTMLNode trCloseNode = null;
		while(subnode != null && subnode != pair){
			if(subnode.isCloseTag()){
				if(trCloseNode != null){
					trCloseNode.reset(head?"||":"|",true);
				}
				subnode = subnode.next();
				continue;
			}
			//reset tr to \n, th to ||, td to |
			if("tr".equalsIgnoreCase(subnode.getTagName())){
				subnode.reset(HTMLNode.LINE_START_TAG, false);
				trCloseNode = subnode.getPair();
			}else if("td".equalsIgnoreCase(subnode.getTagName())){
				if(hasVisibleContent(subnode))
					subnode.reset("|", true);
				else
					subnode.reset("| ", true);
				head = false;
				if(subnode.getPair() != null)
					subnode.getPair().reset("", true);
			}else if("th".equalsIgnoreCase(subnode.getTagName())){
				if(hasVisibleContent(subnode))
					subnode.reset("||", true);
				else
					subnode.reset("|| ", true);
				head = true;
				if(subnode.getPair() != null)
					subnode.getPair().reset("", true);
			}else if("tbody".equalsIgnoreCase(subnode.getTagName())){
				subnode.reset("", true);
				if(subnode.getPair() != null)
					subnode.getPair().reset("", true);
			}else if("caption".equalsIgnoreCase(subnode.getTagName())){
				subnode.reset("", true);
				if(subnode.getPair() != null)
					subnode.getPair().reset("", true);
			}
			subnode = subnode.next();
		}
		
		//clean table node
		node.reset("", true);
		pair.reset(HTMLNode.LINE_END_TAG, false);
		
	}
	

	/**
	 * simple check if there is visible content exist between subnode and its paired node.
	 * @return
	 */
	private boolean hasVisibleContent(HTMLNode subnode) {
		if(subnode.getPair() == null)
			return false;
		
		HTMLNode node = subnode.next();
		while(node != subnode.getPair()){
			//TODO: need further check to distinguish the visible content.
			if(node.getText().length() > 0)
				return true;
				
		}
		return false;
	}
	//JDK1.6 @Override
	public String getPatternKey() {
		return "filter.table";
	}
	@Override
	public void replace(StringBuffer buffer, MatchResult result, RenderContext context) {
		try {
			//this text does not contain leading newline (\n) but contain all tailed newline.
			//this text may contain multiple list which is separate by multiple newline 
			String wholeText = result.group(0);
			
			BufferedReader reader = new BufferedReader(new StringReader(wholeText));
			buildTable(buffer, reader);

			//don't eat last \n as NewlineFitler need it. do not need consider multiple empty line case, as it will do in addList().
			if(!StringUtil.endOfAny(buffer.toString(),new String[]{"\r","\n"}) && 
					StringUtil.endOfAny(wholeText,new String[]{"\r","\n"}))
				buffer.append("\n");
		} catch (Exception e) {
			log.warn("TableFilter: unable get table content", e);
		}
		
	}

	//********************************************************************
	//               private method
	//********************************************************************
	/**
	 * @param buffer
	 * @param reader
	 * @throws IOException 
	 */
	private void buildTable(StringBuffer buffer, BufferedReader reader) throws IOException {
		
		String line = null;
		boolean newtable = false;
		boolean evenLine = false;
		//this avoid multiple empty line case
		boolean firstLine = true;
		boolean hasTable = false;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (StringUtils.isBlank(line)) {
				//an empty line, then try to create another new table
				newtable = true;
				break;
			}
			if(firstLine){
				hasTable = true;
				buffer.append("<table class=\"macroTable\">");
				firstLine = false;
			}
			buffer.append("<tr");
			
			if(line.startsWith("||")){
				buffer.append(">");
				buffer.append(buildRow(line,true));
			}else{ //start with "|"
				buffer.append(evenLine?" class=\"even\"":" class=\"odd\"").append(">");
				buffer.append(buildRow(line,false));
				evenLine = !evenLine;
			}
			
			buffer.append("</tr>");
		}
		if(hasTable){
			buffer.append("</table>");
		}
		
		//OK, there are empty lines, then go on build another 
		if(newtable){
			//append \n so that don't eat out this empty line between table
			if(hasTable)
				buffer.append("\n");
			
			buffer.append("\n");
			buildTable(buffer,reader);
		}
	}
	/**
	 * @param line 
	 * @return
	 */
	private String buildRow(String line, boolean head) {
		String sep;
		String tag;
		if(head){
			//header
			sep = "||";
			tag = "th";
			line = trimSurronding(line);
		}else{
			sep ="|";
			tag = "td";
			line = trimSurronding(line);
		}
		StringBuffer sb = new StringBuffer();
		//split
		String[] columns = StringUtils.splitByWholeSeparatorPreserveAllTokens(line,sep);
		for (String col : columns) {
			sb.append("<"+tag+">").append(col.trim().length()==0?"&nbsp;":col).append("</"+tag+">");
		}
		return sb.toString();
	}
	/**
	 * Remove leading and tailed "|" 
	 * @param line 
	 * @param string
	 * @return
	 */
	private String trimSurronding(String line) {
		int len = line.length();
		int idx = 0;
		char[] val = line.toCharArray();

		while ((idx < len) && (val[idx] == '|')) {
		    idx++;
		}
		
		int end = len;
		while ((idx < end) && (val[end - 1] == '|')) {
		    end--;
		}
		return ((idx > 0) || (end < len)) ? line.substring(idx, end) : line;
	}


}
