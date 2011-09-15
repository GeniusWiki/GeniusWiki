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
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.LinkUtil;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.FilterRegxConstants;
import com.edgenius.wiki.render.ImmutableContentFilter;
import com.edgenius.wiki.render.LinkRenderHelper;
import com.edgenius.wiki.render.Region;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.render.SubRegionFilter;
import com.edgenius.wiki.render.TokenVisitor;
import com.edgenius.wiki.render.handler.LinkHandler;
import com.edgenius.wiki.render.object.ObjectPosition;
/**
 * [view>page link]
 * [view>page link#anchor@space]
 * [view>http:external.com]
 * [view>mailto:help@edgenius.com]
 * 
 * Download attachment files
 * [^attachment_file_name]
 * [view>^attachment_file_name]
 * 
 * @author Dapeng.Ni
 */
public class LinkFilter extends BasePatternTokenFilter  implements ImmutableContentFilter, SubRegionFilter{
	
	//JDK1.6 @Override
	public void init(){
		regexProvider.compile(getRegex(), Pattern.MULTILINE);
	}
	//JDK1.6 @Override
	public String getPatternKey() {
		return "filter.link";
	}
	//JDK1.6 @Override
	public List<Region> getRegions(CharSequence input) {
		final List<Region> list = new ArrayList<Region>();
		regexProvider.replaceByTokenVisitor(input,  new TokenVisitor<Matcher>() {
			public void handleMatch(StringBuffer buffer, Matcher matcher) {
				
				int contentStart = matcher.start(1);
				int contentEnd= matcher.end(1);
				int start = contentStart -1;
				int end = contentEnd +1;
				
				String full = matcher.group(1);
				int sep;
				//link has possible 2 Region, [view>link], entire text is immutable region, but view is mutable. 
				if ((sep=StringUtil.indexSeparatorWithoutEscaped(full, ">")) != -1) {
					//entire is immutable
					Region bodyRegion = new Region(LinkFilter.this, true, start,end,contentStart,contentEnd);
					
					//view part is normal mutable, it needs independent render 
					Region viewPartRegion = new Region(LinkFilter.this, false, contentStart ,contentStart+sep,contentStart,contentStart+sep);
					bodyRegion.setSubRegion(viewPartRegion);
					
					list.add(bodyRegion);
				}else{
					//[viewAsLink] only 1 region, and it is immutable 
					list.add(new Region(LinkFilter.this, true, start,end,contentStart,contentEnd));
				}
			}


		});
		return list;
	}

	@Override
	public void setRegex(String regex) {

		this.regex = FilterRegxConstants.PATTERN_ANYTEXT_SURROUNDING.replaceFirst(FilterRegxConstants.PATTERN_REP_TOKEN, "[");
		this.regex = this.regex.replaceFirst(FilterRegxConstants.PATTERN_REP_TOKEN, "]");
	}

	@Override
	public void replace(StringBuffer buffer, MatchResult result, RenderContext context) {
		int idx = result.groupCount();


		if (idx < 1) {
			// failure tolerance
			buffer.append(result.group(0));
			return;
		}

		Region bodyRegion = context.getCurrentRegion();
		LinkModel model = getLinkModel(result, bodyRegion);
		
		LinkRenderHelper linkRenderHelper = context.getLinkRenderHelper();
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//   Concatenate LINK URL
		String view = model.getView(), link = model.getLink(), anchor = model.getAnchor(), extSpaceUname = model.getSpaceUname();
		int type = model.getType();
		ObjectPosition pos = null;
		if (LinkUtil.isExtLink(link)) {
			// if it start from "http://" etc. start, it is extlink
			
			//use ObjectPosition rather than direct render to String, is for link surrounding content
			//for example, [!image.jpg!>http://www.edgenius.com], the !image.jpg! can render as well
			pos = new ObjectPosition(result.group(0));
			pos.serverHandler = LinkHandler.HANDLER;
			pos.uuid = context.createUniqueKey(false);
			pos.values = new HashMap<String, String>();
			//if it is [view>http://foo.com] format, view will be reset in subRegion()
			pos.values.put(NameConstants.VIEW,view);
			//Please note, NameConstants.NAME won't be set!!! See LinkFilter, it treats if(name==null) as extlink format.
			context.getObjectList().add(pos);
			
			//here will make LinkHandler process content part beside <a>, ie, <a>content</a>
			model.setView(pos.uuid);
			
			//model view is pos.uuid now, will replace in subRegion render.
			buffer.append(linkRenderHelper.getExternalImage(context,link));
			buffer.append(model.toRichAjaxTag());
	
		}else{
			if (extSpaceUname != null) {
				if (!linkRenderHelper.exists(extSpaceUname,link)) {
					//does not allow create new page from outside space. here only show link break image.
					buffer.append(linkRenderHelper.getExtspaceLinkBreakImage(context));
				}
				if (anchor != null) {
					pos = linkRenderHelper.appendExtSpaceLink(buffer, extSpaceUname, link, view, anchor);
				} else {
					pos = linkRenderHelper.appendExtSpaceLink(buffer, extSpaceUname, link, view);
				}
			} else {
				//if link is blank, maybe anchor only format(redir in same page) [view>#anchor] 
				if(type == LinkModel.LINK_TO_ATTACHMENT){
					//file attachment link
					pos = new ObjectPosition(result.group(0));
					pos.serverHandler = LinkHandler.HANDLER;
					pos.uuid = context.createUniqueKey(false);
					pos.values.put(NameConstants.VIEW, view);
					//remove leading "^"
					pos.values.put(NameConstants.NAME, link.substring(1));
					pos.values.put(NameConstants.TYPE,String.valueOf(LinkModel.LINK_TO_ATTACHMENT));
					context.getObjectList().add(pos);
					buffer.append(pos.uuid);
					
				}else if (StringUtils.isBlank(link) || linkRenderHelper.exists(link)
						//if web service or RSS request, then return page always return valid even the page doesn't exist.
						//For example, shell request the page, it doesn't want to the non-exist page link block back javascript
						//it may want to display 404 error.
						|| RenderContext.RENDER_TARGET_PLAIN_VIEW.equals(context.getRenderTarget())
						) {
					// Do not add hash if an alias was given
					if (anchor != null) {
						pos = linkRenderHelper.appendLink(buffer, link, view, anchor);
					} else {
						pos = linkRenderHelper.appendLink(buffer, link, view);
					}
				} else if (linkRenderHelper.showCreate()) {
					buffer.append(linkRenderHelper.getNonexistImage(context));
					pos = linkRenderHelper.appendCreateLink(buffer, link, view);
					// links with "create" are not cacheable because
					// a missing wiki could be created
					// context.getRenderContext().setCacheable(false);
				} else {
					// cannot display/create wiki, so just display the text
					pos = new ObjectPosition(result.group(0));
					pos.serverHandler = LinkHandler.HANDLER;
					pos.uuid = context.createUniqueKey(false);
					pos.values.put(NameConstants.VIEW, view);
					pos.values.put(NameConstants.NAME, link);
					//this flag will tell LinkHandler "PageNotFound, But no permission to create page" - comparing with external link, ie, http:// etc.
					pos.values.put(NameConstants.TYPE,String.valueOf(LinkModel.LINK_TO_READONLY));
					context.getObjectList().add(pos);
					buffer.append(pos.uuid);
				}
			}
		}
		
		if(bodyRegion != null && bodyRegion.getSubRegion() != null && pos != null){
			bodyRegion.getSubRegion().setKey(pos.uuid);
		}
	}

	//JDK1.6 @Override
	public void subRegion(Region subRegion, List<RenderPiece> subPieces, RenderContext context) {
		RenderUtil.serialPieceTo(NameConstants.VIEW, subRegion, subPieces, context);
		
	}
	/**
	 * Accept <a> or <object file="*"> tags
	 */
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> nodeIter, RenderContext context){
		if(node.getPair() == null || node.getAttributes() == null){
			AuditLogger.error("Unexpected case: Unable to find close </a> tag");
			return;
		}
		Map<String, String> atts = node.getAttributes();
		if(atts.get(NameConstants.AID) != null
				//this is special checking for Safari browser, it remove all other attributes if it has "name" attribute.
				||(atts.size() == 1 && atts.containsKey(NameConstants.NAME))){
			//default link filter won't have "aid" - it should be anchor
			return;
		}

		StringBuffer viewSb = new StringBuffer();
		
		//whatever internal(wajax) or external link, view always parse out from  tag surrounded text
		HTMLNode cursor = node;
		boolean hasStyleNode = false;
		for(;nodeIter.hasNext() && cursor != node.getPair();cursor = nodeIter.next()){
			if(cursor.isTextNode()){
				viewSb.append(cursor.getText());
			}else{
				hasStyleNode = true;
			}
		}
		
		LinkModel link = new LinkModel();
		if("object".equals(node.getTagName())){
			//pattern ensure it must has "data" attribute
			String fileURL = node.getAttributes().get("data");
			
			//must root file i.e., file:///myfile.txt
			if(!StringUtils.startsWith(fileURL, "file:///")){
				log.warn("Object type link has invalid URL {}", fileURL);
				return;
			}
			fileURL = fileURL.substring(8);
			//if it has "/" means it is possible file:///c:/document/myfile.txt format, ignore it 
			if(StringUtils.isBlank(fileURL) || fileURL.indexOf("/") != -1){
				log.warn("Object type link is not root file {}", fileURL);
				return;
			}
			
			link.setLink(fileURL);
			link.setView(viewSb.toString());
			link.setType(LinkModel.LINK_TO_ATTACHMENT);
		}else{
			link.fillToObject(node.getText(),viewSb.toString());
			if(LinkUtil.isAttachmentLink(link.getLink())){
				link.setType(LinkModel.LINK_TO_ATTACHMENT);
				link.setLink(StringEscapeUtils.unescapeHtml(LinkUtil.getAttachmentFile(link.getLink())));
			}
		}
		
		
		//as link may contain some style mark up, such as [th%%*is is b*%%old>view], so here won't process view
		//but only if view==link and view is only TextNode, I will reset view TextNode at this case
		node.reset("[", true);
		
		
		//this endMarkup only include the part after "view", such as ">link@space]". The part before that "[view" is handled above
		StringBuffer endMarkup = new StringBuffer();
		//only the link is not equals view, [view>link] needs display "view" part
		String escapedLink = EscapeUtil.escapeMarkupLink(link.getLink());
		if(!hasStyleNode || !StringUtils.equals(link.getView(),escapedLink)){
			endMarkup.append(">");
		}else{
			//clean embedded text of link, as view == link, don't need display
			cursor = node.next();
			for(;cursor != null && cursor != node.getPair();cursor = cursor.next()){
				cursor.reset("", true);
			}
		}
		if(!StringUtils.isBlank(link.getLink())){
			if(link.getType()==LinkModel.LINK_TO_ATTACHMENT){
				endMarkup.append("^");
			}
			endMarkup.append(escapedLink);
		}
		if(!StringUtils.isBlank(link.getAnchor())){
			endMarkup.append("#").append(link.getAnchor());
		}
		//only different space, append spaceUname to link
		if(!StringUtils.isBlank(link.getSpaceUname()) 
			&&!StringUtils.equalsIgnoreCase(link.getSpaceUname(), context.getSpaceUname())){
			endMarkup.append("@").append(link.getSpaceUname());
		}
		endMarkup.append("]");

		node.getPair().reset(endMarkup.toString(), true);
		
	}
	/**
	 * @param result
	 * @param bodyRegion
	 * @return
	 */
	protected LinkModel getLinkModel(MatchResult result, Region bodyRegion) {
		String full;
		if(bodyRegion != null){
			full = bodyRegion.getContent();
		}else{
			AuditLogger.error("Unexpected case: Immutable fitler cannot find out current region." + result.group());
			full = result.group(1);
		}

		LinkModel model = LinkUtil.parseMarkup(full);
		return model;
	}
}
