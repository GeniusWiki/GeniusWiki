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
package com.edgenius.core.service.impl;

import java.util.Map;
import java.util.Set;

import javax.jms.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mail.SimpleMailMessage;

import com.edgenius.core.Global;
import com.edgenius.core.service.MailService;
import com.edgenius.core.service.UserReadingService;
/**
 * 
 * @author Dapeng.Ni
 */
public class MailMQProducer implements MailService{
	protected final Logger log = LoggerFactory.getLogger(MailMQProducer.class);
	private JmsTemplate template;
	private Queue destination;
	private UserReadingService userReadingService;
	
	public void send(SimpleMailMessage msg) {
		MailMQObject mail = new MailMQObject();
		mail.setType(MailMQObject.JUST_SEND);
		mail.setMessage(msg);
		sendMQ(mail);

	}

	public void sendHtmlMail(SimpleMailMessage msg, String templateName, Map model) {
		MailMQObject mail = new MailMQObject();
		mail.setType(MailMQObject.PURE_HTML);
		mail.setTemplateName(templateName);
		mail.setValueMap(model);
		mail.setMessage(msg);
		sendMQ(mail);
	}

	public void sendPlainMail(SimpleMailMessage msg, String templateName, Map model) {
		MailMQObject mail = new MailMQObject();
		mail.setType(MailMQObject.PURE_TEXT);
		mail.setTemplateName(templateName);
		mail.setValueMap(model);
		mail.setMessage(msg);
		sendMQ(mail);
	
	}
	
    @Override
    public void sendPlainToSystemAdmins(String templateName, Map model){
        Set<String> mailAddrList =userReadingService.getSystemAdminMailList();
        for (String addr : mailAddrList) {
            try {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setFrom(Global.DefaultNotifyMail);
                mail.setTo(addr); 
                this.sendPlainMail(mail, templateName,model);
            } catch (Exception e) {
                log.error("Failed send email to system admin:" + addr,e);
            }
        }
        
    }

	//********************************************************************
	//               private 
	//********************************************************************
	private void sendMQ(MailMQObject message){
		try{
			template.convertAndSend(destination, message);	
		} catch (Exception e) {
			log.error("Send mail failed on " + e.toString(),e);
		}
	}
	//********************************************************************
	//               Get / Set
	//********************************************************************
	public void setTemplate(JmsTemplate template) {
		this.template = template;
	}

	public void setDestination(Queue destination) {
		this.destination = destination;
	}

    public void setUserReadingService(UserReadingService userReadingService) {
        this.userReadingService = userReadingService;
    }



}
