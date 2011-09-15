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
package com.edgenius.wiki.gwt.client.html;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Dapeng.Ni
 */
public class TestHTMLNode extends TestCase{

	@Test
	public void testContains(){
		HTMLNode container = new HTMLNode("<a href='link' style='font:red'/>",false);
		HTMLNode node = new HTMLNode("<a href='link'>",false);
		Assert.assertTrue(container.contains(node));
		
		node = new HTMLNode("<a href='link' style='font:red'>",false);
		Assert.assertTrue(container.contains(node));
		
		node = new HTMLNode("<* href='link' style='font:red'>",false);
		Assert.assertTrue(container.contains(node));
		
		node = new HTMLNode("<* href=' link ' style=' font : red; '>",false);
		Assert.assertTrue(container.contains(node));
		
		node = new HTMLNode("<* style='font:*'>",false);
		Assert.assertTrue(container.contains(node));
		
		node = new HTMLNode("<* style='font:blue'>",false);
		Assert.assertFalse(container.contains(node));
		
		node = new HTMLNode("<a href='link1'>",false);
		Assert.assertFalse(container.contains(node));
		
		node = new HTMLNode("<a style='font:red; bk:blue'>",false);
		Assert.assertFalse(container.contains(node));
		
		node = new HTMLNode("<a style='font:blue; bk:red'>",false);
		Assert.assertFalse(container.contains(node));
	}
	

}
