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

import java.text.MessageFormat;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;

/**
 * TODO: so far, anchor text could be replace by other Filters, such as #--123--# will not correct work. 
 * It may use Handler to solve this problem...
 * @author Dapeng.Ni
 */
public class AnchorFilter  extends BasePatternTokenFilter {
	private static final Pattern VALID = Pattern.compile("[a-zA-Z 0-9]+"); 
	private MessageFormat formatter;
	
	//JDK1.6 @Override
	public void init(){
		regexProvider.compile(getRegex(), Pattern.MULTILINE);
		
	    formatter = new MessageFormat("");
	    formatter.applyPattern(getReplacement());
	}
	
	public String getPatternKey() {
		return "filter.anchor";
	}
	@Override
	public void replace(StringBuffer buffer, MatchResult result, RenderContext context) {
		String anchorText = result.group(2);
		
		//validate
		//See our issue http://bug.edgenius.com/issues/34
		//and SUN Java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337993
		try {
			if(!VALID.matcher(anchorText).matches()){
				buffer.append(RenderUtil.renderError("Only letters, digits and spaces are allowed in anchor markup,",result.group(0)));
				return;
			}
		} catch (StackOverflowError e) {
			AuditLogger.error("StackOverflow Error in AnchorFilter.replace. Input[" 
					+ anchorText+"]  Pattern [" + VALID.pattern()+ "]");
			return;
		} catch (Throwable e) {
			AuditLogger.error("Unexpected error in AnchorFilter.replace. Input[" 
					+ anchorText+"]  Pattern [" + VALID.pattern()+ "]",e);
			return;
		}			
		
		buffer.append(formatter.format(new Object[]{result.group(1),anchorText,result.group(3)}));
		
	}
	
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> nodeIter, RenderContext context) {
		Map<String, String> atts = node.getAttributes();
		if(atts != null && (AnchorFilter.class.getName().equalsIgnoreCase(atts.get(NameConstants.AID))
				//this is special checking for Safari browser, it remove all other attributes if it has "name" attribute.
				||(atts.size() == 1 && atts.containsKey(NameConstants.NAME)))){
			String sep = getSeparatorFilter(node);
			node.reset(sep+getMarkupPrint()+atts.get(NameConstants.NAME)+getMarkupPrint()+sep,true);
			if(node.getPair() != null)
				node.getPair().reset("", true);
		}
	}
}
