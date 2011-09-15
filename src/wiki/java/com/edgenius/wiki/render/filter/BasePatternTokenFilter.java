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
import java.util.regex.Matcher;

import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.TokenVisitor;

/**
 * @author Dapeng.Ni
 */
public abstract class BasePatternTokenFilter extends BasePatternFilter {
	
	public abstract void replace(StringBuffer buffer, MatchResult matchResult, RenderContext context);

	@Override
	public String filter(String input, final RenderContext context) {
		String result = input;
		result = regexProvider.replaceByTokenVisitor(result, new TokenVisitor<Matcher>() {
			public void handleMatch(StringBuffer buffer, Matcher matcher) {
				replace(buffer, matcher.toMatchResult(), context);
			}
		});
		
		return result;
	}

}
