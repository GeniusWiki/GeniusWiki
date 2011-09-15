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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Global;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.Skin;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.render.LinkRenderHelper;
import com.edgenius.wiki.render.Region;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderEngine;
import com.edgenius.wiki.render.object.ObjectPosition;
import com.edgenius.wiki.service.ExportService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
public class RenderContextImpl implements RenderContext, Cloneable {
	private static final Logger log = LoggerFactory.getLogger(RenderContextImpl.class);
	
	private LinkRenderHelper linkRenderHelper;
	private RenderEngine renderEngine;
	
	private AbstractPage page;
	private String spaceUname;
	private String pageContent;
	private String pageTitle;
	private String pageUuid;
	private String hostAppURL;
	
	private int incremetalKey = 0;
	private List<ObjectPosition> objectList = new ArrayList<ObjectPosition>();
	private Map<String, String> uniKeys = new HashMap<String, String>(); 
	//data saved for entire Page render process. 
	private Map<String, Object> globalMap = new HashMap<String, Object>();
	
	private Region currentRegion;
	private Collection<Region> regions;
	private String regionPrimaryKey;
	
	//Text could be referred(e.g., heading, its text will be referred by toc}. 
	//This map saves referenceKey and surrounding key - The former is unique key for reference text
	//the latter key surrounds the referred text. For example, "h2. My header" -  the surrounding key "123" 
	//then text looks "h2. 123My header123". 
	//The reason we don't directly put referred text into cache is because text can include some markup and
	//text can be different after render completed.
	private Map<String,String> referenceTextKeys = new HashMap<String, String>();
	
	private String target = RENDER_TARGET_PAGE;

	private String[] visibleAttachmentNodes = null;

	//indicator flag to tell render text has some macro(e.g. toc) needs reference text(i.e., heading text).
	//This flag is for performance purpose.
	//@see referenceTextKeys
	private boolean hasReferrer;

	public RenderContext clone(){
		RenderContext cContext = null;
		try {
			cContext = (RenderContext) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cContext;
	}
	//JDK1.6 @Override
	public LinkRenderHelper getLinkRenderHelper() {
		return linkRenderHelper;
	}
	public void setLinkRenderHelper(LinkRenderHelper linkRenderHelper) {
		this.linkRenderHelper = linkRenderHelper;
	}
	public RenderEngine getRenderEngine() {
		return renderEngine;
	}
	public void setRenderEngine(RenderEngine renderEngine) {
		this.renderEngine = renderEngine;
	}
	//JDK1.6 @Override
	public String createUniqueKey(boolean multipleLines) {
		String key = null; 
		do{
			key = WikiUtil.findUniqueKey(pageContent, multipleLines, true);
		}while(uniKeys.get(key) != null);
		//to ensure key is not duplicated in same render cycle
		uniKeys.put(key, key);
		return key;
	}
	//JDK1.6 @Override
	public boolean isUniqueKey(String key) {
		return uniKeys.get(key) != null;
	}
	//JDK1.6 @Override
	public int createIncremetalKey() {
		return ++incremetalKey;
	}
	//JDK1.6 @Override
	public List<ObjectPosition> getObjectList() {
		return objectList;
	}

	//JDK1.6 @Override
	public void putGlobalParam(String uuid, Object value) {
		globalMap.put(uuid, value);
	}
	//JDK1.6 @Override
	public Map<String, Object> getGlobalParams() {
		return globalMap;
	}

	//JDK1.6 @Override
	public Object getGlobalParam(String name) {
		return globalMap.get(name);
	}

	public Collection<Region> getRegions() {
		return regions;
	}
	public void setRegions(List<Region> region) {
		this.regions = Collections.unmodifiableCollection(region);
	}

	//JDK1.6 @Override
	public Region getCurrentRegion() {
		return currentRegion;
	}
	public String getSpaceUname() {
		return spaceUname;
	}
	public void setSpaceUname(String spaceUname) {
		this.spaceUname = spaceUname;
	}
	public void setCurrentRegion(Region region) {
		this.currentRegion = region;
		
	}
	//JDK1.6 @Override
	public String getRegionPrimaryKey() {
		return regionPrimaryKey;
	}
	public void setRegionPrimaryKey(String regionPrimaryKey) {
		this.regionPrimaryKey = regionPrimaryKey;
	}
	
	//JDK1.6 @Override
	public RenderContext subContext(String text) {
		RenderContextImpl subContext = new RenderContextImpl();
		subContext.linkRenderHelper = this.linkRenderHelper;
		subContext.renderEngine = this.renderEngine;
		subContext.pageContent = text;
		subContext.pageTitle = this.pageTitle;
		subContext.pageUuid = this.pageUuid;
		subContext.spaceUname = this.spaceUname;
		subContext.target = target;
		subContext.page = this.page;
		
		return subContext;
	}
	//JDK1.6 @Override
	public String buildURL(RenderPiece obj) {
		if(obj instanceof LinkModel){
			//LinkModel need render absolute URL info, so here don't use toString() simply.
			LinkModel lm = ((LinkModel)obj);
			if(lm.getType() == LinkModel.LINK_TO_VIEW_FLAG){
				if(RENDER_TARGET_PAGE.equals(target) || RENDER_TARGET_INDEX.equals(target) || RENDER_TARGET_RICH_EDITOR.equals(target)){
					//return empty - even index does not this link setup URL 
					return "";
				}
				
				//export URL: as all link will be in one page, so, all of them are anchor in this page.
				if(RENDER_TARGET_EXPORT.equals(target)){
					//TODO: there are problem to handle extspace anchor!!!
					StringBuffer buf = new StringBuffer("<a href ='#");
					if(!StringUtils.isBlank(lm.getAnchor())){
						buf.append(lm.getAnchor());
					}else{
						if(!StringUtils.isBlank(lm.getSpaceUname())){
							buf.append(StringEscapeUtils.escapeHtml(lm.getSpaceUname()));
							if(!StringUtils.isBlank(lm.getLink()))
								buf.append("_").append(StringEscapeUtils.escapeHtml(lm.getLink()));
						}
					}
					buf.append("'>").append(lm.getView()).append("</a>").toString();
					return buf.toString();
				}
				
				if(RENDER_TARGET_PLAIN_VIEW.equals(target)){
					String anchor = StringUtils.isBlank(lm.getAnchor())?"":EscapeUtil.escapeToken(lm.getAnchor().trim());
					return new StringBuffer("<a href ='")
						.append(linkRenderHelper.getFullURL(this, lm.getSpaceUname(),lm.getLink(),anchor))
						.append("'>").append(lm.getView()).append("</a>").toString();
				}else{
					
				}
			}else{
				//TODO: handle HyperLink type, not space, pageTitle model link 
				return lm.getView();
			}
		}
		return "";
	}

	public String buildDownloadURL(String filename, String fileNodeUuid, boolean download) {
		if(RENDER_TARGET_PAGE.equals(target) || RENDER_TARGET_RICH_EDITOR.equals(target)){
			//relative URL
			return WebUtil.getPageRepoFileUrl(WebUtil.getWebConext(), spaceUname, filename, fileNodeUuid, download);
		}else if(RENDER_TARGET_EXPORT.equals(target)){
			return ExportService.EXPORT_HTML_SUB_DIR+"/"+ pageUuid + "/" + filename;
		}else if(RENDER_TARGET_PLAIN_VIEW.equals(target)){
			//absolute URL
			return WebUtil.getPageRepoFileUrl(StringUtils.isEmpty(this.getHostAppURL())?WebUtil.getHostAppURL():this.getHostAppURL()
					,spaceUname, filename, fileNodeUuid, download);
			
		}
		//skip this one
//		RENDER_TARGET_INDEX
		return "";
	}
	public String buildDownloadURL(String relativeURL) {
		if(RENDER_TARGET_PAGE.equals(target) || RENDER_TARGET_RICH_EDITOR.equals(target)){
			//relative URL
			return WebUtil.getWebConext() + relativeURL;
//		}else if(RENDER_TARGET_EXPORT.equals(target)){
//			//TODO: does this content export?
//			return relativeURL;
		}else if(RENDER_TARGET_PLAIN_VIEW.equals(target)){
			//absolute URL
			return StringUtils.isEmpty(this.getHostAppURL())?WebUtil.getHostAppURL():this.getHostAppURL() + relativeURL;
		}
		//skip this one
//		RENDER_TARGET_INDEX
		
		return "";
	}
	public String buildSkinImageTag(String relativeSrc, String... attsList){
		StringBuilder sbuf = new StringBuilder("<img");
		if(attsList != null && attsList.length > 0){
			for (int idx=0;idx<attsList.length-1;idx=idx+2) {
				sbuf.append(" ").append(attsList[idx]).append("=\"").append(attsList[idx+1]).append("\"");
			}
		}

		sbuf.append(" src=\"");
		if(RENDER_TARGET_EXPORT.equals(target)){
			sbuf.append(ExportService.EXPORT_HTML_SUB_DIR).append("/");
		}else if(RENDER_TARGET_PLAIN_VIEW.equals(target)){
			sbuf.append(StringUtils.isEmpty(this.getHostAppURL())?WebUtil.getHostAppURL():this.getHostAppURL());
		}else{
			sbuf.append(WebUtil.getWebConext());
		}
		//for plain view, it always uses resources under default skin - this is good for Shell service, which doesn't needs to 
		//prepare images for all skins - this is impossible as user may create new skin but it won't upload to Shell.
		//this may has side effect but leave it is at the moment.
		sbuf.append("skins/").append(RENDER_TARGET_PLAIN_VIEW.equals(target)?Skin.DEFAULT_SKIN:Global.Skin).append("/");
		sbuf.append(relativeSrc).append("\"");
		sbuf.append(">");
		
		return sbuf.toString();
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public void setPageVisibleAttachments(String[] visibleAttachmentNodes){
		this.visibleAttachmentNodes = visibleAttachmentNodes;
	}
	public String[] getPageVisibleAttachments() {
		return visibleAttachmentNodes;
	}

	public Map<String, String> getReferenceTextKeys() {
		return referenceTextKeys;
	}
	
	public void setHasReferrer(boolean hasReferrer) {
		this.hasReferrer = hasReferrer;
	}
	public boolean hasReferrer() {
		return hasReferrer;
	}
	public AbstractPage getPage() {
		return page;
	}
	public void setPage(AbstractPage page) {
		this.page = page;
	}
	public String getPageContent() {
		return pageContent;
	}
	public String getRenderTarget(){
		return target;
	}
	public void setPageContent(String pageContent) {
		this.pageContent = pageContent;
	}
	public String getPageTitle() {
		return pageTitle;
	}
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	public String getPageUuid() {
		return pageUuid;
	}
	public void setPageUuid(String pageUuid) {
		this.pageUuid = pageUuid;
	}
	public String getHostAppURL() {
		return hostAppURL;
	}
	public void setHostAppURL(String hostAppURL) {
		this.hostAppURL = hostAppURL;
	}

}
