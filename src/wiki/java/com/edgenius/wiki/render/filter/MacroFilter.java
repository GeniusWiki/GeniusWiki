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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.CompareToComparator;
import com.edgenius.core.util.StringEscapeUtil;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.html.HTMLNodeContainer;
import com.edgenius.wiki.render.Filter;
import com.edgenius.wiki.render.FilterRegxConstants;
import com.edgenius.wiki.render.GroupProcessor;
import com.edgenius.wiki.render.GroupProcessorMacro;
import com.edgenius.wiki.render.ImmutableContentFilter;
import com.edgenius.wiki.render.ImmutableContentMacro;
import com.edgenius.wiki.render.Macro;
import com.edgenius.wiki.render.MacroManager;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.MarkupUtil;
import com.edgenius.wiki.render.RegexProvider;
import com.edgenius.wiki.render.Region;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.render.TokenVisitor;
import com.edgenius.wiki.render.impl.BaseMacroParameter;
import com.edgenius.wiki.render.impl.JdkRegexProvider;

/**
 * @author Dapeng.Ni
 */
public class MacroFilter implements Filter, ImmutableContentFilter {
	private static final Logger log = LoggerFactory.getLogger(MacroFilter.class);

	private static final String MACRO_REGION_KEY_START = "S";
	private static final String MACRO_REGION_KEY_UNKNOWN = "U";
	private static final String MACRO_REGION_KEY_PROCESSED = "P";

	private RegexProvider<Matcher> pairedMacroProvider = new JdkRegexProvider();

	private RegexProvider<Matcher> singleMacroProvider = new JdkRegexProvider();
	
	private MacroManager macroMgr;

	private List<HTMLNode> immutableHTMLIds = new ArrayList<HTMLNode>();
	
	public void setMacroMgr(MacroManager macroMgr) {
		this.macroMgr = macroMgr;
	}
	//JDK1.6 @Override
	public void init() {
		
		pairedMacroProvider.compile(FilterRegxConstants.PAIRED_MACRO, Pattern.DOTALL);
		singleMacroProvider.compile(FilterRegxConstants.SINGLE_MACRO, Pattern.MULTILINE);
		Collection<Macro> list = macroMgr.getImmutableContentMacros();
		for (Macro macro : list) {
			String identifier = macro.getHTMLIdentifier();
			immutableHTMLIds.addAll(RenderUtil.parseHtmlIdentifier(identifier));
		}
		
	}

	/**
	 * Initial macro filter only for special Macro. 
	 * 
	 * At the moment, only {piece} macro need this method - which need get Phase Piece from entire page content...
	 * @param macro
	 */
	public void init(Macro macro, boolean immutable){
		pairedMacroProvider.compile(FilterRegxConstants.PAIRED_MACRO, Pattern.DOTALL);
		singleMacroProvider.compile(FilterRegxConstants.SINGLE_MACRO, Pattern.MULTILINE);
		
		macroMgr.addMacro(macro, immutable);
		
	}
	
	/**
	 * Some macro has group concept. For example, table and cell, tabs and tab. To process these 
	 * relative macro, this method parses text and insert a "group key" into these macro as a part of parameters. 
	 * During macro process, it is able to MacroParameter.getParam(Macro.GROUP_KEY) to get back the group key. 
	 * The group key contains this group unique number, the children macro unique number etc. For example,
	 * a 2x2 table will insert following group keys(edgnius_group_key as name). 
	 * <pre>
	    {table:edgnius_group_key=0}
		{cell:edgnius_group_key=0-0-0-2}
		123
		{cell}
		{cell:edgnius_group_key=0-0-1-2}
		abc
		{cell}
		{rowdiv:edgnius_group_key=0-1}
		{cell:edgnius_group_key=0-1-0-2}
		456
		{cell}
		{cell:edgnius_group_key=0-1-1-2}
		def
		{cell}
		{table}
	 * </pre>
	 * 
	 * For 2 tabs deck:
	 * <pre>
		{tabs:edgnius_group_key=0}
		{tab:edgnius_group_key=0-1|name=work}
		working for money
		{tab}
		{tab:edgnius_group_key=0-2|name=life}
		life for fun
		{tab}
		{tabs}
	 * </pre>
	 * @param text
	 * @return
	 */
	public String initialGroup(String text) {
		CharSequence  buffer = MarkupUtil.hideEscapeMarkup(text);
		
		LinkedList<GroupProcessor> stack = new LinkedList<GroupProcessor>();
		List<GroupProcessor> processors = new ArrayList();
		checkGroup(0,buffer, stack, processors);
		
		//insert group id into text
		if(processors.size() > 0){
			//sort processor first, so that it becomes easier when insert - just insert one by one 
			//without worries of position change impact
			Map<Integer,GroupProcessor> borders = new TreeMap<Integer,GroupProcessor>(
							new CompareToComparator(CompareToComparator.TYPE_OVERWRITE_SAME_VALUE|CompareToComparator.DESCEND));
			for (GroupProcessor gp : processors) {
				Set<Integer> posList = gp.getPositions();
				for (Integer pos : posList) {
					borders.put(pos,gp);
				}
			}
			
			//recreate new stringBuilder as buffer(from first line of this method) is escaped
			StringBuilder sb = new StringBuilder(text);
			//on behalf of above sort result, insert is simple, just looping borderPoint one by one as new insert won't impact 
			//the others un-inserted point location.
			for(Entry<Integer,GroupProcessor> entry: borders.entrySet()){
				GroupProcessor gp = entry.getValue();
				int start = entry.getKey();
				if(gp.suppressNewlineBetweenElements()){
					//Here want to remove newline between table and cells. There are 2 tricks here, as example,
					//(A){table} 
					//(B1){cell}abc{cell} (B)
					//(C1){cell}123{cell} (C)
					//{table} (D)
					//surroundMacro is table, it start from the 2nd cell, its end is (C) then. as GroupProcessor construct method
					//getPreviousMacroStart() return (D) first.  Here is assume {table} macro doesn't have newline!
					//so it is safe remove all newline between (C) and (D).  
					// Then next process is (B) and (BC), next is (A) and (B1), Here is also assume {table} macro doesn't have newline.
					// The (A) is before {table} is because GroupProcessor construct method uses surrounding macro start as "end" 
					int end = gp.getMacroEnd(start);
					if(end != -1){
						int prevStart = gp.getPreviousMacroStart();
						//remove all newline between end(current macro) and previous processed macro start
						if(end <= prevStart){
							//if equals, means 2 group is conjunction without any character. such as {cell}{cell} - no space or any character between cell markup.
							if(end < prevStart){
								sb.replace(end, prevStart, sb.subSequence(end, prevStart).toString().replaceAll("\n", ""));
							}
						}else{
							AuditLogger.warn("GroupProcessor current macro end is less than last macro start:"+end+">"+start+". Overlapped!");
						}
					}else{
						AuditLogger.warn("GroupProcessor doesn't find the macro end value by start "+start);
					}
				}
				//after process done, set new current macro start - where any unexpected happens, always setPreviousMacroStart to latest!
				//as insertGroupKey() will change text content.
				gp.setPreviousMacroStart(start);
				gp.insertGroupKey(start, sb);
			}
			return sb.toString();
		}else{
			return text;
		}
	}
	/**
	 */
	private void checkGroup(final int initPos, CharSequence input, final LinkedList<GroupProcessor> stack, List<GroupProcessor> processors) {
		final List<Region> pairRegions = new ArrayList<Region>();
		
		singleMacroProvider.replaceByTokenVisitor(input, new TokenVisitor<Matcher>() {
			public void handleMatch(StringBuffer buffer, Matcher result) {
				String macroName = result.group(1);
				if (macroName != null && !macroName.startsWith("$")) {
					Macro macro = macroMgr.getMacro(macroName);
					
					if(macro != null){
						//IMPORTANT: here does not check Macro.isPair() and also put it into pairRegions for following process
						//it is the sequence of process must keep consistant with physical sequence in input text, 
						//for example, {table}{cell}...{rowdiv}, rowdiv is single and must be after cell
						int start = result.start(0);
						int end = result.end(0);
						Region pair = new Region(start,end);
						//no parameter, then mark as unknown, otherwise, must be a start macro
						if(StringUtils.isBlank(result.group(2))){
							pair.setKey(MACRO_REGION_KEY_UNKNOWN);
						}else{
							pair.setKey(MACRO_REGION_KEY_START);
						}

						//just for temporary to remember the macro name...
						pair.setContent(macroName);
						//sum to list
						pairRegions.add(pair);
					}
				}
			}
		});
		

		int size = pairRegions.size();
		if(size > 0){
			StringBuffer inputBuf = new StringBuffer(input);
			for(int idx=0;idx<size;idx++){
				Region reg = pairRegions.get(idx);
				Macro macro = macroMgr.getMacro(reg.getContent());
				if(macro.isPaired()){
					int deep = 0;
					Region pair = null;
					//looking for pairs...
					for(int chIdx=idx+1;chIdx<size;chIdx++){
						Region next = pairRegions.get(chIdx);
						if(StringUtils.equalsIgnoreCase(reg.getContent(), next.getContent())){
							//start is unknown (no attribute), then end must be unknown
							if(MACRO_REGION_KEY_UNKNOWN.equals(reg.getKey())
								&& MACRO_REGION_KEY_UNKNOWN.equals(next.getKey())){
								//matched
								pair = next;
								//skip all internal node - which is handle by embedded recursive
								idx=chIdx;
								break;
							}
							
							if(MACRO_REGION_KEY_START.equals(reg.getKey())
								&& MACRO_REGION_KEY_UNKNOWN.equals(next.getKey())){
								if(deep == 0){
									//matched;
									pair = next;
									//skip all internal node - which is handle by embedded recursive
									idx=chIdx;
									break;
								}else{
									//just another inner same name macro matched, deep minus 1
									deep--;
								}
							}
							if(MACRO_REGION_KEY_START.equals(next.getKey())){
								//ok, it gets another start, in 4th scenarios - then add deep
								deep++;
							}
						}
					}
					//ok, success find paired
					if(pair != null){
						int start = initPos + reg.getStart();
						int end = initPos + pair.getEnd();
						int contentStart = initPos + reg.getEnd(); 
						int contentEnd= initPos + pair.getStart();
						
						GroupProcessor currProcessor = stack.size() == 0?null:stack.getLast();
						if(currProcessor != null){
							currProcessor.adoptChild(macro,start,end);
						}
		
						
						if (macro.isProcessEmbedded() && (end > start)) {
							if(macro.hasChildren() != null){
								stack.add(((GroupProcessorMacro)macro).newGroupProcessor(macro, start,end));
							}
							checkGroup(contentStart, inputBuf.subSequence(contentStart-initPos, contentEnd-initPos),stack, processors);
							if(macro.hasChildren() != null){
								//pop the current one, means it is a completed GroupProcessor
								processors.add(stack.removeLast());
							}
						}
					}
				}else{
					//single macro - directly detect if it is child
					GroupProcessor currProcessor = stack.size() == 0?null:stack.getLast();
					if(currProcessor != null){
						currProcessor.adoptChild(macro,initPos + reg.getStart(),initPos + reg.getEnd());
					}
				}
			}
		}
	}
	
	/**
	 * This method will call before any filter execution as it will mark some region which want to keep original text content.
	 *  
	 * @see com.edgenius.wiki.render.ImmutableContentMacro
	 * @param input
	 * @param context
	 */
	//JDK1.6 @Override
	public List<Region> getRegions(final CharSequence input) {
		
		List<Region> regions = new ArrayList<Region>();
		resetRegion(0,input, regions);
		return regions;
		
	}

	private void resetRegion(final int initPos, final CharSequence input,final List<Region> list){

		
		final List<Region> pairRegions = new ArrayList<Region>();
		
		singleMacroProvider.replaceByTokenVisitor(input, new TokenVisitor<Matcher>() {
			public void handleMatch(StringBuffer buffer, Matcher result) {
				String macroName = result.group(1);
				if (macroName != null && !macroName.startsWith("$")) {
					Macro macro = macroMgr.getMacro(macroName);
					if(macro != null && macro.isPaired()){
						String body = result.group(0);
						int start = result.start(0);
						int end = result.end(0);
						Region pair = new Region(start,end);
						//no parameter, then mark as unknown, otherwise, must be a start macro
						if(StringUtils.isBlank(result.group(2))){
							pair.setKey(MACRO_REGION_KEY_UNKNOWN);
						}else{
							pair.setKey(MACRO_REGION_KEY_START);
						}

						//just for temporary to remember the macro name...
						pair.setContent(macroName);
						pair.setBody(body);
						//sum to list
						pairRegions.add(pair);
					}
				}
			}
		});
		

		int size = pairRegions.size();
		if(size > 0){
			StringBuffer inputBuf = new StringBuffer(input);
			for(int idx=0;idx<size;idx++){
				Region reg = pairRegions.get(idx);
				int deep = 0;
				Region pair = null;
				//looking for pairs...
				for(int chIdx=idx+1;chIdx<size;chIdx++){
					Region next = pairRegions.get(chIdx);
					if(StringUtils.equalsIgnoreCase(reg.getContent(), next.getContent())){
						//start is unknown (no attribute), then end must be unknown
						if(MACRO_REGION_KEY_UNKNOWN.equals(reg.getKey())
							&& MACRO_REGION_KEY_UNKNOWN.equals(next.getKey())){
							//matched
							pair = next;
							//skip all internal node - which is handle by embedded recursive
							idx=chIdx;
							break;
						}
						
						if(MACRO_REGION_KEY_START.equals(reg.getKey())
							&& MACRO_REGION_KEY_UNKNOWN.equals(next.getKey())){
							if(deep == 0){
								//matched;
								pair = next;
								//skip all internal node - which is handle by embedded recursive
								idx=chIdx;
								break;
							}else{
								//just another inner same name macro matched, deep minus  
								deep--;
							}
						}
						if(MACRO_REGION_KEY_START.equals(next.getKey())){
							//ok, it gets another start, in 4th scenarios - then add deep
							deep++;
						}
					}
				}
				//ok, success find paired
				if(pair != null){
					int start = initPos + reg.getStart();
					int end = initPos + pair.getEnd();
					int contentStart = initPos + reg.getEnd(); 
					int contentEnd= initPos + pair.getStart();
					
					String macroName = reg.getContent();
					Macro macro = macroMgr.getMacro(macroName);
					boolean immutable = macro instanceof ImmutableContentMacro;
					
					list.add(new Region(MacroFilter.this,immutable,start, end,  contentStart,contentEnd));
					if (macro.isProcessEmbedded() && (end > start)) {
						resetRegion(contentStart, inputBuf.subSequence(contentStart-initPos, contentEnd-initPos),list);
					}
				}
			}
		}
	
	}

	/**
	 * Get all html identifiers if macro implements ImmutableContentMacro interface
	 */
	public List<HTMLNode> getImmutableHTMLIdenifiers() {
		
		return immutableHTMLIds;
	}

	//JDK1.6 @Override
	public String filter(String input, final RenderContext context) {

		final List<Region> pairRegion = new ArrayList<Region>();
		
		String result =  singleMacroProvider.replaceByTokenVisitor(input, new TokenVisitor<Matcher>() {
			public void handleMatch(StringBuffer buffer, Matcher matcher) {
				handleMacro(false, buffer, matcher.toMatchResult(), context, pairRegion,null);
			}

		});
		
		// scenarios (s = start, u = unknown - no any attributes in macro, so could be start or end):
		// A B B A - B is process by recursive inside A
		// A B A B - B is treat as invalid - ignore
		// Au Au Au Au - First 2 Au is paired, Last 2 Au is paired
		// As1 As2 Au2 Au1 - OK, this case is special, which can not process by original Paired Regex patter
		// 					because it will treat it as As1 and Au2 as paired, but ignore As2 and Au1. 
		//					Here just try to resolve this problem. 
		//exceptions
		// As1 As2 Au1 - then As1 can not find pair - no processed , but As2 could match with Au1
		
		int size = pairRegion.size();
		if(size > 0){
			StringBuffer inputBuf = new StringBuffer(result);
			for(int idx=0;idx<size;idx++){
				Region reg = pairRegion.get(idx);
				int deep = 0;
				Region pair = null;
				//looking for pairs...
				for(int chIdx=idx+1;chIdx<size;chIdx++){
					Region next = pairRegion.get(chIdx);
					if(StringUtils.equalsIgnoreCase(reg.getContent(), next.getContent())){
						//start is unknown (no attribute), then end must be unknown
						if(MACRO_REGION_KEY_UNKNOWN.equals(reg.getKey())
							&& MACRO_REGION_KEY_UNKNOWN.equals(next.getKey())){
							//matched
							pair = next;
							//skip all internal node - which is handle by embedded recursive
							idx=chIdx;
							break;
						}
						
						if(MACRO_REGION_KEY_START.equals(reg.getKey())
							&& MACRO_REGION_KEY_UNKNOWN.equals(next.getKey())){
							if(deep == 0){
								//matched;
								pair = next;
								//skip all internal node - which is handle by embedded recursive
								idx=chIdx;
								break;
							}else{
								//just another inner same name macro matched, deep minus  
								deep--;
							}
						}
						if(MACRO_REGION_KEY_START.equals(next.getKey())){
							//ok, it gets another start, in 4th scenarios - then add deep
							deep++;
						}
					}
				}
				//ok, success find paired
				if(pair != null){
					CharSequence macroContent = inputBuf.subSequence(reg.getStart(), pair.getEnd());
					//for example, {font:size=12}abc{font}, the value is start markup string,i.e., {font:size=12}
					//so far, this text is useful to display markup if it has errors - which need highlight and with hover text 
					final String pairStartMarkup = pair.getBody();
					result = pairedMacroProvider.replaceByTokenVisitor(macroContent, new TokenVisitor<Matcher>() {
						public void handleMatch(StringBuffer buffer, Matcher matcher) {
							handleMacro(true, buffer, matcher.toMatchResult(), context, null, pairStartMarkup);
						}
					});
					reg.setBody(result);
					reg.setSubRegion(pair);
					reg.setKey(MACRO_REGION_KEY_PROCESSED);
				}
			}
			
			//reverse, and replace input by region processed string (region.getBody())
			for(int idx=size-1;idx>=0;idx--){
				Region reg = pairRegion.get(idx);
				if(!MACRO_REGION_KEY_PROCESSED.equals(reg.getKey()))
					continue;
				
				inputBuf.replace(reg.getStart(), reg.getSubRegion().getEnd(), reg.getBody());
			}
			
			return inputBuf.toString();
		}
		
		
		
		return result;
	}
	
	//JDK1.6 @Override
	public HTMLNodeContainer filter(HTMLNodeContainer nodeList, RenderContext context) {
		Collection<Macro> macros = macroMgr.getMacros();
		for (Macro macro : macros) {
			nodeList = macro.filter(nodeList, context);
		}
		return nodeList;
	}
	//********************************************************************
	//               private methods
	//********************************************************************
	private void handleMacro(boolean paired, StringBuffer buffer, MatchResult result, RenderContext context, 
			List<Region> pairRegions, String startTagBody) {
		//don't eat first character, which is checking for Escape "\"
		//result group: 1-macro name, 2-parameter, 3-surrounding content
		String macroName = result.group(1);

		if (macroName != null) {
			// {$peng} are variables not macros.
			if (!macroName.startsWith("$")) {
				try {
					Macro macro = macroMgr.getMacro(macroName);
					if(macro.isPaired() != paired){
						String body = result.group(0);
						int start = buffer.length();
						int end = start + body.length();
						Region pair = new Region(start,end);
						//no parameter, then mark as unknown, otherwise, must be a start macro
						if(StringUtils.isBlank(result.group(2))){
							pair.setKey(MACRO_REGION_KEY_UNKNOWN);
						}else{
							pair.setKey(MACRO_REGION_KEY_START);
						}
						pair.setContent(macroName);
						pair.setBody(body);
						//sum into list
						pairRegions.add(pair);
						
						buffer.append(body);
						return;
					}
					
					BaseMacroParameter mParams = new BaseMacroParameter();
					mParams.setMacroName(macroName);
					mParams.setRenderContext(context);
					
					switch (result.groupCount()) {
						case 3:
							if(macro instanceof ImmutableContentMacro){
								if(context.getCurrentRegion() == null){
									AuditLogger.error("Unexpected case: Immutable fitler cannot find out current region." + result.group());
								}

								mParams.setContent(context.getCurrentRegion() != null? context.getCurrentRegion().getContent(): result.group(3));
							}else{
								mParams.setContent(result.group(3));
							}
							//no break!
						case 2:
							//parameter won't be HTML entity format. And macro parameter value slash unescape will do while 
							//parameter parsed, inside MacroParameter.setParams().
							mParams.setParams(StringEscapeUtil.unescapeHtml(result.group(2)));
					}

					// recursively filter macros within macros
					if (macro.isProcessEmbedded() && mParams.getContent() != null) {
						mParams.setContent(filter(mParams.getContent(), context));
					}
					
					if(mParams.getContent()==null)
						mParams.setContent("");
					
					//if paired markup, the startTagBody is parsed from filter beginning singleMarkupParser...
					//for single markup, then just the whole text.
					mParams.setStartMarkup(paired?startTagBody:result.group(0));
					
					macro.execute(buffer, mParams);
				} catch (MalformedMacroException e) {
					buffer.append(RenderUtil.renderError("Invalid macro: " + e.getMessage(), result.group(0)).toString());
				} catch (Throwable e) {
					log.warn("MacroFilter: unable to format macro: " + result.group(0));
					buffer.append(result.group(0));
					return;
				}
			} else {
				// {$peng}, so far, do nothing for this format
				buffer.append(result.group(0));
			}
		} else {
			//unable to parse out correct macro name, render original String
			buffer.append(result.group(0));
		}
	}




}
