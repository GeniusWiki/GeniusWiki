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
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.SitemapService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * 
 * @author Dapeng.Ni
 */
public class SitemapServiceImpl implements SitemapService, InitializingBean {
	private static final String SITEMAP_URL_CONTEXT = "sitemap/";
	private static final String ROBOTS_TXT = "robots.txt";

	static final Logger log = LoggerFactory.getLogger(SitemapServiceImpl.class);
	
	public static final String SITEMAP_INDEX_NAME = "sitemap.xml";
	public static final String SITEMAP_NAME_PREFIX = "sitemap-";

	private static final int SITEMAP_GENERATE_DAYS_FEQUENCE = -28;
    private static final String SITEMAP_INDEX_TAIL_FLAG = "</sitemapindex>";
	
	 
	private SimpleDateFormat TIME_FORMAT = new  SimpleDateFormat("yyyy-MM-dd");
	
	private Resource mapResourcesRoot;
	private PageService pageService;
	
	private SitemapMetadata metadata;

	//********************************************************************
	//               Function method
	//********************************************************************
	@Override
	public File getSitemapFile(String filename) throws IOException{
		return new File(mapResourcesRoot.getFile(), filename);
	}
	
	/*
	 * A cron job in MaintainJob which is trigger daily, however, below method will only execute after <code>SITEMAP_GENERATE_DAYS_FEQUENCE</code> defined days, normally, it is 28 days.
	 */
	public boolean createSitemap() throws IOException{
		Calendar targetDate = Calendar.getInstance();
		targetDate.add(Calendar.DAY_OF_MONTH, SITEMAP_GENERATE_DAYS_FEQUENCE);
		
		if(metadata.getModifiedDate() != null && metadata.getModifiedDate().after(targetDate.getTime())){
			log.info("Sitemap is not generated because its frequency is {} days. Last creation is at {}", SITEMAP_GENERATE_DAYS_FEQUENCE, metadata.getModifiedDate());
			return false;
		}
		
		log.info("Sitemap is going to generating URLs since last update {}", metadata.getModifiedDate());
		
		List<Page> pages = pageService.getPageForSitemap(metadata.getModifiedDate());
		
		String zipSitemapFileName = generateSitemap(pages, TIME_FORMAT.format(new Date()));
		
		log.info("Sitemap success complete {} pages URLs generation on sitemap {}", pages.size(), zipSitemapFileName);
		
		return true;
	}

	//not test yet
	public boolean removePage(String pageUuid) throws IOException{
		boolean removed = false;
		String sitemapIndex = metadata.getSitemapIndex(pageUuid);
		if(sitemapIndex == null){
			log.warn("Page {} does not exist in sitemap", pageUuid);
			return removed;
		}
		
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
		String body = IOUtils.toString(bis);
		int loc = body.indexOf("<loc>"+pageUrl+"</loc>");
		if(loc != -1){
			int start = StringUtils.lastIndexOf(body, "<url>", loc);
			int end = StringUtils.indexOf(body, "</url>", loc);
			if(start != -1 && end != -1){
				//remove this URL
				body = StringUtils.substring(body, start, end+6);
				zipToFile(sizemapZipFile, body.getBytes());
				removed = true;
			}
		}
		
		metadata.removePageMap(pageUuid);
		
		return removed;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(!mapResourcesRoot.exists() && !mapResourcesRoot.getFile().mkdirs()){
			throw new BeanInitializationException("Failed creating public sitemap location.");
		}
		
		//copy existed sitemap file to explode directory
		File robotFile = new File(mapResourcesRoot.getFile(),ROBOTS_TXT);
		if(!robotFile.exists()){
			FileUtils.writeStringToFile(robotFile, "Sitemap: " + WebUtil.getHostAppURL()+ SITEMAP_URL_CONTEXT + SITEMAP_INDEX_NAME);
		}
		
		metadata = SitemapMetadata.load(mapResourcesRoot.getFile());
		
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
        if(!sitemapIndexFile.exists()){
            //if a new sitemap file
            List<String> lines = new ArrayList<String>();
            lines.add("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            lines.add("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
            lines.add("</sitemapindex>");
            FileUtils.writeLines(sitemapIndexFile, lines);
        }
        
        RandomAccessFile rfile = new RandomAccessFile(sitemapIndexFile, "rw");
        FileChannel channel = rfile.getChannel();

        //this new content will append to end of file before XML end tag
        StringBuilder lines = new StringBuilder();
        lines.append("   <sitemap>\n");
        lines.append("     <loc>" + WebUtil.getHostAppURL() + SITEMAP_URL_CONTEXT + sitemap + "</loc>\n");
        lines.append("     <lastmod>"+TIME_FORMAT.format(new Date())+" </lastmod>\n");
        lines.append("   </sitemap>\n");
        //the last tag will be overwrite, so append it again to new content. 
        lines.append(SITEMAP_INDEX_TAIL_FLAG);
        byte[] content = lines.toString().getBytes();

        ByteBuffer byteBuf = ByteBuffer.allocate(512);
        // seek first
        int len = 0, headIdx = 0;
        long tailIdx = channel.size() - 512;
        tailIdx = tailIdx < 0 ? 0 : tailIdx;

        long headPos = -1;
        StringBuilder header = new StringBuilder();
        while ((len = channel.read(byteBuf, tailIdx)) > 0) {
            byteBuf.rewind();
            byte[] dst = new byte[len];
            byteBuf.get(dst, 0, len);
            header.append(new String(dst, "UTF8"));
            headIdx = header.indexOf(SITEMAP_INDEX_TAIL_FLAG);
            if (headIdx != -1) {
                headPos = channel.size() - header.substring(headIdx).getBytes().length;
                break;
            }
        }
        FileLock lock = channel.tryLock(headPos, content.length, false);
        try {
            channel.write(ByteBuffer.wrap(content), headPos);
        } finally {
            lock.release();
        }

        channel.force(false);
        rfile.close();
        
	}
	
	private String generateSitemap(List<Page> pages, String sitemapIndex) throws IOException {
		String sitemapZip = SITEMAP_NAME_PREFIX+sitemapIndex+".xml.gz";
		File sizemapZipFile = new File(mapResourcesRoot.getFile(), sitemapZip);
		if(sizemapZipFile.exists()){
			throw new IOException("Sitemap doesn't support over once at same day. Please extend sitemap creating frequence at least daily.");
		}
		
		StringBuilder lines = new StringBuilder();
		lines.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		lines.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
		
		for (Page page : pages) {
			String url = WikiUtil.getPageRedirFullURL(page.getSpace().getUnixName(), page.getTitle(), page.getPageUuid());
			lines.append("<url>\n");
			lines.append("<loc>"+ url +"</loc>\n");
			lines.append("<lastmod>"+TIME_FORMAT.format(page.getModifiedDate())+"</lastmod>\n");
			lines.append("</url>\n");
			metadata.addPageMap(page.getPageUuid(), sitemapIndex, url);
		}
		
		lines.append("</urlset>");
		
		//gzip sitemap
		zipToFile(sizemapZipFile, lines.toString().getBytes());
		
		//Update sitemap index and metadata
		appendSitemapIndex(sitemapZip);
		
		metadata.save(mapResourcesRoot.getFile());
		
		return sitemapZip;
	}


	private void zipToFile(File sizemapZipFile, byte[] bytes) throws FileNotFoundException, IOException {
		FileOutputStream gzos = new FileOutputStream(sizemapZipFile);
        GZIPOutputStream gzipstream = new GZIPOutputStream(gzos);
        gzipstream.write(bytes);
        gzipstream.finish();
        IOUtils.closeQuietly(gzos);
	}
	
	//********************************************************************
	//  Set / Get
	//********************************************************************
	public void setMapResourcesRoot(Resource mapResourcesRoot) {
		this.mapResourcesRoot = mapResourcesRoot;
	}

	public void setPageService(PageService pageService) {
		this.pageService = pageService;
	}


}
