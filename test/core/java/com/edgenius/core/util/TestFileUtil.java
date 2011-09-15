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
package com.edgenius.core.util;

import java.io.File;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.edgenius.core.DataRoot;

public class TestFileUtil extends TestCase{

	public void testGetFullPath(){
		Assert.assertEquals(FileUtil.getFullPath(),"");
		Assert.assertEquals(FileUtil.getFullPath("path"),"path");
		Assert.assertEquals(FileUtil.getFullPath("path"),"path");
		Assert.assertEquals(FileUtil.getFullPath("path1","path2"),"path1"+File.separator + "path2");
		Assert.assertEquals(FileUtil.getFullPath("path1","path2","path3"),"path1"+File.separator + "path2" +File.separator + "path3");
		Assert.assertEquals(FileUtil.getFullPath("path1","path2"+File.separator+"path3"),"path1"+File.separator + "path2" +File.separator + "path3");
		Assert.assertEquals(FileUtil.getFullPath("path1"+"\\\\path2"+File.separator+"path3"),"path1"+File.separator + "path2" +File.separator + "path3");
		Assert.assertEquals(FileUtil.getFullPath("path1","\\\\path2"+File.separator+"path3"),"path1"+File.separator + "path2" +File.separator + "path3");
	}
	public void testDataRoot(){
		DataRoot.rootResource ="classpath:testcase/root.properties";
		Assert.assertEquals("file://c:/var/g1/",DataRoot.getDataRoot().toLowerCase());
		
	}


}
