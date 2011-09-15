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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.util.StringEscapeUtil;
import com.edgenius.wiki.gwt.client.html.HTMLUtil;
import com.edgenius.wiki.gwt.client.html.HtmlListener;
/**
 * Remove html tag to return pure text string.
 * @author Dapeng.Ni
 */
public class PureTextHtmlListenerImpl implements HtmlListener {
	private static final Logger log = LoggerFactory.getLogger(PureTextHtmlListenerImpl.class);
	private StringBuffer pureText;
	
	//********************************************************************
	//               implementation methods
	//********************************************************************

	public void content(String content) {
		//old code(20090429) uses StringEscapeUtils.unescapeHtml(content) - it may cause javascript attack
		//For example, this class only used in indexing so far, then input is escaped HTML from renderHMTL(Page) method 
		//So, this method input can be &lt;sript&gt;alert('attack')&lt;/sript&gt;
		//if to unescape text, then goes to <script>alert('attack')</script>
		//in search result display, this piece test will be inside a HTML() widget as highlighter require the client side must use HTML() 
		//to display highlight text, then this script is execute in client side then!
		pureText.append(content);
	}

	public void endDocument() {

	}

	public void startDocument() {
		pureText = new StringBuffer();
	}

	public void tag(String tagStr) {
		String tagname = HTMLUtil.getTagName(tagStr);
		if(tagname != null){
			//treat some tag as space. For example <br> will be space, it is useful to do word divide in Lucene Index
			for (String btag : HtmlTagName.VISIBLE_TAGS) {
				if(btag.equalsIgnoreCase(tagname)){
					//???Maybe replace by \n?
					pureText.append(" ");
					break;
				}
			}
		}else{
			log.warn("Unable to find tag name from string " + tagStr);
			pureText.append(StringEscapeUtil.unescapeHtml(tagStr));
		}
	}

	//********************************************************************
	//               return result method
	//********************************************************************
	public String getPureText(){
		return pureText.toString();
	}
}
