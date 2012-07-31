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
package com.edgenius.wiki.render.macro;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.render.ImmutableContentMacro;
import com.edgenius.wiki.render.MacroParameter;
import com.edgenius.wiki.render.MalformedMacroException;
import com.edgenius.wiki.render.RenderContext;

/**
 * {code:source=Java}
 * some java code
 * {code}
 * 
 * @author Dapeng.Ni
 */
public class CodeMacro extends BaseMacro implements ImmutableContentMacro{
	
	//this is compliance with dp_SyntaxHighligher support
	private static final String[]  SUPPORTED_CODE_NAMES = new String[]{
	    "applescript",
	    "actionscript3","as3",
	    "bash", "shell",
	    "coldfusion", "cf",
	    "cpp", "c",
	    "c#", "c-sharp", "csharp",
	    "css",
	    "delphi", "pascal",
	    "diff", "patch", "pas",
	    "erl", "erlang",
	    "groovy",
	    "java",
	    "jfx", "javafx",
	    "js", "jscript", "javascript",
	    "perl", "pl",
	    "php",
	    "text", "plain",
	    "py", "python",
	    "ruby", "rails", "ror", "rb",
	    "scala",
	    "sql",
	    "vb", "vbnet",
	    "xml", "xhtml", "xslt", "html"
		};
	public static final String DEFAULT_CODE = "common";
	
	@Override
	public String getHTMLIdentifier() {
		return "<pre aid=\""+this.getClass().getName()+"\">";
	}
	public String[] getName() {
		return new String[]{"code"};
	}
	public boolean isProcessEmbedded(){
		return false;
	}
	public boolean isPaired(){
		return true;
	}

	public void execute(StringBuffer buffer, MacroParameter params) throws MalformedMacroException {
		RenderContext context = params.getRenderContext();
		//put original text into attribute, then replace after all text replaced finished.
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put(NameConstants.CODE, params.getContent());

		//handle possible alias (source=src)
		String src = params.getParam(NameConstants.SOURCE);
		if(StringUtils.isBlank(src)){
			//alias for SOURCE
			src = params.getParam(NameConstants.SRC);
		}
		if(!StringUtils.isBlank(src))
			attributes.put(NameConstants.SOURCE, src);
		
		handle(buffer,context,attributes);
		
	}
	private void handle(StringBuffer buffer, RenderContext renderContext, Map<String, String> values) {

		String src = values.get(NameConstants.SOURCE);
		if(src == null){
			//give default source name, as it is mandatory for dp_SyntaxHighlighter
			src = DEFAULT_CODE;
		}else{
		    src = src.toLowerCase();
		    __supported:{
    			for (String code : SUPPORTED_CODE_NAMES) {
    				if(code.equals(src)){
    					break __supported;
    				}
    			}
    			src = DEFAULT_CODE;
		    }
		}
		String result = values.get(NameConstants.CODE);
		//following dp_SyntaxHighligher require: must replace "<", I just do HTML entity replace then
		//reverse replace must do in Rich to Markup render
		result = EscapeUtil.escapeHTML(result);
		
		//name="sourcecode" must keep consist with view-scripts.jsp highlightSyntax()
		buffer.append("<pre name=\"sourcecode\" class=\"brush:").append(src).append("\" ");
		buffer.append(NameConstants.AID).append("=\"").append(CodeMacro.class.getName()).append("\">");
		buffer.append(result);
		buffer.append("</pre>");
	}
	
	@Override
	protected void replaceHTML(HTMLNode node, ListIterator<HTMLNode> iter, RenderContext context) {
		if(node.getPair() == null){
			log.warn("Unexpect case: No close div tag for " + this.getClass().getName());
			return;
		}
		//remove all HTML tag, as any these tag must from RichEditor, which is invalid to add style to code macro part...
		HTMLNode subnode;
		StringBuffer sb = new StringBuffer(); 
		
		//!!! Here changes ListIterator cursor position!!!
		for(;iter.hasNext();){
			subnode = iter.next();
			if(subnode == node.getPair())
				break;
			
			if(!subnode.isTextNode()){
				subnode.reset("", true);
				if(subnode.getPair() != null)
					subnode.getPair().reset("", true);
			}else{
				sb.append(subnode.getText());
			}
		}
		
		//do reverse replace, refer to CodeHandler...
		if(sb.length() > 0){
			String code = sb.toString();
			code = code.replaceAll("&lt;", "<");
			code = code.replaceAll("&amp;", "&");
			//if code content is not empty, node must has next()
			node.next().reset(code, true);
			
			String src = "";
			if(node.getAttributes() != null && node.getAttributes().get(NameConstants.CLASS) != null){
				String srcName = node.getAttributes().get(NameConstants.CLASS);
				if(!DEFAULT_CODE.equalsIgnoreCase(srcName))
					src = "|"+NameConstants.SRC+"="+srcName;
			}
			
			//as above Iterator cursor already move to end of pair node, so move them back and restore around insertMarkup method
			
			moveIteratorCursorTo(node,iter,false); //move from end to start 
			resetMacroMarkup(TIDY_STYLE_BLOCK, node, iter, "{code"+src+"}", "{code}");
			moveIteratorCursorTo(node.getPair(),iter,true); // move from start to end

		}		
	}
	
}
