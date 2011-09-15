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
package com.edgenius.wiki.render;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Dapeng.Ni
 */
public class TestPattern extends TestCase{
	private String filterResourcePattern = "META-INF/services/" + Filter.class.getName()+".properties";
	private Properties patternResource = new Properties();
	private Pattern listPattern;
	@Before
	public void setUp() throws IOException{
		ClassLoader classLoader = this.getClass().getClassLoader();
		patternResource.load(classLoader.getResourceAsStream(filterResourcePattern));
		String pattern = patternResource.getProperty("filter.list.match");
		listPattern = Pattern.compile(pattern,Pattern.MULTILINE|Pattern.DOTALL);
//		listPattern = Pattern.compile("(^[ \t]*([-#*]+|[-#*]*[iIaA1gkKj]+\\.)[ \t]+(.+?)([\r\n]+|$))+",Pattern.MULTILINE);
	}
	
	@Test
	public void testListPattern(){
		String input = "* abc \n* def\n123Newline";
		Matcher m = listPattern.matcher(input);
		Assert.assertTrue(m.find());
		Assert.assertEquals("* abc \n* def\n123Newline", m.group(0));
		
		input = "* abc \n* def\n123Newline \n new line";
		m = listPattern.matcher(input);
		Assert.assertTrue(m.find());
		Assert.assertEquals("* abc \n* def\n123Newline \n new line", m.group(0));
		
		input = "* abc \n* def";
		m = listPattern.matcher(input);
		Assert.assertTrue(m.find());
		Assert.assertEquals("* abc \n* def", m.group(0));
		
		input = "* abc \n* def \n\n* 123";
		m = listPattern.matcher(input);
		Assert.assertTrue(m.find());
		Assert.assertEquals("* abc \n* def \n\n", m.group(0));
		
		input = "* abc \n* def \n\n* 123";
		m = listPattern.matcher(input);
		Assert.assertTrue(m.find());
		Assert.assertEquals("* abc \n* def \n\n", m.group(0));
		
		input = "* abc \n* def \n\nnew line\n* 123";
		m = listPattern.matcher(input);
		Assert.assertTrue(m.find());
		Assert.assertEquals("* abc \n* def \n\n", m.group(0));
		
		input = "\n\n* abc \n* def \n\nnew line\n* 123";
		m = listPattern.matcher(input);
		Assert.assertTrue(m.find());
		Assert.assertEquals("* abc \n* def \n\n", m.group(0));
		
		input = "\n\n* \nabc";
		m = listPattern.matcher(input);
		Assert.assertTrue(m.find());
	}
}
