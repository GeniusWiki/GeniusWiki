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
package com.edgenius.wiki.gwt.client.html;


/**
 * 
 * This is a very simple HTMLParser, which extract from my another project. 
 * No HTML body handle, so it means all tag will treat same if it insides &lt;body&gt; tag.   
 * 
 * @author Dapeng.Ni
 */
public class HtmlParser{

	/**
	 * Scan HTML and HtmlContentListener will be invoke
	 * @param listener 
	 *
	 */
	public void scan(String line, HtmlListener listener){
		
		int tagS,tagE;
		listener.startDocument();
		StringBuffer buf = new StringBuffer(line);
		do{
			tagS = buf.indexOf("<");
			//only has content without any tag
			if(tagS == -1){
				listener.content(buf.toString());
				break;
			}
			if(tagS > 0){
				listener.content(buf.substring(0,tagS));
				buf.delete(0, tagS);
			}

			tagS = 0;
			//comment is trunk in checkSkipedTag() method
			if(buf.length() > 4 && buf.substring(0, 4).equals("<!--") ){
				//eager to comments close tag -->
				tagE = buf.indexOf("-->");
				if(tagE == -1){
					//invalid comment start, just treat it as content
					listener.content(buf.substring(tagS,4));
					buf.delete(0, 4);
				}else{
					//??? treat it as text?  currently, only ignore comment
					listener.content(buf.substring(tagS,tagE+3));
					buf.delete(0, tagE+3);
				}
				continue;
			}
			tagE = buf.indexOf(">");
			if(tagE == -1){
				//invalid tag(only has start tag <, then skip it as content.
				listener.content(buf.substring(tagS,1));
				buf.delete(0, tagS+1);
			}else{
				listener.tag(buf.substring(tagS,tagE+1));
				buf.delete(0, tagE+1);
			}
		}while(buf.length() > 0);
		
		listener.endDocument();
		
		
	}

//	public static void main(String[] args) throws FileNotFoundException {
//		Reader r = new FileReader(new File("c:/temp/a.html"));
//		StringBuffer c = new StringBuffer();
//		StringBuffer t = new StringBuffer();
//		HtmlParser p  = new HtmlParser();
//		p.scan(r,p.new HtmlListenerImpl(c,t));
//		System.out.println(c);
//		System.out.println("=====================================================");
//		System.out.println(t);
//	}
//	public class HtmlListenerImpl implements HtmlListener {
//
//		
//		private StringBuffer pureText;
//		private StringBuffer tag;
//
//		/**
//		 * @param pureText
//		 * @param t 
//		 */
//		public HtmlListenerImpl(StringBuffer pureText, StringBuffer tag) {
//			this.pureText = pureText;
//			this.tag = tag;
//		}
//
//		public void content(String content) {
//			pureText.append(content);
//		}
//
//		public void endDocument() {
//
//		}
//
//		public void startDocument() {
//
//		}
//
//		public void tag(String tagStr) {
//			tag.append(tagStr);
//		}
//
//	}
}
