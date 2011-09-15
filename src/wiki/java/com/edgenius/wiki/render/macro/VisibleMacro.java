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

import com.edgenius.core.model.User;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.util.WikiUtil;

/**
 * Display content according to login/logout status:
 * {visible:on=logout} some content only view while status is logout {visible) 
 * {visible:on=login}some content only view while status is login {visible)
 * 
 * @author Dapeng.Ni
 */
public class VisibleMacro extends BaseMacro {
	
	//JDK1.6 @Override
	public String[] getName() {
		return new String[]{"visible"};
	}
	@Override
	public String getHTMLIdentifier() {
		return "<img aid='visible'>";
	}
	//JDK1.6 @Override
	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		User user = WikiUtil.getUser();
		String status = params.getParam(NameConstants.ON);
		if(user == null || user.isAnonymous() ){
			if(NameConstants.LOGOUT.equalsIgnoreCase(status)){
				//display logout content for anonymous user
				buffer.append(params.getContent());
			}
		}else{
			if(NameConstants.LOGIN.equalsIgnoreCase(status)){
				//display login content for login user
				buffer.append(params.getContent());
			}
			
		}
	}
	public boolean isPaired(){
		return true;
	}
	
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		resetMacroMarkup(node, "visible");
		
		//TODO: how to handle inside tag in visible? Just let as it is???
	}
}
