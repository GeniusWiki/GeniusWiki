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

import java.util.regex.Pattern;

/**
 * @author Dapeng.Ni
 */
public class FilterRegxConstants {

	// In this pattern the after char of "\" must not contain &(Entity leading)
	public static final String FILTER_KEYWORD = "_^-*~#!@[]{}|()";
	//some special condition for filter, so that the escapeMarkupToSlash works more exactly
	
	//there are slash "\\" are not filter keyword, but need do slash
	//it needs consider LinkFilter keyword,ie, @,>,^ and # 
	public static final String FILTER_ANYTEXT_KEYWORD = "_^\\@>#";
	//this key word, must surround by non_word, whatever in front or end of it.
	public static final String FILTER_SURR_NON_WORD_KEYWORD = "-*~#!@[]{}";
	public static final String FILTER_ONLYLINESTART_KEYWORD = "|";
	//at moment it does not do any escape on it   
	public static final String FILTER_EMOTIONS_KEYWORD = "()";
	
	public static final Pattern NON_WORD_PATTERN = Pattern.compile("[\\p{Space}\\p{Punct}]");
	
	//parse out "{blog}" macro so that the page which contains this macro won't incurs infinite looping while doing render for blog
	public static final Pattern BLOG_FILTER_PATTERN = Pattern.compile("\\{blog(?:[:|]([^\\}]*))?\\}");
	
	public static final Pattern SINGLE_MACRO_FILTER_PATTERN = Pattern.compile("\\{([^\\p{Space}:|}]+)(?:[:|]([^\\}]*))?\\}");
	
	//parse out {piece:name=foo}content{piece}
	public final static String PIECE_MACRO_PATTERN = "^\\{piece(?:[:|]([^\\}]*))?\\}(.*?)\\{piece\\}$";
	public final static Pattern PIECE_MACRO_START_TAG_PATTERN = Pattern.compile("^\\{piece(?:[:|]([^\\}]*))?\\}");
	
	public final static String SINGLE_MACRO = "\\{([^\\p{Space}:|}]+)(?:[:|]([^\\}]*))?\\}";
	
	//as paired macro is chopped into independent string and match, so it must start from line start and end 
	public final static String PAIRED_MACRO = "^\\{([^\\p{Space}:|}]+)(?:[:|]([^\\}]*))?\\}(.*?)\\{\\1\\}$";
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// some filter use same pattern,to keep consistent, let them build out from same string 
	// @see setRegex()
	
	
	//last TOKEN ensure consequence pattern can be convert as well
	//for example _under__123_, if no last token, _under__ will render first(last group eat third _), but 123_ does not render
	public static final String PATTERN_NORMAL_SURROUNDING = 
		"(^|\\G|[\\p{Space}\\p{Punct}]+)\\@@TOKEN@@([^\\p{Space}].*?[^\\p{Space}])\\@@TOKEN@@([\\p{Space}\\p{Punct}&&[^@@TOKEN@@]]+|$)";
	
	//there are some change on Anchor: &#98; is entity!, so ignore it, and anchor only allow space, digit and letter inside!
	public static final String PATTERN_ANCHOR = 
		"(^|\\G|[\\p{Space}\\p{Punct}&&[^&]]+)\\@@TOKEN@@([^\\p{Space}][ \\w\\d]*?[^\\p{Space}])\\@@TOKEN@@([\\p{Space}\\p{Punct}&&[^@@TOKEN@@]]+|$)";
	
	public static final String PATTERN_SINGLE_TOKEN = 
		"(^|\\G|[\\p{Space}\\p{Punct}]+)@@TOKEN@@([\\p{Space}\\p{Punct}]+|$)";
	
	public static final String PATTERN_ANYTEXT_SURROUNDING = "\\@@TOKEN@@([^\\p{Space}].*?[^\\p{Space}])\\@@TOKEN@@";

	//skip [ as it is treat is link container. When check [!image.png!], ! should surround separator %% 
	public static final Pattern BORDER_PATTERN = Pattern.compile("[\\p{Space}\\p{Punct}&&[^\\[]]");
	//space and SeparatorFilter markup "%%"
	public static final Pattern SPACE_PATTERN = Pattern.compile("[\\p{Space}%%]");
	
	public static final String PATTERN_REP_TOKEN = "@@TOKEN@@";
	public static final String PATTERN_NORMAL_KEY = "PATTERN_NORMAL_SURROUNDING";
	public static final String PATTERN_ANYTEXT_KEY = "PATTERN_ANYTEXT_SURROUNDING";
	public static final String PATTERN_SINGLE_KEY = "PATTERN_SINGLE_TOKEN";
	public static final String PATTERN_ANCHOR_KEY = "PATTERN_ANCHOR";

	public static final String PRINT_VARIABLE = "PRINT_VARIABLE";

	
}
