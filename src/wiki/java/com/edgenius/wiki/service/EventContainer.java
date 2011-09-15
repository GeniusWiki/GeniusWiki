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
package com.edgenius.wiki.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Spring managed singleton class to allow third party program to add the implemented class of EventListener.  
 * @author Dapeng.Ni
 */
public class EventContainer{
	
	public static final String SERVICE_NAME = "eventContainer";
	
	private ConcurrentMap<String, ConcurrentLinkedQueue<PageEventListener>> pageListeners = new ConcurrentHashMap<String, ConcurrentLinkedQueue<PageEventListener>>();
	
	/**
	 * Add listener against specified page. 
	 * @param pageUuid
	 * @param listener
	 */
	public void addPageEventListener(String pageUuid, PageEventListener listener){

		if(pageUuid == null || listener == null)
			return;
		
		ConcurrentLinkedQueue<PageEventListener> queue = pageListeners.get(pageUuid);
		if(queue == null){
			queue = new ConcurrentLinkedQueue<PageEventListener>();
			pageListeners.put(pageUuid, queue);
		}
		queue.add(listener);
	}
	public boolean removePageEventListener(String pageUuid, PageEventListener listener){
		ConcurrentLinkedQueue<PageEventListener> queue = pageListeners.get(pageUuid);
		if(queue == null){
			return false;
		}
		return queue.remove(listener);
	}
	public void removePageEventListeners(String pageUuid){
		 pageListeners.remove(pageUuid);
	}

	/**
	 * @return 
	 * @return a safe PageEventListener array, any operation on this array won't impact its original listeners.
	 */
	public  PageEventListener[] getPageEventListeners(String pageUuid){
		ConcurrentLinkedQueue<PageEventListener> queue = pageListeners.get(pageUuid);
		if(queue != null)
			return queue.toArray(new PageEventListener[queue.size()]);
		
		return null;
	}

	
}
