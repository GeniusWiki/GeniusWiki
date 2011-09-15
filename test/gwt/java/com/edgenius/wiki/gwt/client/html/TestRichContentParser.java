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

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.edgenius.test.TestItem;
import com.edgenius.test.TestMain;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;

/**
 * @author Dapeng.Ni
 */
public class TestRichContentParser extends TestMain{

	@Test
	public void testParser() throws IOException{
		List<TestItem> cases = readTestcaseFile("RichContentParser");
		
		TestItem testItem = cases.get(0);
			
		List<RenderPiece> list = RenderPieceParser.parse(testItem.expected,true);
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("bbc<p> </p><h1>a</h1><span aid=\"com.edgenius.wiki.render.filter.NewlineFilter\"></span><h1>",list.get(0).toString());
		
		Assert.assertTrue(list.get(1) instanceof LinkModel);
		
		LinkModel ln = (LinkModel) list.get(1);
		Assert.assertEquals("test",ln.getView());
		Assert.assertEquals("view",ln.getLink());
		
		Assert.assertEquals(" sdf wy</h1><span aid=\"com.edgenius.wiki.render.filter.NewlineFilter\"></span>abababab<p><br />bbb<br />aa<br />sdfsdf</p>",list.get(2).toString());
			
			
	}
}
