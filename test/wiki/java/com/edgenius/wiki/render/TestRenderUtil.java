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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Dapeng.Ni
 */
public class TestRenderUtil extends TestCase{

	@Test
	public void testIsEndByBlockHTML(){
		Assert.assertFalse(RenderUtil.isEndByBlockHtmlTag(new StringBuffer("abc<b>")));
		Assert.assertFalse(RenderUtil.isEndByBlockHtmlTag(new StringBuffer("abc</b>")));
		Assert.assertFalse(RenderUtil.isEndByBlockHtmlTag(new StringBuffer("abc<div> some text")));
		Assert.assertFalse(RenderUtil.isEndByBlockHtmlTag(new StringBuffer("abc some text")));
		Assert.assertFalse(RenderUtil.isEndByBlockHtmlTag(new StringBuffer("abc <div style='display:inline'>some text</div>")));
		Assert.assertFalse(RenderUtil.isEndByBlockHtmlTag(new StringBuffer("abc <div  style='display:inline'>some text</div> \t ")));
		//no paired open tag
		Assert.assertFalse(RenderUtil.isEndByBlockHtmlTag(new StringBuffer("abc some text</div>")));
		
		Assert.assertTrue(RenderUtil.isEndByBlockHtmlTag(new StringBuffer("abc some text<div>")));
		Assert.assertTrue(RenderUtil.isEndByBlockHtmlTag(new StringBuffer("abc <div>some text</div>")));
		Assert.assertTrue(RenderUtil.isEndByBlockHtmlTag(new StringBuffer("abc <div>some text</div> \t ")));
		Assert.assertTrue(RenderUtil.isEndByBlockHtmlTag(new StringBuffer("abc <div>some text</div>  ")));
		Assert.assertTrue(RenderUtil.isEndByBlockHtmlTag(new StringBuffer("abc <div></div>  ")));
	}
	
	@Test
	public void testCreateRegionBorder(){
		String key ="WWW";
		String text = "0123456789012345";
		List<Region> regions = new ArrayList<Region>();
		regions.add(new Region(2,6));
		regions.add(new Region(4,6));
		regions.add(new Region(6,9));
		regions.add(new Region(10,12));
		regions.add(new Region(12,15));
		
		CharSequence out = RenderUtil.createRegionBorder(text, key, regions ,"ab\nab");
		assertEquals("01WWW1S23WWW2S45WWW2EWWW1EWWW3S678WWW3E9WWW4S01WWW4EWWW5S234WWW5E5",out.toString());
		
		regions = new ArrayList<Region>();
		regions.add(new Region(12,15));
		regions.add(new Region(4,6));
		regions.add(new Region(10,12));
		regions.add(new Region(6,9));
		regions.add(new Region(2,6));
		
		out = RenderUtil.createRegionBorder(text, key, regions ,"ab\nab");
		assertEquals("01WWW5S23WWW2S45WWW2EWWW5EWWW4S678WWW4E9WWW3S01WWW3EWWW1S234WWW1E5",out.toString());
		
//		assertEquals(text,RenderUtil.removeBorders(out, key));
	}
	@Test
	public void testCreateRegionBorder2(){
		String key ="WWW";
		String text = "0123456789012345";
		List<Region> regions = new ArrayList<Region>();
		regions.add(new Region(2,15));
		regions.add(new Region(5,15));
		
		CharSequence out = RenderUtil.createRegionBorder(text, key, regions ,"ab\nab");
		assertEquals("01WWW1S234WWW2S5678901234WWW2EWWW1E5",out.toString());
		
		regions = new ArrayList<Region>();
		regions.add(new Region(5,15));
		regions.add(new Region(2,15));
		
		out = RenderUtil.createRegionBorder(text, key, regions ,"ab\nab");
		assertEquals("01WWW2S234WWW1S5678901234WWW1EWWW2E5",out.toString());
		
//		assertEquals(text,RenderUtil.removeBorders(out, key));
	}
	@Test
	public void testCreateRegionBorder3(){
		String key ="WWW";
		String text = "0123456789012345";
		List<Region> regions = new ArrayList<Region>();
		regions.add(new Region(3,10));
		regions.add(new Region(3,12));
		
		CharSequence out = RenderUtil.createRegionBorder(text, key, regions ,"ab\nab");
		assertEquals("012WWW2SWWW1S3456789WWW1E01WWW2E2345",out.toString());
		
		regions = new ArrayList<Region>();
		regions.add(new Region(3,12));
		regions.add(new Region(3,10));
		
		out = RenderUtil.createRegionBorder(text, key, regions ,"ab\nab");
		assertEquals("012WWW1SWWW2S3456789WWW2E01WWW1E2345",out.toString());
		
//		assertEquals(text,RenderUtil.removeBorders(out, key));
	}
	@Test
	public void testCreateRegionBorder4(){
		String key ="WWW";
		String text = "0123456789012345";
		List<Region> regions = new ArrayList<Region>();
		regions.add(new Region(3,10));
		regions.add(new Region(3,10));
		regions.add(new Region(3,10));
		regions.add(new Region(3,10));
		regions.add(new Region(3,10));
		regions.add(new Region(3,10));
		CharSequence out = RenderUtil.createRegionBorder(text, key, regions ,"ab\nab");
		assertEquals("012WWW6SWWW5SWWW4SWWW3SWWW2SWWW1S3456789WWW1EWWW2EWWW3EWWW4EWWW5EWWW6E012345",out.toString());
		
	}
	
	@Test
	public void testHasBlogMacro(){
		String t1 = "abc{blog}xzy";
		assertTrue(RenderUtil.hasBlogMacro(t1));
		
		//double
		String t2 = "abc \\\\{blog} xzy";
		assertTrue(RenderUtil.hasBlogMacro(t2));
		
		//triple
		String t3 = "abc \\\\\\{blog} xzy";
		assertFalse(RenderUtil.hasBlogMacro(t3));
		
		//with some parameters
		String t4 = "abc {blog:test=abc} xzy";
		assertTrue(RenderUtil.hasBlogMacro(t4));
		
		//with leading space
		String t5 = "abc { blog:test=abc} xzy";
		assertFalse(RenderUtil.hasBlogMacro(t5));
	}
	
	@Test
	public void testGetPiece(){
		String t1 = "out1{piece:name=p1}content{piece}123";
		assertEquals("content",RenderUtil.getPiece(t1, "p1"));
		
		String t2 = "\\{piece:name=p1} out1{piece:name=p1}content{piece}123";
		assertEquals("content",RenderUtil.getPiece(t2, "p1"));
		
		//although first is valid (even leading slash) but it has not end markup
		String t3 = "\\\\{piece:name=p1} out1{piece:name=p1}content{piece}123";
		assertEquals("content",RenderUtil.getPiece(t3, "p1"));
		
		//ok, it has valid end markup now
		String t31 = "\\\\{piece:name=p1} out1{piece}{piece:name=p1}content{piece}123";
		assertEquals(" out1",RenderUtil.getPiece(t31, "p1"));
		
		//contain a invalid piece with odd leading slash
		String t4 = "\\\\{piece:name=p1} out1\\{piece:name=p1}content{piece}123";
		assertEquals(" out1\\{piece:name=p1}content",RenderUtil.getPiece(t4, "p1"));
		
		//different name pieces
		String t5 = "{piece:name=p1} out1{piece}{piece:name=p2}content{piece}123";
		assertEquals(" out1",RenderUtil.getPiece(t5, "p1"));
		assertEquals("content",RenderUtil.getPiece(t5, "p2"));
		
		//get first if duplicated piece name
		String t6 = "{piece:name=p1} out1{piece}{piece:name=p1}content{piece}123";
		assertEquals(" out1",RenderUtil.getPiece(t6, "p1"));
	}
}
