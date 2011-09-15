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
package com.edgenius.wiki.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.unitils.spring.annotation.SpringBean;

import com.edgenius.core.service.UserService;
import com.edgenius.core.util.StringEscapeUtil;
import com.edgenius.test.TestDataConstants;
import com.edgenius.test.TestItem;
import com.edgenius.test.TestMain;
import com.edgenius.test.TestUtil;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.plugin.PluginServiceImpl;
import com.edgenius.wiki.render.FilterRegxConstants;
import com.edgenius.wiki.render.RegexProvider;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.TokenVisitor;
import com.edgenius.wiki.render.impl.BaseMacroParameter;
import com.edgenius.wiki.render.impl.JdkRegexProvider;
import com.edgenius.wiki.security.service.SecurityService;

/**
 * @author Dapeng.Ni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/testAplicationContext-services.xml"
	,"/com/edgenius/core/applicationContext-cache.xml"
	,"/com/edgenius/core/applicationContext-core-orm.xml"
	,"/com/edgenius/core/applicationContext-core-service.xml"
	,"/com/edgenius/wiki/applicationContext-service.xml"
	,"/com/edgenius/wiki/applicationContext-orm.xml"
	,"/com/edgenius/wiki/applicationContext-security.xml"
	,"/com/edgenius/ext/applicationContext-ext-service.xml"
	,"/com/edgenius/ext/applicationContext-ext-orm.xml"
	})
public class TestRenderService  extends TestMain{
	RegexProvider<Matcher> singleMacroProvider = new JdkRegexProvider();
	

	@Autowired
	RenderServiceImpl renderService;
	@Autowired
	PluginServiceImpl pluginService;  

	@Autowired
	@Qualifier("testPluginTemplateEngine")
	FreeMarkerConfigurer testPluginTemplateEngine;
	
	@SpringBean("userService")
	UserService userService;
	
	@Autowired
	SecurityService securityService;
	
	@Before
	public void setUp() throws Exception {
		
		singleMacroProvider.compile(FilterRegxConstants.SINGLE_MACRO, Pattern.MULTILINE);
		pluginService.setPluginTemplateEngine(testPluginTemplateEngine);
		securityService.proxyLogin("admin");
	}
	@After
	public void tearDown(){
		securityService.proxyLogout();
	}
	
	/**
	 * Mark to HTML, then HTML to markup, markup to HTML, final, go back to Markup
	 * @throws IOException
	 */
	@Test
	public void testMarkupConversion() throws IOException{
		List<TestItem> cases = readTestcaseFile("MarkupConversion");
		
		for (TestItem testItem : cases) {
			//to html
			List<RenderPiece> pieces = renderService.renderHTML(RenderContext.RENDER_TARGET_RICH_EDITOR, TestDataConstants.spaceUname1,TestDataConstants.pageUuid1,testItem.expected, null);
			String html = renderService.renderRichHTML(TestDataConstants.spaceUname1,TestDataConstants.pageUuid1,pieces);
			System.out.println(html);
			
			//goback to markup
			String markup = renderService.renderHTMLtoMarkup(TestDataConstants.spaceUname1,html);
			
			//go html again
			pieces = renderService.renderHTML(RenderContext.RENDER_TARGET_RICH_EDITOR, TestDataConstants.spaceUname1,TestDataConstants.pageUuid1,markup, null);
			html = renderService.renderRichHTML(TestDataConstants.spaceUname1,TestDataConstants.pageUuid1, pieces);
			
			
			//return markup 
			markup = renderService.renderHTMLtoMarkup(TestDataConstants.spaceUname1,html);
			if(testItem.input.startsWith("MIXED") || testItem.input == ""){
				if(testItem.input == ""){
					Assert.assertEquals(testItem.expected, markup);
				}else{
					//MIXED - need parse
					final Map<String,String> expMap = new HashMap<String, String>();
					final Map<String,String> outMap = new HashMap<String, String>();
					//maybe the parameter value is in wrong sequence, so have to parse macro and compare
					singleMacroProvider.replaceByTokenVisitor(testItem.expected, new TokenVisitor<Matcher>() {
						public void handleMatch(StringBuffer buffer, Matcher result) {
							expMap.put("MACRO_NAME", result.group(1));
							BaseMacroParameter mParams = new BaseMacroParameter();
							mParams.setParams(StringEscapeUtil.unescapeHtml(result.group(2)));
							expMap.putAll(mParams.getParams());
						}
					});
					singleMacroProvider.replaceByTokenVisitor(markup, new TokenVisitor<Matcher>() {
						public void handleMatch(StringBuffer buffer, Matcher result) {
							outMap.put("MACRO_NAME", result.group(1));
							BaseMacroParameter mParams = new BaseMacroParameter();
							mParams.setParams(StringEscapeUtil.unescapeHtml(result.group(2)));
							outMap.putAll(mParams.getParams());
						}
					});
					
					for (Entry<String,String> entry: expMap.entrySet()) {
						if(!StringUtils.equalsIgnoreCase(outMap.remove(entry.getKey()),entry.getValue())){
							//this just for throw readable error message and stop test
							Assert.assertEquals(testItem.expected, markup);
						}
					}
					if(outMap.size() != 0)
						//this just for throw readable error message and stop test
						Assert.assertEquals(testItem.expected, markup);
				}
			}else{
				Assert.assertEquals(testItem.input, markup);
			}
		}
	}

	/**
	 * Markup to HTML
	 * @throws IOException
	 */
	@Test 
	public void testMarkupRender() throws IOException{
		List<TestItem> cases = readTestcaseFile("Markup");
		for (TestItem testItem : cases) {
			//to html
			List<RenderPiece> pieces = renderService.renderHTML(RenderContext.RENDER_TARGET_RICH_EDITOR, TestDataConstants.spaceUname1,TestDataConstants.pageUuid1,testItem.input, null);
			StringBuffer markupsb = new StringBuffer(); 
			for (RenderPiece renderPiece : pieces) {
				if(renderPiece instanceof LinkModel){
					markupsb.append(((LinkModel)renderPiece).toRichAjaxTag());
				}else{
					markupsb.append(renderPiece.toString());
				}
			}

			if(!TestUtil.equalsHMTL(appendSurroundP(testItem.expected), markupsb.toString()))
				//this is just for easy to view in Eclipse  
				Assert.assertEquals(testItem.expected,markupsb.toString());
		}
	}
	
	
	/**
	 * HTML to Markup 
	 * @throws IOException
	 */
	@Test 
	public void testHTMLRender() throws IOException{
		List<TestItem> cases = readTestcaseFile("HTML");
		for (TestItem testItem : cases) {
			//to markup
			String markup = renderService.renderHTMLtoMarkup(TestDataConstants.spaceUname1,testItem.input);
			Assert.assertEquals(testItem.expected, markup);
		}
	}
	
	@Test 
	public void testHTMLNativeRender() throws Exception{
		List<TestItem> cases = readTestcaseFile("renderHTMLText");
		for (TestItem item : cases) {
			List<RenderPiece> pieces = renderService.renderHTML(item.input);
			String html = renderService.renderNativeHTML(null, null, pieces);
			if(!TestUtil.equalsHMTL(appendSurroundP(item.expected), html)){
				Assert.assertEquals(appendSurroundP(item.expected), html);
			}
		}
	}
	

}
