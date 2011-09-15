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
package com.edgenius.wiki.gwt.client.model;

/**
 * @author Dapeng.Ni
 */
public interface RichTagPiece {

	
	/**
	 * 
	 * Convert object to a tag string, this string can be convert back to this object. 
	 * For example, convert LinkModel to <link wajax="..." href="....">
	 * @return
	 */
	abstract String toRichAjaxTag();
	/**
	 * Parse the tag string and fill in macroName and values
	 * 
	 * @param richAjaxString the open tag string, such as <a href="foo.com">
	 * @param surroundedText the tag enclosed text, such as "abc" from tag <a href="">abc</a>
	 */
	abstract void fillToObject(String richAjaxString, String enclosedText);
}
