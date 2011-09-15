/* 
 * =============================================================
 * Copyright (C) 2007-2010 Edgenius (http://www.edgenius.com)
 * =============================================================
 * Edgenius, Confidential and Proprietary
 * License Information: http://www.edgenius.com/licensing/edgenius/1.0/
 *
 * This computer program contains valuable, confidential and proprietary
 * information.  Disclosure, use, or reproduction without the written
 * authorization of Edgenius is prohibited.  This unpublished
 * work by Edgenius is protected by the laws of the United States
 * and other countries.  If publication of the computer program should occur,
 * the following notice shall apply:
 *  
 * Copyright (C) 2007-2010 Edgenius.  All rights reserved.                                                              
 * ****************************************************************
 */
package com.edgenius.core.util;

import java.util.Calendar;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.edgenius.core.UserSetting;
import com.edgenius.core.model.User;

/**
 * @author Dapeng.Ni
 */
public class TestDateUtil extends TestCase{
	private static final String TZ_SYD = "Australia/Sydney";
	private static final String TZ_CN = "Asia/Harbin";
	
	UserSetting setting = new UserSetting();
	User user = new User();
	public void setUp(){
		user.setSetting(setting);
	}
	public void testGetLocalDate(){
//		setting.setTimeZone(TZ_CN);
//		Date date = new Date();
//		System.out.println(date.toString());
//		System.out.println(new Date(DateUtil.getLocalDate(user, date)).toString());
		//I don't think out a way to test it in any date as Sydney has summer time saving ...
	}
	
	public void testDiffInDays(){
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.set(2000, 0, 20, 0, 0, 0);
		c2.set(2000, 1, 15, 0, 0, 0);
		Assert.assertEquals(-26,DateUtil.diffInDays(c1, c2));
		Assert.assertEquals(26,DateUtil.diffInDays(c2, c1));
		
		c1.set(2000, 10, 1, 0, 0, 0);
		c2.set(2000, 6, 1, 0, 0, 0);
		Assert.assertEquals(123,DateUtil.diffInDays(c1, c2));
		Assert.assertEquals(-123,DateUtil.diffInDays(c2, c1));
		
		c1.set(2000, 11, 31, 0, 0, 0);
		c2.set(2001, 0, 1, 0, 0, 0);
		Assert.assertEquals(-1,DateUtil.diffInDays(c1, c2));
		Assert.assertEquals(1,DateUtil.diffInDays(c2, c1));
	}
}
