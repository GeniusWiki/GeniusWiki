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
package com.edgenius.wiki.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import com.edgenius.core.Global;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.SitemapService;
import com.edgenius.wiki.util.WikiUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * @author Dapeng.Ni
 */
public class SitemapServiceImpl implements SitemapService, InitializingBean {
	private static final Logger log = LoggerFactory.getLogger(SitemapServiceImpl.class);
	
	public static final String SITEMAP_INDEX_NAME = "sitemap.xml";
	public static final String SITEMAP_NAME_PREFIX = "sitemap-";

	private static final int SITEMAP_GENERATE_DAYS_FEQUENCE = -7;
	
	 
	private SimpleDateFormat TIME_FORMAT = new  SimpleDateFormat("yyyy-MM-dd");
	
	private Resource mapResourcesRoot;
	@Autowired private PageService pageService;
	
	private SitemapMetadata metadata;

	//********************************************************************
	//               Function method
	//********************************************************************
	@Override
	public File getSitemapFile(String filename) throws IOException{
		return new File(mapResourcesRoot.getFile(), filename);
	}
	public boolean createSitemap() throws IOException{
		Calendar targetDate = Calendar.getInstance();
		targetDate.add(Calendar.DAY_OF_MONTH, SITEMAP_GENERATE_DAYS_FEQUENCE);
		
		if(metadata.getModifiedDate() != null && metadata.getModifiedDate().after(targetDate.getTime())){
			log.info("Sitemap is not generated because its frequency is {} days. Last creation is at {}", SITEMAP_GENERATE_DAYS_FEQUENCE, metadata.getModifiedDate());
			return false;
		}
		
		List<Page> pages = pageService.getPageForSitemap(metadata.getModifiedDate());
		
		generateSitemap(pages, TIME_FORMAT.format(new Date()));
		
		return true;
	}

	public void removePage(String pageUuid) throws IOException{
		String sitemapIndex = metadata.getSitemapIndex(pageUuid);
		
		String sitemapZip = SITEMAP_NAME_PREFIX+sitemapIndex+".xml.gz";
		File sizemapZipFile = new File(mapResourcesRoot.getFile(), sitemapZip);
		if(!sizemapZipFile.exists()){
			throw new IOException("Remove pageUuid " + pageUuid + " from sitemap failed becuase sitemap not found");
		}
		
		PipedInputStream bis = new PipedInputStream();
		PipedOutputStream bos = new PipedOutputStream(bis);
		InputStream zipfile = new FileInputStream(sizemapZipFile);
		GZIPInputStream gzipstream = new GZIPInputStream(zipfile);
		byte[] bytes = new byte[1024*1000];
		int len = 0;
		while((len = gzipstream.read(bytes)) > 0){
			bos.write(bytes, 0, len);
		}
		
		IOUtils.closeQuietly(zipfile);
		IOUtils.closeQuietly(gzipstream);
		
		String pageUrl = metadata.getPageUrl(pageUuid);
		
		Pattern sitemapElementPattern = Pattern.compile("<url>[^(?:<url>)]+"+Pattern.quote(pageUrl)+"[^(?:</url>)]+</url>");
		String body = IOUtils.toString(bis);
		Matcher matcher = sitemapElementPattern.matcher(body);
		body = matcher.replaceAll("");
		
		zipToFile(sizemapZipFile, body.getBytes());
		metadata.removePageMap(pageUuid);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(!Global.ADSENSE) return;
		
		if(!mapResourcesRoot.exists() && !mapResourcesRoot.getFile().mkdirs()){
			throw new BeanInitializationException("Failed creating public sitemap location.");
		}
		
		//copy existed sitemap file to explode directory
		File robotFile = new File(mapResourcesRoot.getFile(),"Robots.txt");
		if(!robotFile.exists()){
			FileUtils.writeStringToFile(robotFile, "Sitemap: " + WebUtil.getHostAppURL()+SITEMAP_INDEX_NAME);
		}
		
		metadata = new SitemapMetadata().load(mapResourcesRoot.getFile());
		
		try{
			this.createSitemap();
		}catch (IOException e) {
			log.info("Site map is not recreate in same day");
		}
	}
	
	//********************************************************************
	//               Private
	//********************************************************************
	private void appendSitemapIndex(String sitemap) throws IOException {
		File sitemapIndexFile = new File(mapResourcesRoot.getFile(), SITEMAP_INDEX_NAME);
		
		List<String> lines;
		int removeIdx = -1;
		if(sitemapIndexFile.exists()){
			lines = FileUtils.readLines(sitemapIndexFile);
			if(lines.size() > 0){
				//remove last tag: </sitemapindex>
				for(int idx = lines.size()-1; idx >= 0;idx--){
					if("</sitemapindex>".equals(lines.get(idx).trim())){
						removeIdx = idx;
						break;
					}
				}
				if(removeIdx != -1){
					lines.remove(removeIdx);
				}
			}
		}else{
			lines = new ArrayList<String>();
		}
		
		if(removeIdx == -1){
			//assume a new file - this may be cause problem if sitemap crashed.
			lines.add("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			lines.add("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
		}
		
		lines.add("   <sitemap>");
		lines.add("     <loc>" + WebUtil.getHostAppURL()+sitemap+"</loc>");
		lines.add("     <lastmod>"+TIME_FORMAT.format(new Date())+" </lastmod>");
		lines.add("   </sitemap>");
		
		lines.add("</sitemapindex>");
		
		FileUtils.writeLines(sitemapIndexFile, lines);
		
	}
	
	private void generateSitemap(List<Page> pages, String sitemapIndex) throws IOException {
		String sitemapZip = SITEMAP_NAME_PREFIX+sitemapIndex+".xml.gz";
		File sizemapZipFile = new File(mapResourcesRoot.getFile(), sitemapZip);
		if(sizemapZipFile.exists()){
			throw new IOException("Sitemap doesn't support over once at same day. Please extend sitemap creating frequence at least daily.");
		}
		
		StringBuilder lines = new StringBuilder();
		lines.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		lines.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
		
		for (Page page : pages) {
			String url = WikiUtil.getPageRedirFullURL(page.getSpace().getUnixName(), page.getTitle(), page.getPageUuid());
			lines.append("<url>");
			lines.append("<loc>"+ url +"</loc>");
			lines.append("<lastmod>"+TIME_FORMAT.format(page.getModifiedDate())+"</lastmod>");
			lines.append("</url>");
			metadata.addPageMap(page.getPageUuid(), sitemapIndex, url);
		}
		
		lines.append("</urlset>");
		
		//gzip sitemap
		zipToFile(sizemapZipFile, lines.toString().getBytes());
		
		//Update sitemap index and metadata
		appendSitemapIndex(sitemapZip);
		
		metadata.save(mapResourcesRoot.getFile());
	}


	private void zipToFile(File sizemapZipFile, byte[] bytes) throws FileNotFoundException, IOException {
		FileOutputStream gzos = new FileOutputStream(sizemapZipFile);
        GZIPOutputStream gzipstream = new GZIPOutputStream(gzos);
        gzipstream.write(bytes);
        gzipstream.finish();
        IOUtils.closeQuietly(gzos);
	}
	

	//********************************************************************
	//               Private class
	//********************************************************************
	@XStreamAlias("SitemapMetadata")
	private class SitemapMetadata{
		private static final String SEP = "|";
		
		private Date modifiedDate;
		//Save the page into which sitemap. Key: pageUuid, Value: sitemapIndex
		private LinkedHashMap<String, String> pageMap = new LinkedHashMap<String, String>();
		
		public void addPageMap(String pageUuid, String sitemapIndex, String pageurl){
			pageMap.put(pageUuid, sitemapIndex + SEP + pageurl);
		}
		
		/**
		 * @param pageUuid
		 * @return
		 */
		public String getPageUrl(String pageUuid) {
			String value = pageMap.get(pageUuid);
			String[] str = StringUtils.split(value, SEP);
			if(str != null && str.length == 2){
				return str[1];
			}
			return null;
		}

		/**
		 * @param pageUuid
		 * @return
		 */
		public String getSitemapIndex(String pageUuid) {
			String value = pageMap.get(pageUuid);
			String[] str = StringUtils.split(value, SEP);
			if(str != null && str.length == 2){
				return str[0];
			}
			return null;
		}

		public void removePageMap(String pageUuid){
			pageMap.remove(pageUuid);
		}
		
		public void save(File root) {
			
			File sitemapIndexFile = new File(root,"sitemap-metadata.xml");
			FileOutputStream fos = null;
			try {
				
				modifiedDate = new Date();
				
				fos = new FileOutputStream(sitemapIndexFile);
				XStream xs = new XStream();
				xs.processAnnotations(SitemapMetadata.class);
				xs.toXML(this, fos);
			} catch (FileNotFoundException e) {
				log.error("Unable to write sitemap-metadata.xml",e);
			} finally{
				IOUtils.closeQuietly(fos);
			}
			
		}
		
		public SitemapMetadata load(File root){
			
			File sitemapIndexFile = new File(root,"sitemap-metadata.xml");
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(sitemapIndexFile);
				XStream xs = new XStream();
				xs.processAnnotations(SitemapMetadata.class);
				SitemapMetadata meta = (SitemapMetadata) xs.fromXML(fis);
				return meta;
			} catch (FileNotFoundException e) {
				log.warn("Sitemap-metadata.xml not found");
			} finally{
				IOUtils.closeQuietly(fis);
			}
			return new SitemapMetadata(); 
		}

		public Date getModifiedDate() {
			return modifiedDate;
		}

		public void setModifiedDate(Date modifiedDate) {
			this.modifiedDate = modifiedDate;
		}

		public LinkedHashMap<String, String> getPageMap() {
			return pageMap;
		}

		public void setPageMap(LinkedHashMap<String, String> pageMap) {
			this.pageMap = pageMap;
		}
	}
	//********************************************************************
	//               Set / Get
	//********************************************************************

	public void setMapResourcesRoot(Resource mapResourcesRoot) {
		this.mapResourcesRoot = mapResourcesRoot;
	}


}
