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
package com.edgenius.wiki.gwt.client.server.utils;

/**
 * @author Dapeng.Ni
 */
public class NumberUtil {
	private static final char[] DIGIT = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

	/**
	 * @param string
	 * @param defaultValue
	 * @return
	 */
	public static int toInt(String str, int defaultValue) {
		if(str != null){
			try {
				return Integer.parseInt(str.trim());
			} catch (Exception e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * @param charAt
	 * @return
	 */
	public static boolean isDigit(char charAt) {
		for (int idx = 0; idx < 10; idx++) {
			if (charAt == DIGIT[idx])
				return true;
		}
		return false;
	}

	
}
