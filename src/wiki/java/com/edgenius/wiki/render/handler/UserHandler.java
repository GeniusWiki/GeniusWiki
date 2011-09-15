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
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.render.object.RenderHandlerException;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
public class UserHandler implements ObjectHandler{
	private UserReadingService userReadingService;
	public List<RenderPiece> handle(RenderContext renderContext, Map<String, String> values) throws RenderHandlerException {
		String username = values.get(NameConstants.NAME);
		
		User user = userReadingService.getUserByName(username);
		
		if(user == null || user.isAnonymous())
			throw new RenderHandlerException("User '" + username + "' not exist");
			
		List<RenderPiece> pieces = new ArrayList<RenderPiece>();
		pieces.add(new TextModel(RenderUtil.getUserLinkImage(renderContext)));
		
		//It was a link for user
		pieces.add(WikiUtil.createUserLinkModel(user));
		
		return pieces;
	}

	public void init(ApplicationContext context) {
		userReadingService = (UserReadingService) context.getBean(UserReadingService.SERVICE_NAME);
		
	}

	public void renderEnd() {
	}

	public void renderStart(AbstractPage page) {
	}

}
