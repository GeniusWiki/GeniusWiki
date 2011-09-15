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
 * A indicator for Macro which need original text. If macro implements this interface and any HTML tags inside its content 
 * is able to keep as original HTML tags. Otherwise, HTML tags is able to replace to entity code. <br>
 * 
 * The reason of this interface is because HTMLEscapeFilter is the only filter before MacroFilter.  
 * 
 * I don't want to put HTMLEscapeFilter after MacroFilter, it means any macro can not (or very hard) to append any HTML tag
 * while render Macro. And HTMLMacro is also need do special handling in HMTLEscapeFilter. It is complex than current solution.
 *  
 * @author Dapeng.Ni
 */
public interface ImmutableContentMacro {

}
