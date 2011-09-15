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
package com.edgenius.wiki.ext.tabs;

import java.util.Map.Entry;

import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.GroupProcessor;
import com.edgenius.wiki.render.Macro;

/**
 * @author Dapeng.Ni
 */
public class TabsGroupProcessor extends GroupProcessor {

	/**
	 * @param surroundMacro
	 * @param start
	 */
	public TabsGroupProcessor(Macro surroundMacro, int start, int end) {
		
		super(surroundMacro, start, end);
		
	}

	@Override
	public String getGroupKey(int start) {
		int tab=0;
		for (Entry<Integer,Macro> entry: childrenMap.entrySet()) {
			Macro macro = entry.getValue();
			if(entry.getKey() == start){
				if(StringUtil.containsIgnoreCase(macro.getName(), TabMacro.NAME)){
					tab++;
				}
				if(StringUtil.containsIgnoreCase(macro.getName(), TabsMacro.NAME0)
					||StringUtil.containsIgnoreCase(macro.getName(), TabsMacro.NAME1)){
					tab = -1;
				}
				break;
			}
		
			if(StringUtil.containsIgnoreCase(macro.getName(), TabMacro.NAME)){
				tab++;
			}
		}
		return groupID+(tab == -1?"":"-"+tab);
	}

}
