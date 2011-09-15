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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.gwt.client.html.ImageModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.filter.ImageFilter;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.render.object.RenderHandlerException;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.util.WikiUtil;
/**
 * 
 * @author Dapeng.Ni
 */
public class ImageHandler  implements ObjectHandler{
	private static final Logger log = LoggerFactory.getLogger(ImageHandler.class);
	
	private PageService pageService; 
	private MessageService messageService;
	private List<FileNode> atts;

	private UserReadingService userReadingService;


	/*
	 * replace image text with download servlet url: /download?space=xxx&uuid=xxx
	 */
	public List<RenderPiece> handle(RenderContext renderContext, Map<String,String> values) throws RenderHandlerException {
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Build ImageModel object, then render it
		String source = values.get(ImageFilter.SRC);
		values.remove(ImageFilter.SRC);
		
		ImageModel img = new ImageModel();
		if(!source.toLowerCase().startsWith("http://") && !source.toLowerCase().startsWith("https://")){
			//ok, this normal image render rather than draft preview etc(can see draft images), then remove all draft status attachments from list
			if(renderContext.getPageVisibleAttachments() == null && atts != null){
				for (Iterator<FileNode> iter = atts.iterator();iter.hasNext();) {
					FileNode node = iter.next();
					if(node.getStatus() > 0)
						iter.remove();
				}
			}
			
			if(atts == null || atts.size() == 0){
				throw new RenderHandlerException("Image can not render because image repository can not find any attachment.");
			}

			img.filename = source;
			//try to find the image from repository
			for (FileNode node : atts) {
				if(StringUtils.equalsIgnoreCase(node.getFilename(),source)){
					//found attachment
					img.url = renderContext.buildDownloadURL(node.getFilename(),node.getNodeUuid(),false);
					break;
				}
			}
		}else{
			img.url = source;
		}
		
		if(img.url == null){
			log.info("Unable parse out external image url or unavailable image attachment from " +source);
			throw new RenderHandlerException(messageService.getMessage("render.image.not.found",new String[]{source}));
		}
		List<RenderPiece> pieces = new ArrayList<RenderPiece>();
		//does not keep align as they are not standard <img> tag attributes
		img.align = values.remove(NameConstants.ALIGN);
		img.width = values.remove(NameConstants.WIDTH);
		img.height = values.remove(NameConstants.HEIGHT);
		img.title = values.remove(NameConstants.TITLE);
		img.attributes = values;
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// render ImageModel
		String imgBuf = img.toRichAjaxTag();
		 
		pieces.add(new TextModel(imgBuf.toString()));
		return pieces;
		
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
		messageService = (MessageService) context.getBean(MessageService.SERVICE_NAME);
		userReadingService = (UserReadingService) context.getBean(UserReadingService.SERVICE_NAME);
	}

}
