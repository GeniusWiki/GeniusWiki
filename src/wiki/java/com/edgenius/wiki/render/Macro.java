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

import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.edgenius.wiki.gwt.client.html.HTMLNodeContainer;

/**
 * @author Dapeng.Ni
 */
public interface Macro {
	public static final int TIDY_STYLE_NO = 0;
	/**
	 * For instance, {warn} macro convert to div tag. If HTML input is <br>
	 * "some text<div id=warn">waring msg</div>end text". <br>
	 * The block style will put newline surrounding the {warn} macro like this:<br>
	 * <pre>
	 * some text
	 * {macro}
	 * warning msg
	 * {macro}
	 * end text
	 * </pre>
	 */
	public static final int TIDY_STYLE_BLOCK = 1;
	
	public static final String GROUP_KEY = "edgnius_group_key";
	/**
	 * Call while after marco object initialized.
	 */
	void init(ApplicationContext applicationContext);
	/**
	 * Allow multiple name mapping to same macro, for example, image=img, warning=warn
	 * @return
	 */
	String[] getName();
	
	/**
	 * Does this macro will recursive handle embedded macro? For example {preview} and {code} will don't need process embedded macro. 
	 * @return
	 */
	boolean isProcessEmbedded();
	/**
	 * @param buffer
	 * @param params
	 */
	void execute(StringBuffer buffer, MacroParameter params)  throws MalformedMacroException;

	/**
	 * Execute macro against same name Freemarker template which is from PluginTemplateEngine. This function is for advance macro,  
	 * normally which is using NameMacroHandler, render to plain HMTL format
	 * @param params
	 * @return the map for Freemarker template render usage.
	 */
	Map<String,Object> getTemplValues(MacroParameter params, RenderContext renderContext, ApplicationContext appContext);

	/**
	 * Macro is use paired macro or single macro. For example, paired is {code}some code{code}, single is {toc}.
	 * @return
	 */
	boolean isPaired();
//	/**
//	 * Indicate if this macro display as block in HTML or not. 
//	 * @return
//	 */
//	boolean isBlock();
//	
	/**
	 * Return String type HTML identifier for this marco. The identifier is used to find out which HTML tag will be
	 * convert back to macro during HTML to Markup conversion.
	 * @return
	 */
	String getHTMLIdentifier();

	/**
	 * Convert HTMLNodeList according to Rich to Markup logic.
	 * @param nodeList
	 * @param context
	 * @return
	 */
	HTMLNodeContainer filter(HTMLNodeContainer nodeList, RenderContext context);
	/**
	 * If this macro has children elements, such as {table} macro has {cell} children macro inside.   
	 * @return
	 */
	String[] hasChildren();

}
