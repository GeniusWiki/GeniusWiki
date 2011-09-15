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
package com.edgenius.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;


/**
 * @author Dapeng.Ni
 */
public class TestMain{
	/**
	 * @param expected
	 * @return
	 */
	protected String appendSurroundP(String expected) {
		return expected;
//		return "<p>"+expected+"</p>";
	}
	/*
	 * Test case file format:
	 * REM (must first character) - comment
	 *  over 10 ====== is start of a test case, then following expected value
	 *  over 10 ------ is input of test method (?TODO multiple input)
	 *  
	 */
	protected List<TestItem> readTestcaseFile(String filename) throws IOException{
		
		System.out.println("Load test file from URL:"+this.getClass().getClassLoader().getResource("testcase/"+filename));
		URL url = this.getClass().getClassLoader().getResource("testcase/"+filename);
		
		TestItem item = null;
		int exp = 0;
		boolean withNewline=false;
		List<String> lines;
		try {
			lines = FileUtils.readLines(new File(url.toURI()), "UTF8");
		} catch (URISyntaxException e) {
			throw new IOException(e.toString());
		}
		List<TestItem> testcases = new ArrayList<TestItem>(); 
		for (String line: lines){
			if(!"".equals(line)){
				if(line.startsWith("REM "))
					continue;
				if(line.startsWith("===========")){
					if(item != null){
						testcases.add(item);
					}
					item = null;
					exp = 1;
					withNewline=false;
					continue;
				}
				if(line.startsWith("----------")){
					exp = 2;
					withNewline=false;
					continue;
				}
			}
			if(exp == 1){
				//if it is after first line of input
				if(withNewline){
					item.expected +="\n";
				}else{
					withNewline = true;
				}
				if(item == null) item = new TestItem();
				item.expected += line;
			}else if (exp == 2){
				//if it is after first line of input
				if(withNewline){
					item.input +="\n";
				}else{
					withNewline = true;
				}
				item.input += line;
			}
		}
		
		if(item != null)
			testcases.add(item);
		
		
		return testcases;
	}

	
}
