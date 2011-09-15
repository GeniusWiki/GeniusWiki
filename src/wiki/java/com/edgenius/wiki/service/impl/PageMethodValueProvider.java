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
package com.edgenius.wiki.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.History;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.security.MethodValueProvider;
import com.edgenius.wiki.service.PageService;

/**
 * @author Dapeng.Ni
 */
public class PageMethodValueProvider implements MethodValueProvider{
	protected transient final Logger log = LoggerFactory.getLogger(getClass());
	
	public Map<RESOURCE_TYPES, String> getFromInput(String mi, Object[] args) {
		if(args == null || args.length == 0)
			return null;
		
		Map<RESOURCE_TYPES, String> map = new HashMap<RESOURCE_TYPES, String>(3);
		if(PageService.savePage.equals(mi)){
			//for page permission: it is only limit existed update(for new page, no chance to set permission, certainly, no security policy)
			//exist page will bring pageUuid from Client side
			Page page = (Page) args[0];
			map.put(RESOURCE_TYPES.PAGE, page.getPageUuid());
			extractSpaceUname(mi, page, map);
			//new page:no pageUuid, no need check in PageScope permission
			if(StringUtils.isBlank(page.getPageUuid()))
				return null;
		}else if(PageService.uploadAttachments.equals(mi)){
			if(args.length < 2)
				return null;
			//second parameter is PageUuid
			map.put(RESOURCE_TYPES.PAGE, (String) args[1]);
			map.put(RESOURCE_TYPES.SPACE, (String) args[0]);
		}else if(PageService.removeAttachment.equals(mi)){
			if(args.length < 2)
				return null;
			//second parameter is PageUuid
			map.put(RESOURCE_TYPES.PAGE, (String) args[1]);
			map.put(RESOURCE_TYPES.SPACE, (String) args[0]);
		}else if(PageService.removePage.equals(mi)){
			//first parameter is PageUuid
			map.put(RESOURCE_TYPES.SPACE, (String) args[0]);
			map.put(RESOURCE_TYPES.PAGE, (String) args[1]);
		} else if(PageService.getPageTree.equals(mi)){
			//only get spaceUname is OK: not suitable to single page permission
			map.put(RESOURCE_TYPES.SPACE, (String) args[0]);
		} else if(PageService.getHistoryPages.equals(mi)){
			map.put(RESOURCE_TYPES.SPACE, (String) args[0]);
			map.put(RESOURCE_TYPES.PAGE, (String) args[1]);
		} else if(PageService.updateAttachmentMetaData.equals(mi)){
			map.put(RESOURCE_TYPES.SPACE, (String) args[0]);
			map.put(RESOURCE_TYPES.PAGE, (String) args[1]);
		} else if(PageService.restorePage.equals(mi)){
			map.put(RESOURCE_TYPES.SPACE, (String) args[0]);
			map.put(RESOURCE_TYPES.PAGE, (String) args[1]);
		} else if(PageService.restoreHistory.equals(mi)){
			map.put(RESOURCE_TYPES.SPACE, (String) args[0]);
			map.put(RESOURCE_TYPES.PAGE, (String) args[1]);
		} else if(PageService.copy.equals(mi)){
			//TODO:	only check if target space allow WRITE, need check src allow read ??
			//to spaceUname: first fromSpaceUname, second fromPageUuid
			map.put(RESOURCE_TYPES.SPACE, (String) args[2]);
		} else if(PageService.move.equals(mi)){
			//TODO: only check target space allow WRITE, need check src allow write as well!!!
			//to spaceUname : first fromSpaceUname, second fromPageUuid
			map.put(RESOURCE_TYPES.SPACE, (String) args[2]);
		}else
			//reset map
			map = null;
		
		return map;
	}

	public Map<RESOURCE_TYPES, String> getFromOutput(String mi, Object returnValue) {
		if(returnValue == null)
			return null;
		
		AbstractPage page = null;
		if(PageService.getCurrentPageByTitle.equals(mi)){
			//need extract pageUuid
			page = (Page) returnValue;
		}else if(PageService.getHomepage.equals(mi)){
			//need extract pageUuid
			page = (Page) returnValue;
		}else if(PageService.getPage.equals(mi)){
			page = (Page) returnValue;
		}else if(PageService.getHistory.equals(mi)){
			page = (History) returnValue;
		}
		Map<RESOURCE_TYPES, String> map = null;
		if(page != null){
			map = new HashMap<RESOURCE_TYPES, String>(2);
			map.put(RESOURCE_TYPES.PAGE, page.getPageUuid());
			extractSpaceUname(mi, page, map);
		}
		return map;
	
	}

	/**
	 * @param mi
	 * @param page
	 * @param map
	 */
	private void extractSpaceUname(String mi, AbstractPage page, Map<RESOURCE_TYPES, String> map) {
		if(page.getSpace() !=null){
			map.put(RESOURCE_TYPES.SPACE, page.getSpace().getUnixName());
		}else{
			log.error("can not get spaceUname from output : "+ mi);
		}
	}

	public boolean isSupport(String clz) {
		return PageServiceImpl.class.getName().equals(clz);
	}
	
}
