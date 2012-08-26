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
package com.edgenius.wiki.webapp.servlet;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.Queue;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.edgenius.core.Version;
import com.edgenius.core.webapp.BaseServlet;
import com.edgenius.wiki.service.NotifyMQObject;
import com.edgenius.wiki.service.SpaceService;

/**
 * System status check servlet - most for hosting service to check if this webapp is running.
 * @author Dapeng.Ni
 */
public class StatusServlet extends BaseServlet {
	private static final long serialVersionUID = 6556291757098976964L;
	private transient final Logger log = LoggerFactory.getLogger(StatusServlet.class);
	
	private Queue notifyQueue;
	private JmsTemplate jmsTemplate;
	
	private Lock jmsAckLocker = new ReentrantLock();
	private Condition jmsAckCondition = jmsAckLocker.newCondition();
	private String uuid;
	
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		String jmsAckUuid = request.getParameter("uuid");
		if(StringUtils.isNotEmpty(jmsAckUuid)){
			//This is JMS confirm request - Please note, for simply reason, here does not check if the UUID is that was sent one. 
			jmsAckLocker.lock();
			try{
				jmsAckCondition.signalAll();
			} finally{
				jmsAckLocker.unlock();
			}
			response.getWriter().write("Received");
		}else{
			String running = null;
			try {
				if( getSpaceService() == null){
					running = "Get system service bean failed";
				}
				if ( getSpaceService().getSystemSpace() == null){
					running = "Get system sapce failed";
				}
				if(uuid == null){
					jmsTemplate = getJmsTemplate();
					notifyQueue = getJmsQueue();
					
					//wait JMS call back this servlet
					jmsAckLocker.lock();
					try{
						uuid = UUID.randomUUID().toString();
						NotifyMQObject message = new NotifyMQObject(NotifyMQObject.TYPE_SYSTEM_STATUS_CHECK, uuid);
						jmsTemplate.convertAndSend(notifyQueue, message);
						
						boolean ack = jmsAckCondition.await(10, TimeUnit.SECONDS);
						if(!ack){
							running = "JMS is not response in 10 seconds!";
						}
					} finally{
						uuid = null;
						jmsAckLocker.unlock();
					}
				}else{
					log.warn("Last status check thread is not completed yet, this request is ignored");
					running = "Last status check thread is not completed yet, this request is ignored";
				}
			} catch (Exception e) {
				uuid = null;
				log.error("System checking failed", e);
				running = "Exception on " + e.getMessage();
			}
			
			if(running == null)
				response.getWriter().write("<!--OK--><!--V1:"+Version.VERSION+"--><span style='color:#05BF54'>Running</span>");
			else{
				log.warn("System status check failed: {}", running);
				response.getWriter().write("<!--FAILED--><!--V1:"+Version.VERSION+"--><span style='color:#CC3300'>"+running+"</span>");
			}
		}
	}
	
	private Queue getJmsQueue(){
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
		return (Queue) ctx.getBean("notifyDestination");
	}
	private JmsTemplate getJmsTemplate(){
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
		return (JmsTemplate) ctx.getBean("jmsTemplate");
	}
	private SpaceService getSpaceService(){
		
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
		return (SpaceService) ctx.getBean(SpaceService.SERVICE_NAME);
	}

}
