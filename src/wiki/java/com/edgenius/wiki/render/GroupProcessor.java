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
package com.edgenius.wiki.render;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.util.AuditLogger;

/**
 * If Macro has children, it must implement GroupProcessorMacro interface, which need GroupProcessor.
 * 
 * This class is able to adopt children(e.g. tableCell) for surrounding macro(e.g. table).  
 * @author Dapeng.Ni
 */
public abstract class GroupProcessor {
	//key: position of child macro in render text,  value: child macro object
	protected LinkedHashMap<Integer,Macro> childrenMap = new LinkedHashMap<Integer, Macro>();
	protected Map<Integer,Integer> endMap = new HashMap<Integer, Integer>();
	
	protected int previousMacroStart;
	
	private String[] children;
	protected int groupID;

	/**
	 * @param macro 
	 * @param start
	 */
	public GroupProcessor(Macro surroundMacro, int start,int end) {
		children = surroundMacro.hasChildren();
		childrenMap.put(start,surroundMacro);
		//trick: surroundMacro as initial previousMacroStart
		previousMacroStart = end;
		//trick, put start as end for surroundMacro
		endMap.put(start, start);
		
		this.groupID = start;
	}

	/**
	 * Combine a group KEY for implemented child class - which is better includes enough information not only for group,
	 * but also for current children's extra information. such as Table Processor, which needs 
	 * identify the cell in which row then decide it is first-line-as-title. So for key, it is groupID+rowNumber+cellNumber
	 * 
	 * As design, childrenMap is sorted map, so here can retrieve childrenMap and figure out the specified macro's location information.
	 * For example, it is able to figure out the table cell is in which row and column. 
	 * 
	 * @param start Current group element's position. Get child macro object by childrenMap.get(start).
	 * @return
	 */
	public abstract String getGroupKey(int start);
	/**
	 * Insert a group key as macro parameter
	 * @param start 
	 * @param sb
	 */
	public void insertGroupKey(int start, StringBuilder sb) {
		Macro macro = childrenMap.get(start);
		if(macro == null){
			AuditLogger.error("Unable to find macro in position");
			return;
		}
		
		int sep;
		
		String first = ":";
		String last = "";
		for(sep=start;sep<sb.length();sep++){
			char c = sb.charAt(sep);
			if(c =='}'){
				break;
			}
			if( c == ' ')
				continue;
			if(c == ':' ){
				sep++;
				first = "";
				last = "|";
				break;
			}
		}
		//insert after macro name, such as "{table" and the space after macro name will be next char of insertion.
		sb.insert(sep,first + Macro.GROUP_KEY+"="+getGroupKey(start) + last);
	}

	/**
	 * Try to decide if given macro is child. If yes, adopt it. Otherwise, discard it.
	 * @param macro
	 * @param start
	 */
	public void adoptChild(Macro macro, int start, int end) {
		boolean isChild = false;
		for (String child: children) {
			for(String mn :macro.getName()){
				if(StringUtils.equalsIgnoreCase(mn,child)){
					isChild = true;
					break;
				}
			}
		}
		
		if(!isChild)
			return;
		
		childrenMap.put(start,macro);
		endMap.put(start, end);
	}

	/**
	 * @return
	 */
	public Set<Integer> getPositions() {
		return childrenMap.keySet();
	}

	/**
	 * 
	 * Remove all newline between table/tbody/tr/td/th/caption etc content
	 * this special useful for tinyMCE, which cannot display correctly if \n between <table><tr>\n<td>
	 * TODO: this has assumption: user must input valid {cell} pair inside table... it is better do pre-process for valid table/cell pair
	 * @return
	 */
	public boolean suppressNewlineBetweenElements() {
		return true;
	}

	public int getMacroEnd(int start) {
		Integer end = endMap.get(start);
		return end == null?-1:end;
	}
	public int getPreviousMacroStart() {
		return previousMacroStart;
	}

	public void setPreviousMacroStart(int lastMacroStart) {
		this.previousMacroStart = lastMacroStart;
	}

}
