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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.PageTheme;
import com.edgenius.wiki.Theme;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.dao.SpaceDAO;
import com.edgenius.wiki.gwt.client.html.HtmlParser;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.MacroModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.html.PureTextHtmlListenerImpl;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.Draft;
import com.edgenius.wiki.model.History;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageContent;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.plugin.PluginService;
import com.edgenius.wiki.render.LinkRenderHelper;
import com.edgenius.wiki.render.LinkReplacerEngine;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderEngine;
import com.edgenius.wiki.render.RichRenderEngine;
import com.edgenius.wiki.render.impl.LinkRenderHelperImpl;
import com.edgenius.wiki.render.impl.LinkReplacer;
import com.edgenius.wiki.render.impl.RenderContextImpl;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.ThemeService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@Transactional(readOnly=true)
public class RenderServiceImpl implements RenderService{
	static final Logger log = LoggerFactory.getLogger(RenderServiceImpl.class);
	
	private RenderEngine renderEngine;
	private RichRenderEngine richRenderEngine;
	private LinkReplacerEngine linkReplacerEngine;
	
	private PluginService pluginService;

	private SpaceDAO spaceDAO;
	private PageDAO pageDAO;

	private SecurityService securityService;
	private UserReadingService userReadingService;
	private ThemeService themeService;

	//example string: {action:name=requestFriendship|sender=textSpaceUname}
//	private static final Pattern msgPattern = 
//		Pattern.compile("(^|[\\p{Space}]+)\\{[\\p{Space}]*action[\\p{Space}]*(:[^}]*|[\\p{Space}]*)\\}([\\p{Space}]+|$)");
//	
//	//parse above example string attributes 
//	private static final Pattern paramsPattern = 
//		Pattern.compile("\\p{Space}*([^=\\p{Space}]+)[\\p{Space}]*=([^\\|]+)[\\|]?");

	//********************************************************************
	//               Method
	//********************************************************************
	//JDK1.6 @Override
	public List<RenderPiece> renderHTML(AbstractPage page) {
		return renderHTML(RenderContext.RENDER_TARGET_PAGE,page);
	}
	/**
	 * return Render Pieces list, which could contain various object.<br>
	 * For normal page, it may contain text and hyper-link<br>
	 * String<br>
	 * LinkModel<br>
	 * 
	 * Following possible appear in customized page, such as search result, tag cloud or user profile. The list could expand
	 * dependent on more MarcoHandler. They are implement interface <code>ComplexPiece</code> <br>
	 * 
	 * SearchResultModel<br>
	 * TagListModel<br>
	 * UserProfileModel<br>
	 * MacroModel<br>
	 * UserPopupModel<br>
	 * LinkModel<br>
	 * 
	 * Basically, they all need implement toString() method to let Search Engine filter out the pure text for search usage.
	 */
	@Override
	public List<RenderPiece> renderHTML(String target, AbstractPage page) {
		//hostAppURL is null, then it will get value from WebUtil.getHostAppURL(), i.e., current website URL;
		return this.renderHTML(target, null, page);
	}
	@Override
	public List<RenderPiece> renderHTML(String target, String hostAppURL, AbstractPage page) {
		
		long s = 0;
		
		if(log.isDebugEnabled()){
			s = System.currentTimeMillis();
		}
		
		String spaceUname = page.getSpace().getUnixName();
		
		//???does any case should check a special user? if so, need a User as input parameter for this page?
		boolean allowCreate = false;
		
		//indexing page - pure text is not necessary to fill security permission.
		if(spaceUname != null && !RenderContext.RENDER_TARGET_INDEX.equals(target)){
			//if spaceUname must not be null in this case, but pageUuid could be null, but it will fill space security option only in this case.
			securityService.fillPageWikiOperations(WikiUtil.getUser(userReadingService), page);
			List<WikiOPERATIONS> perms = page.getWikiOperations();
			for (WikiOPERATIONS wikiOPERATIONS : perms) {
				if(OPERATIONS.WRITE.equals(wikiOPERATIONS.operation)){
					allowCreate = true;
					break;
				}
			}
		}	
		
		if(log.isDebugEnabled()){
			log.debug("Render service fill page security takes :" + (System.currentTimeMillis() -s ) + "ms");
		}
		String wikiText = "";
		boolean phaseRender = false;
		if(page.getPhaseContent() != null){
			//phase part content render
			phaseRender = true;
			wikiText = page.getPhaseContent() ;
		}else{
			if(page instanceof Page){
				wikiText = ((Page)page).getContent().getContent();
			}else if(page instanceof Draft){
				wikiText = ((Draft)page).getContent().getContent();
			}else if(page instanceof History){
				wikiText = ((History)page).getContent().getContent();
			}
		}
		if(wikiText == null)
			wikiText = "";
		
		//if whole page content render - need embed it into theme - it is not suitable for phase part render...
		if(spaceUname != null && !phaseRender){
			//get out space Theme content, and merge with page render markup
			Theme theme = themeService.getPageTheme(page,WikiUtil.isHomepage(page)?PageTheme.SCOPE_HOME:PageTheme.SCOPE_DEFAULT);
			
			String body = theme.getCurrentPageTheme().getBodyMarkup();

			if(!StringUtils.isBlank(body)){
				if(body.indexOf(Theme.BODY_PLACEHOLDER) != -1){
					wikiText = body.replace(Theme.BODY_PLACEHOLDER, wikiText);
				}else{
					//if missing Theme.BODY_PLACEHOLDER, then just append page render markup after body
					wikiText = body + wikiText;
				}
			}
			
		}
		//initial render context for this time render call 
		RenderContext renderContext = new RenderContextImpl();
		((RenderContextImpl)renderContext).setRenderEngine(renderEngine);
		((RenderContextImpl)renderContext).setPage(page);
		((RenderContextImpl)renderContext).setPageContent(wikiText);
		((RenderContextImpl)renderContext).setSpaceUname(spaceUname);
		((RenderContextImpl)renderContext).setPageTitle(page.getTitle());
		((RenderContextImpl)renderContext).setPageUuid(page.getPageUuid());
		((RenderContextImpl)renderContext).setTarget(target);
		
		hostAppURL = StringUtils.trimToNull(hostAppURL);
		if(hostAppURL != null && !hostAppURL.endsWith("/"))
			hostAppURL += "/";
		((RenderContextImpl)renderContext).setHostAppURL(hostAppURL);
		((RenderContextImpl)renderContext).setPageVisibleAttachments(page.getVisibleAttachmentNodeList());
		
		//initial wiki page link engine 
		LinkRenderHelper linkRenderHelper = new LinkRenderHelperImpl();
		linkRenderHelper.initialize(renderContext,spaceDAO,pageDAO);
		linkRenderHelper.setSpaceUname(spaceUname);
		linkRenderHelper.setAllowCreate(allowCreate);
		((RenderContextImpl)renderContext).setLinkRenderHelper(linkRenderHelper);
		
		//start render
		List<RenderPiece> pieces = renderEngine.render(wikiText, renderContext);

		//set Piece to page: It is not good practice, but it is easy to save time...
		page.setRenderPieces(pieces);
		
		if(log.isDebugEnabled()){
			log.debug("Render service complete takes :" + (System.currentTimeMillis() -s ) + "ms");
		}
		return pieces;
		
	}

	@Override
	public List<RenderPiece> renderHTML(String target, String spaceUname, String pageUuid, String markupText, String[] visibleAttachmentNodeList) {
		Page page = new Page();
		page.setPageUuid(pageUuid);
		Space space = new Space();
		space.setUnixName(spaceUname);
		page.setSpace(space);
		//please be aware, here use phase render - it means this method won't apply page Theme
		page.setPhaseContent(markupText);
		page.setVisibleAttachmentNodeList(visibleAttachmentNodeList);
		
		return renderHTML(target, page);
	}
	
	@Override
	public List<RenderPiece> renderHTML(String markupText) {
		return renderHTML(RenderContext.RENDER_TARGET_PLAIN_VIEW, markupText);
	}
	@Override
	public List<RenderPiece> renderHTML(String target, String markupText) {
		Page page = new Page();
		Space space = new Space();
		page.setSpace(space);
		PageContent content = new PageContent();
		content.setContent(markupText);
		page.setContent(content);
		
		//render to absolute URL
		return renderHTML(target,page);
	}
	
	
	@Override
	public String renderNativeHTML(String spaceUname, String pageUuid, List<RenderPiece> pieces) {
		StringBuffer sb = new StringBuffer();

		if(pieces != null){
			for (RenderPiece obj : pieces) {
				//does it only limit to 3 model?
				if(obj instanceof TextModel){
					sb.append(obj.toString());
				}else if(obj instanceof LinkModel){
					sb.append(((LinkModel)obj).getLinkTagStr());
				}else if(obj instanceof MacroModel){
					RenderContext renderContext = new RenderContextImpl();
					((RenderContextImpl)renderContext).setSpaceUname(spaceUname);
					((RenderContextImpl)renderContext).setPageUuid(pageUuid);
					((RenderContextImpl)renderContext).setPageTitle(null);
					sb.append(pluginService.renderMacro((MacroModel) obj, renderContext));
				}
			}
		}

		return sb.toString();
		
	}
	
	@Override
	public String renderRichHTML(String spaceUname, String pageUuid,  List<RenderPiece> pieces) {
		StringBuffer sb = new StringBuffer();

		if(pieces != null){
			for (RenderPiece obj : pieces) {
				//some wiki markup which is for normal usage, such as search result etc...
				RenderPiece piece = (RenderPiece)obj;
				if(obj instanceof MacroModel){
					RenderContext renderContext = new RenderContextImpl();
					((RenderContextImpl)renderContext).setSpaceUname(spaceUname);
					((RenderContextImpl)renderContext).setPageUuid(pageUuid);
					((RenderContextImpl)renderContext).setPageTitle(null);
					sb.append(pluginService.renderMacro((MacroModel) obj, renderContext));
				}else{
					sb.append(((RenderPiece)piece).toRichAjaxTag());
				}
				
			}
		}

		return sb.toString();
	}
	@Override
	public String renderPureText(AbstractPage page) {
	
		List<RenderPiece> pieces = page.getRenderPieces();
		if(pieces == null){
			//SpaceService.saveHomePage() could go this step.
			pieces = renderHTML(RenderContext.RENDER_TARGET_INDEX, page);
		}

		String pureText = "";
		if(pieces != null){
			StringBuffer sb = new StringBuffer();
			for (RenderPiece object : pieces) {
				//NOTE: all pieces object must implements toPureText() to give a acceptable string
				sb.append(object.toString());
			}
			PureTextHtmlListenerImpl listener = new PureTextHtmlListenerImpl();
			HtmlParser htmlParser = new HtmlParser();
			htmlParser.scan(sb.toString(), listener);
			pureText = listener.getPureText();
		}else{
			AuditLogger.warn("Unexpected case: Page does not render to HTML before render Pure Text" + page.getTitle());
		}
		
		return pureText.toString();
	}
	/**
	 * Convert HTML to Wiki markup
	 */
	@Override
	public String renderHTMLtoMarkup(String spaceUname, String htmlText) {
		RenderContext context = new RenderContextImpl();
		((RenderContextImpl)context).setSpaceUname(spaceUname);
		return richRenderEngine.render(htmlText, context);
		
		
	}
	
	@Override
	public String changeLinkTitle(String content, String spaceUname, String toSpaceUname, String oldLink, String newLink) {
		//initial Replacer object and initial context
		RenderContext context  = new RenderContextImpl();
		
		LinkReplacer replacer = new LinkReplacer();
		replacer.setType(WikiConstants.AUTO_FIX_TITLE_CHANGE_LINK);
		replacer.setFromSpaceUname(spaceUname);
		replacer.setToSpaceUname(toSpaceUname);
		replacer.setOldTitle(oldLink);
		replacer.setNewTitle(newLink);
		
		context.putGlobalParam(LinkReplacer.class.getName(), replacer);
		
		return linkReplacerEngine.render(content, context);
	}

	@Override
	public String changeLinkSpace(String content, String fromSpaceUname, String toSpaceUname) {
		//initial Replacer object and initial context
		RenderContext context  = new RenderContextImpl();
		
		LinkReplacer replacer = new LinkReplacer();
		replacer.setType(WikiConstants.AUTO_FIX_COPY_LINK);
		replacer.setFromSpaceUname(fromSpaceUname);
		replacer.setToSpaceUname(toSpaceUname);
		
		context.putGlobalParam(LinkReplacer.class.getName(), replacer);
		
		return linkReplacerEngine.render(content, context);
	}

	//********************************************************************
	//               private content
	//********************************************************************

	//********************************************************************
	//               set /get method
	//********************************************************************


	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}
	public void setRenderEngine(RenderEngine renderEngine) {
		this.renderEngine = renderEngine;
	}
	public void setRichRenderEngine(RichRenderEngine richRenderEngine) {
		this.richRenderEngine = richRenderEngine;
	}

	public void setSpaceDAO(SpaceDAO spaceDAO) {
		this.spaceDAO = spaceDAO;
	}
	public void setPageDAO(PageDAO pageDAO) {
		this.pageDAO = pageDAO;
	}

	public void setThemeService(ThemeService themeService) {
		this.themeService = themeService;
	}

	public void setLinkReplacerEngine(LinkReplacerEngine linkReplacerEngine) {
		this.linkReplacerEngine = linkReplacerEngine;
	}

	public void setPluginService(PluginService pluginService) {
		this.pluginService = pluginService;
	}
}
