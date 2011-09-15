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
package com.edgenius.wiki.security.service;

import java.io.Serializable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * @author Dapeng.Ni
 */
public class CaptchaReqiredFilterServiceImpl implements CaptchaReqiredFilterService{

	private Cache loginTimesCache;
	//unit:second
	private long retryPeriodThreshold;
	private int retryTimeThreshold;
	public boolean reqiredCaptche(String username){
		if(retryTimeThreshold < 1){
			//always require captcha
			return true;
		}
		
		Element ele = loginTimesCache.get(username);
		boolean reqired = false;
		if(ele == null){
			//never failed on login before (or EHCache expired), first time come in
			LoginRetryObject obj = new LoginRetryObject();
			obj.time = 1;
			obj.latestTime = System.currentTimeMillis();
			ele = new Element(username, obj);
		}else{
			LoginRetryObject obj = (LoginRetryObject) ele.getValue();
			obj.time += 1;
			long periodSec = (System.currentTimeMillis() - obj.latestTime)/1000;
			if(periodSec > retryPeriodThreshold){
				//the latest retry already over given period, so captcha is not required, this again allow user retry in limited times
				//reset
				obj.time = 1;
			}else if(obj.time > retryTimeThreshold){
				//in given time threshold, it is over maximum retry time, need captcha in later check.
				reqired = true;
			}
			obj.latestTime = System.currentTimeMillis();
		}
		loginTimesCache.put(ele);
		return reqired;
	}
	public void clean(String username) {
		loginTimesCache.remove(username);
	}
	private static class LoginRetryObject implements Serializable{
		private static final long serialVersionUID = -8080183563184909638L;
		private int time;
		private long latestTime;
	}
	//********************************************************************
	//               set /get 
	//********************************************************************
	public void setLoginTimesCache(Cache loginTimesCache) {
		this.loginTimesCache = loginTimesCache;
	}
	public void setRetryPeriodThreshold(long retryPeriodThresold) {
		this.retryPeriodThreshold = retryPeriodThresold;
	}
	public void setRetryTimeThreshold(int retryTimeThresold) {
		this.retryTimeThreshold = retryTimeThresold;
	}
	
}
