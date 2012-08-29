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

import java.util.List;

/**
 * @author Dapeng.Ni
 */
public interface DiffService {
	public static final String diffToHtml = "diffToHtml";

	/**
	 * This method is called in history comparison.It directly returns HTML text for UI rendering. 
	 * !!! the input uid1 or uid2 would be -1 if it is current page 
	 * @param uid1
	 * @param uid2
	 * @param byword
	 * @return
	 * @throws DiffException
	 */
	String diffToHtml(Integer uid1, Integer uid2,boolean byword) throws DiffException;

	/**
	 * When user try to save a page, but an exist page has higher version than saving page(version 
	 * conflict), a message will prompt user to compare or force save. If user choose compare, This method is called 
	 * to render a diff page, which provides ability to allow user merge/reject different in a popup menu.
	 * 
	 * @param text1
	 * @param text2
	 * @param byword
	 * @return
	 * @throws DiffException
	 */
	List<DeltaObject> diffToObjectList(String text1, String text2, boolean byword) throws DiffException;
}
