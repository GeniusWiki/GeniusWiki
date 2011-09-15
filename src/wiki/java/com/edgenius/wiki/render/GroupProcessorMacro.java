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

/**
 * If Macro.hasChildren() != null, it must implement this interface to provide a valid GroupProcessor.
 * @author Dapeng.Ni
 */
public interface GroupProcessorMacro {

	/**
	 * @param macro 
	 * @param start macro start position in render text 
	 * @param end macro end position in render text. If macro is paired, then then end is its paired(close) end.  For example
	 * {cell}abc{cell}, end is the line lenght - 15. 
	 * @return
	 */
	GroupProcessor newGroupProcessor(Macro macro, int start, int end);

}
