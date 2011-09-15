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
package com.edgenius.wiki.service;

import java.util.List;

import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.model.AbstractPage;

/**
 * @author Dapeng.Ni
 */
public interface RenderService {
	String SERVICE_NAME = "renderService";
	/**
	 * This method is same with renderHTML(RenderContext.RENDER_TARGET_PAGE,page);
	 * 
	 * Render Wiki markup to HTML object list.
	 * 
	 * Input page, it will use following parameters:
	 * <li>spaceUname</li>
	 * <li>pageUuid</li>
	 * <li>wikiText</li>
	 * 
	 * It also auto fill <code>Page.renderPieces</code>, the filled value is same with return value.
	 * 
	 * @return Mix types of possible in GWT page view. Possible values are String, PageLink, or RenderPiece implementation bean.
	 */
	List<RenderPiece> renderHTML(AbstractPage page);

	/**
	 * Same with renderHTML(String target, null, AbstractPage page);
	 * 
	 * URL will be WebUtil.getHostAppURL(), i.e., current website URL;
	 */
	List<RenderPiece> renderHTML(String renderTarget, AbstractPage page);
	
	/**
	 * Render wiki markup to HTML object list with give host URL prefix for all URL element(image or link) 
	 */
	List<RenderPiece> renderHTML(String renderTarget, String hostAppURL, AbstractPage page);
	/**
	 * Same with renderHtml(AbstractPage); but it won't render some Page dependent Macro or Filter, such as PageAttribute.
	 * NOTE: this method will treat markup text as piece of text, i.e., it won't apply the page theme. 
	 * @param markupText
	 * @return
	 */
	List<RenderPiece> renderHTML(String renderTarget, String spaceUname, String pageUuid, String markupText, String[] visibleAttachmentNodeList);
	/**
	 * Without any page or space info exist! To be carefully on Page dependent Macro or Filter!!!
	 * 
	 * 
	 * @param page
	 * @return
	 */
	List<RenderPiece> renderHTML(String renderTarget, String markupText);
	/**
	 * Same with renderHTML(RenderContext.RENDER_TARGET_PLAIN_VIEW, markupText);
	 */
	List<RenderPiece> renderHTML(String markupText);
	/**
	 * Render to HTML text from render Pieces List, which is returned by renderHtml(). The different with
	 * renderNativeHTML() is, HTML of this method returned may contain HTML identifier, such as attribute "aid" etc.
	 * And link URL won't be absolutely URL.
	 * 
	 * Scenarios:
	 * <li> Rich editor editing content</li>
	 * 
	 * @param spaceUname
	 * @param page
	 * @return
	 */
	String renderRichHTML(String spaceUname, String pageUuid, List<RenderPiece> pieces);
	
	/**
	 * Render to HTML text from render Pieces List, which is returned by renderHtml(). The returned HTML is valid to 
	 * access outside website, this means URL could be absolute URL etc.
	 * 
	 * Scenarios:
	 * <li>Public Search Engine(google,yahoo, etc.) requested pages</li>
	 * <li>RSS feed</li>
	 * <li>Print</li>
	 * <li>HTML format export</li>
	 * 
	 * @param page
	 *   
	 * @return
	 */
	String renderNativeHTML(String spaceUname, String pageUuid, List<RenderPiece> pieces);
	
	
	/**
	 * Render Wiki markup to pure text content(remove all format/style information), useful for internal search index file.
	 * Scenarios:
	 * <li> Internal search indexing</li>
	 * 
	 * RenderTarget: RenderContext.RENDER_TARGET_INDEX
	 * @param page
	 * @return
	 */
	String renderPureText(AbstractPage page);
	
	/**
	 * Render HTML to wiki markup, used when RichEditor is switching to markup editor.
	 * @param page
	 * @return
	 */
	String renderHTMLtoMarkup(String spaceUname,String htmlText);
	/**
	 * @param content
	 * @param fromSpaceUname: this is content page's spaceUname, it maybe different space with toSpaceName 
	 * @param toSpaceUname: the changed title page's spaceUname 
	 * @param oldLnk
	 * @param newLink
	 * @return
	 */
	String changeLinkTitle(String content, String fromSpaceUname, String toSpaceUname, String oldLnk, String newLink);

	/**
	 * @param content
	 * @param titles
	 * @param fromSpaceUname
	 * @param toSpaceUname
	 * @return
	 */
	String changeLinkSpace(String content, String fromSpaceUname, String toSpaceUname);



}
