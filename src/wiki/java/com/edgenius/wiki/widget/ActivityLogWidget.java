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

import java.util.List;

import org.springframework.context.ApplicationContext;

import com.edgenius.core.model.User;
import com.edgenius.core.service.MessageService;
import com.edgenius.wiki.gwt.client.model.RenderMarkupModel;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.ActivityLog;
import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.service.ActivityLogService;

/**
 * @author Dapeng.Ni
 */
public class ActivityLogWidget extends AbstractWidgetTemplate{
	Widget obj = null;
	private ActivityLogService activityLogService;
	private MessageService messageService;

	@Override
	public boolean isAllowView(User viewer) {
		if(viewer == null || viewer.isAnonymous())
			return false;
		else
			return true;
	}
	public void reset() {
		obj = null;
	}
	//JDK1.6 @Override
	public Widget createWidgetObject(String key) {
		if(obj == null){
			obj = new Widget();
			obj.setType(getType());
			obj.setUuid(SharedConstants.ACTIVITYLOG_KEY);
			obj.setTitle(messageService.getMessage("widget.activies.title"));
			obj.setDescription(messageService.getMessage("widget.activies.desc"));
		}
		return obj;
	}
	//JDK1.6 @Override
	public Widget invoke(String key, User viewer) throws WidgetException {
		Widget obj = new Widget();
		
		//invoke - get first page messages
		//try to return 1 more than request - then know if it has next page.
		List<ActivityLog> logs = activityLogService.getActivities(0, SharedConstants.ITEM_COUNT_IN_ACTIVITY_BOARD+1,viewer);
		obj.setFlag((logs.size() > SharedConstants.MSG_ITEM_COUNT_IN_MSG_BOARD)?1:0);

		if(logs.size() > SharedConstants.ITEM_COUNT_IN_ACTIVITY_BOARD){
			//remove the extra one 
			logs.remove(logs.size()-1);
		}
		
		RenderMarkupModel model = activityLogService.renderActivities(logs);
		obj.setRenderContent(model);
		
		return obj;
	}
	

	public void init(ApplicationContext applicationContext){
		activityLogService = (ActivityLogService) applicationContext.getBean(ActivityLogService.SERVICE_NAME);
		messageService = (MessageService) applicationContext.getBean(MessageService.SERVICE_NAME);
	}
	

}
