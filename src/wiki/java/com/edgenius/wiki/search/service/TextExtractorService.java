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
package com.edgenius.wiki.search.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.extractor.TextExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.ITicket;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.repository.RepositoryService;

/**
 * @author Dapeng.Ni
 */
public class TextExtractorService implements InitializingBean {
	private static final Logger log = LoggerFactory.getLogger(TextExtractorService.class);
	private RepositoryService repositoryService;
	private List<String> extractors;
	private List<TextExtractor> extractorsInstances = new ArrayList<TextExtractor>();
	/**
	 * 
	 * @param spaceUname
	 * @param uuid
	 * @param version
	 * @return Could return null if there is not proper TextExtractor is found.
	 */
	public Reader extract(String spaceUname,String uuid, String version){
		Reader reader = null;
		FileNode fileNode = null;
		try {
			ITicket ticket = repositoryService.login(spaceUname,spaceUname, spaceUname);
			fileNode = repositoryService.downloadFile(ticket, uuid, version,null);
			if(fileNode != null){
				TextExtractor extractor = getExtractor(fileNode.getContentType());
				if(extractor != null)
					reader = extractor.extractText(fileNode.getFile(), fileNode.getContentType(), fileNode.getEncoding());
			}
		} catch (RepositoryException e) {
			log.error("Extract test failed ", e);
		} catch (IOException e) {
			log.error("Extract test failed " , e);
		} finally{
//			TODO: if close fileNode here. and if TextExtractor is TXT extractor, this also make the reader is closed... 
//			if(fileNode != null) fileNode.closeStream();
		}
		return reader;
	}
	public String extractText(String spaceUname,String uuid, String version){
		Reader reader = null;
		InputStream input = null;
		try {
			ITicket ticket = repositoryService.login(spaceUname,spaceUname, spaceUname);
			FileNode fileNode = repositoryService.downloadFile(ticket, uuid, version,null);
			if(fileNode != null){
				TextExtractor extractor = getExtractor(fileNode.getContentType());
				input = fileNode.getFile();
				if(extractor != null){
					reader = extractor.extractText(input, fileNode.getContentType(), fileNode.getEncoding());
				}
				if(reader != null){
//					So far, Must return string. If return reader, the reader and input must close outside this method! Otherwise, the 
					//repostiroy can not delete files...
					return IOUtils.toString(reader);
				}
			}
		} catch (RepositoryException e) {
			log.error("Extract test failed ", e);
		} catch (IOException e) {
			log.error("Extract test failed " , e);
		} finally{
			if(reader != null)
				IOUtils.closeQuietly(reader);
			if(input != null)
				IOUtils.closeQuietly(input);
		}
		return null;
	}
	public void afterPropertiesSet() throws Exception {
		if(extractors == null){
			log.error("No any extractor inject, This Service won't work any more");
			throw new BeanInitializationException("No any extractor inject, This Service won't work any more");
		}
		for(String extractor:extractors){
			TextExtractor ext = (TextExtractor) Class.forName(extractor).newInstance();
			extractorsInstances.add(ext);
		}
	}
	//********************************************************************
	//               private
	//********************************************************************
	private TextExtractor getExtractor(String contentType){
		for(TextExtractor extractor:extractorsInstances){
			String[] types = extractor.getContentTypes();
			for (String type : types) {
				if(type.equalsIgnoreCase(contentType)){
					return extractor;
				}
			}
			
		}
		return null;
	}
	//********************************************************************
	//               set /get 
	//********************************************************************
	public RepositoryService getRepositoryService() {
		return repositoryService;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public void setExtractors(List<String> extractors) {
		this.extractors = extractors;
	}

	
}
