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
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;

import com.edgenius.core.service.LocaleContextConfHolder;
import com.edgenius.core.service.MessageService;

/**
 * Service class to help Service bean get i18n value quickly. The locale information will get from <code>Global Local</code>
 * table. 
 * 
 * To spring default message service, this class won't throw exception if message doesn't exist in properties file, instead, 
 * it displays ???message.key??? format to warning message missing.
 *  
 * @author Dapeng.Ni
 * @see org.springframework.context.support.MessageSourceAccessor
 */
public class MessageServiceImpl implements MessageService {
	    private MessageSourceAccessor messageAccessor;
	    
	    /* (non-Javadoc)
		 * @see com.edgenius.core.service.impl.MessageService#setMessageSource(org.springframework.context.MessageSource)
		 */
	    public void setMessageSource(MessageSource messageSource){
	    	messageAccessor = new MessageSourceAccessor(messageSource);
	    }
	    
	    /* (non-Javadoc)
		 * @see com.edgenius.core.service.impl.MessageService#getMessage(java.lang.String)
		 */
	    public String getMessage(String key){
	    	String message;
	    	try {
	    		message = messageAccessor.getMessage(key,LocaleContextConfHolder.getLocale());
			} catch (NoSuchMessageException e) {
				message = "??" + key + "??";
			}
			return message;
	    }
	    /* (non-Javadoc)
		 * @see com.edgenius.core.service.impl.MessageService#getMessage(java.lang.String, java.lang.String)
		 */
	    //I delete this method as it is very easy to misuse - treat defaultMessage as message parameter...
//	    public String getMessage(String key, String defaultMessage){
//	    	String message = defaultMessage;
//	    	try {
//	    		message = messageAccessor.getMessage(key,defaultMessage,LocaleContextConfHolder.getLocale());
//			} catch (NoSuchMessageException e) {
//				message = defaultMessage;
//			}
//			return message;
//	    }
	    /* (non-Javadoc)
		 * @see com.edgenius.core.service.impl.MessageService#getMessage(java.lang.String, java.lang.Object[])
		 */
	    public String getMessage(String key, Object[] args){
	    	String message;
	    	try {
	    		message = messageAccessor.getMessage(key,args,LocaleContextConfHolder.getLocale());
			} catch (NoSuchMessageException e) {
				message = "??" + key + "??";
			}
			return message;
	    }
	    /* (non-Javadoc)
		 * @see com.edgenius.core.service.impl.MessageService#getMessage(java.lang.String, java.lang.Object[], java.lang.String)
		 */
	    public String getMessage(String key, Object[] args, String defaultMessage){
	    	String message = defaultMessage;
	    	try {
	    		message = messageAccessor.getMessage(key,args,defaultMessage,LocaleContextConfHolder.getLocale());
			} catch (NoSuchMessageException e) {
				message = defaultMessage;
			}
			return message;
	    }

		public String getMessage(String key, String arg) {
			return getMessage(key, new String[]{arg});
		}
}
