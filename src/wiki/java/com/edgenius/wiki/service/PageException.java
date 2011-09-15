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
package com.edgenius.wiki.service;


/**
 * Page CRUD exception. 
 * This class is extends RuntimeException so it is able to roll back the transaction. 
 *
 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute.DefaultTransactionAttribute
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class PageException extends RuntimeException {

	/**
	 * 
	 */
	public PageException() {
		super();
		
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PageException(String message, Throwable cause) {
		super(message, cause);
		
	}

	/**
	 * @param message
	 */
	public PageException(String message) {
		super(message);
		
	}

	/**
	 * @param cause
	 */
	public PageException(Throwable cause) {
		super(cause);
		
	}

}
