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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.edgenius.core.model.User;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.DateUtil;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.server.UserUtil;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.render.object.RenderHandlerException;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
public class PageInfoHandler implements ObjectHandler{
	private static final Logger log = LoggerFactory.getLogger(PageInfoHandler.class);

	private AbstractPage page;
	private MessageService messageService;
	private UserReadingService userReadingService;
	
	public List<RenderPiece> handle(RenderContext renderContext, Map<String, String> values)
			throws RenderHandlerException {
		if(page == null){
			throw new RenderHandlerException("Invalid content. {pageinfo} must inside a page.");
		}
		String type = StringUtils.trim(values.get(NameConstants.TYPE));
		if(StringUtils.isEmpty(type) || (!NameConstants.CREATOR.equals(type) && ! NameConstants.MODIFIER.equals(type))){
			log.warn("Unable to find valid type parameter from PageInfoMacro");
			throw new RenderHandlerException("Type parameter is invalid, either creator or modifier. Sample {pageinfo:type=creator}");
		}

		User user = (NameConstants.CREATOR.equals(type))?page.getCreator():page.getModifier();
		if(user == null){
			//anonymous user
			user = userReadingService.getUser(-1);
		}
		Date date = (NameConstants.CREATOR.equals(type))?page.getCreatedDate():page.getModifiedDate();
		
		String value = StringUtils.trimToEmpty(values.get("value"));

		List<RenderPiece> pieces = new ArrayList<RenderPiece>();
		pieces.add(new TextModel("<div class=\"macroPageinfo\" " +NameConstants.WAJAX + "=\"" + RichTagUtil.buildWajaxAttributeString(this.getClass().getName(),values) +"\">"));
		
		StringBuffer buf = new StringBuffer();

		if(value.length() == 0 || "portrait".equals(value)){
			String portraitUrl = UserUtil.getPortraitUrl( user.getPortrait());
			buf.append("<div class=\"portrait\">").append(GwtUtils.getUserPortraitHTML(portraitUrl, user.getFullname(), -1)).append("</div>");
			pieces.add(new TextModel(buf.toString()));
		}
		
		if(value.length() == 0 || "name".equals(value)){
			String msg = NameConstants.CREATOR.equals(type)?messageService.getMessage("created.by"):messageService.getMessage("last.updated.by");
			pieces.add(new TextModel("<div class=\"name\">" +  msg + " "));
			pieces.add(WikiUtil.createUserLinkModel(user));
			pieces.add(new TextModel("</div>"));
		}
		
		if((value.length() == 0 || "date".equals(value)) && date != null){
			buf = new StringBuffer();
			String showDate = DateUtil.toDisplayDateWithPrep(WikiUtil.getUser(), date,messageService);
			buf.append("<div class=\"date\">").append(showDate).append("</div>");
			pieces.add(new TextModel(buf.toString()));
		}
		
		if("status".equals(value)){
			buf = new StringBuffer();
			buf.append("<div class=\"status\">").append(user.getSetting().getStatus()).append("</div>");
			pieces.add(new TextModel(buf.toString()));
		}
		pieces.add(new TextModel("</div>"));
		
		return pieces;
	}


	public void renderStart(AbstractPage page) {
		this.page = page;
	}

	public void renderEnd() {
	}

	public void init(ApplicationContext context) {
		messageService = (MessageService) context.getBean(MessageService.SERVICE_NAME);
		userReadingService = (UserReadingService) context.getBean(UserReadingService.SERVICE_NAME);
		
	}

}
