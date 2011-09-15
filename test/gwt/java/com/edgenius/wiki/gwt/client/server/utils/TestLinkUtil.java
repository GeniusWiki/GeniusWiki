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
package com.edgenius.wiki.gwt.client.server.utils;

import junit.framework.TestCase;

import org.junit.Test;

import com.edgenius.wiki.gwt.client.model.LinkModel;

/**
 * @author Dapeng.Ni
 */
public class TestLinkUtil extends TestCase{

	@Test
	public void testParseMarkup(){
		String t1 = "link#anchor@space";
		LinkModel link = LinkUtil.parseMarkup(t1);
		assertEquals("link", link.getLink());
		assertEquals("space", link.getSpaceUname());
		assertEquals("anchor", link.getAnchor());
		
		
		String t2 = "link#anchor";
		link = LinkUtil.parseMarkup(t2);
		assertEquals("link", link.getLink());
		assertNull(link.getSpaceUname());
		assertEquals("anchor", link.getAnchor());
		
		String t3 = "link";
		link = LinkUtil.parseMarkup(t3);
		assertEquals("link", link.getLink());
		assertNull(link.getSpaceUname());
		assertNull(link.getAnchor());
		
		
	}
	
}
