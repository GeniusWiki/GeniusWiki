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
package com.edgenius.wiki.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.model.User;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.DateUtil;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.LinkUtil;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.plugin.PluginService;
import com.edgenius.wiki.render.handler.BlogHandler;
import com.edgenius.wiki.service.BlogService;
import com.edgenius.wiki.service.CommentException;
import com.edgenius.wiki.service.CommentService;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.ThemeService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
public class BlogServiceImpl implements BlogService {
	private static final Logger log = LoggerFactory.getLogger(BlogServiceImpl.class);

	private UserReadingService userReadingService;
	private RenderService renderService;
	private PluginService pluginService;
	private ThemeService themeService;
	private CommentService commentService;
	private MessageService messageService;
	
	//JDK1.6 @Override
	public List<RenderPiece> renderBlog(List<Page> pages, int pageNumber, int retCount, boolean hasPrevious, boolean hasNext) {
		if(pages == null)
			return null;
		
		List<RenderPiece> list = new ArrayList<RenderPiece>();
		String spaceUname = null;
		int len = pages.size();
		int skip = 0;
		for (int idx=0;idx<len;idx++) {
			Page page = pages.get(idx);
			if(spaceUname == null)
				spaceUname = page.getSpace().getUnixName();
			
			//must ensure the rendered page does not contain another {blog} marco which will recursive this method unlimited.
			if(WikiUtil.hasBlogRender(page,themeService)){
				skip++;
				continue;
			}
			list.add(new TextModel("<div class='blogpost'>"));
			//~~~~~~~~~~~~~~~ Title
			list.add(new TextModel("<h2 class='title'>" +page.getTitle() + "</h2>"));
			
			//~~~~~~~~~~~~~~~ Author
			String modifier;
			if(page.getModifier()!=null){
				modifier  = page.getModifier().getFullname();
			}else{
				//Anonymous
				User anony = WikiUtil.getAnonymous(userReadingService);
				modifier =  anony.getFullname();
			}
			
			//~~~~~~~~~~~~~~~ Content
			list.addAll(renderService.renderHTML(page));
			
			//-----------------BOTTOM ------------------
			list.add(new TextModel("<br><div class='bottom'>"));
			//~~~~~~~~~~~~~~~  Post by
			list.add(new TextModel("Post by " + modifier+" at " + DateUtil.toDisplayDate(WikiUtil.getUser(), page.getModifiedDate(),messageService)));
			list.add(new TextModel(" | "));
		
			//~~~~~~~~~~~~~~~ Perm link 
			LinkModel permLink = new LinkModel();
			permLink.setLink(page.getTitle());
			permLink.setSpaceUname(spaceUname);
			permLink.setView("PermaLink");
			permLink.setType(LinkModel.LINK_TO_VIEW_FLAG);
			//TODO
			permLink.setLinkTagStr("");
			list.add(permLink);
			list.add(new TextModel(" | "));
			//~~~~~~~~~~~~~~~ comment count
			int count = 0;
			try {
				count = commentService.getPageCommentCount(spaceUname,page.getPageUuid());
			} catch (CommentException e) {
				log.error("unable to get page comment count " + page.getPageUuid(),e);
			}
			list.add(new TextModel(" Comments("+count+")"));
			
			//end bottom DIV
			list.add(new TextModel("</div>"));

			//Post end DIV
			list.add(new TextModel("</div><br>"));
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// previous or next
		if(hasPrevious || hasNext){
			list.add(new TextModel("<p class='pagination'>"));
		}
		if(hasPrevious){
			LinkModel previousLink = new LinkModel();
			previousLink.setLink(GwtUtils.buildToken(SharedConstants.TOKEN_CLINK,spaceUname, LinkUtil.createCLinkToken(pluginService.getPluginUuid(BlogHandler.class.getName())
					, ""+ (pageNumber - 1),""+retCount)));
			previousLink.setView("Previous");
			previousLink.setType(LinkModel.LINK_TO_HYPER_TOKEN);
			//TODO: how to display blog in native HTML? Now link is just plain text:(
			previousLink.setLinkTagStr("Previous");
			list.add(previousLink);
			if(hasNext) list.add(new TextModel(" | "));
		}
		if(hasNext){
			LinkModel nextLink = new LinkModel();
			nextLink.setLink(GwtUtils.buildToken(SharedConstants.TOKEN_CLINK,spaceUname, LinkUtil.createCLinkToken(pluginService.getPluginUuid(BlogHandler.class.getName())
					, ""+ (pageNumber + 1),""+retCount)));
			nextLink.setView("Next");
			nextLink.setType(LinkModel.LINK_TO_HYPER_TOKEN);
			//TODO: how to display blog in native HTML? Now link is just plain text:(
			nextLink.setLinkTagStr("Next");

			list.add(nextLink);
		}
		if(hasPrevious || hasNext){
			list.add(new TextModel("</p>"));
		}
		return list;
	}
	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}
	public void setRenderService(RenderService renderService) {
		this.renderService = renderService;
	}
	public void setPluginService(PluginService pluginService) {
		this.pluginService = pluginService;
	}
	public void setThemeService(ThemeService themeService) {
		this.themeService = themeService;
	}
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

}
