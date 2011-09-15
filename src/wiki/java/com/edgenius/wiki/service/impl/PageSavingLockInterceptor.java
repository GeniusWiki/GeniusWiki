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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.util.ThreadInterruptManager;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.service.PageSaveTiemoutExcetpion;

/**
 * Save page in concurrent situation. Try to get lock by pageUid (if existed page save) or by spaceUname and pageTitle(if new page), 
 * if getting lock failed, it will wait lock release until timeout.
 * 
 * @author Dapeng.Ni
 */
public class PageSavingLockInterceptor implements MethodInterceptor{
	private static final Logger log = LoggerFactory.getLogger(PageSavingLockInterceptor.class);
	//60 seconds if page can not acquire saving lock, PageSavingTimeoutExcption will throw.
	private static final int SAVE_PAGE_TIMEOUT = 60;

	private ReentrantLock savePageLock = new ReentrantLock();
	//will block new page or other page rename to same title with current page title saving request
	private ConcurrentHashMap<String, Condition> savePageLockMap = new ConcurrentHashMap<String, Condition>();
	//will block same page, whatever it changes its title or not
	private ConcurrentHashMap<Integer, Condition> savePageUidLockMap = new ConcurrentHashMap<Integer, Condition>();

	
	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation mi) throws Throwable {
		Object result;
        
        Page pageValue = (Page) mi.getArguments()[0];
        String spaceUname = pageValue.getSpace().getUnixName();
        String newPageTitle = pageValue.getTitle();
        Integer newPageUid = pageValue.getUid();
		try {
			//try to get this page lock by PageUid, SpaceUname and PageTitle in order to prevent others concurrent update this page
			//lock before method pageDAO.getCurrentPageByTitle(), so that DuplicatedPageException could be verify correctly
			//UID could be null if it is new page created
			acquireSavingLock(newPageUid, spaceUname,newPageTitle);
            result = mi.proceed();
        } finally {
        	releaseLock(newPageUid, spaceUname,newPageTitle);
        }
        return result;
	}
	/**
	 * 
	 */
	private void acquireSavingLock(Integer uid, String spaceUname, String pageTitle) throws PageSaveTiemoutExcetpion{
		try {
			//maybe it is performance bottleneck: all saving page thread must acquired SavingLock, although for different page
			//it just acquired lock and return, but at least, it is single thread process. 
			savePageLock.lock();
			String key = spaceUname+"$"+pageTitle;
			Condition cond;
			
			if(uid != null){
				//try to get by UID, if uid is not null 
				cond = savePageUidLockMap.get(uid);
				//if UID has no lock, means same page is not saving by others, but it also need check if there are same PageTitle
				//is saving in this space, otherwise, it may cause same page title in this space
				//think: a new page with title (T1) is saving, this page also has change title from XX to T1, 
				//new page saving first, it will acquire lock because it has no UID and not page title as T1, 
				//then existed page(which old title is XX, new title is T1), if it won't check savePageLockMap, it also will acquire lock,
				//this is bad.
				if(cond == null)
					cond = savePageLockMap.get(key);
			}else
				cond = savePageLockMap.get(key);
			if(cond == null){
				//success acquired lock
				cond = savePageLock.newCondition();
				if(uid != null){
					//OK, save both UidMap and SpaceTitleMap. SpaceTitleMap will block 
					//new page or other page rename to same title with current page title saving request
					//UidMap will block same page, whatever it changes its title or not
					savePageUidLockMap.put(uid, cond);
					savePageLockMap.put(key, cond);
				}else
					savePageLockMap.put(key, cond);
			}else{
				log.info("Page save can not acquire lock. SpaceUname " + spaceUname + ", Page title:" + pageTitle);
				//60 seconds, this thread will be interrupted and throw TimeOutException.
				ThreadInterruptManager.addThread(Thread.currentThread(), SAVE_PAGE_TIMEOUT);
				try{
					cond.await();
				} catch (InterruptedException e) {
					log.info("Save page concurrent interrupted");
					throw new PageSaveTiemoutExcetpion();
				}
			}
		}finally{
			savePageLock.unlock();
		}
	}
	/**
	 */
	private void releaseLock(Integer pageUid, String spaceUname, String pageTitle) {
		
		String key = spaceUname+"$"+pageTitle;
		Condition cond = null;
		if(pageUid != null){
			//anyway, remove this condition also from its space+title map
			savePageLockMap.remove(key);
			cond = savePageUidLockMap.remove(pageUid);
		}else
			cond = savePageLockMap.remove(key);
		
		if(cond != null){
			try {
				savePageLock.lock();
				cond.signal();
			} catch (Exception e) {
				log.warn(key + " throw exception when send condition signal:" +e);
			}finally{
				savePageLock.unlock();
			}
		}
		log.info("Page lock for UID " + pageUid + " Title "+ pageTitle + " in space " + spaceUname + " is released" );
	}
	
}
