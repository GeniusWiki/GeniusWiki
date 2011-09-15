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
package com.edgenius.wiki.render.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.render.RegexProvider;
import com.edgenius.wiki.render.TokenVisitor;

/**
 * @author Dapeng.Ni
 */
public class JdkRegexProvider implements RegexProvider<Matcher>{
	private Pattern pattern;
	
	public void compile(String regex, int patternFlag) {
		pattern = Pattern.compile(regex, patternFlag);
	}
	
	public String replaceAll(CharSequence input, String replacement) {
		
		
//		long start = System.currentTimeMillis();
		
		//See our issue http://bug.edgenius.com/issues/34
		//and SUN Java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337993
		try {
			Matcher matcher = pattern.matcher(input);
			String output = matcher.replaceAll(replacement);
			return output;
		} catch (StackOverflowError e) {
			AuditLogger.error("StackOverflow Error in JdkRegexProvider.replaceAll. Input[" + input+"] Pattern [" 
					+ pattern.pattern()+ "] Replacement["+replacement+"]");
		} catch (Throwable e) {
			AuditLogger.error("Unexpected error in JdkRegexProvider.replaceAll. Input[" + input+"] Pattern [" 
					+ pattern.pattern()+ "]  Replacement["+replacement+"]",e);
		}
		
//		System.out.println("Time consume:" + (System.currentTimeMillis() - start));
		
		//this is failure tolerance - don't do any replacement!
		return input.toString();
		
	}

	public String replaceByTokenVisitor(CharSequence  input, TokenVisitor<Matcher> visitor) {
		//See our issue http://bug.edgenius.com/issues/34
		//and SUN Java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337993
		try {
			Matcher matcher = pattern.matcher(input);
	
		    StringBuffer buffer = new StringBuffer();
		    while (matcher.find()) {
		       matcher.appendReplacement(buffer, "");
		       visitor.handleMatch(buffer, matcher);
		    }
		    matcher.appendTail(buffer);
		    return buffer.toString();
		    
		} catch (StackOverflowError e) {
			AuditLogger.error("StackOverflow Error in JdkRegexProvider.replaceByTokenVisitor. Input[" 
					+ input+"]  Pattern [" + pattern.pattern()+ "]");
		} catch (Throwable e) {
			AuditLogger.error("Unexpected error in JdkRegexProvider.replaceByTokenVisitor. Input[" 
					+ input+"]  Pattern [" + pattern.pattern()+ "]",e);
		}
		
		
		//this is failure tolerance - don't do any replacement!
		return input.toString();
	}

	
}
