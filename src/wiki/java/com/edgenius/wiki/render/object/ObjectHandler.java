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
package com.edgenius.wiki.render.object;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.render.RenderContext;

/**
 * The ObjectHandler is processed after wiki text parsed. 
 *  
 * Macro object handler is managed by Object Pool. Please note this handler life cycle is NOT render scope. It means, 
 * the values of class level field is kept. Think it likes Servlet. The possible misuse is the class level 
 * field does not be reset in renderStart(). 
 *     
 * @author Dapeng.Ni
 */
public interface ObjectHandler {

	/**
	 * This method is processed after wiki text parsed. It is safe to return any value which won't break original wiki text input.
	 *  
	 * @param renderContext 
	 * @param values parameter values from Macro
	 * @return List of RenderPiece that appends the macro render list.
	 */
	List<RenderPiece> handle(RenderContext renderContext, Map<String,String> values) throws RenderHandlerException;
	
	/**
	 * This method will run once for each rendering whatever how many same ObjectPosition exist
	 * in render text. For instance, there are multiple image marco objects in render text, 
	 * this method will only be run when first image object occurs.
	 * 
	 * 
	 * NOTE: the given page maybe a full object read out from database or maybe a object only contains
	 * few fields, for example, the value of spaceUname or PageContent. PageUuid may be null when page does 
	 * not saved and try to render(e.g., when user switch rich/plain editor).
	 */
	void renderStart(AbstractPage page);

	/**
	 * This method will run once for each rendering whatever how many same ObjectPosition exist. 
	 * this method will only be run when first object occurs, same with rednerStart();
	 * in render text.
	 */
	void renderEnd();
	
	/**
	 * This method call once only for ObjectHandler instance initialized.
	 * (18/07/2008) I mark this method to deprecated. This method allows Macro has chance to access system service, but 
	 * it has several drawbacks: 
	 * <li>It makes macro logic a little confused. macro becomes too powerful as it can do anything thru service bean...</li> 
	 * <li>It makes hard implementation on offline model. Offline rich tag have to do powerful thing for these macro...</li>
	 * <li>Although it saves a little bit performance,e.g, SearchMacro could do search inside macro then render to server,
	 * but you can not avoid call Controller to implement pagination, which is almost same code with SearchMacro. Same case is 
	 * for TagCloud, which need Controller to get list pages for a tag, and macro itself do first tag page lists as well...  
	 * </li>
	 * 
	 * @param context
	 * 
	 */
	@Deprecated
	void init(ApplicationContext context);
}
