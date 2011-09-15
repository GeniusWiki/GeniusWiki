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
package com.edgenius.wiki.integration.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.wiki.Shell;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.integration.rest.model.CommentBean;
import com.edgenius.wiki.integration.rest.model.FileBean;
import com.edgenius.wiki.integration.rest.model.PageBean;
import com.edgenius.wiki.integration.rest.model.TagBean;
import com.edgenius.wiki.integration.rest.model.mapper.CommentMapper;
import com.edgenius.wiki.integration.rest.model.mapper.FileMapper;
import com.edgenius.wiki.integration.rest.model.mapper.PageMapper;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.PageTag;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.service.CommentException;
import com.edgenius.wiki.service.CommentService;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.SecurityDummy;
import com.google.gson.Gson;

/**
 * @author Dapeng.Ni
 */
@Path("/page")
@Component
@Scope("singleton")
public class PageResource {
	private static final Logger log = LoggerFactory.getLogger(PageResource.class);
	private @Autowired PageService pageService;
	private @Autowired RenderService renderService;
	private @Autowired CommentService commentService;
	private @Autowired SecurityDummy securityDummy;

	/**
	 * 
	 * @param pageUuid
	 * @param withComments optional
	 * @param withAttachments optional
	 * @param myURL optional - this URL will be as root URL in render text, for example, page link, image link etc.
	 * 							If it is null, it will use Shell.url if Shell.url is not null. Otherwise, it will use WebUtil.getHostApp() url
	 * 							which is current website root URL.
	 * @return
	 */
	@GET
	@Produces("application/json")
	public String getPage(@QueryParam("puuid") String pageUuid, 
			@QueryParam("withcomments") boolean withComments,
			@QueryParam("withattachments") boolean withAttachments,
			@QueryParam("myurl") String myURL) {
		Page page = pageService.getCurrentPageByUuid(pageUuid);
		if (page != null) {
			String spaceUname = page.getSpace().getUnixName();
			//as pageService.getCurrentPageByUuid() doesn't do any security check, so here confirm if this requestor(anonymous) has read permission
			securityDummy.checkPageRead(spaceUname, pageUuid);
			
			Gson gson = new Gson();
			PageBean bean = PageMapper.pageToBean(page);
			
			//URL has 3 level, input parameter > Shell.url if shell enabled > WebUtil.getHostApp() i.e., current host URL.
			if(myURL == null && Shell.enabled){
				//try shell url if it enabled
				myURL = Shell.url;
			}
			
			//fill tags
			if(page.getTags() != null){
				List<TagBean> tagBeans = getTags(page.getTags());
				
				bean.setTags(tagBeans);
			}
			
			//fill render content
			List<RenderPiece> pieces = renderService.renderHTML(RenderContext.RENDER_TARGET_PLAIN_VIEW, myURL, page);
			bean.setContent(renderService.renderNativeHTML(spaceUname, pageUuid, pieces));
			
			//fill attachment information
			if(withAttachments){
				bean.setAttachments(getAttachments(spaceUname, pageUuid));
			}
			
			//fill comments
			if(withComments){
				try {
					List<PageComment> pageComments = commentService.getPageComments(spaceUname, pageUuid);
					if(pageComments != null){
						List<CommentBean> comments = new ArrayList<CommentBean>();
						for (PageComment pageComment : pageComments) {
							comments.add(CommentMapper.commentToBean(pageComment));
						}
						
						bean.setComments(comments);
					}
				} catch (CommentException e) {
					log.error("Get page REST service failed to get its comments",e);
				}
			
			}
			String json = gson.toJson(bean);
			return json;
		}

		return "";
	}

	
	@GET
	@Path("/tags")
	@Produces("application/json")
	public String getPageTags(@QueryParam("puuid") String pageUuid){
		Page page = pageService.getCurrentPageByUuid(pageUuid);
		if(page != null){
			List<TagBean> tags = getTags(page.getTags());
			
			Gson gson = new Gson();
			String json = gson.toJson(tags);
			return json;
		}
		return "";
	}
	
	@GET
	@Path("/attachments")
	@Produces("application/json")
	public String getPageAttachments(@QueryParam("puuid") String pageUuid){
		Page page = pageService.getCurrentPageByUuid(pageUuid);
		if(page != null){
			List<FileBean> attachments = getAttachments(page.getSpace().getUnixName(), pageUuid);
			
			Gson gson = new Gson();
			String json = gson.toJson(attachments);
			return json;
		}
		return "";
	}
	
	//********************************************************************
	//               Tag 
	//********************************************************************
	private List<TagBean> getTags(List<PageTag> tags) {
		List<TagBean> tagBeans = new ArrayList<TagBean>();
		for (PageTag pageTag : tags) {
			
			TagBean tag = new TagBean();
			tag.setName(pageTag.getName());
			tag.setType(TagBean.TYPE_PAGE);
			tagBeans.add(tag);
		}
		return tagBeans;
	}

	private List<FileBean> getAttachments(String spaceUname, String pageUuid) {
		List<FileBean> files = new ArrayList<FileBean>();
		try {
			List<FileNode> nodes = pageService.getPageAttachment(spaceUname, pageUuid, false, false, null);
			if(nodes != null){
				for (FileNode fileNode : nodes) {
					files.add(FileMapper.fileToBean(fileNode));
				}
			}
		} catch (RepositoryException  e) {
			log.error("Unable to get page attachments " + pageUuid, e);
		}
		
		return files;
	}


}
