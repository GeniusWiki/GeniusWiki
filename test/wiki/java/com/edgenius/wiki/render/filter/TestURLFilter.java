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
package com.edgenius.wiki.render.filter;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.edgenius.wiki.render.impl.FilterPipeImpl;
import com.edgenius.wiki.render.impl.MacroManagerImpl;
import com.edgenius.wiki.render.impl.RenderContextImpl;

/**
 * @author Dapeng.Ni
 */
public class TestURLFilter extends TestCase{
	FilterPipeImpl pipe = new FilterPipeImpl();
	UrlFilter filter;
	
	@Before
	public void setUp() throws Exception {
		pipe.setMacroManager(new MacroManagerImpl());
		pipe.load();
		filter = (UrlFilter) pipe.getFilter(UrlFilter.class.getName());
	}
	@Test
	public void testUrl(){
		Assert.assertEquals(
				"<img aid=\"norender\" title=\"Open link in new window\" class=\"renderExtLinkImg\" src=\"/skins/default/render/link/extlink.png\"><a aid=\"com.edgenius.wiki.render.filter.UrlFilter\" href=\"http://www.foo.com\" target=\"_blank\">http://www.foo.com</a>"
				,filter.filter("http://www.foo.com", new RenderContextImpl()));
		
		Assert.assertEquals(
				"<img aid=\"norender\" title=\"Open link in new window\" class=\"renderExtLinkImg\" src=\"/skins/default/render/link/extlink.png\"><a aid=\"com.edgenius.wiki.render.filter.UrlFilter\" href=\"http://www.foo.com:9012\" target=\"_blank\">http://www.foo.com:9012</a>"
				,filter.filter("http://www.foo.com:9012", new RenderContextImpl()));
		
		Assert.assertEquals(
				"<img aid=\"norender\" title=\"Open link in new window\" class=\"renderExtLinkImg\" src=\"/skins/default/render/link/extlink.png\"><a aid=\"com.edgenius.wiki.render.filter.UrlFilter\" href=\"http://www.foo.com/test\" target=\"_blank\">http://www.foo.com/test</a>. and"
				,filter.filter("http://www.foo.com/test. and", new RenderContextImpl()));
		
		Assert.assertEquals(filter.filter("http://www.foo.com/test/. and", new RenderContextImpl()),
				"<img aid=\"norender\" title=\"Open link in new window\" class=\"renderExtLinkImg\" src=\"/skins/default/render/link/extlink.png\"><a aid=\"com.edgenius.wiki.render.filter.UrlFilter\" href=\"http://www.foo.com/test/\" target=\"_blank\">http://www.foo.com/test/</a>. and");
		
		Assert.assertEquals(filter.filter("http://www.foo.com/test?abc=123. and", new RenderContextImpl()),
				"<img aid=\"norender\" title=\"Open link in new window\" class=\"renderExtLinkImg\" src=\"/skins/default/render/link/extlink.png\"><a aid=\"com.edgenius.wiki.render.filter.UrlFilter\" href=\"http://www.foo.com/test?abc=123\" target=\"_blank\">http://www.foo.com/test?abc=123</a>. and");
		
		Assert.assertEquals(filter.filter("http://www.foo.com/test#abc=123. and", new RenderContextImpl()),
				"<img aid=\"norender\" title=\"Open link in new window\" class=\"renderExtLinkImg\" src=\"/skins/default/render/link/extlink.png\"><a aid=\"com.edgenius.wiki.render.filter.UrlFilter\" href=\"http://www.foo.com/test#abc=123\" target=\"_blank\">http://www.foo.com/test#abc=123</a>. and");
		
		Assert.assertEquals(filter.filter("http://www.foo.com/test?test=123&bb=ab#abc=123. and", new RenderContextImpl()),
				"<img aid=\"norender\" title=\"Open link in new window\" class=\"renderExtLinkImg\" src=\"/skins/default/render/link/extlink.png\"><a aid=\"com.edgenius.wiki.render.filter.UrlFilter\" href=\"http://www.foo.com/test?test=123&bb=ab#abc=123\" target=\"_blank\">http://www.foo.com/test?test=123&bb=ab#abc=123</a>. and");
		
		Assert.assertEquals(filter.filter("http://www.foo.com/test#/$CP/test/hello#123. and", new RenderContextImpl()),
		"<img aid=\"norender\" title=\"Open link in new window\" class=\"renderExtLinkImg\" src=\"/skins/default/render/link/extlink.png\"><a aid=\"com.edgenius.wiki.render.filter.UrlFilter\" href=\"http://www.foo.com/test#/$CP/test/hello#123\" target=\"_blank\">http://www.foo.com/test#/$CP/test/hello#123</a>. and");
		
		
		//<> will cause problem if next string is HTML tag.
//		Assert.assertEquals(filter.filter("http://foo.com?abc=test<and>&hello=koo. and", new RenderContextImpl()),
//		"<a aid=\"com.edgenius.wiki.render.filter.UrlFilter\" href=\"http://foo.com?abc=test<and>&hello=koo\" target=\"_blank\">http://foo.com?abc=test<and>&hello=koo</a><img aid=\"norender\" src=\"/skins/default/render/link/extlink.png\" title=\"Open link in new window\" class=\"renderExtLinkImg\">. and");
		
		Assert.assertEquals(filter.filter("http://localhost", new RenderContextImpl()),
		"<img aid=\"norender\" title=\"Open link in new window\" class=\"renderExtLinkImg\" src=\"/skins/default/render/link/extlink.png\"><a aid=\"com.edgenius.wiki.render.filter.UrlFilter\" href=\"http://localhost\" target=\"_blank\">http://localhost</a>");

		Assert.assertEquals(filter.filter("http://localhost:8080!?.", new RenderContextImpl()),
		"<img aid=\"norender\" title=\"Open link in new window\" class=\"renderExtLinkImg\" src=\"/skins/default/render/link/extlink.png\"><a aid=\"com.edgenius.wiki.render.filter.UrlFilter\" href=\"http://localhost:8080\" target=\"_blank\">http://localhost:8080</a>!?.");

		Assert.assertEquals(filter.filter("http://abc.test.localhost.com:8080. and", new RenderContextImpl()),
			"<img aid=\"norender\" title=\"Open link in new window\" class=\"renderExtLinkImg\" src=\"/skins/default/render/link/extlink.png\"><a aid=\"com.edgenius.wiki.render.filter.UrlFilter\" href=\"http://abc.test.localhost.com:8080\" target=\"_blank\">http://abc.test.localhost.com:8080</a>. and");

	}
}
