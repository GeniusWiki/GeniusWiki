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
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.render.Macro;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.handler.TOCHandler;
import com.edgenius.wiki.render.impl.RenderContextImpl;
import com.edgenius.wiki.render.object.ObjectPosition;

/**
 * 
 * Table of Content macro, support attribute (align|deep|ordered {true,false})
 * @author Dapeng.Ni
 */
public class TOCMacro extends BaseMacro{
	private static final String HANDLER = TOCHandler.class.getName();

	
	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		//default to heading 3
		RenderContext context = params.getRenderContext();
		ObjectPosition obj = new ObjectPosition(params.getStartMarkup());
		obj.serverHandler = HANDLER;
		//whatever, treat it as multiple line content
		obj.uuid = context.createUniqueKey(false);
		obj.values = params.getParams();
		context.getObjectList().add(obj);
		
		buffer.append(obj.uuid);
		
		((RenderContextImpl)context).setHasReferrer(true);

	}

	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		HTMLNode pair = node.getPair();
		if(pair == null){
			AuditLogger.error("Unexpected case: unable to find div close tag for " + this.getClass().getName());
			return;
		}
		//replace all content inside node to empty type
		HTMLNode subnode = node.next();
		while(subnode != null && subnode != pair){
			subnode.reset("", true);
			subnode = subnode.next();
		}
		String att = "";
		Map<String, String> map = RichTagUtil.parseWajaxAttribute(node.getAttributes().get(NameConstants.WAJAX));
		if(map.size() > 0){
			String deep = map.get(NameConstants.DEEP);
			deep = deep ==null?"":NameConstants.DEEP.toLowerCase()+"="+deep;
			String align = map.get(NameConstants.ALIGN);
			align = align ==null?"":NameConstants.ALIGN.toLowerCase()+"="+align;
			String order = map.get(NameConstants.ORDERED);
			order = order ==null?"":NameConstants.ORDERED.toLowerCase()+"="+order;
			att = deep+align+order;
		}
		if(!StringUtils.isBlank(att))
			att = ":" + att;
		
		resetMacroMarkup(Macro.TIDY_STYLE_NO, node, iter, "{toc"+att+"}", null);
		
	}

	public String[] getName() {
		return  new String[]{"toc","tableofcontent"};
	}

	public boolean isPaired(){
		return false;
	}
}
