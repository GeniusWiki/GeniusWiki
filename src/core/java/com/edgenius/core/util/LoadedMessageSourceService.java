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
package com.edgenius.core.util;
import java.util.HashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

/** 
 * Access a message service related to a programatically loaded message file.
 * Authoring uses this to access the message files for tools and activities.
 */ 
public class LoadedMessageSourceService implements ILoadedMessageSourceService, BeanFactoryAware {

	private static final String LOADED_MESSAGE_SOURCE_BEAN = "loadedMessageSource";
	private HashMap<String,MessageSource> messageServices = new HashMap<String,MessageSource>();
	private BeanFactory beanFactory = null;

	/* (non-Javadoc)
	 * @see org.lamsfoundation.lams.authoring.service.ILoadMessageService#getMessageService(java.lang.String)
	 */
	public MessageSource getMessageService(String messageFilename) {
		if ( messageFilename != null ) {
			MessageSource ms = messageServices.get(messageFilename);
			if ( ms == null ) {
				ResourceBundleMessageSource rbms = (ResourceBundleMessageSource) beanFactory.getBean(LOADED_MESSAGE_SOURCE_BEAN);
				rbms.setBasename(messageFilename);
				messageServices.put(messageFilename,rbms);
				ms = rbms;
			}
			return ms;
		} else {
			return null;
		}
	}
	
	/* **** Method for BeanFactoryAware interface *****************/
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;	
	}

}
