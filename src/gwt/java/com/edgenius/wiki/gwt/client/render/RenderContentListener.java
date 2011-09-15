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
package com.edgenius.wiki.gwt.client.render;


/**
 * Render page may has some ajax component, such as portal, tagcloud, profile etc. These component has no content while PageRender
 * just submit RenderPanel. But after ajax call, they may have content. To detect whether they have content or not is necessary in
 * following scenario: 
 * 
 * <li>Display welcome message on space home page if that home page has not any text input.A little overhead use, 
 * but welcome message is important to tells newbie to do next actions.</li>
 * <li>Decide when the whole page complete render even ajax call end. It is helpful for Dashboard render(if it has portal macro).  
 * Only all ajax calls are completed, the PageMain switches deck to Dashborad. It looks nice, otherwise if some other content(logo etc) 
 *  already displayed but portal macro is still display a busy indicator...(Overhead again?)</li>
 * 
 * @author Dapeng.Ni
 */
public interface RenderContentListener {
	/**
	 * Before PageRender render any content, this method will be invoked.
	 */
	public void renderStart();
	/**
	 * This method may be called many times according to the coming-in RenderPiece list.
	 * 
	 * Please note, the input content is not exactly correct text content. Some RenderWidget has its owned HTML skeleton, such as
	 * tagCloud or profile etc. Only pure visible content will be input text. Any invisible content, such as HTML tag,  won't treat 
	 * as input text.
	 * 
	 * @param  
	 */
	public void render(String text);
	
	/**
	 * All ajax RenderWidget completed loading, whatever failed or success loading, the input text is "possible" visible text. "Possible"
	 * means the input content is not exactly correct text content.
	 *  
	 * @param text
	 */
	public void renderEnd(String text);
	
}
