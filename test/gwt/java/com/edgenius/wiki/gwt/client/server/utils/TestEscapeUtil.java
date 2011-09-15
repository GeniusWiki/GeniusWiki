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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Dapeng.Ni
 */
public class TestEscapeUtil extends TestCase{

	@Test
	public void testUnescapeHTML(){
		Assert.assertEquals("&and&\"", EscapeUtil.unescapeHTML("&#38;and&amp;&quot;"));
		Assert.assertEquals("#ab&and&", EscapeUtil.unescapeHTML("#ab&#38;and&amp;"));
	}
	@Test
	public void testEscapeBySlash(){
		Assert.assertEquals("\\\\\\'", EscapeUtil.escapeBySlash("\\'", new char[]{'\\','\'','"'}));
		Assert.assertEquals("\\\\\\'\\\\\\'", EscapeUtil.escapeBySlash("\\'\\'", new char[]{'\\','\'','"'}));
		Assert.assertEquals("\\'\\'", EscapeUtil.removeSlashEscape(EscapeUtil.escapeBySlash("\\'\\'", new char[]{'\\','\'','"'}),new char[]{'\\','\'','"'}));
		
	}
	@Test
	public void testRemoveSlashEscape(){
		Assert.assertEquals("\\'", EscapeUtil.removeSlashEscape("\\\\'", new char[]{'\\','\'','"'}));
		Assert.assertEquals("\\'", EscapeUtil.removeSlashEscape("\\\\\\'", new char[]{'\\','\'','"'}));
		Assert.assertEquals("\\abc\\", EscapeUtil.removeSlashEscape("\\abc\\", new char[]{'\\','\'','"'}));
		Assert.assertEquals("\\abc'", EscapeUtil.removeSlashEscape("\\abc\\'", new char[]{'\\','\'','"'}));
		Assert.assertEquals("\\", EscapeUtil.removeSlashEscape("\\", new char[]{' ','\\'}));
		Assert.assertEquals("'", EscapeUtil.removeSlashEscape("\\'", new char[]{'\\','\'','"'}));
		
		Assert.assertEquals("' abc'", EscapeUtil.removeSlashEscape("\\' abc\\'", new char[]{'\\','\'','"'}));
		Assert.assertEquals("abc\\", EscapeUtil.removeSlashEscape("abc\\", new char[]{'\\','\'','"'}));
		Assert.assertEquals("abc\\", EscapeUtil.removeSlashEscape("abc\\\\", new char[]{'\\','\'','"'}));
	}
}
