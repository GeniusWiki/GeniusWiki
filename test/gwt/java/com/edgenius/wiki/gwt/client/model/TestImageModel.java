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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

import com.edgenius.wiki.gwt.client.html.ImageModel;

/**
 * @author Dapeng.Ni
 */
public class TestImageModel extends TestCase{

	@Test
	public void testToRichAjaxTag(){
		ImageModel img = new ImageModel();
		img.filename = "adsf + & $.jpg";
		Assert.assertEquals("<img wajax=\"aname:com.edgenius.wiki.gwt.client.html.ImageModel|filename:adsf + \\&#38; $.jpg\">",img.toRichAjaxTag());
	}
	@Test
	public void testFillToObject(){
		ImageModel img = new ImageModel();
		img.fillToObject("<img wajax=\"aname:com.edgenius.wiki.gwt.client.html.ImageModel|filename:adsf + \\&#38; $.jpg\">",null);
		
		Assert.assertEquals("adsf + & $.jpg",img.filename);
		
	}
}
