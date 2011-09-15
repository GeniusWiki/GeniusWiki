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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.SecurityValues.SYSTEM_ROLES;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.html.HTMLNodeContainer;
import com.edgenius.wiki.gwt.client.html.HtmlNodeListenerImpl;
import com.edgenius.wiki.gwt.client.html.HtmlParser;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;

/**
 * @author Dapeng.Ni
 */
public class TestUtil {

	/**
	 * HTML conversion uses HashMap as attribute, which cause attribute sequence is random order, further wajax attribute
	 * also has random sequence. This method compare them according attributes and its value rather than their order.
	 * 
	 * User must ensure in and out are html format. And this method relay on com.edgenius.wiki.gwt.client.html.HTMLParser, 
	 * so it can not be used on test on HTMLParser class:)
	 * @param in
	 * @param out
	 * @return
	 */
	public static  boolean equalsHMTL(String in, String out){
		if(in.equals(out))
			return true;
		
		in = in.replaceAll("\r\n","\n");
		out = out.replaceAll("\r\n","\n");
		if(in.equals(out))
			return true;
		
		
		Map<String,String> dynamicAttMap = new HashMap<String, String>();
		
		HtmlNodeListenerImpl inListener = new HtmlNodeListenerImpl();
		HtmlNodeListenerImpl outListener = new HtmlNodeListenerImpl();
		HtmlParser htmlParser = new HtmlParser();
		htmlParser.scan(in, inListener);
		htmlParser.scan(out, outListener);
		
		HTMLNodeContainer inNodes = inListener.getHtmlNode();
		HTMLNodeContainer outNodes = outListener.getHtmlNode();
		
		if(inNodes.size() != outNodes.size()){
			return false;
		}
		//compare nodes
		Iterator<HTMLNode> inIter = inNodes.iterator();
		Iterator<HTMLNode> outIter = outNodes.iterator();
		for(int idx=0;idx<inNodes.size();idx++){
			HTMLNode inN = inIter.next();
			HTMLNode outN = outIter.next();
			if(inN.isTextNode() || inN.isCloseTag()){
				if(!inN.toString().equals(outN.toString())){
					//here will throw exception rather than return false, it is helpful to view in JUnit output, ie, output
					//really content, rather than only Assert.assertTrue/False()
					Assert.assertEquals(in, out);
					return false;
				}
			}else{
				//open Tag -- need compare attribute
				Map<String, String> inAttMap = inN.getAttributes();
				Map<String, String> outAttMap = outN.getAttributes();
				if((inAttMap == null && outAttMap != null)
					|| (inAttMap != null && outAttMap == null)){
					//here will throw exception rather than return false, it is helpful to view in JUnit output, ie, output
					//really content, rather than only Assert.assertTrue/False()
					Assert.assertEquals(in, out);
					return false;
				}
				if(inAttMap == null)
					//no attribute whatever in and out
					continue;
				
				if(inAttMap.size() != outAttMap.size())
					return false;
				for (java.util.Map.Entry<String,String> entry: inAttMap.entrySet()) {
					String outValue = outAttMap.get(entry.getKey());
					String inValue = entry.getValue();
					//dynamic attribute value
					if(inValue.matches("\\$\\$DD\\d\\$\\$")){
						if(checkDynamicAttribute(dynamicAttMap, outValue, inValue)){
							continue;
						}else{
							//here will throw exception rather than return false, it is helpful to view in JUnit output, ie, output
							//really content, rather than only Assert.assertTrue/False()
							Assert.assertEquals(in, out);
							return false;
						}
					}
					if(!StringUtils.equals(inValue, outValue)){
						if(outValue == null || inValue == null){
							return false;
						}
						//compare attribute value - only wajax at moment. ???style need like wajax
						if(!NameConstants.WAJAX.equals(entry.getKey())){
							//here will throw exception rather than return false, it is helpful to view in JUnit output, ie, output
							//really content, rather than only Assert.assertTrue/False()
							Assert.assertEquals(in, out);
							return false;
						}
						
						
						if(!compareWajx(outValue, inValue,dynamicAttMap)){
							//here will throw exception rather than return false, it is helpful to view in JUnit output, ie, output
							//really content, rather than only Assert.assertTrue/False()
							Assert.assertEquals(in, out);
							return false;
						}
						
					}
					
				}
				
			}
		}
		return true;
	}

	/**
	 * @param dynamicAttMap
	 * @param outValue
	 * @param inValue
	 */
	private static boolean checkDynamicAttribute(Map<String, String> dynamicAttMap, String outValue, String inValue) {

		String existVal = dynamicAttMap.get(inValue);
		if(existVal != null){
			if(!outValue.equals(existVal))
				return false;
		}else{
			dynamicAttMap.put(inValue, outValue);
		}
		return true;
	}

	/**
	 * @param outAtt
	 * @param inAtt
	 * @param dynamicAttMap 
	 */
	private static boolean compareWajx(String outAtt, String inAtt, Map<String, String> dynamicAttMap) {
		
		//parse wajax
		Map<String, String> inWajxMap = RichTagUtil.parseWajaxAttribute(inAtt);
		Map<String, String> outWajxMap = RichTagUtil.parseWajaxAttribute(outAtt);
		
		if(inWajxMap.size() != outWajxMap.size())
			return false;
		
		for (java.util.Map.Entry<String,String> entry: inWajxMap.entrySet()) {
			String outValue = outWajxMap.get(entry.getKey());
			String inValue = entry.getValue();
			//dynamic attribute value
			if(inValue.matches("\\$\\$DD\\d\\$\\$")){
				if(checkDynamicAttribute(dynamicAttMap, outValue, inValue)){
					continue;
				}else{
					return false;
				}
			}
			
			if(!StringUtils.equals(inValue, outValue)){
				return false;
			}
		}
		
		return true;
	}

	public static User getAdminUser(){
		Date now = new Date();
		User user = new User();
		user.setUid(1);
		user.setUsername("admin");
		user.setFullname("admin");
		user.setEnabled(true);
		user.setAccountExpired(false);
		user.setCredentialsExpired(false);
		user.setCreatedDate(now);
		
		Set<Role> roles = new HashSet<Role>();
		Role admRole = new Role();
		admRole.setName(SYSTEM_ROLES.ADMIN.getName());
		admRole.setDisplayName("administrator role");
		roles.add(admRole);
		user.setRoles(roles);
		
		return user;
	}
}
