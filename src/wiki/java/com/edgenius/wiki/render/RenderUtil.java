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
package com.edgenius.wiki.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.CompareToComparator;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.filter.MacroFilter;
import com.edgenius.wiki.render.impl.BaseMacroParameter;
import com.edgenius.wiki.render.impl.MacroManagerImpl;
import com.edgenius.wiki.render.impl.RenderContextImpl;
import com.edgenius.wiki.render.macro.PieceMacro;
import com.edgenius.wiki.render.object.ObjectPosition;

/**
 * @author Dapeng.Ni
 */
public class RenderUtil {

	
	public static TextModel renderError(String errorText, String original){
		//surround text by error tag(red) and hover error message
		return new TextModel(new StringBuffer("<span aid=\"") //+ SharedConstants.NO_RENDER_TAG
				//I original use title as field name, but it is weird, GWT render always remove that field, so use "hint" now.
				.append(SharedConstants.RENDER_ERROR_TAG)
				.append("\" hint=\"")
				.append(EscapeUtil.escapeBySlash(errorText, new char['"'])) 
				.append("\" class='renderError'>")
				.append(original)
				.append("</span>").toString()); 
	}

	/**
	 * So far, simply split by "||", e.g., "<span style=font-weight:bold>||<strong>"
	 * Future, it may need more complicated expression, such as "(a&&c)||d"
	 */
	public static List<HTMLNode> parseHtmlIdentifier(String htmlID) {
		List<HTMLNode> list = new ArrayList<HTMLNode>();
		if(StringUtil.isBlank(htmlID))
			//return empty list
			return list;
		
		String[] ids = htmlID.split("\\|\\|");
		for (String id : ids) {
			list.add(new HTMLNode(id,false));
		}
		return list;
	}

	/**
	 * If given content start or end with new line, delete it. Only trim first or last new line even it has multiple new line surronding.
	 * @param content
	 * @return
	 */
	public static String trimSurrondingNewline(String content) {
		if(content == null)
			return content;
		int sIdx = 0,eIdx=content.length();
		if(content.startsWith("\r") || content.startsWith("\n"))	
			sIdx =1;
		if(content.startsWith("\r\n"))	
			sIdx =2;
		if(content.endsWith("\r") || content.endsWith("\n"))	
			eIdx = eIdx-1;
		if(content.endsWith("\r\n"))	
			eIdx = eIdx-2;
		
		if(sIdx !=0 || eIdx != content.length())
			content = content.substring(sIdx,eIdx);
		
		return content;
	}


	/**
	 * @param subnode
	 * @return
	 */
	public static boolean isBlockTag(HTMLNode node) {
		for (String block : WikiConstants.ISBLOCK_TAGS) {
			if (StringUtils.equalsIgnoreCase(block, node.getTagName())) {
				//also need check if its CSS does not contain  "display:inline"
				if(node.getStyle() == null || !NameConstants.INLINE.equalsIgnoreCase(node.getStyle().get(NameConstants.DISPLAY))){
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * @param cursor
	 * @return
	 */
	public static boolean isVisibleTag(HTMLNode cursor) {
		if(cursor.getAttributes() != null){
			String aid = cursor.getAttributes().get(NameConstants.AID);
			if(aid != null && SharedConstants.NO_RENDER_TAG.equalsIgnoreCase(aid)){
				return false;
			}
		}
		//this list may be change according to my test result...
		if("img".equalsIgnoreCase(cursor.getTagName()))
			return true;
		
		return false;
	}
	/**
	 * Check if given string is end by a Blocked HTML tag (see RenderUtil.isBlockTag());
	 * The "end" means the except spaces or tab mark (it won't be newline mark! please see code in NewLineFilter.replace()
	 * , the last visible String must be some tag and tag must be blocked tag.
	 * 
	 * @param buffer
	 */
	public static boolean isEndByBlockHtmlTag(StringBuffer buffer) {
	
		if (buffer != null && buffer.length() > 0) {
			String before = buffer.toString();
	
			int len = before.length();
			//0 start looking close tag '>'; 1 in last tag string; 2 need find paired tag to check
			int looking = 0; 
			StringBuffer tag = null;
			HTMLNode node = null,pair=null;
			for(int idx=len-1;idx>=0;idx--){
				char ch = before.charAt(idx);
				if(looking == 0 ){
					if(ch == '>' ){
						looking = 1;
						tag = new StringBuffer();
						tag.append(ch);
						continue;
					}else if(ch !=' ' && ch != '\t'){
						break;
					}
				}else if(looking == 1){
					tag.insert(0, ch);
					if(ch == '<'){
						node = new HTMLNode(tag.toString(),false);
						if(node.isCloseTag()){
							//have to continue to looking up its open tag to decide
							looking = 2;
						}else{
							//it is not close tag, then I can decide if it is close tag now
							return isBlockTag(node);
						}
					}
				}else if(looking == 2){
					if(ch == '>' ){
						looking = 3;
						tag = new StringBuffer();
						tag.append(ch);
						continue;
					}
				}else if(looking == 3){
					tag.insert(0, ch);
					if(ch == '<'){
						pair = new HTMLNode(tag.toString(),false);
						if(pair.isPaired(node)){
							return isBlockTag(pair);
						}else{
							//it is not paired open tag, have to continue;
							looking = 2;
							continue;
						}
					}
				}
				
				
			}
		}
		return false;
	}

	/**
	 * @param input
	 * @param key
	 * @param regions
	 * @param newlineKey 
	 * @return
	 */
	public static  CharSequence createRegion(CharSequence input, String key, Collection<Region> regions, String newlineKey) {
		if(regions == null || regions.size() == 0)
			return input;
		
		//first, web split whole text by special border tag string some think like "key_regionKey_S|E"
		input = createRegionBorder(input, key, regions,newlineKey);
		
		//then we split them one by one. The split is dependent on the RegionComparator(), which ensure the split 
		//from end to start, and from inside to outside. And this makes easier on below replacement process.
		Set<Region> sortRegions = new TreeSet<Region>(new RegionComparator());
		sortRegions.addAll(regions);
		
		StringBuilder buf = new StringBuilder(input);
		StringBuilder regKey = new StringBuilder(key);
		int ks = key.length();
		for (Region region : sortRegions) {
			//See our issue http://bug.edgenius.com/issues/34
			//and SUN Java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337993
			Pattern pat = Pattern.compile(new StringBuilder(key).append(region.getKey()).append("S(.*?)")
						.append(key).append(region.getKey()).append("E").toString(), Pattern.DOTALL);
			try {
				Matcher matcher = pat.matcher(buf);
				if(matcher.find()){
					region.setBody(matcher.group(1));
					buf.delete(matcher.start(), matcher.end());
					regKey.delete(ks, regKey.length());
					buf.insert(matcher.start(), regKey.append(region.getKey()).append(Region.REGION_SUFFIX));
				}
			} catch (StackOverflowError e) {
				AuditLogger.error("StackOverflow Error in RenderUtil.createRegion. Input[" 
						+ buf+"]  Pattern [" + pat.pattern()+ "]");
			} catch (Throwable e) {
				AuditLogger.error("Unexpected error in RenderUtil.createRegion. Input[" 
						+ buf+"]  Pattern [" + pat.pattern()+ "]",e);
			}	
		}
		
		return buf;
	}
	
	/**
	 * @param text
	 * @param key
	 * @param regions
	 */
	public static  CharSequence createRegionBorder(CharSequence text, String key, Collection<Region> regions, String newlineKey) {
		if(regions == null || regions.size() == 0)
			return text;
		
		//sort border separators from end to start, so that it can easily insert into whole text from end to start,
		//and need not care about string change
		
		//the key is border point value, value is Set as it may have duplicated start or end in same point.
		Map<Integer,Set<RegionBorderPoint>> borders = new TreeMap<Integer,Set<RegionBorderPoint>>(
						new CompareToComparator(CompareToComparator.TYPE_OVERWRITE_SAME_VALUE|CompareToComparator.DESCEND));
		
		int index = 1;
		for (Region region : regions) {
			int start = region.getStart();
			int end = region.getEnd();
			String newlineFix = new StringBuilder(text.subSequence(start, end)).indexOf("\n") == -1?"":newlineKey;
			String regionKey = key + index + newlineFix;
			region.setKey(index + newlineFix);
			region.setKeyIndex(index);
			//~~~~~~~~~ start
			Set<RegionBorderPoint> list = borders.get(start);
			if(list == null){
				list = new TreeSet<RegionBorderPoint>(new RegionBorderPointComparator());
				list.add(new RegionBorderPoint(start,true,end,regionKey, index));
				borders.put(start, list);
			}else{
				list.add(new RegionBorderPoint(start,true,end,regionKey, index));
			}
			//~~~~~~~~~ end
			list = borders.get(end);
			if(list == null){
				list = new TreeSet<RegionBorderPoint>(new RegionBorderPointComparator());
				list.add(new RegionBorderPoint(end,false,start,regionKey, index));
				borders.put(end, list);
			}else{
				list.add(new RegionBorderPoint(end,false,start,regionKey, index));
			}
			index++;
		}
		
		//ok, here will do insert border key into original string 
		StringBuilder sb = new StringBuilder(text);
		//on behalf of above sort result, insert is simple, just looping borderPoint one by one as new insert won't impact 
		//the others un-inserted point location.
		for(Set<RegionBorderPoint> set: borders.values()){
			for (RegionBorderPoint point : set) {
				String regionKey = point.getRegionKey() +(point.isStart()?"S":"E");
				sb.insert(point.getPoint(),regionKey);
			}
		}
		return sb;
		
	}	
	/**
	 * @param subRegion
	 * @param subPieces
	 * @param context
	 */
	public static void serialPieceTo(String name, Region subRegion, List<RenderPiece> subPieces, RenderContext context) {
		if(context.getObjectList() != null && context.getObjectList().size() > 0){
			ObjectPosition linkPos = null;
			for (ObjectPosition pos: context.getObjectList()) {
				if(pos.uuid.equals(subRegion.getKey())){
					linkPos = pos;
					break;
				}
			}
			if(linkPos != null){
				if(subPieces != null && subPieces.size() > 0){
					linkPos.values.put(name, serialPieces(subPieces));
				}else{
					//do nothing? view is empty? keep original view
				}
			}
		}
	}


	/**
	 * @param subPieces
	 * @return
	 */
	public static  String serialPieces(List<RenderPiece> subPieces) {
		StringBuffer buf = new StringBuffer();
		for (RenderPiece piece : subPieces) {
			//TODO: this maybe not good??? for example, image title has link, 
			//but this linkModel will convert to pure text only with View?
			buf.append(piece.toString());
		}
		return buf.toString();
	}

	/**
	 * @param text
	 * @param context
	 * @return
	 */
	public static String buildRegions(List<RegionContentFilter> filters, String text, RenderContext context) {
		//as all filter won't check escape "\", so here will hide escape.
		//it is not easy to detect escape for filter as you need check it is odd or even number of "\" before markup character
		//the reason use hideEscapeMarkup() rather than directly do escapeToHMTLEntity is, here need
		//find out Immutable Filter original content, hide() method will <strong>keep input text length unchanged</strong>, so that 
		//we can use text.substring(s,e) to get back original text after reverse hidden escape back.
		CharSequence  buffer = MarkupUtil.hideEscapeMarkup(text);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// get regions for all RegionContentFilter
	
		//please NOTE: the regions sequence is important as grouped macro reply on it. Such as table macro and its rows and cells...
		//table must be first then its embedded cells which are from first to last...
		List<Region> regions = null;
		for (RegionContentFilter filter : filters) {
			List<Region> list = filter.getRegions(buffer);
			
			//merge regions
			if(list != null && list.size() > 0){
				if(regions == null){
					regions = new ArrayList<Region>();
				}
				//region replacement rule: bigger eat contained smaller, older eat overlapped younger
				for (Iterator<Region> newRegionIter = list.iterator();newRegionIter.hasNext();) {
					Region region = newRegionIter.next();
					for (Iterator<Region> existRegionIter = regions.iterator();existRegionIter.hasNext();) {
						Region exist = existRegionIter.next();
						if(region.isContain(exist) && region.isImmutable()){
							//for example !my {pre}img name{pre}.png!, LinkFilter is immutable, then {pre} won't be render
							//Macro filter runs before ImageFilter, so exist Region is between {pre}, ImageFilter
							//will mark another region between "!", so the large region(image) will replace smaller one(pre). 
							existRegionIter.remove();
						}
						if(exist.isImmutable() && (exist.isContain(region) || exist.isOverlap(region))){
							//Overlap:  {pre} has some markup !{pre}is image.png!,  here {pre} and ! has overlap,
							//but the first "!" is invalid markup as it is inside {pre} macro
							//Contain: {pre}!image.png!{pre}, image won't be render as well 
							newRegionIter.remove();
							break;
						}
						if(!exist.isImmutable() && region.isOverlap(exist)){
							//Contain should be fine, as quote content also need render:{quote}!image.png!{quote} 
							// but overlap still ignore latter one: {quote} has some markup !{quote}is image.png! 
							newRegionIter.remove();
							break;
						}
					}
				}
				
				if(!list.isEmpty()){
					//only add these without overlaps with old region
					regions.addAll(list);
				}
			}
		}
	
	
		if(regions != null){
			//this is just primary key, so it won't use multiple mode, but the regionKey will be dependent on the region content 
			String primary = context.createUniqueKey(false);
			((RenderContextImpl)context).setRegionPrimaryKey(primary);
			
			//set immutable region content: it cannot set in Filter.getRegion(), as content has done hideEscapeMarkup()
			for (Region region : regions) {
				if(region.isImmutable()){ 
					//use original text to set content
					region.setContent(text.substring(region.getContentStart(),region.getContentEnd()));
				}
				if(region.getSubRegion() != null){
					//use original text to set content
					region.getSubRegion().setContent(text.substring(region.getSubRegion().getContentStart(),region.getSubRegion().getContentEnd()));
				}
			}
			
			((RenderContextImpl)context).setRegions(regions);
			
			// Create region border by key: text is original content, can not use buffer as it is changed by MarkupUtil.hideEscapeMarkup()
			buffer = createRegion(new StringBuilder(text), primary, context.getRegions(), context.createUniqueKey(true));
			return buffer.toString();
		}else{
			//go back original text
			return text;
		}
	}

	/**
	 * {piece:name=foo}content{piece} - return content between piece macro
	 * @param content - markup
	 * @return
	 */
	public static String getPiece(String content, String pieceName) {
		if(StringUtils.isBlank(pieceName))
			return content;
		
		//do escape first, then find valid {piece}
		CharSequence text = MarkupUtil.hideEscapeMarkup(content);

		Macro pieceMacro = new PieceMacro();
		MacroFilter filter = new MacroFilter();
		filter.setMacroMgr(new MacroManagerImpl());
		filter.init(pieceMacro, false);

		List<Region> regions = filter.getRegions(text);
		if(regions == null || regions.size() == 0){
			//must return null ! it is required by called method PageService.renderPhasePiece()
			return null;
		}
		
		BaseMacroParameter mParams = new BaseMacroParameter();
		for (Region region : regions) {
			//check if piece macro name is given name, return first matched
			String piece = content.subSequence(region.getStart(), region.getEnd()).toString();
			
			//See our issue http://bug.edgenius.com/issues/34
			//and SUN Java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337993
			try {
				Matcher m = FilterRegxConstants.PIECE_MACRO_START_TAG_PATTERN.matcher(piece);
				if(m.find()){
					mParams.setParams(m.group(1));
					if(pieceName.equalsIgnoreCase(mParams.getParam(NameConstants.NAME))){
						//find the matched piece! because region.body is escaped content, so substring to get back original context
						return  content.substring(region.getContentStart(),region.getContentEnd());
					}
				}
			} catch (StackOverflowError e) {
				AuditLogger.error("StackOverflow Error in RenderUtil.getPiece. Input[" + piece+"] Pattern [" 
						+ FilterRegxConstants.PIECE_MACRO_START_TAG_PATTERN.pattern()+ "]");
			} catch (Throwable e) {
				AuditLogger.error("Unexpected error in RenderUtil.getPiece. Input[" + piece+"] Pattern [" 
						+ FilterRegxConstants.PIECE_MACRO_START_TAG_PATTERN.pattern()+ "]",e);
			}
		}
		
		//no give piece name
		return null;
	}

	/**
	 * Detect if content contains {blog} macro
	 * @param content
	 * 
	 * @return
	 */
	public static boolean hasBlogMacro(String content) {
		//replace invalid {blog} macro, aka, leading with odd number slash, then decide if it is {blog} macro
		CharSequence text = MarkupUtil.hideEscapeMarkup(content);
		
		try {
			Matcher m = FilterRegxConstants.BLOG_FILTER_PATTERN.matcher(text);
			return m.find();
		} catch (StackOverflowError e) {
			AuditLogger.error("StackOverflow Error in RenderUtil.hasBlogMacro. Input[" 
					+ text+"]  Pattern [" + FilterRegxConstants.BLOG_FILTER_PATTERN.pattern()+ "]");
		} catch (Throwable e) {
			AuditLogger.error("Unexpected error in RenderUtil.hasBlogMacro. Input[" 
					+ text+"]  Pattern [" + FilterRegxConstants.BLOG_FILTER_PATTERN.pattern()+ "]",e);
		}
		//failure tolerance just be safe 
		return content.toLowerCase().indexOf("${blog") != -1;
	}
	/**
	 * Parse macro string and return macro name and its parameters. The value from key of WikiConstants.MACRO_NAME_KEY in return map 
	 * is macro name then with its parameters. This input suppose only has one macro (the first is parse if there are multiple macro) 
	 * and this macro doesn't  be check if esacped,i.e., like \{macor} also is treat as valid macro.  
	 * If input hasn't valid macro, null is returned.
	 * @param macro
	 * @return
	 */
	public static Map<String,String> parseSignleMacro(String macro){
		if(StringUtils.isBlank(macro))
			return null;
		
		MatchResult rs = FilterRegxConstants.SINGLE_MACRO_FILTER_PATTERN.matcher(macro).toMatchResult();
		int count = rs.groupCount();
		if(count < 1)
			return null;
		
		Map<String,String> out = new HashMap<String,String>();
		out.put(WikiConstants.MACRO_NAME_KEY, rs.group(1));
		if(count > 1){
			BaseMacroParameter params = new BaseMacroParameter();
			params.setParams(rs.group(2));
			out.putAll(params.getParams());
		}		
		return out; 
	}
	public static String getExternalImage(RenderContext renderContext) {
        //Now(11/02/2012) I hide ext link image - it break page layout 
	    return "";
//		return renderContext.buildSkinImageTag("render/link/extlink.png"
//				,NameConstants.AID,SharedConstants.NO_RENDER_TAG
//				,NameConstants.TITLE, "Open link in new window"
//				,NameConstants.CLASS, "renderExtLinkImg");
	}

	/**
	 * @return
	 */
	public static String getExternalEmailImage(RenderContext renderContext) {
        //Now(11/02/2012) I hide ext link image - it break page layout 
        return "";
//		return renderContext.buildSkinImageTag("render/link/email.png"
//				,NameConstants.AID, SharedConstants.NO_RENDER_TAG
//				,NameConstants.TITLE,"Email to"
//				,NameConstants.CLASS,"renderExtLinkImg");
	}
	
	public static String getUserLinkImage(RenderContext renderContext) {
		return renderContext.buildSkinImageTag("render/link/user.png"
				,NameConstants.AID, SharedConstants.NO_RENDER_TAG
				,NameConstants.TITLE,"Open user profile"
				,NameConstants.CLASS,"renderExtLinkImg");
	}
}
