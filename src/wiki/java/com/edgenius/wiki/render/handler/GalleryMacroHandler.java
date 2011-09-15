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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.html.HTMLUtil;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.macro.GalleryMacro;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.render.object.RenderHandlerException;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
public class GalleryMacroHandler implements ObjectHandler{
	private static final Logger log = LoggerFactory.getLogger(GalleryMacroHandler.class);
	private static final String[] IMAGE_FILTERS= new String[SharedConstants.IMAGE_FILTERS.length];
	private static final int DEFAULT_THUMB_WIDTH = 145;
	private static final int DEFAULT_THUMB_HEIGHT = 145;
	
	//no wrap?
	private static final int DEFAULT_MATRIX_COLUMN = Integer.MAX_VALUE;
	
	static{
		//append * before extension name, so that file name filter can work.
		for (int idx=0;idx< SharedConstants.IMAGE_FILTERS.length;idx++) {
			IMAGE_FILTERS[idx] = "*"+SharedConstants.IMAGE_FILTERS[idx];
		}
	}
	private PageService pageService; 
	private List<FileNode> atts;
	private UserReadingService userReadingService;


	/*
	 * replace image text with download servlet url: /download?space=xxx&uuid=xxx
	 */
	public List<RenderPiece> handle(RenderContext renderContext, Map<String,String> values) throws RenderHandlerException {
		if(atts == null){
			throw new RenderHandlerException("No attachmnets.");
		}
		
		List<FileNode> images = getImageList(values.get("filter"), renderContext.getPageVisibleAttachments());
		if(images.size() == 0){
			throw new RenderHandlerException("No valid images in attachment list.");
		}
		
		List<RenderPiece> pieces = new ArrayList<RenderPiece>();
		Map<String, String> wajaxMap = new HashMap<String, String>();
		if(!StringUtils.isBlank(values.get("filter"))){
			wajaxMap.put("filter", values.get("filter"));
		}
		
		List<String> urls = new ArrayList<String>();
		//try to find the image from repository
		for (FileNode node : images) {
			//found attachment
			urls.add(renderContext.buildDownloadURL(node.getFilename(),node.getNodeUuid(),false));
		}
		
		
		int width = DEFAULT_THUMB_WIDTH;  
		int height = DEFAULT_THUMB_HEIGHT;  
		String size = values.get("thumbsize");
		if(size != null){
			String[] si = size.split("\\*");
			if(si.length == 2){
				width = NumberUtils.toInt(si[0],DEFAULT_THUMB_WIDTH);
				height = NumberUtils.toInt(si[1],DEFAULT_THUMB_HEIGHT);
			}else if (si.length == 1){
				width = NumberUtils.toInt(si[0],DEFAULT_THUMB_WIDTH);
				height = width;
			}
			wajaxMap.put("thumbsize", size);
		}
		
		int mxCol = DEFAULT_MATRIX_COLUMN;  
		String column = values.get("column");
		if(column != null){
			mxCol = NumberUtils.toInt(column,DEFAULT_MATRIX_COLUMN);
			wajaxMap.put("column", column);
		}
		
		int id = renderContext.createIncremetalKey();
		
		String wajax = RichTagUtil.buildWajaxAttributeString(GalleryMacro.class.getName(),wajaxMap);
		
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("aid", "gallery");
		attributes.put("class", "macroGallery " + WikiConstants.mceNonEditable);
		attributes.put("id", "renderGalleryID-"+id);
		attributes.put(NameConstants.WAJAX, wajax);
		
		StringBuffer imgBuf = new StringBuffer(HTMLUtil.buildTagString("div",null, attributes));
		
		int count = 0;
		int sum = urls.size();
		while(count < sum) {
			imgBuf.append("<ul>");
			for (int idj = 0; idj < mxCol && count < sum; idj++) {
				imgBuf.append("<li><a href=\"");
				imgBuf.append(urls.get(count));
				imgBuf.append("\" title=\"");
				imgBuf.append(StringEscapeUtils.escapeHtml(images.get(count).getComment()));
				imgBuf.append("\"/><img src=\"");
				imgBuf.append(urls.get(count));
				imgBuf.append("\" width=\"");
				imgBuf.append(width);
				imgBuf.append("\" height=\"");
				imgBuf.append(height);
				imgBuf.append("\"/></a></li>");
				count++;
			}
			imgBuf.append("</ul>");
		}
		
		
		imgBuf.append("</div>");
		pieces.add(new TextModel(imgBuf.toString()));
		return pieces;
		
	}

	/**
	 * @param string
	 * @return
	 */
	private List<FileNode> getImageList(String filterStr, String[] visibles){
		List<String> filter = new ArrayList<String>();
		if(!StringUtils.isEmpty(filterStr)){
			String[] fs = filterStr.split(",");
			for (String str : fs) {
				if(!StringUtils.isEmpty(str))
					filter.add(str.trim());
			}
		}else{
			//default filter
			for (String str : IMAGE_FILTERS) {
				filter.add(str);
			}
		}
		
		List<FileNode> matched = new ArrayList<FileNode>();
		for (FileNode node : atts) {
			if(visibles != null){
				if(!StringUtil.containsIgnoreCase(visibles, node.getNodeUuid()))
					continue;
			}else{
				//normal render, for example, page view. Then only view non-draft
				if(node.getStatus() > 0)
					continue;
			}
			
			String name = node.getFilename();
			for (String flt : filter) {
				if(FilenameUtils.wildcardMatch(name.toLowerCase(), flt.toLowerCase())){
					matched.add(node);
					break;
				}
			}
		}
		return matched;
	}

	public void renderEnd() {

	}

	public void renderStart(AbstractPage page) {
		
		//Must reset! create a empty att, then all image will display error message
		atts = new ArrayList<FileNode>();
		
		if(page != null && page.getSpace() != null){
			String spaceUname = page.getSpace().getUnixName();
			String pageUuid = page.getPageUuid();
			//only when page not found(display create new page info) pageUuid show be null.
			if(pageUuid == null){
				log.error("Image can not render with null pageUuid");
				return;
			}
			try {
				atts = pageService.getPageAttachment(spaceUname, pageUuid, false,true, WikiUtil.getUser(userReadingService));
			} catch (RepositoryException e) {
				log.error("render image failed to get image attachment list");

			}
		}
	}
	public void init(ApplicationContext context) {
		pageService = (PageService) context.getBean(PageService.SERVICE_NAME);
		userReadingService = (UserReadingService) context.getBean(UserReadingService.SERVICE_NAME);
	}

}
