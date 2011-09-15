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
package com.edgenius.wiki.search.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.ITicket;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.repository.RepositoryQuotaException;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.repository.RepositoryTiemoutExcetpion;
import com.edgenius.core.util.FileUtil;
import com.edgenius.test.TestDataConstants;
import com.edgenius.test.TestUtil;
import com.edgenius.wiki.gwt.client.model.WidgetModel;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.PageContent;
import com.edgenius.wiki.model.PageTag;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.SpaceTag;
import com.edgenius.wiki.model.Widget;

/**
 * @author Dapeng.Ni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/testAplicationContext-services.xml"
		,"/com/edgenius/wiki/applicationContext-search.xml"
		,"/com/edgenius/core/activemq-dummy-server.xml"
		,"/com/edgenius/core/applicationContext-mail.xml"
		,"/com/edgenius/core/applicationContext-activemq.xml"
		,"/com/edgenius/wiki/applicationContext-service.xml"
		,"/com/edgenius/core/applicationContext-core-orm.xml"
		,"/com/edgenius/wiki/applicationContext-orm.xml"
		,"/com/edgenius/wiki/applicationContext-security.xml"
		,"/com/edgenius/wiki/applicationContext-quartz.xml"
		,"/com/edgenius/core/applicationContext-cache.xml"
		,"/com/edgenius/core/applicationContext-core-service.xml"
		})
public class TestSearchService{

	@Autowired
	private SearchService searchService;
	
	@Autowired
	private UserSearchService userSearchService;
	
	@Autowired
	private WidgetSpaceSearchService widgetSpaceSearchService;
	
	@Autowired
	private SpaceSearchService spaceSearchService;
	
	@Autowired
	private RoleSearchService roleSearchService;
	
	@Autowired
	private IndexService indexService;
	
	@Autowired
	private RepositoryService repositoryService;
	
	private Date now = new Date();
	private Date now2 = DateUtils.addDays(new Date(), 1);
	
	@Before
	public void setUp() throws Exception {
		indexService.cleanIndexes(null);
	}
	
	@Test
	public void testUserSearchService() throws SearchException{
		//insert
		User user = new User();
		user.setUsername("user1");
		user.setFullname("userI fullname");
		user.setCreatedDate(now);
		indexService.saveOrUpdateUser(user);
		
		SearchResult rs = searchService.search("userI", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		
		//update
		user = new User();
		user.setUsername("user1");
		user.setFullname("userI new fullname");
		user.setCreatedDate(now);
		indexService.saveOrUpdateUser(user);
		
		rs = searchService.search("userI", 0	, Integer.MAX_VALUE, TestUtil.getAdminUser());
		
		Assert.assertEquals(1, rs.getTotalItem());
		SearchResultItem item = rs.getItems().get(0);
		Assert.assertEquals(user.getUsername(), item.getTitle());
		Assert.assertEquals(user.getFullname(), item.getContributor());
		Assert.assertEquals(user.getCreatedDate().getTime()+"", item.getDatetime());
		
		
		//update - case sensitive for user name
		user = new User();
		user.setUsername("USER1");
		user.setFullname("userI new upper fullname");
		user.setCreatedDate(now);
		indexService.saveOrUpdateUser(user);
		
		rs = searchService.search("userI", 0	, Integer.MAX_VALUE, TestUtil.getAdminUser());
		
		Assert.assertEquals(1, rs.getTotalItem());
		item = rs.getItems().get(0);
		Assert.assertEquals(user.getUsername(), item.getTitle());
		Assert.assertEquals(user.getFullname(), item.getContributor());
		Assert.assertEquals(user.getCreatedDate().getTime()+"", item.getDatetime());
		
		//test userSearchService
		userSearchService.searchUser("uSERi", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		
		//add new 
		user = new User();
		user.setUsername("user2");
		user.setFullname("userI fullname");
		user.setCreatedDate(now);
		indexService.saveOrUpdateUser(user);
		
		rs = searchService.search("fullname", 0 , Integer.MAX_VALUE, TestUtil.getAdminUser());
		
		Assert.assertEquals(2, rs.getTotalItem());
		
		//test remove
		indexService.removeUser("user1");
		rs = searchService.search("userI", 0 , Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
	}
	
	
	@Test
	public void testSpaceSearchService() throws SearchException{
		//insert
		Space space = new Space();
		space.setUnixName("space1");
		space.setName("spaceI space1 name");
		space.setDescription("space1 desc");
		space.setCreatedDate(now);
		indexService.saveOrUpdateSpace(space);
		
		SearchResult rs = searchService.search("spaceI", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		
		//update - case sensitive
		space = new Space();
		space.setUnixName("SPACE1");
		space.setName("spaceI space1 new name ");
		space.setDescription("space1 new desc");
		space.setCreatedDate(now);
		indexService.saveOrUpdateSpace(space);
		
		rs = searchService.search("spaceI", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		SearchResultItem item = rs.getItems().get(0);
		Assert.assertEquals(1, rs.getTotalItem());
		Assert.assertEquals(space.getUnixName(), item.getSpaceUname());
		Assert.assertEquals(space.getName(), item.getTitle());
		Assert.assertEquals(space.getDescription(), item.getDesc());
		Assert.assertEquals(space.getCreatedDate().getTime()+"", item.getDatetime());
		
		//test spaceSearchService
		spaceSearchService.searchSpace("SPacEi", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		//test widgetSpaceSearchService
		widgetSpaceSearchService.search("SPacEi", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		
		//add 
		space = new Space();
		space.setUnixName("SPACE2");
		space.setName("spaceI space2 name");
		space.setDescription("space2 desc");
		space.setCreatedDate(now);
		indexService.saveOrUpdateSpace(space);
		rs = searchService.search("spaceI", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(2, rs.getTotalItem());
		
		
		//test remove
		indexService.removeSpace("space1");
		rs = searchService.search("spaceI", 0 , Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
	}
	
	
	@Test
	public void testPageSearchService() throws SearchException{
		Page page = new Page();
		page.setUid(1);
		page.setPageUuid("puuid1");
		page.setTitle("pageI page1 title");
		PageContent content = new PageContent();
		content.setContent("content1");
		page.setContent(content);
		Space space = new Space();
		space.setUid(2);
		space.setUnixName(TestDataConstants.spaceUname1);
		page.setSpace(space);
		page.setCreatedDate(now);
		page.setModifiedDate(now2);
		indexService.saveOrUpdatePage(page);
		
		SearchResult rs = searchService.search("pageI", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		SearchResultItem item = rs.getItems().get(0);
		Assert.assertEquals(page.getTitle(), item.getTitle());
		//Assert.assertEquals(page.getContent().getContent(), item.getDesc());
		Assert.assertEquals(page.getModifiedDate().getTime()+"", item.getDatetime());
		
		//update
		page = new Page();
		page.setUid(1);
		page.setPageUuid("PUUID1");
		page.setTitle("pageI page1 new title");
		content = new PageContent();
		content.setContent("content1 new ");
		page.setContent(content);
		page.setSpace(space);
		page.setCreatedDate(now);
		page.setModifiedDate(now2);
		indexService.saveOrUpdatePage(page);
		
		rs = searchService.search("pageI", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		
		//add
		page = new Page();
		page.setUid(2);
		page.setPageUuid("puuid2");
		page.setTitle("pageI page2 new title");
		content = new PageContent();
		content.setContent("content2 new ");
		page.setContent(content);
		page.setSpace(space);
		page.setCreatedDate(now);
		page.setModifiedDate(now2);
		indexService.saveOrUpdatePage(page);
		
		rs = searchService.search("PAGeI", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(2, rs.getTotalItem());
		
		
		//test remove
		indexService.removePage("PUUID1");
		rs = searchService.search("pageI", 0 , Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
	}
	
	
	@Test
	public void testPageTagSearchService() throws SearchException{
		PageTag tag = new PageTag();
		tag.setName("ptagI");
		tag.setCreatedDate(now);
		Space space = new Space();
		space.setUid(2);
		space.setUnixName(TestDataConstants.spaceUname1);
		tag.setSpace(space);
		indexService.saveOrUpdatePageTag(tag);
		
		SearchResult rs = searchService.search("ptagI", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		SearchResultItem item = rs.getItems().get(0);
		Assert.assertEquals(tag.getName(), item.getTitle());
		
		//update
		tag = new PageTag();
		tag.setName("PTAGI");
		tag.setCreatedDate(now);
		tag.setSpace(space);
		indexService.saveOrUpdatePageTag(tag);
		
		rs = searchService.search("ptagI", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());

		//remove
		indexService.removePageTag("ptagI");
		rs = searchService.search("ptagI", 0 , Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(0, rs.getTotalItem());

	}
	
	@Test
	public void testSpaceTagSearchService() throws SearchException{
		SpaceTag tag = new SpaceTag();
		tag.setName("stagI");
		tag.setCreatedDate(now);
		indexService.saveOrUpdateSpaceTag(tag);
		
		SearchResult rs = searchService.search("stagI", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		SearchResultItem item = rs.getItems().get(0);
		Assert.assertEquals(tag.getName(), item.getTitle());
		
		//update
		tag = new SpaceTag();
		tag.setName("STAGI");
		tag.setCreatedDate(now);
		indexService.saveOrUpdateSpaceTag(tag);
		
		rs = searchService.search("stagI", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		
		//remove
		indexService.removeSpaceTag("stagI");
		rs = searchService.search("stagI", 0 , Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(0, rs.getTotalItem());
	
		
	}
	

	@Test
	public void testAttachmentSearchService() throws SearchException, RepositoryException, IOException, RepositoryTiemoutExcetpion, RepositoryQuotaException{
		FileNode node1 = setupRepository();
		
		InputStream is = FileUtil.getFileInputStream("classpath:testcase/attachments/test1.txt");
		String text1 = IOUtils.toString(is);
		IOUtils.closeQuietly(is);
		String comment1 = "attach2";
		text1 = replaceToHighlighter(StringUtil.join(" ", comment1, text1), "hello");
		//add
		indexService.saveOrUpdateAttachment(TestDataConstants.spaceUname1, node1, false);
		
		SearchResult rs = searchService.search("hello", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		SearchResultItem item = rs.getItems().get(0);
		Assert.assertEquals(node1.getComment(), item.getDesc());
		
		//only update metadata
		node1.setFilename("text2.txt");
		node1.setComment(comment1);
		indexService.saveOrUpdateAttachment(TestDataConstants.spaceUname1, node1, true);
		
		rs = searchService.search("HELLO", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		item = rs.getItems().get(0);
		Assert.assertEquals(node1.getFilename(), item.getTitle());  
		Assert.assertEquals(node1.getComment(), item.getDesc());

		//check file content still there
		rs = searchService.search("hello", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		item = rs.getItems().get(0);
		Assert.assertEquals(text1, item.getFragment());
		
		tearupRepository();
	}

	@Test
	public void testRoleSearchService() throws SearchException{
	
		Role role = new Role();
		role.setDescription("role desc1");
		role.setName("ROLE1");
		role.setDisplayName("role display name1");
		role.setCreatedDate(now);
		indexService.saveOrUpdateRole(role);
		
		SearchResult  rs = roleSearchService.searchRole("name1", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		SearchResultItem item = rs.getItems().get(0);
		Assert.assertEquals(role.getDescription(), item.getDesc());
		Assert.assertEquals(role.getCreatedDate().getTime()+"", item.getDatetime());
		
		//update
		role.setDisplayName("roLE display name2");
		indexService.saveOrUpdateRole(role);
		rs = roleSearchService.searchRole("ROLE", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		
		//add
		role.setName("ROLE2");
		indexService.saveOrUpdateRole(role);
		rs = roleSearchService.searchRole("RolE", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(2, rs.getTotalItem());
		
	}
	@Test
	public void testWidgetSearchService() throws SearchException{
		Widget widget = new Widget();
		widget.setUuid(TestDataConstants.widgetUuid1);
		widget.setCreator(TestUtil.getAdminUser());
		widget.setShared(true);
		widget.setTitle("widget title1");
		widget.setDescription("widget desc1");
		widget.setContent("widget content1");
		widget.setCreatedDate(now);
		widget.setType(WidgetModel.TYPE_MARKUP_RENDER);
		indexService.saveOrUpdateWidget(widget);
		
		SearchResult rs = searchService.search("title1", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		
		//update
		widget.setTitle("WIDget title2");
		indexService.saveOrUpdateWidget(widget);
		
		rs = searchService.search("WIDGET", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		SearchResultItem item = rs.getItems().get(0);
		Assert.assertEquals(widget.getTitle(), item.getTitle());
		
		
		//test widgetSpaceSearchService
		widgetSpaceSearchService.search("WiDGet", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		
		//delete
		indexService.removeWidget(TestDataConstants.widgetUuid1);
		rs = searchService.search("TITLE2", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(0, rs.getTotalItem());
		
	}

	@Test
	public void testCommentSearchService() throws SearchException{
		PageComment comment = new PageComment();
		comment.setUid(1);
		comment.setBody("comment body1");
		comment.setCreatedDate(now);
		Page page = new Page();
		page.setUid(1);
		page.setPageUuid(TestDataConstants.pageUuid1);
		page.setTitle(TestDataConstants.pageTitle1);
		Space space = new Space();
		space.setUnixName(TestDataConstants.spaceUname1);
		page.setSpace(space);
		comment.setPage(page);
		indexService.saveOrUpdateComment(comment);
		
		SearchResult  rs = searchService.search("COMMENT", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		SearchResultItem item = rs.getItems().get(0);
		Assert.assertEquals(TestDataConstants.pageTitle1, item.getTitle());
		Assert.assertEquals(replaceToHighlighter(comment.getBody(), "comment"), item.getFragment());
		
		//update
		comment.setBody("comment body2");
		indexService.saveOrUpdateComment(comment);
		rs = searchService.search("COMMENT", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		item = rs.getItems().get(0);
		Assert.assertEquals(TestDataConstants.pageTitle1, item.getTitle());
		Assert.assertEquals(replaceToHighlighter(comment.getBody(), "comment"), item.getFragment());
		
		//add
		comment.setUid(2);
		comment.setBody("COMMENT body3");
		indexService.saveOrUpdateComment(comment);
		rs = searchService.search("comment", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(2, rs.getTotalItem());
		
		//remove
		indexService.removeComment(1);
		rs = searchService.search("COMMENT", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		
	}


	@Test
	public void testTagMixService() throws SearchException{
		//add space tag
		SpaceTag stag = new SpaceTag();
		stag.setName("tag");
		stag.setCreatedDate(now);
		indexService.saveOrUpdateSpaceTag(stag);
		
		SearchResult  rs = searchService.search("tag", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		
		//add page tag
		PageTag ptag = new PageTag();
		ptag.setName("tag");
		ptag.setCreatedDate(now);
		Space space = new Space();
		space.setUid(2);
		space.setUnixName(TestDataConstants.spaceUname1);
		ptag.setSpace(space);
		indexService.saveOrUpdatePageTag(ptag);
		rs = searchService.search("TAG", 0, Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(2, rs.getTotalItem());
		
		indexService.removeSpaceTag("TAG");
		rs = searchService.search("tag", 0 , Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(1, rs.getTotalItem());
		
		indexService.removePageTag("TAG");
		rs = searchService.search("tag", 0 , Integer.MAX_VALUE, TestUtil.getAdminUser());
		Assert.assertEquals(0, rs.getTotalItem());
		
	}
	

	private FileNode setupRepository() throws RepositoryException, IOException, RepositoryTiemoutExcetpion,
			RepositoryQuotaException {
		//only update attachment in test page 2(puuid2) - the page 1 is used for image attachment test in RenderService test etc.
		ITicket ticket = repositoryService.login(TestDataConstants.spaceUname1, TestDataConstants.spaceUname1, TestDataConstants.spaceUname1);
		List<FileNode> list = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_ATTACHMENT, TestDataConstants.pageUuid2, false);
		for (FileNode fileNode : list) {
			try {
				repositoryService.removeFile(ticket, fileNode.getNodeUuid(), fileNode.getVersion());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		FileNode node1 = new FileNode();
		node1.setComment("attach1");
		node1.setFilename("temp-test1.txt");
		node1.setContentType("text/plain");
		node1.setCreateor("admin");
		node1.setType(RepositoryService.TYPE_ATTACHMENT);
		node1.setIdentifier(TestDataConstants.pageUuid2);
		node1.setDate(now.getTime());
		node1.setStatus(0);
		node1.setFile(FileUtil.getFileInputStream("classpath:testcase/attachments/test1.txt"));
		repositoryService.saveFile(ticket, node1, false,false);
		return node1;
	}
	
	private void tearupRepository() throws RepositoryException{
		ITicket ticket = repositoryService.login(TestDataConstants.spaceUname1, TestDataConstants.spaceUname1, TestDataConstants.spaceUname1);
		List<FileNode> list = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_ATTACHMENT, TestDataConstants.pageUuid2, false);
		for (FileNode fileNode : list) {
			try {
				repositoryService.removeFile(ticket, fileNode.getNodeUuid(), fileNode.getVersion());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private String replaceToHighlighter(String body, String key) {
		return body.replace(key, "<span class=\"highlighter\">"+key+"</span>");
	}
}
