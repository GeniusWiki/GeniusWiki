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

import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.BooleanUtil;
import com.edgenius.wiki.render.Macro;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;

/**
 * @author Dapeng.Ni
 */
public class TableCellMacro  extends BaseMacro{

	static final String NAME = "cell";
	private static final String COL_SPAN = "colspan";
	private static final String ROW_SPAN = "rowspan";

	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		/*
		 * !!! Important
		 * This class has an assumption: Table macro must processed before its cells. This is rely on 
		 * MacroFilter.getRegions() to return correct sequence region. Then MarkupRenderEngineImpl.processRegions() 
		 * process region by this sequence...
		 * 
		 * If any change impact this process sequence, here has to use ObjectHandler to eliminate sequence dependent. 
		 */
		int[] tableInfo = TableGroupProcessor.getTableInfo(params);
		if(tableInfo == null || tableInfo.length != 4){
			AuditLogger.error("There is no valid Table Group processed for key:" + params.getParam(Macro.GROUP_KEY));
			//Failure tolerance
			buffer.append("<td>").append(params.getContent()).append("</td>");
			return;
		}
		
		RenderContext context = params.getRenderContext();
		Map<Integer,Map<String,String>> tableParams = (Map<Integer, Map<String, String>>) context.getGlobalParam(TableMacro.class.getName());
		String style = null;
		boolean hasTitle = false;
		boolean isLayout = false;
		if(tableParams != null){
			Map<String, String> tableParam = tableParams.get(tableInfo[0]);
			//this is for non-Grid check 
			if(tableParam != null){
				hasTitle = BooleanUtil.toBoolean(tableParam.get(TableMacro.FIRST_LINE_AS_TITLE));
				style = tableParam.get(TableMacro.CELL_STYLE);
				isLayout = BooleanUtil.toBoolean(tableParam.get(TableMacro.IS_GRID_LAYOUT));
			}
		}
		String cellWidth = "";
		if(isLayout){
			int columns = tableInfo[3];
			if(columns > 0){
				cellWidth =  " width='" + 100/columns +"%' ";
			}
		}
		StringBuffer att = new StringBuffer();
		String colspan = params.getParam(COL_SPAN);
		if(!StringUtils.isBlank(colspan)){
			att.append("colspan=").append(colspan);
		}
		String rowspan = params.getParam(ROW_SPAN);
		if(!StringUtils.isBlank(rowspan)){
			if(att.length() >  0)
				att.append(" ");
			att.append("rowspan=").append(rowspan);
		}
		if(att.length() > 0){
			att.insert(0, " ");
		}
		
		String start = "<td" + (StringUtils.isBlank(style)?"":(" " + style))+ att.toString() + cellWidth + ">";
		String end = "</td>";
		if(tableInfo[1] == 0){
			//first row
			
			if(hasTitle){
				start = "<th" + (StringUtils.isBlank(style)?"":(" " + style)) + att.toString() + cellWidth + ">";
				end = "</th>";
			}
		}
		
		StringBuffer content = new StringBuffer(params.getContent().replaceAll("\\r", ""));
		if(content.length() == 0)
			content.append("&nbsp;");
		buffer.append(start).append(content).append(end);
	}

	public String[] getName() {
		return new String[]{NAME};
	}

	public boolean isPaired() {
		return true;
	}

	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		// Do nothing, work is done in {table} macro
	}

}
