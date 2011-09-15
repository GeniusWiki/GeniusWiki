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

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.edgenius.core.util.FileUtil;

/**
 * @author Dapeng.Ni
 */
public class BPListParaserHandler extends DefaultHandler {
	
	
	private static final Logger log = LoggerFactory.getLogger(BPListParaserHandler.class);

	
	private Map<String,File> files;
	
	private File fileCacheDir;
	private CharArrayWriter characters;
	//1: WebResourceTextEncodingName, 2:WebResourceURL, 3:WebResourceMIMEType, 4:WebResourceFrameName,5:WebResourceData,6:WebResourceResponse
	private int tagFlag = 0;
	//1: main resource, 2: sub resource
	private int mainResource = 0;
	private String url;
	
	/**
	 * @param file
	 */
	public BPListParaserHandler(File file) {
		fileCacheDir = file;
	}
	public void startDocument() throws SAXException {
		characters = new CharArrayWriter();
		files = new HashMap<String, File>();
	}
	public void endDocument() throws SAXException {
		characters.close();
		characters = null;
	}
	
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		characters.reset();
		if("dict".equals(qName)){
			url = null;
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equals("key")){
			String content = StringUtils.trim(characters.toString());
			if("WebMainResource".equals(content)){
				mainResource = 1;
			}else if("WebSubresources".equals(content)){
				mainResource = 2;
			}else if("WebResourceURL".equals(content)){
				tagFlag = 2;
			}else if("WebResourceData".equals(content)){
				tagFlag = 5;
			}else{
				//other
				tagFlag = 0;
			}
			return;
		}
		if(qName.equals("string")){
			if(tagFlag == 2){
				url = StringUtils.trim(characters.toString());
			}
		}
		
		if(tagFlag == 5 && qName.equals("data")){
			File file = new File(fileCacheDir,FileUtil.getFileName(url));
			//Attachments
			if(url != null){
				try {
					if(mainResource == 1){
						//HTML
						url = NutParser.MAIN_RESOURCE_URL;
					}
					FileUtils.writeByteArrayToFile(file,Base64.decodeBase64(characters.toString().trim().getBytes()));
					//cache to return map
					files.put(url, file);
				} catch (IOException e) {
					log.error("Unable to write attachment for BPList",e);
				}
			}else{
				log.error("Null URL for file resource in BPList");
			}
		}

	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		characters.write(ch, start, length);
	}

	//********************************************************************
	//               Set / Get
	//********************************************************************
	public Map<String,File> getFiles() {
		return files;
	}


}
