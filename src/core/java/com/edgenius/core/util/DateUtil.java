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
package com.edgenius.core.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.edgenius.core.Constants;
import com.edgenius.core.Global;
import com.edgenius.core.UserSetting;
import com.edgenius.core.model.User;
import com.edgenius.core.service.MessageService;

/**
 * @author Dapeng.Ni
 */
public class DateUtil {
	//display hour, minutes, year etc.
	public static int TIME_FORMAT_EXACT = 0;
	//display xx minutes/days ago etc.
	public static int TIME_FORMAT_PASSED = 1;
	
	//3 days in ms
	private static long PASSED_TIME_GAP = 3*24*3600*1000;
	/**
	 * Get current data calendar on Global setting time zone. 
	 * @return
	 */
	public static Calendar now(){
		Locale preferredLocale = Global.getDefaultLocale();
		return Calendar.getInstance(TimeZone.getTimeZone(Global.DefaultTimeZone),preferredLocale);
	}
	
	/**
	 * Return  Date.getTime()  according to user local information.
	 * @param viewer
	 * @param date this is from database date
	 * @return
	 */
	public static long getLocalDate(User viewer, Date date){
		if(date == null)
			return 0;
		return date.getTime();
		
		
//		TimeZone userTz = getUserTimeZone(viewer);
//		if(userTz == null){
//			userTz =  TimeZone.getTimeZone(Global.DefaultTimeZone);
//		}
//
//		//convert date to user timezone string without timezone info
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
//		format.setTimeZone(userTz);
//		String localDateStr = format.format(date);
//
//		//convert date (no timezone) string to date with default system timezone offset, 
//		//then return it to long which should be the time of user local time
//		Date localDate;
//		try {
//			SimpleDateFormat format1 =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
//			localDate = format1.parse(localDateStr);
//		} catch (ParseException e) {
//			localDate = date;
//		}
//		
//		return localDate.getTime();
	}
	
	/**
	 * Smart date with preposition, such as at xx hours ago, on Aug 18, etc.
	 * @param viewer
	 * @param date
	 * @return
	 */
	public static String toDisplayDateWithPrep(User viewer, Date date,MessageService msg) {
		if(date == null)
			return "";
		
		return getSmartDate(viewer, date, TIME_FORMAT_PASSED,true, msg);
	}
	/**
	 * This method should keep consistent with client side GwtUtil.toDisplayDate() method
	 * @param viewer
	 * @param date
	 * @return
	 */
	public static String toDisplayDate(User viewer, Date date, MessageService msg) {
		if(date == null)
			return "";
		//I still prefer exactly time display in most case, espiecally, in system admin user login time etc.
		return getSmartDate(viewer, date,TIME_FORMAT_EXACT,false, msg);
	}
	
	/**
	 * TIME_FORMAT_EXACT:<br>
	 * If same day, return hh:mm, if different day but same year, return hh:mm MMM:dd, otherwise hh:mm MMM:dd,yy.
	 * 
	 * TIME_FORMAT_PASSED:<br>
	 * If under 1 minute, return less a minute, if same day, return xx minutes/days ago.  If older less than 3 days, display hh:mm xx days ago.
	 * otherwise, same with TIME_FORMAT_EXACT format.
	 * 
	 * @param user
	 * @param date this date also bring Timezone info, which is same with system Database setting.
	 * @return
	 */
	private static String getSmartDate(User user, Date date, int format, boolean withPrep, MessageService msg){
		//there are 2 conversion happen here: 
		//from input Date timezone to system local timezone, then to UserSetting timezone. 
		//the input Date timezone must keep consist if it read from in database and web server timezone
		
		
		//this user has set his/her time zone manually.
//		boolean customizedTz = true;
		TimeZone userTz = getUserTimeZone(user);
		if(userTz == null){
			userTz =  TimeZone.getTimeZone(Global.DefaultTimeZone);
//			customizedTz = false;
		}
		
		Calendar systemCal = Calendar.getInstance();
		systemCal.setTime(date);
		//convert input date with user same timezone, then compare day is reasonable
		systemCal.setTimeZone(userTz);
		int givenYear = systemCal.get(Calendar.YEAR);
		int givenMth = systemCal.get(Calendar.MONTH);
		int givenDate = systemCal.get(Calendar.DATE);
		
		//now date must be user timezone date
		Calendar userCal = Calendar.getInstance(userTz);
		int nowYear = userCal.get(Calendar.YEAR);
		int nowMth = userCal.get(Calendar.MONTH);
		int nowDate = userCal.get(Calendar.DATE);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// format date 
		SimpleDateFormat dateFormat = null;
		String retDate = null;
		long timeDiff = userCal.getTimeInMillis() - systemCal.getTimeInMillis();
		
		//timeDiff > 0 just for failure tolerance, as this method is not intend display future time.
		if(TIME_FORMAT_PASSED == format && timeDiff < PASSED_TIME_GAP && timeDiff > 0){ 
			timeDiff = timeDiff/60000; //to minute
			if(timeDiff == 0L){
				//less than 1 minute:less a minute
				retDate = msg.getMessage("less.a.minute.ago");
			}else if(timeDiff < 60){
				//less than 1 hour: xx minute(s) ago
				retDate = msg.getMessage((timeDiff == 1)?"minute.ago":"minutes.ago", String.valueOf(timeDiff));
			}else if(timeDiff < 24*60){
				//less than 1 day: xx hour(s) ago
				timeDiff = (timeDiff/60); //to hours
				retDate = msg.getMessage((timeDiff == 1)?"hour.ago":"hours.ago", String.valueOf(timeDiff));
			}else{
				//greater than 24 hours under 3 days: HH:mm yesterday(xx days ago) 
				dateFormat = new SimpleDateFormat("HH:mm");
				dateFormat.setTimeZone(userTz);
				retDate = dateFormat.format(systemCal.getTime());
				//append yesterday(xx days ago) 
				retDate = (timeDiff < 48*60)?msg.getMessage("yesterday.ago", retDate):
					msg.getMessage("days.ago", new String[]{retDate, String.valueOf((timeDiff/(24*60)))});
			}
		}else{
			if(nowYear == givenYear){
				if(nowDate == givenDate && nowMth == givenMth){
					dateFormat = new SimpleDateFormat("HH:mm '"+msg.getMessage("today")+"'");
				}else{
					dateFormat = new SimpleDateFormat("HH:mm MMM dd");
				}
			}else{
				dateFormat = new SimpleDateFormat("HH:mm MMM dd yyyy");
			}
			dateFormat.setTimeZone(userTz);
			//TODO: as curtomized timezone maybe is null display Global.DefaultTZ is ugly ... TBD
			retDate = dateFormat.format(systemCal.getTime()); // + (customizedTz?"":(" "+Global.DefaultTimeZone));
		}
		
		if(withPrep){
			retDate = msg.getMessage("at.time",retDate);
		}
		
		return retDate;
		
	}
	/**
	 * @param user
	 * @return
	 */
	public static Locale getUserLocal(User user) {
		Locale userLocal = null;
		if(user != null){
			UserSetting set = user.getSetting();
    		String userLang = set.getLocaleLanguage();
    		String userCountry = set.getLocaleCountry();
    		if(userLang != null && userCountry != null){
    			userLocal = new Locale(userLang,userCountry);
    		}
		}
		return userLocal;
	}


	/**
	 * @param user
	 * @param userTz
	 * @return
	 */
	public static TimeZone getUserTimeZone(User user) {
		TimeZone userTz = null;
		if(user != null){
			UserSetting set = user.getSetting();
			String userTzID = set.getTimeZone();
			if(userTzID == null){
				//try to get timezone from HttpSession, which will be initial set in SecurityControllerImpl.checkLogin() method
				HttpServletRequest request = WebUtil.getRequest();
				if(request != null){
					HttpSession session = request.getSession();
					if (session != null) {
						userTz = (TimeZone) session.getAttribute(Constants.TIMEZONE);
					}
				}
			}else{
				userTz = TimeZone.getTimeZone(userTzID);
			}
		}
		return userTz;
	}
	/**
	 * 
	 * @param startDate
	 * @param endDate
	 * @return startDate - endDate
	 */
	 public static long diffInDays(Calendar startDate, Calendar endDate) {
		 int minus = -1;
		 if(startDate.after(endDate)){
			 minus = 1;
			 Calendar m = startDate;
			 startDate = endDate;
			 endDate = m;
		 }
	    Calendar date = (Calendar) startDate.clone();
	    long daysBetween = 0;
	    while (date.before(endDate)) {
	      date.add(Calendar.DAY_OF_MONTH, 1);
	      daysBetween++;
	    }
	    return daysBetween * minus;
	  }

	
//	public static void main(String[] args) {
////String[] ids = TimeZone.getAvailableIDs();
////for (String s : ids) {
////	System.out.println(s);
////	
////}
//Global.DefaultTimeZone = TimeZone.getTimeZone("Asia/Harbin");
//Calendar cal = Calendar.getInstance();
////TimeZone.getTimeZone("Asia/Harbin")
////cal.setTimeZone();
//cal.set(2006, 9 , 12, 12, 30, 50);
//
//User user = new User();
//UserSetting s = new UserSetting();
//s.setTimeZone("Asia/Harbin");
//s.setLocaleCountry("CN");
//s.setLocaleLanguage("zh");
//user.setSetting(s);
//
//String o = DateUtil.getSmartDate(user, cal.getTime());
////cal.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
////Calendar userCal = Calendar.getInstance(new Locale("EN","AU"));
////cal.set(2007, 11, 10, 12, 30, 50);
////userCal.setTime(cal.getTime());
//
//
////System.out.println(userCal.getTimeInMillis() + ": " + TimeZone.getTimeZone("GMT-8:00").getOffset(userCal.getTimeInMillis()));
////System.out.println(userCal.getTimeInMillis() + ": " + Global.DefaultTimeZone.getOffset(userCal.getTimeInMillis()));
////String o = DateFormat.getgetDateTimeInstance(DateFormat.LONG,DateFormat.LONG).format(cal.getTime());
////SimpleDateFormat format = new SimpleDateFormat();
////System.out.println(format.getTimeZone());
////String o = format.format(cal.getTime());
//System.out.println(o);
//}
}
