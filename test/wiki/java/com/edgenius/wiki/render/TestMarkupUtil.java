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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Dapeng.Ni
 */
public class TestMarkupUtil  extends TestCase{
	@Test
	public void testEscapeMarkupToSlash(){
		String key = "uniqueK";
		Assert.assertEquals(key+"\\[test\\]"+key,MarkupUtil.escapeMarkupToSlash(key+"[test]"+key, key));
		
		Assert.assertEquals("ab"+key+"[test\\]"+key,MarkupUtil.escapeMarkupToSlash("ab"+key+"[test]"+key, key));
		
		Assert.assertEquals("ab$"+key+"\\[test\\]"+key,MarkupUtil.escapeMarkupToSlash("ab$"+key+"[test]"+key, key));
		
		Assert.assertEquals("ab$"+key+"\\[test]"+key+"cd",MarkupUtil.escapeMarkupToSlash("ab$"+key+"[test]"+key+"cd", key));
		
		Assert.assertEquals("ab$"+key+"\\[test\\]"+key+"$cd",MarkupUtil.escapeMarkupToSlash("ab$"+key+"[test]"+key+"$cd", key));
		
		Assert.assertEquals("ab$"+key+key+"\\[test\\]"+key+key+"$cd",MarkupUtil.escapeMarkupToSlash("ab$"+key+key+"[test]"+key+key+"$cd", key));
		
		Assert.assertEquals("ab"+key+"[test]"+key+"cd",MarkupUtil.escapeMarkupToSlash("ab"+key+"[test]"+key+"cd", key));
		
		Assert.assertEquals("ab"+key+key+"[test]"+key+"cd",MarkupUtil.escapeMarkupToSlash("ab"+key+key+"[test]"+key+"cd", key));
		
		Assert.assertEquals("ab"+key+key+"[test]"+key+key+"cd",MarkupUtil.escapeMarkupToSlash("ab"+key+key+"[test]"+key+key+"cd", key));
		
		Assert.assertEquals("ab"+key+"a"+ key+"[test]"+key+"a"+ key+"cd",MarkupUtil.escapeMarkupToSlash("ab"+key+"a"+ key+"[test]"+key+"a"+ key+"cd", key));
		
		Assert.assertEquals("ab"+key+"$"+ key+"\\[test\\]"+key+"$"+ key+"cd",MarkupUtil.escapeMarkupToSlash("ab"+key+"$"+ key+"[test]"+key+"$"+ key+"cd", key));
		
		Assert.assertEquals("ab"+key+key+"[test]"+key+key+"cd",MarkupUtil.escapeMarkupToSlash("ab"+key+key+"[test]"+key+key+"cd", null));
		
	}
	@Test
	public void testEscapeMarkupToEntity(){
		//end double
		Assert.assertEquals("ab&#92;",MarkupUtil.escapeMarkupToEntity("ab\\\\"));
		//end single
		Assert.assertEquals("ab&#92;",MarkupUtil.escapeMarkupToEntity("ab\\"));
		//end triple
		Assert.assertEquals("ab&#92;&#92;",MarkupUtil.escapeMarkupToEntity("ab\\\\\\"));
		//1 slash
		Assert.assertEquals("ab&#123;",MarkupUtil.escapeMarkupToEntity("ab\\{"));
		//1 slash
		Assert.assertEquals("ab&#123;ce",MarkupUtil.escapeMarkupToEntity("ab\\{ce"));
		//1 slash
		Assert.assertEquals("&#123;",MarkupUtil.escapeMarkupToEntity("\\{"));
		//2 slash
		Assert.assertEquals("ab&#92;{ce",MarkupUtil.escapeMarkupToEntity("ab\\\\{ce"));
		//2 slash
		Assert.assertEquals("&#92;{",MarkupUtil.escapeMarkupToEntity("\\\\{"));
		//3 slash
		Assert.assertEquals("&#92;&#123;",MarkupUtil.escapeMarkupToEntity("\\\\\\{"));
		//4 slash
		Assert.assertEquals("&#92;&#92;{",MarkupUtil.escapeMarkupToEntity("\\\\\\\\{"));
		//5 slash
		Assert.assertEquals("&#92;&#92;&#123;",MarkupUtil.escapeMarkupToEntity("\\\\\\\\\\{"));
		//6 slash
		Assert.assertEquals("ab&#92;&#92;&#92;{ce",MarkupUtil.escapeMarkupToEntity("ab\\\\\\\\\\\\{ce"));
		//6 slash
		Assert.assertEquals("&#92;&#92;&#92;{",MarkupUtil.escapeMarkupToEntity("\\\\\\\\\\\\{"));
	}
	@Test
	public void testHideEscapeMarkup(){
		String input = "ab\\{";
		CharSequence output = MarkupUtil.hideEscapeMarkup(input);
		Assert.assertEquals("abHH", output.toString());
		
		
		input = "ab\\{ce";
		output = MarkupUtil.hideEscapeMarkup(input);
		Assert.assertEquals("abHHce", output.toString());
		
		input = "\\{";
		output = MarkupUtil.hideEscapeMarkup(input);
		Assert.assertEquals("HH", output.toString());
		
		input = "ab\\\\{ce";
		output = MarkupUtil.hideEscapeMarkup(input);
		Assert.assertEquals("ab\\\\{ce", output.toString());
		
		//4 slash
		input = "\\\\\\\\{";
		output = MarkupUtil.hideEscapeMarkup(input);
		Assert.assertEquals("\\\\\\\\{", output.toString());
		
		//5 slash
		input = "\\\\\\\\\\{";
		output = MarkupUtil.hideEscapeMarkup(input);
		Assert.assertEquals("\\\\\\\\HH", output.toString());
		
		
		//with 2 escape
		input = "\\#abc\\{";
		output = MarkupUtil.hideEscapeMarkup(input);
		Assert.assertEquals("HHabcHH", output.toString());
		
		//2 escape, but first one does not escape valid characters
		input = "\\1abc\\{";
		output = MarkupUtil.hideEscapeMarkup(input);
		Assert.assertEquals("\\1abcHH", output.toString());
	}
}
