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
import com.edgenius.wiki.render.handler.PageAttributeHandler;
import com.edgenius.wiki.render.object.ObjectPosition;

/**
 * @author Dapeng.Ni
 */
public class PageAttributeMacro extends BaseMacro {
	public final static String HANDLER = PageAttributeHandler.class.getName();

	public String[] getName() {
		return new String[]{"page-attribute","pageattribute"};
	}

	public void execute(StringBuffer writer, MacroParameter params) throws MalformedMacroException {
		int len = params.getParamsSize();
		if (len > 0) {

			// put back SRC at one of attributes

			// this image scr will be handler after all page finish scan to HTML
			RenderContext context = params.getRenderContext();
			ObjectPosition attPos = new ObjectPosition(params.getStartMarkup());
			attPos.serverHandler = HANDLER;
			attPos.uuid = context.createUniqueKey(false);
			attPos.values = params.getParams();
			context.getObjectList().add(attPos);

			writer.append(attPos.uuid);
			return;
		}
	}
	public boolean isPaired(){
		return false;
	}

	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		// TODO Auto-generated method stub
		
	}
}
