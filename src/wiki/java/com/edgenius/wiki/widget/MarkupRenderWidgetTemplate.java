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
package com.edgenius.wiki.widget;

import org.springframework.context.ApplicationContext;

import com.edgenius.core.model.User;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.WidgetService;


/**
 * @author Dapeng.Ni
 */
public class MarkupRenderWidgetTemplate extends AbstractWidgetTemplate {

	private RenderService renderService;
	private WidgetService widgetService;

	public void reset() {
	}
	@Override
	public void init(ApplicationContext applicationContext) {
		renderService = (RenderService) applicationContext.getBean(RenderService.SERVICE_NAME);
		widgetService = (WidgetService) applicationContext.getBean(WidgetService.SERVICE_NAME);
	}

	//JDK1.6 @Override
	public Widget invoke(String key, User viewer) throws WidgetException {
		Widget widget = widgetService.getWidgetByKey(key);
		if(widget == null)
			throw new WidgetException("Widget does not exist. It maybe deleted by owner.");
		
		//TODO: need check if use allow view this widget
		String markup = widget.getContent();

		PageModel model = new PageModel();
		model.renderContent = renderService.renderHTML(markup);
		widget.setRenderContent(model);
		//no url at moment
		//widget.setTitleURL();
		
		return widget;
	}

	//JDK1.6 @Override
	public Widget createWidgetObject(String key) {
		return widgetService.getWidgetByKey(key);
	}



}
