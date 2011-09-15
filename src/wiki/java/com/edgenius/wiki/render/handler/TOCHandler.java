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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.context.ApplicationContext;

import com.edgenius.core.service.MessageService;
import com.edgenius.core.util.CompareToComparator;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.html.HTMLUtil;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.filter.HeadingModel;
import com.edgenius.wiki.render.macro.TOCMacro;
import com.edgenius.wiki.render.object.ObjectHandler;

/**
 * @author Dapeng.Ni
 */
public class TOCHandler implements ObjectHandler{

	private String spaceUname;
	private MessageService messageService;
	private String pageTitle;

	@SuppressWarnings("unchecked")
	public List<RenderPiece> handle(RenderContext renderContext, Map<String, String> values) {
		TreeMap<Integer, HeadingModel> tree = new TreeMap<Integer, HeadingModel>(new CompareToComparator());

		List<HeadingModel> list = (List<HeadingModel>) renderContext.getGlobalParam(TOCMacro.class.getName());
		//hi, here cannot simple return even list is null because the TOCMacro place holder need remove in following code.
		if(list != null){
			for (HeadingModel headingModel : list) {
				//OK, find heading model, then put it into sorted list
				tree.put(headingModel.getOrder(), headingModel);
			}
			//in buildTOCHTML() method, {toc} will replace to "no head exist" string
		}

		Collection<HeadingModel> headTree = tree.values();
		//replace all TOCMacro(GlobalFilter) object in renderPiece by TOC HTML string.
		int deep = 3;
		String align = null;
		boolean ordered = true;
		Map<String, String> map = new HashMap<String, String>();
		if(values != null){
			//get back deep, order and align value.
			String deepStr = values.get(NameConstants.DEEP);
			String orderStr = values.get(NameConstants.ORDERED);
			align =	values.get(NameConstants.ALIGN);
			map.put(NameConstants.DEEP, deepStr);
			map.put(NameConstants.ALIGN, align);
			map.put(NameConstants.ORDERED,orderStr);
			
			deep = NumberUtils.toInt(deepStr,3);
			if(orderStr == null){
				//default value is true! show ordered list
				ordered = true;
			}else{
				ordered = BooleanUtils.toBoolean(values.get(NameConstants.ORDERED));
			}
		}
		String wajax ="";
		if(map.size() > 0)
			wajax = RichTagUtil.buildWajaxAttributeString(this.getClass().getName(),map);
		
		List<RenderPiece> pieces = new ArrayList<RenderPiece>();
		pieces.addAll(buildTOCHTML(renderContext, headTree,deep,align, !ordered, wajax));
		return pieces;
	}

	/**
	 * @param renderContext 
	 * @param headTree
	 * @param deep
	 * @param align
	 * @param wajax  
	 * @return
	 */
	private List<RenderPiece> buildTOCHTML(RenderContext renderContext, Collection<HeadingModel> headTree, int deep, String align, boolean unorderList, String wajax) {

	    Map<String, String> attributes = new HashMap<String, String>();
		attributes.put(NameConstants.WAJAX, wajax);
		attributes.put("class", "macroToc " + WikiConstants.mceNonEditable);
		attributes.put(NameConstants.AID, TOCMacro.class.getName());
		
		List<RenderPiece> list = new ArrayList<RenderPiece>();

		//align of TOC
		if(!StringUtils.isBlank(align)){
			if(StringUtils.equalsIgnoreCase(SharedConstants.ALIGN_VALUES.center.getName(),align)
				|| StringUtils.equalsIgnoreCase(SharedConstants.ALIGN_VALUES.centre.getName(),align) ){
				attributes.put("style","margin-left: auto; margin-right: auto;");
				//special handle for center image
				attributes.put("align", "center");
			}else{
				//left or right, use float style
				attributes.put("style","float: " + align);
			}
		}
		StringBuffer sb = new StringBuffer(HTMLUtil.buildTagString("div",null,attributes));
		
		sb.append("<div class=\'title\'>").append(messageService.getMessage("toc")).append("</div>");

		if(headTree == null || headTree.size() == 0){
			//nothing found in h1. - h7. 
			sb.append("<p>").append(messageService.getMessage("no.header.for.toc")).append("</p>");
			sb.append("</div>");
			list.add(new TextModel(sb.toString()));
			return list;
		}
		//TOC content
		//7 is enough
		int[] headNum = new int[10];
		
		//find out start of heading level, for example, page may only contain heading 2. the start number should be 2 then
		int startLevel = 8;
		for (HeadingModel head : headTree) {
			startLevel = startLevel > head.getLevel()?head.getLevel():startLevel;
		}
		int level = startLevel-1;
		
		for (HeadingModel head : headTree) {
			if(head.getLevel() > deep)
				continue;
			if(unorderList){
				for(int idx=head.getLevel(); idx<level ;idx++)
					sb.append("</ul>");
				for(int idx=level; idx< head.getLevel();idx++)
					sb.append("<ul>");
			}			
			
			level = head.getLevel();
			if(unorderList){
				sb.append("<li>");
			}else{
				sb.append("<p style=\"text-indent:").append((level-1) * 30).append("px\"><b>");
				//reset sub levels
				Arrays.fill(headNum, level, headNum.length, 0);
				++headNum[level-1];
			
				//construct 2.2.3 etc head number
				for(int idx=startLevel-1;idx<level;idx++){
					//For example if heading start from h2. Then first
					sb.append(headNum[idx]);
					if(idx < level -1)
						sb.append(".");
					else
						sb.append(" ");
				}
				sb.append("</b>");
			}
			list.add(new TextModel(sb.toString()));
			sb = new StringBuffer();
			
			LinkModel link = new LinkModel();
			link.setView(head.getTitle());
			//Don't put back pageTitle, as link will be same page anchor; link.setLink(pageTitle);
			link.setAnchor(head.getAnchor());
			link.setSpaceUname(spaceUname);
			link.setType(LinkModel.LINK_TO_VIEW_FLAG);
			link.setLinkTagStr(renderContext.buildURL(link));
			list.add(link);
			
			if(unorderList){
				sb.append("</li>");
			}else{
				sb.append("</p>");
			}
			
			sb.append("\n");
		}
		if(unorderList){
			for(int idx=0; idx<level;idx++)
				sb.append("</ul>");
		}
		
		sb.append("</div>");
		list.add(new TextModel(sb.toString()));
		return list;
	}

	public void init(ApplicationContext context) {
		messageService = (MessageService) context.getBean(MessageService.SERVICE_NAME);
	}

	public void renderEnd() {
		// TODO Auto-generated method stub
		
	}

	public void renderStart(AbstractPage page) {
		if(page != null && page.getSpace() != null){
			this.spaceUname = page.getSpace().getUnixName();
			this.pageTitle = page.getTitle();
		}
		
	}

}
