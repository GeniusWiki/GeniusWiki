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

import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.handler.NameMacroHandler;
import com.edgenius.wiki.render.object.ObjectPosition;

/**
 * Open feedback dialog. Image could be an attachment from page or default "on" means use system default image.  
 * Normally title and image won't use together. 
 * {feedback:title=Give us some feedback|image=on}
 * <br> 
 *
 * @author Dapeng.Ni
 */
public class FeedbackMacro extends BaseMacro {
	
	private final static String HANDLER = NameMacroHandler.class.getName();
	
	//JDK1.6 @Override
	public String[] getName() {
		return new String[]{"feedback"};
	}
	@Override
	public String getHTMLIdentifier() {
		return "<div aid='feedback'>";
	}
	//JDK1.6 @Override
	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
	
		RenderContext context = params.getRenderContext();
		ObjectPosition obj = new ObjectPosition(params.getStartMarkup());
		obj.uuid = context.createUniqueKey(true);
		obj.serverHandler = HANDLER;
		obj.values.put(NameConstants.MACRO, SharedConstants.MACRO_FEEDBACK);
		
		Map<String, String> ps = params.getParams();
		if(ps != null){
			//put default title
			String title = ps.get(NameConstants.TITLE);
			if(StringUtil.isBlank(title)){
				title = "feedback";
				ps.put(NameConstants.TITLE, title);
			}
			
			//put default image
			String img = ps.get(NameConstants.IMAGE);
			if(NameConstants.ON.equalsIgnoreCase(img)){
				//it sucks, put static image for macro!!! will refactor sooner or later....
				img = WebUtil.getWebConext() + "static/images/feedback.png";
				ps.put(NameConstants.IMAGE, img);
			}else{
				//TODO: get from page attachment
				
			}
			obj.values.putAll(ps);
		}
		
		context.getObjectList().add(obj);
		
		buffer.append(obj.uuid);
	}
	public boolean isPaired(){
		return false;
	}
	
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		resetMacroMarkup(node, "feedback");
		resetInsideNode(node, iter);
	}

}
