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
package com.edgenius.wiki.gwt.client.page;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public interface PanelSwitchListener {

	/**
	 * @param index
	 */
	void onPanelSwitched(Widget src, int toIndex);

	/**
	 * Please note, the panel may be the loading panel when this method is executed. The execute sequence is
	 * 1. HyperLink reset token
	 * 2. PageMain.onHistoryChange() is executed.
	 * 3. 
	 * @param index
	 * @return false give up panel switch.  
	 * @See PageMain.onHistoryChange().
	 */
	boolean onPanelSwitching(Widget src, int toIndex);

}
