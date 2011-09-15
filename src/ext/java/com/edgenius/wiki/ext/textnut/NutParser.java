/* 
 * =============================================================
 * Copyright (C) 2007-2011 Edgenius (http://www.edgenius.com)
 * =============================================================
 * License Information: http://www.edgenius.com/licensing/edgenius/2.0/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 *  
 * ****************************************************************
 */
package com.edgenius.wiki.ext.textnut;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.xml.sax.XMLReader;

import com.dd.plist.BinaryPropertyListParser;
import com.dd.plist.NSObject;
import com.edgenius.core.util.FileUtil;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.html.HTMLNodeContainer;
import com.edgenius.wiki.gwt.client.html.HtmlNodeListenerImpl;
import com.edgenius.wiki.gwt.client.html.HtmlParser;
import com.steadystate.css.parser.CSSOMParser;

/**
 * @author Dapeng.Ni
 */
public class NutParser {
	public static final String MAIN_RESOURCE_URL = "index.html";
	
	private static final Logger log = LoggerFactory.getLogger(NutParser.class);
	
	public static void main(String[] args) throws IOException {
		NutParser parser = new NutParser();
//		parser.parseHTML(FileUtils.readFileToString(new File("c:/temp/a.html")));
		Map<String, File> map = parser.parseBPlist(new FileInputStream(new File("C:/Dapeng/Future/webarchive/TextNut.nut/20110312/P1.webarchive")));
		if(map != null){
			for (Entry<String, File> entry: map.entrySet()) {
				System.out.println(entry.getKey() + ":" + entry.getValue());
			}
			File file = map.get(MAIN_RESOURCE_URL);
			String content = parser.convertNutHTMLToPageHTML(FileUtils.readFileToString(file));
			System.out.println("=======");
			System.out.println(content);
			System.out.println("=======");
		}
	}
	public Map<String, File> parseBPlist(InputStream bs){
	
		try {
			NSObject obj = BinaryPropertyListParser.parse(bs);

			//parse BPList
			BPListParaserHandler handler = new BPListParaserHandler(new File(FileUtil.createTempDirectory("_nut")));
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setContentHandler(handler);
			
			//parse
			reader.parse(new org.xml.sax.InputSource(new StringReader(obj.toXML())));
			
			//all files - main HTML file key is "MAIN_RESOURCE_URL"
			return handler.getFiles();
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupport encoding for BPList",e);
		} catch (Exception e) {
			log.error("Parase BPList error.",e);
		}
		return null;
	}
	/**
	 * Remove tags outside body.
	 * Apply HTML CSS into tag style.
	 * Remove default attribute in tag.
	 * 
	 * @param htmlContent
	 * @return
	 */
	public String convertNutHTMLToPageHTML(String htmlContent){
		
		//Parse whole HTML, and separator out CSS and tags list inside body.
		HtmlParser htmlParser = new HtmlParser();
		HtmlNodeListenerImpl listener = new HtmlNodeListenerImpl();
		htmlParser.scan(htmlContent, listener);
		
		StringBuffer css = new StringBuffer();
		HTMLNodeContainer body = new HTMLNodeContainer();
		
		//First, exact CSS style part, then remove all element outside body tag
		int require = 0; //1=css;2=body
		HTMLNode endNode = null;
		for (HTMLNode node : listener.getHtmlNode()) {
			
			if(!node.isTextNode() && "style".equals(node.getTagName()) && !node.isCloseTag()){
				endNode = node.getPair();
				require = 1;
				continue;
			}
			if(!node.isTextNode() && "body".equals(node.getTagName()) && !node.isCloseTag()){
				endNode = node.getPair();
				require = 2;
				continue;
			}
			if(endNode != null && require == 1){
				if(endNode == node){
					endNode = null;
				}else{
					if(node.isTextNode())
						css.append(node.getText());
				}
				
				continue;
			}
			if(endNode != null && require == 2){
				if(endNode == node){
					break;
				}else{
					body.add(node);
				}
			}
		}
		
		// parse and create a stylesheet composition
	    CSSOMParser parser = new CSSOMParser();
	    //I don't know how to use CSSParser API to quick located style by selector, so blow map will be used for this purpose
	    //its key is selector text, value is style list separated by ";" which is able to use in tag "class" attribute.
	    Map<String, String> selectors = new HashMap<String, String>();
        try {
			CSSStyleSheet stylesheet = parser.parseStyleSheet(new InputSource(new StringReader(css.toString().trim())), null, null);
			CSSRuleList ruleList = stylesheet.getCssRules();
		    for (int idx = 0; idx < ruleList.getLength(); idx++){ 
              CSSRule rule = ruleList.item(idx);
              if (rule instanceof CSSStyleRule){ 
                  CSSStyleDeclaration style = ((CSSStyleRule)rule).getStyle();
                  StringBuffer buf = new StringBuffer();
                  int len = style.getLength();
                  for (int idj = 0; idj < len; idj++){
                       String name = style.item(idj);
                       String value = style.getPropertyCSSValue(name).getCssText();
                       if("margin".equals(name) && value.equals("0px 0px 0px 0px")){
                    	   continue;
                       }else if("font".equals(name) && value.equals("18px Helvetica")){
                    	   //This value must coordinate with TextNut default font setting.
                    	   continue;
                       }
                       
                       buf.append(name).append(":").append(value).append(";");
                  }
                  
                  //buf could be blank, even so, we still need put it into map so that class attribute can locate style by name
                  selectors.put(((CSSStyleRule) rule).getSelectorText(), buf.toString());
               
              }
            } 
		    
		    //debug use: only for display:
//		    for (int idx = 0; idx < ruleList.getLength(); idx++) {
//              CSSRule rule = ruleList.item(idx);
//              if (rule instanceof CSSStyleRule){ 
//                  CSSStyleRule styleRule=(CSSStyleRule)rule;
//                  CSSStyleDeclaration style = styleRule.getStyle();
//                  System.out.println(styleRule.getSelectorText());
//                  for (int idj = 0; idj < style.getLength(); idj++){
//                       String name = style.item(idj);
//                       System.out.println(name +"=" + style.getPropertyCSSValue(name).getCssText());
//                  }
//              }
//            }

			//apply CSS to body tags
			HTMLNode node;
			for(Iterator<HTMLNode> iter = body.iterator();iter.hasNext();){
				node = iter.next();
				if(node.isTextNode())
					continue;
				
				if(node.getAttributes() != null && !StringUtils.isBlank(node.getAttributes().get("class"))){
					//we only support "tagName.styleName" format selector
					String style = selectors.get(node.getTagName()+"."+node.getAttributes().get("class"));
					if(style != null){
						if(!"".equals(style)){
							//could be blank, if style all removed because they are default value.
							node.setAttribute("style", style);
						}
						node.removeAttribute("class");
					}
				}
				//embedded files object - <object data="file:///index_1.html">index_1.html</object>
				if("object".equals(node.getTagName()) && node.getAttributes() != null 
					&& StringUtils.startsWith(node.getAttributes().get("data"),"file://")){
					
				}
			}
			
			return body.toString().trim();
		} catch (IOException e) {
			log.error("Parse CSS failed",e);
		}
		return htmlContent;
	}

}
