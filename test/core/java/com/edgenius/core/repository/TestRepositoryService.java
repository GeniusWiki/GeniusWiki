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
package com.edgenius.core.repository;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.edgenius.core.util.FileUtil;
import com.edgenius.test.TestDataConstants;

/**
 * @author Dapeng.Ni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/testAplicationContext-services.xml"
		,"/com/edgenius/core/applicationContext-core-orm.xml"
		,"/com/edgenius/wiki/applicationContext-security.xml"
		,"/com/edgenius/wiki/applicationContext-orm.xml"
		,"/com/edgenius/wiki/applicationContext-service.xml"
		,"/com/edgenius/core/applicationContext-cache.xml"
		,"/com/edgenius/core/applicationContext-core-service.xml"
		})
public class TestRepositoryService {

	@Autowired
	private RepositoryService repositoryService;
	@Before
	public void setUp() throws RepositoryException{
		cleanRepository();
	}
	@After
	public void tearDown() throws RepositoryException{
		cleanRepository();
	}
	private void cleanRepository() throws RepositoryException{
		ITicket ticket = repositoryService.login(TestDataConstants.spaceUname1, TestDataConstants.spaceUname1, TestDataConstants.spaceUname1);
		List<FileNode> list = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_ATTACHMENT, TestDataConstants.pageUuid3, false);
		for (FileNode fileNode : list) {
			try {
				repositoryService.removeFile(ticket, fileNode.getNodeUuid(), fileNode.getVersion());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void testSave() throws IOException, RepositoryException, RepositoryTiemoutExcetpion, RepositoryQuotaException{
		
		FileNode node1 = new FileNode();
		node1.setComment("attach1");
		node1.setFilename("attachment-test1.txt");
		node1.setContentType("text/plain");
		node1.setCreateor("admin");
		node1.setType(RepositoryService.TYPE_ATTACHMENT);
		node1.setIdentifier(TestDataConstants.pageUuid3);
		node1.setDate(new Date().getTime());
		node1.setStatus(0);
		node1.setFile(FileUtil.getFileInputStream("classpath:testcase/attachments/test1.txt"));
		
		ITicket ticket = repositoryService.login(TestDataConstants.spaceUname1, TestDataConstants.spaceUname1, TestDataConstants.spaceUname1);
		repositoryService.saveFile(ticket, node1, true, true);
		
		List<FileNode> nodes = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_ATTACHMENT, TestDataConstants.pageUuid3, false);
		Assert.assertEquals(1,nodes.size()); 
		
		//upload with same MD5 - hope it ignore
		FileNode node2 = new FileNode();
		node2.setComment("attach2");
		node2.setFilename("attachment-test1.txt");
		node2.setContentType("text/plain");
		node2.setCreateor("admin");
		node2.setType(RepositoryService.TYPE_ATTACHMENT);
		node2.setIdentifier(TestDataConstants.pageUuid3);
		node2.setDate(new Date().getTime());
		node2.setStatus(0);
		node2.setFile(FileUtil.getFileInputStream("classpath:testcase/attachments/test1.txt"));
		
		repositoryService.saveFile(ticket, node2, true, true);
		nodes = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_ATTACHMENT, TestDataConstants.pageUuid3, false);
		Assert.assertEquals(1,nodes.size()); 
		Assert.assertEquals("attach1",nodes.get(0).getComment());
		
		//upload but differ MD5 - checked-in
		FileNode node3 = new FileNode();
		node3.setComment("attach3");
		node3.setFilename("attachment-test1.txt");
		node3.setContentType("text/plain");
		node3.setCreateor("admin");
		node3.setType(RepositoryService.TYPE_ATTACHMENT);
		node3.setIdentifier(TestDataConstants.pageUuid3);
		node3.setDate(new Date().getTime());
		node3.setStatus(0);
		node3.setFile(FileUtil.getFileInputStream("classpath:testcase/attachments/test2.txt"));
		
		repositoryService.saveFile(ticket, node3, true, true);
		nodes = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_ATTACHMENT, TestDataConstants.pageUuid3, false);
		Assert.assertEquals(2,nodes.size()); 
		Assert.assertEquals("attach3",nodes.get(0).getComment());
		
		//upload another but same MD5 - what again text2? hope ignore
		FileNode node4 = new FileNode();
		node4.setComment("attach4");
		node4.setFilename("attachment-test1.txt");
		node4.setContentType("text/plain");
		node4.setCreateor("admin");
		node4.setType(RepositoryService.TYPE_ATTACHMENT);
		node4.setIdentifier(TestDataConstants.pageUuid3);
		node4.setDate(new Date().getTime());
		node4.setStatus(0);
		node4.setFile(FileUtil.getFileInputStream("classpath:testcase/attachments/test2.txt"));
		
		repositoryService.saveFile(ticket, node4, true, true);
		nodes = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_ATTACHMENT, TestDataConstants.pageUuid3, false);
		Assert.assertEquals(2,nodes.size()); 
		Assert.assertEquals("attach3",nodes.get(0).getComment());
		
		//upload same MD5 but force save - hope save
		FileNode node5 = new FileNode();
		node5.setComment("attach5");
		node5.setFilename("attachment-test1.txt");
		node5.setContentType("text/plain");
		node5.setCreateor("admin");
		node5.setType(RepositoryService.TYPE_ATTACHMENT);
		node5.setIdentifier(TestDataConstants.pageUuid3);
		node5.setDate(new Date().getTime());
		node5.setStatus(0);
		node5.setFile(FileUtil.getFileInputStream("classpath:testcase/attachments/test2.txt"));
		
		repositoryService.saveFile(ticket, node5, true, false);
		nodes = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_ATTACHMENT, TestDataConstants.pageUuid3, false);
		Assert.assertEquals(3,nodes.size()); 
		Assert.assertEquals("attach5",nodes.get(0).getComment());
		
		//upload another but diff MD5 - but like version 1. Hope checkin
		FileNode node6 = new FileNode();
		node6.setComment("attach6");
		node6.setFilename("attachment-test1.txt");
		node6.setContentType("text/plain");
		node6.setCreateor("admin");
		node6.setType(RepositoryService.TYPE_ATTACHMENT);
		node6.setIdentifier(TestDataConstants.pageUuid3);
		node6.setDate(new Date().getTime());
		node6.setStatus(0);
		node6.setFile(FileUtil.getFileInputStream("classpath:testcase/attachments/test1.txt"));
		
		repositoryService.saveFile(ticket, node6, true, true);
		nodes = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_ATTACHMENT, TestDataConstants.pageUuid3, false);
		Assert.assertEquals(4,nodes.size()); 
		Assert.assertEquals("attach6",nodes.get(0).getComment());
		
		//upload another but don't validate MD5 - check-in
		FileNode node7 = new FileNode();
		node7.setComment("attach7");
		node7.setFilename("attachment-test1.txt");
		node7.setContentType("text/plain");
		node7.setCreateor("admin");
		node7.setType(RepositoryService.TYPE_ATTACHMENT);
		node7.setIdentifier(TestDataConstants.pageUuid3);
		node7.setDate(new Date().getTime());
		node7.setStatus(0);
		node7.setFile(FileUtil.getFileInputStream("classpath:testcase/attachments/test1.txt"));
		
		repositoryService.saveFile(ticket, node7, false, false);
		nodes = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_ATTACHMENT, TestDataConstants.pageUuid3, false);
		Assert.assertEquals(5,nodes.size()); 
		Assert.assertEquals("attach7",nodes.get(0).getComment());
		
		
		
	}
}
