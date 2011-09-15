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


/**
 * @author Dapeng.Ni
 */
public interface PageEventListener extends EventListener{
	/**
	 * 
	 * @param pageUuid
	 * @throws PageEventHanderException  if the implementation doesn't want to roll back the page save transaction, but want to log errors, then
	 * 									 throw this exception.
	 * @throws PageException  if the implementation want the page save transaction rolled back, then throw this exception.
	 */
	public void pageSaving(String pageUuid) throws PageEventHanderException, PageException;
	/**
	 * @throws PageEventHanderException  if the implementation doesn't want to roll back the page save transaction, but want to log errors, then
	 * 									 throw this exception.
	 * @throws PageException  if the implementation want the page save transaction rolled back, then throw this exception.
	 */
	public void pageRemoving(String pageUuid, boolean permanent) throws PageEventHanderException, PageException;
}
