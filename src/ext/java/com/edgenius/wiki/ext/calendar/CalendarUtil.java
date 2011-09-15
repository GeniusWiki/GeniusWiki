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
package com.edgenius.wiki.ext.calendar;

import java.util.Date;

/**
 * @author Dapeng.Ni
 */
public class CalendarUtil {

	/**
	 * Get start/end date for in current calendar view request.
	 * @param viewType
	 * @param showday
	 * @param weekStartDay
	 * @return
	 */
	public static Date[] getCalendarScope(CalendarConstants.VIEW viewType, Date showday, int weekStartDay){
		Date[] scope = new Date[2];
		//reset showday time to 0.
		java.util.Calendar dateCal = java.util.Calendar.getInstance();
		dateCal.setTime(showday);
		dateCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
		dateCal.set(java.util.Calendar.MINUTE, 0);
		dateCal.set(java.util.Calendar.SECOND, 0);
		dateCal.set(java.util.Calendar.MILLISECOND,0);
		
		if(CalendarConstants.VIEW.DAY.equals(viewType)){
			scope[0] = dateCal.getTime();
			dateCal.set(java.util.Calendar.HOUR_OF_DAY, 23);
			dateCal.set(java.util.Calendar.MINUTE, 59);
			dateCal.set(java.util.Calendar.SECOND, 59);
			dateCal.set(java.util.Calendar.MILLISECOND, 999);
			scope[1] = dateCal.getTime();
		}else if(CalendarConstants.VIEW.WEEK.equals(viewType)){
			//minor 1 so, SUNDAY - 0,  1 - Monday, 5 - Friday. 
			int showdayOfWeek = dateCal.get(java.util.Calendar.DAY_OF_WEEK)-1;
			int w = weekStartDay - showdayOfWeek;
			if(w > 0 ) 
				w -= 7;
			dateCal.add(java.util.Calendar.DATE, w);
			scope[0] = dateCal.getTime();
			dateCal.add(java.util.Calendar.DATE, 6);
			
			dateCal.set(java.util.Calendar.HOUR_OF_DAY, 23);
			dateCal.set(java.util.Calendar.MINUTE, 59);
			dateCal.set(java.util.Calendar.SECOND, 59);
			dateCal.set(java.util.Calendar.MILLISECOND, 999);
			scope[1] = dateCal.getTime();
		}else if(CalendarConstants.VIEW.MONTH.equals(viewType)){
			java.util.Calendar mthCal = java.util.Calendar.getInstance();
			mthCal.set(dateCal.get(java.util.Calendar.YEAR), dateCal.get(java.util.Calendar.MONTH), 1,0,0,0);
			mthCal.set(java.util.Calendar.MILLISECOND, 0);
			
			int showdayOfWeek = mthCal.get(java.util.Calendar.DAY_OF_WEEK)-1;
	        int w = weekStartDay - showdayOfWeek;
	        if (w > 0){
	            w -= 7;
	        }
	        mthCal.add(java.util.Calendar.DATE, w);
	        scope[0] = mthCal.getTime();
	        
	        dateCal.set(mthCal.get(java.util.Calendar.YEAR), mthCal.get(java.util.Calendar.MONTH), mthCal.get(java.util.Calendar.DATE));
	        dateCal.add(java.util.Calendar.DATE, 34);
	        
			if (mthCal.get(java.util.Calendar.YEAR) == dateCal.get(java.util.Calendar.YEAR)
					&& mthCal.get(java.util.Calendar.MONTH) == dateCal.get(java.util.Calendar.MONTH)
					&& mthCal.get(java.util.Calendar.DATE) == dateCal.get(java.util.Calendar.DATE)) {
				dateCal.add(java.util.Calendar.DATE, 7);
			}
			dateCal.set(java.util.Calendar.HOUR_OF_DAY, 23);
			dateCal.set(java.util.Calendar.MINUTE, 59);
			dateCal.set(java.util.Calendar.SECOND, 59);
			dateCal.set(java.util.Calendar.MILLISECOND, 999);
			scope[1] = dateCal.getTime();
		}
		return scope;
	}

}
