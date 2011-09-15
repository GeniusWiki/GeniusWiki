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
package com.edgenius.wiki.search.interceptor;

import java.lang.reflect.Method;

import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.service.WidgetService;

/**
 * @author Dapeng.Ni
 */
public class WidgetIndexInterceptor  extends IndexInterceptor {
	
	
	public void afterReturning(Object retValue, Method method, Object[] args, Object target) throws Throwable {
		if(WidgetService.saveOrUpdateWidget.equals(method.getName())){
			Widget widget = (Widget) retValue;
			log.info("JMS message send for Widget index creating/updating. Key: " + widget.getUuid());
			IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_INSERT_WIDGET,widget.getUid());
			jmsTemplate.convertAndSend(queue, mqObj);
		}else if(WidgetService.removeWidget.equals(method.getName())){
			Widget widget = (Widget) retValue;
			log.info("JMS message send for Widget index delete. Key: " + widget.getUuid());
			IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_REMOVE_WIDGET,widget.getUuid());
			jmsTemplate.convertAndSend(queue, mqObj);
		}
		
	}

}
