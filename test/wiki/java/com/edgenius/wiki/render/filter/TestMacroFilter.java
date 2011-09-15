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

import com.edgenius.wiki.render.MacroManager;
import com.edgenius.wiki.render.impl.MacroManagerImpl;

/**
 * This unit test need /META-INF/services/+Macro.class.getName() file exists.
 * @author Dapeng.Ni
 */
public class TestMacroFilter extends TestCase{
	MacroFilter macro  = new MacroFilter();
	MacroManager mgrManager = new MacroManagerImpl();
	@Before
	public void setUp() throws Exception {
		mgrManager.load();
		macro.setMacroMgr(mgrManager);
		macro.init();
	}
	
	@Test
	public void testGroup(){
		String text = "abc {table}{cell}hello{cell}{rowdiv}{cell}hello{cell}{table}123";
		String out = "abc {table:edgnius_group_key=4}{cell:edgnius_group_key=4-0-0-1}hello{cell}{rowdiv:edgnius_group_key=4-1}{cell:edgnius_group_key=4-1-0-1}hello{cell}{table}123";
		Assert.assertEquals(out, macro.initialGroup(text));;
		
		
		text = "{table}{cell}hello{cell}{cell}hello{cell}{cell}hello{cell}{rowdiv}{cell}hello{cell}{rowdiv}{cell}hello{cell}{cell}hello{cell}{cell}hello{cell}{table}123";
		out ="{table:edgnius_group_key=0}{cell:edgnius_group_key=0-0-0-3}hello{cell}{cell:edgnius_group_key=0-0-1-3}hello{cell}{cell:edgnius_group_key=0-0-2-3}hello{cell}{rowdiv:edgnius_group_key=0-1}{cell:edgnius_group_key=0-1-0-1}hello{cell}{rowdiv:edgnius_group_key=0-2}{cell:edgnius_group_key=0-2-0-3}hello{cell}{cell:edgnius_group_key=0-2-1-3}hello{cell}{cell:edgnius_group_key=0-2-2-3}hello{cell}{table}123";
		Assert.assertEquals(out, macro.initialGroup(text));;
		
		text = "{table}{cell}hello{cell}{rowdiv}{cell}hello{cell}{table}123 {table}{cell}hello{cell}{rowdiv}{cell}hello{cell}{table}123";
		out="{table:edgnius_group_key=0}{cell:edgnius_group_key=0-0-0-1}hello{cell}{rowdiv:edgnius_group_key=0-1}{cell:edgnius_group_key=0-1-0-1}hello{cell}{table}123 {table:edgnius_group_key=60}{cell:edgnius_group_key=60-0-0-1}hello{cell}{rowdiv:edgnius_group_key=60-1}{cell:edgnius_group_key=60-1-0-1}hello{cell}{table}123";
		Assert.assertEquals(out, macro.initialGroup(text));;
		
		text = "{table:hasTitle=yes}{cell}hello{cell}{rowdiv}{cell}hello{cell}{table}123";
		out = "{table:edgnius_group_key=0|hasTitle=yes}{cell:edgnius_group_key=0-0-0-1}hello{cell}{rowdiv:edgnius_group_key=0-1}{cell:edgnius_group_key=0-1-0-1}hello{cell}{table}123";
		Assert.assertEquals(out, macro.initialGroup(text));;
		

	}
}
