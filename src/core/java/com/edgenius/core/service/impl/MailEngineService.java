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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.edgenius.core.Global;
import com.edgenius.core.service.MailService;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Mail engine to send email.
 */
public class MailEngineService implements MailService {
	private  static final Logger log = LoggerFactory.getLogger(MailEngineService.class);

	private static final String SUBJECT_KEY = "_subject";

	private FreeMarkerConfigurer mailTemplateEngine = null;
	private JavaMailSender mailSender;

	/* (non-Javadoc)
	 * @see com.edgenius.text.engine.service.impl.IMail#sendHtmlMail(org.springframework.mail.SimpleMailMessage, java.lang.String, java.util.Map)
	 */
	public void sendHtmlMail(final SimpleMailMessage msg, final String templateName, final Map model) {
		final String content = generateContent(templateName, model);
	
		final String subject = generateContent(getSubjectName(templateName), model);
		try{
			mailSender.send(new MimeMessagePreparator(){
				public void prepare(MimeMessage mimeMsg) throws Exception {
					MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true, "utf-8");
					helper.setTo(msg.getTo());
					helper.setFrom(msg.getFrom());
					if(msg.getBcc() != null)
						helper.setBcc(msg.getBcc());
					if(!StringUtils.isBlank(subject))
						helper.setSubject(subject);
					else
						helper.setSubject(msg.getSubject());
					helper.setText(content, true);
				}
				
			});
		} catch (Exception e) {
			log.error("Send HTML mail failed on {}",  e.toString(), e);
			log.info("Message subject: {}", subject);
			log.info("Message content: {}", content);
		}
	}



	/* (non-Javadoc)
	 * @see com.edgenius.text.engine.service.impl.IMail#sendPlainMail(org.springframework.mail.SimpleMailMessage, java.lang.String, java.util.Map)
	 */
	public void sendPlainMail(SimpleMailMessage msg, String templateName, Map model) {
		String content = generateContent(templateName, model);
		String subject = generateContent(getSubjectName(templateName), model);
		try{
			msg.setText(content);
			if(!StringUtils.isBlank(subject))
				msg.setSubject(subject);
			mailSender.send(msg);
		} catch (Exception e) {
			log.error("Send plain mail failed on {}" , e.toString(), e);
			log.info("Message subject: {}", subject);
			log.info("Message content: {}", content);
		}
	}

	/* (non-Javadoc)
	 * @see com.edgenius.text.engine.service.impl.IMail#send(org.springframework.mail.SimpleMailMessage)
	 */
	public void send(SimpleMailMessage msg) {
		try {
			mailSender.send(msg);
		} catch (MailException e) {
			log.error(e.getMessage(), e);
		}
	}

	//********************************************************************
	//               private method
	//********************************************************************
	/**
	 * @param templateName
	 * @return
	 */
	private String getSubjectName(final String templateName) {
		//assume template always has extension (so indexOf(.) doesn't return -1)
		String subjectTempl = new StringBuffer(templateName).insert(templateName.lastIndexOf("."), SUBJECT_KEY).toString();
		return subjectTempl;
	}
	
	private String generateContent(String templateName, Map map) {
		try {
			mailTemplateEngine.getConfiguration().setLocale(Global.getDefaultLocale());
			Template t = mailTemplateEngine.getConfiguration().getTemplate(templateName);
			return FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
		} catch (TemplateException e) {
			log.error("Error while processing FreeMarker template ", e);
		} catch (FileNotFoundException e) {
			log.error("Error while open template file ", e);
		} catch (IOException e) {
			log.error("Error while generate Email Content ", e);
		}
		return null;
	}
	
	//**************************************************************************
	// Set / Get 
	//**************************************************************************
	public void setMailTemplateEngine(FreeMarkerConfigurer mailTemplateEngine) {
		this.mailTemplateEngine = mailTemplateEngine;
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
}
