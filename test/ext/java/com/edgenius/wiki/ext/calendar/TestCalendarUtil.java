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
package com.edgenius.wiki.ext.calendar;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Dapeng.Ni
 */
public class TestCalendarUtil extends TestCase{

	@Test
	public void testGetCalendarScope(){
		Calendar cal = Calendar.getInstance();
		cal.set(2010, 0, 29, 13, 25, 5);
		
		//test day
		Date[] scope = CalendarUtil.getCalendarScope(CalendarConstants.VIEW.DAY,cal.getTime() , 1);
		
		Calendar cal1 = Calendar.getInstance();
		cal1.set(2010, 0, 29, 0, 0, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		Assert.assertEquals(cal1.getTime(),scope[0]);
		
		Calendar cal2 = Calendar.getInstance();
		cal2.set(2010, 0, 29, 23, 59, 59);
		cal2.set(Calendar.MILLISECOND, 999);
		Assert.assertEquals(cal2.getTime(),scope[1]);
		
		//test week		
		scope = CalendarUtil.getCalendarScope(CalendarConstants.VIEW.WEEK,cal.getTime() , 1);
		
		cal1 = Calendar.getInstance();
		cal1.set(2010, 0, 25, 0, 0, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		Assert.assertEquals(cal1.getTime(),scope[0]);
		
		cal2 = Calendar.getInstance();
		cal2.set(2010, 0, 31, 23, 59, 59);
		cal2.set(Calendar.MILLISECOND, 999);
		Assert.assertEquals(cal1.getTime(),scope[0]);
		
		//test week		
		scope = CalendarUtil.getCalendarScope(CalendarConstants.VIEW.WEEK,cal.getTime() , 0);
		
		cal1 = Calendar.getInstance();
		cal1.set(2010, 0, 24, 0, 0, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		Assert.assertEquals(cal1.getTime(),scope[0]);
		
		cal2 = Calendar.getInstance();
		cal2.set(2010, 0, 30, 23, 59, 59);
		cal2.set(Calendar.MILLISECOND, 999);
		Assert.assertEquals(cal1.getTime(),scope[0]);
		
		//test month
		scope = CalendarUtil.getCalendarScope(CalendarConstants.VIEW.MONTH,cal.getTime() , 1);
		
		cal1 = Calendar.getInstance();
		cal1.set(2009, 11, 28, 0, 0, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		Assert.assertEquals(cal1.getTime(),scope[0]);
		
		cal2 = Calendar.getInstance();
		cal2.set(2010, 0, 31, 23, 59, 59);
		cal2.set(Calendar.MILLISECOND, 999);
		Assert.assertEquals(cal2.getTime(),scope[1]);
		
		//test month
		cal = Calendar.getInstance();
		cal.set(2010, 2, 19, 13, 25, 5);
		
		scope = CalendarUtil.getCalendarScope(CalendarConstants.VIEW.MONTH,cal.getTime() , 1);
		
		cal1 = Calendar.getInstance();
		cal1.set(2010, 2, 1, 0, 0, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		Assert.assertEquals(cal1.getTime(),scope[0]);
		
		cal2 = Calendar.getInstance();
		cal2.set(2010, 3, 4, 23, 59, 59);
		cal2.set(Calendar.MILLISECOND, 999);
		Assert.assertEquals(cal2.getTime(),scope[1]);
		
		//test month
		cal = Calendar.getInstance();
		cal.set(2010, 3, 19, 13, 25, 5);
		
		scope = CalendarUtil.getCalendarScope(CalendarConstants.VIEW.MONTH,cal.getTime() , 1);
		
		cal1 = Calendar.getInstance();
		cal1.set(2010, 2, 29, 0, 0, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		Assert.assertEquals(cal1.getTime(),scope[0]);
		
		cal2 = Calendar.getInstance();
		cal2.set(2010, 4, 2, 23, 59, 59);
		cal2.set(Calendar.MILLISECOND, 999);
		Assert.assertEquals(cal2.getTime(),scope[1]);
		
	}
}
