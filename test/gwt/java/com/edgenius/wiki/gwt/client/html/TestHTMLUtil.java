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

import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.edgenius.test.TestUtil;
import com.edgenius.wiki.gwt.client.model.LinkModel;

/**
 * @author Dapeng.Ni
 */
public class TestHTMLUtil extends TestCase{
	LinkModel model = new LinkModel();
	String rs = "<a wajax=\"aname:com.edgenius.wiki.gwt.client.model.LinkModel|spaceuname:my space \\&38; + = some |type:1|anchor:hello\\&34;anchor\"" +
			" href=\"#\">viewabc</a>";

	@Before
	public void setUp() throws Exception {
		model.setView("viewabc");
		model.setAnchor("hello\"anchor");
		model.setLink(null);
		model.setSpaceUname("my space & + = some ");
		model.setType(1);
	}
	@Test
	public void testGetTagName(){
		Assert.assertEquals(HTMLUtil.getTagName("<a>"),"a");
		Assert.assertEquals(HTMLUtil.getTagName("</a>"),"a");
		Assert.assertEquals(HTMLUtil.getTagName("<a/>"),"a");
		Assert.assertEquals(HTMLUtil.getTagName("<a1/>"),"a1");
		Assert.assertEquals(HTMLUtil.getTagName("<a href='abc'>"),"a");
		Assert.assertEquals(HTMLUtil.getTagName("<*a href='abc'>"),"*a");
		Assert.assertEquals(HTMLUtil.getTagName("<*>"),"*");
		Assert.assertNull(HTMLUtil.getTagName("< a >"));
		Assert.assertNull(HTMLUtil.getTagName("<_a >"));
		
	}
	@Test
	public void testParseAttribute(){
		Map<String, String> map = HTMLUtil.parseAttributes("<a a1='v1' a2=\"v2\"/>");
		Assert.assertEquals("v1",map.get("a1"));
		Assert.assertEquals("v2",map.get("a2"));
		
		map = HTMLUtil.parseAttributes("<a a1= v1  a2=\"v2\"/>");
		Assert.assertEquals("v1",map.get("a1"));
		Assert.assertEquals("v2",map.get("a2"));
		
		map = HTMLUtil.parseAttributes("<a a1 = v1  a2=v2/>");
		Assert.assertEquals("v1",map.get("a1"));
		Assert.assertEquals("v2",map.get("a2"));
		
		map = HTMLUtil.parseAttributes("<a a1 = v1  a2=v2\"/>");
		Assert.assertEquals("v1",map.get("a1"));
		Assert.assertEquals("v2\"",map.get("a2"));
		
		map = HTMLUtil.parseAttributes("<a a1 =\" v1 \"  a2=' v2 '>");
		Assert.assertEquals("v1",map.get("a1"));
		Assert.assertEquals("v2",map.get("a2"));
		
		map = HTMLUtil.parseAttributes("<a>");
		Assert.assertNull(map);
		
		
	}
	@Test
	public void testParseStyle(){
		Map<String, String> map = HTMLUtil.parseStyle("text-align : left; font:red");
		Assert.assertEquals("left",map.get("text-align"));
		Assert.assertEquals("red",map.get("font"));
		
		map = HTMLUtil.parseStyle(" font:red ");
		Assert.assertEquals("red",map.get("font"));
		
		
		
	}
	@Test
	public void testToTagString(){
		Assert.assertTrue(TestUtil.equalsHMTL(rs,model.toRichAjaxTag()));
	}
	@Test
	public void testToObject(){
		LinkModel rsObj = new LinkModel();
		rsObj.fillToObject(rs,"viewabc");
		
		Assert.assertEquals(rsObj.getAnchor(),model.getAnchor());
		Assert.assertEquals(rsObj.getLink(),model.getLink());
		Assert.assertEquals(rsObj.getSpaceUname(),model.getSpaceUname());
		Assert.assertEquals(rsObj.getType(),model.getType());
		Assert.assertEquals(rsObj.getView(),model.getView());
	}
	
}
