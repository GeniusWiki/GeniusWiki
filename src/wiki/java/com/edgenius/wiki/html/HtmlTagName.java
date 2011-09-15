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
package com.edgenius.wiki.html;

/**
 * Define html tag name and their attributes. For example, visible, isolable.
 * @author Dapeng.Ni
 */
public class HtmlTagName {

	public HtmlTagName() {
	}


	protected HtmlTagName(String id) {
		name = id;
	}

	
	/**
	 * Returns the string representation of the
	 * tag.
		 *
		 * @return the <code>String</code> representation of the tag
	 */
	public String toString() {
		return name;
	}

	public String name;

	// --- Tag Names -----------------------------------

	public static final HtmlTagName A = new HtmlTagName("a");
	public static final HtmlTagName ADDRESS = new HtmlTagName("address");
	public static final HtmlTagName APPLET = new HtmlTagName("applet");
	public static final HtmlTagName AREA = new HtmlTagName("area");
	public static final HtmlTagName B = new HtmlTagName("b");
	public static final HtmlTagName BASE = new HtmlTagName("base");
	public static final HtmlTagName BASEFONT = new HtmlTagName("basefont");
	public static final HtmlTagName BIG = new HtmlTagName("big");
	public static final HtmlTagName BLOCKQUOTE = new HtmlTagName("blockquote");
	public static final HtmlTagName BODY = new HtmlTagName("body");
	public static final HtmlTagName BR = new HtmlTagName("br");
	public static final HtmlTagName CAPTION = new HtmlTagName("caption");
	public static final HtmlTagName CENTER = new HtmlTagName("center");
	public static final HtmlTagName CITE = new HtmlTagName("cite");
	public static final HtmlTagName CODE = new HtmlTagName("code");
	public static final HtmlTagName DD = new HtmlTagName("dd");
	public static final HtmlTagName DFN = new HtmlTagName("dfn");
	public static final HtmlTagName DIR = new HtmlTagName("dir");
	public static final HtmlTagName DIV = new HtmlTagName("div");
	public static final HtmlTagName DL = new HtmlTagName("dl");
	public static final HtmlTagName DT = new HtmlTagName("dt");
	public static final HtmlTagName EM = new HtmlTagName("em");
	public static final HtmlTagName FONT = new HtmlTagName("font");
	public static final HtmlTagName FORM = new HtmlTagName("form");
	public static final HtmlTagName FRAME = new HtmlTagName("frame");
	public static final HtmlTagName FRAMESET = new HtmlTagName("frameset");
	public static final HtmlTagName H1 = new HtmlTagName("h1");
	public static final HtmlTagName H2 = new HtmlTagName("h2");
	public static final HtmlTagName H3 = new HtmlTagName("h3");
	public static final HtmlTagName H4 = new HtmlTagName("h4");
	public static final HtmlTagName H5 = new HtmlTagName("h5");
	public static final HtmlTagName H6 = new HtmlTagName("h6");
	public static final HtmlTagName HEAD = new HtmlTagName("head");
	public static final HtmlTagName HR = new HtmlTagName("hr");
	public static final HtmlTagName HTML = new HtmlTagName("html");
	public static final HtmlTagName I = new HtmlTagName("i");
	public static final HtmlTagName IMG = new HtmlTagName("img");
	public static final HtmlTagName INPUT = new HtmlTagName("input");
	public static final HtmlTagName ISINDEX = new HtmlTagName("isindex");
	public static final HtmlTagName KBD = new HtmlTagName("kbd");
	public static final HtmlTagName LI = new HtmlTagName("li");
	public static final HtmlTagName LINK = new HtmlTagName("link");
	public static final HtmlTagName MAP = new HtmlTagName("map");
	public static final HtmlTagName MENU = new HtmlTagName("menu");
	public static final HtmlTagName META = new HtmlTagName("meta");
	/*public*/
	static final HtmlTagName NOBR = new HtmlTagName("nobr");
	public static final HtmlTagName NOFRAMES = new HtmlTagName("noframes");
	public static final HtmlTagName OBJECT = new HtmlTagName("object");
	public static final HtmlTagName OL = new HtmlTagName("ol");
	public static final HtmlTagName OPTION = new HtmlTagName("option");
	public static final HtmlTagName P = new HtmlTagName("p");
	public static final HtmlTagName PARAM = new HtmlTagName("param");
	public static final HtmlTagName PRE = new HtmlTagName("pre");
	public static final HtmlTagName SAMP = new HtmlTagName("samp");
	public static final HtmlTagName SCRIPT = new HtmlTagName("script");
	public static final HtmlTagName SELECT = new HtmlTagName("select");
	public static final HtmlTagName SMALL = new HtmlTagName("small");
	public static final HtmlTagName SPAN = new HtmlTagName("span");
	public static final HtmlTagName STRIKE = new HtmlTagName("strike");
	public static final HtmlTagName S = new HtmlTagName("s");
	public static final HtmlTagName STRONG = new HtmlTagName("strong");
	public static final HtmlTagName STYLE = new HtmlTagName("style");
	public static final HtmlTagName SUB = new HtmlTagName("sub");
	public static final HtmlTagName SUP = new HtmlTagName("sup");
	public static final HtmlTagName TABLE = new HtmlTagName("table");
	public static final HtmlTagName TD = new HtmlTagName("td");
	public static final HtmlTagName TEXTAREA = new HtmlTagName("textarea");
	public static final HtmlTagName TH = new HtmlTagName("th");
	public static final HtmlTagName TITLE = new HtmlTagName("title");
	public static final HtmlTagName TR = new HtmlTagName("tr");
	public static final HtmlTagName TT = new HtmlTagName("tt");
	public static final HtmlTagName U = new HtmlTagName("u");
	public static final HtmlTagName UL = new HtmlTagName("ul");
	public static final HtmlTagName VAR = new HtmlTagName("var");

	//customized tag:
	//All html file end by this tag
	public static final HtmlTagName EOF = new HtmlTagName("eof");
	/**
	 * All text content must be in a paragraph element.
	 * If a paragraph didn't exist when content was
	 * encountered, a paragraph is manufactured.
	 * <p>
	 * This is a tag synthesized by the HTML reader.
	 * Since elements are identified by their tag type,
	 * we create a some fake tag types to mark the elements
	 * that were manufactured.
	 */
	public static final HtmlTagName IMPLIED = new HtmlTagName("p-implied");

	/**
	 * All text content is labeled with this tag.
	 * <p>
	 * This is a tag synthesized by the HTML reader.
	 * Since elements are identified by their tag type,
	 * we create a some fake tag types to mark the elements
	 * that were manufactured.
	 */
	public static final HtmlTagName CONTENT = new HtmlTagName("content");

	/**
	 * All comments are labeled with this tag.
	 * <p>
	 * This is a tag synthesized by the HTML reader.
	 * Since elements are identified by their tag type,
	 * we create a some fake tag types to mark the elements
	 * that were manufactured.
	 */
	public static final HtmlTagName COMMENT = new HtmlTagName("comment");

	public static final HtmlTagName DOCTYPE = new HtmlTagName("!DOCTYPE");

	static final HtmlTagName allTags[] =
		{
			A,
			ADDRESS,
			APPLET,
			AREA,
			B,
			BASE,
			BASEFONT,
			BIG,
			BLOCKQUOTE,
			BODY,
			BR,
			CAPTION,
			CENTER,
			CITE,
			CODE,
			DD,
			DFN,
			DIR,
			DIV,
			DL,
			DT,
			EM,
			FONT,
			FORM,
			FRAME,
			FRAMESET,
			H1,
			H2,
			H3,
			H4,
			H5,
			H6,
			HEAD,
			HR,
			HTML,
			I,
			IMG,
			INPUT,
			ISINDEX,
			KBD,
			LI,
			LINK,
			MAP,
			MENU,
			META,
			NOBR,
			NOFRAMES,
			OBJECT,
			OL,
			OPTION,
			P,
			PARAM,
			PRE,
			SAMP,
			SCRIPT,
			SELECT,
			SMALL,
			SPAN,
			STRIKE,
			S,
			STRONG,
			STYLE,
			SUB,
			SUP,
			TABLE,
			TD,
			TEXTAREA,
			TH,
			TITLE,
			TR,
			TT,
			U,
			UL,
			VAR,
			EOF};
			
			
	public static final String[] VISIBLE_TAGS = new String[]{
		IMG.name,
		HR.name,
		LI.name,
		UL.name,
		OL.name,
		BR.name,
		P.name,
		H1.name,
		H2.name,
		H3.name,
		H4.name,
		H5.name,
		H6.name,
		TABLE.name,
		TR.name,
		TD.name,
		BLOCKQUOTE.name,
		DIV.name,
		EOF.name
	};
	public static final String[] ISOLABLE_TAGS = new String[]{
		DOCTYPE.name,
		META.name,
		BR.name,
		INPUT.name,
		OPTION.name,
		IMG.name,
		LI.name,
		P.name,
//		"ALIGN",
		PARAM.name,
		HR.name,
		EOF.name
	};
	
	public static final String[] ISBLOCK_TAGS = new String[]{
		TABLE.name,
		DIV.name,
		BLOCKQUOTE.name
	};

}
