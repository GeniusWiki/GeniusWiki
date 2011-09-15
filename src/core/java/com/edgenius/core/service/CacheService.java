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
package com.edgenius.core.service;


public interface CacheService {
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//               Cache number
	public static final int CACHE_ALL = 0;
	public static final int CACHE_POLICY = 1;
	public static final int CACHE_SPACE_READING = 2;
	public static final int CACHE_PAGE_READING = 3;
	public static final int CACHE_PAGE_TREE_READING = 4;
	public static final int CACHE_USER_READING = 5;
	public static final int CACHE_TAG_READING = 6;
	public static final int CACHE_LOGINTIMES_READING = 7;
	public static final int CACHE_THEMES = 8;
	public static final int CACHE_PAGE_EDITING = 9;
	

	void printPolicyCache();
	
	void resetPolicyCache();
	void resetSpaceReadingCache();
	void resetPageReadingCache();
	void resetPageTreeCacheCache();
	void resetTagCache();
	void resetUserCache();
	void resetLoginTimesCache();
	void resetThemeCache();
	void resetPageEditingCache();

	/**
	 * @param cacheAll
	 */
	void reset(int cacheNumber);
}
