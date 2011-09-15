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
package com.edgenius.wiki.widget;

import org.springframework.context.ApplicationContext;

import com.edgenius.core.model.User;
import com.edgenius.wiki.model.Widget;

/**
 * @author Dapeng.Ni
 */
public interface WidgetTemplate {
	/**
	 * Unique value for widget object, usually return widget implementation class name. 
	 * @return
	 */
	public String getType();

	/**
	 * for most widget, key is null. It means this type widget is same for all instance. Such as Message board, My Drafts etc.
	 * But for spaceWidget, it will be spaceUname, then return different space name, title, desc etc.
	 * Create a widget instance, for most widget, it return same object, but for space, it return new widget with special space 
	 * space name, title, desc etc.
	 * 
	 * @param key
	 * @return
	 */
	public Widget createWidgetObject(String key);
	/**
	 * Initialize this widget by given key value
	 * @param key
	 */
	public void init(ApplicationContext applicationContext);

	/**
	 * @param viewer
	 * @return
	 */
	public boolean isAllowView(User viewer);

	/**
	 * @param key
	 * @param viewer 
	 * @return
	 */
	public Widget invoke(String key, User viewer) throws WidgetException;

	/**
	 * Clean widget template to null. 
	 */
	public void reset();
	
}
