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

import java.io.Serializable;
import java.util.Map;

import org.springframework.mail.SimpleMailMessage;

/**
 * @author Dapeng.Ni
 */
public class MailMQObject implements Serializable{
	public static final int PURE_TEXT = 0;
	public static final int PURE_HTML = 1;
	public static final int JUST_SEND = 2;
	//HTML or PureText
	private int type;
	private SimpleMailMessage message;
	private String templateName;
	private Map<String,String> valueMap;
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public SimpleMailMessage getMessage() {
		return message;
	}
	public void setMessage(SimpleMailMessage message) {
		this.message = message;
	}
	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	public Map<String, String> getValueMap() {
		return valueMap;
	}
	public void setValueMap(Map<String, String> valueMap) {
		this.valueMap = valueMap;
	}
	
	
}
