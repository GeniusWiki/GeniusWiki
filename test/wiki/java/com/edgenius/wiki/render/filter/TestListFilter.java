/* 
 * =============================================================
 * Copyright (C) 2007-2008 Edgenius (http://edgenius.com)
 * =============================================================
 * Edgenius, Confidential and Proprietary
 * License Information: http://edgenius.com/licensing/edgenius/1.0/
 *
 * This computer program contains valuable, confidential and proprietary
 * information.  Disclosure, use, or reproduction without the written
 * authorization of Edgenius is prohibited.  This unpublished
 * work by Edgenius is protected by the laws of the United States
 * and other countries.  If publication of the computer program should occur,
 * the following notice shall apply:
 *  
 * Copyright (c) 2007-2008 Edgenius.  All rights reserved.                                                              
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
public class TestListFilter extends TestCase{
	FilterPipeImpl pipe = new FilterPipeImpl();
	ListFilter filter;
	
	@Before
	public void setUp() throws Exception {
		pipe.setMacroManager(new MacroManagerImpl());
		pipe.load();
		filter = (ListFilter) pipe.getFilter(ListFilter.class.getName());
	}
	@Test
	public void testFilter(){
		Assert.assertEquals("<ul class=\"star\"><li>list1</li></ul>\n\nabc", 
				filter.filter("* list1\n\nabc", new RenderContextImpl()));
		Assert.assertEquals("<ul class=\"star\"><li>list1</li></ul>\n   \nabc", 
				filter.filter("* list1\n   \nabc", new RenderContextImpl()));
		Assert.assertEquals("<ul class=\"star\"><li>list1</li></ul>", 
				filter.filter("* list1", new RenderContextImpl()));
		Assert.assertEquals("<ul class=\"star\"><li>list1</li></ul>\n", 
				filter.filter("* list1\n", new RenderContextImpl()));
		Assert.assertEquals("<ul class=\"star\"><li>list1</li></ul>\n  \n", 
				filter.filter("* list1\n  \n", new RenderContextImpl()));
		
	}

}
