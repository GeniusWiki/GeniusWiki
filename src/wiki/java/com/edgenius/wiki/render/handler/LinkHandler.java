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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.context.ApplicationContext;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.core.model.CrFileNode;
import com.edgenius.core.repository.ITicket;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.service.MessageService;
import com.edgenius.wiki.gwt.client.html.HTMLUtil;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.render.object.RenderHandlerException;

/**
 * @author Dapeng.Ni
 */
public class LinkHandler  implements ObjectHandler{
	public static final String HANDLER = LinkHandler.class.getName();
	private MessageService messageService;
	private String pageUuid;
	private RepositoryService repositoryService;
	
	public List<RenderPiece> handle(RenderContext renderContext, Map<String, String> values)  throws RenderHandlerException {
		String view = values.get(NameConstants.VIEW);
		String name = values.get(NameConstants.NAME); 
		List<RenderPiece> pieces = new ArrayList<RenderPiece>();
		
		int type = NumberUtils.toInt(values.get(NameConstants.TYPE));
		if(name != null){
			if(type == LinkModel.LINK_TO_ATTACHMENT){
				//download link for attachment
				String spaceUname = renderContext.getSpaceUname();
				CrFileNode node = null;
				try {
					ITicket ticket = repositoryService.login(spaceUname, spaceUname, spaceUname);
					node = repositoryService.getLatestCRFileNode(ticket, RepositoryService.TYPE_ATTACHMENT, pageUuid, name);
					if(node != null){
						Map<String,String> attributes = new HashMap<String,String>();
						attributes.put(NameConstants.HREF, renderContext.buildDownloadURL(name,node.getNodeUuid(),true));
						attributes.put(NameConstants.TITLE, "Download file "+StringEscapeUtils.escapeHtml(name));
						pieces.add(new TextModel( HTMLUtil.buildTagString("a",view, attributes)));
					}
				} catch (RepositoryException e) {
					Log.error("Unable to get file node",e);
				}
				
				if(node == null){
					//error - doesn't exist
					throw new RenderHandlerException("The attachment doesn't exist.");
				}
			}else if(type == LinkModel.LINK_TO_READONLY){
				//the case is, page no "link to create" permission, and linked page not existed. then simple put view
				//this view maybe pure text, or some markup, such as image
				//build tag
				
				Map<String,String> attributes = new HashMap<String,String>();
				//We don't need put link meta info into Wajax as it won't convert back to Markup at this scenario.
//				Map<String,String> wajaxMap = new HashMap<String,String>();
//				wajaxMap.put(NameConstants.TYPE, String.valueOf(type));
//				//here, name is already removed leading "^"
//				wajaxMap.put(NameConstants.LINK, name); 
//				String wajax = RichTagUtil.buildWajaxAttributeString(LinkModel.class.getName(),wajaxMap);
//				attributes.put(NameConstants.WAJAX, wajax);
				attributes.put(NameConstants.HREF, "javascript:;");
				attributes.put("onClick", "alert(\""+messageService.getMessage("readonly.link")+"\")");
				pieces.add(new TextModel(HTMLUtil.buildTagString("a",view, attributes)));
			}else{
				//page link
				LinkModel link = new LinkModel();
				link.setLink(name);
				link.setType(type);
				link.setSpaceUname(values.get(NameConstants.SPACE));
				link.setView(view);
				if(!StringUtils.isBlank(values.get(NameConstants.ANCHOR))){
					link.setAnchor(values.get(NameConstants.ANCHOR));
				}
				
				link.setLinkTagStr(renderContext.buildURL(link));
				pieces.add(link);
			}
		}else{
			//[http://foo.com] will go here..., no name, but view is http...
			//please note, at this moment, view is already surrounding by link, e.g., <a href=xxx>view</a>, so here just replace view.
			pieces.add(new TextModel(view));
		}
		return pieces;
	}

	public void init(ApplicationContext context) {
		messageService = (MessageService)context.getBean(MessageService.SERVICE_NAME);
		repositoryService = (RepositoryService)context.getBean(RepositoryService.SERVICE_NAME);
	}

	public void renderEnd() {
		
	}

	public void renderStart(AbstractPage page) {
		if(page != null){
			this.pageUuid = page.getPageUuid();
		}		
	}

}
