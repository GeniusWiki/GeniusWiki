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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.util.ThreadInterruptManager;
import com.edgenius.wiki.service.PageSaveTiemoutExcetpion;
import com.edgenius.wiki.util.WikiUtil;

/**
 * Acquire save draft lock. The lock is dependent on username. This may cause by 
 * auto save and manual save draft simultaneously occurs, lock by username is not more exactly lock, could be append
 * PageTitle as well, but this is quite unnecessary becasue this case seldom happen.
 * 
 * @author Dapeng.Ni
 */
public class DraftSavingLockInterceptor implements MethodInterceptor{
	private static final Logger log = LoggerFactory.getLogger(DraftSavingLockInterceptor.class);
	//60 seconds if page can not acquire saving lock, PageSavingTimeoutExcption will throw.
	private static final int SAVE_PAGE_TIMEOUT = 60;

	private ReentrantLock saveDraftLock = new ReentrantLock();
	//will block by username
	private ConcurrentHashMap<String, Condition> saveDraftLockMap = new ConcurrentHashMap<String, Condition>();

	public Object invoke(MethodInvocation mi) throws Throwable {
		Object result;
        
		try {
			//try to get this page lock by PageUid, SpaceUname and PageTitle in order to prevent others concurrent update this page
			//lock before method pageDAO.getCurrentPageByTitle(), so that DuplicatedPageException could be verify correctly
			//UID could be null if it is new page created
			acquireSavingLock();
            result = mi.proceed();
        } finally {
        	releaseLock();
        }
        return result;
	}
	/**
	 * 
	 */
	private void acquireSavingLock() throws PageSaveTiemoutExcetpion{
		try {
			//maybe it is performance bottleneck: all saving page thread must acquired SavingLock, although for different page
			//it just acquired lock and return, but at least, it is single thread process. 
			saveDraftLock.lock();
			String username = getUsername();
			Condition cond = saveDraftLockMap.get(username);
			if(cond == null){
				//success acquired lock
				cond = saveDraftLock.newCondition();
				saveDraftLockMap.put(username, cond);
			}else{
				log.info("Draft save can not acquire lock. Username " + username);
				//60 seconds, this thread will be interrupted and throw TimeOutException.
				ThreadInterruptManager.addThread(Thread.currentThread(), SAVE_PAGE_TIMEOUT);
				try{
					cond.await();
				} catch (InterruptedException e) {
					log.info("Save draft concurrent interrupted");
					throw new PageSaveTiemoutExcetpion();
				}
			}
		}finally{
			saveDraftLock.unlock();
		}
	}

	/**
	 */
	private void releaseLock() {
		
		String username = getUsername();
		Condition cond = null;
		cond = saveDraftLockMap.remove(username);
		
		if(cond != null){
			try {
				saveDraftLock.lock();
				cond.signal();
			} catch (Exception e) {
				log.warn(username + " throw exception when send condition signal:" +e);
			}finally{
				saveDraftLock.unlock();
			}
		}
		log.info("Draft lock for user " + username + " is released" );
	}
	/**
	 * @return
	 */
	private String getUsername() {
		String username = WikiUtil.getUserName();
		if(StringUtils.isBlank(username)){
			username = "$anonymous$";
		}
		return username;
	}
}
