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
package com.edgenius.wiki.gwt.client.offline;

import junit.framework.Assert;

import org.junit.Test;


/**
 * To run this test:
 * * It needs 2 dll in IVY cache dir
 * * It need set memory as -Xmx512m.
 * * It need add gwt source to classpath
 * Here need add 2 source "src/gwt" and "test/gwt"
 * 
 * Need solve problem:
 * Gears always popup security warning every time.
 * 
 * @author Dapeng.Ni
 */
public class TestGearsDB{//extends GWTTestCase {
	@Test
	public void testDummy(){
		Assert.assertEquals(true, true);
	}
//	private static final String ITME_SEP = SharedConstants.PORTLET_SEP+SharedConstants.PORTLET_SEP;
//	@Override
//	public String getModuleName() {
//		return "com.edgenius.wiki.gwt.TestModule";
//	}
//	public void testGetLayoutString() throws Exception{
		 
//		GearsDB obj = GearsDB.getUserDB(1);
//		 

//		System.out.println(getLayoutString(
//				PortletModel.SPACE + SharedConstants.PORTLET_SEP + "s1"+ SharedConstants.PORTLET_SEP +"0" + SharedConstants.PORTLET_SEP + "0"
//				+ ITME_SEP + 
//				PortletModel.SPACE + SharedConstants.PORTLET_SEP + "ab"+ SharedConstants.PORTLET_SEP +"0" + SharedConstants.PORTLET_SEP + "0" 
//				+ ITME_SEP + 
//				PortletModel.SPACE + SharedConstants.PORTLET_SEP + "c1"+ SharedConstants.PORTLET_SEP +"0" + SharedConstants.PORTLET_SEP + "0"
//				, "s1", false));
//	}


}
