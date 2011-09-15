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
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.Global;
import com.edgenius.wiki.Shell;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageTag;
import com.edgenius.wiki.model.SpaceTag;
import com.edgenius.wiki.service.TagService;

/**
 * @author Dapeng.Ni
 */
public class TagIndexInterceptor extends IndexInterceptor {

	public void afterReturning(Object retValue, Method method, Object[] args, Object target) throws Throwable {
		//although saveUpdatePageTag will call internalSaveUpdatePageTag() to save tag, but for spring, same service interceptor 
		//does not work twice if methods are in same service.
		//This means, if PageService call saveUpdatePageTag(), then saveUpdatePageTag() call  internalSaveUpdatePageTag(),
		//the interceptor only work for first method.
		if(StringUtils.equals(method.getName(), TagService.saveUpdatePageTag)){
			List<PageTag> tags = (List<PageTag>) retValue;
			if(tags != null){
				//update all tag
				for (PageTag pageTag : tags) {
					sendMessage(pageTag);
				}
			}
			Page page = (Page) args[0];
			if(page != null && Shell.enabled && Global.restServiceEnabled){
				sendMessage(page.getPageUuid());
			}
		}else if(StringUtils.equals(method.getName(), TagService.saveUpdateSpaceTag)){
			List<SpaceTag> tags = (List<SpaceTag>) retValue;
			if(tags != null){
				//update all tag
				for (SpaceTag pageTag : tags) {
					sendMessage(pageTag);
				}
			}	

			//not suppose to raise Shell request as space update will send such request
		}else if(StringUtils.equals(method.getName(), TagService.internalSavePageTag)){
			PageTag pageTag  = (PageTag) retValue;
			if(pageTag != null){
				sendMessage(pageTag);
			}
			
			//not suppose to raise Shell request as this method only used for Page copy method, and it will send pageUpdate Shell request
		}
		
	}

	/**
	 * @param pageTag
	 */
	private void sendMessage(PageTag pageTag) {
		log.info("JMS message send for Page Tag index creating/updating. Tag: " + pageTag.getName());
		IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_INSERT_PTAG,pageTag.getUid());
		jmsTemplate.convertAndSend(queue, mqObj);
	}
	private void sendMessage(String pageUuid) {
		log.info("JMS message send for Page Tag update shell request.");
		IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_INSERT_PTAG_BATCH,pageUuid);
		jmsTemplate.convertAndSend(queue, mqObj);
	}
	
	/**
	 * @param spaceTag
	 */
	private void sendMessage(SpaceTag spaceTag) {
		log.info("JMS message send for Space Tag index creating/updating. Tag: " + spaceTag.getName());
		IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_INSERT_STAG,spaceTag.getUid());
		jmsTemplate.convertAndSend(queue, mqObj);
	}

}
