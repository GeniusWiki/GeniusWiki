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

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;

/**
 * @author Dapeng.Ni
 */
public class TableRowMacro extends BaseMacro{

	static final String NAME = "rowdiv";

	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		buffer.append("</tr><tr>");
	}

	public String[] getName() {
		return new String[]{NAME};
	}

	public boolean isPaired() {
		return false;
	}
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		// Do nothing, work is done in {table} macro
	}

}
