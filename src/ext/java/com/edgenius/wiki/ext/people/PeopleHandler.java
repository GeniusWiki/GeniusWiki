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
package com.edgenius.wiki.ext.people;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.server.UserUtil;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.plugin.PluginRenderException;
import com.edgenius.wiki.plugin.PluginService;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.render.object.RenderHandlerException;
import com.edgenius.wiki.util.WikiUtil;

/**
 * TODO: not implemented in UI part....
 * @author Dapeng.Ni
 */
public class PeopleHandler  implements ObjectHandler{
	private static final Logger log = LoggerFactory.getLogger(PeopleHandler.class);
	private static final int PAGE_COUNT=20;
	
	private PluginService pluginService;
	private UserReadingService userReadingService;
	private User viewer;
	
	public List<RenderPiece> handle(RenderContext renderContext, Map<String, String> values)
			throws RenderHandlerException {
		List<RenderPiece> pieces = new ArrayList<RenderPiece>();
		
		try {
			//get unique index ID for same page Todos render
			Integer pageKey = (Integer) renderContext.getGlobalParam(PeopleHandler.class.getName());
			if(pageKey == null)
				pageKey = 1;
			else 
				++pageKey;
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("peopleMarkup", values.remove("markup"));
			
			map.put("peopleKey", pageKey);
			map.put("peopleWajax", RichTagUtil.buildWajaxAttributeString(this.getClass().getName(),values));
			int page = NumberUtils.toInt(values.get("page"),0);
			String sortBy = values.get("sortby");
			String filter = values.get("filter");
			
			List<User> users = userReadingService.getUsers(page*PAGE_COUNT, PAGE_COUNT, sortBy, filter, false);
			List<UserModel> userModels = new ArrayList<UserModel>(users.size());
			for (User user : users) {
				userModels.add(UserUtil.copyUserToModel(viewer, user));
			}
			map.put("people", userModels);
			pieces.add(new TextModel(pluginService.renderMacro("people",map,renderContext)));
		} catch (PluginRenderException e) {
			log.error("People to render plugin",e);
			throw new RenderHandlerException("People macro can't be rendered.");
		}
		return pieces;
	}


	public void init(ApplicationContext context) {
		pluginService = (PluginService) context.getBean(PluginService.SERVICE_NAME);
		userReadingService = (UserReadingService) context.getBean(UserReadingService.SERVICE_NAME);
		viewer = WikiUtil.getUser(userReadingService);
	}

	public void renderStart(AbstractPage page) {}
	public void renderEnd() {}
}
