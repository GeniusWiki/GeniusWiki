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
package com.edgenius.wiki.render.filter;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.edgenius.wiki.gwt.client.html.HTMLNodeContainer;
import com.edgenius.wiki.render.RenderContext;

/**
 * Most filter need some special characters as boundary, such as, bold need
 * surrounding by spaces or any \\p{Punct} characters. If rich editor marked bold is not surrounding by them, it is 
 * impossible to do markup and rich content switch. For example, markup text "abc*bold*def" render any bold text. 
 * So, this filter will help to do this  "abc%%*bold*%%def", will render to <pre>"abc<strong>bold</strong>def"</pre>
 * 
 *   
 * @author Dapeng.Ni
 */
public class SeparatorFilter extends BasePatternTokenFilter{
	

	//JDK1.6 @Override
	public void init(){
		regexProvider.compile(getRegex(), Pattern.DOTALL);
	}
	public String getPatternKey() {
		return "filter.separator";
	}

	@Override
	public void replace(StringBuffer buffer, MatchResult result, RenderContext context) {
		String content = result.group(1);
		if(content.length() > 2){
			//at least, there are some content between this filter markup, such as %%some text%%
			//remove surrounding "%%"
			buffer.append(content);
		}else{
			buffer.append(result.group(0));
		}
	}
	
	@Override
	public HTMLNodeContainer filter(HTMLNodeContainer nodeList, RenderContext context) {
		//do nothing while rich content convert to markup 
		return nodeList;
	}
}
