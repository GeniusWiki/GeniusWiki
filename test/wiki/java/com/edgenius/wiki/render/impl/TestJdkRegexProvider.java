/* 
 * =============================================================
 * Copyright (C) 2007-2010 Edgenius (http://www.edgenius.com)
 * =============================================================
 * Edgenius, Confidential and Proprietary
 * License Information: http://www.edgenius.com/licensing/edgenius/1.0/
 *
 * This computer program contains valuable, confidential and proprietary
 * information.  Disclosure, use, or reproduction without the written
 * authorization of Edgenius is prohibited.  This unpublished
 * work by Edgenius is protected by the laws of the United States
 * and other countries.  If publication of the computer program should occur,
 * the following notice shall apply:
 *  
 * Copyright (C) 2007-2010 Edgenius.  All rights reserved.                                                              
 * ****************************************************************
 */
package com.edgenius.wiki.render.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.junit.Test;

import com.edgenius.wiki.render.FilterRegxConstants;
import com.edgenius.wiki.render.Region;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.filter.MacroFilter;

/**
 * @author Dapeng.Ni
 */
public class TestJdkRegexProvider extends TestCase {

	@Test
	public void testReplaceAll(){
		String regTxt = FilterRegxConstants.PATTERN_NORMAL_SURROUNDING.replaceAll(FilterRegxConstants.PATTERN_REP_TOKEN, "-");
		JdkRegexProvider regx = new JdkRegexProvider();
		regx.compile(regTxt, Pattern.DOTALL);
		MacroFilter mf = new MacroFilter();
		MacroManagerImpl macroMgr = new MacroManagerImpl();
		mf.setMacroMgr(macroMgr);
		mf.init();
		
		String replacement = "$1<span style=\"text-decoration: line-through;\">$2</span>$3";
		StringBuilder input = new StringBuilder("{pre} -hello{pre} and- world. -text-");
		RenderContext context = new RenderContextImpl();
		List<Region> regions = new ArrayList<Region>();
		((RenderContextImpl)context).setRegions(regions);
		
		regions = mf.getRegions(input);
		String output = regx.replaceAll(input, replacement);
		
		regions.clear();
	
		input = new StringBuilder( "-abc-{pre} -hello{pre} and- world. -text-");
		regions = mf.getRegions(input);
		((RenderContextImpl)context).setRegions(regions);
		
		output = regx.replaceAll(input, replacement);
		
		System.out.println("out:"+output);
	}

	
}
