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
package com.edgenius.wiki.render.macro;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;

import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.html.HTMLUtil;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.server.utils.LinkUtil;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.render.handler.NameMacroHandler;
import com.edgenius.wiki.render.object.ObjectPosition;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.SpaceNotFoundException;
import com.edgenius.wiki.util.WikiUtil;

/**
 * Width is default as 100%, height is 200. Please note, it is impossible to access iframe src URL document if
 * this URL is external site. And this makes impossible to use onload javascript event to dynamic adjust iframe height.
 * <br>
 * 
 * Please note, height and width can not accept %, as it will cause Separator Filter, which leads unexpected result.
 * And width and height is only valid for external include mode:<br> 
 * 
 * {include:src=http:\\foo.com|height=200}<br>
 * 
 * or internal mode: <br>
 * 
 * {include:src=pageTitle#phaseName@space}<br>
 * 
 * @author Dapeng.Ni
 */
//Please note, this macro is construct in PortletCreateDialog class. If any parameter name changed, please change there as well.
public class IncludeMacro  extends BaseMacro{

	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		if(!toExtLinkInclude(buffer, params)){
			//internal page link: pageTitle@space#phaseName
			//The reason use NameMacro then use client side render - which need another ajax call
			//First, offline easier to implementation, second, less decouple with PageService etc in macro or its handler... 
			RenderContext context = params.getRenderContext();
			ObjectPosition obj = new ObjectPosition(params.getStartMarkup());
			//whatever, treat it as multiple line content
			obj.uuid = context.createUniqueKey(true);
			obj.serverHandler = NameMacroHandler.class.getName();;
			obj.values.put(NameConstants.MACRO, SharedConstants.MACRO_INCLUDE);
			obj.values.putAll(params.getParams());
			
			context.getObjectList().add(obj);
			
			buffer.append(obj.uuid);
		}
	}

	public Map<String,Object> getTemplValues(MacroParameter params , RenderContext renderContext, ApplicationContext appContext){
		PageService pageService = (PageService) appContext.getBean(PageService.SERVICE_NAME);
		SecurityService securityService = (SecurityService) appContext.getBean(SecurityService.SERVICE_NAME);
		UserReadingService userReadingService = (UserReadingService) appContext.getBean(UserReadingService.SERVICE_NAME);
		RenderService renderService = (RenderService) appContext.getBean(RenderService.SERVICE_NAME);
		
		//as Freemarker request variable must has value in input Map, otherwise it throw exception, I hate it, so stupid!
		//so here put a blank value first.
		Map<String,Object> values = new HashMap<String, Object>();
		values.put(NameConstants.CONTENT, "");
		
		StringBuffer buf = new StringBuffer();
		if(!toExtLinkInclude(buf, params)){
			//get spaceUname and pageTitle - as include src format is same with LinkFilter, so use LinkModel to parse out.
			final String src = params.getParam(NameConstants.SRC);
			LinkModel link = LinkUtil.parseMarkup(src);
			String spaceUname = link.getSpaceUname();
			if(StringUtils.isBlank(spaceUname))
				spaceUname = renderContext.getSpaceUname();
				
			String pageTitle = link.getLink();
			String pieceName = link.getAnchor();
			
			//security check first - here does not use method default security is,  it is not necessary to 
			//redirect user to login page if only included page has not reading permission
			Page page = null;
			try {
				page = pageService.getCurrentPageByTitleWithoutSecurity(spaceUname, pageTitle, false);
			} catch (SpaceNotFoundException e) {
				log.error("Space not found for render page phase",e);
			}
			
			if(page == null){
				return values;
			}
				
			if(!securityService.isAllowPageReading(spaceUname, page.getPageUuid(), WikiUtil.getUser(userReadingService))){
				return values;
			}
			
			String content = null;
			if(StringUtils.isBlank(pieceName)){
				content = page.getContent().getContent();
			}else{
				content = RenderUtil.getPiece(page.getContent().getContent(),pieceName);
				page.setPhaseContent(content);
				if(content == null){
					return values;
				}
			}
			Map<String, String> wajaxMap = new HashMap<String, String>();
			wajaxMap.put(NameConstants.SRC, src);
			String wajax = RichTagUtil.buildWajaxAttributeString(IncludeMacro.class.getName(),wajaxMap);
			
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put(NameConstants.AID,  IncludeMacro.class.getName());
			attributes.put("class", "macroInclude "  + WikiConstants.mceNonEditable);
			attributes.put(NameConstants.WAJAX, wajax);
			buf.append(HTMLUtil.buildTagString("div",null, attributes));
			
			//TODO: how to process whole page content but with PageAttribute macro? or similar macro??? 
			if(RenderContext.RENDER_TARGET_RICH_EDITOR.equals(renderContext.getRenderTarget()))
				buf.append(renderService.renderRichHTML(spaceUname,page.getPageUuid(), renderService.renderHTML(renderContext.getRenderTarget(), page)));
			else
				buf.append(renderService.renderNativeHTML(spaceUname,page.getPageUuid(), renderService.renderHTML(renderContext.getRenderTarget(), page)));
			
			buf.append("</div>");
		}
		
		values.put(NameConstants.CONTENT, buf.toString());
		return values;
	}
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		StringBuffer srcBuf = new StringBuffer("{include:src=");
		boolean valid = false;
		if("iframe".equalsIgnoreCase(node.getTagName())){
			//external include
			if(node.getAttributes() != null){
				String src = node.getAttributes().get(NameConstants.SRC);
				if(!StringUtils.isBlank(src)){
					srcBuf.append(src);
					valid = true;
					
					String w = node.getAttributes().get(NameConstants.WIDTH);
					if(!StringUtils.isBlank(w)){
						srcBuf.append("|width=").append(w);
					}
					
					String h = node.getAttributes().get(NameConstants.HEIGHT);
					if(!StringUtils.isBlank(h)){
						srcBuf.append("|height=").append(h);
					}
				}
			}
			if(node.getPair() != null){
				node.reset("", true);
			}
		}else{
			//internal include
			String wajax = node.getAttributes().get(NameConstants.WAJAX);
			Map<String,String> wmap = RichTagUtil.parseWajaxAttribute(wajax);
			String src = wmap.get(NameConstants.SRC);
			if(!StringUtils.isBlank(src)){
				srcBuf.append(src);
				valid = true;
			}
		}
		if(valid){
			srcBuf.append("}");
			node.reset(srcBuf.toString(), true);
			if(node.getPair() != null){
				node.getPair().reset("", true);
				while(iter.hasNext()){
					HTMLNode subnode = iter.next();
					if(subnode == node.getPair())
						break;
					subnode.reset("", true);
				}
			}
			
		}
		
	}
	
	/**
	 * @param buffer
	 * @param params
	 */
	private boolean toExtLinkInclude(StringBuffer buffer, MacroParameter params) {
		String src = params.getParam(NameConstants.SRC);
		
		if(src.startsWith("http://") || src.startsWith("https://")){
			//external link
			String wStr = params.getParam(NameConstants.WIDTH);
			String hStr = params.getParam(NameConstants.HEIGHT);
			
			buffer.append("<iframe src='").append(src).append("'");
			buffer.append(" class='macroInclude'");
			if(!StringUtils.isBlank(wStr)){
				buffer.append(" width='").append(wStr).append("'");
			}
			if(!StringUtils.isBlank(hStr)){
				buffer.append(" height='").append(hStr).append("'");
			}
			buffer.append(" frameborder=\"0\"></iframe>");
			return true;
		}
		return false;
	}
	public boolean isPaired(){
		return false;
	}
	
	public boolean isProcessEmbedded(){
		return false;
	}
	public String[] getName() {
		return new String[]{"include"};
	}
	@Override
	public String getHTMLIdentifier() {
		return "<iframe>||<div aid='"+IncludeMacro.class.getName()+"'>";
	}

}
