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

import java.util.ListIterator;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Dapeng.Ni
 */
public class TestHTMLNodeContainer extends TestCase{
	
	@Test
	public void testIteratorAdd(){
		HTMLNode h1 = new HTMLNode("1",true);
		HTMLNode h2 = new HTMLNode("2",true);
		HTMLNode h3 = new HTMLNode("3",true);
		HTMLNode h11 = new HTMLNode("11",true);
		HTMLNode h21 = new HTMLNode("21",true);
		
		
		HTMLNodeContainer con = new HTMLNodeContainer();
		con.add(h1);
		con.add(h2);
		con.add(h3);
		
		ListIterator<HTMLNode> iter = con.listIterator();
		Assert.assertEquals("1", iter.next().getText());
		iter.add(h11);
		Assert.assertEquals("2", iter.next().getText());
		iter.add(h21);
		Assert.assertEquals("3", iter.next().getText());
		
		Assert.assertEquals("1", h11.previous().getText());
		Assert.assertEquals("2", h11.next().getText());
		
		Assert.assertEquals("11", h1.next().getText());
		Assert.assertEquals("11", h2.previous().getText());
		Assert.assertEquals("21", h2.next().getText());
		Assert.assertEquals("21", h3.previous().getText());
		
		iter = con.listIterator();
		Assert.assertEquals("1", iter.next().getText());
		Assert.assertEquals("11", iter.next().getText());
		Assert.assertEquals("2", iter.next().getText());
		Assert.assertEquals("21", iter.next().getText());
		Assert.assertEquals("3", iter.next().getText());
		
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//test inertia -> previous -> next, return current cursor! 
		iter = con.listIterator();
		iter.next(); //1
		iter.next(); //11
		iter.next(); //2
		
		Assert.assertEquals("2",iter.previous().getText());
		Assert.assertEquals("11",iter.previous().getText());
		Assert.assertEquals("11",iter.next().getText());
		Assert.assertEquals("11",iter.previous().getText());
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// test multiple iterator.add()
		//prepare
		con = new HTMLNodeContainer();
		con.add(h1);
		con.add(h2);
		con.add(h3);
		
		iter = con.listIterator();
		iter.next(); //1
		iter.add(h11);
		iter.add(h21);
		//test
		iter = con.listIterator();
		Assert.assertEquals("1", iter.next().getText());
		Assert.assertEquals("11", iter.next().getText());
		Assert.assertEquals("21", iter.next().getText());
		Assert.assertEquals("2", iter.next().getText());
		Assert.assertEquals("3", iter.next().getText());
		
		iter = con.listIterator();
		HTMLNode node = iter.next();
		node = node.next();
		Assert.assertEquals("11", node.getText());
		node = node.next();
		Assert.assertEquals("21", node.getText());
		
		con = new HTMLNodeContainer();
		con.add(h1);
		con.add(h2);
		con.add(h3);
		
		iter = con.listIterator();
		iter.next(); //1
		iter.next(); //2
		iter.previous(); //2
		iter.next(); //2
		
		iter.add(h11);
		
		System.out.println(con);
		
		
		
	}

}
