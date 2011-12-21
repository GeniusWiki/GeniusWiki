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
package com.edgenius.wiki.rss;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.edgenius.core.Constants;
import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.FileUtil;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.ThemeService;
import com.edgenius.wiki.util.WikiUtil;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.feed.synd.SyndPersonImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * @author Dapeng.Ni
 */
@Transactional(readOnly=true, propagation=Propagation.REQUIRES_NEW, noRollbackFor=Exception.class)
public class RSSServiceImpl implements RSSService, InitializingBean{
	private static final Logger log = LoggerFactory.getLogger(RSSServiceImpl.class);
	private static final String RSS_OUTPUT_TYPE = "rss_2.0";
	private static final String RSS_TITLE_PRE = WikiConstants.APP_NAME + " - ";
	
	//DON'T user SpaceService,PageService, which require Security check and then require HttpServletRequest is available, 
	//but in this case RSSServiceImpl is call inside MQ rather than HTTPRequest
	private SecurityService securityService;
	private SpaceService spaceService; 
	private RenderService renderService; 
	private UserReadingService userReadingService;
	private Resource rssRoot;
	//for each space Feed file, we provide a unique lock for it. It maybe not good idea for if huge spaces exist.
	private ConcurrentHashMap<String, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<String, ReentrantReadWriteLock>();
	private ConcurrentLinkedQueue<DocumentBuilder> xmlBuilderPool = new ConcurrentLinkedQueue<DocumentBuilder>();
	
	private ThemeService themeService;
	//********************************************************************
	//               Function methods
	//********************************************************************
	//JDK1.6 @Override
	public void createFeed(String spaceUname) {
		Space space = spaceService.getSpaceByUname(spaceUname);
		if(space == null){
			log.error("Space does not exist:" + spaceUname);
			return;
		}
		
		log.info("RSS Feed for space " + spaceUname + " is going to build.");

		SpaceSetting setting = space.getSetting();
		int rsslen = setting.getRssContentLen();
		
		SyndFeed feed = new SyndFeedImpl();
		feed.setEncoding("UTF-8");
		feed.setFeedType(RSS_OUTPUT_TYPE);
		feed.setTitle(RSS_TITLE_PRE+space.getName());
		feed.setDescription(space.getDescription());
		try {
			feed.setLink(WebUtil.getHostAppURL()+ "page/" + URLEncoder.encode(spaceUname,Constants.UTF8));
		} catch (UnsupportedEncodingException e1) {
			feed.setLink(WebUtil.getHostAppURL());
		}
		feed.getModules().add(new PageRSSModuleImpl());
		
		List<SyndEntry> entries = feed.getEntries();
		
		int style = setting.getWidgetStyle();
		boolean sortByModify = true;
		if((style & SpaceSetting.WIDGET_STYLE_ITEM_SHORT_BY_CREATE_DATE) >0){
			sortByModify = false;
		}
		try{
			//some pages may need some permission to render, for example, page with {index} macro which will use pageService.getPageTree()
			//and that method needs spaceReading permission - here just login as space admin
			securityService.proxyLoginAsSpaceAdmin(spaceUname);
			List<Page> pages = spaceService.getRecentPages(spaceUname,setting.getRssItemsCount(), sortByModify);
			for (Page page : pages) {
				//skip page which contain {blog} macro, which contains all posts of page, so it is useless to put on feed
				if(WikiUtil.hasBlogRender(page,themeService)){
					continue;
				}
				
				SyndEntry entry = new SyndEntryImpl();
				//~~~~~~~~ Title
				entry.setTitle(page.getTitle());
				
				//~~~~~~~~ Author
				List<SyndPerson> authors = new ArrayList<SyndPerson>();
				SyndPerson creator = new SyndPersonImpl();
				if (page.getCreator() != null) {
					creator.setName(page.getCreator().getFullname());
					creator.setUri(page.getCreator().getUsername());
				} else {
					// Anonymous
					User anony = WikiUtil.getAnonymous(userReadingService);
					creator.setName(anony.getFullname());
					creator.setUri(anony.getUsername());
				}
				SyndPerson modifier = new SyndPersonImpl();
				if (page.getModifier() != null) {
					modifier.setName(page.getModifier().getFullname());
					modifier.setUri(page.getModifier().getUsername());
				} else {
					// Anonymous
					User anony = WikiUtil.getAnonymous(userReadingService);
					modifier.setName(anony.getFullname());
					modifier.setUri(anony.getUsername());
				}
				
				authors.add(creator);
				authors.add(modifier);
				entry.setAuthors(authors);
				
				//~~~~~~~~ Desc
				//need render to HTML object first
				renderService.renderHTML(RenderContext.RENDER_TARGET_PLAIN_VIEW, page);
	
				String content = renderService.renderNativeHTML(spaceUname, page.getPageUuid(), page.getRenderPieces()); 
	//			//could not simply use substring because it is not pure text
	//			if(rsslen > 0 && rsslen < content.length())
	//				content = content.substring(0,rsslen);
				
				SyndContentImpl desc = new SyndContentImpl();  
				desc.setType("text/html");  
				desc.setValue(content);
				entry.setDescription(desc);
				
				//~~~~~~~~ Link
				String link = WikiUtil.getPageRedirFullURL(spaceUname, page.getTitle(),page.getPageUuid());
				entry.setLink(link);
				//~~~~~~~~ Date
				entry.setPublishedDate(page.getModifiedDate());
				
				//~~~~~~~~ Page Modules
				PageRSSModule pageModule = new PageRSSModuleImpl();
				pageModule.setPageUuid(page.getPageUuid());
				pageModule.setSpaceUname(spaceUname);
				pageModule.setVersion(page.getVersion());
				pageModule.setCreator(creator.getUri());
				pageModule.setModifier(modifier.getUri());
				pageModule.setCreateDate(page.getCreatedDate());
				pageModule.setModifiedDate(page.getModifiedDate());
				entry.getModules().add(pageModule);
				
				entries.add(entry);
			}
		}finally{
			securityService.proxyLogout();
		}
		OutputStream os = null;
		ReentrantReadWriteLock lock = null;
		try {
			String uid = space.getUid().toString();
			//do write lock
			lock = lockMap.get(uid);
			if(lock == null){
				lock = new ReentrantReadWriteLock();
				lockMap.put(uid, lock);
			}
			lock.writeLock().lock();
			
			File out = new File(FileUtil.getFullPath(rssRoot.getFile().getAbsolutePath(),uid+".xml"));
			SyndFeedOutput output = new SyndFeedOutput();
			String str = output.outputString(feed);
			os = new FileOutputStream(out);
			os.write(str.getBytes("UTF-8"));
			
			log.info("RSS Feed for space " + spaceUname + " is success created.");
		} catch (IOException e) {
			log.error("Failed create RSS fead file " + space.getUid(),e);
		} catch (FeedException e) {
			log.error("Failed create RSS fead file " + space.getUid(),e);
		}finally{
			try{
				if(os != null) os.close();
			}catch (Exception e) {
				//nothing
			}
			if(lock != null) lock.writeLock().unlock();
		}
		
	}

	public boolean removeFeedItem(String spaceUname, String removePageUuid) {
		log.info("Feed item of pageUuid " + removePageUuid  + " is going to remove from space " + spaceUname);
		Space space = spaceService.getSpaceByUname(spaceUname);
		boolean exist = false;
		try {
			//don't filter out any page item (user is anonymous), this ensure return entire page list, not only these anonymous has read permission
			List<Page> list = getPagesFromFeed(space.getUid(), spaceUname, null, true);
			if(list != null){
				for (Page page : list) {
					if(StringUtils.equals(page.getPageUuid(),removePageUuid)){
						log.info("Feed item of pageUuid " + removePageUuid  + " remove successed.");
						exist = true;
						break;
					}
				}
			}
		} catch (FeedException e) {
			log.error("Remove feed item pre-check failed, just rebuild entire RSS. Space " + spaceUname + " remove item " + removePageUuid,e);
			exist = true;
		}
		
		//only this page item exist in current RSS, rebuild entire RSS.
		if(exist)
			createFeed(spaceUname);
		
		return exist;
	}

	public void removeFeed(Integer spaceUid){
		try {
			File out = new File(FileUtil.getFullPath(rssRoot.getFile().getAbsolutePath(),spaceUid.toString()+".xml"));
			boolean del = out.delete();
			if(!del)
				//try again
				out.deleteOnExit();
			log.info("RSS feed for spaceUid " + spaceUid + " is removed.");
		} catch (IOException e) {
			log.error("Unable delete RSS feed file for spaceUid " + spaceUid + " with error " , e);
		}
	}


	public String outputFeed(Integer spaceUid, String spaceUname, User viewer) throws FeedException {
		Document dom = getFeedDom(spaceUid, spaceUname, viewer,false);
		
		//out put to XML feed string
		try {
			
			StringWriter output = new StringWriter();
			Result result = new StreamResult(output);
			Transformer xout = TransformerFactory.newInstance().newTransformer();
			xout.transform(new DOMSource(dom), result);
			
			return output.toString();
		} catch (Exception e) {
			log.error("Failed to write feed for " + spaceUname,e);
			throw new FeedException("Failed to write feed for " + spaceUname, e);
		}
		
		
	}

	
	public List<Page> getPagesFromFeed(Integer spaceUid, String spaceUname, User viewer) throws FeedException {
		return getPagesFromFeed(spaceUid, spaceUname, viewer, false);
		
	}
	public void cleanAllRss() {
		try {
			FileUtils.cleanDirectory(rssRoot.getFile());
		} catch (IOException e) {
			log.error("Unable to clean RSS directory", e);
		}
	}

	public void afterPropertiesSet() throws Exception {
		if(rssRoot == null)
			throw new BeanInitializationException("Must set rssRoot for RSS feed root directory.");
		if(!rssRoot.getFile().exists()){
			if(!rssRoot.getFile().mkdirs()){
				throw new BeanInitializationException("Unable to create RSS feed root directory.");
			}
		}
	}
	
	//********************************************************************
	//               private method
	//********************************************************************
	private List<Page> getPagesFromFeed(Integer spaceUid, String spaceUname,User viewer, boolean skipSecurityCheck) throws FeedException {
		Document dom = getFeedDom(spaceUid, spaceUname, viewer,skipSecurityCheck );
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(dom);
		List<Page> pageList = new ArrayList<Page>();
		List<SyndEntry> entries = feed.getEntries();
		//common user for all pages which are all inside this space
		Space space = new Space();
		space.setUid(spaceUid);
		space.setUnixName(spaceUname);
		if(entries != null){
			for (SyndEntry entry : entries) {
				String title = entry.getTitle();
				PageRSSModule module = (PageRSSModule) entry.getModule(PageRSSModule.URI);
				if(module != null){
					Page page = new Page();
					page.setTitle(title);
					page.setPageUuid(module.getPageUuid());
					page.setSpace(space);
					page.setModifiedDate(module.getModifiedDate());
					page.setCreatedDate(module.getCreateDate());
					page.setVersion(module.getVersion());
					page.setCreator(userReadingService.getUserByName(module.getCreator()));
					page.setModifier(userReadingService.getUserByName(module.getModifier()));
					//so far, RSS only contains creator/modifier full name, for security reason, does not expose user login name and email
					//so, if need further user info, it need set user Uid or Email to RSS feed then read out User info from DB here.
					pageList.add(page);
				}
			}
		}
		
		log.info("From RSS feed of Space " + spaceUname + " gets page item count " + pageList.size());
		return pageList;
	}
	/**
	 * @param spaceUid
	 * @param spaceUname
	 * @param viewer
	 * @param skipSecurityCheck 
	 * @return
	 * @throws FeedException
	 */
	private Document getFeedDom(Integer spaceUid, String spaceUname, User viewer, boolean skipSecurityCheck) throws FeedException {
		ReentrantReadWriteLock lock = null;
		Document dom;
		try {
			
			File feedFile = new File(FileUtil.getFullPath(rssRoot.getFile().getAbsolutePath(),spaceUid+".xml"));
			if(!feedFile.exists()){
				createFeed(spaceUname);
			}
			
			//do read lock! must after createFeed possibility, otherwise, deadlock 
			lock = lockMap.get(spaceUid.toString());
			if(lock == null){
				lock = new ReentrantReadWriteLock();
				lockMap.put(spaceUid.toString(), lock);
			}
			lock.readLock().lock();
			//!!! DON'T USE JDOM - although I test DOM, JDOM, DOM4J: JDOM is fastest. And DOM and DOM4J almost 2 time slow. But
			//!!! JDOM is not thread safe, an infinite looping can happen while this method reading different XML source, 
			//in different thread, and SAXBuild are different instance! The problem is mostly caused by NameSpace.getNamespace(), 
			//which using static HashMap and cause HashMap dead lock!!!
			DocumentBuilder builder = xmlBuilderPool.poll();
			if (builder == null) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setCoalescing(true);
				factory.setIgnoringComments(true);
				builder = factory.newDocumentBuilder();
			}
			dom = builder.parse(feedFile);
			xmlBuilderPool.add(builder);

		} catch (Exception e) {
			log.error("Unable get feed " +spaceUname + " with excpetion ",  e);
			throw new FeedException("Unable get feed " +spaceUname + " with excpetion "+ e);
		} finally{
			if(lock != null) lock.readLock().unlock();
		}
		if(dom == null){
			log.error("Unable get feed " +spaceUname);
			throw new FeedException("Unable get feed " + spaceUname);
		}
		
		//~~~~~~~~~~~~ Security filter
		if(!skipSecurityCheck){
			//need filter out the page that viewer has not permission to read.  
			List<Node> forbidPageUuidList = new ArrayList<Node>();
			String pageUuid;
			Node ele;
			NodeList list = dom.getElementsByTagName(PageRSSModule.NS_PREFIX+":"+PageRSSModule.PAGE_UUID);
			int len = list.getLength();
			for(int idx=0;idx<len;idx++){
				ele = list.item(idx);
				pageUuid =  ele.getTextContent();
				if(!securityService.isAllowPageReading(spaceUname, pageUuid, viewer)){
					log.info("User " + (viewer==null?"anonymous":viewer.getUsername()) + "  has not reading permission for pageUuid " 
							+ pageUuid + " on space " + spaceUname + ". Feed item of this page is removed from RSS output.");
					forbidPageUuidList.add(ele.getParentNode());
				}
				
			}
			if(forbidPageUuidList.size() > 0){
				NodeList cl = dom.getElementsByTagName(PageRSSModule.CHANNEL);
				if(cl.getLength() > 0){
					//only one channel tag!
					Node channel = cl.item(0);
					for (Node element : forbidPageUuidList) {
						channel.removeChild(element);
					}
				}
			}
		}		
		return dom;
	}

	//********************************************************************
	//               set / get
	//********************************************************************
	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}

	public void setRssRoot(Resource rssRoot) {
		this.rssRoot = rssRoot;
	}

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public void setRenderService(RenderService exportService) {
		this.renderService = exportService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setThemeService(ThemeService themeService) {
		this.themeService = themeService;
	}

		 
}
