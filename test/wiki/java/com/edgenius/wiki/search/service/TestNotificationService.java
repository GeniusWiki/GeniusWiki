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

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

import com.edgenius.wiki.service.impl.NotificationServiceImpl;

/**
 * @author Dapeng.Ni
 */
public class TestNotificationService  extends TestCase{

//	@Autowired
//	private NotificationService notificationService;

	@Test
	public void testTwitterMessagePattern(){
		NotificationServiceImpl impl = new NotificationServiceImpl();
		
		//normal one
		StringBuilder buf = new StringBuilder("@receiver1 msg word2 and @my message");
		List<String> receivers = impl.parseReceivers(buf);
		Assert.assertEquals(1,receivers.size());
		Assert.assertEquals("@receiver1",receivers.get(0));
		Assert.assertEquals("msg word2 and @my message",buf.toString());
		
		//case 1
		buf = new StringBuilder("@receiver1 @receiver2 @receiver3 msg word2 and @my message");
		receivers = impl.parseReceivers(buf);
		Assert.assertEquals(3, receivers.size());
		Assert.assertEquals("@receiver1",receivers.get(0));
		Assert.assertEquals("@receiver2",receivers.get(1));
		Assert.assertEquals("@receiver3",receivers.get(2));
		Assert.assertEquals("msg word2 and @my message",buf.toString());
		
		//'space name'
		buf = new StringBuilder("@receiver1 @'receiver name' @@receiver3 msg word2 and @my message");
		receivers = impl.parseReceivers(buf);
		Assert.assertEquals(3, receivers.size());
		Assert.assertEquals("@receiver1",receivers.get(0));
		Assert.assertEquals("@'receiver name'",receivers.get(1));
		Assert.assertEquals("@@receiver3",receivers.get(2));
		Assert.assertEquals("msg word2 and @my message",buf.toString());
		
		//'space name'
		buf = new StringBuilder("@receiver1  @@'receiver name' @@receiver3 msg word2 and @my message");
		receivers = impl.parseReceivers(buf);
		Assert.assertEquals(3, receivers.size());
		Assert.assertEquals("@receiver1",receivers.get(0));
		Assert.assertEquals("@@'receiver name'",receivers.get(1));
		Assert.assertEquals("@@receiver3",receivers.get(2));
		Assert.assertEquals("msg word2 and @my message",buf.toString());
		
		//forget '
		buf = new StringBuilder("@receiver1 @@'receiver name @@receiver3 msg word2 and @my message");
		receivers = impl.parseReceivers(buf);
		Assert.assertEquals(2, receivers.size());
		Assert.assertEquals("@receiver1",receivers.get(0));
		Assert.assertEquals("@@'receiver name @@receiver3 msg word2 and @my message",receivers.get(1));
		Assert.assertEquals("",buf.toString());
		
		//no @
		buf = new StringBuilder("receiver1 @@'receiver name @@receiver3 msg word2 and @my message");
		receivers = impl.parseReceivers(buf);
		Assert.assertEquals(0, receivers.size());
		Assert.assertEquals("receiver1 @@'receiver name @@receiver3 msg word2 and @my message",buf.toString());
		
		//has ' inside receiver name
		buf = new StringBuilder("@receiv'er1 @@'receiver name' @@receiver3 msg word2 and @my message");
		receivers = impl.parseReceivers(buf);
		Assert.assertEquals(3, receivers.size());
		Assert.assertEquals("@receiv'er1",receivers.get(0));
		Assert.assertEquals("@@'receiver name'",receivers.get(1));
		Assert.assertEquals("@@receiver3",receivers.get(2));
		Assert.assertEquals("msg word2 and @my message",buf.toString());
		
		//first space after @
		buf = new StringBuilder("@ receiv'er1   @@'receiver name' @@receiver3 msg word2 and @my message");
		receivers = impl.parseReceivers(buf);
		Assert.assertEquals(1, receivers.size());
		Assert.assertEquals("@",receivers.get(0));
		Assert.assertEquals("receiv'er1   @@'receiver name' @@receiver3 msg word2 and @my message",buf.toString());
		
		//first space after @
		buf = new StringBuilder("@ @ @");
		receivers = impl.parseReceivers(buf);
		Assert.assertEquals(3, receivers.size());
		Assert.assertEquals("@",receivers.get(0));
		Assert.assertEquals("@",receivers.get(1));
		Assert.assertEquals("@",receivers.get(2));
		Assert.assertEquals("",buf.toString());

	
	}
}
