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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.dao.CrFileNodeDAO;
import com.edgenius.core.dao.RoleDAO;
import com.edgenius.core.dao.UserDAO;
import com.edgenius.core.model.CrFileNode;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.dao.CommentDAO;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.dao.PageTagDAO;
import com.edgenius.wiki.dao.SpaceDAO;
import com.edgenius.wiki.dao.SpaceTagDAO;
import com.edgenius.wiki.dao.WidgetDAO;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.PageTag;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.SpaceTag;
import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.quartz.MaintainJobInvoker;
import com.edgenius.wiki.quartz.QuartzException;
import com.edgenius.wiki.search.lucene.IndexCallback;
import com.edgenius.wiki.search.lucene.IndexFactory;
import com.edgenius.wiki.search.lucene.IndexWriterTemplate;
import com.edgenius.wiki.search.lucene.SimpleIndexFactory;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.ThemeService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */

@Transactional(readOnly=true)
public class IndexServiceImpl implements IndexService, InitializingBean {
	private static final Logger log = LoggerFactory.getLogger(IndexServiceImpl.class);
	
	private IndexWriterTemplate pageTemplate;
	private IndexWriterTemplate commentTemplate;
	private IndexWriterTemplate spaceTemplate;
	private IndexWriterTemplate userTemplate;
	private IndexWriterTemplate roleTemplate;
	private IndexWriterTemplate pageTagTemplate;
	private IndexWriterTemplate spaceTagTemplate;
	private IndexWriterTemplate attachmentTemplate;
	private IndexWriterTemplate widgetTemplate;
	
	private ReentrantLock pageLock = new ReentrantLock();
	private ReentrantLock commentLock = new ReentrantLock();
	private ReentrantLock spaceLock = new ReentrantLock();
	private ReentrantLock userLock = new ReentrantLock();
	private ReentrantLock roleLock = new ReentrantLock();
	private ReentrantLock pageTagLock = new ReentrantLock();
	private ReentrantLock spaceTagLock = new ReentrantLock();
	private ReentrantLock attachmentLock = new ReentrantLock();
	private ReentrantLock widgetLock = new ReentrantLock();
	
	private Resource indexRoot;
	
	private PageDAO pageDAO;
	private CommentDAO commentDAO;
	private SpaceDAO spaceDAO;
	private UserDAO userDAO;
	private RoleDAO roleDAO;
	private PageTagDAO pageTagDAO;
	private SpaceTagDAO spaceTagDAO;
	private CrFileNodeDAO crFileNodeDAO;
	private WidgetDAO widgetDAO;
	
	private RenderService renderService;
	private ThemeService themeService;
	
	private AttachmentSearchService attachmentSearchService;
	private TextExtractorService textExtractorService; 

	private MaintainJobInvoker maintainJobInvoker;
	
	//JDK1.6 @Override
	public void initOptimizeJob(){
		try {
			maintainJobInvoker.invokeJob();
		} catch (QuartzException e) {
			log.error("Unable start schedule job for index optimize.",e);
		}
	}
	//JDK1.6 @Override
	public void saveOrUpdatePage(final Page page){
		try{
			pageLock.lock();
			if(WikiUtil.hasBlogRender(page, themeService)){
				log.info("Page has blog macro, skip indexing:" + page.getTitle());
				return;
			}
			log.info("Index will create for page, title is " + page.getTitle());
			final Term identifierTerm = new Term(FieldName.KEY,page.getPageUuid().toLowerCase());
			saveUpdate(pageTemplate,  createPageDocument(page), identifierTerm);
			log.info("Index create for page, title is " + page.getTitle());
		}finally{
			pageLock.unlock();
		}
	}

	//JDK1.6 @Override
	public void saveOrUpdateComment(PageComment comment) {
		try{
			commentLock.lock();
			log.info("Index will create for comment, page uuid is " + comment.getPage().getPageUuid());
			final Term identifierTerm = new Term(FieldName.KEY,comment.getUid().toString());
			saveUpdate(commentTemplate,  createCommentDocument(comment), identifierTerm);
			log.info("Index create for comment");
		}finally{
			commentLock.unlock();
		}
	}

	//JDK1.6 @Override
	public void saveOrUpdateSpace(Space space) {
		try{
			spaceLock.lock();
			log.info("Index will create for space Uname: " + space.getUnixName());
			final Term identifierTerm = new Term(FieldName.KEY,space.getUnixName().toLowerCase());
			saveUpdate(spaceTemplate, createSpaceDocument(space), identifierTerm);
			log.info("Index create for space Uname: " + space.getUnixName());
		}finally{
			spaceLock.unlock();
		}
		
	}
	//JDK1.6 @Override
	public void saveOrUpdateWidget(Widget widget) {
		try{
			widgetLock.lock();
			log.info(new StringBuilder("Index will create for widget: ").append(widget.getType())
					.append(":").append(widget.getDescription()).toString());
			
			final Term identifierTerm = new Term(FieldName.KEY,widget.getUuid().toLowerCase());
			saveUpdate(widgetTemplate, createWidgetDocument(widget), identifierTerm);
			
			log.info(new StringBuilder("Index created for widget: ").append(widget.getType())
					.append(":").append(widget.getDescription()).toString());
		}finally{
			widgetLock.unlock();
		}
		
	}
	
	//JDK1.6 @Override
	public void saveOrUpdatePageTag(PageTag tag) {
		try{
			pageTagLock.lock();
			log.info("Index will create for Page Tag: " + tag.getName());
			final Term identifierTerm = new Term(FieldName.KEY,tag.getName().toLowerCase());
			saveUpdate(pageTagTemplate, createPageTagDocument(tag), identifierTerm);
		}finally{
			pageTagLock.unlock();
		}
	}
	//JDK1.6 @Override
	public void saveOrUpdateSpaceTag(SpaceTag tag) {
		try{
			spaceTagLock.lock();
			log.info("Index will create for space tag : " + tag.getName());
			final Term identifierTerm = new Term(FieldName.KEY,tag.getName().toLowerCase());
			saveUpdate(spaceTagTemplate, createSpaceTagDocument(tag), identifierTerm);
			log.info("Index created for space tag : " + tag.getName());
		}finally{
			spaceTagLock.unlock();
		}
	}
	//JDK1.6 @Override
	public void saveOrUpdateUser(User user) {
		try{
			userLock.lock();
			log.info("Index will create for user: " + user.getUsername());
			final Term identifierTerm = new Term(FieldName.KEY,user.getUsername().toLowerCase());
			saveUpdate(userTemplate, createUserDocument(user), identifierTerm);
			log.info("Index created for user: " + user.getUsername());
		}finally{
			userLock.unlock();
		}
		
	}
	
	public void saveOrUpdateRole(Role role) {
		try{
			roleLock.lock();
			log.info("Index will create for role: " + role.getName());
			final Term identifierTerm = new Term(FieldName.KEY,role.getName().toLowerCase());
			saveUpdate(roleTemplate, createRoleDocument(role), identifierTerm);
			log.info("Index created for role: " + role.getName());
		}finally{
			roleLock.unlock();
		}
	}

	//JDK1.6 @Override
	public void saveOrUpdateAttachment(final String spaceUname, final FileNode node, boolean keepFileContent) {
		try{
			attachmentLock.lock();
			log.info("Index will create for attachment: " + node.getFilename());
			final Term identifierTerm = new Term(FieldName.KEY,node.getNodeUuid().toLowerCase());
			Document doc = null;
			try {
				doc = attachmentSearchService.searchByNodeUuid(node.getNodeUuid());
			} catch (SearchException e) {
				log.info("Attachment does not exist then create new one:" + node.getNodeUuid());
			}
			if(doc != null){
				String text = null;
				if(keepFileContent)
					text = doc.get(FieldName.TEXT);
				doc = createAttachmentDocument(spaceUname, node, text);
			}else{
				doc = createAttachmentDocument(spaceUname, node, null);
			}
			saveUpdate(attachmentTemplate, doc, identifierTerm);
			log.info("Index created for attachment: " + node.getFilename());
		}finally{
			attachmentLock.unlock();
		}
	}

	//JDK1.6 @Override
	public void removeAttachment(String nodeUuid, String version) {
		try{
			attachmentLock.lock();
			final Term identifierTerm = new Term(FieldName.KEY,nodeUuid.toLowerCase());
			attachmentTemplate.deleteDocuments(identifierTerm);
		}finally{
			attachmentLock.unlock();
		}
	}


	//JDK1.6 @Override
	public void removePage(String removedPageUuid) {
		try{
			pageLock.lock();
			final Term identifierTerm = new Term(FieldName.KEY,removedPageUuid.toLowerCase());
			pageTemplate.deleteDocuments(identifierTerm);
		}finally{
			pageLock.unlock();
		}
		
	}
	//JDK1.6 @Override
	public void removeComment(Integer commentUid) {
		try{
			commentLock.lock();
			final Term identifierTerm = new Term(FieldName.KEY,commentUid.toString());
			commentTemplate.deleteDocuments(identifierTerm);
		}finally{
			commentLock.unlock();
		}
		
	}
	//JDK1.6 @Override
	public void removePageTag(String tag) {
		try{
			pageTagLock.lock();
			final Term identifierTerm = new Term(FieldName.KEY,tag.toLowerCase());
			pageTagTemplate.deleteDocuments(identifierTerm);
		}finally{
			pageTagLock.unlock();
		}
		
	}
	//JDK1.6 @Override
	public void removeSpace(String spaceUname) {
		try{
			spaceLock.lock();
			final Term identifierTerm = new Term(FieldName.KEY,spaceUname.toLowerCase());
			spaceTemplate.deleteDocuments(identifierTerm);
		}finally{
			spaceLock.unlock();
		}
		
	}
	//JDK1.6 @Override
	public void removeWidget(String widgetKey) {
		try{
			widgetLock.lock();
			final Term identifierTerm = new Term(FieldName.KEY,widgetKey.toLowerCase());
			widgetTemplate.deleteDocuments(identifierTerm);
		}finally{
			widgetLock.unlock();
		}
		
	}
	//JDK1.6 @Override
	public void removeSpaceTag(String tag) {
		try{
			spaceTagLock.lock();
			final Term identifierTerm = new Term(FieldName.KEY,tag.toLowerCase());
			spaceTagTemplate.deleteDocuments(identifierTerm);
		}finally{
			spaceTagLock.unlock();
		}
		
	}
	//JDK1.6 @Override
	public void removeUser(String username) {
		try{
			userLock.lock();
			final Term identifierTerm = new Term(FieldName.KEY,username.toLowerCase());
			userTemplate.deleteDocuments(identifierTerm);
		}finally{
			userLock.unlock();
		}
	}
	
	//removeRole() - no such function yet
	
	//JDK1.6 @Override
	public void optimize() {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Page
		new Runnable(){
			public void run() {
				pageLock.lock();
				try{
					pageTemplate.optimize();
					log.info("Page index is optimized");
				}finally{
					pageLock.unlock();
				}
			}
		}.run();
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Page comment
		new Runnable(){
			public void run() {
				commentLock.lock();
				try{
					commentTemplate.optimize();
					log.info("Page comment index is optimized");
				}finally{
					commentLock.unlock();
				}
			}
		}.run();
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// space
		new Runnable(){
			public void run() {
				spaceLock.lock();
				try{
					spaceTemplate.optimize();
					log.info("Space index is optimized");
				}finally{
					spaceLock.unlock();
				}
			}
		}.run();
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// PageTag
		new Runnable(){
			public void run() {
				pageTagLock.lock();
				try{
					pageTagTemplate.optimize();
					log.info("Page Tag index is optimized");
				}finally{
					pageTagLock.unlock();
				}
			}
		}.run();
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// SpaceTag
		new Runnable(){
			public void run() {
				spaceTagLock.lock();
				try{
					spaceTagTemplate.optimize();
					log.info("Space Tag index is optimized");
				}finally{
					spaceTagLock.unlock();
				}
			}
		}.run();
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// User
		new Runnable(){
			public void run() {
				userLock.lock();
				try{
					userTemplate.optimize();
					log.info("User index is optimized");
				}finally{
					userLock.unlock();
				}
			}
		}.run();
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Role
		new Runnable(){
			public void run() {
				roleLock.lock();
				try{
					roleTemplate.optimize();
					log.info("Role index is optimized");
				}finally{
					roleLock.unlock();
				}
			}
		}.run();
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Attachment
		new Runnable(){
			public void run() {
				attachmentLock.lock();
				try{
					attachmentTemplate.optimize();
					log.info("Attachment index is optimized");
				}finally{
					attachmentLock.unlock();
				}
			}
		}.run();
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Widget
		new Runnable(){
			public void run() {
				widgetLock.lock();
				try{
					widgetTemplate.optimize();
					log.info("Widgets index is optimized");
				}finally{
					widgetLock.unlock();
				}
			}
		}.run();

		
	}
	
	//JDK1.6 @Override
	public void cleanIndexes(final IndexRebuildListener listener) {
		//code move to AdvanceAdminAction.rebuild(), may move back until I find solution for lazy loading for each rebuild*()
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Clean all sub directories(first level) under index root, but not delete directory itself.
		try {
			File[] list = indexRoot.getFile().listFiles(new FileFilter(){
				public boolean accept(File pathname) {
					return pathname.isDirectory()?true:false;
				}
			});
			for (File file : list) {
				try {
				FileUtils.cleanDirectory(file);
				} catch (IOException e) {
					log.error("Unable to clean index root directory:" + indexRoot.getFilename(), e);
				}
			}
		} catch (IOException e1) {
			log.error("Unable to list index root directory", e1);
		}
		
		closeIndex();
		
	}
	private void closeIndex() {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// initialize indexes
		pageTemplate.close();
		commentTemplate.close();
		spaceTemplate.close();
		userTemplate.close();
		roleTemplate.close();
		pageTagTemplate.close();
		spaceTagTemplate.close();
		attachmentTemplate.close();
		widgetTemplate.close();
		
	}

	/**
	 * 
	 */
	public void rebuildAttachmentIndex() {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// attachment
		final List<CrFileNode> attachments = crFileNodeDAO.getAllCurrentNode();
		if(attachments != null){
			attachmentLock.lock();
			try{
				attachmentTemplate.addDocument(new IndexCallback() {
					@Override
					public void addDocument(IndexWriter attWriter) {
						for (CrFileNode node : attachments) {
							if(RepositoryService.DEFAULT_SPACE_NAME.equals(node.getSpaceUname())){
								//don't index default space stuff: it is user portrait etc.
								continue;
							}
							try {
								FileNode fNode = FileNode.copyPersistToNode(node);
								attWriter.addDocument(createAttachmentDocument(node.getSpaceUname(), fNode, null));
							} catch (Exception e) {
								log.error("Rebuild index failed on attachment" + node,e);
							}
						}
					}
				});
			}finally{
				attachmentLock.unlock();
			}
		}
	}


	/**
	 * 
	 */
	public void rebuildSpaceTagIndex() {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// SpaceTag
		final List<SpaceTag> spaceTags = spaceTagDAO.getObjects();
		if(spaceTags != null){
			spaceTagLock.lock();
			try {
				spaceTagTemplate.addDocument(new IndexCallback() {
					@Override
					public void addDocument(IndexWriter spaceTagWriter) {
						for (SpaceTag tag : spaceTags) {
							try {
								spaceTagWriter.addDocument(createSpaceTagDocument(tag));
							} catch (Exception e) {
								log.error("Rebuild index failed on space  tag" + tag, e);
							}
						}
					}
				});
			}finally{
				spaceTagLock.unlock();
			}
		}
	}


	/**
	 * 
	 */
	public void rebuildPageTagIndex() {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// PageTag
		final List<PageTag> pageTags = pageTagDAO.getObjects();
		if(pageTags != null){
			pageTagLock.lock();
			try{
				pageTagTemplate.addDocument(new IndexCallback() {
					
					@Override
					public void addDocument(IndexWriter pageTagWriter) {
						for (PageTag tag : pageTags) {
							try {
								pageTagWriter.addDocument(createPageTagDocument(tag));
							} catch (Exception e) {
								log.error("Rebuild index failed on page tag" + tag, e);
							}
						}
					}
				});
			}finally{
				pageTagLock.unlock();
			}
		}
	}


	/**
	 * 
	 */
	public void rebuildUserIndex() {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// User
		final List<User> users = userDAO.getObjects();
		if(users != null){
			userLock.lock();
			try{
				userTemplate.addDocument(new IndexCallback() {
					
					@Override
					public void addDocument(IndexWriter userWriter) {
						for (User user : users) {
							try {
								userWriter.addDocument(createUserDocument(user));
							} catch (Exception e) {
								log.error("Rebuild index failed on user" + user, e);
							}
						}
						
					}
				});
			}finally{
				userLock.unlock();
			}
		}
	}
	public void rebuildRoleIndex() {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Role - only group type role is indexed
		final List<Role> roles = roleDAO.getRoles(Role.TYPE_GROUP,null);
		if(roles != null){
			roleLock.lock();
			try{
				roleTemplate.addDocument(new IndexCallback() {
					
					@Override
					public void addDocument(IndexWriter roleWriter) {
						for (Role role : roles) {
							try {
								roleWriter.addDocument(createRoleDocument(role));
							} catch (Exception e) {
								log.error("Rebuild index failed on role" + role, e);
							}
						}
					}
				});
			}finally{
				roleLock.unlock();
			}
		}
	}


	/**
	 * 
	 */
	public void rebuildSpaceIndex() {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Space
		final List<Space> spaces = spaceDAO.getObjects();
		if(spaces != null){
			spaceLock.lock();
			try {
				spaceTemplate.addDocument(new IndexCallback() {
					@Override
					public void addDocument(IndexWriter spaceWriter) {
						for (Space space : spaces) {
							try {
								//skip system space 
								if(StringUtils.equalsIgnoreCase(SharedConstants.SYSTEM_SPACEUNAME, space.getUnixName()))
									continue;
								spaceWriter.addDocument(createSpaceDocument(space));
							} catch (Exception e) {
								log.error("Rebuild space index failed " + space, e);
							}
						}
					}
				});
			}finally{
				spaceLock.unlock();
			}
		}
	}
	/**
	 * 
	 */
	public void rebuildWidgetIndex() {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Space
		final List<Widget> widgets = widgetDAO.getObjects();
		if(widgets != null){
			widgetLock.lock();
			try {
				widgetTemplate.addDocument(new IndexCallback() {
					@Override
					public void addDocument(IndexWriter widgetWriter) {
						for (Widget widget : widgets) {
							try {
								widgetWriter.addDocument(createWidgetDocument(widget));
							} catch (Exception e) {
								log.error("Rebuild widget index failed " + widget, e);
							}
						}
					}
				});
			}finally{
				widgetLock.unlock();
			}
		}
	}


	/**
	 * 
	 */
	public void rebuildCommentIndex() {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// page comment
		final List<PageComment> comments = commentDAO.getObjects();
		IndexWriter commentWriter = null;
		if(comments != null){
			commentLock.lock();
			try {
				commentTemplate.addDocument(new IndexCallback() {
					
					@Override
					public void addDocument(IndexWriter commentWriter) {
						
						for (PageComment comment : comments) {
							try{
								commentWriter.addDocument(createCommentDocument(comment));
							} catch (Exception e) {
								log.error("Rebuild index failed on comment. Owner page title " + comment.getPage().getTitle(),e);
							}
						}
					}
				});
			}finally{
				try {
					if(commentWriter != null) commentWriter.close();
				} catch (Exception e) {
					log.error("Close comment index failed " , e);
				}
				commentLock.unlock();
			}
		}
	}


	/**
	 * 
	 */
	public void rebuildPageIndex() {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// page
		
		//get how many page in whole system, then decide if use optimised way to build indexing
		final long size = pageDAO.getSystemPageCount();
		if(size > 0){
			pageLock.lock();
			try{
				pageTemplate.addDocument(new IndexCallback() {
					
					@Override
					public void addDocument(IndexWriter pageWriter) {
						//it is memory killer if using pageDAO.getObjects(); Now read page by native SQL and initialise object manually.
						//Current page returns:
						//PageTitle, PageUUID, page.getContent().getContent(), 
						//page.getSpace().getUnixName(), 
						//page.getSpace().getHomepage().getPageUuid()
						//page.getSpace().getUid()
						//page.getSpace().getSetting()
						final int returnNum = 500; 
						int start = 0;
						int indexedSize = 0, skipped=0;
						Map<Integer, Space> spaceCache = new HashMap<Integer, Space>();
						
						//get pages by returnNum count size list and looping until all pages done
						do{
							List<Page> pages = pageDAO.getPageForIndexing(start, returnNum);
							if(pages == null){
								AuditLogger.error("PageDAO get null from PageForIndex():start" + start);
								break;
							}
							
							//insert space information - the basic assumption is, space is less. So I cache all spaces into a Hashmap.
							for (Page page : pages) {
								//current page only has spaceUid value
								Space space = spaceCache.get(page.getSpace().getUid());
								if(space == null){
									space = spaceDAO.get(page.getSpace().getUid());
			
									if(space == null){
										AuditLogger.error("Page get null space by spaceUid:" + page.getSpace().getUid());
										continue;
									}
	
									spaceCache.put(space.getUid(), space);
								}
								page.setSpace(space);
				
							}
							
							//index current page list
							for (Page page : pages) {
	
								try{
									if(WikiUtil.hasBlogRender(page, themeService)){
										log.info("Page has blog macro, skip indexing:" + page.getTitle());
										skipped++;
										continue;
									}
									log.debug("Page index rebuilding:" + page.getTitle());
									
									//skip system space pages
									if(StringUtils.equalsIgnoreCase(SharedConstants.SYSTEM_SPACEUNAME, page.getSpace() != null? page.getSpace().getUnixName():null))
										continue;
									pageWriter.addDocument(createPageDocument(page));
									indexedSize++;
								} catch (Exception e) {
									log.error("Rebuild page index failed " + page, e);
								}
							}
	
							//ready for next bundle
							start += returnNum;
						}while(start < size);
						
						log.info(new StringBuilder("Page index rebuild. Expected: ").append(size).append(": Actual indexed:").append(indexedSize)
								.append(": Normal skipped:").append(skipped).append(": Failed ").append((size-indexedSize-skipped)).toString());
						
					}
				});
				
			}finally{
				
				pageLock.unlock();	
				
				log.info("Page index is rebuilt");
			}
			
		}
		
	}



	public void afterPropertiesSet() throws Exception {
		if(indexRoot == null || (indexRoot.getFile().exists() && !indexRoot.getFile().isDirectory())){
			throw new BeanInitializationException("Must set indexRoot and it must be directory");
		}
			
		if(this.pageTemplate == null || this.commentTemplate == null || this.spaceTemplate == null
			|| this.pageTagTemplate == null || this.spaceTagTemplate == null || this.userTemplate == null
			|| this.roleTemplate == null || this.attachmentTemplate == null || this.widgetTemplate == null){
			throw new BeanInitializationException("Must set all templates: pageTemplate, commentTemplate, spaceTemplate == null" +
			", pageTagTemplate, spaceTagTemplate, userTemplate" +
			", roleTemplate, attachmentTemplate, widgetTemplate");
		}
		
	}
	
	//********************************************************************
	//               private method
	//********************************************************************
	private void saveUpdate(IndexWriterTemplate template, final Document doc, final Term identifierTerm) {
		//so ugly code, does it can be a good way saveUpdate?
		try {
			template.deleteDocuments(identifierTerm);
		} catch (Exception e) {
			log.info("Remove index failed " + e);
		}

		template.addDocument(doc);
	}
	

	private Document createPageDocument(final Page page) {
		Document doc = new Document();
		Fieldable type = new Field(FieldName.DOC_TYPE, Integer.valueOf(SharedConstants.SEARCH_PAGE).toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable date = new Field(FieldName.UPDATE_DATE, page.getModifiedDate().getTime()+"", Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable suname = new Field(FieldName.UNSEARCH_SPACE_UNIXNAME, page.getSpace().getUnixName(), Field.Store.YES, Field.Index.ANALYZED);
		Fieldable contributor = new Field(FieldName.CONTRIBUTOR, getUsername(page.getModifier()), Field.Store.YES, Field.Index.ANALYZED);
		//deleting key - must be indexed
		Fieldable key = new Field(FieldName.KEY, page.getPageUuid(), Field.Store.NO, Field.Index.ANALYZED);
		
		//convert render pieces to pure text and indexed
		String pureText = renderService.renderPureText(page);
		
		Fieldable uuid = new Field(FieldName.PAGE_UUID, page.getPageUuid(), Field.Store.YES, Field.Index.NO);
		Fieldable title = new Field(FieldName.PAGE_TITLE, page.getTitle(), Field.Store.YES, Field.Index.NO);
		Fieldable pcontent = new Field(FieldName.PAGE_CONTENT, pureText, Field.Store.YES, Field.Index.NO);
		
		//combined index field - no store
		Fieldable content = new Field(FieldName.CONTENT, StringUtil.join(" ",page.getTitle(), pureText), Field.Store.NO, Field.Index.ANALYZED);
		
		
		doc.add(key);
		doc.add(contributor);
		doc.add(type);
		doc.add(uuid);
		doc.add(title);
		doc.add(pcontent);
		doc.add(content);
		doc.add(suname);
		doc.add(date);
		return doc;
	}


	private Document createCommentDocument(PageComment comment) {
		Document doc = new Document();
		Fieldable type = new Field(FieldName.DOC_TYPE, Integer.valueOf(SharedConstants.SEARCH_COMMENT).toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable unixName = new Field(FieldName.UNSEARCH_SPACE_UNIXNAME, comment.getPage().getSpace().getUnixName(), Field.Store.YES, Field.Index.ANALYZED);
		Fieldable contributor = new Field(FieldName.CONTRIBUTOR, getUsername(comment.getCreator()), Field.Store.YES, Field.Index.ANALYZED);
		Fieldable date = new Field(FieldName.UPDATE_DATE, comment.getCreatedDate().getTime()+"", Field.Store.YES, Field.Index.NOT_ANALYZED);

		//deleting key - must be indexed
		Fieldable key = new Field(FieldName.KEY, comment.getUid().toString(), Field.Store.NO, Field.Index.ANALYZED);
		
		Fieldable cuid = new Field(FieldName.COMMENT_UID, comment.getUid().toString(), Field.Store.YES, Field.Index.NO);
		Fieldable puuid = new Field(FieldName.PAGE_UUID, comment.getPage().getPageUuid(), Field.Store.YES, Field.Index.NO);
		Fieldable ptitle = new Field(FieldName.UNSEARCH_PAGE_TITLE, comment.getPage().getTitle(), Field.Store.YES, Field.Index.NO);
		
		//store it for highlight purpose
		Fieldable content = new Field(FieldName.CONTENT, comment.getBody(), Field.Store.YES, Field.Index.ANALYZED);
		
		doc.add(key);
		doc.add(contributor);
		doc.add(type);
		doc.add(unixName);
		doc.add(puuid);
		doc.add(ptitle);
		doc.add(cuid);
		doc.add(content);
		doc.add(date);
		return doc;
	}
	private Document createSpaceDocument(Space space) {
		Document doc = new Document();
		Fieldable type = new Field(FieldName.DOC_TYPE, Integer.valueOf(SharedConstants.SEARCH_SPACE).toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable date = new Field(FieldName.UPDATE_DATE, space.getCreatedDate().getTime()+"", Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable contributor = new Field(FieldName.CONTRIBUTOR, getUsername(space.getCreator()), Field.Store.YES, Field.Index.ANALYZED);
		//spaceUname as key when deleting - must be indexed.
		Fieldable key = new Field(FieldName.KEY, space.getUnixName(), Field.Store.NO, Field.Index.ANALYZED);
		
		
		Fieldable unixName = new Field(FieldName.SPACE_UNIXNAME, space.getUnixName(), Field.Store.YES, Field.Index.NO);
		Fieldable title = new Field(FieldName.SPACE_NAME, space.getName(), Field.Store.YES, Field.Index.NO);
		Fieldable scontent = new Field(FieldName.SPACE_DESC, space.getDescription(), Field.Store.YES, Field.Index.NO);

		Fieldable content = new Field(FieldName.CONTENT, StringUtil.join(" ",space.getUnixName(),space.getName(), space.getDescription())
				, Field.Store.YES, Field.Index.ANALYZED);

		doc.add(key);
		doc.add(contributor);
		doc.add(type);
		doc.add(date);
		doc.add(unixName);
		doc.add(title);
		doc.add(scontent);
		doc.add(content);
		return doc;
	}
	private Document createWidgetDocument(Widget widget) {
		Document doc = new Document();
		Fieldable type = new Field(FieldName.DOC_TYPE, Integer.valueOf(SharedConstants.SEARCH_WIDGET).toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable date = new Field(FieldName.UPDATE_DATE, widget.getCreatedDate().getTime()+"", Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable contributor = new Field(FieldName.CONTRIBUTOR, getUsername(widget.getCreator()), Field.Store.YES, Field.Index.ANALYZED);
		
		//deleting key - must be indexed
		Fieldable key = new Field(FieldName.KEY, widget.getUuid(), Field.Store.NO, Field.Index.ANALYZED);
		
		Fieldable wType = new Field(FieldName.WIDGET_TYPE, widget.getType(), Field.Store.YES, Field.Index.NO);
		Fieldable wKey = new Field(FieldName.WIDGET_KEY, widget.getUuid(), Field.Store.YES, Field.Index.NO);
		Fieldable title = new Field(FieldName.WIDGET_TITLE, StringUtils.trimToEmpty(widget.getTitle()), Field.Store.YES, Field.Index.NO);
		//content, title already ensure not blank on client side, description is optional but must not null so that search could works
		Fieldable desc = new Field(FieldName.WIDGET_DESC, StringUtils.trimToEmpty(widget.getDescription()), Field.Store.YES, Field.Index.NO);
		Fieldable wcontent = new Field(FieldName.WIDGET_CONTENT, widget.getContent(), Field.Store.YES, Field.Index.NO);
		
		Fieldable content = new Field(FieldName.CONTENT,  StringUtil.join(" ",widget.getTitle(),widget.getDescription(), widget.getContent())
				, Field.Store.NO, Field.Index.ANALYZED);

		
		doc.add(key);
		doc.add(contributor);
		doc.add(type);
		doc.add(wType);
		doc.add(wKey);
		doc.add(title);
		doc.add(desc);
		doc.add(wcontent);
		doc.add(content);
		doc.add(date);
		return doc;
	}
	private Document createPageTagDocument(PageTag tag) {
		Document doc = new Document();
		Fieldable type = new Field(FieldName.DOC_TYPE, Integer.valueOf(SharedConstants.SEARCH_PAGE_TAG).toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable date = new Field(FieldName.UPDATE_DATE, tag.getCreatedDate().getTime()+"", Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable unixName = new Field(FieldName.UNSEARCH_SPACE_UNIXNAME, tag.getSpace().getUnixName(), Field.Store.YES, Field.Index.ANALYZED);
		Fieldable contributor = new Field(FieldName.CONTRIBUTOR, getUsername(tag.getCreator()), Field.Store.YES, Field.Index.ANALYZED);
		
		Fieldable key = new Field(FieldName.KEY, tag.getName(), Field.Store.NO, Field.Index.ANALYZED);

		Fieldable title = new Field(FieldName.PAGE_TAG_NAME, tag.getName(), Field.Store.YES, Field.Index.NO);
		Fieldable content = new Field(FieldName.CONTENT, tag.getName(), Field.Store.NO, Field.Index.ANALYZED);

		doc.add(key);
		doc.add(contributor);
		doc.add(type);
		doc.add(unixName);
		doc.add(title);
		doc.add(content);
		doc.add(date);
		return doc;
	}
	private Document createSpaceTagDocument(SpaceTag tag) {
		Document doc = new Document();
		Fieldable type = new Field(FieldName.DOC_TYPE, Integer.valueOf(SharedConstants.SEARCH_SPACE_TAG).toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable date = new Field(FieldName.UPDATE_DATE, tag.getCreatedDate().getTime()+"", Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable contributor = new Field(FieldName.CONTRIBUTOR, getUsername(tag.getCreator()), Field.Store.YES, Field.Index.ANALYZED);
		
		Fieldable key = new Field(FieldName.KEY, tag.getName(), Field.Store.NO, Field.Index.ANALYZED);
		
		Fieldable title = new Field(FieldName.SPACE_TAG_NAME, tag.getName(), Field.Store.YES, Field.Index.NO);
		Fieldable content = new Field(FieldName.CONTENT, tag.getName(), Field.Store.NO, Field.Index.ANALYZED);

		doc.add(key);
		doc.add(contributor);
		doc.add(type);
		doc.add(title);
		doc.add(content);
		doc.add(date);
		return doc;
	}
	private Document createUserDocument(User user) {
		Document doc = new Document();
		Fieldable type = new Field(FieldName.DOC_TYPE, Integer.valueOf(SharedConstants.SEARCH_USER).toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable date = new Field(FieldName.UPDATE_DATE, user.getCreatedDate().getTime()+"", Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable contributor = new Field(FieldName.CONTRIBUTOR, getUsername(user), Field.Store.YES, Field.Index.ANALYZED);
		
		//deleting key - must be indexed
		Fieldable key = new Field(FieldName.KEY, user.getUsername(), Field.Store.NO, Field.Index.ANALYZED);
		
		Fieldable username = new Field(FieldName.USER_NAME, user.getUsername(), Field.Store.YES, Field.Index.NO);
		Fieldable fullname = new Field(FieldName.USER_FULLNAME, user.getFullname(), Field.Store.YES, Field.Index.NO);

		Fieldable content = new Field(FieldName.CONTENT, StringUtil.join(" ",user.getUsername(),user.getFullname())
				, Field.Store.NO, Field.Index.ANALYZED);
		
		doc.add(key);
		doc.add(contributor);
		doc.add(type);
		doc.add(username);
		doc.add(fullname);
		doc.add(content);
		doc.add(date);
		return doc;
	}

	/**
	 * @param role
	 * @return
	 */
	private Document createRoleDocument(Role role) {
		Document doc = new Document();
		Fieldable type = new Field(FieldName.DOC_TYPE, Integer.valueOf(SharedConstants.SEARCH_ROLE).toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
		String createTime = role.getCreatedDate() != null? role.getCreatedDate().getTime()+"":"";
		Fieldable date = new Field(FieldName.UPDATE_DATE,createTime , Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable contributor = new Field(FieldName.CONTRIBUTOR, getUsername(role.getCreator()), Field.Store.YES, Field.Index.ANALYZED);
		
		//deleting key - must be indexed
		Fieldable key = new Field(FieldName.KEY, role.getName(), Field.Store.NO, Field.Index.ANALYZED);
		
		Fieldable name = new Field(FieldName.ROLE_NAME, role.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable displayName = new Field(FieldName.ROLE_DISPLAY_NAME, role.getDisplayName(), Field.Store.YES, Field.Index.NO);
		Fieldable desc = new Field(FieldName.ROLE_DESC, role.getDescription(), Field.Store.YES, Field.Index.NO);

		//role name is not index-able
		Fieldable content = new Field(FieldName.CONTENT, StringUtil.join(" ",role.getDisplayName(),role.getDescription())
				, Field.Store.NO, Field.Index.ANALYZED);

		doc.add(key);
		doc.add(contributor);
		doc.add(type);
		doc.add(date);
		doc.add(name);
		doc.add(displayName);
		doc.add(desc);
		doc.add(content);
		return doc;
	}
	private Document createAttachmentDocument(String spaceUname, FileNode node, String origFileContent) {
		Document doc = new Document();
		Fieldable type = new Field(FieldName.DOC_TYPE, Integer.valueOf(SharedConstants.SEARCH_ATTACHMENT).toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable date = new Field(FieldName.UPDATE_DATE, node.getDate()+"", Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable unixName = new Field(FieldName.UNSEARCH_SPACE_UNIXNAME,spaceUname , Field.Store.YES, Field.Index.ANALYZED);
		Fieldable contributor = new Field(FieldName.CONTRIBUTOR, node.getCreateor(), Field.Store.YES, Field.Index.ANALYZED);
		
		//deleting key - must be indexed
		Fieldable key = new Field(FieldName.KEY, node.getNodeUuid(),Field.Store.NO, Field.Index.NOT_ANALYZED);
		
		Fieldable uuid = new Field(FieldName.FILE_NODE_UUID, node.getNodeUuid(),Field.Store.YES, Field.Index.NO);
		Fieldable pageUuid = new Field(FieldName.PAGE_UUID,node.getIdentifier(), Field.Store.YES, Field.Index.NO);
		//if this attachment is shared, this field should be useless, otherwise, it uses on checking search permission on page level.
		Fieldable shared = new Field(FieldName.FILE_SHARED,Boolean.valueOf(node.isShared()).toString(), Field.Store.YES, Field.Index.NO);
		Fieldable filename = new Field(FieldName.FILE_NAME, node.getFilename(), Field.Store.YES, Field.Index.NO);
		Fieldable comment = new Field(FieldName.FILE_COMMENT, node.getComment(), Field.Store.YES, Field.Index.NO);
		
		String extract = origFileContent;
		if(origFileContent == null){
			//TODO: if attachment is big, how to handle memory problem? - I can not use Reader as there 
			//is no way to store them and it is required for highlighter...
			extract = textExtractorService.extractText(spaceUname, node.getNodeUuid(), node.getVersion());
			
		}
		//store for highlight purpose
		Fieldable text = new Field(FieldName.TEXT, StringUtils.trimToEmpty(extract),Field.Store.YES, Field.Index.NOT_ANALYZED);
		Fieldable content = new Field(FieldName.CONTENT, StringUtil.join(" ",node.getFilename(),node.getComment(), extract)
				, Field.Store.NO, Field.Index.ANALYZED);
		
		doc.add(key);
		doc.add(contributor);
		doc.add(type);
		doc.add(date);
		doc.add(unixName);
		
		doc.add(pageUuid);
		doc.add(uuid);
		doc.add(shared);
		doc.add(filename);
		doc.add(comment);
		
		doc.add(text);
		doc.add(content);
		
		return doc;
	}
	private String getUsername(User user) {
		if(user == null)
			return "";
		return user.getUsername();
	}
	// ********************************************************************
	// set/get and inherit method
	// ********************************************************************
	
	public void setAttachmentSearchService(AttachmentSearchService attachmentSearchService) {
		this.attachmentSearchService = attachmentSearchService;
	}

	public void setPageDAO(PageDAO pageDAO) {
		this.pageDAO = pageDAO;
	}

	public void setSpaceDAO(SpaceDAO spaceDAO) {
		this.spaceDAO = spaceDAO;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	/**
	 * @param roleDAO the roleDAO to set
	 */
	public void setRoleDAO(RoleDAO roleDAO) {
		this.roleDAO = roleDAO;
	}
	public void setPageTagDAO(PageTagDAO pageTagDAO) {
		this.pageTagDAO = pageTagDAO;
	}

	public void setSpaceTagDAO(SpaceTagDAO spaceTagDAO) {
		this.spaceTagDAO = spaceTagDAO;
	}

	public void setCrFileNodeDAO(CrFileNodeDAO crFileNodeDAO) {
		this.crFileNodeDAO = crFileNodeDAO;
	}

	public void setCommentDAO(CommentDAO commentDAO) {
		this.commentDAO = commentDAO;
	}


	public void setWidgetDAO(WidgetDAO widgetDAO) {
		this.widgetDAO = widgetDAO;
	}
	public void setIndexRoot(Resource indexRoot) {
		this.indexRoot = indexRoot;
	}
	public void setRenderService(RenderService renderService) {
		this.renderService = renderService;
	}
	public void setTextExtractorService(TextExtractorService textExtractorService) {
		this.textExtractorService = textExtractorService;
	}

	public void setMaintainJobInvoker(MaintainJobInvoker maintainJobInvoker) {
		this.maintainJobInvoker = maintainJobInvoker;
	}
	public void setThemeService(ThemeService themeService) {
		this.themeService = themeService;
	}

	public void setPageTemplate(IndexWriterTemplate pageTemplate) {
		this.pageTemplate = pageTemplate;
	}
	public void setCommentTemplate(IndexWriterTemplate commentTemplate) {
		this.commentTemplate = commentTemplate;
	}
	public void setSpaceTemplate(IndexWriterTemplate spaceTemplate) {
		this.spaceTemplate = spaceTemplate;
	}
	public void setUserTemplate(IndexWriterTemplate userTemplate) {
		this.userTemplate = userTemplate;
	}
	public void setRoleTemplate(IndexWriterTemplate roleTemplate) {
		this.roleTemplate = roleTemplate;
	}
	public void setPageTagTemplate(IndexWriterTemplate pageTagTemplate) {
		this.pageTagTemplate = pageTagTemplate;
	}
	public void setSpaceTagTemplate(IndexWriterTemplate spaceTagTemplate) {
		this.spaceTagTemplate = spaceTagTemplate;
	}
	public void setAttachmentTemplate(IndexWriterTemplate attachmentTemplate) {
		this.attachmentTemplate = attachmentTemplate;
	}
	public void setWidgetTemplate(IndexWriterTemplate widgetTemplate) {
		this.widgetTemplate = widgetTemplate;
	}
}
