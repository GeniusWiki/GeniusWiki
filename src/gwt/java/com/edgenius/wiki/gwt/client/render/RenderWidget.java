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
package com.edgenius.wiki.gwt.client.render;

import com.edgenius.wiki.gwt.client.model.UserModel;



/**
 * If any widget implement this interface, it means the widget may render some content to page so that it need notice
 * PageRender.RenderContentListener() has new text...
 * 
 * This is useful for ajax tag render, such as tagcloud, which has ajax call after widget put on renderPanel.
 * 
 * @author Dapeng.Ni
 */
public interface RenderWidget {
	
	public void onLoad(String widgetKey, UserModel currentUser, RenderWidgetListener listener);
	
	public void onUserChanged(UserModel user);
}
