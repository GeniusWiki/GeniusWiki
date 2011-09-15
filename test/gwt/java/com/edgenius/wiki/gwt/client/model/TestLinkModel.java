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
package com.edgenius.wiki.gwt.client.model;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Dapeng.Ni
 */
public class TestLinkModel extends TestCase{
	@Test
	public void testToRichAjaxTag(){
		LinkModel link = new LinkModel();
		link.setLink("my link");
		link.setAnchor("anchor");
		link.setView("my view");
		
		System.out.println(link.toRichAjaxTag());
	}

}
