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
package com.edgenius.wiki.render.handler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.render.object.RenderHandlerException;
import com.edgenius.wiki.service.PageService;

/**
 * Page index macro ObjectHandler
 * @author Dapeng.Ni
 */
public class PageIndexHandler implements ObjectHandler{
	private static final Logger log = LoggerFactory.getLogger(PageIndexHandler.class);
	private PageService pageService;
	
	//key:page title,  value:link of page
	private TreeMap<String, LinkModel> indexMap;
	
	//key: character of index(i.e., A, B, ... Z), value: anchor index value, just an incremental value.
	private TreeMap<Character, Integer> indexer; 
	
	public List<RenderPiece> handle(RenderContext renderContext, Map<String, String> values) throws RenderHandlerException {
		if(indexer == null){
			log.warn("Unable to find valid page for index");
			throw new RenderHandlerException("Unable to find valid page for index");
		}
		
		List<String> filters = getFilterList(values != null?StringUtils.trimToNull(values.get("filter")):null);
		List<String> filtersOut = getFilterList(values != null?StringUtils.trimToNull(values.get("filterout")):null);

		//temporary cache for indexer after filter out
		TreeMap<Character, Integer> indexerFiltered = new TreeMap<Character, Integer>(); 
		List<RenderPiece> listPieces = new ArrayList<RenderPiece>();
		//render each character list
		Character indexKey = null;
		boolean requireEndOfMacroPageIndexList = false;
		for (Entry<String, LinkModel> entry: indexMap.entrySet()) {
			String title = entry.getKey();
			
			if(filters.size() > 0){
				boolean out = false;
				for (String filter : filters) {
					if(!FilenameUtils.wildcardMatch(title.toLowerCase(), filter.toLowerCase())){
						out = true;
						break;
					}
				}
				if(out) continue;
			}
			
			if(filtersOut.size() > 0){
				boolean out = false;
				for (String filterOut : filtersOut) {
					if(FilenameUtils.wildcardMatch(title.toLowerCase(), filterOut.toLowerCase())){
						out = true;
						break;
					}
				}
				if(out) continue;
			}

			Character first = Character.toUpperCase(title.charAt(0));
			if(!first.equals(indexKey)){
				if(requireEndOfMacroPageIndexList){
					listPieces.add(new TextModel("</div>")); //macroPageIndexList
				}
				Integer anchorIdx = indexer.get(first);
				indexKey = first;
				if(anchorIdx != null){
					indexerFiltered.put(first, anchorIdx);
					listPieces.add(new TextModel(new StringBuilder()
							.append("<div class=\"macroPageIndexList\"><div class=\"macroPageIndexKey\" id=\"pageindexanchor-")
							.append(anchorIdx)
							.append("\">")
							.append(first)
							.toString()));
					requireEndOfMacroPageIndexList = true;
					//up image line to return top
					if(RenderContext.RENDER_TARGET_PAGE.equals(renderContext.getRenderTarget())){
						LinkModel back = new LinkModel();
						back.setAnchor("pageindexanchor-0");
						back.setAid("Go back index character list");
						back.setView(renderContext.buildSkinImageTag("render/link/up.png", NameConstants.AID, SharedConstants.NO_RENDER_TAG));
						listPieces.add(back);
					}
					
					listPieces.add(new TextModel("</div>"));//macroPageIndexKey
				}else{
					log.error("Unable to page indexer for {}", indexKey);
				}
			}
			listPieces.add(new TextModel("<div class=\"macroPageIndexLink\">"));
			LinkModel link = entry.getValue();
			link.setLinkTagStr(renderContext.buildURL(link));
			listPieces.add(link);
			listPieces.add(new TextModel("</div>"));//macroPageIndexLink
			
			
		}
		if(requireEndOfMacroPageIndexList){
			listPieces.add(new TextModel("</div>")); //macroPageIndexList
		}
		
		//render sum of characters - although it display before page list, however, as filter may hide some characters, so display later than
		//other
		List<RenderPiece> pieces = new ArrayList<RenderPiece>();
		
		StringBuffer sbuf = new StringBuffer("<div aid=\"pageindex\" class=\"macroPageIndex ").append(WikiConstants.mceNonEditable).append("\"");
		if(values != null && values.size() > 0){
			sbuf.append(" wajax=\"").append(RichTagUtil.buildWajaxAttributeString(this.getClass().getName(),values)).append("\" ");
		}
		sbuf.append("><div id=\"pageindexanchor-0\" class=\"macroPageIndexKeys\">");
		
		pieces.add(new TextModel(sbuf.toString()));
		for (Entry<Character, Integer> entry: indexerFiltered.entrySet()) {
			LinkModel anchor = new LinkModel();
			anchor.setView(entry.getKey().toString());
			anchor.setAnchor("pageindexanchor-"+entry.getValue());
			anchor.setLinkTagStr(renderContext.buildURL(anchor));
			pieces.add(anchor);
		}
		pieces.add(new TextModel("</div>")); //macroPageIndexKeys
		pieces.addAll(listPieces);
		pieces.add(new TextModel("</div>")); //macroPageIndex
		
		return pieces;
		
	}



	/**
	 * @param filter
	 * @param filterStr
	 * @return 
	 */
	private List<String> getFilterList(String filterStr) {
		List<String> filter = new ArrayList<String>();
		if(!StringUtils.isEmpty(filterStr)){
			String[] fs = filterStr.split(",");
			for (String str : fs) {
				if(!StringUtils.isEmpty(str))
					filter.add(str.trim());
			}
		}
		
		return filter;
	}

	public void renderEnd() {
	
	}

	public void renderStart(AbstractPage page) {
		if(page != null && page.getSpace() != null){
			String spaceUname = page.getSpace().getUnixName();
			List<Page> pages = pageService.getPageTree(spaceUname);
			indexMap = new TreeMap<String, LinkModel>(new Comparator<String>() {
				public int compare(String o1, String o2) {
					Character c1 = Character.toUpperCase(o1.charAt(0));
					Character c2= Character.toUpperCase(o2.charAt(0));
					if(c1.equals(c2)){
						return o1.compareToIgnoreCase(o2);
					}else{
						return c1.compareTo(c2);
					}
						
				}
			});
			indexer = new TreeMap<Character, Integer> (new Comparator<Character>() {
				public int compare(Character o1, Character o2) {
					return o1.compareTo(o2);
				}
			});
			Character indexKey = null;
			int indexAnchor = 1;
			for (Page pg : pages) {
				String title = pg.getTitle();
				if(StringUtils.isBlank(title)){
					log.error("Blank title page found in {}", spaceUname);
					continue;
				}
				LinkModel link = new LinkModel();
				link.setLink(title);
				link.setType(LinkModel.LINK_TO_VIEW_FLAG);
				link.setSpaceUname(spaceUname);
				link.setView(title);
				indexMap.put(title,link);
				
				Character first = Character.toUpperCase(title.charAt(0));
				if(!first.equals(indexKey)){
					indexKey = first;
					indexer.put(indexKey,indexAnchor++);
				}
			}
			
		}
	}
	public void init(ApplicationContext context) {
		pageService = (PageService) context.getBean(PageService.SERVICE_NAME);
		
	}
}
