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
package com.edgenius.wiki.blogsync;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcHttpTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.gwt.client.model.BlogCategory;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.CommentService;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.SpaceService;

/**
 * @author Dapeng.Ni
 */
public abstract class AbstractBlogProxy implements BlogProxy {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
		
	protected RenderService renderService;
	protected PageService pageService;
	protected SettingService settingService;
	protected SpaceService spaceService;
	protected CommentService commentService;
	protected UserReadingService userReadingService;
	/**
	 * @param exists
	 * @param comment
	 * @return
	 */
	protected boolean isExistedComment(List<PageComment> exists, PageComment comment) {
		if(exists == null)
			return false;
		
		//for performance consideration, this does not compara content, only createData and content length
		for (PageComment exist : exists) {
			if(exist.getCreatedDate().equals(comment.getCreatedDate()) && exist.getBody().length() == comment.getBody().length())
				return true;
		}
		return false;
	}
	/**
	 * @param blog
	 * @param space
	 * @param categories
	 * @throws BlogSyncException
	 */
	protected void saveCategories(BlogMeta blog, Space space, List<BlogCategory> categories) throws BlogSyncException {
		//always replace old categories
		SpaceSetting setting = space.getSetting();
		BlogMeta meta = setting.getBlogMeta(blog.getKey());
		if(meta != null){
			meta.setCategories(categories);
			settingService.saveOrUpdateSpaceSetting(space, setting);
		}else{
			throw new BlogSyncException("Unlinked blog can not update categories " + blog.getXmlrpc());
		}
	}
	/**
	 * @param xmlrpc
	 * @param command
	 * @param params
	 * @return
	 * @throws MalformedURLException
	 * @throws XmlRpcException
	 */
	protected Object callService(String xmlrpc, String command, Object[] params) throws BlogSyncException{
		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(xmlrpc));
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

			return client.execute(command, params);
		} catch (XmlRpcHttpTransportException e) {
			throw new BlogSyncException("Blog transport error.", e);
		} catch (XmlRpcException e) {
			log.error("Request blog download failed:" + xmlrpc, e);
			throw new BlogSyncException(e.getMessage(), e);
		} catch (MalformedURLException e) {
			log.error("Request blog download failed:" + xmlrpc, e);
			throw new BlogSyncException(e);
		}
	}
	/**
	 * @param page
	 * @return
	 */
	protected String getBlogContent(Page page) {
		List<RenderPiece> pieces = renderService.renderHTML(page);
		return renderService.renderNativeHTML(page.getSpace().getUnixName(), page.getPageUuid(), pieces);
	}
	
	public void setRenderService(RenderService renderService) {
		this.renderService = renderService;
	}
	public void setPageService(PageService pageService) {
		this.pageService = pageService;
	}
	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}
	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}
}
