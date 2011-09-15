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
package com.edgenius.wiki.ext.calendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.edgenius.wiki.ext.calendar.model.CalendarEvent;
import com.edgenius.wiki.ext.calendar.service.CalendarService;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.plugin.PluginRenderException;
import com.edgenius.wiki.plugin.PluginService;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.render.object.RenderHandlerException;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;

/**
 * @author Dapeng.Ni
 */
public class CalendarHandler  implements ObjectHandler{
	private static final Logger log = LoggerFactory.getLogger(CalendarHandler.class);

	private PluginService pluginService;
	private CalendarService calendarService;
	private boolean editable;
	
	public List<RenderPiece> handle(RenderContext renderContext, Map<String, String> values) throws RenderHandlerException {
		List<RenderPiece> pieces = new ArrayList<RenderPiece>();
		
		if(values == null || StringUtils.isBlank(values.get(NameConstants.NAME))){
			throw new RenderHandlerException("Calendar must have a name, e.g., {calendar:name=work}");
		}
		
		try {
			//get unique index ID for same page calendar render
			Integer calID = (Integer) renderContext.getGlobalParam(CalendarHandler.class.getName());
			if(calID == null)
				calID = 1;
			else 
				++calID;
			
			renderContext.putGlobalParam(CalendarHandler.class.getName(), calID);
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("calWajax", RichTagUtil.buildWajaxAttributeString(this.getClass().getName(),values));
			map.put("pageUuid", renderContext.getPageUuid());
			map.put("calName", values.get(NameConstants.NAME));
			map.put("calID", calID);
			int ht = NumberUtils.toInt(GwtUtils.removeUnit(values.get(NameConstants.HEIGHT)), -1);
			if(ht == -1)
				ht = 600;
			map.put("height",ht);
			
			String view = values.get(NameConstants.VIEW);
			if(StringUtils.isBlank(view))
				view = CalendarConstants.VIEW.WEEK.getName();
			map.put("view",view);
			int weekStartDay = 1;
			map.put("weekStartDay", weekStartDay);
			
			map.put("readonly",!editable);
			
			if(RenderContext.RENDER_TARGET_EXPORT.equals(renderContext.getRenderTarget())
				|| RenderContext.RENDER_TARGET_PLAIN_VIEW.equals(renderContext.getRenderTarget())){
				//export or print, then immediately read event rather then from cal JS.
				Date[] scope = CalendarUtil.getCalendarScope(CalendarConstants.VIEW.valueOf(view.toUpperCase()), new Date(), weekStartDay);
				List<CalendarEvent> calEvents = calendarService.getEvents(values.get(NameConstants.NAME), renderContext.getPageUuid(), scope[0], scope[1]);
				map.put("events", calEvents);
			}
			pieces.add(new TextModel(pluginService.renderMacro("calendar",map,renderContext)));
		} catch (PluginRenderException e) {
			log.error("Calendar render plugin failed.",e);
			throw new RenderHandlerException("Calendar can't be rendered.");
		}
		return pieces;
	}


	public void init(ApplicationContext context) {
		pluginService = (PluginService) context.getBean(PluginService.SERVICE_NAME);
		calendarService = (CalendarService) context.getBean(CalendarService.SERVICE_NAME);
		
	}

	public void renderEnd() {
		
	}

	public void renderStart(AbstractPage page) {
		//!!! must set false for each render - This handler is cache in ObjectPool
		editable = false;
		
		List<WikiOPERATIONS> perms = page.getWikiOperations();
		if(perms != null){
			//this is true when IndexService - it doesn't fill page permission at the moment
			for (WikiOPERATIONS wikiOPERATIONS : perms) {
				if (WikiOPERATIONS.PAGE_WRITE.equals(wikiOPERATIONS)) {
					editable = true;
					break;
				}
			}
		}
	}

}
