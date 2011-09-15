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
package com.edgenius.wiki.render;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.render.object.ObjectPosition;

/**
 * @author Dapeng.Ni
 */
public interface RenderContext {

	//normal page render
	public static final String RENDER_TARGET_PAGE = "PGE";
	//print, rss, webservice, public search engine
	public static final String RENDER_TARGET_PLAIN_VIEW = "PVW";
	
	//export render - URL needs to be anchor as all content in one page
	public static final String RENDER_TARGET_EXPORT = "EXP";
	//internal Lucene search index
	public static final String RENDER_TARGET_INDEX = "IDX";
	
	//For rich editor
	public static final String RENDER_TARGET_RICH_EDITOR = "RED";

	
	RenderEngine getRenderEngine();
	LinkRenderHelper getLinkRenderHelper();
	//TODO: will remove this method sooner or later. 
	AbstractPage getPage();
	String getSpaceUname();
	String getPageTitle();
	String getPageUuid();
	String getPageContent();
	String getRenderTarget();
	//for RENDER_TARGET_WEBSERVICE target, web root may not from WebUtil.getWe 
	String getHostAppURL();
	/**
	 * Helper method, create unique string key against entire content. This key length is WikiConstants.UUID_KEY_SIZE(current is 8).
	 * @param to identify if this key include "\n". This is useful while unique key as part of render text. For instance, 
	 * [view {someMacro}
	 * abc{someMacro} > link] 
	 * 
	 * won't be valid link filter as they are in multiple lines, but this {someMacro} use MacroHandler to insert an UUID as replacement
	 * in render text, such as [view va0123ab > link],  so it is better to keep render works, just use multipleLines as true.  
	 * @return
	 */
	String createUniqueKey(boolean multipleLines);
	/**
	 * Helper method, Return incremental unique integer value for this RenderContext, i.e, unique for per render process. 
	 * Start from 1. 
	 * 
	 * 
	 * @return
	 */
	int createIncremetalKey();

	/**
	 * TODO: current, return list is sorted by natural sequence (the object scan out from text first, the ObjectPosition is first)
	 * But, in future some case, it may need sorted order. For example, if HeadingFilter also use Handler(not true), 
	 * then TocHandler must handle after HeadingHandler so that ToCHandler can get correct all HeadingModel list.
	 *   
	 * @return
	 */
	List<ObjectPosition> getObjectList();

	/**
	 * Set/get parameter is for transfer value between filters/macros.
	 * @param uuid
	 * @param value
	 */
	void putGlobalParam(String name, Object value);
	
	Map<String,Object> getGlobalParams();
	
	Object getGlobalParam(String name);
	

	
	/**
	 * Return an unmodifiable collection of regions.
	 * @return
	 */
	Collection<Region> getRegions();

	/**
	 * @return at moment, only Immutable, content are valid value while you use this method in Filter class.
	 */
	Region getCurrentRegion();

	/**
	 * Check given string if it is key which create by createUniqueKey(). 
	 * @return
	 */
	boolean isUniqueKey(String key);

	/**
	 * @return
	 */
	String getRegionPrimaryKey();
	/**
	 * @return
	 */
	RenderContext subContext(String text);
	/**
	 * In different scenario, it needs different URL for these elements need URL link, such as image, link etc.
	 * For example, normal render, host could be empty, as image can use relative URL to host. But for RSS/Print, host must
	 * be filled. For export, host must point to sub directory...
	 * 
	 * This method can be decide in render runtime according to render scenarios
	 * 
	 */
	String buildURL(RenderPiece link);
	/**
	 * Build img tag according to different scenario(render target). The image is must under certain skin, for instance, /skin/default/render/link/extlink.png
	 * 
	 * @param relativeSrc Relative to skin root, for example, if image is under /skin/default/render/link/extlink.png, then relativeSrc is "render/link/extlink.png"
	 * @param attsList optional, must be name1,value1, name2,value2 pair format.
	 * @return
	 */
	String buildSkinImageTag(String relativeSrc, String... attsList);

	/**
	 * Build attachment download URL in current RenderContext space and page
	 * @param filename
	 * @param fileNodeUuid
	 * @param download
	 * @return
	 */
	String buildDownloadURL(String filename, String fileNodeUuid, boolean download);
	String buildDownloadURL(String relativeURL);
	
	/**
	 * Return current page visible attachments on editing/preview page. Return null if it is normal render. 
	 * 
	 * The visible attachments can not get from database as the the draft status may be various. For example, some scenarios:
	 * <li>User upload foo.png and exit after auto-draft saved. When edit same page but not reload auto-draft, then upload
	 * bar.png. Then in database, foo.png and bar.png both are auto-draft status attachment for this user. but only bar.png 
	 * is visible in current editing page.</li>
	 * <li>User upload foo.png and save manual draft and exit. When edit same page and load manual draft, and upload
	 * bar.png. Then both image are visible. In this case, foo.png is manual status, bar.png is auto-draft status</li>
	 * 
	 */
	String[] getPageVisibleAttachments();
	/**
	 * Tells if this render text has macros/filters need refer to another text(ReferenceContentFilter). 
	 * 
	 * For example, if text has heading, the it should have text that can be as reference.  But only text has {toc} macro,
	 * this method return true, then this means this reference text(heading title) is processed.  This method will save 
	 * performance. 
	 *  
	 * @return
	 */
	boolean hasReferrer();

	
	
}
