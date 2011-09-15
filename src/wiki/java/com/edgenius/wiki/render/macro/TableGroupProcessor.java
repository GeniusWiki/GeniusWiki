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
package com.edgenius.wiki.render.macro;

import java.util.Map.Entry;

import com.edgenius.wiki.gwt.client.server.utils.NumberUtil;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.GroupProcessor;
import com.edgenius.wiki.render.Macro;
import com.edgenius.wiki.render.MacroParameter;

/**
 * @author Dapeng.Ni
 */
public class TableGroupProcessor extends GroupProcessor {

	/**
	 * @param surroundMacro
	 * @param start
	 */
	public TableGroupProcessor(Macro surroundMacro, int start, int end) {
		super(surroundMacro, start, end);
		
	}

	/**
	 * Return a constructed groupID, row, cell and its cells count in current row combination if it is cell . Otherwise, only groupID and row combination if it is row.
	 * Only return groupID if it is table.
	 * For example, table groupID "0", row groupID "0-1" (first row), cell groupID "0-1-1-3"(2nd row, 2nd column, and total 3 cells in this row)
	 */
	@Override
	public String getGroupKey(int start) {
		int row=0;int cell=0;
		boolean cellDone = false;
		int column = 1;
		for (Entry<Integer,Macro> entry: childrenMap.entrySet()) {
			Macro macro = entry.getValue();
			
			if(cellDone && !StringUtil.containsIgnoreCase(macro.getName(), TableCellMacro.NAME)){
				//column already counter done
				break;
			}
			if(entry.getKey() == start){
				if(StringUtil.containsIgnoreCase(macro.getName(), TableRowMacro.NAME)){
					row++;
					cell = -1;
					break;
				}
				if(StringUtil.containsIgnoreCase(macro.getName(), TableMacro.NAME)
					|| StringUtil.containsIgnoreCase(macro.getName(), TableMacro.GRID_NAME)){
					cell = -1;
					row = -1;
					break;
				}
				//cell - need calculate the total cells count in this row
				cellDone = true;
				column = cell;
			}
			if(StringUtil.containsIgnoreCase(macro.getName(), TableRowMacro.NAME)){
				row++;
				cell=0;
				column = 1;
			}else if(StringUtil.containsIgnoreCase(macro.getName(), TableCellMacro.NAME)){
				if(!cellDone){
					cell++;
				}else{
					++column; 
				}
			}
			
		}
		return groupID+(row==-1?"":("-" +row +(cell==-1?"":("-"+cell+"-"+column))));
	}



	/**
	 * @param param
	 * @return
	 */
	public static int[] getTableInfo(MacroParameter params) {
		String param = params.getParam(Macro.GROUP_KEY);
		if(param == null)
			return null;
		
		//this is helper method to decouple GroupKey into tableID, rowNum and cellNum, refer to how getGroupKey() build groupKey 
		String[] vals = param.split("-");
		int ret[] = new int[vals.length];
		for (int idx=0;idx < vals.length;idx++) {
			ret[idx] = NumberUtil.toInt(vals[idx],-1);
			//treat invalid if any illegal value 
			if(ret[idx] == -1)
				return null;
		}
		return ret;
	}
}
