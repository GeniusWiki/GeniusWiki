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
package com.edgenius.wiki.render.impl;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.dao.SpaceDAO;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.render.LinkRenderHelper;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.render.handler.LinkHandler;
import com.edgenius.wiki.render.object.ObjectPosition;
import com.edgenius.wiki.util.WikiUtil;

/**
 * Which only handle wiki page link
 * @author Dapeng.Ni
 */
public class LinkRenderHelperImpl implements LinkRenderHelper{

	private String spaceUname;
	private PageDAO pageDAO;
	private SpaceDAO spaceDAO;
	private boolean allowCreate = false;
	private RenderContext context;

	//you must call this method before render to set context to engine .
	public void initialize(RenderContext context,SpaceDAO spaceDAO,PageDAO pageDAO){
		this.context = context;
		this.spaceDAO = spaceDAO;
		this.pageDAO = pageDAO;
	}
	/* (non-Javadoc)
	 * @see org.radeox.api.engine.WikiRenderEngine#appendCreateLink(java.lang.StringBuffer, java.lang.String, java.lang.String)
	 */
	public ObjectPosition appendCreateLink(StringBuffer buffer, String link, String view) {
		link = StringUtils.trimToEmpty(link);
		view = StringUtils.trimToEmpty(view);
		//check if link start by 0:link(always create) or 2:link (create home page)
		String type = String.valueOf(LinkModel.LINK_TO_CREATE_FLAG);
		if(link != null){
			if(link.indexOf(':') == 1){
				type = new String(new char[] {link.charAt(0)});
				link = link.substring(2);
			}
		}
		
		
		ObjectPosition pos = new ObjectPosition("[" + view + "]");
		pos.serverHandler = LinkHandler.HANDLER;
		pos.uuid = context.createUniqueKey(false);
		pos.values.put(NameConstants.TYPE, String.valueOf(type));
		pos.values.put(NameConstants.NAME, link);
		pos.values.put(NameConstants.VIEW, view);
		pos.values.put(NameConstants.SPACE, spaceUname);
		context.getObjectList().add(pos);
		
		buffer.append(pos.uuid);
		
		return pos;
	}

	/* (non-Javadoc)
	 * @see org.radeox.api.engine.WikiRenderEngine#appendLink(java.lang.StringBuffer, java.lang.String, java.lang.String)
	 */
	public ObjectPosition appendLink(StringBuffer buffer, String link, String view) {
		link = StringUtils.trimToEmpty(link);
		view = StringUtils.trimToEmpty(view);
		
		ObjectPosition pos = new ObjectPosition("[" + view + "]");
		pos.serverHandler = LinkHandler.HANDLER;
		pos.uuid = context.createUniqueKey(false);
		pos.values.put(NameConstants.TYPE, String.valueOf(LinkModel.LINK_TO_VIEW_FLAG));
		pos.values.put(NameConstants.NAME, link);
		pos.values.put(NameConstants.VIEW, view);
		pos.values.put(NameConstants.SPACE, spaceUname);
		context.getObjectList().add(pos);
		buffer.append(pos.uuid);
		return pos;
	}

	/* (non-Javadoc)
	 * @see org.radeox.api.engine.WikiRenderEngine#appendLink(java.lang.StringBuffer, java.lang.String, java.lang.String, java.lang.String)
	 */
	public ObjectPosition appendLink(StringBuffer buffer, String link, String view, String anchor) {
		link = StringUtils.trimToEmpty(link);
		view = StringUtils.trimToEmpty(view);
		anchor = StringUtils.trimToEmpty(anchor);
		
		ObjectPosition pos = new ObjectPosition("[" + view + "]");
		pos.serverHandler = LinkHandler.HANDLER;
		pos.uuid = context.createUniqueKey(false);
		pos.values.put(NameConstants.TYPE, String.valueOf(LinkModel.LINK_TO_VIEW_FLAG));
		pos.values.put(NameConstants.NAME, link);
		pos.values.put(NameConstants.VIEW, view);
		pos.values.put(NameConstants.ANCHOR, anchor);
		pos.values.put(NameConstants.SPACE, spaceUname);
		context.getObjectList().add(pos);
		buffer.append(pos.uuid);
		return pos;
	}

	/* 
	 * There are 3 kinds check:
	 * <li>mandatory create: during page not found error happen. At this case, it maybe have 2 options: create home page or other pages</li>
	 * <li>Extlinks, start by http://, https://, mailto:, ftp://,news:// etc</li>
	 * <li>Check database if this page exist or not</li>
	 */
	public boolean exists(String title) {
		title = StringUtils.trimToEmpty(title);
		if(!StringUtils.isBlank(title)){
			//mandatory create validate.
			//0:link create new page
			//2:link create new home page
			if(title.startsWith(LinkModel.LINK_TO_CREATE_FLAG + ":") 
				|| title.startsWith(LinkModel.LINK_TO_CREATE_HOME_FLAG + ":")){
				return false;
			}
		}
		
		// Is there a page is current version and non-draft exist by given pageTitle and spaceUname?
		return pageDAO.getCurrentPageByTitle(spaceUname, title)==null?false:true;
		
	}
	public ObjectPosition appendExtSpaceLink(StringBuffer buffer, String extSpaceUname, String link, String view) {
		extSpaceUname = StringUtils.trimToEmpty(extSpaceUname);
		link = StringUtils.trimToEmpty(link);
		view = StringUtils.trimToEmpty(view);
		
		ObjectPosition pos = new ObjectPosition("[" + view + "]");
		pos.serverHandler = LinkHandler.HANDLER;
		pos.uuid = context.createUniqueKey(false);
		pos.values.put(NameConstants.TYPE, String.valueOf(LinkModel.LINK_TO_VIEW_FLAG));
		pos.values.put(NameConstants.SPACE, extSpaceUname);
		pos.values.put(NameConstants.NAME, link);
		pos.values.put(NameConstants.VIEW, view);
		context.getObjectList().add(pos);
		buffer.append(pos.uuid);
		return pos;
	}
	public ObjectPosition appendExtSpaceLink(StringBuffer buffer, String extSpaceUname, String link, String view, String anchor) {
		extSpaceUname = StringUtils.trimToEmpty(extSpaceUname);
		link = StringUtils.trimToEmpty(link);
		view = StringUtils.trimToEmpty(view);
		anchor = StringUtils.trimToEmpty(anchor);
		
		ObjectPosition pos = new ObjectPosition("[" + view + "]");
		pos.serverHandler = LinkHandler.HANDLER;
		pos.uuid = context.createUniqueKey(false);
		pos.values.put(NameConstants.TYPE, String.valueOf(LinkModel.LINK_TO_VIEW_FLAG));
		pos.values.put(NameConstants.SPACE, extSpaceUname);
		pos.values.put(NameConstants.NAME, link);
		pos.values.put(NameConstants.VIEW, view);
		pos.values.put(NameConstants.ANCHOR, anchor);
		context.getObjectList().add(pos);
		buffer.append(pos.uuid);
		return pos;
	}
	public boolean exists(String extSpaceUname, String title) {
		extSpaceUname = StringUtils.trimToEmpty(extSpaceUname);
		title = StringUtils.trimToEmpty(title);
		if(StringUtils.isBlank(title)){
			//only check if space exist, and this link will redir to home page of space
			return spaceDAO.getByUname(extSpaceUname) == null?false:true;
		}else{
			return pageDAO.getCurrentPageByTitle(extSpaceUname, title)==null?false:true;
		}
	}

	/* (non-Javadoc)
	 * @see org.radeox.api.engine.WikiRenderEngine#showCreate()
	 */
	public boolean showCreate() {
		return allowCreate;
	}
	public String getExternalImage(RenderContext renderContext, String url) {
		//put aid="norender, so that in RichRender, it won't render to !image.jpg! markup
		// NOTE: the same image also hardcode in Filter regex in com.edgenius.wiki.render.Filter.properties file
		if(StringUtils.trimToEmpty(url).startsWith("mailto")){
			return RenderUtil.getExternalEmailImage(renderContext);
		}else{
			return RenderUtil.getExternalImage(renderContext);
		}
	}
	
	public String getNonexistImage(RenderContext renderContext) {
		return renderContext.buildSkinImageTag("render/link/createlink.png"
				,NameConstants.AID,SharedConstants.NO_RENDER_TAG
				,NameConstants.TITLE,"Page does not exist, click to create it."
				,NameConstants.CLASS,"renderExtLinkImg");
	}
	public String getExtspaceLinkBreakImage(RenderContext renderContext) {
		return renderContext.buildSkinImageTag("render/link/brokenlink.png"
				,NameConstants.AID ,SharedConstants.NO_RENDER_TAG
				,NameConstants.TITLE,"Link is broken"
				,NameConstants.CLASS,"renderExtLinkImg");
	}
	
	public String getFullURL(RenderContext renderContext, String spaceUname, String pageTitle, String anchor) {
		String hostURL = renderContext.getHostAppURL();
		if(StringUtils.isEmpty(hostURL)){
			hostURL = WebUtil.getHostAppURL();
		}
		
		String pageUuid = null;
		if(!GwtUtils.isSupportInURL(pageTitle) || !GwtUtils.isSupportInURL(spaceUname)){
			//try to get pageUUID as title or spaceUname is invalid in URL
			Page page = pageDAO.getCurrentPageByTitle(spaceUname, pageTitle);
			if(page != null){
				pageUuid = page.getPageUuid();
			}
		}
		return WikiUtil.getPageRedirFullURL(hostURL, spaceUname, pageTitle, pageUuid, anchor);
	}

	//********************************************************************
	//               Set / Get
	//********************************************************************

	public void setSpaceUname(String spaceUname) {
		this.spaceUname = spaceUname;
	}

	public void setAllowCreate(boolean allowCreate) {
		this.allowCreate = allowCreate;
	}

}
